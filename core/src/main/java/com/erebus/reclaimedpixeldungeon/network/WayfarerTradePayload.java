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

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class WayfarerTradePayload {

	public static final int ITEM_SLOTS = 3;

	private static final String ITEM = "item_";
	private static final String GOLD = "gold";
	private static final String ENERGY = "energy";
	private static final String MATERIALS = "materials";
	private static final String FORGE = "forge";

	private final Item[] items = new Item[ITEM_SLOTS];
	private int gold;
	private int energy;
	private int[] materials = new int[HomebaseState.Material.values().length];
	private int[] forge = new int[HomebaseState.ForgeResource.values().length];

	public Item item( int slot ) {
		return slot >= 0 && slot < ITEM_SLOTS ? items[slot] : null;
	}

	public void item( int slot, Item item ) {
		if (slot >= 0 && slot < ITEM_SLOTS) items[slot] = item;
	}

	public int gold() {
		return gold;
	}

	public void gold( int gold ) {
		this.gold = Math.max( 0, gold );
	}

	public int energy() {
		return energy;
	}

	public void energy( int energy ) {
		this.energy = Math.max( 0, energy );
	}

	public int material( HomebaseState.Material material ) {
		return material == null ? 0 : materials[material.ordinal()];
	}

	public void material( HomebaseState.Material material, int amount ) {
		if (material != null) materials[material.ordinal()] = Math.max( 0, amount );
	}

	public int forge( HomebaseState.ForgeResource resource ) {
		return resource == null ? 0 : forge[resource.ordinal()];
	}

	public void forge( HomebaseState.ForgeResource resource, int amount ) {
		if (resource != null) forge[resource.ordinal()] = Math.max( 0, amount );
	}

	public boolean isEmpty() {
		if (gold > 0 || energy > 0) return false;
		for (int amount : materials) if (amount > 0) return false;
		for (int amount : forge) if (amount > 0) return false;
		for (Item item : items) if (item != null) return false;
		return true;
	}

	public WayfarerTradePayload copy() {
		return fromPacket( toPacket() );
	}

	public String toPacket() {
		Bundle bundle = new Bundle();
		for (int i = 0; i < ITEM_SLOTS; i++) {
			if (items[i] != null) bundle.put( ITEM + i, items[i] );
		}
		bundle.put( GOLD, gold );
		bundle.put( ENERGY, energy );
		bundle.put( MATERIALS, materials );
		bundle.put( FORGE, forge );
		return bundle.toString();
	}

	public static WayfarerTradePayload fromPacket( String packet ) {
		WayfarerTradePayload payload = new WayfarerTradePayload();
		if (packet == null || packet.isEmpty()) return payload;
		try {
			Bundle bundle = Bundle.read( new ByteArrayInputStream( packet.getBytes( StandardCharsets.UTF_8 ) ) );
			for (int i = 0; i < ITEM_SLOTS; i++) {
				if (bundle.contains( ITEM + i )) {
					Object item = bundle.get( ITEM + i );
					if (item instanceof Item) payload.items[i] = (Item)item;
				}
			}
			payload.gold = Math.max( 0, bundle.getInt( GOLD ) );
			payload.energy = Math.max( 0, bundle.getInt( ENERGY ) );
			int[] materialValues = bundle.getIntArray( MATERIALS );
			if (materialValues != null) {
				for (int i = 0; i < Math.min( materialValues.length, payload.materials.length ); i++) {
					payload.materials[i] = Math.max( 0, materialValues[i] );
				}
			}
			int[] forgeValues = bundle.getIntArray( FORGE );
			if (forgeValues != null) {
				for (int i = 0; i < Math.min( forgeValues.length, payload.forge.length ); i++) {
					payload.forge[i] = Math.max( 0, forgeValues[i] );
				}
			}
		} catch (Exception e) {
			Game.reportException( e );
		}
		return payload;
	}
}
