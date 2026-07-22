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
import com.watabou.utils.Random;

public abstract class BuildingMaterial extends Item {

	public static final float MONSTER_DROP_CHANCE = 0.25f;
	public static final float CHEST_DROP_CHANCE = 0.60f;
	private static final int BASIC_RESOURCE_COUNT = HomebaseState.Material.values().length;

	{
		stackable = true;
		dropsDownHeap = true;
	}

	public abstract HomebaseState.Material material();

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
			Dungeon.homebase.add( material(), quantity() );
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

	public static BuildingMaterial randomForDepth( int depth ) {
		return basicForIndex( randomResourceIndex( depth, false ) );
	}

	public static int depthStackBonus( int depth ) {
		return Math.max( 0, (Math.max( 1, depth ) - 1) / 4 );
	}

	public static BuildingMaterial randomLooseForDepth( int depth ) {
		int bonus = depthStackBonus( depth );
		return randomBundleForDepth( depth, 1 + bonus, 2 + bonus );
	}

	public static BuildingMaterial randomBundleForDepth( int depth, int min, int max ) {
		BuildingMaterial material = randomForDepth( depth );
		material.quantity( Random.NormalIntRange( min, max ) );
		return material;
	}

	public static Item randomLooseResourceForDepth( int depth ) {
		int bonus = depthStackBonus( depth );
		return randomResourceBundleForDepth( depth, 1 + bonus, 2 + bonus );
	}

	public static Item randomResourceBundleForDepth( int depth, int min, int max ) {
		depth = Math.max( 1, depth );
		Item resource = resourceForIndex( randomResourceIndex( depth, true ) );
		resource.quantity( resourceQuantity( resource, depth, min, max ) );
		return resource;
	}

	private static int randomResourceIndex( int depth, boolean includeForgeResources ) {
		depth = Math.max( 1, depth );
		float[] weights = new float[]{
				48f,
				34f,
				18f + Math.min( 10f, depth / 3f ),
				depth >= 6 ? 9f + Math.min( 8f, (depth - 6) / 3f ) : 0f,
				depth >= 11 ? 4.5f + Math.min( 5f, (depth - 11) / 4f ) : 0f,
				includeForgeResources && depth >= 11 ? 2.4f + Math.min( 3f, (depth - 11) / 5f ) : 0f,
				includeForgeResources && depth >= 16 ? 1.1f + Math.min( 1.8f, (depth - 16) / 6f ) : 0f,
				includeForgeResources && depth >= 21 ? 0.35f + Math.min( 0.9f, (depth - 21) / 8f ) : 0f
		};
		return Random.chances( weights );
	}

	private static Item resourceForIndex( int index ) {
		if (index < BASIC_RESOURCE_COUNT) {
			return basicForIndex( index );
		}
		switch (index - BASIC_RESOURCE_COUNT) {
			case 0:
				return new ScrapBundle();
			case 1:
				return new EmberShard();
			case 2:
			default:
				return new EmberCore();
		}
	}

	private static BuildingMaterial basicForIndex( int index ) {
		switch (index) {
			default:
			case 0:
				return new WoodBundle();
			case 1:
				return new StoneBlock();
			case 2:
				return new CopperOre();
			case 3:
				return new IronOre();
			case 4:
				return new GoldOre();
		}
	}

	private static int resourceQuantity( Item item, int depth, int min, int max ) {
		int roll = Random.NormalIntRange( Math.max( 1, min ), Math.max( min, max ) );
		int depthBonus = Math.max( 0, depth / 10 );
		float multiplier = 1f;
		int rank = resourceRank( item );
		switch (rank) {
			case 0:
				multiplier = 1.30f;
				break;
			case 1:
				multiplier = 1.15f;
				break;
			case 2:
				multiplier = 1.00f;
				break;
			case 3:
				multiplier = 0.85f;
				break;
			case 4:
				multiplier = 0.70f;
				break;
			case 5:
				multiplier = 0.60f;
				break;
			case 6:
				multiplier = 0.45f;
				break;
			case 7:
			default:
				multiplier = 0.30f;
				break;
		}
		return Math.max( 1, Math.round( (roll + depthBonus) * multiplier ) );
	}

	private static int resourceRank( Item item ) {
		if (item instanceof BuildingMaterial) {
			return ((BuildingMaterial)item).material().ordinal();
		}
		if (item instanceof ForgeResourceMaterial) {
			return BASIC_RESOURCE_COUNT + ((ForgeResourceMaterial)item).resource().ordinal();
		}
		return 0;
	}
}
