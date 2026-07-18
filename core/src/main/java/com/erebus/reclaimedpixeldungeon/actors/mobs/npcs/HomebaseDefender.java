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
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Healing;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Invisibility;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.armor.ClassArmor;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.wands.DamageWand;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

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
		strength = record.strength();
		weapon = record.weapon();
		armor = record.armor();
		Item nextRanged = record.ranged();
		if (ranged instanceof Wand && ranged != nextRanged) {
			((Wand)ranged).stopCharging();
		}
		ranged = nextRanged;
		if (ranged instanceof Wand) {
			((Wand)ranged).charge( this );
		}
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

	public void gainExperienceFrom( Mob defeated ) {
		if (Dungeon.homebase == null || defenderId == -1 || defeated == null) return;
		HomebaseState.DefenderRecord record = Dungeon.homebase.defender( defenderId );
		if (record == null || !record.alive()) return;

		int amount = Math.max( 1, defeated.EXP > 0 ? defeated.EXP : com.erebus.reclaimedpixeldungeon.actors.mobs.MobStats.currentLevel() );
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
			updatePeacefulBehavior();
		}
		return super.act();
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
		clearDefensingPos();
		state = HUNTING;

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
		int bestDistance = Integer.MAX_VALUE;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob == this
					|| !mob.isAlive()
					|| mob.alignment != Alignment.ENEMY
					|| mob.isInvulnerable( getClass() )) {
				continue;
			}
			int distance = Dungeon.level.distance( pos, mob.pos );
			if (best == null
					|| (mob.countsInHomebaseRaid() && !(best instanceof Mob && ((Mob)best).countsInHomebaseRaid()))
					|| distance < bestDistance) {
				best = mob;
				bestDistance = distance;
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
				+ "Supplies " + suppliesDescription();
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

	private boolean hasRangedWeapon() {
		return weapon instanceof MissileWeapon || weapon instanceof SpiritBow;
	}

	private boolean hasUsableRangedSlot() {
		if (ranged instanceof Wand) {
			return ((Wand)ranged).curCharges > 0;
		}
		return ranged instanceof MissileWeapon && ranged.quantity() > 0;
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

		if (hasRangedWeapon()) {
			return !Dungeon.level.adjacent( pos, enemy.pos )
					&& new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos == enemy.pos;
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
				&& new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (!canUseRangedSlot( enemy )) {
			return super.doAttack( enemy );
		}

		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			sprite.zap( enemy.pos );
		}
		performRangedSlotAttack( enemy );
		Invisibility.dispel( this );
		spend( attackDelay() );
		return true;
	}

	private void performRangedSlotAttack( Char enemy ) {
		if (ranged instanceof Wand) {
			zapWand( (Wand)ranged, enemy );
		} else if (ranged instanceof MissileWeapon) {
			throwMissile( (MissileWeapon)ranged, enemy );
		}
	}

	private void zapWand( Wand wand, Char enemy ) {
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
		damage += wand.rarityStat( RarityStat.Type.MAGIC_DAMAGE );
		if (Dungeon.homebase != null) {
			damage += Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_DAMAGE );
			int magicBonus = wand.rarityStat( RarityStat.Type.MAGIC_BONUS )
					+ Dungeon.homebase.trainingBonus( HomebaseState.Training.MAGIC_POWER )
					+ Dungeon.homebase.trainingBonus( HomebaseState.Training.WAND_DAMAGE );
			damage = Math.round( damage * (1f + magicBonus / 100f) );
		} else {
			damage = Math.round( damage * (1f + wand.rarityStat( RarityStat.Type.MAGIC_BONUS ) / 100f) );
		}
		damage = Math.max( 1, Math.round( applyMobStatDamage( damage ) ) );
		enemy.damage( damage, this );
	}

	private void throwMissile( MissileWeapon missile, Char enemy ) {
		rangedAttack = true;
		attack( enemy );
		rangedAttack = false;

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
		if (rangedAttack && ranged instanceof MissileWeapon) {
			return Math.max( 1, Math.round( skill * ((MissileWeapon)ranged).accuracyFactor( this, target ) ) );
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
		if (rangedAttack && ranged instanceof MissileWeapon) {
			return delay * ((MissileWeapon)ranged).delayFactor( this );
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
		Weapon attackWeapon = rangedAttack && ranged instanceof MissileWeapon ? (MissileWeapon)ranged : weapon;
		int damage = attackWeapon == null
				? Random.NormalIntRange( minDamage, Math.max( minDamage, maxDamage ) )
				: attackWeapon.damageRoll( this );
		if (attackWeapon != null && attackWeapon.STRReq() > strength) {
			damage -= 2 * (attackWeapon.STRReq() - strength);
		}
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
		return super.drRoll() + Math.max( 0, armorRoll );
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		Weapon attackWeapon = rangedAttack && ranged instanceof MissileWeapon ? (MissileWeapon)ranged : weapon;
		return attackWeapon == null ? damage : attackWeapon.proc( this, enemy, damage );
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		damage = super.defenseProc( enemy, damage );
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
