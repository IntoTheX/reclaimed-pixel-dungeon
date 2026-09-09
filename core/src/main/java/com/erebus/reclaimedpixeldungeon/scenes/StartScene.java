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

package com.erebus.reclaimedpixeldungeon.scenes;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Chrome;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.journal.Journal;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.SaveTransferService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.ExitButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.TitleBackground;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.StyledButton;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.erebus.reclaimedpixeldungeon.windows.WndGameInProgress;
import com.erebus.reclaimedpixeldungeon.windows.WndMessage;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.utils.RectF;

import java.util.ArrayList;

public class StartScene extends PixelScene {
	
	private static final int SLOT_WIDTH = 120;
	private static final int SLOT_HEIGHT = 22;
	
	@Override
	public void create() {
		super.create();
		WayfarerAccountService.finishAcknowledgedDeletion();
		SaveTransferService.startReceiver();
		
		Badges.loadGlobal();
		Journal.loadGlobal();
		
		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();

		TitleBackground BG = new TitleBackground(w, h);
		add( BG );

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;
		
		ExitButton btnExit = new ExitButton();
		btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
		add( btnExit );
		
		IconTitle title = new IconTitle( Icons.ENTER.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);
		
		ArrayList<GamesInProgress.Info> games = GamesInProgress.checkAll();
		
		int slotCount = Math.min(GamesInProgress.MAX_SLOTS, games.size()+1);
		int slotGap = 10 - slotCount;
		int slotsHeight = slotCount*SLOT_HEIGHT + (slotCount-1)* slotGap;
		slotsHeight += 14;

		while (slotGap >= 2 && slotsHeight > (h-title.bottom()-2)){
			slotGap--;
			slotsHeight -= slotCount-1;
		}
		
		float yPos = insets.top + (h - slotsHeight + title.bottom() + 2)/2f - 4;
		yPos = Math.max(yPos, title.bottom()+2);
		float slotLeft = insets.left + (w - SLOT_WIDTH) / 2f;
		
		for (GamesInProgress.Info game : games) {
			SaveSlotButton existingGame = new SaveSlotButton();
			existingGame.set(game.slot);
			existingGame.setRect(slotLeft, yPos, SLOT_WIDTH, SLOT_HEIGHT);
			yPos += SLOT_HEIGHT + slotGap;
			align(existingGame);
			add(existingGame);
			
		}
		
		if (games.size() < GamesInProgress.MAX_SLOTS){
			SaveSlotButton newGame = new SaveSlotButton();
			newGame.set(GamesInProgress.firstEmpty());
			newGame.setRect(slotLeft, yPos, SLOT_WIDTH, SLOT_HEIGHT);
			yPos += SLOT_HEIGHT + slotGap;
			align(newGame);
			add(newGame);
		}
		
		GamesInProgress.curSlot = 0;

		String sortText = "";
		switch (SPDSettings.gamesInProgressSort()){
			case "level":
				sortText = Messages.get(this, "sort_level");
				break;
			case "last_played":
				sortText = Messages.get(this, "sort_recent");
				break;
		}

		StyledButton btnSort = new StyledButton(Chrome.Type.TOAST_TR, sortText, 6){
			@Override
			protected void onClick() {
				super.onClick();

				if (SPDSettings.gamesInProgressSort().equals("level")){
					SPDSettings.gamesInProgressSort("last_played");
				} else {
					SPDSettings.gamesInProgressSort("level");
				}

				ShatteredPixelDungeon.seamlessResetScene();
			}
		};
		btnSort.textColor(0xCCCCCC);

		if (yPos + 10 > Camera.main.height) {
			btnSort.setRect(slotLeft - btnSort.reqWidth() - 6, Camera.main.height - 14, btnSort.reqWidth() + 4, 12);
		} else {
			btnSort.setRect(slotLeft, yPos, btnSort.reqWidth() + 4, 12);
		}
		if (games.size() >= 2) add(btnSort);

		fadeIn();
		
	}

