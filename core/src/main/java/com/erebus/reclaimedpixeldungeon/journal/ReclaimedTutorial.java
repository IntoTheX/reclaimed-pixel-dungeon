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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReclaimedTutorial {

	private static final ArrayList<String> pendingPages = new ArrayList<>();
	private static final Set<String> TRIGGERED_PAGES = new HashSet<>( Arrays.asList(
			Document.GUIDE_HOMEBASE,
			Document.GUIDE_MATERIALS,
			Document.GUIDE_RETURNING,
			Document.GUIDE_REBUILDING,
			Document.GUIDE_FOUNDERS_CAMP,
			Document.GUIDE_QUARTERMASTER_VAULT,
			Document.GUIDE_EMBERFORGE,
			Document.GUIDE_ALCHEMISTS_STILL,
			Document.GUIDE_MOONROOT_GARDEN,
			Document.GUIDE_DEFENSE_WALLS,
			Document.GUIDE_DEFENSE_TOWERS,
			Document.GUIDE_RARITY_STATS,
			Document.GUIDE_CATALYSTS,
			Document.GUIDE_MOB_STATS,
			Document.GUIDE_RAIDS,
			Document.GUIDE_DUNGEON_PRESSURE,
			Document.GUIDE_DEFENDERS,
			Document.GUIDE_BAGS_STORAGE,
			Document.GUIDE_FORGE_STILL,
			Document.GUIDE_WAYFARER_NETWORK,
			Document.GUIDE_UPGRADE_LIMITS
	) );

	public static boolean isTriggeredPage( String page ) {
		return TRIGGERED_PAGES.contains( page );
	}

	public static boolean checkWayfarerNetworkGuide() {
		if (Dungeon.homebase == null
				|| Document.ADVENTURERS_GUIDE.isPageFound( Document.GUIDE_WAYFARER_NETWORK )) {
			return false;
		}
		if (Dungeon.homebase.wayfarerExchangeUnlocked()) {
			return flash( Document.GUIDE_WAYFARER_NETWORK );
		}
		return Dungeon.homebase.canUnlockWayfarerExchange()
				&& flash( Document.GUIDE_WAYFARER_NETWORK );
	}

	public static boolean checkUpgradeLimitGuide() {
		if (Dungeon.hero == null || Dungeon.hero.belongings == null
				|| Document.ADVENTURERS_GUIDE.isPageFound( Document.GUIDE_UPGRADE_LIMITS )) {
			return false;
		}
		for (Item item : Dungeon.hero.belongings) {
			if (item instanceof Weapon && item.upgradeLimitReached()) {
				return flash( Document.GUIDE_UPGRADE_LIMITS );
			}
		}
		return false;
	}

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
