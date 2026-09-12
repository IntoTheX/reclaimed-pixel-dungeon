-- Reclaimed Pixel Dungeon v0.2.4 - Wayfarer account deletion requests
--
-- This migration adds a Google Play-compatible self-service deletion request
-- workflow. It intentionally DOES NOT delete auth.users directly from a public
-- RPC. Final deletion must be performed by trusted server-side/admin tooling
-- because Supabase Auth user deletion requires privileged credentials and
-- Wayfarer trade escrow may need to be settled safely first.

begin;

create table if not exists public.wayfarer_account_deletion_requests (
    request_id uuid primary key default gen_random_uuid(),
    user_id uuid references auth.users(id) on delete set null,
    status text not null default 'pending'
        check (status in ('pending', 'processing', 'blocked', 'completed', 'cancelled', 'failed')),
    request_source text not null default 'web'
        check (request_source in ('web', 'game')),
    requested_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    processing_at timestamptz,
    completed_at timestamptz,
    cancelled_at timestamptz,
    public_message text,
    internal_note text
);

create unique index if not exists wayfarer_account_deletion_one_active_uidx
    on public.wayfarer_account_deletion_requests(user_id)
    where user_id is not null
      and status in ('pending', 'processing', 'blocked');

create index if not exists wayfarer_account_deletion_status_idx
    on public.wayfarer_account_deletion_requests(status, requested_at);

alter table public.wayfarer_account_deletion_requests enable row level security;
revoke all on table public.wayfarer_account_deletion_requests from public, anon, authenticated;

-- Request deletion of the currently authenticated Wayfarer account.
-- The request is idempotent: if an active request already exists, the same
-- request is returned instead of creating duplicates.
--
-- Requesting deletion also immediately removes the account's live Wayfarer
-- presence so the account is no longer shown on the nearby-player map while
-- the request is awaiting final processing.
create or replace function public.wayfarer_request_account_deletion(
    requested_source text default 'web'
)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    caller uuid := auth.uid();
    existing_request public.wayfarer_account_deletion_requests;
    created_request public.wayfarer_account_deletion_requests;
begin
    if caller is null then
        raise exception 'Authentication required';
    end if;

    if requested_source not in ('web', 'game') then
        raise exception 'Invalid deletion request source';
    end if;

    select * into existing_request
    from public.wayfarer_account_deletion_requests request
    where request.user_id = caller
      and request.status in ('pending', 'processing', 'blocked')
    order by request.requested_at desc
    limit 1;

    if existing_request.request_id is not null then
        return jsonb_build_object(
            'request_id', existing_request.request_id,
            'status', existing_request.status,
            'requested_at', existing_request.requested_at,
            'updated_at', existing_request.updated_at,
            'message', coalesce(existing_request.public_message,
                'Your Wayfarer account deletion request is already active.')
        );
    end if;

    -- Live location/presence is ephemeral and should disappear immediately
    -- once an account deletion request is made.
    delete from public.wayfarer_presence
    where owner_id = caller;

    insert into public.wayfarer_account_deletion_requests
        (user_id, status, request_source, public_message)
    values
        (caller, 'pending', requested_source,
         'Your Wayfarer account deletion request has been received.')
    returning * into created_request;

    return jsonb_build_object(
        'request_id', created_request.request_id,
        'status', created_request.status,
        'requested_at', created_request.requested_at,
        'updated_at', created_request.updated_at,
        'message', created_request.public_message
    );
end;
$$;

-- Return the current account's most recent deletion request.
create or replace function public.wayfarer_account_deletion_status()
returns jsonb
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
    caller uuid := auth.uid();
    request public.wayfarer_account_deletion_requests;
begin
    if caller is null then
        raise exception 'Authentication required';
    end if;

    select * into request
    from public.wayfarer_account_deletion_requests row
    where row.user_id = caller
    order by row.requested_at desc
    limit 1;

    if request.request_id is null then
        return jsonb_build_object('status', 'none');
    end if;

    return jsonb_build_object(
        'request_id', request.request_id,
        'status', request.status,
        'requested_at', request.requested_at,
        'updated_at', request.updated_at,
        'completed_at', request.completed_at,
        'cancelled_at', request.cancelled_at,
        'message', coalesce(request.public_message, '')
    );
end;
$$;

-- A player may withdraw a request while it is still pending. Once trusted
-- deletion processing has begun, cancellation is no longer accepted.
create or replace function public.wayfarer_cancel_account_deletion()
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    caller uuid := auth.uid();
    request public.wayfarer_account_deletion_requests;
begin
    if caller is null then
        raise exception 'Authentication required';
    end if;

    select * into request
    from public.wayfarer_account_deletion_requests row
    where row.user_id = caller
      and row.status = 'pending'
    order by row.requested_at desc
    limit 1
    for update;

    if request.request_id is null then
        return jsonb_build_object(
            'cancelled', false,
            'message', 'There is no pending deletion request to cancel.'
        );
    end if;

    update public.wayfarer_account_deletion_requests
    set status = 'cancelled',
        cancelled_at = now(),
        updated_at = now(),
        public_message = 'The Wayfarer account deletion request was cancelled.'
    where request_id = request.request_id;

    return jsonb_build_object(
        'cancelled', true,
        'request_id', request.request_id,
        'status', 'cancelled',
        'message', 'The Wayfarer account deletion request was cancelled.'
    );
end;
$$;

