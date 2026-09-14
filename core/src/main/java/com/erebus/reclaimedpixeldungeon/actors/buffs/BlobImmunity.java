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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.erebus.reclaimedpixeldungeon.actors.buffs;

import com.erebus.reclaimedpixeldungeon.actors.blobs.Blizzard;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ConfusionGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.CorrosiveGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Electricity;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Fire;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Freezing;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Inferno;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ParalyticGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Regrowth;
import com.erebus.reclaimedpixeldungeon.actors.blobs.SmokeScreen;
import com.erebus.reclaimedpixeldungeon.actors.blobs.StenchGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.StormCloud;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ToxicGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.VaultFlameTraps;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Web;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Tengu;
import com.erebus.reclaimedpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;

public class BlobImmunity extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
	}
	
	public static final float DURATION	= 20f;
	
	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	{
		//all harmful blobs
		immunities.add( Blizzard.class );
		immunities.add( ConfusionGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Electricity.class );
		immunities.add( Fire.class );
		immunities.add( MagicalFireRoom.EternalFire.class );
		immunities.add( Freezing.class );
		immunities.add( Inferno.class );
		immunities.add( ParalyticGas.class );
		immunities.add( Regrowth.class );
		immunities.add( SmokeScreen.class );
		immunities.add( StenchGas.class );
		immunities.add( StormCloud.class );
		immunities.add( ToxicGas.class );
		immunities.add( Web.class );

		immunities.add(Tengu.FireAbility.FireBlob.class);

		immunities.add(VaultFlameTraps.class);
	}

}
