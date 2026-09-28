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


import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicImmune;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Preparation;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Regeneration;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfInvisibility;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfLevitation;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEnergy;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class CloakOfShadows extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_CLOAK;

		exp = 0;
		levelCap = 10;

		charge = level()+3;
		partialCharge = 0;
		chargeCap = level()+3;

		defaultAction = AC_STEALTH;

		unique = true;
		bones = false;
	}

	public static final String AC_STEALTH = "STEALTH";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ((isEquipped( hero ) || hero.hasTalent(Talent.LIGHT_CLOAK))
				&& !cursed
				&& hero.buff(MagicImmune.class) == null
				&& (charge > 0 || activeBuff != null)) {
			actions.add(AC_STEALTH);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals( AC_STEALTH )) {

			if (activeBuff == null){
				if (!isEquipped(hero) && !hero.hasTalent(Talent.LIGHT_CLOAK)) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				else if (cursed)       GLog.i( Messages.get(this, "cursed") );
				else if (charge <= 0)  GLog.i( Messages.get(this, "no_charge") );
				else {
					hero.spend( 1f );
					hero.busy();
					Sample.INSTANCE.play(Assets.Sounds.MELD);
					activeBuff = activeBuff();
					activeBuff.attachTo(hero);
					Talent.onArtifactUsed(Dungeon.hero);
					hero.sprite.operate(hero.pos);
				}
			} else {
				activeBuff.detach();
				activeBuff = null;
				if (hero.invisible <= 0 && hero.buff(Preparation.class) != null){
					hero.buff(Preparation.class).detach();
				}
				hero.sprite.operate( hero.pos );
			}

		}
	}

	@Override
	public void activate(Char ch){
		super.activate(ch);
		if (activeBuff != null && activeBuff.target == null){
			activeBuff.attachTo(ch);
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			if (!collect || !hero.hasTalent(Talent.LIGHT_CLOAK)){
				if (activeBuff != null){
					activeBuff.detach();
					activeBuff = null;
				}
			} else {
				activate(hero);
			}

			return true;
		} else
			return false;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect(container)){
			if (container.owner instanceof Hero
					&& passiveBuff == null
					&& ((Hero) container.owner).hasTalent(Talent.LIGHT_CLOAK)){
				activate((Hero) container.owner);
			}
			return true;
		} else{
			return false;
		}
	}

	@Override
	protected void onDetach() {
		if (passiveBuff != null){
			passiveBuff.detach();
			passiveBuff = null;
		}
		if (activeBuff != null && !isEquipped((Hero) activeBuff.target)){
			activeBuff.detach();
			activeBuff = null;
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new cloakRecharge();
	}

	@Override
	protected ArtifactBuff activeBuff( ) {
		return new cloakStealth();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		if (charge < chargeCap) {
			if (!isEquipped(target)) amount *= 0.75f*target.pointsInTalent(Talent.LIGHT_CLOAK)/3f;
			partialCharge += 0.25f*amount;
			while (partialCharge >= 1f) {
				charge++;
				partialCharge--;
			}
			if (charge >= chargeCap){
				partialCharge = 0;
				charge = chargeCap;
			}
			updateQuickslot();
		}
	}

	public void directCharge(int amount){
		charge = Math.min(charge+amount, chargeCap);
		updateQuickslot();
	}

	@Override
	protected void syncChargeCapToLevel() {
		setChargeCapKeepingCharge( level()+3 );
	}
	
	@Override
	public Item upgrade() {
		return super.upgrade();
	}

	public static int etherealLevel( Hero hero ) {
		if (hero == null) return 0;
		cloakStealth stealth = hero.buff( cloakStealth.class );
		return stealth == null ? 0 : stealth.cloakLevel();
	}

	public static int wallPhaseDestination( Hero hero, int wall ) {
		if (etherealLevel( hero ) < 30 || Dungeon.level == null || !Dungeon.level.solid[wall]) return -1;
		int destination = wall + (wall - hero.pos);
		if (!Dungeon.level.insideMap( destination ) || Actor.findChar( destination ) != null) return -1;
		return Dungeon.level.passable[destination] || Dungeon.level.avoid[destination] ? destination : -1;
	}

	public static boolean phaseThroughEnemy( Hero hero, Char enemy ) {
		int cloakLevel = etherealLevel( hero );
		if (cloakLevel < 20 || enemy == null || !Dungeon.level.adjacent( hero.pos, enemy.pos )) return false;
		int destination = enemy.pos + (enemy.pos - hero.pos);
		if (!Dungeon.level.insideMap( destination ) || Actor.findChar( destination ) != null
				|| !(Dungeon.level.passable[destination] || Dungeon.level.avoid[destination])) return false;

		enemy.damage( Math.max( 1, cloakLevel / 2 ), hero.buff( cloakStealth.class ) );
		hero.sprite.move( hero.pos, destination );
		hero.move( destination );
		hero.spend( 1f / hero.speed() );
		Dungeon.observe();
		GameScene.updateFog();
		return true;
	}

	private int requiredMaterialCount( int nextVisibleLevel, int startLevel ) {
		return nextVisibleLevel < startLevel ? 0 : 1 + (nextVisibleLevel - startLevel) / 5;
	}

	private int itemCount( Hero hero, Class<? extends Item> type ) {
		int count = 0;
		for (Item item : hero.belongings.getAllItems( type )) count += item.quantity();
		return count;
	}

	private void consumeItems( Hero hero, Class<? extends Item> type, int amount ) {
		for (Item item : new ArrayList<Item>( hero.belongings.getAllItems( type ) )) {
			while (amount > 0 && item.quantity() > 0) {
				item.detach( hero.belongings.backpack );
				amount--;
			}
			if (amount <= 0) return;
		}
	}

	private boolean consumeUpgradeMaterials( Hero hero ) {
		int next = visiblyUpgraded() + 1;
		if (next <= 10) return true;
		int invisibility = requiredMaterialCount( next, 11 );
		int mapping = requiredMaterialCount( next, 30 );
		int levitation = requiredMaterialCount( next, 40 );
		if (itemCount( hero, PotionOfInvisibility.class ) < invisibility
				|| itemCount( hero, ScrollOfMagicMapping.class ) < mapping
				|| itemCount( hero, PotionOfLevitation.class ) < levitation) {
			GLog.w( "The Cloak needs " + invisibility + " Potion(s) of Invisibility"
					+ (mapping > 0 ? ", " + mapping + " Scroll(s) of Magic Mapping" : "")
					+ (levitation > 0 ? ", and " + levitation + " Potion(s) of Levitation" : "")
					+ " for its next level." );
			return false;
		}
		consumeItems( hero, PotionOfInvisibility.class, invisibility );
		consumeItems( hero, ScrollOfMagicMapping.class, mapping );
		consumeItems( hero, PotionOfLevitation.class, levitation );
		return true;
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (visiblyUpgraded() >= 20) desc += "\n\nAt _+20_, Ethereal stealth _lasts twice as long_ and can _phase through enemies_, damaging them.";
		if (visiblyUpgraded() >= 30) desc += " At _+30_ it can _phase through a single wall_ into a clear space.";
		if (visiblyUpgraded() >= 40) desc += " At _+40_ the wearer _flies_ while the Cloak is active.";
		if (visiblyUpgraded() >= 10) desc += "\n\nFurther levels require _increasing quantities of Potions of Invisibility_;"
				+ " _Scrolls of Magic Mapping_ join the cost at _+30_, and _Potions of Levitation_ at _+40_.";
		return desc;
	}

	private static final String STEALTHED = "stealthed";
	private static final String BUFF = "buff";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		if (activeBuff != null) bundle.put(BUFF, activeBuff);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BUFF)){
			activeBuff = new cloakStealth();
			activeBuff.restoreFromBundle(bundle.getBundle(BUFF));
		}
	}

	@Override
	public int value() {
		return 0;
	}

	public class cloakRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
				if (activeBuff == null && Regeneration.regenOn()) {
					float missing = (chargeCap - charge);
					if (level() > 7) missing += 5*(level() - 7)/3f;
					float chargeToGain = artifactChargeGain( target, 45 - missing, 15f );
					if (!isEquipped(Dungeon.hero)){
						chargeToGain *= 0.75f*Dungeon.hero.pointsInTalent(Talent.LIGHT_CLOAK)/3f;
					}
					partialCharge += chargeToGain;
				}

				while (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge == chargeCap){
						partialCharge = 0;
					}

				}
			} else {
				partialCharge = 0;
			}

			if (cooldown > 0)
				cooldown --;

			updateQuickslot();

			spend( TICK );

			return true;
		}

	}

	public class cloakStealth extends ArtifactBuff{
		
		{
			type = buffType.POSITIVE;
		}
		
		int turnsToCost = 0;

		@Override
		public int icon() {
			return BuffIndicator.INVISIBLE;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.brightness(0.6f);
		}

		@Override
		public float iconFadePercent() {
			return (4f - turnsToCost) / 4f;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(turnsToCost);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", turnsToCost);
		}

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				target.invisible++;
				if (cloakLevel() >= 40) target.flying = true;
				if (target instanceof Hero && ((Hero) target).subClass == HeroSubClass.ASSASSIN){
					Buff.affect(target, Preparation.class);
				}
				if (target instanceof Hero && ((Hero) target).hasTalent(Talent.PROTECTIVE_SHADOWS)){
					Buff.affect(target, Talent.ProtectiveShadowsTracker.class);
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean act(){
			turnsToCost--;
			
			if (turnsToCost <= 0){
				charge--;
				if (charge < 0) {
					charge = 0;
					detach();
					GLog.w(Messages.get(this, "no_charge"));
					((Hero) target).interrupt();
				} else {
					//target hero level is 1 + 2*cloak level
					int lvlDiffFromTarget = ((Hero) target).lvl - (1+level()*2);
					//plus an extra one for each level after 6
					if (level() >= 7){
						lvlDiffFromTarget -= level()-6;
					}
					if (lvlDiffFromTarget >= 0){
						exp += Math.round(10f * Math.pow(1.1f, lvlDiffFromTarget));
					} else {
						exp += Math.round(10f * Math.pow(0.75f, -lvlDiffFromTarget));
					}
					
					if (exp >= (trueLevel() + 1) * 50 && canGainArtifactLevel()
							&& consumeUpgradeMaterials( (Hero)target )) {
						upgrade();
						Catalog.countUse(CloakOfShadows.class);
						exp -= trueLevel() * 50;
						GLog.p(Messages.get(this, "levelup"));
						
					}
					turnsToCost = cloakLevel() >= 20 ? 8 : 4;
				}
				updateQuickslot();
			}

			spend( TICK );

			return true;
		}

		public void dispel(){
			if (turnsToCost <= 0 && charge > 0){
				charge--;
			}
			updateQuickslot();
			detach();
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add( CharSprite.State.INVISIBLE );
			else if (target.invisible == 0) target.sprite.remove( CharSprite.State.INVISIBLE );
		}

		@Override
		public void detach() {
			activeBuff = null;

			if (target.invisible > 0)   target.invisible--;
			if (cloakLevel() >= 40 && target.buff(com.erebus.reclaimedpixeldungeon.actors.buffs.Levitation.class) == null) {
				target.flying = false;
			}

			updateQuickslot();
			super.detach();
		}

		private int cloakLevel() {
			return CloakOfShadows.this.visiblyUpgraded();
		}
		
		private static final String TURNSTOCOST = "turnsToCost";
		private static final String BARRIER_INC = "barrier_inc";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			
			bundle.put( TURNSTOCOST , turnsToCost);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			
			turnsToCost = bundle.getInt( TURNSTOCOST );
		}
	}
}
