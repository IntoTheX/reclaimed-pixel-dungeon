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
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.HeroClassUnlocks;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.SaveTransferService;
import com.erebus.reclaimedpixeldungeon.scenes.InterlevelScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.scenes.StartScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.ui.ActionIndicator;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.StatusPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.Locale;

public class WndGameInProgress extends Window {
	
	private static final int WIDTH    = 120;
	
	private int GAP	  = 6;
	
	private float pos;
	
	public WndGameInProgress(final int slot){
		
		final GamesInProgress.Info info = GamesInProgress.check(slot);
		final boolean locked = !info.heroClass.isUnlocked();
		
		String className = null;
		if (info.subClass != HeroSubClass.NONE){
			className = info.subClass.title();
		} else {
			className = info.heroClass.title();
		}
		
		IconTitle title = new IconTitle();
		title.icon( HeroSprite.avatar(info.heroClass, info.armorTier) );
		String characterName = info.characterName == null || info.characterName.isEmpty()
				? Messages.titleCase( className )
				: info.characterName;
		title.label((characterName + "\n" + Messages.get(this, "title", info.level, className)).toUpperCase(Locale.ENGLISH));
		title.color(Window.TITLE_COLOR);
		title.setRect( 0, 0, WIDTH, 0 );
		add(title);
		
		if (info.challenges > 0) GAP -= 2;
		
		pos = title.bottom() + GAP;
		
		if (info.challenges > 0) {
			RedButton btnChallenges = new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					Game.scene().add( new WndChallenges( info.challenges, false ) );
				}
			};
			btnChallenges.icon(Icons.get(Icons.CHALLENGE_COLOR));
			float btnW = btnChallenges.reqWidth() + 2;
			btnChallenges.setRect( (WIDTH - btnW)/2, pos, btnW , 18 );
			add( btnChallenges );
			
