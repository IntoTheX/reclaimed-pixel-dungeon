/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 */

package com.erebus.reclaimedpixeldungeon.items;

import com.watabou.utils.Random;

public final class EnchantmentSlots {

	public static final int MAX_SLOTS = 5;
	private static final int[] MERGE_CHANCES = {100, 90, 75, 60};
	private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};

	private EnchantmentSlots() {}

	public static int slotForLevel( int level ) {
		return Math.max( 0, Math.min( MAX_SLOTS - 1, level ) );
	}

	public static String roman( int level ) {
		return ROMAN[slotForLevel( level )];
	}

	public static int mergeChance( int level ) {
		return level >= 0 && level < MERGE_CHANCES.length ? MERGE_CHANCES[level] : 0;
	}

	public static int randomNaturalCount() {
		float roll = Random.Float( 100f );
		if (roll < 0.1f) return 5;
		if (roll < 0.5f) return 4;
		if (roll < 2f) return 3;
		if (roll < 10f) return 2;
		return 1;
	}
}
