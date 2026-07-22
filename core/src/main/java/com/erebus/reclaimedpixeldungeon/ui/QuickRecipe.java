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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.items.ArcaneResin;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.LiquidMetal;
import com.erebus.reclaimedpixeldungeon.items.Recipe;
import com.erebus.reclaimedpixeldungeon.items.bombs.Bomb;
import com.erebus.reclaimedpixeldungeon.items.food.Blandfruit;
import com.erebus.reclaimedpixeldungeon.items.food.Food;
import com.erebus.reclaimedpixeldungeon.items.food.MeatPie;
import com.erebus.reclaimedpixeldungeon.items.food.MysteryMeat;
import com.erebus.reclaimedpixeldungeon.items.food.Pasty;
import com.erebus.reclaimedpixeldungeon.items.food.StewedMeat;
import com.erebus.reclaimedpixeldungeon.items.potions.Potion;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.AquaBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.BlizzardBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.CausticBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.InfernalBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.ShockingBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.brews.UnstableBrew;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.erebus.reclaimedpixeldungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.ExoticPotion;
import com.erebus.reclaimedpixeldungeon.items.scrolls.Scroll;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfReturn;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.erebus.reclaimedpixeldungeon.items.spells.Alchemize;
import com.erebus.reclaimedpixeldungeon.items.spells.BeaconOfReturning;
import com.erebus.reclaimedpixeldungeon.items.spells.CurseInfusion;
import com.erebus.reclaimedpixeldungeon.items.spells.MagicalInfusion;
import com.erebus.reclaimedpixeldungeon.items.spells.PhaseShift;
import com.erebus.reclaimedpixeldungeon.items.spells.ReclaimTrap;
import com.erebus.reclaimedpixeldungeon.items.spells.Recycle;
import com.erebus.reclaimedpixeldungeon.items.spells.SummonElemental;
import com.erebus.reclaimedpixeldungeon.items.spells.TelekineticGrab;
import com.erebus.reclaimedpixeldungeon.items.spells.UnstableSpell;
import com.erebus.reclaimedpixeldungeon.items.spells.WildEnergy;
import com.erebus.reclaimedpixeldungeon.items.stones.Runestone;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.plants.Plant;
import com.erebus.reclaimedpixeldungeon.scenes.AlchemyScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndInfoItem;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class QuickRecipe extends Component {
	
	private ArrayList<Item> ingredients;
	
	private ArrayList<ItemSlot> inputs;
	private QuickRecipe.arrow arrow;
	private ItemSlot output;
	private BitmapText outputRange;
	
	public QuickRecipe(Recipe.SimpleRecipe r){
		this(r, r.getIngredients(), r.sampleOutput(null));
	}
	
	public QuickRecipe(Recipe r, ArrayList<Item> inputs, final Item output) {
		
		ingredients = inputs;
		int cost = r.cost(inputs);
		boolean hasInputs = true;
		this.inputs = new ArrayList<>();
		for (final Item in : inputs) {
			anonymize(in);
			ItemSlot curr;
			curr = new ItemSlot(in) {
				{
					hotArea.blockLevel = PointerArea.NEVER_BLOCK;
				}

				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(in));
				}
			};

			int quantity = 0;
			if (Dungeon.hero != null) {
				ArrayList<Item> similar = Dungeon.hero.belongings.getAllSimilar(in);
				for (Item sim : similar) {
					//if we are looking for a specific item, it must be IDed
					if (sim.getClass() != in.getClass() || sim.isIdentified())
						quantity += sim.quantity();
				}
				if (quantity < in.quantity()) {
					curr.sprite.alpha(0.3f);
					hasInputs = false;
				}
			} else {
				hasInputs = false;
			}

			curr.showExtraInfo(false);
			add(curr);
			this.inputs.add(curr);
		}
		
		if (cost > 0) {
			arrow = new arrow(Icons.get(Icons.ARROW), cost);
			arrow.hardlightText(0x44CCFF);
		} else {
			arrow = new arrow(Icons.get(Icons.ARROW));
		}
		if (hasInputs) {
			arrow.icon.tint(1, 1, 0, 1);
			if (!(ShatteredPixelDungeon.scene() instanceof AlchemyScene)) {
				arrow.enable(false);
			}
		} else {
			arrow.icon.color(0, 0, 0);
			arrow.enable(false);
		}
		add(arrow);
		
		anonymize(output);
		this.output = new ItemSlot(output){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(output));
			}
		};
		if (Dungeon.hero != null && !hasInputs){
			this.output.sprite.alpha(0.3f);
		}
		this.output.showExtraInfo(false);
		add(this.output);

		if (r instanceof Potion.SeedToPotion || r instanceof ExoticPotion.PotionToExotic) {
			outputRange = new BitmapText( "1-3", PixelScene.pixelFont );
			outputRange.measure();
			outputRange.hardlight( 0xFFFFFF );
			add( outputRange );
		}
		
		layout();
	}
	
	@Override
	protected void layout() {
		
		height = 16;
		width = 0;

		int padding = inputs.size() == 1 ? 8 : 0;

		for (ItemSlot item : inputs){
			item.setRect(x + width + padding, y, 16, 16);
			width += 16 + padding;
		}
		
		arrow.setRect(x + width, y, 14, 16);
		width += 14;
		
		output.setRect(x + width, y, 16, 16);
		if (outputRange != null) {
			outputRange.x = output.right() - outputRange.width() + 2;
			outputRange.y = output.bottom() - outputRange.height() + 1;
			PixelScene.align( outputRange );
		}
		width += 16;

		width += padding;
	}
	
	//used to ensure that un-IDed items are not spoiled
	private void anonymize(Item item){
		if (item instanceof Potion){
			((Potion) item).anonymize();
		} else if (item instanceof Scroll){
			((Scroll) item).anonymize();
		}
	}
	
	public class arrow extends IconButton {
		
		BitmapText text;
		
		public arrow(){
			super();
		}
		
		public arrow( Image icon ){
			super( icon );
		}
		
		public arrow( Image icon, int count ){
			super( icon );
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;

			text = new BitmapText( Integer.toString(count), PixelScene.pixelFont);
			text.measure();
			add(text);
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			if (text != null){
				text.x = x;
				text.y = y;
				PixelScene.align(text);
			}
		}
		
		@Override
		protected void onPointerUp() {
			icon.brightness(1f);
		}

		@Override
		protected void onClick() {
			super.onClick();
			
			//find the window this is inside of and close it
			Group parent = this.parent;
			while (parent != null){
				if (parent instanceof Window){
					((Window) parent).hide();
					break;
				} else {
					parent = parent.parent;
				}
			}
			
			((AlchemyScene)ShatteredPixelDungeon.scene()).populate(ingredients, Dungeon.hero.belongings);
		}
		
		public void hardlightText(int color ){
			if (text != null) text.hardlight(color);
		}
	}
	
	//gets recipes for a particular alchemy guide page
	//a null entry indicates a break in section
	public static ArrayList<QuickRecipe> getRecipes( int pageIdx ){
		ArrayList<QuickRecipe> result = new ArrayList<>();
		switch (pageIdx){
			case 0: default:
				result.add(new QuickRecipe( new Potion.SeedToPotion(), new ArrayList<>(Arrays.asList(new Plant.Seed.PlaceHolder().quantity(3))), new WndBag.Placeholder(ItemSpriteSheet.POTION_HOLDER){
					@Override
					public String name() {
						return Messages.get(Potion.SeedToPotion.class, "name");
					}

					@Override
					public String info() {
						return "";
					}
				}));
				return result;
			case 1:
				Recipe r = new Scroll.ScrollToStone();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					if (cls == ScrollOfReturn.class) continue;
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					if (!scroll.isKnown()) scroll.anonymize();
					ArrayList<Item> in = new ArrayList<Item>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 2:
				result.add(new QuickRecipe( new StewedMeat.oneMeat() ));
				result.add(new QuickRecipe( new StewedMeat.twoMeat() ));
				result.add(new QuickRecipe( new StewedMeat.threeMeat() ));
				result.add(null);
				result.add(new QuickRecipe( new MeatPie.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Pasty(), new Food(), new MysteryMeat.PlaceHolder())),
						new MeatPie()));
				result.add(null);
				result.add(new QuickRecipe( new Blandfruit.CookFruit(),
						new ArrayList<>(Arrays.asList(new Blandfruit(), new Plant.Seed.PlaceHolder())),
						new Blandfruit(){

							public String name(){
								return Messages.get(Blandfruit.class, "cooked");
							}
							
							@Override
							public String info() {
								return "";
							}
						}));
				return result;
			case 3:
				r = new ExoticPotion.PotionToExotic();
				for (Class<?> cls : Generator.Category.POTION.classes){
					Potion pot = (Potion) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(pot));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 4:
				r = new ExoticScroll.ScrollToExotic();
				for (Class<?> cls : Generator.Category.SCROLL.classes){
					if (cls == ScrollOfReturn.class || !ExoticScroll.regToExo.containsKey( cls )) continue;
					Scroll scroll = (Scroll) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(scroll));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
				}
				return result;
			case 5:
				r = new Bomb.EnhanceBomb();
				int i = 0;
				for (Class<?> cls : Bomb.EnhanceBomb.validIngredients.keySet()){
					if (i == 2){
						result.add(null);
						i = 0;
					}
					Item item = (Item) Reflection.newInstance(cls);
					ArrayList<Item> in = new ArrayList<>(Arrays.asList(new Bomb(), item));
					result.add(new QuickRecipe( r, in, r.sampleOutput(in)));
					i++;
				}
				return result;
			case 6:
				result.add(new QuickRecipe( new LiquidMetal.Recipe(),
						new ArrayList<Item>(Arrays.asList(new MissileWeapon.PlaceHolder())),
						new LiquidMetal()));
				result.add(new QuickRecipe( new ArcaneResin.Recipe(),
						new ArrayList<Item>(Arrays.asList(new Wand.PlaceHolder())),
						new ArcaneResin()));
				return result;
			case 7:
				result.add(new QuickRecipe(new UnstableBrew.Recipe(), new ArrayList<>(Arrays.asList(new Potion.PlaceHolder(), new  Plant.Seed.PlaceHolder())), new UnstableBrew()));
				result.add(new QuickRecipe(new CausticBrew.Recipe()));
				result.add(new QuickRecipe(new BlizzardBrew.Recipe()));
				result.add(new QuickRecipe(new ShockingBrew.Recipe()));
				result.add(new QuickRecipe(new InfernalBrew.Recipe()));
				result.add(new QuickRecipe(new AquaBrew.Recipe()));
				result.add(null);
				result.add(null);
				result.add(new QuickRecipe(new ElixirOfHoneyedHealing.Recipe()));
				result.add(new QuickRecipe(new ElixirOfAquaticRejuvenation.Recipe()));
				result.add(new QuickRecipe(new ElixirOfArcaneArmor.Recipe()));
				result.add(new QuickRecipe(new ElixirOfIcyTouch.Recipe()));
				result.add(new QuickRecipe(new ElixirOfToxicEssence.Recipe()));
				result.add(new QuickRecipe(new ElixirOfDragonsBlood.Recipe()));
				result.add(new QuickRecipe(new ElixirOfFeatherFall.Recipe()));
				result.add(new QuickRecipe(new ElixirOfMight.Recipe()));
				return result;
			case 8:
				result.add(new QuickRecipe(new UnstableSpell.Recipe(), new ArrayList<>(Arrays.asList(new Scroll.PlaceHolder(), new  Runestone.PlaceHolder())), new UnstableSpell()));
				result.add(new QuickRecipe(new WildEnergy.Recipe()));
				result.add(new QuickRecipe(new TelekineticGrab.Recipe()));
				result.add(new QuickRecipe(new PhaseShift.Recipe()));
				if (!PixelScene.landscape()) result.add(null);
				result.add(null);
				result.add(new QuickRecipe(new Alchemize.Recipe(), new ArrayList<>(Arrays.asList(new Plant.Seed.PlaceHolder(), new Runestone.PlaceHolder())), new Alchemize().quantity(8)));
				result.add(new QuickRecipe(new CurseInfusion.Recipe()));
				result.add(new QuickRecipe(new MagicalInfusion.Recipe()));
				result.add(new QuickRecipe(new Recycle.Recipe()));
				if (!PixelScene.landscape()) result.add(null);
				result.add(null);
				result.add(new QuickRecipe(new ReclaimTrap.Recipe()));
				result.add(new QuickRecipe(new SummonElemental.Recipe()));
				return result;
		}
	}
	
}
