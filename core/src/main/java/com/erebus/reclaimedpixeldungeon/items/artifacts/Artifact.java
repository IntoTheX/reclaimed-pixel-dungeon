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

package com.erebus.reclaimedpixeldungeon.items.artifacts;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicImmune;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.GuidingLight;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemPreviewContext;
import com.erebus.reclaimedpixeldungeon.items.KindofMisc;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEnergy;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Artifact extends KindofMisc {

	protected Buff passiveBuff;
	protected Buff activeBuff;

	//level is used internally to track upgrades to artifacts, size/logic varies per artifact.
	//already inherited from item superclass
	//exp is used to count progress towards levels for some artifacts
	protected int exp = 0;
	//levelCap is the artifact's maximum level
	protected int levelCap = 0;

	//the current artifact charge
	protected int charge = 0;
	//the build towards next charge, usually rolls over at 1.
	//better to keep charge as an int and use a separate float than casting.
	protected float partialCharge = 0;
	//the maximum charge, varies per artifact, not all artifacts use this.
	protected int chargeCap = 0;
	private static final float MIN_ARTIFACT_CHARGE_TURNS = 10f;

	//used by some artifacts to keep track of duration of effects or cooldowns to use.
	protected int cooldown = 0;

	private boolean restoring;

	@Override
	public boolean doEquip( final Hero hero ) {

		if (hero.belongings.hasEquippedMiscClass( getClass() )){

			GLog.w( Messages.get(Artifact.class, "cannot_wear_two") );
			return false;

		} else {

			if (super.doEquip( hero )){

				identify();
				return true;

			} else {

				return false;

			}

		}

	}

	public void activate( Char ch ) {
		if (passiveBuff != null){
			if (passiveBuff.target != null) passiveBuff.detach();
			passiveBuff = null;
		}
		passiveBuff = passiveBuff();
		passiveBuff.attachTo(ch);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			if (passiveBuff != null) {
				if (passiveBuff.target != null) passiveBuff.detach();
				passiveBuff = null;
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isUpgradable() {
		return levelCap > 0 && trueLevel() >= levelCap;
	}

	@Override
	public Item upgrade() {
		int oldLevel = trueLevel();
		Item result = super.upgrade();
		syncChargeCapToLevel();
		rollRarityStatsForArtifactLevels( oldLevel, trueLevel() );
		return result;
	}

	@Override
	public void level( int value ) {
		int oldLevel = trueLevel();
		super.level( value );
		syncChargeCapToLevel();
		rollRarityStatsForArtifactLevels( oldLevel, trueLevel() );
	}

	protected void rollRarityStatsForArtifactLevels( int oldLevel, int newLevel ) {
		if (restoring || newLevel <= oldLevel) return;
		if (Dungeon.hero != null && isEquipped( Dungeon.hero )) {
			Dungeon.increaseMobLevelPressure( newLevel - oldLevel );
		}

		int rolls = rarityUpgradeRollsForArtifactLevels( oldLevel, newLevel );
		if (rolls > 0 && improveRarityStatsFromUpgrade( rolls )) {
			GLog.p( Messages.capitalize( name() ) + "'s rarity stats improve!" );
		}
	}

	protected int rarityUpgradeRollsForArtifactLevels( int oldLevel, int newLevel ) {
		if (levelCap <= 0) return Math.max( 0, newLevel - oldLevel );

		oldLevel = Math.max( 0, oldLevel );
		newLevel = Math.max( 0, newLevel );
		return Math.max( 0,
				Math.round( (newLevel * 10) / (float)levelCap )
						- Math.round( (oldLevel * 10) / (float)levelCap ) );
	}

	protected boolean canGainArtifactLevel() {
		return true;
	}

	protected int artifactLevelsRemaining() {
		return 999;
	}

	protected void syncChargeCapToLevel() {
		// Subclasses whose charge capacity scales with uncapped artifact levels override this.
	}

	protected void setChargeCapKeepingCharge( int newChargeCap ) {
		chargeCap = Math.max( 0, newChargeCap );
		if (charge > chargeCap) {
			charge = chargeCap;
			partialCharge = 0;
		}
		sanitizePartialCharge();
	}

	protected void sanitizePartialCharge() {
		if (Float.isNaN( partialCharge ) || Float.isInfinite( partialCharge ) || partialCharge < 0) {
			partialCharge = 0;
		}
	}

	protected float artifactChargeGain( Char target, float turnsToCharge ) {
		return artifactChargeGain( target, turnsToCharge, MIN_ARTIFACT_CHARGE_TURNS );
	}

	protected float artifactChargeGain( Char target, float turnsToCharge, float minTurnsToCharge ) {
		sanitizePartialCharge();
		if (Float.isNaN( turnsToCharge ) || Float.isInfinite( turnsToCharge )) {
			return 0;
		}
		return RingOfEnergy.artifactChargeMultiplier( target ) / Math.max( minTurnsToCharge, turnsToCharge );
	}

	protected float artifactChargeGain( float turnsToCharge ) {
		return artifactChargeGain( turnsToCharge, MIN_ARTIFACT_CHARGE_TURNS );
	}

	protected float artifactChargeGain( float turnsToCharge, float minTurnsToCharge ) {
		sanitizePartialCharge();
		if (Float.isNaN( turnsToCharge ) || Float.isInfinite( turnsToCharge )) {
			return 0;
		}
		return 1f / Math.max( minTurnsToCharge, turnsToCharge );
	}

	@Override
	public int level() {
		int homebasePotency = Dungeon.homebase == null ? 0 : Dungeon.homebase.trainingBonus( HomebaseState.Training.ARTIFACT_POTENCY );
		homebasePotency = ItemPreviewContext.artifactPotency( this, homebasePotency );
		return Math.max( 0, super.level() + rarityStat( RarityStat.Type.ARTIFACT_POTENCY ) + homebasePotency );
	}

	@Override
	protected void onRarityStatsChanged() {
		super.onRarityStatsChanged();
		syncChargeCapToLevel();
		if (Dungeon.hero != null && isEquipped( Dungeon.hero )) {
			activate( Dungeon.hero );
		}
	}

	@Override
	public int visiblyUpgraded() {
		return levelKnown && levelCap > 0 ? Math.round((level()*10)/(float)levelCap): 0;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return visiblyUpgraded();
	}

	@Override
	public int buffedLvl() {
		//level isn't affected by buffs/debuffs
		return level();
	}

	//transfers upgrades from another artifact, transfer level will equal the displayed level
	public void transferUpgrade(int transferLvl) {
		upgrade(Math.round((transferLvl*levelCap)/10f));
	}

	public void resetForTrinity(int visibleLevel){
		restoring = true;
		try {
			level(Math.round((visibleLevel*levelCap)/10f));
		} finally {
			restoring = false;
		}
		exp = Integer.MIN_VALUE; //ensures no levelling
		charge = chargeCap;
		cooldown = 0;
	}

	public static void artifactProc(Char target, int artifLevel, int chargesUsed){
		if (Dungeon.hero.subClass == HeroSubClass.PRIEST && target.buff(GuidingLight.Illuminated.class) != null) {
			target.buff(GuidingLight.Illuminated.class).detach();
			target.damage(5+Dungeon.hero.lvl, GuidingLight.INSTANCE);
		}

		if (target.alignment != Char.Alignment.ALLY
				&& Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.SEARING_LIGHT)
				&& Dungeon.hero.buff(Talent.SearingLightCooldown.class) == null){
			Buff.affect(target, GuidingLight.Illuminated.class);
			Buff.affect(Dungeon.hero, Talent.SearingLightCooldown.class, 20f);
		}

		if (target.alignment != Char.Alignment.ALLY
				&& Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.SUNRAY)){
			// 15/25% chance
			if (Random.Int(20) < 1 + 2*Dungeon.hero.pointsInTalent(Talent.SUNRAY)){
				Buff.prolong(target, Blindness.class, 4f);
			}
		}
	}

	@Override
	public String info() {
		if (cursed && cursedKnown && !isEquipped( Dungeon.hero )) {
			return super.info() + "\n\n" + Messages.get(Artifact.class, "curse_known");
			
		} else if (!isIdentified() && cursedKnown && !isEquipped( Dungeon.hero)) {
			return super.info() + "\n\n" + Messages.get(Artifact.class, "not_cursed");
			
		} else {
			return super.info();
			
		}
	}

	@Override
	public String status() {
		
		//if the artifact isn't IDed, or is cursed, don't display anything
		if (!isIdentified() || cursed){
			return null;
		}

		//display the current cooldown
		if (cooldown != 0)
			return Messages.format( "%d", cooldown );

		//display as percent
		if (chargeCap == 100)
			return Messages.format( "%d%%", charge );

		//display as #/#
		if (chargeCap > 0)
			return Messages.format( "%d/%d", charge, chargeCap );

		//if there's no cap -
		//- but there is charge anyway, display that charge
		if (charge != 0)
			return Messages.format( "%d", charge );

		//otherwise, if there's no charge, return null.
		return null;
	}

	@Override
	public Item random() {
		//always +0
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}
		return this;
	}

	@Override
	public int value() {
		int price = 100;
		if (level() > 0)
			price += 20*visiblyUpgraded();
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}


	protected ArtifactBuff passiveBuff() {
		return null;
	}

	protected ArtifactBuff activeBuff() {return null; }
	
	public void charge(Hero target, float amount){
		//do nothing by default;
	}

	public class ArtifactBuff extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (target instanceof Hero && Dungeon.hero == null && cooldown() == 0 && target.cooldown() > 0) {
					spend(TICK);
				}
				return true;
			}
			return false;
		}

		public int itemLevel() {
			return level();
		}

		public boolean isCursed() {
			return target.buff(MagicImmune.class) == null && cursed;
		}

		public void charge(Hero target, float amount){
			Artifact.this.charge(target, amount);
		}

	}
	
	private static final String EXP = "exp";
	private static final String CHARGE = "charge";
	private static final String PARTIALCHARGE = "partialcharge";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( EXP , exp );
		bundle.put( CHARGE , charge );
		bundle.put( PARTIALCHARGE , partialCharge );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		restoring = true;
		try {
			super.restoreFromBundle(bundle);
		} finally {
			restoring = false;
		}
		syncChargeCapToLevel();
		exp = bundle.getInt( EXP );
		if (chargeCap > 0)  charge = Math.min( chargeCap, bundle.getInt( CHARGE ));
		else                charge = bundle.getInt( CHARGE );
		partialCharge = bundle.getFloat( PARTIALCHARGE );
		sanitizePartialCharge();
	}
}
