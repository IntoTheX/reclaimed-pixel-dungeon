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
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

class WndEmberforgeConfirm extends Window {

	private static final int WIDTH = 144;
	private static final int GAP = 3;
	private static final int BUTTON_HEIGHT = 18;

	private final String[] options;
	private final Callback callback;
	private final Runnable back;

	interface Callback {
		void onSelect( int index );
	}

	WndEmberforgeConfirm( Image icon, String title, String message, String[] options ) {
		this( icon, title, message, options, null, null );
	}

	WndEmberforgeConfirm( Image icon, String title, String message, String[] options, Callback callback, Runnable back ) {
		super();
		this.options = options;
		this.callback = callback;
		this.back = back;
		int width = PixelScene.landscape() ? WIDTH : 120;
		float pos = 0;

		IconTitle titleBlock = new IconTitle( icon, title );
		titleBlock.setRect( 0, pos, width, 0 );
		add( titleBlock );
		pos = titleBlock.bottom() + GAP;

		RenderedTextBlock body = PixelScene.renderTextBlock( message, 6 );
		body.maxWidth( width );
		body.setPos( 0, pos );
		add( body );
		pos = body.bottom() + GAP;

		pos = addBody( width, pos );

		for (int i = 0; i < options.length; i++) {
			final int index = i;
			RedButton button = new RedButton( options[i], 6 ) {
				@Override
				protected void onClick() {
					hide();
					if (callback != null) {
						callback.onSelect( index );
					} else {
						onSelect( index );
					}
				}
			};
			button.multiline = true;
			button.enable( enabled( i ) );
			add( button );
			button.setRect( 0, pos, width, BUTTON_HEIGHT );
			pos = button.bottom() + GAP;
		}

		resize( width, (int)(pos - GAP) );
	}

	protected float addBody( int width, float pos ) {
		return pos;
	}

	protected boolean enabled( int index ) {
		return true;
	}

	protected void onSelect( int index ) {
	}

	@Override
	public void onBackPressed() {
		hide();
		if (back != null) {
			back.run();
		}
	}

	static WndEmberforgeConfirm salvage( final Item item, final int count, String[] options ) {
		return salvage( item, count, options, null, null );
	}

	static WndEmberforgeConfirm salvage( final Item item, final int count, String[] options, Callback callback, Runnable back ) {
		return new WndEmberforgeConfirm(
				new ItemSprite( item ),
				com.erebus.reclaimedpixeldungeon.messages.Messages.titleCase( item.name() ),
				"The forge will break this item down into:",
				options,
				callback,
				back ) {
			@Override
			protected float addBody( int width, float pos ) {
				ForgeResourceLine line = ForgeResourceLine.salvageYield( item, count, false );
				add( line );
				line.setRect( 0, pos, width, 0 );
				return line.bottom() + GAP;
			}
		};
	}

	static WndEmberforgeConfirm salvageStack( final Item item, String[] options ) {
		return salvageStack( item, options, null, null );
	}

	static WndEmberforgeConfirm salvageStack( final Item item, String[] options, Callback callback, Runnable back ) {
		return new WndEmberforgeConfirm(
				new ItemSprite( item ),
				com.erebus.reclaimedpixeldungeon.messages.Messages.titleCase( item.name() ),
				"The forge will break this item down into:",
				options,
				callback,
				back ) {
			@Override
			protected float addBody( int width, float pos ) {
				ForgeResourceLine one = ForgeResourceLine.salvageYield( item, 1, false );
				add( one );
				one.setRect( 0, pos, width, 0 );
				pos = one.bottom() + GAP;

				RenderedTextBlock allLabel = PixelScene.renderTextBlock( "All:", 6 );
				allLabel.hardlight( Window.WHITE );
				allLabel.setPos( 0, pos );
				add( allLabel );
				pos = allLabel.bottom() + 1;

				ForgeResourceLine all = ForgeResourceLine.salvageYield( item, item.quantity(), false );
				add( all );
				all.setRect( 0, pos, width, 0 );
				return all.bottom() + GAP;
			}
		};
	}

	static WndEmberforgeConfirm upgrade( final Item item, String[] options ) {
		return upgrade( item, options, null, null );
	}

	static WndEmberforgeConfirm upgrade( final Item item, String[] options, Callback callback, Runnable back ) {
		return new WndEmberforgeConfirm(
				new ItemSprite( item ),
				com.erebus.reclaimedpixeldungeon.messages.Messages.titleCase( item.name() ),
				"Upgrade cost:",
				options,
				callback,
				back ) {
			@Override
			protected float addBody( int width, float pos ) {
				ForgeResourceLine cost = ForgeResourceLine.upgradeCost( item, false );
				add( cost );
				cost.setRect( 0, pos, width, 0 );
				pos = cost.bottom() + GAP;

				RenderedTextBlock have = PixelScene.renderTextBlock( "You have:", 6 );
				have.hardlight( Window.WHITE );
				have.setPos( 0, pos );
				add( have );
				pos = have.bottom() + 1;

				ForgeResourceLine owned = ForgeResourceLine.upgradeCost( item, true );
				add( owned );
				owned.setRect( 0, pos, width, 0 );
				return owned.bottom() + GAP;
			}

			@Override
			protected boolean enabled( int index ) {
				return index != 0 || Dungeon.homebase.canForgeUpgrade( item );
			}
		};
	}

