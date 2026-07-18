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

public class RarityStat {

	public enum Type {
		EMPTY_SLOT( "Empty Slot", false ),
		ARMOR_BONUS( "Armor Bonus", true ),
		ATTACK_BONUS( "Attack Bonus", true ),
		ATTACK_DAMAGE( "Attack Damage", false ),
		ATTACK_SPEED( "Attack Speed", true ),
		ATTACK_ACCURACY( "Attack Accuracy", true ),
		BARKSKIN_PROC( "Barkskin Proc", true ),
		BARKSKIN_POWER( "Barkskin Power", false ),
		BARRIER_PROC( "Barrier Proc", true ),
		BARRIER_POWER( "Barrier Power", false ),
		BLESS_PROC( "Bless Proc", true ),
		BLESS_DURATION( "Bless Duration", false ),
		BLINDNESS_PROC( "Blindness Proc", true ),
		BLINDNESS_DURATION( "Blindness Duration", false ),
		BLINDNESS_RESISTANCE( "Blindness Resistance", true ),
		BLEED_PROC( "Bleed Proc", true ),
		BLEED_DURATION( "Bleed Duration", false ),
		BLEED_RESISTANCE( "Bleed Resistance", true ),
		BLOCK_CHANCE( "Block Chance", true ),
		BONUS_LOOT( "Bonus Loot", true ),
		BURNING_PROC( "Burning Proc", true ),
		BURNING_DURATION( "Burning Duration", false ),
		CLEAVE_CHANCE( "Cleave Chance", true ),
		CORROSION_PROC( "Corrosion Proc", true ),
		CORROSION_DURATION( "Corrosion Duration", false ),
		CORROSION_RESISTANCE( "Corrosion Resistance", true ),
		CRIMSON_ECHO( "Crimson Echo", false ),
		CRITICAL_CHANCE( "Critical Hit Chance", true ),
		CRITICAL_DAMAGE_MULTIPLIER( "Critical Damage Multiplier", true ),
		CRITICAL_HIT_RESISTANCE( "Critical Hit Resistance", true ),
		CRITICAL_DAMAGE_REDUCTION( "Critical Damage Reduction", true ),
		CRIPPLE_PROC( "Cripple Proc", true ),
		CRIPPLE_DURATION( "Cripple Duration", false ),
		CRIPPLE_RESISTANCE( "Cripple Resistance", true ),
		DAZE_PROC( "Daze Proc", true ),
		DAZE_DURATION( "Daze Duration", false ),
		DAZE_RESISTANCE( "Daze Resistance", true ),
		DEFENSE( "Armor", false ),
		DODGE_CHANCE( "Dodge Chance", true ),
		EVASION( "Evasion", false ),
		FATAL_SYNCHRONICITY( "Fatal Synchronicity", false ),
		FEATHER_FALLING( "Feather Falling", true ),
		FIRE_RESISTANCE( "Fire Resistance", true ),
		FROST_PROC( "Frost Proc", true ),
		FROST_DURATION( "Frost Duration", false ),
		FROST_RESISTANCE( "Frost Resistance", true ),
		GLACIAL_REND( "Glacial Rend", false ),
		HASTE_PROC( "Haste Proc", true ),
		HASTE_DURATION( "Haste Duration", false ),
		HEX_PROC( "Hex Proc", true ),
		HEX_DURATION( "Hex Duration", false ),
		HEX_RESISTANCE( "Hex Resistance", true ),
		KNOCKBACK_CHANCE( "Knockback Chance", true ),
		KNOCKBACK_STRENGTH( "Knockback Strength", false ),
		LIFESTEAL( "Lifesteal", true ),
		MAGIC_BONUS( "Magic Bonus", true ),
		MAGIC_DAMAGE( "Magic Damage", false ),
		MAX_HEALTH( "Max Health", false ),
		MOVEMENT_SPEED( "Movement Speed", true ),
		PIERCING_CHANCE( "Piercing Chance", true ),
		POISON_PROC( "Poison Proc", true ),
		POISON_DURATION( "Poison Duration", false ),
		POISON_RESISTANCE( "Poison Resistance", true ),
		RECHARGING_PROC( "Recharging Proc", true ),
		RECHARGING_DURATION( "Recharging Duration", false ),
		RESOURCEFUL( "Resourceful", true ),
		ROOT_PROC( "Root Proc", true ),
		ROOT_DURATION( "Root Duration", false ),
		ROOT_RESISTANCE( "Root Resistance", true ),
		SLOW_PROC( "Slow Proc", true ),
		SLOW_DURATION( "Slow Duration", false ),
		SLOW_RESISTANCE( "Slow Resistance", true ),
		SOUL_REAPING( "Soul Reaping", true ),
		SOULBOUND( "Soulbound", false ),
		SPIRITBREAK( "Spiritbreak", false ),
		STATIC_RUIN( "Static Ruin", false ),
		STUN_CHANCE( "Stun Chance", true ),
		STUN_DURATION( "Stun Duration", false ),
		STUN_RESISTANCE( "Stun Resistance", true ),
		SUMMON_LIGHTNING_CHANCE( "Summon Lightning Chance", true ),
		THORNS_CHANCE( "Thorns Chance", true ),
		THORNS_DAMAGE( "Thorns Damage", false ),
		TREASURE_LUCK( "Treasure Luck", true ),
		UNBREAKABLE( "Unbreakable", false ),
		VERTIGO_PROC( "Vertigo Proc", true ),
		VERTIGO_DURATION( "Vertigo Duration", false ),
		VERTIGO_RESISTANCE( "Vertigo Resistance", true ),
		VULNERABLE_PROC( "Vulnerable Proc", true ),
		VULNERABLE_DURATION( "Vulnerable Duration", false ),
		VULNERABLE_RESISTANCE( "Vulnerable Resistance", true ),
		WAND_CHARGES( "Wand Charges", false ),
		WAND_RECHARGE_RATE( "Wand Recharge Rate", true ),
		WEAKNESS_PROC( "Weakness Proc", true ),
		WEAKNESS_DURATION( "Weakness Duration", false ),
		WEAKNESS_RESISTANCE( "Weakness Resistance", true ),
		THROWN_DURABILITY( "Throw Usages", false ),
		XP_GAIN( "XP Gain", true ),
		RING_POTENCY( "Ring Potency", false ),
		TRINKET_POTENCY( "Trinket Potency", false ),
		ARTIFACT_POTENCY( "Artifact Potency", false );

