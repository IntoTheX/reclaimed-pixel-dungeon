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

public class ProvisionKey extends Key {

	{
		image = ItemSpriteSheet.PROVISION_KEY;
	}

	public ProvisionKey() {
		this(0);
	}

	public ProvisionKey(int depth) {
		super();
		this.depth = depth;
	}
}
