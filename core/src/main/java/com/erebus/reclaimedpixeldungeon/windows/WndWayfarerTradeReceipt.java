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

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndWayfarerTradeReceipt extends Window {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 170 );
	private static final int GAP = 4;
	private static final int SLOT_SIZE = 32;
	private static final int SLOT_GAP = 5;
	private static final int RESOURCE_ICON_SIZE = 10;
	private static final int RESOURCE_ROW_HEIGHT = 13;
	private static final int TITLE_COLOR = 0xFFFF44;
	private static final int RECEIVED_COLOR = 0x66FF66;

	private float pos;
	private int resourceColumn;

	public WndWayfarerTradeReceipt( WayfarerTradePayload payload ) {
		super();
		if (payload == null) {
			payload = new WayfarerTradePayload();
		}

		RenderedTextBlock title = PixelScene.renderTextBlock( "Trade Received", 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH - 6 );
		title.setPos( (WIDTH - title.width()) / 2f, GAP );
		PixelScene.align( title );
		add( title );
		pos = title.bottom() + GAP;

		RenderedTextBlock saved = PixelScene.renderTextBlock( "The exchange has saved these received goods.", 6 );
		saved.maxWidth( WIDTH - 8 );
		saved.hardlight( RECEIVED_COLOR );
		saved.setPos( 4, pos );
		add( saved );
		pos = saved.bottom() + GAP;

		addItems( payload );
		addResources( payload );

		RedButton ok = new RedButton( "OK" ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		ok.setRect( 4, pos + GAP, WIDTH - 8, 18 );
		add( ok );
		pos = ok.bottom() + GAP;

		resize( WIDTH, (int)Math.ceil( pos ) );
	}

	private void addItems( WayfarerTradePayload payload ) {
		float slotsWidth = SLOT_SIZE * WayfarerTradePayload.ITEM_SLOTS
				+ SLOT_GAP * (WayfarerTradePayload.ITEM_SLOTS - 1);
		float slotsX = (WIDTH - slotsWidth) / 2f;

		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			final Item item = payload.item( i );
			InventorySlot slot = new InventorySlot( item ) {
				@Override
				protected void onClick() {
					if (item != null) {
						GameScene.show( new WndInfoItem( item ) );
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item == null) return false;
					GameScene.show( new WndInfoItem( item ) );
					return true;
				}
			};
			slot.setRect( slotsX + i * (SLOT_SIZE + SLOT_GAP), pos, SLOT_SIZE, SLOT_SIZE );
			if (item == null) {
				slot.clear();
				slot.enable( false );
			}
			add( slot );
		}
		pos += SLOT_SIZE + GAP;
	}

	private void addResources( WayfarerTradePayload payload ) {
		resourceColumn = 0;
		boolean any = false;
		if (payload.gold() > 0) {
			addResource( Icons.get( Icons.COIN_SML ), payload.gold(), WndHomebaseFacility.goldColor() );
			any = true;
		}
		if (payload.energy() > 0) {
			addResource( Icons.get( Icons.ENERGY_SML ), payload.energy(), WndHomebaseFacility.energyColor() );
			any = true;
		}
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			int amount = payload.material( material );
			if (amount > 0) {
				addResource( new ItemSprite( WndHomebaseFacility.materialIcon( material ) ), amount, WndHomebaseFacility.materialColor( material ) );
				any = true;
			}
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			int amount = payload.forge( resource );
			if (amount > 0) {
				addResource( new ItemSprite( WndHomebaseFacility.forgeIcon( resource ) ), amount, WndHomebaseFacility.forgeColor( resource ) );
				any = true;
			}
		}
		if (resourceColumn == 1) {
			resourceColumn = 0;
			pos += RESOURCE_ROW_HEIGHT + 1;
		}
		if (any) pos += GAP;
	}

	private void addResource( Image icon, int amount, int color ) {
		float columnWidth = (WIDTH - 10) / 2f;
		float x = 4 + resourceColumn * columnWidth;
		if (icon != null) {
			if (icon.width > 0 && icon.height > 0) {
				float scale = Math.min( RESOURCE_ICON_SIZE / icon.width, RESOURCE_ICON_SIZE / icon.height );
				icon.scale.set( scale );
			}
			icon.x = x;
			icon.y = pos + (RESOURCE_ROW_HEIGHT - icon.height()) / 2f;
			PixelScene.align( icon );
			add( icon );
		}

		RenderedTextBlock text = PixelScene.renderTextBlock( "x" + WndHomebaseFacility.compactAmount( amount ), 6 );
		text.hardlight( color );
		text.maxWidth( (int)(columnWidth - RESOURCE_ICON_SIZE - 3) );
		text.setPos( x + RESOURCE_ICON_SIZE + 3, pos + (RESOURCE_ROW_HEIGHT - text.height()) / 2f );
		add( text );

		resourceColumn++;
		if (resourceColumn >= 2) {
			resourceColumn = 0;
			pos += RESOURCE_ROW_HEIGHT + 1;
		}
	}
}
