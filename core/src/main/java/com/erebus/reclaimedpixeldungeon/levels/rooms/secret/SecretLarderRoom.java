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
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hunger;
import com.erebus.reclaimedpixeldungeon.items.food.ChargrilledMeat;
import com.erebus.reclaimedpixeldungeon.items.food.Food;
import com.erebus.reclaimedpixeldungeon.items.food.Pasty;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.plants.BlandfruitBush;
import com.watabou.utils.Point;

public class SecretLarderRoom extends SecretRoom {
	
	@Override
	public int minHeight() {
		return 6;
	}
	
	@Override
	public int minWidth() {
		return 6;
	}
	
	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY_SP);
		
		Point c = center();
		
		Painter.fill(level, c.x-1, c.y-1, 3, 3, Terrain.WATER);
		Painter.set(level, c, Terrain.GRASS);
		
		level.plant(new BlandfruitBush.Seed(), level.pointToCell(c));
		
		int extraFood = (int)(Hunger.STARVING - Hunger.HUNGRY) * (1 + Dungeon.depth / 5);
		
		while (extraFood > 0){
			Food food;
			if (extraFood >= Hunger.STARVING){
				food = new Pasty();
				extraFood -= Hunger.STARVING;
			} else {
				food = new ChargrilledMeat();
				extraFood -= (Hunger.STARVING - Hunger.HUNGRY);
			}
			int foodPos;
			do {
				foodPos = level.pointToCell(random());
			} while (level.map[foodPos] != Terrain.EMPTY_SP || level.heaps.get(foodPos) != null);
			level.drop(food, foodPos);
		}
		
		entrance().set(Door.Type.HIDDEN);
	}
	
	
}