		private final String displayName;
		private final boolean percent;

		Type( String displayName, boolean percent ) {
			this.displayName = displayName;
			this.percent = percent;
		}

		public String displayName() {
			return displayName;
		}

		public String compactDisplayName() {
			switch (this) {
				case ARMOR_BONUS: return "Armor +";
				case ATTACK_BONUS: return "Atk +";
				case ATTACK_DAMAGE: return "Atk Dmg";
				case ATTACK_SPEED: return "Atk Spd";
				case ATTACK_ACCURACY: return "Atk Acc";
				case BARKSKIN_PROC: return "Barkskin %";
				case BARKSKIN_POWER: return "Barkskin";
				case BARRIER_PROC: return "Barrier %";
				case BARRIER_POWER: return "Barrier";
				case BLESS_PROC: return "Bless %";
				case BLESS_DURATION: return "Bless Dur";
				case BLOCK_CHANCE: return "Block %";
				case BONUS_LOOT: return "Loot";
				case CLEAVE_CHANCE: return "Cleave %";
				case CRITICAL_CHANCE: return "Crit %";
				case CRITICAL_DAMAGE_MULTIPLIER: return "Crit Dmg";
				case CRITICAL_HIT_RESISTANCE: return "Crit Res";
				case CRITICAL_DAMAGE_REDUCTION: return "Crit Red";
				case DODGE_CHANCE: return "Dodge %";
				case FATAL_SYNCHRONICITY: return "Fatal Sync";
				case FEATHER_FALLING: return "Feather";
				case GLACIAL_REND: return "Glacial";
				case KNOCKBACK_CHANCE: return "Knockback %";
				case KNOCKBACK_STRENGTH: return "Knockback";
				case MAGIC_BONUS: return "Magic +";
				case MAGIC_DAMAGE: return "Magic Dmg";
				case MAX_HEALTH: return "HP";
				case MOVEMENT_SPEED: return "Move Spd";
				case PIERCING_CHANCE: return "Pierce %";
				case RECHARGING_PROC: return "Recharge %";
				case RECHARGING_DURATION: return "Recharge Dur";
				case RESOURCEFUL: return "Resource";
				case SOUL_REAPING: return "Soul Reap";
				case SUMMON_LIGHTNING_CHANCE: return "Lightning %";
				case THORNS_CHANCE: return "Thorns %";
				case THORNS_DAMAGE: return "Thorns Dmg";
				case TREASURE_LUCK: return "Treasure";
				case WAND_CHARGES: return "Charges";
				case WAND_RECHARGE_RATE: return "Wand Regen";
				case THROWN_DURABILITY: return "Throw Use";
				case RING_POTENCY: return "Ring Pot";
				case TRINKET_POTENCY: return "Trinket Pot";
				case ARTIFACT_POTENCY: return "Artifact Pot";
				default:
					return displayName
							.replace( "Critical", "Crit" )
							.replace( "Resistance", "Res" )
							.replace( "Reduction", "Red" )
							.replace( "Duration", "Dur" )
							.replace( "Damage", "Dmg" )
							.replace( "Chance", "%" )
							.replace( "Proc", "%" )
							.replace( "Speed", "Spd" )
							.replace( "Accuracy", "Acc" )
							.replace( "Attack", "Atk" )
							.replace( "Strength", "Str" )
							.replace( "Weakness", "Weak" )
							.replace( "Vulnerable", "Vuln" );
			}
		}

