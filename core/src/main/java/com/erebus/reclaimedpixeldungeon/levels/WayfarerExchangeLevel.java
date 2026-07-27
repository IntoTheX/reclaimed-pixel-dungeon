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
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Mob;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.WayfarerTrader;
import com.erebus.reclaimedpixeldungeon.levels.features.LevelTransition;
import com.erebus.reclaimedpixeldungeon.levels.painters.Painter;
import com.erebus.reclaimedpixeldungeon.network.WayfarerExchangeService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.tiles.CustomTilemap;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTileSheet;
import com.watabou.noosa.Tilemap;

import java.util.Arrays;

public class WayfarerExchangeLevel extends Level {

	private static final int WIDTH = 15;
	private static final int HEIGHT = 13;

	private static final int CENTER_X = WIDTH / 2;
	private static final int CENTER_Y = HEIGHT / 2;

	private final boolean hostSide;
	private WayfarerTrader remoteTrader;

	{
		color1 = 0x4f5743;
		color2 = 0x8f9874;
		viewDistance = 6;
	}

	public WayfarerExchangeLevel() {
		this( true );
	}

	public WayfarerExchangeLevel( boolean hostSide ) {
		this.hostSide = hostSide;
	}

	public boolean hostSide() {
		return hostSide;
	}

	public static void enter( boolean hostSide ) {
		WayfarerExchangeLevel level = new WayfarerExchangeLevel( hostSide );
		level.create();
		Dungeon.switchLevelTransient( level, level.localPedestal() );
		ShatteredPixelDungeon.switchScene( GameScene.class );
	}

	public static void returnHomebase() {
		HomebaseLevel level = new HomebaseLevel();
		level.create();
		Dungeon.depth = 0;
		Dungeon.switchLevelTransient( level, level.entrance() );
		ShatteredPixelDungeon.switchScene( GameScene.class );
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_PRISON;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_PRISON;
	}

	@Override
	protected boolean build() {
		setSize( WIDTH, HEIGHT );
		Arrays.fill( map, Terrain.WALL );

		Painter.fill( this, 2, 2, WIDTH - 4, HEIGHT - 4, Terrain.EMPTY_SP );
		Painter.fill( this, 3, 3, WIDTH - 6, HEIGHT - 6, Terrain.EMPTY );

		for (int x = 4; x < WIDTH - 4; x += 4) {
			map[x + 2 * width()] = Terrain.WALL_DECO;
		}

		paintTable();
		paintPedestals( true );
		paintPedestals( false );
		customTiles.add( new SewerPedestalTable() );
		customTiles.add( new CityTraderPedestals() );

		int exit = CENTER_X + (HEIGHT - 3) * width();
		map[exit] = Terrain.EXIT;
		transitions.add( new LevelTransition(
				this,
				exit,
				LevelTransition.Type.REGULAR_ENTRANCE,
				0,
				0,
				LevelTransition.Type.REGULAR_ENTRANCE ) );

		return true;
	}

	private void paintTable() {
		Painter.fill( this, CENTER_X - 3, CENTER_Y, 7, 2, Terrain.EMPTY );
	}

	private void paintPedestals( boolean northSide ) {
		int y = northSide ? CENTER_Y - 2 : CENTER_Y + 3;
		for (int dx = -3; dx <= 3; dx += 3) {
			map[(CENTER_X + dx) + y * width()] = Terrain.EMPTY;
		}
	}

	public int localPedestal() {
		int y = hostSide ? CENTER_Y - 2 : CENTER_Y + 3;
		return CENTER_X + y * width();
	}

	private int remotePedestal() {
		int y = hostSide ? CENTER_Y + 3 : CENTER_Y - 2;
		return CENTER_X + y * width();
	}

	public void syncRemoteTrader() {
		if (!WayfarerExchangeService.tradeReady()) {
			removeRemoteTrader();
			return;
		}
		if (remoteTrader != null && mobs.contains( remoteTrader )) return;
		if (WayfarerExchangeService.connectedPeer() == null || WayfarerExchangeService.connectedPeer().isEmpty()) return;

		remoteTrader = new WayfarerTrader(
				WayfarerExchangeService.connectedPeer(),
				WayfarerExchangeService.connectedPeerHeroClass(),
				WayfarerExchangeService.connectedPeerArmorTier() );
		remoteTrader.pos = remotePedestal();
		GameScene.add( remoteTrader );
	}

	private void removeRemoteTrader() {
		if (remoteTrader == null) return;
		if (mobs.contains( remoteTrader )) {
			remoteTrader.destroy();
			mobs.remove( remoteTrader );
		}
		remoteTrader = null;
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
	}

	@Override
	protected void createItems() {
	}

	@Override
	public Actor addRespawner() {
		ExchangeTicker ticker = new ExchangeTicker();
		Actor.add( ticker );
		return ticker;
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		return localPedestal();
	}

	@Override
	public int randomDestination( Char ch ) {
		return localPedestal();
	}

	@Override
	public boolean invalidHeroPos( int pos ) {
		return pos < 0 || pos >= length() || !passable[pos];
	}

	private class ExchangeTicker extends Actor {

		private float localNameTicker = 0;
		private float remoteNameTicker = 0;

		@Override
		protected boolean act() {
			if (WayfarerExchangeService.consumeCloseRequest()) {
				WayfarerExchangeService.stop();
				WayfarerExchangeLevel.returnHomebase();
				return true;
			}
			syncRemoteTrader();
			localNameTicker -= TICK;
			if (Dungeon.hero != null && Dungeon.hero.sprite != null && localNameTicker <= 0) {
				Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, Dungeon.hero.characterName() );
				localNameTicker = 0.75f;
			}
			remoteNameTicker -= TICK;
			if (remoteTrader != null && remoteTrader.sprite != null && remoteNameTicker <= 0) {
				remoteTrader.sprite.showStatus( CharSprite.POSITIVE, remoteTrader.name() );
				remoteNameTicker = 0.75f;
			}
			spend( TICK );
			return true;
		}
	}

	public static class SewerPedestalTable extends CustomTilemap {

		{
			texture = Assets.Environment.TILES_SEWERS;
			tileW = 7;
			tileH = 2;
		}

		public SewerPedestalTable() {
			pos( CENTER_X - 3, CENTER_Y );
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int[] data = new int[tileW * tileH];
			Arrays.fill( data, DungeonTileSheet.PEDESTAL );
			v.map( data, tileW );
			return v;
		}
	}

	public static class CityTraderPedestals extends CustomTilemap {

		{
			texture = Assets.Environment.TILES_CITY;
			tileW = WIDTH;
			tileH = HEIGHT;
		}

		public CityTraderPedestals() {
			pos( 0, 0 );
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			int[] data = new int[tileW * tileH];
			Arrays.fill( data, -1 );
			for (int y : new int[]{ CENTER_Y - 2, CENTER_Y + 3 }) {
				for (int dx = -3; dx <= 3; dx += 3) {
					data[(CENTER_X + dx) + y * tileW] = DungeonTileSheet.PEDESTAL;
				}
			}
			v.map( data, tileW );
			return v;
		}
	}
}
