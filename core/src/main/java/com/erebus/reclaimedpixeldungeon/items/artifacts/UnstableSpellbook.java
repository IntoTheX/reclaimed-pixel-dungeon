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
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicImmune;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Regeneration;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.effects.particles.ElmoParticle;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.bags.ScrollHolder;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEnergy;
import com.erebus.reclaimedpixeldungeon.items.scrolls.Scroll;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRage;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfReturn;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTerror;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class UnstableSpellbook extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_SPELLBOOK;

		levelCap = 10;

		charge = (int)(level()*0.6f)+2;
		partialCharge = 0;
		chargeCap = (int)(level()*0.6f)+2;

		defaultAction = AC_READ;
	}

	public static final String AC_READ = "READ";
	public static final String AC_ADD = "ADD";

	private final ArrayList<Class> scrolls = new ArrayList<>();

	public UnstableSpellbook() {
		super();

		setupScrolls();
	}

	private void setupScrolls(){
		scrolls.clear();

		Class<?>[] scrollClasses = Generator.Category.SCROLL.classes;
		float[] probs = Generator.Category.SCROLL.defaultProbsTotal.clone(); //array of primitives, clone gives deep copy.
		int i = Random.chances(probs);

		while (i != -1){
			scrolls.add(scrollClasses[i]);
			probs[i] = 0;

			i = Random.chances(probs);
		}
		scrolls.remove(ScrollOfTransmutation.class);
	}

	private Class<?> randomInfusionScroll() {
		Class<?>[] scrollClasses = Generator.Category.SCROLL.classes;
		float[] probs = Generator.Category.SCROLL.defaultProbsTotal.clone();
		for (int i = 0; i < scrollClasses.length; i++) {
			if (scrollClasses[i].equals( ScrollOfTransmutation.class ) || scrolls.contains( scrollClasses[i] )) {
				probs[i] = 0;
			}
		}
		int i = Random.chances( probs );
		return i == -1 ? null : scrollClasses[i];
	}

	private int maxInfusionScrollsForCurrentLevel() {
		if (trueLevel() >= levelCap) {
			return 2;
		}
		return Math.max( 0, levelCap - trueLevel() );
	}

	private void trimInfusionScrolls() {
		int maxScrolls = maxInfusionScrollsForCurrentLevel();
		while (!scrolls.isEmpty() && scrolls.size() > maxScrolls) {
			scrolls.remove(0);
		}
	}

	private void ensureInfusionScrollsAvailable() {
		int desiredScrolls = Math.min( 2, maxInfusionScrollsForCurrentLevel() );
		if (scrolls.isEmpty() && desiredScrolls > 0) {
			setupScrolls();
		}
		while (scrolls.size() < desiredScrolls) {
			Class<?> scroll = randomInfusionScroll();
			if (scroll == null) {
				setupScrolls();
				break;
			}
			scrolls.add( scroll );
		}
		trimInfusionScrolls();
	}

	private boolean isCurrentInfusionScroll( Item item ) {
		if (!(item instanceof Scroll) || !item.isIdentified()) return false;
		ensureInfusionScrollsAvailable();
		for (int i = 0; i <= 1 && i < scrolls.size(); i++) {
			if (scrolls.get(i).equals( item.getClass() )) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 0 && !cursed && hero.buff(MagicImmune.class) == null) {
			actions.add(AC_READ);
		}
		if (isEquipped( hero ) && canGainArtifactLevel() && !cursed && hero.buff(MagicImmune.class) == null) {
			actions.add(AC_ADD);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals( AC_READ )) {

			if (hero.buff( Blindness.class ) != null) GLog.w( Messages.get(this, "blinded") );
			else if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge <= 0)                     GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                          GLog.i( Messages.get(this, "cursed") );
			else {
				doReadEffect(hero);
			}

		} else if (action.equals( AC_ADD )) {
			ensureInfusionScrollsAvailable();
			GameScene.selectItem(itemSelector);
		}
	}

	public void doReadEffect(Hero hero){
		charge--;

		Scroll scroll;
		do {
			scroll = (Scroll) Generator.randomUsingDefaults(Generator.Category.SCROLL);
		} while (scroll == null
				//reduce the frequency of these scrolls by half
				||((scroll instanceof ScrollOfIdentify ||
				scroll instanceof ScrollOfRemoveCurse ||
				scroll instanceof ScrollOfMagicMapping) && Random.Int(2) == 0)
				//cannot roll transmutation
				|| (scroll instanceof ScrollOfTransmutation));

		boolean scrollWasKnown = Scroll.getKnown().contains( scroll.getClass() );
		scroll.anonymize();
		scroll.talentChance = 0;  //spellbook does not trigger on-scroll talents
		curItem = scroll;
		curUser = hero;

		//if there are charges left and the scroll has been given to the book
		if (charge > 0 && !scrolls.contains(scroll.getClass())) {
			final Scroll fScroll = scroll;
			final Class<? extends ExoticScroll> exoticClass = ExoticScroll.regToExo.get( fScroll.getClass() );

			if (exoticClass == null) {
				if (fScroll instanceof ScrollOfReturn && scrollWasKnown) {
					showReturnScrollConfirmation( fScroll );
				} else {
					readSpellbookScroll( fScroll );
				}
				return;
			}

			final ExploitHandler handler = Buff.affect(hero, ExploitHandler.class);
			handler.scroll = scroll;

			GameScene.show(new WndOptions(new ItemSprite(this),
					Messages.get(this, "prompt"),
					Messages.get(this, "read_empowered"),
					scroll.trueName(),
					Messages.get(exoticClass, "name")){
				@Override
				protected void onSelect(int index) {
					handler.detach();
					if (index == 1){
						Scroll scroll = Reflection.newInstance(exoticClass);
						curItem = scroll;
						charge--;
						scroll.anonymize();
						scroll.talentChance = 0;
						readSpellbookScroll( scroll );
					} else {
						readSpellbookScroll( fScroll );
					}
					updateQuickslot();
				}

				@Override
				public void onBackPressed() {
					//do nothing
				}
			});
		} else {
			readSpellbookScroll( scroll );
		}

		updateQuickslot();
	}

	private void showReturnScrollConfirmation( final Scroll scroll ) {
		final boolean[] resolved = { false };
		GameScene.show(new WndOptions(new ItemSprite(this),
				Messages.get(this, "prompt"),
				Messages.get(this, "read_return"),
				scroll.trueName(),
				Messages.get(this, "cancel")){
			@Override
			protected void onSelect(int index) {
				resolved[0] = true;
				if (index == 0) {
					readSpellbookScroll( scroll );
				} else {
					refundPendingScrollRead();
				}
			}

			@Override
			public void onBackPressed() {
				if (!resolved[0]) {
					resolved[0] = true;
					refundPendingScrollRead();
				}
				super.onBackPressed();
			}
		});
	}

	private void refundPendingScrollRead() {
		charge = Math.min( charge + 1, chargeCap );
		updateQuickslot();
	}

	private void readSpellbookScroll( Scroll scroll ) {
		checkForArtifactProc(curUser, scroll);
		scroll.doRead();
		Talent.onArtifactUsed(Dungeon.hero);
		updateQuickslot();
	}

	private void checkForArtifactProc(Hero user, Scroll scroll){
		//if the base scroll (exotics all match) is an AOE effect, then also trigger illuminate
		if (scroll instanceof ScrollOfLullaby
				|| scroll instanceof ScrollOfRemoveCurse || scroll instanceof ScrollOfTerror) {
			for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
				if (Dungeon.level.heroFOV[mob.pos]) {
					artifactProc(mob, visiblyUpgraded(), 1);
				}
			}
		//except rage, which affects everything even if it isn't visible
		} else if (scroll instanceof ScrollOfRage){
			for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
				artifactProc(mob, visiblyUpgraded(), 1);
			}
		}
	}

	//forces the reading of a regular scroll if the player tried to exploit by quitting the game when the menu was up
	public static class ExploitHandler extends Buff {
		{ actPriority = VFX_PRIO; }

		public Scroll scroll;

		@Override
		public boolean act() {
			curUser = Dungeon.hero;
			curItem = scroll;
			scroll.anonymize();
			scroll.talentChance = 0;
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					scroll.doRead();
					Item.updateQuickslot();
				}
			});
			detach();
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( "scroll", scroll );
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			scroll = (Scroll)bundle.get("scroll");
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new bookRecharge();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null){
			partialCharge += 0.1f*amount;
			while (partialCharge >= 1){
				partialCharge--;
				charge++;
			}
			if (charge >= chargeCap){
				partialCharge = 0;
			}
			updateQuickslot();
		}
	}

	@Override
	public Item upgrade() {
		Item result = super.upgrade();

		//for artifact transmutation and post-cap uncapped levels.
		ensureInfusionScrollsAvailable();

		return result;
	}

	@Override
	protected void syncChargeCapToLevel() {
		setChargeCapKeepingCharge( (int)(level()*0.6f)+2 );
	}

	@Override
	public void resetForTrinity(int visibleLevel) {
		super.resetForTrinity(visibleLevel);
		setupScrolls();
		trimInfusionScrolls();
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped(Dungeon.hero)) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
			
			ensureInfusionScrollsAvailable();
			if (canGainArtifactLevel() && scrolls.size() > 0) {
				desc += "\n\n" + Messages.get(this, "desc_index");
				desc += "\n" + "_" + Messages.get(scrolls.get(0), "name") + "_";
				if (scrolls.size() > 1)
					desc += "\n" + "_" + Messages.get(scrolls.get(1), "name") + "_";
			}
		}
		
		if (level() > 0) {
			desc += "\n\n" + Messages.get(this, "desc_empowered");
		}

		return desc;
	}

	private static final String SCROLLS =   "scrolls";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( SCROLLS, scrolls.toArray(new Class[scrolls.size()]) );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		scrolls.clear();
		if (bundle.contains(SCROLLS) && bundle.getClassArray(SCROLLS) != null) {
			for (Class<?> scroll : bundle.getClassArray(SCROLLS)) {
				if (scroll != null) scrolls.add(scroll);
			}
		}
	}

	public class bookRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			if (charge < chargeCap
					&& !cursed
					&& target.buff(MagicImmune.class) == null
					&& Regeneration.regenOn()) {
				//120 turns to charge at full, 80 turns to charge at 0/8
				partialCharge += artifactChargeGain( target, 120f - (chargeCap - charge)*5f );

				while (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

			updateQuickslot();

			spend( TICK );

			return true;
		}
	}

	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(UnstableSpellbook.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return ScrollHolder.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return isCurrentInfusionScroll( item );
		}

		@Override
		public void onSelect(Item item) {
			if (item != null && item instanceof Scroll && item.isIdentified()){
				Hero hero = Dungeon.hero;
				for (int i = 0; ( i <= 1 && i < scrolls.size() ); i++){
					if (scrolls.get(i).equals(item.getClass())){
						hero.sprite.operate( hero.pos );
						hero.busy();
						hero.spend( 2f );
						Sample.INSTANCE.play(Assets.Sounds.BURNING);
						hero.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );

						scrolls.remove(i);
						item.detach(hero.belongings.backpack);

						upgrade();
						Catalog.countUse(UnstableSpellbook.class);
						GLog.i( Messages.get(UnstableSpellbook.class, "infuse_scroll") );
						return;
					}
				}
				GLog.w( Messages.get(UnstableSpellbook.class, "unable_scroll") );
			} else if (item instanceof Scroll && !item.isIdentified()) {
				GLog.w( Messages.get(UnstableSpellbook.class, "unknown_scroll") );
			}
		}
	};
}
