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

package com.erebus.reclaimedpixeldungeon.items.wands;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.buffs.WandEmpower;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

//for wands that directly damage a target
//wands with AOE or circumstantial direct damage count here (e.g. fireblast, transfusion), but wands with indirect damage do not (e.g. corrosion)
public abstract class DamageWand extends Wand{

	public int min(){
		return min(buffedLvl());
	}

	public abstract int min(int lvl);

	public int max(){
		return max(buffedLvl());
	}

	public abstract int max(int lvl);

	public int damageRoll(){
		return damageRoll(buffedLvl());
	}

	public int damageRoll(int lvl){
		int dmg = Hero.heroDamageIntRange(min(lvl), max(lvl)) + rarityStat( RarityStat.Type.MAGIC_DAMAGE );
		if (Dungeon.homebase != null) {
			dmg += Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_DAMAGE );
		}
		dmg = Math.round( dmg * (1f + rarityStat( RarityStat.Type.MAGIC_BONUS ) / 100f) );
		int critChance = rarityStat( RarityStat.Type.CRITICAL_CHANCE );
		int critDamage = rarityStat( RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER );
		if (Dungeon.homebase != null) {
			critChance += Dungeon.homebase.trainingBonus( HomebaseState.Training.CRITICAL_CHANCE );
			critDamage += Dungeon.homebase.trainingBonus( HomebaseState.Training.CRITICAL_DAMAGE );
		}
		if (Random.Int( 100 ) < critChance) {
			dmg = Math.round( dmg * (2f + critDamage / 100f) );
		}
		if (Dungeon.homebase != null) {
			int directWandBonus = Dungeon.homebase.trainingBonus( HomebaseState.Training.WAND_DAMAGE )
					+ Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_POWER );
			dmg = Math.round( dmg * (1f + directWandBonus / 100f) );
		}
		WandEmpower emp = Dungeon.hero.buff(WandEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}
		return dmg;
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", min(), max());
		else
			return Messages.get(this, "stats_desc", min(0), max(0));
	}

	@Override
	public String upgradeStat1(int level) {
		return min(level) + "-" + max(level);
	}
}
