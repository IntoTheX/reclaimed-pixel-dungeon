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

package com.erebus.reclaimedpixeldungeon.actors.hero;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Challenges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HeroClassUnlocks;
import com.erebus.reclaimedpixeldungeon.QuickSlot;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.cleric.AscendedForm;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.huntress.SpectralBlades;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.erebus.reclaimedpixeldungeon.items.BrokenSeal;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.Waterskin;
import com.erebus.reclaimedpixeldungeon.items.armor.ClothArmor;
import com.erebus.reclaimedpixeldungeon.items.artifacts.CloakOfShadows;
import com.erebus.reclaimedpixeldungeon.items.artifacts.HolyTome;
import com.erebus.reclaimedpixeldungeon.items.bags.VelvetPouch;
import com.erebus.reclaimedpixeldungeon.items.food.Food;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfInvisibility;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfMindVision;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfPurity;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfStrength;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRage;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfMagicMissile;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.Cudgel;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.Dagger;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.Gloves;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MagesStaff;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.Rapier;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.WornShortsword;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;

public enum HeroClass {

	WARRIOR( HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR ),
	MAGE( HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK ),
	ROGUE( HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER ),
	HUNTRESS( HeroSubClass.SNIPER, HeroSubClass.WARDEN ),
	DUELIST( HeroSubClass.CHAMPION, HeroSubClass.MONK ),
	CLERIC( HeroSubClass.PRIEST, HeroSubClass.PALADIN );

	private HeroSubClass[] subClasses;

	HeroClass( HeroSubClass...subClasses ) {
		this.subClasses = subClasses;
	}

