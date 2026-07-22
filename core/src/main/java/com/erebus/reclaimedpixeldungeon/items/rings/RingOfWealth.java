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

package com.erebus.reclaimedpixeldungeon.items.rings;

import com.erebus.reclaimedpixeldungeon.Challenges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.CounterBuff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.effects.Flare;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Gold;
import com.erebus.reclaimedpixeldungeon.items.Honeypot;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.bombs.Bomb;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfExperience;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.UnstableBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.ExoticPotion;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.erebus.reclaimedpixeldungeon.items.spells.UnstableSpell;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfEnchantment;
import com.erebus.reclaimedpixeldungeon.items.trinkets.ExoticCrystals;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class RingOfWealth extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_WEALTH;
		buffClass = Wealth.class;
	}

	private float triesToDrop = Float.MIN_VALUE;
	private int dropsToRare = Integer.MIN_VALUE;
	
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, soloBuffedBonus()) - 1f)));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, combinedBuffedBonus(Dungeon.hero)) - 1f)));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", Messages.decimalFormat("#.##", 20f));
		}
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (Math.pow(1.2f, level+1)-1f)) + "%";
	}

	private static final String TRIES_TO_DROP = "tries_to_drop";
	private static final String DROPS_TO_RARE = "drops_to_rare";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIES_TO_DROP, triesToDrop);
		bundle.put(DROPS_TO_RARE, dropsToRare);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		triesToDrop = bundle.getFloat(TRIES_TO_DROP);
		dropsToRare = bundle.getInt(DROPS_TO_RARE);
	}

	@Override
	protected RingBuff buff( ) {
		return new Wealth();
	}
	
	public static float dropChanceMultiplier( Char target ){
		float multiplier = (float)Math.pow(1.20, getBuffedBonus(target, Wealth.class));
		if (target instanceof Hero) {
			int lootBonus = ((Hero) target).belongings.equippedRarityStat( RarityStat.Type.TREASURE_LUCK )
					+ ((Hero) target).belongings.equippedRarityStat( RarityStat.Type.BONUS_LOOT );
			if (Dungeon.homebase != null) {
				lootBonus += Dungeon.homebase.trainingBonus( HomebaseState.Training.TREASURE_LUCK );
				lootBonus += Dungeon.homebase.trainingBonus( HomebaseState.Training.BONUS_LOOT );
			}
			multiplier *= 1f + lootBonus / 100f;
		}
		return multiplier;
	}
	
	public static ArrayList<Item> tryForBonusDrop(Char target, int tries ){
		int bonus = getBuffedBonus(target, Wealth.class);

		if (bonus <= 0) return null;

		CounterBuff triesToDrop = target.buff(TriesToDropTracker.class);
		if (triesToDrop == null){
			triesToDrop = Buff.affect(target, TriesToDropTracker.class);
			triesToDrop.countUp( Random.NormalIntRange(0, 20) );
		}

		CounterBuff dropsToEquip = target.buff(DropsToEquipTracker.class);
		if (dropsToEquip == null){
			dropsToEquip = Buff.affect(target, DropsToEquipTracker.class);
			dropsToEquip.countUp( Random.NormalIntRange(5, 10) );
		}

		//now handle reward logic
		ArrayList<Item> drops = new ArrayList<>();

		triesToDrop.countDown(tries);
		while ( triesToDrop.count() <= 0 ){
			if (dropsToEquip.count() <= 0){
				int equipBonus = 0;

				//A second ring of wealth can be at most +1 when calculating wealth bonus for equips
				//This is to prevent using an upgraded wealth to farm another upgraded wealth and
				//using the two to get substantially more upgrade value than intended
				for (Wealth w : target.buffs(Wealth.class)){
					if (w.buffedLvl() > equipBonus){
						equipBonus = w.buffedLvl() + Math.min(equipBonus, 2);
					} else {
						equipBonus += Math.min(w.buffedLvl(), 2);
					}
				}

				Item i;
				do {
					i = genEquipmentDrop(equipBonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip.countUp(Random.NormalIntRange(5, 10));
			} else {
				Item i;
				do {
					i = genConsumableDrop(bonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip.countDown(1);
			}
			triesToDrop.countUp( Random.NormalIntRange(0, 20) );
		}
		
		return drops;
	}

	//used for visuals
	// 1/2/3 used for low/mid/high tier consumables
	// 3 used for +0-1 equips, 4 used for +2 or higher equips
	private static int latestDropTier = 0;

	public static void showFlareForBonusDrop( Visual vis ){
		if (vis == null || vis.parent == null) return;
		switch (latestDropTier){
			default:
				break; //do nothing
			case 1:
				new Flare(6, 20).color(0x00FF00, true).show(vis, 3f);
				break;
			case 2:
				new Flare(6, 24).color(0x00AAFF, true).show(vis, 3.33f);
				break;
			case 3:
				new Flare(6, 28).color(0xAA00FF, true).show(vis, 3.67f);
				break;
			case 4:
				new Flare(6, 32).color(0xFFAA00, true).show(vis, 4f);
				break;
		}
		latestDropTier = 0;
	}
	
	public static Item genConsumableDrop(int level) {
		float roll = Random.Float();
		//60% chance - 4% per level. Starting from +15: 0%
		if (roll < (0.6f - 0.04f * level)) {
			latestDropTier = 1;
			return genLowValueConsumable();
		//30% chance + 2% per level. Starting from +15: 60%-2%*(lvl-15)
		} else if (roll < (0.9f - 0.02f * level)) {
			latestDropTier = 2;
			return genMidValueConsumable();
		//10% chance + 2% per level. Starting from +15: 40%+2%*(lvl-15)
		} else {
			latestDropTier = 3;
			return genHighValueConsumable();
		}
	}

	private static Item genLowValueConsumable(){
		switch (Random.Int(5)){
			case 0: default:
				Item i = new Gold().random();
				return i.quantity(i.quantity()/2);
			case 1:
				return Generator.randomUsingDefaults(Generator.Category.STONE);
			case 2:
				return Generator.randomUsingDefaults(Generator.Category.POTION);
			case 3:
				return Generator.randomUsingDefaults(Generator.Category.SCROLL);
			case 4:
				return BuildingMaterial.randomResourceBundleForDepth( Dungeon.depth, 1, 2 + BuildingMaterial.depthStackBonus( Dungeon.depth ) );
		}
	}

	private static Item genMidValueConsumable(){
		switch (Random.Int(8)){
			case 0: default:
				Item i = genLowValueConsumable();
				return i.quantity(i.quantity()*2);
			case 1:
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
				if (!(i instanceof ExoticPotion)) {
					return Reflection.newInstance(ExoticPotion.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 2:
				i = Generator.randomUsingDefaults(Generator.Category.SCROLL);
				if (!(i instanceof ExoticScroll)){
					return Reflection.newInstance(ExoticScroll.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 3:
				return Random.Int(2) == 0 ? new UnstableBrew() : new UnstableSpell();
			case 4:
				return new Bomb();
			case 5:
				return new Honeypot();
			case 6:
				return BuildingMaterial.randomResourceBundleForDepth( Dungeon.depth, 2, 4 + BuildingMaterial.depthStackBonus( Dungeon.depth ) );
			case 7:
				return Generator.randomRarityCatalyst();
		}
	}

	private static Item genHighValueConsumable(){
		switch (Random.Int(6)){
			case 0: default:
				Item i = genMidValueConsumable();
				if (i instanceof Bomb){
					return new Bomb.DoubleBomb();
				} else {
					return i.quantity(i.quantity()*2);
				}
			case 1:
				return new StoneOfEnchantment();
			case 2:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new PotionOfDivineInspiration() : new PotionOfExperience();
			case 3:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new ScrollOfMetamorphosis() : new ScrollOfTransmutation();
			case 4:
				return BuildingMaterial.randomResourceBundleForDepth( Dungeon.depth, 4, 7 + BuildingMaterial.depthStackBonus( Dungeon.depth ) );
			case 5:
				Item catalyst = Generator.randomRarityCatalyst();
				return Random.Int(4) == 0 ? catalyst.quantity( 2 ) : catalyst;
		}
	}

	private static Item genEquipmentDrop( int level ){
		Item result;
		//each upgrade increases depth used for calculating drops by 1
		int floorset = (Dungeon.depth + level)/5;
		switch (Random.Int(5)){
			default: case 0: case 1:
				Weapon w = Generator.randomWeapon(floorset, true);
				if (!w.hasGoodEnchant() && Random.Int(10) < level)      w.enchant();
				else if (w.hasCurseEnchant())                           w.enchant(null);
				result = w;
				break;
			case 2:
				Armor a = Generator.randomArmor(floorset);
				if (!a.hasGoodGlyph() && Random.Int(10) < level)        a.inscribe();
				else if (a.hasCurseGlyph())                             a.inscribe(null);
				result = a;
				break;
			case 3:
				result = Generator.randomUsingDefaults(Generator.Category.RING);
				break;
			case 4:
				result = Generator.random(Generator.Category.ARTIFACT);
				break;
		}
		//minimum level is 1/2/3/4/5/6 when ring level is 1/3/5/7/9/11
		if (result.isUpgradable()){
			int minLevel = (level+1)/2;
			if (result.level() < minLevel){
				result.level(minLevel);
			}
		}
		result.cursed = false;
		result.cursedKnown = true;
		if (result.level() >= 2) {
			latestDropTier = 4;
		} else {
			latestDropTier = 3;
		}
		return result;
	}

	public class Wealth extends RingBuff {
	}

	public static class TriesToDropTracker extends CounterBuff {
		{
			revivePersists = true;
		}
	}

	public static class DropsToEquipTracker extends CounterBuff {
		{
			revivePersists = true;
		}
	}
}
