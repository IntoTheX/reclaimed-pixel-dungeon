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
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class WayfarerTradePayload {

	public static final int ITEM_SLOTS = 3;

	private static final String ITEM = "item_";
	private static final String ITEM_TEMPLATES = "item_templates";
	private static final String MINIMUM_RARITIES = "minimum_rarities";
	private static final String GOLD = "gold";
	private static final String ENERGY = "energy";
	private static final String MATERIALS = "materials";
	private static final String FORGE = "forge";
	private static final String EMERALD_COST = "emerald_cost";

	private final Item[] items = new Item[ITEM_SLOTS];
	private int[] itemTemplates = new int[ITEM_SLOTS];
	private int[] minimumRarities = new int[ITEM_SLOTS];
	private int gold;
	private int energy;
	private int[] materials = new int[HomebaseState.Material.values().length];
	private int[] forge = new int[HomebaseState.ForgeResource.values().length];
	private int reservedEmeraldCost = -1;

	public Item item( int slot ) {
		return slot >= 0 && slot < ITEM_SLOTS ? items[slot] : null;
	}

	public void item( int slot, Item item ) {
		if (slot >= 0 && slot < ITEM_SLOTS) {
			items[slot] = item;
			if (item == null) {
				itemTemplates[slot] = 0;
				minimumRarities[slot] = 0;
			}
		}
	}

	public boolean itemTemplate( int slot ) {
		return slot >= 0 && slot < ITEM_SLOTS && itemTemplates[slot] != 0;
	}

	public void itemTemplate( int slot, boolean template ) {
		if (slot >= 0 && slot < ITEM_SLOTS) itemTemplates[slot] = template ? 1 : 0;
	}

	public ItemRarity minimumRarity( int slot ) {
		if (!itemTemplate( slot )) return null;
		int ordinal = minimumRarities[slot];
		return ordinal >= 0 && ordinal < ItemRarity.values().length
				? ItemRarity.values()[ordinal] : ItemRarity.COMMON;
	}

	public void minimumRarity( int slot, ItemRarity rarity ) {
		if (slot < 0 || slot >= ITEM_SLOTS) return;
		minimumRarities[slot] = rarity == null ? ItemRarity.COMMON.ordinal() : rarity.ordinal();
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

	public int emeraldCost() {
		return totalEmeraldCost( this, null );
	}

	public static int totalEmeraldCost( WayfarerTradePayload first, WayfarerTradePayload second ) {
		long items = itemCount( first ) + itemCount( second );
		long resources = resourceCount( first ) + resourceCount( second );
		long resourceCost = resources == 0 ? 0 : (resources + 9_999L) / 10_000L;
		return (int)Math.min( Integer.MAX_VALUE, items + resourceCost );
	}

	public static int emeraldShare( WayfarerTradePayload sent, WayfarerTradePayload received,
			boolean paysExactTie ) {
		int total = totalEmeraldCost( sent, received );
		int share = total / 2;
		if ((total & 1) == 0) return share;

		long sentWeight = tradeWeight( sent );
		long receivedWeight = tradeWeight( received );
		if (receivedWeight > sentWeight || (receivedWeight == sentWeight && paysExactTie)) share++;
		return share;
	}

	private static long itemCount( WayfarerTradePayload payload ) {
		if (payload == null) return 0;
		long count = 0;
		for (Item item : payload.items) if (item != null) count += Math.max( 1, item.quantity() );
		return count;
	}

	private static long resourceCount( WayfarerTradePayload payload ) {
		if (payload == null) return 0;
		long count = (long)payload.gold + payload.energy;
		for (int amount : payload.materials) count += amount;
		for (int amount : payload.forge) count += amount;
		return count;
	}

	private static long tradeWeight( WayfarerTradePayload payload ) {
		long items = itemCount( payload );
		long resources = resourceCount( payload );
		if (items > (Long.MAX_VALUE - resources) / 10_000L) return Long.MAX_VALUE;
		return items * 10_000L + resources;
	}

	public void reservedEmeraldCost( int cost ) {
		reservedEmeraldCost = Math.max( 0, cost );
	}

	public int reservedEmeraldCost() {
		// Legacy offers predate variable fees and always reserved one Emerald.
		return reservedEmeraldCost >= 0 ? reservedEmeraldCost : 1;
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
		bundle.put( ITEM_TEMPLATES, itemTemplates );
		bundle.put( MINIMUM_RARITIES, minimumRarities );
		bundle.put( ENERGY, energy );
		bundle.put( MATERIALS, materials );
		bundle.put( FORGE, forge );
		bundle.put( EMERALD_COST, reservedEmeraldCost );
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
			int[] templates = bundle.getIntArray( ITEM_TEMPLATES );
			if (templates != null) {
				for (int i = 0; i < Math.min( templates.length, ITEM_SLOTS ); i++) {
					payload.itemTemplates[i] = templates[i] == 0 ? 0 : 1;
				}
			}
			int[] rarities = bundle.getIntArray( MINIMUM_RARITIES );
			if (rarities != null) {
				for (int i = 0; i < Math.min( rarities.length, ITEM_SLOTS ); i++) {
					payload.minimumRarities[i] = Math.max( 0,
							Math.min( ItemRarity.values().length - 1, rarities[i] ) );
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
			if (bundle.contains( EMERALD_COST )) {
				payload.reservedEmeraldCost = Math.max( 0, bundle.getInt( EMERALD_COST ) );
			}
		} catch (Exception e) {
			Game.reportException( e );
		}
		return payload;
	}
}
