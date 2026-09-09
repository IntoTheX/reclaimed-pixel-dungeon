/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.scenes.StartScene;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.windows.WndCharacterDeletion;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerName;
import com.watabou.utils.FileUtils;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public final class WayfarerAccountService {

	private static final String PROJECT_URL = "https://banqbcyyrlkwvyivbdfn.supabase.co";
	private static final String PUBLISHABLE_KEY = "sb_publishable_5wU9dOWG3zzejoEZ1ONvjQ_hKm2uc2K";
	private static final int CONNECT_TIMEOUT_MS = 10_000;
	private static final int READ_TIMEOUT_MS = 15_000;

	private static volatile Session session;
	private static volatile boolean busy;
	private static volatile Boolean moderatorAccount;
	private static volatile String registeredCharacterId = "";
	private static volatile String registeredCharacterName = "";
	private static volatile boolean characterNamePromptActive;
	private static WndCharacterDeletion deletionPrompt;
	private static DeletionTarget acknowledgedDeletion;
	private static volatile String deletionNotice;
	private static final String ENDED_CHARACTERS = "wayfarer_ended_characters";
	private static volatile long nextEndReport;
	private static boolean endReporting;
	private static int endReportCursor;
	private static final ExecutorService NETWORK_EXECUTOR = Executors.newSingleThreadExecutor( runnable -> {
		Thread thread = new Thread( runnable, "Wayfarer Network" );
		thread.setDaemon( true );
		return thread;
	} );

	private static void startNetworkTask( Thread task ) {
		try {
			NETWORK_EXECUTOR.execute( () -> {
				try {
					task.run();
				} catch (OutOfMemoryError error) {
					// Keep allocation pressure in a background request from terminating the game loop.
					recoverNetworkTaskState();
				} catch (RuntimeException error) {
					recoverNetworkTaskState();
					Game.reportException( error );
				}
			} );
		} catch (RejectedExecutionException | OutOfMemoryError error) {
			recoverNetworkTaskState();
		}
	}

	private static void recoverNetworkTaskState() {
		busy = false;
		endReporting = false;
		WayfarerPresenceService.networkTaskFailed();
		WayfarerModeratorRewards.networkTaskFailed();
	}

	public static synchronized void queueCharacterEnd( int slot, String reason ) {
		try {
			String id = FileUtils.bundleFromFile( GamesInProgress.gameFile( slot ) ).getString( "wayfarer_character_id" );
			if (id == null || id.isEmpty()) return;
			JsonValue queue = new JsonReader().parse( com.watabou.utils.GameSettings.getString( ENDED_CHARACTERS, "{}" ) );
			if (!queue.has( id )) queue.addChild( id, new JsonValue( reason ) );
			com.watabou.utils.GameSettings.put( ENDED_CHARACTERS, queue.toJson( JsonWriter.OutputType.json ) );
			nextEndReport = 0;
		} catch (Exception error) { Game.reportException( error ); }
	}

	public static synchronized void pollCharacterEnds() {
		if (endReporting || busy || !isSignedIn() || System.currentTimeMillis() < nextEndReport) return;
		nextEndReport = System.currentTimeMillis() + 60000;
		JsonValue queue;
		try { queue = new JsonReader().parse( com.watabou.utils.GameSettings.getString( ENDED_CHARACTERS, "{}" ) ); }
		catch (RuntimeException error) { Game.reportException( error ); return; }
		if (queue.size == 0) return;
		ArrayList<String> ids = new ArrayList<>();
		for (JsonValue entry = queue.child; entry != null; entry = entry.next) ids.add( entry.name );
		LinkedHashMap<String, String> batch = new LinkedHashMap<>();
		for (int n = 0; n < Math.min( 16, ids.size() ); n++) {
			String id = ids.get( (endReportCursor + n) % ids.size() );
			batch.put( id, queue.getString( id ) );
		}
		endReportCursor = (endReportCursor + batch.size()) % ids.size();
		endReporting = true;
		startNetworkTask( new Thread( () -> {
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) return;
				ensureFreshSession();
				String token = session.accessToken;
				for (Map.Entry<String, String> entry : batch.entrySet()) {
					Map<String, Object> body = new LinkedHashMap<>();
					body.put( "requested_character_id", entry.getKey() ); body.put( "requested_reason", entry.getValue() );
					JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_end_character", body, token );
					if (response != null && response.isBoolean() && response.asBoolean()) {
						synchronized (WayfarerAccountService.class) {
							JsonValue pending = new JsonReader().parse( com.watabou.utils.GameSettings.getString( ENDED_CHARACTERS, "{}" ) );
							pending.remove( entry.getKey() );
							com.watabou.utils.GameSettings.put( ENDED_CHARACTERS, pending.toJson( JsonWriter.OutputType.json ) );
						}
					}
				}
			} catch (Exception ignored) {
				// Keep the durable event until the owning account can reach the server.
			} finally {
				if (acquired) busy = false;
				synchronized (WayfarerAccountService.class) { endReporting = false; }
			}
		}, "Wayfarer Character End Reports" ) );
	}

	private WayfarerAccountService() {
	}

	public interface ResultCallback {
		void completed( Result result );
	}

	public interface TradeCallback { void completed(Result result, JsonValue data); }
	public interface TradeUpdatesCallback { void completed(Result result, ArrayList<GlobalTradeUpdate> updates); }
	public static final class GlobalTradeUpdate {
		public final NearbyPlayer peer;
		public final String tradeId, state, updatedAt;
		public final boolean notify;
		private GlobalTradeUpdate(JsonValue row) {
			peer = new NearbyPlayer(row.getString("peer_character_id", ""), row.getString("peer_name", "Wayfarer"),
					row.getString("peer_class", "WARRIOR"), row.getInt("peer_level", 1),
					row.getInt("peer_head_sprite", 0), row.getString("peer_public_key", ""),
					true, 0, 0, 0, false, 0);
			tradeId=row.getString("trade_id", ""); state=row.getString("state", "");
			updatedAt=row.getString("updated_at", ""); notify=row.getBoolean("notify", false);
		}
	}

	public static void tradeAction(String peer, String trade, String action, String payload, TradeCallback callback) {
		final String character = Dungeon.wayfarerCharacterId();
		final int slot = GamesInProgress.curSlot;
		startNetworkTask( new Thread(() -> {
			boolean acquired = false;
			Result result; JsonValue data = null;
			try {
				if (!(acquired = acquireBusy(5000))) throw new IOException("The network is busy. Retry this trade action.");
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put("requested_character_id", character); body.put("requested_peer", peer);
				body.put("requested_trade_id", trade); body.put("requested_action", action);
				body.put("requested_payload", payload); body.put("requested_version", Game.version);
				data = request("POST", "/rest/v1/rpc/wayfarer_trade_action", body, session.accessToken);
				result = new Result(true, "");
			} catch (Exception error) { result = new Result(false, error.getMessage()); }
			finally { if (acquired) busy = false; }
			final Result response = result; final JsonValue value = data;
			Game.runOnRenderThread(() -> {
				if (slot == GamesInProgress.curSlot && character.equals(Dungeon.wayfarerCharacterId())) callback.completed(response, value);
			});
		}, "Wayfarer Global Trade") );
	}

	public static void pendingTrades(TradeUpdatesCallback callback) {
		final String character= Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread(() -> {
			boolean acquired=false;Result result;ArrayList<GlobalTradeUpdate> updates=new ArrayList<>();
			try {
				if (!(acquired=acquireBusy(1000))) throw new IOException("Wayfarer network busy");
				ensureFreshSession();
				Map<String,Object> body=new LinkedHashMap<>();body.put("requested_character_id",character);
				JsonValue rows=request("POST","/rest/v1/rpc/wayfarer_pending_trades",body,session.accessToken);
				for(JsonValue row=rows==null?null:rows.child;row!=null;row=row.next) updates.add(new GlobalTradeUpdate(row));
				result=new Result(true,"");
			} catch(Exception error) { result=new Result(false,friendlyMessage(error)); }
			finally { if(acquired)busy=false; }
			final Result delivered=result;final ArrayList<GlobalTradeUpdate> values=updates;
			Game.runOnRenderThread(() -> { if(character.equals(Dungeon.wayfarerCharacterId()))callback.completed(delivered,values); });
		},"Wayfarer Trade Inbox") );
	}

	public interface NearbyPlayersCallback {
		void completed( Result result, ArrayList<NearbyPlayer> players );
	}

	public interface MessagesCallback {
		void completed( Result result, ArrayList<WayfarerMessage> messages );
	}

	public interface ModeratorMessagesCallback {
		void completed( Result result, ArrayList<ModeratorSpaceMessage> messages );
	}

	public interface ModeratorMessageCallback {
		void completed( Result result, ModeratorSpaceMessage message );
	}

	public interface ModeratorRewardStatusCallback {
		void completed( Result result, ModeratorRewardStatus status );
	}

	public interface ModeratorRewardClaimCallback {
		void completed( Result result, ModeratorRewardClaim claim );
	}

	public interface ReportsCallback {
		void completed( Result result, ArrayList<WayfarerReport> reports );
	}

	public interface BooleanCallback {
		void completed( Result result, boolean value );
	}
	public interface IntCallback {
		void completed( Result result, int value );
	}
	public interface RestrictionCallback {
		void completed( Result result, RestrictionStatus restriction );
	}
	public interface DeletionReviewCallback {
		void completed( Result result, DeletionReviewStatus status );
	}
	public static final class DeletionReviewStatus {
		public final String status, requestedAt, deadline, submittedAt, appeal;
		private DeletionReviewStatus( JsonValue row ) {
			status = row.getString( "status", "none" );
			requestedAt = row.getString( "requested_at", "" );
			deadline = row.getString( "appeal_deadline", "" );
			submittedAt = row.getString( "appeal_submitted_at", "" );
			appeal = row.getString( "appeal_text", "" );
		}
	}

	public static void deletionReviewStatus( final DeletionReviewCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		final int slot = GamesInProgress.curSlot;
		startNetworkTask( new Thread( () -> {
			boolean acquired = false;
			Result result; DeletionReviewStatus status = null;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>(); body.put( "requested_character_id", characterId );
				JsonValue row = request( "POST", "/rest/v1/rpc/wayfarer_deletion_review_status", body, session.accessToken );
				if (row == null) throw new IOException( "The deletion review could not be loaded." );
				status = new DeletionReviewStatus( row );
				if ("approved".equals( status.status )) scheduleCurrentCharacterDeletion( characterId, slot );
				result = new Result( true, "Deletion review loaded." );
			} catch (Exception error) { result = new Result( false, friendlyMessage( error ) ); }
			finally { if (acquired) busy = false; }
			final Result completed = result; final DeletionReviewStatus value = status;
			Game.runOnRenderThread( () -> {
				if (new DeletionTarget( slot, characterId ).matches( GamesInProgress.curSlot, Dungeon.wayfarerCharacterId() ))
					callback.completed( completed, value );
			} );
		}, "Wayfarer Deletion Review" ) );
	}

	public static void submitDeletionAppeal( final String characterId, final String requestedAt,
			final String appeal, final ResultCallback callback ) {
		startNetworkTask( new Thread( () -> {
			boolean acquired = false; Result result;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId ); body.put( "expected_review_requested_at", requestedAt );
				body.put( "requested_appeal_text", appeal.trim() );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_submit_deletion_appeal", body, session.accessToken );
				if (response == null || !response.asBoolean()) throw new IOException( "The appeal could not be confirmed. Reopen Deletion Review to check its status." );
				result = new Result( true, "Your appeal was received. A second moderator can now issue a verdict." );
			} catch (Exception error) { result = new Result( false, friendlyMessage( error ) ); }
			finally { if (acquired) busy = false; }
			deliver( callback, result );
		}, "Wayfarer Deletion Appeal" ) );
	}
	public static final class RestrictionStatus {
		public final boolean active;
		public final int stage;
		public final String endsAt;
		private RestrictionStatus( boolean active, int stage, String endsAt ) {
			this.active = active; this.stage = stage; this.endsAt = endsAt == null ? "" : endsAt;
		}
	}
	public interface NotificationsCallback {
		void completed( Result result, ArrayList<ModerationNotification> notifications );
	}
	public static final class ModerationNotification {
		public final String moderator, text, createdAt;
		private ModerationNotification( JsonValue row ) {
			moderator = row.getString( "moderator_name", "Wayfarer Moderation" );
			text = row.getString( "message_text", "" );
			createdAt = row.getString( "created_at", "" );
		}
	}

	public static final class WayfarerReport {
		public final long id;
		public final String reporter, reported, reason, status, createdAt, evidence, assignedModerator;
		public final boolean assignedToCurrentModerator;
		private WayfarerReport( JsonValue row ) {
			id = row.getLong( "report_id", 0 );
			reporter = row.getString( "reporter_name", "Wayfarer" );
			reported = row.getString( "reported_name", "Wayfarer" );
			reason = row.getString( "reason", "" );
			status = row.getString( "report_status", "open" );
			createdAt = row.getString( "created_at", "" );
			evidence = row.getString( "evidence", "" );
			assignedModerator = row.getString( "assigned_moderator_name", "" );
			assignedToCurrentModerator = row.getBoolean( "assigned_to_me", false );
		}
	}

	public static final class WayfarerMessage {
		public final String senderCharacterId;
		public final String senderName;
		public final String senderHeroClass;
		public final int senderHeadSprite;
		public final String senderPublicEncryptionKey;
		public final String text;
		public final String sentAt;
		public final String senderDisplayTime;

		private WayfarerMessage( JsonValue row, WayfarerMessageCrypto.MessagePayload message ) {
			senderCharacterId = row.getString( "sender_character_id", "" );
			senderName = row.getString( "sender_player_name", "Wayfarer" );
			senderHeroClass = row.getString( "sender_hero_class", "WARRIOR" );
			senderHeadSprite = row.getInt( "sender_head_sprite", 0 );
			senderPublicEncryptionKey = row.getString( "sender_public_encryption_key", "" );
			this.text = message.text;
			sentAt = row.getString( "sent_at", "" );
			senderDisplayTime = message.displayTime;
		}
	}

	public static final class ModeratorSpaceMessage {
		public final long messageId;
		public final String senderCharacterId;
		public final String senderName;
		public final String senderHeroClass;
		public final int senderHeroLevel;
		public final int senderHeadSprite;
		public final String text;
		public final String createdAt;
		public final boolean outgoing;

		private ModeratorSpaceMessage( JsonValue row ) {
			messageId = row.getLong( "message_id", 0 );
			senderCharacterId = row.getString( "sender_character_id", "" );
			senderName = row.getString( "sender_name", "Moderator" );
			senderHeroClass = row.getString( "sender_hero_class", "WARRIOR" );
			senderHeroLevel = row.getInt( "sender_hero_level", 1 );
			senderHeadSprite = row.getInt( "sender_head_sprite", 0 );
			text = row.getString( "message_text", "" );
			createdAt = row.getString( "created_at", "" );
			outgoing = row.getBoolean( "is_mine", false );
		}
	}

	public static final class ModeratorRewardClaim {
		public final String claimId;
		public final String period;
		public final String rewardKey;
		public final long seed;
		public final int selectedOption;

		private ModeratorRewardClaim( JsonValue row ) {
			claimId = row == null ? "" : row.getString( "claim_id", "" );
			period = row == null ? "" : row.getString( "reward_period", "" );
			rewardKey = row == null ? "" : row.getString( "reward_key", "" );
			seed = row == null ? 0 : row.getLong( "reward_seed", 0 );
			JsonValue selection = row == null ? null : row.get( "selected_option" );
			selectedOption = selection == null || selection.isNull() ? -2 : selection.asInt();
		}
	}

	public static final class ModeratorRewardStatus {
		public final boolean welcomeAvailable;
		public final int dailyActiveSeconds;
		public final int dailyEarnedCount;
		public final int dailyClaimedCount;
		public final int weeklyActiveSeconds;
		public final boolean weeklyAvailable;
		public final long characterLifetimeActiveSeconds;
		public final long moderatorLifetimeActiveSeconds;
		public final ModeratorRewardClaim pendingClaim;

		private ModeratorRewardStatus( JsonValue row ) {
			welcomeAvailable = row != null && row.getBoolean( "welcome_available", false );
			dailyActiveSeconds = row == null ? 0 : row.getInt( "daily_active_seconds", 0 );
			dailyEarnedCount = row == null ? 0 : row.getInt( "daily_earned_count", 0 );
			dailyClaimedCount = row == null ? 0 : row.getInt( "daily_claimed_count", 0 );
			weeklyActiveSeconds = row == null ? 0 : row.getInt( "weekly_active_seconds", 0 );
			weeklyAvailable = row != null && row.getBoolean( "weekly_available", false );
			characterLifetimeActiveSeconds = row == null ? 0
					: row.getLong( "character_lifetime_active_seconds", 0 );
			moderatorLifetimeActiveSeconds = row == null ? 0
					: row.getLong( "moderator_lifetime_active_seconds", characterLifetimeActiveSeconds );
			JsonValue claimId = row == null ? null : row.get( "pending_claim_id" );
			if (claimId == null || claimId.isNull()) {
				pendingClaim = null;
			} else {
				JsonValue claim = new JsonValue( JsonValue.ValueType.object );
				claim.addChild( "claim_id", new JsonValue( claimId.asString() ) );
				claim.addChild( "reward_period", new JsonValue( row.getString( "pending_reward_period", "" ) ) );
				claim.addChild( "reward_key", new JsonValue( row.getString( "pending_reward_key", "" ) ) );
				claim.addChild( "reward_seed", new JsonValue( row.getLong( "pending_reward_seed", 0 ) ) );
				JsonValue selected = row.get( "pending_selected_option" );
				claim.addChild( "selected_option", selected == null || selected.isNull()
						? new JsonValue( JsonValue.ValueType.nullValue ) : new JsonValue( selected.asInt() ) );
				pendingClaim = new ModeratorRewardClaim( claim );
			}
		}

		public boolean hasClaimableReward() {
			return pendingClaim != null || welcomeAvailable || dailyClaimedCount < dailyEarnedCount || weeklyAvailable;
		}
	}

	public static final class NearbyPlayer {
		public final String characterId;
		public final String playerName;
		public final String heroClass;
		public final int heroLevel;
		public final int headSprite;
		public final String publicEncryptionKey;
		public final boolean interestedInTrading;
		public final int distanceMeters;
		public final double mapLatitude;
		public final double mapLongitude;
		public final boolean online;
		public final long statusSeconds;
		private final long statusRecordedAt;

		private NearbyPlayer( JsonValue row ) {
			characterId = row.getString( "character_id", "" );
			playerName = row.getString( "player_name", "Wayfarer" );
			heroClass = row.getString( "hero_class", "WARRIOR" );
			heroLevel = row.getInt( "hero_level", 1 );
			headSprite = row.getInt( "head_sprite", 0 );
			publicEncryptionKey = row.getString( "public_encryption_key", "" );
			interestedInTrading = row.getBoolean( "interested_in_trading", false );
			distanceMeters = Math.max( 0, row.getInt( "distance_meters", 0 ) );
			mapLatitude = row.getDouble( "map_latitude", 0 );
			mapLongitude = row.getDouble( "map_longitude", 0 );
			online = row.getBoolean( "is_online", true );
			statusSeconds = Math.max( 0, row.getLong( "status_seconds", 0 ) );
			statusRecordedAt = System.currentTimeMillis();
		}

		public NearbyPlayer( String characterId, String playerName, String heroClass, int heroLevel,
				int headSprite, String publicEncryptionKey, boolean interestedInTrading,
				int distanceMeters, double mapLatitude, double mapLongitude ) {
			this( characterId, playerName, heroClass, heroLevel, headSprite, publicEncryptionKey,
					interestedInTrading, distanceMeters, mapLatitude, mapLongitude, true, 0 );
		}

		public NearbyPlayer( String characterId, String playerName, String heroClass, int heroLevel,
				int headSprite, String publicEncryptionKey, boolean interestedInTrading,
				int distanceMeters, double mapLatitude, double mapLongitude,
				boolean online, long statusSeconds ) {
			this.characterId = characterId;
			this.playerName = playerName;
			this.heroClass = heroClass;
			this.heroLevel = heroLevel;
			this.headSprite = headSprite;
			this.publicEncryptionKey = publicEncryptionKey;
			this.interestedInTrading = interestedInTrading;
			this.distanceMeters = distanceMeters;
			this.mapLatitude = mapLatitude;
			this.mapLongitude = mapLongitude;
			this.online = online;
			this.statusSeconds = statusSeconds;
			this.statusRecordedAt = System.currentTimeMillis();
		}

		public String presenceStatus() {
			long elapsed = Math.max( 0, (System.currentTimeMillis() - statusRecordedAt) / 1000 );
			return (online ? "Online for " : "Offline for ") + duration( statusSeconds + elapsed );
		}

		private static String duration( long seconds ) {
			if (seconds >= 86400) return (seconds / 86400) + "d " + ((seconds % 86400) / 3600) + "h";
			if (seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
			return (seconds / 60) + "m " + (seconds % 60) + "s";
		}
	}

	public static final class Result {
		public final boolean success;
		public final String message;

		public Result( boolean success, String message ) {
			this.success = success;
			this.message = message;
		}
	}

	private static final class Session {
		String accessToken;
		String refreshToken;
		String userId;
		String email;
		String accountName;
		String accountStatus;
		long expiresAtMillis;
	}

	public static boolean isBusy() {
		return busy;
	}

	public static boolean isSignedIn() {
		return session != null && session.accessToken != null;
	}

	public static boolean hasSavedSession() {
		return !SPDSettings.wayfarerRefreshToken().isEmpty();
	}

	public static String email() {
		return session == null || session.email == null ? "" : session.email;
	}

	public static String maskedEmail() {
		String value = email();
		int separator = value.indexOf( '@' );
		if (separator < 0) return "***";
		String local = value.substring( 0, separator );
		String prefix = local.substring( 0, Math.min( 3, local.length() ) );
		return prefix + "***" + value.substring( separator );
	}

	public static String accountName() {
		return session == null || session.accountName == null ? "" : session.accountName;
	}

	public static String accountStatus() {
		return session == null || session.accountStatus == null ? "" : session.accountStatus;
	}

	public static boolean currentCharacterEligible() {
		HomebaseState homebase = Dungeon.homebase;
		return Dungeon.hero != null && homebase != null && homebase.wayfarerExchangeUnlocked();
	}

	public static void signIn( final String email, final String password, final ResultCallback callback ) {
		if (busy) return;
		persistCurrentCharacterIdentity();
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "email", email == null ? "" : email.trim() );
				body.put( "password", password == null ? "" : password );
				JsonValue json = request( "POST", "/auth/v1/token?grant_type=password", body, null );

				Session authenticated = sessionFromResponse( json, email );
				session = authenticated;

				loadProfile();
				if (currentCharacterEligible()) registerAuthenticatedCharacter();
				persistSession();
				result = new Result( true, "Connected as " + authenticated.accountName + "." );
			} catch (Exception error) {
				session = null;
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Account Sign In" ) );
	}

	public static void signUp( final String email, final String password, final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				String passwordProblem = passwordProblem( password );
				if (passwordProblem != null) throw new IOException( passwordProblem );
				String normalizedEmail = email == null ? "" : email.trim();
				Map<String, Object> lookup = new LinkedHashMap<>();
				lookup.put( "requested_email", normalizedEmail );
				JsonValue registered = request( "POST",
						"/rest/v1/rpc/wayfarer_email_registered", lookup, null );
				if (registered != null && registered.asBoolean()) {
					throw new IOException( "That email address already has a Wayfarer account or is waiting for verification." );
				}
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "email", normalizedEmail );
				body.put( "password", password == null ? "" : password );
				JsonValue json = request( "POST", "/auth/v1/signup", body, null );
				JsonValue returnedUser = json == null ? null : json.get( "user" );
				JsonValue identities = returnedUser == null ? null : returnedUser.get( "identities" );
				if (identities != null && identities.size == 0) {
					throw new IOException( "That email address already has a Wayfarer account." );
				}
				if (json != null && json.getString( "access_token", null ) != null) {
					session = sessionFromResponse( json, email );
					loadProfile();
					if (currentCharacterEligible()) registerAuthenticatedCharacter();
					persistSession();
					result = new Result( true, "Account created and connected." );
				} else {
					result = new Result( true, "Account created. Enter the confirmation code from the email." );
				}
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Account Sign Up" ) );
	}

	public static void checkSignUpEmail( final String email, final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				String normalizedEmail = email == null ? "" : email.trim();
				Map<String, Object> lookup = new LinkedHashMap<>();
				lookup.put( "requested_email", normalizedEmail );
				JsonValue registered = request( "POST",
						"/rest/v1/rpc/wayfarer_email_registered", lookup, null );
				if (registered != null && registered.asBoolean()) {
					result = new Result( false,
							"That email address already has a Wayfarer account or is waiting for verification." );
				} else {
					result = new Result( true, "Email address is available." );
				}
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Email Availability" ) );
	}

	public static void completeSignUp( final String email, final String token,
			final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				Map<String, Object> verification = new LinkedHashMap<>();
				verification.put( "email", email == null ? "" : email.trim() );
				verification.put( "token", token == null ? "" : token.trim() );
				verification.put( "type", "signup" );
				session = sessionFromResponse(
						request( "POST", "/auth/v1/verify", verification, null ), email );
				loadProfile();
				if (currentCharacterEligible()) registerAuthenticatedCharacter();
				persistSession();
				result = new Result( true, "Account confirmed and connected." );
			} catch (Exception error) {
				session = null;
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Account Confirmation" ) );
	}

	public static void requestPasswordRecovery( final String email, final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "email", email == null ? "" : email.trim() );
				request( "POST", "/auth/v1/recover", body, null );
				result = new Result( true, "If that address has an account, Supabase has sent its recovery email." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Password Recovery" ) );
	}

	public static void resendSignUpConfirmation( final String email, final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "email", email == null ? "" : email.trim() );
				body.put( "type", "signup" );
				request( "POST", "/auth/v1/resend", body, null );
				result = new Result( true, "A new confirmation code was requested. Check the account email." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Confirmation Resend" ) );
	}

	public static void completePasswordRecovery( final String email, final String token,
			final String newPassword, final ResultCallback callback ) {
		if (busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				String passwordProblem = passwordProblem( newPassword );
				if (passwordProblem != null) throw new IOException( passwordProblem );
				Map<String, Object> verification = new LinkedHashMap<>();
				verification.put( "email", email == null ? "" : email.trim() );
				verification.put( "token", token == null ? "" : token.trim() );
				verification.put( "type", "recovery" );
				JsonValue json = request( "POST", "/auth/v1/verify", verification, null );
				session = sessionFromResponse( json, email );

				Map<String, Object> update = new LinkedHashMap<>();
				update.put( "password", newPassword == null ? "" : newPassword );
				JsonValue updatedUser = request( "PUT", "/auth/v1/user", update, session.accessToken );
				if (updatedUser == null || updatedUser.getString( "id", null ) == null) {
					throw new IOException( "Supabase did not confirm the password update." );
				}
				loadProfile();
				if (currentCharacterEligible()) registerAuthenticatedCharacter();
				persistSession();
				result = new Result( true, "Password updated. This device is now connected." );
			} catch (Exception error) {
				session = null;
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Password Recovery Completion" ) );
	}

	public static void restoreSession( final ResultCallback callback ) {
		if (busy || isSignedIn()) return;
		String refreshToken = SPDSettings.wayfarerRefreshToken();
		if (refreshToken.isEmpty()) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				Session restoring = new Session();
				restoring.refreshToken = refreshToken;
				session = restoring;
				refreshSession();
				loadProfile();
				if (currentCharacterEligible()) registerAuthenticatedCharacter();
				persistSession();
				result = new Result( true, "Restored " + session.accountName + "." );
			} catch (Exception error) {
				session = null;
				SPDSettings.wayfarerRefreshToken( "" );
				result = new Result( false, "The saved Wayfarer session expired. Please sign in again." );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Session Restore" ) );
	}

	public static void registerCurrentCharacter( final ResultCallback callback ) {
		if (busy) return;
		persistCurrentCharacterIdentity();
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				if (!currentCharacterEligible()) {
					throw new IOException( "This character has not unlocked the Wayfarer Exchange." );
				}
				ensureFreshSession();
				registerCurrentCharacterInternal();
				result = new Result( true, "This character is connected to your Wayfarer account." );
			} catch (Exception error) {
				if (error instanceof CharacterNameTakenException) scheduleCharacterNamePrompt();
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Character Registration" ) );
	}

	public static void ensureCurrentCharacterRegistration() {
		if (!isSignedIn() || busy || !currentCharacterEligible()) return;
		String characterId = Dungeon.wayfarerCharacterId();
		String characterName = Dungeon.hero.characterName();
		if (characterId.equals( registeredCharacterId ) && characterName.equals( registeredCharacterName )) return;
		registerCurrentCharacter( result -> {} );
	}

	public static void renameCurrentCharacter( final String requestedName, final ResultCallback callback ) {
		final String cleanName = GamesInProgress.cleanCharacterName( requestedName );
		if (cleanName.isEmpty()) {
			deliver( callback, new Result( false, "Please enter a character name." ) );
			return;
		}
		if (busy) {
			deliver( callback, new Result( false, "The Wayfarer network is busy. Try again." ) );
			return;
		}
		final String characterId = Dungeon.wayfarerCharacterId();
		final int slot = GamesInProgress.curSlot;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				if (!currentCharacterEligible()) {
					throw new IOException( "This character has not unlocked the Wayfarer Exchange." );
				}
				ensureFreshSession();
				Map<String, Object> availability = new LinkedHashMap<>();
				availability.put( "requested_character_id", characterId );
				availability.put( "requested_player_name", cleanName );
				JsonValue available = request( "POST",
						"/rest/v1/rpc/wayfarer_character_name_available", availability, session.accessToken );
				if (available == null || !available.asBoolean()) throw new CharacterNameTakenException();
				registerCurrentCharacterInternal( cleanName );
				result = new Result( true, "Character name updated." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			final Result response = result;
			Game.runOnRenderThread( () -> {
				Result delivered = response;
				if (response.success) {
					if (slot != GamesInProgress.curSlot || !characterId.equals( Dungeon.wayfarerCharacterId() )) {
						delivered = new Result( false, "The active character changed before its name could be saved." );
					} else {
						Dungeon.hero.customName( cleanName );
						if (!Dungeon.saveGameChecked( slot )) {
							delivered = new Result( false, "The online name was reserved, but the local save could not be updated." );
						} else {
							GamesInProgress.set( slot );
						}
					}
				}
				if (callback != null) callback.completed( delivered );
			} );
		}, "Wayfarer Character Rename" ) );
	}

	public static boolean isCharacterNameTaken( Result result ) {
		return result != null && !result.success
				&& result.message != null && result.message.toLowerCase().contains( "username is already taken" );
	}

	public static void characterNamePromptFinished() {
		characterNamePromptActive = false;
	}

	public static void publishPresence( final double latitude, final double longitude,
			final ResultCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) {
					throw new IOException( "The Wayfarer network is busy." );
				}
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				if (!currentCharacterEligible()) {
					throw new IOException( "This character has not unlocked the Wayfarer Exchange." );
				}
				ensureFreshSession();
				registerCurrentCharacterInternal();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "latitude", latitude );
				body.put( "longitude", longitude );
				body.put( "make_visible", true );
				body.put( "trade_interest", true );
				request( "POST", "/rest/v1/rpc/wayfarer_set_presence", body, session.accessToken );
				result = new Result( true, "This character is now visible to nearby players." );
			} catch (Exception error) {
				if (error instanceof CharacterNameTakenException) scheduleCharacterNamePrompt();
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Presence Publish" ) );
	}

	public static void goOffline( final ResultCallback callback ) {
		if (busy) {
			deliver( callback, new Result( false, "The Wayfarer network is busy. Try again." ) );
			return;
		}
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			try {
				if (isSignedIn()) {
					ensureFreshSession();
					Map<String, Object> body = new LinkedHashMap<>();
					body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
					request( "POST", "/rest/v1/rpc/wayfarer_go_offline",
							body, session.accessToken );
				}
				result = new Result( true, "This character is hidden from nearby players." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Presence Offline" ) );
	}

	public static void nearbyPlayers( final double latitude, final double longitude,
			final NearbyPlayersCallback callback ) {
		if (busy) {
			deliverNearby( callback, new Result( false, "The Wayfarer network is busy. Try again." ),
					new ArrayList<>() );
			return;
		}
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<NearbyPlayer> players = new ArrayList<>();
			try {
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				if (!SPDSettings.wayfarerVisible()) {
					throw new IOException( "Become visible before searching for nearby players." );
				}
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "latitude", latitude );
				body.put( "longitude", longitude );
				body.put( "radius_meters", 100000 );
				body.put( "result_limit", 100 );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_nearby_players",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next) {
					players.add( new NearbyPlayer( row ) );
				}
				result = new Result( true, players.isEmpty()
						? "No visible Wayfarers are nearby right now."
						: "Found " + players.size() + " nearby Wayfarer" + (players.size() == 1 ? "." : "s.") );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliverNearby( callback, result, players );
		}, "Wayfarer Nearby Players" ) );
	}

	public static void contactPresence( final String contactIds,
			final NearbyPlayersCallback callback ) {
		if (contactIds == null || contactIds.isEmpty() || busy) return;
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<NearbyPlayer> players = new ArrayList<>();
			try {
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "requested_contact_ids", contactIds );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_contact_presence",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next)
					players.add( new NearbyPlayer( row ) );
				result = new Result( true, "Contact presence updated." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliverNearby( callback, result, players );
		}, "Wayfarer Contact Presence" ) );
	}

	public static void sendMessage( final NearbyPlayer recipient, final String text,
			final String senderTimestamp,
			final ResultCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) {
					throw new IOException( "The Wayfarer network is busy. Try again." );
				}
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				String message = text == null ? "" : text.trim();
				if (message.isEmpty() || message.length() > 500) {
					throw new IOException( "Messages must contain 1 to 500 characters." );
				}
				if (recipient == null) throw new IOException( "The receiving character is unavailable." );
				ensureFreshSession();
				String publicKey = recipient.publicEncryptionKey;
				if (publicKey.isEmpty()) {
					Map<String, Object> keyRequest = new LinkedHashMap<>();
					keyRequest.put( "requested_character_id", recipient.characterId );
					JsonValue keyResult = request( "POST", "/rest/v1/rpc/wayfarer_character_message_key",
							keyRequest, session.accessToken );
					publicKey = keyResult == null || keyResult.isNull() ? "" : keyResult.asString();
				}
				if (publicKey.isEmpty()) {
					throw new IOException( "This character is not ready to receive secure messages." );
				}
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_sender_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "requested_recipient_character_id", recipient.characterId );
				body.put( "requested_encrypted_payload",
						WayfarerMessageCrypto.encrypt(
								WayfarerMessageCrypto.packMessage( message, senderTimestamp ), publicKey ) );
				request( "POST", "/rest/v1/rpc/wayfarer_send_message", body, session.accessToken );
				result = new Result( true, "Message sent." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Message Send" ) );
	}

	public static void sendModeratorSpaceMessage( final String text,
			final ModeratorMessageCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			ModeratorSpaceMessage message = null;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy." );
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				String cleanMessage = text == null ? "" : text.trim();
				if (cleanMessage.isEmpty() || cleanMessage.length() > 500) {
					throw new IOException( "Messages must contain 1 to 500 characters." );
				}
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				body.put( "requested_message", cleanMessage );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_send_moderator_message",
						body, session.accessToken );
				JsonValue row = rows != null && rows.isArray() ? rows.child : rows;
				if (row == null || row.getLong( "message_id", 0 ) <= 0) {
					throw new IOException( "The moderator message could not be confirmed." );
				}
				message = new ModeratorSpaceMessage( row );
				moderatorAccount = true;
				result = new Result( true, "Message sent." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			final ModeratorSpaceMessage delivered = message;
			Game.runOnRenderThread( () -> {
				if (callback != null && characterId.equals( Dungeon.wayfarerCharacterId() )) {
					callback.completed( completed, delivered );
				}
			} );
		}, "Wayfarer Moderator Message Send" ) );
	}

	public static void receiveModeratorSpaceMessages( final long afterMessageId,
			final ModeratorMessagesCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<ModeratorSpaceMessage> messages = new ArrayList<>();
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 1500 ))) throw new IOException( "The Wayfarer network is busy." );
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				body.put( "requested_after_message_id", Math.max( 0, afterMessageId ) );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_receive_moderator_messages",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next) {
					messages.add( new ModeratorSpaceMessage( row ) );
				}
				moderatorAccount = true;
				result = new Result( true, "Moderator messages loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliverModeratorMessages( callback, result, messages, characterId );
		}, "Wayfarer Moderator Message Receive" ) );
	}

	public static void moderatorRewardStatus( final ModeratorRewardStatusCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			ModeratorRewardStatus status = null;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_moderator_reward_status",
						body, session.accessToken );
				JsonValue row = firstRow( response );
				if (row == null) throw new IOException( "Moderator rewards could not be loaded." );
				status = new ModeratorRewardStatus( row );
				moderatorAccount = true;
				result = new Result( true, "Moderator rewards loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result delivered = result;
			final ModeratorRewardStatus value = status;
			Game.runOnRenderThread( () -> {
				if (callback != null && characterId.equals( Dungeon.wayfarerCharacterId() ))
					callback.completed( delivered, value );
			} );
		}, "Wayfarer Moderator Rewards" ) );
	}

	public static void reportModeratorActivity( final int activeSeconds, final IntCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			int accepted = 0;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 1500 ))) throw new IOException( "The Wayfarer network is busy." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				body.put( "requested_active_seconds", Math.max( 1, Math.min( 180, activeSeconds ) ) );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_moderator_report_activity",
						body, session.accessToken );
				accepted = response == null ? 0 : response.asInt();
				result = new Result( true, "Moderator activity recorded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result delivered = result;
			final int value = accepted;
			Game.runOnRenderThread( () -> {
				if (callback != null && characterId.equals( Dungeon.wayfarerCharacterId() ))
					callback.completed( delivered, value );
			} );
		}, "Wayfarer Moderator Activity" ) );
	}

	public static void beginModeratorReward( final String period, final String rewardKey,
			final ModeratorRewardClaimCallback callback ) {
		moderatorRewardClaimRequest( "/rest/v1/rpc/wayfarer_begin_moderator_reward", period,
				rewardKey, "", -1, callback );
	}

	public static void selectModeratorReward( final String claimId, final int option,
			final ModeratorRewardClaimCallback callback ) {
		moderatorRewardClaimRequest( "/rest/v1/rpc/wayfarer_select_moderator_reward", "",
				"", claimId, option, callback );
	}

	private static void moderatorRewardClaimRequest( final String path, final String period,
			final String rewardKey, final String claimId, final int option,
			final ModeratorRewardClaimCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			ModeratorRewardClaim claim = null;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				if (claimId == null || claimId.isEmpty()) {
					body.put( "requested_reward_period", period );
					body.put( "requested_reward_key", rewardKey );
				} else {
					body.put( "requested_claim_id", claimId );
					body.put( "requested_option", option );
				}
				JsonValue row = firstRow( request( "POST", path, body, session.accessToken ) );
				if (row == null) throw new IOException( "The moderator reward could not be reserved." );
				claim = new ModeratorRewardClaim( row );
				result = new Result( true, "Moderator reward reserved." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result delivered = result;
			final ModeratorRewardClaim value = claim;
			Game.runOnRenderThread( () -> {
				if (callback != null && characterId.equals( Dungeon.wayfarerCharacterId() ))
					callback.completed( delivered, value );
			} );
		}, "Wayfarer Moderator Reward Claim" ) );
	}

	public static void acknowledgeModeratorReward( final String claimId, final BooleanCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acknowledged = false;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				body.put( "requested_claim_id", claimId );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_acknowledge_moderator_reward",
						body, session.accessToken );
				acknowledged = response != null && response.asBoolean();
				if (!acknowledged) throw new IOException( "The moderator reward receipt was not confirmed." );
				result = new Result( true, "Moderator reward claimed." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result delivered = result;
			final boolean value = acknowledged;
			Game.runOnRenderThread( () -> {
				if (callback != null && characterId.equals( Dungeon.wayfarerCharacterId() ))
					callback.completed( delivered, value );
			} );
		}, "Wayfarer Moderator Reward Receipt" ) );
	}

	public static void blockPlayer( final NearbyPlayer player, final boolean block,
			final ResultCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn() || player == null) throw new IOException( "The Wayfarer account is unavailable." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "target_character_id", player.characterId );
				body.put( "requested_should_block", block );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_set_character_block",
						body, session.accessToken );
				if (response == null || response.asBoolean() != block) {
					throw new IOException( "The server could not confirm the block status." );
				}
				result = new Result( true, block ? "Wayfarer blocked." : "Wayfarer unblocked." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliver( callback, result );
		}, block ? "Wayfarer Block" : "Wayfarer Unblock" ) );
	}

	public static void blockStatus( final NearbyPlayer player, final BooleanCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean blocked = false;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn() || player == null) throw new IOException( "The Wayfarer account is unavailable." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "target_character_id", player.characterId );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_block_status",
						body, session.accessToken );
				blocked = response != null && response.asBoolean();
				result = new Result( true, blocked ? "Wayfarer is blocked." : "Wayfarer is not blocked." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			final boolean value = blocked;
			Game.runOnRenderThread( () -> {
				if (callback != null) callback.completed( completed, value );
			} );
		}, "Wayfarer Block Status" ) );
	}

	public static void blockRelationship( final NearbyPlayer player, final IntCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			int relationship = 0;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn() || player == null) throw new IOException( "The Wayfarer account is unavailable." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "target_character_id", player.characterId );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_block_relationship",
						body, session.accessToken );
				relationship = response == null ? 0 : response.asInt();
				result = new Result( true, "Block relationship updated." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			final int value = relationship;
			Game.runOnRenderThread( () -> {
				if (callback != null) callback.completed( completed, value );
			} );
		}, "Wayfarer Block Relationship" ) );
	}

	public static void blockedPlayers( final NearbyPlayersCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<NearbyPlayer> players = new ArrayList<>();
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_blocked_characters",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next) {
					players.add( new NearbyPlayer( row ) );
				}
				result = new Result( true, "Blocked players loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliverNearby( callback, result, players );
		}, "Wayfarer Blocked Players" ) );
	}

	public static void reportPlayer( final NearbyPlayer player, final String reason,
			final String evidence, final ResultCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn() || player == null) throw new IOException( "The Wayfarer account is unavailable." );
				ensureFreshSession();
				int messageCount = WayfarerChatStore.reportEvidenceCount( player.characterId );
				if (messageCount <= 0 || evidence == null || evidence.isEmpty()) {
					throw new IOException( "There are no messages available for this incident report." );
				}
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "target_character_id", player.characterId );
				body.put( "report_reason", reason );
				body.put( "conversation_evidence", evidence );
				body.put( "reported_message_count", messageCount );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_submit_incident",
						body, session.accessToken );
				long incidentId = response == null ? 0 : response.asLong();
				if (incidentId <= 0) throw new IOException( "The server did not confirm the incident report." );
				result = new Result( true, "Incident report #" + incidentId + " was sent to the moderator group." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Incident Submission" ) );
	}

	public static void reports( final ReportsCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<WayfarerReport> reports = new ArrayList<>();
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_moderator_incidents",
						new LinkedHashMap<>(), session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next)
					reports.add( new WayfarerReport( row ) );
				result = new Result( true, "Reports loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliverReports( callback, result, reports );
		}, "Wayfarer Reports" ) );
	}

	public static void moderatorStatus( final BooleanCallback callback ) {
		if (moderatorAccount != null) {
			deliverBoolean( callback, new Result( true, "Moderator status loaded." ), moderatorAccount );
			return;
		}
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean moderator = false;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				JsonValue value = request( "POST", "/rest/v1/rpc/wayfarer_is_moderator",
						new LinkedHashMap<>(), session.accessToken );
				moderator = value != null && value.asBoolean();
				result = new Result( true, "Moderator status loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			if (result.success) moderatorAccount = moderator;
			final boolean value = moderator;
			final Result completed = result;
			Game.runOnRenderThread( new Callback() {
				@Override public void call() {
					if (completed.success) WayfarerChatStore.moderatorSpaceEnabled( value );
					if (callback != null) callback.completed( completed, value );
				}
			} );
		}, "Wayfarer Moderator Status" ) );
	}

	public static boolean moderatorStatusKnown() {
		return moderatorAccount != null;
	}

	public static boolean isModeratorAccount() {
		return Boolean.TRUE.equals( moderatorAccount );
	}

	public static void currentCharacterRestricted( final BooleanCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean restricted = true;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				JsonValue value = request( "POST", "/rest/v1/rpc/wayfarer_character_restricted",
						body, session.accessToken );
				restricted = value != null && value.asBoolean();
				result = new Result( true, restricted ? "This character is currently restricted from Wayfarer chat and trading."
						: "Wayfarer access is available." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			final boolean value = restricted;
			Game.runOnRenderThread( () -> { if (callback != null) callback.completed( completed, value ); } );
		}, "Wayfarer Restriction Check" ) );
	}

	public static void currentRestriction( final RestrictionCallback callback ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			RestrictionStatus restriction = new RestrictionStatus( false, 0, "" );
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_current_restriction", body, session.accessToken );
				JsonValue row = rows == null ? null : rows.child;
				if (row != null) restriction = new RestrictionStatus( row.getBoolean( "is_active", false ),
						row.getInt( "sanction_stage", 0 ), row.getString( "ends_at", "" ) );
				result = new Result( true, "Restriction status loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			final RestrictionStatus value = restriction;
			Game.runOnRenderThread( () -> { if (callback != null) callback.completed( completed, value ); } );
		}, "Wayfarer Restriction Details" ) );
	}

	public static void claimIncident( long incidentId, ResultCallback callback ) {
		Map<String, Object> extra = new LinkedHashMap<>();
		extra.put( "moderator_character_id", Dungeon.wayfarerCharacterId() );
		incidentAction( "wayfarer_claim_incident", incidentId, extra, callback, "Case accepted." );
	}

	public static void resolveIncident( long incidentId, boolean valid, String category, String moderatorNote,
			ResultCallback callback ) {
		Map<String, Object> extra = new LinkedHashMap<>();
		extra.put( "report_is_valid", valid );
		extra.put( "requested_violation_category", valid ? category : "" );
		extra.put( "requested_moderator_note", valid ? moderatorNote : "No violation found." );
		extra.put( "moderator_character_id", Dungeon.wayfarerCharacterId() );
		incidentAction( "wayfarer_resolve_incident", incidentId, extra, callback,
				valid ? "Incident validated." : "Incident dismissed as invalid." );
	}

	private static void incidentAction( String function, long incidentId, Map<String, Object> extra,
			ResultCallback callback, String success ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_incident_id", incidentId );
				if (extra != null) body.putAll( extra );
				JsonValue response = request( "POST", "/rest/v1/rpc/" + function, body, session.accessToken );
				if ("wayfarer_claim_incident".equals( function ) && (response == null || !response.asBoolean()))
					throw new IOException( "Another moderator already accepted this case." );
				result = new Result( true, success );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			if (result.success) WayfarerModeratorRewards.recordActivity();
			deliver( callback, result );
		}, "Wayfarer Incident Action" ) );
	}

	private interface SafetyBody { void add( Map<String, Object> body ); }

	private static void runSafetyRequest( String function, NearbyPlayer player, SafetyBody extra,
			ResultCallback callback, String successMessage ) {
		startNetworkTask( new Thread( () -> {
			Result result;
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				if (!isSignedIn() || player == null) throw new IOException( "The Wayfarer account is unavailable." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", Dungeon.wayfarerCharacterId() );
				body.put( "target_character_id", player.characterId );
				extra.add( body );
				request( "POST", "/rest/v1/rpc/" + function, body, session.accessToken );
				result = new Result( true, successMessage );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			deliver( callback, result );
		}, "Wayfarer Safety Request" ) );
	}

	private static boolean acquireBusy( long timeoutMillis ) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		do {
			synchronized (WayfarerAccountService.class) {
				if (!busy) {
					busy = true;
					return true;
				}
			}
			Thread.sleep( 50 );
		} while (System.currentTimeMillis() < deadline);
		return false;
	}

	public static void receiveMessages( final MessagesCallback callback ) {
		if (busy) {
			deliverMessages( callback, new Result( false, "The Wayfarer network is busy. Try again." ),
					new ArrayList<>() );
			return;
		}
		busy = true;
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<WayfarerMessage> messages = new ArrayList<>();
			try {
				if (!isSignedIn()) throw new IOException( "Sign in first." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_recipient_character_id", Dungeon.wayfarerCharacterId() );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_receive_messages",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next) {
					try {
						messages.add( new WayfarerMessage( row, WayfarerMessageCrypto.unpackMessage(
								WayfarerMessageCrypto.decrypt( row.getString( "encrypted_payload", "" ) ) ) ) );
					} catch (Exception ignored) {
						// A malformed or wrong-key envelope is discarded without exposing the chat window.
					}
				}
				result = new Result( true, messages.isEmpty() ? "No new messages." : "Messages received." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				busy = false;
			}
			deliverMessages( callback, result, messages );
		}, "Wayfarer Message Receive" ) );
	}

	public static void receiveModerationNotifications( final NotificationsCallback callback ) {
		final String characterId = Dungeon.wayfarerCharacterId();
		final int slot = GamesInProgress.curSlot;
		startNetworkTask( new Thread( () -> {
			Result result;
			ArrayList<ModerationNotification> notifications = new ArrayList<>();
			boolean acquired = false;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "The Wayfarer network is busy. Try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>();
				body.put( "requested_character_id", characterId );
				JsonValue deletion = request( "POST", "/rest/v1/rpc/wayfarer_character_deletion_pending", body, session.accessToken );
				if (deletion != null && deletion.asBoolean()) scheduleCurrentCharacterDeletion( characterId, slot );
				JsonValue rows = request( "POST", "/rest/v1/rpc/wayfarer_receive_moderation_notifications",
						body, session.accessToken );
				for (JsonValue row = rows == null ? null : rows.child; row != null; row = row.next)
					notifications.add( new ModerationNotification( row ) );
				result = new Result( true, "Moderation notifications loaded." );
			} catch (Exception error) {
				result = new Result( false, friendlyMessage( error ) );
			} finally {
				if (acquired) busy = false;
			}
			final Result completed = result;
			Game.runOnRenderThread( new Callback() {
				@Override public void call() {
					if (callback != null) callback.completed( completed, notifications );
				}
			} );
		}, "Wayfarer Moderation Notifications" ) );
	}

	public static void signOut() {
		Session oldSession = session;
		String oldCharacterId = currentCharacterEligible() ? Dungeon.wayfarerCharacterId() : null;
		session = null;
		moderatorAccount = null;
		WayfarerChatStore.moderatorSpaceEnabled( false );
		registeredCharacterId = registeredCharacterName = "";
		characterNamePromptActive = false;
		SPDSettings.wayfarerRefreshToken( "" );
		SPDSettings.wayfarerVisible( false );
		if (oldSession == null || oldSession.accessToken == null) return;
		startNetworkTask( new Thread( () -> {
			try {
				if (oldCharacterId != null) {
					Map<String, Object> body = new LinkedHashMap<>();
					body.put( "requested_character_id", oldCharacterId );
					request( "POST", "/rest/v1/rpc/wayfarer_go_offline", body, oldSession.accessToken );
				}
			} catch (Exception ignored) {
			}
			try {
				request( "POST", "/auth/v1/logout", null, oldSession.accessToken );
			} catch (Exception ignored) {
				// The local session is already gone; remote expiry remains a safe fallback.
			}
		}, "Wayfarer Account Sign Out" ) );
	}

	private static void loadProfile() throws IOException {
		JsonValue rows = request( "GET",
				"/rest/v1/wayfarer_profiles?select=player_name,account_status&user_id=eq."
						+ urlEncode( session.userId ), null, session.accessToken );
		JsonValue profile = rows == null ? null : rows.child;
		if (profile == null) throw new IOException( "The Wayfarer account profile was not found." );
		session.accountName = profile.getString( "player_name", "Wayfarer" );
		session.accountStatus = profile.getString( "account_status", "active" );
		if (!"active".equals( session.accountStatus ) && !"muted".equals( session.accountStatus )) {
			throw new IOException( "Online access for this account is " + session.accountStatus + "." );
		}
	}

	private static void persistCurrentCharacterIdentity() {
		if (!currentCharacterEligible() || GamesInProgress.curSlot <= 0) return;
		Dungeon.wayfarerCharacterId();
		Dungeon.saveGame( GamesInProgress.curSlot );
		GamesInProgress.set( GamesInProgress.curSlot );
	}

	private static final class CharacterDeletionApprovedException extends IOException {
		CharacterDeletionApprovedException() {
			super( "Character deletion has been approved. Please read the deletion notice." );
		}
	}

	private static final class CharacterNameTakenException extends IOException {
		CharacterNameTakenException() {
			super( "WAYFARER_NAME_TAKEN" );
		}
	}

	private static void registerAuthenticatedCharacter() throws IOException {
		try {
			registerCurrentCharacterInternal();
		} catch (CharacterDeletionApprovedException approved) {
			// Account authentication remains valid so the owner can acknowledge the final notice.
			// Presence publication and manual sync still reject this character through the strict path.
		} catch (CharacterNameTakenException taken) {
			scheduleCharacterNamePrompt();
		}
	}

	private static void registerCurrentCharacterInternal() throws IOException {
		registerCurrentCharacterInternal( Dungeon.hero.characterName() );
	}

	private static void registerCurrentCharacterInternal( String playerName ) throws IOException {
		String characterId = Dungeon.wayfarerCharacterId();
		int slot = GamesInProgress.curSlot;
		Map<String, Object> deletionCheck = new LinkedHashMap<>();
		deletionCheck.put( "requested_character_id", characterId );
		JsonValue deletionPending = request( "POST",
				"/rest/v1/rpc/wayfarer_character_deletion_pending", deletionCheck, session.accessToken );
		if (deletionPending != null && deletionPending.asBoolean()) {
			scheduleCurrentCharacterDeletion( characterId, slot );
			throw new CharacterDeletionApprovedException();
		}
		try {
			WayfarerMessageCrypto.ensureCharacterKeys();
		} catch (Exception error) {
			throw new IOException( "Unable to prepare secure messaging for this character.", error );
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put( "requested_character_id", characterId );
		body.put( "requested_player_name", playerName );
		body.put( "requested_hero_class", Dungeon.hero.heroClass.name() );
		body.put( "requested_hero_level", Dungeon.hero.lvl );
		// Remote portraits should match the armor currently shown by the hero avatar.
		body.put( "requested_head_sprite", Dungeon.hero.tier() );
		body.put( "requested_app_version", Game.version );
		body.put( "requested_public_encryption_key", Dungeon.wayfarerPublicKey() );
		try {
			request( "POST", "/rest/v1/rpc/wayfarer_register_character", body, session.accessToken );
		} catch (IOException error) {
			if (nameTakenMessage( error.getMessage() )) throw new CharacterNameTakenException();
			throw error;
		}
		registeredCharacterId = characterId;
		registeredCharacterName = playerName;
	}

	private static void scheduleCharacterNamePrompt() {
		if (characterNamePromptActive) return;
		characterNamePromptActive = true;
		Game.runOnRenderThread( () -> {
			if (!(ShatteredPixelDungeon.scene() instanceof GameScene) || !currentCharacterEligible()) {
				characterNamePromptActive = false;
				return;
			}
			GameScene.show( new WndWayfarerName() );
		} );
	}

	private static boolean nameTakenMessage( String message ) {
		if (message == null) return false;
		String normalized = message.toLowerCase();
		return normalized.contains( "wayfarer_name_taken" )
				|| normalized.contains( "wayfarer_active_character_name_uidx" )
				|| normalized.contains( "username is already taken" );
	}

	private static void scheduleCurrentCharacterDeletion( final String characterId, final int slot ) {
		final DeletionTarget target = new DeletionTarget( slot, characterId );
		Game.runOnRenderThread( () -> {
			if (!(ShatteredPixelDungeon.scene() instanceof GameScene)
					|| !target.matches( GamesInProgress.curSlot, Dungeon.wayfarerCharacterId() )) return;
			if (deletionPrompt != null && deletionPrompt.parent != null) return;
			deletionPrompt = new WndCharacterDeletion( slot, characterId );
			GameScene.show( deletionPrompt );
		} );
	}

	public static void acknowledgeCharacterDeletion( final int slot, final String characterId, final ResultCallback callback ) {
		final DeletionTarget target = new DeletionTarget( slot, characterId );
		if (!target.matches( GamesInProgress.curSlot, Dungeon.wayfarerCharacterId() )) {
			callback.completed( new Result( false, "The active character changed. Reopen the character to read its notice." ) ); return;
		}
		startNetworkTask( new Thread( () -> {
			boolean acquired = false; Result result;
			try {
				if (!(acquired = acquireBusy( 5000 ))) throw new IOException( "Unable to connect. Please try again." );
				ensureFreshSession();
				Map<String, Object> body = new LinkedHashMap<>(); body.put( "requested_character_id", characterId );
				JsonValue response = request( "POST", "/rest/v1/rpc/wayfarer_acknowledge_character_deletion", body, session.accessToken );
				if (response == null || !response.asBoolean()) throw new IOException( "The deletion approval could not be confirmed. Please reconnect." );
				result = new Result( true, "Acknowledged." );
			} catch (Exception error) { result = new Result( false, friendlyMessage( error ) ); }
			finally { if (acquired) busy = false; }
			final Result completed = result;
			Game.runOnRenderThread( () -> {
				if (!target.matches( GamesInProgress.curSlot, Dungeon.wayfarerCharacterId() )
						|| !(ShatteredPixelDungeon.scene() instanceof GameScene)) return;
				if (!completed.success) { callback.completed( completed ); return; }
				acknowledgedDeletion = target;
				WayfarerPresenceService.pause(); SPDSettings.wayfarerVisible( false );
				GameScene.endActorThread();
				ShatteredPixelDungeon.switchScene( StartScene.class );
			} );
		}, "Wayfarer Deletion Acknowledgement" ) );
	}

	// StartScene calls this after GameScene has stopped its actor thread, before listing saves.
	public static void finishAcknowledgedDeletion() {
		DeletionTarget target = acknowledgedDeletion;
		acknowledgedDeletion = null; deletionPrompt = null;
		if (target == null) return;
		try {
			String savedId = FileUtils.bundleFromFile( GamesInProgress.gameFile( target.slot ) ).getString( "wayfarer_character_id" );
			if (!target.matches( target.slot, savedId )) throw new IOException( "The save identity changed; no save was deleted." );
			// Startup recovery must not restore an interrupted save over the deletion marker.
			String pendingSave = GamesInProgress.gameFile( target.slot ) + ".spdtmp";
			if (FileUtils.fileExists( pendingSave ) && !FileUtils.deleteFile( pendingSave ))
				throw new IOException( "The interrupted save could not be removed. Reopen this character to retry." );
			Dungeon.deleteGame( target.slot, true );
			if (GamesInProgress.gameExists( target.slot )) throw new IOException( "The save could not be removed. Reopen it to retry the deletion notice." );
		} catch (Exception error) { deletionNotice = error.getMessage(); }
	}

	public static String consumeDeletionNotice() {
		String notice = deletionNotice;
		deletionNotice = null;
		return notice;
	}

	private static void ensureFreshSession() throws IOException {
		if (session == null) throw new IOException( "Sign in first." );
		if (System.currentTimeMillis() < session.expiresAtMillis) return;

		refreshSession();
		persistSession();
	}

	private static void refreshSession() throws IOException {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put( "refresh_token", session.refreshToken );
		JsonValue json = request( "POST", "/auth/v1/token?grant_type=refresh_token", body, null );
		Session refreshed = sessionFromResponse( json, session.email );
		refreshed.accountName = session.accountName;
		refreshed.accountStatus = session.accountStatus;
		session = refreshed;
	}

	private static Session sessionFromResponse( JsonValue json, String fallbackEmail ) throws IOException {
		Session authenticated = new Session();
		authenticated.accessToken = requiredString( json, "access_token" );
		authenticated.refreshToken = requiredString( json, "refresh_token" );
		authenticated.expiresAtMillis = System.currentTimeMillis()
				+ Math.max( 30, json.getLong( "expires_in", 3600 ) - 30 ) * 1000L;
		JsonValue user = json.get( "user" );
		if (user == null) throw new IOException( "Supabase did not return the account." );
		authenticated.userId = requiredString( user, "id" );
		authenticated.email = user.getString( "email", fallbackEmail == null ? "" : fallbackEmail.trim() );
		return authenticated;
	}

	private static void persistSession() {
		if (session != null && session.refreshToken != null) {
			SPDSettings.wayfarerRefreshToken( session.refreshToken );
		}
	}

	private static JsonValue request( String method, String path, Object body, String bearer ) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)new URL( PROJECT_URL + path ).openConnection();
		connection.setRequestMethod( method );
		connection.setConnectTimeout( CONNECT_TIMEOUT_MS );
		connection.setReadTimeout( READ_TIMEOUT_MS );
		connection.setRequestProperty( "apikey", PUBLISHABLE_KEY );
		connection.setRequestProperty( "Accept", "application/json" );
		if (bearer != null && !bearer.isEmpty()) {
			connection.setRequestProperty( "Authorization", "Bearer " + bearer );
		}

		if (body != null) {
			byte[] bytes = jsonObject( body ).getBytes( StandardCharsets.UTF_8 );
			connection.setDoOutput( true );
			connection.setRequestProperty( "Content-Type", "application/json" );
			connection.setFixedLengthStreamingMode( bytes.length );
			try (OutputStream output = connection.getOutputStream()) {
				output.write( bytes );
			}
		}

		int status = connection.getResponseCode();
		String response = read( status >= 200 && status < 300
				? connection.getInputStream() : connection.getErrorStream() );
		connection.disconnect();

		if (status < 200 || status >= 300) {
			String message = response;
			try {
				JsonValue error = new JsonReader().parse( response );
				message = error.getString( "msg",
						error.getString( "message", error.getString( "error_description", response ) ) );
			} catch (Exception ignored) {
			}
			throw new IOException( message == null || message.isEmpty()
					? "Connection failed (HTTP " + status + ")." : message );
		}

		if (response == null || response.trim().isEmpty()) return null;
		return new JsonReader().parse( response );
	}

	private static String read( InputStream input ) throws IOException {
		if (input == null) return "";
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader( input, StandardCharsets.UTF_8 ) )) {
			String line;
			while ((line = reader.readLine()) != null) result.append( line );
		}
		return result.toString();
	}

	private static String requiredString( JsonValue json, String name ) throws IOException {
		String value = json == null ? null : json.getString( name, null );
		if (value == null || value.isEmpty()) throw new IOException( "Invalid response from Supabase." );
		return value;
	}

	private static JsonValue firstRow( JsonValue value ) {
		return value != null && value.isArray() ? value.child : value;
	}

	private static String jsonObject( Object body ) throws IOException {
		if (!(body instanceof Map)) throw new IOException( "Invalid request body." );
		JsonValue root = new JsonValue( JsonValue.ValueType.object );
		for (Map.Entry<?, ?> entry : ((Map<?, ?>)body).entrySet()) {
			Object value = entry.getValue();
			JsonValue child;
			if (value instanceof Boolean) {
				child = new JsonValue( (Boolean)value );
			} else if (value instanceof Byte || value instanceof Short
					|| value instanceof Integer || value instanceof Long) {
				child = new JsonValue( ((Number)value).longValue() );
			} else if (value instanceof Number) {
				child = new JsonValue( ((Number)value).doubleValue() );
			} else if (value == null) {
				child = new JsonValue( JsonValue.ValueType.nullValue );
			} else {
				child = new JsonValue( String.valueOf( value ) );
			}
			child.name = String.valueOf( entry.getKey() );
			root.addChild( child );
		}
		return root.toJson( JsonWriter.OutputType.json );
	}

	private static String urlEncode( String value ) {
		return value.replace( "%", "%25" ).replace( "+", "%2B" )
				.replace( " ", "%20" ).replace( "#", "%23" );
	}

	private static String friendlyMessage( Exception error ) {
		String message = error.getMessage();
		if (message == null || message.trim().isEmpty()) return "Unable to connect to Supabase.";
		if (error instanceof CharacterNameTakenException || nameTakenMessage( message )) {
			return "That username is already taken.";
		}
		if (message.toLowerCase().contains( "invalid login credentials" )) {
			return "The email or password is incorrect.";
		}
		if (message.toLowerCase().contains( "email rate limit exceeded" )
				|| message.toLowerCase().contains( "email rate exceeded" )) {
			return "The authentication email limit has been reached. Wait for the current hourly limit to reset or raise the Auth email rate limit in Supabase.";
		}
		return message;
	}

	public static String passwordProblem( String password ) {
		if (password == null || password.length() < 6) {
			return "Password must contain at least 6 characters.";
		}
		boolean uppercase = false;
		boolean lowercase = false;
		boolean number = false;
		boolean special = false;
		for (int i = 0; i < password.length(); i++) {
			char character = password.charAt( i );
			if (Character.isUpperCase( character )) uppercase = true;
			else if (Character.isLowerCase( character )) lowercase = true;
			else if (Character.isDigit( character )) number = true;
			else if (!Character.isWhitespace( character )) special = true;
		}
		if (!uppercase || !lowercase || !number || !special) {
			return "Password must include an uppercase letter, a lowercase letter, a number, and a special character.";
		}
		return null;
	}

	private static void deliver( final ResultCallback callback, final Result result ) {
		if (callback == null) return;
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				callback.completed( result );
			}
		} );
	}

	private static void deliverNearby( final NearbyPlayersCallback callback, final Result result,
			final ArrayList<NearbyPlayer> players ) {
		if (callback == null) return;
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				callback.completed( result, players );
			}
		} );
	}

	private static void deliverMessages( final MessagesCallback callback, final Result result,
			final ArrayList<WayfarerMessage> messages ) {
		if (callback == null) return;
		Game.runOnRenderThread( new Callback() {
			@Override public void call() { callback.completed( result, messages ); }
		} );
	}

	private static void deliverModeratorMessages( final ModeratorMessagesCallback callback,
			final Result result, final ArrayList<ModeratorSpaceMessage> messages,
			final String characterId ) {
		if (callback == null) return;
		Game.runOnRenderThread( () -> {
			if (characterId.equals( Dungeon.wayfarerCharacterId() )) callback.completed( result, messages );
		} );
	}

	private static void deliverBoolean( final BooleanCallback callback, final Result result,
			final boolean value ) {
		if (callback == null) return;
		Game.runOnRenderThread( () -> callback.completed( result, value ) );
	}

	private static void deliverReports( final ReportsCallback callback, final Result result,
			final ArrayList<WayfarerReport> reports ) {
		if (callback == null) return;
		Game.runOnRenderThread( new Callback() {
			@Override public void call() { callback.completed( result, reports ); }
		} );
	}
}
