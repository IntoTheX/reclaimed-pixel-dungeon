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

import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;

import java.util.ArrayList;

public abstract class RarityCatalystStone extends InventoryCatalystStone {

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

	protected void finish( String message ) {
		consume( message, true );
	}

	protected void consumeFailure( String message ) {
		consume( message, false );
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
				}
			};
			cancel.setRect( MARGIN, pos + MARGIN, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( cancel );

			resize( WIDTH, (int)cancel.bottom() + MARGIN );
		}
	}
}