	public void initHero( Hero hero ) {

		hero.heroClass = this;
		Talent.initClassTalents(hero);

		Item i = new ClothArmor().identify();
		if (!Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		i = new Food();
		if (!Challenges.isItemBlocked(i)) i.collect();

		new VelvetPouch().collect();
		Dungeon.LimitedDrops.VELVET_POUCH.drop();

		Waterskin waterskin = new Waterskin();
		waterskin.collect();

		new ScrollOfIdentify().identify();

		switch (this) {
			case WARRIOR:
				initWarrior( hero );
				break;

			case MAGE:
				initMage( hero );
				break;

			case ROGUE:
				initRogue( hero );
				break;

			case HUNTRESS:
				initHuntress( hero );
				break;

			case DUELIST:
				initDuelist( hero );
				break;

			case CLERIC:
				initCleric( hero );
				break;
		}

		if (SPDSettings.quickslotWaterskin()) {
			assignInitialQuickslot( waterskin, -1 );
		}

	}

	public Badges.Badge masteryBadge() {
		switch (this) {
			case WARRIOR:
				return Badges.Badge.MASTERY_WARRIOR;
			case MAGE:
				return Badges.Badge.MASTERY_MAGE;
			case ROGUE:
				return Badges.Badge.MASTERY_ROGUE;
			case HUNTRESS:
				return Badges.Badge.MASTERY_HUNTRESS;
			case DUELIST:
				return Badges.Badge.MASTERY_DUELIST;
			case CLERIC:
				return Badges.Badge.MASTERY_CLERIC;
		}
		return null;
	}

	private static void initWarrior( Hero hero ) {
		(hero.belongings.weapon = new WornShortsword()).identify();
		ThrowingStone stones = new ThrowingStone();
		stones.identify().collect();

		assignInitialQuickslot( stones, 0 );

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
			Catalog.setSeen(BrokenSeal.class); //as it's not added to the inventory
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}

	private static void initMage( Hero hero ) {
		MagesStaff staff;

		staff = new MagesStaff(new WandOfMagicMissile());

		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		assignInitialQuickslot( staff, 0 );

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}

	private static void initRogue( Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();

		CloakOfShadows cloak = new CloakOfShadows();
		(hero.belongings.artifact = cloak).identify();
		hero.belongings.artifact.activate( hero );

		ThrowingKnife knives = new ThrowingKnife();
		knives.identify().collect();

		assignInitialQuickslot( cloak, 0 );
		assignInitialQuickslot( knives, 1 );

		new ScrollOfMagicMapping().identify();
		new PotionOfInvisibility().identify();
	}

	private static void initHuntress( Hero hero ) {

		(hero.belongings.weapon = new Gloves()).identify();
		SpiritBow bow = new SpiritBow();
		bow.identify().collect();

		assignInitialQuickslot( bow, 0 );

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}

	private static void initDuelist( Hero hero ) {

		(hero.belongings.weapon = new Rapier()).identify();
		hero.belongings.weapon.activate(hero);

		ThrowingSpike spikes = new ThrowingSpike();
		spikes.quantity(2).identify().collect(); //set quantity is 3, but Duelist starts with 2

		assignInitialQuickslot( hero.belongings.weapon, 0 );
		assignInitialQuickslot( spikes, 1 );

		new PotionOfStrength().identify();
		new ScrollOfMirrorImage().identify();
	}

	private static void initCleric( Hero hero ) {

		(hero.belongings.weapon = new Cudgel()).identify();
		hero.belongings.weapon.activate(hero);

		HolyTome tome = new HolyTome();
		(hero.belongings.artifact = tome).identify();
		hero.belongings.artifact.activate( hero );

		assignInitialQuickslot( tome, 0 );

		new PotionOfPurity().identify();
		new ScrollOfRemoveCurse().identify();
	}

	public void grantInitialKit( Hero hero ) {
		new ScrollOfIdentify().identify();

		if (hero.belongings.armor() == null && hero.belongings.getItem( ClothArmor.class ) == null) {
			ClothArmor armor = new ClothArmor();
			armor.identify();
			if (!Challenges.isItemBlocked( armor )) hero.belongings.armor = armor;
		}

		collectIfMissing( hero, Food.class, new Food() );

		if (hero.belongings.getItem( VelvetPouch.class ) == null) {
			new VelvetPouch().collect();
		}
		Dungeon.LimitedDrops.VELVET_POUCH.drop();

		Waterskin waterskin = hero.belongings.getItem( Waterskin.class );
		if (waterskin == null) {
			waterskin = new Waterskin();
			waterskin.collect();
		}

		switch (this) {
			case WARRIOR:
				if (hero.belongings.weapon() == null && hero.belongings.getItem( WornShortsword.class ) == null) {
					WornShortsword sword = new WornShortsword();
					sword.identify();
					if (!Challenges.isItemBlocked( sword )) hero.belongings.weapon = sword;
				}
				if (hero.belongings.armor() != null && hero.belongings.armor().checkSeal() == null) {
					hero.belongings.armor().affixSeal( new BrokenSeal() );
					Catalog.setSeen( BrokenSeal.class );
				}
				ThrowingStone stones = new ThrowingStone();
				stones.identify();
				stones = collectIfMissing( hero, ThrowingStone.class, stones );
				assignInitialQuickslot( stones, 0 );
				new PotionOfHealing().identify();
				new ScrollOfRage().identify();
				break;
			case MAGE:
				MagesStaff staff = hero.belongings.getItem( MagesStaff.class );
				if (hero.belongings.weapon() == null && staff == null) {
					staff = new MagesStaff( new WandOfMagicMissile() );
					staff.identify();
					if (!Challenges.isItemBlocked( staff )) {
						hero.belongings.weapon = staff;
						hero.belongings.weapon.activate( hero );
					}
				}
				assignInitialQuickslot( staff, 0 );
				new ScrollOfUpgrade().identify();
				new PotionOfLiquidFlame().identify();
				break;
			case ROGUE:
				if (hero.belongings.weapon() == null && hero.belongings.getItem( Dagger.class ) == null) {
					Dagger dagger = new Dagger();
					dagger.identify();
					if (!Challenges.isItemBlocked( dagger )) hero.belongings.weapon = dagger;
				}
				CloakOfShadows cloak = hero.belongings.getItem( CloakOfShadows.class );
				if (hero.belongings.artifact() == null && cloak == null) {
					cloak = new CloakOfShadows();
					cloak.identify();
					if (!Challenges.isItemBlocked( cloak )) {
						hero.belongings.artifact = cloak;
						hero.belongings.artifact.activate( hero );
					}
				}
				ThrowingKnife knives = new ThrowingKnife();
				knives.identify();
				knives = collectIfMissing( hero, ThrowingKnife.class, knives );
				assignInitialQuickslot( cloak, 0 );
				assignInitialQuickslot( knives, 1 );
				new ScrollOfMagicMapping().identify();
				new PotionOfInvisibility().identify();
				break;
			case HUNTRESS:
				if (hero.belongings.weapon() == null && hero.belongings.getItem( Gloves.class ) == null) {
					Gloves gloves = new Gloves();
					gloves.identify();
					if (!Challenges.isItemBlocked( gloves )) hero.belongings.weapon = gloves;
				}
				SpiritBow bow = hero.belongings.getItem( SpiritBow.class );
				if (bow == null) {
					bow = new SpiritBow();
					bow.identify();
					if (Challenges.isItemBlocked( bow )) {
						bow = null;
					} else {
						bow.collect();
					}
				}
				assignInitialQuickslot( bow, 0 );
				new PotionOfMindVision().identify();
				new ScrollOfLullaby().identify();
				break;
			case DUELIST:
				Rapier rapier = hero.belongings.getItem( Rapier.class );
				if (hero.belongings.weapon() == null && rapier == null) {
					rapier = new Rapier();
					rapier.identify();
					if (!Challenges.isItemBlocked( rapier )) {
						hero.belongings.weapon = rapier;
						hero.belongings.weapon.activate( hero );
					}
				}
				ThrowingSpike spikes = new ThrowingSpike();
				spikes.quantity(2);
				spikes.identify();
				spikes = collectIfMissing( hero, ThrowingSpike.class, spikes );
				assignInitialQuickslot( rapier, 0 );
				assignInitialQuickslot( spikes, 1 );
				new PotionOfStrength().identify();
				new ScrollOfMirrorImage().identify();
				break;
			case CLERIC:
				if (hero.belongings.weapon() == null && hero.belongings.getItem( Cudgel.class ) == null) {
					Cudgel cudgel = new Cudgel();
					cudgel.identify();
					if (!Challenges.isItemBlocked( cudgel )) {
						hero.belongings.weapon = cudgel;
						hero.belongings.weapon.activate( hero );
					}
				}
				HolyTome tome = hero.belongings.getItem( HolyTome.class );
				if (hero.belongings.artifact() == null && tome == null) {
					tome = new HolyTome();
					tome.identify();
					if (!Challenges.isItemBlocked( tome )) {
						hero.belongings.artifact = tome;
						hero.belongings.artifact.activate( hero );
					}
				}
				assignInitialQuickslot( tome, 0 );
				new PotionOfPurity().identify();
				new ScrollOfRemoveCurse().identify();
				break;
		}

		if (SPDSettings.quickslotWaterskin()) {
			assignInitialQuickslot( waterskin, -1 );
		}
	}

	static void assignInitialQuickslot( Item item, int preferredSlot ) {
		if (item == null || Dungeon.quickslot.contains( item )) return;
		if (preferredSlot >= 0 && preferredSlot < QuickSlot.SIZE
				&& Dungeon.quickslot.getItem( preferredSlot ) == null) {
			Dungeon.quickslot.setSlot( preferredSlot, item );
			return;
		}
		for (int slot = 0; slot < QuickSlot.SIZE; slot++) {
			if (Dungeon.quickslot.getItem( slot ) == null) {
				Dungeon.quickslot.setSlot( slot, item );
				return;
			}
		}
	}

	private static <T extends Item> T collectIfMissing( Hero hero, Class<T> itemClass, T item ) {
		T existing = hero.belongings.getItem( itemClass );
		if (existing != null) return existing;
		if (Challenges.isItemBlocked( item )) return null;
		item.collect();
		return item;
	}

	public String title() {
		return Messages.get(HeroClass.class, name());
	}

	public String desc(){
		return Messages.get(HeroClass.class, name()+"_desc");
	}

	public String shortDesc(){
		return Messages.get(HeroClass.class, name()+"_desc_short");
	}

	public HeroSubClass[] subClasses() {
		return subClasses;
	}

	public ArmorAbility[] armorAbilities(){
		switch (this) {
			case WARRIOR: default:
				return new ArmorAbility[]{new HeroicLeap(), new Shockwave(), new Endure()};
			case MAGE:
				return new ArmorAbility[]{new ElementalBlast(), new WildMagic(), new WarpBeacon()};
			case ROGUE:
				return new ArmorAbility[]{new SmokeBomb(), new DeathMark(), new ShadowClone()};
			case HUNTRESS:
				return new ArmorAbility[]{new SpectralBlades(), new NaturesPower(), new SpiritHawk()};
			case DUELIST:
				return new ArmorAbility[]{new Challenge(), new ElementalStrike(), new Feint()};
			case CLERIC:
				return new ArmorAbility[]{new AscendedForm(), new Trinity(), new PowerOfMany()};
		}
	}

	public String spritesheet() {
		switch (this) {
			case WARRIOR: default:
				return Assets.Sprites.WARRIOR;
			case MAGE:
				return Assets.Sprites.MAGE;
			case ROGUE:
				return Assets.Sprites.ROGUE;
			case HUNTRESS:
				return Assets.Sprites.HUNTRESS;
			case DUELIST:
				return Assets.Sprites.DUELIST;
			case CLERIC:
				return Assets.Sprites.CLERIC;
		}
	}

	public String splashArt(){
		switch (this) {
			case WARRIOR: default:
				return Assets.Splashes.WARRIOR;
			case MAGE:
				return Assets.Splashes.MAGE;
			case ROGUE:
				return Assets.Splashes.ROGUE;
			case HUNTRESS:
				return Assets.Splashes.HUNTRESS;
			case DUELIST:
				return Assets.Splashes.DUELIST;
			case CLERIC:
				return Assets.Splashes.CLERIC;
		}
	}
	
	public boolean isUnlocked(){
		return HeroClassUnlocks.isUnlocked( this );
	}
	
	public String unlockMsg() {
		return shortDesc() + "\n\n" + HeroClassUnlocks.unlockMessage( this );
	}

}
