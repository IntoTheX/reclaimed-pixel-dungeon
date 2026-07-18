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
import com.erebus.reclaimedpixeldungeon.actors.blobs.Electricity;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ToxicGas;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Corrosion;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Frost;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Ooze;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Poison;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.AntiMagic;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

import java.util.HashSet;

public class RingOfElements extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_ELEMENTS;
		buffClass = Resistance.class;
	}

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					Messages.decimalFormat("#.##", 100f * (1f - Math.pow(0.825f, soloBuffedBonus()))));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#.##", 100f * (1f - Math.pow(0.825f, combinedBuffedBonus(Dungeon.hero)))));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", Messages.decimalFormat("#.##", 17.5f));
		}
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (1f - Math.pow(0.825f, level+1))) + "%";
	}
	
	@Override
	protected RingBuff buff( ) {
		return new Resistance();
	}

	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( Burning.class );
		RESISTS.add( Chill.class );
		RESISTS.add( Frost.class );
		RESISTS.add( Ooze.class );
		RESISTS.add( Paralysis.class );
		RESISTS.add( Poison.class );
		RESISTS.add( Corrosion.class );

		RESISTS.add( ToxicGas.class );
		RESISTS.add( Electricity.class );

		RESISTS.addAll( AntiMagic.RESISTS );
	}
	
	public static float resist( Char target, Class effect ){
		boolean resisted = false;
		for (Class c : RESISTS){
			if (c.isAssignableFrom(effect)){
				resisted = true;
				break;
			}
		}
		if (!resisted) return 1f;

		float multiplier = 1f;
		int ringBonus = getBuffedBonus(target, Resistance.class);
		if (ringBonus > 0) multiplier *= (float)Math.pow(0.825, ringBonus);
		if (target instanceof Hero && Dungeon.homebase != null) {
			multiplier *= Math.max( 0f, 1f - Dungeon.homebase.trainingBonus( HomebaseState.Training.ELEMENTAL_RESISTANCE ) / 100f );
		}
		
		return multiplier;
	}
	
	public class Resistance extends RingBuff {
	
	}
}