		public boolean percent() {
			return percent;
		}

		public boolean capsAtHundred() {
			return hasValue()
					&& (displayName.endsWith( " Resistance" )
					|| displayName.endsWith( " Proc" )
					|| displayName.endsWith( " Chance" ));
		}

		public int capValue( int value ) {
			return capsAtHundred() ? Math.min( 100, value ) : value;
		}

		public int displayColor() {
			return minimumRarity().color();
		}

		public ItemRarity minimumRarity() {
			switch (this) {
				case ATTACK_DAMAGE:
				case DEFENSE:
				case EVASION:
				case MAGIC_DAMAGE:
				case MAX_HEALTH:
				case RING_POTENCY:
				case THROWN_DURABILITY:
				case TRINKET_POTENCY:
				case ARTIFACT_POTENCY:
				case WAND_RECHARGE_RATE:
					return ItemRarity.COMMON;
				case ARMOR_BONUS:
				case ATTACK_BONUS:
				case ATTACK_SPEED:
				case ATTACK_ACCURACY:
				case BLOCK_CHANCE:
				case DODGE_CHANCE:
				case MOVEMENT_SPEED:
				case RESOURCEFUL:
				case TREASURE_LUCK:
				case XP_GAIN:
					return ItemRarity.UNCOMMON;
				case BLINDNESS_RESISTANCE:
				case BLEED_PROC:
				case BLEED_DURATION:
				case BLEED_RESISTANCE:
				case BURNING_PROC:
				case BURNING_DURATION:
				case CORROSION_RESISTANCE:
				case CRIPPLE_PROC:
				case CRIPPLE_DURATION:
				case CRIPPLE_RESISTANCE:
				case CRITICAL_CHANCE:
				case CRITICAL_DAMAGE_MULTIPLIER:
				case CRITICAL_HIT_RESISTANCE:
				case CRITICAL_DAMAGE_REDUCTION:
				case DAZE_PROC:
				case DAZE_DURATION:
				case DAZE_RESISTANCE:
				case FEATHER_FALLING:
				case FIRE_RESISTANCE:
				case FROST_PROC:
				case FROST_DURATION:
				case FROST_RESISTANCE:
				case KNOCKBACK_CHANCE:
				case KNOCKBACK_STRENGTH:
				case POISON_PROC:
				case POISON_DURATION:
				case POISON_RESISTANCE:
				case ROOT_PROC:
				case ROOT_DURATION:
				case ROOT_RESISTANCE:
				case SLOW_PROC:
				case SLOW_DURATION:
				case SLOW_RESISTANCE:
				case STUN_CHANCE:
				case STUN_DURATION:
				case STUN_RESISTANCE:
				case THORNS_CHANCE:
				case THORNS_DAMAGE:
				case WAND_CHARGES:
				case WEAKNESS_PROC:
				case WEAKNESS_DURATION:
				case WEAKNESS_RESISTANCE:
					return ItemRarity.RARE;
				case BARKSKIN_PROC:
				case BARKSKIN_POWER:
				case BARRIER_PROC:
				case BARRIER_POWER:
				case BLESS_PROC:
				case BLESS_DURATION:
				case BLINDNESS_PROC:
				case BLINDNESS_DURATION:
				case BONUS_LOOT:
				case CLEAVE_CHANCE:
				case CORROSION_PROC:
				case CORROSION_DURATION:
				case HASTE_PROC:
				case HASTE_DURATION:
				case HEX_PROC:
				case HEX_DURATION:
				case HEX_RESISTANCE:
				case LIFESTEAL:
				case MAGIC_BONUS:
				case PIERCING_CHANCE:
				case RECHARGING_PROC:
				case RECHARGING_DURATION:
				case SOUL_REAPING:
				case SUMMON_LIGHTNING_CHANCE:
				case VERTIGO_PROC:
				case VERTIGO_DURATION:
				case VERTIGO_RESISTANCE:
				case VULNERABLE_PROC:
				case VULNERABLE_DURATION:
				case VULNERABLE_RESISTANCE:
					return ItemRarity.EPIC;
				case CRIMSON_ECHO:
				case FATAL_SYNCHRONICITY:
				case GLACIAL_REND:
				case SOULBOUND:
				case SPIRITBREAK:
				case STATIC_RUIN:
				case UNBREAKABLE:
					return ItemRarity.LEGENDARY;
				default:
					return ItemRarity.COMMON;
			}
		}

