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

package com.erebus.reclaimedpixeldungeon.items.spells;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

public class PhaseShift extends TargetedSpell {
	
	{
		image = ItemSpriteSheet.PHASE_SHIFT;

		usesTargeting = true;

		talentChance = 1/(float)Recipe.OUT_QUANTITY;
	}
	
	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
		final Char ch = Actor.findChar(bolt.collisionPos);
		
		if (ch != null) {
			if (ScrollOfTeleportation.teleportChar(ch)){

				if (ch instanceof Mob) {
					if (((Mob) ch).state == ((Mob) ch).HUNTING) ((Mob) ch).state = ((Mob) ch).WANDERING;
					((Mob) ch).beckon(Dungeon.level.randomDestination( ch ));
				}
				if (!Char.hasProp(ch, Char.Property.BOSS) && !Char.hasProp(ch, Char.Property.MINIBOSS)) {
					Buff.affect(ch, Paralysis.class, Paralysis.DURATION);
				}
				
			}
		} else {
			GLog.w( Messages.get(this, "no_target") );
		}
		onSpellused();
	}
	
	@Override
	public int value() {
		return (int)(60 * (quantity/(float)Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(12 * (quantity/(float)Recipe.OUT_QUANTITY));
	}
	
	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 6;
		
		{
			inputs =  new Class[]{ScrollOfTeleportation.class};
			inQuantity = new int[]{1};
			
			cost = 10;
			
			output = PhaseShift.class;
			outQuantity = OUT_QUANTITY;
		}
		
	}
	
}
