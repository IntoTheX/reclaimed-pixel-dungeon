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

package com.erebus.reclaimedpixeldungeon.journal;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Acidic;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Albino;
import com.erebus.reclaimedpixeldungeon.actors.mobs.ArmoredBrute;
import com.erebus.reclaimedpixeldungeon.actors.mobs.ArmoredStatue;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Bandit;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Bat;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Bee;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Brute;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CausticSlime;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Crab;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CrystalGuardian;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CrystalMimic;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CrystalSpire;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CrystalWisp;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DM100;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DM200;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DM201;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DM300;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DemonSpawner;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DwarfKing;
import com.erebus.reclaimedpixeldungeon.actors.mobs.EbonyMimic;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Elemental;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Eye;
import com.erebus.reclaimedpixeldungeon.actors.mobs.FetidRat;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Ghoul;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Gnoll;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GnollExile;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GnollGeomancer;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GnollGuard;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GnollSapper;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GnollTrickster;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GoldenMimic;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Golem;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Goo;
import com.erebus.reclaimedpixeldungeon.actors.mobs.GreatCrab;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Guard;
import com.erebus.reclaimedpixeldungeon.actors.mobs.HermitCrab;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mimic;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Monk;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Necromancer;
import com.erebus.reclaimedpixeldungeon.actors.mobs.PhantomPiranha;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Piranha;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Pylon;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Rat;
import com.erebus.reclaimedpixeldungeon.actors.mobs.RipperDemon;
import com.erebus.reclaimedpixeldungeon.actors.mobs.RotHeart;
import com.erebus.reclaimedpixeldungeon.actors.mobs.RotLasher;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Scorpio;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Senior;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Shaman;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Skeleton;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Slime;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Snake;
import com.erebus.reclaimedpixeldungeon.actors.mobs.SpectralNecromancer;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Spinner;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Statue;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Succubus;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Swarm;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Tengu;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Thief;
import com.erebus.reclaimedpixeldungeon.actors.mobs.TormentedSpirit;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Warlock;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Wraith;
import com.erebus.reclaimedpixeldungeon.actors.mobs.YogDzewa;
import com.erebus.reclaimedpixeldungeon.actors.mobs.YogFist;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Ghost;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Imp;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.RatKing;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Sheep;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.erebus.reclaimedpixeldungeon.items.artifacts.DriedRose;
import com.erebus.reclaimedpixeldungeon.items.quest.CorpseDust;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLivingEarth;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfRegrowth;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfWarding;
import com.erebus.reclaimedpixeldungeon.levels.rooms.special.SentryRoom;
import com.erebus.reclaimedpixeldungeon.levels.traps.AlarmTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.BlazingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.BurningTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.ChillingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.ConfusionTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.CorrosionTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.CursingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.DisarmingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.DisintegrationTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.DistortionTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.ExplosiveTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.FlashingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.FlockTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.FrostTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GatewayTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GeyserTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GnollRockfallTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GrimTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GrippingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GuardianTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.OozeTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.PitfallTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.PoisonDartTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.RockfallTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.ShockingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.StormTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.SummoningTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.TeleportationTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.TenguDartTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.ToxicTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.WarpingTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.WeakeningTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.WornDartTrap;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.plants.BlandfruitBush;
import com.erebus.reclaimedpixeldungeon.plants.Blindweed;
import com.erebus.reclaimedpixeldungeon.plants.Earthroot;
import com.erebus.reclaimedpixeldungeon.plants.Fadeleaf;
import com.erebus.reclaimedpixeldungeon.plants.Firebloom;
import com.erebus.reclaimedpixeldungeon.plants.Icecap;
import com.erebus.reclaimedpixeldungeon.plants.Mageroyal;
import com.erebus.reclaimedpixeldungeon.plants.Rotberry;
import com.erebus.reclaimedpixeldungeon.plants.Sorrowmoss;
import com.erebus.reclaimedpixeldungeon.plants.Starflower;
import com.erebus.reclaimedpixeldungeon.plants.Stormvine;
import com.erebus.reclaimedpixeldungeon.plants.Sungrass;
import com.erebus.reclaimedpixeldungeon.plants.Swiftthistle;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

//contains all the game's various entities, mostly enemies, NPCS, and allies, but also traps and plants
public enum Bestiary {

	REGIONAL,
	BOSSES,
	UNIVERSAL,
	RARE,
	QUEST,
	NEUTRAL,
	ALLY,
	TRAP,
	PLANT;

	//tracks whether an entity has been encountered
	private final LinkedHashMap<Class<?>, Boolean> seen = new LinkedHashMap<>();
	//tracks enemy kills, trap activations, plant tramples, or just sets to 1 for seen on allies
	private final LinkedHashMap<Class<?>, Integer> encounterCount = new LinkedHashMap<>();

