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

package com.erebus.reclaimedpixeldungeon;

import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.remains.BowFragment;
import com.erebus.reclaimedpixeldungeon.items.remains.BrokenHilt;
import com.erebus.reclaimedpixeldungeon.items.remains.BrokenStaff;
import com.erebus.reclaimedpixeldungeon.items.remains.ClassCallItem;
import com.erebus.reclaimedpixeldungeon.items.remains.CloakScrap;
import com.erebus.reclaimedpixeldungeon.items.remains.RemainsItem;
import com.erebus.reclaimedpixeldungeon.items.remains.SealShard;
import com.erebus.reclaimedpixeldungeon.items.remains.TornPage;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class HeroClassUnlocks {

	public static final int FRAGMENTS_REQUIRED = 5;
	public static final int MAX_EXTRA_UNLOCKS = 2;

	private static final String KEY_UNLOCKED = "reclaimed_hero_unlocks";

	public static boolean isUnlocked( HeroClass cls ) {
		return cls == HeroClass.WARRIOR || unlockedNames().contains( cls.name() );
	}

	public static boolean canUnlock( HeroClass cls ) {
		return cls != null && cls != HeroClass.WARRIOR && !isUnlocked( cls ) && saveUnlocks() < MAX_EXTRA_UNLOCKS;
	}

	public static boolean unlock( HeroClass cls ) {
		if (!canUnlock( cls )) return false;
		HashSet<String> names = unlockedNames();
		names.add( cls.name() );
		storeUnlockedNames( names );
		Statistics.heroClassesUnlockedThisSave++;
		Badges.validateHeroClassUnlock( cls );
		return true;
	}

	public static int extraUnlocks() {
		return unlockedNames().size();
	}

	public static int saveUnlocks() {
		return Statistics.heroClassesUnlockedThisSave;
	}

	public static String lockMessage( HeroClass cls ) {
		return Messages.get( HeroClassUnlocks.class, "locked_save", Messages.titleCase( cls.title() ) );
	}

	public static String unlockMessage( HeroClass cls ) {
		if (cls == HeroClass.WARRIOR) return Messages.get( HeroClassUnlocks.class, "warrior_unlocked" );
		if (saveUnlocks() >= MAX_EXTRA_UNLOCKS && !isUnlocked( cls )) {
			return Messages.get( HeroClassUnlocks.class, "limit_reached", MAX_EXTRA_UNLOCKS );
		}
		return Messages.get( HeroClassUnlocks.class, "unlock_hint",
				Messages.titleCase( cls.title() ),
				FRAGMENTS_REQUIRED,
				Messages.titleCase( remainsFor( cls ).name() ),
				Messages.titleCase( ClassCallItem.get( cls ).name() ) );
	}

	public static HeroClass classFor( RemainsItem item ) {
		if (item instanceof SealShard) return HeroClass.WARRIOR;
		if (item instanceof BrokenStaff) return HeroClass.MAGE;
		if (item instanceof CloakScrap) return HeroClass.ROGUE;
		if (item instanceof BowFragment) return HeroClass.HUNTRESS;
		if (item instanceof BrokenHilt) return HeroClass.DUELIST;
		if (item instanceof TornPage) return HeroClass.CLERIC;
		return HeroClass.WARRIOR;
	}

	public static RemainsItem remainsFor( HeroClass cls ) {
		return RemainsItem.get( cls );
	}

	public static void dropBossRemains( Mob mob ) {
		if (mob == null || Dungeon.level == null || Dungeon.hero == null) return;
		if (!Statistics.amuletSecured || !Dungeon.bossLevel() || !mob.properties().contains( Char.Property.BOSS )) return;
		if (saveUnlocks() >= MAX_EXTRA_UNLOCKS) return;

		float chance = reducedBossRemainsChance() ? 0.25f : 0.50f;
		if (Random.Float() >= chance) return;

		RemainsItem remains = randomLockedRemains();
		if (remains == null) return;
		Statistics.qualifiedForBossRemainsBadge = true;
		Dungeon.level.drop( remains, mob.pos ).sprite.drop( mob.pos );
		GLog.p( Messages.get( HeroClassUnlocks.class, "boss_drop", Messages.titleCase( remains.name() ) ) );
	}

	public static RemainsItem debugBossRemainsDrop() {
		if (saveUnlocks() >= MAX_EXTRA_UNLOCKS) return null;
		return randomLockedRemains();
	}

	private static boolean reducedBossRemainsChance() {
		if (saveUnlocks() > 0) return true;
		for (HeroClass cls : HeroClass.values()) {
			if (canUnlock( cls ) && totalOwnedFragments( remainsFor( cls ).getClass() ) >= FRAGMENTS_REQUIRED) {
				return true;
			}
		}
		return false;
	}

	private static int totalOwnedFragments( Class<? extends RemainsItem> remainsClass ) {
		if (remainsClass == null) return 0;
		int total = 0;

		if (Dungeon.hero != null && Dungeon.hero.belongings != null) {
			for (Item item : Dungeon.hero.belongings.backpack.items) {
				total += countFragments( item, remainsClass );
			}
		}

		if (Dungeon.homebase != null) {
			for (Item item : Dungeon.homebase.vaultItems()) {
				total += countFragments( item, remainsClass );
			}
		}

		if (Dungeon.level != null && Dungeon.level.heaps != null) {
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				if (heap == null || heap.items == null) continue;
				for (Item item : heap.items) {
					total += countFragments( item, remainsClass );
				}
			}
		}

		return total;
	}

	private static int countFragments( Item item, Class<? extends RemainsItem> remainsClass ) {
		if (item == null || remainsClass == null) return 0;
		int total = item.getClass() == remainsClass ? item.quantity() : 0;
		if (item instanceof Bag) {
			for (Item nested : (Bag)item) {
				if (nested != item) total += countFragments( nested, remainsClass );
			}
		}
		return total;
	}

	private static RemainsItem randomLockedRemains() {
		float[] chances = new float[HeroClass.values().length];
		for (HeroClass cls : HeroClass.values()) {
			chances[cls.ordinal()] = isUnlocked( cls ) ? 0 : classDropWeight( cls );
		}

		int index = Random.chances( chances );
		if (index < 0) return null;
		return remainsFor( HeroClass.values()[index] );
	}

	private static float classDropWeight( HeroClass cls ) {
		switch (cls) {
			case MAGE:
				return 14;
			case HUNTRESS:
				return 13;
			case ROGUE:
				return 11;
			case DUELIST:
				return 10;
			case CLERIC:
				return 9;
			case WARRIOR: default:
				return 0;
		}
	}

	private static HashSet<String> unlockedNames() {
		HashSet<String> result = new HashSet<>();
		String raw = SPDSettings.getString( KEY_UNLOCKED, "" );
		if (!raw.isEmpty()) {
			for (String name : raw.split( "," )) {
				if (!name.isEmpty()) result.add( name );
			}
		}
		return result;
	}

	private static void storeUnlockedNames( HashSet<String> names ) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			if (builder.length() > 0) builder.append( "," );
			builder.append( name );
		}
		SPDSettings.put( KEY_UNLOCKED, builder.toString() );
	}

	public static class UnlockRecipe extends com.erebus.reclaimedpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			RemainsItem remains = matchingRemains( ingredients );
			if (remains == null) return false;
			HeroClass cls = classFor( remains );
			return totalMatchingRemains( ingredients, remains.getClass() ) >= FRAGMENTS_REQUIRED
					&& cls != HeroClass.WARRIOR
					&& !isUnlocked( cls );
		}

		@Override
		public int cost( ArrayList<Item> ingredients ) {
			return 0;
		}

		@Override
		public Item brew( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			RemainsItem remains = matchingRemains( ingredients );
			HeroClass cls = classFor( remains );
			int needed = FRAGMENTS_REQUIRED;
			for (Item ingredient : ingredients) {
				if (ingredient != null && ingredient.getClass() == remains.getClass() && needed > 0) {
					int spent = Math.min( needed, ingredient.quantity() );
					ingredient.quantity( ingredient.quantity() - spent );
					needed -= spent;
				}
			}
			return ClassCallItem.get( cls );
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			return ClassCallItem.get( classFor( matchingRemains( ingredients ) ) );
		}

		private RemainsItem matchingRemains( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.isEmpty()) return null;
			RemainsItem remains = null;
			for (Item ingredient : ingredients) {
				if (!(ingredient instanceof RemainsItem)) return null;
				if (remains == null) {
					remains = (RemainsItem)ingredient;
				} else if (ingredient.getClass() != remains.getClass()) {
					return null;
				}
			}
			return remains;
		}

		private int totalMatchingRemains( ArrayList<Item> ingredients, Class<? extends RemainsItem> remainsClass ) {
			int total = 0;
			for (Item ingredient : ingredients) {
				if (ingredient != null && ingredient.getClass() == remainsClass) {
					total += ingredient.quantity();
				}
			}
			return total;
		}
	}
}
