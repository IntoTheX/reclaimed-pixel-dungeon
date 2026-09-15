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

package com.erebus.reclaimedpixeldungeon.items.scrolls.exotic;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.effects.Enchanting;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.EnchantmentSlots;
import com.erebus.reclaimedpixeldungeon.items.Recipe;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.items.scrolls.InventoryScroll;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfEnchantment;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.erebus.reclaimedpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ScrollOfEnchantment extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_ENCHANT;

		unique = true;

		talentFactor = 2f;
	}

	@Override
	public String name() {
		return super.name() + " " + EnchantmentSlots.roman( level() );
	}

	@Override
	public int visiblyUpgraded() {
		return 0;
	}

	@Override
	public int buffedVisiblyUpgraded() {
		return 0;
	}

	@Override
	public String inventoryLevelText() {
		return EnchantmentSlots.roman( level() );
	}

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar( item ) && item.level() == level();
	}

	@Override
	public String desc() {
		if (!isKnown()) return super.desc();
		return Messages.get( this, "desc", EnchantmentSlots.roman(level()) ) + Messages.get( this, "merge_desc" );
	}

	protected static boolean identifiedByUse = false;
	
	@Override
	public void doRead() {
		if (!isKnown()) {
			identify();
			curItem = detach(curUser.belongings.backpack);
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		GameScene.selectItem( itemSelector );
	}

	public static boolean enchantable( Item item ){
		return (item instanceof Weapon || item instanceof Armor)
				&& (item.isUpgradable() || item instanceof SpiritBow);
	}

	private void confirmCancelation() {
		GameScene.show( new WndOptions(new ItemSprite(this),
				Messages.titleCase(name()),
				Messages.get(InventoryScroll.class, "warning"),
				Messages.get(InventoryScroll.class, "yes"),
				Messages.get(InventoryScroll.class, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
					case 0:
						curUser.spendAndNext( TIME_TO_READ );
						identifiedByUse = false;
						break;
					case 1:
						GameScene.selectItem(itemSelector);
						break;
				}
			}
			public void onBackPressed() {}
		} );
	}
	
	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(ScrollOfEnchantment.class, "inv_title");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return enchantable(item);
		}

		@Override
		public void onSelect(final Item item) {
			
			if (item instanceof Weapon){
				if (!identifiedByUse) {
					curItem.detach(curUser.belongings.backpack);
				}
				identifiedByUse = false;
				
				final Weapon.Enchantment enchants[] = new Weapon.Enchantment[3];
				
				int slot = EnchantmentSlots.slotForLevel( curItem.level() );
				Class<? extends Weapon.Enchantment>[] existing = ((Weapon)item).enchantmentClasses();
				enchants[0] = Weapon.Enchantment.randomCommon( existing );
				enchants[1] = Weapon.Enchantment.randomUncommon( append(existing, enchants[0].getClass()) );
				enchants[2] = Weapon.Enchantment.random( append(existing, enchants[0].getClass(), enchants[1].getClass()) );

				GameScene.show(new WndEnchantSelect((Weapon) item, slot, enchants[0], enchants[1], enchants[2]));
			
			} else if (item instanceof Armor) {
				if (!identifiedByUse) {
					curItem.detach(curUser.belongings.backpack);
				}
				identifiedByUse = false;
				
				final Armor.Glyph glyphs[] = new Armor.Glyph[3];
				
				int slot = EnchantmentSlots.slotForLevel( curItem.level() );
				Class<? extends Armor.Glyph>[] existing = ((Armor)item).glyphClasses();
				glyphs[0] = Armor.Glyph.randomCommon( existing );
				glyphs[1] = Armor.Glyph.randomUncommon( append(existing, glyphs[0].getClass()) );
				glyphs[2] = Armor.Glyph.random( append(existing, glyphs[0].getClass(), glyphs[1].getClass()) );
				
				GameScene.show(new WndGlyphSelect((Armor) item, slot, glyphs[0], glyphs[1], glyphs[2]));
			} else if (identifiedByUse){
				((ScrollOfEnchantment)curItem).confirmCancelation();
			}
		}
	};

	@SafeVarargs
	private static <T> Class<? extends T>[] append( Class<? extends T>[] existing, Class<? extends T>... additions ) {
		Class<? extends T>[] result = java.util.Arrays.copyOf( existing, existing.length + additions.length );
		System.arraycopy( additions, 0, result, existing.length, additions.length );
		return result;
	}

	public static class WndEnchantSelect extends WndOptions {

		private static Weapon wep;
		private static Weapon.Enchantment[] enchantments;
		private static int slot;

		//used in PixelScene.restoreWindows
		public WndEnchantSelect(){
			this(wep, slot, enchantments[0], enchantments[1], enchantments[2]);
		}

		public WndEnchantSelect(Weapon wep, int targetSlot, Weapon.Enchantment ench1,
		                           Weapon.Enchantment ench2, Weapon.Enchantment ench3){
			super(new ItemSprite(tieredScroll(targetSlot)),
					Messages.titleCase(tieredScroll(targetSlot).name()),
					Messages.get(ScrollOfEnchantment.class, "weapon"),
					ench1.name(),
					ench2.name(),
					ench3.name(),
					Messages.get(ScrollOfEnchantment.class, "cancel"));
			this.wep = wep;
			slot = targetSlot;
			enchantments = new Weapon.Enchantment[3];
			enchantments[0] = ench1;
			enchantments[1] = ench2;
			enchantments[2] = ench3;

			WndGlyphSelect.arm = null;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 3) {
				wep.enchant(slot, enchantments[index]);
				GLog.p(Messages.get(StoneOfEnchantment.class, "weapon"));
				((ScrollOfEnchantment)curItem).readAnimation();

				Sample.INSTANCE.play( Assets.Sounds.READ );
				Enchanting.show(curUser, wep);
			} else {
				GameScene.show(new WndConfirmCancel());
			}
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < 3;
		}

		@Override
		protected void onInfo( int index ) {
			GameScene.show(new WndTitledMessage(
					Icons.get(Icons.INFO),
					Messages.titleCase(enchantments[index].name()),
					enchantments[index].desc()));
		}

		@Override
		public void onBackPressed() {
			//do nothing, reader has to cancel
		}

	}

	public static class WndGlyphSelect extends WndOptions {

		private static Armor arm;
		private static Armor.Glyph[] glyphs;
		private static int slot;

		//used in PixelScene.restoreWindows
		public WndGlyphSelect() {
			this(arm, slot, glyphs[0], glyphs[1], glyphs[2]);
		}

		public WndGlyphSelect(Armor arm, int targetSlot, Armor.Glyph glyph1,
		                      Armor.Glyph glyph2, Armor.Glyph glyph3) {
			super(new ItemSprite(tieredScroll(targetSlot)),
					Messages.titleCase(tieredScroll(targetSlot).name()),
					Messages.get(ScrollOfEnchantment.class, "armor"),
					glyph1.name(),
					glyph2.name(),
					glyph3.name(),
					Messages.get(ScrollOfEnchantment.class, "cancel"));
			this.arm = arm;
			slot = targetSlot;
			glyphs = new Armor.Glyph[3];
			glyphs[0] = glyph1;
			glyphs[1] = glyph2;
			glyphs[2] = glyph3;

			WndEnchantSelect.wep = null;
		}

		@Override
		protected void onSelect(int index) {
			if (index < 3) {
				arm.inscribe(slot, glyphs[index]);
				GLog.p(Messages.get(StoneOfEnchantment.class, "armor"));
				((ScrollOfEnchantment) curItem).readAnimation();

				Sample.INSTANCE.play(Assets.Sounds.READ);
				Enchanting.show(curUser, arm);
			} else {
				GameScene.show(new WndConfirmCancel());
			}
		}

		@Override
		protected boolean hasInfo(int index) {
			return index < 3;
		}

		@Override
		protected void onInfo(int index) {
			GameScene.show(new WndTitledMessage(
					Icons.get(Icons.INFO),
					Messages.titleCase(glyphs[index].name()),
					glyphs[index].desc()));
		}

		@Override
		public void onBackPressed() {
			//do nothing, reader has to cancel
		}

	}

	public static class WndConfirmCancel extends WndOptions{

		public WndConfirmCancel(){
			super(new ItemSprite(new ScrollOfEnchantment()),
					Messages.titleCase(new ScrollOfEnchantment().name()),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn"),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn_yes"),
					Messages.get(ScrollOfEnchantment.class, "cancel_warn_no"));
		}

		@Override
		protected void onSelect(int index) {
			super.onSelect(index);
			if (index == 1){
				if (WndEnchantSelect.wep != null) {
					GameScene.show(new WndEnchantSelect());
				} else {
					GameScene.show(new WndGlyphSelect());
				}
			} else {
				WndEnchantSelect.wep = null;
				WndEnchantSelect.enchantments = null;
				WndGlyphSelect.arm = null;
				WndGlyphSelect.glyphs = null;
			}
		}

		@Override
		public void onBackPressed() {
			//do nothing
		}
	}

	private static ScrollOfEnchantment tieredScroll( int slot ) {
		ScrollOfEnchantment scroll = new ScrollOfEnchantment();
		scroll.level( slot );
		return scroll;
	}

	public static class MergeRecipe extends Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.size() != 2) return false;
			return ingredients.get(0) instanceof ScrollOfEnchantment
					&& ingredients.get(1) instanceof ScrollOfEnchantment
					&& ingredients.get(0).level() == ingredients.get(1).level()
					&& EnchantmentSlots.mergeChance( ingredients.get(0).level() ) > 0;
		}

		@Override
		public int cost( ArrayList<Item> ingredients ) {
			return 5 + ingredients.get(0).level() * 3;
		}

		@Override
		public Item brew( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			int level = ingredients.get(0).level();
			for (Item ingredient : ingredients) ingredient.quantity( ingredient.quantity() - 1 );
			ScrollOfEnchantment result = new ScrollOfEnchantment();
			result.level( Random.Int(100) < EnchantmentSlots.mergeChance(level) ? level + 1 : level );
			result.identify( false );
			return result;
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			ScrollOfEnchantment result = new ScrollOfEnchantment();
			result.level( ingredients.get(0).level() + 1 );
			return result;
		}
	}
}