		public boolean hasValue() {
			switch (this) {
				case EMPTY_SLOT:
				case SOULBOUND:
				case UNBREAKABLE:
					return false;
				default:
					return true;
			}
		}

		public String description() {
			switch (this) {
				case ARMOR_BONUS:
					return "Increases the armor blocking power granted by this item.";
				case ATTACK_BONUS:
					return "Increases weapon attack damage by a percentage.";
				case ATTACK_DAMAGE:
					return "Adds flat damage to weapon attacks.";
				case ATTACK_SPEED:
					return "Increases attack speed with this weapon.";
				case ATTACK_ACCURACY:
					return "Increases accuracy with this weapon.";
				case BLOCK_CHANCE:
					return "Gives armor a chance to block incoming damage.";
				case BONUS_LOOT:
					return "Improves the chance for extra loot drops.";
				case CLEAVE_CHANCE:
					return "Gives attacks a chance to strike another nearby enemy.";
				case CRIMSON_ECHO:
					return "Adds a special bleed-based follow-up effect when its required bleed stat is present.";
				case CRITICAL_CHANCE:
					return "Gives attacks or wand damage a chance to critically strike.";
				case CRITICAL_DAMAGE_MULTIPLIER:
					return "Increases the damage dealt by critical hits.";
				case CRITICAL_HIT_RESISTANCE:
					return "Reduces incoming critical-hit pressure while this item is active.";
				case CRITICAL_DAMAGE_REDUCTION:
					return "Reduces damage taken from critical hits while this item is active.";
				case DEFENSE:
					return "Adds flat armor blocking power.";
				case DODGE_CHANCE:
					return "Increases the chance to avoid attacks.";
				case EVASION:
					return "Adds flat evasion.";
				case FATAL_SYNCHRONICITY:
					return "Adds a special finisher-style effect when its required frost, bleed, stun, and weakness stats are present.";
				case FEATHER_FALLING:
					return "Reduces fall damage while this item is active.";
				case FIRE_RESISTANCE:
					return "Reduces fire and burning danger while this item is active.";
				case GLACIAL_REND:
					return "Adds a special frost-based damage effect when its required frost stat is present.";
				case KNOCKBACK_CHANCE:
					return "Gives attacks a chance to knock enemies back.";
				case KNOCKBACK_STRENGTH:
					return "Increases the distance or force of knockback effects.";
				case LIFESTEAL:
					return "Gives attacks a chance to heal the hero from damage dealt.";
				case MAGIC_BONUS:
					return "Increases magic damage by a percentage.";
				case MAGIC_DAMAGE:
					return "Adds flat damage to damage-dealing wands.";
				case MAX_HEALTH:
					return "Increases maximum health while this item is active.";
				case MOVEMENT_SPEED:
					return "Increases movement speed while this item is active.";
				case PIERCING_CHANCE:
					return "Gives attacks a chance to ignore part of enemy defenses.";
				case RESOURCEFUL:
					return "Improves resource-related gains from dungeon rewards.";
				case SOUL_REAPING:
					return "Gives attacks a chance to draw power from defeated or wounded enemies.";
				case SOULBOUND:
					return "Marks the item with a soulbound-style special property.";
				case SPIRITBREAK:
					return "Adds a special weakness-based effect when its required weakness stat is present.";
				case STATIC_RUIN:
					return "Adds a special stun-based lightning effect when its required stun stat is present.";
				case SUMMON_LIGHTNING_CHANCE:
					return "Gives attacks a chance to call down lightning.";
				case THORNS_CHANCE:
					return "Gives armor a chance to retaliate when hit.";
				case THORNS_DAMAGE:
					return "Increases damage dealt by thorns retaliation.";
				case TREASURE_LUCK:
					return "Increases normal monster loot drop chance. This affects whether a monster's usual loot drops, not item rarity, chest rewards, or gold stack size.";
				case UNBREAKABLE:
					return "Prevents the item from being destroyed by durability loss.";
				case WAND_CHARGES:
					return "Adds extra maximum charges to a wand.";
				case WAND_RECHARGE_RATE:
					return "Increases the rate at which a wand recharges.";
				case THROWN_DURABILITY:
					return "Adds extra uses before a thrown weapon breaks.";
				case XP_GAIN:
					return "Increases experience gained.";
				case RING_POTENCY:
					return "Increases the effective level of rings while this item is active.";
				case TRINKET_POTENCY:
					return "Increases the effective level of trinkets while this item is active.";
				case ARTIFACT_POTENCY:
					return "Increases the effective level of artifacts while this item is active.";
				default:
					if (name().endsWith( "_PROC" )) {
						return "Gives this item a chance to apply " + effectName() + ".";
					}
					if (name().endsWith( "_DURATION" )) {
						return "Increases the duration of this item's " + effectName() + " effect.";
					}
					if (name().endsWith( "_RESISTANCE" )) {
						return "Reduces the danger of " + effectName() + " while this item is active.";
					}
					return "Adds a rarity-based special effect.";
			}
		}

