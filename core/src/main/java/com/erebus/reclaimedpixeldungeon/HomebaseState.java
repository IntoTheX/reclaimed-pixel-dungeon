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

package com.erebus.reclaimedpixeldungeon;

import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.MobStats;
import com.erebus.reclaimedpixeldungeon.items.EnergyCrystal;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Gold;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.food.Blandfruit;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.scrolls.Scroll;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.erebus.reclaimedpixeldungeon.items.stones.RarityCatalystStone;
import com.erebus.reclaimedpixeldungeon.items.stones.Runestone;
import com.erebus.reclaimedpixeldungeon.items.trinkets.Trinket;
import com.erebus.reclaimedpixeldungeon.items.trinkets.TrinketCatalyst;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.plants.BlandfruitBush;
import com.erebus.reclaimedpixeldungeon.plants.Blindweed;
import com.erebus.reclaimedpixeldungeon.plants.Earthroot;
import com.erebus.reclaimedpixeldungeon.plants.Fadeleaf;
import com.erebus.reclaimedpixeldungeon.plants.Firebloom;
import com.erebus.reclaimedpixeldungeon.plants.Icecap;
import com.erebus.reclaimedpixeldungeon.plants.Mageroyal;
import com.erebus.reclaimedpixeldungeon.plants.Plant;
import com.erebus.reclaimedpixeldungeon.plants.Sorrowmoss;
import com.erebus.reclaimedpixeldungeon.plants.Starflower;
import com.erebus.reclaimedpixeldungeon.plants.Stormvine;
import com.erebus.reclaimedpixeldungeon.plants.Sungrass;
import com.erebus.reclaimedpixeldungeon.plants.Swiftthistle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class HomebaseState implements Bundlable {

	private static final int MAX_BUILDING_LEVEL = 100;
	private static final boolean INFINITE_TEST_RESOURCES = false;
	private static final boolean HOMEBASE_NPC_TEST_ITEMS = false;
	private static final int TEST_RESOURCE_AMOUNT = 999999;
	private static final int RAID_THREAT_PER_MOB = 35;
	private static final int RAID_MOBS_PER_WAVE = 8;
	private static final String RAID_MOB_PREFIX = "com.erebus.reclaimedpixeldungeon.actors.mobs.";
	private static final int BASE_BUILDING_HP = 500;

	public enum Material {
		WOOD,
		STONE,
		COPPER,
		IRON,
		GOLD
	}

	public enum ForgeResource {
		SCRAP( "scrap" ),
		EMBER_SHARD( "ember shards" ),
		EMBER_CORE( "ember cores" );

		private final String label;

		ForgeResource( String label ) {
			this.label = label;
		}

		public String label() {
			return label;
		}
	}

	public enum Building {
		CAMP( new int[][]{
				{4, 2, 0, 0, 0},
				{10, 6, 2, 0, 0},
				{16, 12, 5, 2, 0},
				{24, 20, 8, 5, 1}
		} ),
		VAULT( new int[][]{
				{6, 8, 0, 0, 0},
				{12, 16, 2, 0, 0},
				{18, 26, 6, 3, 0},
				{28, 36, 12, 8, 2}
		} ),
		FORGE( new int[][]{
				{4, 6, 4, 0, 0},
				{8, 12, 10, 3, 0},
				{10, 18, 16, 8, 1},
				{16, 24, 24, 14, 4}
		} ),
		ALCHEMY( new int[][]{
				{6, 4, 2, 0, 0},
				{10, 8, 6, 1, 0},
				{14, 12, 12, 4, 1},
				{20, 18, 18, 8, 3}
		} ),
		GARDEN( new int[][]{
				{8, 3, 0, 0, 0},
				{14, 6, 2, 0, 0},
				{22, 10, 5, 1, 0},
				{32, 14, 8, 3, 1}
		} ),
		NORTH_WALL( new int[][]{
				{5, 10, 0, 0, 0},
				{8, 18, 2, 0, 0},
				{12, 28, 5, 2, 0},
				{18, 42, 9, 5, 1}
		} ),
		EAST_WALL( new int[][]{
				{5, 10, 0, 0, 0},
				{8, 18, 2, 0, 0},
				{12, 28, 5, 2, 0},
				{18, 42, 9, 5, 1}
		} ),
		SOUTH_WALL( new int[][]{
				{5, 10, 0, 0, 0},
				{8, 18, 2, 0, 0},
				{12, 28, 5, 2, 0},
				{18, 42, 9, 5, 1}
		} ),
		WEST_WALL( new int[][]{
				{5, 10, 0, 0, 0},
				{8, 18, 2, 0, 0},
				{12, 28, 5, 2, 0},
				{18, 42, 9, 5, 1}
		} ),
		NORTHWEST_TOWER( new int[][]{
				{8, 14, 1, 0, 0},
				{12, 24, 4, 1, 0},
				{18, 36, 8, 4, 1},
				{26, 52, 14, 8, 2}
		} ),
		NORTHEAST_TOWER( new int[][]{
				{8, 14, 1, 0, 0},
				{12, 24, 4, 1, 0},
				{18, 36, 8, 4, 1},
				{26, 52, 14, 8, 2}
		} ),
		SOUTHWEST_TOWER( new int[][]{
				{8, 14, 1, 0, 0},
				{12, 24, 4, 1, 0},
				{18, 36, 8, 4, 1},
				{26, 52, 14, 8, 2}
		} ),
		SOUTHEAST_TOWER( new int[][]{
				{8, 14, 1, 0, 0},
				{12, 24, 4, 1, 0},
				{18, 36, 8, 4, 1},
				{26, 52, 14, 8, 2}
		} );

		private final int[][] costs;

		Building( int[][] costs ) {
			this.costs = costs;
		}

		private int maxLevel() {
			return MAX_BUILDING_LEVEL;
		}

		private int cost( int targetLevel, Material material ) {
			if (targetLevel <= 0) return 0;
			int materialIndex = material.ordinal();
			if (targetLevel <= costs.length) {
				return costs[targetLevel - 1][materialIndex];
			}
			int lastCost = costs[costs.length - 1][materialIndex];
			if (lastCost <= 0) return 0;
			int extraLevel = targetLevel - costs.length;
			return Math.round( lastCost * (1f + extraLevel * 0.55f) ) + extraLevel * extraLevel * (materialIndex + 1);
		}
	}

	public static boolean isTowerBuilding( Building building ) {
		if (building == null) return false;
		switch (building) {
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return true;
			default:
				return false;
		}
	}

	private static Building firstTowerWall( Building tower ) {
		switch (tower) {
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
				return Building.NORTH_WALL;
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return Building.SOUTH_WALL;
			default:
				return null;
		}
	}

	private static Building secondTowerWall( Building tower ) {
		switch (tower) {
			case NORTHWEST_TOWER:
			case SOUTHWEST_TOWER:
				return Building.WEST_WALL;
			case NORTHEAST_TOWER:
			case SOUTHEAST_TOWER:
				return Building.EAST_WALL;
			default:
				return null;
		}
	}

	private static boolean towerUsesWall( Building tower, Building wall ) {
		return isTowerBuilding( tower ) && (firstTowerWall( tower ) == wall || secondTowerWall( tower ) == wall);
	}

	public static boolean visualDependsOn( Building visualBuilding, Building changedBuilding ) {
		if (visualBuilding == null || changedBuilding == null) return false;
		return visualBuilding == changedBuilding || towerUsesWall( visualBuilding, changedBuilding );
	}

	public enum Training {
		HEALTH( "Health", Building.CAMP, 1, 2, 5 ),
		STRENGTH( "Strength", Building.FORGE, 1, 1, 1 ),
		ACCURACY( "Accuracy", Building.ALCHEMY, 2, 1, 1 ),
		EVASION( "Evasion", Building.GARDEN, 2, 1, 1 ),
		TALENT_POINT( "Tier 1 Talent Point", Building.CAMP, 2, 1, 1 ),
		TALENT_TIER_2( "Tier 2 Talent Point", Building.CAMP, 3, 1, 1 ),
		TALENT_TIER_3( "Tier 3 Talent Point", Building.CAMP, 4, 1, 1 ),
		TALENT_TIER_4( "Tier 4 Talent Point", Building.CAMP, 5, 1, 1 ),
		ARMOR( "Armor", Building.FORGE, 2, 1, 1 ),
		TREASURE_LUCK( "Treasure Luck", Building.VAULT, 2, 1, 5 ),
		WAND_RECHARGE( "Wand Recharge", Building.ALCHEMY, 3, 1, 5 ),
		MOVEMENT_SPEED( "Move Speed", Building.GARDEN, 3, 1, 3 ),
		ATTACK_DAMAGE( "Attack Damage", Building.FORGE, 3, 1, 1 ),
		ATTACK_SPEED( "Attack Speed", Building.FORGE, 4, 1, 3 ),
		ARMOR_ABILITY_CHARGE( "Armor Ability Charge", Building.FORGE, 5, 1, 5 ),
		WAND_DAMAGE( "Wand Damage", Building.ALCHEMY, 4, 1, 3 ),
		WAND_CHARGES( "Wand Charges", Building.ALCHEMY, 5, 1, 1 ),
		ENCHANTMENT_POWER( "Enchant Power", Building.ALCHEMY, 6, 1, 5 ),
		ELEMENTAL_RESISTANCE( "Elemental Resist", Building.ALCHEMY, 7, 1, 2 ),
		RANGED_DAMAGE( "Ranged Damage", Building.GARDEN, 4, 1, 1 ),
		THROWN_DURABILITY( "Throw Usages", Building.GARDEN, 5, 1, 5 ),
		TRINKET_POTENCY( "Trinket Potency", Building.GARDEN, 6, 1, 1 ),
		TENACITY( "Tenacity", Building.CAMP, 6, 1, 2 ),
		XP_GAIN( "XP Gain", Building.CAMP, 7, 1, 5 ),
		ARTIFACT_RECHARGE( "Artifact Recharge", Building.VAULT, 3, 1, 5 ),
		RING_POTENCY( "Ring Potency", Building.VAULT, 4, 1, 1 ),
		ARTIFACT_POTENCY( "Artifact Potency", Building.VAULT, 5, 1, 1 ),
		GOLD_GAIN( "Gold Gain", Building.VAULT, 6, 1, 5 ),
		CATALYST_DROP_RATE( "Catalyst Drop Rate", Building.VAULT, 7, 1, 10 ),
		CRITICAL_CHANCE( "Crit Chance", Building.FORGE, 6, 1, 2 ),
		CRITICAL_DAMAGE( "Crit Damage", Building.FORGE, 7, 1, 5 ),
		LIFESTEAL( "Lifesteal", Building.GARDEN, 7, 1, 1 ),
		DODGE_CHANCE( "Dodge Chance", Building.GARDEN, 8, 1, 2 ),
		BLOCK_CHANCE( "Block Chance", Building.FORGE, 8, 1, 2 ),
		BONUS_LOOT( "Bonus Loot", Building.VAULT, 8, 1, 5 ),
		RESOURCE_YIELD( "Resource Yield", Building.CAMP, 8, 1, 5 ),
		ARMOR_BONUS( "Armor Bonus", Building.FORGE, 9, 1, 3 ),
		THORNS_CHANCE( "Thorns Chance", Building.FORGE, 10, 1, 2 ),
		THORNS_DAMAGE( "Thorns Damage", Building.FORGE, 11, 1, 1 ),
		KNOCKBACK_CHANCE( "Impact Chance", Building.FORGE, 12, 1, 2 ),
		KNOCKBACK_STRENGTH( "Impact Force", Building.FORGE, 13, 1, 1 ),
		CLEAVE_CHANCE( "Cleave Chance", Building.FORGE, 14, 1, 2 ),
		PIERCING_CHANCE( "Piercing Chance", Building.FORGE, 15, 1, 2 ),
		STATUS_PROC_CHANCE( "Status Proc", Building.ALCHEMY, 8, 1, 2 ),
		STATUS_DURATION( "Status Duration", Building.ALCHEMY, 9, 1, 5 ),
		MAGIC_DAMAGE( "Magic Damage", Building.ALCHEMY, 10, 1, 1 ),
		MAGIC_POWER( "Magic Power", Building.ALCHEMY, 11, 1, 3 ),
		LIGHTNING_CHANCE( "Lightning Chance", Building.ALCHEMY, 12, 1, 2 ),
		BARRIER_GUARD( "Barrier Guard", Building.CAMP, 9, 1, 2 ),
		BARRIER_POWER( "Barrier Power", Building.CAMP, 10, 1, 1 ),
		FIRE_RESISTANCE( "Burning Resist", Building.ALCHEMY, 13, 1, 2 ),
		FROST_RESISTANCE( "Frost Resist", Building.ALCHEMY, 14, 1, 2 ),
		POISON_RESISTANCE( "Poison Resist", Building.GARDEN, 9, 1, 2 ),
		CORROSION_RESISTANCE( "Corrosion Resist", Building.ALCHEMY, 15, 1, 2 ),
		BLEED_RESISTANCE( "Bleed Resist", Building.CAMP, 11, 1, 2 ),
		BLINDNESS_RESISTANCE( "Blindness Resist", Building.GARDEN, 10, 1, 2 ),
		CRIPPLE_RESISTANCE( "Cripple Resist", Building.GARDEN, 11, 1, 2 ),
		DAZE_RESISTANCE( "Daze Resist", Building.CAMP, 12, 1, 2 ),
		HEX_RESISTANCE( "Hex Resist", Building.ALCHEMY, 16, 1, 2 ),
		ROOT_RESISTANCE( "Root Resist", Building.GARDEN, 12, 1, 2 ),
		SLOW_RESISTANCE( "Slow Resist", Building.GARDEN, 13, 1, 2 ),
		VERTIGO_RESISTANCE( "Vertigo Resist", Building.GARDEN, 14, 1, 2 ),
		VULNERABLE_RESISTANCE( "Vulnerable Resist", Building.CAMP, 13, 1, 2 ),
		STUN_RESISTANCE( "Stun Resist", Building.CAMP, 14, 1, 2 ),
		WEAKNESS_RESISTANCE( "Weakness Resist", Building.CAMP, 15, 1, 2 ),
		CHARM_RESISTANCE( "Charm Resist", Building.CAMP, 16, 1, 2 ),
		TERROR_RESISTANCE( "Terror Resist", Building.CAMP, 17, 1, 2 ),
		DREAD_RESISTANCE( "Dread Resist", Building.CAMP, 18, 1, 2 ),
		SLEEP_RESISTANCE( "Sleep Resist", Building.GARDEN, 15, 1, 2 ),
		AMOK_RESISTANCE( "Amok Resist", Building.ALCHEMY, 17, 1, 2 ),
		DEGRADE_RESISTANCE( "Degrade Resist", Building.ALCHEMY, 18, 1, 2 ),
		DOOM_RESISTANCE( "Doom Resist", Building.ALCHEMY, 19, 1, 2 ),
		CHILL_RESISTANCE( "Chill Resist", Building.GARDEN, 16, 1, 2 ),
		OOZE_RESISTANCE( "Ooze Resist", Building.GARDEN, 17, 1, 2 ),
		MAGICAL_SLEEP_RESISTANCE( "Magical Sleep Resist", Building.GARDEN, 18, 1, 2 ),
		BLEED_PROC( "Bleed Proc", Building.FORGE, 16, 1, 2 ),
		BLEED_DURATION( "Bleed Duration", Building.FORGE, 17, 1, 1 ),
		STUN_CHANCE( "Stun Chance", Building.FORGE, 18, 1, 2 ),
		STUN_DURATION( "Stun Duration", Building.FORGE, 19, 1, 1 ),
		BURNING_PROC( "Burning Proc", Building.ALCHEMY, 20, 1, 2 ),
		BURNING_DURATION( "Burning Duration", Building.ALCHEMY, 21, 1, 1 ),
		CORROSION_PROC( "Corrosion Proc", Building.ALCHEMY, 22, 1, 2 ),
		CORROSION_DURATION( "Corrosion Duration", Building.ALCHEMY, 23, 1, 1 ),
		HEX_PROC( "Hex Proc", Building.ALCHEMY, 24, 1, 2 ),
		HEX_DURATION( "Hex Duration", Building.ALCHEMY, 25, 1, 1 ),
		FROST_PROC( "Frost Proc", Building.GARDEN, 19, 1, 2 ),
		FROST_DURATION( "Frost Duration", Building.GARDEN, 20, 1, 1 ),
		POISON_PROC( "Poison Proc", Building.GARDEN, 21, 1, 2 ),
		POISON_DURATION( "Poison Duration", Building.GARDEN, 22, 1, 1 ),
		ROOT_PROC( "Root Proc", Building.GARDEN, 23, 1, 2 ),
		ROOT_DURATION( "Root Duration", Building.GARDEN, 24, 1, 1 ),
		SLOW_PROC( "Slow Proc", Building.GARDEN, 25, 1, 2 ),
		SLOW_DURATION( "Slow Duration", Building.GARDEN, 26, 1, 1 ),
		VERTIGO_PROC( "Vertigo Proc", Building.GARDEN, 27, 1, 2 ),
		VERTIGO_DURATION( "Vertigo Duration", Building.GARDEN, 28, 1, 1 ),
		BLINDNESS_PROC( "Blindness Proc", Building.CAMP, 19, 1, 2 ),
		BLINDNESS_DURATION( "Blindness Duration", Building.CAMP, 20, 1, 1 ),
		CRIPPLE_PROC( "Cripple Proc", Building.CAMP, 21, 1, 2 ),
		CRIPPLE_DURATION( "Cripple Duration", Building.CAMP, 22, 1, 1 ),
		DAZE_PROC( "Daze Proc", Building.CAMP, 23, 1, 2 ),
		DAZE_DURATION( "Daze Duration", Building.CAMP, 24, 1, 1 ),
		VULNERABLE_PROC( "Vulnerable Proc", Building.CAMP, 25, 1, 2 ),
		VULNERABLE_DURATION( "Vulnerable Duration", Building.CAMP, 26, 1, 1 ),
		WEAKNESS_PROC( "Weakness Proc", Building.CAMP, 27, 1, 2 ),
		WEAKNESS_DURATION( "Weakness Duration", Building.CAMP, 28, 1, 1 ),
		MATERIAL_CACHE_SIZE( "Material Cache", Building.VAULT, 9, 1, 10 );

		private final String label;
		private final Building building;
		private final int unlockLevel;
		private final int maxPerBuildingLevel;
		private final int bonusPerLevel;

		Training( String label, Building building, int unlockLevel, int maxPerBuildingLevel, int bonusPerLevel ) {
			this.label = label;
			this.building = building;
			this.unlockLevel = unlockLevel;
			this.maxPerBuildingLevel = maxPerBuildingLevel;
			this.bonusPerLevel = bonusPerLevel;
		}

		public String label() {
			return label;
		}

		public Building building() {
			return building;
		}

		public int unlockLevel() {
			return unlockLevel;
		}

		public int bonusPerLevel() {
			return bonusPerLevel;
		}

		private int cap( int buildingLevel ) {
			if (buildingLevel < unlockLevel) return 0;
			return (buildingLevel - unlockLevel + 1) * maxPerBuildingLevel;
		}
	}

	public enum BuildingDefense {
		HP( "Structure HP", 1, 2, 50 ),
		ARMOR( "Armor", 2, 1, 1 ),
		FIRE_RESISTANCE( "Burning Resist", 3, 1, 2 ),
		FROST_RESISTANCE( "Frost Resist", 3, 1, 2 ),
		POISON_RESISTANCE( "Poison Resist", 4, 1, 2 ),
		CORROSION_RESISTANCE( "Corrosion Resist", 4, 1, 2 ),
		BLEED_RESISTANCE( "Bleed Resist", 5, 1, 2 ),
		BLINDNESS_RESISTANCE( "Blindness Resist", 5, 1, 2 ),
		CRIPPLE_RESISTANCE( "Cripple Resist", 6, 1, 2 ),
		DAZE_RESISTANCE( "Daze Resist", 6, 1, 2 ),
		HEX_RESISTANCE( "Hex Resist", 7, 1, 2 ),
		ROOT_RESISTANCE( "Root Resist", 7, 1, 2 ),
		SLOW_RESISTANCE( "Slow Resist", 8, 1, 2 ),
		VERTIGO_RESISTANCE( "Vertigo Resist", 8, 1, 2 ),
		VULNERABLE_RESISTANCE( "Vulnerable Resist", 9, 1, 2 ),
		STUN_RESISTANCE( "Stun Resist", 9, 1, 2 ),
		WEAKNESS_RESISTANCE( "Weakness Resist", 10, 1, 2 ),
		CHARM_RESISTANCE( "Charm Resist", 10, 1, 2 ),
		TERROR_RESISTANCE( "Terror Resist", 11, 1, 2 ),
		DREAD_RESISTANCE( "Dread Resist", 11, 1, 2 ),
		SLEEP_RESISTANCE( "Sleep Resist", 12, 1, 2 ),
		AMOK_RESISTANCE( "Amok Resist", 12, 1, 2 ),
		DEGRADE_RESISTANCE( "Degrade Resist", 13, 1, 2 ),
		DOOM_RESISTANCE( "Doom Resist", 13, 1, 2 ),
		CHILL_RESISTANCE( "Chill Resist", 14, 1, 2 ),
		OOZE_RESISTANCE( "Ooze Resist", 14, 1, 2 ),
		MAGICAL_SLEEP_RESISTANCE( "Magical Sleep Resist", 15, 1, 2 );

		private final String label;
		private final int unlockLevel;
		private final int maxPerBuildingLevel;
		private final int bonusPerLevel;

		BuildingDefense( String label, int unlockLevel, int maxPerBuildingLevel, int bonusPerLevel ) {
			this.label = label;
			this.unlockLevel = unlockLevel;
			this.maxPerBuildingLevel = maxPerBuildingLevel;
			this.bonusPerLevel = bonusPerLevel;
		}

		public String label() {
			return label;
		}

		public int unlockLevel() {
			return unlockLevel;
		}

		public int bonusPerLevel() {
			return bonusPerLevel;
		}

		private int cap( int buildingLevel ) {
			if (buildingLevel < unlockLevel) return 0;
			return (buildingLevel - unlockLevel + 1) * maxPerBuildingLevel;
		}
	}

	private static final String[] MATERIAL_NAMES = {
			"wood",
			"stone",
			"copper",
			"iron",
			"gold"
	};

	private static String materialLabel( Material material ) {
		if (material == Material.COPPER) return "copper ore";
		if (material == Material.IRON) return "iron ore";
		if (material == Material.GOLD) return "gold ore";
		int index = material == null ? 0 : material.ordinal();
		return MATERIAL_NAMES[Math.max( 0, Math.min( index, MATERIAL_NAMES.length - 1 ) )];
	}

	private static final String AMOUNTS = "amounts";
	private static final String FORGE_RESOURCES = "forge_resources";
	private static final String VAULT_LEVEL = "vault_level";
	private static final String VAULT_ITEMS = "vault_items";
	private static final String BUILDING_LEVELS = "building_levels";
	private static final String TRAINING_LEVELS = "training_levels";
	private static final String BUILDING_HP = "building_hp";
	private static final String BUILDING_DESTROYED = "building_destroyed";
	private static final String BUILDING_DEFENSE_LEVELS = "building_defense_levels";
	private static final String MOONROOT_GROWTH = "moonroot_growth";
	private static final String MOONROOT_SEEDS = "moonroot_seeds";
	private static final String SETTLEMENT_REQUESTS = "settlement_requests";
	private static final String RAID_ACTIVE = "raid_active";
	private static final String RAID_POPUP_PENDING = "raid_popup_pending";
	private static final String RAID_THREAT = "raid_threat";
	private static final String RAID_TOTAL_MOBS = "raid_total_mobs";
	private static final String RAID_WAVE = "raid_wave";
	private static final String RAID_WAVES = "raid_waves";
	private static final String RAID_WAVE_TOTAL = "raid_wave_total";
	private static final String RAID_WAVE_SPAWNED = "raid_wave_spawned";
	private static final String RAID_WAVE_KILLED = "raid_wave_killed";
	private static final String RAID_MOB_CLASS = "raid_mob_class";
	private static final String REVENGE_KILL_CLASSES = "revenge_kill_classes";
	private static final String REVENGE_KILL_COUNTS = "revenge_kill_counts";
	private static final String DEFENDERS = "defenders";
	private static final String NEXT_DEFENDER_ID = "next_defender_id";

	public static final int RAID_PROGRESS_ACTIVE = 0;
	public static final int RAID_PROGRESS_NEXT_WAVE = 1;
	public static final int RAID_PROGRESS_COMPLETE = 2;

	private static final Class<? extends Plant.Seed>[] MOONROOT_SEED_CLASSES = new Class[]{
			Sungrass.Seed.class,
			Fadeleaf.Seed.class,
			Icecap.Seed.class,
			Firebloom.Seed.class,
			Sorrowmoss.Seed.class,
			Swiftthistle.Seed.class,
			Blindweed.Seed.class,
			Stormvine.Seed.class,
			Earthroot.Seed.class,
			Mageroyal.Seed.class,
			Starflower.Seed.class,
			BlandfruitBush.Seed.class
	};

	private static final float[] MOONROOT_SEED_WEIGHTS = {
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0.35f
	};

	private int[] amounts = new int[Material.values().length];
	private int[] forgeResources = new int[ForgeResource.values().length];
	private int[] buildingLevels = new int[Building.values().length];
	private int[] trainingLevels = new int[Training.values().length];
	private int[] buildingHp = new int[Building.values().length];
	private int[] buildingDestroyed = new int[Building.values().length];
	private int[] buildingDefenseLevels = new int[Building.values().length * BuildingDefense.values().length];
	private int[] moonrootSeeds = new int[0];
	private ArrayList<Item> vaultItems = new ArrayList<>();
	private ArrayList<SettlementRequest> settlementRequests = new ArrayList<>();
	private ArrayList<String> revengeKillClasses = new ArrayList<>();
	private ArrayList<Integer> revengeKillCounts = new ArrayList<>();
	private ArrayList<DefenderRecord> defenders = new ArrayList<>();
	private int nextDefenderId = 1;
	private int vaultLevel = 0;
	private int moonrootGrowth = 0;
	private boolean raidActive = false;
	private boolean raidPopupPending = false;
	private int raidThreat = 0;
	private int raidTotalMobs = 0;
	private int raidWave = 0;
	private int raidWaves = 0;
	private int raidWaveTotal = 0;
	private int raidWaveSpawned = 0;
	private int raidWaveKilled = 0;
	private String raidMobClass = "";
	private String lastRaidRewardText = "";

	public int amount( Material material ) {
		if (INFINITE_TEST_RESOURCES) return TEST_RESOURCE_AMOUNT;
		return amounts[material.ordinal()];
	}

	public int goldAmount() {
		ensureTestCurrencies();
		return Dungeon.gold;
	}

	public int energyAmount() {
		ensureTestCurrencies();
		return Dungeon.energy;
	}

	private void ensureTestCurrencies() {
		if (!INFINITE_TEST_RESOURCES) return;
		Dungeon.gold = Math.max( Dungeon.gold, TEST_RESOURCE_AMOUNT );
		Dungeon.energy = Math.max( Dungeon.energy, TEST_RESOURCE_AMOUNT );
	}

	public void add( Material material, int amount ) {
		if (amount <= 0) return;
		amounts[material.ordinal()] += amount;
	}

	public int forgeResourceAmount( ForgeResource resource ) {
		if (INFINITE_TEST_RESOURCES) return TEST_RESOURCE_AMOUNT;
		return forgeResources[resource.ordinal()];
	}

	public void addForgeResource( ForgeResource resource, int amount ) {
		if (amount <= 0) return;
		forgeResources[resource.ordinal()] += amount;
	}

	public int maxForgeUpgradeLevel() {
		int level = buildingLevel( Building.FORGE );
		return level <= 0 ? -1 : level * 2;
	}

	public boolean canSalvage( Item item ) {
		return isBuilt( Building.FORGE )
				&& isSalvageable( item );
	}

	public boolean salvage( Item item ) {
		return salvage( item, 1 );
	}

	public boolean salvage( Item item, int amount ) {
		if (!canSalvage( item )) return false;
		amount = Math.max( 1, amount );
		Item yieldItem = singleSalvageYieldItem( item );

		for (ForgeResource resource : ForgeResource.values()) {
			addForgeResource( resource, salvageYield( yieldItem, resource ) * amount );
		}
		for (Material material : Material.values()) {
			add( material, salvageMaterialYield( yieldItem, material ) * amount );
		}
		Dungeon.gold += salvageGoldYield( yieldItem ) * amount;
		Dungeon.energy += salvageEnergyYield( yieldItem ) * amount;
		return true;
	}

	private Item singleSalvageYieldItem( Item item ) {
		if (item == null || item.quantity() <= 1) return item;
		Item single = item.duplicate();
		if (single == null) return item;
		single.quantity( 1 );
		return single;
	}

	public int salvageYield( Item item, ForgeResource resource ) {
		if (item == null || !isSalvageable( item )) return 0;

		int rawYield = rawSalvageYield( item, resource );
		return adjustedSalvageYield( item, resource, rawYield );
	}

	private int rawSalvageYield( Item item, ForgeResource resource ) {
		if (item instanceof Scroll) {
			boolean exotic = item instanceof ExoticScroll;
			switch (resource) {
				case SCRAP:
					return exotic ? 8 : 5;
				case EMBER_SHARD:
					return exotic ? 2 : 1;
				case EMBER_CORE:
					return exotic ? 1 : 0;
				default:
					return 0;
			}
		}

		if (item instanceof Runestone) {
			boolean catalyst = item instanceof RarityCatalystStone;
			switch (resource) {
				case SCRAP:
					return catalyst ? 10 : 4;
				case EMBER_SHARD:
					return catalyst ? 2 : 1;
				case EMBER_CORE:
					return catalyst ? 1 : 0;
				default:
					return 0;
			}
		}

		if (item instanceof TrinketCatalyst) {
			switch (resource) {
				case SCRAP:
					return 12;
				case EMBER_SHARD:
					return 2;
				case EMBER_CORE:
					return 1;
				default:
					return 0;
			}
		}

		int rarity = item.isIdentified() && item.hasRarityRoll() ? item.rarity().ordinal() : 0;
		int stats = item.isIdentified() ? item.rarityStatCount() : 0;
		int level = item.isIdentified() ? Math.max( 0, item.trueLevel() ) : 0;
		int base = 4 + level * 3 + rarity * 5 + stats * 2;

		if (item instanceof Weapon || item instanceof Armor) {
			base += 4;
		} else if (item instanceof Wand || item instanceof Ring) {
			base += 5;
		} else if (item instanceof Artifact || item instanceof Trinket) {
			base += 8;
		}

		switch (resource) {
			case SCRAP:
				return base;
			case EMBER_SHARD:
				return Math.max( 0, level / 2 + Math.max( 0, rarity - 1 ) + stats / 3 );
			case EMBER_CORE:
				return (rarity >= 4 ? 1 : 0) + (level >= 6 ? 1 : 0) + (item.isIdentified() && item.isTranscendantRarity() ? 1 : 0);
			default:
				return 0;
		}
	}

	private int adjustedSalvageYield( Item item, ForgeResource resource, int rawYield ) {
		if (rawYield <= 0) return 0;
		int adjusted = rawYield * salvageYieldPercent( item ) / 100;
		return resource == ForgeResource.SCRAP ? Math.max( 1, adjusted ) : adjusted;
	}

	public int salvageMaterialYield( Item item, Material material ) {
		if (item == null || !isSalvageable( item )) return 0;
		int raw = rawSalvageMaterialYield( item, material );
		if (raw <= 0) return 0;
		return adjustedSalvageExtraYield( item, raw );
	}

	private int rawSalvageMaterialYield( Item item, Material material ) {
		int rarity = item.isIdentified() && item.hasRarityRoll() ? item.rarity().ordinal() : 0;
		int stats = item.isIdentified() ? item.rarityStatCount() : 0;
		int level = item.isIdentified() ? Math.max( 0, item.trueLevel() ) : 0;
		int valueBase = Math.max( 1, item.value() / 30 );

		switch (material) {
			case WOOD:
				if (item instanceof Wand || item instanceof Weapon || item instanceof Scroll || item instanceof Runestone) {
					return valueBase + level + (item instanceof Wand ? 2 : 1);
				}
				return 0;
			case STONE:
				if (item instanceof Armor || item instanceof Runestone || item instanceof RarityCatalystStone || item instanceof TrinketCatalyst) {
					return valueBase + level + (item instanceof Runestone ? 3 : 1);
				}
				return item instanceof Artifact ? valueBase : 0;
			case COPPER:
				return item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring
						? Math.max( 1, valueBase / 2 + level + stats / 3 )
						: 0;
			case IRON:
				return item instanceof Weapon || item instanceof Armor || item instanceof Artifact
						? Math.max( 0, level / 2 + rarity + stats / 4 )
						: 0;
			case GOLD:
				return rarity >= 3 || item instanceof Ring || item instanceof Artifact || item instanceof Trinket
						? Math.max( 0, rarity - 1 + level / 4 )
						: 0;
			default:
				return 0;
		}
	}

	public int salvageGoldYield( Item item ) {
		if (item == null || !isSalvageable( item )) return 0;
		int raw = Math.max( 1, item.value() / 4 );
		if (item instanceof Scroll || item instanceof Runestone) raw = Math.max( 1, raw / 2 );
		return adjustedSalvageExtraYield( item, raw );
	}

	public int salvageEnergyYield( Item item ) {
		if (item == null || !isSalvageable( item )) return 0;
		int raw = 0;
		if (item instanceof Wand) raw = 2 + Math.max( 0, item.trueLevel() );
		else if (item instanceof Artifact || item instanceof Trinket) raw = 4 + Math.max( 0, item.trueLevel() );
		else if (item instanceof Scroll || item instanceof ExoticScroll) raw = item instanceof ExoticScroll ? 3 : 1;
		else if (item instanceof Runestone || item instanceof TrinketCatalyst) raw = item instanceof RarityCatalystStone ? 3 : 1;
		return adjustedSalvageExtraYield( item, raw );
	}

	private int adjustedSalvageExtraYield( Item item, int rawYield ) {
		if (rawYield <= 0) return 0;
		return Math.max( 1, rawYield * salvageYieldPercent( item ) / 100 );
	}

	private int salvageYieldPercent( Item item ) {
		int percent = 100;
		if (!item.isIdentified()) {
			percent = 55;
		}
		if (item.cursed) {
			percent -= item.isIdentified() ? 30 : 20;
		}
		return Math.max( 25, percent );
	}

	public String salvageYieldText( Item item ) {
		return salvageYieldText( item, 1 );
	}

	public String salvageYieldText( Item item, int count ) {
		count = Math.max( 1, count );
		Item yieldItem = singleSalvageYieldItem( item );
		StringBuilder text = new StringBuilder();
		for (ForgeResource resource : ForgeResource.values()) {
			int amount = salvageYield( yieldItem, resource ) * count;
			if (amount <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount ).append( ' ' ).append( resource.label() );
		}
		for (Material material : Material.values()) {
			int amount = salvageMaterialYield( yieldItem, material ) * count;
			if (amount <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount ).append( ' ' ).append( materialLabel( material ) );
		}
		int gold = salvageGoldYield( yieldItem ) * count;
		if (gold > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( gold ).append( " gold coins" );
		}
		int energy = salvageEnergyYield( yieldItem ) * count;
		if (energy > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( energy ).append( " energy" );
		}
		return text.length() == 0 ? "nothing useful" : text.toString();
	}

	public boolean canForgeUpgrade( Item item ) {
		if (!canForgeUpgradeTarget( item )) return false;

		for (Material material : Material.values()) {
			if (amount( material ) < forgeUpgradeMaterialCost( item, material )) return false;
		}
		for (ForgeResource resource : ForgeResource.values()) {
			if (forgeResourceAmount( resource ) < forgeUpgradeCost( item, resource )) return false;
		}
		return true;
	}

	public boolean canForgeUpgradeTarget( Item item ) {
		return isBuilt( Building.FORGE )
				&& isForgeUpgradeTarget( item )
				&& item.isUpgradable()
				&& item.isIdentified()
				&& !item.cursed
				&& item.trueLevel() < maxForgeUpgradeLevel();
	}

	public boolean spendForgeUpgradeCost( Item item ) {
		if (!canForgeUpgrade( item )) return false;

		if (!INFINITE_TEST_RESOURCES) {
			for (Material material : Material.values()) {
				amounts[material.ordinal()] -= forgeUpgradeMaterialCost( item, material );
			}
			for (ForgeResource resource : ForgeResource.values()) {
				forgeResources[resource.ordinal()] -= forgeUpgradeCost( item, resource );
			}
		}
		return true;
	}

	public int forgeUpgradeCost( Item item, ForgeResource resource ) {
		if (item == null) return 0;

		int nextLevel = Math.max( 1, item.trueLevel() + 1 );
		int rarity = item.hasRarityRoll() ? item.rarity().ordinal() : 0;
		int stats = item.rarityStatCount();

		switch (resource) {
			case SCRAP:
				return 10 + nextLevel * 8 + rarity * 4 + stats * 3;
			case EMBER_SHARD:
				return Math.max( 0, nextLevel - 1 ) + Math.max( 0, rarity - 1 ) + stats / 3;
			case EMBER_CORE:
				return (nextLevel >= 5 ? 1 + (nextLevel - 5) / 2 : 0) + (rarity >= 4 ? 1 : 0);
			default:
				return 0;
		}
	}

	public int forgeUpgradeMaterialCost( Item item, Material material ) {
		if (item == null) return 0;

		int nextLevel = Math.max( 1, item.trueLevel() + 1 );
		int rarity = item.hasRarityRoll() ? item.rarity().ordinal() : 0;

		switch (material) {
			case WOOD:
				return item instanceof Wand || item instanceof Weapon ? 1 + nextLevel : 0;
			case STONE:
				return item instanceof Armor ? 4 + nextLevel * 2 : 2 + nextLevel;
			case COPPER:
				return 2 + nextLevel * 2 + rarity;
			case IRON:
				return nextLevel >= 2 ? nextLevel + Math.max( 0, rarity / 2 ) : 0;
			case GOLD:
				return nextLevel >= 5 || rarity >= 4 ? Math.max( 1, nextLevel - 4 ) : 0;
			default:
				return 0;
		}
	}

	public String forgeUpgradeCostText( Item item ) {
		StringBuilder text = new StringBuilder();
		appendMaterialCostText( text, item );
		appendForgeCostText( text, item );
		return text.toString();
	}

	public String forgeUpgradeOwnedCostText( Item item ) {
		StringBuilder text = new StringBuilder();
		appendOwnedMaterialCostText( text, item );
		appendOwnedForgeCostText( text, item );
		return text.toString();
	}

	public String forgeResourcesText() {
		StringBuilder text = new StringBuilder();
		for (ForgeResource resource : ForgeResource.values()) {
			if (text.length() > 0) text.append( ", " );
			text.append( forgeResourceAmount( resource ) ).append( ' ' ).append( resource.label() );
		}
		return text.toString();
	}

	private boolean isForgeUpgradeTarget( Item item ) {
		return item instanceof Weapon
				|| item instanceof Armor
				|| item instanceof Wand
				|| item instanceof Ring
				|| item instanceof Artifact
				|| item instanceof Trinket;
	}

	private boolean isSalvageable( Item item ) {
		return isForgeUpgradeTarget( item )
				|| item instanceof Scroll
				|| item instanceof Runestone
				|| item instanceof RarityCatalystStone
				|| item instanceof TrinketCatalyst;
	}

	public int vaultLevel() {
		return buildingLevel( Building.VAULT );
	}

	public int vaultSlots() {
		int level = vaultLevel();
		return level > 0 ? 5 + 5*(level - 1) : 0;
	}

	public int campContractSlots() {
		int level = buildingLevel( Building.CAMP );
		if (level <= 0) return 0;
		return Math.min( 50, 1 + level / 2 );
	}

	public ArrayList<SettlementRequest> settlementRequests() {
		ensureSettlementRequests();
		return settlementRequests;
	}

	public boolean canFulfillSettlementRequest( int index ) {
		ensureSettlementRequests();
		if (index < 0 || index >= settlementRequests.size()) return false;

		SettlementRequest request = settlementRequests.get( index );
		if (request == null) return false;
		switch (request.objectiveType()) {
			case SettlementRequest.OBJECTIVE_BOUNTY:
			case SettlementRequest.OBJECTIVE_SCOUTING:
			case SettlementRequest.OBJECTIVE_RECOVERY:
				return request.progress() >= request.amount();
			case SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return forgeResourceAmount( request.objectiveForgeResource() ) >= request.amount();
			case SettlementRequest.OBJECTIVE_GOLD:
				return goldAmount() >= request.amount();
			case SettlementRequest.OBJECTIVE_ENERGY:
				return energyAmount() >= request.amount();
			case SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return amount( request.objectiveMaterial() ) >= request.amount();
		}
	}

	public boolean fulfillSettlementRequest( int index ) {
		if (!canFulfillSettlementRequest( index )) return false;

		SettlementRequest request = settlementRequests.get( index );
		if (!INFINITE_TEST_RESOURCES) {
			switch (request.objectiveType()) {
				case SettlementRequest.OBJECTIVE_BOUNTY:
				case SettlementRequest.OBJECTIVE_SCOUTING:
				case SettlementRequest.OBJECTIVE_RECOVERY:
					break;
				case SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
					forgeResources[request.objectiveForgeResource().ordinal()] -= request.amount();
					break;
				case SettlementRequest.OBJECTIVE_GOLD:
					Dungeon.gold -= request.amount();
					break;
				case SettlementRequest.OBJECTIVE_ENERGY:
					Dungeon.energy -= request.amount();
					break;
				case SettlementRequest.OBJECTIVE_MATERIAL:
				default:
					amounts[request.objectiveMaterial().ordinal()] -= request.amount();
					break;
			}
		}
		grantSettlementReward( request );
		settlementRequests.set( index, randomSettlementRequest() );
		return true;
	}

	public void progressBountyMission() {
		progressSettlementObjective( SettlementRequest.OBJECTIVE_BOUNTY, 1 );
	}

	public void progressScoutingMission( int floor ) {
		if (floor <= 0) return;
		ensureSettlementRequests();
		for (SettlementRequest request : settlementRequests) {
			if (request != null && request.objectiveType() == SettlementRequest.OBJECTIVE_SCOUTING) {
				request.setProgress( Math.max( request.progress(), Math.min( floor, request.amount() ) ) );
			}
		}
	}

	public void progressRecoveryMission( Item item ) {
		if (item == null || Dungeon.depth <= 0) return;
		if (item instanceof Gold || item instanceof EnergyCrystal || item instanceof BuildingMaterial) return;
		progressSettlementObjective( SettlementRequest.OBJECTIVE_RECOVERY, 1 );
	}

	public void recordRaidRevengeKill( Class mobClass ) {
		if (mobClass == null) return;

		String className = mobClass.getName();
		if (excludedRaidMobClass( className )) return;

		for (int i = 0; i < revengeKillClasses.size(); i++) {
			if (className.equals( revengeKillClasses.get( i ) )) {
				revengeKillCounts.set( i, revengeKillCounts.get( i ) + 1 );
				return;
			}
		}
		revengeKillClasses.add( className );
		revengeKillCounts.add( 1 );
	}

	public boolean rollRaidOnReturn() {
		if (raidActive || !Dungeon.raidThreatReady()) return false;

		startRaid( Dungeon.consumeRaidThreatForRaid() );
		return true;
	}

	public static boolean homebaseNpcTestItemsEnabled() {
		return HOMEBASE_NPC_TEST_ITEMS;
	}

	public boolean forceRaidForTesting() {
		if (!HOMEBASE_NPC_TEST_ITEMS || raidActive) return false;
		startRaid( RAID_THREAT_PER_MOB * RAID_MOBS_PER_WAVE );
		return true;
	}

	public ArrayList<DefenderRecord> defenders() {
		pruneDeadDefenders();
		return defenders;
	}

	public DefenderRecord addDefenderForTesting() {
		if (!HOMEBASE_NPC_TEST_ITEMS) return null;
		return recruitDefender( null );
	}

	public DefenderRecord recruitDefender( DefenderRecord candidate ) {
		DefenderRecord defender = candidate == null
				? DefenderRecord.random( nextDefenderId++ )
				: candidate.copyWithId( nextDefenderId++ );
		defenders.add( defender );
		return defender;
	}

	public DefenderRecord defender( int id ) {
		pruneDeadDefenders();
		for (DefenderRecord defender : defenders) {
			if (defender != null && defender.id() == id) {
				return defender;
			}
		}
		return null;
	}

	public void markDefenderDead( int id ) {
		for (int i = defenders.size() - 1; i >= 0; i--) {
			DefenderRecord defender = defenders.get( i );
			if (defender != null && defender.id() == id) {
				defender.clearEquipment();
				defenders.remove( i );
				return;
			}
		}
	}

	private void pruneDeadDefenders() {
		for (int i = defenders.size() - 1; i >= 0; i--) {
			DefenderRecord defender = defenders.get( i );
			if (defender == null || !defender.alive()) {
				if (defender != null) defender.clearEquipment();
				defenders.remove( i );
			}
		}
	}

	public String collectDefenderScoutingRewards() {
		pruneDeadDefenders();
		if (defenders.isEmpty()) return "";

		int[] found = new int[Material.values().length];
		int scouts = 0;
		int total = 0;
		for (DefenderRecord defender : defenders) {
			if (defender == null || !defender.alive()) continue;
			int chance = Math.min( 60, 10 + defender.level() * 2 + defender.rarity().power() * 5 );
			if (Random.Int( 100 ) >= chance) continue;

			Material material = Material.values()[Random.chances( new float[]{
					5,
					5,
					Math.max( 2, defender.level() ),
					defender.level() >= 4 ? 2 + defender.level() / 4f : 0,
					defender.level() >= 8 ? 1 + defender.level() / 6f : 0
			} )];
			int amount = Random.NormalIntRange(
					1 + defender.level() / 3 + defender.rarity().power() / 2,
					2 + defender.level() + defender.rarity().power() * 2 );
			found[material.ordinal()] += amount;
			add( material, amount );
			scouts++;
			total += amount;
		}
		if (total <= 0) return "";

		StringBuilder text = new StringBuilder();
		text.append( scouts == 1 ? "A defender returns from scouting with " : "Defenders return from scouting with " );
		boolean first = true;
		for (Material material : Material.values()) {
			int amount = found[material.ordinal()];
			if (amount <= 0) continue;
			if (!first) text.append( ", " );
			text.append( amount ).append( ' ' ).append( material.name().toLowerCase().replace( '_', ' ' ) );
			first = false;
		}
		text.append( '.' );
		return text.toString();
	}

	private void startRaid( int threat ) {
		raidActive = true;
		raidPopupPending = true;
		raidThreat = Math.max( 0, threat );
		int raidBudget = Math.max( RAID_THREAT_PER_MOB, raidThreat - raidThreat % RAID_THREAT_PER_MOB );
		raidTotalMobs = Math.max( RAID_MOBS_PER_WAVE, raidBudget / RAID_THREAT_PER_MOB );
		raidWave = 1;
		raidWaves = Math.max( 1, raidTotalMobs / RAID_MOBS_PER_WAVE + (raidTotalMobs % RAID_MOBS_PER_WAVE > 0 ? 1 : 0) );
		raidMobClass = chooseRaidMobClass();
		revengeKillClasses.clear();
		revengeKillCounts.clear();
		prepareRaidWave();
	}

	private void prepareRaidWave() {
		int remaining = Math.max( 1, raidTotalMobs - (raidWave - 1) * RAID_MOBS_PER_WAVE );
		raidWaveTotal = Math.min( RAID_MOBS_PER_WAVE, remaining );
		raidWaveSpawned = 0;
		raidWaveKilled = 0;
	}

	public boolean raidActive() {
		return raidActive;
	}

	public boolean consumeRaidPopupPending() {
		boolean pending = raidPopupPending;
		raidPopupPending = false;
		return pending;
	}

	public String raidPopupText() {
		return "A revenge raid has reached the homebase!\n\n"
				+ raidMobDisplayName() + " raiders are coming in "
				+ raidWaves + (raidWaves == 1 ? " wave." : " waves.")
				+ "\nRaid threat spent: " + raidThreat + "."
				+ "\nDefend the camp before they overrun the settlement.";
	}

	public String raidProgressText() {
		return "Raid wave " + raidWave + "/" + raidWaves + ": "
				+ raidAlive() + "/" + Math.max( 1, raidWaveTotal ) + " raiders remain.";
	}

	public String raidVictoryText() {
		if (lastRaidRewardText == null || lastRaidRewardText.isEmpty()) {
			return "The homebase raid has been repelled.";
		}
		return lastRaidRewardText;
	}

	public String raidMobClassName() {
		if (raidMobClass == null || raidMobClass.isEmpty() || !raidMobClassUnlocked( raidMobClass )) {
			raidMobClass = fallbackRaidMobClass();
		}
		return raidMobClass;
	}

	public String raidMobClassNameForSpawn() {
		ArrayList<String> roster = raidMobRoster();
		return roster.isEmpty() ? raidMobClassName() : Random.element( roster );
	}

	public int raidWave() {
		return raidWave;
	}

	public int raidWaves() {
		return raidWaves;
	}

	public int raidWaveTotal() {
		return Math.max( 1, raidWaveTotal );
	}

	public int raidWaveSpawned() {
		return raidWaveSpawned;
	}

	public int raidAlive() {
		return Math.max( 0, raidWaveTotal - raidWaveKilled );
	}

	public boolean canSpawnRaidMob() {
		return raidActive && raidWaveSpawned < raidWaveTotal;
	}

	public boolean raidHasDamageTargets() {
		if (!raidActive) return false;
		for (Building building : Building.values()) {
			if (canRaidDamageBuilding( building )) {
				return true;
			}
		}
		return false;
	}

	public void recordRaidMobSpawned() {
		if (!raidActive) return;
		raidWaveSpawned = Math.min( raidWaveTotal, raidWaveSpawned + 1 );
	}

	public void recordRaidMobReinforcement() {
		if (!raidActive) return;
		raidWaveTotal++;
		raidWaveSpawned++;
		raidTotalMobs++;
	}

	public int recordRaidMobKilled() {
		if (!raidActive) return RAID_PROGRESS_ACTIVE;

		raidWaveKilled = Math.min( raidWaveTotal, raidWaveKilled + 1 );
		return resolveRaidWaveProgress();
	}

	public int reconcileRaidProgress( int liveTrackedEnemyRaiders ) {
		if (!raidActive) return RAID_PROGRESS_ACTIVE;

		liveTrackedEnemyRaiders = Math.max( 0, liveTrackedEnemyRaiders );
		int inactiveTrackedRaiders = Math.max( 0, raidWaveSpawned - liveTrackedEnemyRaiders );
		raidWaveKilled = Math.max( raidWaveKilled, Math.min( raidWaveTotal, inactiveTrackedRaiders ) );
		if (raidWaveSpawned < raidWaveTotal) {
			return RAID_PROGRESS_ACTIVE;
		}

		return resolveRaidWaveProgress();
	}

	private int resolveRaidWaveProgress() {
		if (raidWaveKilled < raidWaveTotal) {
			return RAID_PROGRESS_ACTIVE;
		}

		if (raidWave < raidWaves) {
			raidWave++;
			prepareRaidWave();
			return RAID_PROGRESS_NEXT_WAVE;
		}

		completeRaid();
		return RAID_PROGRESS_COMPLETE;
	}

	private void completeRaid() {
		raidActive = false;
		raidPopupPending = false;
		settleRaidBuildingDamage();

		int materialReward = Math.max( 3, raidTotalMobs / 2 + raidWaves * 2 );
		int scrapReward = Math.max( 1, raidTotalMobs / 3 + raidWaves );
		int emberReward = raidThreat >= 500 ? Math.max( 1, raidThreat / 250 ) : 0;

		add( Material.WOOD, materialReward );
		add( Material.STONE, Math.max( 2, materialReward * 2 / 3 ) );
		addForgeResource( ForgeResource.SCRAP, scrapReward );
		if (emberReward > 0) {
			addForgeResource( ForgeResource.EMBER_SHARD, emberReward );
		}
		int[] securedByDefenders = secureLooseHomebaseMaterials();
		String securedText = materialListText( securedByDefenders );

		lastRaidRewardText = "The raid breaks against the homebase. Recovered "
				+ materialReward + " wood, "
				+ Math.max( 2, materialReward * 2 / 3 ) + " stone, "
				+ scrapReward + " scrap"
				+ (emberReward > 0 ? ", and " + emberReward + " ember shards." : ".")
				+ (securedText.isEmpty() ? "" : " Defenders secured " + securedText + " from the battlefield.");
		raidWave = 0;
		raidWaves = 0;
		raidWaveTotal = 0;
		raidWaveSpawned = 0;
		raidWaveKilled = 0;
		raidTotalMobs = 0;
		raidMobClass = "";
		Dungeon.rollNextRaidThreatTarget();
	}

	private int[] secureLooseHomebaseMaterials() {
		int[] found = new int[Material.values().length];
		if (Dungeon.depth != 0 || Dungeon.level == null || Dungeon.level.heaps == null) return found;

		for (Heap heap : Dungeon.level.heaps.valueList().toArray( new Heap[0] )) {
			if (heap == null || heap.type != Heap.Type.HEAP) continue;
			for (Item item : heap.items.toArray( new Item[0] )) {
				if (item instanceof BuildingMaterial) {
					BuildingMaterial material = (BuildingMaterial)item;
					int amount = Math.max( 1, material.quantity() );
					add( material.material(), amount );
					found[material.material().ordinal()] += amount;
					heap.remove( item );
				}
			}
		}
		return found;
	}

	private String materialListText( int[] found ) {
		if (found == null || found.length == 0) return "";

		StringBuilder text = new StringBuilder();
		for (Material material : Material.values()) {
			int amount = material.ordinal() < found.length ? found[material.ordinal()] : 0;
			if (amount <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
		return text.toString();
	}

	private int revengeKillTotal() {
		int total = 0;
		for (int count : revengeKillCounts) {
			total += count;
		}
		return total;
	}

	private String chooseRaidMobClass() {
		String bestClass = null;
		int bestCount = -1;
		for (int i = 0; i < revengeKillClasses.size() && i < revengeKillCounts.size(); i++) {
			String className = revengeKillClasses.get( i );
			int count = revengeKillCounts.get( i );
			if (!excludedRaidMobClass( className ) && raidMobClassUnlocked( className ) && count > bestCount) {
				bestClass = className;
				bestCount = count;
			}
		}
		return bestClass == null ? fallbackRaidMobClass() : bestClass;
	}

	private String fallbackRaidMobClass() {
		return Random.element( fallbackRaidMobClasses() );
	}

	private ArrayList<String> raidMobRoster() {
		ArrayList<String> roster = new ArrayList<>();
		addRaidMobClass( roster, raidMobClassName() );
		for (String className : fallbackRaidMobClasses()) {
			addRaidMobClass( roster, className );
		}
		for (String className : new String[]{
				RAID_MOB_PREFIX + "Rat",
				RAID_MOB_PREFIX + "Gnoll",
				RAID_MOB_PREFIX + "Snake",
				RAID_MOB_PREFIX + "Thief",
				RAID_MOB_PREFIX + "Skeleton"}) {
			if (roster.size() >= 3) break;
			addRaidMobClass( roster, className );
		}
		return roster;
	}

	private void addRaidMobClass( ArrayList<String> roster, String className ) {
		if (excludedRaidMobClass( className ) || !raidMobClassUnlocked( className ) || roster.contains( className )) return;
		roster.add( className );
	}

	private String[] fallbackRaidMobClasses() {
		int deepest = raidUnlockedDepth();
		ArrayList<String> classes = new ArrayList<>();

		addRaidFallbackClass( classes, "Rat" );
		if (deepest >= 2) {
			addRaidFallbackClass( classes, "Gnoll" );
			addRaidFallbackClass( classes, "Snake" );
		}
		if (deepest >= 3) {
			addRaidFallbackClass( classes, "Swarm" );
			addRaidFallbackClass( classes, "Crab" );
		}
		if (deepest >= 4) {
			addRaidFallbackClass( classes, "Slime" );
		}

		if (deepest >= 6) {
			addRaidFallbackClass( classes, "Skeleton" );
			addRaidFallbackClass( classes, "Thief" );
		}
		if (deepest >= 7) {
			addRaidFallbackClass( classes, "DM100" );
			addRaidFallbackClass( classes, "Guard" );
		}
		if (deepest >= 8) {
			addRaidFallbackClass( classes, "Necromancer" );
		}

		if (deepest >= 11) {
			addRaidFallbackClass( classes, "Bat" );
			addRaidFallbackClass( classes, "Brute" );
		}
		if (deepest >= 12) {
			addRaidFallbackClass( classes, "Spinner" );
		}
		if (deepest >= 13) {
			addRaidFallbackClass( classes, "DM200" );
		}

		if (deepest >= 16) {
			addRaidFallbackClass( classes, "Ghoul" );
			addRaidFallbackClass( classes, "Warlock" );
		}
		if (deepest >= 17) {
			addRaidFallbackClass( classes, "Monk" );
		}
		if (deepest >= 18) {
			addRaidFallbackClass( classes, "Golem" );
		}

		if (deepest >= 21) {
			addRaidFallbackClass( classes, "Succubus" );
			addRaidFallbackClass( classes, "Eye" );
		}
		if (deepest >= 23) {
			addRaidFallbackClass( classes, "Scorpio" );
		}

		return classes.toArray( new String[0] );
	}

	private int raidUnlockedDepth() {
		return Math.max( 1, Math.min( 26, Statistics.deepestFloor ) );
	}

	private void addRaidFallbackClass( ArrayList<String> classes, String simpleName ) {
		String className = RAID_MOB_PREFIX + simpleName;
		if (!excludedRaidMobClass( className ) && !classes.contains( className )) {
			classes.add( className );
		}
	}

	private boolean raidMobClassUnlocked( String className ) {
		int minDepth = raidMobMinDepth( className );
		return minDepth <= 0 || raidUnlockedDepth() >= minDepth;
	}

	private int raidMobMinDepth( String className ) {
		if (className == null || className.isEmpty()) return 1;
		int dot = className.lastIndexOf( '.' );
		String simple = dot >= 0 ? className.substring( dot + 1 ) : className;
		int inner = simple.lastIndexOf( '$' );
		if (inner >= 0) simple = simple.substring( inner + 1 );

		if (simple.equals( "Rat" )) return 1;
		if (simple.equals( "Gnoll" ) || simple.equals( "Snake" )) return 2;
		if (simple.equals( "Swarm" ) || simple.equals( "Crab" )) return 3;
		if (simple.equals( "Slime" )) return 4;

		if (simple.equals( "Skeleton" ) || simple.equals( "Thief" )) return 6;
		if (simple.equals( "DM100" ) || simple.equals( "Guard" )) return 7;
		if (simple.equals( "Necromancer" )) return 8;

		if (simple.equals( "Bat" ) || simple.equals( "Brute" )) return 11;
		if (simple.equals( "Spinner" )) return 12;
		if (simple.equals( "DM200" )
				|| simple.equals( "RedShaman" )
				|| simple.equals( "BlueShaman" )
				|| simple.equals( "PurpleShaman" )) return 13;

		if (simple.equals( "Ghoul" ) || simple.equals( "Warlock" )) return 16;
		if (simple.equals( "Elemental" ) || simple.equals( "Monk" )) return 17;
		if (simple.equals( "Golem" )) return 18;

		if (simple.equals( "Succubus" ) || simple.equals( "Eye" )) return 21;
		if (simple.equals( "Scorpio" )) return 23;
		return 1;
	}

	private String[] prefixedMobClasses( String... simpleNames ) {
		String[] classes = new String[simpleNames.length];
		for (int i = 0; i < simpleNames.length; i++) {
			classes[i] = RAID_MOB_PREFIX + simpleNames[i];
		}
		return classes;
	}

	private String raidMobDisplayName() {
		String className = raidMobClassName();
		int dot = className.lastIndexOf( '.' );
		String simple = dot >= 0 ? className.substring( dot + 1 ) : className;
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < simple.length(); i++) {
			char c = simple.charAt( i );
			if (i > 0 && Character.isUpperCase( c )) {
				name.append( ' ' );
			}
			name.append( c );
		}
		return name.toString();
	}

	private static int materialValue( Material material ) {
		switch (material) {
			case WOOD:
				return 2;
			case STONE:
				return 3;
			case COPPER:
				return 7;
			case IRON:
				return 18;
			case GOLD:
			default:
				return 45;
		}
	}

	private static int forgeResourceValue( ForgeResource resource ) {
		switch (resource) {
			case SCRAP:
				return 8;
			case EMBER_SHARD:
				return 28;
			case EMBER_CORE:
			default:
				return 90;
		}
	}

	private boolean excludedRaidMobClass( String className ) {
		if (className == null || className.isEmpty()) return true;
		int dot = className.lastIndexOf( '.' );
		String simple = dot >= 0 ? className.substring( dot + 1 ) : className;
		return simple.equals( "RatKing" );
	}

	private void progressSettlementObjective( int objectiveType, int amount ) {
		if (amount <= 0) return;
		ensureSettlementRequests();
		for (SettlementRequest request : settlementRequests) {
			if (request != null && request.objectiveType() == objectiveType && request.progress() < request.amount()) {
				request.addProgress( amount );
				return;
			}
		}
	}

	private void ensureSettlementRequests() {
		int slots = campContractSlots();
		while (settlementRequests.size() < slots) {
			settlementRequests.add( randomSettlementRequest() );
		}
		while (settlementRequests.size() > slots) {
			settlementRequests.remove( settlementRequests.size() - 1 );
		}
		for (int i = 0; i < settlementRequests.size(); i++) {
			SettlementRequest request = settlementRequests.get( i );
			if (request == null || request.isLegacyObjective() || invalidSettlementRequest( request )) {
				settlementRequests.set( i, randomSettlementRequest() );
			}
		}
	}

	private boolean invalidSettlementRequest( SettlementRequest request ) {
		return request != null
				&& !request.progressObjective()
				&& settlementRewardValue( request ) <= settlementObjectiveValue( request );
	}

	private int settlementObjectiveValue( SettlementRequest request ) {
		switch (request.objectiveType()) {
			case SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return request.amount() * forgeResourceValue( request.objectiveForgeResource() );
			case SettlementRequest.OBJECTIVE_GOLD:
				return request.amount();
			case SettlementRequest.OBJECTIVE_ENERGY:
				return request.amount() * 35;
			case SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return request.amount() * materialValue( request.objectiveMaterial() );
		}
	}

	private int settlementRewardValue( SettlementRequest request ) {
		switch (request.rewardType()) {
			case SettlementRequest.REWARD_MATERIAL:
				return request.rewardAmount() * materialValue( request.rewardMaterial() );
			case SettlementRequest.REWARD_FORGE_RESOURCE:
				return request.rewardAmount() * forgeResourceValue( request.rewardForgeResource() );
			case SettlementRequest.REWARD_GOLD:
				return request.rewardAmount();
			case SettlementRequest.REWARD_ENERGY:
			default:
				return request.rewardAmount() * 35;
		}
	}

	private SettlementRequest randomSettlementRequest() {
		int campLevel = Math.max( 1, buildingLevel( Building.CAMP ) );
		int objectiveType = Random.chances( new float[]{
				3f,
				campLevel >= 2 ? 1.5f : 0.5f,
				1.5f,
				campLevel >= 2 ? 1.5f : 1f,
				3f,
				2.5f,
				2.5f
		} );
		int objectiveIndex = 0;
		int amount;
		int value;
		int tier = 0;

		switch (objectiveType) {
			case SettlementRequest.OBJECTIVE_BOUNTY:
				amount = Math.max( 3, Random.NormalIntRange( 4 + campLevel / 4, 7 + campLevel / 2 ) );
				value = amount * (9 + campLevel / 3);
				break;
			case SettlementRequest.OBJECTIVE_SCOUTING:
				amount = Math.min( 25, Math.max( 2, Random.NormalIntRange( 2 + campLevel / 8, 4 + campLevel / 4 ) ) );
				value = amount * (10 + campLevel / 4);
				break;
			case SettlementRequest.OBJECTIVE_RECOVERY:
				amount = Math.max( 3, Random.NormalIntRange( 4 + campLevel / 5, 8 + campLevel / 3 ) );
				value = amount * (8 + campLevel / 4);
				break;
			case SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				objectiveIndex = Random.chances( new float[]{
						5f,
						campLevel >= 3 ? 2f : 0.5f,
						campLevel >= 8 ? 1f : 0f
				} );
				ForgeResource resource = ForgeResource.values()[objectiveIndex];
				switch (resource) {
					case SCRAP:
						amount = Math.max( 1, Random.NormalIntRange( 3 + campLevel / 2, 7 + campLevel ) );
						value = amount * forgeResourceValue( resource );
						break;
					case EMBER_SHARD:
						amount = Math.max( 1, Random.NormalIntRange( 1 + campLevel / 6, 3 + campLevel / 3 ) );
						value = amount * forgeResourceValue( resource );
						break;
					case EMBER_CORE:
					default:
						amount = Math.max( 1, Random.NormalIntRange( 1, 1 + campLevel / 15 ) );
						value = amount * forgeResourceValue( resource );
						break;
				}
				break;
			case SettlementRequest.OBJECTIVE_GOLD:
				amount = Math.max( 10, Random.NormalIntRange( 18 + campLevel * 3, 36 + campLevel * 7 ) );
				value = amount;
				break;
			case SettlementRequest.OBJECTIVE_ENERGY:
				amount = Math.max( 1, Random.NormalIntRange( 1 + campLevel / 8, 3 + campLevel / 5 ) );
				value = amount * 35;
				break;
			case SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				objectiveType = SettlementRequest.OBJECTIVE_MATERIAL;
				float[] materialWeights = {
						4f,
						4f,
						campLevel >= 2 ? 3f : 1f,
						campLevel >= 5 ? 2f : 0f,
						campLevel >= 10 ? 1f : 0f
				};
				Material material = Material.values()[Random.chances( materialWeights )];
				objectiveIndex = material.ordinal();
				int targetValue = Random.NormalIntRange( 24 + campLevel * 4, 45 + campLevel * 7 );
				amount = Math.max( 1, Math.round( targetValue / (float)materialValue( material ) ) );
				value = amount * materialValue( material );
				break;
		}

		int rewardType = Random.chances( new float[]{2f, 3f, 4f, 2f} );
		int rewardIndex = 0;
		int rewardAmount;
		int rewardValue = Math.max(
				value + 1,
				Random.NormalIntRange( Math.round( value * 1.2f ), Math.round( value * 1.55f ) + campLevel )
		);
		switch (rewardType) {
			case SettlementRequest.REWARD_MATERIAL:
				float[] rewardMaterialWeights = new float[]{
						4f,
						4f,
						campLevel >= 2 ? 3f : 1f,
						campLevel >= 5 ? 2f : 0f,
						campLevel >= 10 ? 1f : 0f
				};
				if (objectiveType == SettlementRequest.OBJECTIVE_MATERIAL) {
					for (int i = 0; i < objectiveIndex && i < rewardMaterialWeights.length; i++) {
						rewardMaterialWeights[i] = 0f;
					}
				}
				rewardIndex = Random.chances( rewardMaterialWeights );
				rewardAmount = Math.max( 1, (int)Math.ceil( rewardValue / (float)materialValue( Material.values()[rewardIndex] ) ) );
				break;
			case SettlementRequest.REWARD_FORGE_RESOURCE:
				rewardIndex = Random.chances( new float[]{
						5f,
						campLevel >= 3 ? 2f : 0f,
						campLevel >= 8 ? 1f : 0f
				} );
				ForgeResource rewardResource = ForgeResource.values()[rewardIndex];
				switch (rewardResource) {
					case SCRAP:
						rewardAmount = Math.max( 1, (int)Math.ceil( rewardValue / (float)forgeResourceValue( rewardResource ) ) );
						break;
					case EMBER_SHARD:
						rewardAmount = Math.max( 1, (int)Math.ceil( rewardValue / (float)forgeResourceValue( rewardResource ) ) );
						break;
					case EMBER_CORE:
					default:
						rewardAmount = Math.max( 1, (int)Math.ceil( rewardValue / (float)forgeResourceValue( rewardResource ) ) );
						break;
				}
				break;
			case SettlementRequest.REWARD_GOLD:
				rewardAmount = Math.max( 5, rewardValue );
				break;
			case SettlementRequest.REWARD_ENERGY:
			default:
				rewardAmount = Math.max( 1, (int)Math.ceil( rewardValue / 35f ) );
				break;
		}

		return new SettlementRequest( objectiveType, objectiveIndex, amount, rewardType, rewardIndex, rewardAmount );
	}

	private void grantSettlementReward( SettlementRequest request ) {
		switch (request.rewardType()) {
			case SettlementRequest.REWARD_MATERIAL:
				add( request.rewardMaterial(), request.rewardAmount() );
				break;
			case SettlementRequest.REWARD_FORGE_RESOURCE:
				addForgeResource( request.rewardForgeResource(), request.rewardAmount() );
				break;
			case SettlementRequest.REWARD_GOLD:
				Dungeon.gold += request.rewardAmount();
				break;
			case SettlementRequest.REWARD_ENERGY:
				Dungeon.energy += request.rewardAmount();
				break;
		}
	}

	public ArrayList<Item> vaultItems() {
		return vaultItems;
	}

	public boolean canStoreInVault( Item item ) {
		if (item == null || vaultSlots() <= 0) return false;
		if (item.stackable) {
			for (Item stored : vaultItems) {
				if (item.isSimilar( stored )) return true;
			}
		}
		return vaultItems.size() < vaultSlots();
	}

	public boolean storeInVault( Item item ) {
		if (!canStoreInVault( item )) return false;
		if (item.stackable) {
			for (Item stored : vaultItems) {
				if (item.isSimilar( stored )) {
					stored.merge( item );
					return true;
				}
			}
		}
		vaultItems.add( item );
		return true;
	}

	public boolean removeFromVault( Item item ) {
		return vaultItems.remove( item );
	}

	public int buildingLevel( Building building ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			return derivedTowerLevel( building );
		}
		return buildingLevels[building.ordinal()];
	}

	public Building interactionBuilding( Building building ) {
		ensureBuildingState();
		if (!isTowerBuilding( building )) {
			return building;
		}

		Building first = firstTowerWall( building );
		Building second = secondTowerWall( building );
		if (first == null || second == null) {
			return building;
		}

		int firstLevel = rawWallLevelForTower( first );
		int secondLevel = rawWallLevelForTower( second );
		if (firstLevel != secondLevel) {
			return firstLevel <= secondLevel ? first : second;
		}

		float firstHealth = wallHealthRatio( first );
		float secondHealth = wallHealthRatio( second );
		return firstHealth <= secondHealth ? first : second;
	}

	public int maxBuildingLevel( Building building ) {
		if (isTowerBuilding( building )) {
			return Math.min( firstTowerWall( building ).maxLevel(), secondTowerWall( building ).maxLevel() );
		}
		return building.maxLevel();
	}

	public boolean isBuilt( Building building ) {
		return buildingLevel( building ) > 0;
	}

	private int rawBuildingLevel( Building building ) {
		return buildingLevels[building.ordinal()];
	}

	private boolean rawBuildingDestroyed( Building building ) {
		int index = building.ordinal();
		return buildingDestroyed[index] > 0 || (buildingLevels[index] > 0 && buildingHp[index] <= 0);
	}

	private int rawWallLevelForTower( Building wall ) {
		return rawBuildingDestroyed( wall ) ? 0 : rawBuildingLevel( wall );
	}

	private int derivedTowerLevel( Building tower ) {
		Building first = firstTowerWall( tower );
		Building second = secondTowerWall( tower );
		if (first == null || second == null) {
			return rawBuildingLevel( tower );
		}
		return Math.min( rawWallLevelForTower( first ), rawWallLevelForTower( second ) );
	}

	private float wallHealthRatio( Building wall ) {
		int max = buildingMaxHPRaw( wall );
		if (max <= 0 || rawWallLevelForTower( wall ) <= 0) {
			return 0f;
		}
		return Math.max( 0, buildingHp[wall.ordinal()] ) / (float)max;
	}

	private void ensureBuildingState() {
		if (buildingHp == null || buildingHp.length != Building.values().length) {
			int[] old = buildingHp;
			buildingHp = new int[Building.values().length];
			if (old != null) {
				System.arraycopy( old, 0, buildingHp, 0, Math.min( old.length, buildingHp.length ) );
			}
		}
		if (buildingDestroyed == null || buildingDestroyed.length != Building.values().length) {
			int[] old = buildingDestroyed;
			buildingDestroyed = new int[Building.values().length];
			if (old != null) {
				System.arraycopy( old, 0, buildingDestroyed, 0, Math.min( old.length, buildingDestroyed.length ) );
			}
		}
		int defenseLength = Building.values().length * BuildingDefense.values().length;
		if (buildingDefenseLevels == null || buildingDefenseLevels.length != defenseLength) {
			int[] old = buildingDefenseLevels;
			buildingDefenseLevels = new int[defenseLength];
			if (old != null) {
				System.arraycopy( old, 0, buildingDefenseLevels, 0, Math.min( old.length, buildingDefenseLevels.length ) );
			}
		}
		for (Building building : Building.values()) {
			int index = building.ordinal();
			if (buildingLevels[index] > 0 && buildingDestroyed[index] == 0 && buildingHp[index] <= 0) {
				buildingHp[index] = buildingMaxHPRaw( building );
			}
			if (buildingHp[index] > buildingMaxHPRaw( building )) {
				buildingHp[index] = buildingMaxHPRaw( building );
			}
		}
	}

	private int defenseIndex( Building building, BuildingDefense defense ) {
		return building.ordinal() * BuildingDefense.values().length + defense.ordinal();
	}

	private int buildingDefenseLevelRaw( Building building, BuildingDefense defense ) {
		if (buildingDefenseLevels == null || defenseIndex( building, defense ) >= buildingDefenseLevels.length) return 0;
		return buildingDefenseLevels[defenseIndex( building, defense )];
	}

	private int buildingDefenseBonusRaw( Building building, BuildingDefense defense ) {
		return Math.min( buildingDefenseLevelRaw( building, defense ), buildingDefenseHardCap( defense ) ) * defense.bonusPerLevel();
	}

	private int buildingMaxHPRaw( Building building ) {
		return BASE_BUILDING_HP + buildingDefenseBonusRaw( building, BuildingDefense.HP );
	}

	public int buildingHP( Building building ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			Building first = firstTowerWall( building );
			Building second = secondTowerWall( building );
			if (derivedTowerLevel( building ) <= 0 || first == null || second == null) return 0;
			return Math.min( buildingHP( first ), buildingHP( second ) );
		}
		return isBuilt( building ) ? Math.max( 0, buildingHp[building.ordinal()] ) : 0;
	}

	public int buildingMaxHP( Building building ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			Building first = firstTowerWall( building );
			Building second = secondTowerWall( building );
			if (first == null || second == null) return buildingMaxHPRaw( building );
			return Math.min( buildingMaxHP( first ), buildingMaxHP( second ) );
		}
		return buildingMaxHPRaw( building );
	}

	public int buildingArmor( Building building ) {
		return buildingDefenseBonus( building, BuildingDefense.ARMOR );
	}

	public boolean buildingDamaged( Building building ) {
		return isBuilt( building ) && buildingHP( building ) < buildingMaxHP( building );
	}

	public boolean buildingDestroyed( Building building ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			Building first = firstTowerWall( building );
			Building second = secondTowerWall( building );
			return first != null
					&& second != null
					&& rawBuildingLevel( first ) > 0
					&& rawBuildingLevel( second ) > 0
					&& (rawBuildingDestroyed( first ) || rawBuildingDestroyed( second ));
		}
		return buildingDestroyed[building.ordinal()] > 0 || (isBuilt( building ) && buildingHp[building.ordinal()] <= 0);
	}

	public int buildingDefenseLevel( Building building, BuildingDefense defense ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			Building first = firstTowerWall( building );
			Building second = secondTowerWall( building );
			if (first == null || second == null) return buildingDefenseLevelRaw( building, defense );
			return Math.min( buildingDefenseLevel( first, defense ), buildingDefenseLevel( second, defense ) );
		}
		return buildingDefenseLevelRaw( building, defense );
	}

	public int buildingDefenseCap( Building building, BuildingDefense defense ) {
		return Math.min( defense.cap( buildingLevel( building ) ), buildingDefenseHardCap( defense ) );
	}

	public int buildingDefenseBonus( Building building, BuildingDefense defense ) {
		ensureBuildingState();
		if (isTowerBuilding( building )) {
			Building first = firstTowerWall( building );
			Building second = secondTowerWall( building );
			if (first == null || second == null) return buildingDefenseBonusRaw( building, defense );
			return Math.min( buildingDefenseBonus( first, defense ), buildingDefenseBonus( second, defense ) );
		}
		return buildingDefenseBonusRaw( building, defense );
	}

	public int buildingDefenseHardCap( BuildingDefense defense ) {
		switch (defense) {
			case HP:
				return 100;
			case ARMOR:
				return 75;
			default:
				return 50;
		}
	}

	public boolean canEverUpgradeBuildingDefense( BuildingDefense defense ) {
		return buildingDefenseHardCap( defense ) > 0;
	}

	public boolean canUpgradeBuildingDefense( Building building, BuildingDefense defense ) {
		if (isTowerBuilding( building )) return false;
		if (!isBuilt( building )) return false;
		if (!canEverUpgradeBuildingDefense( defense )) return false;
		if (buildingDefenseLevel( building, defense ) >= buildingDefenseCap( building, defense )) return false;
		for (Material material : Material.values()) {
			if (amount( material ) < buildingDefenseCost( building, defense, material )) {
				return false;
			}
		}
		if (goldAmount() < buildingDefenseGoldCost( building, defense )) return false;
		if (energyAmount() < buildingDefenseEnergyCost( building, defense )) return false;
		return true;
	}

	public boolean upgradeBuildingDefense( Building building, BuildingDefense defense ) {
		if (!canUpgradeBuildingDefense( building, defense )) return false;

		int oldMax = buildingMaxHP( building );
		if (!INFINITE_TEST_RESOURCES) {
			for (Material material : Material.values()) {
				amounts[material.ordinal()] -= buildingDefenseCost( building, defense, material );
			}
			Dungeon.gold -= buildingDefenseGoldCost( building, defense );
			Dungeon.energy -= buildingDefenseEnergyCost( building, defense );
		}
		buildingDefenseLevels[defenseIndex( building, defense )]++;
		if (defense == BuildingDefense.HP) {
			ensureBuildingState();
			buildingHp[building.ordinal()] += Math.max( 0, buildingMaxHP( building ) - oldMax );
		}
		return true;
	}

	public int buildingDefenseCost( Building building, BuildingDefense defense, Material material ) {
		int next = buildingDefenseLevel( building, defense ) + 1;
		int wood = 0;
		int stone = 0;
		int copper = 0;
		int iron = 0;
		int gold = 0;

		switch (defense) {
			case HP:
				wood = 4 + next * 3;
				stone = 3 + next * 2;
				copper = next >= 4 ? next - 2 : 0;
				iron = next >= 8 ? 1 + next / 4 : 0;
				break;
			case ARMOR:
				stone = 5 + next * 3;
				copper = 2 + next * 2;
				iron = next >= 4 ? next : 0;
				gold = next >= 10 ? next / 5 : 0;
				break;
			default:
				wood = 2 + next;
				stone = 2 + next;
				copper = next >= 2 ? next : 0;
				iron = next >= 6 ? next / 2 : 0;
				gold = next >= 12 ? next / 4 : 0;
				break;
		}

		switch (material) {
			case WOOD:
				return wood;
			case STONE:
				return stone;
			case COPPER:
				return copper;
			case IRON:
				return iron;
			case GOLD:
			default:
				return gold;
		}
	}

	public int buildingDefenseGoldCost( Building building, BuildingDefense defense ) {
		int next = buildingDefenseLevel( building, defense ) + 1;
		return 8 + next * 7 + building.ordinal() * 2;
	}

	public int buildingDefenseEnergyCost( Building building, BuildingDefense defense ) {
		int next = buildingDefenseLevel( building, defense ) + 1;
		switch (defense) {
			case HP:
			case ARMOR:
				return next >= 5 ? next / 3 : 0;
			default:
				return next >= 3 ? Math.max( 1, next / 2 ) : 0;
		}
	}

	public int repairCost( Building building, Material material ) {
		if (isTowerBuilding( building )) return 0;
		if (!isBuilt( building ) && !buildingDestroyed( building )) return 0;
		int missing = Math.max( 0, buildingMaxHP( building ) - buildingHP( building ) );
		if (missing <= 0 && !buildingDestroyed( building )) return 0;
		int baseCost = building.cost( repairCostLevel( building ), material );
		if (baseCost <= 0) return 0;
		float ratio = buildingDestroyed( building ) ? 1f : missing / (float)Math.max( 1, buildingMaxHP( building ) );
		int raw = Math.max( 1, Math.round( baseCost * ratio * (buildingDestroyed( building ) ? 1.35f : 0.85f) ) );
		return Math.min( baseCost, raw );
	}

	public int repairGoldCost( Building building ) {
		if (isTowerBuilding( building )) return 0;
		if (!isBuilt( building ) && !buildingDestroyed( building )) return 0;
		int missing = Math.max( 0, buildingMaxHP( building ) - buildingHP( building ) );
		if (missing <= 0 && !buildingDestroyed( building )) return 0;
		int baseCost = goldCost( building, repairCostLevel( building ) );
		if (baseCost <= 0) return 0;
		float ratio = buildingDestroyed( building ) ? 1f : missing / (float)Math.max( 1, buildingMaxHP( building ) );
		int raw = Math.max( 1, Math.round( baseCost * ratio * (buildingDestroyed( building ) ? 1.35f : 0.85f) ) );
		return Math.min( baseCost, raw );
	}

	public int repairEnergyCost( Building building ) {
		if (isTowerBuilding( building )) return 0;
		if (!isBuilt( building ) && !buildingDestroyed( building )) return 0;
		int missing = Math.max( 0, buildingMaxHP( building ) - buildingHP( building ) );
		if (missing <= 0 && !buildingDestroyed( building )) return 0;
		int baseCost = energyCost( building, repairCostLevel( building ) );
		if (baseCost <= 0) return 0;
		float ratio = buildingDestroyed( building ) ? 1f : missing / (float)Math.max( 1, buildingMaxHP( building ) );
		int raw = Math.max( 1, Math.round( baseCost * ratio * (buildingDestroyed( building ) ? 1.35f : 0.85f) ) );
		return Math.min( baseCost, raw );
	}

	private int repairCostLevel( Building building ) {
		return Math.max( 1, buildingLevel( building ) );
	}

	public boolean canRepair( Building building ) {
		if (isTowerBuilding( building )) return false;
		if (!buildingDamaged( building ) && !buildingDestroyed( building )) return false;
		for (Material material : Material.values()) {
			if (amount( material ) < repairCost( building, material )) return false;
		}
		if (goldAmount() < repairGoldCost( building )) return false;
		if (energyAmount() < repairEnergyCost( building )) return false;
		return true;
	}

	public boolean repair( Building building ) {
		if (isTowerBuilding( building )) return false;
		if (!canRepair( building )) return false;
		if (!INFINITE_TEST_RESOURCES) {
			for (Material material : Material.values()) {
				amounts[material.ordinal()] -= repairCost( building, material );
			}
			Dungeon.gold -= repairGoldCost( building );
			Dungeon.energy -= repairEnergyCost( building );
		}
		if (buildingLevel( building ) <= 0) {
			buildingLevels[building.ordinal()] = 1;
		}
		buildingDestroyed[building.ordinal()] = 0;
		buildingHp[building.ordinal()] = buildingMaxHP( building );
		vaultLevel = buildingLevel( Building.VAULT );
		return true;
	}

	private Building towerDamageTarget( Building tower ) {
		Building first = firstTowerWall( tower );
		Building second = secondTowerWall( tower );
		if (first == null || second == null || derivedTowerLevel( tower ) <= 0) {
			return null;
		}

		boolean firstDamageable = canRaidDamageBuilding( first );
		boolean secondDamageable = canRaidDamageBuilding( second );
		if (firstDamageable && !secondDamageable) return first;
		if (secondDamageable && !firstDamageable) return second;
		if (!firstDamageable) return null;

		int firstLevel = rawWallLevelForTower( first );
		int secondLevel = rawWallLevelForTower( second );
		if (firstLevel != secondLevel) {
			return firstLevel <= secondLevel ? first : second;
		}

		float firstHealth = wallHealthRatio( first );
		float secondHealth = wallHealthRatio( second );
		return firstHealth <= secondHealth ? first : second;
	}

	public boolean canRaidDamageBuilding( Building building ) {
		if (isTowerBuilding( building )) {
			Building target = towerDamageTarget( building );
			return target != null && canRaidDamageBuilding( target );
		}
		return isBuilt( building ) && buildingHP( building ) > 0;
	}

	public int damageBuilding( Building building, int rawDamage ) {
		if (isTowerBuilding( building )) {
			Building target = towerDamageTarget( building );
			return target == null ? 0 : damageBuilding( target, rawDamage );
		}
		if (!canRaidDamageBuilding( building ) || rawDamage <= 0) return 0;
		int damage = Math.max( 1, rawDamage - buildingArmor( building ) );
		int index = building.ordinal();
		buildingHp[index] = Math.max( 0, buildingHp[index] - damage );
		if (buildingHp[index] <= 0) {
			buildingDestroyed[index] = 1;
		}
		return damage;
	}

	private void settleRaidBuildingDamage() {
		ensureBuildingState();
		for (Building building : Building.values()) {
			if (isTowerBuilding( building )) continue;
			int index = building.ordinal();
			if (buildingLevels[index] > 0 && buildingHp[index] <= 0) {
				buildingLevels[index] = Math.max( 0, buildingLevels[index] - 1 );
				buildingDestroyed[index] = 1;
				buildingHp[index] = 0;
			}
		}
		vaultLevel = buildingLevel( Building.VAULT );
	}

	public int cost( Building building, Material material ) {
		if (isTowerBuilding( building )) return 0;
		return building.cost( buildingLevel( building ) + 1, material );
	}

	public int goldCost( Building building ) {
		if (isTowerBuilding( building )) return 0;
		return goldCost( building, buildingLevel( building ) + 1 );
	}

	private int goldCost( Building building, int targetLevel ) {
		switch (building) {
			case CAMP:
				return 15 + targetLevel * 10;
			case VAULT:
				return 20 + targetLevel * 18;
			case FORGE:
				return 25 + targetLevel * 20;
			case ALCHEMY:
				return 12 + targetLevel * 12;
			case NORTH_WALL:
			case EAST_WALL:
			case SOUTH_WALL:
			case WEST_WALL:
				return 8 + targetLevel * 8;
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return 12 + targetLevel * 12;
			case GARDEN:
			default:
				return 10 + targetLevel * 10;
		}
	}

	public int energyCost( Building building ) {
		if (isTowerBuilding( building )) return 0;
		return energyCost( building, buildingLevel( building ) + 1 );
	}

	private int energyCost( Building building, int targetLevel ) {
		switch (building) {
			case ALCHEMY:
				return Math.max( 1, targetLevel * 2 );
			case FORGE:
				return targetLevel >= 2 ? targetLevel : 0;
			case GARDEN:
				return targetLevel >= 3 ? targetLevel - 1 : 0;
			case VAULT:
				return targetLevel >= 4 ? targetLevel - 2 : 0;
			case NORTH_WALL:
			case EAST_WALL:
			case SOUTH_WALL:
			case WEST_WALL:
				return targetLevel >= 5 ? targetLevel / 2 : 0;
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return targetLevel >= 3 ? targetLevel - 2 : 0;
			case CAMP:
			default:
				return 0;
		}
	}

	public boolean canBuild( Building building ) {
		if (isTowerBuilding( building )) return false;
		return !isBuilt( building ) && canUpgrade( building );
	}

	public boolean canUpgrade( Building building ) {
		if (isTowerBuilding( building )) return false;
		if (buildingDestroyed( building ) || buildingDamaged( building )) return false;
		if (buildingLevel( building ) >= maxBuildingLevel( building )) return false;
		for (Material material : Material.values()) {
			if (amount( material ) < cost( building, material )) {
				return false;
			}
		}
		if (goldAmount() < goldCost( building )) return false;
		if (energyAmount() < energyCost( building )) return false;
		return true;
	}

	public boolean build( Building building ) {
		if (isTowerBuilding( building )) return false;
		if (isBuilt( building )) return false;
		return upgrade( building );
	}

	public boolean upgrade( Building building ) {
		if (isTowerBuilding( building )) return false;
		if (!canUpgrade( building )) return false;

		if (!INFINITE_TEST_RESOURCES) {
			for (Material material : Material.values()) {
				amounts[material.ordinal()] -= cost( building, material );
			}
			Dungeon.gold -= goldCost( building );
			Dungeon.energy -= energyCost( building );
		}

		buildingLevels[building.ordinal()]++;
		ensureBuildingState();
		buildingDestroyed[building.ordinal()] = 0;
		if (buildingHp[building.ordinal()] <= 0) {
			buildingHp[building.ordinal()] = buildingMaxHP( building );
		}
		vaultLevel = buildingLevel( Building.VAULT );
		return true;
	}

	public String costText( Building building ) {
		StringBuilder text = new StringBuilder();
		for (Material material : Material.values()) {
			int cost = cost( building, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
		if (goldCost( building ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( goldCost( building ) ).append( " gold" );
		}
		if (energyCost( building ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( energyCost( building ) ).append( " energy" );
		}
		return text.toString();
	}

	public int trainingLevel( Training training ) {
		return trainingLevels[training.ordinal()];
	}

	public int trainingCap( Training training ) {
		return Math.min( training.cap( buildingLevel( training.building() ) ), trainingHardCap( training ) );
	}

	public int trainingBonus( Training training ) {
		return Math.min( trainingLevel( training ), trainingHardCap( training ) ) * training.bonusPerLevel();
	}

	public int permanentMobLevelPressureBonus() {
		ensureBuildingState();

		int buildingTotal = 0;
		for (Building building : Building.values()) {
			if (!isTowerBuilding( building )) {
				buildingTotal += Math.max( 0, buildingLevel( building ) );
			}
		}

		int trainingTotal = 0;
		for (Training training : Training.values()) {
			trainingTotal += Math.max( 0, Math.min( trainingLevel( training ), trainingHardCap( training ) ) );
		}

		return Math.min( 9999, buildingTotal / 4 + trainingTotal / 8 );
	}

	public int trainingHardCap( Training training ) {
		switch (training) {
			case TALENT_POINT:
				return talentTierExtraCap( 1 );
			case TALENT_TIER_2:
				return talentTierExtraCap( 2 );
			case TALENT_TIER_3:
				return talentTierExtraCap( 3 );
			case TALENT_TIER_4:
				return talentTierExtraCap( 4 );
			case ELEMENTAL_RESISTANCE:
			case TENACITY:
			case CRITICAL_CHANCE:
			case DODGE_CHANCE:
			case BLOCK_CHANCE:
			case THORNS_CHANCE:
			case STATUS_PROC_CHANCE:
			case CLEAVE_CHANCE:
			case PIERCING_CHANCE:
			case LIGHTNING_CHANCE:
			case BLEED_PROC:
			case STUN_CHANCE:
			case BURNING_PROC:
			case CORROSION_PROC:
			case HEX_PROC:
			case FROST_PROC:
			case POISON_PROC:
			case ROOT_PROC:
			case SLOW_PROC:
			case VERTIGO_PROC:
			case BLINDNESS_PROC:
			case CRIPPLE_PROC:
			case DAZE_PROC:
			case VULNERABLE_PROC:
			case WEAKNESS_PROC:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case BLEED_RESISTANCE:
			case BLINDNESS_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case DAZE_RESISTANCE:
			case HEX_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
			case CHARM_RESISTANCE:
			case TERROR_RESISTANCE:
			case DREAD_RESISTANCE:
			case SLEEP_RESISTANCE:
			case AMOK_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
			case CHILL_RESISTANCE:
			case OOZE_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
				return 50;
			case KNOCKBACK_CHANCE:
			case BARRIER_GUARD:
				return 25;
			case KNOCKBACK_STRENGTH:
				return 5;
			case LIFESTEAL:
				return 25;
			case ARMOR_BONUS:
			case STATUS_DURATION:
			case MAGIC_POWER:
			case BLEED_DURATION:
			case STUN_DURATION:
			case BURNING_DURATION:
			case CORROSION_DURATION:
			case HEX_DURATION:
			case FROST_DURATION:
			case POISON_DURATION:
			case ROOT_DURATION:
			case SLOW_DURATION:
			case VERTIGO_DURATION:
			case BLINDNESS_DURATION:
			case CRIPPLE_DURATION:
			case DAZE_DURATION:
			case VULNERABLE_DURATION:
			case WEAKNESS_DURATION:
				return 40;
			case GOLD_GAIN:
			case BONUS_LOOT:
			case RESOURCE_YIELD:
				return 40;
			case MATERIAL_CACHE_SIZE:
				return 20;
			case CATALYST_DROP_RATE:
			case CRITICAL_DAMAGE:
				return 30;
			case THORNS_DAMAGE:
			case BARRIER_POWER:
			case MAGIC_DAMAGE:
				return 50;
			default:
				return Integer.MAX_VALUE;
		}
	}

	public boolean canEverTrain( Training training ) {
		return trainingHardCap( training ) > 0;
	}

	private int talentTierExtraCap( int tier ) {
		int naturalPoints = talentTierNaturalPoints( tier );
		int totalPoints = talentTierTotalPoints( tier );
		return Math.max( 0, totalPoints - naturalPoints );
	}

	private int talentTierNaturalPoints( int tier ) {
		switch (tier) {
			case 1:
				return 5; // Talent.tierLevelThresholds[2] - Talent.tierLevelThresholds[1]
			case 2:
				return 6; // Talent.tierLevelThresholds[3] - Talent.tierLevelThresholds[2]
			case 3:
				return 8; // Talent.tierLevelThresholds[4] - Talent.tierLevelThresholds[3]
			case 4:
				return 10; // Talent.tierLevelThresholds[5] - Talent.tierLevelThresholds[4]
			default:
				return 0;
		}
	}

	private int talentTierTotalPoints( int tier ) {
		switch (tier) {
			case 1:
				return 8; // four class talents with two ranks each
			case 2:
				return 10; // five class talents with two ranks each
			case 3:
				return 15; // two class talents plus three subclass talents, each with three ranks
			case 4:
				return 16; // four armor ability talents with four total ranks each
			default:
				return 0;
		}
	}

	public boolean canTrain( Training training ) {
		if (!canEverTrain( training )) return false;
		if (trainingLevel( training ) >= trainingCap( training )) return false;
		for (Material material : Material.values()) {
			if (amount( material ) < trainingCost( training, material )) {
				return false;
			}
		}
		if (goldAmount() < trainingGoldCost( training )) return false;
		if (energyAmount() < trainingEnergyCost( training )) return false;
		return true;
	}

	public boolean train( Training training ) {
		if (!canTrain( training )) return false;

		if (!INFINITE_TEST_RESOURCES) {
			for (Material material : Material.values()) {
				amounts[material.ordinal()] -= trainingCost( training, material );
			}
			Dungeon.gold -= trainingGoldCost( training );
			Dungeon.energy -= trainingEnergyCost( training );
		}
		trainingLevels[training.ordinal()]++;
		return true;
	}

	public int trainingCost( Training training, Material material ) {
		int next = trainingLevel( training ) + 1;
		int wood = 0;
		int stone = 0;
		int copper = 0;
		int iron = 0;
		int gold = 0;

		switch (training) {
			case HEALTH:
				wood = 4 + next * 3;
				stone = 2 + next * 2;
				copper = next >= 4 ? next - 2 : 0;
				iron = next >= 7 ? 1 : 0;
				break;
			case TALENT_POINT:
				wood = 10 + next * 5;
				stone = 10 + next * 5;
				copper = 6 + next * 4;
				iron = Math.max( 1, next * 2 );
				gold = Math.max( 1, next );
				break;
			case TALENT_TIER_2:
				wood = 14 + next * 6;
				stone = 14 + next * 6;
				copper = 8 + next * 5;
				iron = 2 + next * 2;
				gold = 1 + next;
				break;
			case TALENT_TIER_3:
				wood = 18 + next * 8;
				stone = 18 + next * 8;
				copper = 12 + next * 6;
				iron = 4 + next * 3;
				gold = 2 + next * 2;
				break;
			case TALENT_TIER_4:
				wood = 24 + next * 10;
				stone = 24 + next * 10;
				copper = 16 + next * 8;
				iron = 6 + next * 4;
				gold = 4 + next * 3;
				break;
			case STRENGTH:
				stone = 6 + next * 4;
				copper = 4 + next * 4;
				iron = Math.max( 1, next * 2 );
				gold = next >= 3 ? next - 2 : 0;
				break;
			case ARMOR:
				stone = 7 + next * 4;
				copper = 3 + next * 3;
				iron = Math.max( 1, next * 2 );
				gold = next >= 4 ? next - 3 : 0;
				break;
			case ACCURACY:
				wood = 2 + next * 2;
				stone = 2 + next * 2;
				copper = 3 + next * 3;
				iron = next >= 5 ? next - 3 : 0;
				gold = next >= 7 ? 1 : 0;
				break;
			case WAND_RECHARGE:
				wood = 4 + next * 2;
				stone = 4 + next * 2;
				copper = 5 + next * 4;
				iron = next >= 3 ? next : 0;
				gold = next >= 5 ? 1 + next / 2 : 0;
				break;
			case EVASION:
				wood = 5 + next * 4;
				stone = 2 + next;
				copper = next >= 3 ? next : 0;
				iron = next >= 6 ? 1 : 0;
				break;
			case MOVEMENT_SPEED:
				wood = 8 + next * 5;
				stone = 3 + next * 2;
				copper = 3 + next * 2;
				iron = next >= 4 ? 1 + next / 2 : 0;
				break;
			case TREASURE_LUCK:
				wood = 6 + next * 3;
				stone = 8 + next * 4;
				copper = 5 + next * 3;
				iron = next >= 3 ? next : 0;
				gold = next >= 2 ? next : 0;
				break;
			case ATTACK_DAMAGE:
				stone = 8 + next * 5;
				copper = 6 + next * 4;
				iron = Math.max( 1, next * 2 );
				gold = next >= 4 ? next - 2 : 0;
				break;
			case ATTACK_SPEED:
				wood = 4 + next * 2;
				copper = 8 + next * 4;
				iron = Math.max( 1, next * 2 );
				gold = next >= 4 ? next : 0;
				break;
			case ARMOR_ABILITY_CHARGE:
				stone = 10 + next * 5;
				copper = 8 + next * 4;
				iron = 2 + next * 3;
				gold = next >= 4 ? next : 0;
				break;
			case WAND_DAMAGE:
				wood = 4 + next * 2;
				stone = 6 + next * 3;
				copper = 8 + next * 4;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case WAND_CHARGES:
				wood = 8 + next * 4;
				stone = 8 + next * 4;
				copper = 10 + next * 5;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case ENCHANTMENT_POWER:
				wood = 5 + next * 2;
				stone = 8 + next * 4;
				copper = 10 + next * 4;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case ELEMENTAL_RESISTANCE:
				wood = 6 + next * 3;
				stone = 10 + next * 5;
				copper = 6 + next * 3;
				iron = next >= 3 ? next : 0;
				gold = next >= 5 ? 1 + next / 2 : 0;
				break;
			case RANGED_DAMAGE:
				wood = 8 + next * 5;
				stone = 4 + next * 2;
				copper = 6 + next * 3;
				iron = next >= 4 ? next : 0;
				break;
			case THROWN_DURABILITY:
				wood = 10 + next * 5;
				stone = 5 + next * 3;
				copper = 4 + next * 2;
				iron = next >= 5 ? next : 0;
				break;
			case TRINKET_POTENCY:
				wood = 12 + next * 6;
				stone = 8 + next * 4;
				copper = 8 + next * 4;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case TENACITY:
				wood = 10 + next * 5;
				stone = 8 + next * 4;
				copper = 4 + next * 3;
				iron = next >= 4 ? next : 0;
				break;
			case XP_GAIN:
				wood = 8 + next * 4;
				stone = 8 + next * 4;
				copper = 6 + next * 3;
				iron = next >= 3 ? next : 0;
				gold = next >= 4 ? next : 0;
				break;
			case ARTIFACT_RECHARGE:
				wood = 6 + next * 3;
				stone = 10 + next * 5;
				copper = 8 + next * 4;
				iron = Math.max( 1, next * 2 );
				gold = next >= 3 ? next : 0;
				break;
			case RING_POTENCY:
				wood = 8 + next * 4;
				stone = 12 + next * 6;
				copper = 8 + next * 4;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case ARTIFACT_POTENCY:
				wood = 8 + next * 4;
				stone = 14 + next * 6;
				copper = 10 + next * 5;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case GOLD_GAIN:
				wood = 8 + next * 4;
				stone = 10 + next * 5;
				copper = 8 + next * 4;
				iron = next >= 3 ? next : 0;
				gold = 2 + next * 2;
				break;
			case CATALYST_DROP_RATE:
				wood = 10 + next * 4;
				stone = 12 + next * 5;
				copper = 10 + next * 5;
				iron = 2 + next * 2;
				gold = next >= 4 ? next * 2 : next;
				break;
			case CRITICAL_CHANCE:
				stone = 10 + next * 5;
				copper = 10 + next * 5;
				iron = 2 + next * 2;
				gold = next >= 3 ? next : 0;
				break;
			case CRITICAL_DAMAGE:
				stone = 12 + next * 5;
				copper = 12 + next * 5;
				iron = 3 + next * 2;
				gold = next >= 3 ? next * 2 : next;
				break;
			case LIFESTEAL:
				wood = 10 + next * 4;
				stone = 8 + next * 4;
				copper = 10 + next * 4;
				iron = next >= 3 ? next * 2 : 0;
				gold = next >= 5 ? next : 0;
				break;
			case DODGE_CHANCE:
				wood = 12 + next * 5;
				stone = 6 + next * 3;
				copper = 8 + next * 4;
				iron = next >= 4 ? next : 0;
				break;
			case BLOCK_CHANCE:
				stone = 12 + next * 5;
				copper = 8 + next * 4;
				iron = 2 + next * 2;
				gold = next >= 4 ? next : 0;
				break;
			case BONUS_LOOT:
				wood = 10 + next * 4;
				stone = 12 + next * 5;
				copper = 8 + next * 4;
				iron = next >= 4 ? next : 0;
				gold = next >= 3 ? next : 0;
				break;
			case RESOURCE_YIELD:
				wood = 12 + next * 5;
				stone = 12 + next * 5;
				copper = 8 + next * 4;
				iron = next >= 4 ? next : 0;
				gold = next >= 6 ? next : 0;
				break;
			case MATERIAL_CACHE_SIZE:
				wood = 10 + next * 4;
				stone = 14 + next * 6;
				copper = 10 + next * 4;
				iron = next >= 4 ? next : 0;
				gold = next >= 6 ? next : 0;
				break;
			case ARMOR_BONUS:
				stone = 14 + next * 6;
				copper = 8 + next * 4;
				iron = 2 + next * 2;
				gold = next >= 4 ? next : 0;
				break;
			case THORNS_CHANCE:
			case THORNS_DAMAGE:
			case BARRIER_GUARD:
			case BARRIER_POWER:
				wood = 8 + next * 4;
				stone = 14 + next * 6;
				copper = 10 + next * 4;
				iron = 2 + next * 2;
				gold = next >= 4 ? next : 0;
				break;
			case KNOCKBACK_CHANCE:
			case KNOCKBACK_STRENGTH:
			case CLEAVE_CHANCE:
			case PIERCING_CHANCE:
				stone = 14 + next * 6;
				copper = 12 + next * 5;
				iron = 3 + next * 2;
				gold = next >= 4 ? next : 0;
				break;
			case STATUS_PROC_CHANCE:
			case STATUS_DURATION:
			case BLEED_PROC:
			case BLEED_DURATION:
			case STUN_CHANCE:
			case STUN_DURATION:
			case BURNING_PROC:
			case BURNING_DURATION:
			case CORROSION_PROC:
			case CORROSION_DURATION:
			case HEX_PROC:
			case HEX_DURATION:
			case FROST_PROC:
			case FROST_DURATION:
			case POISON_PROC:
			case POISON_DURATION:
			case ROOT_PROC:
			case ROOT_DURATION:
			case SLOW_PROC:
			case SLOW_DURATION:
			case VERTIGO_PROC:
			case VERTIGO_DURATION:
			case BLINDNESS_PROC:
			case BLINDNESS_DURATION:
			case CRIPPLE_PROC:
			case CRIPPLE_DURATION:
			case DAZE_PROC:
			case DAZE_DURATION:
			case VULNERABLE_PROC:
			case VULNERABLE_DURATION:
			case WEAKNESS_PROC:
			case WEAKNESS_DURATION:
			case MAGIC_DAMAGE:
			case MAGIC_POWER:
			case LIGHTNING_CHANCE:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case BLEED_RESISTANCE:
			case BLINDNESS_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case DAZE_RESISTANCE:
			case HEX_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
			case CHARM_RESISTANCE:
			case TERROR_RESISTANCE:
			case DREAD_RESISTANCE:
			case SLEEP_RESISTANCE:
			case AMOK_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
			case CHILL_RESISTANCE:
			case OOZE_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
				wood = 8 + next * 4;
				stone = 10 + next * 5;
				copper = 14 + next * 5;
				iron = 2 + next * 2;
				gold = next >= 4 ? next : 0;
				break;
		}

		switch (material) {
			case WOOD:   return wood;
			case STONE:  return stone;
			case COPPER: return copper;
			case IRON:   return iron;
			case GOLD:   return gold;
			default:     return 0;
		}
	}

	public int trainingGoldCost( Training training ) {
		int next = trainingLevel( training ) + 1;
		switch (training) {
			case TALENT_POINT:
				return 40 + next * 25;
			case TALENT_TIER_2:
				return 65 + next * 35;
			case TALENT_TIER_3:
				return 90 + next * 45;
			case HEALTH:
			case STRENGTH:
			case ACCURACY:
			case EVASION:
			case ARMOR:
			case ATTACK_DAMAGE:
			case ATTACK_SPEED:
			case RANGED_DAMAGE:
			case THROWN_DURABILITY:
			case TENACITY:
			case XP_GAIN:
			case TREASURE_LUCK:
			case GOLD_GAIN:
			case CATALYST_DROP_RATE:
			case BONUS_LOOT:
			case RESOURCE_YIELD:
			case MATERIAL_CACHE_SIZE:
			case CRITICAL_CHANCE:
			case CRITICAL_DAMAGE:
			case LIFESTEAL:
			case DODGE_CHANCE:
			case BLOCK_CHANCE:
			case ARMOR_BONUS:
			case THORNS_CHANCE:
			case THORNS_DAMAGE:
			case KNOCKBACK_CHANCE:
			case KNOCKBACK_STRENGTH:
			case CLEAVE_CHANCE:
			case PIERCING_CHANCE:
			case BARRIER_GUARD:
			case BARRIER_POWER:
			case RING_POTENCY:
				return 10 + next * 8;
			case STATUS_PROC_CHANCE:
			case STATUS_DURATION:
			case BLEED_PROC:
			case BLEED_DURATION:
			case STUN_CHANCE:
			case STUN_DURATION:
			case BURNING_PROC:
			case BURNING_DURATION:
			case CORROSION_PROC:
			case CORROSION_DURATION:
			case HEX_PROC:
			case HEX_DURATION:
			case FROST_PROC:
			case FROST_DURATION:
			case POISON_PROC:
			case POISON_DURATION:
			case ROOT_PROC:
			case ROOT_DURATION:
			case SLOW_PROC:
			case SLOW_DURATION:
			case VERTIGO_PROC:
			case VERTIGO_DURATION:
			case BLINDNESS_PROC:
			case BLINDNESS_DURATION:
			case CRIPPLE_PROC:
			case CRIPPLE_DURATION:
			case DAZE_PROC:
			case DAZE_DURATION:
			case VULNERABLE_PROC:
			case VULNERABLE_DURATION:
			case WEAKNESS_PROC:
			case WEAKNESS_DURATION:
			case MAGIC_DAMAGE:
			case MAGIC_POWER:
			case LIGHTNING_CHANCE:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case BLEED_RESISTANCE:
			case BLINDNESS_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case DAZE_RESISTANCE:
			case HEX_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
			case CHARM_RESISTANCE:
			case TERROR_RESISTANCE:
			case DREAD_RESISTANCE:
			case SLEEP_RESISTANCE:
			case AMOK_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
			case CHILL_RESISTANCE:
			case OOZE_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
				return 12 + next * 9;
			case ARTIFACT_POTENCY:
			case TRINKET_POTENCY:
				return 15 + next * 10;
			default:
				return 0;
		}
	}

	public int trainingEnergyCost( Training training ) {
		int next = trainingLevel( training ) + 1;
		switch (training) {
			case WAND_RECHARGE:
			case WAND_DAMAGE:
			case WAND_CHARGES:
			case ENCHANTMENT_POWER:
			case ELEMENTAL_RESISTANCE:
				return Math.max( 1, next );
			case ARMOR_ABILITY_CHARGE:
			case ARTIFACT_RECHARGE:
			case ARTIFACT_POTENCY:
			case TRINKET_POTENCY:
				return next >= 2 ? next : 1;
			case CATALYST_DROP_RATE:
			case BONUS_LOOT:
			case LIFESTEAL:
			case STATUS_PROC_CHANCE:
			case STATUS_DURATION:
			case BLEED_PROC:
			case BLEED_DURATION:
			case STUN_CHANCE:
			case STUN_DURATION:
			case BURNING_PROC:
			case BURNING_DURATION:
			case CORROSION_PROC:
			case CORROSION_DURATION:
			case HEX_PROC:
			case HEX_DURATION:
			case FROST_PROC:
			case FROST_DURATION:
			case POISON_PROC:
			case POISON_DURATION:
			case ROOT_PROC:
			case ROOT_DURATION:
			case SLOW_PROC:
			case SLOW_DURATION:
			case VERTIGO_PROC:
			case VERTIGO_DURATION:
			case BLINDNESS_PROC:
			case BLINDNESS_DURATION:
			case CRIPPLE_PROC:
			case CRIPPLE_DURATION:
			case DAZE_PROC:
			case DAZE_DURATION:
			case VULNERABLE_PROC:
			case VULNERABLE_DURATION:
			case WEAKNESS_PROC:
			case WEAKNESS_DURATION:
			case MAGIC_DAMAGE:
			case MAGIC_POWER:
			case LIGHTNING_CHANCE:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case BLEED_RESISTANCE:
			case BLINDNESS_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case DAZE_RESISTANCE:
			case HEX_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
			case CHARM_RESISTANCE:
			case TERROR_RESISTANCE:
			case DREAD_RESISTANCE:
			case SLEEP_RESISTANCE:
			case AMOK_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
			case CHILL_RESISTANCE:
			case OOZE_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
			case BARRIER_GUARD:
			case BARRIER_POWER:
				return next >= 3 ? 1 + next / 2 : 1;
			default:
				return 0;
		}
	}

	public String trainingCostText( Training training ) {
		StringBuilder text = new StringBuilder();
		for (Material material : Material.values()) {
			int cost = trainingCost( training, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
		if (trainingGoldCost( training ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( trainingGoldCost( training ) ).append( " gold" );
		}
		if (trainingEnergyCost( training ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( trainingEnergyCost( training ) ).append( " energy" );
		}
		return text.toString();
	}

	public String trainingOwnedCostText( Training training ) {
		StringBuilder text = new StringBuilder();
		for (Material material : Material.values()) {
			int cost = trainingCost( training, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount( material ) ).append( '/' ).append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
		if (trainingGoldCost( training ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( goldAmount() ).append( '/' ).append( trainingGoldCost( training ) ).append( " gold" );
		}
		if (trainingEnergyCost( training ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( energyAmount() ).append( '/' ).append( trainingEnergyCost( training ) ).append( " energy" );
		}
		return text.toString();
	}

	public String ownedCostText( Building building ) {
		StringBuilder text = new StringBuilder();
		for (Material material : Material.values()) {
			int cost = cost( building, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount( material ) ).append( '/' ).append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
		if (goldCost( building ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( goldAmount() ).append( '/' ).append( goldCost( building ) ).append( " gold" );
		}
		if (energyCost( building ) > 0) {
			if (text.length() > 0) text.append( ", " );
			text.append( energyAmount() ).append( '/' ).append( energyCost( building ) ).append( " energy" );
		}
		return text.toString();
	}

	public int moonrootPlots() {
		int level = buildingLevel( Building.GARDEN );
		return level <= 0 ? 0 : level * 2;
	}

	public int moonrootReady() {
		ensureMoonrootSeedSlots();
		int ready = 0;
		for (int seed : moonrootSeeds) {
			if (seed > 0) ready++;
		}
		return ready;
	}

	public void growMoonrootGarden() {
		if (!isBuilt( Building.GARDEN )) return;
		ensureMoonrootSeedSlots();
		int growth = Math.max( 1, buildingLevel( Building.GARDEN ) );
		for (int i = 0; i < moonrootSeeds.length && growth > 0; i++) {
			if (moonrootSeeds[i] <= 0) {
				moonrootSeeds[i] = randomMoonrootSeedIndex();
				growth--;
			}
		}
	}

	public int harvestMoonrootGarden() {
		return harvestMoonrootGardenSeeds().size();
	}

	public ArrayList<Item> harvestMoonrootGardenSeeds() {
		ensureMoonrootSeedSlots();
		ArrayList<Item> harvested = new ArrayList<>();
		for (int i = 0; i < moonrootSeeds.length; i++) {
			if (moonrootSeeds[i] <= 0) continue;

			Item harvest = moonrootHarvest( i );
			if (harvest != null) {
				harvested.add( harvest );
			}
			moonrootSeeds[i] = 0;
		}
		return harvested;
	}

	public boolean moonrootPlotReady( int plot ) {
		ensureMoonrootSeedSlots();
		return plot >= 0 && plot < moonrootSeeds.length && moonrootSeeds[plot] > 0;
	}

	public Item moonrootSeed( int plot ) {
		Class<? extends Plant.Seed> seedClass = moonrootSeedClass( plot );
		return seedClass == null ? null : Reflection.newInstance( seedClass );
	}

	private Item moonrootHarvest( int plot ) {
		Class<? extends Plant.Seed> seedClass = moonrootSeedClass( plot );
		if (seedClass == BlandfruitBush.Seed.class) {
			return new Blandfruit();
		}
		return seedClass == null ? null : Reflection.newInstance( seedClass );
	}

	public Plant moonrootPlant( int plot ) {
		Item seed = moonrootSeed( plot );
		if (seed instanceof Plant.Seed) {
			return ((Plant.Seed)seed).couch( 0, null );
		}
		return null;
	}

	private Class<? extends Plant.Seed> moonrootSeedClass( int plot ) {
		ensureMoonrootSeedSlots();
		if (plot < 0 || plot >= moonrootSeeds.length || moonrootSeeds[plot] <= 0) return null;

		int index = moonrootSeeds[plot] - 1;
		if (index < 0 || index >= MOONROOT_SEED_CLASSES.length) return null;
		return MOONROOT_SEED_CLASSES[index];
	}

	private void ensureMoonrootSeedSlots() {
		int plots = moonrootPlots();
		if (moonrootSeeds.length == plots) return;

		int[] resized = new int[plots];
		System.arraycopy( moonrootSeeds, 0, resized, 0, Math.min( moonrootSeeds.length, resized.length ) );
		moonrootSeeds = resized;
	}

	private int randomMoonrootSeedIndex() {
		return Random.chances( MOONROOT_SEED_WEIGHTS ) + 1;
	}

	private void appendMaterialCostText( StringBuilder text, Item item ) {
		for (Material material : Material.values()) {
			int cost = forgeUpgradeMaterialCost( item, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
	}

	private void appendForgeCostText( StringBuilder text, Item item ) {
		for (ForgeResource resource : ForgeResource.values()) {
			int cost = forgeUpgradeCost( item, resource );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( cost ).append( ' ' ).append( resource.label() );
		}
	}

	private void appendOwnedMaterialCostText( StringBuilder text, Item item ) {
		for (Material material : Material.values()) {
			int cost = forgeUpgradeMaterialCost( item, material );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount( material ) ).append( '/' ).append( cost ).append( ' ' ).append( MATERIAL_NAMES[material.ordinal()] );
		}
	}

	private void appendOwnedForgeCostText( StringBuilder text, Item item ) {
		for (ForgeResource resource : ForgeResource.values()) {
			int cost = forgeUpgradeCost( item, resource );
			if (cost <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( forgeResourceAmount( resource ) ).append( '/' ).append( cost ).append( ' ' ).append( resource.label() );
		}
	}

	public int depositMaterials( Hero hero ) {
		if (hero == null || hero.belongings == null) return 0;

		ArrayList<BuildingMaterial> recovered = new ArrayList<>();
		for (Item item : hero.belongings) {
			if (item instanceof BuildingMaterial) {
				recovered.add( (BuildingMaterial)item );
			}
		}

		int total = 0;
		for (BuildingMaterial material : recovered) {
			int quantity = material.quantity();
			add( material.material(), quantity );
			total += quantity;
			material.detachAll( hero.belongings.backpack );
		}

		if (total > 0) {
			Item.updateQuickslot();
		}
		return total;
	}

	public static class SettlementRequest implements Bundlable {

		public static final int OBJECTIVE_MATERIAL = 0;
		public static final int OBJECTIVE_FORGE_RESOURCE = 1;
		public static final int OBJECTIVE_GOLD = 2;
		public static final int OBJECTIVE_ENERGY = 3;
		public static final int OBJECTIVE_BOUNTY = 4;
		public static final int OBJECTIVE_SCOUTING = 5;
		public static final int OBJECTIVE_RECOVERY = 6;

		private static final int MAX_OBJECTIVE = OBJECTIVE_RECOVERY;

		public static final int REWARD_MATERIAL = 0;
		public static final int REWARD_FORGE_RESOURCE = 1;
		public static final int REWARD_GOLD = 2;
		public static final int REWARD_ENERGY = 3;

		private static final String OBJECTIVE_TYPE = "objective_type";
		private static final String OBJECTIVE_INDEX = "objective_index";
		private static final String MATERIAL = "material";
		private static final String AMOUNT = "amount";
		private static final String REWARD_TYPE = "reward_type";
		private static final String REWARD_INDEX = "reward_index";
		private static final String REWARD_AMOUNT = "reward_amount";
		private static final String PROGRESS = "progress";

		private int objectiveType = OBJECTIVE_MATERIAL;
		private int objectiveIndex;
		private int material;
		private int amount;
		private int rewardType;
		private int rewardIndex;
		private int rewardAmount;
		private int progress;
		private boolean legacyObjective;

		public SettlementRequest() {
		}

		private SettlementRequest( int objectiveType, int objectiveIndex, int amount, int rewardType, int rewardIndex, int rewardAmount ) {
			this.objectiveType = objectiveType;
			this.objectiveIndex = objectiveIndex;
			this.material = objectiveType == OBJECTIVE_MATERIAL ? objectiveIndex : 0;
			this.amount = amount;
			this.rewardType = rewardType;
			this.rewardIndex = rewardIndex;
			this.rewardAmount = rewardAmount;
		}

		public int objectiveType() {
			return Math.max( OBJECTIVE_MATERIAL, Math.min( objectiveType, MAX_OBJECTIVE ) );
		}

		public int objectiveIndex() {
			return objectiveIndex;
		}

		private boolean isLegacyObjective() {
			return legacyObjective;
		}

		public Material objectiveMaterial() {
			return Material.values()[clampIndex( objectiveType() == OBJECTIVE_MATERIAL ? objectiveIndex : material, Material.values().length )];
		}

		public ForgeResource objectiveForgeResource() {
			return ForgeResource.values()[clampIndex( objectiveIndex, ForgeResource.values().length )];
		}

		public Material material() {
			return objectiveMaterial();
		}

		public int amount() {
			return amount;
		}

		public int progress() {
			return Math.max( 0, Math.min( progress, amount() ) );
		}

		public boolean progressObjective() {
			int type = objectiveType();
			return type == OBJECTIVE_BOUNTY || type == OBJECTIVE_SCOUTING || type == OBJECTIVE_RECOVERY;
		}

		private void addProgress( int amount ) {
			progress = Math.max( 0, Math.min( this.amount, progress + amount ) );
		}

		private void setProgress( int progress ) {
			this.progress = Math.max( 0, Math.min( this.amount, progress ) );
		}

		public int rewardType() {
			return rewardType;
		}

		public int rewardAmount() {
			return rewardAmount;
		}

		public Material rewardMaterial() {
			return Material.values()[clampIndex( rewardIndex, Material.values().length )];
		}

		public ForgeResource rewardForgeResource() {
			return ForgeResource.values()[clampIndex( rewardIndex, ForgeResource.values().length )];
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			if (bundle.contains( OBJECTIVE_TYPE )) {
				objectiveType = Math.max( OBJECTIVE_MATERIAL, Math.min( bundle.getInt( OBJECTIVE_TYPE ), MAX_OBJECTIVE ) );
				objectiveIndex = bundle.getInt( OBJECTIVE_INDEX );
				legacyObjective = false;
			} else {
				objectiveType = OBJECTIVE_MATERIAL;
				objectiveIndex = bundle.getInt( MATERIAL );
				legacyObjective = true;
			}
			material = bundle.getInt( MATERIAL );
			amount = Math.max( 1, bundle.getInt( AMOUNT ) );
			rewardType = Math.max( REWARD_MATERIAL, Math.min( bundle.getInt( REWARD_TYPE ), REWARD_ENERGY ) );
			rewardIndex = bundle.getInt( REWARD_INDEX );
			rewardAmount = Math.max( 1, bundle.getInt( REWARD_AMOUNT ) );
			progress = Math.max( 0, Math.min( bundle.getInt( PROGRESS ), amount ) );
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put( OBJECTIVE_TYPE, objectiveType() );
			bundle.put( OBJECTIVE_INDEX, objectiveIndex );
			bundle.put( MATERIAL, material );
			bundle.put( AMOUNT, amount );
			bundle.put( REWARD_TYPE, rewardType );
			bundle.put( REWARD_INDEX, rewardIndex );
			bundle.put( REWARD_AMOUNT, rewardAmount );
			bundle.put( PROGRESS, progress() );
		}

		private static int clampIndex( int index, int length ) {
			return Math.max( 0, Math.min( index, length - 1 ) );
		}
	}

	public static class DefenderRecord implements Bundlable {

		private static final String ID = "id";
		private static final String NAME = "name";
		private static final String ARCHETYPE = "archetype";
		private static final String RARITY = "rarity";
		private static final String LEVEL = "level";
		private static final String XP = "xp";
		private static final String ALIVE = "alive";
		private static final String MOB_STATS = "mob_stats";
		private static final String WEAPON = "weapon";
		private static final String ARMOR = "armor";
		private static final String RANGED = "ranged";
		private static final String STRENGTH = "strength";
		private static final String ANKHS = "ankhs";
		private static final String HEALING_POTIONS = "healing_potions";
		private static final String INVISIBILITY_POTIONS = "invisibility_potions";
		private static final String IDENTIFY_PROGRESS = "identify_progress";

		public static final int WARRIOR = 0;
		public static final int MAGE = 1;
		public static final int ROGUE = 2;
		public static final int HUNTRESS = 3;
		public static final int DUELIST = 4;
		public static final int PRIEST = 5;

		private static final String[] ARCHETYPE_NAMES = {
				"warrior",
				"mage",
				"rogue",
				"huntress",
				"duelist",
				"priest"
		};

		private static final String[] DEFENDER_NAMES = {
				"Ansel",
				"Mira",
				"Corvin",
				"Lysa",
				"Rowan",
				"Seren",
				"Kael",
				"Iona",
				"Darian",
				"Nessa",
				"Alden",
				"Maris",
				"Tavian",
				"Elowen",
				"Bran",
				"Selene",
				"Orin",
				"Vera",
				"Cassian",
				"Nyra",
				"Fenn",
				"Alina",
				"Torren",
				"Maelis",
				"Rook",
				"Iris",
				"Galen",
				"Thalia",
				"Osric",
				"Junia",
				"Riven",
				"Elara",
				"Damon",
				"Vesper",
				"Calla",
				"Leoric",
				"Sable",
				"Marek",
				"Anika",
				"Jorin",
				"Lyra",
				"Keir",
				"Sabine",
				"Bastian",
				"Vaila",
				"Theron",
				"Ophel",
				"Renna",
				"Lucan",
				"Isolde",
				"Matteo",
				"Ysola",
				"Cedric",
				"Nalia",
				"Perrin",
				"Astra",
				"Darius",
				"Milena",
				"Soren",
				"Clara",
				"Evran",
				"Rhea",
				"Cael",
				"Tamsin",
				"Arlen",
				"Petra",
				"Eamon",
				"Zara",
				"Halden",
				"Meriel",
				"Ronan",
				"Celis",
				"Tobin",
				"Amara",
				"Lucien",
				"Elise",
				"Finnian",
				"Vanya",
				"Oren",
				"Nimue",
				"Garret",
				"Seraph",
				"Emrys",
				"Linnea",
				"Dariel",
				"Orla",
				"Magnus",
				"Sylvi",
				"Brennan",
				"Eira",
				"Quill",
				"Maren",
				"Toren",
				"Viola",
				"Aric",
				"Lenora",
				"Kellan",
				"Moira",
				"Jasper",
				"Rina",
				"Hadrian",
				"Estra",
				"Silas",
				"Nora",
				"Vale",
				"Anwen",
				"Roderic",
				"Calista",
				"Fenric",
				"Yara"
		};

		private int id;
		private String name = "";
		private int archetype = WARRIOR;
		private ItemRarity rarity = ItemRarity.COMMON;
		private int level = 1;
		private int xp = 0;
		private boolean alive = true;
		private MobStats stats;
		private Weapon weapon;
		private Armor armor;
		private Item ranged;
		private int strength = 10;
		private int ankhs = 0;
		private int healingPotions = 0;
		private int invisibilityPotions = 0;
		private int identifyProgress = 0;

		public DefenderRecord() {
		}

		public static DefenderRecord randomCandidate() {
			return random( -1 );
		}

		private static DefenderRecord random( int id ) {
			DefenderRecord defender = new DefenderRecord();
			defender.id = id;
			defender.name = DEFENDER_NAMES[Random.Int( DEFENDER_NAMES.length )];
			defender.archetype = Random.Int( ARCHETYPE_NAMES.length );
			defender.rarity = randomRarity();
			defender.level = 1;
			defender.xp = 0;
			defender.stats = MobStats.rollForLevel( defender.level(), defender.rarity().power() * 2 );
			defender.strength = 10 + defender.rarity().power();
			defender.rollStartingEquipment();
			defender.alive = true;
			return defender;
		}

		private void rollStartingEquipment() {
			int pieces = Random.chances( new float[]{3, 4, 2, 1} );
			if (pieces <= 0) return;

			ArrayList<Integer> slots = new ArrayList<>();
			slots.add( 0 );
			slots.add( 1 );
			slots.add( 2 );
			Random.shuffle( slots );

			for (int i = 0; i < pieces && i < slots.size(); i++) {
				switch (slots.get( i )) {
					case 0:
						weapon = Generator.randomWeapon( defenderFloorSet() );
						break;
					case 1:
						armor = Generator.randomArmor( defenderFloorSet() );
						break;
					case 2:
						ranged = randomStartingRanged( defenderFloorSet() );
						break;
					default:
						break;
				}
			}
		}

		private static int defenderFloorSet() {
			return Math.max( 0, Dungeon.depth / 5 );
		}

		private static Item randomStartingRanged( int floorSet ) {
			if (Random.Int( 2 ) == 0) {
				return Generator.random( Generator.Category.WAND );
			}
			return Generator.randomMissile( floorSet );
		}

		private DefenderRecord copyWithId( int id ) {
			DefenderRecord copy = new DefenderRecord();
			copy.id = id;
			copy.name = defenderName();
			copy.archetype = archetype;
			copy.rarity = rarity();
			copy.level = level();
			copy.xp = xp();
			copy.alive = alive();
			copy.stats = mobStats();
			copy.weapon = weapon;
			copy.armor = armor;
			copy.ranged = ranged;
			copy.strength = strength();
			copy.ankhs = ankhs();
			copy.healingPotions = healingPotions();
			copy.invisibilityPotions = invisibilityPotions();
			copy.identifyProgress = identifyProgress;
			return copy;
		}

		private static ItemRarity randomRarity() {
			int roll = Random.Int( 100 );
			if (roll < 50) return ItemRarity.COMMON;
			if (roll < 75) return ItemRarity.UNCOMMON;
			if (roll < 90) return ItemRarity.RARE;
			if (roll < 98) return ItemRarity.EPIC;
			return ItemRarity.LEGENDARY;
		}

		public int id() {
			return id;
		}

		public String defenderName() {
			return name == null || name.isEmpty() ? "Defender" : name;
		}

		public int archetype() {
			return archetype;
		}

		public String archetypeName() {
			return ARCHETYPE_NAMES[Math.max( 0, Math.min( archetype, ARCHETYPE_NAMES.length - 1 ) )];
		}

		public ItemRarity rarity() {
			return rarity == null ? ItemRarity.COMMON : rarity;
		}

		public int level() {
			return Math.max( 1, level );
		}

		public int xp() {
			return Math.max( 0, xp );
		}

		public int xpToNext() {
			return Hero.maxExp( level() );
		}

		public int strength() {
			return Math.max( 1, strength );
		}

		public void increaseStrength( int amount ) {
			strength = Math.max( 1, strength() + Math.max( 0, amount ) );
		}

		public float xpProgress() {
			return Math.max( 0f, Math.min( 1f, xp() / (float)Math.max( 1, xpToNext() ) ) );
		}

		public MobStats mobStats() {
			if (stats == null) {
				stats = MobStats.rollForLevel( level(), rarity().power() * 2 );
			}
			return stats;
		}

		public boolean gainExperience( int amount ) {
			if (!alive || amount <= 0) return false;
			xp += amount;
			boolean levelled = false;
			while (xp >= xpToNext()) {
				xp -= xpToNext();
				level++;
				mobStats().improveForDefenderLevel( level(), rarity().power() );
				levelled = true;
			}
			return levelled;
		}

		public int maxHP() {
			return 24 + level() * 6 + rarity().power() * 8;
		}

		public int attackSkill() {
			int skill = 10 + level() * 2 + rarity().power() * 2;
			if (weapon != null && weapon.STRReq() > strength()) {
				skill -= 2 * (weapon.STRReq() - strength());
			}
			return Math.max( 1, skill );
		}

		public int defenseSkill() {
			int skill = 5 + level() * 2 + rarity().power() * 2;
			if (armor != null && armor.STRReq() > strength()) {
				skill -= 2 * (armor.STRReq() - strength());
			}
			return Math.max( 1, skill );
		}

		public int minDamage() {
			return 2 + level() + rarity().power();
		}

		public int maxDamage() {
			return 5 + level() * 2 + rarity().power() * 3;
		}

		public int maxArmor() {
			return 1 + level() / 2 + rarity().power();
		}

		public boolean alive() {
			return alive;
		}

		public int ankhs() {
			return Math.max( 0, ankhs );
		}

		public int healingPotions() {
			return Math.max( 0, healingPotions );
		}

		public int invisibilityPotions() {
			return Math.max( 0, invisibilityPotions );
		}

		public void addAnkh() {
			ankhs = ankhs() + 1;
		}

		public void addHealingPotion() {
			healingPotions = healingPotions() + 1;
		}

		public void addInvisibilityPotion() {
			invisibilityPotions = invisibilityPotions() + 1;
		}

		public boolean consumeAnkh() {
			if (ankhs() <= 0) return false;
			ankhs--;
			return true;
		}

		public boolean consumeHealingPotion() {
			if (healingPotions() <= 0) return false;
			healingPotions--;
			return true;
		}

		public boolean consumeInvisibilityPotion() {
			if (invisibilityPotions() <= 0) return false;
			invisibilityPotions--;
			return true;
		}

		public Weapon weapon() {
			return weapon;
		}

		public Armor armor() {
			return armor;
		}

		public Item ranged() {
			return ranged;
		}

		public Item equipWeapon( Weapon replacement ) {
			Item previous = weapon;
			weapon = replacement;
			return previous;
		}

		public Item equipArmor( Armor replacement ) {
			Item previous = armor;
			armor = replacement;
			return previous;
		}

		public Item equipRanged( Item replacement ) {
			Item previous = ranged;
			ranged = replacement instanceof Wand || replacement instanceof MissileWeapon ? replacement : null;
			return previous;
		}

		public Item upgradeRandomEquipment() {
			ArrayList<Item> choices = new ArrayList<>();
			if (weapon != null) choices.add( weapon );
			if (armor != null) choices.add( armor );
			if (ranged != null) choices.add( ranged );
			if (choices.isEmpty()) return null;

			Item target = choices.get( Random.Int( choices.size() ) );
			if (target == null) return null;

			Item upgraded = target.upgrade();
			upgraded.improveRarityStatsFromUpgrade();
			if (target == weapon && upgraded instanceof Weapon) weapon = (Weapon)upgraded;
			if (target == armor && upgraded instanceof Armor) armor = (Armor)upgraded;
			if (target == ranged && (upgraded instanceof Wand || upgraded instanceof MissileWeapon)) ranged = upgraded;
			return upgraded;
		}

		public Item studyEquipment() {
			Item target = firstUnidentifiedEquipment();
			if (target == null) {
				identifyProgress = 0;
				return null;
			}

			identifyProgress++;
			int needed = Math.max( 20, 80 - level() * 2 - rarity().power() * 6 );
			if (identifyProgress >= needed) {
				identifyProgress = 0;
				target.identify( false );
				return target;
			}
			return null;
		}

		private Item firstUnidentifiedEquipment() {
			if (weapon != null && !weapon.isIdentified()) return weapon;
			if (armor != null && !armor.isIdentified()) return armor;
			if (ranged != null && !ranged.isIdentified()) return ranged;
			return null;
		}

		private void clearEquipment() {
			weapon = null;
			armor = null;
			ranged = null;
			identifyProgress = 0;
		}

		public String title() {
			return rarity().displayName() + " " + archetypeName();
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			id = bundle.getInt( ID );
			name = bundle.getString( NAME );
			archetype = bundle.getInt( ARCHETYPE );
			level = Math.max( 1, bundle.getInt( LEVEL ) );
			xp = bundle.getInt( XP );
			alive = !bundle.contains( ALIVE ) || bundle.getBoolean( ALIVE );
			if (bundle.contains( RARITY )) {
				try {
					rarity = ItemRarity.valueOf( bundle.getString( RARITY ) );
				} catch (Exception e) {
					rarity = ItemRarity.COMMON;
				}
			} else {
				rarity = ItemRarity.COMMON;
			}
			if (bundle.contains( MOB_STATS )) {
				stats = (MobStats)bundle.get( MOB_STATS );
			} else {
				stats = null;
			}
			if (bundle.contains( WEAPON )) {
				weapon = (Weapon)bundle.get( WEAPON );
			}
			if (bundle.contains( ARMOR )) {
				armor = (Armor)bundle.get( ARMOR );
			}
			if (bundle.contains( RANGED )) {
				Object restored = bundle.get( RANGED );
				if (restored instanceof Wand || restored instanceof MissileWeapon) {
					ranged = (Item)restored;
				}
			}
			if (bundle.contains( STRENGTH )) {
				strength = Math.max( 1, bundle.getInt( STRENGTH ) );
			} else {
				strength = 10 + rarity().power();
			}
			if (bundle.contains( ANKHS )) {
				ankhs = Math.max( 0, bundle.getInt( ANKHS ) );
			}
			if (bundle.contains( HEALING_POTIONS )) {
				healingPotions = Math.max( 0, bundle.getInt( HEALING_POTIONS ) );
			}
			if (bundle.contains( INVISIBILITY_POTIONS )) {
				invisibilityPotions = Math.max( 0, bundle.getInt( INVISIBILITY_POTIONS ) );
			}
			if (bundle.contains( IDENTIFY_PROGRESS )) {
				identifyProgress = Math.max( 0, bundle.getInt( IDENTIFY_PROGRESS ) );
			}
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			bundle.put( ID, id );
			bundle.put( NAME, defenderName() );
			bundle.put( ARCHETYPE, archetype );
			bundle.put( RARITY, rarity().name() );
			bundle.put( LEVEL, level() );
			bundle.put( XP, xp );
			bundle.put( ALIVE, alive );
			if (stats != null) {
				bundle.put( MOB_STATS, stats );
			}
			if (weapon != null) {
				bundle.put( WEAPON, weapon );
			}
			if (armor != null) {
				bundle.put( ARMOR, armor );
			}
			if (ranged != null) {
				bundle.put( RANGED, ranged );
			}
			bundle.put( STRENGTH, strength() );
			bundle.put( ANKHS, ankhs() );
			bundle.put( HEALING_POTIONS, healingPotions() );
			bundle.put( INVISIBILITY_POTIONS, invisibilityPotions() );
			bundle.put( IDENTIFY_PROGRESS, identifyProgress );
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		int[] restored = bundle.getIntArray( AMOUNTS );
		amounts = new int[Material.values().length];
		if (restored != null) {
			System.arraycopy( restored, 0, amounts, 0, Math.min(restored.length, amounts.length) );
		}

		int[] restoredForgeResources = bundle.getIntArray( FORGE_RESOURCES );
		forgeResources = new int[ForgeResource.values().length];
		if (restoredForgeResources != null) {
			System.arraycopy( restoredForgeResources, 0, forgeResources, 0, Math.min(restoredForgeResources.length, forgeResources.length) );
		}

		int[] restoredBuildings = bundle.getIntArray( BUILDING_LEVELS );
		buildingLevels = new int[Building.values().length];
		if (restoredBuildings != null) {
			System.arraycopy( restoredBuildings, 0, buildingLevels, 0, Math.min(restoredBuildings.length, buildingLevels.length) );
		} else {
			buildingLevels[Building.VAULT.ordinal()] = Math.max( 0, bundle.getInt( VAULT_LEVEL ) );
		}
		vaultLevel = buildingLevel( Building.VAULT );

		int[] restoredTraining = bundle.getIntArray( TRAINING_LEVELS );
		trainingLevels = new int[Training.values().length];
		if (restoredTraining != null) {
			System.arraycopy( restoredTraining, 0, trainingLevels, 0, Math.min(restoredTraining.length, trainingLevels.length) );
		}

		int[] restoredBuildingHp = bundle.getIntArray( BUILDING_HP );
		buildingHp = new int[Building.values().length];
		if (restoredBuildingHp != null) {
			System.arraycopy( restoredBuildingHp, 0, buildingHp, 0, Math.min(restoredBuildingHp.length, buildingHp.length) );
		}

		int[] restoredBuildingDestroyed = bundle.getIntArray( BUILDING_DESTROYED );
		buildingDestroyed = new int[Building.values().length];
		if (restoredBuildingDestroyed != null) {
			System.arraycopy( restoredBuildingDestroyed, 0, buildingDestroyed, 0, Math.min(restoredBuildingDestroyed.length, buildingDestroyed.length) );
		}

		int[] restoredBuildingDefense = bundle.getIntArray( BUILDING_DEFENSE_LEVELS );
		buildingDefenseLevels = new int[Building.values().length * BuildingDefense.values().length];
		if (restoredBuildingDefense != null) {
			System.arraycopy( restoredBuildingDefense, 0, buildingDefenseLevels, 0, Math.min(restoredBuildingDefense.length, buildingDefenseLevels.length) );
		}
		ensureBuildingState();
		moonrootGrowth = bundle.getInt( MOONROOT_GROWTH );

		int[] restoredMoonrootSeeds = bundle.getIntArray( MOONROOT_SEEDS );
		moonrootSeeds = new int[0];
		if (restoredMoonrootSeeds != null && restoredMoonrootSeeds.length > 0) {
			moonrootSeeds = new int[restoredMoonrootSeeds.length];
			System.arraycopy( restoredMoonrootSeeds, 0, moonrootSeeds, 0, restoredMoonrootSeeds.length );
			ensureMoonrootSeedSlots();
		} else if (moonrootGrowth > 0) {
			ensureMoonrootSeedSlots();
			for (int i = 0; i < moonrootSeeds.length && i < moonrootGrowth; i++) {
				moonrootSeeds[i] = randomMoonrootSeedIndex();
			}
		}

		vaultItems = new ArrayList<>();
		for (Bundlable item : bundle.getCollection( VAULT_ITEMS )) {
			if (item instanceof Item) {
				vaultItems.add( (Item)item );
			}
		}

		settlementRequests = new ArrayList<>();
		for (Bundlable request : bundle.getCollection( SETTLEMENT_REQUESTS )) {
			if (request instanceof SettlementRequest) {
				settlementRequests.add( (SettlementRequest)request );
			}
		}
		ensureSettlementRequests();

		raidActive = bundle.getBoolean( RAID_ACTIVE );
		raidPopupPending = bundle.getBoolean( RAID_POPUP_PENDING );
		raidThreat = bundle.getInt( RAID_THREAT );
		raidTotalMobs = bundle.contains( RAID_TOTAL_MOBS ) ? bundle.getInt( RAID_TOTAL_MOBS ) : 0;
		raidWave = bundle.getInt( RAID_WAVE );
		raidWaves = bundle.getInt( RAID_WAVES );
		raidWaveTotal = bundle.getInt( RAID_WAVE_TOTAL );
		raidWaveSpawned = bundle.getInt( RAID_WAVE_SPAWNED );
		raidWaveKilled = bundle.getInt( RAID_WAVE_KILLED );
		raidMobClass = bundle.getString( RAID_MOB_CLASS );
		if (raidActive) {
			if (raidWave <= 0) raidWave = 1;
			if (raidTotalMobs <= 0) raidTotalMobs = Math.max( 1, raidWaveTotal * Math.max( 1, raidWaves ) );
			if (raidWaves <= 0) raidWaves = 1;
			if (raidWaveTotal <= 0) prepareRaidWave();
			if (raidMobClass == null || raidMobClass.isEmpty()) raidMobClass = fallbackRaidMobClass();
		}

		revengeKillClasses = new ArrayList<>();
		revengeKillCounts = new ArrayList<>();
		if (bundle.contains( REVENGE_KILL_CLASSES ) && bundle.contains( REVENGE_KILL_COUNTS )) {
			String[] restoredRevengeClasses = bundle.getStringArray( REVENGE_KILL_CLASSES );
			int[] restoredRevengeCounts = bundle.getIntArray( REVENGE_KILL_COUNTS );
			if (restoredRevengeClasses != null && restoredRevengeCounts != null) {
				for (int i = 0; i < restoredRevengeClasses.length && i < restoredRevengeCounts.length; i++) {
					if (!excludedRaidMobClass( restoredRevengeClasses[i] ) && restoredRevengeCounts[i] > 0) {
						revengeKillClasses.add( restoredRevengeClasses[i] );
						revengeKillCounts.add( restoredRevengeCounts[i] );
					}
				}
			}
		}

		defenders = new ArrayList<>();
		for (Bundlable defender : bundle.getCollection( DEFENDERS )) {
			if (defender instanceof DefenderRecord && ((DefenderRecord)defender).alive()) {
				defenders.add( (DefenderRecord)defender );
				nextDefenderId = Math.max( nextDefenderId, ((DefenderRecord)defender).id() + 1 );
			}
		}
		if (bundle.contains( NEXT_DEFENDER_ID )) {
			nextDefenderId = Math.max( nextDefenderId, bundle.getInt( NEXT_DEFENDER_ID ) );
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		ensureBuildingState();
		ensureSettlementRequests();
		bundle.put( AMOUNTS, amounts );
		bundle.put( FORGE_RESOURCES, forgeResources );
		bundle.put( VAULT_LEVEL, vaultLevel );
		bundle.put( VAULT_ITEMS, vaultItems );
		bundle.put( BUILDING_LEVELS, buildingLevels );
		bundle.put( TRAINING_LEVELS, trainingLevels );
		bundle.put( BUILDING_HP, buildingHp );
		bundle.put( BUILDING_DESTROYED, buildingDestroyed );
		bundle.put( BUILDING_DEFENSE_LEVELS, buildingDefenseLevels );
		bundle.put( MOONROOT_GROWTH, moonrootReady() );
		bundle.put( MOONROOT_SEEDS, moonrootSeeds );
		bundle.put( SETTLEMENT_REQUESTS, settlementRequests );
		bundle.put( RAID_ACTIVE, raidActive );
		bundle.put( RAID_POPUP_PENDING, raidPopupPending );
		bundle.put( RAID_THREAT, raidThreat );
		bundle.put( RAID_TOTAL_MOBS, raidTotalMobs );
		bundle.put( RAID_WAVE, raidWave );
		bundle.put( RAID_WAVES, raidWaves );
		bundle.put( RAID_WAVE_TOTAL, raidWaveTotal );
		bundle.put( RAID_WAVE_SPAWNED, raidWaveSpawned );
		bundle.put( RAID_WAVE_KILLED, raidWaveKilled );
		bundle.put( RAID_MOB_CLASS, raidMobClass );
		bundle.put( REVENGE_KILL_CLASSES, revengeKillClasses.toArray( new String[0] ) );
		int[] savedRevengeCounts = new int[revengeKillCounts.size()];
		for (int i = 0; i < savedRevengeCounts.length; i++) {
			savedRevengeCounts[i] = revengeKillCounts.get( i );
		}
		bundle.put( REVENGE_KILL_COUNTS, savedRevengeCounts );
		pruneDeadDefenders();
		bundle.put( DEFENDERS, defenders );
		bundle.put( NEXT_DEFENDER_ID, nextDefenderId );
	}
}
