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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.HomebaseDefender;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.WayfarerTrader;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemPreviewContext;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.levels.WayfarerExchangeLevel;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerExchangeService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTraderProfile;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.HealthBar;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.ItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import com.erebus.reclaimedpixeldungeon.utils.GLog;
import java.io.IOException;

public class WndInfoMob extends WndTabbed {

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_LAND = 160;
	private static final int WIDTH_MAX = 220;
	private static final int GAP = 2;
	private static final int SLOT = 28;
	private static final int BTN_HEIGHT = 18;

	private ScrollPane infoPane;
	private ScrollPane statsPane;
	private ScrollPane tradePane;
	private ScrollPane gearPane;
	private int contentTop;

	public WndInfoMob( Mob mob ) {
		super();

		String rarityStats = mob.rarityStatsInfo();
		boolean hasStats = rarityStats != null && !rarityStats.isEmpty();
		boolean hasGear = mob instanceof HomebaseDefender;
		boolean hasTraderProfile = mob instanceof WayfarerTrader;
		final boolean enemyStats = !(mob instanceof HomebaseDefender);

		int width = initialWidth( hasTraderProfile ? traderInfo( (WayfarerTrader)mob ) : mob.baseInfo(), rarityStats );
		Component titlebar = new MobTitle( mob );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		contentTop = (int)(titlebar.bottom() + 2 * GAP);
		int height = Math.max( 95, Math.min( (int)PixelScene.uiCamera.height - tabHeight() - 28, 170 ) );

		infoPane = hasTraderProfile
				? traderPane( (WayfarerTrader)mob, width, height )
				: textPane( hasGear ? defenderInfo( (HomebaseDefender)mob ) : mob.baseInfo(), width, height );
		add( infoPane );

		if (hasStats) {
			statsPane = textPane( rarityStats, width, height );
			add( statsPane );
		}

		if (hasGear) {
			gearPane = gearPane( (HomebaseDefender)mob, width, height );
			add( gearPane );
			tradePane = tradePane( (HomebaseDefender)mob, width, height );
			add( tradePane );
		}

		add( new LabeledTab( "Info" ) {
			@Override
			protected void select( boolean value ) {
				super.select( value );
				infoPane.visible = infoPane.active = selected;
			}
		} );
		if (hasStats) {
			add( new LabeledTab( "Stats" ) {
				@Override
				protected void select( boolean value ) {
					super.select( value );
					statsPane.visible = statsPane.active = selected;
					if (selected && enemyStats) {
						ReclaimedTutorial.flash( Document.GUIDE_MOB_STATS );
					}
				}
			} );
		}
		if (hasGear) {
			add( new LabeledTab( "Gear" ) {
				@Override
				protected void select( boolean value ) {
					super.select( value );
					gearPane.visible = gearPane.active = selected;
				}
			} );
			add( new LabeledTab( "Trade" ) {
				@Override
				protected void select( boolean value ) {
					super.select( value );
					tradePane.visible = tradePane.active = selected;
				}
			} );
		}

		resize( width, height );
		layoutPanes( width, height );
		layoutTabs();
		select( 0 );
	}

	@Override
	public void offset( int xOffset, int yOffset ) {
		super.offset( xOffset, yOffset );
		layoutPanes( width, height );
	}

	private void layoutPanes( int width, int height ) {
		infoPane.setRect( 0, contentTop, width, height - contentTop );
		if (statsPane != null) {
			statsPane.setRect( 0, contentTop, width, height - contentTop );
		}
		if (gearPane != null) {
			gearPane.setRect( 0, contentTop, width, height - contentTop );
		}
		if (tradePane != null) {
			tradePane.setRect( 0, contentTop, width, height - contentTop );
		}
	}

