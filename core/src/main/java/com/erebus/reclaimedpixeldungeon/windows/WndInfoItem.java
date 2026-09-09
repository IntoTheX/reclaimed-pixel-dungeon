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

import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.ItemSlot;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.TranscendantProgressBar;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public class WndInfoItem extends Window {
	
	private static final float GAP	= 2;
	private static final float INNER_MARGIN = 2;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private static final int FIXED_DETAIL_WIDTH = ReclaimedWindow.INVENTORY_WIDTH;
	private static final int FIXED_DETAIL_HEIGHT = 160;
	private static final int USE_ITEM_BUTTON_SCROLL_PAD = 38;

	//only one WndInfoItem can appear at a time
	private static WndInfoItem INSTANCE;

	public WndInfoItem( Heap heap ) {

		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;

		if (heap.type == Heap.Type.HEAP) {
			fillFields( heap.peek() );

		} else {
			fillFields( heap );

		}
	}
	
	public WndInfoItem( Item item ) {
		super();

		if (INSTANCE != null){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		fillFields( item );
	}

	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}

	private void fillFields(Heap heap ) {
		
		IconTitle titlebar = new IconTitle( heap );
		titlebar.color( TITLE_COLOR );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( heap.info(), 6 );

		layoutFields(titlebar, txtInfo, null);
	}
	
	private void fillFields( Item item ) {
		
		int color = TITLE_COLOR;
		if (item.levelKnown && item.level() > 0) {
			color = ItemSlot.UPGRADED;
		} else if (item.levelKnown && item.level() < 0) {
			color = ItemSlot.DEGRADED;
		}
		if (item.showsRarityStats()) {
			color = item.rarityColor();
			ReclaimedTutorial.flash( Document.GUIDE_RARITY_STATS );
		}

		IconTitle titlebar = new IconTitle( item );
		titlebar.color( color );
		
		RenderedTextBlock txtInfo = PixelScene.renderTextBlock( item.info(), 6 );
		TranscendantProgressBar progress = item.showsRarityStats() && item.isTranscendantRarity()
				? new TranscendantProgressBar( item )
				: null;
		
		layoutFields(titlebar, txtInfo, progress);
	}

	private void layoutFields(IconTitle title, RenderedTextBlock info, TranscendantProgressBar progress){
		boolean fixedDetail = progress != null;
		int width = fixedDetail ? ReclaimedWindow.modalWidth( FIXED_DETAIL_WIDTH ) : ReclaimedWindow.modalWidth( WIDTH_MIN );

		int textWidth = fixedDetail ? Math.max( 20, (int)(width - 2 * INNER_MARGIN) ) : width;
		info.maxWidth(textWidth);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (!fixedDetail
				&& width != ReclaimedWindow.INVENTORY_WIDTH
				&& PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			textWidth = width;
			info.maxWidth(textWidth);
		}

		//leaves some space to add the journal button in WndUseItem. This is messy I know.
		if (this instanceof WndUseItem){
			title.setRect( 0, 0, width-16, 0 );
		} else {
			title.setRect( 0, 0, width, 0 );
		}
		add( title );

		float pos = title.bottom() + GAP;
		if (progress != null) {
			progress.setRect( title.left(), pos, width, 0 );
			add( progress );
			pos = progress.bottom() + GAP;
		}

		int naturalHeight = (int)(pos + info.height() + 2);
		int maxHeight = fixedDetail
				? ReclaimedWindow.modalHeight( FIXED_DETAIL_HEIGHT, 0 )
				: ReclaimedWindow.modalHeight( naturalHeight, 0 );

		if (naturalHeight > maxHeight) {
			Component content = new Component();
			info.setPos( fixedDetail ? INNER_MARGIN : 0, 0 );
			content.add( info );
			float bottomPad = this instanceof WndUseItem ? USE_ITEM_BUTTON_SCROLL_PAD : 2;
			content.setSize( fixedDetail ? width : textWidth, info.bottom() + bottomPad );

			ScrollPane pane = new ScrollPane( content );
			float paneX = title.left();
			float paneWidth = width;
			add( pane );
			// Attach the pane to the window camera before resize repositions that camera.
			// Otherwise desktop scroll content can remain bound to the scene viewport.
			resize( width, maxHeight );
			pane.setRect( paneX, pos, paneWidth, Math.max( 20, maxHeight - pos - INNER_MARGIN ) );
			pane.scrollTo( 0, 0 );
		} else {
			info.setPos(title.left() + (fixedDetail ? INNER_MARGIN : 0), pos);
			add( info );

			resize( width, naturalHeight );
		}
	}
}
