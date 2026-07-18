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

package com.erebus.reclaimedpixeldungeon.actors.hero;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Amok;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bleeding;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Charm;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Corrosion;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Cripple;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Daze;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Degrade;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Doom;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Dread;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Frost;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hex;
import com.erebus.reclaimedpixeldungeon.actors.buffs.LostInventory;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicalSleep;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Ooze;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Poison;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Roots;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Sleep;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Slow;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Terror;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vertigo;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vulnerable;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Weakness;
import com.erebus.reclaimedpixeldungeon.items.EquipableItem;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.KindOfWeapon;
import com.erebus.reclaimedpixeldungeon.items.KindofMisc;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.armor.ClassArmor;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.bags.TrinketBag;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.erebus.reclaimedpixeldungeon.items.trinkets.ShardOfOblivion;
import com.erebus.reclaimedpixeldungeon.items.trinkets.Trinket;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static final int ARTIFACT_SLOT_COUNT = 3;
	public static final int FLEX_SLOT_COUNT = 2;
	public static final int RING_SLOT_COUNT = 3;
	public static final int EQUIPMENT_SLOT_COUNT = 2 + ARTIFACT_SLOT_COUNT + FLEX_SLOT_COUNT + RING_SLOT_COUNT;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public Artifact artifact = null;
	public Artifact artifact2 = null;
	public Artifact artifact3 = null;
	public KindofMisc misc = null;
	public KindofMisc misc2 = null;
	public Ring ring = null;
	public Ring ring2 = null;
	public Ring ring3 = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public Artifact artifact2(){
		if (!lostInventory() || (artifact2 != null && artifact2.keptThroughLostInventory())){
			return artifact2;
		} else {
			return null;
		}
	}

	public Artifact artifact3(){
		if (!lostInventory() || (artifact3 != null && artifact3.keptThroughLostInventory())){
			return artifact3;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	public KindofMisc misc2(){
		if (!lostInventory() || (misc2 != null && misc2.keptThroughLostInventory())){
			return misc2;
		} else {
			return null;
		}
	}

	public Ring ring(){
		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	public Ring ring2(){
		if (!lostInventory() || (ring2 != null && ring2.keptThroughLostInventory())){
			return ring2;
		} else {
			return null;
		}
	}

	public Ring ring3(){
		if (!lostInventory() || (ring3 != null && ring3.keptThroughLostInventory())){
			return ring3;
		} else {
			return null;
		}
	}

	public int equippedRarityStat( RarityStat.Type type ) {
		int value = 0;
		value += equippedRarityStat( weapon(), type );
		value += equippedRarityStat( armor(), type );
		for (KindofMisc item : equippedMiscItems()) {
			value += equippedRarityStat( item, type );
		}
		value += equippedRarityStat( secondWep(), type );
		value += trinketRarityStat( type );
		return value;
	}

	public int equippedRarityResistance( Class effect ) {
		if (effect == null) return 0;

		int resistance = 0;
		if (Burning.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.FIRE_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.FIRE_RESISTANCE );
		}
		if (Blindness.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.BLINDNESS_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.BLINDNESS_RESISTANCE );
		}
		if (Chill.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.FROST_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.CHILL_RESISTANCE );
		}
		if (Frost.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.FROST_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.FROST_RESISTANCE );
		}
		if (Corrosion.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.CORROSION_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.CORROSION_RESISTANCE );
		}
		if (Ooze.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.OOZE_RESISTANCE );
		}
		if (Cripple.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.CRIPPLE_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.CRIPPLE_RESISTANCE );
		}
		if (Daze.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.DAZE_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.DAZE_RESISTANCE );
		}
		if (Hex.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.HEX_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.HEX_RESISTANCE );
		}
		if (Poison.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.POISON_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.POISON_RESISTANCE );
		}
		if (Roots.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.ROOT_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.ROOT_RESISTANCE );
		}
		if (Slow.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.SLOW_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.SLOW_RESISTANCE );
		}
		if (Vertigo.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.VERTIGO_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.VERTIGO_RESISTANCE );
		}
		if (Vulnerable.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.VULNERABLE_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.VULNERABLE_RESISTANCE );
		}
		if (Bleeding.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.BLEED_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.BLEED_RESISTANCE );
		}
		if (Paralysis.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.STUN_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.STUN_RESISTANCE );
		}
		if (Weakness.class.isAssignableFrom( effect )) {
			resistance += equippedRarityStat( RarityStat.Type.WEAKNESS_RESISTANCE );
			resistance += homebaseResistance( HomebaseState.Training.WEAKNESS_RESISTANCE );
		}
		if (Charm.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.CHARM_RESISTANCE );
		}
		if (Terror.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.TERROR_RESISTANCE );
		}
		if (Dread.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.DREAD_RESISTANCE );
		}
		if (Sleep.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.SLEEP_RESISTANCE );
		}
		if (MagicalSleep.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.MAGICAL_SLEEP_RESISTANCE );
		}
		if (Amok.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.AMOK_RESISTANCE );
		}
		if (Degrade.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.DEGRADE_RESISTANCE );
		}
		if (Doom.class.isAssignableFrom( effect )) {
			resistance += homebaseResistance( HomebaseState.Training.DOOM_RESISTANCE );
		}
		return resistance;
	}

	private int homebaseResistance( HomebaseState.Training training ) {
		return Dungeon.homebase == null ? 0 : Dungeon.homebase.trainingBonus( training );
	}

	private int equippedRarityStat( Item item, RarityStat.Type type ) {
		if (item == null || type == null) return 0;
		return item.rarityStat( type );
	}

	public boolean gainTranscendantXP( int exp ) {
		if (exp <= 0) return false;

		ArrayList<Item> transcendantItems = new ArrayList<>();
		for (Item item : this) {
			if (item.canGainTranscendantXP()) transcendantItems.add( item );
		}
		if (transcendantItems.isEmpty()) return false;

		int baseShare = exp / transcendantItems.size();
		int remainder = exp % transcendantItems.size();
		ArrayList<Item> bonusItems = new ArrayList<>( transcendantItems );
		ArrayList<Item> selectedBonusItems = new ArrayList<>();
		while (remainder > 0 && !bonusItems.isEmpty()) {
			selectedBonusItems.add( bonusItems.remove( Random.Int( bonusItems.size() ) ) );
			remainder--;
		}

		boolean leveled = false;
		for (Item item : transcendantItems) {
			int share = baseShare + (selectedBonusItems.contains( item ) ? 1 : 0);
			if (share > 0) leveled |= item.addTranscendantXP( share );
		}

		if (leveled) {
			for (Item item : transcendantItems) {
				if (item.hasPendingTranscendantChoice()) {
					item.showTranscendantChoice();
					break;
				}
			}
		}
		return leveled;
	}

	private int trinketRarityStat( RarityStat.Type type ) {
		if (type == null) return 0;

		int value = 0;
		boolean lostInvent = lostInventory();
		for (Item item : this) {
			if (item instanceof Trinket && (!lostInvent || item.keptThroughLostInventory())) {
				value += item.rarityStat( type );
			}
		}
		return value;
	}

	public<T extends Trinket> T getActiveTrinket( Class<T> trinketClass ) {
		boolean lostInvent = lostInventory();
		T strongest = null;
		for (Item item : this) {
			if (trinketClass.isInstance( item ) && (!lostInvent || item.keptThroughLostInventory())) {
				T trinket = trinketClass.cast( item );
				if (strongest == null || trinket.buffedLvl() > strongest.buffedLvl()) {
					strongest = trinket;
				}
			}
		}
		return strongest;
	}

	public Item equipmentItem( int index ) {
		switch (index) {
			case 0: return weapon;
			case 1: return armor;
			case 2: return artifact;
			case 3: return artifact2;
			case 4: return artifact3;
			case 5: return misc;
			case 6: return misc2;
			case 7: return ring;
			case 8: return ring2;
			case 9: return ring3;
			default: return null;
		}
	}

	public int equipmentPlaceholder( int index ) {
		switch (index) {
			case 0: return ItemSpriteSheet.WEAPON_HOLDER;
			case 1: return ItemSpriteSheet.ARMOR_HOLDER;
			case 2:
			case 3:
			case 4:
				return ItemSpriteSheet.ARTIFACT_HOLDER;
			case 5:
			case 6:
				return ItemSpriteSheet.SOMETHING;
			case 7:
			case 8:
			case 9:
				return ItemSpriteSheet.RING_HOLDER;
			default:
				return ItemSpriteSheet.SOMETHING;
		}
	}

	public ArrayList<KindofMisc> equippedMiscItems() {
		ArrayList<KindofMisc> items = new ArrayList<>();
		addIfAvailable(items, artifact());
		addIfAvailable(items, artifact2());
		addIfAvailable(items, artifact3());
		addIfAvailable(items, misc());
		addIfAvailable(items, misc2());
		addIfAvailable(items, ring());
		addIfAvailable(items, ring2());
		addIfAvailable(items, ring3());
		return items;
	}

	public ArrayList<Ring> equippedRings() {
		ArrayList<Ring> rings = new ArrayList<>();
		for (KindofMisc item : equippedMiscItems()) {
			if (item instanceof Ring) {
				rings.add((Ring)item);
			}
		}
		return rings;
	}

	public ArrayList<Artifact> equippedArtifacts() {
		ArrayList<Artifact> artifacts = new ArrayList<>();
		for (KindofMisc item : equippedMiscItems()) {
			if (item instanceof Artifact) {
				artifacts.add((Artifact)item);
			}
		}
		return artifacts;
	}

	private void addIfAvailable( ArrayList<KindofMisc> items, KindofMisc item ) {
		if (item != null) items.add(item);
	}

	public boolean hasEquippedMiscClass( Class<?> itemClass ) {
		for (KindofMisc item : equippedMiscItems()) {
			if (item.getClass().equals( itemClass )) return true;
		}
		return false;
	}

	public ArrayList<KindofMisc> replacementCandidatesFor( KindofMisc item ) {
		ArrayList<KindofMisc> items = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			KindofMisc equipped = rawMiscSlot( i );
			if (equipped != null && canSlotHold( i, item )) {
				items.add( equipped );
			}
		}
		return items;
	}

	public boolean hasAvailableSlotFor( KindofMisc item ) {
		compactFlexSlotsFor( item );
		return firstEmptySlotFor( item ) != -1;
	}

	public boolean equipMiscItem( KindofMisc item ) {
		compactFlexSlotsFor( item );
		int slot = firstEmptySlotFor( item );
		if (slot == -1) return false;
		setMiscSlot( slot, item );
		return true;
	}

	public void clearMiscItem( KindofMisc item ) {
		for (int i = 0; i < 8; i++) {
			if (rawMiscSlot( i ) == item) {
				setMiscSlot( i, null );
				return;
			}
		}
	}

	public boolean isMiscItemEquipped( KindofMisc item ) {
		if (item == null) return false;
		for (KindofMisc equipped : equippedMiscItems()) {
			if (equipped == item) return true;
		}
		return false;
	}

	private void compactFlexSlotsFor( KindofMisc item ) {
		if (item instanceof Artifact) {
			moveFlexRingsToRingSlots();
		} else if (item instanceof Ring) {
			moveFlexArtifactsToArtifactSlots();
		}
	}

	private void moveFlexRingsToRingSlots() {
		for (int i = 3; i <= 4; i++) {
			KindofMisc flex = rawMiscSlot( i );
			if (flex instanceof Ring) {
				int empty = firstEmptyRingSlot();
				if (empty != -1) {
					setMiscSlot( empty, flex );
					setMiscSlot( i, null );
				}
			}
		}
	}

	private void moveFlexArtifactsToArtifactSlots() {
		for (int i = 3; i <= 4; i++) {
			KindofMisc flex = rawMiscSlot( i );
			if (flex instanceof Artifact) {
				int empty = firstEmptyArtifactSlot();
				if (empty != -1) {
					setMiscSlot( empty, flex );
					setMiscSlot( i, null );
				}
			}
		}
	}

	private int firstEmptySlotFor( KindofMisc item ) {
		if (item instanceof Artifact) {
			int artifactSlot = firstEmptyArtifactSlot();
			if (artifactSlot != -1) return artifactSlot;
			return firstEmptyFlexSlot();
		} else if (item instanceof Ring) {
			int ringSlot = firstEmptyRingSlot();
			if (ringSlot != -1) return ringSlot;
			return firstEmptyFlexSlot();
		}
		return -1;
	}

	private int firstEmptyArtifactSlot() {
		for (int i = 0; i <= 2; i++) {
			if (rawMiscSlot( i ) == null) return i;
		}
		return -1;
	}

	private int firstEmptyFlexSlot() {
		for (int i = 3; i <= 4; i++) {
			if (rawMiscSlot( i ) == null) return i;
		}
		return -1;
	}

	private int firstEmptyRingSlot() {
		for (int i = 5; i <= 7; i++) {
			if (rawMiscSlot( i ) == null) return i;
		}
		return -1;
	}

	private boolean canSlotHold( int slot, KindofMisc item ) {
		if (item instanceof Artifact) return slot <= 4;
		if (item instanceof Ring) return slot >= 3;
		return false;
	}

	private KindofMisc rawMiscSlot( int slot ) {
		switch (slot) {
			case 0: return artifact;
			case 1: return artifact2;
			case 2: return artifact3;
			case 3: return misc;
			case 4: return misc2;
			case 5: return ring;
			case 6: return ring2;
			case 7: return ring3;
			default: return null;
		}
	}

	private void setMiscSlot( int slot, KindofMisc item ) {
		switch (slot) {
			case 0:
				artifact = (Artifact)item;
				break;
			case 1:
				artifact2 = (Artifact)item;
				break;
			case 2:
				artifact3 = (Artifact)item;
				break;
			case 3:
				misc = item;
				break;
			case 4:
				misc2 = item;
				break;
			case 5:
				ring = (Ring)item;
				break;
			case 6:
				ring2 = (Ring)item;
				break;
			case 7:
				ring3 = (Ring)item;
				break;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String ARTIFACT2  = "artifact2";
	private static final String ARTIFACT3  = "artifact3";
	private static final String MISC       = "misc";
	private static final String MISC2      = "misc2";
	private static final String RING       = "ring";
	private static final String RING2      = "ring2";
	private static final String RING3      = "ring3";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifact );
		bundle.put( ARTIFACT2, artifact2 );
		bundle.put( ARTIFACT3, artifact3 );
		bundle.put( MISC, misc );
		bundle.put( MISC2, misc2 );
		bundle.put( RING, ring );
		bundle.put( RING2, ring2 );
		bundle.put( RING3, ring3 );
		bundle.put( SECOND_WEP, secondWep );
	}

	public static boolean bundleRestoring = false;
	
	public void restoreFromBundle( Bundle bundle ) {
		bundleRestoring = true;
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		armor = (Armor)bundle.get( ARMOR );
		artifact = (Artifact) bundle.get(ARTIFACT);
		artifact2 = (Artifact) bundle.get(ARTIFACT2);
		artifact3 = (Artifact) bundle.get(ARTIFACT3);
		misc = (KindofMisc) bundle.get(MISC);
		misc2 = (KindofMisc) bundle.get(MISC2);
		ring = (Ring) bundle.get(RING);
		ring2 = (Ring) bundle.get(RING2);
		ring3 = (Ring) bundle.get(RING3);
		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		activateEquipped();

		bundleRestoring = false;
	}

	public void activateEquipped(){
		if (weapon() != null)       weapon().activate(owner);
		if (armor() != null)        armor().activate( owner );
		for (KindofMisc item : equippedMiscItems()) {
			item.activate( owner );
		}
		if (secondWep() != null)    secondWep().activate(owner);
	}

	public void clear(){
		backpack.clear();
		weapon = secondWep = null;
		armor = null;
		artifact = artifact2 = artifact3 = null;
		misc = misc2 = null;
		ring = ring2 = ring3 = null;
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		for (KindofMisc item : equippedMiscItems()) {
			if (item instanceof Artifact) {
				//oblivion shard does not prevent artifact IDing
				item.identify();
				Badges.validateItemLevelAquired(item);
			} else if (item instanceof Ring) {
				if (ShardOfOblivion.passiveIDDisabled()){
					((Ring)item).setIDReady();
				} else {
					item.identify();
					Badges.validateItemLevelAquired(item);
				}
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ArrayList<Item> items = new ArrayList<>();
		items.add( armor() );
		items.add( weapon() );
		items.addAll( equippedMiscItems() );
		items.add( secondWep() );
		ScrollOfRemoveCurse.uncurse( owner, items.toArray( new Item[0] ) );
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		private int lastReturnedEquippedIndex = -1;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {
				weapon, armor,
				artifact, artifact2, artifact3,
				misc, misc2,
				ring, ring2, ring3,
				secondWep
		};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index];
				index++;
				if (item != null) {
					lastReturnedEquippedIndex = index - 1;
					return item;
				}
			}
			
			lastReturnedEquippedIndex = -1;
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (lastReturnedEquippedIndex) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = artifact2 = null;
				break;
			case 4:
				equipped[4] = artifact3 = null;
				break;
			case 5:
				equipped[5] = misc = null;
				break;
			case 6:
				equipped[6] = misc2 = null;
				break;
			case 7:
				equipped[7] = ring = null;
				break;
			case 8:
				equipped[8] = ring2 = null;
				break;
			case 9:
				equipped[9] = ring3 = null;
				break;
			case 10:
				equipped[10] = secondWep = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
