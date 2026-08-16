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

package com.erebus.reclaimedpixeldungeon.actors.blobs;

import com.erebus.reclaimedpixeldungeon.messages.Messages;

/** Smoke created by a non-player source, which also blocks the hero's sight. */
public class HostileSmokeScreen extends SmokeScreen {

	@Override
	public String tileDesc() {
		return Messages.get(SmokeScreen.class, "desc");
	}
}
