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

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Halo;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndDefenderScoutingRewards extends Window {

	private static final int WIDTH_DESKTOP = 152;
	private static final int HEIGHT = 130;
	private static final int GAP = 3;
	private static final int SPRITE_COLUMN = 18;
	private static final int DIVIDER_COLOR = 0xFF000000;

	public WndDefenderScoutingRewards( ArrayList<HomebaseState.DefenderScoutingReport> reports ) {
		super();
		int width = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );
		int height = ReclaimedWindow.modalHeight( HEIGHT, chrome.marginVer() );

		RenderedTextBlock title = PixelScene.renderTextBlock( "Defender Returns", 9 );
		title.hardlight( Window.TITLE_COLOR );
		title.maxWidth( width );
		title.setPos( 0, 0 );
		add( title );

		ScrollPane list = new ScrollPane( new Component() );
		add( list );
		resize( width, height );
		list.setRect( 0, title.bottom() + GAP, width, height - title.bottom() - GAP );
		offset( 0, ReclaimedWindow.modalYOffset( height, chrome.marginVer() ) );

		Component content = list.content();
		float pos = 0;
		for (HomebaseState.DefenderScoutingReport report : reports) {
			if (report == null || !report.hasRewards()) continue;
			if (pos > 0) {
				ColorBlock divider = new ColorBlock( width, 1, DIVIDER_COLOR );
				divider.x = 0;
				divider.y = pos;
				content.add( divider );
				pos += GAP;
			}

			HomebaseDefenderSprite sprite = new HomebaseDefenderSprite( heroClass( report.archetype ), report.armorTier, report.rarity );
			Halo halo = new Halo( 10f, report.rarity.color(), Math.min( 0.22f, Math.max( 0.10f, report.rarity.auraAlpha() * 0.55f ) ) );
			content.add( halo );
			content.add( sprite );
			sprite.x = 1;
			sprite.y = pos + 1;
			PixelScene.align( sprite );
			halo.point( sprite.x + sprite.width() / 2f, sprite.y + sprite.height() / 2f + 1 );

			String intro = report.totalDonated() > 0
					? colorText( report.rarity.color(), report.defenderName ) + " has given you:"
					: colorText( report.rarity.color(), report.defenderName ) + " returns from a recent dungeon run.";
			RenderedTextBlock text = PixelScene.renderTextBlock( intro, 6 );
			text.maxWidth( width - SPRITE_COLUMN );
			text.setPos( SPRITE_COLUMN, pos );
			content.add( text );

			float nextY = text.bottom() + 1;
			if (report.totalDonated() > 0) {
				WndCurrencyLine rewards = new WndCurrencyLine( "" );
				for (HomebaseState.Material material : HomebaseState.Material.values()) {
					rewards.addMaterial( material, report.materials[material.ordinal()] );
				}
				for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
					rewards.addForge( resource, report.forgeResources[resource.ordinal()] );
				}
				rewards.setRect( SPRITE_COLUMN, nextY, width - SPRITE_COLUMN, 10 );
				content.add( rewards );
				nextY = rewards.bottom() + 1;

				RenderedTextBlock outro = PixelScene.renderTextBlock( "from their recent dungeon run.", 6 );
				outro.maxWidth( width - SPRITE_COLUMN );
				outro.setPos( SPRITE_COLUMN, nextY );
				content.add( outro );
				nextY = outro.bottom() + 1;
			}

			if (report.xpGained > 0) {
				RenderedTextBlock xp = PixelScene.renderTextBlock(
						colorText( 0x44CCFF, "Gained " + report.xpGained + " XP" ), 6 );
				xp.maxWidth( width - SPRITE_COLUMN );
				xp.setPos( SPRITE_COLUMN, nextY );
				content.add( xp );
				nextY = xp.bottom() + 1;
			}
			if (report.leveledUp()) {
				RenderedTextBlock level = PixelScene.renderTextBlock(
						colorText( Window.TITLE_COLOR, "Leveled Up from " + report.levelBefore + " to " + report.levelAfter ), 6 );
				level.maxWidth( width - SPRITE_COLUMN );
				level.setPos( SPRITE_COLUMN, nextY );
				content.add( level );
				nextY = level.bottom() + 1;
			}

			pos = Math.max( nextY, sprite.y + sprite.height() ) + GAP;
		}
		content.setSize( width, Math.max( list.height(), pos ) );
	}

	private static HeroClass heroClass( int archetype ) {
		switch (archetype) {
			case HomebaseState.DefenderRecord.MAGE:
				return HeroClass.MAGE;
			case HomebaseState.DefenderRecord.ROGUE:
				return HeroClass.ROGUE;
			case HomebaseState.DefenderRecord.HUNTRESS:
				return HeroClass.HUNTRESS;
			case HomebaseState.DefenderRecord.DUELIST:
				return HeroClass.DUELIST;
			case HomebaseState.DefenderRecord.PRIEST:
				return HeroClass.CLERIC;
			case HomebaseState.DefenderRecord.WARRIOR:
			default:
				return HeroClass.WARRIOR;
		}
	}

	static String colorText( int color, String text ) {
		return "@@C" + String.format( "%06X", color & 0xFFFFFF ) + "@@" + text + "@@CEND@@";
	}
}
