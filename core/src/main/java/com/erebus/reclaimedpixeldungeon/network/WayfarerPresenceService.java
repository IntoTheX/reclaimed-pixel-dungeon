/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class WayfarerPresenceService {
	private static final long HEARTBEAT_SECONDS = 60;
	private static final long INBOX_SECONDS = 5;
	private static final long CONTACT_STATUS_SECONDS = 31;
	private static final long LOCATION_REFRESH_MILLIS = 15L * 60 * 1000;
	private static ScheduledExecutorService heartbeat;
	private static volatile boolean paused;
	private static volatile double lastLatitude;
	private static volatile double lastLongitude;
	private static volatile double publicMapLatitude;
	private static volatile double publicMapLongitude;
	private static volatile boolean enabling;
	private static volatile boolean refreshing;
	private static volatile boolean presencePublishing;
	private static volatile long lastLocationAt;
	private static volatile boolean moderationPolling;
	private static volatile long nextModerationPoll;
	private static volatile boolean tradePolling;
	private static volatile long nextTradePoll;
	private static volatile boolean moderatorRolePolling;
	private static volatile boolean moderatorSpacePolling;
	private static volatile long nextModeratorSpacePoll;

	private WayfarerPresenceService() {}

	public static void nearbyPlayers( final WayfarerAccountService.NearbyPlayersCallback callback ) {
		if (!SPDSettings.wayfarerVisible()) {
			deliverNearbyFailure( callback, "Become visible before searching for nearby players." );
			return;
		}
		if (Game.platform == null || !Game.platform.supportsLocation()) {
			deliverNearbyFailure( callback, "Nearby-player discovery is not available on this platform yet." );
			return;
		}
		Game.platform.requestApproximateLocation( new PlatformSupport.LocationCallback() {
			@Override public void onLocation( double latitude, double longitude ) {
				updateLocation( latitude, longitude );
				WayfarerAccountService.publishPresence( latitude, longitude, presence -> {
					if (presence.success) {
						WayfarerAccountService.nearbyPlayers( latitude, longitude, callback );
					} else {
						deliverNearbyFailure( callback, presence.message );
					}
				} );
			}
			@Override public void onFailure( String message ) {
				deliverNearbyFailure( callback, message );
			}
		} );
	}

	public static void enable( final WayfarerAccountService.ResultCallback callback ) {
		if (Game.platform == null || !Game.platform.supportsLocation()) {
			SPDSettings.wayfarerVisible( false );
			deliverFailure( callback, "Automatic map location is not available on this platform. Visibility remains off." );
			return;
		}
		enabling = true;
		publishVisiblePresence( result -> {
			enabling = false;
			SPDSettings.wayfarerVisible( result.success );
			if (result.success) startHeartbeat();
			if (callback != null) callback.completed( result );
		} );
	}

	public static void disable( final WayfarerAccountService.ResultCallback callback ) {
		SPDSettings.wayfarerVisible( false );
		stopHeartbeat();
		WayfarerAccountService.goOffline( callback );
	}

	public static void pause() {
		paused = true;
		stopHeartbeat();
		if (SPDSettings.wayfarerVisible() && !enabling) {
			WayfarerAccountService.goOffline( result -> {
				if (!paused) resume();
			} );
		}
	}

	public static void resume() {
		paused = false;
		if (Game.platform == null) return;
		if (Dungeon.hero == null || Dungeon.level == null || heartbeat != null) return;
		if (WayfarerAccountService.isSignedIn() && WayfarerAccountService.currentCharacterEligible()) {
			WayfarerAccountService.ensureCurrentCharacterRegistration();
		}
		if (SPDSettings.wayfarerVisible() && WayfarerAccountService.isSignedIn() && !refreshing) {
			refreshing = true;
			publishVisiblePresence( result -> {
				refreshing = false;
				if (result.success && !paused) startHeartbeat();
			} );
		}
	}

	public static void pollModerationNotifications() {
		WayfarerAccountService.pollCharacterEnds();
		WayfarerModeratorRewards.poll();
		pollGlobalTrades();
		pollModeratorSpace();
		long now = System.currentTimeMillis();
		if (moderationPolling || now < nextModerationPoll || !WayfarerAccountService.isSignedIn()
				|| !WayfarerAccountService.currentCharacterEligible()) return;
		moderationPolling = true;
		nextModerationPoll = now + INBOX_SECONDS * 1000L;
		WayfarerAccountService.receiveModerationNotifications( (result, notifications) -> {
			moderationPolling = false;
			if (result.success) WayfarerChatStore.acceptModeration( notifications );
		} );
	}

	private static void pollModeratorSpace() {
		long now = System.currentTimeMillis();
		if (!WayfarerAccountService.isSignedIn() || !WayfarerAccountService.currentCharacterEligible()) return;
		if (!WayfarerAccountService.moderatorStatusKnown()) {
			if (moderatorRolePolling || WayfarerAccountService.isBusy()) return;
			moderatorRolePolling = true;
			WayfarerAccountService.moderatorStatus( (result, moderator) -> {
				moderatorRolePolling = false;
				if (result.success && moderator) nextModeratorSpacePoll = 0;
			} );
			return;
		}
		if (!WayfarerAccountService.isModeratorAccount() || moderatorSpacePolling
				|| now < nextModeratorSpacePoll || WayfarerAccountService.isBusy()) return;
		moderatorSpacePolling = true;
		nextModeratorSpacePoll = now + INBOX_SECONDS * 1000L;
		long afterMessageId = WayfarerChatStore.lastModeratorMessageId();
		boolean countUnread = WayfarerChatStore.moderatorSpaceInitialized();
		WayfarerAccountService.receiveModeratorSpaceMessages( afterMessageId, (result, messages) -> {
			moderatorSpacePolling = false;
			if (result.success) WayfarerChatStore.acceptModeratorSpace( messages, countUnread );
		} );
	}

	private static void pollGlobalTrades() {
		long now=System.currentTimeMillis();
		if(tradePolling || now<nextTradePoll || WayfarerAccountService.isBusy() || !WayfarerAccountService.isSignedIn()
				|| !WayfarerAccountService.currentCharacterEligible())return;
		tradePolling=true;
		nextTradePoll=now+5000;
		WayfarerAccountService.pendingTrades((result,updates)->{
			tradePolling=false;
			if(result.success)WayfarerChatStore.acceptTrades(updates);
		});
	}

	private static synchronized void startHeartbeat() {
		stopHeartbeat();
		refreshContactPresence();
		ThreadFactory factory = runnable -> {
			Thread thread = new Thread( runnable, "Wayfarer Presence Heartbeat" );
			thread.setDaemon( true );
			return thread;
		};
		heartbeat = Executors.newSingleThreadScheduledExecutor( factory );
		heartbeat.scheduleAtFixedRate( () -> {
			refreshVisiblePresence();
		}, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS );
		heartbeat.scheduleAtFixedRate( () -> {
			if (SPDSettings.wayfarerVisible() && WayfarerAccountService.isSignedIn()
					&& !WayfarerAccountService.isBusy()) {
				WayfarerAccountService.receiveMessages( (result, messages) -> {
					if (result.success) WayfarerChatStore.accept( messages, null );
				} );
			}
		}, INBOX_SECONDS, INBOX_SECONDS, TimeUnit.SECONDS );
		heartbeat.scheduleAtFixedRate( () -> {
			refreshContactPresence();
		}, CONTACT_STATUS_SECONDS, CONTACT_STATUS_SECONDS, TimeUnit.SECONDS );
	}

	private static void refreshContactPresence() {
		String ids = WayfarerChatStore.contactIds();
		if (SPDSettings.wayfarerVisible() && WayfarerAccountService.isSignedIn()
				&& !WayfarerAccountService.isBusy() && !ids.isEmpty()) {
			WayfarerAccountService.contactPresence( ids, (result, players) -> {
				if (result.success) WayfarerChatStore.updateContacts( players );
			} );
		}
	}

	private static synchronized void stopHeartbeat() {
		if (heartbeat != null) heartbeat.shutdownNow();
		heartbeat = null;
	}

	static void networkTaskFailed() {
		moderationPolling = false;
		tradePolling = false;
		moderatorRolePolling = false;
		moderatorSpacePolling = false;
		presencePublishing = false;
		enabling = false;
		refreshing = false;
		long retryAt = System.currentTimeMillis() + INBOX_SECONDS * 1000L;
		nextModerationPoll = retryAt;
		nextTradePoll = retryAt;
		nextModeratorSpacePoll = retryAt;
	}

	public static void refreshVisiblePresence() {
		if (!paused && SPDSettings.wayfarerVisible() && WayfarerAccountService.isSignedIn()) {
			publishVisiblePresence( null );
		}
	}

	private static void publishVisiblePresence( final WayfarerAccountService.ResultCallback callback ) {
		synchronized (WayfarerPresenceService.class) {
			if (presencePublishing) {
				deliverFailure( callback, "A presence refresh is already in progress." );
				return;
			}
			presencePublishing = true;
		}
		requestAndPublish( result -> {
			presencePublishing = false;
			if (callback != null) callback.completed( result );
		} );
	}

	private static void requestAndPublish( final WayfarerAccountService.ResultCallback callback ) {
		if (Game.platform == null) {
			deliverFailure( callback, "Location services are not ready yet." );
			return;
		}
		if (lastLocationAt > 0 && System.currentTimeMillis() - lastLocationAt < LOCATION_REFRESH_MILLIS) {
			WayfarerAccountService.publishPresence( lastLatitude, lastLongitude, callback );
			return;
		}
		Game.platform.requestApproximateLocation( new PlatformSupport.LocationCallback() {
			@Override public void onLocation( double latitude, double longitude ) {
				updateLocation( latitude, longitude );
				lastLocationAt = System.currentTimeMillis();
				WayfarerAccountService.publishPresence( latitude, longitude, callback );
			}
			@Override public void onFailure( String message ) { deliverFailure( callback, message ); }
		} );
	}

	private static void deliverFailure( WayfarerAccountService.ResultCallback callback, String message ) {
		if (callback != null) callback.completed( new WayfarerAccountService.Result( false, message ) );
	}

	public static double publicMapLatitude() { return publicMapLatitude; }
	public static double publicMapLongitude() { return publicMapLongitude; }

	private static void updateLocation( double latitude, double longitude ) {
		lastLatitude = latitude;
		lastLongitude = longitude;
		double[] publicLocation = displacedMapLocation( Dungeon.wayfarerCharacterId(), latitude, longitude );
		publicMapLatitude = publicLocation[0];
		publicMapLongitude = publicLocation[1];
	}

	static double[] displacedMapLocation( String characterId, double latitude, double longitude ) {
		if (characterId == null || characterId.isEmpty()) return new double[]{latitude, longitude};
		try {
			byte[] hash = MessageDigest.getInstance( "MD5" ).digest(
					characterId.toLowerCase( Locale.ENGLISH ).getBytes( StandardCharsets.UTF_8 ) );
			double angle = (((hash[0] & 0xFF) * 256 + (hash[1] & 0xFF)) / 65535.0)
					* 2 * Math.PI;
			double distance = 400 + ((hash[2] & 0xFF) % 101);
			double displacedLatitude = latitude + distance * Math.cos( angle ) / 111320.0;
			double longitudeScale = Math.max( 0.15, Math.abs( Math.cos( Math.toRadians( latitude ) ) ) );
			double displacedLongitude = longitude + distance * Math.sin( angle )
					/ (111320.0 * longitudeScale);
			return new double[]{roundCoordinate( displacedLatitude ), roundCoordinate( displacedLongitude )};
		} catch (NoSuchAlgorithmException ignored) {
			return new double[]{latitude, longitude};
		}
	}

	private static double roundCoordinate( double coordinate ) {
		return BigDecimal.valueOf( coordinate ).setScale( 5, RoundingMode.HALF_UP ).doubleValue();
	}

	private static void deliverNearbyFailure( WayfarerAccountService.NearbyPlayersCallback callback,
			String message ) {
		if (callback != null) callback.completed(
				new WayfarerAccountService.Result( false, message ), new java.util.ArrayList<>() );
	}
}
