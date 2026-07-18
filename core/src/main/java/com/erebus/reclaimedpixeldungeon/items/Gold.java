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

package com.erebus.reclaimedpixeldungeon.items;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gold extends Item {

	{
		image = ItemSpriteSheet.GOLD;
		stackable = true;
	}
	
	public Gold() {
		this( 1 );
	}
	
	public Gold( int value ) {
		this.quantity = value;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {

		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());

		int gained = quantity;
		if (Dungeon.homebase != null) {
			gained = Math.max( 1, Math.round( gained * (1f + Dungeon.homebase.trainingBonus( HomebaseState.Training.GOLD_GAIN ) / 100f) ) );
		}

		Dungeon.gold += gained;
		Statistics.goldCollected += gained;
		Badges.validateGoldCollected();

		GameScene.pickUp( this, pos );
		hero.sprite.showStatusWithIcon( CharSprite.NEUTRAL, Integer.toString(gained), FloatingText.GOLD );
		hero.spendAndNext( pickupDelay() );
		
		Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		updateQuickslot();
		
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public Item random() {
		quantity = Random.IntRange( 30 + Dungeon.depth * 10, 60 + Dungeon.depth * 20 );
		return this;
	}

}
