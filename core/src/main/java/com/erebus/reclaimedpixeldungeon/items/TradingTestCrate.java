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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.materials.CopperOre;
import com.erebus.reclaimedpixeldungeon.items.materials.EmberCore;
import com.erebus.reclaimedpixeldungeon.items.materials.EmberShard;
import com.erebus.reclaimedpixeldungeon.items.materials.GoldOre;
import com.erebus.reclaimedpixeldungeon.items.materials.IronOre;
import com.erebus.reclaimedpixeldungeon.items.materials.ScrapBundle;
import com.erebus.reclaimedpixeldungeon.items.materials.StoneBlock;
import com.erebus.reclaimedpixeldungeon.items.materials.WoodBundle;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class TradingTestCrate extends Item {

	public static final String AC_OPEN = "OPEN";

	private static final int ITEMS_PER_USE = 10;

	{
		image = ItemSpriteSheet.CHEST;

		// Only one test crate should exist at a time.
		unique = true;

		// Keep the crate between dungeon runs and lost-inventory situations.
		keptThoughLostInvent = true;

		defaultAction = AC_OPEN;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_OPEN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (!AC_OPEN.equals(action)) {
			return;
		}

		if (!HomebaseState.tradingTestItemsEnabled()) {
			GLog.w("Trading test items are disabled.");
			return;
		}

		int collected = 0;
		int displayDrops = 0;
		int overflowDrops = 0;

		ArrayList<Item> groundItems = new ArrayList<>();
		groundItems.add(new Emerald());
		groundItems.add(new WoodBundle());
		groundItems.add(new StoneBlock());
		groundItems.add(new CopperOre());
		groundItems.add(new IronOre());
		groundItems.add(new GoldOre());
		groundItems.add(new ScrapBundle());
		groundItems.add(new EmberShard());
		groundItems.add(new EmberCore());
		displayDrops += dropForDisplay(hero, groundItems);

		SpatialGeode geode = new SpatialGeode();
		if (geode.collect(hero.belongings.backpack)) {
			collected++;
		} else {
			Dungeon.level.drop(geode, hero.pos).sprite.drop();
			overflowDrops++;
		}

		for (int i = 0; i < ITEMS_PER_USE; i++) {
			Item generatedItem = Generator.randomUsingDefaults();

			if (generatedItem == null) {
				continue;
			}
			if (generatedItem instanceof Gold) {
				i--;
				continue;
			}

			// Identifying the item makes trading tests easier because both
			// players can immediately see exactly what was generated.
			generatedItem.identify();

			if (generatedItem.collect(hero.belongings.backpack)) {
				collected++;
			} else {
				Dungeon.level.drop(generatedItem, hero.pos).sprite.drop();
				overflowDrops++;
			}
		}

		String message = "Generated " + (collected + displayDrops + overflowDrops)
				+ " trading test items. " + displayDrops
				+ " currencies were placed on the ground for sprite testing.";
		if (overflowDrops > 0) {
			message += " " + overflowDrops
							+ " item"
							+ (overflowDrops == 1 ? " was" : "s were")
							+ " also dropped because your backpack is full.";
		}
		GLog.p(message);
	}

	private int dropForDisplay(Hero hero, ArrayList<Item> items) {
		ArrayList<Integer> cells = new ArrayList<>();
		int width = Dungeon.level.width();
		int height = Dungeon.level.height();
		int heroX = hero.pos % width;
		int heroY = hero.pos / width;

		for (int radius = 1; radius <= 4 && cells.size() < items.size(); radius++) {
			for (int y = heroY - radius; y <= heroY + radius && cells.size() < items.size(); y++) {
				for (int x = heroX - radius; x <= heroX + radius && cells.size() < items.size(); x++) {
					if (Math.max(Math.abs(x - heroX), Math.abs(y - heroY)) != radius
							|| x < 0 || x >= width || y < 0 || y >= height) {
						continue;
					}

					int cell = x + y * width;
					if (Dungeon.level.passable[cell] && Dungeon.level.heaps.get(cell) == null) {
						cells.add(cell);
					}
				}
			}
		}

		for (int i = 0; i < items.size(); i++) {
			int cell = i < cells.size() ? cells.get(i) : hero.pos;
			Dungeon.level.drop(items.get(i), cell).sprite.drop(hero.pos);
		}
		return items.size();
	}
}
