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

import com.erebus.reclaimedpixeldungeon.levels.rooms.Room;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.MineGiantRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.MineLargeRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.MineSecretRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.quest.MineSmallRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.standard.StandardRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.standard.entrance.EntranceRoom;
import com.erebus.reclaimedpixeldungeon.levels.rooms.standard.exit.ExitRoom;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class EndlessMiningLevel extends MiningLevel {

	@Override
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> initRooms = new ArrayList<>();
		initRooms.add( roomEntrance = new EntranceRoom() );
		initRooms.add( roomExit = ExitRoom.createExit() );

		StandardRoom s = new MineGiantRoom();
		s.setSizeCat();
		initRooms.add( s );

		for (int i = 0; i < 3; i++) {
			s = new MineLargeRoom();
			s.setSizeCat();
			initRooms.add( s );
		}

		int rooms = Random.NormalIntRange( 6, 8 );
		for (int i = 0; i < rooms; i++) {
			s = new MineSmallRoom();
			s.setSizeCat();
			initRooms.add( s );
		}

		for (int i = 0; i < 2; i++) {
			initRooms.add( new MineSecretRoom() );
		}

		return initRooms;
	}
}
