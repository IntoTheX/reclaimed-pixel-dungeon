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
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerExchange;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerTradeReceipt;
import com.watabou.noosa.Tilemap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class WayfarerExchangeLevel extends Level {

	private static final int WIDTH = 15;
	private static final int HEIGHT = 13;

	private static final int CENTER_X = WIDTH / 2;
	private static final int CENTER_Y = HEIGHT / 2;
	private static boolean returningHomebase = false;

	private final boolean hostSide;
	private final LinkedHashMap<String, WayfarerTrader> remoteTraders = new LinkedHashMap<>();
	private int syncedTradeRevision = -1;
	private ExchangeTicker exchangeTicker;

	{
		color1 = 0x4f5743;
		color2 = 0x8f9874;
		viewDistance = 10;
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
		if (returningHomebase) return;
		returningHomebase = true;
		try {
			if (Dungeon.level instanceof WayfarerExchangeLevel) {
				((WayfarerExchangeLevel)Dungeon.level).cleanupExchangeActors();
			}
			HomebaseLevel level = new HomebaseLevel();
			level.create();
			Dungeon.depth = 0;
			Dungeon.switchLevelTransient( level, level.entrance() );
			if (WayfarerExchangeService.consumePostReturnSavePending()) {
				try {
					Dungeon.saveAll();
				} catch (Exception e) {
					ShatteredPixelDungeon.reportException( e );
				}
			}
			ShatteredPixelDungeon.switchScene( GameScene.class );
		} finally {
			returningHomebase = false;
		}
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
		int seat = WayfarerExchangeService.localSeat();
		if (seat < 1 || seat > 6) seat = hostSide ? 1 : 4;
		return pedestalForSeat( seat );
	}

	private int pedestalForSeat( int seat ) {
		switch (seat) {
			case 2:
				return (CENTER_X - 3) + (CENTER_Y - 2) * width();
			case 3:
				return (CENTER_X + 3) + (CENTER_Y - 2) * width();
			case 4:
				return CENTER_X + (CENTER_Y + 3) * width();
			case 5:
				return (CENTER_X - 3) + (CENTER_Y + 3) * width();
			case 6:
				return (CENTER_X + 3) + (CENTER_Y + 3) * width();
			case 1:
			default:
				return CENTER_X + (CENTER_Y - 2) * width();
		}
	}

	public void syncRemoteTrader() {
		if (!WayfarerExchangeService.lobbyReady()) {
			removeRemoteTrader();
			return;
		}
		ArrayList<WayfarerExchangeService.PeerInfo> peers = WayfarerExchangeService.lobbyPeers();
		LinkedHashMap<String, WayfarerExchangeService.PeerInfo> live = new LinkedHashMap<>();
		for (WayfarerExchangeService.PeerInfo peer : peers) {
			if (peer == null || peer.id == null || peer.id.isEmpty()) continue;
			live.put( peer.id, peer );
			WayfarerTrader trader = remoteTraders.get( peer.id );
			if (trader == null || !mobs.contains( trader )) {
				trader = new WayfarerTrader( peer.profile, peer.id );
				trader.pos = pedestalForSeat( peer.seat );
				remoteTraders.put( peer.id, trader );
				GameScene.add( trader );
			} else if (syncedTradeRevision != WayfarerExchangeService.tradeRevision()) {
				trader.updateProfile( peer.profile );
				trader.pos = pedestalForSeat( peer.seat );
			}
		}
		for (String peerId : new ArrayList<>( remoteTraders.keySet() )) {
			if (!live.containsKey( peerId )) {
				removeRemoteTrader( peerId );
			}
		}
		syncedTradeRevision = WayfarerExchangeService.tradeRevision();
	}

	private void removeRemoteTrader() {
		for (String peerId : new ArrayList<>( remoteTraders.keySet() )) {
			removeRemoteTrader( peerId );
		}
		syncedTradeRevision = -1;
	}

	private void removeRemoteTrader( String peerId ) {
		WayfarerTrader trader = remoteTraders.remove( peerId );
		if (trader == null) return;
		if (trader.sprite != null) {
			trader.sprite.killAndErase();
			trader.sprite = null;
		}
		if (mobs.contains( trader )) {
			trader.destroy();
			mobs.remove( trader );
		}
	}

	private void cleanupExchangeActors() {
		removeRemoteTrader();
		if (exchangeTicker != null) {
			Actor.remove( exchangeTicker );
			exchangeTicker = null;
		}
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
		exchangeTicker = new ExchangeTicker();
		Actor.add( exchangeTicker );
		return exchangeTicker;
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
			if (Dungeon.level != WayfarerExchangeLevel.this) {
				Actor.remove( this );
				return true;
			}
			if (WayfarerExchangeService.consumeCloseRequest()) {
				WayfarerExchangeService.stop();
				WayfarerExchangeLevel.returnHomebase();
				return true;
			}
			String disconnectMessage = WayfarerExchangeService.consumeDisconnectMessage();
			if (disconnectMessage != null && !disconnectMessage.isEmpty()) {
				removeRemoteTrader();
				if (Dungeon.hero != null) {
					Dungeon.observe();
				}
				GameScene.updateFog();
			}
			if (WayfarerExchangeService.peerSealed() && WayfarerExchangeService.readyToFinalize()) {
				String error = WayfarerExchangeService.finalizeTradeFromPeerSeal();
				if (error == null || error.isEmpty()) {
					GameScene.show( new WndWayfarerTradeReceipt( WayfarerExchangeService.consumeReceiptPayload() ) );
				} else {
					GLog.w( error );
				}
			}
			if (WayfarerExchangeService.consumeIncomingRequestPopup()) {
				GameScene.show( new WndWayfarerExchange( WayfarerExchangeLevel.this.hostSide(), false ) );
			}
			syncRemoteTrader();
			localNameTicker -= TICK;
			if (Dungeon.hero != null && Dungeon.hero.sprite != null && localNameTicker <= 0) {
				Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, Dungeon.hero.characterName() );
				localNameTicker = 0.75f;
			}
			remoteNameTicker -= TICK;
			if (remoteNameTicker <= 0) {
				for (WayfarerTrader trader : remoteTraders.values()) {
					if (trader != null && trader.sprite != null) {
						trader.sprite.showStatus( CharSprite.POSITIVE, trader.name() );
					}
				}
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
