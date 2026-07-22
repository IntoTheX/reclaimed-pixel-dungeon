/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.HomebaseFacilityScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.io.IOException;

public class WndEmberforge extends Window {

	private static final int WIDTH_DESKTOP = 136;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;
	private static String lastResultText = "";

	public WndEmberforge() {
		super();
		int windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		IconTitle title = new IconTitle( new ItemSprite( ItemSpriteSheet.BUILDING_IRON ), Messages.get( this, "title" ) );
		title.setRect( 0, 0, windowWidth, 0 );
		add( title );

		String statusText = Dungeon.homebase == null
				? Messages.get( this, "no_forge" )
				: Messages.get( this, "status", Dungeon.homebase.forgeResourcesText(), Dungeon.homebase.maxForgeUpgradeLevel() );
		RenderedTextBlock status = PixelScene.renderTextBlock( statusText, 6 );
		status.maxWidth( windowWidth );
		status.setPos( 0, title.bottom() + GAP );
		add( status );

		float bottom = status.bottom() + 2 * GAP;
		if (lastResultText != null && !lastResultText.isEmpty()) {
			RenderedTextBlock result = PixelScene.renderTextBlock( lastResultText, 6 );
			result.maxWidth( windowWidth );
			result.hardlight( Window.TITLE_COLOR );
			result.setPos( 0, bottom );
			add( result );
			bottom = result.bottom() + 2 * GAP;
		}

		RedButton salvage = new RedButton( Messages.get( this, "salvage" ), 6 ) {
			@Override
			protected void onClick() {
				hide();
				selectItem( salvageSelector );
			}
		};
		salvage.enable( Dungeon.homebase != null && Dungeon.homebase.isBuilt( HomebaseState.Building.FORGE ) );
		add( salvage );
		salvage.setRect( 0, bottom, windowWidth, BTN_HEIGHT );
		bottom = salvage.bottom() + GAP;

		RedButton upgrade = new RedButton( Messages.get( this, "upgrade" ), 6 ) {
			@Override
			protected void onClick() {
				hide();
				selectItem( upgradeSelector );
			}
		};
		upgrade.enable( Dungeon.homebase != null && Dungeon.homebase.isBuilt( HomebaseState.Building.FORGE ) );
		add( upgrade );
		upgrade.setRect( 0, bottom, windowWidth, BTN_HEIGHT );
		bottom = upgrade.bottom();

		resize( windowWidth, (int)bottom );
		offset( 0, ReclaimedWindow.modalYOffset( (int)bottom, chrome.marginVer() ) );
	}

