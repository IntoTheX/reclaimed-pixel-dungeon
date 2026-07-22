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

package com.erebus.reclaimedpixeldungeon.items.materials;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

public abstract class ForgeResourceMaterial extends Item {

	{
		stackable = true;
		dropsDownHeap = true;
	}

	public abstract HomebaseState.ForgeResource resource();

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public Item randomizeRarityStats() {
		return this;
	}

	@Override
	public int value() {
		return quantity();
	}

	@Override
	public int energyVal() {
		return 0;
	}

	@Override
	public boolean doPickUp( Hero hero, int pos ) {
		if (Dungeon.homebase != null && Dungeon.depth == 0) {
			Dungeon.homebase.addForgeResource( resource(), quantity() );
			GameScene.pickUp( this, pos );
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext( pickupDelay() );
			updateQuickslot();
			ReclaimedTutorial.flash( Document.GUIDE_MATERIALS );
			return true;
		}
		boolean pickedUp = super.doPickUp( hero, pos );
		if (pickedUp) {
			ReclaimedTutorial.flash( Document.GUIDE_MATERIALS );
		}
		return pickedUp;
	}
}
