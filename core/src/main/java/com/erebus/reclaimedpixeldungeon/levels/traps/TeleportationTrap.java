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
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.CellEmitter;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Honeypot;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class TeleportationTrap extends Trap {

	{
		color = TEAL;
		shape = DOTS;
	}

	@Override
	public void activate() {

		for (int i : PathFinder.NEIGHBOURS9){
			Char ch = Actor.findChar(pos + i);
			if (ch != null){
				if (ScrollOfTeleportation.teleportChar(ch)) {
					if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).HUNTING) {
						((Mob) ch).state = ((Mob) ch).WANDERING;
						Buff.prolong(ch, Trap.HazardAssistTracker.class, HazardAssistTracker.DURATION);
					}
				}
			}
			Heap heap = Dungeon.level.heaps.get(pos + i);
			if (heap != null && heap.type == Heap.Type.HEAP){
				int cell = Dungeon.level.randomRespawnCell( null );

				Item item = heap.pickUp();

				if (cell != -1) {
					Dungeon.level.drop( item, cell );
					if (item instanceof Honeypot.ShatteredPot){
						((Honeypot.ShatteredPot)item).movePot(pos, cell);
					}
					Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
					CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 4);
				}
			}
		}

	}
}
