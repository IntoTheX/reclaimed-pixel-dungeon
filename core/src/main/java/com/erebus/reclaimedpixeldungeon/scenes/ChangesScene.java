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
import com.erebus.reclaimedpixeldungeon.Chrome;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.messages.Languages;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.ui.ExitButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.TitleBackground;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.StyledButton;
import com.erebus.reclaimedpixeldungeon.ui.changelist.ChangeInfo;
import com.erebus.reclaimedpixeldungeon.ui.changelist.Reclaimed_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.WndChanges;
import com.erebus.reclaimedpixeldungeon.ui.changelist.WndChangesTabbed;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_1_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_2_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_3_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_4_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_5_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_6_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_7_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_8_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v0_9_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v1_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v2_X_Changes;
import com.erebus.reclaimedpixeldungeon.ui.changelist.v3_X_Changes;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

import java.util.ArrayList;

public class ChangesScene extends PixelScene {

	public static final int SHATTERED_CHANGES = 0;
	public static final int RECLAIMED_CHANGES = 1;

	public static int changeCategory = RECLAIMED_CHANGES;
	public static int changesSelected = 0;

	private NinePatch rightPanel;
	private ScrollPane rightScroll;
	private IconTitle changeTitle;
	private RenderedTextBlock changeBody;
	
