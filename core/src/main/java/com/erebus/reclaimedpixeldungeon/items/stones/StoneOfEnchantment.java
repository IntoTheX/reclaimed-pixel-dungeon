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

package com.erebus.reclaimedpixeldungeon.items.stones;

import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.effects.Enchanting;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.EnchantmentSlots;
import com.erebus.reclaimedpixeldungeon.items.Recipe;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class StoneOfEnchantment extends InventoryStone {
	
	{
		preferredBag = Belongings.Backpack.class;
		image = ItemSpriteSheet.STONE_ENCHANT;

		unique = true;
	}

	@Override
	public String name() {
		return super.name() + " " + EnchantmentSlots.roman( level() );
	}

	@Override
	public int visiblyUpgraded() {
		return 0;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return 0;
	}

	@Override
	public String inventoryLevelText() {
		return EnchantmentSlots.roman( level() );
	}

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar( item ) && item.level() == level();
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc", EnchantmentSlots.roman(level()) ) + Messages.get( this, "merge_desc" );
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return ScrollOfEnchantment.enchantable(item);
	}
	
	@Override
	protected void onItemSelected(Item item) {
		if (!anonymous) {
			curItem.detach(curUser.belongings.backpack);
			Catalog.countUse(getClass());
			Talent.onRunestoneUsed(curUser, curUser.pos, getClass());
		}
		
		if (item instanceof Weapon) {
			((Weapon)item).enchantRandom( EnchantmentSlots.slotForLevel(level()) );
			
		} else {
			
			((Armor)item).inscribeRandom( EnchantmentSlots.slotForLevel(level()) );
			
		}
		
		curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
		Enchanting.show( curUser, item );
		
		if (item instanceof Weapon) {
			GLog.p(Messages.get(this, "weapon"));
		} else {
			GLog.p(Messages.get(this, "armor"));
		}
		
		useAnimation();
		
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 5 * quantity;
	}

	public static class MergeRecipe extends Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.size() != 2) return false;
			return ingredients.get(0) instanceof StoneOfEnchantment
					&& ingredients.get(1) instanceof StoneOfEnchantment
					&& ingredients.get(0).level() == ingredients.get(1).level()
					&& EnchantmentSlots.mergeChance( ingredients.get(0).level() ) > 0;
		}

		@Override
		public int cost( ArrayList<Item> ingredients ) {
			return 5 + ingredients.get(0).level() * 3;
		}

		@Override
		public Item brew( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			int level = ingredients.get(0).level();
			for (Item ingredient : ingredients) ingredient.quantity( ingredient.quantity() - 1 );
			StoneOfEnchantment result = new StoneOfEnchantment();
			result.level( Random.Int(100) < EnchantmentSlots.mergeChance(level) ? level + 1 : level );
			result.identify( false );
			return result;
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			StoneOfEnchantment result = new StoneOfEnchantment();
			result.level( ingredients.get(0).level() + 1 );
			result.identify( false );
			return result;
		}
	}

}