		private String effectName() {
			String text = displayName;
			if (text.endsWith( " Proc" )) {
				text = text.substring( 0, text.length() - 5 );
			} else if (text.endsWith( " Duration" )) {
				text = text.substring( 0, text.length() - 9 );
			} else if (text.endsWith( " Resistance" )) {
				text = text.substring( 0, text.length() - 11 );
			}
			return text.toLowerCase();
		}

		public boolean unique() {
			switch (this) {
				case BARKSKIN_PROC:
				case BARKSKIN_POWER:
				case BARRIER_PROC:
				case BARRIER_POWER:
				case BLESS_PROC:
				case BLESS_DURATION:
				case BLINDNESS_PROC:
				case BLINDNESS_DURATION:
				case BLINDNESS_RESISTANCE:
				case BLEED_PROC:
				case BLEED_DURATION:
				case BLEED_RESISTANCE:
				case BLOCK_CHANCE:
				case BONUS_LOOT:
				case BURNING_PROC:
				case BURNING_DURATION:
				case CLEAVE_CHANCE:
				case CORROSION_PROC:
				case CORROSION_DURATION:
				case CORROSION_RESISTANCE:
				case CRIMSON_ECHO:
				case CRITICAL_CHANCE:
				case CRITICAL_DAMAGE_MULTIPLIER:
				case CRITICAL_HIT_RESISTANCE:
				case CRITICAL_DAMAGE_REDUCTION:
				case CRIPPLE_PROC:
				case CRIPPLE_DURATION:
				case CRIPPLE_RESISTANCE:
				case DAZE_PROC:
				case DAZE_DURATION:
				case DAZE_RESISTANCE:
				case DODGE_CHANCE:
				case FATAL_SYNCHRONICITY:
				case FEATHER_FALLING:
				case FIRE_RESISTANCE:
				case FROST_PROC:
				case FROST_DURATION:
				case FROST_RESISTANCE:
				case GLACIAL_REND:
				case HASTE_PROC:
				case HASTE_DURATION:
				case HEX_PROC:
				case HEX_DURATION:
				case HEX_RESISTANCE:
				case KNOCKBACK_CHANCE:
				case KNOCKBACK_STRENGTH:
				case LIFESTEAL:
				case MOVEMENT_SPEED:
				case PIERCING_CHANCE:
				case POISON_PROC:
				case POISON_DURATION:
				case POISON_RESISTANCE:
				case RECHARGING_PROC:
				case RECHARGING_DURATION:
				case RESOURCEFUL:
				case ROOT_PROC:
				case ROOT_DURATION:
				case ROOT_RESISTANCE:
				case SLOW_PROC:
				case SLOW_DURATION:
				case SLOW_RESISTANCE:
				case SOUL_REAPING:
				case SOULBOUND:
				case SPIRITBREAK:
				case STATIC_RUIN:
				case STUN_CHANCE:
				case STUN_DURATION:
				case STUN_RESISTANCE:
				case SUMMON_LIGHTNING_CHANCE:
				case THORNS_CHANCE:
				case THORNS_DAMAGE:
				case UNBREAKABLE:
				case VERTIGO_PROC:
				case VERTIGO_DURATION:
				case VERTIGO_RESISTANCE:
				case VULNERABLE_PROC:
				case VULNERABLE_DURATION:
				case VULNERABLE_RESISTANCE:
				case WEAKNESS_PROC:
				case WEAKNESS_DURATION:
				case WEAKNESS_RESISTANCE:
				case XP_GAIN:
					return true;
				default:
					return !hasValue();
			}
		}

