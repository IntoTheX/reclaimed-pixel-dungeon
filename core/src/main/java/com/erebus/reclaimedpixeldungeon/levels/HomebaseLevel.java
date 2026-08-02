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

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Gnoll;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.HomebaseDefender;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.items.HomebaseClassFragmentBeacon;
import com.erebus.reclaimedpixeldungeon.items.HomebaseDefenderBeacon;
import com.erebus.reclaimedpixeldungeon.items.HomebaseRaidHorn;
import com.erebus.reclaimedpixeldungeon.items.TradingTestCrate;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.levels.features.LevelTransition;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.tiles.CustomTilemap;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.ui.BossHealthBar;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class HomebaseLevel extends Level {

	private static final int WIDTH = 33;
	private static final int HEIGHT = 42;
	private transient boolean raidProgressDeferred;

	{
		color1 = 0x4b4a35;
		color2 = 0x8f8b56;
		viewDistance = 8;
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_HOMEBASE;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HOMEBASE;
	}

	@Override
	protected boolean build() {
		setSize(WIDTH, HEIGHT);
		paintOutdoorGround();

		int homePos = WIDTH / 2 + (HEIGHT / 2) * width();
		int dungeonGate = WIDTH / 2 + 3 * width();

		paintPath( WIDTH/2, 3, WIDTH/2, HEIGHT - 7 );
		paintPath( 5, HEIGHT/2, WIDTH - 6, HEIGHT/2 );
		paintPath( 7, 9, 25, 9 );
		paintPath( 7, 31, 25, 31 );
		paintPath( 8, 9, 8, 31 );
		paintPath( 24, 9, 24, 31 );
		paintPond();
		paintDetails();
		addGroundTransitions();

		placeBuilding( Building.GATE, WIDTH/2 - 1, 1 );
		placeBuilding( Building.VAULT, 10, 14 );
		placeBuilding( Building.FORGE, 20, 14 );
		placeBuilding( Building.ALCHEMY, 10, 25 );
		placeBuilding( Building.GARDEN, 20, 25 );
		placeBuilding( Building.CAMP, WIDTH/2 - 1, HEIGHT/2 - 1 );
		placeDefenses();

		map[dungeonGate] = Terrain.EXIT;

		transitions.add(new LevelTransition(
				this,
				homePos,
				LevelTransition.Type.REGULAR_ENTRANCE,
				0,
				0,
				LevelTransition.Type.REGULAR_ENTRANCE));
		transitions.add(new LevelTransition(
				this,
				dungeonGate,
				LevelTransition.Type.REGULAR_EXIT,
				1,
				0,
				LevelTransition.Type.REGULAR_ENTRANCE));

		return true;
	}

	private void paintOutdoorGround() {
		Arrays.fill( map, Terrain.HOMEBASE_SHORT_GRASS );

		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				int cell = x + y * width();
				int roll = Random.Int( 100 );
				boolean edge = x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1;

				if (edge) {
					if (roll < 55) {
						map[cell] = Terrain.HOMEBASE_TALL_GRASS;
					} else if (roll < 70) {
						map[cell] = Terrain.HOMEBASE_MEDIUM_GRASS;
					} else {
						map[cell] = Terrain.HOMEBASE_SHORT_GRASS;
					}
				} else if (roll < 9) {
					map[cell] = Terrain.HOMEBASE_MEDIUM_GRASS;
				} else if (roll < 14) {
					map[cell] = Terrain.HOMEBASE_MEDIUM_GRASS;
				} else if (roll < 17) {
					map[cell] = Terrain.HOMEBASE_TALL_GRASS;
				}
			}
		}
	}

	private void paintPath( int x1, int y1, int x2, int y2 ) {
		if (x1 == x2) {
			int min = Math.min( y1, y2 );
			int max = Math.max( y1, y2 );
			for (int y = min; y <= max; y++) {
				paintPathBrush( x1, y );
			}
		} else if (y1 == y2) {
			int min = Math.min( x1, x2 );
			int max = Math.max( x1, x2 );
			for (int x = min; x <= max; x++) {
				paintPathBrush( x, y1 );
			}
		} else {
			int steps = Math.max( Math.abs( x2 - x1 ), Math.abs( y2 - y1 ) );
			if (steps == 0) {
				paintPathBrush( x1, y1 );
			} else {
				for (int i = 0; i <= steps; i++) {
					int x = x1 + (x2 - x1) * i / steps;
					int y = y1 + (y2 - y1) * i / steps;
					paintPathBrush( x, y );
				}
			}
		}
	}

	private void setPathTile( int x, int y ) {
		if (!inside( x, y )) return;
		map[x + y * width()] = Terrain.EMPTY;
	}

	private void paintPathBrush( int centerX, int centerY ) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				int dist = dx*dx + dy*dy;
				if (dist <= 1) {
					setPathTile( centerX + dx, centerY + dy );
				} else if (dist <= 4) {
					int chance = dist == 2 ? 80 : 45;
					if (Random.Int( 100 ) < chance) {
						setPathTile( centerX + dx, centerY + dy );
					}
				}
			}
		}
	}

	private boolean isPathTile( int tile ) {
		return tile == Terrain.EMPTY || tile == Terrain.HOMEBASE_DIRT_EDGE;
	}

	private void addGroundTransitions() {
		HomebaseGroundOverlay overlay = new HomebaseGroundOverlay();
		overlay.setRect( 0, 0, WIDTH, HEIGHT );
		customTiles.add( overlay );
	}

	private void paintPond() {
		if (Random.Int( 100 ) < 40) {
			paintStream();
		} else {
			int[][] ponds = {
					{13, 15},
					{20, 16},
					{13, 27},
					{20, 28}
			};
			int[] pond = ponds[Random.Int( ponds.length )];
			paintWaterBlob( pond[0], pond[1], Random.IntRange( 24, 40 ), 2 );
		}

		for (int i = 0; i < Random.IntRange( 1, 3 ); i++) {
			paintWaterBlob( Random.IntRange( 7, WIDTH - 8 ), Random.IntRange( 11, HEIGHT - 8 ), Random.IntRange( 7, 14 ), 1 );
		}

		fringeWater();
	}

	private void paintWaterBlob( int x, int y, int steps, int brushRadius ) {
		for (int i = 0; i < steps; i++) {
			paintWaterBrush( x, y, Random.Int( 100 ) < 30 ? Math.max( 0, brushRadius - 1 ) : brushRadius );
			x = Math.max( 3, Math.min( WIDTH - 4, x + Random.IntRange( -1, 1 ) ) );
			y = Math.max( 7, Math.min( HEIGHT - 4, y + Random.IntRange( -1, 1 ) ) );
		}
	}

	private void paintStream() {
		boolean horizontal = Random.Int( 2 ) == 0;
		int x = horizontal ? 2 : Random.IntRange( 9, WIDTH - 10 );
		int y = horizontal ? Random.IntRange( 12, HEIGHT - 10 ) : 7;
		int steps = horizontal ? WIDTH - 4 : HEIGHT - 11;

		for (int i = 0; i < steps; i++) {
			paintWaterBrush( x, y, Random.Int( 100 ) < 22 ? 1 : 0 );
			if (horizontal) {
				x++;
				if (Random.Int( 100 ) < 45) {
					y = Math.max( 8, Math.min( HEIGHT - 5, y + Random.IntRange( -1, 1 ) ) );
				}
			} else {
				y++;
				if (Random.Int( 100 ) < 45) {
					x = Math.max( 4, Math.min( WIDTH - 5, x + Random.IntRange( -1, 1 ) ) );
				}
			}
		}
	}

	private void paintWaterBrush( int centerX, int centerY, int radius ) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				if (dx*dx + dy*dy <= radius*radius + Random.Int( 2 )) {
					setWaterCell( centerX + dx, centerY + dy );
				}
			}
		}
		if (radius == 0) {
			setWaterCell( centerX, centerY );
		}
	}

	private void setWaterCell( int x, int y ) {
		if (!inside( x, y ) || reservedCell( x, y )) return;
		int cell = x + y * width();
		if (map[cell] == Terrain.CUSTOM_DECO || map[cell] == Terrain.CUSTOM_DECO_EMPTY || map[cell] == Terrain.EXIT) return;
		map[cell] = Terrain.WATER;
	}

	private void fringeWater() {
		int[] before = map.clone();
		for (int y = 1; y < HEIGHT - 1; y++) {
			for (int x = 1; x < WIDTH - 1; x++) {
				int cell = x + y * width();
				if (before[cell] == Terrain.WATER) continue;
				if (pathOrWater( x, y ) || reservedCell( x, y )) continue;
				if (touchesWater( before, x, y ) && before[cell] == Terrain.HOMEBASE_SHORT_GRASS && Random.Int( 100 ) < 55) {
					map[cell] = Random.Int( 100 ) < 70 ? Terrain.HOMEBASE_MEDIUM_GRASS : Terrain.HOMEBASE_TALL_GRASS;
				}
			}
		}
	}

	private boolean touchesWater( int[] terrain, int x, int y ) {
		return terrain[x + (y - 1) * width()] == Terrain.WATER
				|| terrain[x + 1 + y * width()] == Terrain.WATER
				|| terrain[x + (y + 1) * width()] == Terrain.WATER
				|| terrain[x - 1 + y * width()] == Terrain.WATER;
	}

	private void paintDetails() {
		for (int i = 0; i < 36; i++) {
			int x = Random.IntRange( 2, WIDTH - 3 );
			int y = Random.IntRange( 5, HEIGHT - 3 );
			if (reservedCell( x, y ) || pathOrWater( x, y )) continue;
			map[x + y * width()] = Random.Int( 100 ) < 70 ? Terrain.HOMEBASE_MEDIUM_GRASS : Terrain.HOMEBASE_TALL_GRASS;
		}

		for (int i = 0; i < 10; i++) {
			int x = Random.IntRange( 2, WIDTH - 3 );
			int y = Random.IntRange( 5, HEIGHT - 3 );
			if (reservedCell( x, y ) || pathOrWater( x, y )) continue;
			map[x + y * width()] = Random.Int( 100 ) < 60 ? Terrain.REGION_DECO : Terrain.BARRICADE;
		}

		paintFenceFragment( 2, 6, 9, true );
		paintFenceFragment( 22, 36, 9, true );
		paintFenceFragment( 2, 34, 5, false );
		paintFenceFragment( 30, 5, 5, false );

		setDetailTile( 25, 10, Terrain.EMBERS );
		setDetailTile( 29, 10, Terrain.EMBERS );
		setDetailTile( 26, 11, Terrain.EMBERS );
		setDetailTile( 27, 11, Terrain.EMBERS );
		setDetailTile( 28, 11, Terrain.EMBERS );

		setDetailTile( WIDTH/2 - 2, HEIGHT/2 + 2, Terrain.HOMEBASE_MEDIUM_GRASS );
		setDetailTile( WIDTH/2 + 2, HEIGHT/2 + 2, Terrain.HOMEBASE_MEDIUM_GRASS );
		setDetailTile( WIDTH/2 - 2, HEIGHT/2 - 2, Terrain.HOMEBASE_MEDIUM_GRASS );
		setDetailTile( WIDTH/2 + 2, HEIGHT/2 - 2, Terrain.HOMEBASE_MEDIUM_GRASS );
	}

	private void paintFenceFragment( int x, int y, int length, boolean horizontal ) {
		for (int i = 0; i < length; i++) {
			if (Random.Int( 100 ) < 25) continue;
			setDetailTile( x + (horizontal ? i : 0), y + (horizontal ? 0 : i), Terrain.BARRICADE );
		}
	}

	private void setDetailTile( int x, int y, int terrain ) {
		if (!inside( x, y ) || reservedCell( x, y ) || pathOrWater( x, y )) return;
		map[x + y * width()] = terrain;
	}

	private boolean pathOrWater( int x, int y ) {
		int tile = map[x + y * width()];
		return isPathTile( tile ) || tile == Terrain.HOMEBASE_GRASS_DIRT_EDGE || tile == Terrain.WATER || tile == Terrain.EXIT;
	}

	private boolean inside( int x, int y ) {
		return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT;
	}

	private boolean reservedCell( int x, int y ) {
		return inRect( x, y, WIDTH/2 - 1, 1, 3, 3 )
				|| inRect( x, y, 10, 14, 3, 3 )
				|| inRect( x, y, 20, 14, 3, 3 )
				|| inRect( x, y, 10, 25, 3, 3 )
				|| inRect( x, y, 20, 25, 3, 3 )
				|| inRect( x, y, WIDTH/2 - 1, HEIGHT/2 - 1, 3, 3 )
				|| defenseReservedCell( x, y );
	}

	private boolean defenseReservedCell( int x, int y ) {
		return inRect( x, y, 4, 8, 3, 3 )
				|| inRect( x, y, 26, 8, 3, 3 )
				|| inRect( x, y, 4, 31, 3, 3 )
				|| inRect( x, y, 26, 31, 3, 3 )
				|| inRect( x, y, 7, 8, 7, 3 )
				|| inRect( x, y, 15, 8, 3, 3 )
				|| inRect( x, y, 18, 8, 8, 3 )
				|| inRect( x, y, 7, 31, 7, 3 )
				|| inRect( x, y, 15, 31, 3, 3 )
				|| inRect( x, y, 18, 31, 8, 3 )
				|| inRect( x, y, 4, 11, 3, 9 )
				|| inRect( x, y, 4, 20, 3, 3 )
				|| inRect( x, y, 4, 23, 3, 8 )
				|| inRect( x, y, 26, 11, 3, 9 )
				|| inRect( x, y, 26, 20, 3, 3 )
				|| inRect( x, y, 26, 23, 3, 8 );
	}

	private boolean inRect( int x, int y, int left, int top, int width, int height ) {
		return x >= left && x < left + width && y >= top && y < top + height;
	}

	private void placeBuilding( Building building, int x, int y ) {
		prepareStructureGround( building, x, y );
		HomebaseBuildingVisual visual = new HomebaseBuildingVisual( building );
		visual.pos( x, y );
		customTiles.add( visual );
	}

	private void placeBuildingUnderlay( Building building, int x, int y ) {
		HomebaseBuildingUnderlayVisual visual = new HomebaseBuildingUnderlayVisual( building );
		visual.pos( x, y );
		customTiles.add( visual );
	}

	private void prepareStructureGround( Building building, int left, int top ) {
		if (building.alliedGate || building == Building.GATE) return;
		for (int x = left; x < left + building.width; x++) {
			for (int y = top; y < top + building.height; y++) {
				if (!inside( x, y )) continue;
				int cell = x + y * width();
				if (map[cell] == Terrain.EMPTY
						|| map[cell] == Terrain.HOMEBASE_DIRT_EDGE
						|| map[cell] == Terrain.HOMEBASE_GRASS_DIRT_EDGE) {
					map[cell] = naturalStructureGround( x, y );
				}
			}
		}
	}

	private int naturalStructureGround( int x, int y ) {
		int noise = Math.abs( x * 7349 + y * 9127 + 137 ) % 100;
		if (noise < 72) {
			return Terrain.HOMEBASE_SHORT_GRASS;
		} else if (noise < 92) {
			return Terrain.HOMEBASE_MEDIUM_GRASS;
		} else {
			return Terrain.HOMEBASE_TALL_GRASS;
		}
	}

	private void placeDefenses() {
		placeBuilding( Building.TOWER_NORTHWEST, 4, 8 );
		placeBuilding( Building.TOWER_NORTHEAST, 26, 8 );
		placeBuildingUnderlay( Building.WALL_WEST, 4, 31 );
		placeBuildingUnderlay( Building.WALL_EAST, 26, 31 );
		placeBuilding( Building.TOWER_SOUTHWEST, 4, 31 );
		placeBuilding( Building.TOWER_SOUTHEAST, 26, 31 );

		for (int x : new int[]{7, 10, 12, 18, 21, 23}) {
			placeBuilding( Building.WALL_NORTH, x, 8 );
		}
		placeBuilding( Building.GATE_NORTH, 15, 8 );
		for (int x : new int[]{7, 10, 12, 18, 21, 23}) {
			placeBuilding( Building.WALL_SOUTH, x, 31 );
		}
		placeBuilding( Building.GATE_SOUTH, 15, 31 );
		for (int y : new int[]{11, 14, 17, 23, 26, 28}) {
			placeBuilding( Building.WALL_WEST, 4, y );
			placeBuilding( Building.WALL_EAST, 26, y );
		}
		placeBuilding( Building.GATE_WEST, 4, 20 );
		placeBuilding( Building.GATE_EAST, 26, 20 );
	}

	public HomebaseBuildingVisual buildingAt( int cell ) {
		int x = cell % width();
		int y = cell / width();
		for (CustomTilemap tilemap : customTiles) {
			if (tilemap instanceof HomebaseBuildingVisual
					&& x >= tilemap.tileX && x < tilemap.tileX + tilemap.tileW
					&& y >= tilemap.tileY && y < tilemap.tileY + tilemap.tileH) {
				HomebaseBuildingVisual visual = (HomebaseBuildingVisual)tilemap;
				if (visual.image( x - visual.tileX, y - visual.tileY ) != null) {
					return visual;
				}
			}
		}
		return null;
	}

	public boolean isHomebaseStructureCell( int cell ) {
		return structureAt( cell ) != null || defenseStructureAt( cell ) != null;
	}

	@Override
	public Heap drop( Item item, int cell ) {
		if (cell >= 0 && cell < length() && isHomebaseStructureCell( cell )) {
			int destination = nearestSafeStructureFreeDropCell( cell );
			if (destination != -1) {
				cell = destination;
			}
		}
		return super.drop( item, cell );
	}

	public boolean canAllyUseGate( Char ch, int cell ) {
		if (ch == null || (ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY)) return false;
		HomebaseBuildingVisual visual = structureAt( cell );
		return (visual != null && visual.allowsGatePassageFor( ch, cell ))
				|| defenseGatePassageFor( ch, cell );
	}

	public boolean allowsStructureMovement( Char ch, int cell ) {
		HomebaseBuildingVisual visual = structureAt( cell );
		if (visual != null && visual.allowsMovementFor( ch, cell )) return true;
		HomebaseState.Building defense = defenseStructureAt( cell );
		return defense != null && defenseAllowsMovementFor( ch, defense, cell );
	}

	public boolean blocksStructureMovement( Char ch, int cell ) {
		HomebaseBuildingVisual visual = structureAt( cell );
		if (visual != null) {
			if (visual.blocksMovementFor( ch, cell )) return true;
			if (visual.allowsMovementFor( ch, cell )) return false;
		}
		HomebaseState.Building defense = defenseStructureAt( cell );
		return defense != null && defenseBlocksMovementFor( ch, defense, cell );
	}

	public void applyHomebaseStructurePassability( Char ch, boolean[] passable ) {
		if (passable == null) return;
		for (CustomTilemap tilemap : customTiles) {
			if (!(tilemap instanceof HomebaseBuildingVisual)) continue;
			HomebaseBuildingVisual visual = (HomebaseBuildingVisual)tilemap;
			for (int x = visual.tileX; x < visual.tileX + visual.tileW; x++) {
				for (int y = visual.tileY; y < visual.tileY + visual.tileH; y++) {
					int cell = x + y * width();
					if (!visual.coversCell( cell )) continue;
					if (visual.blocksMovementFor( ch, cell )) {
						passable[cell] = false;
					} else if (visual.allowsMovementFor( ch, cell )) {
						passable[cell] = true;
					}
				}
			}
		}
	}

	public boolean ensureHeroOutsideBlockedStructure() {
		if (Dungeon.hero == null || Dungeon.hero.pos < 0 || !blocksStructureMovement( Dungeon.hero, Dungeon.hero.pos )) {
			return false;
		}

		int destination = nearestUnstuckCell( Dungeon.hero, Dungeon.hero.pos, 8 );
		if (destination == -1) {
			return false;
		}

		Dungeon.hero.pos = destination;
		occupyCell( Dungeon.hero );
		if (Dungeon.hero.sprite != null) {
			Dungeon.hero.sprite.place( destination );
		}
		return true;
	}

	public int ensureDefendersOutsideBlockedStructure( HomebaseState.Building target ) {
		if (target == null || mobs == null || mobs.isEmpty()) {
			return 0;
		}

		int moved = 0;
		for (Mob mob : new ArrayList<>( mobs )) {
			if (!(mob instanceof HomebaseDefender) || mob.pos < 0 || mob.pos >= length()) continue;
			HomebaseBuildingVisual visual = structureAt( mob.pos );
			if (visual == null
					|| !HomebaseState.visualDependsOn( visual.rebuildTarget(), target )
					|| !visual.blocksMovementFor( mob, mob.pos )) {
				continue;
			}

			int destination = nearestUnstuckCell( mob, mob.pos, 12 );
			if (destination == -1) continue;

			mob.pos = destination;
			occupyCell( mob );
			if (mob.sprite != null) {
				mob.sprite.place( destination );
			}
			moved++;
		}
		return moved;
	}

	public int rescueBlockedDefenders() {
		if (mobs == null || mobs.isEmpty()) {
			return 0;
		}

		int moved = 0;
		for (Mob mob : new ArrayList<>( mobs )) {
			if (!(mob instanceof HomebaseDefender) || mob.pos < 0 || mob.pos >= length()) continue;
			if (!defenderNeedsRescue( mob )) continue;

			int destination = nearestUnstuckCell( mob, mob.pos, Math.max( width(), height() ) );
			if (destination == -1) continue;

			mob.pos = destination;
			((HomebaseDefender)mob).defendPos( destination );
			occupyCell( mob );
			if (mob.sprite != null) {
				mob.sprite.place( destination );
			}
			moved++;
		}
		return moved;
	}

	private boolean defenderNeedsRescue( Char ch ) {
		if (ch == null || ch.pos < 0 || ch.pos >= length()) return false;
		if (Actor.findChar( ch.pos ) != ch) return true;
		if (blocksStructureMovement( ch, ch.pos )) return true;
		if (solid[ch.pos] || pit[ch.pos]) return true;
		return !(passable[ch.pos] || avoid[ch.pos] || allowsStructureMovement( ch, ch.pos ) || canAllyUseGate( ch, ch.pos ));
	}

	public int relocateHeapsBlockedByStructure( HomebaseState.Building target ) {
		if (target == null || heaps == null || heaps.valueList().isEmpty()) {
			return 0;
		}

		int moved = 0;
		for (Heap heap : new ArrayList<>( heaps.valueList() )) {
			if (heap == null || heap.isEmpty() || !heapBlockedByStructure( heap, target )) continue;
			int destination = nearestSafeHeapCell( heap.pos );
			if (destination == -1 || destination == heap.pos) continue;

			int oldPos = heap.pos;
			heaps.remove( oldPos );
			heap.pos = destination;
			heap.seen = Dungeon.level == this && heroFOV[destination];
			heaps.put( destination, heap );
			if (heap.sprite != null) {
				heap.sprite.view( heap ).place( destination );
			}
			moved++;
		}
		return moved;
	}

	private boolean heapBlockedByStructure( Heap heap, HomebaseState.Building target ) {
		if (heap.pos < 0 || heap.pos >= length()) return false;
		HomebaseBuildingVisual visual = structureAt( heap.pos );
		return visual != null
				&& HomebaseState.visualDependsOn( visual.rebuildTarget(), target )
				&& visual.blocksMovementFor( Dungeon.hero, heap.pos );
	}

	private int nearestSafeHeapCell( int from ) {
		int bestCell = -1;
		int bestDistance = Integer.MAX_VALUE;
		int fromX = from % width();
		int fromY = from / width();
		int maxRadius = Math.max( width(), height() );

		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int x = fromX - radius; x <= fromX + radius; x++) {
				for (int y = fromY - radius; y <= fromY + radius; y++) {
					if (Math.abs( x - fromX ) != radius && Math.abs( y - fromY ) != radius) continue;
					if (!inside( x, y )) continue;
					int cell = x + y * width();
					if (!safeHeapCellAfterConstruction( cell )) continue;
					int distance = distance( from, cell );
					if (distance < bestDistance) {
						bestDistance = distance;
						bestCell = cell;
					}
				}
			}
			if (bestCell != -1) {
				return bestCell;
			}
		}

		return bestCell;
	}

	private boolean safeHeapCellAfterConstruction( int cell ) {
		if (!insideMap( cell )) return false;
		if (heaps.get( cell ) != null) return false;
		if (solid[cell] || pit[cell]) return false;
		if (blocksStructureMovement( Dungeon.hero, cell )) return false;
		return passable[cell] || avoid[cell] || allowsStructureMovement( Dungeon.hero, cell );
	}

	private int nearestSafeStructureFreeDropCell( int from ) {
		int bestCell = -1;
		int bestDistance = Integer.MAX_VALUE;
		int fromX = from % width();
		int fromY = from / width();
		int maxRadius = Math.max( width(), height() );

		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int x = fromX - radius; x <= fromX + radius; x++) {
				for (int y = fromY - radius; y <= fromY + radius; y++) {
					if (Math.abs( x - fromX ) != radius && Math.abs( y - fromY ) != radius) continue;
					if (!inside( x, y )) continue;
					int cell = x + y * width();
					if (!safeStructureFreeDropCell( cell )) continue;
					int distance = distance( from, cell );
					if (distance < bestDistance) {
						bestDistance = distance;
						bestCell = cell;
					}
				}
			}
			if (bestCell != -1) {
				return bestCell;
			}
		}

		return bestCell;
	}

	private boolean safeStructureFreeDropCell( int cell ) {
		if (!insideMap( cell )) return false;
		if (structureAt( cell ) != null || defenseStructureAt( cell ) != null) return false;
		if (solid[cell] || pit[cell]) return false;
		return passable[cell] || avoid[cell];
	}

	private int nearestUnstuckCell( Char ch, int from, int maxSearchRadius ) {
		int bestCell = -1;
		int bestDistance = Integer.MAX_VALUE;
		int fromX = from % width();
		int fromY = from / width();

		for (int radius = 1; radius <= maxSearchRadius; radius++) {
			for (int x = fromX - radius; x <= fromX + radius; x++) {
				for (int y = fromY - radius; y <= fromY + radius; y++) {
					if (Math.abs( x - fromX ) != radius && Math.abs( y - fromY ) != radius) continue;
					if (!inside( x, y )) continue;
					int cell = x + y * width();
					if (!charCanStandAfterConstruction( ch, cell )) continue;
					int distance = distance( from, cell );
					if (distance < bestDistance) {
						bestDistance = distance;
						bestCell = cell;
					}
				}
			}
			if (bestCell != -1) {
				return bestCell;
			}
		}

		return bestCell;
	}

	private boolean charCanStandAfterConstruction( Char ch, int cell ) {
		if (!insideMap( cell )) return false;
		if (Actor.findChar( cell ) != null && Actor.findChar( cell ) != ch) return false;
		if (blocksStructureMovement( ch, cell )) return false;
		if (solid[cell] || pit[cell]) return false;
		return passable[cell] || avoid[cell] || allowsStructureMovement( ch, cell ) || canAllyUseGate( ch, cell );
	}

	public boolean safeTeleportCell( Char ch, int cell ) {
		if (!insideMap( cell )) return false;
		if (Actor.findChar( cell ) != null && Actor.findChar( cell ) != ch) return false;
		if (blocksStructureMovement( ch, cell )) return false;
		if (solid[cell] || pit[cell]) return false;
		if (ch != null && Char.hasProp( ch, Char.Property.LARGE ) && !openSpace[cell]) return false;
		return passable[cell] || avoid[cell] || allowsStructureMovement( ch, cell ) || canAllyUseGate( ch, cell );
	}

	public boolean canStandOnHomebaseCell( Char ch, int cell, int currentCell ) {
		if (!insideMap( cell )) return false;
		if (blocksStructureMovement( ch, cell )) return false;
		if (solid[cell] || pit[cell]) return false;
		if (ch != null && Char.hasProp( ch, Char.Property.LARGE ) && !openSpace[cell]) return false;
		Char occupant = Actor.findChar( cell );
		if (occupant != null && cell != currentCell) return false;
		return passable[cell] || avoid[cell] || allowsStructureMovement( ch, cell ) || canAllyUseGate( ch, cell );
	}

	private HomebaseBuildingVisual structureAt( int cell ) {
		int x = cell % width();
		int y = cell / width();
		for (CustomTilemap tilemap : customTiles) {
			if (tilemap instanceof HomebaseBuildingVisual
					&& x >= tilemap.tileX && x < tilemap.tileX + tilemap.tileW
					&& y >= tilemap.tileY && y < tilemap.tileY + tilemap.tileH) {
				return (HomebaseBuildingVisual)tilemap;
			}
		}
		return null;
	}

	private HomebaseState.Building defenseStructureAt( int cell ) {
		if (!insideMap( cell )) return null;
		int x = cell % width();
		int y = cell / width();

		if (inRect( x, y, 4, 8, 3, 3 )) return HomebaseState.Building.NORTHWEST_TOWER;
		if (inRect( x, y, 26, 8, 3, 3 )) return HomebaseState.Building.NORTHEAST_TOWER;
		if (inRect( x, y, 4, 31, 3, 3 )) return HomebaseState.Building.SOUTHWEST_TOWER;
		if (inRect( x, y, 26, 31, 3, 3 )) return HomebaseState.Building.SOUTHEAST_TOWER;

		if (inRect( x, y, 7, 8, 7, 3 )
				|| inRect( x, y, 15, 8, 3, 3 )
				|| inRect( x, y, 18, 8, 8, 3 )) {
			return HomebaseState.Building.NORTH_WALL;
		}
		if (inRect( x, y, 7, 31, 7, 3 )
				|| inRect( x, y, 15, 31, 3, 3 )
				|| inRect( x, y, 18, 31, 8, 3 )) {
			return HomebaseState.Building.SOUTH_WALL;
		}
		if (inRect( x, y, 4, 11, 3, 9 )
				|| inRect( x, y, 4, 20, 3, 3 )
				|| inRect( x, y, 4, 23, 3, 8 )) {
			return HomebaseState.Building.WEST_WALL;
		}
		if (inRect( x, y, 26, 11, 3, 9 )
				|| inRect( x, y, 26, 20, 3, 3 )
				|| inRect( x, y, 26, 23, 3, 8 )) {
			return HomebaseState.Building.EAST_WALL;
		}
		return null;
	}

	private boolean defenseGatePassageFor( Char ch, int cell ) {
		if (ch == null || (ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY)) return false;
		HomebaseState.Building defense = defenseStructureAt( cell );
		return defense != null
				&& defenseBuiltAndIntact( defense )
				&& defenseGatePassageCell( cell );
	}

	private boolean defenseAllowsMovementFor( Char ch, HomebaseState.Building defense, int cell ) {
		if (defenseBuiltAndIntact( defense )) {
			return defenseGatePassageCell( cell )
					&& ch != null
					&& (ch == Dungeon.hero || ch.alignment == Char.Alignment.ALLY);
		}
		return true;
	}

	private boolean defenseBlocksMovementFor( Char ch, HomebaseState.Building defense, int cell ) {
		if (!defenseBuiltAndIntact( defense )) return false;
		return !defenseGatePassageCell( cell )
				|| ch == null
				|| (ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY);
	}

	private boolean defenseBuiltAndIntact( HomebaseState.Building defense ) {
		return Dungeon.homebase != null
				&& Dungeon.homebase.isBuilt( defense )
				&& !Dungeon.homebase.buildingDestroyed( defense );
	}

	private boolean defenseGatePassageCell( int cell ) {
		if (!insideMap( cell )) return false;
		int x = cell % width();
		int y = cell / width();
		return (inRect( x, y, 15, 8, 3, 3 ) && x == 16)
				|| (inRect( x, y, 15, 31, 3, 3 ) && x == 16)
				|| (inRect( x, y, 4, 20, 3, 3 ) && y == 21)
				|| (inRect( x, y, 26, 20, 3, 3 ) && y == 21);
	}

	public int nearestRaidBuildingAttackCell( int from, Char ch ) {
		int bestCell = -1;
		int bestDistance = Integer.MAX_VALUE;
		boolean[] pathable = null;
		if (ch != null && insideMap( from )) {
			int oldPos = ch.pos;
			ch.pos = from;
			pathable = Dungeon.findPassable( ch, passable, null, false );
			pathable[from] = true;
			PathFinder.buildDistanceMap( from, pathable );
			ch.pos = oldPos;
		}
		for (CustomTilemap tilemap : customTiles) {
			if (!(tilemap instanceof HomebaseBuildingVisual)) continue;
			HomebaseBuildingVisual visual = (HomebaseBuildingVisual)tilemap;
			HomebaseState.Building building = visual.rebuildTarget();
			if (building == null || Dungeon.homebase == null || !Dungeon.homebase.canRaidDamageBuilding( building )) continue;

			for (int x = visual.tileX; x < visual.tileX + visual.tileW; x++) {
				for (int y = visual.tileY; y < visual.tileY + visual.tileH; y++) {
					int buildingCell = x + y * width();
					if (!visual.coversCell( buildingCell )) continue;
					for (int offset : PathFinder.NEIGHBOURS8) {
						int attackCell = buildingCell + offset;
						if (!canStandOnHomebaseCell( ch, attackCell, from )) continue;

						int distance = pathable == null ? distance( from, attackCell ) : PathFinder.distance[attackCell];
						if (distance == Integer.MAX_VALUE) continue;
						if (distance < bestDistance) {
							bestDistance = distance;
							bestCell = attackCell;
						}
					}
				}
			}
		}
		return bestCell;
	}

	public HomebaseState.Building raidBuildingAdjacentTo( int cell ) {
		for (int offset : PathFinder.NEIGHBOURS8) {
			HomebaseBuildingVisual visual = structureAt( cell + offset );
			if (visual == null) continue;
			HomebaseState.Building building = visual.rebuildTarget();
			if (building != null && Dungeon.homebase != null && Dungeon.homebase.canRaidDamageBuilding( building )) {
				return building;
			}
		}
		return null;
	}

	public int raidBuildingImpactCell( int cell, HomebaseState.Building target ) {
		for (int offset : PathFinder.NEIGHBOURS8) {
			HomebaseBuildingVisual visual = structureAt( cell + offset );
			if (visual != null && visual.rebuildTarget() == target) {
				return cell + offset;
			}
		}
		return cell;
	}

	public void showBuildingDamage( int cell, int damage ) {
		if (damage <= 0) return;
		PointF p = DungeonTilemap.raisedTileCenterToWorld( cell );
		FloatingText.show( p.x, p.y, cell, Integer.toString( damage ), CharSprite.NEGATIVE, FloatingText.PHYS_DMG_NO_BLOCK, false );
	}

	public void refreshBuildingVisual( HomebaseState.Building target ) {
		boolean refreshed = false;
		for (CustomTilemap tilemap : customTiles) {
			if (tilemap instanceof HomebaseBuildingVisual
					&& HomebaseState.visualDependsOn( ((HomebaseBuildingVisual)tilemap).rebuildTarget(), target )) {
				GameScene.add( tilemap, false );
				refreshed = true;
			} else if (tilemap instanceof HomebaseBuildingUnderlayVisual
					&& HomebaseState.visualDependsOn( ((HomebaseBuildingUnderlayVisual)tilemap).rebuildTarget(), target )) {
				GameScene.add( tilemap, false );
				refreshed = true;
			}
		}
		if (refreshed) {
			buildFlagMaps();
		}
	}

	@Override
	public Mob createMob() {
		if (Dungeon.homebase != null && Dungeon.homebase.raidActive()) {
			Mob mob = createRaidMob();
			if (mob != null) {
				if (mob.state != mob.PASSIVE) {
					mob.state = mob.HUNTING;
				}
			}
			return mob;
		}
		return null;
	}

	private Mob createRaidMob() {
		Class cls = Reflection.forName( Dungeon.homebase.raidMobClassNameForSpawn() );
		if (cls != null && Mob.class.isAssignableFrom( cls )) {
			Mob mob = (Mob)Reflection.newInstance( cls );
			if (mob != null) {
				return mob;
			}
		}
		return new Gnoll();
	}

	public int spawnRaidWave() {
		if (Dungeon.homebase == null || !Dungeon.homebase.raidActive()) return 0;

		spawnHomebaseDefenders();
		boolean hasBuildingTargets = Dungeon.homebase.raidHasDamageTargets();

		int spawned = 0;
		while (Dungeon.homebase.canSpawnRaidMob()) {
			Mob mob = createMob();
			if (mob == null) break;

			mob.pos = randomRaidEdgeCell( mob );
			if (mob.pos == -1) break;
			int target = hasBuildingTargets ? nearestRaidBuildingAttackCell( mob.pos, mob ) : Dungeon.hero == null ? -1 : Dungeon.hero.pos;
			if (target != -1) {
				mob.beckon( target );
			}

			mob.countInHomebaseRaid();
			GameScene.add( mob, spawned * 0.25f );
			Dungeon.homebase.recordRaidMobSpawned();
			spawned++;
		}

		if (spawned > 0) {
			GLog.w( Dungeon.homebase.raidProgressText() );
		}
		BossHealthBar.refreshRaid();
		return spawned;
	}

	public int reconcileRaidProgress() {
		return reconcileRaidProgress( false );
	}

	public void deferRaidProgress() {
		if (raidProgressDeferred) return;
		raidProgressDeferred = true;

		Actor.addDelayed(new Actor() {
			{
				actPriority = MOB_PRIO + 1;
			}

			@Override
			protected boolean act() {
				Actor.remove( this );
				raidProgressDeferred = false;
				if (Dungeon.level == HomebaseLevel.this) {
					reconcileRaidProgress( true );
				}
				return true;
			}
		}, 0.01f);
	}

	private int reconcileRaidProgress( boolean allowSpawn ) {
		if (Dungeon.homebase == null || !Dungeon.homebase.raidActive()) return HomebaseState.RAID_PROGRESS_ACTIVE;

		int liveRaiders = 0;
		for (Mob mob : mobs.toArray( new Mob[0] )) {
			if (mob != null
					&& mob.countsInHomebaseRaid()
					&& mob.alignment == Char.Alignment.ENEMY
					&& mob.isAlive()) {
				liveRaiders++;
			}
		}

		int raidProgress = Dungeon.homebase.reconcileRaidProgress( liveRaiders );
		BossHealthBar.refreshRaid();
		if (raidProgress == HomebaseState.RAID_PROGRESS_NEXT_WAVE) {
			if (allowSpawn) {
				spawnRaidWave();
			} else {
				deferRaidProgress();
			}
		} else if (raidProgress == HomebaseState.RAID_PROGRESS_COMPLETE) {
			GLog.p( Dungeon.homebase.raidVictoryText() );
		} else if (liveRaiders == 0 && Dungeon.homebase.canSpawnRaidMob()) {
			if (allowSpawn) {
				spawnRaidWave();
			} else {
				deferRaidProgress();
			}
		}
		return raidProgress;
	}

	public void spawnHomebaseDefenders() {
		if (Dungeon.homebase == null) return;
		for (HomebaseState.DefenderRecord defender : Dungeon.homebase.defenders()) {
			spawnDefender( defender );
		}
		rescueBlockedDefenders();
	}

	public boolean spawnDefender( HomebaseState.DefenderRecord defender ) {
		if (defender == null || !defender.alive() || defenderPresent( defender.id() )) return false;

		HomebaseDefender mob = new HomebaseDefender( defender );
		int cell = defenderSpawnCell( defender.id(), mob );
		if (cell == -1) return false;

		mob.pos = cell;
		mob.defendPos( cell );
		GameScene.add( mob );
		return true;
	}

	public static final int RAID_SIDE_NORTH = 0;
	public static final int RAID_SIDE_EAST = 1;
	public static final int RAID_SIDE_SOUTH = 2;
	public static final int RAID_SIDE_WEST = 3;

	public int defenderRaidSide( int defenderId ) {
		if (Dungeon.homebase == null) return Math.abs( defenderId ) % 4;
		int rank = 0;
		for (HomebaseState.DefenderRecord defender : Dungeon.homebase.defenders()) {
			if (defender == null || !defender.alive()) continue;
			if (defender.id() == defenderId) {
				int[] order = raidSidePriority();
				return order[Math.max( 0, rank ) % order.length];
			}
			rank++;
		}
		return Math.abs( defenderId ) % 4;
	}

	public int defenderRaidStation( int defenderId, Char ch ) {
		int side = defenderRaidSide( defenderId );
		int rankOnSide = defenderRankOnSide( defenderId, side );
		int[][][] stations = new int[][][]{
				{{16, 11}, {13, 11}, {19, 11}, {10, 11}, {22, 11}, {7, 11}, {25, 11}},
				{{24, 20}, {24, 17}, {24, 23}, {24, 14}, {24, 26}, {24, 11}, {24, 29}},
				{{16, 29}, {13, 29}, {19, 29}, {10, 29}, {22, 29}, {7, 29}, {25, 29}},
				{{8, 20}, {8, 17}, {8, 23}, {8, 14}, {8, 26}, {8, 11}, {8, 29}}
		};
		int[][] sideStations = stations[Math.max( 0, Math.min( side, stations.length - 1 ) )];
		for (int i = 0; i < sideStations.length; i++) {
			int[] station = sideStations[(rankOnSide + i) % sideStations.length];
			int cell = nearestOpenDefenderCell( station[0] + station[1] * width(), ch );
			if (cell != -1) return cell;
		}
		return defenderSpawnCell( defenderId, ch );
	}

	private int defenderRankOnSide( int defenderId, int side ) {
		if (Dungeon.homebase == null) return 0;
		int[] order = raidSidePriority();
		int rank = 0;
		int onSide = 0;
		for (HomebaseState.DefenderRecord defender : Dungeon.homebase.defenders()) {
			if (defender == null || !defender.alive()) continue;
			int assigned = order[rank % order.length];
			if (defender.id() == defenderId) {
				return assigned == side ? onSide : 0;
			}
			if (assigned == side) onSide++;
			rank++;
		}
		return 0;
	}

	private int[] raidSidePriority() {
		int[] sides = {RAID_SIDE_NORTH, RAID_SIDE_EAST, RAID_SIDE_SOUTH, RAID_SIDE_WEST};
		int[] pressure = new int[4];
		for (Mob mob : mobs) {
			if (mob == null
					|| !mob.isAlive()
					|| mob.alignment != Char.Alignment.ENEMY
					|| !mob.countsInHomebaseRaid()) {
				continue;
			}
			pressure[raidApproachSide( mob.pos )]++;
		}
		for (int i = 0; i < sides.length - 1; i++) {
			for (int j = i + 1; j < sides.length; j++) {
				if (pressure[sides[j]] > pressure[sides[i]]) {
					int swap = sides[i];
					sides[i] = sides[j];
					sides[j] = swap;
				}
			}
		}
		return sides;
	}

	public int raidApproachSide( int cell ) {
		int x = cell % width();
		int y = cell / width();
		int centerX = WIDTH / 2;
		int centerY = HEIGHT / 2;
		int dx = x - centerX;
		int dy = y - centerY;
		if (Math.abs( dy ) >= Math.abs( dx )) {
			return dy < 0 ? RAID_SIDE_NORTH : RAID_SIDE_SOUTH;
		}
		return dx < 0 ? RAID_SIDE_WEST : RAID_SIDE_EAST;
	}

	private boolean defenderPresent( int id ) {
		for (Mob mob : mobs) {
			if (mob instanceof HomebaseDefender
					&& ((HomebaseDefender)mob).defenderId() == id
					&& mob.isAlive()) {
				return true;
			}
		}
		return false;
	}

	private int defenderSpawnCell( int defenderId, Char ch ) {
		int[][] stations = {
				{16, 18},
				{13, 20},
				{19, 20},
				{8, 20},
				{24, 20},
				{16, 28},
				{8, 12},
				{24, 12}
		};
		int start = Math.abs( defenderId ) % stations.length;
		for (int i = 0; i < stations.length; i++) {
			int[] station = stations[(start + i) % stations.length];
			int cell = nearestOpenDefenderCell( station[0] + station[1] * width(), ch );
			if (cell != -1) return cell;
		}
		return nearestOpenDefenderCell( entrance(), ch );
	}

	private int nearestOpenDefenderCell( int center, Char ch ) {
		int centerX = center % width();
		int centerY = center / width();
		for (int radius = 0; radius <= 3; radius++) {
			for (int x = centerX - radius; x <= centerX + radius; x++) {
				for (int y = centerY - radius; y <= centerY + radius; y++) {
					if (!inside( x, y )) continue;
					int cell = x + y * width();
					if (passable[cell]
							&& !solid[cell]
							&& Actor.findChar( cell ) == null
							&& !blocksStructureMovement( ch, cell )
							&& (!Char.hasProp( ch, Char.Property.LARGE ) || openSpace[cell])) {
						return cell;
					}
				}
			}
		}
		return -1;
	}

	private int randomRaidEdgeCell( Char ch ) {
		boolean needsBuildingTarget = Dungeon.homebase == null
				|| !Dungeon.homebase.raidActive()
				|| Dungeon.homebase.raidHasDamageTargets();
		for (int tries = 0; tries < 100; tries++) {
			int side = Random.Int( 4 );
			int x;
			int y;
			switch (side) {
				case 0:
					x = Random.IntRange( 1, WIDTH - 2 );
					y = 1;
					break;
				case 1:
					x = Random.IntRange( 1, WIDTH - 2 );
					y = HEIGHT - 2;
					break;
				case 2:
					x = 1;
					y = Random.IntRange( 4, HEIGHT - 2 );
					break;
				case 3:
				default:
					x = WIDTH - 2;
					y = Random.IntRange( 4, HEIGHT - 2 );
					break;
			}

			int cell = x + y * width();
			if (canStandOnHomebaseCell( ch, cell, -1 )
					&& (Dungeon.hero == null || cell != Dungeon.hero.pos)
					&& (!needsBuildingTarget || nearestRaidBuildingAttackCell( cell, ch ) != -1)) {
				return cell;
			}
		}
		return -1;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
		giveHomebaseTestItems();
	}

	private void giveHomebaseTestItems() {
		if (HomebaseState.homebaseNpcTestItemsEnabled()) {
			giveHomebaseTestItem(
					new HomebaseDefenderBeacon(),
					HomebaseDefenderBeacon.class
			);

			giveHomebaseTestItem(
					new HomebaseRaidHorn(),
					HomebaseRaidHorn.class
			);
		}

		if (HomebaseState.infiniteTestResourcesEnabled()) {
			giveHomebaseTestItem(
					new HomebaseClassFragmentBeacon(),
					HomebaseClassFragmentBeacon.class
			);
		}

		if (HomebaseState.tradingTestItemsEnabled()) {
			giveHomebaseTestItem(
					new TradingTestCrate(),
					TradingTestCrate.class
			);
		}
	}

	private void giveHomebaseTestItem( Item item, Class<? extends Item> itemClass ) {
		if (Dungeon.hero == null
				|| Dungeon.hero.belongings == null
				|| Dungeon.hero.belongings.getItem( itemClass ) != null) {
			return;
		}
		if (!item.collect()) {
			drop( item, entrance() );
		}
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		if (ch == Dungeon.hero) {
			giveHomebaseTestItems();
		}
		if (Dungeon.homebase != null && Dungeon.homebase.raidActive()) {
			return randomRaidEdgeCell( ch );
		}
		return entrance();
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.HOMEBASE_SHORT_GRASS:
				return Messages.get( this, "short_grass_name" );
			case Terrain.EMPTY:
			case Terrain.HOMEBASE_DIRT_EDGE:
				return Messages.get( this, "path_name" );
			case Terrain.HOMEBASE_GRASS_DIRT_EDGE:
			case Terrain.HOMEBASE_MEDIUM_GRASS:
				return Messages.get( this, "medium_grass_name" );
			case Terrain.HOMEBASE_TALL_GRASS:
				return Messages.get( this, "tall_grass_name" );
			case Terrain.WATER:
				return Messages.get( this, "water_name" );
			case Terrain.BARRICADE:
				return Messages.get( this, "fence_name" );
			case Terrain.REGION_DECO:
				return Messages.get( this, "rubble_name" );
			case Terrain.EMBERS:
				return Messages.get( this, "embers_name" );
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
			case Terrain.HOMEBASE_SHORT_GRASS:
				return Messages.get( this, "short_grass_desc" );
			case Terrain.EMPTY:
			case Terrain.HOMEBASE_DIRT_EDGE:
				return Messages.get( this, "path_desc" );
			case Terrain.HOMEBASE_GRASS_DIRT_EDGE:
			case Terrain.HOMEBASE_MEDIUM_GRASS:
				return Messages.get( this, "medium_grass_desc" );
			case Terrain.HOMEBASE_TALL_GRASS:
				return Messages.get( this, "tall_grass_desc" );
			case Terrain.WATER:
				return Messages.get( this, "water_desc" );
			case Terrain.BARRICADE:
				return Messages.get( this, "fence_desc" );
			case Terrain.REGION_DECO:
				return Messages.get( this, "rubble_desc" );
			case Terrain.EMBERS:
				return Messages.get( this, "embers_desc" );
			default:
				return super.tileDesc( tile );
		}
	}

	public enum Building {
		GATE( "gate", 0, 0, null ),
		VAULT( "vault", 3, 0, HomebaseState.Building.VAULT ),
		FORGE( "forge", 6, 0, HomebaseState.Building.FORGE ),
		ALCHEMY( "alchemy", 9, 0, HomebaseState.Building.ALCHEMY ),
		GARDEN( "garden", 12, 0, HomebaseState.Building.GARDEN ),
		CAMP( "camp", 15, 0, HomebaseState.Building.CAMP ),
		WALL_NORTH( "north_wall", 18, 0, HomebaseState.Building.NORTH_WALL, false ),
		WALL_EAST( "east_wall", 21, 0, HomebaseState.Building.EAST_WALL, false ),
		WALL_SOUTH( "south_wall", 18, 0, HomebaseState.Building.SOUTH_WALL, false ),
		WALL_WEST( "west_wall", 21, 0, HomebaseState.Building.WEST_WALL, false ),
		GATE_NORTH( "north_gate", 27, 0, HomebaseState.Building.NORTH_WALL, false, true ),
		GATE_EAST( "east_gate", 30, 0, HomebaseState.Building.EAST_WALL, false, true ),
		GATE_SOUTH( "south_gate", 27, 0, HomebaseState.Building.SOUTH_WALL, false, true ),
		GATE_WEST( "west_gate", 30, 0, HomebaseState.Building.WEST_WALL, false, true ),
		TOWER_NORTHWEST( "northwest_tower", 24, 0, HomebaseState.Building.NORTHWEST_TOWER, false ),
		TOWER_NORTHEAST( "northeast_tower", 24, 0, HomebaseState.Building.NORTHEAST_TOWER, false ),
		TOWER_SOUTHWEST( "southwest_tower", 24, 0, HomebaseState.Building.SOUTHWEST_TOWER, false ),
		TOWER_SOUTHEAST( "southeast_tower", 24, 0, HomebaseState.Building.SOUTHEAST_TOWER, false );

		private final String key;
		private final int textureX;
		private final int textureY;
		private final int width = 3;
		private final int height = 3;
		private final int doorX = 1;
		private final int doorY = 2;
		private final HomebaseState.Building stateBuilding;
		private final boolean hasDoor;
		private final boolean alliedGate;

		Building( String key, int textureX, int textureY, HomebaseState.Building stateBuilding ) {
			this( key, textureX, textureY, stateBuilding, true );
		}

		Building( String key, int textureX, int textureY, HomebaseState.Building stateBuilding, boolean hasDoor ) {
			this( key, textureX, textureY, stateBuilding, hasDoor, false );
		}

		Building( String key, int textureX, int textureY, HomebaseState.Building stateBuilding, boolean hasDoor, boolean alliedGate ) {
			this.key = key;
			this.textureX = textureX;
			this.textureY = textureY;
			this.stateBuilding = stateBuilding;
			this.hasDoor = hasDoor;
			this.alliedGate = alliedGate;
		}

		private boolean built() {
			return stateBuilding == null || Dungeon.homebase == null || Dungeon.homebase.isBuilt( stateBuilding );
		}

		private int doorCell( int x, int y, int levelWidth ) {
			return x + doorX + (y + doorY) * levelWidth;
		}

		private boolean blocksMovement() {
			if (hasDoor) return true;
			return stateBuilding == null
					|| Dungeon.homebase == null
					|| (Dungeon.homebase.isBuilt( stateBuilding ) && !Dungeon.homebase.buildingDestroyed( stateBuilding ));
		}

		private boolean activeAlliedGate() {
			return alliedGate
					&& stateBuilding != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.isBuilt( stateBuilding )
					&& !Dungeon.homebase.buildingDestroyed( stateBuilding );
		}

		private boolean sideGate() {
			return alliedGate && ("east_gate".equals( key ) || "west_gate".equals( key ));
		}

		private boolean defenseStructure() {
			return stateBuilding != null && !hasDoor;
		}
	}

	public static class HomebaseBuildingVisual extends CustomTilemap {

		private static final int TEX_WIDTH = 528;
		private static final int RUINED_TEX_OFFSET = 0;
		private static final int[] LEVEL_TEX_OFFSETS = {3, 6, 9, 12};
		private static final String BUILDING = "building";

		private Building building = Building.CAMP;

		{
			texture = Assets.Environment.HOMEBASE_BUILDINGS;
		}

		public HomebaseBuildingVisual() {
		}

		public HomebaseBuildingVisual( Building building ) {
			this.building = building;
			tileW = building.width;
			tileH = building.height;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int textureY = building.textureY + textureOffset();
			v.map( mapSimpleImage( building.textureX, textureY, TEX_WIDTH ), tileW );
			return v;
		}

		private int textureOffset() {
			if (building.stateBuilding == null || Dungeon.homebase == null) return 0;
			if (Dungeon.homebase.buildingDestroyed( building.stateBuilding )) return RUINED_TEX_OFFSET;
			int level = Dungeon.homebase.buildingLevel( building.stateBuilding );
			if (level <= 0) return RUINED_TEX_OFFSET;
			return LEVEL_TEX_OFFSETS[Math.min( level, LEVEL_TEX_OFFSETS.length ) - 1];
		}

		@Override
		public String name( int tileX, int tileY ) {
			String state = building.built() && !destroyed() ? "" : "ruined_";
			String name = Messages.get( this, state + building.key + "_name" );
			if (building.built() && !destroyed() && building.stateBuilding != null && Dungeon.homebase != null) {
				name = Messages.get( this, "level_name", Dungeon.homebase.buildingLevel( building.stateBuilding ), name );
			}
			return name;
		}

		@Override
		public String desc( int tileX, int tileY ) {
			String state = building.built() && !destroyed() ? "" : "ruined_";
			return Messages.get( this, state + building.key + "_desc" );
		}

		private boolean destroyed() {
			return building.stateBuilding != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.buildingDestroyed( building.stateBuilding );
		}

		public HomebaseState.Building rebuildTarget() {
			return building.stateBuilding;
		}

		public boolean needsRebuild() {
			return rebuildTarget() != null && Dungeon.homebase != null && !Dungeon.homebase.isBuilt( rebuildTarget() );
		}

		public boolean isDoorTile( int x, int y ) {
			return building.hasDoor && x == building.doorX && y == building.doorY;
		}

		public boolean isDoorCell( int cell ) {
			int x = cell % Dungeon.level.width();
			int y = cell / Dungeon.level.width();
			return isDoorTile( x - tileX, y - tileY );
		}

		public boolean isActiveAlliedGate() {
			return building.activeAlliedGate();
		}

		public boolean coversCell( int cell ) {
			int x = cell % Dungeon.level.width();
			int y = cell / Dungeon.level.width();
			int localX = x - tileX;
			int localY = y - tileY;
			return localX >= 0
					&& localY >= 0
					&& localX < tileW
					&& localY < tileH;
		}

		public boolean allowsGatePassageFor( Char ch, int cell ) {
			if (!building.activeAlliedGate()
					|| ch == null
					|| (ch != Dungeon.hero && ch.alignment != Char.Alignment.ALLY)
					|| !coversCell( cell )) {
				return false;
			}
			int localX = cell % Dungeon.level.width() - tileX;
			int localY = cell / Dungeon.level.width() - tileY;
			return building.sideGate() ? localY == 1 : localX == 1;
		}

		public boolean blocksMovementFor( Char ch, int cell ) {
			if (!coversCell( cell )) return false;
			if (building.alliedGate) {
				return building.activeAlliedGate() && !allowsGatePassageFor( ch, cell );
			}
			if (building.stateBuilding != null
					&& Dungeon.homebase != null
					&& (!Dungeon.homebase.isBuilt( building.stateBuilding )
					|| Dungeon.homebase.buildingDestroyed( building.stateBuilding ))) {
				return false;
			}
			if (building.hasDoor && isDoorCell( cell )) return false;
			if (building.defenseStructure()) return true;
			return building.blocksMovement();
		}

		public boolean allowsMovementFor( Char ch, int cell ) {
			if (!coversCell( cell ) || blocksMovementFor( ch, cell )) return false;
			if (building.alliedGate) {
				return building.activeAlliedGate() ? allowsGatePassageFor( ch, cell ) : true;
			}
			if (building.hasDoor) {
				if (building.stateBuilding != null
						&& Dungeon.homebase != null
						&& (!Dungeon.homebase.isBuilt( building.stateBuilding )
						|| Dungeon.homebase.buildingDestroyed( building.stateBuilding ))) {
					return true;
				}
				return isDoorCell( cell );
			}
			return building.defenseStructure()
					&& building.stateBuilding != null
					&& Dungeon.homebase != null
					&& (!Dungeon.homebase.isBuilt( building.stateBuilding )
					|| Dungeon.homebase.buildingDestroyed( building.stateBuilding ));
		}

		public boolean canInteractFromCell( int cell ) {
			return building.hasDoor ? isDoorCell( cell ) : occupiesCell( cell );
		}

		public boolean occupiesCell( int cell ) {
			int x = cell % Dungeon.level.width();
			int y = cell / Dungeon.level.width();
			int localX = x - tileX;
			int localY = y - tileY;
			return localX >= 0
					&& localY >= 0
					&& localX < tileW
					&& localY < tileH
					&& image( localX, localY ) != null;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			if (bundle.contains( BUILDING )) {
				building = Building.valueOf( bundle.getString( BUILDING ) );
				tileW = building.width;
				tileH = building.height;
			}
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( BUILDING, building.name() );
		}
	}

	public static class HomebaseBuildingUnderlayVisual extends CustomTilemap {

		private static final int TEX_WIDTH = 528;
		private static final int RUINED_TEX_OFFSET = 0;
		private static final int[] LEVEL_TEX_OFFSETS = {3, 6, 9, 12};
		private static final String BUILDING = "building";

		private Building building = Building.WALL_WEST;

		{
			texture = Assets.Environment.HOMEBASE_BUILDINGS;
		}

		public HomebaseBuildingUnderlayVisual() {
		}

		public HomebaseBuildingUnderlayVisual( Building building ) {
			this.building = building;
			tileW = building.width;
			tileH = building.height;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int textureY = building.textureY + textureOffset();
			v.map( mapSimpleImage( building.textureX, textureY, TEX_WIDTH ), tileW );
			return v;
		}

		private int textureOffset() {
			if (building.stateBuilding == null || Dungeon.homebase == null) return RUINED_TEX_OFFSET;
			if (Dungeon.homebase.buildingDestroyed( building.stateBuilding )) return RUINED_TEX_OFFSET;
			int level = Dungeon.homebase.buildingLevel( building.stateBuilding );
			if (level <= 0) return RUINED_TEX_OFFSET;
			return LEVEL_TEX_OFFSETS[Math.min( level, LEVEL_TEX_OFFSETS.length ) - 1];
		}

		public HomebaseState.Building rebuildTarget() {
			return building.stateBuilding;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			if (bundle.contains( BUILDING )) {
				building = Building.valueOf( bundle.getString( BUILDING ) );
				tileW = building.width;
				tileH = building.height;
			}
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( BUILDING, building.name() );
		}
	}

	public static class HomebaseGroundOverlay extends CustomTilemap {

		{
			texture = Assets.Environment.HOMEBASE_TRANSITIONS;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int[] data = new int[tileW * tileH];
			int[] terrain = Dungeon.level.map;
			int levelWidth = Dungeon.level.width();
			int levelHeight = Dungeon.level.height();

			for (int y = 0; y < tileH; y++) {
				for (int x = 0; x < tileW; x++) {
					int dataCell = x + y * tileW;
					int levelX = tileX + x;
					int levelY = tileY + y;
					int levelCell = levelX + levelY * levelWidth;

					if (!isDirtPath( terrain[levelCell] )) {
						data[dataCell] = -1;
						continue;
					}

					int mask = 0;
					if (levelY > 0 && isOverlayNeighbor( terrain[levelCell - levelWidth] )) mask += 1;
					if (levelX < levelWidth - 1 && isOverlayNeighbor( terrain[levelCell + 1] )) mask += 2;
					if (levelY < levelHeight - 1 && isOverlayNeighbor( terrain[levelCell + levelWidth] )) mask += 4;
					if (levelX > 0 && isOverlayNeighbor( terrain[levelCell - 1] )) mask += 8;

					data[dataCell] = mask == 0 ? -1 : mask;
				}
			}

			v.map( data, tileW );
			return v;
		}

		private static boolean isDirtPath( int tile ) {
			return tile == Terrain.EMPTY || tile == Terrain.HOMEBASE_DIRT_EDGE;
		}

		private static boolean isHomebaseGrass( int tile ) {
			return tile == Terrain.HOMEBASE_SHORT_GRASS
					|| tile == Terrain.HOMEBASE_MEDIUM_GRASS
					|| tile == Terrain.HOMEBASE_TALL_GRASS
					|| tile == Terrain.HOMEBASE_GRASS_DIRT_EDGE;
		}

		private static boolean isOverlayNeighbor( int tile ) {
			return isHomebaseGrass( tile ) || tile == Terrain.WATER;
		}
	}
}
