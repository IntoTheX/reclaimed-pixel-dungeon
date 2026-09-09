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

package com.erebus.reclaimedpixeldungeon.actors.mobs.npcs;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Healing;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Invisibility;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.Beam;
import com.erebus.reclaimedpixeldungeon.effects.CellEmitter;
import com.erebus.reclaimedpixeldungeon.effects.Lightning;
import com.erebus.reclaimedpixeldungeon.effects.MagicMissile;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.effects.particles.SparkParticle;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.armor.ClassArmor;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.materials.ForgeResourceMaterial;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.wands.DamageWand;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfBlastWave;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfCorrosion;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfCorruption;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfDisintegration;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFireblast;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFrost;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLightning;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLivingEarth;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfPrismaticLight;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfRegrowth;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfTransfusion;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfWarding;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MagesStaff;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;
import com.erebus.reclaimedpixeldungeon.sprites.MissileSprite;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HomebaseDefender extends DirectableAlly {

	private static final String DEFENDER_ID = "defender_id";
	private static final String DEFENDER_NAME = "defender_name";
	private static final String ARCHETYPE = "archetype";
	private static final String LEVEL = "level";
	private static final String ATTACK_SKILL = "attack_skill";
	private static final String DEFENSE_SKILL = "defense_skill";
	private static final String MIN_DAMAGE = "min_damage";
	private static final String MAX_DAMAGE = "max_damage";
	private static final String MAX_ARMOR = "max_armor";
	private static final String TITLE = "title";
	private static final String RARITY = "rarity";
	private static final String ARCHETYPE_NAME = "archetype_name";
	private static final String XP = "xp";
	private static final String XP_TO_NEXT = "xp_to_next";
	private static final String PEACEFUL_MODE = "peaceful_mode";
	private static final String PEACEFUL_TURNS = "peaceful_turns";
	private static final String RECOVERY_TURNS = "recovery_turns";
	private static final String SLEEP_REGEN_TICKER = "sleep_regen_ticker";

	private static final int MODE_IDLE = 0;
	private static final int MODE_WANDER = 1;
	private static final int MODE_SLEEP = 2;
	private static final int MODE_PATROL = 3;
	private static final int POST_RAID_RECOVERY_TURNS = 50;
	private static final int SLEEP_REGEN_DELAY = 10;
	private static final float LIFE_PRESERVATION_HP = 0.35f;

	private int defenderId = -1;
	private String defenderName = "homebase defender";
	private int archetype = HomebaseState.DefenderRecord.WARRIOR;
	private int level = 1;
	private int attackSkill = 12;
	private int minDamage = 3;
	private int maxDamage = 7;
	private int maxArmor = 2;
	private String title = "Common defender";
	private ItemRarity rarity = ItemRarity.COMMON;
	private String archetypeName = "warrior";
	private int xp = 0;
	private int xpToNext = 20;
	private int strength = 10;
	private Weapon weapon;
	private Armor armor;
	private Item ranged;
	private int peacefulMode = MODE_IDLE;
	private int peacefulTurns = 0;
	private int recoveryTurns = 0;
	private int sleepRegenTicker = 0;
	private boolean wasRaidActive = false;
	private boolean rangedAttack = false;
	private DefenderSkills defenderSkills = new DefenderSkills();

	{
		spriteClass = HomebaseDefenderSprite.class;
		EXP = 0;
		maxLvl = 0;
		state = WANDERING;
		viewDistance = 8;
	}

	public HomebaseDefender() {
	}

	public HomebaseDefender( HomebaseState.DefenderRecord record ) {
		applyRecord( record );
	}

	public int defenderId() {
		return defenderId;
	}

	@Override
	public CharSprite sprite() {
		return new HomebaseDefenderSprite( heroClass(), armorTier(), rarity() );
	}

	private void applyRecord( HomebaseState.DefenderRecord record ) {
		if (record == null) return;
		float healthPercent = HT > 0 ? HP / (float)HT : 1f;
		defenderId = record.id();
		defenderName = record.defenderName();
		archetype = record.archetype();
		level = record.level();
		mobStats = record.mobStats();
		HT = record.maxHP() + (!firstAdded && mobStats != null ? mobStats.health() : 0);
		HP = Math.max( 1, Math.min( HT, Math.round( HT * healthPercent ) ) );
		attackSkill = record.attackSkill();
		defenseSkill = record.defenseSkill();
		minDamage = record.minDamage();
		maxDamage = record.maxDamage();
		maxArmor = record.maxArmor();
		title = record.title();
		rarity = record.rarity();
		archetypeName = record.archetypeName();
		xp = record.xp();
		xpToNext = record.xpToNext();
		defenderSkills = record.defenderSkills();
		strength = record.strength();
		Weapon nextWeapon = record.weapon();
		if (weapon instanceof MagesStaff && weapon != nextWeapon && ((MagesStaff)weapon).imbuedWand() != null) {
			((MagesStaff)weapon).imbuedWand().stopCharging();
		}
		weapon = nextWeapon;
		if (weapon instanceof MagesStaff) {
			((MagesStaff)weapon).applyWandChargeBuff( this );
		}
		armor = record.armor();
		Item nextRanged = record.ranged();
		if (ranged != nextRanged) stopRangedCharging( ranged );
		ranged = nextRanged;
		chargeRangedItem( ranged );
		spriteClass = HomebaseDefenderSprite.class;
		if (sprite instanceof HomebaseDefenderSprite) {
			((HomebaseDefenderSprite)sprite).updateArmor( heroClass(), armorTier() );
			((HomebaseDefenderSprite)sprite).updateRarityHalo( rarity() );
		}
	}

	public HeroClass heroClass() {
		switch (archetype) {
			case HomebaseState.DefenderRecord.MAGE:
				return HeroClass.MAGE;
			case HomebaseState.DefenderRecord.ROGUE:
				return HeroClass.ROGUE;
			case HomebaseState.DefenderRecord.HUNTRESS:
				return HeroClass.HUNTRESS;
			case HomebaseState.DefenderRecord.DUELIST:
				return HeroClass.DUELIST;
			case HomebaseState.DefenderRecord.PRIEST:
				return HeroClass.CLERIC;
			case HomebaseState.DefenderRecord.WARRIOR:
			default:
				return HeroClass.WARRIOR;
		}
	}

	public int armorTier() {
		if (armor instanceof ClassArmor) {
			return 6;
		}
		return armor == null ? 0 : armor.tier;
	}

	public Weapon weapon() {
		return weapon;
	}

	public Armor armor() {
		return armor;
	}

	public Item ranged() {
		return ranged;
	}

	public void refreshFromRecord() {
		syncRecord();
	}

	public float xpProgress() {
		return Math.max( 0f, Math.min( 1f, xp / (float)Math.max( 1, xpToNext ) ) );
	}

	public int defenderLevel() { return level; }
	public int experience() { return xp; }
	public int experienceToNext() { return xpToNext; }

	public void gainExperienceFrom( Mob defeated ) {
		if (Dungeon.homebase == null || defenderId == -1 || defeated == null) return;
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null || !record.alive()) return;

		int base = Math.max( 1, defeated.EXP > 0 ? defeated.EXP : com.erebus.reclaimedpixeldungeon.actors.mobs.MobStats.currentLevel() );
		int amount = Math.max( 1, Math.round( base * (1f
				+ record.mobStats().stat( com.erebus.reclaimedpixeldungeon.items.RarityStat.Type.XP_GAIN ) / 100f) ) );
		boolean levelled = record.gainExperience( amount );
		applyRecord( record );
		if (levelled) showLevelUpEffect();
	}

	public void showLevelUpEffect() {
		if (sprite == null) return;
		sprite.showStatusWithIcon( CharSprite.POSITIVE, "Lvl " + level, com.erebus.reclaimedpixeldungeon.effects.FloatingText.EXPERIENCE );
		sprite.showStatus( CharSprite.POSITIVE, "Level up!" );
		sprite.centerEmitter().burst( Speck.factory( Speck.STAR ), 12 );
		Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
	}

	@Override
	protected boolean act() {
		if (!homebaseResidentAllowed()) {
			destroy();
			return false;
		}
		syncRecord();
		studyEquipment();
		if (homebaseRaidActive()) {
			wasRaidActive = true;
			prepareRaidHunt();
			if (tryRaidAttackOutsideFOV()) {
				return true;
			}
		} else {
			if (wasRaidActive) {
				wasRaidActive = false;
				clearDefensingPos();
				if (HP < HT) {
					startRecoverySleep( POST_RAID_RECOVERY_TURNS );
				} else {
					peacefulTurns = 0;
				}
			}
			if (HP >= HT && tryCollectBattlefieldMaterialAtFeet()) {
				return true;
			}
			if (HP >= HT && assignBattlefieldMaterialCollection()) {
				return super.act();
			}
			updatePeacefulBehavior();
		}
		return super.act();
	}

	private boolean tryRaidAttackOutsideFOV() {
		if (enemy == null
				|| !enemy.isAlive()
				|| shouldPreserveLife()
				|| isCharmedBy( enemy )
				|| !canAttack( enemy )) {
			return false;
		}
		target = enemy.pos;
		return doAttack( enemy );
	}

	private void syncRecord() {
		if (Dungeon.homebase == null || defenderId == -1) return;
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record != null && record.alive()) {
			applyRecord( record );
		}
	}

	private void studyEquipment() {
		if (Dungeon.homebase == null || defenderId == -1) return;
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null || !record.alive()) return;
		Item identified = record.studyEquipment();
		if (identified != null) {
			applyRecord( record );
			GLog.p( name() + " identifies " + identified.name() + "." );
		}
	}

	@Override
	protected Char chooseEnemy() {
		Char raidTarget = homebaseRaidActive() ? nearestRaidTarget() : null;
		return raidTarget != null ? raidTarget : super.chooseEnemy();
	}

	private boolean homebaseRaidActive() {
		return homebaseResidentAllowed() && Dungeon.homebase != null && Dungeon.homebase.raidActive();
	}

	private boolean homebaseResidentAllowed() {
		return Dungeon.depth == 0 && Dungeon.level instanceof HomebaseLevel;
	}

	private void prepareRaidHunt() {
		attacksAutomatically = true;
		state = HUNTING;
		if (Dungeon.level instanceof HomebaseLevel) {
			int station = ((HomebaseLevel)Dungeon.level).defenderRaidStation( defenderId, this );
			if (station != -1 && prefersDistance()) {
				defendPos( station );
			} else {
				clearDefensingPos();
			}
		}

		Char raidTarget = nearestRaidTarget();
		boolean lowHealth = isLowHealth();
		useStoredHealingPotion( lowHealth );
		if (lowHealth) {
			useStoredInvisibilityPotion();
		}

		if (shouldPreserveLife()) {
			enemy = raidTarget;
			target = Dungeon.hero == null ? -1 : Dungeon.hero.pos;
			regenerateWhileRetreating();
			return;
		}

		if (raidTarget != null) {
			enemy = raidTarget;
			target = raidTarget.pos;
		}
	}

	private Char nearestRaidTarget() {
		if (Dungeon.level == null) return null;

		Char best = null;
		int bestScore = Integer.MAX_VALUE;
		int assignedSide = Dungeon.level instanceof HomebaseLevel
				? ((HomebaseLevel)Dungeon.level).defenderRaidSide( defenderId )
				: -1;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob == this
					|| !mob.isAlive()
					|| mob.alignment != Alignment.ENEMY
					|| mob.isInvulnerable( getClass() )) {
				continue;
			}
			int distance = Dungeon.level.distance( pos, mob.pos );
			int score = distance;
			if (assignedSide >= 0 && Dungeon.level instanceof HomebaseLevel
					&& ((HomebaseLevel)Dungeon.level).raidApproachSide( mob.pos ) != assignedSide) {
				score += 8;
			}
			if (mob.countsInHomebaseRaid()) {
				score -= 3;
			}
			if (best == null
					|| (mob.countsInHomebaseRaid() && !(best instanceof Mob && ((Mob)best).countsInHomebaseRaid()))
					|| score < bestScore) {
				best = mob;
				bestScore = score;
			}
		}
		return best;
	}

	private void updatePeacefulBehavior() {
		attacksAutomatically = peacefulMode == MODE_PATROL;

		if (HP < HT && peacefulMode != MODE_SLEEP) {
			startRecoverySleep( Math.max( POST_RAID_RECOVERY_TURNS, recoveryTurns ) );
		}

		if (peacefulTurns <= 0 || reachedPeacefulDestination()) {
			choosePeacefulBehavior();
		}
		peacefulTurns--;
		if (recoveryTurns > 0) recoveryTurns--;

		switch (peacefulMode) {
			case MODE_SLEEP:
				clearDefensingPos();
				state = SLEEPING;
				target = -1;
				regenerateWhileSleeping();
				break;
			case MODE_IDLE:
				clearDefensingPos();
				state = PASSIVE;
				target = -1;
				break;
			case MODE_PATROL:
				state = WANDERING;
				break;
			case MODE_WANDER:
			default:
				state = WANDERING;
			break;
		}
	}

	private boolean tryCollectBattlefieldMaterialAtFeet() {
		if (!(Dungeon.level instanceof HomebaseLevel)
				|| Dungeon.homebase == null
				|| Dungeon.level.heaps == null) {
			return false;
		}
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap == null || heap.isEmpty() || heap.type != Heap.Type.HEAP) {
			return false;
		}

		int[] collected = new int[HomebaseState.Material.values().length + HomebaseState.ForgeResource.values().length];
		for (Item item : heap.items.toArray( new Item[0] )) {
			if (item instanceof BuildingMaterial) {
				BuildingMaterial material = (BuildingMaterial)item;
				int amount = Math.max( 1, material.quantity() );
				Dungeon.homebase.add( material.material(), amount );
				collected[material.material().ordinal()] += amount;
				heap.remove( item );
			} else if (item instanceof ForgeResourceMaterial) {
				ForgeResourceMaterial resource = (ForgeResourceMaterial)item;
				int amount = Math.max( 1, resource.quantity() );
				Dungeon.homebase.addForgeResource( resource.resource(), amount );
				collected[HomebaseState.Material.values().length + resource.resource().ordinal()] += amount;
				heap.remove( item );
			}
		}

		String secured = collectedText( collected );
		if (secured.isEmpty()) return false;
		GLog.p( name() + " secures " + secured + " from the battlefield." );
		if (sprite != null) {
			sprite.showStatus( CharSprite.POSITIVE, "secured" );
		}
		clearDefensingPos();
		peacefulTurns = 0;
		spend( TICK );
		return true;
	}

	private boolean assignBattlefieldMaterialCollection() {
		if (!(Dungeon.level instanceof HomebaseLevel)
				|| Dungeon.homebase == null
				|| Dungeon.level.heaps == null) {
			return false;
		}

		int best = -1;
		int bestDistance = Integer.MAX_VALUE;
		for (Heap heap : Dungeon.level.heaps.valueList()) {
			if (heap == null || heap.isEmpty() || heap.type != Heap.Type.HEAP || !heapHasHomebaseMaterial( heap )) continue;
			if (Actor.findChar( heap.pos ) != null && heap.pos != pos) continue;
			if (!Dungeon.level.passable[heap.pos] || Dungeon.level.solid[heap.pos]) continue;
			int distance = Dungeon.level.distance( pos, heap.pos );
			if (distance < bestDistance) {
				bestDistance = distance;
				best = heap.pos;
			}
		}
		if (best == -1) return false;

		peacefulMode = MODE_WANDER;
		peacefulTurns = Math.max( 4, bestDistance + 2 );
		attacksAutomatically = false;
		state = WANDERING;
		defendPos( best );
		target = best;
		return true;
	}

	private boolean heapHasHomebaseMaterial( Heap heap ) {
		for (Item item : heap.items) {
			if (item instanceof BuildingMaterial || item instanceof ForgeResourceMaterial) {
				return true;
			}
		}
		return false;
	}

	private String collectedText( int[] collected ) {
		StringBuilder text = new StringBuilder();
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			int amount = material.ordinal() < collected.length ? collected[material.ordinal()] : 0;
			if (amount <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount ).append( ' ' ).append( material.name().toLowerCase().replace( '_', ' ' ) );
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			int index = HomebaseState.Material.values().length + resource.ordinal();
			int amount = index < collected.length ? collected[index] : 0;
			if (amount <= 0) continue;
			if (text.length() > 0) text.append( ", " );
			text.append( amount ).append( ' ' ).append( resource.label() );
		}
		return text.toString();
	}

	private boolean reachedPeacefulDestination() {
		return (peacefulMode == MODE_WANDER || peacefulMode == MODE_PATROL)
				&& defendingPos != -1
				&& pos == defendingPos;
	}

	private void choosePeacefulBehavior() {
		clearDefensingPos();
		path = null;

		if (HP < HT) {
			startRecoverySleep( Math.max( POST_RAID_RECOVERY_TURNS, recoveryTurns ) );
			return;
		}

		int roll = Random.Int( 100 );
		if (roll < 25) {
			peacefulMode = MODE_IDLE;
			peacefulTurns = Random.IntRange( 4, 10 );
		} else if (roll < 70) {
			peacefulMode = MODE_WANDER;
			peacefulTurns = Random.IntRange( 8, 18 );
			defendPos( randomHomebaseDestination() );
			attacksAutomatically = false;
		} else if (roll < 85) {
			peacefulMode = MODE_SLEEP;
			peacefulTurns = Random.IntRange( 8, 18 );
		} else {
			peacefulMode = MODE_PATROL;
			peacefulTurns = Random.IntRange( 10, 24 );
			defendPos( randomPatrolDestination() );
			attacksAutomatically = true;
		}
	}

	private void startRecoverySleep( int turns ) {
		peacefulMode = MODE_SLEEP;
		peacefulTurns = Math.max( peacefulTurns, Math.max( 1, turns ) );
		recoveryTurns = Math.max( recoveryTurns, Math.max( 1, turns ) );
		attacksAutomatically = false;
		clearDefensingPos();
		target = -1;
	}

	private void regenerateWhileSleeping() {
		if (HP >= HT) {
			sleepRegenTicker = 0;
			return;
		}
		sleepRegenTicker++;
		if (sleepRegenTicker >= SLEEP_REGEN_DELAY) {
			sleepRegenTicker = 0;
			HP = Math.min( HT, HP + 1 );
		}
	}

	private void regenerateWhileRetreating() {
		if (HP >= HT) {
			sleepRegenTicker = 0;
			return;
		}
		if (enemy != null && Dungeon.level != null && Dungeon.level.adjacent( pos, enemy.pos )) {
			sleepRegenTicker = 0;
			return;
		}
		sleepRegenTicker++;
		if (sleepRegenTicker >= SLEEP_REGEN_DELAY * 2) {
			sleepRegenTicker = 0;
			HP = Math.min( HT, HP + 1 );
		}
	}

	private int randomHomebaseDestination() {
		if (Dungeon.level == null) return pos;
		for (int tries = 0; tries < 20; tries++) {
			int cell = Dungeon.level.randomDestination( this );
			if (cell != -1 && Dungeon.level.distance( pos, cell ) <= 10) {
				return cell;
			}
		}
		return Dungeon.level.randomDestination( this );
	}

	private int randomPatrolDestination() {
		if (Dungeon.level == null) return pos;
		int w = Dungeon.level.width();
		int[] patrolCells = {
				16 + 3 * w,
				16 + 38 * w,
				5 + 20 * w,
				27 + 20 * w,
				8 + 9 * w,
				24 + 9 * w,
				8 + 31 * w,
				24 + 31 * w
		};
		for (int tries = 0; tries < patrolCells.length; tries++) {
			int cell = patrolCells[Random.Int( patrolCells.length )];
			if (Dungeon.level.insideMap( cell )) {
				return cell;
			}
		}
		return randomHomebaseDestination();
	}

	@Override
	public String name() {
		return defenderName == null || defenderName.isEmpty() ? "homebase defender" : defenderName;
	}

	@Override
	public String description() {
		String displayTitle = colored( rarity().displayName(), rarity().color() ) + " "
				+ (archetypeName == null || archetypeName.isEmpty() ? "defender" : archetypeName);
		return displayTitle + "\n\nA reclaimed settler who will defend the homebase during raids.\n\n"
				+ "Level " + level + "\n"
				+ "XP " + xp + "/" + xpToNext + "\n"
				+ "Strength " + strength + "\n"
				+ "Health " + HP + "/" + HT + "\n"
				+ "Damage " + minDamage + "-" + maxDamage + "\n"
				+ "Armor 0-" + maxArmor + "\n"
				+ "Weapon " + equipmentDescription( weapon ) + "\n"
				+ "Armor " + equipmentDescription( armor ) + "\n"
				+ "Ranged " + equipmentDescription( ranged ) + "\n"
				+ "Supplies " + suppliesDescription()
				+ (defenderSkills.hasSkills() ? "\n\nSkills" + defenderSkills.description() : "");
	}

	private String equipmentDescription( Item item ) {
		if (item == null) return "none";
		String status = item.status();
		String text = item.name() + (status == null ? "" : " [" + status + "]");
		if (item instanceof Weapon) {
			text += " (STR " + ((Weapon)item).STRReq() + ")";
		} else if (item instanceof Armor) {
			text += " (STR " + ((Armor)item).STRReq() + ")";
		}
		return text;
	}

	private String suppliesDescription() {
		if (Dungeon.homebase == null || defenderId == -1) return "none";
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null) return "none";

		String supplies = "";
		if (record.healingPotions() > 0) supplies += record.healingPotions() + " healing";
		if (record.invisibilityPotions() > 0) supplies += (supplies.isEmpty() ? "" : ", ") + record.invisibilityPotions() + " invisibility";
		if (record.ankhs() > 0) supplies += (supplies.isEmpty() ? "" : ", ") + record.ankhs() + " ankh";
		return supplies.isEmpty() ? "none" : supplies;
	}

	public ItemRarity rarity() {
		return rarity == null ? ItemRarity.COMMON : rarity;
	}

	private String colored( String text, int color ) {
		return "@@C" + String.format( "%06X", color & 0xFFFFFF ) + "@@" + text + "@@CEND@@";
	}

	private void stopRangedCharging( Item item ) {
		if (item instanceof Wand) {
			((Wand)item).stopCharging();
		} else if (item instanceof MagesStaff && ((MagesStaff)item).imbuedWand() != null) {
			((MagesStaff)item).imbuedWand().stopCharging();
		}
	}

	private void chargeRangedItem( Item item ) {
		if (item instanceof Wand) {
			((Wand)item).charge( this );
		} else if (item instanceof MagesStaff) {
			((MagesStaff)item).applyWandChargeBuff( this );
		}
	}

	private Wand rangedWand() {
		if (ranged instanceof Wand) {
			return (Wand)ranged;
		}
		if (ranged instanceof MagesStaff) {
			return ((MagesStaff)ranged).imbuedWand();
		}
		return null;
	}

	private Weapon rangedAttackWeapon() {
		return rangedAttack && ranged instanceof Weapon ? (Weapon)ranged : null;
	}

	private boolean hasRangedWeapon() {
		return hasUsableMainRangedWeapon();
	}

	private boolean hasUsableRangedSlot() {
		Wand wand = rangedWand();
		if (wand != null) {
			return wand.curCharges > 0;
		}
		return ranged instanceof SpiritBow
				|| ranged instanceof MissileWeapon && ranged.quantity() > 0;
	}

	private boolean prefersDistance() {
		return hasUsableRangedSlot() || hasRangedWeapon() || (weapon != null && weapon.reachFactor( this ) > 1);
	}

	private int preferredDistance() {
		if (hasUsableRangedSlot() || hasRangedWeapon()) {
			return 4;
		}
		return weapon != null && weapon.reachFactor( this ) > 1 ? 2 : 1;
	}

	private boolean hasEmergencyBackup() {
		if (Dungeon.homebase == null || defenderId == -1) return false;
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		return record != null
				&& (record.ankhs() > 0
				|| record.healingPotions() > 0
				|| record.invisibilityPotions() > 0);
	}

	private boolean isLowHealth() {
		return HT > 0 && HP <= Math.max( 1, Math.round( HT * LIFE_PRESERVATION_HP ) );
	}

	private boolean shouldPreserveLife() {
		return homebaseRaidActive()
				&& isLowHealth()
				&& !hasEmergencyBackup();
	}

	public boolean useStoredHealingPotion( boolean emergency ) {
		if (Dungeon.homebase == null || defenderId == -1 || HP >= HT) return false;
		if (!emergency && buff( Healing.class ) != null) return false;

		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null || !record.consumeHealingPotion()) return false;

		PotionOfHealing.cure( this );
		PotionOfHealing.heal( this );
		if (sprite != null) {
			sprite.showStatusWithIcon( CharSprite.POSITIVE, "Potion", com.erebus.reclaimedpixeldungeon.effects.FloatingText.HEALING );
		}
		GLog.p( name() + " drinks a healing potion." );
		return true;
	}

	private boolean useStoredInvisibilityPotion() {
		if (Dungeon.homebase == null || defenderId == -1 || buff( Invisibility.class ) != null || invisible > 0) return false;

		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null || !record.consumeInvisibilityPotion()) return false;

		Buff.prolong( this, Invisibility.class, Invisibility.DURATION );
		if (sprite != null) {
			sprite.showStatus( CharSprite.POSITIVE, "invisible" );
		}
		GLog.p( name() + " drinks an invisibility potion." );
		return true;
	}

	private boolean hasRetreatRoom( int from ) {
		return Dungeon.level != null
				&& fieldOfView != null
				&& fieldOfView.length == Dungeon.level.length()
				&& Dungeon.flee( this, from, Dungeon.level.passable, fieldOfView, true ) != -1;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (enemy == null) {
			return false;
		}

		if (shouldPreserveLife()) {
			return false;
		}

		if (prefersDistance()
				&& Dungeon.level.distance( pos, enemy.pos ) < preferredDistance()
				&& hasRetreatRoom( enemy.pos )) {
			return false;
		}

		if (hasUsableRangedSlot() && Dungeon.level.adjacent( pos, enemy.pos )) {
			return false;
		}

		if (canUseRangedSlot( enemy )) {
			return true;
		}

		if (weapon == null) {
			return super.canAttack( enemy );
		}

		if (hasRangedWeapon() && hasUsableMainRangedWeapon()) {
			return !Dungeon.level.adjacent( pos, enemy.pos )
					&& new Ballistica( pos, enemy.pos, mainRangedCollisionProperties( enemy.pos ) ).collisionPos == enemy.pos;
		}

		if (weapon.reachFactor( this ) > 1) {
			return !Dungeon.level.adjacent( pos, enemy.pos ) && weapon.canReach( this, enemy.pos );
		}

		return weapon.canReach( this, enemy.pos ) || super.canAttack( enemy );
	}

	private boolean canUseRangedSlot( Char enemy ) {
		return hasUsableRangedSlot()
				&& enemy != null
				&& !Dungeon.level.adjacent( pos, enemy.pos )
				&& new Ballistica( pos, enemy.pos, rangedCollisionProperties( enemy.pos ) ).collisionPos == enemy.pos;
	}

	private boolean hasUsableMainRangedWeapon() {
		if (weapon instanceof MissileWeapon) {
			return weapon.quantity() > 0;
		}
		if (weapon instanceof MagesStaff) {
			Wand wand = ((MagesStaff)weapon).imbuedWand();
			return wand != null && wand.curCharges > 0;
		}
		return weapon instanceof SpiritBow;
	}

	private boolean canUseMainRangedWeapon( Char enemy ) {
		return hasUsableMainRangedWeapon()
				&& enemy != null
				&& !Dungeon.level.adjacent( pos, enemy.pos )
				&& new Ballistica( pos, enemy.pos, mainRangedCollisionProperties( enemy.pos ) ).collisionPos == enemy.pos;
	}

	private int rangedCollisionProperties( int target ) {
		Wand wand = rangedWand();
		if (wand != null) {
			return wand.collisionProperties( target );
		}
		return Ballistica.PROJECTILE;
	}

	private int mainRangedCollisionProperties( int target ) {
		if (weapon instanceof MagesStaff && ((MagesStaff)weapon).imbuedWand() != null) {
			return ((MagesStaff)weapon).imbuedWand().collisionProperties( target );
		}
		return Ballistica.PROJECTILE;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (canUseRangedSlot( enemy )) {
			if (sprite != null && enemy != null && enemy.sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
			}
			showRangedAttackFx( ranged, enemy );
			performRangedSlotAttack( enemy );
			Invisibility.dispel( this );
			spend( attackDelay() );
			return true;
		}

		if (canUseMainRangedWeapon( enemy )) {
			if (sprite != null && enemy != null && enemy.sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
			}
			showRangedAttackFx( weapon, enemy );
			performMainRangedAttack( enemy );
			Invisibility.dispel( this );
			spend( attackDelay() );
			return true;
		}

		return super.doAttack( enemy );
	}

	private void showRangedAttackFx( Item attackItem, Char enemy ) {
		if (sprite == null || sprite.parent == null || enemy == null) return;
		if (attackItem instanceof SpiritBow) {
			((MissileSprite)sprite.parent.recycle( MissileSprite.class )).reset( sprite, enemy.pos, ((SpiritBow)attackItem).knockArrow(), null );
		} else if (attackItem instanceof MissileWeapon) {
			((MissileSprite)sprite.parent.recycle( MissileSprite.class )).reset( sprite, enemy.pos, attackItem, null );
		} else if (attackItem instanceof MagesStaff && ((MagesStaff)attackItem).imbuedWand() != null) {
			showWandFx( ((MagesStaff)attackItem).imbuedWand(), enemy );
		} else if (attackItem instanceof Wand) {
			showWandFx( (Wand)attackItem, enemy );
		}
	}

	private void showWandFx( Wand wand, Char enemy ) {
		if (wand == null || enemy == null || sprite == null || sprite.parent == null) return;
		Ballistica bolt = new Ballistica( pos, enemy.pos, wand.collisionProperties( enemy.pos ) );
		int target = bolt.collisionPos;
		if (wand instanceof WandOfDisintegration) {
			int beamCell = bolt.path.get( Math.min( bolt.dist, 10 ) );
			sprite.parent.add( new Beam.DeathRay( sprite.center(), DungeonTilemap.raisedTileCenterToWorld( beamCell ) ) );
			Sample.INSTANCE.play( Assets.Sounds.RAY );
		} else if (wand instanceof WandOfPrismaticLight) {
			sprite.parent.add( new Beam.LightRay( sprite.center(), DungeonTilemap.raisedTileCenterToWorld( target ) ) );
			Sample.INSTANCE.play( Assets.Sounds.RAY );
		} else if (wand instanceof WandOfTransfusion) {
			sprite.parent.add( new Beam.HealthRay( sprite.center(), DungeonTilemap.raisedTileCenterToWorld( target ) ) );
			Sample.INSTANCE.play( Assets.Sounds.RAY );
		} else if (wand instanceof WandOfLightning) {
			ArrayList<Lightning.Arc> arcs = new ArrayList<>();
			if (enemy.sprite != null) {
				arcs.add( new Lightning.Arc( sprite.center(), enemy.sprite.center() ) );
			} else {
				arcs.add( new Lightning.Arc( sprite.center(), DungeonTilemap.raisedTileCenterToWorld( target ) ) );
			}
			CellEmitter.center( target ).burst( SparkParticle.FACTORY, 3 );
			sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		} else {
			MagicMissile.boltFromChar( sprite.parent, wandMissileType( wand ), sprite, target, null );
			Sample.INSTANCE.play( Assets.Sounds.ZAP );
			if (wand instanceof WandOfFireblast) Sample.INSTANCE.play( Assets.Sounds.BURNING );
		}
	}

	private int wandMissileType( Wand wand ) {
		if (wand instanceof WandOfBlastWave) return MagicMissile.FORCE;
		if (wand instanceof WandOfCorrosion) return MagicMissile.CORROSION;
		if (wand instanceof WandOfCorruption) return MagicMissile.SHADOW;
		if (wand instanceof WandOfFireblast) return MagicMissile.FIRE_CONE;
		if (wand instanceof WandOfFrost) return MagicMissile.FROST;
		if (wand instanceof WandOfLivingEarth) return MagicMissile.EARTH;
		if (wand instanceof WandOfRegrowth) return MagicMissile.FOLIAGE_CONE;
		if (wand instanceof WandOfWarding) return MagicMissile.WARD;
		return MagicMissile.MAGIC_MISSILE;
	}

	private void performRangedSlotAttack( Char enemy ) {
		if (ranged instanceof Wand) {
			zapWand( (Wand)ranged, ranged, enemy );
		} else if (ranged instanceof MagesStaff) {
			zapStaff( (MagesStaff)ranged, enemy );
		} else if (ranged instanceof MissileWeapon) {
			throwMissile( (MissileWeapon)ranged, enemy );
		} else if (ranged instanceof SpiritBow) {
			shootRangedWeapon( (SpiritBow)ranged, enemy );
		}
	}

	private void performMainRangedAttack( Char enemy ) {
		if (weapon instanceof MagesStaff) {
			zapStaff( (MagesStaff)weapon, enemy );
		} else if (weapon instanceof Weapon) {
			attack( enemy );
		}
	}

	private void zapStaff( MagesStaff staff, Char enemy ) {
		Wand wand = staff.imbuedWand();
		if (wand != null) {
			zapWand( wand, staff, enemy );
		}
	}

	private void zapWand( Wand wand, Item statSource, Char enemy ) {
		if (wand.curCharges <= 0) return;
		wand.curCharges--;
		wand.curChargeKnown = true;
		Item.updateQuickslot();

		int lvl = Math.max( 0, wand.buffedLvl() );
		int damage;
		if (wand instanceof DamageWand) {
			DamageWand damageWand = (DamageWand)wand;
			damage = Random.NormalIntRange( damageWand.min( lvl ), damageWand.max( lvl ) );
		} else {
			damage = Random.NormalIntRange( 2 + lvl, 5 + 2 * lvl );
		}
		damage += statSource.rarityStat( RarityStat.Type.MAGIC_DAMAGE );
		if (Dungeon.homebase != null) {
			damage += Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_DAMAGE );
			int magicBonus = statSource.rarityStat( RarityStat.Type.MAGIC_BONUS )
					+ Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_POWER )
					+ Dungeon.homebase.trainingBonus( HomebaseState.Training.WAND_DAMAGE );
			damage = Math.round( damage * (1f + magicBonus / 100f) );
		} else {
			damage = Math.round( damage * (1f + statSource.rarityStat( RarityStat.Type.MAGIC_BONUS ) / 100f) );
		}
		damage = Math.max( 1, Math.round( applyMobStatDamage( damage ) ) );
		enemy.damage( damage, this );
	}

	private void shootRangedWeapon( Weapon attackWeapon, Char enemy ) {
		rangedAttack = true;
		attack( enemy );
		rangedAttack = false;
	}

	private void throwMissile( MissileWeapon missile, Char enemy ) {
		shootRangedWeapon( missile, enemy );

		if (!missile.useFromDefenderStack()) {
			if (Dungeon.homebase != null && defenderId != -1) {
				HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
				if (record != null) record.equipRanged( null );
			}
			ranged = null;
		}
	}

	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING && shouldPreserveLife()) {
			if (enemySeen
					&& enemy != null
					&& Dungeon.level.distance( pos, enemy.pos ) <= preferredDistance() + 1
					&& getFurther( enemy.pos )) {
				return true;
			}
			if (Dungeon.hero != null
					&& Dungeon.level.distance( pos, Dungeon.hero.pos ) > 1
					&& super.getCloser( Dungeon.hero.pos )) {
				return true;
			}
			return enemy != null && getFurther( enemy.pos );
		}
		if (state == HUNTING
				&& enemySeen
				&& enemy != null
				&& prefersDistance()
				&& Dungeon.level.distance( pos, enemy.pos ) < preferredDistance()
				&& getFurther( enemy.pos )) {
			return true;
		}
		return super.getCloser( target );
	}

	@Override
	public int attackSkill( Char target ) {
		int skill = attackSkill;
		skill = Math.round( skill * (1f + 0.08f * defenderSkills.level( DefenderSkills.Skill.KEEN_HUNTER )) );
		Weapon attackWeapon = rangedAttackWeapon();
		if (attackWeapon instanceof MissileWeapon) {
			return Math.max( 1, Math.round( skill * ((MissileWeapon)attackWeapon).accuracyFactor( this, target ) ) );
		}
		if (attackWeapon != null) {
			skill = Math.round( skill * attackWeapon.accuracyFactor( this, target ) );
			return Math.max( 1, skill );
		}
		if (weapon != null && weapon.STRReq() > strength) {
			skill -= 2 * (weapon.STRReq() - strength);
		}
		if (weapon != null) {
			skill = Math.round( skill * weapon.accuracyFactor( this, target ) );
		}
		return Math.max( 1, skill );
	}

	@Override
	public float attackDelay() {
		float delay = super.attackDelay();
		delay /= 1f + 0.06f * defenderSkills.level( DefenderSkills.Skill.BLOODRUSH );
		Weapon attackWeapon = rangedAttackWeapon();
		if (attackWeapon instanceof MissileWeapon) {
			return delay * ((MissileWeapon)attackWeapon).delayFactor( this );
		}
		if (attackWeapon != null) {
			return delay * attackWeapon.delayFactor( this );
		}
		if (weapon != null) {
			delay *= weapon.delayFactor( this );
		}
		return delay;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		int defense = super.defenseSkill( enemy );
		if (defense != 0 && armor != null) {
			defense = Math.round( armor.evasionFactor( this, defense ) );
		}
		return Math.max( 0, defense );
	}

	@Override
	public int damageRoll() {
		Weapon attackWeapon = rangedAttackWeapon();
		if (attackWeapon == null) attackWeapon = weapon;
		int damage = attackWeapon == null
				? Random.NormalIntRange( minDamage, Math.max( minDamage, maxDamage ) )
				: attackWeapon.damageRoll( this );
		if (attackWeapon != null && attackWeapon.STRReq() > strength) {
			damage -= 2 * (attackWeapon.STRReq() - strength);
		}
		if (HP * 2 < HT) damage = Math.round( damage * (1f + 0.08f * defenderSkills.level( DefenderSkills.Skill.BATTLE_TRANCE )) );
		return Math.max( 1, damage );
	}

	@Override
	public int drRoll() {
		int armorRoll = Random.NormalIntRange( 0, Math.max( 0, maxArmor ) );
		if (armor != null) {
			armorRoll += Random.NormalIntRange( armor.DRMin(), armor.DRMax() );
			if (armor.STRReq() > strength) {
				armorRoll -= 2 * (armor.STRReq() - strength);
			}
		}
		armorRoll += defenderSkills.level( DefenderSkills.Skill.THICK_HIDE ) * 2;
		if (HP * 2 < HT) armorRoll += defenderSkills.level( DefenderSkills.Skill.EARTHEN_COVENANT ) * 2;
		return super.drRoll() + Math.max( 0, armorRoll );
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		int vampiric = defenderSkills.level( DefenderSkills.Skill.VAMPIRIC_EDGE );
		if (vampiric > 0 && damage > 0) HP = Math.min( HT, HP + Math.max( 1, Math.round( damage * vampiric * 0.03f ) ) );
		Weapon attackWeapon = rangedAttackWeapon();
		if (attackWeapon == null) attackWeapon = weapon;
		return attackWeapon == null ? damage : attackWeapon.proc( this, enemy, damage );
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		damage = super.defenseProc( enemy, damage );
		int spines = defenderSkills.level( DefenderSkills.Skill.RETALIATORY_SPINES );
		if (spines > 0 && enemy != null && enemy.alignment == Alignment.ENEMY && damage > 0) {
			enemy.damage( Math.max( 1, damage * spines / 20 ), this );
		}
		return armor == null ? damage : armor.proc( enemy, this, damage );
	}

	@Override
	public void die( Object cause ) {
		if (!homebaseResidentAllowed()) {
			destroy();
			return;
		}
		if (Dungeon.homebase != null && defenderId != -1) {
			HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
			if (record != null && record.consumeAnkh()) {
				applyRecord( record );
				HP = HT;
				if (sprite != null) {
					sprite.showStatusWithIcon( CharSprite.POSITIVE, "Ankh", com.erebus.reclaimedpixeldungeon.effects.FloatingText.HEALING );
				}
				GLog.p( name() + "'s ankh restores them." );
				return;
			}
			Dungeon.homebase.markDefenderDead( defenderId );
		}
		GLog.w( name() + " has fallen defending the homebase." );
		super.die( cause );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEFENDER_ID, defenderId );
		bundle.put( DEFENDER_NAME, defenderName );
		bundle.put( ARCHETYPE, archetype );
		bundle.put( LEVEL, level );
		bundle.put( ATTACK_SKILL, attackSkill );
		bundle.put( DEFENSE_SKILL, defenseSkill );
		bundle.put( MIN_DAMAGE, minDamage );
		bundle.put( MAX_DAMAGE, maxDamage );
		bundle.put( MAX_ARMOR, maxArmor );
		bundle.put( TITLE, title );
		bundle.put( RARITY, rarity().name() );
		bundle.put( ARCHETYPE_NAME, archetypeName );
		bundle.put( XP, xp );
		bundle.put( XP_TO_NEXT, xpToNext );
		bundle.put( PEACEFUL_MODE, peacefulMode );
		bundle.put( PEACEFUL_TURNS, peacefulTurns );
		bundle.put( RECOVERY_TURNS, recoveryTurns );
		bundle.put( SLEEP_REGEN_TICKER, sleepRegenTicker );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		defenderId = bundle.getInt( DEFENDER_ID );
		defenderName = bundle.getString( DEFENDER_NAME );
		archetype = bundle.getInt( ARCHETYPE );
		level = Math.max( 1, bundle.getInt( LEVEL ) );
		attackSkill = bundle.getInt( ATTACK_SKILL );
		defenseSkill = bundle.getInt( DEFENSE_SKILL );
		minDamage = bundle.getInt( MIN_DAMAGE );
		maxDamage = bundle.getInt( MAX_DAMAGE );
		maxArmor = bundle.getInt( MAX_ARMOR );
		title = bundle.getString( TITLE );
		archetypeName = bundle.getString( ARCHETYPE_NAME );
		if (bundle.contains( XP )) xp = bundle.getInt( XP );
		if (bundle.contains( XP_TO_NEXT )) xpToNext = bundle.getInt( XP_TO_NEXT );
		if (bundle.contains( RARITY )) {
			try {
				rarity = ItemRarity.valueOf( bundle.getString( RARITY ) );
			} catch (Exception e) {
				rarity = ItemRarity.COMMON;
			}
		}
		if (bundle.contains( PEACEFUL_MODE )) peacefulMode = bundle.getInt( PEACEFUL_MODE );
		if (bundle.contains( PEACEFUL_TURNS )) peacefulTurns = bundle.getInt( PEACEFUL_TURNS );
		if (bundle.contains( RECOVERY_TURNS )) recoveryTurns = bundle.getInt( RECOVERY_TURNS );
		if (bundle.contains( SLEEP_REGEN_TICKER )) sleepRegenTicker = bundle.getInt( SLEEP_REGEN_TICKER );
		spriteClass = HomebaseDefenderSprite.class;
		syncRecord();
	}
}