		public Type[] requires() {
			switch (this) {
				case ARMOR_BONUS:
					return new Type[]{ DEFENSE };
				case BARKSKIN_POWER:
					return new Type[]{ BARKSKIN_PROC };
				case BARRIER_POWER:
					return new Type[]{ BARRIER_PROC };
				case BLESS_DURATION:
					return new Type[]{ BLESS_PROC };
				case BLINDNESS_DURATION:
					return new Type[]{ BLINDNESS_PROC };
				case BLEED_DURATION:
				case CRIMSON_ECHO:
					return new Type[]{ BLEED_PROC };
				case BURNING_DURATION:
					return new Type[]{ BURNING_PROC };
				case CORROSION_DURATION:
					return new Type[]{ CORROSION_PROC };
				case CRITICAL_DAMAGE_MULTIPLIER:
					return new Type[]{ CRITICAL_CHANCE };
				case CRIPPLE_DURATION:
					return new Type[]{ CRIPPLE_PROC };
				case DAZE_DURATION:
					return new Type[]{ DAZE_PROC };
				case FATAL_SYNCHRONICITY:
					return new Type[]{ FROST_PROC, BLEED_PROC, STUN_CHANCE, WEAKNESS_PROC };
				case FROST_DURATION:
				case GLACIAL_REND:
					return new Type[]{ FROST_PROC };
				case HASTE_DURATION:
					return new Type[]{ HASTE_PROC };
				case HEX_DURATION:
					return new Type[]{ HEX_PROC };
				case KNOCKBACK_STRENGTH:
					return new Type[]{ KNOCKBACK_CHANCE };
				case POISON_DURATION:
					return new Type[]{ POISON_PROC };
				case RECHARGING_DURATION:
					return new Type[]{ RECHARGING_PROC };
				case ROOT_DURATION:
					return new Type[]{ ROOT_PROC };
				case SLOW_DURATION:
					return new Type[]{ SLOW_PROC };
				case SPIRITBREAK:
					return new Type[]{ WEAKNESS_PROC };
				case STATIC_RUIN:
				case STUN_DURATION:
					return new Type[]{ STUN_CHANCE };
				case THORNS_DAMAGE:
					return new Type[]{ THORNS_CHANCE };
				case VERTIGO_DURATION:
					return new Type[]{ VERTIGO_PROC };
				case VULNERABLE_DURATION:
					return new Type[]{ VULNERABLE_PROC };
				case WEAKNESS_DURATION:
					return new Type[]{ WEAKNESS_PROC };
				default:
					return new Type[0];
			}
		}

