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

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.HomebaseDefender;
import com.erebus.reclaimedpixeldungeon.items.Ankh;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.armor.ClassArmor;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfExperience;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfInvisibility;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfStrength;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.InventoryItemButton;
import com.erebus.reclaimedpixeldungeon.ui.ItemSlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Halo;
import com.watabou.noosa.ui.Component;

import java.io.IOException;

public class WndDefenderManagement extends Window {

	private static final int WIDTH_DESKTOP = 152;
	private static final int HEIGHT = 170;
	private static final int GAP = 3;
	private static final int SLOT = 24;
	private static final int BTN_HEIGHT = 16;
	private static final int SPRITE_COLUMN = 18;
	private static final int SLOT_WEAPON = 0;
	private static final int SLOT_ARMOR = 1;
	private static final int SLOT_RANGED = 2;
	private static final int DIVIDER_COLOR = 0xFF000000;

	private ScrollPane roster;
	private final int focusDefenderId;
	private final int windowWidth;
	private final float initialScrollY;

	public WndDefenderManagement() {
		this( -1 );
	}

	public WndDefenderManagement( int focusDefenderId ) {
		this( focusDefenderId, 0 );
	}

	private WndDefenderManagement( int focusDefenderId, float initialScrollY ) {
		super();
		this.focusDefenderId = focusDefenderId;
		this.initialScrollY = initialScrollY;
		windowWidth = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );

		String titleText = focusDefenderId >= 0 ? "Defender" : "Defenders";
		RenderedTextBlock title = PixelScene.renderTextBlock( titleText, 9 );
		title.hardlight( Window.TITLE_COLOR );
		title.maxWidth( windowWidth );
		title.setPos( 0, 0 );
		add( title );

		roster = new ScrollPane( new Component() );
		add( roster );

