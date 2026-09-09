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
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.effects.Enchanting;
import com.erebus.reclaimedpixeldungeon.effects.particles.PurpleParticle;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Stylus extends Item {
	
	private static final float TIME_TO_INSCRIBE = 2;
	
	private static final String AC_INSCRIBE = "INSCRIBE";
	
	{
		image = ItemSpriteSheet.STYLUS;
		
		stackable = true;

		defaultAction = AC_INSCRIBE;

		bones = true;
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
		return Messages.get( this, "desc", EnchantmentSlots.roman(level()) ) + Messages.get( this, "merge_desc" );
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_INSCRIBE );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_INSCRIBE)) {

			curUser = hero;
			GameScene.selectItem( itemSelector );
			
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
	
	private void inscribe( Armor armor ) {

		if (!armor.cursedKnown){
			GLog.w( Messages.get(this, "identify"));
			return;
		} else if (armor.cursed || armor.hasCurseGlyph()){
			GLog.w( Messages.get(this, "cursed"));
			return;
		}
		
		detach(curUser.belongings.backpack);
		Catalog.countUse(getClass());

		GLog.w( Messages.get(this, "inscribed"));

		armor.inscribeRandom( EnchantmentSlots.slotForLevel(level()) );
		
		curUser.sprite.operate(curUser.pos);
		curUser.sprite.centerEmitter().start(PurpleParticle.BURST, 0.05f, 10);
		Enchanting.show(curUser, armor);
		Sample.INSTANCE.play(Assets.Sounds.BURNING);
		
		curUser.spend(TIME_TO_INSCRIBE);
		curUser.busy();
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(Stylus.class, "prompt");
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
		public void onSelect( Item item ) {
			if (item != null) {
				Stylus.this.inscribe( (Armor)item );
			}
		}
	};

	public static class MergeRecipe extends Recipe {

		@Override
		public boolean testIngredients( ArrayList<Item> ingredients ) {
			if (ingredients == null || ingredients.size() != 2) return false;
			return ingredients.get(0) instanceof Stylus
					&& ingredients.get(1) instanceof Stylus
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
			Stylus result = new Stylus();
			result.level( Random.Int(100) < EnchantmentSlots.mergeChance(level) ? level + 1 : level );
			result.identify( false );
			return result;
		}

		@Override
		public Item sampleOutput( ArrayList<Item> ingredients ) {
			if (!testIngredients( ingredients )) return null;
			Stylus result = new Stylus();
			result.level( ingredients.get(0).level() + 1 );
			result.identify( false );
			return result;
		}
	}
}