	private int initialWidth( String info, String rarityStats ) {
		int width = PixelScene.landscape() ? WIDTH_LAND : WIDTH_MIN;
		String longest = info == null ? "" : info;
		if (rarityStats != null && rarityStats.length() > longest.length()) {
			longest = rarityStats;
		}
		if (PixelScene.landscape() && longest.length() > 260) {
			width = WIDTH_MAX;
		}
		return ReclaimedWindow.modalWidth( width );
	}

	private ScrollPane textPane( String message, int width, int height ) {
		Component content = new Component();
		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		text.text( message == null ? "" : message, width );
		text.setPos( 0, 0 );
		content.add( text );
		content.setSize( width, Math.max( height - contentTop, text.bottom() + GAP ) );

		ScrollPane pane = new ScrollPane( content );
		pane.visible = pane.active = false;
		return pane;
	}

	private ScrollPane traderPane( WayfarerTrader trader, int width, int height ) {
		Component content = new Component();
		WayfarerTraderProfile profile = trader.profile();
		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		text.text( traderInfo( trader ), width );
		text.setPos( 0, 0 );
		content.add( text );

		int[] slots = {
				WayfarerTraderProfile.WEAPON,
				WayfarerTraderProfile.ARMOR,
				WayfarerTraderProfile.ARTIFACT_1,
				WayfarerTraderProfile.ARTIFACT_2,
				WayfarerTraderProfile.ARTIFACT_3,
				WayfarerTraderProfile.MISC_1,
				WayfarerTraderProfile.MISC_2,
				WayfarerTraderProfile.RING_1,
				WayfarerTraderProfile.RING_2,
				WayfarerTraderProfile.RING_3
		};

		float y = text.bottom() + GAP * 3;
		int columns = 5;
		float totalWidth = columns * SLOT + (columns - 1) * GAP;
		float left = Math.max( 0, (width - totalWidth) / 2f );
		final Item[] clickableItems = new Item[slots.length];
		final float[] slotXs = new float[slots.length];
		final float[] slotYs = new float[slots.length];
		for (int i = 0; i < slots.length; i++) {
			int col = i % columns;
			int row = i / columns;
			Item item = profile == null || slots[i] < 0 ? null : profile.item( slots[i] );
			registerTraderPreviewContext( profile, item );
			InventorySlot slot = viewOnlyInventorySlot( item );
			content.add( slot );
			slotXs[i] = left + col * (SLOT + GAP);
			slotYs[i] = y + row * (SLOT + GAP);
			clickableItems[i] = item;
			slot.setRect( slotXs[i], slotYs[i], SLOT, SLOT );
		}

		float bottom = y + ((slots.length + columns - 1) / columns) * (SLOT + GAP);
		final RedButton[] openTradeRef = new RedButton[1];
		final String traderPeerId = trader.peerId();
		if (Dungeon.level instanceof WayfarerExchangeLevel) {
			if (WayfarerExchangeService.peerBusy( traderPeerId )) {
				RenderedTextBlock busy = PixelScene.renderTextBlock( trader.name() + " is currently in a Trade.", 6 );
				busy.maxWidth( width );
				busy.hardlight( 0xAAAAAA );
				busy.setPos( 0, bottom + GAP * 2 );
				content.add( busy );
				bottom = busy.bottom();
			} else {
				RedButton openTrade = new RedButton( "Trade with " + trader.name(), 6 ) {
					@Override
					protected void onClick() {
						WayfarerExchangeLevel exchangeLevel = (WayfarerExchangeLevel)Dungeon.level;
						WayfarerExchangeService.requestTrade( traderPeerId );
						WndInfoMob.this.hide();
						GameScene.show( new WndWayfarerExchange( exchangeLevel.hostSide(), false ) );
					}
				};
				content.add( openTrade );
				openTrade.setRect( 0, bottom + GAP * 2, width, BTN_HEIGHT );
				openTradeRef[0] = openTrade;
				bottom = openTrade.bottom();
			}
		}

		content.setSize( width, Math.max( height - contentTop, bottom + GAP ) );

		ScrollPane pane = new ScrollPane( content ) {
			@Override
			public void onClick( float x, float y ) {
				for (int i = 0; i < clickableItems.length; i++) {
					if (clickableItems[i] != null && x >= slotXs[i] && x < slotXs[i] + SLOT
							&& y >= slotYs[i] && y < slotYs[i] + SLOT) {
						showTraderItemInfo( profile, clickableItems[i] );
						return;
					}
				}
				if (openTradeRef[0] != null && x >= openTradeRef[0].left() && x < openTradeRef[0].right()
						&& y >= openTradeRef[0].top() && y < openTradeRef[0].bottom()
						&& Dungeon.level instanceof WayfarerExchangeLevel) {
					WayfarerExchangeLevel exchangeLevel = (WayfarerExchangeLevel)Dungeon.level;
					WndInfoMob.this.hide();
					WayfarerExchangeService.requestTrade( traderPeerId );
					GameScene.show( new WndWayfarerExchange( exchangeLevel.hostSide(), false ) );
				}
			}
		};
		pane.visible = pane.active = false;
		return pane;
	}

