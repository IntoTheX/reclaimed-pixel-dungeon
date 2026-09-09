/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 */

package com.erebus.reclaimedpixeldungeon.actors.mobs.npcs;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Cripple;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vertigo;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.CellEmitter;
import com.erebus.reclaimedpixeldungeon.effects.Beam;
import com.erebus.reclaimedpixeldungeon.effects.Lightning;
import com.erebus.reclaimedpixeldungeon.effects.MagicMissile;
import com.erebus.reclaimedpixeldungeon.effects.particles.BlastParticle;
import com.erebus.reclaimedpixeldungeon.effects.particles.SparkParticle;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bombs.ArcaneBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Bomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Firebomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.FlashBangBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.FrostBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Noisemaker;
import com.erebus.reclaimedpixeldungeon.items.bombs.ShrapnelBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.SmokeBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.WoollyBomb;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfBlastWave;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfCorrosion;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfCorruption;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfDisintegration;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFireblast;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFrost;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLightning;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfPrismaticLight;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.MissileSprite;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;

/** Runs the independent arsenals of the four homebase towers during a raid. */
public class HomebaseTowerDefense extends Actor {

	private static final float TIME_EPSILON = 0.0001f;

	private final HomebaseLevel level;
	private final HashMap<String, Float> readyTimes = new HashMap<>();
	private final EnumMap<HomebaseState.Building, Integer> towerAnchors = new EnumMap<>( HomebaseState.Building.class );
	private final EnumMap<HomebaseState.Building, ArrayList<HomebaseState.TowerWeaponRecord>> cachedArsenals =
			new EnumMap<>( HomebaseState.Building.class );
	private final ArrayList<Mob> activeRaiders = new ArrayList<>();
	private int raiderScanWindow = Integer.MIN_VALUE;
	private TowerBombVolley pendingBombVolley;
	private float pendingBombVolleyTime = Float.NaN;

	public HomebaseTowerDefense( HomebaseLevel level ) {
		this.level = level;
	}

	@Override
	protected boolean act() {
		if (Dungeon.level != level || Dungeon.homebase == null || !Dungeon.homebase.raidActive()) {
			spend( TICK );
			return true;
		}

		int attackWindow = (int)Math.floor( Actor.now() + TIME_EPSILON );
		refreshRaiders( attackWindow );
		if (activeRaiders.isEmpty()) {
			spend( TICK );
			return true;
		}

		for (HomebaseState.Building tower : HomebaseState.Building.values()) {
			if (!HomebaseState.isTowerBuilding( tower ) || !Dungeon.homebase.isBuilt( tower )
					|| Dungeon.homebase.buildingDestroyed( tower )) continue;
			Integer source = towerAnchors.get( tower );
			if (source == null) {
				source = level.towerAnchor( tower );
				towerAnchors.put( tower, source );
			}
			if (!cachedArsenals.containsKey( tower )) {
				cachedArsenals.put( tower, Dungeon.homebase.towerWeapons( tower ) );
			}
			fireReadyWeapons( tower, source, cachedArsenals.get( tower ) );
		}

		spend( TICK );
		return true;
	}

	private void refreshRaiders( int window ) {
		if (raiderScanWindow == window) return;
		raiderScanWindow = window;
		activeRaiders.clear();
		for (Char ch : Actor.chars()) {
			if (ch instanceof Mob && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY
					&& ((Mob)ch).countsInHomebaseRaid()) activeRaiders.add( (Mob)ch );
		}
	}

	private void fireReadyWeapons( HomebaseState.Building tower, int source,
			ArrayList<HomebaseState.TowerWeaponRecord> arsenal ) {
		if (source < 0) return;
		for (HomebaseState.TowerWeaponRecord weapon : arsenal) {
			if (!weapon.valid() || Actor.now() < readyTime( tower, weapon )) continue;
			Mob target = nearestRaider( source, weapon.visionRange() );
			if (target == null) continue;
			Item item = weapon.item();
			if (item == null) continue;

			showAttack( source, target, item );
			if (item instanceof Bomb) {
				queueBomb( target.pos, item, weapon.rollDamage() );
			} else {
				target.damage( weapon.rollDamage(), this );
				applyWandEffect( target, item );
			}
			readyTimes.put( key( tower, weapon ), Actor.now() + weapon.cooldownTurns() );
		}
	}

	private void queueBomb( int cell, Item bomb, int damage ) {
		if (pendingBombVolley == null || Math.abs( pendingBombVolleyTime - Actor.now() ) > TIME_EPSILON) {
			pendingBombVolley = new TowerBombVolley();
			pendingBombVolleyTime = Actor.now();
			Actor.addDelayed( pendingBombVolley, 2f );
		}
		pendingBombVolley.add( cell, bomb, damage );
	}

	private float readyTime( HomebaseState.Building tower, HomebaseState.TowerWeaponRecord weapon ) {
		Float time = readyTimes.get( key( tower, weapon ) );
		return time == null ? 0f : time;
	}

	private String key( HomebaseState.Building tower, HomebaseState.TowerWeaponRecord weapon ) {
		return tower.ordinal() + ":" + weapon.weaponClassName();
	}

	private Mob nearestRaider( int source, int range ) {
		Mob result = null;
		int resultDistance = Integer.MAX_VALUE;
		for (Mob mob : activeRaiders) {
			if (!mob.isAlive()) continue;
			int distance = level.distance( source, mob.pos );
			if (distance <= range && distance < resultDistance) {
				result = mob;
				resultDistance = distance;
			}
		}
		return result;
	}

