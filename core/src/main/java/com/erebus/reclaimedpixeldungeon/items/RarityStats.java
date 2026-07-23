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

package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.trinkets.Trinket;
import com.erebus.reclaimedpixeldungeon.items.wands.DamageWand;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Random;

import java.util.ArrayList;

final class RarityStats {

	private static final float[] RARITY_CHANCES = new float[]{
	 		55f, 28f, 12f, 4f, 0.9f, 0.1f
	};
	// 	private static final float[] RARITY_CHANCES = new float[]{
	//         0f, 0f, 0f, 0f, 0f, 100f
	// };

	private static final float[][] STAT_COUNT_CHANCES = new float[][]{
			{50000f, 25000f, 10000f, 5000f, 2500f, 1500f, 700f, 300f},
			{20000f, 35000f, 25000f, 10000f, 5000f, 3500f, 1500f, 1000f},
			{0f, 30000f, 20000f, 17500f, 15000f, 12500f, 3000f, 2000f},
			{0f, 0f, 20000f, 25000f, 25000f, 20000f, 8000f, 2000f},
			{0f, 0f, 0f, 10000f, 25000f, 25000f, 25000f, 15000f},
			{0f, 0f, 0f, 0f, 100000f, 0f, 0f, 0f}
	};

	private RarityStats() {
	}

	static Item roll( Item item ) {
		if (item == null || item.hasRarityRoll()) return item;
		return reroll( item );
	}

	static Item reroll( Item item ) {
		if (item == null) return null;

		ArrayList<RarityStat.Type> statPool = statPool( item );
		if (statPool.isEmpty()) return item;

		ItemRarity rarity = rollRarity();
		int statCount = Math.min( rarity.statSlots(), rollStatCount( rarity ) );
		ArrayList<RarityStat> stats = rollStats( item, rarity, statCount, new ArrayList<RarityStat>(), null );

		item.setRarityStats( rarity, stats );
		return item;
	}

	static ItemRarity rollRarity() {
		int rarityIndex = Random.chances( RARITY_CHANCES );
		if (rarityIndex < 0) rarityIndex = 0;
		return ItemRarity.values()[rarityIndex];
	}

	static boolean hasStatPool( Item item ) {
		return item != null && !statPool( item ).isEmpty();
	}

	static boolean typeAvailable( Item item, ItemRarity rarity, RarityStat.Type type ) {
		return type != null && statPool( item ).contains( type ) && type.allowedFor( rarity );
	}

	static ArrayList<RarityStat> rollStats( Item item, ItemRarity rarity, int statCount, ArrayList<RarityStat> selectedStats, RarityStat.Type excludedType ) {
		ArrayList<RarityStat> stats = new ArrayList<>();
		if (selectedStats != null) {
			for (RarityStat stat : selectedStats) {
				if (stat != null) stats.add( stat.copy() );
			}
		}

		while (stats.size() < statCount) {
			RarityStat stat = rollStat( item, rarity, stats, excludedType );
			if (stat == null) {
				stats.add( new RarityStat( RarityStat.Type.EMPTY_SLOT, 0 ) );
			} else {
				stats.add( stat );
			}
		}

		return stats;
	}

	static RarityStat rollStat( Item item, ItemRarity rarity, ArrayList<RarityStat> selectedStats, RarityStat.Type excludedType ) {
		ArrayList<RarityStat.Type> eligible = allowedStatPool( statPool( item ), rarity, selectedStats );
		if (excludedType != null) eligible.remove( excludedType );
		if (eligible.isEmpty()) return null;

		RarityStat.Type type = eligible.get( Random.Int( eligible.size() ) );
		return new RarityStat( type, rollValue( type, rarity ) );
	}

	private static int rollStatCount( ItemRarity rarity ) {
		int rarityIndex = rarity.ordinal();
		if (rarityIndex < 0 || rarityIndex >= STAT_COUNT_CHANCES.length) return 1;
		int statCount = Random.chances( STAT_COUNT_CHANCES[rarityIndex] );
		return Math.max( 1, statCount );
	}

	private static ArrayList<RarityStat.Type> allowedStatPool( ArrayList<RarityStat.Type> rawPool, ItemRarity rarity, ArrayList<RarityStat> selectedStats ) {
		ArrayList<RarityStat.Type> statPool = new ArrayList<>();
		for (RarityStat.Type type : rawPool) {
			if (!type.allowedFor( rarity )) continue;
			if (type.unique() && containsStat( selectedStats, type )) continue;
			if (!hasRequiredStats( selectedStats, type )) continue;
			statPool.add( type );
		}
		return statPool;
	}

