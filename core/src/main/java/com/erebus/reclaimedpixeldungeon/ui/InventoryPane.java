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

package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.Chrome;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.SPDAction;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.ArtifactBag;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.bags.FoodBag;
import com.erebus.reclaimedpixeldungeon.items.bags.KeyHolder;
import com.erebus.reclaimedpixeldungeon.items.bags.MagicalHolster;
import com.erebus.reclaimedpixeldungeon.items.bags.MaterialSatchel;
import com.erebus.reclaimedpixeldungeon.items.bags.PotionBandolier;
import com.erebus.reclaimedpixeldungeon.items.bags.ScrollHolder;
import com.erebus.reclaimedpixeldungeon.items.bags.TrinketBag;
import com.erebus.reclaimedpixeldungeon.items.bags.VelvetPouch;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.materials.ForgeResourceMaterial;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndInfoItem;
import com.erebus.reclaimedpixeldungeon.windows.WndUseItem;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class InventoryPane extends Component {

	private NinePatch bg;
	private NinePatch bg2; //2 backgrounds to reduce transparency

	//used to prevent clicks through the BG normally, or to cancel selectors if they're enabled
	private PointerArea blocker;

	private Signal.Listener<KeyEvent> keyBlocker;

	private static InventoryPane instance;

	private ArrayList<InventorySlot> equipped;
	private ArrayList<InventorySlot> bagItems;
	private Component bagContent;
	private ScrollPane bagScroll;
	private float lastBagScrollY = 0;
	private Bag scrollBag;

	private Image gold;
	private BitmapText goldTxt;
	private Image energy;
	private BitmapText energyTxt;
	private Image[] homebaseResourceIcons;
	private BitmapText[] homebaseResourceTxts;
	private RenderedTextBlock promptTxt;

	private ArrayList<BagButton> bags;

	public static final int WIDTH = 187;
	public static final int HEIGHT = 107;

	private static final int SLOT_WIDTH = 17;
	private static final int SLOT_HEIGHT = 24;
	private static final int SLOT_MARGIN = 1;
	private static final int EQUIPMENT_COLUMNS = 5;
	private static final int MIN_BAG_BUTTONS = 5;
	private static final int FORGE_RESOURCE_START = HomebaseState.Material.values().length;
	private static final int EMERALD_RESOURCE_INDEX = FORGE_RESOURCE_START + HomebaseState.ForgeResource.values().length;
	public static final int HOMEBASE_RESOURCE_COUNT = EMERALD_RESOURCE_INDEX + 1;
	private static final int BAG_BUTTONS_PER_ROW = 5;
	private static final int BAG_BUTTON_HEIGHT = 14;
	private static final float CURRENCY_SCALE = 0.75f;
	private static final float CURRENCY_ROW_HEIGHT = 7f;
	private static final float CURRENCY_ENTRY_GAP = 2f;
	private int activeBagButtonCount = 1;
	private int activeBagSlotCount = 20;

	private WndBag.ItemSelector selector;

	public static Bag lastBag;

	private boolean lastEnabled = true;

	private static Image crossB;
	private static Image crossM;

	private static boolean targeting = false;
	private static InventorySlot targetingSlot = null;
	public static Char lastTarget = null;

	public InventoryPane(){
		super();
		instance = this;
	}

	@Override
	public synchronized void destroy() {
		KeyEvent.removeKeyListener(keyBlocker);
		super.destroy();
		if (instance == this) instance = null;
	}

	@Override
	protected void createChildren() {

		bg = Chrome.get(Chrome.Type.TOAST_TR_HEAVY);
		add(bg);

		blocker = new PointerArea(0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height){
			@Override
			protected void onClick(PointerEvent event) {
				if (selector != null && !bg.overlapsScreenPoint((int)event.current.x, (int)event.current.y)){
					//any windows opened as a consequence of this should be centered on the inventory
					GameScene.centerNextWndOnInvPane();
					selector.onSelect(null);
					selector = null;
					updateInventory();
				}
			}
		};
		blocker.target = bg; //targets bg when there is no selector, otherwise targets itself
		add (blocker);

		keyBlocker = new Signal.Listener<KeyEvent>(){
			@Override
			public boolean onSignal(KeyEvent keyEvent) {
				if (keyEvent.pressed && isSelecting() && InventoryPane.this.visible
						&& KeyBindings.getActionForKey(keyEvent) != SPDAction.BAG_1
						&& KeyBindings.getActionForKey(keyEvent) != SPDAction.BAG_2
						&& KeyBindings.getActionForKey(keyEvent) != SPDAction.BAG_3
						&& KeyBindings.getActionForKey(keyEvent) != SPDAction.BAG_4
						&& KeyBindings.getActionForKey(keyEvent) != SPDAction.BAG_5){
					//any windows opened as a consequence of this should be centered on the inventory
					GameScene.centerNextWndOnInvPane();
					selector.onSelect(null);
					selector = null;
					updateInventory();
					return true;
				}
				return false;
			}
		};

		equipped = new ArrayList<>();
		for (int i = 0; i < Belongings.EQUIPMENT_SLOT_COUNT; i++){
			InventorySlot btn = new InventoryPaneSlot(null);
			equipped.add(btn);
			add(btn);
		}

		gold = Icons.get(Icons.COIN_SML);
		gold.scale.set( PixelScene.align( CURRENCY_SCALE ) );
		add(gold);
		goldTxt = new BitmapText(PixelScene.pixelFont);
		goldTxt.scale.set( PixelScene.align( CURRENCY_SCALE ) );
		goldTxt.hardlight(Window.TITLE_COLOR);
		add(goldTxt);

		energy = Icons.get(Icons.ENERGY_SML);
		energy.scale.set( PixelScene.align( CURRENCY_SCALE ) );
		add(energy);
		energyTxt = new BitmapText(PixelScene.pixelFont);
		energyTxt.scale.set( PixelScene.align( CURRENCY_SCALE ) );
		energyTxt.hardlight(0x44CCFF);
		add(energyTxt);

		homebaseResourceIcons = new Image[HOMEBASE_RESOURCE_COUNT];
		homebaseResourceTxts = new BitmapText[HOMEBASE_RESOURCE_COUNT];
		for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
			homebaseResourceIcons[i] = new ItemSprite( homebaseResourceIcon( i ) );
			homebaseResourceIcons[i].scale.set( PixelScene.align( CURRENCY_SCALE ) );
			homebaseResourceIcons[i].visible = false;
			add( homebaseResourceIcons[i] );

			homebaseResourceTxts[i] = new BitmapText(PixelScene.pixelFont);
			homebaseResourceTxts[i].scale.set( PixelScene.align( CURRENCY_SCALE ) );
			homebaseResourceTxts[i].hardlight( homebaseResourceColor( i ) );
			homebaseResourceTxts[i].visible = false;
			add( homebaseResourceTxts[i] );
		}

		promptTxt = PixelScene.renderTextBlock(6);
		promptTxt.hardlight(Window.TITLE_COLOR);
		add(promptTxt);

		bagContent = new Component();
		bagScroll = new ScrollPane( bagContent );
		add( bagScroll );

		bagItems = new ArrayList<>();
		for (int i = 0; i < 20; i++){
			InventorySlot btn = new InventoryPaneSlot(null);
			bagItems.add(btn);
			bagContent.add(btn);
		}

		bags = new ArrayList<>();
		ensureBagButtons( MIN_BAG_BUTTONS );

		crossB = Icons.TARGET.get();
		crossB.visible = false;
		add( crossB );

		crossM = new Image();
		crossM.copy( crossB );

		width = desiredWidthForBagCount( activeBagButtonCount );
		height = desiredHeightForBagCount( activeBagButtonCount );

		lastEnabled = true;
		updateInventory();
	}

	@Override
	protected void layout() {
		width = desiredWidthForBagCount( activeBagButtonCount );
		height = desiredHeightForBagCount( activeBagButtonCount );

		bg.x = x;
		bg.y = y;
		bg.size(width, height);

		float left = x+4;
		float top = y+4;
		for (int idx = 0; idx < equipped.size(); idx++){
			InventorySlot i = equipped.get(idx);
			i.setRect(left, top, SLOT_WIDTH, SLOT_HEIGHT);
			if ((idx + 1) % EQUIPMENT_COLUMNS == 0) {
				left = x+4;
				top = i.bottom()+1;
			} else {
				left = i.right()+1;
			}
		}

		left = x + 4 + EQUIPMENT_COLUMNS * (SLOT_WIDTH + 1);
		promptTxt.maxWidth((int) (width - (left - x) - bg.marginRight()));
		if (promptTxt.height() > 10){
			promptTxt.setPos(left, y + 2 + (12 - promptTxt.height()) / 2);
		} else {
			promptTxt.setPos(left, y + 4 + (10 - promptTxt.height()) / 2);
		}

		layoutCurrencyIndicators(
				left,
				y + 5.5f,
				width - (left - x) - bg.marginRight() );

		left = x + 4 + EQUIPMENT_COLUMNS * (SLOT_WIDTH + 1);
		top = y + 10 + SLOT_HEIGHT + 1 + (SLOT_HEIGHT - 14) / 2f;
		for (int idx = 0; idx < bags.size(); idx++){
			BagButton b = bags.get( idx );
			int row = idx / BAG_BUTTONS_PER_ROW;
			int col = idx % BAG_BUTTONS_PER_ROW;
			b.setRect(
					left + col * (SLOT_WIDTH + 1),
					top - row * (BAG_BUTTON_HEIGHT + 1),
					SLOT_WIDTH,
					BAG_BUTTON_HEIGHT );
		}

		int bagCols = Math.max( 1, (int)((width - 8 + SLOT_MARGIN) / (SLOT_WIDTH + SLOT_MARGIN)) );
		int bagRows = (int)Math.ceil( activeBagSlotCount / (float)bagCols );
		int bagScrollX = (int)x + 4;
		int bagScrollY = (int)y + 4 + (SLOT_HEIGHT + SLOT_MARGIN) * 2;
		int bagScrollWidth = (int)width - 8;
		int bagVisibleRows = 2;
		int bagScrollHeight = SLOT_HEIGHT * bagVisibleRows + SLOT_MARGIN * (bagVisibleRows - 1);
		int bagContentHeight = Math.max( bagScrollHeight, bagRows * SLOT_HEIGHT + Math.max( 0, bagRows - 1 ) * SLOT_MARGIN );
		bagContent.setSize( bagScrollWidth, bagContentHeight );
		bagScroll.setRect( bagScrollX, bagScrollY, bagScrollWidth, bagScrollHeight );

		left = 0;
		top = 0;
		for (InventorySlot b : bagItems){
			b.setRect(left, top, SLOT_WIDTH, SLOT_HEIGHT);
			left = b.right() + SLOT_MARGIN;
			if (left + SLOT_WIDTH > bagScrollWidth + 0.1f){
				left = 0;
				top += SLOT_HEIGHT + SLOT_MARGIN;
			}
		}
		bagScroll.scrollTo( 0, Math.min( lastBagScrollY, Math.max( 0, bagContent.height() - bagScroll.height() ) ) );

		super.layout();
	}
	
	public void alpha( float value ){
		bg.alpha( value );
		
		for (InventorySlot slot : equipped){
			slot.alpha( value );
		}
		for (InventorySlot slot : bagItems){
			slot.alpha( value );
		}
		
		gold.alpha(value);
		goldTxt.alpha(value);
		energy.alpha(value);
		energyTxt.alpha(value);
		for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
			homebaseResourceIcons[i].alpha( value );
			homebaseResourceTxts[i].alpha( value );
		}

		for (BagButton bag : bags){
			bag.alpha( value );
		}
	}

	public static void refresh(){
		if (instance != null) instance.updateInventory();
	}

	public void updateInventory(){
		if (selector == null){
			blocker.target = bg;
			KeyEvent.removeKeyListener(keyBlocker);
		} else {
			blocker.target = blocker;
			KeyEvent.addKeyListener(keyBlocker);
		}

		Belongings stuff = Dungeon.hero.belongings;

		if (lastBag == null || !stuff.getBags().contains(lastBag)){
			lastBag = stuff.backpack;
		}
		if (scrollBag != lastBag) {
			scrollBag = lastBag;
			lastBagScrollY = 0;
		} else if (bagContent != null && bagContent.camera != null) {
			lastBagScrollY = bagContent.camera.scroll.y;
		}

		for (int i = 0; i < Belongings.EQUIPMENT_SLOT_COUNT; i++) {
			Item equippedItem = stuff.equipmentItem( i );
			int placeholder = stuff.equipmentPlaceholder( i );
			equipped.get(i).item(equippedItem == null ? new WndBag.Placeholder( placeholder ) : equippedItem);
		}

		ArrayList<Item> items = (ArrayList<Item>) lastBag.items.clone();

		if (lastBag == stuff.backpack && stuff.secondWep != null){
			items.add(0, stuff.secondWep);
		}

		activeBagSlotCount = visibleBagSlotCount( lastBag, items );
		ensureBagItemSlots( activeBagSlotCount );

		int j = 0;

		for (int i = 0; i < bagItems.size(); i++){
			if (i == 0 && lastBag != stuff.backpack){
				bagItems.get(i).item(lastBag);
				continue;
			}
			if (items.size() > j){
				if (items.get(j) instanceof Bag){
					j++;
					i--;
					continue;
				}
				bagItems.get(i).item(items.get(j));
				j++;
			} else {
				bagItems.get(i).item(null);
			}
		}

		if (selector == null) {
			promptTxt.visible = false;

			goldTxt.text(compactHomebaseAmount(Dungeon.gold));
			goldTxt.measure();
			goldTxt.visible = gold.visible = true;

			energyTxt.text(compactHomebaseAmount(Dungeon.energy));
			energyTxt.measure();
			energyTxt.visible = energy.visible = Dungeon.energy > 0;

			boolean showHomebaseResources = showHomebaseResources();
			for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
				int amount = homebaseResourceAmount( i );
				homebaseResourceTxts[i].text( compactHomebaseAmount( amount ) );
				homebaseResourceTxts[i].measure();
				homebaseResourceIcons[i].visible = homebaseResourceTxts[i].visible = showHomebaseResources && amount > 0;
			}
		} else {
			promptTxt.text(selector.textPrompt());
			promptTxt.visible = true;

			goldTxt.visible = gold.visible = false;
			energyTxt.visible = energy.visible = false;
			for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
				homebaseResourceIcons[i].visible = homebaseResourceTxts[i].visible = false;
			}
		}

		ArrayList<Bag> inventBags = stuff.getBags();
		activeBagButtonCount = Math.max( 1, inventBags.size() );
		int bagButtonCount = Math.max( MIN_BAG_BUTTONS, inventBags.size() );
		ensureBagButtons( bagButtonCount );
		updateSizeForBagCount( activeBagButtonCount );
		for (int i = 0; i < bags.size(); i++){
			if (inventBags.size() > i){
				bags.get(i).bag(inventBags.get(i));
			} else {
				bags.get(i).bag(null);
			}
		}

		boolean lostInvent = Dungeon.hero.belongings.lostInventory();
		for (InventorySlot b : equipped){
			b.enable(lastEnabled
					&& !(b.item() instanceof WndBag.Placeholder)
					&& (selector == null || selector.itemSelectable(b.item()))
					&& (!lostInvent || b.item().keptThroughLostInventory()));
		}
		for (InventorySlot b : bagItems){
			b.enable(lastEnabled
					&& b.item() != null
					&& (selector == null || selector.itemSelectable(b.item()))
					&& (!lostInvent || b.item().keptThroughLostInventory()));
		}
		for (BagButton b : bags){
			b.enable(lastEnabled);
		}

		goldTxt.alpha( lastEnabled ? 1f : 0.3f );
		gold.alpha( lastEnabled ? 1f : 0.3f );
		energyTxt.alpha( lastEnabled ? 1f : 0.3f );
		energy.alpha( lastEnabled ? 1f : 0.3f );
		for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
			homebaseResourceTxts[i].alpha( lastEnabled ? 1f : 0.3f );
			homebaseResourceIcons[i].alpha( lastEnabled ? 1f : 0.3f );
		}

		if (camera() != null) {
			layout();
		}
	}

	private int visibleBagSlotCount( Bag bag, ArrayList<Item> items ) {
		int visibleSlots = bag == Dungeon.hero.belongings.backpack ? 0 : 1;
		for (Item item : items) {
			if (!(item instanceof Bag)) {
				visibleSlots++;
			}
		}
		visibleSlots = Math.max( visibleSlots, bag.capacity() + (bag == Dungeon.hero.belongings.backpack && Dungeon.hero.belongings.secondWep != null ? 1 : 0) );
		return Math.max( 20, visibleSlots );
	}

	private void ensureBagItemSlots( int count ) {
		while (bagItems.size() < count) {
			InventorySlot btn = new InventoryPaneSlot(null);
			bagItems.add(btn);
			bagContent.add(btn);
		}
		while (bagItems.size() > count) {
			InventorySlot btn = bagItems.remove( bagItems.size() - 1 );
			bagContent.remove( btn );
			btn.destroy();
		}
	}

	private void layoutCurrencyIndicators( float left, float top, float maxWidth ) {
		float[] pos = new float[]{ left, top };
		layoutCurrencyEntry( goldTxt, gold, left, maxWidth, pos );
		layoutCurrencyEntry( energyTxt, energy, left, maxWidth, pos );
		for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
			layoutCurrencyEntry( homebaseResourceTxts[i], homebaseResourceIcons[i], left, maxWidth, pos );
		}
	}

	private void layoutCurrencyEntry( BitmapText text, Image icon, float left, float maxWidth, float[] pos ) {
		if (!text.visible || !icon.visible) {
			text.visible = false;
			icon.visible = false;
			text.x = icon.x = -1000;
			text.y = icon.y = -1000;
			return;
		}

		float entryWidth = text.width() + 1 + icon.width();
		if (pos[0] > left && pos[0] + entryWidth > left + maxWidth) {
			pos[0] = left;
			pos[1] += CURRENCY_ROW_HEIGHT;
		}

		text.x = pos[0];
		text.y = pos[1] + (CURRENCY_ROW_HEIGHT - text.height()) / 2f;
		PixelScene.align( text );

		icon.x = text.x + text.width() + 1;
		icon.y = pos[1] + (CURRENCY_ROW_HEIGHT - icon.height()) / 2f;
		PixelScene.align( icon );

		pos[0] = icon.x + icon.width() + CURRENCY_ENTRY_GAP;
	}

	public static boolean showHomebaseResources() {
		return (Dungeon.depth == 0 && Dungeon.homebase != null) || currentRunMaterialSatchel() != null;
	}

	public static int homebaseResourceIcon( int index ) {
		if (index == EMERALD_RESOURCE_INDEX) return ItemSpriteSheet.HOMEBASE_EMERALD;
		switch (index) {
			case 0:
				return ItemSpriteSheet.HOMEBASE_WOOD;
			case 1:
				return ItemSpriteSheet.HOMEBASE_STONE;
			case 2:
				return ItemSpriteSheet.HOMEBASE_COPPER;
			case 3:
				return ItemSpriteSheet.HOMEBASE_IRON;
			case 4:
				return ItemSpriteSheet.HOMEBASE_GOLD;
			case 5:
				return ItemSpriteSheet.HOMEBASE_SCRAP;
			case 6:
				return ItemSpriteSheet.HOMEBASE_EMBER;
			case 7:
				return ItemSpriteSheet.HOMEBASE_CORE;
			default:
				return ItemSpriteSheet.HOMEBASE_CORE;
		}
	}

	public static int homebaseResourceColor( int index ) {
		if (index == EMERALD_RESOURCE_INDEX) return 0x33FF88;
		switch (index) {
			case 0:
				return 0xD2A15D;
			case 1:
				return 0xB8B8B8;
			case 2:
				return 0xD9793F;
			case 3:
				return 0xA8C4D8;
			case 4:
				return 0xFFD84A;
			case 5:
				return 0xCACFC2;
			case 6:
				return 0xFF9A3A;
			case 7:
				return 0xFF5555;
			default:
				return 0xFF5555;
		}
	}

	public static int homebaseResourceAmount( int index ) {
		if (index == EMERALD_RESOURCE_INDEX) {
			return Dungeon.homebase == null ? 0 : Dungeon.homebase.emeraldAmount();
		}
		if (Dungeon.depth == 0 && Dungeon.homebase != null) {
			if (index < HomebaseState.Material.values().length) {
				return Dungeon.homebase.amount( HomebaseState.Material.values()[index] );
			} else {
				return Dungeon.homebase.forgeResourceAmount( HomebaseState.ForgeResource.values()[index - FORGE_RESOURCE_START] );
			}
		}

		return currentRunResourceAmount( index );
	}

	private static MaterialSatchel currentRunMaterialSatchel() {
		if (Dungeon.hero == null || Dungeon.hero.belongings == null) return null;
		return Dungeon.hero.belongings.getItem( MaterialSatchel.class );
	}

	private static int currentRunResourceAmount( int index ) {
		MaterialSatchel satchel = currentRunMaterialSatchel();
		if (satchel == null || index < 0 || index >= EMERALD_RESOURCE_INDEX) return 0;

		int amount = 0;
		if (index < HomebaseState.Material.values().length) {
			HomebaseState.Material material = HomebaseState.Material.values()[index];
			for (Item item : satchel.items) {
				if (item instanceof BuildingMaterial && ((BuildingMaterial)item).material() == material) {
					amount += item.quantity();
				}
			}
		} else {
			HomebaseState.ForgeResource resource = HomebaseState.ForgeResource.values()[index - FORGE_RESOURCE_START];
			for (Item item : satchel.items) {
				if (item instanceof ForgeResourceMaterial && ((ForgeResourceMaterial)item).resource() == resource) {
					amount += item.quantity();
				}
			}
		}
		return amount;
	}

	public static String compactHomebaseAmount( int amount ) {
		if (amount >= 1000000) {
			return amount / 1000000 + "m";
		} else if (amount >= 1000) {
			return amount / 1000 + "k";
		} else {
			return Integer.toString( amount );
		}
	}

	private void ensureBagButtons( int count ) {
		while (bags.size() < count) {
			BagButton btn = new BagButton( null, bags.size()+1 );
			bags.add( btn );
			add( btn );
		}
	}

	private int desiredWidthForBagCount( int count ) {
		int tabsLeft = 4 + EQUIPMENT_COLUMNS * (SLOT_WIDTH + 1);
		int tabsInRow = Math.min( Math.max( 1, count ), BAG_BUTTONS_PER_ROW );
		int tabsWidth = tabsInRow * (SLOT_WIDTH + 1) - 1;
		return Math.max( WIDTH, tabsLeft + tabsWidth + 4 );
	}

	private int desiredHeightForBagCount( int count ) {
		return HEIGHT;
	}

	private int bagButtonRows( int count ) {
		return Math.max( 1, (int)Math.ceil( count / (float)BAG_BUTTONS_PER_ROW ) );
	}

	private void updateSizeForBagCount( int count ) {
		int newWidth = desiredWidthForBagCount( count );
		int newHeight = desiredHeightForBagCount( count );
		if (newWidth == width && newHeight == height) return;

		float oldRight = x + width;
		boolean keepRightAnchored = width > 0 && x != 0;
		width = newWidth;
		height = newHeight;
		if (keepRightAnchored) {
			x = oldRight - width;
		}
	}

	public void setSelector(WndBag.ItemSelector selector){
		this.selector = selector;
		if (selector.preferredBag() == Belongings.Backpack.class){
			lastBag = Dungeon.hero.belongings.backpack;
		} else if (selector.preferredBag() != null) {
			Bag preferred = Dungeon.hero.belongings.getItem(selector.preferredBag());
			if (preferred != null)  lastBag = preferred;
			//if a specific preferred bag isn't present, then the relevant items will be in backpack
			else                    lastBag = Dungeon.hero.belongings.backpack;
		}
		updateInventory();
	}

	public WndBag.ItemSelector getSelector() {
		return selector;
	}

	public boolean isSelecting(){
		return selector != null;
	}

	public static void clearTargetingSlot(){
		targetingSlot = null;
	}

	public static void useTargeting(){
		if (instance != null &&
				instance.visible &&
				lastTarget != null &&
				targetingSlot != null &&
				Actor.chars().contains( lastTarget ) &&
				lastTarget.isAlive() &&
				lastTarget.alignment != Char.Alignment.ALLY &&
				Dungeon.level.heroFOV[lastTarget.pos]) {

			targeting = true;
			CharSprite sprite = lastTarget.sprite;

			if (sprite.parent != null) {
				sprite.parent.addToFront(crossM);
				crossM.point(sprite.center(crossM));
			}

			crossB.point(targetingSlot.sprite.center(crossB));
			crossB.visible = true;

		} else {

			lastTarget = null;
			targeting = false;

		}
	}

	public static void cancelTargeting(){
		if (targeting){
			crossB.visible = false;
			crossM.remove();
			targeting = false;
		}
	}

	@Override
	public synchronized void update() {
		super.update();

		if (lastEnabled != (Dungeon.hero.ready || !Dungeon.hero.isAlive())) {
			lastEnabled = (Dungeon.hero.ready || !Dungeon.hero.isAlive());

			boolean lostInvent = Dungeon.hero.belongings.lostInventory();
			for (InventorySlot b : equipped){
				b.enable(lastEnabled
						&& !(b.item() instanceof WndBag.Placeholder)
						&& (selector == null || selector.itemSelectable(b.item()))
						&& (!lostInvent || b.item().keptThroughLostInventory()));
			}
			for (InventorySlot b : bagItems){
				b.enable(lastEnabled
						&& b.item() != null
						&& (selector == null || selector.itemSelectable(b.item()))
						&& (!lostInvent || b.item().keptThroughLostInventory()));
			}
			for (BagButton b : bags){
				b.enable(lastEnabled);
			}

			goldTxt.alpha( lastEnabled ? 1f : 0.3f );
			gold.alpha( lastEnabled ? 1f : 0.3f );
			energyTxt.alpha( lastEnabled ? 1f : 0.3f );
			energy.alpha( lastEnabled ? 1f : 0.3f );
			for (int i = 0; i < HOMEBASE_RESOURCE_COUNT; i++) {
				homebaseResourceTxts[i].alpha( lastEnabled ? 1f : 0.3f );
				homebaseResourceIcons[i].alpha( lastEnabled ? 1f : 0.3f );
			}
		}

	}

	private Image bagIcon(Bag bag ) {
		if (bag instanceof VelvetPouch) {
			return Icons.get( Icons.SEED_POUCH );
		} else if (bag instanceof ScrollHolder) {
			return Icons.get( Icons.SCROLL_HOLDER );
		} else if (bag instanceof MagicalHolster) {
			return Icons.get( Icons.WAND_HOLSTER );
		} else if (bag instanceof PotionBandolier) {
			return Icons.get( Icons.POTION_BANDOLIER );
		} else if (bag instanceof MaterialSatchel
				|| bag instanceof TrinketBag
				|| bag instanceof KeyHolder
				|| bag instanceof ArtifactBag
				|| bag instanceof FoodBag) {
			return new ItemSprite( bag );
		} else {
			return Icons.get( Icons.BACKPACK );
		}
	}

	private class InventoryPaneSlot extends InventorySlot {

		private InventoryPaneSlot( Item item ){
			super(item);
		}

		@Override
		protected void onClick() {
			if (item == null) {
				return;
			}
			if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){
				updateInventory();
				return;
			}

			if (targeting){
				if (targetingSlot == this){
					int cell = QuickSlotButton.autoAim(lastTarget, item());

					if (cell != -1){
						GameScene.handleCell(cell);
					} else {
						//couldn't auto-aim, just target the position and hope for the best.
						GameScene.handleCell( lastTarget.pos );
					}
					return;
				} else {
					cancelTargeting();
				}
			}

			//any windows opened as a consequence of this button should be centered on the inventory
			GameScene.centerNextWndOnInvPane();
			if (selector != null) {
				WndBag.ItemSelector activating = selector;
				selector = null;
				activating.onSelect( item );
				updateInventory();
			} else {
				targetingSlot = this;
				GameScene.show(new WndUseItem( null, item ));
			}
		}

		@Override
		protected boolean onLongClick() {
			if (item == null) {
				return false;
			}
			if (selector == null && item.defaultAction() != null) {
				QuickSlotButton.set( item );
				return true;
			} else if (selector != null) {
				GameScene.centerNextWndOnInvPane();
				GameScene.show(new WndInfoItem(item));
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onMiddleClick() {
			if (item == null) {
				return;
			}
			if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){
				updateInventory();
				return;
			}

			if (!Dungeon.hero.isAlive() || !Dungeon.hero.ready){
				return;
			}

			if (targeting){
				if (targetingSlot == this){
					onClick();
				}
				return;
			}

			if (selector == null && item.defaultAction() != null){
				item.execute(Dungeon.hero);
				if (item != null && item.usesTargeting) {
					targetingSlot = this;
					InventoryPane.useTargeting();
				}
			} else {
				onClick();
			}
		}

		@Override
		protected void onRightClick() {
			if (item == null) {
				return;
			}
			if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){
				updateInventory();
				return;
			}

			if (!Dungeon.hero.isAlive() || !Dungeon.hero.ready){
				return;
			}

			if (targeting){
				//do nothing
				return;
			}

			if (selector == null){
				targetingSlot = this;
				RightClickMenu r = new RightClickMenu(item);
				parent.addToFront(r);
				r.camera = camera();
				PointF mousePos = PointerEvent.currentHoverPos();
				mousePos = camera.screenToCamera((int)mousePos.x, (int)mousePos.y);
				r.setPos(mousePos.x-3, mousePos.y-3);
			} else {
				//do nothing
			}
		}
	}

	private class BagButton extends IconButton {

		private static final int ACTIVE		= 0x9953564D;
		private static final int INACTIVE	= 0x9942443D;

		private ColorBlock bgTop;
		private ColorBlock bgBottom;

		private Bag bag;
		private int index;

		public BagButton( Bag bag, int index ){
			super( bagIcon(bag) );
			this.bag = bag;
			this.index = index;
			visible = active = bag != null;
		}

		public void bag( Bag bag ){
			this.bag = bag;
			icon(bagIcon(bag));
			visible = active = bag != null;

			if (lastBag == bag){
				bgTop.texture(TextureCache.createSolid(ACTIVE));
				bgBottom.texture(TextureCache.createSolid(ACTIVE));
			} else {
				bgTop.texture(TextureCache.createSolid(INACTIVE));
				bgBottom.texture(TextureCache.createSolid(INACTIVE));
			}
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			bgTop = new ColorBlock(1, 1, ACTIVE);
			add(bgTop);

			bgBottom = new ColorBlock(1, 1, ACTIVE);
			add(bgBottom);
		}

		@Override
		protected void layout() {
			super.layout();

			bgTop.size(width-2, 1);
			bgTop.y = y;
			bgTop.x = x+1;

			bgBottom.size(width, height-1);
			bgBottom.y = y+1;
			bgBottom.x = x;
		}
		
		public void alpha( float value ){
			bgTop.alpha(value);
			bgBottom.alpha(value);
			icon.alpha(value);
		}

		@Override
		protected void onClick() {
			super.onClick();
			GameScene.cancel();
			lastBag = bag;
			refresh();
		}

		@Override
		public GameAction keyAction() {
			switch (index){
				case 1:
					return SPDAction.BAG_1;
				case 2:
					return SPDAction.BAG_2;
				case 3:
					return SPDAction.BAG_3;
				case 4:
					return SPDAction.BAG_4;
				case 5:
					return SPDAction.BAG_5;
				default:
					return null;
			}
		}

		@Override
		public GameAction secondaryTooltipAction() {
			return SPDAction.INVENTORY_SELECTOR;
		}

		@Override
		protected String hoverText() {
			if (bag != null) {
				return Messages.titleCase(bag.name());
			} else {
				return null;
			}
		}
	}

}
