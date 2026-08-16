/*
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 */

package com.erebus.reclaimedpixeldungeon.actors.mobs;

import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** A short-lived hostile copy created by an elite's Echo Legion skill. */
public class EliteEcho extends Mob {

	private static final float INHERITED_STAT_SCALE = 0.5f;

	private int minDamage;
	private int maxDamage;
	private int accuracy;
	private int armor;
	private int life;
	private int ownerId = -1;

	private static final String SPRITE = "sprite";
	private static final String MIN_DAMAGE = "min_damage";
	private static final String MAX_DAMAGE = "max_damage";
	private static final String ACCURACY = "accuracy";
	private static final String ARMOR = "armor";
	private static final String LIFE = "life";
	private static final String OWNER_ID = "owner_id";

	{
		EXP = 0;
		maxLvl = -1;
		state = HUNTING;
		properties.add(Property.BOSS_MINION);
	}

	public void configure(Mob source, Char target, int cell) {
		ownerId = source.id();
		spriteClass = source.spriteClass;
		pos = cell;
		mobStats = MobStats.inherit(source.mobStats, INHERITED_STAT_SCALE);
		HT = HP = Math.max(1, source.HT / 5 + (mobStats == null ? 0 : mobStats.health()));
		int rolledDamage = Math.max(1, source.damageRoll());
		minDamage = Math.max(1, rolledDamage / 4);
		maxDamage = Math.max(minDamage, rolledDamage / 2);
		accuracy = Math.max(1, source.attackSkill(target));
		armor = Math.max(0, source.drRoll() / 3);
		life = 8;
		firstAdded = false;
		aggro(target);
	}

	public boolean isEchoOf(Mob mob) {
		return mob != null && ownerId == mob.id();
	}

	@Override
	protected boolean act() {
		if (--life <= 0) {
			die(null);
			if (sprite != null) sprite.die();
			return true;
		}
		return super.act();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(minDamage, maxDamage);
	}

	@Override
	public int attackSkill(Char target) {
		return accuracy;
	}

	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, armor);
	}

	@Override
	public String description() {
		return Messages.get(this, "desc", HP, HT, minDamage, maxDamage, accuracy, armor, life);
	}

	@Override
	public String rarityStatsInfo() {
		String info = super.rarityStatsInfo();
		return info.isEmpty() ? "" : "_Inherited Rarity Stats (50%)_\n" + info;
	}

	/** Echo Legion copies are temporary skill effects, never independent loot sources. */
	@Override
	public void rollToDropLoot() {
	}

	@Override
	public CharSprite sprite() {
		CharSprite sprite = super.sprite();
		sprite.alpha(0.65f);
		return sprite;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SPRITE, spriteClass);
		bundle.put(MIN_DAMAGE, minDamage);
		bundle.put(MAX_DAMAGE, maxDamage);
		bundle.put(ACCURACY, accuracy);
		bundle.put(ARMOR, armor);
		bundle.put(LIFE, life);
		bundle.put(OWNER_ID, ownerId);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		spriteClass = bundle.getClass(SPRITE);
		minDamage = bundle.getInt(MIN_DAMAGE);
		maxDamage = bundle.getInt(MAX_DAMAGE);
		accuracy = bundle.getInt(ACCURACY);
		armor = bundle.getInt(ARMOR);
		life = bundle.getInt(LIFE);
		ownerId = bundle.contains(OWNER_ID) ? bundle.getInt(OWNER_ID) : -1;
	}
}
