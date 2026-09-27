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

package com.erebus.reclaimedpixeldungeon.items.stones;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.util.ArrayList;
import java.util.Locale;

public abstract class RarityCatalystStone extends InventoryCatalystStone {

	private Item applicationTarget;

	{
		preferredBag = Belongings.Backpack.class;
	}

	@Override
	protected boolean usableOnItem( Item item ) {
		return item.canUseRarityCatalyst() && !item.isTranscendantRarity() && usableOnRarityItem( item );
	}

	protected boolean usableOnRarityItem( Item item ) {
		return true;
	}

	protected boolean rerollsRarityItem() {
		return false;
	}

	@Override
	protected void selectItem( final Item item ) {
		applicationTarget = item;
		if (!rerollsRarityItem() || item.rarity().power() < ItemRarity.EPIC.power()) {
			showApplicationWindow( item );
			return;
		}

		GameScene.show( new WndOptions( new ItemSprite( item ), Messages.titleCase( item.name() ),
				Messages.get( RarityCatalystStone.class, "reroll_warning", item.rarity().displayName() ),
				Messages.get( RarityCatalystStone.class, "reroll_confirm" ),
				Messages.get( RarityCatalystStone.class, "cancel" ) ) {
			@Override
			protected void onSelect( int index ) {
				if (index == 0) showApplicationWindow( item );
			}
		} );
	}

	protected String applicationPreview( Item item ) {
		return Messages.get( RarityCatalystStone.class, "preview" );
	}

	protected ArrayList<String> previewLeftRows( Item item ) {
		return new ArrayList<>();
	}

	protected ArrayList<String> previewRightRows( Item item ) {
		return new ArrayList<>();
	}

	protected ArrayList<String> currentStatRows( Item item ) {
		ArrayList<String> rows = new ArrayList<>();
		if (item == null) return rows;
		for (RarityStat stat : item.rarityStatsSnapshot()) {
			if (!stat.isEmptySlot()) rows.add( stat.compactDisplayText() );
		}
		return rows;
	}

	protected ArrayList<String> unknownRows( int count ) {
		ArrayList<String> rows = new ArrayList<>();
		for (int i = 0; i < count; i++) rows.add( "@@CFFFF44@@?@@CEND@@" );
		return rows;
	}

	protected String rarityChanceTable() {
		StringBuilder table = new StringBuilder();
		for (ItemRarity rarity : ItemRarity.values()) {
			float chance = Item.rarityRollChance( rarity );
			if (table.length() > 0) table.append( "\n" );
			table.append( rarity.coloredName() ).append( ": @@CFFFF44@@" );
			if (chance == Math.round( chance )) {
				table.append( Math.round( chance ) );
			} else {
				table.append( String.format( Locale.US, "%.1f", chance ) );
			}
			table.append( "%@@CEND@@" );
		}
		return table.toString();
	}

	protected String valueRange( RarityStat stat, ItemRarity rarity ) {
		if (stat == null || stat.isEmptySlot()) return "@@CFFFF44@@?@@CEND@@";
		if (!stat.type().hasValue()) return stat.coloredDisplayName( stat.type().compactDisplayName() );
		int[] range = Item.rarityStatValueRange( stat.type(), rarity );
		String suffix = stat.type().percent() ? "%" : "";
		return stat.coloredDisplayName( stat.type().compactDisplayName() ) + " "
				+ range[0] + suffix + "-" + range[1] + suffix;
	}

	protected String resultSummary( ItemRarity oldRarity, ArrayList<RarityStat> oldStats,
			Item item, boolean includeCount ) {
		StringBuilder result = new StringBuilder();
		result.append( "\nRarity: " ).append( oldRarity.coloredName() )
				.append( " -> " ).append( item.rarity().coloredName() );

		ArrayList<RarityStat> newStats = item.rarityStatsSnapshot();
		int oldCount = nonEmptyCount( oldStats );
		int newCount = nonEmptyCount( newStats );
		if (includeCount) {
			String color = newCount < oldCount ? "FF5555" : newCount > oldCount ? "55CC55" : "FFAA33";
			result.append( "\nNumber of stats: " ).append( oldCount ).append( " -> @@C" )
					.append( color ).append( "@@" ).append( newCount ).append( "@@CEND@@" );
		}

		int rows = Math.max( oldStats.size(), newStats.size() );
		for (int i = 0; i < rows; i++) {
			String before = i < oldStats.size() ? oldStats.get( i ).displayText() : "@@C888888@@None@@CEND@@";
			String after = i < newStats.size() ? newStats.get( i ).displayText() : "@@C888888@@None@@CEND@@";
			result.append( "\n" ).append( before ).append( " -> " ).append( after );
		}
		return result.toString();
	}

