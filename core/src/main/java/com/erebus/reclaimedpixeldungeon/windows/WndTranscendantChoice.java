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

import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.TranscendantProgressBar;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class WndTranscendantChoice extends Window {

	private static final int WIDTH_DESKTOP = 150;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 28;

	public WndTranscendantChoice( final Item item ) {
		super();
		int windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		IconTitle titlebar = new IconTitle( item );
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		TranscendantProgressBar progress = new TranscendantProgressBar( item );
		progress.setRect( MARGIN, titlebar.bottom() + MARGIN, windowWidth - MARGIN * 2, 0 );
		add( progress );

		RenderedTextBlock message = PixelScene.renderTextBlock( "Choose one power to awaken.", 6 );
		message.maxWidth( windowWidth - MARGIN * 2 );
		message.setPos( MARGIN, progress.bottom() + MARGIN );
		add( message );

		float pos = message.bottom();
		ArrayList<Item.TranscendantChoice> choices = item.transcendantChoices();
		if (choices.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock( "No valid upgrades are available right now.", 6 );
			empty.maxWidth( windowWidth - MARGIN * 2 );
			empty.setPos( MARGIN, pos + MARGIN );
			add( empty );
			pos = empty.bottom();
		}

		for (final Item.TranscendantChoice choice : choices) {
			RedButton button = new RedButton( choice.label(), 6 ) {
				@Override
				protected void onClick() {
					hide();
					if (item.applyTranscendantChoice( choice )) {
						if (item.hasPendingTranscendantChoice()) {
							GameScene.show( new WndTranscendantChoice( item ) );
						}
					} else {
						GLog.w( "The Transcendant power fades before it can take hold." );
					}
				}
			};
			button.textColor( choice.displayColor() );
			button.setRect( MARGIN, pos + MARGIN, windowWidth - MARGIN * 2, BUTTON_HEIGHT );
			add( button );
			pos = button.bottom();
		}

		RedButton cancel = new RedButton( "Later" ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancel.setRect( MARGIN, pos + MARGIN, windowWidth - MARGIN * 2, 18 );
		add( cancel );

		resize( windowWidth, (int)cancel.bottom() + MARGIN );
	}
}
