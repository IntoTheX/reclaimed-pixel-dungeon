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
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class StoneOfNullbrand extends RarityCatalystStone {

	{
		image = ItemSpriteSheet.STONE_NULLBRAND;
	}

	@Override
	protected boolean usableOnRarityItem( Item item ) {
		return item.canUpgradeRarityTier();
	}

	@Override
	protected void onItemSelected( Item item ) {
		ItemRarity target = item.nextRarityTier();
		int chance = item.rarityTierUpgradeChance();
		String itemName = item.name();
		if (target == null || chance <= 0) {
			fail( Messages.get( this, "ineligible" ) );
		} else if (item.upgradeRarityTier()) {
			finish( Messages.get( this, "done", itemName, target.displayName() ) );
		} else {
			consumeFailure( Messages.get( this, "failed", itemName, target.displayName(), chance ) );
		}
	}
}