	private int nonEmptyCount( ArrayList<RarityStat> stats ) {
		int count = 0;
		if (stats != null) for (RarityStat stat : stats) if (stat != null && !stat.isEmptySlot()) count++;
		return count;
	}

	private void showApplicationWindow( Item item ) {
		if (item == null || !usableOnItem( item )) return;
		applicationTarget = item;
		GameScene.show( new WndCatalystApplication( item ) );
	}

	protected void finish( String message ) {
		consume( message, true );
		reopenApplicationWindow();
	}

	protected void consumeFailure( String message ) {
		consume( message, false );
		reopenApplicationWindow();
	}

	private void consume( String message, boolean success ) {
		if (!anonymous) {
			curItem.detach( curUser.belongings.backpack );
			Catalog.countUse( getClass() );
			Talent.onRunestoneUsed( curUser, curUser.pos, getClass() );
		}
		useAnimation();
		if (success) {
			GLog.p( message );
		} else {
			GLog.w( message );
		}
	}

	protected void fail( String message ) {
		GLog.w( message );
		reopenApplicationWindow();
	}

	private void reopenApplicationWindow() {
		final Item target = applicationTarget;
		if (target == null || Dungeon.hero == null
				|| !Dungeon.hero.belongings.contains( this )
				|| !Dungeon.hero.belongings.contains( target )
				|| !usableOnItem( target )) {
			return;
		}
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				showApplicationWindow( target );
			}
		} );
	}

	protected String rarityTransition( Item.RarityTierChange change ) {
		if (change == null) return "";
		return change.oldRarity.coloredName() + " to " + change.newRarity.coloredName();
	}

	protected String statName( RarityStat stat ) {
		if (stat == null || stat.isEmptySlot()) return "@@C888888@@Empty Slot@@CEND@@";
		return stat.coloredDisplayName();
	}

	protected String statNameChange( Item.RarityStatChange change ) {
		if (change == null) return "";
		return statName( change.oldStat ) + " to " + statName( change.newStat );
	}

	protected String statValueChange( Item.RarityStatChange change ) {
		if (change == null) return "";
		return statName( change.newStat ) + " " + change.oldStat.valueText() + " to " + change.newStat.valueText();
	}

	protected String statChangeList( ArrayList<Item.RarityStatChange> changes ) {
		if (changes == null || changes.isEmpty()) return "";

		StringBuilder builder = new StringBuilder();
		for (Item.RarityStatChange change : changes) {
			builder.append( "\n- " ).append( statNameChange( change ) );
		}
		return builder.toString();
	}

	protected void chooseStat( Item item, ArrayList<Integer> indexes, String prompt, StatChoiceAction action ) {
		if (indexes.isEmpty()) {
			fail( Messages.get( RarityCatalystStone.class, "no_stats" ) );
			return;
		}
		GameScene.show( new WndRarityStatChoice( item, indexes, prompt, action ) );
	}

	@Override
	public int value() {
		return 45 * quantity;
	}

	@Override
	public int energyVal() {
		return 8 * quantity;
	}

	protected interface StatChoiceAction {
		void select( Item item, int index );
	}

	private class WndRarityStatChoice extends Window {

		private static final int WIDTH = 136;
		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 18;

		WndRarityStatChoice( final Item item, ArrayList<Integer> indexes, String prompt, final StatChoiceAction action ) {
			super();

			IconTitle titlebar = new IconTitle( item );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			RenderedTextBlock message = PixelScene.renderTextBlock( prompt, 6 );
			message.maxWidth( WIDTH - MARGIN * 2 );
			message.setPos( MARGIN, titlebar.bottom() + MARGIN );
			add( message );

			float pos = message.bottom();
			for (final Integer index : indexes) {
				RedButton button = new RedButton( item.rarityStatChoiceText( index ) ) {
					@Override
					protected void onClick() {
						hide();
						action.select( item, index );
					}
				};
				button.setRect( MARGIN, pos + MARGIN, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
				add( button );
				pos = button.bottom();
			}

			RedButton cancel = new RedButton( Messages.get( RarityCatalystStone.class, "cancel" ) ) {
				@Override
				protected void onClick() {
					hide();
					reopenApplicationWindow();
				}
			};
			cancel.setRect( MARGIN, pos + MARGIN, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( cancel );

			resize( WIDTH, (int)cancel.bottom() + MARGIN );
		}
	}

	private class WndCatalystApplication extends Window {

		private static final int MARGIN = 2;
		private static final int BUTTON_HEIGHT = 18;

		private final RedButton apply;

		WndCatalystApplication( final Item item ) {
			final int width = PixelScene.landscape() ? 160 : 136;
			IconTitle catalystTitle = new IconTitle( new ItemSprite( RarityCatalystStone.this ),
					Messages.get( RarityCatalystStone.class, "apply_title" ) );
			catalystTitle.setRect( 0, 0, width, 0 );
			add( catalystTitle );

			IconTitle targetTitle = new IconTitle( item );
			targetTitle.setRect( 0, catalystTitle.bottom() + MARGIN, width, 0 );
			add( targetTitle );

			String preview = Messages.get( RarityCatalystStone.class, "target",
					item.rarity().coloredName() ) + "\n" + applicationPreview( item );
			RenderedTextBlock message = PixelScene.renderTextBlock( preview, 6 );
			message.maxWidth( width - MARGIN * 2 );
			message.setPos( MARGIN, targetTitle.bottom() + MARGIN );
			add( message );

			float pos = message.bottom() + MARGIN;
			ArrayList<String> leftRows = previewLeftRows( item );
			ArrayList<String> rightRows = previewRightRows( item );
			int rowCount = Math.max( leftRows.size(), rightRows.size() );
			if (rowCount > 0) {
				int columnWidth = (width - MARGIN * 3) / 2;
				RenderedTextBlock current = PixelScene.renderTextBlock( "_Current_", 6 );
				current.maxWidth( columnWidth );
				current.setPos( MARGIN, pos );
				add( current );
				RenderedTextBlock result = PixelScene.renderTextBlock( "_Result_", 6 );
				result.maxWidth( columnWidth );
				result.setPos( MARGIN * 2 + columnWidth, pos );
				add( result );
				pos = Math.max( current.bottom(), result.bottom() ) + 1;

				float dividerTop = pos;
				for (int i = 0; i < rowCount; i++) {
					RenderedTextBlock left = PixelScene.renderTextBlock( i < leftRows.size() ? leftRows.get( i ) : "", 5 );
					left.maxWidth( columnWidth );
					left.setPos( MARGIN, pos );
					add( left );

					RenderedTextBlock right = PixelScene.renderTextBlock( i < rightRows.size() ? rightRows.get( i ) : "", 5 );
					right.maxWidth( columnWidth );
					right.setPos( MARGIN * 2 + columnWidth, pos );
					add( right );
					pos = Math.max( left.bottom(), right.bottom() ) + 2;
				}
				ColorBlock divider = new ColorBlock( 1, Math.max( 1, pos - dividerTop - 1 ), 0xFF777777 );
				divider.x = MARGIN + columnWidth;
				divider.y = dividerTop;
				add( divider );
			}

			if (quantity() > 1) {
				RenderedTextBlock remaining = PixelScene.renderTextBlock(
						Messages.get( RarityCatalystStone.class, "remaining", quantity() ), 6 );
				remaining.maxWidth( width - MARGIN * 2 );
				remaining.setPos( MARGIN, pos + MARGIN );
				add( remaining );
				pos = remaining.bottom() + MARGIN;
			}

			apply = new RedButton( Messages.get( RarityCatalystStone.class, "apply" ) ) {
				@Override
				protected void onClick() {
					hide();
					if (usableOnItem( item )) onItemSelected( item );
				}
			};
			apply.icon( new ItemSprite( RarityCatalystStone.this ) );
			apply.setRect( 0, pos, width / 2f, BUTTON_HEIGHT );
			apply.enable( Dungeon.hero != null && Dungeon.hero.ready );
			add( apply );

			RedButton another = new RedButton( Messages.get( RarityCatalystStone.class, "choose_another" ) ) {
				@Override
				protected void onClick() {
					hide();
					directActivate();
				}
			};
			another.setRect( apply.right() + 1, apply.top(), width / 2f - 1, BUTTON_HEIGHT );
			add( another );

			resize( width, (int)another.bottom() );
		}

		@Override
		public synchronized void update() {
			super.update();
			if (!apply.active && Dungeon.hero != null && Dungeon.hero.ready) apply.enable( true );
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
			directActivate();
		}
	}
}