			pos = btnChallenges.bottom() + GAP;
		}
		
		pos += GAP;

		int strBonus = info.strBonus;
		if (strBonus > 0)           statSlot( Messages.get(this, "str"), info.str + " + " + strBonus );
		else if (strBonus < 0)      statSlot( Messages.get(this, "str"), info.str + " - " + -strBonus );
		else                        statSlot( Messages.get(this, "str"), info.str );
		statSlot( Messages.get(this, "health"), StatusPane.compactBarNumber( info.hp ) + "/" + StatusPane.compactBarNumber( info.ht ) );
		statSlot( Messages.get(this, "shield"), StatusPane.compactBarNumber( info.shld ) );
		statSlot( Messages.get(this, "exp"), info.exp + "/" + Hero.maxExp(info.level) );
		
		pos += GAP;
		statSlot( Messages.get(this, "gold"), info.goldCollected );
		statSlot( Messages.get(this, "depth"), info.maxDepth );
		if (info.daily) {
			if (info.dailyReplay) {
				statSlot(Messages.get(this, "replay_for"), "_" + info.customSeed + "_");
			} else {
				statSlot(Messages.get(this, "daily_for"), "_" + info.customSeed + "_");
			}
		} else if (!info.customSeed.isEmpty()){
			statSlot( Messages.get(this, "custom_seed"), "_" + info.customSeed + "_" );
		} else {
			statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(info.seed) );
		}
		
		pos += GAP;
		
		RedButton cont = new RedButton(Messages.get(this, locked ? "locked" : "continue")){
			@Override
			protected void onClick() {
				super.onClick();

				if (locked) {
					ShatteredPixelDungeon.scene().addToFront( new WndMessage( HeroClassUnlocks.lockMessage( info.heroClass ) ) );
					return;
				}

				if (!info.hasCustomName) {
					promptForCharacterName( new Runnable() {
						@Override
						public void run() {
							continueGame( slot );
						}
					} );
				} else {
					continueGame( slot );
				}
			}
		};
		
		RedButton erase = new RedButton( Messages.get(this, "erase")){
			@Override
			protected void onClick() {
				super.onClick();
				
				ShatteredPixelDungeon.scene().add(new WndOptions(Icons.get(Icons.WARNING),
						Messages.get(WndGameInProgress.class, "erase_warn_title"),
						Messages.get(WndGameInProgress.class, "erase_warn_body"),
						Messages.get(WndGameInProgress.class, "erase_warn_yes"),
						Messages.get(WndGameInProgress.class, "erase_warn_no") ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService.queueCharacterEnd( slot, "deleted" );
							Dungeon.deleteGame(slot, true);
							ShatteredPixelDungeon.switchNoFade(StartScene.class);
						}
					}
				} );
			}
		};

		cont.icon(locked ? new Image( Assets.Interfaces.LOCKED ) : Icons.get(Icons.ENTER));
		cont.setRect(0, pos, WIDTH/2 -1, 20);
		add(cont);

		erase.icon(Icons.get(Icons.CLOSE));
		erase.setRect(WIDTH/2 + 1, pos, WIDTH/2 - 1, 20);
		add(erase);

		RedButton transfer = new RedButton( Messages.get(this, "transfer") ) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
				final WndMessage searching = new WndMessage(Messages.get(WndGameInProgress.class, "transfer_searching"));
				ShatteredPixelDungeon.scene().addToFront(searching);
				new Thread(() -> {
					final ArrayList<SaveTransferService.Peer> peers = SaveTransferService.discover(5000);
					Game.runOnRenderThread(new Callback() {
						@Override
						public void call() {
							searching.hide();
							if (peers.isEmpty()) {
								ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndGameInProgress.class, "transfer_none")));
								return;
							}
							String[] names = new String[peers.size()];
							for (int i = 0; i < peers.size(); i++) names[i] = peers.get(i).name;
							ShatteredPixelDungeon.scene().addToFront(new WndOptions(Icons.get(Icons.CHANGES),
									Messages.get(WndGameInProgress.class, "transfer_title"),
									Messages.get(WndGameInProgress.class, "transfer_body"), names) {
								@Override
								protected void onSelect(int index) {
									if (index >= 0 && index < peers.size()) SaveTransferService.send(peers.get(index), slot);
								}
							});
						}
					});
				}, "Save Transfer Search UI").start();
			}
		};
		transfer.icon(Icons.get(Icons.CHANGES));
		transfer.setRect(0, cont.bottom() + 2, WIDTH, 20);
		add(transfer);
		
		resize(WIDTH, (int)transfer.bottom()+1);
	}

	private static void continueGame( int slot ) {
		GamesInProgress.curSlot = slot;

		Dungeon.hero = null;
		Dungeon.daily = Dungeon.dailyReplay = false;
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		ShatteredPixelDungeon.switchScene(InterlevelScene.class);
	}

	private static void promptForCharacterName( final Runnable onNamed ) {
		ShatteredPixelDungeon.scene().addToFront( new WndTextInput(
				Messages.get( StartScene.class, "name_title" ),
				Messages.get( StartScene.class, "name_body" ),
				"",
				20,
				false,
				Messages.get( StartScene.class, "name_confirm" ),
				null ) {
			@Override
			public void onSelect( boolean positive, String text ) {
				String name = GamesInProgress.cleanCharacterName( text );
				if (name.isEmpty()) {
					ShatteredPixelDungeon.scene().addToFront( new WndMessage( Messages.get( StartScene.class, "name_empty" ) ) );
					promptForCharacterName( onNamed );
					return;
				}
				GamesInProgress.pendingCharacterName( name );
				onNamed.run();
			}
		} );
	}
	
	private void statSlot( String label, String value ) {

		int size = 8;
		RenderedTextBlock txt;
		do {
			txt = PixelScene.renderTextBlock( label, size );
			size--;
		} while (txt.width() >= WIDTH * 0.55f);
		txt.setPos(0, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add( txt );

		size = 8;
		do {
			txt = PixelScene.renderTextBlock( value, size );
			size--;
		} while (txt.width() >= WIDTH * 0.45f);
		txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
		PixelScene.align(txt);
		add( txt );
		
		pos += GAP + txt.height();
	}
	
	private void statSlot( String label, int value ) {
		statSlot( label, Integer.toString( value ) );
	}
}