	private void showAttack( int source, Mob target, Item item ) {
		if (Dungeon.hero == null || Dungeon.hero.sprite == null || Dungeon.hero.sprite.parent == null) return;
		Group parent = Dungeon.hero.sprite.parent;
		if (item instanceof Wand) {
			if (item instanceof WandOfDisintegration) {
				parent.add( new Beam.DeathRay(
						DungeonTilemap.raisedTileCenterToWorld( source ),
						DungeonTilemap.raisedTileCenterToWorld( target.pos ) ) );
				Sample.INSTANCE.play( Assets.Sounds.RAY );
				return;
			}
			if (item instanceof WandOfPrismaticLight) {
				parent.add( new Beam.LightRay(
						DungeonTilemap.raisedTileCenterToWorld( source ),
						DungeonTilemap.raisedTileCenterToWorld( target.pos ) ) );
				Sample.INSTANCE.play( Assets.Sounds.RAY );
				return;
			}
			if (item instanceof WandOfLightning) {
				CellEmitter.center( target.pos ).burst( SparkParticle.FACTORY, 3 );
				parent.addToFront( new Lightning(
						DungeonTilemap.raisedTileCenterToWorld( source ),
						DungeonTilemap.raisedTileCenterToWorld( target.pos ), null ) );
				Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				return;
			}
			MagicMissile missile = (MagicMissile)parent.recycle( MagicMissile.class );
			missile.reset( wandMissileType( (Wand)item ), source, target.pos, null );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
		} else {
			((MissileSprite)parent.recycle( MissileSprite.class )).reset( source, target.pos, item, null );
		}
	}

	private int wandMissileType( Wand wand ) {
		if (wand instanceof WandOfBlastWave) return MagicMissile.FORCE;
		if (wand instanceof WandOfCorrosion) return MagicMissile.CORROSION;
		if (wand instanceof WandOfCorruption) return MagicMissile.SHADOW;
		if (wand instanceof WandOfFireblast) return MagicMissile.FIRE_CONE;
		if (wand instanceof WandOfFrost) return MagicMissile.FROST;
		if (wand instanceof WandOfLightning) return MagicMissile.LIGHT_MISSILE;
		return MagicMissile.MAGIC_MISSILE;
	}

	private void applyWandEffect( Mob target, Item item ) {
		if (item instanceof WandOfFrost) Buff.affect( target, Chill.class, 3f );
		if (item instanceof WandOfFireblast) Buff.affect( target, Burning.class ).reignite( target, 3f );
		if (item instanceof WandOfCorruption) Buff.affect( target, Vertigo.class, 2f );
	}

	private class TowerBombVolley extends Actor {

		private final ArrayList<PendingBomb> bombs = new ArrayList<>();

		private void add( int cell, Item bomb, int damage ) {
			ItemSprite armedSprite;
			if (Dungeon.hero != null && Dungeon.hero.sprite != null && Dungeon.hero.sprite.parent != null) {
				armedSprite = new ItemSprite( bomb );
				armedSprite.place( cell );
				Dungeon.hero.sprite.parent.addToFront( armedSprite );
			} else {
				armedSprite = null;
			}
			bombs.add( new PendingBomb( cell, bomb, damage, armedSprite ) );
		}

		@Override
		protected boolean act() {
			if (pendingBombVolley == this) {
				pendingBombVolley = null;
				pendingBombVolleyTime = Float.NaN;
			}
			ArrayList<Mob> raiders = new ArrayList<>();
			for (Char ch : Actor.chars()) {
				if (!(ch instanceof Mob) || !ch.isAlive() || ch.alignment != Char.Alignment.ENEMY) continue;
				Mob mob = (Mob)ch;
				if (mob.countsInHomebaseRaid()) raiders.add( mob );
			}
			HashSet<Integer> blastCells = new HashSet<>();
			for (PendingBomb pending : bombs) {
				if (pending.armedSprite != null) pending.armedSprite.killAndErase();
				if (blastCells.add( pending.cell )) {
					CellEmitter.center( pending.cell ).burst( BlastParticle.FACTORY, 10 );
				}
				for (Mob mob : raiders) {
					if (!mob.isAlive() || level.distance( pending.cell, mob.pos ) > 1) continue;
					mob.damage( pending.damage, HomebaseTowerDefense.this );
					applyBombEffect( mob, pending.bomb );
				}
			}
			if (!bombs.isEmpty()) Sample.INSTANCE.play( Assets.Sounds.BLAST );
			Actor.remove( this );
			return true;
		}

		private void applyBombEffect( Mob target, Item bomb ) {
			if (bomb instanceof Firebomb) Buff.affect( target, Burning.class ).reignite( target, 5f );
			if (bomb instanceof FrostBomb) Buff.affect( target, Chill.class, 5f );
			if (bomb instanceof FlashBangBomb) {
				Buff.affect( target, Blindness.class, 4f );
				Buff.affect( target, Vertigo.class, 3f );
			}
			if (bomb instanceof SmokeBomb) Buff.affect( target, Blindness.class, 5f );
			if (bomb instanceof WoollyBomb) Buff.affect( target, Paralysis.class, 2f );
			if (bomb instanceof ShrapnelBomb) Buff.affect( target, Cripple.class, 4f );
			if (bomb instanceof ArcaneBomb) Buff.affect( target, Vertigo.class, 3f );
			if (bomb instanceof Noisemaker) Buff.affect( target, Vertigo.class, 4f );
		}

		private class PendingBomb {
			private final int cell;
			private final Item bomb;
			private final int damage;
			private final ItemSprite armedSprite;

			private PendingBomb( int cell, Item bomb, int damage, ItemSprite armedSprite ) {
				this.cell = cell;
				this.bomb = bomb;
				this.damage = damage;
				this.armedSprite = armedSprite;
			}
		}
	}
}
