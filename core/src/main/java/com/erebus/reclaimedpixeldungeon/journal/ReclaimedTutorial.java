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

package com.erebus.reclaimedpixeldungeon.journal;

import com.erebus.reclaimedpixeldungeon.scenes.GameScene;

import java.util.ArrayList;

public class ReclaimedTutorial {

	private static final ArrayList<String> pendingPages = new ArrayList<>();

	public static boolean flash( String page ) {
		if (page == null || Document.ADVENTURERS_GUIDE.isPageFound( page )) {
			return false;
		}
		if (!GameScene.canFlashForDocument()) {
			queue( page );
			return false;
		}
		return flashNow( page );
	}

	public static void queue( String page ) {
		if (page != null
				&& !Document.ADVENTURERS_GUIDE.isPageFound( page )
				&& !pendingPages.contains( page )) {
			pendingPages.add( page );
		}
	}

	public static boolean flashPending() {
		if (!GameScene.canFlashForDocument()) return false;
		while (!pendingPages.isEmpty()) {
			String page = pendingPages.remove( 0 );
			if (page != null && !Document.ADVENTURERS_GUIDE.isPageFound( page )) {
				return flashNow( page );
			}
		}
		return false;
	}

	private static boolean flashNow( String page ) {
		Document.ADVENTURERS_GUIDE.findPage( page );
		GameScene.flashForDocument( Document.ADVENTURERS_GUIDE, page );
		return true;
	}
}
