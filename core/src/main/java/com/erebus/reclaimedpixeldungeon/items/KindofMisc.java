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

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.hero.Talent;
import com.erebus.reclaimedpixeldungeon.items.artifacts.Artifact;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;

import java.util.ArrayList;


public abstract class KindofMisc extends EquipableItem {

	@Override
	public boolean doEquip(final Hero hero) {

		boolean equipFull = !hero.belongings.hasAvailableSlotFor( this );

		if (equipFull) {

			ArrayList<KindofMisc> candidates = hero.belongings.replacementCandidatesFor( this );
			final KindofMisc[] miscs = candidates.toArray( new KindofMisc[0] );
			String[] options = new String[miscs.length];
			for (int i = 0; i < miscs.length; i++) {
				options[i] = Messages.titleCase(miscs[i].title());
			}

			GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.get(KindofMisc.class, "unequip_title"),
							Messages.get(KindofMisc.class, "unequip_message"),
							options) {

						@Override
						protected void onSelect(int index) {

							KindofMisc equipped = miscs[index];
							int slot = Dungeon.quickslot.getSlot(KindofMisc.this);
							slotOfUnequipped = -1;
							KindofMisc.this.detachAll(Dungeon.hero.belongings.backpack);
							if (equipped.doUnequip(hero, true, false)) {
								if (!doEquip(hero) && !hero.belongings.contains(KindofMisc.this)
										&& !KindofMisc.this.collect()) {
									Dungeon.level.drop(KindofMisc.this, hero.pos).sprite.drop();
								}
							} else if (!KindofMisc.this.collect()) {
								Dungeon.level.drop(KindofMisc.this, hero.pos).sprite.drop();
							}
							if (slot != -1) {
								Dungeon.quickslot.setSlot(slot, KindofMisc.this);
							} else if (slotOfUnequipped != -1 && defaultAction() != null){
								Dungeon.quickslot.setSlot(slotOfUnequipped, KindofMisc.this);
							}
							updateQuickslot();
						}
					});

			return false;

		} else {

			// 15/25% chance
			if (hero.heroClass != HeroClass.CLERIC && hero.hasTalent(Talent.HOLY_INTUITION)
					&& cursed && !cursedKnown
					&& Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
				cursedKnown = true;
				GLog.p(Messages.get(this, "curse_detected"));
				return false;
			}

			if (this instanceof Artifact){
				if (!hero.belongings.equipMiscItem( this )) return false;
			} else if (this instanceof Ring){
				if (!hero.belongings.equipMiscItem( this )) return false;
			}

			detach( hero.belongings.backpack );

			Talent.onItemEquipped(hero, this);
			activate( hero );
			hero.updateHT( false );

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(this, "equip_cursed", this) );
			}

			hero.spendAndNext( timeToEquip(hero) );
			return true;

		}

	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){

			hero.belongings.clearMiscItem( this );
			hero.updateHT( false );

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero != null && hero.belongings.isMiscItemEquipped( this );
	}

}
