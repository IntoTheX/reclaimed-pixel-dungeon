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

package com.erebus.reclaimedpixeldungeon.items.spells;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Degrade;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Invisibility;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.erebus.reclaimedpixeldungeon.items.wands.Wand;
import com.erebus.reclaimedpixeldungeon.items.weapon.Weapon;
import com.erebus.reclaimedpixeldungeon.journal.Catalog;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndBag;
import com.erebus.reclaimedpixeldungeon.windows.WndUpgrade;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class MagicalInfusion extends InventorySpell {
	
	{
		image = ItemSpriteSheet.MAGIC_INFUSE;

		unique = true;

		talentFactor = 2;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return item.isUpgradable();
	}

	@Override
	protected void onItemSelected( Item item ) {

		GameScene.show(new WndUpgrade(this, item, false));

	}

	public void reShowSelector(){
		curItem = this;
		GameScene.selectItem(itemSelector);
	}

	public WndBag.ItemSelector getSelector(){
		curItem = this;
		return itemSelector;
	}

	public void useAnimation(){
		curUser.spend(1f);
		curUser.busy();
		(curUser.sprite).operate(curUser.pos);

		Sample.INSTANCE.play(Assets.Sounds.READ);
		Invisibility.dispel();

		Catalog.countUse(curItem.getClass());
		if (Random.Float() < ((Spell) curItem).talentChance) {
			Talent.onScrollUsed(curUser, curUser.pos, ((Spell) curItem).talentFactor, getClass());
		}
	}

	public Item upgradeItem( Item item ){
		ScrollOfUpgrade.upgrade(curUser);

		Degrade.detach( curUser, Degrade.class );

		if (item instanceof Weapon && ((Weapon) item).enchantmentCount() > 0) {
			item = ((Weapon) item).upgrade(true);
		} else if (item instanceof Armor && ((Armor) item).glyphCount() > 0) {
			item = ((Armor) item).upgrade(true);
		} else {
			boolean wasCursed = item.cursed;
			boolean wasCurseInfused = item instanceof Wand && ((Wand) item).curseInfusionBonus;
			item = item.upgrade();
			if (wasCursed) item.cursed = true;
			if (wasCurseInfused) ((Wand) item).curseInfusionBonus = true;
		}

		GLog.p( Messages.get(this, "infuse") );
		Badges.validateItemLevelAquired(item);

		Catalog.countUse(item.getClass());

		Statistics.upgradesUsed++;

		return item;
	}
	
	@Override
	public int value() {
		return 60 * quantity;
	}

	@Override
	public int energyVal() {
		return 12 * quantity;
	}
	
	public static class Recipe extends com.erebus.reclaimedpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{ScrollOfUpgrade.class};
			inQuantity = new int[]{1};
			
			cost = 12;
			
			output = MagicalInfusion.class;
			outQuantity = 1;
		}
		
	}
}
