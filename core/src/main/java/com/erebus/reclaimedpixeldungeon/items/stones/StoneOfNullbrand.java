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

import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class StoneOfNullbrand extends RarityCatalystStone {

	public static final int MAX_SPARK_LEVEL = 5;

	private static final int[][] ASCENSION_CHANCES = new int[][]{
			{90, 70, 45, 25, 10},
			{95, 78, 55, 32, 18},
			{100, 86, 65, 39, 26},
			{100, 94, 75, 46, 34},
			{100, 100, 85, 53, 42},
			{100, 100, 95, 60, 50}
	};

	private static final int[] MERGE_CHANCES = new int[]{100, 90, 75, 60, 50};

	{
		image = ItemSpriteSheet.STONE_NULLBRAND;
	}

	@Override
	public String name() {
		String name = super.name();
		return level() > 0 ? name + " +" + level() : name;
	}

	@Override
	public int visiblyUpgraded() {
		return level();
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return level();
	}

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar( item ) && item.level() == level();
	}

	@Override
	public String desc() {
		return Messages.get( this, "desc" ) + "\n\n" + chanceTable( level() ) + Messages.get( this, "desc2" );
	}

	@Override
	protected boolean usableOnRarityItem( Item item ) {
		return item.canUpgradeRarityTier();
	}

	@Override
	protected void onItemSelected( Item item ) {
		ItemRarity target = item.nextRarityTier();
		int chance = item.rarityTierUpgradeChance( level() );
		String itemName = item.name();
		if (target == null || chance <= 0) {
			fail( Messages.get( this, "ineligible" ) );
		} else {
			Item.RarityTierChange change = item.upgradeRarityTierResult( level() );
			if (change != null) {
				finish( Messages.get( this, "done", itemName, change.newRarity.displayName() ) + " " + rarityTransition( change ) + "." );
			} else {
				consumeFailure( Messages.get( this, "failed", itemName, target.displayName(), chance ) );
			}
		}
	}

	public static int ascensionChance( ItemRarity currentRarity, int sparkLevel ) {
		if (currentRarity == null || currentRarity == ItemRarity.TRANSCENDANT) return 0;
		int current = currentRarity.ordinal();
		if (current < 0 || current >= ItemRarity.TRANSCENDANT.ordinal()) return 0;
		int clampedLevel = Math.max( 0, Math.min( MAX_SPARK_LEVEL, sparkLevel ) );
		return ASCENSION_CHANCES[clampedLevel][current];
	}

	public static int mergeChance( int sparkLevel ) {
		if (sparkLevel < 0 || sparkLevel >= MERGE_CHANCES.length) return 0;
		return MERGE_CHANCES[sparkLevel];
	}

	private static String chanceTable( int sparkLevel ) {
		StringBuilder builder = new StringBuilder();
		for (ItemRarity rarity : ItemRarity.values()) {
			if (rarity == ItemRarity.TRANSCENDANT) break;
			ItemRarity next = ItemRarity.values()[rarity.ordinal() + 1];
			builder.append( rarity.coloredName() )
					.append( " @@CFFFFFF@@->@@CEND@@ " )
					.append( next.coloredName() )
					.append( " @@CFFFF44@@" )
					.append( ascensionChance( rarity, sparkLevel ) )
					.append( "%@@CEND@@" );
			if (next != ItemRarity.TRANSCENDANT) builder.append( "\n" );
		}
		return builder.toString();
	}

	public static class MergeRecipe extends com.erebus.reclaimedpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.size() != 2) return false;
			if (!(ingredients.get( 0 ) instanceof StoneOfNullbrand) || !(ingredients.get( 1 ) instanceof StoneOfNullbrand)) return false;
			return ingredients.get( 0 ).level() == ingredients.get( 1 ).level()
					&& ingredients.get( 0 ).level() >= 0
					&& ingredients.get( 0 ).level() < MAX_SPARK_LEVEL;
		}

		@Override
		public int cost( ArrayList<Item> ingredients ) {
			return 5 + ingredients.get( 0 ).level() * 3;
		}

		@Override
		public Item brew( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			int sparkLevel = ingredients.get( 0 ).level();
			for (Item ingredient : ingredients) {
				ingredient.quantity( ingredient.quantity() - 1 );
			}
			StoneOfNullbrand result = new StoneOfNullbrand();
			result.level( Random.Int( 100 ) < mergeChance( sparkLevel ) ? sparkLevel + 1 : sparkLevel );
			result.identify( false );
			return result;
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			StoneOfNullbrand result = new StoneOfNullbrand();
			result.level( ingredients.get( 0 ).level() + 1 );
			result.identify( false );
			return result;
		}
	}
}
