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

package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.FlavourBuff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class BuffLayoutTestKit extends Item {

	public static final String AC_APPLY = "APPLY";
	public static final String AC_CLEAR = "CLEAR";
	private static final float TEST_DURATION = 100000f;

	private static final Class<? extends TestDisplayBuff>[] TEST_BUFFS = new Class[]{
			TestBuff01.class, TestBuff02.class, TestBuff03.class, TestBuff04.class,
			TestBuff05.class, TestBuff06.class, TestBuff07.class, TestBuff08.class,
			TestBuff09.class, TestBuff10.class, TestBuff11.class, TestBuff12.class,
			TestBuff13.class, TestBuff14.class
	};

	{
		image = ItemSpriteSheet.KIT;
		unique = true;
		keptThoughLostInvent = true;
		defaultAction = AC_APPLY;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_APPLY);
		actions.add(AC_CLEAR);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!HomebaseState.buffLayoutTestItemEnabled()) {
			GLog.w("Buff layout testing is disabled.");
			return;
		}

		if (AC_APPLY.equals(action)) {
			for (Class<? extends TestDisplayBuff> buffClass : TEST_BUFFS) {
				Buff.prolong(hero, buffClass, TEST_DURATION);
			}
			GLog.p("Applied 14 long-duration display buffs.");
		} else if (AC_CLEAR.equals(action)) {
			for (Class<? extends TestDisplayBuff> buffClass : TEST_BUFFS) {
				Buff buff = hero.buff(buffClass);
				if (buff != null) buff.detach();
			}
			GLog.i("Cleared the buff layout test effects.");
		}
	}

	public abstract static class TestDisplayBuff extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public String name() {
			return "Buff layout test";
		}

		@Override
		public String desc() {
			return "A harmless, long-duration effect used to test status icon wrapping.";
		}
	}

	public static class TestBuff01 extends TestDisplayBuff { public int icon() { return BuffIndicator.MIND_VISION; } }
	public static class TestBuff02 extends TestDisplayBuff { public int icon() { return BuffIndicator.LEVITATION; } }
	public static class TestBuff03 extends TestDisplayBuff { public int icon() { return BuffIndicator.FIRE; } }
	public static class TestBuff04 extends TestDisplayBuff { public int icon() { return BuffIndicator.POISON; } }
	public static class TestBuff05 extends TestDisplayBuff { public int icon() { return BuffIndicator.PARALYSIS; } }
	public static class TestBuff06 extends TestDisplayBuff { public int icon() { return BuffIndicator.HUNGER; } }
	public static class TestBuff07 extends TestDisplayBuff { public int icon() { return BuffIndicator.TIME; } }
	public static class TestBuff08 extends TestDisplayBuff { public int icon() { return BuffIndicator.ROOTS; } }
	public static class TestBuff09 extends TestDisplayBuff { public int icon() { return BuffIndicator.INVISIBLE; } }
	public static class TestBuff10 extends TestDisplayBuff { public int icon() { return BuffIndicator.WEAKNESS; } }
	public static class TestBuff11 extends TestDisplayBuff { public int icon() { return BuffIndicator.FROST; } }
	public static class TestBuff12 extends TestDisplayBuff { public int icon() { return BuffIndicator.ARMOR; } }
	public static class TestBuff13 extends TestDisplayBuff { public int icon() { return BuffIndicator.LIGHT; } }
	public static class TestBuff14 extends TestDisplayBuff { public int icon() { return BuffIndicator.BLESS; } }
}