-- Moderator/admin-facing queue. This does not expose email addresses and is
-- suitable for wiring into WayfarerAdmin later.
create or replace function public.wayfarer_admin_account_deletion_requests()
returns table (
    request_id uuid,
    user_id uuid,
    player_name text,
    status text,
    request_source text,
    requested_at timestamptz,
    updated_at timestamptz,
    public_message text
)
language plpgsql
stable
security definer
set search_path = ''
as $$
begin
    if not public.wayfarer_is_moderator() then
        raise exception 'Moderator access is required';
    end if;

    return query
    select
        request.request_id,
        request.user_id,
        coalesce(profile.player_name, 'Deleted Wayfarer'),
        request.status,
        request.request_source,
        request.requested_at,
        request.updated_at,
        request.public_message
    from public.wayfarer_account_deletion_requests request
    left join public.wayfarer_profiles profile
        on profile.user_id = request.user_id
    where request.status in ('pending', 'processing', 'blocked', 'failed')
    order by request.requested_at;
end;
$$;

-- Trusted processors can use this helper to determine whether deleting the
-- account now would strand Global Trade escrow or collide with moderator-only
-- records. It is intentionally unavailable to normal clients.
create or replace function public.wayfarer_account_deletion_blockers(
    requested_user_id uuid
)
returns jsonb
language plpgsql
stable
security definer
set search_path = ''
as $$
declare
    unresolved_trades bigint := 0;
    moderator_account boolean := false;
begin
    select count(*) into unresolved_trades
    from public.wayfarer_trades trade
    where (
        trade.sender in (
            select character.character_id
            from public.wayfarer_characters character
            where character.owner_id = requested_user_id
        )
        or trade.recipient in (
            select character.character_id
            from public.wayfarer_characters character
            where character.owner_id = requested_user_id
        )
    )
    and not (
        (trade.state = 'completed' and trade.sender_claimed and trade.recipient_claimed)
        or
        (trade.state = 'cancelled'
         and (not trade.sender_deposited or trade.sender_claimed)
         and (not trade.recipient_deposited or trade.recipient_claimed))
    );

    select exists (
        select 1 from public.wayfarer_moderators moderator
        where moderator.user_id = requested_user_id
    ) into moderator_account;

    return jsonb_build_object(
        'ready', unresolved_trades = 0 and not moderator_account,
        'unresolved_trades', unresolved_trades,
        'moderator_account', moderator_account
    );
end;
$$;

-- These two functions are for a future trusted server/Edge Function deletion
-- processor. They let that processor update the request without exposing the
-- queue table to public clients.
create or replace function public.wayfarer_mark_account_deletion_processing(
    requested_request_id uuid,
    requested_message text default null
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.wayfarer_account_deletion_requests
    set status = 'processing',
        processing_at = coalesce(processing_at, now()),
        updated_at = now(),
        public_message = coalesce(nullif(trim(requested_message), ''),
            'Your Wayfarer account deletion is being processed.'),
        internal_note = null
    where request_id = requested_request_id
      and status in ('pending', 'blocked', 'failed');
    return found;
end;
$$;

create or replace function public.wayfarer_mark_account_deletion_blocked(
    requested_request_id uuid,
    requested_message text,
    requested_internal_note text default null
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.wayfarer_account_deletion_requests
    set status = 'blocked',
        updated_at = now(),
        public_message = left(coalesce(nullif(trim(requested_message), ''),
            'The deletion request is temporarily blocked.'), 500),
        internal_note = left(coalesce(requested_internal_note, ''), 2000)
    where request_id = requested_request_id
      and status in ('pending', 'processing', 'blocked', 'failed');
    return found;
end;
$$;

create or replace function public.wayfarer_mark_account_deletion_completed(
    requested_request_id uuid
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.wayfarer_account_deletion_requests
    set status = 'completed',
        completed_at = now(),
        updated_at = now(),
        public_message = 'The Wayfarer online account has been deleted.',
        internal_note = null
    where request_id = requested_request_id
      and status <> 'completed';
    return found;
end;
$$;

create or replace function public.wayfarer_mark_account_deletion_failed(
    requested_request_id uuid,
    requested_internal_note text
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
begin
    update public.wayfarer_account_deletion_requests
    set status = 'failed',
        updated_at = now(),
        public_message = 'The deletion request could not be completed automatically and requires review.',
        internal_note = left(coalesce(requested_internal_note, ''), 2000)
    where request_id = requested_request_id
      and status <> 'completed';
    return found;
end;
$$;

revoke all on function public.wayfarer_request_account_deletion(text) from public;
revoke all on function public.wayfarer_account_deletion_status() from public;
revoke all on function public.wayfarer_cancel_account_deletion() from public;
revoke all on function public.wayfarer_admin_account_deletion_requests() from public;
revoke all on function public.wayfarer_account_deletion_blockers(uuid) from public;
revoke all on function public.wayfarer_mark_account_deletion_processing(uuid, text) from public;
revoke all on function public.wayfarer_mark_account_deletion_blocked(uuid, text, text) from public;
revoke all on function public.wayfarer_mark_account_deletion_completed(uuid) from public;
revoke all on function public.wayfarer_mark_account_deletion_failed(uuid, text) from public;

grant execute on function public.wayfarer_request_account_deletion(text) to authenticated;
grant execute on function public.wayfarer_account_deletion_status() to authenticated;
grant execute on function public.wayfarer_cancel_account_deletion() to authenticated;
grant execute on function public.wayfarer_admin_account_deletion_requests() to authenticated;

grant execute on function public.wayfarer_account_deletion_blockers(uuid) to service_role;
grant execute on function public.wayfarer_mark_account_deletion_processing(uuid, text) to service_role;
grant execute on function public.wayfarer_mark_account_deletion_blocked(uuid, text, text) to service_role;
grant execute on function public.wayfarer_mark_account_deletion_completed(uuid) to service_role;
grant execute on function public.wayfarer_mark_account_deletion_failed(uuid, text) to service_role;

notify pgrst, 'reload schema';
commit;
