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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Blob;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.tiles.CustomTilemap;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTerrainTilemap;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

import java.io.IOException;

public class WndInfoCell extends Window {
	
	private static final float GAP	= 2;
	
	private static final int WIDTH = ReclaimedWindow.modalWidth( 120 );

	public static Image cellImage( int cell ){
		int tile = Dungeon.level.map[cell];
		if (Dungeon.level.water[cell]) {
			tile = Terrain.WATER;
		} else if (Dungeon.level.pit[cell]) {
			tile = Terrain.CHASM;
		}

		Image customImage = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		for (CustomTilemap i : Dungeon.level.customTiles){
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if ((customImage = i.image(x - i.tileX, y - i.tileY)) != null) {
					break;
				}
			}
		}

		if (customImage != null){
			return customImage;
		} else {

			if (tile == Terrain.WATER) {
				Image water = new Image(Dungeon.level.waterTex());
				water.frame(0, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE);
				return water;
			} else {
				return DungeonTerrainTilemap.tile(cell, tile);
			}
		}
	}

	public static String cellName( int cell ){

		CustomTilemap customTile = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		for (CustomTilemap i : Dungeon.level.customTiles){
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if (i.image(x - i.tileX, y - i.tileY) != null) {
					x -= i.tileX;
					y -= i.tileY;
					customTile = i;
					break;
				}
			}
		}

		if (customTile != null && customTile.name(x, y) != null){
			return customTile.name(x, y);
		} else {
			return Dungeon.level.tileName(Dungeon.level.map[cell]);
		}
	}
	
	public WndInfoCell( int cell ) {
		
		super();

		CustomTilemap customTile = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		for (CustomTilemap i : Dungeon.level.customTiles){
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if (i.image(x - i.tileX, y - i.tileY) != null) {
					x -= i.tileX;
					y -= i.tileY;
					customTile = i;
					break;
				}
			}
		}


		boolean homebaseBuildingDoor = customTile instanceof HomebaseLevel.HomebaseBuildingVisual
				&& ((HomebaseLevel.HomebaseBuildingVisual)customTile).isDoorTile( x, y );

		if (homebaseBuildingDoor
				&& Dungeon.homebase != null
				&& ((HomebaseLevel.HomebaseBuildingVisual)customTile).rebuildTarget() != null) {
			int recoveredMaterials = Dungeon.homebase.depositMaterials( Dungeon.hero );
			if (recoveredMaterials > 0) {
				GLog.p( Messages.get( WndInfoCell.class, "recovered", recoveredMaterials ) );
				saveHomebase();
			}
		}

		String desc = "";

		IconTitle titlebar = new IconTitle();
		titlebar.icon(cellImage(cell));
		titlebar.label(cellName(cell));

		if (customTile != null){
			String customDesc = customTile.desc(x, y);
			if (customDesc != null) {
				desc += customDesc;
			} else {
				desc += Dungeon.level.tileDesc(Dungeon.level.map[cell]);
			}

		} else {

			desc += Dungeon.level.tileDesc(Dungeon.level.map[cell]);
		}
		titlebar.setRect(0, 0, WIDTH, 0);
		add(titlebar);

		RenderedTextBlock info = PixelScene.renderTextBlock(6);
		add(info);

		if (Dungeon.level.heroFOV[cell]) {
			for (Blob blob : Dungeon.level.blobs.values()) {
				if (blob.volume > 0 && blob.cur[cell] > 0 && blob.tileDesc() != null) {
					if (desc.length() > 0) {
						desc += "\n\n";
					}
					desc += blob.tileDesc();
				}
			}
		}
		
		info.text( desc.length() == 0 ? Messages.get(this, "nothing") : desc );
		info.maxWidth(WIDTH);
		info.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		
		float bottom = info.bottom()+2;

		if (homebaseBuildingDoor) {
			resize( WIDTH, (int)bottom );
			return;
		}

		resize( WIDTH, (int)bottom );
	}

	private static void saveHomebase() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}
}
