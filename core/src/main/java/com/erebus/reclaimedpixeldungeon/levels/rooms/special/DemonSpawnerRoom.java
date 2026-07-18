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

package com.erebus.reclaimedpixeldungeon.levels.rooms.special;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DemonSpawner;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.levels.rooms.Room;
import com.erebus.reclaimedpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

public class DemonSpawnerRoom extends SpecialRoom {
	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Point c = center();
		int cx = c.x;
		int cy = c.y;

		Door door = entrance();
		door.set(Door.Type.UNLOCKED); //cannot be hidden randomly under any circumstance

		DemonSpawner spawner = new DemonSpawner();
		spawner.pos = cx + cy * level.width();
		Statistics.spawnersAlive++;
		spawner.spawnRecorded = true;
		level.mobs.add( spawner );

		CustomFloor vis = new CustomFloor();
		vis.setRect(left+1, top+1, width()-2, height()-2);
		level.customTiles.add(vis);

	}

	@Override
	public boolean connect(Room room) {
		//cannot connect to entrance, otherwise works normally
		if (room.isExit())  return false;
		else                return super.connect(room);
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	public static class CustomFloor extends CustomTilemap {

		{
			texture = Assets.Environment.HALLS_SP;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int cell = tileX + tileY * Dungeon.level.width();
			int[] map = Dungeon.level.map;
			int[] data = new int[tileW*tileH];
			for (int i = 0; i < data.length; i++){
				if (i % tileW == 0){
					cell = tileX + (tileY + i / tileW) * Dungeon.level.width();
				}

				if (Dungeon.level.findMob(cell) instanceof DemonSpawner){
					data[i-1] = 5 + 4*8;
					data[i] = 6 + 4*8;
					data[i+1] = 7 + 4*8;
					i++;
					cell++;
				} else if (map[cell] == Terrain.EMPTY_DECO) {
					if (Statistics.amuletObtained){
						data[i] = 31;
					} else {
						data[i] = 27;
					}
				} else {
					data[i] = 19;
				}

				cell++;
			}
			v.map( data, tileW );
			return v;
		}

	}
}