	@Override
	public void create() {
		super.create();

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		TitleBackground BG = new TitleBackground(w, h);
		//background added later

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		IconTitle title = new IconTitle(Icons.CHANGES.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
		add( btnExit );

		NinePatch panel = Chrome.get(Chrome.Type.TOAST);

		int tabHeight = 16;
		float tabTop = insets.top + 20;
		float panelTop = tabTop + tabHeight + 2;

		int pw = 135 + panel.marginLeft() + panel.marginRight() - 2;
		int ph = h - 36 - tabHeight - 2;

		if (h >= PixelScene.MIN_HEIGHT_FULL && w >= 300) {
			panel.size( pw, ph );
			panel.x = insets.left + (w - pw) / 2f - pw/2 - 1;
			panel.y = panelTop;

			rightPanel = Chrome.get(Chrome.Type.TOAST);
			rightPanel.size( pw, ph );
			rightPanel.x = insets.left + (w - pw) / 2f + pw/2 + 1;
			rightPanel.y = panelTop;
			add(rightPanel);

			rightScroll = new ScrollPane(new Component());
			add(rightScroll);
			rightScroll.setRect(
					rightPanel.x + rightPanel.marginLeft(),
					rightPanel.y + rightPanel.marginTop()-1,
					rightPanel.innerWidth() + 2,
					rightPanel.innerHeight() + 2);
			rightScroll.scrollTo(0, 0);

			changeTitle = new IconTitle(Icons.get(Icons.CHANGES), Messages.get(this, "right_title"));
			changeTitle.setPos(0, 1);
			changeTitle.setSize(pw, 20);
			rightScroll.content().add(changeTitle);

			String body = Messages.get(this, "right_body");

			changeBody = PixelScene.renderTextBlock(body, 6);
			changeBody.maxWidth(pw - panel.marginHor());
			changeBody.setPos(0, changeTitle.bottom()+2);
			rightScroll.content().add(changeBody);

		} else {
			panel.size( pw, ph );
			panel.x = insets.left + (w - pw) / 2f;
			panel.y = panelTop;
		}
		align( panel );
		add( panel );

		float categoryTabsWidth = h >= PixelScene.MIN_HEIGHT_FULL && w >= 300 ? 2 * pw + 2 : pw;
		addCategoryTabs(panel.x, tabTop, categoryTabsWidth, tabHeight);
		
		final ArrayList<ChangeInfo> changeInfos = new ArrayList<>();

		if (Messages.lang() != Languages.ENGLISH){
			ChangeInfo langWarn = new ChangeInfo("", true, Messages.get(this, "lang_warn"));
			langWarn.hardlight(CharSprite.WARNING);
			changeInfos.add(langWarn);
		}
		
		if (changeCategory == RECLAIMED_CHANGES){
			Reclaimed_Changes.addAllChanges(changeInfos);
		} else {
			switch (changesSelected){
				case 0: default:
					v3_X_Changes.addAllChanges(changeInfos);
					break;
				case 1:
					v2_X_Changes.addAllChanges(changeInfos);
					break;
				case 2:
					v1_X_Changes.addAllChanges(changeInfos);
					break;
				case 3:
					v0_9_X_Changes.addAllChanges(changeInfos);
					break;
				case 4:
					v0_8_X_Changes.addAllChanges(changeInfos);
					break;
				case 5:
					v0_7_X_Changes.addAllChanges(changeInfos);
					break;
				case 6:
					v0_6_X_Changes.addAllChanges(changeInfos);
					break;
				case 7:
					v0_5_X_Changes.addAllChanges(changeInfos);
					v0_4_X_Changes.addAllChanges(changeInfos);
					v0_3_X_Changes.addAllChanges(changeInfos);
					v0_2_X_Changes.addAllChanges(changeInfos);
					v0_1_X_Changes.addAllChanges(changeInfos);
					break;
			}
		}

		ScrollPane list = new ScrollPane( new Component() ){

			@Override
			public void onClick(float x, float y) {
				for (ChangeInfo info : changeInfos){
					if (info.onClick( x, y )){
						return;
					}
				}
			}

		};
		add( list );

		Component content = list.content();
		content.clear();

		float posY = 0;
		float nextPosY = 0;
		boolean second = false;
		for (ChangeInfo info : changeInfos){
			if (info.major) {
				posY = nextPosY;
				second = false;
				info.setRect(0, posY, panel.innerWidth(), 0);
				content.add(info);
				posY = nextPosY = info.bottom();
			} else {
				if (!second){
					second = true;
					info.setRect(0, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = info.bottom();
				} else {
					second = false;
					info.setRect(panel.innerWidth()/2f, posY, panel.innerWidth()/2f, 0);
					content.add(info);
					nextPosY = Math.max(info.bottom(), nextPosY);
					posY = nextPosY;
				}
			}
		}

		content.setSize( panel.innerWidth(), (int)Math.ceil(Math.max(posY, nextPosY)) );

		list.setRect(
				panel.x + panel.marginLeft(),
				panel.y + panel.marginTop() - 1,
				panel.innerWidth() + 2,
				panel.innerHeight() + 2);
		list.scrollTo(0, 0);

		if (changeCategory == SHATTERED_CHANGES){
		StyledButton btn3_X = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "3.X", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 0) {
					changesSelected = 0;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 0) btn3_X.textColor( 0xBBBBBB );
		btn3_X.setRect(list.left()-4f, list.bottom(), 19, changesSelected == 0 ? 19 : 15);
		addToBack(btn3_X);

		StyledButton btn2_X = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "2.X", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 1) {
					changesSelected = 1;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 1) btn2_X.textColor( 0xBBBBBB );
		btn2_X.setRect(btn3_X.right()-2, list.bottom(), 19, changesSelected == 1 ? 19 : 15);
		addToBack(btn2_X);

		StyledButton btn1_X = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "1.X", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 2) {
					changesSelected = 2;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 2) btn1_X.textColor( 0xBBBBBB );
		btn1_X.setRect(btn2_X.right()-2, list.bottom(), 19, changesSelected == 2 ? 19 : 15);
		addToBack(btn1_X);

		StyledButton btn0_9 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.9", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 3) {
					changesSelected = 3;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 3) btn0_9.textColor( 0xBBBBBB );
		btn0_9.setRect(btn1_X.right()-2, list.bottom(), 19, changesSelected == 3 ? 19 : 15);
		addToBack(btn0_9);

