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

package com.erebus.reclaimedpixeldungeon.effects;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.ui.BuffIcon;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

/** A short-lived item icon displayed above an elite's skill announcement. */
public class EliteSkillIcon extends Image {

	private static final float LIFESPAN = 1f;
	private static final float RISE_DISTANCE = DungeonTilemap.SIZE;

	private float timeLeft = LIFESPAN;

	private EliteSkillIcon(int icon, int cell) {
		super(Assets.Sprites.ITEM_ICONS);
		frame(ItemSpriteSheet.Icons.film.get(icon));
		placeAt(cell, 1.5f);
	}

	private EliteSkillIcon(Item item, int cell) {
		copy(new ItemSprite(item));
		placeAt(cell, 0.75f, 0f, 0f);
	}

	private EliteSkillIcon(Image source, int cell, float iconScale, float xOffset, float yOffset) {
		copy(source);
		placeAt(cell, iconScale, xOffset, yOffset);
	}

	private void placeAt(int cell, float iconScale) {
		placeAt(cell, iconScale, 0f, 0f);
	}

	private void placeAt(int cell, float iconScale, float xOffset, float yOffset) {
		PointF center = DungeonTilemap.tileCenterToWorld(cell);
		scale.set(iconScale);
		x = center.x - width() / 2f + xOffset;
		y = center.y - 28f + yOffset;
	}

	@Override
	public void update() {
		super.update();
		timeLeft -= Game.elapsed;
		if (timeLeft <= 0f) {
			killAndErase();
			return;
		}

		y -= RISE_DISTANCE * Game.elapsed / LIFESPAN;
		float progress = timeLeft / LIFESPAN;
		alpha(progress > 0.5f ? 1f : progress * 2f);
	}

	public static void showIcon(int cell, int icon) {
		if (icon < 0 || !visible(cell)) return;
		GameScene.effect(new EliteSkillIcon(icon, cell));
	}

	public static void showItem(int cell, Item item) {
		if (item == null || !visible(cell)) return;
		GameScene.effect(new EliteSkillIcon(item, cell));
	}

	public static void showInterfaceIcon(int cell, Icons icon) {
		if (icon == null || !visible(cell)) return;
		GameScene.effect(new EliteSkillIcon(icon.get(), cell, 0.75f, 0f, 0f));
	}

	public static void showTextIcon(int cell, int icon) {
		if (icon < 0 || !visible(cell)) return;
		Image image = new Image(Assets.Effects.TEXT_ICONS);
		image.frame(FloatingText.iconFilm.get(icon));
		GameScene.effect(new EliteSkillIcon(image, cell, 1.5f, 0f, 0f));
	}

	public static void showBuffIcon(int cell, int icon) {
		if (icon < 0 || !visible(cell)) return;
		GameScene.effect(new EliteSkillIcon(new BuffIcon(icon, true), cell, 0.75f, 0f, 0f));
	}

	/** Displays two compact item icons with half of their widths overlapping. */
	public static void showOverlappingIcons(int cell, int leftIcon, int rightIcon) {
		if (!visible(cell)) return;
		GameScene.effect(itemIcon(cell, leftIcon, -2.6f, 0f));
		GameScene.effect(itemIcon(cell, rightIcon, 2.6f, 0f));
	}

	/** Storm Cage combines the lightning text icon above paralytic gas. */
	public static void showStormCage(int cell) {
		if (!visible(cell)) return;
		Image lightning = new Image(Assets.Effects.TEXT_ICONS);
		lightning.frame(FloatingText.iconFilm.get(FloatingText.SHOCKING));
		GameScene.effect(new EliteSkillIcon(lightning, cell, 1.5f, 0f, -3.5f));
		GameScene.effect(itemIcon(cell, ItemSpriteSheet.Icons.POTION_PARAGAS, 0f, 3.5f));
	}

	/** Uses the same spectral web projectile fired by Spinner mobs. */
	public static void showWebProjectile(int cell) {
		if (!visible(cell) || Dungeon.hero == null || Dungeon.hero.sprite == null
				|| Dungeon.hero.sprite.parent == null) return;
		PointF center = DungeonTilemap.tileCenterToWorld(cell);
		MagicMissile missile = (MagicMissile)Dungeon.hero.sprite.parent.recycle(MagicMissile.class);
		missile.reset(MagicMissile.MAGIC_MISSILE,
				new PointF(center.x - 6f, center.y - 24f),
				new PointF(center.x + 6f, center.y - 28f),
				missile::killAndErase);
	}

	private static EliteSkillIcon itemIcon(int cell, int icon, float xOffset, float yOffset) {
		Image image = new Image(Assets.Sprites.ITEM_ICONS);
		image.frame(ItemSpriteSheet.Icons.film.get(icon));
		return new EliteSkillIcon(image, cell, 1.5f, xOffset, yOffset);
	}

	private static boolean visible(int cell) {
		return Dungeon.level != null && Dungeon.level.heroFOV != null
				&& cell >= 0 && cell < Dungeon.level.length()
				&& Dungeon.level.heroFOV[cell];
	}
}
