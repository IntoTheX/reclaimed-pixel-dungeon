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

package com.erebus.reclaimedpixeldungeon.levels.features;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

public class Door {

	public static void enter( int pos ) {
		Level.set( pos, Terrain.OPEN_DOOR );
		GameScene.updateMap( pos );

		if (Dungeon.level.heroFOV[pos]) {
			Dungeon.observe();
			Sample.INSTANCE.play( Assets.Sounds.OPEN );
		}
	}

	public static void leave( int pos ) {
		int chars = 0;
		
		for (Char ch : Actor.chars()){
			if (ch.pos == pos) chars++;
		}
		
		//door does not shut if anything else is also on it
		if (Dungeon.level.heaps.get( pos ) == null && chars <= 1) {
			Level.set( pos, Terrain.DOOR );
			GameScene.updateMap( pos );
			if (Dungeon.level.heroFOV[pos])
				Dungeon.observe();
		}
	}
}
