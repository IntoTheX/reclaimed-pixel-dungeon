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

package com.erebus.reclaimedpixeldungeon.levels.traps;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.Beam;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class DisintegrationTrap extends Trap {

	{
		color = VIOLET;
		shape = CROSSHAIR;
		
		canBeHidden = false;
		avoidsHallways = true;
	}

	@Override
	public void activate() {
		Char target = Actor.findChar(pos);

		//find the closest char that can be aimed at
		//can't target beyond view distance, with a min of 6 (torch range)
		//add 0.5 for better consistency with vision radius shape
		float range = Math.max(6, Dungeon.level.viewDistance)+0.5f;
		if (target == null){
			float closestDist = Float.MAX_VALUE;
			for (Char ch : Actor.chars()){
				if (!ch.isAlive()) continue;
				float curDist = Dungeon.level.trueDistance(pos, ch.pos);
				//invis targets are considered to be at max range
				if (ch.invisible > 0) curDist = Math.max(curDist, range);
				Ballistica bolt = new Ballistica(pos, ch.pos, Ballistica.PROJECTILE);
				if (bolt.collisionPos == ch.pos
						&& ( curDist < closestDist || (curDist == closestDist && target instanceof Hero))){
					target = ch;
					closestDist = curDist;
				}
			}
			if (closestDist > range){
				target = null;
			}
		}
		
		Heap heap = Dungeon.level.heaps.get(pos);
		if (heap != null) heap.explode();
		
		if (target != null) {
			if (target instanceof Mob){
				Buff.prolong(target, Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
			}
			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[target.pos]) {
				Sample.INSTANCE.play(Assets.Sounds.RAY);
				ShatteredPixelDungeon.scene().add(new Beam.DeathRay(DungeonTilemap.tileCenterToWorld(pos), target.sprite.center()));
				Sample.INSTANCE.play( Assets.Sounds.RAY );
			}
			target.damage( Random.NormalIntRange(30, 50) + damageScalingDepth(), this );
			if (target == Dungeon.hero){
				Hero hero = (Hero)target;
				if (!hero.isAlive()){
					Badges.validateDeathFromGrimOrDisintTrap();
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
					if (reclaimed) Badges.validateDeathFromFriendlyMagic();
				}
			}
		}

	}
}
