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
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.FlavourBuff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.erebus.reclaimedpixeldungeon.effects.SpellSprite;
import com.erebus.reclaimedpixeldungeon.items.artifacts.HolyTome;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class DivineSense extends ClericSpell {

	public static final DivineSense INSTANCE = new DivineSense();

	@Override
	public int icon() {
		return HeroIcon.DIVINE_SENSE;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.hasTalent(Talent.DIVINE_SENSE);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		Buff.prolong(hero, DivineSenseTracker.class, DivineSenseTracker.DURATION);
		Dungeon.observe();

		Sample.INSTANCE.play(Assets.Sounds.READ);

		SpellSprite.show(hero, SpellSprite.VISION);
		hero.sprite.operate(hero.pos);
		hero.next();

		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			Buff.prolong(ally, DivineSenseTracker.class, DivineSenseTracker.DURATION);
			SpellSprite.show(ally, SpellSprite.VISION);
		}

		onSpellCast(tome, hero);
	}

	public String desc(){
		return Messages.get(this, "desc", 4+4*Dungeon.hero.pointsInTalent(Talent.DIVINE_SENSE)) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public static class DivineSenseTracker extends FlavourBuff {

		public static final float DURATION = 50f;

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_SIGHT;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public void detach() {
			super.detach();
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

}
