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

package com.erebus.reclaimedpixeldungeon.items.rings;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfSharpshooting extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_SHARPSHOOT;
		buffClass = Aim.class;
	}

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					soloBuffedBonus(), Messages.decimalFormat("#.##", 100f * (Math.pow(1.2, soloBonus()) - 1f)));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						combinedBuffedBonus(Dungeon.hero), Messages.decimalFormat("#.##", 100f * (Math.pow(1.2, combinedBonus(Dungeon.hero)) - 1f)));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", 1, Messages.decimalFormat("#.##", 20f));
		}
	}

	@Override
	public String upgradeStat1(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Integer.toString(level+1);
	}

	@Override
	public String upgradeStat2(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (Math.pow(1.2, level+1)-1f)) + "%";
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Aim();
	}
	
	public static int levelDamageBonus( Char target ){
		int bonus = getBuffedBonus(target, RingOfSharpshooting.Aim.class);
		if (target instanceof Hero && Dungeon.homebase != null) {
			bonus += Dungeon.homebase.trainingBonus( HomebaseState.Training.RANGED_DAMAGE );
		}
		return bonus;
	}
	
	public static float durabilityMultiplier( Char target ){
		float multiplier = (float)(Math.pow(1.2, getBonus(target, Aim.class)));
		if (target instanceof Hero && Dungeon.homebase != null) {
			multiplier *= 1f + Dungeon.homebase.trainingBonus( HomebaseState.Training.THROWN_DURABILITY ) / 100f;
		}
		return multiplier;
	}

	public class Aim extends RingBuff {
	}
}
