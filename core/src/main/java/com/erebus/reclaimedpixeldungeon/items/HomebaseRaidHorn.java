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

package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BossHealthBar;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class HomebaseRaidHorn extends Item {

	public static final String AC_USE = "USE";

	{
		image = ItemSpriteSheet.ARTIFACT_HORN1;
		unique = true;
		keptThoughLostInvent = true;
		defaultAction = AC_USE;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (!AC_USE.equals( action )) return;

		if (!HomebaseState.homebaseNpcTestItemsEnabled()) {
			GLog.w( "Homebase NPC test items are disabled." );
			return;
		}
		if (!(Dungeon.level instanceof HomebaseLevel) || Dungeon.depth != 0 || Dungeon.homebase == null) {
			GLog.w( "The raid horn can only be used at the homebase." );
			return;
		}
		if (!Dungeon.homebase.forceRaidForTesting()) {
			GLog.w( "A homebase raid is already active." );
			return;
		}

		GLog.w( Dungeon.homebase.raidPopupText() );
		((HomebaseLevel)Dungeon.level).spawnRaidWave();
		BossHealthBar.refreshRaid();
	}
}
