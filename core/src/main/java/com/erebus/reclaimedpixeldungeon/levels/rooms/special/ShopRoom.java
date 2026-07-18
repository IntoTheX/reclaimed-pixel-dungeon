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

package com.erebus.reclaimedpixeldungeon.levels.rooms.special;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.erebus.reclaimedpixeldungeon.items.Ankh;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Honeypot;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.Stylus;
import com.erebus.reclaimedpixeldungeon.items.Torch;
import com.erebus.reclaimedpixeldungeon.items.armor.LeatherArmor;
import com.erebus.reclaimedpixeldungeon.items.armor.MailArmor;
import com.erebus.reclaimedpixeldungeon.items.armor.PlateArmor;
import com.erebus.reclaimedpixeldungeon.items.armor.ScaleArmor;
import com.erebus.reclaimedpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.erebus.reclaimedpixeldungeon.items.bags.ArtifactBag;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.bags.FoodBag;
import com.erebus.reclaimedpixeldungeon.items.bags.KeyHolder;
import com.erebus.reclaimedpixeldungeon.items.bags.MagicalHolster;
import com.erebus.reclaimedpixeldungeon.items.bags.MaterialSatchel;
import com.erebus.reclaimedpixeldungeon.items.bags.PotionBandolier;
import com.erebus.reclaimedpixeldungeon.items.bags.ScrollHolder;
import com.erebus.reclaimedpixeldungeon.items.bags.TrinketBag;
import com.erebus.reclaimedpixeldungeon.items.bags.VelvetPouch;
import com.erebus.reclaimedpixeldungeon.items.bombs.Bomb;
import com.erebus.reclaimedpixeldungeon.items.food.SmallRation;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfReturn;
import com.erebus.reclaimedpixeldungeon.items.spells.Alchemize;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfAugmentation;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.erebus.reclaimedpixeldungeon.levels.Level;
import com.erebus.reclaimedpixeldungeon.levels.Terrain;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopRoom extends SpecialRoom {

	protected ArrayList<Item> itemsToSpawn;
	
	@Override
	public int minWidth() {
		return Math.max(7, (int)(Math.sqrt(spacesNeeded())+3));
	}
	
	@Override
	public int minHeight() {
		return Math.max(7, (int)(Math.sqrt(spacesNeeded())+3));
	}

	public int spacesNeeded(){
		if (itemsToSpawn == null) itemsToSpawn = generateItems();

		//sandbags spawn based on current level of an hourglass the player may be holding
		// so, to avoid rare cases of min sizes differing based on that, we ignore all sandbags
		// and then add 4 items in all cases, which is max number of sandbags that can be in the shop
		int spacesNeeded = itemsToSpawn.size();
		for (Item i : itemsToSpawn){
			if (i instanceof TimekeepersHourglass.sandBag){
				spacesNeeded--;
			}
		}
		spacesNeeded += 4;

		//we also add 1 more space, for the shopkeeper
		spacesNeeded++;
		return spacesNeeded;
	}
	
	public void paint( Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );

		placeShopkeeper( level );

		placeItems( level );
		
		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}

	}

	protected void placeShopkeeper( Level level ) {

		int pos = level.pointToCell(center());

		Mob shopkeeper = new Shopkeeper();
		shopkeeper.pos = pos;
		level.mobs.add( shopkeeper );

	}

	protected void placeItems( Level level ){

		if (itemsToSpawn == null){
			itemsToSpawn = generateItems();
		}

		Point entryInset = new Point(entrance());
		if (entryInset.y == top){
			entryInset.y++;
		} else if (entryInset.y == bottom) {
			entryInset.y--;
		} else if (entryInset.x == left){
			entryInset.x++;
		} else {
			entryInset.x--;
		}

		Point curItemPlace = entryInset.clone();

		int inset = 1;

		for (Item item : itemsToSpawn.toArray(new Item[0])) {

			//place items in a clockwise pattern
			if (curItemPlace.x == left+inset && curItemPlace.y != top+inset){
				curItemPlace.y--;
			} else if (curItemPlace.y == top+inset && curItemPlace.x != right-inset){
				curItemPlace.x++;
			} else if (curItemPlace.x == right-inset && curItemPlace.y != bottom-inset){
				curItemPlace.y++;
			} else {
				curItemPlace.x--;
			}

			//once we get to the inset from the entrance again, move another cell inward and loop
			if (curItemPlace.equals(entryInset)){

				if (entryInset.y == top+inset){
					entryInset.y++;
				} else if (entryInset.y == bottom-inset){
					entryInset.y--;
				}
				if (entryInset.x == left+inset){
					entryInset.x++;
				} else if (entryInset.x == right-inset){
					entryInset.x--;
				}
				inset++;

				if (inset > (Math.min(width(), height())-3)/2){
					break; //out of space!
				}

				curItemPlace = entryInset.clone();

				//make sure to step forward again
				if (curItemPlace.x == left+inset && curItemPlace.y != top+inset){
					curItemPlace.y--;
				} else if (curItemPlace.y == top+inset && curItemPlace.x != right-inset){
					curItemPlace.x++;
				} else if (curItemPlace.x == right-inset && curItemPlace.y != bottom-inset){
					curItemPlace.y++;
				} else {
					curItemPlace.x--;
				}
			}

			int cell = level.pointToCell(curItemPlace);
			//prevents high grass from being trampled, potentially dropping dew/seeds onto shop items
			if (level.map[cell] == Terrain.HIGH_GRASS){
				Level.set(cell, Terrain.GRASS, level);
				GameScene.updateMap(cell);
			}
			level.drop( item, cell ).type = Heap.Type.FOR_SALE;
			itemsToSpawn.remove(item);
		}

		//we didn't have enough space to place everything neatly, so now just fill in anything left
		if (!itemsToSpawn.isEmpty()){
			for (Point p : getPoints()){
				int cell = level.pointToCell(p);
				if ((level.map[cell] == Terrain.EMPTY_SP || level.map[cell] == Terrain.EMPTY)
						&& level.heaps.get(cell) == null && level.findMob(cell) == null){
					level.drop( itemsToSpawn.remove(0), level.pointToCell(p) ).type = Heap.Type.FOR_SALE;
				}
				if (itemsToSpawn.isEmpty()){
					break;
				}
			}
		}

		if (!itemsToSpawn.isEmpty()){
			ShatteredPixelDungeon.reportException(new RuntimeException("failed to place all items in a shop!"));
		}

	}
	
	protected static ArrayList<Item> generateItems() {

		ArrayList<Item> itemsToSpawn = new ArrayList<>();

		MeleeWeapon w;
		MissileWeapon m;
		switch (Dungeon.depth) {
		case 6: default:
			w = (MeleeWeapon) Generator.random(Generator.wepTiers[1]);
			m = (MissileWeapon) Generator.random(Generator.misTiers[1]);
			itemsToSpawn.add( new LeatherArmor().identify(false) );
			break;
			
		case 11:
			w = (MeleeWeapon) Generator.random(Generator.wepTiers[2]);
			m = (MissileWeapon) Generator.random(Generator.misTiers[2]);
			itemsToSpawn.add( new MailArmor().identify(false) );
			break;
			
		case 16:
			w = (MeleeWeapon) Generator.random(Generator.wepTiers[3]);
			m = (MissileWeapon) Generator.random(Generator.misTiers[3]);
			itemsToSpawn.add( new ScaleArmor().identify(false) );
			break;

		case 20: case 21:
			w = (MeleeWeapon) Generator.random(Generator.wepTiers[4]);
			m = (MissileWeapon) Generator.random(Generator.misTiers[4]);
			itemsToSpawn.add( new PlateArmor().identify(false) );
			itemsToSpawn.add( new Torch() );
			itemsToSpawn.add( new Torch() );
			itemsToSpawn.add( new Torch() );
			break;
		}
		w.enchant(null);
		w.cursed = false;
		w.level(0);
		w.identify(false);
		itemsToSpawn.add(w);

		m.enchant(null);
		m.cursed = false;
		m.level(0);
		m.identify(false);
		itemsToSpawn.add(m);
		
		itemsToSpawn.add( TippedDart.randomTipped(2) );

		itemsToSpawn.add( new Alchemize().quantity(Random.IntRange(2, 3)));

		Bag bag = ChooseBag(Dungeon.hero.belongings);
		if (bag != null) {
			itemsToSpawn.add(bag);
		} else {
			addBagFallbackStock( itemsToSpawn );
		}

		itemsToSpawn.add( new PotionOfHealing() );
		itemsToSpawn.add( Generator.randomUsingDefaults( Generator.Category.POTION ) );
		itemsToSpawn.add( Generator.randomUsingDefaults( Generator.Category.POTION ) );

		itemsToSpawn.add( new ScrollOfIdentify() );
		itemsToSpawn.add( new ScrollOfRemoveCurse() );
		itemsToSpawn.add( new ScrollOfMagicMapping() );
		itemsToSpawn.add( new ScrollOfReturn() );

		for (int i=0; i < 2; i++)
			itemsToSpawn.add( Random.Int(2) == 0 ?
					Generator.randomUsingDefaults( Generator.Category.POTION ) :
					Generator.randomUsingDefaults( Generator.Category.SCROLL ) );


		itemsToSpawn.add( new SmallRation() );
		itemsToSpawn.add( new SmallRation() );
		
		switch (Random.Int(4)){
			case 0:
				itemsToSpawn.add( new Bomb() );
				break;
			case 1:
			case 2:
				itemsToSpawn.add( new Bomb.DoubleBomb() );
				break;
			case 3:
				itemsToSpawn.add( new Honeypot() );
				break;
		}

		itemsToSpawn.add( new Ankh() );
		itemsToSpawn.add( new StoneOfAugmentation() );

		TimekeepersHourglass hourglass = Dungeon.hero.belongings.getItem(TimekeepersHourglass.class);
		if (hourglass != null && hourglass.isIdentified() && !hourglass.cursed){
			int bags = 0;
			//creates the given float percent of the remaining bags to be dropped.
			//this way players who get the hourglass late can still max it, usually.
			switch (Dungeon.depth) {
				case 6:
					bags = (int)Math.ceil(( 5-hourglass.sandBags) * 0.20f ); break;
				case 11:
					bags = (int)Math.ceil(( 5-hourglass.sandBags) * 0.25f ); break;
				case 16:
					bags = (int)Math.ceil(( 5-hourglass.sandBags) * 0.50f ); break;
				case 20: case 21:
					bags = (int)Math.ceil(( 5-hourglass.sandBags) * 0.80f ); break;
			}

			for(int i = 1; i <= bags; i++){
				itemsToSpawn.add( new TimekeepersHourglass.sandBag());
				hourglass.sandBags ++;
			}
		}

		Item rare;
		switch (Random.Int(10)){
			case 0:
				rare = Generator.random( Generator.Category.WAND );
				rare.level( 0 );
				break;
			case 1:
				rare = Generator.random(Generator.Category.RING);
				rare.level( 0 );
				break;
			case 2:
				rare = Generator.random( Generator.Category.ARTIFACT );
				break;
			default:
				rare = new Stylus();
		}
		rare.cursed = false;
		rare.cursedKnown = true;
		itemsToSpawn.add( rare );

		//use a new generator here to prevent items in shop stock affecting levelgen RNG (e.g. sandbags)
		//we can use a random long for the seed as it will be the same long every time
		Random.pushGenerator(Random.Long());
			Random.shuffle(itemsToSpawn);
		Random.popGenerator();

		return itemsToSpawn;
	}

	protected static Bag ChooseBag(Belongings pack){

		HashMap<Bag, Integer> vanillaBags = new HashMap<>();
		addBagOption( vanillaBags, pack, new VelvetPouch(), Dungeon.LimitedDrops.VELVET_POUCH, 1 );
		addBagOption( vanillaBags, pack, new ScrollHolder(), Dungeon.LimitedDrops.SCROLL_HOLDER, 0 );
		addBagOption( vanillaBags, pack, new PotionBandolier(), Dungeon.LimitedDrops.POTION_BANDOLIER, 0 );
		addBagOption( vanillaBags, pack, new MagicalHolster(), Dungeon.LimitedDrops.MAGICAL_HOLSTER, 0 );

		Bag bestBag = chooseBestBag( vanillaBags, pack );
		if (bestBag == null) {
			HashMap<Bag, Integer> customBags = new HashMap<>();
			addBagOption( customBags, pack, new MaterialSatchel(), null, 2 );
			addBagOption( customBags, pack, new TrinketBag(), null, 1 );
			addBagOption( customBags, pack, new KeyHolder(), null, 1 );
			addBagOption( customBags, pack, new ArtifactBag(), null, 1 );
			addBagOption( customBags, pack, new FoodBag(), null, 1 );
			bestBag = chooseBestBag( customBags, pack );
		}

		if (bestBag == null) return null;

		if (bestBag instanceof VelvetPouch){
			Dungeon.LimitedDrops.VELVET_POUCH.drop();
		} else if (bestBag instanceof ScrollHolder){
			Dungeon.LimitedDrops.SCROLL_HOLDER.drop();
		} else if (bestBag instanceof PotionBandolier){
			Dungeon.LimitedDrops.POTION_BANDOLIER.drop();
		} else if (bestBag instanceof MagicalHolster){
			Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();
		}

		return bestBag;

	}

	private static Bag chooseBestBag( HashMap<Bag, Integer> bags, Belongings pack ) {
		if (bags.isEmpty()) return null;

		for (Item item : pack.backpack.items) {
			for (Bag bag : bags.keySet()){
				if (bag.canHold(item)){
					bags.put(bag, bags.get(bag)+1);
				}
			}
		}

		Bag bestBag = null;
		for (Bag bag : bags.keySet()){
			if (bestBag == null){
				bestBag = bag;
			} else if (bags.get(bag) > bags.get(bestBag)){
				bestBag = bag;
			}
		}
		return bestBag;
	}

	private static void addBagOption( HashMap<Bag, Integer> bags, Belongings pack, Bag bag,
									  Dungeon.LimitedDrops limitedDrop, int score ) {
		if (pack.getItem( bag.getClass() ) != null) return;
		if (limitedDrop != null && limitedDrop.dropped()) return;
		bags.put( bag, score );
	}

	private static void addBagFallbackStock( ArrayList<Item> itemsToSpawn ) {
		itemsToSpawn.add( Generator.randomRarityCatalyst() );
		itemsToSpawn.add( Generator.randomRarityCatalyst() );
		itemsToSpawn.add( BuildingMaterial.randomBundleForDepth( Dungeon.depth, 8, 16 ) );
		itemsToSpawn.add( BuildingMaterial.randomBundleForDepth( Dungeon.depth, 8, 16 ) );
	}

}
