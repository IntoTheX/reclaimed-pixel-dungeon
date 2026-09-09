/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.SpatialGeode;
import com.erebus.reclaimedpixeldungeon.items.SpecialChestLoot;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.materials.ForgeResourceMaterial;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfNullbrand;
import com.erebus.reclaimedpixeldungeon.rewards.GameplayRewards;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public final class WayfarerModeratorRewards {

	private static final long AFK_MILLIS = 3L * 60 * 1000;
	private static final long STATUS_MILLIS = 30_000;
	private static long lastActionAt;
	private static long lastAccountedAt;
	private static long nextStatusAt;
	private static long nextActivityReportAt;
	private static long pendingActiveMillis;
	private static boolean reporting;
	private static boolean loadingStatus;
	private static WayfarerAccountService.ModeratorRewardStatus status;
	private static String statusCharacterId = "";

	private WayfarerModeratorRewards() {}

	public interface StatusCallback {
		void completed( WayfarerAccountService.Result result,
				WayfarerAccountService.ModeratorRewardStatus status );
	}

	public static void recordActivity() {
		GameplayRewards.recordActivity();
		ensureCurrentCharacter();
		if (!eligible()) {
			resetActivityClock();
			return;
		}
		long now = System.currentTimeMillis();
		if (lastActionAt == 0 || now - lastActionAt > AFK_MILLIS) {
			accrueActiveTime( now );
			lastActionAt = now;
			lastAccountedAt = now;
			return;
		}
		accrueActiveTime( now );
		lastActionAt = now;
	}

	public static void poll() {
		ensureCurrentCharacter();
		if (!eligible()) {
			resetActivityClock();
			return;
		}
		long now = System.currentTimeMillis();
		accrueActiveTime( now );
		int availableSeconds = (int)Math.min( Integer.MAX_VALUE, pendingActiveMillis / 1000L );
		if (!reporting && availableSeconds > 0 && now >= nextActivityReportAt
				&& !WayfarerAccountService.isBusy()) {
			final int submitted = Math.min( 180, availableSeconds );
			reporting = true;
			nextActivityReportAt = now + 30_000;
			WayfarerAccountService.reportModeratorActivity( submitted, (result, accepted) -> {
				reporting = false;
				if (result.success) {
					pendingActiveMillis = Math.max( 0L,
							pendingActiveMillis - Math.max( 0, accepted ) * 1000L );
					nextStatusAt = 0;
				} else if (result.message != null && result.message.toLowerCase()
						.contains( "visible online presence" )) {
					WayfarerPresenceService.refreshVisiblePresence();
				}
			} );
		}
		if (!loadingStatus && now >= nextStatusAt && !WayfarerAccountService.isBusy()) {
			refreshStatus( null );
		}
	}

	public static boolean hasClaimableReward() {
		return status != null && status.hasClaimableReward();
	}

	public static WayfarerAccountService.ModeratorRewardStatus cachedStatus() {
		return status;
	}

	public static void refreshStatus( final StatusCallback callback ) {
		ensureCurrentCharacter();
		if (loadingStatus) return;
		loadingStatus = true;
		nextStatusAt = System.currentTimeMillis() + STATUS_MILLIS;
		WayfarerAccountService.moderatorRewardStatus( (result, value) -> {
			loadingStatus = false;
			if (result.success) status = value;
			if (callback != null) callback.completed( result, value );
		} );
	}

	public static ArrayList<Item> selectionOptions( WayfarerAccountService.ModeratorRewardClaim claim ) {
		ArrayList<Item> result = new ArrayList<>();
		if (claim == null || !("artifact".equals( claim.rewardKey ) || "trinket".equals( claim.rewardKey ))) return result;
		Class<?>[] classes = "artifact".equals( claim.rewardKey )
				? Generator.Category.ARTIFACT.classes : Generator.Category.TRINKET.classes;
		Random.pushGenerator( claim.seed );
		try {
			HashSet<Integer> used = new HashSet<>();
			while (result.size() < 3 && used.size() < classes.length) {
				int index = Random.Int( classes.length );
				if (!used.add( index )) continue;
				@SuppressWarnings("unchecked")
				Item item = (Item)Reflection.newInstance( (Class<? extends Item>)classes[index] );
				result.add( item.random().randomizeRarityStats().identify() );
			}
		} finally {
			Random.popGenerator();
		}
		return result;
	}

	public static void deliver( final WayfarerAccountService.ModeratorRewardClaim claim,
			final WayfarerAccountService.ResultCallback callback ) {
		if (claim == null || claim.claimId.isEmpty() || claim.selectedOption == -2) {
			callback.completed( new WayfarerAccountService.Result( false, "Choose a reward first." ) );
			return;
		}
		try {
			JsonValue journal = journal();
			String stage = journal.getString( claim.claimId, "" );
			if (!"delivered".equals( stage )) {
				if (!"pending".equals( stage )) {
					journal.addChild( claim.claimId, new JsonValue( "pending" ) );
					saveJournal( journal );
				}
				applyReward( claim );
				journal = journal();
				journal.remove( claim.claimId );
				journal.addChild( claim.claimId, new JsonValue( "delivered" ) );
				saveJournal( journal );
			}
		} catch (Exception error) {
			callback.completed( new WayfarerAccountService.Result( false, error.getMessage() ) );
			return;
		}
		WayfarerAccountService.acknowledgeModeratorReward( claim.claimId, (result, acknowledged) -> {
			if (result.success && acknowledged) {
				try {
					clearDeliveryMarkers( claim.claimId );
					JsonValue journal = journal();
					journal.remove( claim.claimId );
					saveJournal( journal );
					status = null;
					nextStatusAt = 0;
				} catch (IOException error) {
					callback.completed( new WayfarerAccountService.Result( false, error.getMessage() ) );
					return;
				}
			}
			callback.completed( result );
		} );
	}

	private static void clearDeliveryMarkers( String claimId ) {
		String prefix = "moderator-reward:" + claimId + ":";
		if (Dungeon.hero != null) {
			for (Item item : Dungeon.hero.belongings) {
				if (item.wayfarerDeliveryId().startsWith( prefix )) item.wayfarerDeliveryId( "" );
			}
		}
		if (Dungeon.level != null) {
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				for (Item item : heap.items) {
					if (item.wayfarerDeliveryId().startsWith( prefix )) item.wayfarerDeliveryId( "" );
				}
			}
		}
	}

	private static void applyReward( WayfarerAccountService.ModeratorRewardClaim claim ) throws IOException {
		if (Dungeon.hero == null || Dungeon.level == null || Dungeon.homebase == null) {
			throw new IOException( "Enter the active character before claiming this reward." );
		}
		ArrayList<Item> items = new ArrayList<>();
		Random.pushGenerator( claim.seed );
		try {
			if ("welcome".equals( claim.period )) items.add( new SpatialGeode() );
			switch (claim.rewardKey) {
				case "artifact": case "trinket":
					ArrayList<Item> choices = selectionOptions( claim );
					if (claim.selectedOption >= choices.size()) throw new IOException( "That reward choice is invalid." );
					items.add( choices.get( claim.selectedOption ) );
					break;
				case "catalysts":
					items.addAll( SpecialChestLoot.catalysts( amountFor( claim, 25, 5, 50 ), Math.max( 1, Dungeon.depth ) ) );
					break;
				case "resources":
					addResources( SpecialChestLoot.resources( amountFor( claim, 100, 20, 1000 ), Math.max( 1, Dungeon.depth ) ) );
					break;
				case "emeralds": Dungeon.homebase.addEmeralds( 2 ); break;
				case "geode": items.add( new SpatialGeode() ); break;
				case "sparks": items.add( new StoneOfNullbrand().quantity( 5 ) ); break;
				default: throw new IOException( "That moderator reward is not supported." );
			}
		} finally {
			Random.popGenerator();
		}
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get( i ).identify();
			item.wayfarerDeliveryId( "moderator-reward:" + claim.claimId + ":" + i );
			if (!item.collect( Dungeon.hero.belongings.backpack )) {
				Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
			}
		}
	}

	private static int amountFor( WayfarerAccountService.ModeratorRewardClaim claim,
			int welcome, int daily, int weekly ) {
		return "welcome".equals( claim.period ) ? welcome : "daily".equals( claim.period ) ? daily : weekly;
	}

	private static void addResources( ArrayList<Item> resources ) {
		for (Item item : resources) {
			if (item instanceof BuildingMaterial) {
				Dungeon.homebase.add( ((BuildingMaterial)item).material(), item.quantity() );
			} else if (item instanceof ForgeResourceMaterial) {
				Dungeon.homebase.addForgeResource( ((ForgeResourceMaterial)item).resource(), item.quantity() );
			}
		}
	}

	private static boolean eligible() {
		return WayfarerAccountService.isSignedIn() && WayfarerAccountService.isModeratorAccount()
				&& WayfarerAccountService.currentCharacterEligible() && SPDSettings.wayfarerVisible()
				&& Dungeon.hero != null && Dungeon.level != null;
	}

	private static void accrueActiveTime( long now ) {
		long accrued = activeMillisBetween( lastActionAt, lastAccountedAt, now );
		if (accrued <= 0) return;
		pendingActiveMillis += accrued;
		lastAccountedAt += accrued;
	}

	static long activeMillisBetween( long lastAction, long lastAccounted, long now ) {
		if (lastAction <= 0 || lastAccounted <= 0 || now <= lastAccounted) return 0;
		return Math.max( 0, Math.min( now, lastAction + AFK_MILLIS ) - lastAccounted );
	}

	private static void resetActivityClock() {
		lastActionAt = 0;
		lastAccountedAt = 0;
	}

	static void networkTaskFailed() {
		reporting = false;
		loadingStatus = false;
		long retryAt = System.currentTimeMillis() + 5_000L;
		nextActivityReportAt = retryAt;
		nextStatusAt = retryAt;
	}

	private static void ensureCurrentCharacter() {
		String current = Dungeon.hero == null ? "" : Dungeon.wayfarerCharacterId();
		if (current.equals( statusCharacterId )) return;
		statusCharacterId = current;
		status = null;
		resetActivityClock();
		pendingActiveMillis = 0;
		nextStatusAt = 0;
		nextActivityReportAt = 0;
		reporting = false;
		loadingStatus = false;
	}

	private static JsonValue journal() {
		try {
			return new JsonReader().parse( Dungeon.moderatorRewardJournal == null
					? "{}" : Dungeon.moderatorRewardJournal );
		} catch (RuntimeException error) {
			Game.reportException( error );
			return new JsonValue( JsonValue.ValueType.object );
		}
	}

	private static void saveJournal( JsonValue journal ) throws IOException {
		Dungeon.moderatorRewardJournal = journal.toJson( JsonWriter.OutputType.json );
		Dungeon.saveAll();
	}
}