	//should only be used when initializing
	private void addEntities(Class<?>... classes ){
		for (Class<?> cls : classes){
			seen.put(cls, false);
			encounterCount.put(cls, 0);
		}
	}

	public Collection<Class<?>> entities(){
		return seen.keySet();
	}

	public String title(){
		return Messages.get(this, name() + ".title");
	}

	public int totalEntities(){
		return seen.size();
	}

	public int totalSeen(){
		int seenTotal = 0;
		for (boolean entitySeen : seen.values()){
			if (entitySeen) seenTotal++;
		}
		return seenTotal;
	}

	static {

		REGIONAL.addEntities(Rat.class, Snake.class, Gnoll.class, Swarm.class, Crab.class, Slime.class,
				Skeleton.class, Thief.class, DM100.class, Guard.class, Necromancer.class,
				Bat.class, Brute.class, Shaman.RedShaman.class, Shaman.BlueShaman.class, Shaman.PurpleShaman.class, Spinner.class, DM200.class,
				Ghoul.class, Elemental.FireElemental.class, Elemental.FrostElemental.class, Elemental.ShockElemental.class, Warlock.class, Monk.class, Golem.class,
				RipperDemon.class, DemonSpawner.class, Succubus.class, Eye.class, Scorpio.class);

		BOSSES.addEntities(Goo.class,
				Tengu.class,
				Pylon.class, DM300.class,
				DwarfKing.class,
				YogDzewa.Larva.class, YogFist.BurningFist.class, YogFist.SoiledFist.class, YogFist.RottingFist.class, YogFist.RustedFist.class,YogFist.BrightFist.class, YogFist.DarkFist.class, YogDzewa.class);

		UNIVERSAL.addEntities(Wraith.class, Piranha.class, Mimic.class, GoldenMimic.class, EbonyMimic.class, Statue.class, GuardianTrap.Guardian.class, SentryRoom.Sentry.class);

		RARE.addEntities(Albino.class, GnollExile.class, HermitCrab.class, CausticSlime.class,
				Bandit.class, SpectralNecromancer.class,
				ArmoredBrute.class, DM201.class,
				Elemental.ChaosElemental.class, Senior.class,
				Acidic.class,
				TormentedSpirit.class, PhantomPiranha.class, CrystalMimic.class, ArmoredStatue.class);

		QUEST.addEntities(FetidRat.class, GnollTrickster.class, GreatCrab.class,
				Elemental.NewbornFireElemental.class, RotLasher.class, RotHeart.class,
				CrystalWisp.class, CrystalGuardian.class, CrystalSpire.class, GnollGuard.class, GnollSapper.class, GnollGeomancer.class);

		NEUTRAL.addEntities(Ghost.class, RatKing.class, Shopkeeper.class, Wandmaker.class, Blacksmith.class, Imp.class, Sheep.class, Bee.class);

		ALLY.addEntities(MirrorImage.class, PrismaticImage.class,
				DriedRose.GhostHero.class,
				WandOfWarding.Ward.class, WandOfWarding.Ward.WardSentry.class, WandOfLivingEarth.EarthGuardian.class,
				ShadowClone.ShadowAlly.class, SmokeBomb.NinjaLog.class, SpiritHawk.HawkAlly.class, PowerOfMany.LightAlly.class);

		TRAP.addEntities(WornDartTrap.class, PoisonDartTrap.class, DisintegrationTrap.class, GatewayTrap.class,
				ChillingTrap.class, BurningTrap.class, ShockingTrap.class, AlarmTrap.class, GrippingTrap.class, TeleportationTrap.class, OozeTrap.class,
				FrostTrap.class, BlazingTrap.class, StormTrap.class, GuardianTrap.class, FlashingTrap.class, WarpingTrap.class,
				ConfusionTrap.class, ToxicTrap.class, CorrosionTrap.class,
				FlockTrap.class, SummoningTrap.class, WeakeningTrap.class, CursingTrap.class,
				GeyserTrap.class, ExplosiveTrap.class, RockfallTrap.class, PitfallTrap.class,
				DistortionTrap.class, DisarmingTrap.class, GrimTrap.class);

		PLANT.addEntities(Rotberry.class, Sungrass.class, Fadeleaf.class, Icecap.class,
				Firebloom.class, Sorrowmoss.class, Swiftthistle.class, Blindweed.class,
				Stormvine.class, Earthroot.class, Mageroyal.class, Starflower.class,
				BlandfruitBush.class,
				WandOfRegrowth.Dewcatcher.class, WandOfRegrowth.Seedpod.class, WandOfRegrowth.Lotus.class);

	}

