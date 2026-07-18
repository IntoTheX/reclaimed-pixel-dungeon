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
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.HomebaseFacilityScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;

import java.io.IOException;
import java.util.ArrayList;

public class WndMoonrootGarden extends Window {

	private static final int WIDTH_DESKTOP = 140;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP = 2;

	public WndMoonrootGarden() {
		super();
		int windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		IconTitle title = new IconTitle( new ItemSprite( ItemSpriteSheet.SEED_STARFLOWER ), Messages.get( this, "title" ) );
		title.setRect( 0, 0, windowWidth, 0 );
		add( title );

		boolean built = Dungeon.homebase != null && Dungeon.homebase.isBuilt( HomebaseState.Building.GARDEN );
		String statusText = built
				? Messages.get( this, "status",
						Dungeon.homebase.buildingLevel( HomebaseState.Building.GARDEN ),
						Dungeon.homebase.moonrootReady(),
						Dungeon.homebase.moonrootPlots() )
				: Messages.get( this, "no_garden" );
		RenderedTextBlock status = PixelScene.renderTextBlock( statusText, 6 );
		status.hardlight( Window.TITLE_COLOR );
		status.maxWidth( windowWidth );
		status.setPos( 0, title.bottom() + GAP );
		add( status );

		RenderedTextBlock desc = PixelScene.renderTextBlock( Messages.get( this, "desc" ), 6 );
		desc.maxWidth( windowWidth );
		desc.setPos( 0, status.bottom() + GAP );
		add( desc );

		RedButton harvest = new RedButton( Messages.get( this, "harvest" ), 6 ) {
			@Override
			protected void onClick() {
				hide();
				harvestGarden();
				show( gardenWindow() );
			}
		};
		harvest.enable( built && Dungeon.homebase.moonrootReady() > 0 );
		add( harvest );
		harvest.setRect( 0, desc.bottom() + 2*GAP, windowWidth, BTN_HEIGHT );

		resize( windowWidth, (int)harvest.bottom() );
		offset( 0, ReclaimedWindow.modalYOffset( (int)harvest.bottom(), chrome.marginVer() ) );
	}

	public static void harvestGarden() {
		if (Dungeon.homebase == null || Dungeon.hero == null) return;

		ArrayList<Item> harvests = Dungeon.homebase.harvestMoonrootGardenSeeds();
		if (harvests.isEmpty()) {
			GLog.w( Messages.get( WndMoonrootGarden.class, "nothing_ready" ) );
			return;
		}

		int seeds = 0;
		for (Item seed : harvests) {
			seeds += Math.max( 1, seed.quantity() );
			if (!seed.collect( Dungeon.hero.belongings.backpack )) {
				Dungeon.level.drop( seed, Dungeon.hero.pos ).sprite.drop();
			}
		}

		Item.updateQuickslot();
		GLog.p( Messages.get( WndMoonrootGarden.class, "harvested", seeds ) );
		save();
	}

	private static void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	private static void show( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	private static WndMoonrootGarden gardenWindow() {
		if (ShatteredPixelDungeon.scene() instanceof HomebaseFacilityScene) {
			return embedded();
		} else {
			return new WndMoonrootGarden();
		}
	}

	public static WndMoonrootGarden embedded() {
		return new WndMoonrootGarden() {
			@Override
			protected boolean blocksInput() {
				return false;
			}

			@Override
			protected boolean handlesBackButton() {
				return false;
			}
		};
	}
}
