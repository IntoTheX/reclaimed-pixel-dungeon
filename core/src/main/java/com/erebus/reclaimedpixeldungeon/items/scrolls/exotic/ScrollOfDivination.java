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

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.effects.Identification;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.potions.Potion;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.scrolls.Scroll;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;

public class ScrollOfDivination extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_DIVINATE;
	}
	
	@Override
	public void doRead() {

		detach(curUser.belongings.backpack);
		curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );
		
		Sample.INSTANCE.play( Assets.Sounds.READ );
		
		HashSet<Class<? extends Potion>> potions = Potion.getUnknown();
		HashSet<Class<? extends Scroll>> scrolls = Scroll.getUnknown();
		HashSet<Class<? extends Ring>> rings = Ring.getUnknown();
		
		int total = potions.size() + scrolls.size() + rings.size();
		
		ArrayList<Item> IDed = new ArrayList<>();
		int left = 4;
		
		float[] baseProbs = new float[]{3, 3, 3};
		float[] probs = baseProbs.clone();
		
		while (left > 0 && total > 0) {
			switch (Random.chances(probs)) {
				default:
					probs = baseProbs.clone();
					continue;
				case 0:
					if (potions.isEmpty()) {
						probs[0] = 0;
						continue;
					}
					probs[0]--;
					Potion p = Reflection.newInstance(Random.element(potions));
					p.identify();
					IDed.add(p);
					potions.remove(p.getClass());
					break;
				case 1:
					if (scrolls.isEmpty()) {
						probs[1] = 0;
						continue;
					}
					probs[1]--;
					Scroll s = Reflection.newInstance(Random.element(scrolls));
					s.identify();
					IDed.add(s);
					scrolls.remove(s.getClass());
					break;
				case 2:
					if (rings.isEmpty()) {
						probs[2] = 0;
						continue;
					}
					probs[2]--;
					Ring r = Reflection.newInstance(Random.element(rings));
					r.setKnown();
					IDed.add(r);
					rings.remove(r.getClass());
					break;
			}
			left --;
			total --;
		}

		if (left > 0) {
			ArrayList<Item> unidentified = unidentifiedInventoryItems();
			while (left > 0 && !unidentified.isEmpty()) {
				Item item = Random.element(unidentified);
				item.identify();
				Badges.validateItemLevelAquired(item);
				IDed.add(item);
				unidentified.remove(item);
				left--;
			}
		}

		if (IDed.isEmpty()){
			GLog.n( Messages.get(this, "nothing_left") );
		} else {
			GameScene.show(new WndDivination(IDed));
		}

		readAnimation();
		identify();
	}

	private ArrayList<Item> unidentifiedInventoryItems(){
		ArrayList<Item> items = new ArrayList<>();
		for (Item item : curUser.belongings.backpack) {
			if (!item.isIdentified()) {
				items.add(item);
			}
		}
		return items;
	}
	
	private class WndDivination extends Window {
		
		private static final int WIDTH = 120;
		
		WndDivination(ArrayList<Item> IDed ){
			IconTitle cur = new IconTitle(new ItemSprite(ScrollOfDivination.this),
					Messages.titleCase(Messages.get(ScrollOfDivination.class, "name")));
			cur.setRect(0, 0, WIDTH, 0);
			add(cur);
			
			RenderedTextBlock msg = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
			msg.maxWidth(120);
			msg.setPos(0, cur.bottom() + 2);
			add(msg);
			
			float pos = msg.bottom() + 10;
			
			for (Item i : IDed){
				
				cur = new IconTitle(i);
				cur.setRect(0, pos, WIDTH, 0);
				add(cur);
				pos = cur.bottom() + 2;
				
			}
			
			resize(WIDTH, (int)pos);
		}
		
	}
}