	@Override
	public void update() {
		super.update();
		WayfarerAccountService.pollCharacterEnds();
		String deletionNotice = WayfarerAccountService.consumeDeletionNotice();
		if (deletionNotice != null) addToFront( new WndMessage( deletionNotice ) );
		SaveTransferService.IncomingRequest request = SaveTransferService.consumeIncoming();
		if (request != null) showIncomingTransfer(request);
		int transferredSlot = SaveTransferService.consumeCompletedSenderSlot();
		if (transferredSlot > 0 && GamesInProgress.gameExists(transferredSlot)) {
			Dungeon.deleteGame(transferredSlot, true);
		}
		String notice = SaveTransferService.consumeNotice();
		if (notice != null) {
			addToFront(new WndMessage(notice) {
				@Override
				public void hide() {
					super.hide();
					ShatteredPixelDungeon.seamlessResetScene();
				}
			});
		}
	}

	private void showIncomingTransfer(final SaveTransferService.IncomingRequest request) {
		final int slot = GamesInProgress.firstEmpty();
		if (slot == -1) {
			SaveTransferService.decline(request);
			addToFront(new WndMessage("There are no empty character slots available."));
			return;
		}

		addToFront(new WndOptions(Icons.get(Icons.CHANGES),
				"Incoming Save Transfer",
				request.deviceName + " is asking to send a saved file (" + request.characterName + " Level " + request.level + "). It will be placed in empty slot " + slot + ".",
				"Accept", "Decline") {
			@Override
			protected void onSelect(int index) {
				if (index == 0) SaveTransferService.accept(request, slot);
				else SaveTransferService.decline(request);
			}

			@Override
			public void onBackPressed() {
				SaveTransferService.decline(request);
				super.onBackPressed();
			}
		});
	}

	@Override
	protected void onBackPressed() {
		SaveTransferService.stopReceiver();
		ShatteredPixelDungeon.switchNoFade( TitleScene.class );
	}

	@Override
	public void destroy() {
		SaveTransferService.stopReceiver();
		super.destroy();
	}
	
	private static class SaveSlotButton extends Button {
		
		private NinePatch bg;
		
		private Image hero;
		private RenderedTextBlock name;
		private RenderedTextBlock lastPlayed;
		
		private Image steps;
		private BitmapText depth;
		private Image classIcon;
		private BitmapText level;
		
		private int slot;
		private boolean newGame;
		private boolean locked;
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			bg = Chrome.get(Chrome.Type.TOAST_TR);
			add( bg );
			
			name = PixelScene.renderTextBlock(9);
			add(name);

			lastPlayed = PixelScene.renderTextBlock(6);
			add(lastPlayed);
		}
		
