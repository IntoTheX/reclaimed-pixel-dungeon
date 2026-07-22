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
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;

import java.io.IOException;

public class WndDefenderTrade extends Window {

	private static final int WIDTH_DESKTOP = 152;
	private static final int HEIGHT = 170;
	private static final int GAP = 3;

	private final HomebaseState.DefenderRecord defender;
	private final int width;
	private final float titleGap;
	private ScrollPane offers;

	public WndDefenderTrade( HomebaseState.DefenderRecord defender ) {
		super();
		this.defender = defender;
		width = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );
		titleGap = PixelScene.landscape() ? GAP : GAP * 4;

		RenderedTextBlock title = PixelScene.renderTextBlock( defender.defenderName() + "'s Trade", 9 );
		title.hardlight( Window.TITLE_COLOR );
		title.maxWidth( width );
		title.setPos( 0, 0 );
		add( title );

		offers = new ScrollPane( new Component() );
		add( offers );

		int height = ReclaimedWindow.modalHeight( HEIGHT, chrome.marginVer() );
		resize( width, height );
		offers.setRect( 0, title.bottom() + titleGap, width, height - title.bottom() - titleGap );
		offset( 0, ReclaimedWindow.modalYOffset( height, chrome.marginVer() ) );
		buildOffers();
	}

	private void buildOffers() {
		DefenderTradeContent content = new DefenderTradeContent( defender, width, (int)offers.height(), new DefenderTradeContent.TradeCallback() {
			@Override
			public void buy( HomebaseState.DefenderTradeOffer offer ) {
				buyOffer( offer );
			}
		} );
		offers.content().clear();
		offers.content().add( content );
		content.setPos( 0, 0 );
		offers.content().setSize( width, Math.max( offers.height(), content.height() ) );
	}

	private void buyOffer( HomebaseState.DefenderTradeOffer offer ) {
		final Item item = offer == null ? null : offer.item();
		if (item == null || !defender.buyTradeOffer( offer )) return;
		if (!item.collect( Dungeon.hero.belongings.backpack )) {
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
		GLog.p( "You trade with " + defender.defenderName() + " for " + item.name() + "." );
		save();
		buildOffers();
	}

	private static void showWindow( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	private void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}
}