	private static boolean containsStat( ArrayList<RarityStat> stats, RarityStat.Type type ) {
		for (RarityStat stat : stats) {
			if (stat.type() == type) return true;
		}
		return false;
	}

	private static boolean hasRequiredStats( ArrayList<RarityStat> selectedStats, RarityStat.Type type ) {
		for (RarityStat.Type required : type.requires()) {
			if (!containsStat( selectedStats, required )) return false;
		}
		return true;
	}

	static ArrayList<RarityStat.Type> statPool( Item item ) {
		ArrayList<RarityStat.Type> statPool = new ArrayList<>();

		if (item instanceof Weapon) {
			statPool.add( RarityStat.Type.ATTACK_DAMAGE );
			statPool.add( RarityStat.Type.ATTACK_BONUS );
			statPool.add( RarityStat.Type.ATTACK_ACCURACY );
			statPool.add( RarityStat.Type.ATTACK_SPEED );
			statPool.add( RarityStat.Type.BARRIER_PROC );
			statPool.add( RarityStat.Type.BARRIER_POWER );
			statPool.add( RarityStat.Type.BLESS_PROC );
			statPool.add( RarityStat.Type.BLESS_DURATION );
			statPool.add( RarityStat.Type.BLINDNESS_PROC );
			statPool.add( RarityStat.Type.BLINDNESS_DURATION );
			statPool.add( RarityStat.Type.CRITICAL_CHANCE );
			statPool.add( RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER );
			statPool.add( RarityStat.Type.BLEED_PROC );
			statPool.add( RarityStat.Type.BLEED_DURATION );
			statPool.add( RarityStat.Type.BURNING_PROC );
			statPool.add( RarityStat.Type.BURNING_DURATION );
			statPool.add( RarityStat.Type.CLEAVE_CHANCE );
			statPool.add( RarityStat.Type.CORROSION_PROC );
			statPool.add( RarityStat.Type.CORROSION_DURATION );
			statPool.add( RarityStat.Type.CRIPPLE_PROC );
			statPool.add( RarityStat.Type.CRIPPLE_DURATION );
			statPool.add( RarityStat.Type.DAZE_PROC );
			statPool.add( RarityStat.Type.DAZE_DURATION );
			statPool.add( RarityStat.Type.FROST_PROC );
			statPool.add( RarityStat.Type.FROST_DURATION );
			statPool.add( RarityStat.Type.HASTE_PROC );
			statPool.add( RarityStat.Type.HASTE_DURATION );
			statPool.add( RarityStat.Type.HEX_PROC );
			statPool.add( RarityStat.Type.HEX_DURATION );
			statPool.add( RarityStat.Type.KNOCKBACK_CHANCE );
			statPool.add( RarityStat.Type.KNOCKBACK_STRENGTH );
			statPool.add( RarityStat.Type.LIFESTEAL );
			statPool.add( RarityStat.Type.PIERCING_CHANCE );
			statPool.add( RarityStat.Type.POISON_PROC );
			statPool.add( RarityStat.Type.POISON_DURATION );
			statPool.add( RarityStat.Type.RECHARGING_PROC );
			statPool.add( RarityStat.Type.RECHARGING_DURATION );
			statPool.add( RarityStat.Type.ROOT_PROC );
			statPool.add( RarityStat.Type.ROOT_DURATION );
			statPool.add( RarityStat.Type.SLOW_PROC );
			statPool.add( RarityStat.Type.SLOW_DURATION );
			statPool.add( RarityStat.Type.SOUL_REAPING );
			statPool.add( RarityStat.Type.STUN_CHANCE );
			statPool.add( RarityStat.Type.STUN_DURATION );
			statPool.add( RarityStat.Type.SUMMON_LIGHTNING_CHANCE );
			statPool.add( RarityStat.Type.VERTIGO_PROC );
			statPool.add( RarityStat.Type.VERTIGO_DURATION );
			statPool.add( RarityStat.Type.VULNERABLE_PROC );
			statPool.add( RarityStat.Type.VULNERABLE_DURATION );
			statPool.add( RarityStat.Type.WEAKNESS_PROC );
			statPool.add( RarityStat.Type.WEAKNESS_DURATION );
			statPool.add( RarityStat.Type.CRIMSON_ECHO );
			statPool.add( RarityStat.Type.GLACIAL_REND );
			statPool.add( RarityStat.Type.STATIC_RUIN );
			statPool.add( RarityStat.Type.SPIRITBREAK );
			statPool.add( RarityStat.Type.FATAL_SYNCHRONICITY );
			statPool.add( RarityStat.Type.SOULBOUND );
			if (item instanceof MissileWeapon) {
				statPool.add( RarityStat.Type.THROWN_DURABILITY );
			}
		} else if (item instanceof Armor) {
			statPool.add( RarityStat.Type.DEFENSE );
			statPool.add( RarityStat.Type.ARMOR_BONUS );
			statPool.add( RarityStat.Type.BARKSKIN_PROC );
			statPool.add( RarityStat.Type.BARKSKIN_POWER );
			statPool.add( RarityStat.Type.BARRIER_PROC );
			statPool.add( RarityStat.Type.BARRIER_POWER );
			statPool.add( RarityStat.Type.BLESS_PROC );
			statPool.add( RarityStat.Type.BLESS_DURATION );
			statPool.add( RarityStat.Type.BLINDNESS_RESISTANCE );
			statPool.add( RarityStat.Type.BLEED_RESISTANCE );
			statPool.add( RarityStat.Type.BLOCK_CHANCE );
			statPool.add( RarityStat.Type.CORROSION_RESISTANCE );
			statPool.add( RarityStat.Type.CRIPPLE_RESISTANCE );
			statPool.add( RarityStat.Type.CRITICAL_HIT_RESISTANCE );
			statPool.add( RarityStat.Type.CRITICAL_DAMAGE_REDUCTION );
			statPool.add( RarityStat.Type.DAZE_RESISTANCE );
			statPool.add( RarityStat.Type.DODGE_CHANCE );
			statPool.add( RarityStat.Type.EVASION );
			statPool.add( RarityStat.Type.FEATHER_FALLING );
			statPool.add( RarityStat.Type.FIRE_RESISTANCE );
			statPool.add( RarityStat.Type.FROST_RESISTANCE );
			statPool.add( RarityStat.Type.HASTE_PROC );
			statPool.add( RarityStat.Type.HASTE_DURATION );
			statPool.add( RarityStat.Type.HEX_RESISTANCE );
			statPool.add( RarityStat.Type.MAX_HEALTH );
			statPool.add( RarityStat.Type.MOVEMENT_SPEED );
			statPool.add( RarityStat.Type.POISON_RESISTANCE );
			statPool.add( RarityStat.Type.RECHARGING_PROC );
			statPool.add( RarityStat.Type.RECHARGING_DURATION );
			statPool.add( RarityStat.Type.ROOT_RESISTANCE );
			statPool.add( RarityStat.Type.SLOW_RESISTANCE );
			statPool.add( RarityStat.Type.STUN_RESISTANCE );
			statPool.add( RarityStat.Type.SURVIVOR );
			statPool.add( RarityStat.Type.THORNS_CHANCE );
			statPool.add( RarityStat.Type.THORNS_DAMAGE );
			statPool.add( RarityStat.Type.VERTIGO_RESISTANCE );
			statPool.add( RarityStat.Type.VULNERABLE_RESISTANCE );
			statPool.add( RarityStat.Type.WEAKNESS_RESISTANCE );
			statPool.add( RarityStat.Type.SOULBOUND );
		} else if (item instanceof Wand) {
			statPool.add( RarityStat.Type.WAND_RECHARGE_RATE );
			statPool.add( RarityStat.Type.WAND_CHARGES );
			if (item instanceof DamageWand) {
				statPool.add( RarityStat.Type.MAGIC_DAMAGE );
				statPool.add( RarityStat.Type.MAGIC_BONUS );
				statPool.add( RarityStat.Type.CRITICAL_CHANCE );
				statPool.add( RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER );
			}
			statPool.add( RarityStat.Type.SOULBOUND );
		} else if (item instanceof Ring) {
			statPool.add( RarityStat.Type.RING_POTENCY );
			statPool.add( RarityStat.Type.BLINDNESS_RESISTANCE );
			statPool.add( RarityStat.Type.BLEED_RESISTANCE );
			statPool.add( RarityStat.Type.BLOCK_CHANCE );
			statPool.add( RarityStat.Type.CORROSION_RESISTANCE );
			statPool.add( RarityStat.Type.CRIPPLE_RESISTANCE );
			statPool.add( RarityStat.Type.DAZE_RESISTANCE );
			statPool.add( RarityStat.Type.DODGE_CHANCE );
			statPool.add( RarityStat.Type.FIRE_RESISTANCE );
			statPool.add( RarityStat.Type.FROST_RESISTANCE );
			statPool.add( RarityStat.Type.HEX_RESISTANCE );
			statPool.add( RarityStat.Type.MAX_HEALTH );
			statPool.add( RarityStat.Type.MOVEMENT_SPEED );
			statPool.add( RarityStat.Type.POISON_RESISTANCE );
			statPool.add( RarityStat.Type.RESOURCEFUL );
			statPool.add( RarityStat.Type.ROOT_RESISTANCE );
			statPool.add( RarityStat.Type.SLOW_RESISTANCE );
			statPool.add( RarityStat.Type.TREASURE_LUCK );
			statPool.add( RarityStat.Type.VERTIGO_RESISTANCE );
			statPool.add( RarityStat.Type.VULNERABLE_RESISTANCE );
			statPool.add( RarityStat.Type.WEAKNESS_RESISTANCE );
			statPool.add( RarityStat.Type.XP_GAIN );
			statPool.add( RarityStat.Type.SOULBOUND );
		} else if (item instanceof Trinket) {
			statPool.add( RarityStat.Type.TRINKET_POTENCY );
			statPool.add( RarityStat.Type.BLINDNESS_RESISTANCE );
			statPool.add( RarityStat.Type.BLEED_RESISTANCE );
			statPool.add( RarityStat.Type.BONUS_LOOT );
			statPool.add( RarityStat.Type.CORROSION_RESISTANCE );
			statPool.add( RarityStat.Type.CRIPPLE_RESISTANCE );
			statPool.add( RarityStat.Type.DAZE_RESISTANCE );
			statPool.add( RarityStat.Type.DODGE_CHANCE );
			statPool.add( RarityStat.Type.FIRE_RESISTANCE );
			statPool.add( RarityStat.Type.FROST_RESISTANCE );
			statPool.add( RarityStat.Type.HEX_RESISTANCE );
			statPool.add( RarityStat.Type.MAX_HEALTH );
			statPool.add( RarityStat.Type.MOVEMENT_SPEED );
			statPool.add( RarityStat.Type.POISON_RESISTANCE );
			statPool.add( RarityStat.Type.RESOURCEFUL );
			statPool.add( RarityStat.Type.ROOT_RESISTANCE );
			statPool.add( RarityStat.Type.SLOW_RESISTANCE );
			statPool.add( RarityStat.Type.TREASURE_LUCK );
			statPool.add( RarityStat.Type.VERTIGO_RESISTANCE );
			statPool.add( RarityStat.Type.VULNERABLE_RESISTANCE );
			statPool.add( RarityStat.Type.WEAKNESS_RESISTANCE );
			statPool.add( RarityStat.Type.XP_GAIN );
			statPool.add( RarityStat.Type.SOULBOUND );
		} else if (item instanceof Artifact) {
			statPool.add( RarityStat.Type.ARTIFACT_POTENCY );
			statPool.add( RarityStat.Type.BLINDNESS_RESISTANCE );
			statPool.add( RarityStat.Type.BLEED_RESISTANCE );
			statPool.add( RarityStat.Type.BLOCK_CHANCE );
			statPool.add( RarityStat.Type.BONUS_LOOT );
			statPool.add( RarityStat.Type.CORROSION_RESISTANCE );
			statPool.add( RarityStat.Type.CRIPPLE_RESISTANCE );
			statPool.add( RarityStat.Type.DAZE_RESISTANCE );
			statPool.add( RarityStat.Type.DODGE_CHANCE );
			statPool.add( RarityStat.Type.FIRE_RESISTANCE );
			statPool.add( RarityStat.Type.FROST_RESISTANCE );
			statPool.add( RarityStat.Type.HEX_RESISTANCE );
			statPool.add( RarityStat.Type.MAX_HEALTH );
			statPool.add( RarityStat.Type.MOVEMENT_SPEED );
			statPool.add( RarityStat.Type.POISON_RESISTANCE );
			statPool.add( RarityStat.Type.RESOURCEFUL );
			statPool.add( RarityStat.Type.ROOT_RESISTANCE );
			statPool.add( RarityStat.Type.SLOW_RESISTANCE );
			statPool.add( RarityStat.Type.SOUL_REAPING );
			statPool.add( RarityStat.Type.TREASURE_LUCK );
			statPool.add( RarityStat.Type.VERTIGO_RESISTANCE );
			statPool.add( RarityStat.Type.VULNERABLE_RESISTANCE );
			statPool.add( RarityStat.Type.WEAKNESS_RESISTANCE );
			statPool.add( RarityStat.Type.XP_GAIN );
			statPool.add( RarityStat.Type.SOULBOUND );
		}

		return statPool;
	}

