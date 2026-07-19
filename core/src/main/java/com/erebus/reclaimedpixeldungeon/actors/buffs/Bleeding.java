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

package com.erebus.reclaimedpixeldungeon.actors.buffs;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.effects.Splash;
import com.erebus.reclaimedpixeldungeon.items.weapon.curses.Sacrificial;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.Sickle;
import com.erebus.reclaimedpixeldungeon.levels.features.Chasm;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Bleeding extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = false;
	}
	
	protected float level;

	//used in specific cases where the source of the bleed is important for death logic
	private Class source;

	public float level(){
		return level;
	}
	
	private static final String LEVEL	= "level";
	private static final String SOURCE	= "source";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
		bundle.put( SOURCE, source );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getFloat( LEVEL );
		source = bundle.getClass( SOURCE );
	}
	
	public void set( float level ) {
		set( level, null );
	}

	public void set( float level, Class source ){
		if (target != null) {
			float resistance = target.resist( Bleeding.class );
			if (resistance <= 0f) {
				Buff.showResisted( target );
				if (this.level <= 0f) detach();
				return;
			}
			level *= resistance;
			if (level <= 0f) {
				Buff.showResisted( target );
				if (this.level <= 0f) detach();
				return;
			}
		}
		if (this.level < level) {
			boolean wasInactive = this.level <= 0f;
			this.level = Math.max(this.level, level);
			this.source = source;
			if (wasInactive) {
				announceBleeding();
			}
		}
	}

	@Override
	public String heroMessage(){
		return null;
	}

	private void announceBleeding() {
		if (target != null && target.sprite != null) {
			target.sprite.showStatus( CharSprite.WARNING, Messages.titleCase( name() ) );
		}
		if (target == Dungeon.hero) {
			String msg = super.heroMessage();
			if (msg != null) {
				GLog.w( msg );
			}
		}
	}

	public void extend( float amount ) {
		level += amount;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BLEEDING;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.round(level));
	}
	
	@Override
	public boolean act() {
		if (target.isAlive()) {
			
			level = Random.NormalFloat(level / 2f, level);
			int dmg = Math.round(level);
			
			if (dmg > 0) {
				
				target.damage( dmg, this );
				if (target.sprite.visible) {
					Splash.at( target.sprite.center(), -PointF.PI / 2, PointF.PI / 6,
							target.sprite.blood(), Math.min( 10 * dmg / target.HT, 10 ) );
				}
				
				if (target == Dungeon.hero && !target.isAlive()) {
					if (source == Chasm.class){
						Badges.validateDeathFromFalling();
					} else if (source == Sacrificial.class){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
				}

				if (source == Sickle.HarvestBleedTracker.class && !target.isAlive()){
					MeleeWeapon.onAbilityKill(Dungeon.hero, target);
				}
				
				spend( TICK );
			} else {
				detach();
			}
			
		} else {
			
			detach();
			
		}
		
		return true;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round(level));
	}
}
