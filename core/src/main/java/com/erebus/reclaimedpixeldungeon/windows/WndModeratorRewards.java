/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.SpatialGeode;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfNullbrand;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerModeratorRewards;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventoryItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class WndModeratorRewards extends WndOptions {

	private final WayfarerAccountService.ModeratorRewardStatus rewardStatus;
	private final ArrayList<String> periods = new ArrayList<>();

	public static void open() {
		WayfarerModeratorRewards.refreshStatus( (result, status) -> {
			if (!result.success || status == null) {
				GameScene.show( notice( "Moderator Rewards", result.message, false ) );
				return;
			}
			if (status.pendingClaim != null) {
				resume( status.pendingClaim );
			} else {
				ArrayList<String> available = availablePeriods( status );
				if (available.size() == 1) GameScene.show( new WndRewardChoices( available.get( 0 ) ) );
				else GameScene.show( new WndModeratorRewards( status ) );
			}
		} );
	}

	public static void openServiceRecord() {
		WayfarerModeratorRewards.refreshStatus( (result, status) -> {
			if (!result.success || status == null) {
				GameScene.show( notice( "Moderator Service", result.message, false ) );
				return;
			}
			GameScene.show( new WndModeratorRewards( status ) );
		} );
	}

	private WndModeratorRewards( WayfarerAccountService.ModeratorRewardStatus status ) {
		super( new ItemSprite( ItemSpriteSheet.LOCKED_CHEST ), "Moderator Rewards",
				progressText( status ), periodOptions( status ) );
		rewardStatus = status;
		if (status.welcomeAvailable) periods.add( "welcome" );
		if (status.dailyClaimedCount < status.dailyEarnedCount) periods.add( "daily" );
		if (status.weeklyAvailable) periods.add( "weekly" );
	}

	@Override protected void onSelect( int index ) {
		if (index < 0 || index >= periods.size()) return;
		showChoices( periods.get( index ) );
	}

	private void showChoices( String period ) {
		GameScene.show( new WndRewardChoices( period ) );
	}

	private static void reserve( String period, String key ) {
		WayfarerAccountService.beginModeratorReward( period, key, (result, claim) -> {
			if (!result.success || claim == null) {
				GameScene.show( notice( "Reward Not Reserved", result.message, false ) );
				return;
			}
			resume( claim );
		} );
	}

	private static void resume( WayfarerAccountService.ModeratorRewardClaim claim ) {
		if (claim.selectedOption == -2) {
			GameScene.show( new WndRewardSelection( claim ) );
		} else {
			deliver( claim );
		}
	}

	private static void deliver( WayfarerAccountService.ModeratorRewardClaim claim ) {
		WayfarerModeratorRewards.deliver( claim, result -> GameScene.show( notice(
				result.success ? "Reward Claimed" : "Claim Paused",
				result.success ? "Your moderator reward was delivered. Items that did not fit were placed at your feet."
						: result.message, result.success ) ) );
	}

	private static WndOptions notice( String title, String message, boolean success ) {
		return new WndOptions( Icons.get( success ? Icons.CHANGES : Icons.WARNING ), title, message, "OK" );
	}

	private static String[] periodOptions( WayfarerAccountService.ModeratorRewardStatus status ) {
		ArrayList<String> options = new ArrayList<>();
		for (String period : availablePeriods( status )) options.add( "Claim " + title( period ) );
		if (options.isEmpty()) options.add( "No Reward Ready" );
		return options.toArray( new String[0] );
	}

	private static ArrayList<String> availablePeriods( WayfarerAccountService.ModeratorRewardStatus status ) {
		ArrayList<String> available = new ArrayList<>();
		if (status.welcomeAvailable) available.add( "welcome" );
		if (status.dailyClaimedCount < status.dailyEarnedCount) available.add( "daily" );
		if (status.weeklyAvailable) available.add( "weekly" );
		return available;
	}

	private static String progressText( WayfarerAccountService.ModeratorRewardStatus status ) {
		return "Active play and moderation earn rewards. AFK time is excluded after 3 minutes.\n\n"
				+ "_Total moderator service:_ " + duration( status.moderatorLifetimeActiveSeconds ) + "\n"
				+ "_This character:_ " + duration( status.characterLifetimeActiveSeconds ) + "\n\n"
				+ "Daily shift " + duration( Math.min( 10800, status.dailyActiveSeconds ) ) + "/3h {"
				+ status.dailyClaimedCount + "/3 claimed}\n"
				+ "Weekly shift: " + duration( status.weeklyActiveSeconds ) + " / 15h";
	}

	private static String duration( long seconds ) {
		long days = seconds / 86400;
		long hours = (seconds % 86400) / 3600;
		long minutes = (seconds % 3600) / 60;
		return days > 0 ? days + "d " + hours + "h " + minutes + "m" : hours + "h " + minutes + "m";
	}

	private static String title( String period ) {
		return Character.toUpperCase( period.charAt( 0 ) ) + period.substring( 1 ) + " Reward";
	}

	private static String choiceIntro( String period ) {
		return "welcome".equals( period )
				? "Welcome to the moderator team. Thank you for giving your time and care to help every Wayfarer feel safer, heard, and supported. Please choose the gift that feels most useful for your journey."
				: "Choose one reward for this completed moderator shift.";
	}

	private static String rewardDescription( String period, String key ) {
		String extra = "welcome".equals( period ) ? "\n\nAlso includes: 1 Spatial Geode." : "";
		if ("artifact".equals( key )) return "Preview and choose 1 of 3 random Artifacts. Each rolls its own rarity and stats." + extra;
		if ("trinket".equals( key )) return "Preview and choose 1 of 3 random Trinkets. Each rolls its own rarity and stats." + extra;
		if ("catalysts".equals( key )) return ("daily".equals( period ) ? "5" : "weekly".equals( period ) ? "50" : "25") + " random catalysts, split across up to 3 types." + extra;
		if ("resources".equals( key )) return ("daily".equals( period ) ? "20" : "weekly".equals( period ) ? "1000" : "100") + " random homebase resources, split across up to 3 types." + extra;
		if ("emeralds".equals( key )) return "Receive 2 Emeralds for Wayfarer trading.";
		if ("geode".equals( key )) return "Receive 1 Spatial Geode.";
		return "Receive 5 Stones of Ascendant Spark.";
	}

	private static Image rewardIcon( String period, String key ) {
		if (key.isEmpty()) return new ItemSprite( ItemSpriteSheet.LOCKED_CHEST );
		if ("artifact".equals( key )) return new ItemSprite( ItemSpriteSheet.CRYSTAL_CHEST );
		if ("trinket".equals( key )) return new ItemSprite( ItemSpriteSheet.EBONY_CHEST );
		if ("catalysts".equals( key )) return new ItemSprite( ItemSpriteSheet.ARCANE_RELIQUARY );
		if ("resources".equals( key )) return new ItemSprite( ItemSpriteSheet.PROVISION_CACHE );
		if ("emeralds".equals( key )) return new ItemSprite( WndHomebaseFacility.emeraldIcon() );
		if ("geode".equals( key ) || "geodebonus".equals( key )) return new ItemSprite( new SpatialGeode() );
		return new ItemSprite( new StoneOfNullbrand() );
	}

	private static int rewardImage( String key ) {
		if ("artifact".equals( key )) return ItemSpriteSheet.CRYSTAL_CHEST;
		if ("trinket".equals( key )) return ItemSpriteSheet.EBONY_CHEST;
		if ("catalysts".equals( key )) return ItemSpriteSheet.ARCANE_RELIQUARY;
		if ("resources".equals( key )) return ItemSpriteSheet.PROVISION_CACHE;
		if ("emeralds".equals( key )) return WndHomebaseFacility.emeraldIcon();
		if ("geode".equals( key )) return new SpatialGeode().image;
		return new StoneOfNullbrand().image;
	}

	private static String[] rewardKeys( String period ) {
		if ("welcome".equals( period )) return new String[]{"artifact", "trinket", "catalysts", "resources"};
		if ("daily".equals( period )) return new String[]{"catalysts", "resources", "emeralds"};
		return new String[]{"catalysts", "resources", "geode", "sparks"};
	}

	private static String rewardLabel( String period, String key ) {
		if ("artifact".equals( key )) return "Artifact Crystal Chest";
		if ("trinket".equals( key )) return "Trinket Ebony Chest";
		if ("catalysts".equals( key )) return ("daily".equals( period ) ? "5" : "weekly".equals( period ) ? "50" : "25") + " Random Catalysts";
		if ("resources".equals( key )) return ("daily".equals( period ) ? "20" : "weekly".equals( period ) ? "1000" : "100") + " Random Resources";
		if ("emeralds".equals( key )) return "2 Emeralds";
		if ("geode".equals( key )) return "1 Spatial Geode";
		return "5 Ascendant Sparks";
	}

	private static class WndRewardChoices extends Window {
		private static final int WIDTH = ReclaimedWindow.modalWidth( 170 );
		private static final int SLOT_SIZE = 32;
		private static final int SLOT_GAP = 7;
		private final String period;

		private WndRewardChoices( String period ) {
			this.period = period;
			RenderedTextBlock title = PixelScene.renderTextBlock( WndModeratorRewards.title( period ), 9 );
			title.hardlight( TITLE_COLOR );
			title.setPos( (WIDTH - title.width()) / 2f, 4 );
			add( title );

			RenderedTextBlock message = PixelScene.renderTextBlock( choiceIntro( period ), 6 );
			message.maxWidth( WIDTH - 8 );
			message.setPos( 4, title.bottom() + 4 );
			add( message );

			String[] keys = rewardKeys( period );
			float rowWidth = keys.length * SLOT_SIZE + (keys.length - 1) * SLOT_GAP;
			float startX = (WIDTH - rowWidth) / 2f;
			float contentBottom = message.bottom();
			if ("welcome".equals( period )) contentBottom = addInfoHint( contentBottom + 6 );
			float slotY = contentBottom + 7;
			for (int i = 0; i < keys.length; i++) {
				final String key = keys[i];
				final RewardDisplayItem display = new RewardDisplayItem(
						rewardImage( key ), rewardLabel( period, key ), rewardDescription( period, key ) );
				InventoryItemButton button = new InventoryItemButton() {
					@Override protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront( new RewardPreview( display, key ) );
					}
				};
				button.forceIdentifiedAppearance( true );
				button.item( display );
				button.setRect( startX + i * (SLOT_SIZE + SLOT_GAP), slotY, SLOT_SIZE, SLOT_SIZE );
				add( button );
			}

			float bottom = slotY + SLOT_SIZE;
			if ("welcome".equals( period )) bottom = addGeodeBonus( bottom + 7 );
			resize( WIDTH, (int)Math.ceil( bottom + 5 ) );
		}

		private float addInfoHint( float y ) {
			Image info = Icons.get( Icons.INFO );
			if (info.width > 0 && info.height > 0) {
				float scale = Math.min( 1f, Math.min( 12f / info.width, 12f / info.height ) );
				info.scale.set( scale );
			}
			info.x = 5;
			info.y = y;
			add( info );
			RenderedTextBlock hint = PixelScene.renderTextBlock(
					"_Info Button:_ Open it anytime for moderator duties, tools, chat guidelines, examples, and complete reward details.", 6 );
			hint.maxWidth( WIDTH - 25 );
			hint.setPos( 21, y + Math.max( 0, (info.height() - hint.height()) / 2f ) );
			add( hint );
			return Math.max( info.y + info.height(), hint.bottom() );
		}

		private float addGeodeBonus( float y ) {
			RenderedTextBlock prefix = PixelScene.renderTextBlock( "+ 1", 7 );
			RenderedTextBlock name = PixelScene.renderTextBlock( "Spatial Geode", 7 );
			name.hardlight( 0xFFFF44 );
			ItemSprite geode = new ItemSprite( new SpatialGeode() );
			float gap = 3;
			float totalWidth = prefix.width() + gap + geode.width() + gap + name.width();
			float x = (WIDTH - totalWidth) / 2f;
			prefix.setPos( x, y + (geode.height() - prefix.height()) / 2f );
			add( prefix );
			geode.x = prefix.right() + gap;
			geode.y = y;
			add( geode );
			name.setPos( geode.x + geode.width() + gap, y + (geode.height() - name.height()) / 2f );
			add( name );
			return Math.max( geode.y + geode.height(), Math.max( prefix.bottom(), name.bottom() ) );
		}

		private class RewardPreview extends WndInfoItem {
			private RewardPreview( Item display, String key ) {
				super( display );
				RedButton confirm = new RedButton( "Confirm" ) {
					@Override protected void onClick() {
						reserve( period, key );
						RewardPreview.this.hide();
						WndRewardChoices.this.hide();
					}
				};
				confirm.setRect( 0, height + 2, width / 2f - 1, 18 );
				add( confirm );
				RedButton back = new RedButton( "Back" ) {
					@Override protected void onClick() { RewardPreview.this.hide(); }
				};
				back.setRect( confirm.right() + 2, height + 2, confirm.width(), 18 );
				add( back );
				resize( width, (int)back.bottom() );
			}
		}
	}

	private static class RewardDisplayItem extends Item {
		private final String displayName;
		private final String description;

		private RewardDisplayItem( int image, String displayName, String description ) {
			this.image = image;
			this.displayName = displayName;
			this.description = description;
		}

		@Override public String name() { return displayName; }
		@Override public String desc() { return description; }
		@Override public boolean isIdentified() { return true; }
		@Override public boolean isUpgradable() { return false; }
	}

	private static class WndRewardSelection extends Window {
		private static final int WIDTH = ReclaimedWindow.modalWidth( 140 );
		private final WayfarerAccountService.ModeratorRewardClaim claim;

		private WndRewardSelection( WayfarerAccountService.ModeratorRewardClaim claim ) {
			this.claim = claim;
			RenderedTextBlock title = PixelScene.renderTextBlock( "Choose Your "
					+ ("artifact".equals( claim.rewardKey ) ? "Artifact" : "Trinket"), 9 );
			title.hardlight( TITLE_COLOR );
			title.setPos( (WIDTH - title.width()) / 2f, 4 );
			add( title );
			RenderedTextBlock message = PixelScene.renderTextBlock(
					"Preview all three independently rolled choices, then confirm one.", 6 );
			message.maxWidth( WIDTH - 8 );
			message.setPos( 4, title.bottom() + 4 );
			add( message );
			ArrayList<Item> choices = WayfarerModeratorRewards.selectionOptions( claim );
			float size = 32;
			float gap = 7;
			float start = (WIDTH - (size * 3 + gap * 2)) / 2f;
			for (int i = 0; i < choices.size(); i++) {
				final int selected = i;
				final Item item = choices.get( i );
				InventoryItemButton button = new InventoryItemButton() {
					@Override protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront( new SelectionInfo( item, selected ) );
					}
				};
				button.forceIdentifiedAppearance( true );
				button.item( item );
				button.setRect( start + i * (size + gap), message.bottom() + 7, size, size );
				add( button );
			}
			resize( WIDTH, (int)(message.bottom() + 46) );
		}

		@Override public void onBackPressed() { }

		private class SelectionInfo extends WndInfoItem {
			private SelectionInfo( Item item, int selected ) {
				super( item );
				RedButton confirm = new RedButton( "Confirm" ) {
					@Override protected void onClick() {
						WayfarerAccountService.selectModeratorReward( claim.claimId, selected, (result, updated) -> {
							if (!result.success || updated == null) {
								GameScene.show( notice( "Selection Failed", result.message, false ) );
								return;
							}
							SelectionInfo.this.hide();
							WndRewardSelection.this.hide();
							deliver( updated );
						} );
					}
				};
				confirm.setRect( 0, height + 2, width, 18 );
				add( confirm );
				resize( width, (int)confirm.bottom() );
			}
		}
	}
}
