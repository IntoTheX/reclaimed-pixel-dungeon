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
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpatialGeode extends Item {

	public static final String AC_EXPAND = "EXPAND";
	private static final int SLOT_BONUS = 5;
	private static final float BOSS_DROP_CHANCE = 0.05f;

	{
		image = ItemSpriteSheet.SPATIAL_GEODE;
		stackable = true;
		bones = false;
		defaultAction = AC_EXPAND;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_EXPAND );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_EXPAND )) {
			final int targetCycle = targetCycle( hero );
			final ArrayList<Bag> bags = eligibleBags( hero, targetCycle );
			if (bags.isEmpty()) {
				GLog.w( Messages.get( this, "no_bag" ) );
				return;
			}

			String[] options = new String[bags.size()];
			for (int i = 0; i < bags.size(); i++) {
				Bag bag = bags.get( i );
				options[i] = Messages.get( this, "option",
						Messages.titleCase( bag.name() ),
						bag.capacity(),
						bag.capacity() + SLOT_BONUS );
			}

			GameScene.show( new WndOptions( new ItemSprite( this ), name(), Messages.get( this, "prompt" ), options ) {
				@Override
				protected void onSelect( int index ) {
					Bag bag = bags.get( index );
					bag.expandCapacity( SLOT_BONUS, targetCycle );
					GLog.p( Messages.get( SpatialGeode.this, "expanded",
							Messages.titleCase( bag.name() ),
							SLOT_BONUS,
							bag.capacity() ) );
					Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
					if (hero.sprite != null) {
						hero.sprite.operate( hero.pos );
					}
					hero.spendAndNext( Actor.TICK );
					detach( hero.belongings.backpack );
				}
			} );
		}
	}

	private static int targetCycle( Hero hero ) {
		int cycle = Integer.MAX_VALUE;
		for (Bag bag : hero.belongings.getBags()) {
			cycle = Math.min( cycle, bag.expansionCycle() );
		}
		return cycle == Integer.MAX_VALUE ? 1 : cycle + 1;
	}

	private static ArrayList<Bag> eligibleBags( Hero hero, int targetCycle ) {
		ArrayList<Bag> result = new ArrayList<>();
		for (Bag bag : hero.belongings.getBags()) {
			if (bag.expansionCycle() < targetCycle) {
				result.add( bag );
			}
		}
		return result;
	}

	public static void rollBossDrop( Mob mob ) {
		if (mob == null
				|| Dungeon.depth <= 0
				|| Dungeon.depth % 50 != 0
				|| !Dungeon.bossLevel()
				|| !mob.properties().contains( Char.Property.BOSS )) {
			return;
		}
		if (Random.Float() >= BOSS_DROP_CHANCE) {
			return;
		}
		Dungeon.level.drop( new SpatialGeode(), mob.pos ).sprite.drop( mob.pos );
		GLog.p( Messages.get( SpatialGeode.class, "boss_drop" ) );
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
	public int value() {
		return 1000 * quantity;
	}
}
