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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.ui.Component;

import java.io.IOException;
import java.util.ArrayList;

public class WndDefenderTrades extends Window {

	private static final int WIDTH_DESKTOP = 152;
	private static final int HEIGHT = 170;
	private static final int GAP = 3;
	private static final int DIVIDER_COLOR = 0xFF000000;

	private final int windowWidth;
	private final TradeListContent listContent;
	private final ScrollPane trades;
	private final ArrayList<TradeSection> sections = new ArrayList<>();

	public WndDefenderTrades() {
		windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		RenderedTextBlock title = PixelScene.renderTextBlock( "Defender Trades", 9 );
		title.hardlight( Window.TITLE_COLOR );
		title.maxWidth( windowWidth );
		title.setPos( 0, 0 );
		add( title );

		listContent = new TradeListContent();
		trades = new ScrollPane( listContent );
		add( trades );

		int height = ReclaimedWindow.modalHeight( HEIGHT, chrome.marginVer() );
		resize( windowWidth, height );
		trades.setRect( 0, title.bottom() + GAP, windowWidth, height - title.bottom() - GAP );
		offset( 0, ReclaimedWindow.modalYOffset( height, chrome.marginVer() ) );
		buildTrades( 0 );
	}

	private void buildTrades( float scrollY ) {
		listContent.destroyChildren();
		sections.clear();

		if (Dungeon.homebase == null || Dungeon.homebase.defenders().isEmpty()) {
			RenderedTextBlock none = PixelScene.renderTextBlock( "No defenders have joined the Homebase yet.", 6 );
			none.maxWidth( windowWidth );
			none.setPos( 0, 0 );
			listContent.add( none );
			listContent.setSize( windowWidth, Math.max( trades.height(), none.bottom() + GAP ) );
			return;
		}

		for (HomebaseState.DefenderRecord defender : Dungeon.homebase.defenders()) {
			if (defender == null || !defender.alive()) continue;
			TradeSection section = new TradeSection( defender );
			sections.add( section );
			listContent.add( section.title );
			listContent.add( section.trade );
			listContent.add( section.divider );
		}

		layoutSections();
		trades.scrollTo( 0, scrollY );
	}

	private void layoutSections() {
		float pos = 0;
		for (int i = 0; i < sections.size(); i++) {
			TradeSection section = sections.get( i );
			section.title.setPos( 0, pos );
			pos = section.title.bottom() + GAP;
			section.trade.setTradePos( 0, pos );
			pos = section.trade.bottom() + GAP;
			section.divider.x = 0;
			section.divider.y = pos;
			section.divider.size( windowWidth, 1 );
			section.divider.visible = i < sections.size() - 1;
			if (section.divider.visible) pos += 1 + GAP;
		}
		listContent.setSize( windowWidth, Math.max( trades.height(), pos ) );
		trades.setSize( trades.width(), trades.height() );
	}

	private void buyOffer( HomebaseState.DefenderRecord defender, HomebaseState.DefenderTradeOffer offer ) {
		Item item = defender.buyTradeOfferItem( offer );
		if (item == null) return;
		DefenderUi.deliverPurchasedItem( item );
		GLog.p( "You trade with " + defender.defenderName() + " for " + item.name() + "." );
		save();
		buildTrades( trades.scrollY() );
	}

	private void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	private class TradeSection {
		private final RenderedTextBlock title;
		private final DefenderTradeContent trade;
		private final ColorBlock divider;

		private TradeSection( final HomebaseState.DefenderRecord defender ) {
			title = PixelScene.renderTextBlock( defender.defenderName() + "'s Trade", 7 );
			title.hardlight( Window.TITLE_COLOR );
			title.maxWidth( windowWidth );
			trade = new DefenderTradeContent( defender, windowWidth, 0, new DefenderTradeContent.TradeCallback() {
				@Override
				public void buy( HomebaseState.DefenderTradeOffer offer ) {
					buyOffer( defender, offer );
				}

				@Override
				public void resized() {
					layoutSections();
				}
			} );
			divider = new ColorBlock( windowWidth, 1, DIVIDER_COLOR );
		}
	}

	private static class TradeListContent extends Component {
		private void destroyChildren() {
			if (members == null) return;
			for (Gizmo child : members.toArray( new Gizmo[0] )) {
				if (child != null) child.destroy();
			}
			clear();
		}
	}
}
