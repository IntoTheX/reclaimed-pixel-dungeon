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

package com.erebus.reclaimedpixeldungeon.items;

import java.util.Map;
import java.util.WeakHashMap;

public class ItemPreviewContext {

	private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();
	private static final Map<Item, Context> ITEM_CONTEXTS = new WeakHashMap<>();

	public static void set( int ringPotency, int artifactPotency, int trinketPotency ) {
		CONTEXT.set( new Context( ringPotency, artifactPotency, trinketPotency ) );
	}

	public static void clear() {
		CONTEXT.remove();
	}

	public static void register( Item item, int ringPotency, int artifactPotency, int trinketPotency ) {
		if (item != null) {
			ITEM_CONTEXTS.put( item, new Context( ringPotency, artifactPotency, trinketPotency ) );
		}
	}

	public static int ringPotency( int fallback ) {
		Context context = CONTEXT.get();
		return context == null ? fallback : context.ringPotency;
	}

	public static int ringPotency( Item item, int fallback ) {
		Context context = item == null ? null : ITEM_CONTEXTS.get( item );
		return context == null ? ringPotency( fallback ) : context.ringPotency;
	}

	public static int artifactPotency( int fallback ) {
		Context context = CONTEXT.get();
		return context == null ? fallback : context.artifactPotency;
	}

	public static int artifactPotency( Item item, int fallback ) {
		Context context = item == null ? null : ITEM_CONTEXTS.get( item );
		return context == null ? artifactPotency( fallback ) : context.artifactPotency;
	}

	public static int trinketPotency( int fallback ) {
		Context context = CONTEXT.get();
		return context == null ? fallback : context.trinketPotency;
	}

	public static int trinketPotency( Item item, int fallback ) {
		Context context = item == null ? null : ITEM_CONTEXTS.get( item );
		return context == null ? trinketPotency( fallback ) : context.trinketPotency;
	}

	private static class Context {
		final int ringPotency;
		final int artifactPotency;
		final int trinketPotency;

		Context( int ringPotency, int artifactPotency, int trinketPotency ) {
			this.ringPotency = ringPotency;
			this.artifactPotency = artifactPotency;
			this.trinketPotency = trinketPotency;
		}
	}
}
