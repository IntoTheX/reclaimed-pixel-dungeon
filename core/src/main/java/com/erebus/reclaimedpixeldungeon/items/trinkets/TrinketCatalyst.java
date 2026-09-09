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

package com.erebus.reclaimedpixeldungeon.items.trinkets;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.AlchemyScene;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.InventoryItemButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.erebus.reclaimedpixeldungeon.windows.WndInfoItem;
import com.erebus.reclaimedpixeldungeon.windows.WndSadGhost;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class TrinketCatalyst extends Item {

	{
		image = ItemSpriteSheet.TRINKET_CATA;

		unique = true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (super.doPickUp(hero, pos)){
			if (!Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_ALCHEMY)){
				GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_ALCHEMY);
			}
			return true;
		} else {
			return false;
		}
	}

	private ArrayList<Trinket> rolledTrinkets = new ArrayList<>();

	public boolean hasRolledTrinkets(){
		return !rolledTrinkets.isEmpty();
	}

	private static final String ROLLED_TRINKETS = "rolled_trinkets";

	private static Trinket randomCycleTrinket( ArrayList<Trinket> rolledTrinkets ) {
		Class<?>[] trinkets = Generator.Category.TRINKET.classes;
		int[] owned = ownedTrinketCounts();
		boolean[] alreadyRolled = new boolean[trinkets.length];
		for (Trinket trinket : rolledTrinkets) {
			int index = trinketIndex( trinket.getClass() );
			if (index >= 0) {
				alreadyRolled[index] = true;
			}
		}

		int minOwned = Integer.MAX_VALUE;
		for (int count : owned) {
			minOwned = Math.min( minOwned, count );
		}
		if (minOwned == Integer.MAX_VALUE) {
			minOwned = 0;
		}

		ArrayList<Integer> currentCycle = new ArrayList<>();
		ArrayList<Integer> unrolledCurrentCycle = new ArrayList<>();
		for (int i = 0; i < trinkets.length; i++) {
			if (owned[i] == minOwned) {
				currentCycle.add( i );
				if (!alreadyRolled[i]) {
					unrolledCurrentCycle.add( i );
				}
			}
		}
		if (!unrolledCurrentCycle.isEmpty()) {
			return randomTrinketByIndex( unrolledCurrentCycle.get( Random.Int( unrolledCurrentCycle.size() ) ) );
		}
		if (!currentCycle.isEmpty()) {
			return randomTrinketByIndex( currentCycle.get( Random.Int( currentCycle.size() ) ) );
		}

		return (Trinket)Generator.random( Generator.Category.TRINKET );
	}

	private static Trinket randomTrinketByIndex( int index ) {
		@SuppressWarnings("unchecked")
		Class<? extends Item> trinketClass = (Class<? extends Item>)Generator.Category.TRINKET.classes[index];
		return (Trinket)Generator.random( trinketClass );
	}

	private static int[] ownedTrinketCounts() {
		Class<?>[] trinkets = Generator.Category.TRINKET.classes;
		int[] counts = new int[trinkets.length];

		if (Dungeon.hero != null && Dungeon.hero.belongings != null) {
			for (Item item : Dungeon.hero.belongings) {
				countOwnedTrinket( item, counts, false );
			}
		}

		if (Dungeon.homebase != null) {
			for (Item item : Dungeon.homebase.vaultItems()) {
				countOwnedTrinket( item, counts, true );
			}
		}

		return counts;
	}

	private static void countOwnedTrinket( Item item, int[] counts, boolean includeNestedBags ) {
		if (item instanceof Trinket && !(item instanceof Trinket.PlaceHolder)) {
			int index = trinketIndex( item.getClass() );
			if (index >= 0) {
				counts[index]++;
			}
		} else if (includeNestedBags && item instanceof Bag) {
			for (Item nested : (Bag)item) {
				countOwnedTrinket( nested, counts, true );
			}
		}
	}

	private static int trinketIndex( Class<?> trinketClass ) {
		Class<?>[] trinkets = Generator.Category.TRINKET.classes;
		for (int i = 0; i < trinkets.length; i++) {
			if (trinkets[i] == trinketClass) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (!rolledTrinkets.isEmpty()){
			bundle.put(ROLLED_TRINKETS, rolledTrinkets);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		rolledTrinkets.clear();
		if (bundle.contains(ROLLED_TRINKETS)){
			rolledTrinkets.addAll((Collection<Trinket>) ((Collection<?>)bundle.getCollection( ROLLED_TRINKETS )));
		}
	}

	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1 && ingredients.get(0) instanceof TrinketCatalyst;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 6;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			//we silently re-add the catalyst so that we can clear it when a trinket is selected
			//this way player isn't totally screwed if they quit the game while selecting
			TrinketCatalyst newCata = (TrinketCatalyst) ingredients.get(0).duplicate();
			newCata.collect();

			ingredients.get(0).quantity(0);

			ShatteredPixelDungeon.scene().addToFront(new WndTrinket(newCata));
			try {
				Dungeon.saveAll(); //do a save here as pausing alch scene doesn't otherwise save
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new Trinket.PlaceHolder();
		}
	}

	public static class RandomTrinket extends Item {

		{
			image = ItemSpriteSheet.SOMETHING;
		}

	}

	public static class WndTrinket extends Window {

		private static final int WIDTH		= 120;
		private static final int BTN_SIZE	= 24;
		private static final int BTN_GAP	= 4;
		private static final int GAP		= 2;

		private static final int NUM_TRINKETS = 4;

		private final TrinketCatalyst cata;

		public WndTrinket( TrinketCatalyst cata ){
			this.cata = cata;

			IconTitle titlebar = new IconTitle();
			titlebar.icon(new ItemSprite(cata));
			titlebar.label(Messages.titleCase(Messages.get(TrinketCatalyst.class, "window_title")));
			titlebar.setRect(0, 0, WIDTH, 0);
			add( titlebar );

			RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(TrinketCatalyst.class, "window_text"), 6 );
			message.maxWidth(WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );

			//roll new trinkets if trinkets were not already rolled
			while (cata.rolledTrinkets.size() < NUM_TRINKETS){
				cata.rolledTrinkets.add( randomCycleTrinket( cata.rolledTrinkets ) );
			}

			for (int i = 0; i < NUM_TRINKETS; i++){
				InventoryItemButton btnReward = new InventoryItemButton() {
					@Override
					protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront(new RewardWindow(item()));
					}
				};
				btnReward.forceIdentifiedAppearance(true);
				btnReward.item(cata.rolledTrinkets.get(i));
				btnReward.setRect( (i+1)*(WIDTH - BTN_GAP) / NUM_TRINKETS - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
				add( btnReward );

			}

			resize(WIDTH, (int)(message.top() + message.height() + 2*BTN_GAP + BTN_SIZE));

		}

		@Override
		public void onBackPressed() {
			//do nothing
		}

		private class RewardWindow extends WndInfoItem {

			public RewardWindow( Item item ) {
				super(item);

				RedButton btnConfirm = new RedButton(Messages.get(WndSadGhost.class, "confirm")){
					@Override
					protected void onClick() {
						RewardWindow.this.hide();
						WndTrinket.this.hide();

						Item result = item;
						if (result instanceof RandomTrinket){
							result = randomCycleTrinket( WndTrinket.this.cata.rolledTrinkets );
						}

						TrinketCatalyst cata = WndTrinket.this.cata;

						if (cata != null && Dungeon.hero.belongings.contains(cata)) {
							cata.detach(Dungeon.hero.belongings.backpack);
							Catalog.countUse(cata.getClass());
							result.identify();
							if (ShatteredPixelDungeon.scene() instanceof AlchemyScene) {
								((AlchemyScene) ShatteredPixelDungeon.scene()).craftItem(null, result);
							} else {
								Sample.INSTANCE.play( Assets.Sounds.PUFF );

								if (result.doPickUp(Dungeon.hero)){
									GLog.p( Messages.capitalize(Messages.get(Hero.class, "you_now_have", item.name())) );
								} else {
									Dungeon.level.drop(result, Dungeon.hero.pos);
								}

								Statistics.itemsCrafted++;
								Badges.validateItemsCrafted();

								try {
									Dungeon.saveAll();
								} catch (IOException e) {
									ShatteredPixelDungeon.reportException(e);
								}
							}
						}
					}
				};
				btnConfirm.setRect(0, height+2, width/2-1, 16);
				add(btnConfirm);

				RedButton btnCancel = new RedButton(Messages.get(WndSadGhost.class, "cancel")){
					@Override
					protected void onClick() {
						hide();
					}
				};
				btnCancel.setRect(btnConfirm.right()+2, height+2, btnConfirm.width(), 16);
				add(btnCancel);

				resize(width, (int)btnCancel.bottom());
			}
		}

	}
}
