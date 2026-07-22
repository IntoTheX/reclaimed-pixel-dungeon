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

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.HomebaseDefender;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MagesStaff;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.ui.ItemSlot;
import com.erebus.reclaimedpixeldungeon.ui.Window;

final class DefenderUi {

	private static final int CYAN = 0x44CCFF;
	private static final int BLUE = 0x4499FF;
	private static final int ORANGE = 0xFF8844;
	private static final int GREEN = 0x66FF66;
	private static final int RED = 0xFF4444;
	private static final int GOLD = 0xFFFF44;

	private DefenderUi() {
	}

	static String infoText( HomebaseDefender defender, HomebaseState.DefenderRecord record ) {
		int level = record == null ? 1 : record.level();
		int xp = record == null ? 0 : record.xp();
		int xpToNext = record == null ? 10 : record.xpToNext();
		int strength = record == null ? 1 : record.strength();
		int minDamage = record == null ? 0 : record.minDamage();
		int maxDamage = record == null ? 0 : record.maxDamage();
		int maxArmor = record == null ? 0 : record.maxArmor();
		Item weapon = record == null ? defender.weapon() : record.weapon();
		Item armor = record == null ? defender.armor() : record.armor();
		Item ranged = record == null ? defender.ranged() : record.ranged();

		return colorText( CYAN, "Level " + level )
				+ "\n" + colorText( BLUE, "XP " + xp + "/" + xpToNext )
				+ "\n" + colorText( ORANGE, "Strength " + strength )
				+ "\n" + colorText( GREEN, "Health " + defender.HP + "/" + defender.HT )
				+ "\n" + colorText( RED, "Damage " + minDamage + "-" + maxDamage )
				+ "\n" + colorText( Window.WHITE, "Armor 0-" + maxArmor )
				+ "\n" + equipmentLine( "Weapon", weapon, strength )
				+ "\n" + equipmentLine( "Armor", armor, strength )
				+ "\n" + equipmentLine( "Ranged", ranged, strength )
				+ "\n" + suppliesLine( record );
	}

	static String equipmentLines( Item weapon, Item armor, Item ranged, int strength ) {
		return equipmentLine( "Wpn", weapon, strength )
				+ "\n" + equipmentLine( "Arm", armor, strength )
				+ "\n" + equipmentLine( "Rng", ranged, strength );
	}

	static String equipmentLine( String label, Item item, int strength ) {
		return label + ": " + equipmentName( item, strength );
	}

	static String equipmentName( Item item, int strength ) {
		if (item == null) return colorText( Window.WHITE, "none" );

		String text = colorText( itemColor( item ), Messages.titleCase( item.name() ) );
		String uses = rangedUses( item );
		if (uses != null) {
			text += " [" + colorText( Window.WHITE, uses ) + "]";
		}

		int req = strengthRequirement( item );
		if (req > 0) {
			text += " (" + colorText( ORANGE, "STR " + req );
			if (strength > 0 && req > strength) {
				text += ", " + colorText( ItemSlot.DEGRADED, "-" + (req - strength) );
			}
			text += ")";
		}
		return text;
	}

	static String itemTitle( Item item ) {
		if (item == null) return colorText( Window.WHITE, "none" );
		return colorText( itemColor( item ), Messages.titleCase( item.name() ) );
	}

	static String suppliesLine( HomebaseState.DefenderRecord record ) {
		int ankhs = record == null ? 0 : record.ankhs();
		int healing = record == null ? 0 : record.healingPotions();
		int invis = record == null ? 0 : record.invisibilityPotions();
		return "Supplies: "
				+ colorText( GOLD, "Ankh " + ankhs )
				+ " " + colorText( GREEN, "Healing " + healing )
				+ " " + colorText( Window.WHITE, "Invisibility " + invis );
	}

	static String colorText( int color, String text ) {
		return "@@C" + String.format( "%06X", color & 0xFFFFFF ) + "@@" + text + "@@CEND@@";
	}

	private static int itemColor( Item item ) {
		if (item != null && item.hasVisibleRarityStats()) {
			ItemRarity rarity = item.rarity();
			if (rarity != null) return rarity.color();
		}
		return Window.WHITE;
	}

	private static int strengthRequirement( Item item ) {
		if (item instanceof Weapon) {
			return ((Weapon)item).STRReq();
		} else if (item instanceof Armor) {
			return ((Armor)item).STRReq();
		}
		return 0;
	}

	private static String rangedUses( Item item ) {
		if (item instanceof Wand) {
			Wand wand = (Wand)item;
			return wand.curCharges + "/" + wand.maxCharges();
		}
		if (item instanceof MagesStaff && ((MagesStaff)item).imbuedWand() != null) {
			Wand wand = ((MagesStaff)item).imbuedWand();
			return wand.curCharges + "/" + wand.maxCharges();
		}
		if (item instanceof SpiritBow) {
			return "unlimited";
		}
		if (item instanceof MissileWeapon) {
			MissileWeapon missile = (MissileWeapon)item;
			float use = missile.durabilityPerUse();
			if (use <= 0) return "unlimited";
			int current = Math.max( 1, (int)Math.ceil( missile.durabilityLeft() / use ) );
			int max = Math.max( 1, (int)Math.ceil( MissileWeapon.MAX_DURABILITY / use ) );
			return current + "/" + max;
		}
		return null;
	}
}
