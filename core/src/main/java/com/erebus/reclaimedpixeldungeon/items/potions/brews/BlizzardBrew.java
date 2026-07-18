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

package com.erebus.reclaimedpixeldungeon.items.potions.brews;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Blizzard;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Blob;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfFrost;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class BlizzardBrew extends Brew {
	
	{
		image = ItemSpriteSheet.BREW_BLIZZARD;
	}
	
	@Override
	public void shatter(int cell) {
		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			Sample.INSTANCE.play( Assets.Sounds.GAS );
		}

		int centerVolume = 120;
		for (int i : PathFinder.NEIGHBOURS8){
			if (!Dungeon.level.solid[cell+i]){
				GameScene.add( Blob.seed( cell+i, 120, Blizzard.class ) );
			} else {
				centerVolume += 120;
			}
		}

		GameScene.add( Blob.seed( cell, centerVolume, Blizzard.class ) );
	}

	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfFrost.class};
			inQuantity = new int[]{1};
			
			cost = 8;
			
			output = BlizzardBrew.class;
			outQuantity = 1;
		}
		
	}
}
