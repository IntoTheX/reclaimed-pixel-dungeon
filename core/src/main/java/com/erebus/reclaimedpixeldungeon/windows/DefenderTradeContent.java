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
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.InventoryItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Gizmo;
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

	void setTradePos( float left, float top ) {
		if (x == left && y == top) return;
		setPos( left, top );
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
		pockets.setRect( x, y + pos, width, 0 );
		pos = pockets.bottom() - y + GAP;

		if (pockets.isEmpty()) {
			RenderedTextBlock emptyPockets = PixelScene.renderTextBlock( "empty pockets", 6 );
			emptyPockets.maxWidth( contentWidth );
			emptyPockets.setPos( x, y + pos );
			add( emptyPockets );
			pos = emptyPockets.bottom() - y + GAP;
		}

		if (defender == null || defender.tradeOffers().isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					(defender == null ? "This defender" : defender.defenderName()) + " has nothing to trade after their latest run.", 6 );
			empty.maxWidth( contentWidth );
			empty.setPos( x, y + pos );
			add( empty );
			pos = empty.bottom() - y + GAP;
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
			TradeOfferButton button = new TradeOfferButton( offer );
			add( button );
			int col = index % columns;
			int row = index / columns;
			int rowItems = Math.min( columns, offerCount - row * columns );
			float rowWidth = rowItems * SLOT_WIDTH + Math.max( 0, rowItems - 1 ) * GAP;
			float rowLeft = Math.max( 0, (width - rowWidth) / 2f );
			button.setRect( x + rowLeft + col * (SLOT_WIDTH + GAP), y + rowTop + row * (SLOT_HEIGHT + GAP), SLOT_WIDTH, SLOT_HEIGHT );
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
				title.setPos( x, y + pos );
				add( title );
				pos = title.bottom() - y + GAP;

				RenderedTextBlock info = PixelScene.renderTextBlock( item.info(), 6 );
				info.maxWidth( contentWidth );
				info.setPos( x, y + pos );
				add( info );
				pos = info.bottom() - y + GAP;

				WndCurrencyLine price = WndCurrencyLine.tradePrice( offer );
				add( price );
				price.setRect( x, y + pos, width, 0 );
				pos = price.bottom() - y + GAP;

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
				buy.setRect( x, y + pos, width, BUTTON_HEIGHT );
				pos = buy.bottom() - y + GAP;
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
		if (callback != null) callback.resized();
	}

	interface TradeCallback {
		void buy( HomebaseState.DefenderTradeOffer offer );

		default void resized() {
		}
	}

	private class TradeOfferButton extends InventoryItemButton {

		private final HomebaseState.DefenderTradeOffer offer;

		private TradeOfferButton( HomebaseState.DefenderTradeOffer offer ) {
			this.offer = offer;
			Item preview = offer.item().duplicate();
			if (preview == null) preview = offer.item();
			else preview.identifyForPreview();
			item( preview );
			forceIdentifiedAppearance( true );
		}

		@Override
		protected void layout() {
			super.layout();
			slot().alpha( selectedOffer == offer ? 1f : 0.72f );
		}

		@Override
		protected void onClick() {
			if (DefenderTradeContent.this.parent == null || DefenderTradeContent.this.members == null) {
				return;
			}
			selectedOffer = offer;
			rebuild();
		}
	}
}