		int windowHeight = ReclaimedWindow.modalHeight( HEIGHT, chrome.marginVer() );
		resize( windowWidth, windowHeight );
		roster.setRect( 0, title.bottom() + GAP, windowWidth, windowHeight - title.bottom() - GAP );
		offset( 0, ReclaimedWindow.modalYOffset( windowHeight, chrome.marginVer() ) );
		buildRoster();
	}

	private void buildRoster() {
		Component content = roster.content();
		content.clear();
		float pos = 0;

		if (Dungeon.homebase == null || Dungeon.homebase.defenders().isEmpty()) {
			RenderedTextBlock none = PixelScene.renderTextBlock( "No defenders have joined the homebase yet.", 6 );
			none.maxWidth( windowWidth );
			none.setPos( 0, pos );
			content.add( none );
			pos = none.bottom() + GAP;
		} else if (focusDefenderId >= 0) {
			HomebaseState.DefenderRecord defender = Dungeon.homebase.defender( focusDefenderId );
			if (defender != null && defender.alive()) {
				pos = addDefender( content, defender, pos );
			} else {
				RenderedTextBlock none = PixelScene.renderTextBlock( "This defender is no longer available.", 6 );
				none.maxWidth( windowWidth );
				none.setPos( 0, pos );
				content.add( none );
				pos = none.bottom() + GAP;
			}
		} else {
			for (HomebaseState.DefenderRecord defender : Dungeon.homebase.defenders()) {
				pos = addDefender( content, defender, pos );
			}
		}

		content.setSize( windowWidth, Math.max( roster.height(), pos ) );
		roster.setSize( roster.width(), roster.height() );
		roster.scrollTo( 0, initialScrollY );
	}

	@Override
	public void offset( int xOffset, int yOffset ) {
		super.offset( xOffset, yOffset );
		if (roster != null) {
			roster.setRect( roster.left(), roster.top(), roster.width(), roster.height() );
		}
	}

	private float addDefender( Component content, final HomebaseState.DefenderRecord defender, float pos ) {
		if (pos > 0) {
			ColorBlock divider = new ColorBlock( windowWidth, 1, DIVIDER_COLOR );
			divider.x = 0;
			divider.y = pos;
			content.add( divider );
			pos += GAP;
		}

		float rowTop = pos;
		Halo halo = new Halo( 10f, defender.rarity().color(), defenderHaloAlpha( defender ) );
		content.add( halo );

		HomebaseDefenderSprite preview = new HomebaseDefenderSprite( defenderHeroClass( defender ), defenderArmorTier( defender ) );
		content.add( preview );
		preview.x = 1;
		preview.y = rowTop + 1;
		PixelScene.align( preview );
		halo.point( preview.x + preview.width() / 2f, preview.y + preview.height() / 2f + 1 );

		String summary = colorText( Window.WHITE, defender.defenderName() )
				+ "\n" + colorText( defender.rarity().color(), defender.rarity().displayName() )
				+ " " + colorText( defender.rarity().color(), capitalize( defender.archetypeName() ) )
				+ "  " + colorText( Window.TITLE_COLOR, "Lv. " + defender.level() )
				+ "  " + colorText( 0x44CCFF, "XP " + defender.xp() + "/" + defender.xpToNext() )
				+ "\n" + colorText( ItemSlot.WARNING, "STR " + defender.strength() )
				+ "  " + colorText( 0xFFFF44, "Ankh " + defender.ankhs() )
				+ "  " + colorText( ItemSlot.UPGRADED, "Heal " + defender.healingPotions() )
				+ "  " + colorText( Window.WHITE, "Invis " + defender.invisibilityPotions() )
				+ "\n" + colorText( 0xDDCC88, "Inventory " + defender.inventoryUsed() + "/" + defender.inventoryCapacity() )
				+ "  " + defender.specialistBagsOwned() + (defender.specialistBagsOwned() == 1 ? " bag" : " bags")
				+ "\n" + colorText( 0xBBBBBB, "Bags: " + defender.ownedBagSummary() );
		RenderedTextBlock summaryText = PixelScene.renderTextBlock( summary, 6 );
		summaryText.maxWidth( windowWidth - SPRITE_COLUMN );
		summaryText.setPos( SPRITE_COLUMN, pos );
		content.add( summaryText );
		pos = Math.max( summaryText.bottom(), rowTop + Math.max( 16, preview.height() ) ) + GAP;

		WndCurrencyLine pockets = WndCurrencyLine.defenderPockets( defender );
		content.add( pockets );
		pockets.setRect( SPRITE_COLUMN, pos, windowWidth - SPRITE_COLUMN, 0 );
		pos = pockets.bottom() + GAP;

		InventoryItemButton weapon = new EquipButton( defender, SLOT_WEAPON );
		content.add( weapon );
		weapon.setRect( 0, pos, SLOT, SLOT );
		weapon.item( defender.weapon() == null ? new WndBag.Placeholder( ItemSpriteSheet.WEAPON_HOLDER ) : defender.weapon() );
		weapon.slot().strengthContext( defender.strength() );

		InventoryItemButton armor = new EquipButton( defender, SLOT_ARMOR );
		content.add( armor );
		armor.setRect( SLOT + GAP, pos, SLOT, SLOT );
		armor.item( defender.armor() == null ? new WndBag.Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) : defender.armor() );
		armor.slot().strengthContext( defender.strength() );

		InventoryItemButton ranged = new EquipButton( defender, SLOT_RANGED );
		content.add( ranged );
		ranged.setRect( 2 * (SLOT + GAP), pos, SLOT, SLOT );
		ranged.item( defender.ranged() == null ? new WndBag.Placeholder( ItemSpriteSheet.WAND_HOLDER ) : defender.ranged() );
		ranged.slot().strengthContext( defender.strength() );

		RenderedTextBlock gear = PixelScene.renderTextBlock(
				DefenderUi.equipmentLines( defender.weapon(), defender.armor(), defender.ranged(), defender.strength() ), 5 );
		gear.maxWidth( windowWidth - 3 * SLOT - 3 * GAP );
		gear.setPos( 3 * (SLOT + GAP), pos + 1 );
		content.add( gear );

		pos = Math.max( Math.max( Math.max( weapon.bottom(), armor.bottom() ), ranged.bottom() ), gear.bottom() ) + GAP;

		RedButton trade = new RedButton( "Trade", 6 ) {
			@Override
			protected void onClick() {
				showWindow( new WndDefenderTrade( defender ) );
			}
		};
		content.add( trade );
		trade.setRect( 0, pos, (windowWidth - GAP) / 2f, BTN_HEIGHT );

		RedButton gift = new RedButton( "Gift Item", 6 ) {
			@Override
			protected void onClick() {
				showGiftOptions( defender );
			}
		};
		content.add( gift );
		gift.setRect( trade.right() + GAP, pos, windowWidth - trade.width() - GAP, BTN_HEIGHT );
		pos = gift.bottom() + GAP * 2;

		return pos;
	}

	private static HeroClass defenderHeroClass( HomebaseState.DefenderRecord defender ) {
		switch (defender.archetype()) {
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

	private static int defenderArmorTier( HomebaseState.DefenderRecord defender ) {
		Armor armor = defender.armor();
		if (armor instanceof ClassArmor) return 6;
		return armor == null ? 0 : armor.tier;
	}

	private static float defenderHaloAlpha( HomebaseState.DefenderRecord defender ) {
		return Math.min( 0.18f, Math.max( 0.08f, defender.rarity().auraAlpha() * 0.45f ) );
	}

	private static String colorText( int color, String text ) {
		return "@@C" + String.format( "%06X", color & 0xFFFFFF ) + "@@" + text + "@@CEND@@";
	}

	private static String capitalize( String text ) {
		if (text == null || text.isEmpty()) return "";
		return Character.toUpperCase( text.charAt( 0 ) ) + text.substring( 1 );
	}

	private class EquipButton extends InventoryItemButton {

		private final HomebaseState.DefenderRecord defender;
		private final int slotType;

		private EquipButton( HomebaseState.DefenderRecord defender, int slotType ) {
			this.defender = defender;
			this.slotType = slotType;
		}

		@Override
		protected void onClick() {
			Item item = equippedItem( defender, slotType );
			if (item == null) {
				selectEquipment( defender, slotType );
			} else {
				showWindow( new WndOptions(
						item.name(),
						DefenderUi.equipmentName( item, defender.strength() ),
						"Replace",
						"Unequip",
						"Cancel" ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							selectEquipment( defender, slotType );
						} else if (index == 1) {
							unequip( defender, slotType );
						}
					}
				} );
			}
		}

		@Override
		protected boolean onLongClick() {
			Item item = equippedItem( defender, slotType );
			if (item != null) {
				showWindow( new WndInfoItem( item ) );
				return true;
			}
			return false;
		}
	}

	private static Item equippedItem( HomebaseState.DefenderRecord defender, int slotType ) {
		switch (slotType) {
			case SLOT_WEAPON:
				return defender.weapon();
			case SLOT_ARMOR:
				return defender.armor();
			case SLOT_RANGED:
				return defender.ranged();
			default:
				return null;
		}
	}

	private void selectEquipment( final HomebaseState.DefenderRecord defender, final int slotType ) {
		selectItem( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				switch (slotType) {
					case SLOT_WEAPON:
						return "Select a melee weapon for " + defender.defenderName();
					case SLOT_ARMOR:
						return "Select armor for " + defender.defenderName();
					case SLOT_RANGED:
						return "Select a wand or thrown weapon for " + defender.defenderName();
					default:
						return "Select equipment for " + defender.defenderName();
				}
			}

			@Override
			public Class<? extends Bag> preferredBag() {
				return Belongings.Backpack.class;
			}

			@Override
			public boolean itemSelectable( Item item ) {
				return item != null
						&& !item.isEquipped( Dungeon.hero )
						&& slotAccepts( slotType, item );
			}

			@Override
			public void onSelect( Item item ) {
				if (item != null) {
					equip( defender, item, slotType );
					WndDefenderManagement.this.hide();
					reopen();
				}
			}
		} );
	}

	private static boolean slotAccepts( int slotType, Item item ) {
		switch (slotType) {
			case SLOT_WEAPON:
				return item instanceof Weapon && !(item instanceof MissileWeapon) && !(item instanceof SpiritBow);
			case SLOT_ARMOR:
				return item instanceof Armor;
			case SLOT_RANGED:
				return HomebaseState.DefenderRecord.isRangedWeapon( item );
			default:
				return false;
		}
	}

	private void equip( HomebaseState.DefenderRecord defender, Item item, int slotType ) {
		Item current = equippedItem( defender, slotType );
		if (HomebaseState.DefenderRecord.isCursedEquipment( current )) {
			GLog.w( defender.defenderName() + " cannot replace cursed gear until it is cleansed." );
			return;
		}
		Item equipped = item.detachAll( Dungeon.hero.belongings.backpack );
		if (equipped == null) return;

		Item previous;
		switch (slotType) {
			case SLOT_WEAPON:
				previous = defender.equipWeapon( (Weapon)equipped );
				break;
			case SLOT_ARMOR:
				previous = defender.equipArmor( (Armor)equipped );
				break;
			case SLOT_RANGED:
				previous = defender.equipRanged( equipped );
				break;
			default:
				returnToHeroOrDrop( equipped );
				return;
		}
		returnToHeroOrDrop( previous );
		refreshLiveDefender( defender );
		String payment = previous == null ? defender.payForGift( equipped ) : "";
		GLog.p( defender.defenderName() + " equips " + equipped.name() + "." + payment );
		save();
	}

	private void unequip( HomebaseState.DefenderRecord defender, int slotType ) {
		if (HomebaseState.DefenderRecord.isCursedEquipment( equippedItem( defender, slotType ) )) {
			GLog.w( defender.defenderName() + " cannot remove cursed gear until it is cleansed." );
			return;
		}
		Item previous;
		switch (slotType) {
			case SLOT_WEAPON:
				previous = defender.equipWeapon( null );
				break;
			case SLOT_ARMOR:
				previous = defender.equipArmor( null );
				break;
			case SLOT_RANGED:
				previous = defender.equipRanged( null );
				break;
			default:
				return;
		}
		returnToHeroOrDrop( previous );
		refreshLiveDefender( defender );
		save();
		float scrollY = currentScrollY();
		hide();
		reopen( scrollY );
	}

	private void showGiftOptions( final HomebaseState.DefenderRecord defender ) {
		showWindow( new WndOptions(
				defender.defenderName(),
				"Give supplies to improve this defender.",
				"Strength Potion",
				"Healing Potion",
				"Experience Potion",
				"Invisibility Potion",
				"Scroll of Upgrade",
				"Scroll of Remove Curse",
				"Ankh",
				"Cancel" ) {
			@Override
			protected boolean enabled( int index ) {
				switch (index) {
					case 0:
						return Dungeon.hero.belongings.getItem( PotionOfStrength.class ) != null;
					case 1:
						return Dungeon.hero.belongings.getItem( PotionOfHealing.class ) != null;
					case 2:
						return Dungeon.hero.belongings.getItem( PotionOfExperience.class ) != null;
					case 3:
						return Dungeon.hero.belongings.getItem( PotionOfInvisibility.class ) != null;
					case 4:
						return Dungeon.hero.belongings.getItem( ScrollOfUpgrade.class ) != null
								&& defender.canAcceptInventoryGift( new ScrollOfUpgrade() );
					case 5:
						return Dungeon.hero.belongings.getItem( ScrollOfRemoveCurse.class ) != null
								&& defender.canAcceptInventoryGift( new ScrollOfRemoveCurse() );
					case 6:
						return Dungeon.hero.belongings.getItem( Ankh.class ) != null;
					default:
						return true;
				}
			}

			@Override
			protected void onSelect( int index ) {
				switch (index) {
					case 0:
						giftStrength( defender );
						break;
					case 1:
						giftHealing( defender );
						break;
					case 2:
						giftExperience( defender );
						break;
					case 3:
						giftInvisibility( defender );
						break;
					case 4:
						giftUpgrade( defender );
						break;
					case 5:
						giftRemoveCurse( defender );
						break;
					case 6:
						giftAnkh( defender );
						break;
					default:
						return;
				}
				float scrollY = currentScrollY();
				WndDefenderManagement.this.hide();
				reopen( scrollY );
			}
		} );
	}

	private void giftStrength( HomebaseState.DefenderRecord defender ) {
		Item potion = Dungeon.hero.belongings.getItem( PotionOfStrength.class );
		if (potion == null) return;
		Item gift = potion.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		defender.increaseStrength( 1 );
		refreshLiveDefender( defender );
		GLog.p( defender.defenderName() + "'s strength increases to " + defender.strength() + "."
				+ defender.payForGift( gift ) );
		save();
	}

	private void giftHealing( HomebaseState.DefenderRecord defender ) {
		Item potion = Dungeon.hero.belongings.getItem( PotionOfHealing.class );
		if (potion == null) return;
		Item gift = potion.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		defender.addHealingPotion();
		HomebaseDefender live = findLiveDefender( defender.id() );
		boolean drank = live != null && live.useStoredHealingPotion( false );
		if (!drank) {
			GLog.p( defender.defenderName() + " stores a healing potion." + defender.payForGift( gift ) );
		} else {
			GLog.p( defender.defenderName() + " drinks the healing potion." + defender.payForGift( gift ) );
		}
		save();
	}

	private void giftExperience( HomebaseState.DefenderRecord defender ) {
		Item potion = Dungeon.hero.belongings.getItem( PotionOfExperience.class );
		if (potion == null) return;
		Item gift = potion.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		int amount = defender.xpToNext();
		boolean levelled = defender.gainExperience( amount );
		refreshLiveDefender( defender );
		HomebaseDefender live = findLiveDefender( defender.id() );
		if (levelled && live != null) {
			live.showLevelUpEffect();
		}
		GLog.p( defender.defenderName() + " gains " + amount + " experience" + (levelled ? " and levels up." : ".")
				+ defender.payForGift( gift ) );
		save();
	}

	private void giftInvisibility( HomebaseState.DefenderRecord defender ) {
		Item potion = Dungeon.hero.belongings.getItem( PotionOfInvisibility.class );
		if (potion == null) return;
		Item gift = potion.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		defender.addInvisibilityPotion();
		refreshLiveDefender( defender );
		GLog.p( defender.defenderName() + " stores an invisibility potion." + defender.payForGift( gift ) );
		save();
	}

	private void giftUpgrade( HomebaseState.DefenderRecord defender ) {
		Item scroll = Dungeon.hero.belongings.getItem( ScrollOfUpgrade.class );
		if (scroll == null) return;
		Item gift = scroll.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		if (!defender.acceptInventoryGift( gift )) {
			returnToHeroOrDrop( gift );
			GLog.w( defender.defenderName() + " has no room for that scroll." );
			return;
		}
		refreshLiveDefender( defender );
		GLog.p( defender.defenderName() + " accepts the scroll for a strategic upgrade."
				+ defender.payForGift( gift ) );
		save();
	}

	private void giftRemoveCurse( HomebaseState.DefenderRecord defender ) {
		Item scroll = Dungeon.hero.belongings.getItem( ScrollOfRemoveCurse.class );
		if (scroll == null) return;
		Item gift = scroll.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		if (!defender.acceptInventoryGift( gift )) {
			returnToHeroOrDrop( gift );
			GLog.w( defender.defenderName() + " has no room for that scroll." );
			return;
		}
		refreshLiveDefender( defender );
		GLog.p( defender.defenderName() + " stores the scroll and cleanses cursed gear when needed."
				+ defender.payForGift( gift ) );
		save();
	}

	private void giftAnkh( HomebaseState.DefenderRecord defender ) {
		Item ankh = Dungeon.hero.belongings.getItem( Ankh.class );
		if (ankh == null) return;
		Item gift = ankh.detach( Dungeon.hero.belongings.backpack );
		if (gift == null) return;
		defender.addAnkh();
		GLog.p( defender.defenderName() + " accepts an ankh." + defender.payForGift( gift ) );
		save();
	}

	private HomebaseDefender findLiveDefender( int id ) {
		if (Dungeon.level == null) return null;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob instanceof HomebaseDefender
					&& ((HomebaseDefender)mob).defenderId() == id
					&& mob.isAlive()) {
				return (HomebaseDefender)mob;
			}
		}
		return null;
	}

	private void refreshLiveDefender( HomebaseState.DefenderRecord defender ) {
		HomebaseDefender live = findLiveDefender( defender.id() );
		if (live != null) {
			live.refreshFromRecord();
		}
	}

	private void returnToHeroOrDrop( Item item ) {
		if (item == null) return;
		if (!item.collect( Dungeon.hero.belongings.backpack )) {
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void selectItem( WndBag.ItemSelector selector ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.selectItem( selector );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( WndBag.lastBag( selector ) );
		}
	}

	private static void showWindow( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	private void reopen() {
		reopen( currentScrollY() );
	}

	private void reopen( float scrollY ) {
		showWindow( new WndDefenderManagement( focusDefenderId, scrollY ) );
	}

	private float currentScrollY() {
		if (roster != null && roster.content() != null && roster.content().camera != null) {
			return roster.content().camera.scroll.y;
		}
		return initialScrollY;
	}

	private void save() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}
}
