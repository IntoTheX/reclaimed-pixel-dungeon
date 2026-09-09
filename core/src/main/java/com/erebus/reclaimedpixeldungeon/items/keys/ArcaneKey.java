/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software under the GNU General Public License v3.
 */
package com.erebus.reclaimedpixeldungeon.items.keys;

import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class ArcaneKey extends Key {

	{
		image = ItemSpriteSheet.ARCANE_KEY;
	}

	public ArcaneKey() {
		this(0);
	}

	public ArcaneKey(int depth) {
		super();
		this.depth = depth;
	}
}
