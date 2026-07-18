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

package com.erebus.reclaimedpixeldungeon.items.potions.elixirs;

import com.erebus.reclaimedpixeldungeon.actors.buffs.ArcaneArmor;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.PotionOfEarthenArmor;
import com.erebus.reclaimedpixeldungeon.items.quest.GooBlob;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class ElixirOfArcaneArmor extends Elixir {
	
	{
		image = ItemSpriteSheet.ELIXIR_ARCANE;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, ArcaneArmor.class).set(5 + hero.lvl/2, 80);
	}
	
	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfEarthenArmor.class, GooBlob.class};
			inQuantity = new int[]{1, 1};
			
			cost = 8;
			
			output = ElixirOfArcaneArmor.class;
			outQuantity = 1;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			Catalog.countUse(GooBlob.class);
			return super.brew(ingredients);
		}
	}
}
