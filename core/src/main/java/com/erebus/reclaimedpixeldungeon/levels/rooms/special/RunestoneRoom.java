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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.keys.IronKey;
import com.erebus.reclaimedpixeldungeon.items.stones.Runestone;
import com.erebus.reclaimedpixeldungeon.items.trinkets.TrinketCatalyst;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class RunestoneRoom extends SpecialRoom {
	
	@Override
	public int minWidth() { return 6; }
	
	@Override
	public int minHeight() {
		return 6;
	}
	
	@Override
	public void paint( Level level) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.CHASM );
		
		Painter.drawInside( level, this, entrance(), 2, Terrain.EMPTY_SP);
		Painter.fill( level, this, 2, Terrain.EMPTY );
		
		int n = Random.NormalIntRange(2, 3);
		int dropPos;
		for (int i = 0; i < n; i++) {
			do {
				dropPos = level.pointToCell(random());
			} while (level.map[dropPos] != Terrain.EMPTY || level.heaps.get( dropPos ) != null);
			level.drop(prize(level), dropPos);
		}
		
		entrance().set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey( Dungeon.depth ) );
	}
	
	private static Item prize( Level level ) {

		Item prize = level.findPrizeItem( TrinketCatalyst.class );
		if (prize == null){
			prize = level.findPrizeItem( Runestone.class );
			if (prize == null) {
				prize = Generator.random( Generator.Category.STONE );
			}
		}
		
		return prize;
	}
}
