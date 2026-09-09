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
 */

package com.erebus.reclaimedpixeldungeon.actors.mobs;

import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.trinkets.MimicTooth;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.MimicSprite;

public class ArcaneReliquaryMimic extends GoldenMimic {

	{
		spriteClass = MimicSprite.ArcaneReliquary.class;
	}

	@Override
	public String name() {
		return alignment == Alignment.NEUTRAL
				? Messages.get(Heap.class, "arcane_reliquary")
				: Messages.get(this, "name");
	}

	@Override
	public String description() {
		if (alignment != Alignment.NEUTRAL) return Messages.get(this, "desc");
		String desc = Messages.get(Heap.class, "arcane_reliquary_desc");
		if (!MimicTooth.stealthyMimics()) desc += "\n\n" + Messages.get(this, "hidden_hint");
		return desc;
	}
}
