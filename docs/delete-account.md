---
layout: default
title: Delete Wayfarer Account
permalink: /delete-account/
---

# Delete Your Wayfarer Account

Use this page to request deletion of the **Wayfarer Network account** associated with Reclaimed Pixel Dungeon.

Deleting a Wayfarer account removes the online account and ordinary account data that is no longer required. It does **not automatically delete local Reclaimed Pixel Dungeon save files stored on your device**.

> **Safety and abuse prevention:** Limited moderation or enforcement records may be retained where reasonably necessary to prevent abuse, sanction evasion, fraud, or repeated safety violations. Retained information is restricted to those purposes and is not used for advertising or marketing.

## Before you continue

- You must sign in using the email address and password for the Wayfarer account you want to delete.
- Requesting deletion is permanent once final processing has begun.
- Active Wayfarer trades may need to be safely settled before final deletion can complete so another player does not lose escrowed items.
- Your current request status can be checked from this page while the account still exists.
- If you only want to stop appearing on the Wayfarer map, you do **not** need to delete your account. Disable **Wayfarer Visibility** in the game instead.

<div id="wayfarer-delete-app" class="wf-card">
  <h2>Wayfarer Account Deletion</h2>

  <label for="wf-email"><strong>Wayfarer account email</strong></label>
  <input id="wf-email" class="wf-input" type="email" autocomplete="username" inputmode="email" placeholder="you@example.com">

  <label for="wf-password"><strong>Password</strong></label>
  <input id="wf-password" class="wf-input" type="password" autocomplete="current-password" placeholder="Your Wayfarer password">

  <label class="wf-confirm">
    <input id="wf-confirm" type="checkbox">
    <span>I understand that this requests permanent deletion of my Wayfarer online account and that limited safety/moderation records may be retained for abuse-prevention purposes.</span>
  </label>

  <div class="wf-actions">
    <button id="wf-request" class="wf-button wf-danger" type="button">Request Account Deletion</button>
    <button id="wf-status" class="wf-button" type="button">Check Request Status</button>
    <button id="wf-cancel" class="wf-button" type="button">Cancel Pending Request</button>
  </div>

  <div id="wf-result" class="wf-result" role="status" aria-live="polite"></div>
</div>

## What happens after a request?

1. The request is recorded against your authenticated Wayfarer account.
2. The request is reviewed/processed by the Wayfarer deletion service.
3. If an active Global Trade contains escrowed items, final deletion may be delayed until the trade can be safely resolved.
4. When final deletion is completed, the Wayfarer online account and ordinary account data that no longer needs to be retained are deleted.
5. Limited moderation or enforcement information may remain only where necessary for security, fraud prevention, abuse prevention, or sanction-evasion prevention.

For privacy questions or help with an account deletion request, contact **erebuspl4ys@gmail.com**.

You can also review the [Reclaimed Pixel Dungeon Privacy Policy](../privacy-policy/).

