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
		int dropped = 0;

		for (int i = 0; i < ITEMS_PER_USE; i++) {
			Item generatedItem = Generator.randomUsingDefaults();

			if (generatedItem == null) {
				continue;
			}

			// Identifying the item makes trading tests easier because both
			// players can immediately see exactly what was generated.
			generatedItem.identify();

			if (generatedItem.collect(hero.belongings.backpack)) {
				collected++;
			} else {
				Dungeon.level.drop(generatedItem, hero.pos).sprite.drop();
				dropped++;
			}
		}

		if (dropped > 0) {
			GLog.p(
					"Generated " + (collected + dropped)
							+ " trading test items. "
							+ dropped
							+ " item"
							+ (dropped == 1 ? " was" : "s were")
							+ " dropped because your backpack is full."
			);
		} else {
			GLog.p("Generated " + collected + " trading test items.");
		}
	}
}