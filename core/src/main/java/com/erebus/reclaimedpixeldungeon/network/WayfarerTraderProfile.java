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

package com.erebus.reclaimedpixeldungeon.network;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class WayfarerTraderProfile {

	private static final String NAME = "name";
	private static final String CHARACTER_ID = "character_id";
	private static final String HERO_CLASS = "hero_class";
	private static final String ARMOR_TIER = "armor_tier";
	private static final String LEVEL = "level";
	private static final String RING_POTENCY = "ring_potency";
	private static final String ARTIFACT_POTENCY = "artifact_potency";
	private static final String TRINKET_POTENCY = "trinket_potency";
	private static final String ITEM = "item_";

	public static final int WEAPON = 0;
	public static final int ARMOR = 1;
	public static final int ARTIFACT_1 = 2;
	public static final int ARTIFACT_2 = 3;
	public static final int ARTIFACT_3 = 4;
	public static final int MISC_1 = 5;
	public static final int MISC_2 = 6;
	public static final int MISC = MISC_1;
	public static final int RING_1 = 7;
	public static final int RING_2 = 8;
	public static final int RING_3 = 9;
	public static final int SLOT_COUNT = 10;

	public String name = "Trader";
	public String characterId = "";
	public String heroClass = "WARRIOR";
	public int armorTier = 0;
	public int level = 1;
	public int ringPotency = 0;
	public int artifactPotency = 0;
	public int trinketPotency = 0;
	private final Item[] equipment = new Item[SLOT_COUNT];

	public static WayfarerTraderProfile local( String fallbackName, String fallbackClass, int fallbackArmorTier ) {
		WayfarerTraderProfile profile = new WayfarerTraderProfile();
		profile.characterId = Dungeon.wayfarerCharacterId();
		profile.name = clean( fallbackName );
		profile.heroClass = clean( fallbackClass );
		profile.armorTier = Math.max( 0, fallbackArmorTier );
		if (Dungeon.hero != null) {
			profile.name = clean( Dungeon.hero.characterName() );
			profile.heroClass = Dungeon.hero.heroClass.name();
			profile.armorTier = Dungeon.hero.tier();
			profile.level = Math.max( 1, Dungeon.hero.lvl );
			if (Dungeon.homebase != null) {
				profile.ringPotency = Dungeon.homebase.trainingBonus( HomebaseState.Training.RING_POTENCY );
				profile.artifactPotency = Dungeon.homebase.trainingBonus( HomebaseState.Training.ARTIFACT_POTENCY );
				profile.trinketPotency = Dungeon.homebase.trainingBonus( HomebaseState.Training.TRINKET_POTENCY );
			}
			if (Dungeon.hero.belongings != null) {
				profile.equipment[WEAPON] = duplicate( Dungeon.hero.belongings.weapon() );
				profile.equipment[ARMOR] = duplicate( Dungeon.hero.belongings.armor() );
				profile.equipment[ARTIFACT_1] = duplicate( Dungeon.hero.belongings.artifact() );
				profile.equipment[ARTIFACT_2] = duplicate( Dungeon.hero.belongings.artifact2() );
				profile.equipment[ARTIFACT_3] = duplicate( Dungeon.hero.belongings.artifact3() );
				profile.equipment[MISC_1] = duplicate( Dungeon.hero.belongings.misc() );
				profile.equipment[MISC_2] = duplicate( Dungeon.hero.belongings.misc2() );
				profile.equipment[RING_1] = duplicate( Dungeon.hero.belongings.ring() );
				profile.equipment[RING_2] = duplicate( Dungeon.hero.belongings.ring2() );
				profile.equipment[RING_3] = duplicate( Dungeon.hero.belongings.ring3() );
			}
		}
		return profile;
	}

	public Item item( int slot ) {
		return slot >= 0 && slot < equipment.length ? equipment[slot] : null;
	}

	public String toPacket() {
		Bundle bundle = new Bundle();
		bundle.put( NAME, name );
		bundle.put( CHARACTER_ID, characterId );
		bundle.put( HERO_CLASS, heroClass );
		bundle.put( ARMOR_TIER, armorTier );
		bundle.put( LEVEL, level );
		bundle.put( RING_POTENCY, ringPotency );
		bundle.put( ARTIFACT_POTENCY, artifactPotency );
		bundle.put( TRINKET_POTENCY, trinketPotency );
		for (int i = 0; i < equipment.length; i++) {
			if (equipment[i] != null) bundle.put( ITEM + i, equipment[i] );
		}
		return bundle.toString();
	}

	public static WayfarerTraderProfile fromPacket( String packet ) {
		WayfarerTraderProfile profile = new WayfarerTraderProfile();
		if (packet == null || packet.isEmpty()) return profile;
		try {
			Bundle bundle = Bundle.read( new ByteArrayInputStream( packet.getBytes( StandardCharsets.UTF_8 ) ) );
			profile.name = clean( bundle.getString( NAME ) );
			profile.characterId = bundle.getString( CHARACTER_ID );
			profile.heroClass = clean( bundle.getString( HERO_CLASS ) );
			profile.armorTier = Math.max( 0, bundle.getInt( ARMOR_TIER ) );
			profile.level = Math.max( 1, bundle.getInt( LEVEL ) );
			profile.ringPotency = Math.max( 0, bundle.getInt( RING_POTENCY ) );
			profile.artifactPotency = Math.max( 0, bundle.getInt( ARTIFACT_POTENCY ) );
			profile.trinketPotency = Math.max( 0, bundle.getInt( TRINKET_POTENCY ) );
			for (int i = 0; i < profile.equipment.length; i++) {
				if (bundle.contains( ITEM + i )) {
					Object item = bundle.get( ITEM + i );
					if (item instanceof Item) profile.equipment[i] = (Item)item;
				}
			}
		} catch (Exception e) {
			Game.reportException( e );
		}
		return profile;
	}

	private static Item duplicate( Item item ) {
		return item == null ? null : item.duplicate();
	}

	private static String clean( String text ) {
		if (text == null || text.trim().isEmpty()) return "Trader";
		return text.replace( '|', ' ' ).replace( '\n', ' ' ).replace( '\r', ' ' ).trim();
	}
}