	private void showTraderItemInfo( WayfarerTraderProfile profile, Item item ) {
		if (item == null) return;
		registerTraderPreviewContext( profile, item );
		if (profile != null) {
			ItemPreviewContext.set( profile.ringPotency, profile.artifactPotency, profile.trinketPotency );
		}
		try {
			showWindow( new WndInfoItem( item ) );
		} finally {
			ItemPreviewContext.clear();
		}
	}

	private void registerTraderPreviewContext( WayfarerTraderProfile profile, Item item ) {
		if (profile != null && item != null) {
			ItemPreviewContext.register( item, profile.ringPotency, profile.artifactPotency, profile.trinketPotency );
		}
	}

	private ScrollPane gearPane( final HomebaseDefender defender, int width, int height ) {
		Component content = new Component();
		HomebaseState.DefenderRecord record = Dungeon.homebase == null ? null : Dungeon.homebase.defender( defender.defenderId() );
		final Weapon weapon = record == null ? defender.weapon() : record.weapon();
		final Armor armor = record == null ? defender.armor() : record.armor();
		final Item ranged = record == null ? defender.ranged() : record.ranged();
		final int strength = record == null ? -1 : record.strength();
		float top = 0;

		RenderedTextBlock prompt = PixelScene.renderTextBlock( "Personal equipment", 6 );
		prompt.hardlight( TITLE_COLOR );
		prompt.maxWidth( width );
		prompt.setPos( 0, top );
		content.add( prompt );

		ItemButton weaponButton = gearButton( defender.defenderId(), weapon, ItemSpriteSheet.WEAPON_HOLDER, strength );
		content.add( weaponButton );
		weaponButton.setRect( 0, prompt.bottom() + 2 * GAP, SLOT, SLOT );

		ItemButton armorButton = gearButton( defender.defenderId(), armor, ItemSpriteSheet.ARMOR_HOLDER, strength );
		content.add( armorButton );
		armorButton.setRect( SLOT + GAP, weaponButton.top(), SLOT, SLOT );

		ItemButton rangedButton = gearButton( defender.defenderId(), ranged, ItemSpriteSheet.WAND_HOLDER, strength );
		content.add( rangedButton );
		rangedButton.setRect( 2 * (SLOT + GAP), weaponButton.top(), SLOT, SLOT );

		RenderedTextBlock names = PixelScene.renderTextBlock(
				DefenderUi.equipmentLines( weapon, armor, ranged, record == null ? 0 : record.strength() ), 5 );
		names.maxWidth( width );
		names.setPos( 0, weaponButton.bottom() + GAP );
		content.add( names );

		RedButton manage = new RedButton( "Manage Equipment", 6 ) {
			@Override
			protected void onClick() {
				openManagement( defender.defenderId() );
			}
		};
		content.add( manage );
		manage.setRect( 0, Math.max( weaponButton.bottom(), names.bottom() ) + GAP, width, BTN_HEIGHT );

		content.setSize( width, Math.max( height - contentTop, manage.bottom() + GAP ) );

		ScrollPane pane = new ScrollPane( content ) {
			@Override
			protected void layout() {
				super.layout();
				controller.active = controller.visible = false;
			}
		};
		pane.visible = pane.active = false;
		return pane;
	}

