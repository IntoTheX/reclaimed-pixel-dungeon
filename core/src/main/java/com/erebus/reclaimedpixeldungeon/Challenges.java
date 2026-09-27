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

package com.erebus.reclaimedpixeldungeon;

import com.erebus.reclaimedpixeldungeon.items.Dewdrop;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;

public class Challenges {

	//Some of these internal IDs are outdated and don't represent what these challenges do
	public static final int NO_FOOD				= 1;
	public static final int NO_ARMOR			= 2;
	public static final int NO_HEALING			= 4;
	public static final int NO_HERBALISM		= 8;
	public static final int SWARM_INTELLIGENCE	= 16;
	public static final int DARKNESS			= 32;
	public static final int NO_SCROLLS		    = 64;
	public static final int CHAMPION_ENEMIES	= 128;
	public static final int STRONGER_BOSSES 	= 256;

	public static final int MAX_VALUE           = 511;
	public static final int MAX_CHALS           = 9;

	public static final String[] NAME_IDS = {
			"champion_enemies",
			"stronger_bosses",
			"no_food",
			"no_armor",
			"no_healing",
			"no_herbalism",
			"swarm_intelligence",
			"darkness",
			"no_scrolls"
	};

	public static final int[] MASKS = {
			CHAMPION_ENEMIES, STRONGER_BOSSES, NO_FOOD, NO_ARMOR, NO_HEALING, NO_HERBALISM, SWARM_INTELLIGENCE, DARKNESS, NO_SCROLLS
	};

	public static int activeChallenges(){
		return activeChallenges(Dungeon.challenges);
	}

	public static int activeChallenges(int mask){
		int chCount = 0;
		for (int ch : Challenges.MASKS){
			if ((mask & ch) != 0) chCount++;
		}
		return chCount;
	}

	public static boolean isItemBlocked( Item item ){

		if (Dungeon.isChallenged(NO_HERBALISM) && item instanceof Dewdrop){
			return true;
		}

		return false;

	}

	public static int adjustRarityStat( RarityStat.Type type, int value ) {
		if (type == null || value == 0) return value;

		if (Dungeon.isChallenged( NO_ARMOR ) && isDefensiveRarityStat( type )) {
			return Math.round( value * 0.10f );
		}
		if (Dungeon.isChallenged( NO_HEALING )
				&& (type == RarityStat.Type.LIFESTEAL || type == RarityStat.Type.SURVIVOR)) {
			return 0;
		}
		if (Dungeon.isChallenged( NO_SCROLLS )
				&& (type == RarityStat.Type.BONUS_LOOT || type == RarityStat.Type.TREASURE_LUCK)) {
			return 0;
		}
		return value;
	}

	private static boolean isDefensiveRarityStat( RarityStat.Type type ) {
		return type == RarityStat.Type.DEFENSE
				|| type == RarityStat.Type.ARMOR_BONUS
				|| type == RarityStat.Type.EVASION
				|| type == RarityStat.Type.DODGE_CHANCE
				|| type == RarityStat.Type.BLOCK_CHANCE
				|| type == RarityStat.Type.BARKSKIN_PROC
				|| type == RarityStat.Type.BARKSKIN_POWER
				|| type == RarityStat.Type.BARRIER_PROC
				|| type == RarityStat.Type.BARRIER_POWER
				|| type == RarityStat.Type.CRITICAL_HIT_RESISTANCE
				|| type == RarityStat.Type.CRITICAL_DAMAGE_REDUCTION
				|| type.name().endsWith( "_RESISTANCE" );
	}

}
