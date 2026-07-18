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

package com.erebus.reclaimedpixeldungeon.levels;

import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Blob;
import com.erebus.reclaimedpixeldungeon.actors.blobs.VaultFlameTraps;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfRegrowth;
import com.erebus.reclaimedpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.erebus.reclaimedpixeldungeon.levels.builders.Builder;
import com.erebus.reclaimedpixeldungeon.levels.builders.GridBuilder;
import com.erebus.reclaimedpixeldungeon.levels.features.LevelTransition;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.levels.rooms.Room;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.AlternatingTrapsRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultCircleRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultCrossRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultEnemyCenterRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultEntranceRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultFinalRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultLasersRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultLongRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultBookcaseTreasureRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultFlamePathRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultLaserTreasureRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultManyScansRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultQuadrantsRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultRingRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultRingsRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.VaultSimpleEnemyTreasureRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultMultipleEnemyTreasureRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.vault.treasure.VaultSingleEnemyTreasureRoom;
import com.erebus.reclaimedpixeldungeon.levels.traps.Trap;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class VaultLevel extends CityLevel {

	@Override
	protected boolean build() {
		itemsToSpawn.clear();

		for (int i = 0; i < 10; i++){
			Item item = Generator.randomUsingDefaults(Random.oneOf(
					Generator.Category.WEP_T2, Generator.Category.WEP_T2,
					Generator.Category.ARMOR, Generator.Category.ARMOR,
					Generator.Category.WAND,
					Generator.Category.RING));
			//regrowth is disallowed as it can be used to farm HP regen
			if (item instanceof WandOfRegrowth){
				continue;
			}
			if (item.cursed){
				item.cursed = false;
				if (item instanceof MeleeWeapon && ((MeleeWeapon) item).hasCurseEnchant()){
					((MeleeWeapon) item).enchant(null);
				} else if (item instanceof Armor && ((Armor) item).hasCurseGlyph()){
					((Armor) item).inscribe(null);
				}
			}
			//not true ID, prevents extra info about rings leaking to main game
			item.levelKnown = item.cursedKnown = true;
			addItemToSpawn(item);
		}

		return super.build();
	}

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> initRooms = new ArrayList<>();

		initRooms.add(roomEntrance = new VaultEntranceRoom());

		initRooms.add(new VaultRingRoom());
		initRooms.add(new VaultRingRoom());
		initRooms.add(new VaultCircleRoom());
		initRooms.add(new VaultCircleRoom());
		initRooms.add(new VaultCrossRoom());
		initRooms.add(new VaultCrossRoom());
		initRooms.add(new VaultQuadrantsRoom());
		initRooms.add(new VaultQuadrantsRoom());
		initRooms.add(new VaultRingsRoom());
		initRooms.add(new VaultRingsRoom());

		initRooms.add(new VaultEnemyCenterRoom());
		initRooms.add(new VaultEnemyCenterRoom());
		initRooms.add(new VaultSimpleEnemyTreasureRoom());
		initRooms.add(new AlternatingTrapsRoom());
		initRooms.add(new VaultLasersRoom());

		initRooms.add(new VaultLaserTreasureRoom());
		initRooms.add(new VaultFlamePathRoom());

		initRooms.add(new VaultBookcaseTreasureRoom());
		initRooms.add(new VaultSingleEnemyTreasureRoom());

		initRooms.add(new VaultMultipleEnemyTreasureRoom());
		initRooms.add(new VaultManyScansRoom());

		initRooms.add(new VaultLongRoom());
		initRooms.add(new VaultLongRoom());

		initRooms.add(new VaultFinalRoom());
		return initRooms;
	}

	@Override
	protected Builder builder() {
		return new GridBuilder();
	}

	@Override
	protected int nTraps() {
		return 0;
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		//walking onto transitions does nothing, need to use crystal
		return false;
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
	}

	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		//copypasta from super.createItems
		for (Item item : itemsToSpawn) {
			int cell = randomDropCell();
			drop( item, cell ).type = Heap.Type.HEAP;
			if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
				map[cell] = Terrain.GRASS;
				losBlocking[cell] = false;
			}
		}
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		return entrance()-width();
	}

	public static class VaultFlameTrap extends Trap {

		{
			color = BLACK;
			shape = DOTS;

			canBeHidden = false;
			active = false;
		}

		@Override
		public void activate() {
			//does nothing, this trap is just decoration and is always deactivated
		}

		public static void setupTrap(Level level, int cell, int initialCD, int afterTriggerCD, int triggers){
			VaultFlameTraps traps = Blob.seed(0, 0, VaultFlameTraps.class, level);
			traps.curCooldowns[cell] = initialCD;
			traps.afterTriggerCooldowns[cell] = afterTriggerCD;
			traps.triggersAfterCooldown[cell] = triggers;
			level.setTrap(new VaultLevel.VaultFlameTrap().reveal(), cell);
			Painter.set(level, cell, Terrain.INACTIVE_TRAP);
		}

	}

}
