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

import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.TranscendantProgressBar;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndTranscendantChoice extends Window {

	private static final int WIDTH_DESKTOP = 150;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 28;
	private static final float INPUT_LOCKOUT = 0.25f;

	private final ArrayList<RedButton> lockedButtons = new ArrayList<>();
	private float inputLockout = INPUT_LOCKOUT;

	public WndTranscendantChoice( final Item item ) {
		this( item, null, false, null, null, null );
	}

	public WndTranscendantChoice(
			final Item item,
			final String defenderName,
			final boolean defenderItem,
			final Runnable onApplied,
			final Runnable onComplete,
			final Runnable onLater ) {
		super();
		int windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		IconTitle titlebar = new IconTitle( item );
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		TranscendantProgressBar progress = new TranscendantProgressBar( item );
		progress.setRect( MARGIN, titlebar.bottom() + MARGIN, windowWidth - MARGIN * 2, 0 );
		add( progress );

		String prompt = defenderName == null || defenderName.isEmpty()
				? "Choose one power to awaken."
				: "Choose one power for " + defenderName + "'s equipment.";
		RenderedTextBlock message = PixelScene.renderTextBlock( prompt, 6 );
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
					boolean applied = defenderItem
							? item.applyDefenderTranscendantChoice( choice )
							: item.applyTranscendantChoice( choice );
					if (applied) {
						if (onApplied != null) onApplied.run();
						if (item.hasPendingTranscendantChoice()) {
							showWindow( new WndTranscendantChoice(
									item, defenderName, defenderItem, onApplied, onComplete, onLater ) );
						} else if (onComplete != null) {
							onComplete.run();
						}
					} else {
						GLog.w( "The Transcendant power fades before it can take hold." );
					}
				}
			};
			button.textColor( choice.displayColor() );
			button.enable( false );
			lockedButtons.add( button );
			button.setRect( MARGIN, pos + MARGIN, windowWidth - MARGIN * 2, BUTTON_HEIGHT );
			add( button );
			pos = button.bottom();
		}

		RedButton cancel = new RedButton( "Later" ) {
			@Override
			protected void onClick() {
				hide();
				if (onLater != null) onLater.run();
			}
		};
		cancel.enable( false );
		lockedButtons.add( cancel );
		cancel.setRect( MARGIN, pos + MARGIN, windowWidth - MARGIN * 2, 18 );
		add( cancel );

		resize( windowWidth, (int)cancel.bottom() + MARGIN );
	}

	@Override
	public void update() {
		super.update();
		if (inputLockout > 0) {
			inputLockout -= Game.elapsed;
			if (inputLockout <= 0) {
				for (RedButton button : lockedButtons) button.enable( true );
			}
		}
	}

	private static void showWindow( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}
}
