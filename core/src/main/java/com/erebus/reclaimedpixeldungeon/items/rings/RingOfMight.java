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

package com.erebus.reclaimedpixeldungeon.items.rings;


import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfMight extends Ring {

	private static final double HT_GROWTH = 1.035;
	private static final double MIN_HT_MULTIPLIER = 0.01;
	private static final double MAX_HT_MULTIPLIER = 1_000_000.0;

	{
		icon = ItemSpriteSheet.Icons.RING_MIGHT;
		buffClass = Might.class;
	}

	@Override
	public boolean doEquip(Hero hero) {
		if (super.doEquip(hero)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			hero.updateHT( false );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item upgrade() {
		super.upgrade();
		updateTargetHT();
		return this;
	}

	@Override
	public Item level(int value) {
		super.level(value);
		updateTargetHT();
		return this;
	}
	
	private void updateTargetHT(){
		if (buff != null && buff.target instanceof Hero){
			((Hero) buff.target).updateHT( false );
		}
	}
	
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					soloBonus(), multiplierPercent( soloBuffedBonus() ));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						getBonus(Dungeon.hero, Might.class), multiplierPercent( combinedBuffedBonus(Dungeon.hero) ));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", 1, Messages.decimalFormat("#.##", 3.5f));
		}
	}

	@Override
	public String upgradeStat1(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Integer.toString(level+1);
	}

	@Override
	public String upgradeStat2(int level) {
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return multiplierPercent( level + 1 ) + "%";
	}

	@Override
	protected RingBuff buff( ) {
		return new Might();
	}
	
	public static int strengthBonus( Char target ){
		return getBonus( target, Might.class );
	}
	
	public static float HTMultiplier( Char target ){
		return (float)HTMultiplierDouble( target );
	}

	public static double HTMultiplierDouble( Char target ){
		return boundedHTMultiplier( getBuffedBonus(target, Might.class) );
	}

	private static double boundedHTMultiplier( int bonus ) {
		double multiplier = Math.pow( HT_GROWTH, bonus );
		if (!Double.isFinite( multiplier )) {
			return bonus < 0 ? MIN_HT_MULTIPLIER : MAX_HT_MULTIPLIER;
		}
		return Math.max( MIN_HT_MULTIPLIER, Math.min( MAX_HT_MULTIPLIER, multiplier ) );
	}

	private static String multiplierPercent( int bonus ) {
		return Messages.decimalFormat("#.##", 100d * (boundedHTMultiplier( bonus ) - 1d));
	}

	public class Might extends RingBuff {
	}
}

