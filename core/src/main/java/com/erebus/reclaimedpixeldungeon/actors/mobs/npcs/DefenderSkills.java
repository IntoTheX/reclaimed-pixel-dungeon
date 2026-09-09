/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2012-2026 Evan Debenham
 *
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.erebus.reclaimedpixeldungeon.actors.mobs.npcs;

import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/** Persistent combat specialties used by Rare and higher homebase defenders. */
public class DefenderSkills implements Bundlable {

	private static final String LEVELS = "levels";

	public enum Skill {
		BLOODRUSH( "Bloodrush", "Attacks faster while pressing an enemy." ),
		KEEN_HUNTER( "Keen Hunter", "Improves attack accuracy." ),
		THICK_HIDE( "Thick Hide", "Reduces incoming damage." ),
		VAMPIRIC_EDGE( "Vampiric Edge", "Restores health from damage dealt." ),
		RETALIATORY_SPINES( "Retaliatory Spines", "Returns part of melee damage to attackers." ),
		BATTLE_TRANCE( "Battle Trance", "Deals more damage while below half health." ),
		CRIMSON_RENEWAL( "Crimson Renewal", "Improves the defender's emergency recovery." ),
		EARTHEN_COVENANT( "Earthen Covenant", "Improves armor while wounded." );

		public final String title;
		public final String description;

		Skill( String title, String description ) {
			this.title = title;
			this.description = description;
		}
	}

	private int[] levels = new int[Skill.values().length];

	public static DefenderSkills forRarity( ItemRarity rarity ) {
		DefenderSkills result = new DefenderSkills();
		int count = rarity == null ? 0 : Math.max( 0, rarity.power() - ItemRarity.UNCOMMON.power() );
		for (int i = 0; i < count; i++) result.grow( rarity );
		return result;
	}

	public int level( Skill skill ) {
		return skill == null ? 0 : levels[skill.ordinal()];
	}

	public boolean hasSkills() {
		for (int level : levels) if (level > 0) return true;
		return false;
	}

	public DefenderSkills copy() {
		DefenderSkills copy = new DefenderSkills();
		System.arraycopy( levels, 0, copy.levels, 0, levels.length );
		return copy;
	}

	public boolean grow( ItemRarity rarity ) {
		if (rarity == null || rarity.power() < ItemRarity.RARE.power()) return false;
		int cap = Math.max( 1, rarity.power() );
		int candidates = 0;
		for (int level : levels) if (level < cap) candidates++;
		if (candidates == 0) return false;
		int selected = Random.Int( candidates );
		for (int i = 0; i < levels.length; i++) {
			if (levels[i] < cap && selected-- == 0) {
				levels[i]++;
				return true;
			}
		}
		return false;
	}

	public String description() {
		StringBuilder text = new StringBuilder();
		for (Skill skill : Skill.values()) {
			int level = level( skill );
			if (level > 0) text.append( "\n_" ).append( skill.title ).append( " Lv. " )
					.append( level ).append( "_ - " ).append( skill.description );
		}
		return text.toString();
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		int[] restored = bundle.getIntArray( LEVELS );
		levels = new int[Skill.values().length];
		if (restored != null) System.arraycopy( restored, 0, levels, 0, Math.min( restored.length, levels.length ) );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( LEVELS, levels );
	}
}