	private ScrollPane tradePane( final HomebaseDefender defender, int width, int height ) {
		ScrollPane pane = new ScrollPane( new Component() );
		pane.visible = pane.active = false;
		populateTradePane( pane, defender, width, height - contentTop );
		return pane;
	}

	private void populateTradePane( final ScrollPane pane, final HomebaseDefender defender, int width, int viewportHeight ) {
		Component content = pane.content();
		content.clear();
		final float tradeTopInset = PixelScene.landscape() ? 0 : GAP * 2;
		final HomebaseState.DefenderRecord record = defenderRecord( defender.defenderId() );
		if (record == null || !record.alive()) {
			RenderedTextBlock missing = PixelScene.renderTextBlock( "This defender is no longer available.", 6 );
			missing.maxWidth( width );
			missing.setPos( 0, tradeTopInset );
			content.add( missing );
			content.setSize( width, Math.max( viewportHeight, missing.bottom() + GAP + tradeTopInset ) );
			return;
		}
		DefenderTradeContent tradeContent = new DefenderTradeContent( record, width, viewportHeight, new DefenderTradeContent.TradeCallback() {
			@Override
			public void buy( HomebaseState.DefenderTradeOffer offer ) {
				buyTradeOffer( defender, record, offer );
			}
		} );
		content.add( tradeContent );
		tradeContent.setPos( 0, tradeTopInset );
		content.setSize( width, Math.max( viewportHeight, tradeContent.bottom() + tradeTopInset ) );
	}

