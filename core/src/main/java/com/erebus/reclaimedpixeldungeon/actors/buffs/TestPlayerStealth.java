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

package com.erebus.reclaimedpixeldungeon.actors.buffs;

import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;

/** Silent, persistent stealth used only by the player targeting test toggle. */
public class TestPlayerStealth extends Buff {

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)) {
			target.invisible++;
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		if (target.invisible > 0) target.invisible--;
		super.detach();
	}

	@Override
	public boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public void fx(boolean on) {
		if (target.sprite == null) return;
		if (on) target.sprite.add(CharSprite.State.INVISIBLE);
		else if (target.invisible == 0) target.sprite.remove(CharSprite.State.INVISIBLE);
	}
}