	private final WndBag.ItemSelector salvageSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndEmberforge.class, "salvage_prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return null;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canSalvage( item )
					&& !item.isEquipped( Dungeon.hero )
					&& !(item instanceof Bag);
		}

		@Override
		public void onSelect( Item item ) {
			if (item == null) {
				show( forgeWindow() );
				return;
			}

			if (item.quantity() > 1) {
				show( WndEmberforgeConfirm.salvageStack( item,
						new String[]{
								Messages.get( WndEmberforge.class, "salvage_one" ),
								Messages.get( WndEmberforge.class, "salvage_all", item.quantity() ),
								Messages.get( WndEmberforge.class, "cancel" ) },
						new WndEmberforgeConfirm.Callback() {
							@Override
							public void onSelect( int index ) {
						if (index == 0) {
							salvageAmount( item, 1 );
							selectItem( salvageSelector );
						} else if (index == 1) {
							salvageAmount( item, item.quantity() );
							selectItem( salvageSelector );
						} else {
							show( forgeWindow() );
						}
					}
						},
						new Runnable() {
							@Override
							public void run() {
								show( forgeWindow() );
							}
						} ) );
				return;
			}

			show( WndEmberforgeConfirm.salvage( item, 1,
					new String[]{
							Messages.get( WndEmberforge.class, "salvage_yes" ),
							Messages.get( WndEmberforge.class, "cancel" ) },
					new WndEmberforgeConfirm.Callback() {
						@Override
						public void onSelect( int index ) {
					if (index == 0) {
						salvageOne( item );
						selectItem( salvageSelector );
					} else {
						show( forgeWindow() );
					}
				}
					},
					new Runnable() {
						@Override
						public void run() {
							show( forgeWindow() );
						}
					} ) );
		}
	};

	private final WndBag.ItemSelector upgradeSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndEmberforge.class, "upgrade_prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canForgeUpgradeTarget( item );
		}

		@Override
		public void onSelect( Item item ) {
			if (item == null) {
				show( forgeWindow() );
				return;
			}

			show( WndEmberforgeConfirm.upgrade( item,
					new String[]{
							Messages.get( WndEmberforge.class, "upgrade_yes" ),
							Messages.get( WndEmberforge.class, "cancel" ) },
					new WndEmberforgeConfirm.Callback() {
						@Override
						public void onSelect( int index ) {
					if (index == 0) {
						upgradeItem( item );
					}
					show( forgeWindow() );
				}
					},
					new Runnable() {
						@Override
						public void run() {
							show( forgeWindow() );
						}
					} ) );
		}
	};

	private static WndEmberforge forgeWindow() {
		if (ShatteredPixelDungeon.scene() instanceof HomebaseFacilityScene) {
			return embedded();
		} else {
			return new WndEmberforge();
		}
	}

	public static WndEmberforge embedded() {
		return new WndEmberforge() {
			@Override
			protected boolean blocksInput() {
				return false;
			}

			@Override
			protected boolean handlesBackButton() {
				return false;
			}
		};
	}

	private static void selectItem( WndBag.ItemSelector selector ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.selectItem( selector );
		} else {
			show( WndBag.getBag( selector ) );
		}
	}

	private static void show( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	public static void salvageOne( Item item ) {
		salvageAmount( item, 1 );
	}

	public static void salvageAmount( Item item, int amount ) {
		lastResultText = "";
		if (item == null || amount <= 0) return;

		int salvagedAmount = Math.max( 1, Math.min( amount, item.quantity() ) );
		Item salvaged = salvagedAmount >= item.quantity()
				? item.detachAll( Dungeon.hero.belongings.backpack )
				: item.split( salvagedAmount );
		if (salvaged == null) return;
		salvaged.quantity( salvagedAmount );

		if (Dungeon.homebase != null && Dungeon.homebase.salvage( salvaged, salvagedAmount )) {
			Sample.INSTANCE.play( Assets.Sounds.EVOKE );
			Item.evoke( Dungeon.hero );
			GLog.p( Messages.get( WndEmberforge.class, "salvaged", Messages.titleCase( salvaged.name() ), Dungeon.homebase.salvageYieldText( salvaged, salvagedAmount ) ) );
			Item.updateQuickslot();
			save();
		} else if (!salvaged.collect( Dungeon.hero.belongings.backpack )) {
			Dungeon.level.drop( salvaged, Dungeon.hero.pos ).sprite.drop();
		}
	}

	public static boolean upgradeItem( Item item ) {
		lastResultText = "";
		if (Dungeon.homebase == null || !Dungeon.homebase.spendForgeUpgradeCost( item )) {
			GLog.w( Messages.get( WndEmberforge.class, "cannot_upgrade" ) );
			return false;
		}

		ScrollOfUpgrade.upgrade( Dungeon.hero );
		Sample.INSTANCE.play( Assets.Sounds.EVOKE );
		item.upgrade();
		Dungeon.increaseMobLevelPressure( 1 );
		boolean rarityImproved = item.improveRarityStatsFromUpgrade();
		if (rarityImproved) {
			lastResultText = Messages.get( WndEmberforge.class, "rarity_improved", Messages.capitalize( item.name() ) );
			GLog.p( lastResultText );
		}
		Item.evoke( Dungeon.hero );
		Badges.validateItemLevelAquired( item );
		Item.updateQuickslot();
		GLog.p( Messages.get( WndEmberforge.class, "upgraded", Messages.titleCase( item.name() ) ) );
		save();
		return rarityImproved;
	}

	private static void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}
}
