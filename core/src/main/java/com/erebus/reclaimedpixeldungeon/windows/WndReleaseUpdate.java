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
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.ChangesScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.services.updates.AvailableUpdateData;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class WndReleaseUpdate extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int MARGIN = 2;
	private static final int PRIMARY_HEIGHT = 22;
	private static final int BUTTON_HEIGHT = 18;

	public WndReleaseUpdate(Image icon, String title, String message, AvailableUpdateData update) {
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		float pos = 0;

		IconTitle titleBlock = new IconTitle(icon, title);
		titleBlock.setRect(0, pos, width, 0);
		add(titleBlock);
		pos = titleBlock.bottom() + 2 * MARGIN;

		RenderedTextBlock body = PixelScene.renderTextBlock(6);
		body.text(message, width);
		body.setPos(0, pos);
		add(body);
		pos = body.bottom() + 2 * MARGIN;

		if (hasUrl(update.googlePlayURL)) {
			RedButton googlePlay = linkButton(Messages.get(this, "google_play"), update.googlePlayURL, 8);
			googlePlay.setRect(0, pos, width, PRIMARY_HEIGHT);
			add(googlePlay);
			pos += PRIMARY_HEIGHT + MARGIN;
		} else if (hasUrl(update.URL)) {
			RedButton download = linkButton(Messages.get(this, "download"), update.URL, 8);
			download.setRect(0, pos, width, PRIMARY_HEIGHT);
			add(download);
			pos += PRIMARY_HEIGHT + MARGIN;
		}

		boolean hasGithub = hasUrl(update.githubReleasesURL);
		boolean hasDrive = hasUrl(update.googleDriveURL);
		if (hasGithub || hasDrive) {
			float buttonWidth = hasGithub && hasDrive ? (width - MARGIN) / 2f : width;
			if (hasGithub) {
				RedButton github = linkButton(Messages.get(this, "github"), update.githubReleasesURL, 7);
				github.multiline = true;
				github.setRect(0, pos, buttonWidth, BUTTON_HEIGHT);
				add(github);
			}
			if (hasDrive) {
				RedButton drive = linkButton(Messages.get(this, "google_drive"), update.googleDriveURL, 7);
				drive.multiline = true;
				drive.setRect(hasGithub ? buttonWidth + MARGIN : 0, pos, buttonWidth, BUTTON_HEIGHT);
				add(drive);
			}
			pos += BUTTON_HEIGHT + MARGIN;
		}

		RedButton changes = new RedButton(Messages.get(this, "changes"), 7) {
			@Override
			protected void onClick() {
				hide();
				ChangesScene.changesSelected = 0;
				ShatteredPixelDungeon.switchNoFade(ChangesScene.class);
			}
		};
		changes.setRect(0, pos, width, BUTTON_HEIGHT);
		add(changes);
		pos += BUTTON_HEIGHT;

		resize(width, (int) pos);
	}

	private RedButton linkButton(String label, final String url, int fontSize) {
		return new RedButton(label, fontSize) {
			@Override
			protected void onClick() {
				hide();
				Game.platform.openURI(url);
			}
		};
	}

	private static boolean hasUrl(String url) {
		return url != null && !url.trim().isEmpty();
	}
}