<style>
.wf-card {
  max-width: 680px;
  margin: 1.5rem 0;
  padding: 1.25rem;
  border: 1px solid #d0d7de;
  border-radius: 10px;
  background: #f6f8fa;
}
.wf-card label { display: block; margin-top: 0.9rem; }
.wf-input {
  width: 100%;
  box-sizing: border-box;
  margin-top: 0.35rem;
  padding: 0.7rem 0.8rem;
  border: 1px solid #8c959f;
  border-radius: 6px;
  font: inherit;
  background: #fff;
  color: #24292f;
}
.wf-confirm {
  display: flex !important;
  gap: 0.65rem;
  align-items: flex-start;
  line-height: 1.45;
}
.wf-confirm input { margin-top: 0.25rem; }
.wf-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-top: 1rem;
}
.wf-button {
  border: 1px solid #8c959f;
  border-radius: 6px;
  padding: 0.65rem 0.9rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
  color: #24292f;
}
.wf-button:hover { background: #eef1f4; }
.wf-button:disabled { opacity: 0.55; cursor: not-allowed; }
.wf-danger {
  color: #fff;
  background: #cf222e;
  border-color: #a40e26;
}
.wf-danger:hover { background: #a40e26; }
.wf-result {
  display: none;
  margin-top: 1rem;
  padding: 0.8rem;
  border-radius: 6px;
  white-space: pre-wrap;
}
.wf-result.info { display: block; background: #ddf4ff; border: 1px solid #54aeff; }
.wf-result.success { display: block; background: #dafbe1; border: 1px solid #4ac26b; }
.wf-result.error { display: block; background: #ffebe9; border: 1px solid #ff8182; }
@media (prefers-color-scheme: dark) {
  .wf-card { background: #161b22; border-color: #30363d; }
  .wf-input, .wf-button { background: #0d1117; color: #e6edf3; border-color: #484f58; }
  .wf-button:hover { background: #21262d; }
}
</style>

<script>
(() => {
  "use strict";

  // These are public client credentials. Never place a Supabase service-role
  // key, secret key, database password, or moderator credential in this page.
  const SUPABASE_URL = "https://banqbcyyrlkwvyivbdfn.supabase.co";
  const SUPABASE_PUBLISHABLE_KEY = "sb_publishable_5wU9dOWG3zzejoEZ1ONvjQ_hKm2uc2K";

  const emailInput = document.getElementById("wf-email");
  const passwordInput = document.getElementById("wf-password");
  const confirmInput = document.getElementById("wf-confirm");
  const requestButton = document.getElementById("wf-request");
  const statusButton = document.getElementById("wf-status");
  const cancelButton = document.getElementById("wf-cancel");
  const resultBox = document.getElementById("wf-result");

  let accessToken = "";

  function setBusy(busy) {
    requestButton.disabled = busy;
    statusButton.disabled = busy;
    cancelButton.disabled = busy;
  }

  function show(message, kind = "info") {
    resultBox.className = "wf-result " + kind;
    resultBox.textContent = message;
  }

  function clearPassword() {
    passwordInput.value = "";
  }

  async function signIn() {
    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {
      throw new Error("Enter the Wayfarer account email and password first.");
    }

    const response = await fetch(
      SUPABASE_URL + "/auth/v1/token?grant_type=password",
      {
        method: "POST",
        headers: {
          "apikey": SUPABASE_PUBLISHABLE_KEY,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
      }
    );

    const payload = await response.json().catch(() => ({}));
    clearPassword();

    if (!response.ok || !payload.access_token) {
      throw new Error("We could not sign in to that Wayfarer account. Check the email/password and try again.");
    }

    accessToken = payload.access_token;
    return accessToken;
  }

  async function rpc(functionName, body = {}) {
    if (!accessToken) {
      await signIn();
    }

    const response = await fetch(
      SUPABASE_URL + "/rest/v1/rpc/" + functionName,
      {
        method: "POST",
        headers: {
          "apikey": SUPABASE_PUBLISHABLE_KEY,
          "Authorization": "Bearer " + accessToken,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      }
    );

    const payload = await response.json().catch(() => ({}));

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        accessToken = "";
      }
      const safeMessage = payload && typeof payload.message === "string"
        ? payload.message
        : "The Wayfarer service could not complete this request.";
      throw new Error(safeMessage);
    }

    return payload;
  }

  async function run(action) {
    setBusy(true);
    try {
      if (action === "request") {
        if (!confirmInput.checked) {
          throw new Error("Confirm that you understand the deletion request before continuing.");
        }

        const result = await rpc("wayfarer_request_account_deletion", {
          requested_source: "web"
        });

        if (!result.request_id) {
          throw new Error("Supabase did not return a deletion request ID.");
        }

        show("Deletion request received. Preparing the account for deletion…", "info");

        const finalizeResponse = await fetch(
          SUPABASE_URL + "/functions/v1/wayfarer-account-deletion",
          {
            method: "POST",
            headers: {
              "apikey": SUPABASE_PUBLISHABLE_KEY,
              "Authorization": "Bearer " + accessToken,
              "Content-Type": "application/json"
            },
            body: JSON.stringify({ request_id: result.request_id })
          }
        );

        const finalized = await finalizeResponse.json().catch(() => ({}));

        if (finalizeResponse.status === 409) {
          const tradeNote = finalized.unresolved_trades
            ? "\nUnresolved trades: " + finalized.unresolved_trades
            : "";
          show(
            (finalized.message || "The deletion request is waiting for manual review or safe trade settlement.") +
            "\nStatus: blocked" + tradeNote +
            "\nRequest ID: " + result.request_id,
            "info"
          );
          return;
        }

        if (!finalizeResponse.ok) {
          throw new Error(finalized.error || "The deletion request was saved, but automatic deletion could not finish. Please use Check Request Status or contact support.");
        }

        accessToken = "";
        show(
          (finalized.message || "Your Wayfarer online account has been deleted.") +
          "\nStatus: " + (finalized.status || "completed") +
          "\nRequest ID: " + result.request_id,
          "success"
        );
      } else if (action === "status") {
        const result = await rpc("wayfarer_account_deletion_status");
        if (!result || result.status === "none") {
          show("No Wayfarer account deletion request was found for this account.", "info");
        } else {
          const requestId = result.request_id ? "\nRequest ID: " + result.request_id : "";
          show(
            (result.message || "Deletion request found.") +
            "\nStatus: " + result.status + requestId,
            result.status === "completed" ? "success" : "info"
          );
        }
      } else if (action === "cancel") {
        const result = await rpc("wayfarer_cancel_account_deletion");
        show(
          result.message || (result.cancelled ? "Deletion request cancelled." : "No pending request was cancelled."),
          result.cancelled ? "success" : "info"
        );
      }
    } catch (error) {
      show(error && error.message ? error.message : "Something went wrong. Please try again.", "error");
    } finally {
      setBusy(false);
    }
  }

  requestButton.addEventListener("click", () => run("request"));
  statusButton.addEventListener("click", () => run("status"));
  cancelButton.addEventListener("click", () => run("cancel"));
})();
</script>

---

*Reclaimed Pixel Dungeon — IntoTheX / ErebusPlays*
