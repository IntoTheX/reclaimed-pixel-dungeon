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

package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.items.Item;
import com.watabou.noosa.ui.Component;

/** A clickable item control which uses the same visual treatment as inventory cells. */
public class InventoryItemButton extends Component {

	protected InventorySlot slot;

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new InventorySlot( null ) {
			@Override
			protected void onClick() {
				InventoryItemButton.this.onClick();
			}

			@Override
			protected boolean onLongClick() {
				return InventoryItemButton.this.onLongClick();
			}
		};
		slot.enable( true );
		add( slot );
	}

	protected void onClick() {
	}

	protected boolean onLongClick() {
		return false;
	}

	@Override
	protected void layout() {
		super.layout();
		slot.setRect( x, y, width, height );
		slot.setMargins( width >= 24 || height >= 24 ? 2 : 1,
				width >= 24 || height >= 24 ? 2 : 1,
				width >= 24 || height >= 24 ? 2 : 1,
				width >= 24 || height >= 24 ? 2 : 1 );
	}

	public Item item() {
		return slot.item();
	}

	public void item( Item item ) {
		slot.item( item );
		// Placeholder items still need to remain clickable.
		slot.enable( true );
	}

	public InventorySlot slot() {
		return slot;
	}

	public void forceIdentifiedAppearance(boolean forceIdentifiedAppearance) {
		slot.forceIdentifiedAppearance(forceIdentifiedAppearance);
	}
}
