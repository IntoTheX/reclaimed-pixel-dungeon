/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software under the GNU General Public License v3.
 */
package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.watabou.utils.Random;

import java.util.ArrayList;

public final class SpecialChestLoot {

	private static final int MAX_TYPES = 3;

	private SpecialChestLoot() {
	}

	public static ArrayList<Item> catalysts(int depth) {
		int total = 5 * region(depth);
		return bundles(total, true, depth);
	}

	public static ArrayList<Item> resources(int depth) {
		int total = 10 * region(depth);
		return bundles(total, false, depth);
	}

	public static ArrayList<Item> catalysts(int total, int depth) {
		return bundles(Math.max(1, total), true, depth);
	}

	public static ArrayList<Item> resources(int total, int depth) {
		return bundles(Math.max(1, total), false, depth);
	}

	private static int region(int depth) {
		return 1 + Math.max(0, depth - 1) / 5;
	}

	private static ArrayList<Item> bundles(int total, boolean catalysts, int depth) {
		ArrayList<Item> result = new ArrayList<>();
		int types = Random.IntRange(1, Math.min(MAX_TYPES, total));
		for (int i = 0; i < types; i++) {
			Item item;
			int attempts = 0;
			do {
				item = catalysts
						? Generator.randomRarityCatalyst()
						: BuildingMaterial.randomResourceBundleForDepth(depth, 1, 1);
				attempts++;
			} while (containsClass(result, item.getClass()) && attempts < 30);
			if (!containsClass(result, item.getClass())) result.add(item);
		}
		while (result.size() < types) {
			Item item = catalysts
					? Generator.randomRarityCatalyst()
					: BuildingMaterial.randomResourceBundleForDepth(depth, 1, 1);
			if (!containsClass(result, item.getClass())) result.add(item);
		}

		int remaining = total;
		for (int i = 0; i < result.size(); i++) {
			int quantity = i == result.size() - 1
					? remaining
					: Random.IntRange(1, remaining - (result.size() - i - 1));
			result.get(i).quantity(quantity);
			remaining -= quantity;
		}
		return result;
	}

	private static boolean containsClass(ArrayList<Item> items, Class<?> type) {
		for (Item item : items) if (item.getClass() == type) return true;
		return false;
	}
}
