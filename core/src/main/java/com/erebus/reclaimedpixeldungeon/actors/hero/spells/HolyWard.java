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

package com.erebus.reclaimedpixeldungeon.actors.hero.spells;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.FlavourBuff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroSubClass;
import com.erebus.reclaimedpixeldungeon.effects.Enchanting;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.artifacts.HolyTome;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class HolyWard extends ClericSpell {

	public static final HolyWard INSTANCE = new HolyWard();

	@Override
	public int icon() {
		return HeroIcon.HOLY_WARD;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		Buff.affect(hero, HolyArmBuff.class, 50f);
		Item.updateQuickslot();

		Sample.INSTANCE.play(Assets.Sounds.READ);

		hero.sprite.operate(hero.pos);
		if (hero.belongings.armor() != null) Enchanting.show(hero, hero.belongings.armor());

		onSpellCast(tome, hero);
	}

	@Override
	public String desc(){
		String desc = Messages.get(this, "desc");
		if (Dungeon.hero.subClass == HeroSubClass.PALADIN){
			desc += "\n\n" + Messages.get(this, "desc_paladin");
		}
		return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public static class HolyArmBuff extends FlavourBuff {

		public static final float DURATION	= 50f;

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_ARMOR;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public String desc() {
			if (Dungeon.hero.subClass == HeroSubClass.PALADIN){
				return Messages.get(this, "desc_paladin", dispTurns());
			} else {
				return Messages.get(this, "desc", dispTurns());
			}
		}

		@Override
		public void detach() {
			super.detach();
			Item.updateQuickslot();
		}

		public void extend(float extension){
			if (cooldown()+extension <= 2*DURATION){
				spend(extension);
			} else {
				postpone(2*DURATION);
			}
		}
	}

}
