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

package com.erebus.reclaimedpixeldungeon.items.food;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Challenges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hunger;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.effects.SpellSprite;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.artifacts.HornOfPlenty;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Food extends Item {

	public static final float TIME_TO_EAT	= 3f;
	
	public static final String AC_EAT	= "EAT";
	
	public float energy = Hunger.HUNGRY;
	
	{
		stackable = true;
		image = ItemSpriteSheet.RATION;

		defaultAction = AC_EAT;

		bones = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_EAT );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_EAT )) {
			
			detach( hero.belongings.backpack );
			Catalog.countUse(getClass());
			
			satisfy(hero);
			GLog.i( Messages.get(this, "eat_msg") );
			
			hero.sprite.operate( hero.pos );
			hero.busy();
			SpellSprite.show( hero, SpellSprite.FOOD );
			eatSFX();
			
			hero.spend( eatingTime() );

			Talent.onFoodEaten(hero, energy, this);
			
			Statistics.foodEaten++;
			Badges.validateFoodEaten();
			
		}
	}

	protected void eatSFX(){
		Sample.INSTANCE.play( Assets.Sounds.EAT );
	}

	protected float eatingTime(){
		if (Dungeon.hero.hasTalent(Talent.IRON_STOMACH)
			|| Dungeon.hero.hasTalent(Talent.ENERGIZING_MEAL)
			|| Dungeon.hero.hasTalent(Talent.MYSTICAL_MEAL)
			|| Dungeon.hero.hasTalent(Talent.INVIGORATING_MEAL)
			|| Dungeon.hero.hasTalent(Talent.FOCUSED_MEAL)
			|| Dungeon.hero.hasTalent(Talent.ENLIGHTENING_MEAL)){
			return TIME_TO_EAT - 2;
		} else {
			return TIME_TO_EAT;
		}
	}
	
	protected void satisfy( Hero hero ){
		float foodVal = energy;
		if (Dungeon.isChallenged(Challenges.NO_FOOD)){
			foodVal /= 3f;
		}

		Artifact.ArtifactBuff buff = hero.buff( HornOfPlenty.hornRecharge.class );
		if (buff != null && buff.isCursed()){
			foodVal *= 0.67f;
			GLog.n( Messages.get(Hunger.class, "cursedhorn") );
		}

		Buff.affect(hero, Hunger.class).satisfy(foodVal);
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int value() {
		return 10 * quantity;
	}
}
