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
import com.erebus.reclaimedpixeldungeon.SPDAction;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
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
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventoryPane;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.QuickSlotButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.RightClickMenu;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

public class WndBag extends WndTabbed {
	
	//only one bag window can appear at a time
	public static Window INSTANCE;

	protected static final int COLS_P   = 5;
	protected static final int COLS_L   = 8;
	
	protected static int SLOT_WIDTH_P   = 28;
	protected static int SLOT_WIDTH_L   = 28;

	protected static int SLOT_HEIGHT_P	= 28;
	protected static int SLOT_HEIGHT_L	= 28;

	protected static final int SLOT_MARGIN	= 1;
	
	protected static final int TITLE_HEIGHT	= 24;
	private static final float TITLE_CURRENCY_SCALE = 0.75f;
	private static final float TITLE_CURRENCY_ROW_HEIGHT = 7f;
	private static final float TITLE_CURRENCY_ENTRY_GAP = 2f;
	
	private ItemSelector selector;

	private int nCols;
	private int nRows;

	private int slotWidth;
	private int slotHeight;

	protected int count;
	protected int col;
	protected int row;

	private boolean placingInScroll;
	private Component itemContent;
	private ScrollPane itemScroll;
	private int itemScrollY;
	private int itemScrollWidth;
	private int itemScrollHeight;
	private int scrollCount;
	private int scrollCol;
	private int scrollRow;
	private boolean itemGridScrollable;
	
	private static Bag lastBag;

	public WndBag( Bag bag ) {
		this(bag, null);
	}

