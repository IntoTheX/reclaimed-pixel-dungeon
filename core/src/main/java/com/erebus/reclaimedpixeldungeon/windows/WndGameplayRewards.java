/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.rewards.GameplayRewards;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventoryItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;

import java.util.ArrayList;

public class WndGameplayRewards extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 160 );
	private static final int SLOT_SIZE = 36;
	private static final int SLOT_GAP = 8;

	public static void open() {
		if (GameplayRewards.pendingRewards() > 0) GameScene.show( new WndGameplayRewards() );
	}

	private WndGameplayRewards() {
		RenderedTextBlock title = PixelScene.renderTextBlock( "Active Play Reward", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 4 );
		add( title );

		int pending = GameplayRewards.pendingRewards();
		RenderedTextBlock message = PixelScene.renderTextBlock(
				"You earned a reward for _30 minutes of active play_. Choose one of three independently rolled rewards."
						+ (pending > 1 ? "\n\n_Queued rewards:_ " + pending : ""), 6 );
		message.maxWidth( WIDTH - 8 );
		message.setPos( 4, title.bottom() + 5 );
		add( message );

		ArrayList<GameplayRewards.RewardOption> options = GameplayRewards.currentOptions();
		float rowWidth = SLOT_SIZE * 3 + SLOT_GAP * 2;
		float startX = (WIDTH - rowWidth) / 2f;
		float slotY = message.bottom() + 8;
		for (int i = 0; i < options.size(); i++) {
			final int selected = i;
			final GameplayRewards.RewardOption option = options.get( i );
			final RewardDisplayItem display = new RewardDisplayItem( option );
			InventoryItemButton button = new InventoryItemButton() {
				@Override protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront( new RewardPreview( display, option, selected ) );
				}
			};
			button.forceIdentifiedAppearance( true );
			button.item( display );
			button.setRect( startX + i * (SLOT_SIZE + SLOT_GAP), slotY, SLOT_SIZE, SLOT_SIZE );
			add( button );
		}
		resize( WIDTH, (int)Math.ceil( slotY + SLOT_SIZE + 6 ) );
	}

	private class RewardPreview extends WndInfoItem {
		private RewardPreview( Item display, GameplayRewards.RewardOption option, int selected ) {
			super( display );
			RedButton confirm = new RedButton( "Choose" ) {
				@Override protected void onClick() {
					if ("artifact".equals( option.key ) || "trinket".equals( option.key )) {
						RewardPreview.this.hide();
						WndGameplayRewards.this.hide();
						GameScene.show( new WndSpecialSelection( selected, option.key ) );
					} else {
						finishClaim( GameplayRewards.claim( selected, -1 ) );
						RewardPreview.this.hide();
						WndGameplayRewards.this.hide();
					}
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

	private static class RewardDisplayItem extends Item {
		private final GameplayRewards.RewardOption option;

		private RewardDisplayItem( GameplayRewards.RewardOption option ) {
			this.option = option;
			image = GameplayRewards.rewardImage( option );
		}

		@Override public String name() {
			return option.rarity.coloredName() + " " + GameplayRewards.rewardLabel( option );
		}
		@Override public String desc() { return GameplayRewards.rewardDescription( option ); }
		@Override public boolean isIdentified() { return true; }
		@Override public boolean isUpgradable() { return false; }
		@Override public boolean hasRarityAura() { return true; }
		@Override public int rarityColor() { return option.rarity.color(); }
		@Override public float rarityAuraAlpha() { return option.rarity.auraAlpha(); }
		@Override public boolean isTranscendantRarity() { return option.rarity == ItemRarity.TRANSCENDANT; }
	}

	private static class WndSpecialSelection extends Window {
		private static final int SELECT_WIDTH = ReclaimedWindow.modalWidth( 145 );

		private WndSpecialSelection( int optionIndex, String key ) {
			RenderedTextBlock title = PixelScene.renderTextBlock(
					"Choose Your " + ("artifact".equals( key ) ? "Artifact" : "Trinket"), 9 );
			title.hardlight( TITLE_COLOR );
			title.setPos( (SELECT_WIDTH - title.width()) / 2f, 4 );
			add( title );
			RenderedTextBlock message = PixelScene.renderTextBlock(
					"Preview all three independently rolled choices, then confirm one.", 6 );
			message.maxWidth( SELECT_WIDTH - 8 );
			message.setPos( 4, title.bottom() + 4 );
			add( message );

			ArrayList<Item> choices = GameplayRewards.specialSelectionOptions( optionIndex );
			float size = 34;
			float gap = 7;
			float startX = (SELECT_WIDTH - (size * 3 + gap * 2)) / 2f;
			for (int i = 0; i < choices.size(); i++) {
				final int selected = i;
				final Item item = choices.get( i );
				InventoryItemButton button = new InventoryItemButton() {
					@Override protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront( new SelectionInfo( item, optionIndex, selected ) );
					}
				};
				button.forceIdentifiedAppearance( true );
				button.item( item );
				button.setRect( startX + i * (size + gap), message.bottom() + 8, size, size );
				add( button );
			}
			resize( SELECT_WIDTH, (int)Math.ceil( message.bottom() + 48 ) );
		}

		private class SelectionInfo extends WndInfoItem {
			private SelectionInfo( Item item, int optionIndex, int selected ) {
				super( item );
				RedButton confirm = new RedButton( "Choose" ) {
					@Override protected void onClick() {
						GameplayRewards.ClaimResult result = GameplayRewards.claim( optionIndex, selected );
						SelectionInfo.this.hide();
						WndSpecialSelection.this.hide();
						finishClaim( result );
					}
				};
				confirm.setRect( 0, height + 2, width, 18 );
				add( confirm );
				resize( width, (int)confirm.bottom() );
			}
		}
	}

	private static void finishClaim( GameplayRewards.ClaimResult result ) {
		boolean more = result.success && GameplayRewards.pendingRewards() > 0;
		if (more) {
			WndGameplayRewards.open();
			return;
		}
		GameScene.show( new WndOptions(
				Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
				result.success ? "Reward Claimed" : "Reward Not Claimed",
				result.success ? result.message + " was delivered. Items that did not fit were placed at your feet."
						: result.message,
				"OK" ) {
			@Override protected void onSelect( int index ) {
			}
		} );
	}
}
