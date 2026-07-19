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
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Degrade;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.darts.Dart;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.darts.TippedDart;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.journal.Notes;
import com.erebus.reclaimedpixeldungeon.mechanics.Ballistica;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.CellSelector;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.MissileSprite;
import com.erebus.reclaimedpixeldungeon.ui.QuickSlotButton;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndTranscendantChoice;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Comparator;

public class Item implements Bundlable {

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 1.0f;
	
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
	public static final String AC_TRANSCEND	= "TRANSCEND";

	private static final int TRANSCENDANT_ITEM_UPGRADE_CHANCE = 18;
	private static final String TRANSCENDANT_ITEM_UPGRADE = "ITEM_UPGRADE";
	
	protected String defaultAction;
	public boolean usesTargeting;

	//TODO should these be private and accessed through methods?
	public int image = 0;
	public int icon = -1; //used as an identifier for items with randomized images
	
	public boolean stackable = false;
	protected int quantity = 1;
	public boolean dropsDownHeap = false;
	
	private int level = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;

	private boolean rarityRolled = false;
	private ItemRarity rarity = ItemRarity.COMMON;
	private ArrayList<RarityStat> rarityStats = new ArrayList<>();
	private boolean lastRarityStatUpgradeImproved = false;
	private int transcendantLevel = 1;
	private int transcendantXP = 0;
	private int transcendantXPToNext = transcendantXPRequirement( 1 );
	private int transcendantPendingChoices = 0;
	private ArrayList<TranscendantChoice> transcendantChoiceCache = new ArrayList<>();

	private static final int RARITY_STAT_UPGRADE_CHANCE = 50;
	private static final int RARITY_STAT_UPGRADE_ALL_CHANCE = 15;
	private static final int RARITY_STAT_UPGRADE_EACH_CHANCE = 65;
	
	// Unique items persist through revival
	public boolean unique = false;

	// These items are preserved even if the hero's inventory is lost via unblessed ankh
	// this is largely set by the resurrection window, items can override this to always be kept
	public boolean keptThoughLostInvent = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;

	public int customNoteID = -1;
	
