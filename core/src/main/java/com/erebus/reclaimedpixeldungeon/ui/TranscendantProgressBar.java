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

package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class TranscendantProgressBar extends Component {

	private static final int COLOR_TRACK = 0xFF3A210C;
	private static final int COLOR_FILL = 0xFFFF8A00;
	private static final int BAR_HEIGHT = 8;
	private static final int GAP = 1;

	private final Item item;
	private RenderedTextBlock label;
	private ColorBlock track;
	private ColorBlock fill;
	private BitmapText xpText;

	public TranscendantProgressBar( Item item ) {
		super();
		this.item = item;
	}

	@Override
	protected void createChildren() {
		label = PixelScene.renderTextBlock( "", 6 );
		label.hardlight( COLOR_FILL );
		label.setHightlighting( false );
		add( label );

		track = new ColorBlock( 1, 1, COLOR_TRACK );
		add( track );

		fill = new ColorBlock( 1, 1, COLOR_FILL );
		add( fill );

		xpText = new BitmapText( PixelScene.pixelFont );
		xpText.hardlight( 0xFFFFFFFF );
		add( xpText );
	}

	@Override
	protected void layout() {
		int xp = Math.max( 0, item.transcendantXP() );
		int xpToNext = Math.max( 1, item.transcendantXPToNext() );
		float progress = Math.max( 0f, Math.min( 1f, xp / (float)xpToNext ) );

		label.text( "Transcendant Lv. " + item.transcendantLevel() );
		label.maxWidth( (int)width );
		label.setPos( x, y );

		float barY = label.bottom() + GAP;
		track.x = fill.x = x;
		track.y = fill.y = barY;
		track.size( width, BAR_HEIGHT );
		fill.size( width * progress, BAR_HEIGHT );

		xpText.text( StatusPane.compactBarNumber( xp ) + "/" + StatusPane.compactBarNumber( xpToNext ) );
		xpText.measure();
		xpText.x = x + (width - xpText.width()) / 2f;
		xpText.y = barY + (BAR_HEIGHT - 1 - xpText.baseLine()) / 2f;
		PixelScene.align( xpText );

		height = label.height() + GAP + BAR_HEIGHT;
	}
}
