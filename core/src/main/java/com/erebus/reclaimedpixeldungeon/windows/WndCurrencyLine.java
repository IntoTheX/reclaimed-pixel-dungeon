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

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

class WndCurrencyLine extends Component {

	private static final int GAP = 4;
	private static final int ROW_HEIGHT = 10;

	private final RenderedTextBlock label;
	private final ArrayList<Chip> chips = new ArrayList<>();

	WndCurrencyLine( String labelText ) {
		label = PixelScene.renderTextBlock( labelText, 6 );
		label.hardlight( Window.WHITE );
		add( label );
	}

	static WndCurrencyLine defenderPockets( HomebaseState.DefenderRecord defender ) {
		WndCurrencyLine line = new WndCurrencyLine( "Pockets:" );
		if (defender == null) return line;
		line.addGold( defender.personalGold() );
		line.addEnergy( defender.personalEnergy() );
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			line.addMaterial( material, defender.personalMaterialAmount( material ) );
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			line.addForge( resource, defender.personalForgeResourceAmount( resource ) );
		}
		return line;
	}

	static WndCurrencyLine tradePrice( HomebaseState.DefenderTradeOffer offer ) {
		WndCurrencyLine line = new WndCurrencyLine( "Price:" );
		if (offer == null) return line;
		if (offer.priceKind() == HomebaseState.DefenderTradeOffer.PRICE_GOLD) {
			line.addGold( offer.priceAmount() );
		} else {
			line.addForge( offer.priceForgeResource(), offer.priceAmount() );
		}
		return line;
	}

	void addGold( int amount ) {
		if (amount <= 0) return;
		addChip( Icons.get( Icons.COIN_SML ), WndHomebaseFacility.goldName(), amount, WndHomebaseFacility.goldColor() );
	}

	void addEnergy( int amount ) {
		if (amount <= 0) return;
		addChip( Icons.get( Icons.ENERGY_SML ), WndHomebaseFacility.energyName(), amount, WndHomebaseFacility.energyColor() );
	}

	void addMaterial( HomebaseState.Material material, int amount ) {
		if (material == null || amount <= 0) return;
		addChip( new ItemSprite( WndHomebaseFacility.materialIcon( material ) ),
				WndHomebaseFacility.materialName( material ),
				amount,
				WndHomebaseFacility.materialColor( material ) );
	}

	void addForge( HomebaseState.ForgeResource resource, int amount ) {
		if (resource == null || amount <= 0) return;
		addChip( new ItemSprite( WndHomebaseFacility.forgeIcon( resource ) ),
				WndHomebaseFacility.forgeName( resource ),
				amount,
				WndHomebaseFacility.forgeColor( resource ) );
	}

	boolean isEmpty() {
		return chips.isEmpty();
	}

	private void addChip( Image icon, String name, int amount, int color ) {
		Chip chip = new Chip( icon, name, amount, color );
		chips.add( chip );
		add( chip );
	}

	@Override
	protected void layout() {
		label.setPos( x, y + (ROW_HEIGHT - label.height()) / 2f );
		PixelScene.align( label );

		float left = label.width() + GAP;
		float rowTop = y;
		for (Chip chip : chips) {
			float chipWidth = chip.reqWidth();
			if (left > label.width() + GAP && left + chipWidth > width) {
				left = 0;
				rowTop += ROW_HEIGHT;
			}
			chip.setRect( x + left, rowTop, chipWidth, ROW_HEIGHT );
			left += chipWidth + GAP;
		}
		height = chips.isEmpty() ? label.height() : rowTop + ROW_HEIGHT - y;
	}

	private static class Chip extends Component {

		private final Image icon;
		private final RenderedTextBlock amount;

		private Chip( Image icon, String name, int value, int color ) {
			this.icon = icon;
			this.icon.resetColor();
			add( this.icon );

			amount = PixelScene.renderTextBlock( 5 );
			amount.text( WndHomebaseFacility.compactAmount( value ) );
			amount.hardlight( color );
			add( amount );
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
