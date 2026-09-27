/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.items.Amulet;
import com.erebus.reclaimedpixeldungeon.items.Emerald;
import com.erebus.reclaimedpixeldungeon.items.EnergyCrystal;
import com.erebus.reclaimedpixeldungeon.items.Gold;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.Waterskin;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.Dewdrop;
import com.erebus.reclaimedpixeldungeon.items.keys.Key;
import com.erebus.reclaimedpixeldungeon.items.materials.BuildingMaterial;
import com.erebus.reclaimedpixeldungeon.items.materials.ForgeResourceMaterial;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollingGridPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;

/** Catalog-backed item picker for Marketplace requested returns. */
class WndMarketplaceCatalog extends Window {

	interface Selection {
		void selected( Item item );
	}

	private final int windowWidth;
	private final int windowHeight;
	private final Selection selection;
	private final RedButton equipment;
	private final RedButton consumables;
	private final ScrollingGridPane grid;
	private int tab;

	WndMarketplaceCatalog( Selection selection ) {
		this.selection = selection;
		windowWidth = ReclaimedWindow.modalWidth( 180 );
		windowHeight = ReclaimedWindow.modalHeight( 190, 0 );

		RenderedTextBlock title = PixelScene.renderTextBlock( "Requested Item Catalog", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( 3, 3 );
		add( title );

		equipment = new RedButton( "Equipment", 7 ) {
			@Override
			protected void onClick() {
				tab = 0;
				rebuild();
			}
		};
		consumables = new RedButton( "Consumables", 7 ) {
			@Override
			protected void onClick() {
				tab = 1;
				rebuild();
			}
		};
		float buttonWidth = (windowWidth - 1) / 2f;
		equipment.setRect( 0, title.bottom() + 3, buttonWidth, 18 );
		consumables.setRect( equipment.right() + 1, equipment.top(), buttonWidth, 18 );
		add( equipment );
		add( consumables );

		grid = new ScrollingGridPane();
		grid.setRect( 0, equipment.bottom() + 2, windowWidth,
				windowHeight - equipment.bottom() - 2 );
		add( grid );
		resize( windowWidth, windowHeight );
		rebuild();
	}

	private void rebuild() {
		grid.clear();
		equipment.enable( tab != 0 );
		consumables.enable( tab != 1 );
		ArrayList<Catalog> categories = tab == 0 ? Catalog.equipmentCatalogs : Catalog.consumableCatalogs;
		for (Catalog category : categories) addCategory( category );
		grid.setRect( 0, equipment.bottom() + 2, windowWidth,
				windowHeight - equipment.bottom() - 2 );
		grid.scrollTo( 0, 0 );
	}

	private void addCategory( Catalog category ) {
		ArrayList<Item> items = marketplaceItems( category.items() );
		if (items.isEmpty()) return;
		grid.addHeader( "_" + Messages.titleCase( category.title() ) + "_" );
		for (final Item item : items) {
			ScrollingGridPane.GridItem button = new ScrollingGridPane.GridItem( new ItemSprite( item ) ) {
				@Override
				public boolean onClick( float x, float y ) {
					if (!inside( x, y )) return false;
					GameScene.show( new WndOptions( new ItemSprite( item ),
							Messages.titleCase( item.trueName() ),
							"Add this item to the requested return?", "Select", "Cancel" ) {
						@Override
						protected void onSelect( int index ) {
							if (index != 0) return;
							WndMarketplaceCatalog.this.hide();
							if (selection != null) selection.selected( item );
						}
					} );
					return true;
				}
			};
			if (item.icon != -1) {
				Image itemIcon = new Image( Assets.Sprites.ITEM_ICONS );
				itemIcon.frame( ItemSpriteSheet.Icons.film.get( item.icon ) );
				button.addSecondIcon( itemIcon );
			}
			grid.addItem( button );
		}
	}

	private ArrayList<Item> marketplaceItems( Collection<Class<?>> classes ) {
		ArrayList<Item> items = new ArrayList<>();
		for (Class<?> itemClass : classes) {
			if (!Item.class.isAssignableFrom( itemClass )) continue;
			Item item = (Item)Reflection.newInstance( itemClass );
			if (item == null || item instanceof Bag || item instanceof Gold
					|| item instanceof EnergyCrystal || item instanceof BuildingMaterial
					|| item instanceof ForgeResourceMaterial || item instanceof Emerald
					|| item instanceof Waterskin || item instanceof Amulet
					|| item instanceof Dewdrop || item instanceof Key) continue;
			item.identifyForPreview();
			items.add( item );
		}
		return items;
	}
}
