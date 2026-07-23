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

package com.erebus.reclaimedpixeldungeon.items.remains;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.HeroClassUnlocks;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public abstract class ClassCallItem extends Item {

	public static final String AC_UNLOCK = "UNLOCK";

	{
		bones = false;
		defaultAction = AC_UNLOCK;
	}

	protected abstract HeroClass heroClass();

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_UNLOCK );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_UNLOCK )) {
			if (HeroClassUnlocks.unlock( heroClass() )) {
				hero.sprite.operate( hero.pos );
				Catalog.countUse( getClass() );
				GLog.p( Messages.get( HeroClassUnlocks.class, "unlocked", Messages.titleCase( heroClass().title() ) ) );
				Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				hero.spendAndNext( Actor.TICK );
				detach( hero.belongings.backpack );
			} else if (HeroClassUnlocks.isUnlocked( heroClass() )) {
				GLog.i( Messages.get( HeroClassUnlocks.class, "already_unlocked", Messages.titleCase( heroClass().title() ) ) );
			} else {
				GLog.w( Messages.get( HeroClassUnlocks.class, "limit_reached", HeroClassUnlocks.MAX_EXTRA_UNLOCKS ) );
			}
		}
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public int value() {
		return 250;
	}

	public static ClassCallItem get( HeroClass cls ) {
		switch (cls) {
			case MAGE:
				return new ArcanistsOath();
			case ROGUE:
				return new ShadowPact();
			case HUNTRESS:
				return new HuntressCall();
			case DUELIST:
				return new DuelistsVow();
			case CLERIC:
				return new SacredSummons();
			case WARRIOR: default:
				return null;
		}
	}
}
