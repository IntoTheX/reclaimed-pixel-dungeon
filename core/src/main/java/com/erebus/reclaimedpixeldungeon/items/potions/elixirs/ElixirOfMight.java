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

package com.erebus.reclaimedpixeldungeon.items.potions.elixirs;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfStrength;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class ElixirOfMight extends Elixir {

	{
		image = ItemSpriteSheet.ELIXIR_MIGHT;

		unique = true;

		talentFactor = 2f;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();
		
		hero.STR++;
		hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "1", FloatingText.STRENGTH);
		
		Buff.affect(hero, HTBoost.class).reset();
		HTBoost boost = Buff.affect(hero, HTBoost.class);
		boost.reset();
		
		hero.updateHT( true );
		GLog.p( Messages.get(this, "msg", hero.STR()) );

		Badges.validateStrengthAttained();
		Badges.validateDuelistUnlock();
	}
	
	public String desc() {
		return Messages.get(this, "desc", HTBoost.boost(Dungeon.hero != null ? Dungeon.hero.HT : 20));
	}
	
	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfStrength.class};
			inQuantity = new int[]{1};
			
			cost = 16;
			
			output = ElixirOfMight.class;
			outQuantity = 1;
		}
		
	}
	
	public static class HTBoost extends Buff {
		
		{
			type = buffType.POSITIVE;
		}
		
		private int left;
		
		public void reset(){
			left = 5;
		}
		
		public int boost(){
			return Math.round(left*boost(15 + 5*((Hero)target).lvl)/5f);
		}
		
		public static int boost(int HT){
			return Math.round(4 + HT/20f);
		}
		
		public void onLevelUp(){
			left --;
			if (left <= 0){
				detach();
			}
		}
		
		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.5f, 0f);
		}

		@Override
		public float iconFadePercent() {
			return (5f - left) / 5f;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(left);
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", boost(), left);
		}
		
		private static String LEFT = "left";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getInt(LEFT);
		}
	}
}