	public static final Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<>();
		if (hasPendingTranscendantChoice()) actions.add( AC_TRANSCEND );
		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}

	public String actionName(String action, Hero hero){
		if (action.equals( AC_TRANSCEND )) return "choose power";
		return Messages.get(this, "ac_" + action);
	}

	public final boolean doPickUp( Hero hero ) {
		return doPickUp( hero, hero.pos );
	}

	public boolean doPickUp(Hero hero, int pos) {
		if (collect( hero.belongings.backpack )) {
			if (Dungeon.homebase != null && Dungeon.depth > 0) {
				Dungeon.homebase.progressRecoveryMission( this );
			}
			
			GameScene.pickUp( this, pos );
			Sample.INSTANCE.play( Assets.Sounds.ITEM );
			hero.spendAndNext( pickupDelay() );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		hero.spendAndNext(TIME_TO_DROP);
		int pos = hero.pos;
		Dungeon.level.drop(detachAll(hero.belongings.backpack), pos).sprite.drop(pos);
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		keptThoughLostInvent = false;
	}

	public boolean keptThroughLostInventory(){
		return keptThoughLostInvent;
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell(thrower);
	}
	
	public void execute( Hero hero, String action ) {

		GameScene.cancel();
		curUser = hero;
		curItem = this;
		
		if (action.equals( AC_DROP )) {
			
			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doDrop(hero);
			}

		} else if (action.equals( AC_THROW )) {

			if (hero.belongings.backpack.contains(this) || isEquipped(hero)) {
				doThrow(hero);
			}

		} else if (action.equals( AC_TRANSCEND )) {

			showTranscendantChoice();

		}
	}

	//can be overridden if default action is variable
	public String defaultAction(){
		return defaultAction;
	}
	
	public void execute( Hero hero ) {
		String action = defaultAction();
		if (action != null) {
			execute(hero, defaultAction());
		}
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}
	
	//takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other )){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}
	
	public boolean collect( Bag container ) {

		if (quantity <= 0){
			return true;
		}

		ArrayList<Item> items = container.items;

		if (items.contains( this )) {
			return true;
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).canHold( this )) {
				if (collect( (Bag)item )){
					return true;
				}
			}
		}

		if (!container.canHold(this)){
			return false;
		}
		
		if (stackable) {
			for (Item item:items) {
				if (isSimilar( item )) {
					item.merge( this );
					item.updateQuickslot();
					if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
						Badges.validateItemLevelAquired( this );
						Talent.onItemCollected(Dungeon.hero, item);
						if (isIdentified()) {
							Catalog.setSeen(getClass());
							Statistics.itemTypesDiscovered.add(getClass());
						}
					}
					if (TippedDart.lostDarts > 0){
						Dart d = new Dart();
						d.quantity(TippedDart.lostDarts);
						TippedDart.lostDarts = 0;
						if (!d.collect()){
							//have to handle this in an actor as we can't manipulate the heap during pickup
							Actor.add(new Actor() {
								{ actPriority = VFX_PRIO; }
								@Override
								protected boolean act() {
									Dungeon.level.drop(d, Dungeon.hero.pos).sprite.drop();
									Actor.remove(this);
									return true;
								}
							});
						}
					}
					return true;
				}
			}
		}

		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Badges.validateItemLevelAquired( this );
			Talent.onItemCollected( Dungeon.hero, this );
			if (isIdentified()){
				Catalog.setSeen(getClass());
				Statistics.itemTypesDiscovered.add(getClass());
			}
		}

		items.add( this );
		Dungeon.quickslot.replacePlaceholder(this);
		Collections.sort( items, itemComparator );
		updateQuickslot();
		return true;

	}
	
	public final boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}
	
	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			//pssh, who needs copy constructors?
			Item split = Reflection.newInstance(getClass());
			
			if (split == null){
				return null;
			}
			
			Bundle copy = new Bundle();
			this.storeInBundle(copy);
			split.restoreFromBundle(copy);
			split.quantity(amount);
			quantity -= amount;
			
			return split;
		}
	}

	public Item duplicate(){
		Item dupe = Reflection.newInstance(getClass());
		if (dupe == null){
			return null;
		}
		Bundle copy = new Bundle();
		this.storeInBundle(copy);
		dupe.restoreFromBundle(copy);
		return dupe;
	}
	
	public final Item detach( Bag container ) {
		
		if (quantity <= 0) {
			
			return null;
			
		} else
		if (quantity == 1) {

			if (stackable){
				Dungeon.quickslot.convertToPlaceholder(this);
			}

			return detachAll( container );
			
		} else {
			
			
			Item detached = split(1);
			updateQuickslot();
			if (detached != null) detached.onDetach( );
			return detached;
			
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				container.grabItems(); //try to put more items into the bag as it now has free space
				updateQuickslot();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		updateQuickslot();
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return getClass() == item.getClass();
	}

	protected void onDetach(){}

	//returns the true level of the item, ignoring all modifiers aside from upgrades
	public final int trueLevel(){
		return level;
	}

	//returns the persistant level of the item, only affected by modifiers which are persistent (e.g. curse infusion)
	public int level(){
		return level;
	}
	
	//returns the level of the item, after it may have been modified by temporary boosts/reductions
	//note that not all item properties should care about buffs/debuffs! (e.g. str requirement)
	public int buffedLvl(){
		//only the hero can be affected by Degradation
		if (Dungeon.hero != null && Dungeon.hero.buff( Degrade.class ) != null
			&& (isEquipped( Dungeon.hero ) || Dungeon.hero.belongings.contains( this ))) {
			return Degrade.reduceLevel(level());
		} else {
			return level();
		}
	}

	public void level( int value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	public int visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}

	public int buffedVisiblyUpgraded() {
		return levelKnown ? buffedLvl() : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}

	public final Item identify(){
		return identify(true);
	}

	public Item identify( boolean byHero ) {

		randomizeRarityStats();

		if (byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(getClass());
			Statistics.itemTypesDiscovered.add(getClass());
		}

		levelKnown = true;
		cursedKnown = true;
		Item.updateQuickslot();
		recordVisibleRarityStats();
		
		return this;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		//do nothing by default
	}
	
	public static void evoke( Hero hero ) {
		if (hero == null || hero.sprite == null) return;
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	public String title() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;

	}
	
	public String name() {
		return rarityName( trueName() );
	}
	
	public final String trueName() {
		return Messages.get(this, "name");
	}
	
	public int image() {
		return image;
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {

		if (Dungeon.hero != null) {
			Notes.CustomRecord note = Notes.findCustomRecord(customNoteID);
			if (note != null) {
				//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
				return Messages.get(this, "custom_note", note.title().replace('_', 'ˍ')) + "\n\n" + desc();
			} else {
				note = Notes.findCustomRecord(getClass());
				if (note != null) {
					//we swap underscore(0x5F) with low macron(0x2CD) here to avoid highlighting in the item window
					return Messages.get(this, "custom_note_type", note.title().replace('_', 'ˍ')) + "\n\n" + desc();
				}
			}
		}

		return appendRarityInfo( desc() );
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public int quantity() {
		return quantity;
	}
	
	public Item quantity( int value ) {
		quantity = value;
		return this;
	}

	//item's value in gold coins
	public int value() {
		return 0;
	}

	public int shopValue() {
		return applyRarityShopValue( value() );
	}

	//item's value in energy crystals
	public int energyVal() {
		return 0;
	}
	
	public Item virtual(){
		Item item = Reflection.newInstance(getClass());
		if (item == null) return null;
		
		item.quantity = 0;
		item.level = level;
		return item;
	}
	
	public Item random() {
		return this;
	}

	public Item randomizeRarityStats() {
		return RarityStats.roll( this );
	}

	public boolean hasRarityRoll() {
		return rarityRolled;
	}

	public ItemRarity rarity() {
		return rarity;
	}

	public boolean canUseRarityCatalyst() {
		return showsRarityStats() && rarity != ItemRarity.TRANSCENDANT && RarityStats.hasStatPool( this );
	}

	public boolean canUpgradeRarityTier() {
		return canUseRarityCatalyst() && nextRarityTier() != null;
	}

	public ItemRarity nextRarityTier() {
		if (!canUseRarityCatalyst()) return null;
		int next = rarity.ordinal() + 1;
		ItemRarity[] rarities = ItemRarity.values();
		return next >= rarities.length ? null : rarities[next];
	}

	public int rarityTierUpgradeChance() {
		ItemRarity next = nextRarityTier();
		if (next == null) return 0;

		switch (next) {
			case UNCOMMON:
				return 85;
			case RARE:
				return 65;
			case EPIC:
				return 45;
			case LEGENDARY:
				return 25;
			case TRANSCENDANT:
				return 10;
			default:
				return 0;
		}
	}

	public boolean upgradeRarityTier() {
		ItemRarity next = nextRarityTier();
		int chance = rarityTierUpgradeChance();
		if (next == null || chance <= 0 || Random.Int( 100 ) >= chance) return false;

		ArrayList<RarityStat> upgradedStats = new ArrayList<>();
		for (RarityStat stat : rarityStats) {
			upgradedStats.add( stat.copy() );
		}
		setRarityStats( next, upgradedStats );
		updateQuickslot();
		return true;
	}

	public boolean isTranscendantRarity() {
		return rarityRolled && rarity == ItemRarity.TRANSCENDANT;
	}

	public boolean hasVisibleRarityStats() {
		return rarityRolled && rarity.isVisible();
	}

	public boolean showsRarityStats() {
		return hasVisibleRarityStats() && isRarityKnown();
	}

	protected boolean isRarityKnown() {
		return levelKnown && (cursedKnown || !cursed);
	}

	public boolean hasRarityAura() {
		return showsRarityStats() && rarity.hasAura();
	}

	public int rarityColor() {
		return rarity.color();
	}

	public float rarityAuraAlpha() {
		return rarity.auraAlpha();
	}

	protected String rarityName( String name ) {
		if (!showsRarityStats()) return name;
		return rarity.displayName() + " " + name;
	}

	public int rarityStatCount() {
		int count = 0;
		for (RarityStat stat : rarityStats) {
			if (!stat.isEmptySlot()) count++;
		}
		return count;
	}

	public ArrayList<RarityStat> visibleRarityStats() {
		ArrayList<RarityStat> stats = new ArrayList<>();
		if (!showsRarityStats()) return stats;

		EnumMap<RarityStat.Type, RarityStat> mergedStats = new EnumMap<>( RarityStat.Type.class );
		for (RarityStat stat : rarityStats) {
			if (stat.isEmptySlot()) continue;

			RarityStat existing = mergedStats.get( stat.type() );
			if (existing == null) {
				existing = stat.copy();
				mergedStats.put( existing.type(), existing );
				stats.add( existing );
			} else if (stat.type().hasValue()) {
				existing.increase( stat.value() );
			}
		}
		return stats;
	}

	protected int applyRarityShopValue( int baseValue ) {
		if (baseValue <= 0 || !rarityRolled) return baseValue;

		int bonus = rarity.priceBonusPercent() + rarityStatCount() * 8;
		if (bonus <= 0) return baseValue;
		return Math.max( 1, Math.round( baseValue * (100 + bonus) / 100f ) );
	}

	void setRarityStats( ItemRarity rarity, ArrayList<RarityStat> rarityStats ) {
		this.rarity = rarity == null ? ItemRarity.COMMON : rarity;
		this.rarityStats.clear();
		if (rarityStats != null) this.rarityStats.addAll( rarityStats );
		rarityRolled = true;
		if (this.rarity == ItemRarity.TRANSCENDANT) {
			ensureTranscendantProgress();
		} else {
			clearTranscendantProgress();
		}
		onRarityStatsChanged();
	}

	public void inheritRarityStatsFrom( Item source ) {
		if (source == null || !source.hasRarityRoll()) return;

		ArrayList<RarityStat> inheritedStats = new ArrayList<>();
		for (RarityStat stat : source.rarityStats) {
			inheritedStats.add( stat.copy() );
		}

		setRarityStats( source.rarity, inheritedStats );
		if (source.isTranscendantRarity()) {
			transcendantLevel = source.transcendantLevel;
			transcendantXP = source.transcendantXP;
			transcendantXPToNext = source.transcendantXPToNext;
			transcendantPendingChoices = source.transcendantPendingChoices;
			transcendantChoiceCache.clear();
			transcendantChoiceCache.addAll( source.transcendantChoiceCache );
		}
		updateQuickslot();
	}

	protected void onRarityStatsChanged() {
		recordVisibleRarityStats();
		if (Dungeon.hero != null && isEquipped( Dungeon.hero )) {
			Dungeon.hero.updateHT( false );
		}
	}

	private void recordVisibleRarityStats() {
		if (Dungeon.hero == null || !showsRarityStats()) return;
		for (RarityStat stat : rarityStats) {
			if (!stat.isEmptySlot()) {
				Catalog.setSeen( stat.type() );
			}
		}
	}

	public int rarityStat( RarityStat.Type type ) {
		int value = 0;
		for (RarityStat stat : rarityStats) {
			if (stat.type() == type) value += stat.value();
		}
		return type == null ? value : type.capValue( value );
	}

	private boolean hasRarityStat( RarityStat.Type type ) {
		for (RarityStat stat : rarityStats) {
			if (!stat.isEmptySlot() && stat.type() == type) return true;
		}
		return false;
	}

	private RarityStat firstRarityStat( RarityStat.Type type ) {
		for (RarityStat stat : rarityStats) {
			if (!stat.isEmptySlot() && stat.type() == type) return stat;
		}
		return null;
	}

	private boolean hasRequiredRarityStats( RarityStat.Type type ) {
		for (RarityStat.Type required : type.requires()) {
			if (!hasRarityStat( required )) return false;
		}
		return true;
	}

	public ArrayList<Integer> rarityStatIndexes( boolean requireValue, boolean includeEmpty, boolean includeLocked ) {
		ArrayList<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < rarityStats.size(); i++) {
			RarityStat stat = rarityStats.get( i );
			if (stat.isEmptySlot() && !includeEmpty) continue;
			if (stat.locked() && !includeLocked) continue;
			if (requireValue && !stat.type().hasValue()) continue;
			indexes.add( i );
		}
		return indexes;
	}

	public String rarityStatChoiceText( int index ) {
		if (index < 0 || index >= rarityStats.size()) return "";
		RarityStat stat = rarityStats.get( index );
		if (stat.isEmptySlot()) return "Empty Slot";

		String text = stat.type().displayName();
		if (stat.type().hasValue()) {
			text += ": " + (stat.value() > 0 ? "+" : "") + stat.value() + (stat.type().percent() ? "%" : "");
		}
		if (stat.locked()) text += " (locked)";
		return text;
	}

	public boolean canAddRarityStatSlot() {
		return canUseRarityCatalyst() && (rarityStats.size() < rarity.statSlots() || hasEmptyRaritySlot());
	}

	public boolean reforgeRarityStats() {
		if (!canUseRarityCatalyst()) return false;
		RarityStats.reroll( this );
		updateQuickslot();
		return true;
	}

	public boolean aetherfluxRarityStats() {
		if (!canUseRarityCatalyst()) return false;

		ItemRarity newRarity = RarityStats.rollRarity();
		int statCount = Math.max( 1, Math.min( rarityStats.size(), newRarity.statSlots() ) );
		ArrayList<RarityStat> newStats = new ArrayList<>();

		for (int i = 0; i < statCount; i++) {
			RarityStat oldStat = rarityStats.get( i );
			RarityStat newStat = null;
			if (!oldStat.isEmptySlot() && RarityStats.typeAvailable( this, newRarity, oldStat.type() )) {
				newStat = new RarityStat( oldStat.type(), RarityStats.rollValue( oldStat.type(), newRarity ), oldStat.locked() );
			}
			if (newStat == null) newStat = RarityStats.rollStat( this, newRarity, newStats, null );
			newStats.add( newStat == null ? new RarityStat( RarityStat.Type.EMPTY_SLOT, 0 ) : newStat );
		}

		setRarityStats( newRarity, newStats );
		updateQuickslot();
		return true;
	}

	public boolean addRarityStatSlot() {
		if (!canAddRarityStatSlot()) return false;

		int emptyIndex = firstEmptyRaritySlot();
		ArrayList<RarityStat> selectedStats = nonEmptyRarityStatsExcept( -1 );
		RarityStat newStat = RarityStats.rollStat( this, rarity, selectedStats, null );

		if (emptyIndex != -1) {
			if (newStat == null) return false;
			rarityStats.set( emptyIndex, newStat );
		} else {
			rarityStats.add( newStat == null ? new RarityStat( RarityStat.Type.EMPTY_SLOT, 0 ) : newStat );
		}

		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean reshapeRarityStats() {
		if (!canUseRarityCatalyst()) return false;

		int statCount = Math.max( 1, rarityStats.size() );
		ArrayList<RarityStat> lockedStats = new ArrayList<>();
		for (RarityStat stat : rarityStats) {
			if (stat.locked() && !stat.isEmptySlot()) lockedStats.add( stat.copy() );
		}

		ArrayList<RarityStat> newStats = RarityStats.rollStats( this, rarity, statCount, lockedStats, null );
		for (RarityStat stat : newStats) {
			stat.locked( false );
		}

		rarityStats.clear();
		rarityStats.addAll( newStats );
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean lockRarityStat( int index ) {
		if (!canUseRarityCatalyst() || index < 0 || index >= rarityStats.size()) return false;
		RarityStat stat = rarityStats.get( index );
		if (stat.isEmptySlot() || stat.locked()) return false;
		stat.locked( true );
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean emptyRarityStat( int index ) {
		if (!canUseRarityCatalyst() || index < 0 || index >= rarityStats.size()) return false;
		if (rarityStats.get( index ).isEmptySlot()) return false;
		rarityStats.set( index, new RarityStat( RarityStat.Type.EMPTY_SLOT, 0 ) );
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean rerollRarityStatType( int index ) {
		if (!canUseRarityCatalyst() || index < 0 || index >= rarityStats.size()) return false;

		RarityStat oldStat = rarityStats.get( index );
		if (oldStat.isEmptySlot() || oldStat.locked()) return false;

		RarityStat newStat = RarityStats.rollStat( this, rarity, nonEmptyRarityStatsExcept( index ), oldStat.type() );
		if (newStat == null) return false;

		rarityStats.set( index, newStat );
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean rerollRarityStatValue( int index ) {
		if (!canUseRarityCatalyst() || index < 0 || index >= rarityStats.size()) return false;

		RarityStat oldStat = rarityStats.get( index );
		if (oldStat.isEmptySlot() || !oldStat.type().hasValue()) return false;

		rarityStats.set( index, new RarityStat( oldStat.type(), RarityStats.rollValue( oldStat.type(), rarity ), oldStat.locked() ) );
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	private boolean hasEmptyRaritySlot() {
		return firstEmptyRaritySlot() != -1;
	}

	private int firstEmptyRaritySlot() {
		for (int i = 0; i < rarityStats.size(); i++) {
			if (rarityStats.get( i ).isEmptySlot()) return i;
		}
		return -1;
	}

	private ArrayList<RarityStat> nonEmptyRarityStatsExcept( int excludedIndex ) {
		ArrayList<RarityStat> stats = new ArrayList<>();
		for (int i = 0; i < rarityStats.size(); i++) {
			if (i == excludedIndex) continue;
			RarityStat stat = rarityStats.get( i );
			if (!stat.isEmptySlot()) stats.add( stat.copy() );
		}
		return stats;
	}

	public boolean improveRarityStatsFromUpgrade() {
		return improveRarityStatsFromUpgrade( 1 );
	}

	public boolean improveRarityStatsFromUpgrade( int rolls ) {
		if (!rarityRolled || rarityStats.isEmpty()) {
			lastRarityStatUpgradeImproved = false;
			return false;
		}

		boolean improved = false;
		for (int i = 0; i < rolls; i++) {
			improved = tryImproveRarityStatsFromUpgrade() || improved;
		}
		lastRarityStatUpgradeImproved = improved;
		return improved;
	}

	public boolean consumeLastRarityStatUpgradeImproved() {
		boolean improved = lastRarityStatUpgradeImproved;
		lastRarityStatUpgradeImproved = false;
		return improved;
	}

	private boolean tryImproveRarityStatsFromUpgrade() {
		if (Random.Int( 100 ) >= RARITY_STAT_UPGRADE_CHANCE) return false;

		ArrayList<RarityStat> eligibleStats = new ArrayList<>();
		for (RarityStat stat : rarityStats) {
			if (stat.type().hasValue()) eligibleStats.add( stat );
		}
		if (eligibleStats.isEmpty()) return false;

		boolean improveAll = Random.Int( 100 ) < RARITY_STAT_UPGRADE_ALL_CHANCE;
		boolean improved = false;
		for (RarityStat stat : eligibleStats) {
			if (improveAll || Random.Int( 100 ) < RARITY_STAT_UPGRADE_EACH_CHANCE) {
				stat.increase( RarityStats.upgradeValue( stat.type(), rarity ) );
				improved = true;
			}
		}

		if (!improved) {
			RarityStat stat = eligibleStats.get( Random.Int( eligibleStats.size() ) );
			stat.increase( RarityStats.upgradeValue( stat.type(), rarity ) );
		}

		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public boolean improveRarityStatsFromUpgradeScroll() {
		return improveRarityStatsFromUpgrade();
	}

	public boolean canGainTranscendantXP() {
		return isTranscendantRarity() && RarityStats.hasStatPool( this );
	}

	public boolean addTranscendantXP( int amount ) {
		if (!canGainTranscendantXP() || amount <= 0) return false;

		ensureTranscendantProgress();
		transcendantXP += amount;

		boolean leveled = false;
		int levelsGained = 0;
		while (transcendantXP >= transcendantXPToNext) {
			transcendantXP -= transcendantXPToNext;
			transcendantLevel++;
			transcendantXPToNext = transcendantXPRequirement( transcendantLevel );
			transcendantPendingChoices++;
			transcendantChoiceCache.clear();
			leveled = true;
			levelsGained++;
		}

		if (leveled) {
			Dungeon.increaseMobLevelPressure( levelsGained * 2 );
			GLog.p( "Your Transcendant " + trueName() + " has ascended to level " + transcendantLevel + "." );
			GLog.i( "It hums with potential, awaiting your choice." );
		}
		updateQuickslot();
		return leveled;
	}

	public int transcendantLevel() {
		ensureTranscendantProgress();
		return transcendantLevel;
	}

	public int transcendantXP() {
		ensureTranscendantProgress();
		return transcendantXP;
	}

	public int transcendantXPToNext() {
		ensureTranscendantProgress();
		return transcendantXPToNext;
	}

	public boolean hasPendingTranscendantChoice() {
		return isTranscendantRarity() && transcendantPendingChoices > 0;
	}

	public ArrayList<TranscendantChoice> transcendantChoices() {
		if (!hasPendingTranscendantChoice()) return new ArrayList<>();
		if (!transcendantChoiceCache.isEmpty()) return new ArrayList<>( transcendantChoiceCache );

		ArrayList<TranscendantChoice> choices = new ArrayList<>();
		ArrayList<RarityStat.Type> seenTypes = new ArrayList<>();
		boolean seenItemUpgrade = false;
		int attempts = 0;
		while (choices.size() < 3 && attempts < 100) {
			attempts++;
			TranscendantChoice choice = rollTranscendantChoice();
			if (choice == null) continue;
			if (choice.itemUpgrade) {
				if (seenItemUpgrade) continue;
				seenItemUpgrade = true;
			} else {
				if (seenTypes.contains( choice.type )) continue;
				seenTypes.add( choice.type );
			}
			choices.add( choice );
		}
		transcendantChoiceCache.clear();
		transcendantChoiceCache.addAll( choices );
		return new ArrayList<>( choices );
	}

	private TranscendantChoice rollTranscendantChoice() {
		if (isUpgradable() && Random.Int( 100 ) < TRANSCENDANT_ITEM_UPGRADE_CHANCE) {
			return TranscendantChoice.itemUpgrade( visiblyUpgraded() );
		}

		ArrayList<RarityStat.Type> pool = RarityStats.statPool( this );
		ArrayList<RarityStat.Type> eligible = new ArrayList<>();
		for (RarityStat.Type type : pool) {
			if (type == RarityStat.Type.EMPTY_SLOT || type == RarityStat.Type.SOULBOUND) continue;
			if (!type.allowedFor( ItemRarity.TRANSCENDANT )) continue;
			if (!hasRequiredRarityStats( type )) continue;
			if (type.capsAtHundred() && rarityStat( type ) >= 100) continue;
			if (!type.hasValue() && hasRarityStat( type )) continue;
			eligible.add( type );
		}
		if (eligible.isEmpty()) return null;

		RarityStat.Type type = eligible.get( Random.Int( eligible.size() ) );
		RarityStat existing = firstRarityStat( type );
		if (existing != null && type.hasValue()) {
			int oldValue = type.capsAtHundred() ? rarityStat( type ) : existing.value();
			int delta = cappedTranscendantIncrease( type, RarityStats.rollValue( type, ItemRarity.TRANSCENDANT ) );
			if (delta <= 0) return null;
			return TranscendantChoice.upgrade( type, delta, oldValue );
		} else if (existing == null || !type.unique()) {
			int value = type.hasValue() ? cappedTranscendantIncrease( type, RarityStats.rollValue( type, ItemRarity.TRANSCENDANT ) ) : 0;
			if (type.hasValue() && value <= 0) return null;
			return TranscendantChoice.add( type, value );
		}
		return null;
	}

	private int cappedTranscendantIncrease( RarityStat.Type type, int amount ) {
		if (type == null || !type.capsAtHundred()) return amount;
		return Math.min( Math.max( 0, amount ), Math.max( 0, 100 - rarityStat( type ) ) );
	}

	public boolean applyTranscendantChoice( TranscendantChoice choice ) {
		if (choice == null || !hasPendingTranscendantChoice()) return false;
		if (choice.itemUpgrade) {
			if (!isUpgradable()) return false;

			Item upgraded = upgrade();
			boolean rarityImproved = upgraded.improveRarityStatsFromUpgrade();
			Badges.validateItemLevelAquired( upgraded );
			Dungeon.increaseMobLevelPressure( 1 );
			GLog.p( "Transcendant power upgrades " + Messages.capitalize( upgraded.name() ) + "." );
			if (rarityImproved) {
				GLog.p( Messages.capitalize( upgraded.name() ) + "'s rarity stats improve!" );
			}

			transcendantPendingChoices--;
			transcendantChoiceCache.clear();
			upgraded.onRarityStatsChanged();
			upgraded.updateQuickslot();
			return true;
		}
		if (!RarityStats.statPool( this ).contains( choice.type )) return false;
		if (!choice.type.allowedFor( ItemRarity.TRANSCENDANT ) || !hasRequiredRarityStats( choice.type )) return false;

		RarityStat existing = firstRarityStat( choice.type );
		if (choice.existing && existing != null && choice.type.hasValue()) {
			int oldValue = choice.type.capsAtHundred() ? rarityStat( choice.type ) : existing.value();
			int delta = cappedTranscendantIncrease( choice.type, choice.delta );
			if (delta <= 0) return false;
			existing.increase( delta );
			int newValue = choice.type.capsAtHundred() ? rarityStat( choice.type ) : existing.value();
			GLog.p( choice.type.displayName() + " increased: " + oldValue + " -> " + newValue + "." );
		} else {
			if (choice.type.unique() && existing != null) return false;
			int value = choice.type.hasValue() ? choice.value : 0;
			if (choice.type.hasValue()) {
				value = cappedTranscendantIncrease( choice.type, value );
				if (value <= 0) return false;
			}
			rarityStats.add( new RarityStat( choice.type, value ) );
			if (choice.type.hasValue()) {
				GLog.p( "New Transcendant power: " + choice.type.displayName() + " +" + value + (choice.type.percent() ? "%" : "") + "." );
			} else {
				GLog.p( "New Transcendant power: " + choice.type.displayName() + "." );
			}
		}

		transcendantPendingChoices--;
		transcendantChoiceCache.clear();
		onRarityStatsChanged();
		updateQuickslot();
		return true;
	}

	public void showTranscendantChoice() {
		if (!hasPendingTranscendantChoice()) return;

		final Item item = this;
		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				if (item.hasPendingTranscendantChoice()) {
					GameScene.show( new WndTranscendantChoice( item ) );
				}
			}
		} );
	}

	private void ensureTranscendantProgress() {
		if (transcendantLevel < 1) transcendantLevel = 1;
		if (transcendantXP < 0) transcendantXP = 0;
		transcendantXPToNext = transcendantXPRequirement( transcendantLevel );
		if (transcendantPendingChoices < 0) transcendantPendingChoices = 0;
	}

	private void clearTranscendantProgress() {
		transcendantLevel = 1;
		transcendantXP = 0;
		transcendantXPToNext = transcendantXPRequirement( transcendantLevel );
		transcendantPendingChoices = 0;
		transcendantChoiceCache.clear();
	}

	private static int transcendantXPRequirement( int level ) {
		return Hero.maxExp( Math.max( 1, level ) );
	}

	protected String appendRarityInfo( String info ) {
		if (!showsRarityStats()) return info;

		StringBuilder builder = new StringBuilder( info );
		builder.append( "\n\nRarity: @@C" )
				.append( String.format( "%06X", rarity.color() & 0xFFFFFF ) )
				.append( "@@" )
				.append( rarity.displayName() )
				.append( "@@CEND@@" );
		if (isTranscendantRarity()) {
			ensureTranscendantProgress();
			if (transcendantPendingChoices > 0) {
				builder.append( "\nAwaiting choices: " ).append( transcendantPendingChoices );
			}
		}

		appendRarityStatLines( builder );
		return builder.toString();
	}

	private void appendRarityStatLines( StringBuilder builder ) {
		ArrayList<String> statLines = new ArrayList<>();
		boolean compact = rarityStats.size() >= 8;
		for (RarityStat stat : visibleRarityStats()) {
			statLines.add( compact ? stat.compactDisplayText() : stat.displayText() );
		}
		if (statLines.isEmpty()) return;

		if (statLines.size() < 8) {
			for (String line : statLines) {
				builder.append( "\n" ).append( line );
			}
			return;
		}

		int rows = (statLines.size() + 1) / 2;
		int columnWidth = 19;
		for (int i = 0; i < rows; i++) {
			columnWidth = Math.max( columnWidth, visibleTextLength( statLines.get( i ) ) + 3 );
		}
		columnWidth = Math.min( columnWidth, 23 );

		for (int i = 0; i < rows; i++) {
			String left = statLines.get( i );
			builder.append( "\n" ).append( padMarkedText( left, columnWidth ) );
			if (i + rows < statLines.size()) {
				builder.append( statLines.get( i + rows ) );
			}
		}
	}

	private static String padMarkedText( String text, int width ) {
		StringBuilder builder = new StringBuilder( text );
		int padding = Math.max( 2, width - visibleTextLength( text ) );
		for (int i = 0; i < padding; i++) {
			builder.append( ' ' );
		}
		return builder.toString();
	}

	private static int visibleTextLength( String text ) {
		int length = 0;
		for (int i = 0; i < text.length(); ) {
			if (text.startsWith( "@@C", i )) {
				int end = text.indexOf( "@@", i + 3 );
				if (end != -1) {
					i = end + 2;
					continue;
				}
			}
			length++;
			i++;
		}
		return length;
	}

	public static class TranscendantChoice {
		public final RarityStat.Type type;
		public final boolean itemUpgrade;
		public final boolean existing;
		public final int delta;
		public final int oldValue;
		public final int value;

		private TranscendantChoice( RarityStat.Type type, boolean itemUpgrade, boolean existing, int delta, int oldValue, int value ) {
			this.type = type;
			this.itemUpgrade = itemUpgrade;
			this.existing = existing;
			this.delta = Math.max( 1, delta );
			this.oldValue = oldValue;
			this.value = value;
		}

		public static TranscendantChoice itemUpgrade( int oldValue ) {
			return new TranscendantChoice( null, true, false, 1, oldValue, 0 );
		}

		public static TranscendantChoice upgrade( RarityStat.Type type, int delta, int oldValue ) {
			return new TranscendantChoice( type, false, true, delta, oldValue, 0 );
		}

		public static TranscendantChoice add( RarityStat.Type type, int value ) {
			return new TranscendantChoice( type, false, false, 1, 0, value );
		}

		public String label() {
			if (itemUpgrade) {
				return "Upgrade Item +1\n" + oldValue + " -> " + (oldValue + 1);
			}
			if (existing) {
				int displayDelta = type.capsAtHundred() ? Math.min( delta, Math.max( 0, 100 - oldValue ) ) : delta;
				return type.displayName() + " +" + displayDelta + (type.percent() ? "%" : "") + "\n" +
						oldValue + " -> " + (oldValue + displayDelta);
			}
			if (type.hasValue()) {
				return "[NEW] " + type.displayName() + " +" + value + (type.percent() ? "%" : "");
			}
			return "[NEW] " + type.displayName();
		}

		public int displayColor() {
			return itemUpgrade ? ItemRarity.TRANSCENDANT.color() : type.minimumRarity().color();
		}

		private String saveString() {
			if (itemUpgrade) {
				return TRANSCENDANT_ITEM_UPGRADE + ":false:1:" + oldValue + ":0";
			}
			return type.name() + ":" + existing + ":" + delta + ":" + oldValue + ":" + value;
		}

		private static TranscendantChoice fromSaveString( String data ) {
			if (data == null) return null;
			String[] parts = data.split( ":" );
			if (parts.length < 5) return null;
			try {
				if (TRANSCENDANT_ITEM_UPGRADE.equals( parts[0] )) {
					return itemUpgrade( Integer.parseInt( parts[3] ) );
				}
				RarityStat.Type type = RarityStat.Type.valueOf( parts[0] );
				boolean existing = Boolean.parseBoolean( parts[1] );
				int delta = Integer.parseInt( parts[2] );
				int oldValue = Integer.parseInt( parts[3] );
				int value = Integer.parseInt( parts[4] );
				return new TranscendantChoice( type, false, existing, delta, oldValue, value );
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}
	
	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}

	public static void updateQuickslot() {
		GameScene.updateItemDisplays = true;
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	private static final String KEPT_LOST       = "kept_lost";
	private static final String CUSTOM_NOTE_ID = "custom_note_id";
	private static final String RARITY_ROLLED = "rarity_rolled";
	private static final String RARITY = "rarity";
	private static final String RARITY_STATS = "rarity_stats";
	private static final String TRANSCENDANT_LEVEL = "transcendant_level";
	private static final String TRANSCENDANT_XP = "transcendant_xp";
	private static final String TRANSCENDANT_XP_TO_NEXT = "transcendant_xp_to_next";
	private static final String TRANSCENDANT_PENDING_CHOICES = "transcendant_pending_choices";
	private static final String TRANSCENDANT_CHOICE_CACHE = "transcendant_choice_cache";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
		bundle.put( KEPT_LOST, keptThoughLostInvent );
		if (customNoteID != -1)     bundle.put(CUSTOM_NOTE_ID, customNoteID);
		if (rarityRolled) {
			bundle.put( RARITY_ROLLED, rarityRolled );
			bundle.put( RARITY, rarity.name() );
			String[] stats = new String[rarityStats.size()];
			for (int i = 0; i < rarityStats.size(); i++) {
				stats[i] = rarityStats.get( i ).saveString();
			}
			bundle.put( RARITY_STATS, stats );
			if (rarity == ItemRarity.TRANSCENDANT) {
				ensureTranscendantProgress();
				bundle.put( TRANSCENDANT_LEVEL, transcendantLevel );
				bundle.put( TRANSCENDANT_XP, transcendantXP );
				bundle.put( TRANSCENDANT_XP_TO_NEXT, transcendantXPToNext );
				bundle.put( TRANSCENDANT_PENDING_CHOICES, transcendantPendingChoices );
				String[] choices = new String[transcendantChoiceCache.size()];
				for (int i = 0; i < transcendantChoiceCache.size(); i++) {
					choices[i] = transcendantChoiceCache.get( i ).saveString();
				}
				bundle.put( TRANSCENDANT_CHOICE_CACHE, choices );
			}
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getInt( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slots when restoring belongings
		if (Belongings.bundleRestoring) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}

		keptThoughLostInvent = bundle.getBoolean( KEPT_LOST );
		if (bundle.contains(CUSTOM_NOTE_ID))    customNoteID = bundle.getInt(CUSTOM_NOTE_ID);

		if (bundle.contains( RARITY_ROLLED )) {
			rarityRolled = bundle.getBoolean( RARITY_ROLLED );
		}
		if (bundle.contains( RARITY )) {
			try {
				rarity = ItemRarity.valueOf( bundle.getString( RARITY ) );
				rarityRolled = true;
			} catch (IllegalArgumentException e) {
				rarity = ItemRarity.COMMON;
			}
		}
		if (bundle.contains( RARITY_STATS )) {
			rarityStats.clear();
			for (String statData : bundle.getStringArray( RARITY_STATS )) {
				RarityStat stat = RarityStat.fromSaveString( statData );
				if (stat != null) rarityStats.add( stat );
			}
		}
		if (bundle.contains( TRANSCENDANT_LEVEL )) {
			transcendantLevel = bundle.getInt( TRANSCENDANT_LEVEL );
			transcendantXP = bundle.getInt( TRANSCENDANT_XP );
			transcendantXPToNext = bundle.getInt( TRANSCENDANT_XP_TO_NEXT );
			transcendantPendingChoices = bundle.getInt( TRANSCENDANT_PENDING_CHOICES );
			transcendantChoiceCache.clear();
			for (String choiceData : bundle.getStringArray( TRANSCENDANT_CHOICE_CACHE )) {
				TranscendantChoice choice = TranscendantChoice.fromSaveString( choiceData );
				if (choice != null) transcendantChoiceCache.add( choice );
			}
		}
		if (isTranscendantRarity()) {
			ensureTranscendantProgress();
		} else {
			clearTranscendantProgress();
		}
	}

	public int targetingPos( Hero user, int dst ){
		return throwPos( user, dst );
	}

	public int throwPos( Hero user, int dst){
		return new Ballistica( user.pos, dst, Ballistica.PROJECTILE ).collisionPos;
	}

	public void throwSound(){
		Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		throwSound();

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);
		
		final float delay = castDelay(user, cell);

		if (enemy != null) {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							enemy.sprite,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							if (i != null) i.onThrow(cell);
							if (curUser.hasTalent(Talent.IMPROVISED_PROJECTILES)
									&& !(Item.this instanceof MissileWeapon)
									&& curUser.buff(Talent.ImprovisedProjectileCooldown.class) == null){
								if (enemy != null && enemy.alignment != curUser.alignment){
									Sample.INSTANCE.play(Assets.Sounds.HIT);
									Buff.affect(enemy, Blindness.class, 1f + curUser.pointsInTalent(Talent.IMPROVISED_PROJECTILES));
									Buff.affect(curUser, Talent.ImprovisedProjectileCooldown.class, 50f);
								}
							}
							if (user.buff(Talent.LethalMomentumTracker.class) != null){
								user.buff(Talent.LethalMomentumTracker.class).detach();
								user.next();
							} else {
								user.spendAndNext(delay);
							}
						}
					});
		} else {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
							new Callback() {
						@Override
						public void call() {
							curUser = user;
							Item i = Item.this.detach(user.belongings.backpack);
							user.spend(delay);
							if (i != null) i.onThrow(cell);
							user.next();
						}
					});
		}
	}
	
	public float castDelay( Char user, int cell ){
		return TIME_TO_THROW;
	}

	public float pickupDelay(){
		return TIME_TO_PICK_UP;
	}
	
	protected static Hero curUser = null;
	protected static Item curItem = null;
	public void setCurrent( Hero hero ){
		curUser = hero;
		curItem = this;
	}

	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};
}
