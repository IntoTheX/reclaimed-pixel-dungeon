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

package com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.features.LevelTransition;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.levels.rooms.Room;
import com.erebus.reclaimedpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

public class VaultEntranceRoom extends StandardRoom {

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 1, 0};
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL);
		Painter.fill( level, this, 1, Terrain.EMPTY );

		for (Room.Door door : connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}

		int entrance;
		do {
			entrance = level.pointToCell(random(2));
		} while (level.findMob(entrance) != null);

		level.transitions.add(new LevelTransition(level,
				entrance,
				LevelTransition.Type.BRANCH_ENTRANCE,
				Dungeon.depth,
				0,
				LevelTransition.Type.BRANCH_EXIT));
	}

	@Override
	public boolean isEntrance() {
		return true;
	}

	@Override
	public int maxConnections(int direction) {
		//max of two connections
		if (direction == ALL) return 2;
		return super.maxConnections(direction);
	}

	@Override
	public boolean canMerge(Level l, Room other, Point p, int mergeTerrain) {
		return false;
	}
}
