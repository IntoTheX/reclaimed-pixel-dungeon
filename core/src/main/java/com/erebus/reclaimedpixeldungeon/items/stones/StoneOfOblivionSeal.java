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

import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class StoneOfOblivionSeal extends RarityCatalystStone {

	{
		image = ItemSpriteSheet.STONE_OBLIVION_SEAL;
	}

	@Override
	protected boolean usableOnRarityItem( Item item ) {
		return !item.rarityStatIndexes( false, false, false ).isEmpty();
	}

	@Override
	protected void onItemSelected( final Item item ) {
		chooseStat( item, item.rarityStatIndexes( false, false, false ), Messages.get( this, "choose" ), new StatChoiceAction() {
			@Override
			public void select( Item item, int index ) {
				RarityStat locked = item.lockRarityStatResult( index );
				if (locked != null) {
					finish( Messages.get( StoneOfOblivionSeal.this, "done", item.name() ) + " Locked " + statName( locked ) + "." );
				} else {
					fail( Messages.get( StoneOfOblivionSeal.this, "failed" ) );
				}
			}
		} );
	}
}
