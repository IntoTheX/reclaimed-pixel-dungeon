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

package com.erebus.reclaimedpixeldungeon.items.keys;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.windows.WndSupportPrompt;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.io.IOException;

public class WornKey extends Key {
	
	{
		image = ItemSpriteSheet.WORN_KEY;
	}
	
	public WornKey() {
		this( 0 );
	}
	
	public WornKey( int depth ) {
		super();
		this.depth = depth;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if(!SPDSettings.supportNagged()){
			try {
				Dungeon.saveAll();
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						ShatteredPixelDungeon.scene().add(new WndSupportPrompt());
					}
				});
			} catch (IOException e) {
				ShatteredPixelDungeon.reportException(e);
			}
			
		}
		
		return super.doPickUp(hero, pos);
	}

}
