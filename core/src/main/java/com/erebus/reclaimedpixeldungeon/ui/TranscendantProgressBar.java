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
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class TranscendantProgressBar extends Component {

	private static final int COLOR_BORDER = 0xFF3B2B12;
	private static final int COLOR_TRACK = 0xFF1F1B12;
	private static final int COLOR_FILL = 0xFFFFE866;
	private static final int COLOR_GLOW = 0xFFFFB33A;
	private static final int BAR_HEIGHT = 4;
	private static final int GAP = 1;

	private final Item item;
	private RenderedTextBlock label;
	private ColorBlock border;
	private ColorBlock track;
	private ColorBlock glow;
	private ColorBlock fill;

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

		border = new ColorBlock( 1, 1, COLOR_BORDER );
		add( border );

		track = new ColorBlock( 1, 1, COLOR_TRACK );
		add( track );

		glow = new ColorBlock( 1, 1, COLOR_GLOW );
		glow.alpha( 0.35f );
		add( glow );

		fill = new ColorBlock( 1, 1, COLOR_FILL );
		add( fill );
	}

	@Override
	protected void layout() {
		int xp = Math.max( 0, item.transcendantXP() );
		int xpToNext = Math.max( 1, item.transcendantXPToNext() );
		float progress = Math.max( 0f, Math.min( 1f, xp / (float)xpToNext ) );

		label.text( "Transcendant Lv. " + item.transcendantLevel() + "  " + xp + "/" + xpToNext + " XP" );
		label.maxWidth( (int)width );
		label.setPos( x, y );

		float barY = label.bottom() + GAP;
		float innerWidth = Math.max( 0, width - 2 );
		float fillWidth = innerWidth * progress;

		border.x = x;
		border.y = barY;
		border.size( width, BAR_HEIGHT );

		track.x = x + 1;
		track.y = barY + 1;
		track.size( innerWidth, BAR_HEIGHT - 2 );

		glow.x = x + 1;
		glow.y = barY + 1;
		glow.size( fillWidth, BAR_HEIGHT - 2 );

		fill.x = x + 1;
		fill.y = barY + 1;
		fill.size( fillWidth, BAR_HEIGHT - 2 );

		height = label.height() + GAP + BAR_HEIGHT;
	}
}