	private static class ForgeResourceLine extends Component {

		private static final int ROW_HEIGHT = 10;
		private final java.util.ArrayList<Chip> chips = new java.util.ArrayList<>();

		static ForgeResourceLine salvageYield( Item item, int count, boolean owned ) {
			ForgeResourceLine line = new ForgeResourceLine();
			if (Dungeon.homebase == null || item == null) return line;
			count = Math.max( 1, count );
			Item yieldItem = item.quantity() > 1 ? item.duplicate() : item;
			if (yieldItem != null) yieldItem.quantity( 1 );
			for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
				line.addForge( resource, Dungeon.homebase.salvageYield( yieldItem, resource ) * count, 0, owned );
			}
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				line.addMaterial( material, Dungeon.homebase.salvageMaterialYield( yieldItem, material ) * count, 0, owned );
			}
			line.addGold( Dungeon.homebase.salvageGoldYield( yieldItem ) * count, 0, owned );
			line.addEnergy( Dungeon.homebase.salvageEnergyYield( yieldItem ) * count, 0, owned );
			return line;
		}

		static ForgeResourceLine upgradeCost( Item item, boolean owned ) {
			ForgeResourceLine line = new ForgeResourceLine();
			if (Dungeon.homebase == null || item == null) return line;
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				int needed = Dungeon.homebase.forgeUpgradeMaterialCost( item, material );
				line.addMaterial( material, owned ? Dungeon.homebase.amount( material ) : needed, needed, owned );
			}
			for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
				int needed = Dungeon.homebase.forgeUpgradeCost( item, resource );
				line.addForge( resource, owned ? Dungeon.homebase.forgeResourceAmount( resource ) : needed, needed, owned );
			}
			return line;
		}

		private void addMaterial( HomebaseState.Material material, int amount, int needed, boolean owned ) {
			if ((!owned && amount <= 0) || (owned && needed <= 0)) return;
			addChip( new ItemSprite( WndHomebaseFacility.materialIcon( material ) ),
					amount, needed, WndHomebaseFacility.materialColor( material ), owned );
		}

		private void addForge( HomebaseState.ForgeResource resource, int amount, int needed, boolean owned ) {
			if ((!owned && amount <= 0) || (owned && needed <= 0)) return;
			addChip( new ItemSprite( WndHomebaseFacility.forgeIcon( resource ) ),
					amount, needed, WndHomebaseFacility.forgeColor( resource ), owned );
		}

		private void addGold( int amount, int needed, boolean owned ) {
			if ((!owned && amount <= 0) || (owned && needed <= 0)) return;
			addChip( Icons.get( Icons.COIN_SML ), amount, needed, WndHomebaseFacility.goldColor(), owned );
		}

		private void addEnergy( int amount, int needed, boolean owned ) {
			if ((!owned && amount <= 0) || (owned && needed <= 0)) return;
			addChip( Icons.get( Icons.ENERGY_SML ), amount, needed, WndHomebaseFacility.energyColor(), owned );
		}

		private void addChip( Image icon, int amount, int needed, int color, boolean owned ) {
			Chip chip = new Chip( icon, amount, needed, color, owned );
			chips.add( chip );
			add( chip );
		}

		@Override
		protected void layout() {
			float left = 0;
			float rowTop = y;
			for (Chip chip : chips) {
				float chipWidth = chip.reqWidth();
				if (left > 0 && left + chipWidth > width) {
					left = 0;
					rowTop += ROW_HEIGHT;
				}
				chip.setRect( x + left, rowTop, chipWidth, ROW_HEIGHT );
				left += chipWidth + 4;
			}
			height = chips.isEmpty() ? 0 : rowTop + ROW_HEIGHT - y;
		}
	}

	private static class Chip extends Component {

		private final Image icon;
		private final RenderedTextBlock amount;

		private Chip( Image icon, int value, int needed, int color, boolean owned ) {
			this.icon = icon;
			this.icon.resetColor();
			add( this.icon );

			amount = PixelScene.renderTextBlock( 5 );
			if (owned) {
				int ownedColor = value >= needed ? 0x44FF44 : 0xFF4444;
				amount.text( color( ownedColor, WndHomebaseFacility.compactAmount( value ) )
						+ "/" + color( Window.WHITE, WndHomebaseFacility.compactAmount( needed ) ) );
			} else {
				amount.text( color( color, WndHomebaseFacility.compactAmount( value ) ) );
			}
			add( amount );
		}

		private static String color( int color, String text ) {
			return "@@C" + String.format( "%06X", color & 0xFFFFFF ) + "@@" + text + "@@CEND@@";
		}

		private float reqWidth() {
			return icon.width() + 1 + amount.width();
		}

		@Override
		protected void layout() {
			icon.x = x;
			icon.y = y + (height - icon.height()) / 2f;
			amount.setPos( icon.x + icon.width() + 1, y + (height - amount.height()) / 2f );
			PixelScene.align( icon );
			PixelScene.align( amount );
		}
	}
}
