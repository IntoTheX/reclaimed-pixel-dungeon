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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.EliteMob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.MobSpawner;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class EliteMobSpawner extends Item {

	public static final String AC_SPAWN = "SPAWN";

	{
		image = ItemSpriteSheet.BEACON;
		unique = true;
		keptThoughLostInvent = true;
		defaultAction = AC_SPAWN;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SPAWN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (!AC_SPAWN.equals(action)) return;
		if (!HomebaseState.eliteMobTestItemEnabled()) {
			GLog.w("Elite mob testing is disabled.");
			return;
		}

		String[] choices = new String[EliteMob.Rank.values().length];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = EliteMob.Rank.values()[i].rarity.coloredName();
		}
		GameScene.show(new WndOptions(new ItemSprite(this), name(),
				"Choose the elite rarity to summon. Test summons ignore normal floor and rarity rules.", choices) {
			@Override
			protected void onSelect(int index) {
				if (index >= 0 && index < EliteMob.Rank.values().length) {
					spawnElite(EliteMob.Rank.values()[index]);
				}
			}
		});
	}

	private void spawnElite(EliteMob.Rank rank) {
		int cell = nearbyCell();
		if (cell == -1) {
			GLog.w("There is no clear space nearby for an elite enemy.");
			return;
		}

		int depth = Math.max(1, Dungeon.level.contentDepth());
		ArrayList<Class<? extends Mob>> rotation = MobSpawner.getMobRotation(depth);
		Mob mob = null;
		for (int attempts = 0; attempts < rotation.size() * 2 && mob == null; attempts++) {
			Mob candidate = Reflection.newInstance(Random.element(rotation));
			if (EliteMob.eligible(candidate)) mob = candidate;
		}
		if (mob == null || !mob.forceEliteForTesting(rank)) {
			GLog.w("No valid elite enemy could be created here.");
			return;
		}

		mob.pos = cell;
		mob.beckon(Dungeon.hero.pos);
		GameScene.add(mob);
		GLog.w("A " + rank.rarity.displayName() + " elite answers the test beacon.");
	}

	private int nearbyCell() {
		ArrayList<Integer> cells = new ArrayList<>();
		int heroX = Dungeon.hero.pos % Dungeon.level.width();
		int heroY = Dungeon.hero.pos / Dungeon.level.width();
		for (int dy = -2; dy <= 2; dy++) {
			for (int dx = -2; dx <= 2; dx++) {
				if (dx == 0 && dy == 0) continue;
				int x = heroX + dx;
				int y = heroY + dy;
				if (x <= 0 || x >= Dungeon.level.width() - 1 || y <= 0 || y >= Dungeon.level.height() - 1) continue;
				int cell = x + y * Dungeon.level.width();
				if (Dungeon.level.passable[cell] && Actor.findChar(cell) == null) cells.add(cell);
			}
		}
		return cells.isEmpty() ? -1 : Random.element(cells);
	}
}
