/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 */

package com.erebus.reclaimedpixeldungeon.actors.blobs;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Random;

public final class BlobResistance {

	private static final ThreadLocal<Boolean> BYPASS_RARITY_RESISTANCE = new ThreadLocal<>();

	private BlobResistance() {}

	public static boolean bypassesRarityResistance() {
		return Boolean.TRUE.equals(BYPASS_RARITY_RESISTANCE.get());
	}

	public static float potency() {
		return 100f + 3f * Math.max(0, Dungeon.depth - 1);
	}

	public static boolean triggers(Char target, Class<?> resistanceEffect) {
		if (!(target instanceof Hero)) return true;
		int resistance = ((Hero)target).belongings.equippedRarityResistance(resistanceEffect);
		float chance = Math.max(0f, Math.min(100f, potency() - resistance));
		if (Random.Float(100f) < chance) return true;
		Buff.showResisted(target);
		return false;
	}

	public static boolean apply(Char target, Class<?> resistanceEffect, Runnable effect) {
		if (!triggers(target, resistanceEffect)) return false;
		boolean previous = bypassesRarityResistance();
		BYPASS_RARITY_RESISTANCE.set(true);
		try {
			effect.run();
		} finally {
			if (previous) BYPASS_RARITY_RESISTANCE.set(true);
			else BYPASS_RARITY_RESISTANCE.remove();
		}
		return true;
	}
}