	private void buyTradeOffer( HomebaseDefender defender, HomebaseState.DefenderRecord record, HomebaseState.DefenderTradeOffer offer ) {
		final Item item = offer == null ? null : offer.item();
		if (item == null || !record.buyTradeOffer( offer )) return;
		if (!item.collect( Dungeon.hero.belongings.backpack )) {
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
		GLog.p( "You trade with " + record.defenderName() + " for " + item.name() + "." );
		save();
		refreshTradePane( defender );
	}

	private void refreshTradePane( HomebaseDefender defender ) {
		if (tradePane != null) {
			remove( tradePane );
		}
		tradePane = tradePane( defender, WndInfoMob.this.width, WndInfoMob.this.height );
		add( tradePane );
		tradePane.setRect( 0, contentTop, WndInfoMob.this.width, WndInfoMob.this.height - contentTop );
		tradePane.visible = tradePane.active = true;
	}

	private ItemButton gearButton( final int defenderId, final Item item, int placeholder, int strength ) {
		ItemButton button = new ItemButton() {
			@Override
			protected void onClick() {
				if (item == null) {
					openManagement( defenderId );
				} else {
					showWindow( new WndInfoItem( item ) );
				}
			}

			@Override
			protected boolean onLongClick() {
				if (item != null) {
					showWindow( new WndInfoItem( item ) );
					return true;
				}
				return false;
			}
		};
		button.item( item == null ? new WndBag.Placeholder( placeholder ) : item );
		button.slot().strengthContext( strength );
		return button;
	}

	private ItemButton viewOnlyItemButton( final Item item, int placeholder ) {
		ItemButton button = new ItemButton() {
			@Override
			protected void onClick() {
				if (item != null) {
					showWindow( new WndInfoItem( item ) );
				}
			}

			@Override
			protected boolean onLongClick() {
				if (item != null) {
					showWindow( new WndInfoItem( item ) );
					return true;
				}
				return false;
			}
		};
		button.item( item == null ? new WndBag.Placeholder( placeholder ) : item );
		return button;
	}

	private InventorySlot viewOnlyInventorySlot( final Item item ) {
		InventorySlot slot = new InventorySlot( item ) {
			@Override
			protected void onClick() {
				if (item != null) {
					showWindow( new WndInfoItem( item ) );
				}
			}

			@Override
			protected boolean onLongClick() {
				if (item == null) return false;
				showWindow( new WndInfoItem( item ) );
				return true;
			}
		};
		if (item == null) {
			slot.clear();
			slot.enable( false );
		} else {
			slot.enable( true );
		}
		return slot;
	}

	private String defenderInfo( HomebaseDefender defender ) {
		return DefenderUi.infoText( defender, defenderRecord( defender.defenderId() ) );
	}

	private String traderInfo( WayfarerTrader trader ) {
		WayfarerTraderProfile profile = trader.profile();
		String heroClass = profile == null ? "" : profile.heroClass;
		int level = profile == null ? 1 : profile.level;
		return DefenderUi.colorText( 0x44CCFF, "Level " + level )
				+ "\n" + DefenderUi.colorText( Window.TITLE_COLOR, Messages.titleCase( heroClass ) )
				+ "\n\n" + DefenderUi.colorText( Window.WHITE, "Equipped Inventory" );
	}

	private void openManagement( int defenderId ) {
		hide();
		showWindow( new WndDefenderManagement( defenderId ) );
	}

	private static HomebaseState.DefenderRecord defenderRecord( int defenderId ) {
		return Dungeon.homebase == null ? null : Dungeon.homebase.defender( defenderId );
	}

	private static void showWindow( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	private static void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	private static class MobTitle extends Component {

		private static final int GAP	= 2;

		private CharSprite image;
		private RenderedTextBlock name;
		private HealthBar health;
		private ColorBlock xpBg;
		private ColorBlock xpFill;
		private BuffIndicator buffs;
		private HomebaseDefender defender;

		public MobTitle( Mob mob ) {

			name = PixelScene.renderTextBlock( Messages.titleCase( mob.name() ), 9 );
			name.hardlight( TITLE_COLOR );
			add( name );

			image = mob.sprite();
			add( image );

			health = new HealthBar();
			health.level(mob);
			add( health );

			if (mob instanceof HomebaseDefender) {
				defender = (HomebaseDefender)mob;
				xpBg = new ColorBlock( 1, 1, 0xFF1B2235 );
				add( xpBg );
				xpFill = new ColorBlock( 1, 1, 0xFF3AA7FF );
				add( xpFill );
			}

			buffs = new BuffIndicator( mob, false );
			add( buffs );
		}

		@Override
		protected void layout() {

			image.x = 0;
			image.y = Math.max( 0, name.height() + health.height() - image.height() );

			float w = width - image.width() - GAP;

			name.setPos(x + image.width() + GAP,
					image.height() > name.height() ? y +(image.height() - name.height()) / 2 : y);

			health.setRect(image.width() + GAP, name.bottom() + GAP, w, health.height());
			float barsBottom = health.bottom();

			if (defender != null) {
				xpBg.x = xpFill.x = health.left();
				xpBg.y = xpFill.y = health.bottom() + 1;
				xpBg.size( w, 2 );
				xpFill.size( w * defender.xpProgress(), 2 );
				barsBottom = xpBg.y + xpBg.height;
			}

			buffs.maxBuffs = 50;
			buffs.setRect(name.right(), name.bottom() - BuffIndicator.SIZE_SMALL-2, w - name.width(), 8);

			if (!buffs.allBuffsVisible()){
				buffs.setRect(0, barsBottom + GAP, width, 8);
				height = Math.max(image.y + image.height(), buffs.bottom());
			} else {
				height = Math.max(image.y + image.height(), barsBottom);
			}
		}
	}
}
