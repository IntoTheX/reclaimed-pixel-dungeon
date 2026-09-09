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

package com.erebus.reclaimedpixeldungeon.actors.mobs.npcs;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.armor.ClassArmor;
import com.erebus.reclaimedpixeldungeon.items.food.Food;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfReturn;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class LostDefender extends NPC {

	private static final String CANDIDATE = "candidate";
	private static final String REQUEST = "request";

	private static final int REQUEST_FOOD = 0;
	private static final int REQUEST_HEALING = 1;
	private static final int REQUEST_RETURN = 2;

	private HomebaseState.DefenderRecord candidate;
	private int request;

	{
		spriteClass = HomebaseDefenderSprite.class;
		properties.add( Property.IMMOVABLE );
	}

	public LostDefender() {
		candidate = HomebaseState.DefenderRecord.randomCandidate();
		request = Random.Int( 3 );
	}

	@Override
	public CharSprite sprite() {
		return new HomebaseDefenderSprite( heroClass(), armorTier(), candidate == null ? null : candidate.rarity() );
	}

	private HeroClass heroClass() {
		if (candidate == null) candidate = HomebaseState.DefenderRecord.randomCandidate();
		switch (candidate.archetype()) {
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

	private int armorTier() {
		if (candidate == null) candidate = HomebaseState.DefenderRecord.randomCandidate();
		Armor armor = candidate.armor();
		if (armor instanceof ClassArmor) return 6;
		return armor == null ? 0 : armor.tier;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact( Char c ) {
		if (sprite != null) sprite.turnTo( pos, c.pos );
		if (c != Dungeon.hero) return true;
		if (candidate == null) candidate = HomebaseState.DefenderRecord.randomCandidate();

		Game.runOnRenderThread( new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndOptions(
						new HomebaseDefenderSprite( heroClass(), armorTier(), candidate.rarity() ),
						candidate.defenderName(),
						candidate.title() + "\n\nA lost survivor needs " + requestedSupplyName() + " before they can make the journey back to the homebase.",
						"Give " + requestedSupplyName(),
						"Leave" ) {
					@Override
					protected boolean enabled( int index ) {
						return index != 0 || requestedSupply() != null;
					}

					@Override
					protected void onSelect( int index ) {
						if (index == 0) recruitWith( requestedSupply() );
					}
				} );
			}
		} );
		return true;
	}

	private String requestedSupplyName() {
		switch (request) {
			case REQUEST_HEALING:
				return "a healing potion";
			case REQUEST_RETURN:
				return "a return scroll";
			case REQUEST_FOOD:
			default:
				return "food";
		}
	}

	private Item requestedSupply() {
		switch (request) {
			case REQUEST_HEALING:
				return Dungeon.hero.belongings.getItem( PotionOfHealing.class );
			case REQUEST_RETURN:
				return Dungeon.hero.belongings.getItem( ScrollOfReturn.class );
			case REQUEST_FOOD:
			default:
				return Dungeon.hero.belongings.getItem( Food.class );
		}
	}

	private void recruitWith( Item payment ) {
		if (payment == null || Dungeon.homebase == null) return;
		payment.detach( Dungeon.hero.belongings.backpack );
		HomebaseState.DefenderRecord recruited = Dungeon.homebase.recruitDefender( candidate );
		GLog.p( recruited.defenderName() + " will return to the homebase." );
		ReclaimedTutorial.flash( Document.GUIDE_DEFENDERS );
		candidate = null;
		if (sprite != null) {
			sprite.operate( pos, new Callback() {
				@Override
				public void call() {
					destroy();
					if (sprite != null) sprite.killAndErase();
				}
			} );
		} else {
			destroy();
		}
	}

	@Override
	public String name() {
		return candidate == null ? "lost survivor" : candidate.defenderName();
	}

	@Override
	public String description() {
		return "A lost survivor who can be recruited to defend the homebase.";
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (candidate != null) {
			bundle.put( CANDIDATE, candidate );
		}
		bundle.put( REQUEST, request );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( CANDIDATE )) {
			candidate = (HomebaseState.DefenderRecord)bundle.get( CANDIDATE );
		}
		if (candidate == null) {
			candidate = HomebaseState.DefenderRecord.randomCandidate();
		}
		if (bundle.contains( REQUEST )) {
			request = bundle.getInt( REQUEST );
		}
		spriteClass = HomebaseDefenderSprite.class;
	}
}