		StyledButton btn0_8 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.8", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 4) {
					changesSelected = 4;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 4) btn0_8.textColor( 0xBBBBBB );
		btn0_8.setRect(btn0_9.right()-2, list.bottom(), 19, changesSelected == 4 ? 19 : 15);
		addToBack(btn0_8);
		
		StyledButton btn0_7 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.7", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 5) {
					changesSelected = 5;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 5) btn0_7.textColor( 0xBBBBBB );
		btn0_7.setRect(btn0_8.right()-2, btn0_8.top(), 19, changesSelected == 5 ? 19 : 15);
		addToBack(btn0_7);
		
		StyledButton btn0_6 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.6", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 6) {
					changesSelected = 6;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 6) btn0_6.textColor( 0xBBBBBB );
		btn0_6.setRect(btn0_7.right()-2, btn0_8.top(), 19, changesSelected == 6 ? 19 : 15);
		addToBack(btn0_6);
		
		StyledButton btnOld = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.5-", 8){
			@Override
			protected void onClick() {
				super.onClick();
				if (changesSelected != 7) {
					changesSelected = 7;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		if (changesSelected != 7) btnOld.textColor( 0xBBBBBB );
		btnOld.setRect(btn0_6.right()-2, btn0_8.top(), 22, changesSelected == 7 ? 19 : 15);
		addToBack(btnOld);
		} else {
			StyledButton btn1_0 = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "0.1", 8);
			btn1_0.setRect(list.left()-4f, list.bottom(), 19, 19);
			addToBack(btn1_0);
		}

		addToBack( BG );

		fadeIn();
	}

	private void addCategoryTabs(float x, float y, float width, float height){
		float half = width / 2f;
		addCategoryTab("Shattered Pixel Dungeon", SHATTERED_CHANGES, x, y, half + 1, height);
		addCategoryTab("Reclaimed Pixel Dungeon", RECLAIMED_CHANGES, x + half - 1, y, half + 1, height);
	}

	private void addCategoryTab(String label, final int category, float x, float y, float width, float height){
		StyledButton tab = new StyledButton(Chrome.Type.GREY_BUTTON_TR, label, 5){
			@Override
			protected void onClick() {
				super.onClick();
				if (changeCategory != category) {
					changeCategory = category;
					ShatteredPixelDungeon.seamlessResetScene();
				}
			}
		};
		tab.multiline = true;
		if (changeCategory != category) tab.textColor( 0xBBBBBB );
		tab.setRect(x, y + (changeCategory == category ? 0 : 3), width, changeCategory == category ? height + 2 : height - 1);
		addToBack(tab);
	}

	private void updateChangesText(Image icon, String title, String... messages){
		if (changeTitle != null){
			changeTitle.icon(icon);
			changeTitle.label(title);
			changeTitle.setPos(changeTitle.left(), changeTitle.top());

			String message = "";
			for (int i = 0; i < messages.length; i++){
				message += messages[i];
				if (i != messages.length-1){
					message += "\n\n";
				}
			}
			changeBody.text(message);
			rightScroll.content().setSize(rightScroll.width(), changeBody.bottom()+2);
			rightScroll.setSize(rightScroll.width(), rightScroll.height());
			rightScroll.scrollTo(0, 0);

		} else {
			if (messages.length == 1) {
				addToFront(new WndChanges(icon, title, messages[0]));
			} else {
				addToFront(new WndChangesTabbed(icon, title, messages));
			}
		}
	}

	public static void showChangeInfo(Image icon, String title, String... messages){
		Scene s = ShatteredPixelDungeon.scene();
		if (s instanceof ChangesScene){
			((ChangesScene) s).updateChangesText(icon, title, messages);
			return;
		}
		if (messages.length == 1) {
			s.addToFront(new WndChanges(icon, title, messages[0]));
		} else {
			s.addToFront(new WndChangesTabbed(icon, title, messages));
		}
	}
	
	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade(TitleScene.class);
	}

}
