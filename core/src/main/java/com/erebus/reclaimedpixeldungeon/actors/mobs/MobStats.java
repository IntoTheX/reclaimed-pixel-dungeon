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

package com.erebus.reclaimedpixeldungeon.actors.mobs;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barkskin;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barrier;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bleeding;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bless;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Corrosion;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Cripple;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Daze;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Frost;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hex;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Haste;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Poison;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Roots;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Slow;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vertigo;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vulnerable;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Weakness;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfBlastWave;
import com.erebus.reclaimedpixeldungeon.levels.VaultLevel;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class MobStats implements Bundlable {

	private int level;
	private float baselineScale = 1f;
	private boolean bossScaleApplied;
	private EnumMap<RarityStat.Type, Integer> stats = new EnumMap<>( RarityStat.Type.class );

	private static final RarityStat.Type[] BONUS_POOL = {
			RarityStat.Type.MAX_HEALTH,
			RarityStat.Type.ATTACK_DAMAGE,
			RarityStat.Type.ATTACK_BONUS,
			RarityStat.Type.ATTACK_ACCURACY,
			RarityStat.Type.ATTACK_SPEED,
			RarityStat.Type.MOVEMENT_SPEED,
			RarityStat.Type.DEFENSE,
			RarityStat.Type.ARMOR_BONUS,
			RarityStat.Type.EVASION,
			RarityStat.Type.DODGE_CHANCE,
			RarityStat.Type.CRITICAL_CHANCE,
			RarityStat.Type.BLOCK_CHANCE,
			RarityStat.Type.THORNS_CHANCE,
			RarityStat.Type.BARKSKIN_PROC,
			RarityStat.Type.BARRIER_PROC,
			RarityStat.Type.BLESS_PROC,
			RarityStat.Type.HASTE_PROC,
			RarityStat.Type.BLEED_PROC,
			RarityStat.Type.BURNING_PROC,
			RarityStat.Type.POISON_PROC,
			RarityStat.Type.CORROSION_PROC,
			RarityStat.Type.FROST_PROC,
			RarityStat.Type.CRIPPLE_PROC,
			RarityStat.Type.DAZE_PROC,
			RarityStat.Type.HEX_PROC,
			RarityStat.Type.VULNERABLE_PROC,
			RarityStat.Type.BLINDNESS_PROC,
			RarityStat.Type.ROOT_PROC,
			RarityStat.Type.SLOW_PROC,
			RarityStat.Type.VERTIGO_PROC,
			RarityStat.Type.STUN_CHANCE,
			RarityStat.Type.WEAKNESS_PROC,
			RarityStat.Type.SUMMON_LIGHTNING_CHANCE,
			RarityStat.Type.KNOCKBACK_CHANCE,
			RarityStat.Type.CLEAVE_CHANCE,
			RarityStat.Type.PIERCING_CHANCE,
			RarityStat.Type.GUARD_BREAK,
			RarityStat.Type.LIFESTEAL,
			RarityStat.Type.XP_GAIN,
			RarityStat.Type.FIRE_RESISTANCE,
			RarityStat.Type.FROST_RESISTANCE,
			RarityStat.Type.POISON_RESISTANCE,
			RarityStat.Type.CORROSION_RESISTANCE,
			RarityStat.Type.BLEED_RESISTANCE,
			RarityStat.Type.CRIPPLE_RESISTANCE,
			RarityStat.Type.DAZE_RESISTANCE,
			RarityStat.Type.HEX_RESISTANCE,
			RarityStat.Type.VULNERABLE_RESISTANCE,
			RarityStat.Type.BLINDNESS_RESISTANCE,
			RarityStat.Type.ROOT_RESISTANCE,
			RarityStat.Type.SLOW_RESISTANCE,
			RarityStat.Type.VERTIGO_RESISTANCE,
			RarityStat.Type.STUN_RESISTANCE,
			RarityStat.Type.WEAKNESS_RESISTANCE
	};

	private static final RarityStat.Type[][] FOCUSED_POOLS = {
			{
					RarityStat.Type.MAX_HEALTH,
					RarityStat.Type.ATTACK_DAMAGE,
					RarityStat.Type.DEFENSE,
					RarityStat.Type.ARMOR_BONUS,
					RarityStat.Type.BLOCK_CHANCE,
					RarityStat.Type.BARRIER_PROC
			},
			{
					RarityStat.Type.MOVEMENT_SPEED,
					RarityStat.Type.ATTACK_SPEED,
					RarityStat.Type.ATTACK_ACCURACY,
					RarityStat.Type.DODGE_CHANCE,
					RarityStat.Type.HASTE_PROC,
					RarityStat.Type.CRITICAL_CHANCE
			},
			{
					RarityStat.Type.ATTACK_DAMAGE,
					RarityStat.Type.ATTACK_ACCURACY,
					RarityStat.Type.CRITICAL_CHANCE,
					RarityStat.Type.CLEAVE_CHANCE,
					RarityStat.Type.PIERCING_CHANCE,
					RarityStat.Type.GUARD_BREAK,
					RarityStat.Type.LIFESTEAL
			},
			{
					RarityStat.Type.BLEED_PROC,
					RarityStat.Type.POISON_PROC,
					RarityStat.Type.CORROSION_PROC,
					RarityStat.Type.BURNING_PROC,
					RarityStat.Type.LIFESTEAL,
					RarityStat.Type.MOVEMENT_SPEED
			},
			{
					RarityStat.Type.ROOT_PROC,
					RarityStat.Type.SLOW_PROC,
					RarityStat.Type.VERTIGO_PROC,
					RarityStat.Type.DAZE_PROC,
					RarityStat.Type.STUN_CHANCE,
					RarityStat.Type.ATTACK_SPEED
			},
			{
					RarityStat.Type.FIRE_RESISTANCE,
					RarityStat.Type.FROST_RESISTANCE,
					RarityStat.Type.POISON_RESISTANCE,
					RarityStat.Type.CORROSION_RESISTANCE,
					RarityStat.Type.STUN_RESISTANCE,
					RarityStat.Type.WEAKNESS_RESISTANCE
			},
			{
					RarityStat.Type.THORNS_CHANCE,
					RarityStat.Type.BARKSKIN_PROC,
					RarityStat.Type.BLESS_PROC,
					RarityStat.Type.KNOCKBACK_CHANCE,
					RarityStat.Type.SUMMON_LIGHTNING_CHANCE,
					RarityStat.Type.VULNERABLE_PROC
			}
	};

	private static final float[] BONUS_POOL_WEIGHTS = {
			2f,     // MAX_HEALTH, already gets level baseline
			2f,     // ATTACK_DAMAGE, already gets level baseline
			2f,     // ATTACK_BONUS, already gets level baseline
			5f,     // ATTACK_ACCURACY
			2f,     // ATTACK_SPEED
			9f,     // MOVEMENT_SPEED
			3f,     // DEFENSE
			2f,     // ARMOR_BONUS
			4f,     // EVASION
			4f,     // DODGE_CHANCE
			4f,     // CRITICAL_CHANCE
			3f,     // BLOCK_CHANCE
			2f,     // THORNS_CHANCE
			2f,     // BARKSKIN_PROC
			2f,     // BARRIER_PROC
			2f,     // BLESS_PROC
			3f,     // HASTE_PROC
			2f,     // BLEED_PROC
			2f,     // BURNING_PROC
			2f,     // POISON_PROC
			2f,     // CORROSION_PROC
			2f,     // FROST_PROC
			2f,     // CRIPPLE_PROC
			2f,     // DAZE_PROC
			2f,     // HEX_PROC
			2f,     // VULNERABLE_PROC
			2f,     // BLINDNESS_PROC
			2f,     // ROOT_PROC
			2f,     // SLOW_PROC
			2f,     // VERTIGO_PROC
			2f,     // STUN_CHANCE
			2f,     // WEAKNESS_PROC
			1.5f,   // SUMMON_LIGHTNING_CHANCE
			2f,     // KNOCKBACK_CHANCE
			1.5f,   // CLEAVE_CHANCE
			1.5f,   // PIERCING_CHANCE
			1.5f,   // GUARD_BREAK
			2f,     // LIFESTEAL
			2f,     // XP_GAIN
			1f,     // FIRE_RESISTANCE
			1f,     // FROST_RESISTANCE
			1f,     // POISON_RESISTANCE
			1f,     // CORROSION_RESISTANCE
			1f,     // BLEED_RESISTANCE
			1f,     // CRIPPLE_RESISTANCE
			1f,     // DAZE_RESISTANCE
			1f,     // HEX_RESISTANCE
			1f,     // VULNERABLE_RESISTANCE
			1f,     // BLINDNESS_RESISTANCE
			1f,     // ROOT_RESISTANCE
			1f,     // SLOW_RESISTANCE
			1f,     // VERTIGO_RESISTANCE
			1f,     // STUN_RESISTANCE
			1f      // WEAKNESS_RESISTANCE
	};

	private static final String LEVEL = "level";
	private static final String BASELINE_SCALE = "baseline_scale";
	private static final String BOSS_SCALE_APPLIED = "boss_scale_applied";
	private static final String STATS = "stats";

	private static final String OLD_HEALTH = "health";
	private static final String OLD_DAMAGE = "damage";
	private static final String OLD_ACCURACY = "accuracy";
	private static final String OLD_EVASION = "evasion";
	private static final String OLD_ARMOR = "armor";
	private static final String OLD_SPEED = "speed";

	private static final int LEVEL_THREAT_STEP = 8;
	private static final int DEFENDER_STAT_UPGRADE_BASE_CHANCE = 35;
	private static final int DEFENDER_NEW_STAT_BASE_CHANCE = 25;

	public static MobStats roll() {
		MobStats stats = new MobStats();
		int threat = effectiveThreat();
		stats.level = levelForPressure( threat );

		int budget = Math.max( 2, 2 + stats.level / 4 + Random.Int( Math.max( 2, stats.level / 12 + 2 ) ) );
		stats.rollLevelChanceStats();
		stats.improve( budget );

		return stats;
	}

	public static MobStats inherit( MobStats source, float scale ) {
		if (source == null || scale <= 0f) return null;

		MobStats inherited = new MobStats();
		inherited.level = source.level;
		inherited.baselineScale = Math.min( 1f, scale );
		for (Map.Entry<RarityStat.Type, Integer> entry : source.stats.entrySet()) {
			int value = Math.round( entry.getValue() * inherited.baselineScale );
			if (entry.getValue() > 0) value = Math.max( 1, value );
			inherited.add( entry.getKey(), value );
		}
		return inherited;
	}

	public static MobStats rollForLevel( int level, int bonusBudget ) {
		MobStats stats = new MobStats();
		stats.level = Math.max( 1, level );
		stats.rollLevelChanceStats();
		stats.improve( Math.max( 2, 2 + stats.level / 4 + Math.max( 0, bonusBudget ) ) );
		return stats;
	}

	public static int currentLevel() {
		return levelForPressure( effectiveThreat() );
	}

	public boolean applyBossScale() {
		if (bossScaleApplied) return false;
		baselineScale *= 3f;
		bossScaleApplied = true;
		return true;
	}

	private static int effectiveThreat() {
		// The Dwarven Vault strips the hero's inventory and starts them with cloth armor.
		// Its enemies must remain level 1 without erasing the progression used outside it.
		if (Dungeon.level instanceof VaultLevel) return 0;
		long threat = Math.max( 0, Dungeon.mobLevelPressure() );
		if (Dungeon.homebase != null) {
			threat += Dungeon.homebase.permanentMobLevelPressureBonus();
		}
		threat += activeItemPotencyPressureBonus();
		return threat >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)threat;
	}

	private static int activeItemPotencyPressureBonus() {
		if (Dungeon.hero == null || Dungeon.hero.belongings == null) return 0;

		int pressure = 0;
		for (Ring ring : Dungeon.hero.belongings.equippedRings()) {
			pressure += ring.rarityStat( RarityStat.Type.RING_POTENCY );
		}
		for (Artifact artifact : Dungeon.hero.belongings.equippedArtifacts()) {
			pressure += artifact.rarityStat( RarityStat.Type.ARTIFACT_POTENCY );
		}
		pressure += Dungeon.hero.belongings.equippedRarityStat( RarityStat.Type.TRINKET_POTENCY );
		return Math.max( 0, pressure );
	}

	private static int levelForPressure( int threat ) {
		return Math.max( 1, 1 + Math.max( 0, threat ) / LEVEL_THREAT_STEP );
	}

	public int health() {
		return baselineHealth() + stat( RarityStat.Type.MAX_HEALTH );
	}

	public float applyDamage( float value ) {
		value += baselineAttackDamage() + stat( RarityStat.Type.ATTACK_DAMAGE );
		value *= 1f + (baselineAttackBonus() + stat( RarityStat.Type.ATTACK_BONUS )) / 100f;

		int critChance = stat( RarityStat.Type.CRITICAL_CHANCE );
		if (critChance > 0 && Random.Int( 100 ) < critChance) {
			value *= 2f + stat( RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER ) / 100f;
		}

		return value;
	}

	public float applyAccuracy( float value ) {
		if (value <= 0) return value;
		return value * (1f + stat( RarityStat.Type.ATTACK_ACCURACY ) / 100f);
	}

	public float applyEvasion( float value ) {
		value += stat( RarityStat.Type.EVASION );
		if (value <= 0) return value;
		return value * (1f + stat( RarityStat.Type.DODGE_CHANCE ) / 100f);
	}

	public int armor( int baseArmor ) {
		long flatArmor = Math.max( 0L, (long)baseArmor + baselineDefense()
				+ stat( RarityStat.Type.DEFENSE ) );
		double armorMultiplier = 1d + stat( RarityStat.Type.ARMOR_BONUS ) / 100d;
		return clampStat( Math.round( flatArmor * armorMultiplier ) );
	}

	public float applyMovementSpeed( float value ) {
		return value * (1f + stat( RarityStat.Type.MOVEMENT_SPEED ) / 100f);
	}

	public float applyAttackDelay( float value ) {
		return value / (1f + stat( RarityStat.Type.ATTACK_SPEED ) / 100f);
	}

	public int applyAttackProcs( Char attacker, Char defender, int damage ) {
		if (attacker == null || defender == null || damage <= 0 || !defender.isAlive()) return damage;
		if (attacker.alignment == defender.alignment) return damage;

		if (rollProc( RarityStat.Type.BLEED_PROC )) {
			Buff.affect( defender, Bleeding.class ).set( Math.max( 1f, damage * 0.25f * duration( RarityStat.Type.BLEED_DURATION ) ) );
		}
		if (rollProc( RarityStat.Type.BURNING_PROC )) {
			Buff.affect( defender, Burning.class ).reignite( defender, duration( 3f, RarityStat.Type.BURNING_DURATION ) );
		}
		if (rollProc( RarityStat.Type.POISON_PROC )) {
			Buff.affect( defender, Poison.class ).set( duration( 3f, RarityStat.Type.POISON_DURATION ) );
		}
		if (rollProc( RarityStat.Type.CORROSION_PROC )) {
			Buff.affect( defender, Corrosion.class ).set( duration( 3f, RarityStat.Type.CORROSION_DURATION ), Math.max( 1, Math.round( damage * 0.20f ) ) );
		}
		if (rollProc( RarityStat.Type.FROST_PROC )) {
			Buff.affect( defender, Chill.class, duration( 2f, RarityStat.Type.FROST_DURATION ) );
		}
		if (rollProc( RarityStat.Type.CRIPPLE_PROC )) {
			Buff.prolong( defender, Cripple.class, duration( 2f, RarityStat.Type.CRIPPLE_DURATION ) );
		}
		if (rollProc( RarityStat.Type.DAZE_PROC )) {
			Buff.prolong( defender, Daze.class, duration( Daze.DURATION, RarityStat.Type.DAZE_DURATION ) );
		}
		if (rollProc( RarityStat.Type.HEX_PROC )) {
			Buff.prolong( defender, Hex.class, duration( 4f, RarityStat.Type.HEX_DURATION ) );
		}
		if (rollProc( RarityStat.Type.VULNERABLE_PROC )) {
			Buff.prolong( defender, Vulnerable.class, duration( 4f, RarityStat.Type.VULNERABLE_DURATION ) );
		}
		if (rollProc( RarityStat.Type.BLINDNESS_PROC )) {
			Buff.prolong( defender, Blindness.class, duration( 2f, RarityStat.Type.BLINDNESS_DURATION ) );
		}
		if (rollProc( RarityStat.Type.ROOT_PROC )) {
			Buff.prolong( defender, Roots.class, duration( 1f, RarityStat.Type.ROOT_DURATION ) );
		}
		if (rollProc( RarityStat.Type.SLOW_PROC )) {
			Buff.prolong( defender, Slow.class, duration( 2f, RarityStat.Type.SLOW_DURATION ) );
		}
		if (rollProc( RarityStat.Type.VERTIGO_PROC )) {
			Buff.prolong( defender, Vertigo.class, duration( 2f, RarityStat.Type.VERTIGO_DURATION ) );
		}
		if (rollProc( RarityStat.Type.STUN_CHANCE )) {
			Buff.prolong( defender, Paralysis.class, duration( 2f, RarityStat.Type.STUN_DURATION ) );
		}
		if (rollProc( RarityStat.Type.WEAKNESS_PROC )) {
			Buff.prolong( defender, Weakness.class, duration( 4f, RarityStat.Type.WEAKNESS_DURATION ) );
		}
		if (rollProc( RarityStat.Type.SUMMON_LIGHTNING_CHANCE )) {
			defender.damage( Math.max( 1, Math.round( damage * 0.35f ) ), attacker );
		}
		if (defender.isAlive() && rollProc( RarityStat.Type.KNOCKBACK_CHANCE )) {
			knockback( attacker, defender );
		}
		if (defender.isAlive() && rollProc( RarityStat.Type.CLEAVE_CHANCE )) {
			cleave( attacker, defender, damage );
		}
		if (rollProc( RarityStat.Type.PIERCING_CHANCE )) {
			damage += Math.max( 1, Math.round( damage * 0.25f ) );
		}
		if (rollProc( RarityStat.Type.BLESS_PROC )) {
			Buff.prolong( attacker, Bless.class, duration( 4f, RarityStat.Type.BLESS_DURATION ) );
		}
		if (rollProc( RarityStat.Type.HASTE_PROC )) {
			Buff.prolong( attacker, Haste.class, duration( 3f, RarityStat.Type.HASTE_DURATION ) );
		}
		if (rollProc( RarityStat.Type.BARRIER_PROC )) {
			Buff.affect( attacker, Barrier.class ).incShield( Math.max( 1, stat( RarityStat.Type.BARRIER_POWER ) + Math.round( damage * 0.15f ) ) );
		}

		int lifesteal = stat( RarityStat.Type.LIFESTEAL );
		if (lifesteal > 0 && attacker.isAlive()) {
			int healAmt = Math.min( attacker.HT - attacker.HP, Math.max( 1, Math.round( damage * lifesteal / 100f ) ) );
			if (healAmt > 0) {
				attacker.HP += healAmt;
				if (attacker.sprite != null) {
					attacker.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( healAmt ), FloatingText.HEALING );
				}
			}
		}

		return damage;
	}

	public int applyDefenseProcs( Char defender, Char attacker, int damage ) {
		if (defender == null || attacker == null || damage <= 0) return damage;

		if (rollProc( RarityStat.Type.BLOCK_CHANCE, guardBreak( attacker ) )) {
			damage = Math.round( damage * 0.5f );
		}
		if (attacker.isAlive() && rollProc( RarityStat.Type.THORNS_CHANCE )) {
			attacker.damage( Math.max( 1, stat( RarityStat.Type.THORNS_DAMAGE ) ), defender );
		}
		if (rollProc( RarityStat.Type.BARKSKIN_PROC )) {
			Buff.affect( defender, Barkskin.class ).set( Math.max( 1, stat( RarityStat.Type.BARKSKIN_POWER ) ), 1 );
		}
		if (rollProc( RarityStat.Type.BLESS_PROC )) {
			Buff.prolong( defender, Bless.class, duration( 4f, RarityStat.Type.BLESS_DURATION ) );
		}
		if (rollProc( RarityStat.Type.HASTE_PROC )) {
			Buff.prolong( defender, Haste.class, duration( 3f, RarityStat.Type.HASTE_DURATION ) );
		}

		return damage;
	}

	public float resistanceMultiplier( Class effect ) {
		int resistance = 0;

		if (Burning.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.FIRE_RESISTANCE );
		if (Chill.class.isAssignableFrom( effect ) || Frost.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.FROST_RESISTANCE );
		if (Poison.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.POISON_RESISTANCE );
		if (Corrosion.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.CORROSION_RESISTANCE );
		if (Bleeding.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.BLEED_RESISTANCE );
		if (Cripple.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.CRIPPLE_RESISTANCE );
		if (Daze.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.DAZE_RESISTANCE );
		if (Hex.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.HEX_RESISTANCE );
		if (Vulnerable.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.VULNERABLE_RESISTANCE );
		if (Blindness.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.BLINDNESS_RESISTANCE );
		if (Roots.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.ROOT_RESISTANCE );
		if (Slow.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.SLOW_RESISTANCE );
		if (Vertigo.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.VERTIGO_RESISTANCE );
		if (Paralysis.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.STUN_RESISTANCE );
		if (Weakness.class.isAssignableFrom( effect )) resistance += stat( RarityStat.Type.WEAKNESS_RESISTANCE );

		return resistance <= 0 ? 1f : Math.max( 0f, 1f - resistance / 100f );
	}

	public String info() {
		return info( true );
	}

	public String info( boolean includeLevel ) {
		StringBuilder info = new StringBuilder();
		if (includeLevel) info.append( "_Level " ).append( level ).append( "_" );
		appendLine( info, health(), RarityStat.Type.MAX_HEALTH );
		appendLine( info, baselineAttackDamage() + stat( RarityStat.Type.ATTACK_DAMAGE ), RarityStat.Type.ATTACK_DAMAGE );
		appendLine( info, baselineAttackBonus() + stat( RarityStat.Type.ATTACK_BONUS ), RarityStat.Type.ATTACK_BONUS );
		appendLine( info, baselineDefense() + stat( RarityStat.Type.DEFENSE ), RarityStat.Type.DEFENSE );
		for (Map.Entry<RarityStat.Type, Integer> entry : stats.entrySet()) {
			if (entry.getKey() == RarityStat.Type.MAX_HEALTH
					|| entry.getKey() == RarityStat.Type.ATTACK_DAMAGE
					|| entry.getKey() == RarityStat.Type.ATTACK_BONUS
					|| entry.getKey() == RarityStat.Type.DEFENSE) {
				continue;
			}
			appendLine( info, stat( entry.getKey() ), entry.getKey() );
		}
		return info.toString();
	}

	private void knockback( Char attacker, Char defender ) {
		if (attacker.pos == defender.pos) return;
		int power = Math.max( 1, 1 + stat( RarityStat.Type.KNOCKBACK_STRENGTH ) );
		Ballistica trajectory = new Ballistica( attacker.pos, defender.pos, Ballistica.STOP_TARGET );
		if (trajectory.path.size() <= 1) return;
		trajectory = new Ballistica( trajectory.collisionPos, trajectory.path.get( trajectory.path.size() - 1 ), Ballistica.PROJECTILE );
		WandOfBlastWave.throwChar( defender, trajectory, power, true, false, attacker );
	}

	private void cleave( Char attacker, Char defender, int damage ) {
		Char target = null;
		int candidates = 0;
		for (int n : PathFinder.NEIGHBOURS8) {
			Char ch = Actor.findChar( defender.pos + n );
			if (ch == null || ch == attacker || ch == defender || ch.alignment == attacker.alignment) continue;
			candidates++;
			if (Random.Int( candidates ) == 0) {
				target = ch;
			}
		}
		if (target != null) {
			target.damage( Math.max( 1, Math.round( damage * 0.5f ) ), attacker );
		}
	}

	public int stat( RarityStat.Type type ) {
		Integer value = stats.get( type );
		return value == null ? 0 : value;
	}

	private void add( RarityStat.Type type, int value ) {
		if (value <= 0) return;
		stats.put( type, clampStat( (long)stat( type ) + value ) );
	}

	public void setLevel( int level ) {
		this.level = Math.max( 1, level );
	}

	public int level() {
		return level;
	}

	public void improve( int budget ) {
		RarityStat.Type[] focus = randomFocusPool();
		while (budget-- > 0) {
			RarityStat.Type type = Random.Int( 100 ) < 70 ? randomExistingFocusedStat( focus ) : randomBonusStat( focus );
			add( type, rollValue( type ) );
			RarityStat.Type companion = companionStat( type );
			if (companion != null) {
				add( companion, rollValue( companion ) );
			}
		}
	}

	/** Improves only stats this mob already owns. Used by Transcendant elite levels. */
	public void improveExistingOnly( int budget ) {
		if (stats.isEmpty()) return;
		ArrayList<RarityStat.Type> existing = new ArrayList<>( stats.keySet() );
		while (budget-- > 0 && !existing.isEmpty()) {
			RarityStat.Type type = existing.get( Random.Int( existing.size() ) );
			add( type, rollValue( type ) );
		}
	}

	public void ensureStat( RarityStat.Type type, int value ) {
		if (type != null && !stats.containsKey( type )) add( type, value );
	}

	public void improveForDefenderLevel( int defenderLevel, int rarityPower ) {
		setLevel( defenderLevel );
		int statChance = Math.min( 70, DEFENDER_STAT_UPGRADE_BASE_CHANCE + Math.max( 0, rarityPower ) * 5 );
		if (Random.Int( 100 ) < statChance) {
			improve( Math.max( 1, 1 + rarityPower / 2 ) );
		}
		int newStatChance = Math.min( 55, DEFENDER_NEW_STAT_BASE_CHANCE + Math.max( 0, rarityPower ) * 5 );
		if (defenderLevel > 1 && defenderLevel % 5 == 0 && Random.Int( 100 ) < newStatChance) {
			addNewStat();
		}
	}

	private void addNewStat() {
		for (int tries = 0; tries < BONUS_POOL.length * 2; tries++) {
			RarityStat.Type type = randomBonusStat();
			if (!stats.containsKey( type )) {
				add( type, rollValue( type ) );
				RarityStat.Type companion = companionStat( type );
				if (companion != null) {
					add( companion, rollValue( companion ) );
				}
				return;
			}
		}
		improve( 1 );
	}

	private static RarityStat.Type randomBonusStat() {
		int index = Random.chances( BONUS_POOL_WEIGHTS );
		return BONUS_POOL[Math.max( 0, index )];
	}

	private static RarityStat.Type[] randomFocusPool() {
		return FOCUSED_POOLS[Random.Int( FOCUSED_POOLS.length )];
	}

	private RarityStat.Type randomExistingFocusedStat( RarityStat.Type[] focus ) {
		ArrayList<RarityStat.Type> existing = new ArrayList<>();
		for (RarityStat.Type type : focus) {
			if (stats.containsKey( type )) {
				existing.add( type );
			}
		}
		if (existing.isEmpty()) {
			return randomBonusStat( focus );
		}
		return existing.get( Random.Int( existing.size() ) );
	}

	private static RarityStat.Type randomBonusStat( RarityStat.Type[] pool ) {
		float[] weights = new float[pool.length];
		for (int i = 0; i < pool.length; i++) {
			weights[i] = weightFor( pool[i] );
		}
		int index = Random.chances( weights );
		return pool[Math.max( 0, index )];
	}

	private static float weightFor( RarityStat.Type type ) {
		for (int i = 0; i < BONUS_POOL.length; i++) {
			if (BONUS_POOL[i] == type) {
				return BONUS_POOL_WEIGHTS[i];
			}
		}
		return 1f;
	}

	private void rollLevelChanceStats() {
		for (int growthLevel = 1; growthLevel <= level; growthLevel++) {
			add( RarityStat.Type.MAX_HEALTH, rollValue( RarityStat.Type.MAX_HEALTH, growthLevel ) );

			if (Random.Int( 100 ) < 70) {
				add( RarityStat.Type.MOVEMENT_SPEED, rollSpeedValue( growthLevel ) );
			}
			if (Random.Int( 100 ) < 70) {
				add( RarityStat.Type.ATTACK_ACCURACY,
						rollValue( RarityStat.Type.ATTACK_ACCURACY, growthLevel ) );
			}
			if (Random.Int( 100 ) < 50) {
				add( RarityStat.Type.ATTACK_DAMAGE,
						rollValue( RarityStat.Type.ATTACK_DAMAGE, growthLevel ) );
			}
			if (Random.Int( 100 ) < 35) {
				add( RarityStat.Type.DEFENSE, rollArmorValue( growthLevel ) );
			}
			if (Random.Int( 100 ) < 25) {
				add( RarityStat.Type.ATTACK_BONUS,
						rollValue( RarityStat.Type.ATTACK_BONUS, growthLevel ) );
			}
			if (Random.Int( 100 ) < 30) {
				add( RarityStat.Type.GUARD_BREAK,
						rollValue( RarityStat.Type.GUARD_BREAK, growthLevel ) );
			}
		}
	}

	private int baselineHealth() {
		return scaledBaseline( 2L + (long)level * 3L );
	}

	private int baselineAttackDamage() {
		return Math.max( 1, scaledBaseline( ((long)level + 1L) / 2L ) );
	}

	private int baselineAttackBonus() {
		return Math.max( 0, scaledBaseline( (long)level * 2L ) );
	}

	private int baselineDefense() {
		return scaledBaseline( Math.round( level * Math.sqrt( level ) * 3d / 8d ) );
	}

	private int scaledBaseline( long value ) {
		return clampStat( Math.round( value * (double)baselineScale ) );
	}

	private static int clampStat( long value ) {
		if (value <= 0) return 0;
		return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)value;
	}

	private int rollArmorValue( int valueLevel ) {
		return Random.IntRange( 1, 2 );
	}

	private int rollSpeedValue( int valueLevel ) {
		return Random.IntRange( 5, 10 ) + valueLevel / 10;
	}

	private boolean rollProc( RarityStat.Type type ) {
		return rollProc( type, 0 );
	}

	private boolean rollProc( RarityStat.Type type, int counter ) {
		int chance = Math.max( 0, stat( type ) - Math.max( 0, counter ) );
		return chance > 0 && Random.Int( 100 ) < chance;
	}

	private int guardBreak( Char attacker ) {
		if (attacker instanceof Hero) {
			return ((Hero)attacker).belongings.equippedRarityStat( RarityStat.Type.GUARD_BREAK );
		}
		if (attacker instanceof Mob) {
			return ((Mob)attacker).rarityStat( RarityStat.Type.GUARD_BREAK );
		}
		return 0;
	}

	private float duration( RarityStat.Type type ) {
		return 1f + stat( type ) / 4f;
	}

	private float duration( float base, RarityStat.Type type ) {
		return base + stat( type );
	}

	private void appendLine( StringBuilder info, int value, RarityStat.Type type ) {
		if (value <= 0) return;
		info.append( "\n+" ).append( value );
		if (type.percent()) info.append( "%" );
		info.append( " " ).append( coloredName( type ) );
	}

	private String coloredName( RarityStat.Type type ) {
		return "@@C" + String.format( "%06X", type.displayColor() & 0xFFFFFF ) + "@@"
				+ type.displayName() + "@@CEND@@";
	}

	private int rollValue( RarityStat.Type type ) {
		return rollValue( type, level );
	}

	private int rollValue( RarityStat.Type type, int valueLevel ) {
		switch (type) {
			case MAX_HEALTH:
				return Random.IntRange( 2, 5 ) + valueLevel / 2;
			case ATTACK_DAMAGE:
			case DEFENSE:
			case BARKSKIN_POWER:
			case BARRIER_POWER:
			case THORNS_DAMAGE:
			case KNOCKBACK_STRENGTH:
				return Random.IntRange( 1, 2 ) + valueLevel / 8;
			case CRITICAL_DAMAGE_MULTIPLIER:
				return Random.IntRange( 15, 30 );
			case ATTACK_BONUS:
			case ATTACK_ACCURACY:
			case ARMOR_BONUS:
			case DODGE_CHANCE:
			case GUARD_BREAK:
			case LIFESTEAL:
				return Random.IntRange( 3, 7 );
			case ATTACK_SPEED:
				return Random.IntRange( 1, 2 );
			case EVASION:
				return Random.IntRange( 1, 3 ) + valueLevel / 10;
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case BLEED_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case DAZE_RESISTANCE:
			case HEX_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case BLINDNESS_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
				return Random.IntRange( 5, 10 );
			case BLEED_DURATION:
			case BURNING_DURATION:
			case POISON_DURATION:
			case CORROSION_DURATION:
			case FROST_DURATION:
			case CRIPPLE_DURATION:
			case DAZE_DURATION:
			case HEX_DURATION:
			case VULNERABLE_DURATION:
			case BLINDNESS_DURATION:
			case ROOT_DURATION:
			case SLOW_DURATION:
			case VERTIGO_DURATION:
			case STUN_DURATION:
			case WEAKNESS_DURATION:
			case BLESS_DURATION:
			case HASTE_DURATION:
				return Random.IntRange( 1, 2 );
			default:
				return Random.IntRange( 3, 6 );
		}
	}

	private RarityStat.Type companionStat( RarityStat.Type type ) {
		switch (type) {
			case BARKSKIN_PROC:
				return RarityStat.Type.BARKSKIN_POWER;
			case BARRIER_PROC:
				return RarityStat.Type.BARRIER_POWER;
			case THORNS_CHANCE:
				return RarityStat.Type.THORNS_DAMAGE;
			case CRITICAL_CHANCE:
				return RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER;
			case BLEED_PROC:
				return RarityStat.Type.BLEED_DURATION;
			case BURNING_PROC:
				return RarityStat.Type.BURNING_DURATION;
			case POISON_PROC:
				return RarityStat.Type.POISON_DURATION;
			case CORROSION_PROC:
				return RarityStat.Type.CORROSION_DURATION;
			case FROST_PROC:
				return RarityStat.Type.FROST_DURATION;
			case CRIPPLE_PROC:
				return RarityStat.Type.CRIPPLE_DURATION;
			case DAZE_PROC:
				return RarityStat.Type.DAZE_DURATION;
			case HEX_PROC:
				return RarityStat.Type.HEX_DURATION;
			case VULNERABLE_PROC:
				return RarityStat.Type.VULNERABLE_DURATION;
			case BLINDNESS_PROC:
				return RarityStat.Type.BLINDNESS_DURATION;
			case ROOT_PROC:
				return RarityStat.Type.ROOT_DURATION;
			case SLOW_PROC:
				return RarityStat.Type.SLOW_DURATION;
			case VERTIGO_PROC:
				return RarityStat.Type.VERTIGO_DURATION;
			case STUN_CHANCE:
				return RarityStat.Type.STUN_DURATION;
			case WEAKNESS_PROC:
				return RarityStat.Type.WEAKNESS_DURATION;
			case KNOCKBACK_CHANCE:
				return RarityStat.Type.KNOCKBACK_STRENGTH;
			case BLESS_PROC:
				return RarityStat.Type.BLESS_DURATION;
			case HASTE_PROC:
				return RarityStat.Type.HASTE_DURATION;
			default:
				return null;
		}
	}

	private static final String STAT_BALANCE_VERSION = "stat_balance_version";
	private static final int CURRENT_STAT_BALANCE_VERSION = 3;

	static int rebalanceLegacyAttackSpeed( int oldValue ) {
		return oldValue <= 0 ? 0 : Math.max( 1, Math.round( (float)Math.sqrt( oldValue ) ) );
	}

	static int rebalanceLegacyDefense( int oldValue, int level ) {
		if (oldValue <= 0) return 0;
		int linearAllowance = Math.max(4, Math.round(level / 3f));
		if (oldValue <= linearAllowance) return oldValue;
		return linearAllowance + Math.max(1,
				Math.round((float)Math.sqrt(oldValue - linearAllowance)));
	}

	static int rebalanceLegacyArmorBonus( int oldValue ) {
		if (oldValue <= 0) return 0;
		return Math.min(oldValue, Math.max(1,
				Math.round((float)Math.sqrt(oldValue))));
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( STAT_BALANCE_VERSION, CURRENT_STAT_BALANCE_VERSION );
		bundle.put( LEVEL, level );
		bundle.put( BASELINE_SCALE, baselineScale );
		bundle.put( BOSS_SCALE_APPLIED, bossScaleApplied );
		String[] entries = new String[stats.size()];
		int i = 0;
		for (Map.Entry<RarityStat.Type, Integer> entry : stats.entrySet()) {
			entries[i++] = entry.getKey().name() + ":" + entry.getValue();
		}
		bundle.put( STATS, entries );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		level = Math.max( 1, bundle.getInt( LEVEL ) );
		baselineScale = bundle.contains( BASELINE_SCALE ) ? bundle.getFloat( BASELINE_SCALE ) : 1f;
		bossScaleApplied = bundle.getBoolean( BOSS_SCALE_APPLIED );
		int statBalanceVersion = bundle.contains( STAT_BALANCE_VERSION )
				? bundle.getInt( STAT_BALANCE_VERSION ) : 1;
		stats.clear();
		if (bundle.contains( STATS )) {
			for (String entry : bundle.getStringArray( STATS )) {
				String[] parts = entry.split( ":" );
				if (parts.length < 2) continue;
				try {
					RarityStat.Type type = RarityStat.Type.valueOf( parts[0] );
					int value = Integer.parseInt( parts[1] );
					if (type == RarityStat.Type.ATTACK_SPEED && statBalanceVersion < 2) {
						value = rebalanceLegacyAttackSpeed( value );
					}
					if (statBalanceVersion < 3) {
						if (type == RarityStat.Type.DEFENSE) {
							value = rebalanceLegacyDefense(value, level);
						} else if (type == RarityStat.Type.ARMOR_BONUS) {
							value = rebalanceLegacyArmorBonus(value);
						}
					}
					add( type, value );
				} catch (IllegalArgumentException ignored) {
					// Ignore stale or malformed stat saves.
				}
			}
		} else {
			add( RarityStat.Type.MAX_HEALTH, bundle.getInt( OLD_HEALTH ) );
			add( RarityStat.Type.ATTACK_DAMAGE, bundle.getInt( OLD_DAMAGE ) );
			add( RarityStat.Type.ATTACK_ACCURACY, bundle.getInt( OLD_ACCURACY ) );
			add( RarityStat.Type.DODGE_CHANCE, bundle.getInt( OLD_EVASION ) );
			add( RarityStat.Type.DEFENSE, bundle.getInt( OLD_ARMOR ) );
			add( RarityStat.Type.MOVEMENT_SPEED, bundle.getInt( OLD_SPEED ) );
		}
	}
}