	public WndBag( Bag bag, ItemSelector selector ) {
		
		super();
		
		if( INSTANCE != null ){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		this.selector = selector;
		
		lastBag = bag;
		InventoryPane.rememberBag( bag );

		slotWidth = PixelScene.landscape() ? SLOT_WIDTH_L : SLOT_WIDTH_P;
		slotHeight = PixelScene.landscape() ? SLOT_HEIGHT_L : SLOT_HEIGHT_P;

		nCols = PixelScene.landscape() ? COLS_L : COLS_P;
		int frozenRows = (int)Math.ceil( frozenSlotCount( bag ) / (float)nCols );
		int scrollRows = (int)Math.ceil( scrollingSlotCount( bag ) / (float)nCols );
		int visibleScrollRows = Math.min( scrollRows, baseScrollRows( bag ) );
		nRows = frozenRows + scrollRows;

		int windowWidth = slotWidth * nCols + SLOT_MARGIN * (nCols - 1);
		while (slotWidth > 18 && (windowWidth + chrome.marginHor()) > PixelScene.uiCamera.width){
			slotWidth--;
			windowWidth -= nCols;
		}
		int frozenHeight = gridHeight( frozenRows );
		int scrollContentHeight = gridHeight( scrollRows );
		int preferredScrollHeight = gridHeight( visibleScrollRows );
		int preferredWindowHeight = TITLE_HEIGHT + frozenHeight + (scrollRows > 0 ? SLOT_MARGIN + preferredScrollHeight : 0);
		int maxWindowHeight = ReclaimedWindow.modalHeight( preferredWindowHeight, chrome.marginTop() + tabHeight() );
		int maxScrollHeight = Math.max( slotHeight, maxWindowHeight - TITLE_HEIGHT - frozenHeight - SLOT_MARGIN );
		int scrollHeight = Math.min( scrollContentHeight, maxScrollHeight );
		int windowHeight = TITLE_HEIGHT + frozenHeight + (scrollRows > 0 ? SLOT_MARGIN + scrollHeight : 0);

		placeTitle( bag, windowWidth );
		
		placeItems( bag, windowWidth, frozenHeight, scrollHeight, scrollContentHeight );

		resize( windowWidth, windowHeight );

		int i = 1;
		for (Bag b : Dungeon.hero.belongings.getBags()) {
			if (b != null) {
				BagTab tab = new BagTab( b, i++ );
				add( tab );
				tab.select( b == bag );
				if  (b == bag){
					selected = tab;
				}
			}
		}

		layoutTabs();
		offset( 0, ReclaimedWindow.modalYOffset( windowHeight, chrome.marginTop() + tabHeight() ) );
	}

	private int gridHeight( int rows ){
		return rows <= 0 ? 0 : slotHeight * rows + SLOT_MARGIN * (rows - 1);
	}

	private int frozenSlotCount( Bag bag ) {
		Belongings stuff = Dungeon.hero.belongings;
		int visibleSlots = Belongings.EQUIPMENT_SLOT_COUNT;
		if (bag == stuff.backpack && stuff.secondWep != null) {
			visibleSlots++;
		}
		return Math.max( nCols, visibleSlots );
	}

	private int baseScrollRows( Bag bag ) {
		int baseSlots = Math.max( nCols, bag.capacity() - bag.extraSlots() );
		return Math.max( 1, (int)Math.ceil( baseSlots / (float)nCols ) );
	}

	private int scrollingSlotCount( Bag bag ) {
		Belongings stuff = Dungeon.hero.belongings;
		int visibleSlots = bag != stuff.backpack ? 1 : 0;
		if (selector == null) {
			visibleSlots += bag.capacity();
		} else {
			for (Item item : bag.items.toArray( new Item[0] )) {
				if (!(item instanceof Bag)) {
					visibleSlots++;
				}
			}
		}

		return Math.max( nCols, visibleSlots );
	}

	public ItemSelector getSelector() {
		return selector;
	}

	public static WndBag lastBag(ItemSelector selector ) {
		
		if (lastBag != null && Dungeon.hero.belongings.backpack.contains( lastBag )) {
			
			return new WndBag( lastBag, selector );
			
		} else {
			
			return new WndBag( Dungeon.hero.belongings.backpack, selector );
			
		}
	}

	public static WndBag getBag( ItemSelector selector ) {
		if (validRememberedBag() && containsSelectableItem( lastBag, selector )) {
			return new WndBag( lastBag, selector );
		}
		if (selector.preferredBag() == Belongings.Backpack.class){
			return new WndBag( Dungeon.hero.belongings.backpack, selector );

		} else if (selector.preferredBag() != null){
			Bag bag = Dungeon.hero.belongings.getItem( selector.preferredBag() );
			if (bag != null)    return new WndBag( bag, selector );
			//if a specific preferred bag isn't present, then the relevant items will be in backpack
			else                return new WndBag( Dungeon.hero.belongings.backpack, selector );
		}

		return lastBag( selector );
	}

	public static void rememberBag( Bag bag ) {
		if (bag != null) lastBag = bag;
	}

	private static boolean validRememberedBag() {
		return lastBag != null && Dungeon.hero != null
				&& Dungeon.hero.belongings.getBags().contains( lastBag );
	}

	public static boolean containsSelectableItem( Bag bag, ItemSelector selector ) {
		if (bag == null || selector == null || Dungeon.hero == null) return false;
		if (bag != Dungeon.hero.belongings.backpack && selector.itemSelectable( bag )) return true;
		for (Item item : bag.items) {
			if (!(item instanceof Bag) && selector.itemSelectable( item )) return true;
		}
		return false;
	}
	
	protected void placeTitle( Bag bag, int width ){

		float titleWidth = placeTitleCurrencyIndicators( width );

		String title = selector != null ? selector.textPrompt() : null;
		RenderedTextBlock txtTitle = PixelScene.renderTextBlock(
				title != null ? Messages.titleCase(title) : Messages.titleCase( bag.name() ), 8 );
		txtTitle.hardlight( TITLE_COLOR );
		txtTitle.maxWidth( (int)titleWidth - 2 );
		txtTitle.setPos(
				1,
				(TITLE_HEIGHT - txtTitle.height()) / 2f - 1
		);
		PixelScene.align(txtTitle);
		add( txtTitle );
	}

	private float placeTitleCurrencyIndicators( int width ) {
		boolean hasHomebaseResources = false;
		if (InventoryPane.showHomebaseResources()) {
			for (int i = 0; i < InventoryPane.HOMEBASE_RESOURCE_COUNT; i++) {
				if (InventoryPane.homebaseResourceAmount( i ) > 0) {
					hasHomebaseResources = true;
					break;
				}
			}
		}

		float left = hasHomebaseResources ? Math.max( 52, width - 96 ) : Math.max( 52, width - 44 );
		float[] pos = new float[]{ left, 2 };
		float maxWidth = width - left;

		addTitleCurrencyEntry(
				InventoryPane.compactHomebaseAmount( Dungeon.gold ),
				TITLE_COLOR,
				Icons.get( Icons.COIN_SML ),
				left,
				maxWidth,
				pos );

		if (Dungeon.energy > 0) {
			addTitleCurrencyEntry(
					InventoryPane.compactHomebaseAmount( Dungeon.energy ),
					0x44CCFF,
					Icons.get( Icons.ENERGY_SML ),
					left,
					maxWidth,
					pos );
		}

		if (InventoryPane.showHomebaseResources()) {
			for (int i = 0; i < InventoryPane.HOMEBASE_RESOURCE_COUNT; i++) {
				int amount = InventoryPane.homebaseResourceAmount( i );
				if (amount <= 0) continue;
				addTitleCurrencyEntry(
						InventoryPane.compactHomebaseAmount( amount ),
						InventoryPane.homebaseResourceColor( i ),
						new ItemSprite( InventoryPane.homebaseResourceIcon( i ) ),
						left,
						maxWidth,
						pos );
			}
		}

		return left - 2;
	}

	private void addTitleCurrencyEntry( String amount, int color, Image icon, float left, float maxWidth, float[] pos ) {
		BitmapText amt = new BitmapText( amount, PixelScene.pixelFont );
		amt.hardlight( color );
		amt.scale.set( PixelScene.align( TITLE_CURRENCY_SCALE ) );
		icon.scale.set( PixelScene.align( TITLE_CURRENCY_SCALE ) );
		amt.measure();

		float entryWidth = amt.width() + 1 + icon.width();
		if (pos[0] > left && pos[0] + entryWidth > left + maxWidth) {
			pos[0] = left;
			pos[1] += TITLE_CURRENCY_ROW_HEIGHT;
		}

		amt.x = pos[0];
		amt.y = pos[1] + (TITLE_CURRENCY_ROW_HEIGHT - amt.height()) / 2f;
		PixelScene.align( amt );
		add( amt );

		icon.x = amt.x + amt.width() + 1;
		icon.y = pos[1] + (TITLE_CURRENCY_ROW_HEIGHT - icon.height()) / 2f;
		PixelScene.align( icon );
		add( icon );

		pos[0] = icon.x + icon.width() + TITLE_CURRENCY_ENTRY_GAP;
	}
	
	protected void placeItems( Bag container, int windowWidth, int frozenHeight, int scrollHeight, int scrollContentHeight ) {
		
		// Equipped items
		Belongings stuff = Dungeon.hero.belongings;
		for (int i = 0; i < Belongings.EQUIPMENT_SLOT_COUNT; i++) {
			Item equipped = stuff.equipmentItem( i );
			placeItem( equipped != null ? equipped : new Placeholder( stuff.equipmentPlaceholder( i ) ) );
		}

		if (container == Dungeon.hero.belongings.backpack && stuff.secondWep != null) {
			//secondary weapons are equipped-style controls and stay frozen above bag scrolling
			placeItem(stuff.secondWep);
		}

		itemContent = new Component();
		itemContent.setSize( windowWidth, scrollContentHeight );
		itemScroll = new ScrollPane( itemContent );
		itemScrollY = TITLE_HEIGHT + frozenHeight + SLOT_MARGIN;
		itemScrollWidth = windowWidth;
		itemScrollHeight = scrollHeight;
		itemGridScrollable = scrollContentHeight > scrollHeight;
		add( itemScroll );
		itemScroll.setRect( 0, itemScrollY, itemScrollWidth, itemScrollHeight );

		placingInScroll = true;
		scrollCount = 0;
		scrollCol = 0;
		scrollRow = 0;

		//the container itself if it's not the root backpack
		if (container != Dungeon.hero.belongings.backpack){
			placeItem(container);
			scrollCount--; //don't count this one, as it's not actually inside of itself
		}

		// Items in the bag, except other containers (they have tags at the bottom)
		for (Item item : container.items.toArray(new Item[0])) {
			if (!(item instanceof Bag)) {
				placeItem( item );
			} else if (selector == null) {
				scrollCount++;
			}
		}
		
		// Free Space
		if (selector == null) {
			while (scrollCount < container.capacity()) {
				placeItem( null );
			}
		}
		placingInScroll = false;
	}
	
	protected void placeItem( final Item item ) {

		final boolean scrollSlot = placingInScroll;
		if (placingInScroll) {
			scrollCount++;
		} else {
			count++;
		}
		
		int activeCol = placingInScroll ? scrollCol : col;
		int activeRow = placingInScroll ? scrollRow : row;
		int x = activeCol * (slotWidth + SLOT_MARGIN);
		int y = (placingInScroll ? 0 : TITLE_HEIGHT) + activeRow * (slotHeight + SLOT_MARGIN);

		InventorySlot slot = new InventorySlot( item ){
			@Override
			protected void onClick() {
				if (item == null) {
					return;
				}
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){

					hide();

				} else if (selector != null) {

					if (selector.hideAfterSelecting()){
						hide();
					}
					selector.onSelect( item );

				} else {

					Game.scene().addToFront(new WndUseItem( WndBag.this, item ) );

				}
			}

			@Override
			protected void onRightClick() {
				if (item == null) {
					return;
				}
				if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){

					hide();

				} else if (selector != null) {

					if (selector.hideAfterSelecting()){
						hide();
					}
					selector.onSelect( item );

				} else {

					RightClickMenu r = new RightClickMenu(item){
						@Override
						public void onSelect(int index) {
							WndBag.this.hide();
						}
					};
					parent.addToFront(r);
					r.camera = camera();
					PointF mousePos = PointerEvent.currentHoverPos();
					mousePos = camera().screenToCamera((int)mousePos.x, (int)mousePos.y);
					r.setPos(mousePos.x-3, mousePos.y-3);

				}
			}

			@Override
			protected boolean onLongClick() {
				if (item == null) {
					return false;
				}
				if (scrollSlot && bagGridScrollable()) {
					return false;
				}
				if (selector == null && item.defaultAction() != null) {
					hide();
					QuickSlotButton.set( item );
					return true;
				} else if (selector != null) {
					Game.scene().addToFront(new WndInfoItem(item));
					return true;
				} else {
					return false;
				}
			}
		};
		slot.setRect( x, y, slotWidth, slotHeight );
		if (scrollSlot && itemGridScrollable) {
			slot.blockLevel( PointerArea.NEVER_BLOCK );
		}
		if (placingInScroll) {
			itemContent.add(slot);
		} else {
			add(slot);
		}

