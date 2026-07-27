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
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barrier;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bless;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bleeding;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Corrosion;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Cripple;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Daze;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Frost;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Haste;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hex;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Poison;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Recharging;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Roots;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Slow;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vertigo;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vulnerable;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Weakness;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfBlastWave;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

abstract public class KindOfWeapon extends EquipableItem {

	protected String hitSound = Assets.Sounds.HIT;
	protected float hitSoundPitch = 1f;
	
	@Override
	public void execute(Hero hero, String action) {
		if (hero.subClass == HeroSubClass.CHAMPION && action.equals(AC_EQUIP)){
			usesTargeting = false;
			String primaryName = Messages.titleCase(hero.belongings.weapon != null ? hero.belongings.weapon.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			String secondaryName = Messages.titleCase(hero.belongings.secondWep != null ? hero.belongings.secondWep.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			if (primaryName.length() > 18) primaryName = primaryName.substring(0, 15) + "...";
			if (secondaryName.length() > 18) secondaryName = secondaryName.substring(0, 15) + "...";
			GameScene.show(new WndOptions(
					new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(KindOfWeapon.class, "which_equip_msg"),
					Messages.get(KindOfWeapon.class, "which_equip_primary", primaryName),
					Messages.get(KindOfWeapon.class, "which_equip_secondary", secondaryName)
			){
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					if (index == 0 || index == 1){
						//In addition to equipping itself, item reassigns itself to the quickslot
						//This is a special case as the item is being removed from inventory, but is staying with the hero.
						int slot = Dungeon.quickslot.getSlot( KindOfWeapon.this );
						slotOfUnequipped = -1;
						if (index == 0) {
							doEquip(hero);
						} else {
							equipSecondary(hero);
						}
						if (slot != -1) {
							Dungeon.quickslot.setSlot( slot, KindOfWeapon.this );
							updateQuickslot();
						//if this item wasn't quickslotted, but the item it is replacing as equipped was
						//then also have the item occupy the unequipped item's quickslot
						} else if (slotOfUnequipped != -1 && defaultAction() != null) {
							Dungeon.quickslot.setSlot( slotOfUnequipped, KindOfWeapon.this );
							updateQuickslot();
						}
					}
				}
			});
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero != null && (hero.belongings.weapon() == this || hero.belongings.secondWep() == this);
	}

	private static boolean isSwiftEquipping = false;

	protected float timeToEquip( Hero hero ) {
		return isSwiftEquipping ? 0f : super.timeToEquip(hero);
	}
	
	@Override
	public boolean doEquip( Hero hero ) {

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
				isSwiftEquipping = true;
			}
		}

		// 15/25% chance
		if (hero.heroClass != HeroClass.CLERIC && hero.hasTalent(Talent.HOLY_INTUITION)
				&& cursed && !cursedKnown
				&& Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
			cursedKnown = true;
			GLog.p(Messages.get(this, "curse_detected"));
			return false;
		}

		detachAll( hero.belongings.backpack );
		
		if (hero.belongings.weapon == null || hero.belongings.weapon.doUnequip( hero, true )) {
			
			hero.belongings.weapon = this;
			activate( hero );
			Talent.onItemEquipped(hero, this);
			hero.updateHT( false );
			Badges.validateDuelistUnlock();
			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}

			hero.spendAndNext( timeToEquip(hero) );
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null){
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;
			
		} else {
			isSwiftEquipping = false;
			collect( hero.belongings.backpack );
			return false;
		}
	}

	public boolean equipSecondary( Hero hero ){

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
				isSwiftEquipping = true;
			}
		}

		boolean wasInInv = hero.belongings.contains(this);
		detachAll( hero.belongings.backpack );

		if (hero.belongings.secondWep == null || hero.belongings.secondWep.doUnequip( hero, true )) {

			hero.belongings.secondWep = this;
			activate( hero );
			Talent.onItemEquipped(hero, this);
			hero.updateHT( false );
			Badges.validateDuelistUnlock();
			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}

			hero.spendAndNext( timeToEquip(hero) );
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null){
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect( hero.belongings.backpack );
			return false;
		}
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		boolean second = hero.belongings.secondWep == this;

		if (second){
			//do this first so that the item can go to a full inventory
			hero.belongings.secondWep = null;
		}

		if (super.doUnequip( hero, collect, single )) {

			if (!second){
				hero.belongings.weapon = null;
			}
			hero.updateHT( false );
			return true;

		} else {

			if (second){
				hero.belongings.secondWep = this;
			}
			return false;

		}
	}

	public int min(){
		return min(buffedLvl());
	}

	public int max(){
		return max(buffedLvl());
	}

	abstract public int min(int lvl);
	abstract public int max(int lvl);

	public int damageRoll( Char owner ) {
		int damage;
		if (owner instanceof Hero){
			damage = Hero.heroDamageIntRange(min(), max());
		} else {
			damage = Random.NormalIntRange(min(), max());
		}

		return applyRarityDamageStats( damage, owner );
	}

	protected int applyRarityDamageStats( int damage ) {
		return applyRarityDamageStats( damage, null );
	}

	protected int applyRarityDamageStats( int damage, Char owner ) {
		damage += rarityStat( RarityStat.Type.ATTACK_DAMAGE );
		damage = Math.round( damage * (1f + rarityStat( RarityStat.Type.ATTACK_BONUS ) / 100f) );
		int critChance = rarityStat( RarityStat.Type.CRITICAL_CHANCE );
		int critDamage = rarityStat( RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER );
		if (owner instanceof Hero && Dungeon.homebase != null) {
			critChance += Dungeon.homebase.trainingBonus( HomebaseState.Training.CRITICAL_CHANCE );
			critDamage += Dungeon.homebase.trainingBonus( HomebaseState.Training.CRITICAL_DAMAGE );
		}
		if (Random.Int( 100 ) < critChance) {
			damage = Math.round( damage * (2f + critDamage / 100f) );
		}
		return damage;
	}
	
	public float accuracyFactor( Char owner, Char target ) {
		return 1f + rarityStat( RarityStat.Type.ATTACK_ACCURACY ) / 100f;
	}
	
	public float delayFactor( Char owner ) {
		return 1f / (1f + rarityStat( RarityStat.Type.ATTACK_SPEED ) / 100f);
	}

	public int reachFactor( Char owner ){
		return 1;
	}
	
	public boolean canReach( Char owner, int target){
		int reach = reachFactor(owner);
		if (Dungeon.level.distance( owner.pos, target ) > reach){
			return false;
		} else {
			boolean[] passable = BArray.not(Dungeon.level.solid, null);
			for (Char ch : Actor.chars()) {
				if (ch != owner) passable[ch.pos] = false;
			}
			
			PathFinder.buildDistanceMap(target, passable, reach);
			
			return PathFinder.distance[owner.pos] <= reach;
		}
	}

	public int defenseFactor( Char owner ) {
		return rarityStat( RarityStat.Type.DEFENSE );
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		return applyRarityProcStats( attacker, defender, damage );
	}

	protected int applyRarityProcStats( Char attacker, Char defender, int damage ) {
		if (attacker == null || defender == null || damage <= 0 || !defender.isAlive()) return damage;
		if (attacker.alignment == defender.alignment) return damage;

		if (rollOffensiveProc( RarityStat.Type.BLEED_PROC, attacker )) {
			float durationBonus = statusDurationMultiplier( attacker ) * (1f + offensiveDurationBonus( RarityStat.Type.BLEED_DURATION, attacker ) / 4f);
			Buff.affect( defender, Bleeding.class ).set( Math.max( 1f, damage * 0.25f * durationBonus ) );
		}

		if (rollOffensiveProc( RarityStat.Type.BURNING_PROC, attacker )) {
			Buff.affect( defender, Burning.class ).reignite( defender, offensiveDuration( 3f, RarityStat.Type.BURNING_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.POISON_PROC, attacker )) {
			Buff.affect( defender, Poison.class ).set( offensiveDuration( 3f, RarityStat.Type.POISON_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.CORROSION_PROC, attacker )) {
			Buff.affect( defender, Corrosion.class ).set( offensiveDuration( 3f, RarityStat.Type.CORROSION_DURATION, attacker ), Math.max( 1, Math.round( damage * 0.20f ) ) );
		}

		if (rollOffensiveProc( RarityStat.Type.FROST_PROC, attacker )) {
			Buff.affect( defender, Chill.class, offensiveDuration( 2f, RarityStat.Type.FROST_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.CRIPPLE_PROC, attacker )) {
			Buff.prolong( defender, Cripple.class, offensiveDuration( 2f, RarityStat.Type.CRIPPLE_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.DAZE_PROC, attacker )) {
			Buff.prolong( defender, Daze.class, offensiveDuration( Daze.DURATION, RarityStat.Type.DAZE_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.HEX_PROC, attacker )) {
			Buff.prolong( defender, Hex.class, offensiveDuration( 4f, RarityStat.Type.HEX_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.VULNERABLE_PROC, attacker )) {
			Buff.prolong( defender, Vulnerable.class, offensiveDuration( 4f, RarityStat.Type.VULNERABLE_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.BLINDNESS_PROC, attacker )) {
			Buff.prolong( defender, Blindness.class, offensiveDuration( 2f, RarityStat.Type.BLINDNESS_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.ROOT_PROC, attacker )) {
			Buff.prolong( defender, Roots.class, offensiveDuration( 1f, RarityStat.Type.ROOT_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.SLOW_PROC, attacker )) {
			Buff.prolong( defender, Slow.class, offensiveDuration( 2f, RarityStat.Type.SLOW_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.VERTIGO_PROC, attacker )) {
			Buff.prolong( defender, Vertigo.class, offensiveDuration( 2f, RarityStat.Type.VERTIGO_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.STUN_CHANCE, attacker )) {
			Buff.prolong( defender, Paralysis.class, offensiveDuration( 2f, RarityStat.Type.STUN_DURATION, attacker ) );
		}

		if (rollOffensiveProc( RarityStat.Type.WEAKNESS_PROC, attacker )) {
			Buff.prolong( defender, Weakness.class, (4f + offensiveDurationBonus( RarityStat.Type.WEAKNESS_DURATION, attacker ) * 2f) * statusDurationMultiplier( attacker ) );
		}

		damage = applyLegendaryComboDamage( defender, damage );

		if (rollCombatProc( RarityStat.Type.SUMMON_LIGHTNING_CHANCE, attacker, HomebaseState.Training.LIGHTNING_CHANCE )) {
			defender.damage( Math.max( 1, Math.round( damage * 0.35f ) ), this );
		}

		if (defender.isAlive() && rollCombatProc( RarityStat.Type.KNOCKBACK_CHANCE, attacker, HomebaseState.Training.KNOCKBACK_CHANCE )) {
			knockback( attacker, defender );
		}

		if (defender.isAlive() && rollCombatProc( RarityStat.Type.CLEAVE_CHANCE, attacker, HomebaseState.Training.CLEAVE_CHANCE )) {
			cleave( attacker, defender, damage );
		}

		if (rollCombatProc( RarityStat.Type.PIERCING_CHANCE, attacker, HomebaseState.Training.PIERCING_CHANCE )) {
			damage += Math.max( 1, Math.round( damage * 0.25f ) );
		}

		if (rollRarityProc( RarityStat.Type.BLESS_PROC )) {
			Buff.prolong( attacker, Bless.class, rarityDuration( 4f, RarityStat.Type.BLESS_DURATION ) );
		}

		if (rollRarityProc( RarityStat.Type.HASTE_PROC )) {
			Buff.prolong( attacker, Haste.class, rarityDuration( 3f, RarityStat.Type.HASTE_DURATION ) );
		}

		if (rollRarityProc( RarityStat.Type.RECHARGING_PROC )) {
			Buff.prolong( attacker, Recharging.class, rarityDuration( 4f, RarityStat.Type.RECHARGING_DURATION ) );
		}

		if (rollRarityProc( RarityStat.Type.BARRIER_PROC )) {
			Buff.affect( attacker, Barrier.class ).incShield( Math.max( 1, rarityStat( RarityStat.Type.BARRIER_POWER ) + Math.round( damage * 0.15f ) ) );
		}

		int lifesteal = rarityStat( RarityStat.Type.LIFESTEAL );
		if (attacker instanceof Hero && Dungeon.homebase != null) {
			lifesteal += Dungeon.homebase.trainingBonus( HomebaseState.Training.LIFESTEAL );
		}
		if (lifesteal > 0 && attacker.isAlive()) {
			int healAmt = Math.min( attacker.HT - attacker.HP, Math.max( 1, Math.round( damage * lifesteal / 100f ) ) );
			if (healAmt > 0) {
				attacker.HP += healAmt;
				attacker.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString( healAmt ), FloatingText.HEALING );
			}
		}

		return damage;
	}

	private int applyLegendaryComboDamage( Char defender, int damage ) {
		boolean bleeding = defender.buff( Bleeding.class ) != null;
		boolean slowedOrFrozen = defender.buff( Slow.class ) != null
				|| defender.buff( Chill.class ) != null
				|| defender.buff( Frost.class ) != null;
		boolean stunnedOrParalyzed = defender.buff( Paralysis.class ) != null;
		boolean weakened = defender.buff( Weakness.class ) != null;

		int bonus = 0;
		if (bleeding && hasRarityStat( RarityStat.Type.CRIMSON_ECHO )) {
			bonus += legendaryComboBonus( damage, RarityStat.Type.CRIMSON_ECHO );
		}
		if (slowedOrFrozen && hasRarityStat( RarityStat.Type.GLACIAL_REND )) {
			bonus += legendaryComboBonus( damage, RarityStat.Type.GLACIAL_REND );
		}
		if (stunnedOrParalyzed && hasRarityStat( RarityStat.Type.STATIC_RUIN )) {
			bonus += legendaryComboBonus( damage, RarityStat.Type.STATIC_RUIN );
		}
		if (weakened && hasRarityStat( RarityStat.Type.SPIRITBREAK )) {
			bonus += legendaryComboBonus( damage, RarityStat.Type.SPIRITBREAK );
		}

		if (hasRarityStat( RarityStat.Type.FATAL_SYNCHRONICITY )) {
			int debuffCount = 0;
			if (bleeding) debuffCount++;
			if (slowedOrFrozen) debuffCount++;
			if (stunnedOrParalyzed) debuffCount++;
			if (weakened) debuffCount++;
			if (debuffCount > 0) {
				bonus += legendaryComboBonus( damage, RarityStat.Type.FATAL_SYNCHRONICITY, debuffCount );
			}
		}

		return damage + bonus;
	}

	private int legendaryComboBonus( int damage, RarityStat.Type type ) {
		return legendaryComboBonus( damage, type, 1 );
	}

	private int legendaryComboBonus( int damage, RarityStat.Type type, int stacks ) {
		int percent = rarityStat( type );
		return percent > 0 && stacks > 0 ? Math.max( 1, Math.round( damage * percent * stacks / 100f ) ) : 0;
	}

	protected boolean rollRarityProc( RarityStat.Type type ) {
		int chance = rarityStat( type );
		return chance > 0 && Random.Int( 100 ) < chance;
	}

	protected float rarityDuration( float base, RarityStat.Type type ) {
		return base + rarityStat( type );
	}

	private boolean rollOffensiveProc( RarityStat.Type type, Char attacker ) {
		return rollChance( rarityStat( type )
				+ homebaseBonus( attacker, HomebaseState.Training.STATUS_PROC_CHANCE )
				+ homebaseOffensiveProcBonus( type, attacker ) );
	}

	private boolean rollCombatProc( RarityStat.Type type, Char attacker, HomebaseState.Training training ) {
		return rollChance( rarityStat( type ) + homebaseBonus( attacker, training ) );
	}

	private boolean rollChance( int chance ) {
		return chance > 0 && Random.Int( 100 ) < Math.min( 100, chance );
	}

	private float offensiveDuration( float base, RarityStat.Type type, Char attacker ) {
		return (base + offensiveDurationBonus( type, attacker )) * statusDurationMultiplier( attacker );
	}

	private int offensiveDurationBonus( RarityStat.Type type, Char attacker ) {
		return rarityStat( type ) + homebaseOffensiveDurationBonus( type, attacker );
	}

	private float statusDurationMultiplier( Char attacker ) {
		return 1f + homebaseBonus( attacker, HomebaseState.Training.STATUS_DURATION ) / 100f;
	}

	private int homebaseOffensiveProcBonus( RarityStat.Type type, Char attacker ) {
		switch (type) {
			case BLEED_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.BLEED_PROC );
			case BURNING_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.BURNING_PROC );
			case POISON_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.POISON_PROC );
			case CORROSION_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.CORROSION_PROC );
			case FROST_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.FROST_PROC );
			case CRIPPLE_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.CRIPPLE_PROC );
			case DAZE_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.DAZE_PROC );
			case HEX_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.HEX_PROC );
			case VULNERABLE_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.VULNERABLE_PROC );
			case BLINDNESS_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.BLINDNESS_PROC );
			case ROOT_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.ROOT_PROC );
			case SLOW_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.SLOW_PROC );
			case VERTIGO_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.VERTIGO_PROC );
			case STUN_CHANCE:
				return homebaseBonus( attacker, HomebaseState.Training.STUN_CHANCE );
			case WEAKNESS_PROC:
				return homebaseBonus( attacker, HomebaseState.Training.WEAKNESS_PROC );
			default:
				return 0;
		}
	}

	private int homebaseOffensiveDurationBonus( RarityStat.Type type, Char attacker ) {
		switch (type) {
			case BLEED_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.BLEED_DURATION );
			case BURNING_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.BURNING_DURATION );
			case POISON_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.POISON_DURATION );
			case CORROSION_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.CORROSION_DURATION );
			case FROST_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.FROST_DURATION );
			case CRIPPLE_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.CRIPPLE_DURATION );
			case DAZE_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.DAZE_DURATION );
			case HEX_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.HEX_DURATION );
			case VULNERABLE_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.VULNERABLE_DURATION );
			case BLINDNESS_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.BLINDNESS_DURATION );
			case ROOT_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.ROOT_DURATION );
			case SLOW_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.SLOW_DURATION );
			case VERTIGO_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.VERTIGO_DURATION );
			case STUN_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.STUN_DURATION );
			case WEAKNESS_DURATION:
				return homebaseBonus( attacker, HomebaseState.Training.WEAKNESS_DURATION );
			default:
				return 0;
		}
	}

	private int homebaseBonus( Char attacker, HomebaseState.Training training ) {
		return attacker instanceof Hero && Dungeon.homebase != null ? Dungeon.homebase.trainingBonus( training ) : 0;
	}

	private void knockback( Char attacker, Char defender ) {
		if (attacker.pos == defender.pos) return;
		int power = Math.max( 1, 1 + rarityStat( RarityStat.Type.KNOCKBACK_STRENGTH )
				+ homebaseBonus( attacker, HomebaseState.Training.KNOCKBACK_STRENGTH ) );
		Ballistica trajectory = new Ballistica( attacker.pos, defender.pos, Ballistica.STOP_TARGET );
		if (trajectory.path.size() <= 1) return;
		trajectory = new Ballistica( trajectory.collisionPos, trajectory.path.get( trajectory.path.size() - 1 ), Ballistica.PROJECTILE );
		WandOfBlastWave.throwChar( defender, trajectory, power, true, false, this );
	}

	private void cleave( Char attacker, Char defender, int damage ) {
		Char target = null;
		int candidates = 0;
		for (int n : PathFinder.NEIGHBOURS8) {
			Char ch = Actor.findChar( defender.pos + n );
			if (ch == null || ch == attacker || ch == defender || ch.alignment == attacker.alignment) continue;
			candidates++;
			if (Random.Int( candidates ) == 0) {
				target = ch;
			}
		}
		if (target != null) {
			target.damage( Math.max( 1, Math.round( damage * 0.5f ) ), this );
		}
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(hitSound, 1, pitch * hitSoundPitch);
	}
	
}