	//some mobs and traps have different internal classes in some cases, so need to convert here
	private static final HashMap<Class<?>, Class<?>> classConversions = new HashMap<>();
	static {
		classConversions.put(CorpseDust.DustWraith.class,      Wraith.class);

		classConversions.put(Necromancer.NecroSkeleton.class,  Skeleton.class);

		classConversions.put(TenguDartTrap.class,              PoisonDartTrap.class);
		classConversions.put(GnollRockfallTrap.class,          RockfallTrap.class);

		classConversions.put(DwarfKing.DKGhoul.class,          Ghoul.class);
		classConversions.put(DwarfKing.DKWarlock.class,        Warlock.class);
		classConversions.put(DwarfKing.DKMonk.class,           Monk.class);
		classConversions.put(DwarfKing.DKGolem.class,          Golem.class);

		classConversions.put(YogDzewa.YogRipper.class,         RipperDemon.class);
		classConversions.put(YogDzewa.YogEye.class,            Eye.class);
		classConversions.put(YogDzewa.YogScorpio.class,        Scorpio.class);
	}

	public static boolean isSeen(Class<?> cls){
		for (Bestiary cat : values()) {
			if (cat.seen.containsKey(cls)) {
				return cat.seen.get(cls);
			}
		}
		return false;
	}

	public static void setSeen(Class<?> cls){
		if (classConversions.containsKey(cls)){
			cls = classConversions.get(cls);
		}
		for (Bestiary cat : values()) {
			if (cat.seen.containsKey(cls) && !cat.seen.get(cls)) {
				cat.seen.put(cls, true);
				Journal.saveNeeded = true;
			}
		}
		Badges.validateCatalogBadges();
	}

	public static int encounterCount(Class<?> cls) {
		for (Bestiary cat : values()) {
			if (cat.encounterCount.containsKey(cls)) {
				return cat.encounterCount.get(cls);
			}
		}
		return 0;
	}

	//used primarily when bosses are killed and need to clean up their minions
	public static boolean skipCountingEncounters = false;

	public static void countEncounter(Class<?> cls){
		countEncounters(cls, 1);
	}

	public static void countEncounters(Class<?> cls, int encounters){
		if (skipCountingEncounters){
			return;
		}
		if (classConversions.containsKey(cls)){
			cls = classConversions.get(cls);
		}
		for (Bestiary cat : values()) {
			if (cat.encounterCount.containsKey(cls) && cat.encounterCount.get(cls) != Integer.MAX_VALUE){
				cat.encounterCount.put(cls, cat.encounterCount.get(cls)+encounters);
				if (cat.encounterCount.get(cls) < -1_000_000_000){ //to catch cases of overflow
					cat.encounterCount.put(cls, Integer.MAX_VALUE);
				}
				Journal.saveNeeded = true;
			}
		}
	}

	private static final String BESTIARY_CLASSES    = "bestiary_classes";
	private static final String BESTIARY_SEEN       = "bestiary_seen";
	private static final String BESTIARY_ENCOUNTERS = "bestiary_encounters";

	public static void store( Bundle bundle ){

		ArrayList<Class<?>> classes = new ArrayList<>();
		ArrayList<Boolean> seen = new ArrayList<>();
		ArrayList<Integer> encounters = new ArrayList<>();

		for (Bestiary cat : values()) {
			for (Class<?> entity : cat.entities()) {
				if (cat.seen.get(entity) || cat.encounterCount.get(entity) > 0){
					classes.add(entity);
					seen.add(cat.seen.get(entity));
					encounters.add(cat.encounterCount.get(entity));
				}
			}
		}

		Class<?>[] storeCls = new Class[classes.size()];
		boolean[] storeSeen = new boolean[seen.size()];
		int[] storeEncounters = new int[encounters.size()];

		for (int i = 0; i < storeCls.length; i++){
			storeCls[i] = classes.get(i);
			storeSeen[i] = seen.get(i);
			storeEncounters[i] = encounters.get(i);
		}

		bundle.put( BESTIARY_CLASSES, storeCls );
		bundle.put( BESTIARY_SEEN, storeSeen );
		bundle.put( BESTIARY_ENCOUNTERS, storeEncounters );

	}

	public static void restore( Bundle bundle ){

		if (bundle.contains(BESTIARY_CLASSES)
				&& bundle.contains(BESTIARY_SEEN)
				&& bundle.contains(BESTIARY_ENCOUNTERS)){
			Class<?>[] classes = bundle.getClassArray(BESTIARY_CLASSES);
			boolean[] seen = bundle.getBooleanArray(BESTIARY_SEEN);
			int[] encounters = bundle.getIntArray(BESTIARY_ENCOUNTERS);

			for (int i = 0; i < classes.length; i++){
				for (Bestiary cat : values()){
					if (cat.seen.containsKey(classes[i])){
						cat.seen.put(classes[i], seen[i]);
						cat.encounterCount.put(classes[i], encounters[i]);
					}
				}
			}
		}

	}

}
