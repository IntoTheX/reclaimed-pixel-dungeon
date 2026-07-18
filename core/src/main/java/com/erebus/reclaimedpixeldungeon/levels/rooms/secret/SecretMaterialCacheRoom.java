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

package com.erebus.reclaimedpixeldungeon.levels.rooms.secret;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class SecretMaterialCacheRoom extends SecretRoom {

	@Override
	public int maxWidth() {
		return 7;
	}

	@Override
	public int maxHeight() {
		return 7;
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		int bonus = Dungeon.homebase == null ? 0 : Dungeon.homebase.trainingBonus( HomebaseState.Training.MATERIAL_CACHE_SIZE );
		float multiplier = 1f + bonus / 100f;
		int min = Math.max( 1, Math.round( (4 + Dungeon.depth / 4f) * multiplier ) );
		int max = Math.max( min, Math.round( (8 + Dungeon.depth / 3f) * multiplier ) );

		for (int x = left + 1; x < right; x++) {
			for (int y = top + 1; y < bottom; y++) {
				level.drop( BuildingMaterial.randomBundleForDepth( Dungeon.depth, min, max ), x + y * level.width() );
			}
		}

		entrance().set( Door.Type.HIDDEN );
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceItem(Point p, Level l) {
		return false;
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return false;
	}
}
