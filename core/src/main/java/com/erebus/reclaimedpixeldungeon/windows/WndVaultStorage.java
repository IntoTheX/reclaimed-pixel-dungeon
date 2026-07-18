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
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.HomebaseFacilityScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;

import java.io.IOException;
import java.util.ArrayList;

public class WndVaultStorage extends Window {

	private static final int WIDTH_DESKTOP = 148;
	private static final int SLOT_SIZE = 24;
	private static final int SLOT_MARGIN = 1;
	private static final int COLS = 5;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;

	public WndVaultStorage() {
		super();
		int windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		IconTitle title = new IconTitle( new ItemSprite( ItemSpriteSheet.CHEST ), Messages.get( this, "title" ) );
		title.setRect( 0, 0, windowWidth, 0 );
		add( title );

		int capacity = Dungeon.homebase == null ? 0 : Dungeon.homebase.vaultSlots();
		int count = Dungeon.homebase == null ? 0 : Dungeon.homebase.vaultItems().size();

		RenderedTextBlock status = PixelScene.renderTextBlock( Messages.get( this, "status", count, capacity ), 6 );
		status.hardlight( Window.TITLE_COLOR );
		status.maxWidth( windowWidth );
		status.setPos( 0, title.bottom() + GAP );
		add( status );

		float top = status.bottom() + 2*GAP;
		Component content = new Component();
		ScrollPane pane = new ScrollPane( content );
		add( pane );

		RedButton deposit = new RedButton( Messages.get( this, "deposit" ), 6 ) {
			@Override
			protected void onClick() {
				hide();
				selectItem( depositSelector );
			}
		};
		deposit.enable( Dungeon.homebase != null && Dungeon.homebase.vaultSlots() > 0 );
		content.add( deposit );
		deposit.setRect( 0, 0, windowWidth, BTN_HEIGHT );

		float slotsTop = deposit.bottom() + GAP;
		ArrayList<Item> items = Dungeon.homebase == null ? new ArrayList<>() : Dungeon.homebase.vaultItems();
		int slots = Math.max( COLS, capacity );
		for (int i = 0; i < slots; i++) {
			final Item item = i < items.size() ? items.get( i ) : null;
			InventorySlot slot = new InventorySlot( item ) {
				@Override
				protected void onClick() {
					if (item == null) return;
					withdraw( item );
				}

				@Override
				protected boolean onLongClick() {
					if (item != null) {
						show( new WndInfoItem( item ) );
						return true;
					}
					return false;
				}
			};
			slot.setRect(
					(i % COLS) * (SLOT_SIZE + SLOT_MARGIN),
					slotsTop + (i / COLS) * (SLOT_SIZE + SLOT_MARGIN),
					SLOT_SIZE,
					SLOT_SIZE );
			if (item == null) slot.enable( false );
			content.add( slot );
		}

		float bottom = slotsTop + (int)Math.ceil( slots/(float)COLS ) * (SLOT_SIZE + SLOT_MARGIN) - SLOT_MARGIN;
		content.setSize( windowWidth, bottom );

		int windowHeight = ReclaimedWindow.modalHeight( (int)(top + bottom), chrome.marginVer() );
		resize( windowWidth, windowHeight );
		pane.setRect( 0, top, windowWidth, Math.max( 1, windowHeight - top ) );
		offset( 0, ReclaimedWindow.modalYOffset( windowHeight, chrome.marginVer() ) );
	}

	private final WndBag.ItemSelector depositSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndVaultStorage.class, "prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canStoreInVault( item )
					&& !item.isEquipped( Dungeon.hero )
					&& !(item instanceof Bag);
		}

		@Override
		public void onSelect( Item item ) {
			if (item == null) {
				show( vaultWindow() );
				return;
			}

			Item stored = item.detachAll( Dungeon.hero.belongings.backpack );
			if (Dungeon.homebase != null && stored != null && Dungeon.homebase.storeInVault( stored )) {
				GLog.p( Messages.get( WndVaultStorage.class, "stored", Messages.titleCase( stored.title() ) ) );
				Item.updateQuickslot();
				save();
			} else if (stored != null) {
				stored.collect( Dungeon.hero.belongings.backpack );
				GLog.w( Messages.get( WndVaultStorage.class, "full" ) );
			}
			show( vaultWindow() );
		}
	};

	private void withdraw( Item item ) {
		if (Dungeon.homebase == null || !Dungeon.homebase.removeFromVault( item )) return;

		if (item.collect( Dungeon.hero.belongings.backpack )) {
			GLog.p( Messages.get( WndVaultStorage.class, "withdrawn", Messages.titleCase( item.title() ) ) );
			Item.updateQuickslot();
			save();
		} else {
			Dungeon.homebase.storeInVault( item );
			GLog.w( Messages.get( WndVaultStorage.class, "inventory_full" ) );
		}
		hide();
		show( vaultWindow() );
	}

	private static WndVaultStorage vaultWindow() {
		if (ShatteredPixelDungeon.scene() instanceof HomebaseFacilityScene) {
			return embedded();
		} else {
			return new WndVaultStorage();
		}
	}

	public static WndVaultStorage embedded() {
		return new WndVaultStorage() {
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

	private static void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}
}