		public void set( int slot ){
			this.slot = slot;
			GamesInProgress.Info info = GamesInProgress.check(slot);
			newGame = info == null;
			locked = false;
			if (newGame){
				name.text( Messages.get(StartScene.class, "new"));
				
				if (hero != null){
					remove(hero);
					hero = null;
					remove(steps);
					steps = null;
					remove(depth);
					depth = null;
					remove(classIcon);
					classIcon = null;
					remove(level);
					level = null;
				}
				name.resetColor();
				lastPlayed.resetColor();
			} else {
				locked = !info.heroClass.isUnlocked();
				
				String className = info.subClass != HeroSubClass.NONE ? info.subClass.title() : info.heroClass.title();
				name.text(info.characterName == null || info.characterName.isEmpty() ? Messages.titleCase(className) : info.characterName);
				
				if (hero == null){
					hero = new Image(info.heroClass.spritesheet(), 0, 15*info.armorTier, 12, 15);
					add(hero);
					
					steps = locked ? new Image( Assets.Interfaces.LOCKED ) : new Image(Icons.get(Icons.STAIRS));
					add(steps);
					depth = new BitmapText(PixelScene.pixelFont);
					add(depth);
					
					classIcon = new Image(Icons.get(info.heroClass));
					add(classIcon);
					level = new BitmapText(PixelScene.pixelFont);
					add(level);
				} else {
					hero.copy(new Image(info.heroClass.spritesheet(), 0, 15*info.armorTier, 12, 15));
					steps.copy( locked ? new Image( Assets.Interfaces.LOCKED ) : Icons.get( Icons.STAIRS ) );
					
					classIcon.copy(Icons.get(info.heroClass));
				}

				long diff = Game.realTime - info.lastPlayed;
				if (diff > 99L * 30 * 24 * 60 * 60_000){
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), " ")); //show no time for >99 months ago
				} else if (diff < 60_000){
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), Messages.get(StartScene.class, "one_minute_ago")));
				} else if (diff < 2 * 60 * 60_000){
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), Messages.get(StartScene.class, "minutes_ago", diff / 60_000)));
				} else if (diff < 2 * 24 * 60 * 60_000){
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), Messages.get(StartScene.class, "hours_ago", diff / (60 * 60_000))));
				} else if (diff < 2L * 30 * 24 * 60 * 60_000){
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), Messages.get(StartScene.class, "days_ago", diff / (24 * 60 * 60_000))));
				} else {
					lastPlayed.text(Messages.get(StartScene.class, "class_last_played", Messages.titleCase(className), Messages.get(StartScene.class, "months_ago", diff / (30L * 24 * 60 * 60_000))));
				}
				
				depth.text(locked ? "" : Integer.toString(info.depth));
				depth.measure();
				
				level.text(Integer.toString(info.level));
				level.measure();
				
				steps.resetColor();
				if (locked) {
					name.hardlight(0x888888);
					lastPlayed.hardlight(0x888888);
					depth.resetColor();
					level.resetColor();
				} else if (info.challenges > 0){
					name.hardlight(Window.TITLE_COLOR);
					lastPlayed.hardlight(Window.TITLE_COLOR);
					depth.hardlight(Window.TITLE_COLOR);
					level.hardlight(Window.TITLE_COLOR);
				} else {
					name.resetColor();
					lastPlayed.resetColor();
					depth.resetColor();
					level.resetColor();
				}

				if (!locked && info.daily){
					if (info.dailyReplay){
						steps.hardlight(1f, 0.5f, 2f);
					} else {
						steps.hardlight(0.5f, 1f, 2f);
					}
				} else if (!locked && !info.customSeed.isEmpty()){
					steps.hardlight(1f, 1.5f, 0.67f);
				}
				
			}
			
			layout();
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
			
			if (hero != null){
				hero.x = x+8;
				hero.y = y + (height - hero.height())/2f;
				align(hero);
				
				name.setPos(
						hero.x + hero.width() + 6,
						y + (height - name.height() - lastPlayed.height() - 2)/2f
				);
				align(name);

				lastPlayed.setPos(
						hero.x + hero.width() + 6,
						name.bottom()+2
				);
				
				classIcon.x = x + width - 24 + (16 - classIcon.width())/2f;
				classIcon.y = y + (height - classIcon.height())/2f;
				align(classIcon);
				
				level.x = classIcon.x + (classIcon.width() - level.width()) / 2f;
				level.y = classIcon.y + (classIcon.height() - level.height()) / 2f + 1;
				align(level);
				
				steps.x = x + width - 40 + (16 - steps.width())/2f;
				steps.y = y + (height - steps.height())/2f;
				align(steps);
				
				depth.x = steps.x + (steps.width() - depth.width()) / 2f;
				depth.y = steps.y + (steps.height() - depth.height()) / 2f + 1;
				align(depth);
				
			} else {
				name.setPos(
						x + (width - name.width())/2f,
						y + (height - name.height())/2f
				);
				align(name);
			}
			
			
		}
		
		@Override
		protected void onClick() {
			if (newGame) {
				GamesInProgress.selectedClass = null;
				GamesInProgress.curSlot = slot;
				ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
			} else {
				ShatteredPixelDungeon.scene().add( new WndGameInProgress(slot));
			}
		}
	}
}
