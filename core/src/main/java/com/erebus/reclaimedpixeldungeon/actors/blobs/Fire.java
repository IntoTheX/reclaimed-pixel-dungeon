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

package com.erebus.reclaimedpixeldungeon.actors.blobs;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.effects.BlobEmitter;
import com.erebus.reclaimedpixeldungeon.effects.particles.FlameParticle;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.plants.Plant;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;

public class Fire extends Blob {

	@Override
	protected void evolve() {

		boolean[] flamable = Dungeon.level.flamable;
		int cell;
		int fire;
		
		Freezing freeze = (Freezing)Dungeon.level.blobs.get( Freezing.class );

		boolean observe = false;

		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j*Dungeon.level.width();
				if (cur[cell] > 0) {
					
					if (freeze != null && freeze.volume > 0 && freeze.cur[cell] > 0){
						freeze.clear(cell);
						off[cell] = cur[cell] = 0;
						continue;
					}

					burn( cell );

					fire = cur[cell] - 1;
					if (fire <= 0 && flamable[cell]) {

						Dungeon.level.destroy( cell );

						observe = true;
						GameScene.updateMap( cell );

					}

				} else if (freeze == null || freeze.volume <= 0 || freeze.cur[cell] <= 0) {

					if (flamable[cell]
							&& (cur[cell-1] > 0
							|| cur[cell+1] > 0
							|| cur[cell-Dungeon.level.width()] > 0
							|| cur[cell+Dungeon.level.width()] > 0)) {
						fire = 4;
						burn( cell );
						area.union(i, j);
					} else {
						fire = 0;
					}

				} else {
					fire = 0;
				}

				volume += (off[cell] = fire);
			}
		}

		if (observe) {
			Dungeon.observe();
		}
	}
	
	public static void burn( int pos ) {
		Char ch = Actor.findChar( pos );
		if (ch != null && !ch.isImmune(Fire.class)) {
			float resistedDuration = Burning.duration() * ch.resist( Burning.class );
			if (resistedDuration > 0f) {
				Buff.affect( ch, Burning.class ).reignite( ch, resistedDuration );
			} else {
				Buff.showResisted( ch );
				Buff.detach( ch, Burning.class );
			}
		}
		
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null) {
			heap.burn();
		}

		Plant plant = Dungeon.level.plants.get( pos );
		if (plant != null){
			plant.wither();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( FlameParticle.FACTORY, 0.03f );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
