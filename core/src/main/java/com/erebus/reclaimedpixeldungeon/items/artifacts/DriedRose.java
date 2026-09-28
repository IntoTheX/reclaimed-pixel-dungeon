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
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.blobs.CorrosiveGas;
import com.erebus.reclaimedpixeldungeon.actors.buffs.AllyBuff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.AscensionChallenge;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Invisibility;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicImmune;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Regeneration;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.Stasis;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Wraith;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.Ghost;
import com.erebus.reclaimedpixeldungeon.effects.CellEmitter;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.effects.particles.ShaftParticle;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEnergy;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.erebus.reclaimedpixeldungeon.items.wands.DamageWand;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.levels.VaultLevel;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.scenes.AlchemyScene;
import com.erebus.reclaimedpixeldungeon.scenes.CellSelector;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.GhostSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BossHealthBar;
import com.erebus.reclaimedpixeldungeon.ui.ItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndInfoItem;
import com.erebus.reclaimedpixeldungeon.windows.WndQuest;
import com.erebus.reclaimedpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DriedRose extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_ROSE1;

		levelCap = 10;

		charge = 100;
		chargeCap = 100;

		defaultAction = AC_SUMMON;
	}

	private boolean talkedTo = false;
	private boolean firstSummon = false;
	
	private GhostHero ghost = null;
	private int ghostID = 0;
	
	private MeleeWeapon weapon = null;
	private Armor armor = null;
	private Wand wand = null;
	private Ring ring = null;
	private Artifact ghostArtifact = null;

	public int droppedPetals = 0;
	private int absorbedPetals = 0;

	public static final String AC_SUMMON = "SUMMON";
	public static final String AC_DIRECT = "DIRECT";
	public static final String AC_OUTFIT = "OUTFIT";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (!Ghost.Quest.completed()){
			return actions;
		}
		if (isEquipped( hero )
				&& charge == chargeCap
				&& !cursed
				&& hero.buff(MagicImmune.class) == null
				&& ghostID == 0) {
			actions.add(AC_SUMMON);
		}
		if (ghostID != 0){
			actions.add(AC_DIRECT);
		}
		//cannot outfit a rose that's cursed, unIDed, or in the vault to prevent smuggling exploits
		if (isIdentified() && !cursed && !(Dungeon.level instanceof VaultLevel)){
			actions.add(AC_OUTFIT);
		}
		
		return actions;
	}

	@Override
	public String defaultAction() {
		if (ghost != null){
			return AC_DIRECT;
		} else {
			return AC_SUMMON;
		}
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals(AC_SUMMON)) {

			if (hero.buff(MagicImmune.class) != null) return;

			if (!Ghost.Quest.completed())   GameScene.show(new WndUseItem(null, this));
			else if (ghost != null)         GLog.i( Messages.get(this, "spawned") );
			else if (!isEquipped( hero ))   GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge != chargeCap)   GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                GLog.i( Messages.get(this, "cursed") );
			else {
				ArrayList<Integer> spawnPoints = new ArrayList<>();
				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					ghost = new GhostHero( this );
					ghostID = ghost.id();
					ghost.pos = Random.element(spawnPoints);

					GameScene.add(ghost, 1f);
					Dungeon.level.occupyCell(ghost);
					
					CellEmitter.get(ghost.pos).start( ShaftParticle.FACTORY, 0.3f, 4 );
					CellEmitter.get(ghost.pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );

					hero.spend(1f);
					hero.busy();
					hero.sprite.operate(hero.pos);

					if (!firstSummon) {
						ghost.yell( Messages.get(GhostHero.class, "hello", Messages.titleCase(Dungeon.hero.name())) );
						Sample.INSTANCE.play( Assets.Sounds.GHOST );
						firstSummon = true;
						
					} else {
						if (BossHealthBar.isAssigned()) {
							ghost.sayBoss();
						} else {
							ghost.sayAppeared();
						}
					}

					Invisibility.dispel(hero);
					Talent.onArtifactUsed(hero);
					charge = 0;
					partialCharge = 0;
					updateQuickslot();

				} else
					GLog.i( Messages.get(this, "no_space") );
			}

		} else if (action.equals(AC_DIRECT)){
			if (ghost == null && ghostID != 0){
				findGhost();
			}
			if (ghost != null && ghost != Stasis.getStasisAlly()){
				GameScene.selectCell(ghostDirector);
			}
			
		} else if (action.equals(AC_OUTFIT)){
			GameScene.show( new WndGhostHero(this) );
		}
	}

	private void findGhost(){
		Actor a = Actor.findById(ghostID);
		if (a != null){
			ghost = (GhostHero)a;
		} else {
			if (Stasis.getStasisAlly() instanceof GhostHero){
				ghost = (GhostHero) Stasis.getStasisAlly();
				ghostID = ghost.id();
			} else {
				ghostID = 0;
			}
		}
	}
	
	public int ghostStrength(){
		return 13 + level()/2;
	}

	@Override
	public String desc() {
		if (!Ghost.Quest.completed()
				&& (ShatteredPixelDungeon.scene() instanceof GameScene || ShatteredPixelDungeon.scene() instanceof AlchemyScene)){
			return Messages.get(this, "desc_no_quest");
		}
		
		String desc = super.desc();

		if (isEquipped( Dungeon.hero )){
			if (!cursed){

				if (canGainArtifactLevel())
					desc+= "\n\n" + Messages.get(this, "desc_hint");

			} else {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
		}

		if (weapon != null || armor != null || wand != null || ring != null || ghostArtifact != null) {
			desc += "\n";

			if (weapon != null) {
				desc += "\n" + Messages.get(this, "desc_weapon", Messages.titleCase(weapon.title()));
			}

			if (armor != null) {
				desc += "\n" + Messages.get(this, "desc_armor", Messages.titleCase(armor.title()));
			}
			if (wand != null) {
				desc += "\nWand: " + Messages.titleCase(wand.title());
			}
			if (ring != null) {
				desc += "\nRing: " + Messages.titleCase(ring.title());
			}
			if (ghostArtifact != null) {
				desc += "\nArtifact: " + Messages.titleCase(ghostArtifact.title());
			}

			desc += "\n" + Messages.get(this, "desc_strength", ghostStrength());

		}
		if (visiblyUpgraded() >= 15) desc += "\n\nThe next Rose level requires _" + petalsForNextLevel()
				+ " petals_. Petal requirements rise every five levels.";
		if (visiblyUpgraded() >= 15) desc += " _New at +15:_ the ghost can _equip and fire wands_.";
		if (visiblyUpgraded() >= 20) desc += " _New at +20:_ it can _equip rings_.";
		if (visiblyUpgraded() >= 30) desc += " _New at +30:_ it can _carry artifacts and use their gear stats_.";
		
		return desc;
	}

	private int petalsForNextLevel() {
		int next = visiblyUpgraded() + 1;
		return next < 15 ? 1 : 2 + (next - 15) / 5;
	}

	private void absorbPetal() {
		absorbedPetals++;
		int required = petalsForNextLevel();
		if (absorbedPetals >= required && canGainArtifactLevel()) {
			absorbedPetals = 0;
			upgrade();
			Catalog.countUse( getClass() );
			GLog.i( Messages.get(Petal.class, "levelup") );
		} else {
			GLog.i( "The Rose has absorbed " + absorbedPetals + "/" + required + " petals for its next level." );
		}
	}
	
	@Override
	public int value() {
		if (weapon != null){
			return -1;
		}
		if (armor != null || wand != null || ring != null || ghostArtifact != null){
			return -1;
		}
		return super.value();
	}

	@Override
	public String status() {
		if (ghost == null && ghostID != 0){
			try {
				findGhost();
			} catch ( ClassCastException e ){
				ShatteredPixelDungeon.reportException(e);
				ghostID = 0;
			}
		}
		if (ghost == null){
			return super.status();
		} else {
			return ((ghost.HP*100) / ghost.HT) + "%";
		}
	}
	
	@Override
	protected ArtifactBuff passiveBuff() {
		return new roseRecharge();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		if (ghost == null){
			if (charge < chargeCap) {
				partialCharge += 4*amount;
				while (partialCharge >= 1f){
					charge++;
					partialCharge--;
				}
				if (charge >= chargeCap) {
					charge = chargeCap;
					partialCharge = 0;
					GLog.p(Messages.get(DriedRose.class, "charged"));
				}
				updateQuickslot();
			}
		} else if (ghost.HP < ghost.HT) {
			int heal = Math.round((1 + level()/3f)*amount);
			ghost.HP = Math.min( ghost.HT, ghost.HP + heal);
			if (ghost.sprite != null) {
				ghost.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
			}
			updateQuickslot();
		}
	}
	
	@Override
	public Item upgrade() {
		if (level() >= 9)
			image = ItemSpriteSheet.ARTIFACT_ROSE3;
		else if (level() >= 4)
			image = ItemSpriteSheet.ARTIFACT_ROSE2;

		//For upgrade transferring via well of transmutation
		droppedPetals = Math.max( level(), droppedPetals );
		
		if (ghost != null){
			ghost.updateRose();
			ghost.HP = Math.min(ghost.HP+8, ghost.HT);
		}

		return super.upgrade();
	}
	
	public Weapon ghostWeapon(){
		return weapon;
	}
	
	public Armor ghostArmor(){
		return armor;
	}

	private static final String TALKEDTO =      "talkedto";
	private static final String FIRSTSUMMON =   "firstsummon";
	private static final String GHOSTID =       "ghostID";
	private static final String PETALS =        "petals";
	private static final String ABSORBED_PETALS = "absorbed_petals";
	
	private static final String WEAPON =        "weapon";
	private static final String ARMOR =         "armor";
	private static final String WAND =          "wand";
	private static final String RING =          "ring";
	private static final String GHOST_ARTIFACT = "ghost_artifact";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);

		bundle.put( TALKEDTO, talkedTo );
		bundle.put( FIRSTSUMMON, firstSummon );
		bundle.put( GHOSTID, ghostID );
		bundle.put( PETALS, droppedPetals );
		bundle.put( ABSORBED_PETALS, absorbedPetals );
		
		if (weapon != null) bundle.put( WEAPON, weapon );
		if (armor != null)  bundle.put( ARMOR, armor );
		if (wand != null) bundle.put( WAND, wand );
		if (ring != null) bundle.put( RING, ring );
		if (ghostArtifact != null) bundle.put( GHOST_ARTIFACT, ghostArtifact );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);

		talkedTo = bundle.getBoolean( TALKEDTO );
		firstSummon = bundle.getBoolean( FIRSTSUMMON );
		ghostID = bundle.getInt( GHOSTID );
		droppedPetals = bundle.getInt( PETALS );
		absorbedPetals = bundle.getInt( ABSORBED_PETALS );
		
		if (bundle.contains(WEAPON)) weapon = (MeleeWeapon)bundle.get( WEAPON );
		if (bundle.contains(ARMOR))  armor = (Armor)bundle.get( ARMOR );
		if (bundle.contains(WAND)) wand = (Wand)bundle.get( WAND );
		if (bundle.contains(RING)) ring = (Ring)bundle.get( RING );
		if (bundle.contains(GHOST_ARTIFACT)) ghostArtifact = (Artifact)bundle.get( GHOST_ARTIFACT );
	}

	public class roseRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			
			spend( TICK );
			
			if (ghost == null && ghostID != 0){
				findGhost();
			}

			if (ghost != null && !ghost.isAlive()){
				ghost = null;
			}
			
			//rose does not charge while ghost hero is alive
			if (ghost != null && !cursed && target.buff(MagicImmune.class) == null){
				
				//heals to full over 500 turns
				if (ghost.HP < ghost.HT && Regeneration.regenOn()) {
					partialCharge += (ghost.HT / 500f) * artifactChargeMultiplier(target);
					updateQuickslot();
					
					while (partialCharge > 1) {
						ghost.HP++;
						partialCharge--;
						if (ghost.HP == ghost.HT){
							partialCharge = 0;
						}
					}
				} else {
					partialCharge = 0;
				}
				
				return true;
			}
			
			if (charge < chargeCap
					&& !cursed
					&& target.buff(MagicImmune.class) == null
					&& Regeneration.regenOn()) {
				//500 turns to a full charge
				partialCharge += (1/5f * artifactChargeMultiplier(target));
				while (partialCharge > 1){
					charge++;
					partialCharge--;
					if (charge == chargeCap){
						partialCharge = 0f;
						GLog.p( Messages.get(DriedRose.class, "charged") );
					}
				}
			} else if (cursed && Random.Int(100) == 0) {

				ArrayList<Integer> spawnPoints = new ArrayList<>();

				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = target.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					Wraith.spawnAt(Random.element(spawnPoints), Wraith.class);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}

			}

			updateQuickslot();

			return true;
		}
	}
	
	public CellSelector.Listener ghostDirector = new CellSelector.Listener(){
		
		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			
			Sample.INSTANCE.play( Assets.Sounds.GHOST );

			ghost.directTocell(cell);

		}
		
		@Override
		public String prompt() {
			return  "\"" + Messages.get(GhostHero.class, "direct_prompt") + "\"";
		}
	};

	public static class Petal extends Item {

		{
			stackable = true;
			dropsDownHeap = true;
			
			image = ItemSpriteSheet.PETAL;
		}

		@Override
		public boolean doPickUp(Hero hero, int pos) {
			Catalog.setSeen(getClass());
			Statistics.itemTypesDiscovered.add(getClass());
			DriedRose rose = hero.belongings.getItem( DriedRose.class );

			if (rose == null){
				GLog.w( Messages.get(this, "no_rose") );
				return false;
			} if ( !rose.canGainArtifactLevel() ){
				GLog.i( Messages.get(this, "no_room") );
				hero.spendAndNext(pickupDelay());
				return true;
			} else {

				rose.absorbPetal();

				Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
				GameScene.pickUp(this, pos);
				hero.spendAndNext(pickupDelay());
				return true;

			}
		}

		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

	}

	public static class GhostHero extends DirectableAlly {

		{
			spriteClass = GhostSprite.class;

			flying = true;
			
			state = HUNTING;
			
			properties.add(Property.UNDEAD);
			properties.add(Property.INORGANIC);
		}
		
		private DriedRose rose = null;
		private Ring activeRing = null;
		
		public GhostHero(){
			super();
		}

		public GhostHero(DriedRose rose){
			super();
			this.rose = rose;
			updateRose();
			HP = HT;
		}

		@Override
		public void defendPos(int cell) {
			yell(Messages.get(this, "directed_position_" + Random.IntRange(1, 5)));
			super.defendPos(cell);
		}

		@Override
		public void followHero() {
			yell(Messages.get(this, "directed_follow_" + Random.IntRange(1, 5)));
			super.followHero();
		}

		@Override
		public void targetChar(Char ch) {
			yell(Messages.get(this, "directed_attack_" + Random.IntRange(1, 5)));
			super.targetChar(ch);
		}

		private void updateRose(){
			if (rose == null) {
				rose = Dungeon.hero.belongings.getItem(DriedRose.class);
				if (rose != null) {
					rose.ghost = this;
					rose.ghostID = id();
				}
			}
			
			//same dodge as the hero
			defenseSkill = (Dungeon.hero.lvl+4);
			if (rose == null) return;
			HT = 40 + 10*rose.level() + equippedRarityStat(RarityStat.Type.MAX_HEALTH);
			if (activeRing != rose.ring) {
				if (activeRing != null) activeRing.deactivate();
				activeRing = rose.ring;
				if (activeRing != null) activeRing.activate(this);
			}
		}

		private Wand wand() {
			return rose != null && rose.visiblyUpgraded() >= 15 ? rose.wand : null;
		}

		private int equippedRarityStat(RarityStat.Type type) {
			if (rose == null) return 0;
			int total = 0;
			if (rose.weapon != null) total += rose.weapon.rarityStat(type);
			if (rose.armor != null) total += rose.armor.rarityStat(type);
			if (rose.wand != null) total += rose.wand.rarityStat(type);
			if (rose.ring != null) total += rose.ring.rarityStat(type);
			if (rose.ghostArtifact != null) total += rose.ghostArtifact.rarityStat(type);
			return total;
		}

		public Weapon weapon(){
			if (rose != null)   return rose.weapon;
			else                return null;
		}

		public void clearWeapon(){
			if (rose != null) rose.weapon = null;
		}

		public Armor armor(){
			if (rose != null)   return rose.armor;
			else                return null;
		}

		@Override
		protected boolean act() {
			updateRose();
			if (rose == null
					|| !rose.isEquipped(Dungeon.hero)
					|| Dungeon.hero.buff(MagicImmune.class) != null){
				damage(1, new NoRoseDamage());
			}
			
			if (!isAlive()) {
				return true;
			}
			return super.act();
		}

		public static class NoRoseDamage{}

		@Override
		public int attackSkill(Char target) {
			
			//same accuracy as the hero.
			int acc = Dungeon.hero.lvl + 9;
			
			if (weapon() != null){
				acc *= weapon().accuracyFactor( this, target );
			}
			acc = Math.round(acc * (1f + equippedRarityStat(RarityStat.Type.ATTACK_ACCURACY) / 100f));
			return Math.max(1, acc);
		}
		
		@Override
		public float attackDelay() {
			float delay = super.attackDelay();
			if (weapon() != null){
				delay *= weapon().delayFactor(this);
			}
			return delay / Math.max(0.1f, 1f + equippedRarityStat(RarityStat.Type.ATTACK_SPEED) / 100f);
		}
		
		@Override
		protected boolean canAttack(Char enemy) {
			Wand wand = wand();
			return super.canAttack(enemy)
					|| (weapon() != null && weapon().canReach(this, enemy.pos))
					|| (wand != null && wand.curCharges > 0
					&& new Ballistica(pos, enemy.pos, wand.collisionProperties(enemy.pos)).collisionPos == enemy.pos);
		}
		
		@Override
		public int damageRoll() {
			int dmg = 0;
			Wand wand = wand();
			if (wand != null && wand.curCharges > 0) {
				int lvl = Math.max(0, wand.buffedLvl());
				if (wand instanceof DamageWand) {
					DamageWand damageWand = (DamageWand)wand;
					dmg = Random.NormalIntRange(damageWand.min(lvl), damageWand.max(lvl));
				} else {
					dmg = Random.NormalIntRange(2 + lvl, 5 + 2 * lvl);
				}
				dmg += equippedRarityStat(RarityStat.Type.MAGIC_DAMAGE);
				dmg = Math.round(dmg * (1f + equippedRarityStat(RarityStat.Type.MAGIC_BONUS) / 100f));
			} else if (weapon() != null){
				dmg += weapon().damageRoll(this);
				if (rose != null){
					int excessStr = rose.ghostStrength()-weapon().STRReq();
					if (excessStr > 0){
						dmg += Random.NormalIntRange(0, excessStr);
					}
				}
			} else if (rose != null) {
				//1-5 to 1-10
				dmg += Random.NormalIntRange(1, rose.ghostStrength()-8);
			}
			
			dmg += equippedRarityStat(RarityStat.Type.ATTACK_DAMAGE);
			dmg = Math.round(dmg * (1f + equippedRarityStat(RarityStat.Type.ATTACK_BONUS) / 100f));
			return Math.max(1, dmg);
		}
		
		@Override
		public int attackProc(Char enemy, int damage) {
			damage = super.attackProc(enemy, damage);

			Wand wand = wand();
			if (wand != null && wand.curCharges > 0) {
				wand.curCharges--;
				wand.curChargeKnown = true;
				Item.updateQuickslot();
			} else if (weapon() != null) {
				damage = weapon().proc(this, enemy, damage);
				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Dungeon.fail(this);
					GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));
				}
			}

			return damage;
		}
		
		@Override
		public int defenseProc(Char enemy, int damage) {
			if (armor() != null) {
				damage = armor().proc( enemy, this, damage );
			}
			return super.defenseProc(enemy, damage);
		}
		
		@Override
		public void damage(int dmg, Object src) {
			super.damage( dmg, src );
			
			//for the rose status indicator
			Item.updateQuickslot();
		}
		
		@Override
		public float speed() {
			float speed = super.speed();

			//moves 2 tiles at a time when returning to the hero
			if (state == WANDERING
					&& defendingPos == -1
					&& Dungeon.level.distance(pos, Dungeon.hero.pos) > 1){
				speed *= 2;
			}
			
			return speed * Math.max(0.1f, 1f + equippedRarityStat(RarityStat.Type.MOVEMENT_SPEED) / 100f);
		}
		
		@Override
		public int defenseSkill(Char enemy) {
			int defense = super.defenseSkill(enemy);

			if (defense != 0 && armor() != null ){
				defense = Math.round(armor().evasionFactor( this, defense ));
			}
			
			return defense;
		}
		
		@Override
		public int drRoll() {
			int dr = super.drRoll();
			if (armor() != null){
				dr += Random.NormalIntRange( armor().DRMin(), armor().DRMax());
			}
			if (weapon() != null){
				dr += Random.NormalIntRange( 0, weapon().defenseFactor( this ));
			}
			dr += equippedRarityStat(RarityStat.Type.DEFENSE);
			dr = Math.round(dr * (1f + equippedRarityStat(RarityStat.Type.ARMOR_BONUS) / 100f));
			return Math.max(0, dr);
		}

		@Override
		public int glyphLevel(Class<? extends Armor.Glyph> cls) {
			if (armor() != null && armor().hasGlyph(cls, this)){
				return Math.max(super.glyphLevel(cls), armor().buffedLvl());
			} else {
				return super.glyphLevel(cls);
			}
		}

		@Override
		public boolean interact(Char c) {
			updateRose();
			if (c == Dungeon.hero && rose != null && !rose.talkedTo){
				rose.talkedTo = true;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndQuest(GhostHero.this, Messages.get(GhostHero.this, "introduce") ));
					}
				});
				return true;
			} else {
				return super.interact(c);
			}
		}

		@Override
		public void die(Object cause) {
			sayDefeated();
			super.die(cause);
		}

		@Override
		public void destroy() {
			updateRose();
			//TODO stasis?
			if (rose != null) {
				rose.ghost = null;
				rose.charge = 0;
				rose.partialCharge = 0;
				rose.ghostID = -1;
			}
			super.destroy();
		}
		
		public void sayAppeared(){
			if (Dungeon.hero.buff(AscensionChallenge.class) != null){
				yell( Messages.get( this, "dialogue_ascension_" + Random.IntRange(1, 6) ));

			} else {

				int depth = (Dungeon.depth - 1) / 5;

				//only some lines are said on the first floor of a depth
				int variant = Dungeon.depth % 5 == 1 ? Random.IntRange(1, 3) : Random.IntRange(1, 6);

				switch (depth) {
					case 0:
						yell(Messages.get(this, "dialogue_sewers_" + variant));
						break;
					case 1:
						yell(Messages.get(this, "dialogue_prison_" + variant));
						break;
					case 2:
						yell(Messages.get(this, "dialogue_caves_" + variant));
						break;
					case 3:
						yell(Messages.get(this, "dialogue_city_" + variant));
						break;
					case 4:
					default:
						yell(Messages.get(this, "dialogue_halls_" + variant));
						break;
				}
			}
			if (ShatteredPixelDungeon.scene() instanceof GameScene) {
				Sample.INSTANCE.play( Assets.Sounds.GHOST );
			}
		}
		
		public void sayBoss(){
			int depth = (Dungeon.depth - 1) / 5;
			
			switch(depth){
				case 0:
					yell( Messages.get( this, "seen_goo_" + Random.IntRange(1, 3) ));
					break;
				case 1:
					yell( Messages.get( this, "seen_tengu_" + Random.IntRange(1, 3) ));
					break;
				case 2:
					yell( Messages.get( this, "seen_dm300_" + Random.IntRange(1, 3) ));
					break;
				case 3:
					yell( Messages.get( this, "seen_king_" + Random.IntRange(1, 3) ));
					break;
				case 4: default:
					yell( Messages.get( this, "seen_yog_" + Random.IntRange(1, 3) ));
					break;
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayDefeated(){
			if (BossHealthBar.isAssigned()){
				yell( Messages.get( this, "defeated_by_boss_" + Random.IntRange(1, 3) ));
			} else {
				yell( Messages.get( this, "defeated_by_enemy_" + Random.IntRange(1, 3) ));
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayHeroKilled(){
			yell( Messages.get( this, "player_killed_" + Random.IntRange(1, 3) ));
			GLog.newLine();
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayAnhk(){
			yell( Messages.get( this, "blessed_ankh_" + Random.IntRange(1, 3) ));
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		{
			immunities.add( CorrosiveGas.class );
			immunities.add( Burning.class );
			immunities.add( ScrollOfRetribution.class );
			immunities.add( ScrollOfPsionicBlast.class );
			immunities.add( AllyBuff.class );
		}

	}
	
	private static class WndGhostHero extends Window{
		
		private static final int BTN_SIZE	= 32;
		private static final float GAP		= 2;
		private static final float BTN_GAP	= 12;
		private static final int WIDTH		= 116;
		
		private ItemButton btnWeapon;
		private ItemButton btnArmor;
		private final ArrayList<ItemButton> specialButtons = new ArrayList<>();
		
		WndGhostHero(final DriedRose rose){
			
			IconTitle titlebar = new IconTitle();
			titlebar.icon( new ItemSprite(rose) );
			titlebar.label( Messages.get(this, "title") );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );
			
			RenderedTextBlock message =
					PixelScene.renderTextBlock(Messages.get(this, "desc", rose.ghostStrength()), 6);
			message.maxWidth( WIDTH );
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );
			
			btnWeapon = new ItemButton(){
				@Override
				protected void onClick() {
					if (rose.weapon != null){
						item(new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
						if (!rose.weapon.doPickUp(Dungeon.hero)){
							Dungeon.level.drop( rose.weapon, Dungeon.hero.pos);
						}
						rose.weapon = null;
					} else {
						GameScene.selectItem(new WndBag.ItemSelector() {

							@Override
							public String textPrompt() {
								return Messages.get(WndGhostHero.class, "weapon_prompt");
							}

							@Override
							public Class<?extends Bag> preferredBag(){
								return Belongings.Backpack.class;
							}

							@Override
							public boolean itemSelectable(Item item) {
								return item instanceof MeleeWeapon;
							}

							@Override
							public void onSelect(Item item) {
								if (!(item instanceof MeleeWeapon)) {
									//do nothing, should only happen when window is cancelled
								} else if (item.unique) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_unique"));
									hide();
								} else if (item.cursed || !item.cursedKnown) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_cursed"));
									hide();
								}  else if (!item.levelKnown && ((MeleeWeapon)item).STRReq(0) > rose.ghostStrength()){
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength_unknown"));
									hide();
								} else if (((MeleeWeapon)item).STRReq() > rose.ghostStrength()) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength"));
									hide();
								} else {
									if (item.isEquipped(Dungeon.hero)){
										((MeleeWeapon) item).doUnequip(Dungeon.hero, false, false);
									} else {
										item.detach(Dungeon.hero.belongings.backpack);
									}
									rose.weapon = (MeleeWeapon) item;
									item(rose.weapon);
								}
								
							}
						});
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item() != null && item().name() != null){
						GameScene.show(new WndInfoItem(item()));
						return true;
					}
					return false;
				}
			};
			btnWeapon.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.top() + message.height() + GAP, BTN_SIZE, BTN_SIZE );
			if (rose.weapon != null) {
				btnWeapon.item(rose.weapon);
			} else {
				btnWeapon.item(new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
			}
			add( btnWeapon );
			
			btnArmor = new ItemButton(){
				@Override
				protected void onClick() {
					if (rose.armor != null){
						item(new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER));
						if (!rose.armor.doPickUp(Dungeon.hero)){
							Dungeon.level.drop( rose.armor, Dungeon.hero.pos);
						}
						rose.armor = null;
					} else {
						GameScene.selectItem(new WndBag.ItemSelector() {

							@Override
							public String textPrompt() {
								return Messages.get(WndGhostHero.class, "armor_prompt");
							}

							@Override
							public Class<?extends Bag> preferredBag(){
								return Belongings.Backpack.class;
							}

							@Override
							public boolean itemSelectable(Item item) {
								return item instanceof Armor;
							}

							@Override
							public void onSelect(Item item) {
								if (!(item instanceof Armor)) {
									//do nothing, should only happen when window is cancelled
								} else if (item.unique || ((Armor) item).checkSeal() != null) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_unique"));
									hide();
								} else if (item.cursed || !item.cursedKnown) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_cursed"));
									hide();
								}  else if (!item.levelKnown && ((Armor)item).STRReq(0) > rose.ghostStrength()){
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength_unknown"));
									hide();
								} else if (((Armor)item).STRReq() > rose.ghostStrength()) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength"));
									hide();
								} else {
									if (item.isEquipped(Dungeon.hero)){
										((Armor) item).doUnequip(Dungeon.hero, false, false);
									} else {
										item.detach(Dungeon.hero.belongings.backpack);
									}
									rose.armor = (Armor) item;
									item(rose.armor);
								}
								
							}
						});
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item() != null && item().name() != null){
						GameScene.show(new WndInfoItem(item()));
						return true;
					}
					return false;
				}
			};
			btnArmor.setRect( btnWeapon.right() + BTN_GAP, btnWeapon.top(), BTN_SIZE, BTN_SIZE );
			if (rose.armor != null) {
				btnArmor.item(rose.armor);
			} else {
				btnArmor.item(new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER));
			}
			add( btnArmor );

			if (rose.visiblyUpgraded() >= 15) {
				specialButtons.add(createSpecialButton(rose, Wand.class,
						ItemSpriteSheet.WAND_HOLDER, "wand_prompt"));
			}
			if (rose.visiblyUpgraded() >= 20) {
				specialButtons.add(createSpecialButton(rose, Ring.class,
						ItemSpriteSheet.RING_HOLDER, "ring_prompt"));
			}
			if (rose.visiblyUpgraded() >= 30) {
				specialButtons.add(createSpecialButton(rose, Artifact.class,
						ItemSpriteSheet.ARTIFACT_HOLDER, "artifact_prompt"));
			}

			float bottom = btnArmor.bottom();
			if (!specialButtons.isEmpty()) {
				float rowGap = (WIDTH - specialButtons.size() * BTN_SIZE) / (specialButtons.size() + 1f);
				float x = rowGap;
				for (ItemButton button : specialButtons) {
					button.setRect(x, btnArmor.bottom() + GAP, BTN_SIZE, BTN_SIZE);
					add(button);
					x = button.right() + rowGap;
					bottom = button.bottom();
				}
			}

			resize(WIDTH, (int)(bottom + GAP));
		}

		private ItemButton createSpecialButton(final DriedRose rose,
				final Class<? extends Item> type, final int holder, final String promptKey) {
			ItemButton button = new ItemButton() {
				@Override
				protected void onClick() {
					Item equipped = specialItem(rose, type);
					if (equipped != null) {
						if (equipped instanceof Ring) ((Ring)equipped).deactivate();
						item(new WndBag.Placeholder(holder));
						if (!equipped.doPickUp(Dungeon.hero)) {
							Dungeon.level.drop(equipped, Dungeon.hero.pos);
						}
						setSpecialItem(rose, type, null);
						return;
					}

					GameScene.selectItem(new WndBag.ItemSelector() {
						@Override
						public String textPrompt() {
							return Messages.get(WndGhostHero.class, promptKey);
						}

						@Override
						public Class<? extends Bag> preferredBag() {
							return Belongings.Backpack.class;
						}

						@Override
						public boolean itemSelectable(Item item) {
							return type.isInstance(item) && item != rose;
						}

						@Override
						public void onSelect(Item selected) {
							if (selected == null || !type.isInstance(selected) || selected == rose) return;
							if (selected.cursed || !selected.cursedKnown) {
								GLog.w(Messages.get(WndGhostHero.class, "cant_cursed"));
								return;
							}

							if (selected instanceof Ring && selected.isEquipped(Dungeon.hero)) {
								if (!((Ring)selected).doUnequip(Dungeon.hero, false, false)) return;
							} else if (selected instanceof Artifact && selected.isEquipped(Dungeon.hero)) {
								if (!((Artifact)selected).doUnequip(Dungeon.hero, false, false)) return;
							} else {
								selected.detach(Dungeon.hero.belongings.backpack);
							}

							setSpecialItem(rose, type, selected);
							if (selected instanceof Ring && rose.ghost != null) {
								((Ring)selected).activate(rose.ghost);
								rose.ghost.activeRing = (Ring)selected;
							}
							item(selected);
						}
					});
				}

				@Override
				protected boolean onLongClick() {
					Item equipped = specialItem(rose, type);
					if (equipped != null) {
						GameScene.show(new WndInfoItem(equipped));
						return true;
					}
					return false;
				}
			};
			Item equipped = specialItem(rose, type);
			button.item(equipped == null ? new WndBag.Placeholder(holder) : equipped);
			return button;
		}

		private static Item specialItem(DriedRose rose, Class<? extends Item> type) {
			if (type == Wand.class) return rose.wand;
			if (type == Ring.class) return rose.ring;
			return rose.ghostArtifact;
		}

		private static void setSpecialItem(DriedRose rose, Class<? extends Item> type, Item item) {
			if (type == Wand.class) rose.wand = (Wand)item;
			else if (type == Ring.class) rose.ring = (Ring)item;
			else rose.ghostArtifact = (Artifact)item;
		}
	
	}
}
