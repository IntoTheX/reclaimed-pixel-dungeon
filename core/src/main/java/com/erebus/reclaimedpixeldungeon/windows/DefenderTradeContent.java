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
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

class DefenderTradeContent extends Component {

	private static final int GAP = 3;
	private static final int SLOT_WIDTH = 32;
	private static final int SLOT_HEIGHT = 32;
	private static final int BUTTON_HEIGHT = 18;
	private static final int TOP_PAD = 2;

	private final HomebaseState.DefenderRecord defender;
	private final int contentWidth;
	private final int viewportHeight;
	private final TradeCallback callback;
	private HomebaseState.DefenderTradeOffer selectedOffer;

	DefenderTradeContent( HomebaseState.DefenderRecord defender, int width, int viewportHeight, TradeCallback callback ) {
		this.defender = defender;
		this.contentWidth = width;
		this.viewportHeight = viewportHeight;
		this.callback = callback;
		setSize( width, viewportHeight );
		rebuild();
	}

	private void rebuild() {
		if (members == null) {
			return;
		}
		destroyChildren();
		float pos = TOP_PAD;
		if (selectedOffer != null && (defender == null || !defender.tradeOffers().contains( selectedOffer ))) {
			selectedOffer = null;
		}

		WndCurrencyLine pockets = WndCurrencyLine.defenderPockets( defender );
		add( pockets );
		pockets.setRect( 0, pos, width, 0 );
		pos = pockets.bottom() + GAP;

		if (pockets.isEmpty()) {
			RenderedTextBlock emptyPockets = PixelScene.renderTextBlock( "empty pockets", 6 );
			emptyPockets.maxWidth( contentWidth );
			emptyPockets.setPos( 0, pos );
			add( emptyPockets );
			pos = emptyPockets.bottom() + GAP;
		}

		if (defender == null || defender.tradeOffers().isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					(defender == null ? "This defender" : defender.defenderName()) + " has nothing to trade after their latest run.", 6 );
			empty.maxWidth( contentWidth );
			empty.setPos( 0, pos );
			add( empty );
			pos = empty.bottom() + GAP;
			resizeContent( Math.max( viewportHeight, pos ) );
			return;
		}

		int columns = Math.max( 1, Math.min( 4, (int)((width + GAP) / (SLOT_WIDTH + GAP)) ) );
		int offerCount = 0;
		for (HomebaseState.DefenderTradeOffer offer : defender.tradeOffers()) {
			if (offer != null && offer.item() != null) offerCount++;
		}
		int index = 0;
		float rowTop = pos;
		for (HomebaseState.DefenderTradeOffer offer : defender.tradeOffers()) {
			if (offer == null) continue;
			Item item = offer.item();
			if (item == null) continue;
			TradeOfferButton button = new TradeOfferButton( index, offer );
			add( button );
			int col = index % columns;
			int row = index / columns;
			int rowItems = Math.min( columns, offerCount - row * columns );
			float rowWidth = rowItems * SLOT_WIDTH + Math.max( 0, rowItems - 1 ) * GAP;
			float rowLeft = Math.max( 0, (width - rowWidth) / 2f );
			button.setRect( rowLeft + col * (SLOT_WIDTH + GAP), rowTop + row * (SLOT_HEIGHT + GAP), SLOT_WIDTH, SLOT_HEIGHT );
			index++;
		}
		int rows = (int)Math.ceil( index / (float)columns );
		pos = rowTop + rows * (SLOT_HEIGHT + GAP) + GAP;

		if (selectedOffer != null) {
			HomebaseState.DefenderTradeOffer offer = selectedOffer;
			Item item = offer.item();
			if (item != null) {
				RenderedTextBlock title = PixelScene.renderTextBlock( DefenderUi.itemTitle( item ), 6 );
				title.maxWidth( contentWidth );
				title.setPos( 0, pos );
				add( title );
				pos = title.bottom() + GAP;

				RenderedTextBlock info = PixelScene.renderTextBlock( item.info(), 6 );
				info.maxWidth( contentWidth );
				info.setPos( 0, pos );
				add( info );
				pos = info.bottom() + GAP;

				WndCurrencyLine price = WndCurrencyLine.tradePrice( offer );
				add( price );
				price.setRect( 0, pos, width, 0 );
				pos = price.bottom() + GAP;

				RedButton buy = new RedButton( "Buy", 6 ) {
					@Override
					protected void onClick() {
						if (callback != null) {
							callback.buy( offer );
						}
					}
				};
				buy.enable( defender.canBuyTradeOffer( offer ) );
				add( buy );
				buy.setRect( 0, pos, width, BUTTON_HEIGHT );
				pos = buy.bottom() + GAP;
			}
		}

		resizeContent( Math.max( viewportHeight, pos ) );
	}

	private void destroyChildren() {
		for (Gizmo child : members.toArray( new Gizmo[0] )) {
			if (child != null) child.destroy();
		}
		clear();
	}

	private void resizeContent( float height ) {
		setSize( width, height );
		if (parent instanceof Component) {
			((Component)parent).setSize( width, Math.max( viewportHeight, height ) );
		}
	}

	interface TradeCallback {
		void buy( HomebaseState.DefenderTradeOffer offer );
	}

	private class TradeOfferButton extends Button {

		private final int index;
		private final HomebaseState.DefenderTradeOffer offer;
		private final Image bg;
		private final ColorBlock selectedFill;
		private final ItemSprite icon;
		private final RenderedTextBlock quantity;

		private TradeOfferButton( int index, HomebaseState.DefenderTradeOffer offer ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.index = index;
			this.offer = offer;

			bg = new Image( Assets.Interfaces.TALENT_BUTTON );
			bg.frame( 0, 0, 20, 26 );
			add( bg );

			selectedFill = new ColorBlock( 0, 4, 0xFFFFFF44 );
			add( selectedFill );

			icon = new ItemSprite( offer.item() );
			add( icon );

			quantity = PixelScene.renderTextBlock( 5 );
			add( quantity );
		}

		@Override
		protected void layout() {
			width = SLOT_WIDTH;
			height = SLOT_HEIGHT;
			super.layout();

			bg.x = x + (width - 20) / 2f;
			bg.y = y;
			boolean selected = selectedOffer == offer;
			bg.am = selected ? 1f : 0.72f;

			selectedFill.x = bg.x + 2;
			selectedFill.y = bg.y + 19;
			selectedFill.size( selected ? 16 : 0, 5 );
			selectedFill.hardlight( Window.TITLE_COLOR );

			icon.x = bg.x + (20 - icon.width()) / 2f;
			icon.y = bg.y + 1 + (18 - icon.height()) / 2f;
			icon.am = selected ? 1f : 0.8f;
			PixelScene.align( icon );

			int qty = offer.item() == null ? 0 : offer.item().quantity();
			quantity.text( qty > 1 ? Integer.toString( qty ) : "" );
			quantity.setPos( bg.x + 18 - quantity.width(), bg.y - 1 );
			PixelScene.align( quantity );
		}

		@Override
		protected void onClick() {
			if (DefenderTradeContent.this.parent == null || DefenderTradeContent.this.members == null) {
				return;
			}
			selectedOffer = offer;
			rebuild();
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			icon.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			bg.resetColor();
			icon.resetColor();
		}

		@Override
		protected String hoverText() {
			return offer.item() == null ? null : offer.item().name();
		}
	}
}
