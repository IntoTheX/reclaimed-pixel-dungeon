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

public enum ItemRarity {

	COMMON( "Common", 7, 1, 0xBBBBBB, 0.16f, 0 ),
	UNCOMMON( "Uncommon", 7, 1, 0x55CC55, 0.28f, 25 ),
	RARE( "Rare", 7, 2, 0x4499FF, 0.32f, 55 ),
	EPIC( "Epic", 7, 3, 0xBB66FF, 0.36f, 90 ),
	LEGENDARY( "Legendary", 7, 4, 0xFFB33A, 0.40f, 140 ),
	TRANSCENDANT( "Transcendant", 4, 5, 0xFFE866, 0.44f, 220 );

	private final String displayName;
	private final int statSlots;
	private final int power;
	private final int color;
	private final float auraAlpha;
	private final int priceBonusPercent;

	ItemRarity( String displayName, int statSlots, int power, int color, float auraAlpha, int priceBonusPercent ) {
		this.displayName = displayName;
		this.statSlots = statSlots;
		this.power = power;
		this.color = color;
		this.auraAlpha = auraAlpha;
		this.priceBonusPercent = priceBonusPercent;
	}

	public String displayName() {
		return displayName;
	}

	public int statSlots() {
		return statSlots;
	}

	public int power() {
		return power;
	}

	public int color() {
		return color;
	}

	public float auraAlpha() {
		return auraAlpha;
	}

	public int priceBonusPercent() {
		return priceBonusPercent;
	}

	public boolean isVisible() {
		return true;
	}

	public boolean hasAura() {
		return auraAlpha > 0f;
	}
}