		public boolean allowedFor( ItemRarity rarity ) {
			if (rarity == null) return false;
			if (rarity == ItemRarity.TRANSCENDANT) return true;
			return rarityAtLeast( rarity, minimumRarity() );
		}

		private static boolean rarityAtLeast( ItemRarity rarity, ItemRarity minimum ) {
			return rarity.ordinal() >= minimum.ordinal();
		}
	}

	private final Type type;
	private int value;
	private boolean locked;

	public RarityStat( Type type, int value ) {
		this( type, value, false );
	}

	public RarityStat( Type type, int value, boolean locked ) {
		this.type = type;
		this.value = type == null ? value : type.capValue( value );
		this.locked = locked;
	}

	public Type type() {
		return type;
	}

	public int value() {
		return type == null ? value : type.capValue( value );
	}

	public boolean locked() {
		return locked;
	}

	public void locked( boolean locked ) {
		if (!isEmptySlot()) this.locked = locked;
	}

	public boolean isEmptySlot() {
		return type == Type.EMPTY_SLOT;
	}

	public RarityStat copy() {
		return new RarityStat( type, value(), locked );
	}

	public void increase( int amount ) {
		if (type.hasValue()) value = type.capValue( value + Math.max( 1, amount ) );
	}

	public String displayText() {
		if (isEmptySlot()) return "@@C888888@@Empty Slot@@CEND@@";
		String lockText = locked ? "@@CFFE866@@Locked @@CEND@@" : "";
		if (!type.hasValue()) return lockText + coloredDisplayName();
		int displayValue = value();
		return lockText + (displayValue > 0 ? "+" : "") + displayValue + (type.percent() ? "% " : " ") + coloredDisplayName();
	}

	public String compactDisplayText() {
		if (isEmptySlot()) return "@@C888888@@Empty@@CEND@@";
		String lockText = locked ? "@@CFFE866@@L @@CEND@@" : "";
		if (!type.hasValue()) return lockText + coloredDisplayName( type.compactDisplayName() );
		int displayValue = value();
		return lockText + (displayValue > 0 ? "+" : "") + displayValue + (type.percent() ? "% " : " ") + coloredDisplayName( type.compactDisplayName() );
	}

	private String coloredDisplayName() {
		return coloredDisplayName( type.displayName() );
	}

	private String coloredDisplayName( String displayName ) {
		return "@@C" + String.format( "%06X", type.displayColor() & 0xFFFFFF ) + "@@" + displayName + "@@CEND@@";
	}

	public String saveString() {
		return type.name() + ":" + value() + (locked ? ":locked" : "");
	}

	public static RarityStat fromSaveString( String data ) {
		if (data == null) return null;
		String[] parts = data.split( ":" );
		if (parts.length < 2) return null;

		try {
			return new RarityStat( Type.valueOf( parts[0] ), Integer.parseInt( parts[1] ), parts.length > 2 && "locked".equals( parts[2] ) );
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
