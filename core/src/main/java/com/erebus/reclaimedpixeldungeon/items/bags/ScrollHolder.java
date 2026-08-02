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

package com.erebus.reclaimedpixeldungeon.items.bags;

import com.erebus.reclaimedpixeldungeon.items.ArcaneResin;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.Stylus;
import com.erebus.reclaimedpixeldungeon.items.scrolls.Scroll;
import com.erebus.reclaimedpixeldungeon.items.spells.BeaconOfReturning;
import com.erebus.reclaimedpixeldungeon.items.spells.Spell;
import com.erebus.reclaimedpixeldungeon.journal.Notes;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class ScrollHolder extends Bag {

	{
		image = ItemSpriteSheet.HOLDER;
	}

	@Override
	public boolean canHold( Item item ) {
		if (item instanceof Scroll || item instanceof Spell
				|| item instanceof ArcaneResin || item instanceof Stylus){
			return super.canHold(item);
		} else {
			return false;
		}
	}

	public int capacity(){
		return capacityWithExpansion( 19 );
	}
	
	@Override
	public void onDetach( ) {
		super.onDetach();
		for (Item item : items) {
			if (item instanceof BeaconOfReturning && ((BeaconOfReturning) item).returnDepth != -1) {
				Notes.remove(Notes.Landmark.BEACON_LOCATION, ((BeaconOfReturning) item).returnDepth);
				((BeaconOfReturning) item).returnDepth = -1;
			}
		}
	}
	
	@Override
	public int value() {
		return 40;
	}

}