	static int upgradeValue( RarityStat.Type type, ItemRarity rarity ) {
		if (!type.hasValue()) return 0;
		int power = Math.max( 1, rarity.power() );
		if (type.percent()) return 1 + Math.max( 0, power / 2 );

		switch (type) {
			case MAX_HEALTH:
				return 2 + power / 2;
			case WAND_CHARGES:
			case RING_POTENCY:
			case TRINKET_POTENCY:
			case ARTIFACT_POTENCY:
			case BARKSKIN_POWER:
			case BARRIER_POWER:
				return 1;
			default:
				return 1;
		}
	}

	static int rollValue( RarityStat.Type type, ItemRarity rarity ) {
		int power = Math.max( 1, rarity.power() );

		switch (type) {
			case ARMOR_BONUS:
			case ATTACK_BONUS:
			case MAGIC_BONUS:
				return Random.IntRange( 5 + power * 3, 8 + power * 5 );
			case ATTACK_ACCURACY:
			case ATTACK_SPEED:
			case MOVEMENT_SPEED:
			case RESOURCEFUL:
			case THROWN_DURABILITY:
			case WAND_RECHARGE_RATE:
				return Random.IntRange( 3 + power * 2, 5 + power * 3 );
			case BLESS_DURATION:
			case BLINDNESS_DURATION:
			case BLEED_DURATION:
			case BURNING_DURATION:
			case CORROSION_DURATION:
			case CRIPPLE_DURATION:
			case DAZE_DURATION:
			case FROST_DURATION:
			case HASTE_DURATION:
			case HEX_DURATION:
			case POISON_DURATION:
			case RECHARGING_DURATION:
			case ROOT_DURATION:
			case SLOW_DURATION:
			case STUN_DURATION:
			case VERTIGO_DURATION:
			case VULNERABLE_DURATION:
			case WEAKNESS_DURATION:
				return Random.IntRange( 1, 1 + Math.max( 1, power / 2 ) );
			case BLINDNESS_RESISTANCE:
			case BLEED_RESISTANCE:
			case CORROSION_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case CRITICAL_DAMAGE_REDUCTION:
			case CRITICAL_HIT_RESISTANCE:
			case DAZE_RESISTANCE:
			case FEATHER_FALLING:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case HEX_RESISTANCE:
			case POISON_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case STUN_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case WEAKNESS_RESISTANCE:
				return Random.IntRange( 8 + power * 4, 12 + power * 6 );
			case BARKSKIN_PROC:
			case BARRIER_PROC:
			case BLESS_PROC:
			case BLINDNESS_PROC:
			case BLEED_PROC:
			case BLOCK_CHANCE:
			case BURNING_PROC:
			case CLEAVE_CHANCE:
			case CORROSION_PROC:
			case CRITICAL_CHANCE:
			case CRIPPLE_PROC:
			case DAZE_PROC:
			case DODGE_CHANCE:
			case FROST_PROC:
			case HASTE_PROC:
			case HEX_PROC:
			case KNOCKBACK_CHANCE:
			case LIFESTEAL:
			case PIERCING_CHANCE:
			case POISON_PROC:
			case RECHARGING_PROC:
			case ROOT_PROC:
			case SLOW_PROC:
			case SOUL_REAPING:
			case SURVIVOR:
			case STUN_CHANCE:
			case SUMMON_LIGHTNING_CHANCE:
			case THORNS_CHANCE:
			case VERTIGO_PROC:
			case VULNERABLE_PROC:
			case WEAKNESS_PROC:
				return Random.IntRange( 3 + power * 2, 5 + power * 3 );
			case CRITICAL_DAMAGE_MULTIPLIER:
				return Random.IntRange( 20 + power * 10, 30 + power * 15 );
			case EVASION:
				return Random.IntRange( 1, 1 + power );
			case KNOCKBACK_STRENGTH:
				return Random.IntRange( 1, 1 + Math.max( 1, power / 2 ) );
			case BARKSKIN_POWER:
			case BARRIER_POWER:
				return Random.IntRange( 1 + power, 2 + power * 2 );
			case MAX_HEALTH:
				return Random.IntRange( 2 + power * 2, 4 + power * 4 );
			case RING_POTENCY:
			case TRINKET_POTENCY:
			case ARTIFACT_POTENCY:
				return Random.IntRange( 1, Math.max( 1, (power + 1) / 2 ) );
			case WAND_CHARGES:
				return Random.IntRange( 1, Math.max( 1, power / 2 ) );
			case BONUS_LOOT:
			case TREASURE_LUCK:
			case XP_GAIN:
				return Random.IntRange( 4 + power * 3, 8 + power * 5 );
			case THORNS_DAMAGE:
				return Random.IntRange( power, power * 3 );
			case ATTACK_DAMAGE:
			case DEFENSE:
			case MAGIC_DAMAGE:
			default:
				return type.hasValue() ? Random.IntRange( power, power * 2 ) : 1;
		}
	}
}
