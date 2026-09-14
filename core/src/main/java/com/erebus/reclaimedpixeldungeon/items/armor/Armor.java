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

package com.erebus.reclaimedpixeldungeon.items.armor;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Challenges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barkskin;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barrier;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bless;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Haste;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicImmune;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Momentum;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Recharging;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.BodyForm;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.HolyWard;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.LifeLinkSpell;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.items.BrokenSeal;
import com.erebus.reclaimedpixeldungeon.items.EquipableItem;
import com.erebus.reclaimedpixeldungeon.items.EnchantmentSlots;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.AntiEntropy;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Bulk;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Corrosion;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Displacement;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Metabolism;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Multiplicity;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Overgrowth;
import com.erebus.reclaimedpixeldungeon.items.armor.curses.Stench;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Affection;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.AntiMagic;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Brimstone;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Camouflage;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Entanglement;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Flow;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Obfuscation;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Potential;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Repulsion;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Stone;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Swiftness;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Thorns;
import com.erebus.reclaimedpixeldungeon.items.armor.glyphs.Viscosity;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfArcana;
import com.erebus.reclaimedpixeldungeon.items.trinkets.ParchmentScrap;
import com.erebus.reclaimedpixeldungeon.items.trinkets.ShardOfOblivion;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class Armor extends EquipableItem {

	protected static final String AC_DETACH       = "DETACH";
	private static final int MAX_SAFE_ARMOR = 1_000_000_000;
	
	public enum Augment {
		EVASION (2f , -1f),
		DEFENSE (-2f, 1f),
		NONE	(0f   ,  0f);
		
		private float evasionFactor;
		private float defenceFactor;
		
		Augment(float eva, float df){
			evasionFactor = eva;
			defenceFactor = df;
		}
		
		public int evasionFactor(int level){
			return Math.round((2 + level) * evasionFactor);
		}
		
		public int defenseFactor(int level){
			return Math.round((2 + level) * defenceFactor);
		}
	}
	
	public Augment augment = Augment.NONE;
	
	public Glyph glyph;
	private final Glyph[] glyphSlots = new Glyph[EnchantmentSlots.MAX_SLOTS];
	public boolean glyphHardened = false;
	public boolean curseInfusionBonus = false;
	public boolean masteryPotionBonus = false;
	
	protected BrokenSeal seal;
	
	public int tier;
	
	private static final int USES_TO_ID = 10;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;
	
	public Armor( int tier ) {
		this.tier = tier;
	}
	
	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String GLYPH			= "glyph";
	private static final String GLYPH_SLOT		= "glyph_slot_";
	private static final String GLYPH_HARDENED	= "glyph_hardened";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String SEAL            = "seal";
	private static final String AUGMENT			= "augment";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		syncPrimaryGlyph();
		bundle.put( GLYPH, glyph );
		for (int i = 1; i < EnchantmentSlots.MAX_SLOTS; i++) bundle.put( GLYPH_SLOT + i, glyphSlots[i] );
		bundle.put( GLYPH_HARDENED, glyphHardened );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( SEAL, seal);
		bundle.put( AUGMENT, augment);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		usesLeftToID = bundle.getInt( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getInt( AVAILABLE_USES );
		inscribe((Glyph) bundle.get(GLYPH));
		for (int i = 1; i < EnchantmentSlots.MAX_SLOTS; i++) glyphSlots[i] = (Glyph)bundle.get( GLYPH_SLOT + i );
		glyphHardened = bundle.getBoolean(GLYPH_HARDENED);
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );
		seal = (BrokenSeal)bundle.get(SEAL);
		
		augment = bundle.getEnum(AUGMENT, Augment.class);
	}

	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
		//armor can be kept in bones between runs, the seal cannot.
		seal = null;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (seal != null) actions.add(AC_DETACH);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_DETACH) && seal != null){
			BrokenSeal detaching = detachSeal();
			GLog.i( Messages.get(Armor.class, "detach_seal") );
			hero.sprite.operate(hero.pos);
			if (!detaching.collect()){
				Dungeon.level.drop(detaching, hero.pos);
			}
			updateQuickslot();
		}
	}

	@Override
	public boolean collect(Bag container) {
		if(super.collect(container)){
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified()){
				for (Glyph effect : glyphs()) {
					Catalog.setSeen(effect.getClass());
					Statistics.itemTypesDiscovered.add(effect.getClass());
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item identify(boolean byHero) {
		if (byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			for (Glyph effect : glyphs()) {
				Catalog.setSeen(effect.getClass());
				Statistics.itemTypesDiscovered.add(effect.getClass());
			}
		}
		return super.identify(byHero);
	}

	public void setIDReady(){
		usesLeftToID = -1;
	}

	public boolean readyToIdentify(){
		return !isIdentified() && usesLeftToID <= 0;
	}

	@Override
	public boolean doEquip( Hero hero ) {

		// 15/25% chance
		if (hero.heroClass != HeroClass.CLERIC && hero.hasTalent(Talent.HOLY_INTUITION)
				&& cursed && !cursedKnown
				&& Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
			cursedKnown = true;
			GLog.p(Messages.get(this, "curse_detected"));
			return false;
		}

		detach(hero.belongings.backpack);

		Armor oldArmor = hero.belongings.armor;
		if (hero.belongings.armor == null || hero.belongings.armor.doUnequip( hero, true, false )) {
			
			hero.belongings.armor = this;
			
			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(Armor.class, "equip_cursed") );
			}
			
			((HeroSprite)hero.sprite).updateArmor();
			activate(hero);
			Talent.onItemEquipped(hero, this);
			hero.updateHT( false );
			hero.spend( timeToEquip( hero ) );

			if (Dungeon.hero.heroClass == HeroClass.WARRIOR && checkSeal() == null){
				BrokenSeal seal = oldArmor != null ? oldArmor.checkSeal() : null;
				if (seal != null && (!cursed || (seal.getGlyph() != null && seal.getGlyph().curse()))){

					GameScene.show(new WndOptions(new ItemSprite(ItemSpriteSheet.SEAL),
							Messages.titleCase(seal.trueName()),
							Messages.get(Armor.class, "seal_transfer"),
							Messages.get(Armor.class, "seal_transfer_yes"),
							Messages.get(Armor.class, "seal_transfer_no")){
						@Override
						protected void onSelect(int index) {
							super.onSelect(index);
							if (index == 0){
								seal.affixToArmor(Armor.this, oldArmor);
								updateQuickslot();
							}
							super.hide();
						}

						@Override
						public void hide() {
							//do nothing, must press button
						}
					});
				} else {
					hero.next();
				}
			} else {
				hero.next();
			}
			return true;
			
		} else {
			
			collect( hero.belongings.backpack );
			return false;
			
		}
	}

	@Override
	public void activate(Char ch) {
		if (seal != null) Buff.affect(ch, BrokenSeal.WarriorShield.class).setArmor(this);
	}

	public void affixSeal(BrokenSeal seal){
		this.seal = seal;
		if (seal.level() > 0){
			//doesn't trigger upgrading logic such as affecting curses/glyphs
			int newLevel = trueLevel()+1;
			level(newLevel);
			Badges.validateItemLevelAquired(this);
		}
		if (seal.getGlyph() != null){
			inscribe(seal.getGlyph());
		}
		if (isEquipped(Dungeon.hero)){
			Buff.affect(Dungeon.hero, BrokenSeal.WarriorShield.class).setArmor(this);
		}
	}

	public BrokenSeal detachSeal(){
		if (seal != null){

			if (isEquipped(Dungeon.hero)) {
				BrokenSeal.WarriorShield sealBuff = Dungeon.hero.buff(BrokenSeal.WarriorShield.class);
				if (sealBuff != null) sealBuff.setArmor(null);
			}

			BrokenSeal detaching = seal;
			seal = null;

			if (detaching.level() > 0){
				degrade();
			}
			if (detaching.canTransferGlyph()){
				inscribe(null);
			} else {
				detaching.setGlyph(null);
			}
			return detaching;
		} else {
			return null;
		}
	}

	public BrokenSeal checkSeal(){
		return seal;
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			hero.belongings.armor = null;
			((HeroSprite)hero.sprite).updateArmor();
			hero.updateHT( false );

			BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
			if (sealBuff != null) sealBuff.setArmor(null);

			return true;

		} else {

			return false;

		}
	}
	
	@Override
	public boolean isEquipped( Hero hero ) {
		return hero != null && hero.belongings.armor() == this;
	}

	public final int DRMax(){
		return DRMax(buffedLvl());
	}

	public int DRMax(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return applyRarityArmorStats( 1 + tier + lvl + augment.defenseFactor(lvl) );
		}

		int max = tier * (2 + lvl) + augment.defenseFactor(lvl);
		if (lvl > max){
			return applyRarityArmorStats( ((lvl - max)+1)/2 );
		} else {
			return applyRarityArmorStats( max );
		}
	}

	public final int DRMin(){
		return DRMin(buffedLvl());
	}

	public int DRMin(int lvl){
		if (Dungeon.isChallenged(Challenges.NO_ARMOR)){
			return applyRarityArmorStats( 0 );
		}

		int max = baseDRMax( lvl );
		if (lvl >= max){
			return applyRarityArmorStats( lvl - max );
		} else {
			return applyRarityArmorStats( lvl );
		}
	}

	private int baseDRMax( int lvl ) {
		int max = tier * (2 + lvl) + augment.defenseFactor(lvl);
		if (lvl > max){
			return ((lvl - max)+1)/2;
		} else {
			return max;
		}
	}

	private int applyRarityArmorStats( int armor ) {
		long base = (long)armor + rarityStat( RarityStat.Type.DEFENSE );
		double scaled = base * (1d + rarityStat( RarityStat.Type.ARMOR_BONUS ) / 100d);
		if (!Double.isFinite( scaled )) return MAX_SAFE_ARMOR;
		return (int)Math.max( 0, Math.min( MAX_SAFE_ARMOR, Math.round( scaled ) ) );
	}

	//This exists so we can test what a char's base evasion would be without armor affecting it
	//more ugly static vars yaaay~
	public static boolean testingNoArmDefSkill = false;
	
	public float evasionFactor( Char owner, float evasion ){
		if (testingNoArmDefSkill) return evasion;
		
		if (hasGlyph(Stone.class, owner) && !Stone.testingEvasion()){
			return 0;
		}
		
		if (owner instanceof Hero){
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) evasion /= Math.pow(1.5, aEnc);
			
			Momentum momentum = owner.buff(Momentum.class);
			if (momentum != null){
				evasion += momentum.evasionBonus(((Hero) owner).lvl, Math.max(0, -aEnc));
			}
		}
		
		evasion += augment.evasionFactor(buffedLvl()) + rarityStat( RarityStat.Type.EVASION );
		int dodgeChance = rarityStat( RarityStat.Type.DODGE_CHANCE );
		if (owner instanceof Hero) {
			if (Char.resolvingHitIsSurpriseAttack()) {
				dodgeChance = 0;
			} else if (Char.resolvingHitIsMagic()) {
				dodgeChance = Math.round( dodgeChance * 0.20f );
			}
		}
		return evasion * (1f + dodgeChance / 100f);
	}
	
	public float speedFactor( Char owner, float speed ){
		
		if (owner instanceof Hero) {
			int aEnc = STRReq() - ((Hero) owner).STR();
			if (aEnc > 0) speed /= Math.pow(1.2, aEnc);
		}
		
		return speed;
		
	}
	
	@Override
	public int level() {
		int level = super.level();
		//TODO warrior's seal upgrade should probably be considered here too
		// instead of being part of true level
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}
	
	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean inscribe ) {

		if (inscribe){
			if (glyphCount() == 0){
				inscribe( Glyph.random() );
			}
		} else if (glyphCount() > 0) {
			//chance to lose harden buff is 10/20/40/80/100% when upgrading from +6/7/8/9/10
			if (glyphHardened) {
				if (level() >= 6 && Random.Float(10) < Math.pow(2, level()-6)){
					glyphHardened = false;
				}

			//chance to remove curse is a static 33%
			} else if (hasCurseGlyph()){
				if (Random.Int(3) == 0) removeRandomGlyph( true );

			//otherwise chance to lose glyph is 10/20/40/80/100% when upgrading from +4/5/6/7/8
			} else {

				//the chance from +4/5, and then +6 can be set to 0% with metamorphed runic transference
				int lossChanceStart = 4;
				if (Dungeon.hero != null && Dungeon.hero.heroClass != HeroClass.WARRIOR && Dungeon.hero.hasTalent(Talent.RUNIC_TRANSFERENCE)){
					lossChanceStart += 1+Dungeon.hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE);
				}

				if (level() >= lossChanceStart && Random.Float(10) < Math.pow(2, level()-4)) {
					removeRandomGlyph( false );
				}
			}
		}
		
		cursed = false;

		if (seal != null && seal.level() == 0)
			seal.upgrade();

		return super.upgrade();
	}
	
	public int proc( Char attacker, Char defender, int damage ) {

		if (defender.buff(MagicImmune.class) == null) {
			Glyph trinityGlyph = null;
			//only when it's the hero or a char that uses the hero's armor
			if (Dungeon.hero.buff(BodyForm.BodyFormBuff.class) != null
					&& (defender == Dungeon.hero || defender instanceof PrismaticImage || defender instanceof ShadowClone.ShadowAlly)){
				trinityGlyph = Dungeon.hero.buff(BodyForm.BodyFormBuff.class).glyph();
				if (trinityGlyph != null && hasStoredGlyph( trinityGlyph.getClass() )){
					trinityGlyph = null;
				}
			}

			if (defender instanceof Hero && isEquipped((Hero) defender)
					&& defender.buff(HolyWard.HolyArmBuff.class) != null){
				for (Glyph effect : glyphs()) {
					if (((Hero)defender).subClass == HeroSubClass.PALADIN || effect.curse()) {
						damage = effect.proc( this, attacker, defender, damage );
					}
				}
				if (trinityGlyph != null){
					damage = trinityGlyph.proc( this, attacker, defender, damage );
				}
				int blocking = ((Hero) defender).subClass == HeroSubClass.PALADIN ? 3 : 1;
				damage -= Math.round(blocking * Glyph.genericProcChanceMultiplier(defender));

			} else {
				for (Glyph effect : glyphs()) damage = effect.proc(this, attacker, defender, damage);
				if (trinityGlyph != null){
					damage = trinityGlyph.proc( this, attacker, defender, damage );
				}
				//so that this effect procs for allies using this armor via aura of protection
				if (defender.alignment == Dungeon.hero.alignment
						&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
						&& (Dungeon.level.distance(defender.pos, Dungeon.hero.pos) <= 2 || defender.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)
						&& Dungeon.hero.buff(HolyWard.HolyArmBuff.class) != null) {
					int blocking = Dungeon.hero.subClass == HeroSubClass.PALADIN ? 3 : 1;
					damage -= Math.round(blocking * Glyph.genericProcChanceMultiplier(defender));
				}
			}
			damage = Math.max(damage, 0);
		}

		damage = applyRarityDefenseProcStats( attacker, defender, damage );
		
		if (!levelKnown && defender == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					if (usesLeftToID > -1){
						GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
					}
					setIDReady();
				} else {
					identify();
					GLog.p(Messages.get(Armor.class, "identify"));
					Badges.validateItemLevelAquired(this);
				}
			}
		}

		return damage;
	}

	private int applyRarityDefenseProcStats( Char attacker, Char defender, int damage ) {
		if (attacker == null || defender == null || damage <= 0) return damage;

		int blockChance = rarityStat( RarityStat.Type.BLOCK_CHANCE );
		if (defender instanceof Hero) {
			if (defender.incomingHitWasSurpriseAttack()) {
				blockChance = 0;
			} else if (defender.incomingHitWasMagic()) {
				blockChance = Math.round( blockChance * 0.25f );
			}
		}
		blockChance = Math.max( 0, blockChance - guardBreak( attacker ) );
		if (Random.Int( 100 ) < Math.max( 0, blockChance )) {
			damage = Math.round( damage * 0.5f );
		}

		if (attacker.isAlive() && Random.Int( 100 ) < rarityStat( RarityStat.Type.THORNS_CHANCE )) {
			attacker.damage( Math.max( 1, rarityStat( RarityStat.Type.THORNS_DAMAGE ) ), this );
		}

		if (rollRarityProc( RarityStat.Type.BARKSKIN_PROC )) {
			Buff.affect( defender, Barkskin.class ).set( Math.max( 1, rarityStat( RarityStat.Type.BARKSKIN_POWER ) ), 1 );
		}

		if (rollRarityProc( RarityStat.Type.BARRIER_PROC )) {
			Buff.affect( defender, Barrier.class ).incShield( Math.max( 1, rarityStat( RarityStat.Type.BARRIER_POWER ) + Math.round( damage * 0.20f ) ) );
		}

		if (rollRarityProc( RarityStat.Type.BLESS_PROC )) {
			Buff.prolong( defender, Bless.class, rarityDuration( 4f, RarityStat.Type.BLESS_DURATION ) );
		}

		if (rollRarityProc( RarityStat.Type.HASTE_PROC )) {
			Buff.prolong( defender, Haste.class, rarityDuration( 3f, RarityStat.Type.HASTE_DURATION ) );
		}

		if (rollRarityProc( RarityStat.Type.RECHARGING_PROC )) {
			Buff.prolong( defender, Recharging.class, rarityDuration( 4f, RarityStat.Type.RECHARGING_DURATION ) );
		}

		return Math.max( 0, damage );
	}

	private int guardBreak( Char attacker ) {
		if (attacker instanceof Hero) {
			return ((Hero)attacker).belongings.equippedRarityStat( RarityStat.Type.GUARD_BREAK );
		}
		if (attacker instanceof Mob) {
			return ((Mob)attacker).rarityStat( RarityStat.Type.GUARD_BREAK );
		}
		return 0;
	}

	private boolean rollRarityProc( RarityStat.Type type ) {
		int chance = rarityStat( type );
		return chance > 0 && Random.Int( 100 ) < chance;
	}

	private float rarityDuration( float base, RarityStat.Type type ) {
		return base + rarityStat( type );
	}
	
	@Override
	public void onHeroGainExp(float levelPercent, Hero hero) {
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}
	
	@Override
	public String name() {
		if (isEquipped(Dungeon.hero) && !hasCurseGlyph() && Dungeon.hero.buff(HolyWard.HolyArmBuff.class) != null
			&& (Dungeon.hero.subClass != HeroSubClass.PALADIN || glyph == null)){
				return Messages.get(HolyWard.class, "glyph_name", super.name());
			} else {
				return glyphCount() == 1 && glyph != null && (cursedKnown || !glyph.curse())
						? glyph.name( super.name() ) : super.name();

		}
	}
	
	@Override
	public String info() {
		String info = super.info();
		
		if (levelKnown) {

			info += "\n\n" + Messages.get(Armor.class, "curr_absorb", tier, DRMin(), DRMax(), STRReq());
			
			if (Dungeon.hero != null && STRReq() > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "too_heavy");
			}
		} else {
			info += "\n\n" + Messages.get(Armor.class, "avg_absorb", tier, DRMin(0), DRMax(0), STRReq(0));

			if (Dungeon.hero != null && STRReq(0) > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "probably_too_heavy");
			}
		}

		switch (augment) {
			case EVASION:
				info += " " + Messages.get(Armor.class, "evasion");
				break;
			case DEFENSE:
				info += " " + Messages.get(Armor.class, "defense");
				break;
			case NONE:
		}

		if (isEquipped(Dungeon.hero) && !hasCurseGlyph() && Dungeon.hero.buff(HolyWard.HolyArmBuff.class) != null
				&& (Dungeon.hero.subClass != HeroSubClass.PALADIN || glyph == null)){
			info += "\n\n" + Messages.capitalize(Messages.get(Armor.class, "inscribed", Messages.get(HolyWard.class, "glyph_name", Messages.get(Glyph.class, "glyph"))));
			info += " " + Messages.get(HolyWard.class, "glyph_desc");
		} else if (!glyphInfo().isEmpty()) {
			info += "\n\n" + glyphInfo();
		} else if (glyphHardened){
			info += "\n\n" + Messages.get(Armor.class, "hardened_no_glyph");
		}
		
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Armor.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Armor.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			if (hasCurseGlyph()) {
				info += "\n\n" + Messages.get(Armor.class, "weak_cursed");
			} else {
				info += "\n\n" + Messages.get(Armor.class, "not_cursed");
			}
		}

		if (seal != null) {
			info += "\n\n" + Messages.get(Armor.class, "seal_attached", seal.maxShield(tier, level()));
		}
		
		return info;
	}

	@Override
	public Emitter emitter() {
		if (seal == null) return super.emitter();
		Emitter emitter = new Emitter();
		emitter.pos(ItemSpriteSheet.film.width(image)/2f + 2f, ItemSpriteSheet.film.height(image)/3f);
		emitter.fillTarget = false;
		emitter.pour(Speck.factory( Speck.RED_LIGHT ), 0.6f);
		return emitter;
	}

	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		level(n);

		//we use a separate RNG here so that variance due to things like parchment scrap
		//does not affect levelgen
		Random.pushGenerator(Random.Long());

			//30% chance to be cursed
			//15% chance to be inscribed
			float effectRoll = Random.Float();
			if (effectRoll < 0.3f * ParchmentScrap.curseChanceMultiplier()) {
				inscribe(Glyph.randomCurse());
				cursed = true;
			} else if (effectRoll >= 1f - (0.15f * ParchmentScrap.enchantChanceMultiplier())){
				int count = EnchantmentSlots.randomNaturalCount();
				for (int i = 0; i < count; i++) inscribe( i, Glyph.random( glyphClasses() ) );
			}

		Random.popGenerator();

		return this;
	}

	public int STRReq(){
		return STRReq(level());
	}

	public int STRReq(int lvl){
		int req = STRReq(tier, lvl);
		if (masteryPotionBonus){
			req -= 2;
		}
		return req;
	}

	protected static int STRReq(int tier, int lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + Math.round(tier * 2)) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}
	
	@Override
	public int value() {
		if (seal != null) return 0;

		int price = 20 * tier;
		if (hasGoodGlyph()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseGlyph())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	public Armor inscribe( Glyph glyph ) {
		return inscribe( 0, glyph );
	}

	public Armor inscribe( int slot, Glyph glyph ) {
		slot = EnchantmentSlots.slotForLevel( slot );
		if (slot == 0 && (glyph == null || !glyph.curse())) curseInfusionBonus = false;
		glyphSlots[slot] = glyph;
		if (slot == 0) this.glyph = glyph;
		updateQuickslot();
		//the hero needs runic transference to actually transfer, but we still attach the glyph here
		// in case they take that talent in the future
		if (seal != null && slot == 0){
			seal.setGlyph(glyph);
		}
		if (glyph != null && isIdentified() && Dungeon.hero != null
				&& Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
			Catalog.setSeen(glyph.getClass());
			Statistics.itemTypesDiscovered.add(glyph.getClass());
		}
		return this;
	}

	public Armor inscribe() {
		return inscribe( 0, Glyph.random( glyphClasses() ) );
	}

	public Armor inscribeRandom( int slot ) {
		return inscribe( slot, Glyph.random( glyphClasses() ) );
	}

	public Glyph glyph( int slot ) {
		syncPrimaryGlyph();
		return glyphSlots[EnchantmentSlots.slotForLevel( slot )];
	}

	public ArrayList<Glyph> glyphs() {
		syncPrimaryGlyph();
		ArrayList<Glyph> result = new ArrayList<>();
		for (Glyph effect : glyphSlots) if (effect != null) result.add( effect );
		return result;
	}

	public int glyphCount() {
		return glyphs().size();
	}

	public void copyGlyphsFrom( Armor source ) {
		for (int i = 0; i < EnchantmentSlots.MAX_SLOTS; i++) inscribe( i, source.glyph(i) );
	}

	public void copySecondaryGlyphsFrom( Armor source ) {
		for (int i = 1; i < EnchantmentSlots.MAX_SLOTS; i++) inscribe( i, source.glyph(i) );
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Glyph>[] glyphClasses() {
		ArrayList<Glyph> effects = glyphs();
		Class<? extends Glyph>[] result = new Class[effects.size()];
		for (int i = 0; i < effects.size(); i++) result[i] = effects.get(i).getClass();
		return result;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Glyph>[] glyphClassesExcept( int slot ) {
		syncPrimaryGlyph();
		ArrayList<Class<? extends Glyph>> result = new ArrayList<>();
		for (int i = 0; i < glyphSlots.length; i++) {
			if (i != slot && glyphSlots[i] != null) result.add( glyphSlots[i].getClass() );
		}
		return result.toArray( new Class[0] );
	}

	public String glyphInfo() {
		ArrayList<Glyph> visible = new ArrayList<>();
		for (Glyph effect : glyphs()) if (cursedKnown || !effect.curse()) visible.add( effect );
		if (visible.isEmpty()) return "";
		if (visible.size() == 1) {
			Glyph effect = visible.get(0);
			String result = Messages.capitalize( Messages.get(Armor.class, "inscribed", effect.name()) );
			if (glyphHardened) result += " " + Messages.get(Armor.class, "glyph_hardened");
			return result + " " + effect.desc();
		}
		StringBuilder result = new StringBuilder( Messages.get(Armor.class, "multiple_glyphs") );
		for (Glyph effect : visible) {
			result.append( "\n_" ).append( Messages.titleCase(effect.name()) ).append( "_: " ).append( effect.desc() );
		}
		if (glyphHardened) result.append( "\n" ).append( Messages.get(Armor.class, "glyph_hardened") );
		return result.toString();
	}

	private void syncPrimaryGlyph() {
		if (glyphSlots[0] != glyph) glyphSlots[0] = glyph;
	}

	private boolean hasStoredGlyph( Class<? extends Glyph> type ) {
		for (Glyph effect : glyphs()) if (effect.getClass() == type) return true;
		return false;
	}

	private void removeRandomGlyph( boolean curse ) {
		syncPrimaryGlyph();
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < glyphSlots.length; i++) {
			if (glyphSlots[i] != null && glyphSlots[i].curse() == curse) candidates.add( i );
		}
		if (!candidates.isEmpty()) inscribe( Random.element(candidates), null );
	}

	public boolean hasGlyph(Class<?extends Glyph> type, Char owner) {
		Glyph stored = storedGlyph( type );
		if (owner.buff(MagicImmune.class) != null) {
			return false;
		} else if (stored != null && !stored.curse()
				&& owner instanceof Hero
				&& isEquipped((Hero) owner)
				&& owner.buff(HolyWard.HolyArmBuff.class) != null
				&& ((Hero) owner).subClass != HeroSubClass.PALADIN){
			return false;
		} else if (owner.buff(BodyForm.BodyFormBuff.class) != null
				&& owner.buff(BodyForm.BodyFormBuff.class).glyph() != null
				&& owner.buff(BodyForm.BodyFormBuff.class).glyph().getClass().equals(type)){
			return true;
		} else if (stored != null) {
			return true;
		} else {
			return false;
		}
	}

	private Glyph storedGlyph( Class<? extends Glyph> type ) {
		for (Glyph effect : glyphs()) if (effect.getClass() == type) return effect;
		return null;
	}

	//these are not used to process specific glyph effects, so magic immune doesn't affect them
	public boolean hasGoodGlyph(){
		for (Glyph effect : glyphs()) if (!effect.curse()) return true;
		return false;
	}

	public boolean hasCurseGlyph(){
		for (Glyph effect : glyphs()) if (effect.curse()) return true;
		return false;
	}

	private static ItemSprite.Glowing HOLY = new ItemSprite.Glowing( 0xFFFF00 );

	@Override
	public ItemSprite.Glowing glowing() {
		if (isEquipped(Dungeon.hero) && !hasCurseGlyph() && Dungeon.hero.buff(HolyWard.HolyArmBuff.class) != null
				&& (Dungeon.hero.subClass != HeroSubClass.PALADIN || glyph == null)){
			return HOLY;
		} else {
			ArrayList<Glyph> visible = new ArrayList<>();
			for (Glyph effect : glyphs()) if (cursedKnown || !effect.curse()) visible.add( effect );
			if (visible.size() == 1) return visible.get(0).glowing();
			if (visible.size() > 1) {
				int[] colors = new int[visible.size()];
				for (int i = 0; i < visible.size(); i++) colors[i] = visible.get(i).glowing().color;
				return new ItemSprite.CyclingGlowing( colors );
			}
			return null;
		}
	}
	
	public static abstract class Glyph implements Bundlable {
		
		public static final Class<?>[] common = new Class<?>[]{
				Obfuscation.class, Swiftness.class, Viscosity.class, Potential.class };

		public static final Class<?>[] uncommon = new Class<?>[]{
				Brimstone.class, Stone.class, Entanglement.class,
				Repulsion.class, Camouflage.class, Flow.class };

		public static final Class<?>[] rare = new Class<?>[]{
				Affection.class, AntiMagic.class, Thorns.class };

		public static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};

		public static final Class<?>[] curses = new Class<?>[]{
				AntiEntropy.class, Corrosion.class, Displacement.class, Metabolism.class,
				Multiplicity.class, Stench.class, Overgrowth.class, Bulk.class
		};
		
		public abstract int proc( Armor armor, Char attacker, Char defender, int damage );

		protected float procChanceMultiplier( Char defender ){
			return genericProcChanceMultiplier( defender );
		}

		public static float genericProcChanceMultiplier( Char defender ){
			float multi = RingOfArcana.enchantPowerMultiplier(defender);

			if (Dungeon.hero.alignment == defender.alignment
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(defender.pos, Dungeon.hero.pos) <= 2 || defender.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)){
				multi += 0.25f + 0.25f*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
			}

			return multi;
		}
		
		public String name() {
			if (!curse())
				return name( Messages.get(this, "glyph") );
			else
				return name( Messages.get(Item.class, "curse"));
		}
		
		public String name( String armorName ) {
			return Messages.get(this, "name", armorName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();

		@SuppressWarnings("unchecked")
		public static Glyph random( Class<? extends Glyph> ... toIgnore ) {
			switch(Random.chances(typeChances)){
				case 0: default:
					return randomCommon( toIgnore );
				case 1:
					return randomUncommon( toIgnore );
				case 2:
					return randomRare( toIgnore );
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Glyph randomCommon( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(common));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Glyph randomUncommon( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(uncommon));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Glyph randomRare( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(rare));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Glyph randomCurse( Class<? extends Glyph> ... toIgnore ){
			ArrayList<Class<?>> glyphs = new ArrayList<>(Arrays.asList(curses));
			glyphs.removeAll(Arrays.asList(toIgnore));
			if (glyphs.isEmpty()) {
				return random();
			} else {
				return (Glyph) Reflection.newInstance(Random.element(glyphs));
			}
		}
		
	}
}
