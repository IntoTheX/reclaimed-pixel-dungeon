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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;

public class RaidThreat extends Buff {

	private static final float CLOSE_THRESHOLD = 0.75f;

	@Override
	public int icon() {
		if ((Dungeon.homebase != null && Dungeon.homebase.raidActive()) || Dungeon.raidThreatReady()) {
			return BuffIndicator.RAID_THREAT_READY;
		}
		return threatRatio() >= CLOSE_THRESHOLD
				? BuffIndicator.RAID_THREAT_CLOSE
				: BuffIndicator.RAID_THREAT_LOW;
	}

	@Override
	public String name() {
		return "Homebase threat";
	}

	@Override
	public String desc() {
		int threat = Dungeon.raidThreat();
		int target = Math.max( 1, Dungeon.raidThreatTarget() );
		int percent = Math.max( 0, Math.round( threat * 100f / target ) );
		String status;
		if (Dungeon.homebase != null && Dungeon.homebase.raidActive()) {
			status = "_Raid in progress._ Defend the Homebase before beginning another expedition.";
		} else if (Dungeon.raidThreatReady()) {
			status = "_Raid ready._ The next eligible return to the Homebase will begin a raid.";
		} else if (threatRatio() >= CLOSE_THRESHOLD) {
			status = "_Threat is high._ Consider returning to prepare the Homebase before the threshold is reached.";
		} else {
			status = "_Threat is low._ Exploring floors, defeating enemies, and opening secured loot will raise it.";
		}
		return status + "\n\nCurrent threat: _" + threat + "/" + target + "_ (" + percent + "%).";
	}

	private float threatRatio() {
		return Dungeon.raidThreat() / (float)Math.max( 1, Dungeon.raidThreatTarget() );
	}
}