		if (item == null || (selector != null && !selector.itemSelectable(item))){
			slot.enable(false);
		}
		
		if (placingInScroll) {
			if (++scrollCol >= nCols) {
				scrollCol = 0;
				scrollRow++;
			}
		} else if (++col >= nCols) {
			col = 0;
			row++;
		}

	}

	private boolean bagGridScrollable() {
		return itemScroll != null
				&& itemContent != null
				&& itemScroll.height() > 0
				&& itemContent.height() > itemScroll.height();
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.INVENTORY) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (selector != null) {
			selector.onSelect( null );
		}
		super.onBackPressed();
	}
	
	@Override
	public void offset( int xOffset, int yOffset ) {
		super.offset( xOffset, yOffset );
		if (itemScroll != null) {
			itemScroll.setRect( 0, itemScrollY, itemScrollWidth, itemScrollHeight );
		}
	}

	@Override
	protected void onClick( Tab tab ) {
		hide();
		Window w = new WndBag(((BagTab) tab).bag, selector);
		if (Game.scene() instanceof GameScene){
			GameScene.show(w);
		} else {
			Game.scene().addToFront(w);
		}
	}
	
	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}
	
	@Override
	protected int tabHeight() {
		return 20;
	}
	
	private Image icon( Bag bag ) {
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
	
	private class BagTab extends IconTab {

		private Bag bag;
		private int index;
		
		public BagTab( Bag bag, int index ) {
			super( icon(bag) );
			
			this.bag = bag;
			this.index = index;
		}

		@Override
		public GameAction keyAction() {
			switch (index){
				case 1: default:
					return SPDAction.BAG_1;
				case 2:
					return SPDAction.BAG_2;
				case 3:
					return SPDAction.BAG_3;
				case 4:
					return SPDAction.BAG_4;
				case 5:
					return SPDAction.BAG_5;
			}
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(bag.name());
		}
	}
	
	public static class Placeholder extends Item {

		public Placeholder(int image ) {
			this.image = image;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}
		
		@Override
		public boolean isEquipped( Hero hero ) {
			return true;
		}
	}

	public abstract static class ItemSelector {
		public abstract String textPrompt();
		public Class<?extends Bag> preferredBag(){
			return null; //defaults to last bag opened
		}
		public boolean hideAfterSelecting(){
			return true; //defaults to hiding the window when an item is picked
		}
		public abstract boolean itemSelectable( Item item );
		public abstract void onSelect( Item item );
	}
}
