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

package com.erebus.reclaimedpixeldungeon.network;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.watabou.noosa.Game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

public class WayfarerExchangeService {

	public static final int DISCOVERY_PORT = 38616;
	public static final int TRADE_PORT = 38617;

	private static final String BEACON_PREFIX = "RPDX1";
	private static final String JOIN_PREFIX = "RPDX_JOIN";
	private static final String ACCEPT_PREFIX = "RPDX_ACCEPT";
	private static final String OFFER_PREFIX = "RPDX_OFFER";
	private static final String CONFIRM_PREFIX = "RPDX_CONFIRM";
	private static final String CLEAR_PREFIX = "RPDX_CLEAR";
	private static final String SEAL_PREFIX = "RPDX_SEAL";
	private static final String CLOSE_PREFIX = "RPDX_CLOSE";

	private static final Object LOCK = new Object();
	private static final LinkedHashMap<String, HostInfo> discoveredHosts = new LinkedHashMap<>();

	private static volatile Mode mode = Mode.IDLE;
	private static volatile boolean running = false;
	private static volatile String status = "The Wayfarer Exchange is idle.";
	private static volatile String localName = "";
	private static volatile String localHeroClass = "";
	private static volatile int localArmorTier = 0;
	private static volatile String connectedPeer = "";
	private static volatile String connectedPeerHeroClass = "";
	private static volatile int connectedPeerArmorTier = 0;
	private static volatile boolean closeRequested = false;

	private static DatagramSocket beaconSocket;
	private static DatagramSocket discoverySocket;
	private static ServerSocket serverSocket;
	private static Socket activeSocket;
	private static BufferedWriter sessionWriter;
	private static Thread beaconThread;
	private static Thread discoveryThread;
	private static Thread serverThread;
	private static Thread sessionThread;

	private static volatile String localOffer = "";
	private static volatile String remoteOffer = "";
	private static volatile boolean localConfirmed = false;
	private static volatile boolean remoteConfirmed = false;
	private static volatile boolean tradeFinalized = false;
	private static volatile int tradeRevision = 0;
	private static WayfarerTradePayload localPayload = new WayfarerTradePayload();
	private static WayfarerTradePayload remotePayload = new WayfarerTradePayload();
	private static final Item[] localEscrow = new Item[WayfarerTradePayload.ITEM_SLOTS];

	public enum Mode {
		IDLE,
		HOSTING,
		SEARCHING,
		CONNECTING,
		CONNECTED,
		ERROR
	}

	public static class HostInfo {
		public final String key;
		public final String name;
		public final String heroClass;
		public final String version;
		public final InetAddress address;
		public final int port;
		public final long lastSeen;

		private HostInfo( String name, String heroClass, String version, InetAddress address, int port, long lastSeen ) {
			this.name = name == null || name.isEmpty() ? "Unknown Trader" : name;
			this.heroClass = heroClass == null || heroClass.isEmpty() ? "unknown" : heroClass;
			this.version = version == null || version.isEmpty() ? "unknown" : version;
			this.address = address;
			this.port = port;
			this.lastSeen = lastSeen;
			this.key = address.getHostAddress() + ":" + port;
		}

		public String summary() {
			return name + " (" + heroClass + ")";
		}
	}

	public static Mode mode() {
		return mode;
	}

	public static String status() {
		return status;
	}

	public static String connectedPeer() {
		return connectedPeer;
	}

	public static String connectedPeerHeroClass() {
		return connectedPeerHeroClass;
	}

	public static int connectedPeerArmorTier() {
		return connectedPeerArmorTier;
	}

	public static boolean consumeCloseRequest() {
		if (!closeRequested) return false;
		closeRequested = false;
		return true;
	}

	public static String localOffer() {
		return localOffer;
	}

	public static String remoteOffer() {
		return remoteOffer;
	}

	public static boolean localConfirmed() {
		return localConfirmed;
	}

	public static boolean remoteConfirmed() {
		return remoteConfirmed;
	}

	public static int tradeRevision() {
		return tradeRevision;
	}

	public static boolean finalized() {
		return tradeFinalized;
	}

	public static WayfarerTradePayload localPayload() {
		return localPayload;
	}

	public static WayfarerTradePayload remotePayload() {
		return remotePayload;
	}

	public static Item localEscrowItem( int slot ) {
		return slot >= 0 && slot < localEscrow.length ? localEscrow[slot] : null;
	}

	public static boolean tradeReady() {
		Socket socket = activeSocket;
		return mode == Mode.CONNECTED && socket != null && socket.isConnected() && !socket.isClosed();
	}

	public static ArrayList<HostInfo> discoveredHosts() {
		synchronized (LOCK) {
			pruneOldHosts();
			return new ArrayList<>( discoveredHosts.values() );
		}
	}

	public static void startHost( String traderName, String heroClass, int armorTier ) {
		stop();
		localName = clean( traderName );
		localHeroClass = clean( heroClass );
		localArmorTier = armorTier;
		running = true;
		mode = Mode.HOSTING;
		status = "Hosting The Wayfarer Exchange on this LAN.";
		startServerThread();
		startBeaconThread();
	}

	public static void startSearch( String traderName, String heroClass, int armorTier ) {
		stop();
		localName = clean( traderName );
		localHeroClass = clean( heroClass );
		localArmorTier = armorTier;
		running = true;
		mode = Mode.SEARCHING;
		status = "Searching for Wayfarer Exchange hosts on this LAN...";
		synchronized (LOCK) {
			discoveredHosts.clear();
		}
		startDiscoveryThread();
	}

	public static void connectTo( final HostInfo host, String traderName, String heroClass, int armorTier ) {
		if (host == null) return;
		closeSocket( activeSocket );
		clearTradeState();
		localName = clean( traderName );
		localHeroClass = clean( heroClass );
		localArmorTier = armorTier;
		mode = Mode.CONNECTING;
		status = "Connecting to " + host.name + "...";
		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					Socket socket = new Socket();
					socket.connect( new InetSocketAddress( host.address, host.port ), 3500 );
					socket.setSoTimeout( 3500 );

					BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ) );
					BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
					writer.write( JOIN_PREFIX + "|" + packetText( localName ) + "|" + packetText( localHeroClass ) + "|" + localArmorTier + "|" + packetText( Game.version ) + "\n" );
					writer.flush();

					String response = reader.readLine();
					if (response != null && response.startsWith( ACCEPT_PREFIX + "|" )) {
						String[] parts = response.split( "\\|", -1 );
						connectedPeer = parts.length > 1 ? parts[1] : host.name;
						connectedPeerHeroClass = parts.length > 2 ? parts[2] : host.heroClass;
						connectedPeerArmorTier = parts.length > 3 ? parseInt( parts[3], 0 ) : 0;
						mode = Mode.CONNECTED;
						status = "Connected to " + connectedPeer + ". Start a trade offer when ready.";
						beginSession( socket );
					} else {
						closeSocket( socket );
						mode = Mode.ERROR;
						status = "The host did not accept the Wayfarer Exchange handshake.";
					}
				} catch (Exception e) {
					mode = Mode.ERROR;
					status = "Could not connect: " + e.getMessage();
				}
			}
		}, "Wayfarer Exchange Connect" );
		thread.setDaemon( true );
		thread.start();
	}

	public static void setLocalOffer( String offer ) {
		String cleanOffer = cleanOffer( offer );
		synchronized (LOCK) {
			localOffer = cleanOffer;
			localConfirmed = false;
			remoteConfirmed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
		sendSessionLine( OFFER_PREFIX + "|" + encode( cleanOffer ) );
	}

	public static boolean reserveItem( int slot, Item item, int quantity ) {
		if (slot < 0 || slot >= localEscrow.length || item == null || item instanceof Bag || Dungeon.hero == null) {
			return false;
		}
		quantity = Math.max( 1, Math.min( quantity, item.quantity() ) );
		Item offered;
		synchronized (LOCK) {
			returnEscrowLocked( slot );
			if (quantity < item.quantity()) {
				offered = item.split( quantity );
				item.updateQuickslot();
			} else {
				offered = item.detachAll( Dungeon.hero.belongings.backpack );
			}
			if (offered == null) return false;
			localEscrow[slot] = offered;
			localPayload.item( slot, offered );
			pushLocalPayloadLocked();
		}
		return true;
	}

	public static void removeItem( int slot ) {
		synchronized (LOCK) {
			returnEscrowLocked( slot );
			pushLocalPayloadLocked();
		}
	}

	public static void setGold( int amount ) {
		synchronized (LOCK) {
			localPayload.gold( Math.min( Math.max( amount, 0 ), Dungeon.homebase == null ? 0 : Dungeon.homebase.goldAmount() ) );
			pushLocalPayloadLocked();
		}
	}

	public static void setEnergy( int amount ) {
		synchronized (LOCK) {
			localPayload.energy( Math.min( Math.max( amount, 0 ), Dungeon.homebase == null ? 0 : Dungeon.homebase.energyAmount() ) );
			pushLocalPayloadLocked();
		}
	}

	public static void setMaterial( HomebaseState.Material material, int amount ) {
		if (material == null) return;
		synchronized (LOCK) {
			localPayload.material( material, Math.min( Math.max( amount, 0 ), Dungeon.homebase == null ? 0 : Dungeon.homebase.amount( material ) ) );
			pushLocalPayloadLocked();
		}
	}

	public static void setForgeResource( HomebaseState.ForgeResource resource, int amount ) {
		if (resource == null) return;
		synchronized (LOCK) {
			localPayload.forge( resource, Math.min( Math.max( amount, 0 ), Dungeon.homebase == null ? 0 : Dungeon.homebase.forgeResourceAmount( resource ) ) );
			pushLocalPayloadLocked();
		}
	}

	public static void confirmOffer() {
		synchronized (LOCK) {
			if (localPayload.isEmpty() || remotePayload.isEmpty() || tradeFinalized) return;
			localConfirmed = true;
			bumpTradeRevision();
		}
		sendSessionLine( CONFIRM_PREFIX );
	}

	public static void clearOffer() {
		synchronized (LOCK) {
			returnAllEscrowLocked();
			localPayload = new WayfarerTradePayload();
			localOffer = "";
			localConfirmed = false;
			remoteConfirmed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
		sendSessionLine( CLEAR_PREFIX );
	}

	public static String finalizeTrade() {
		synchronized (LOCK) {
			if (tradeFinalized) return "The exchange has already sealed this trade.";
			if (!localConfirmed || !remoteConfirmed) return "Both traders must confirm first.";
			if (localPayload.isEmpty() || remotePayload.isEmpty()) return "Both traders must offer something.";
			if (Dungeon.homebase == null) return "No homebase storage is available.";
			if (!hasLocalCurrencies()) return "you no longer have the offered resources.";

			if (!spendLocalCurrencies()) return "you no longer have the offered resources.";
			grantRemotePayload();
			clearEscrowWithoutReturningLocked();
			localPayload = new WayfarerTradePayload();
			remotePayload = new WayfarerTradePayload();
			localOffer = "";
			remoteOffer = "";
			localConfirmed = false;
			remoteConfirmed = false;
			tradeFinalized = true;
			status = "The Wayfarer Exchange seals the trade.";
			bumpTradeRevision();
			sendSessionLine( SEAL_PREFIX );
			return "";
		}
	}

	public static void closeExchange( boolean notifyPeer ) {
		if (notifyPeer) {
			sendSessionLine( CLOSE_PREFIX );
		}
		stop();
	}

	public static void stop() {
		synchronized (LOCK) {
			returnAllEscrowLocked();
		}
		running = false;
		closeDatagramSocket( beaconSocket );
		closeDatagramSocket( discoverySocket );
		closeServerSocket( serverSocket );
		closeSocket( activeSocket );
		beaconSocket = null;
		discoverySocket = null;
		serverSocket = null;
		activeSocket = null;
		sessionWriter = null;
		beaconThread = null;
		discoveryThread = null;
		serverThread = null;
		sessionThread = null;
		connectedPeer = "";
		connectedPeerHeroClass = "";
		connectedPeerArmorTier = 0;
		closeRequested = false;
		clearTradeState();
		mode = Mode.IDLE;
		status = "The Wayfarer Exchange is idle.";
		synchronized (LOCK) {
			discoveredHosts.clear();
		}
	}

	private static void startBeaconThread() {
		beaconThread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					beaconSocket = new DatagramSocket();
					beaconSocket.setBroadcast( true );
					while (running && mode == Mode.HOSTING) {
						byte[] data = beaconPayload().getBytes( StandardCharsets.UTF_8 );
						for (InetAddress address : broadcastAddresses()) {
							DatagramPacket packet = new DatagramPacket( data, data.length, address, DISCOVERY_PORT );
							beaconSocket.send( packet );
						}
						sleep( 1000 );
					}
				} catch (Exception e) {
					if (running) {
						mode = Mode.ERROR;
						status = "LAN beacon failed: " + e.getMessage();
					}
				}
			}
		}, "Wayfarer Exchange Beacon" );
		beaconThread.setDaemon( true );
		beaconThread.start();
	}

	private static void startServerThread() {
		serverThread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket( TRADE_PORT );
					serverSocket.setSoTimeout( 1000 );
					while (running && mode == Mode.HOSTING) {
						try {
							Socket socket = serverSocket.accept();
							handleJoin( socket );
						} catch (SocketTimeoutException ignored) {
							// Allows the thread to notice stop requests.
						}
					}
				} catch (Exception e) {
					if (running) {
						mode = Mode.ERROR;
						status = "Could not host trade: " + e.getMessage();
					}
				}
			}
		}, "Wayfarer Exchange Host" );
		serverThread.setDaemon( true );
		serverThread.start();
	}

	private static void startDiscoveryThread() {
		discoveryThread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					discoverySocket = new DatagramSocket( null );
					discoverySocket.setReuseAddress( true );
					discoverySocket.bind( new InetSocketAddress( DISCOVERY_PORT ) );
					discoverySocket.setSoTimeout( 1000 );
					byte[] buffer = new byte[512];
					while (running && mode == Mode.SEARCHING) {
						try {
							DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
							discoverySocket.receive( packet );
							parseBeacon( packet );
						} catch (SocketTimeoutException ignored) {
							synchronized (LOCK) {
								pruneOldHosts();
							}
						}
					}
				} catch (Exception e) {
					if (running) {
						mode = Mode.ERROR;
						status = "LAN search failed: " + e.getMessage();
					}
				}
			}
		}, "Wayfarer Exchange Search" );
		discoveryThread.setDaemon( true );
		discoveryThread.start();
	}

	private static void handleJoin( Socket socket ) {
		try {
			socket.setSoTimeout( 3500 );
			BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream(), StandardCharsets.UTF_8 ) );
			String request = reader.readLine();
			if (request != null && request.startsWith( JOIN_PREFIX + "|" )) {
				String[] parts = request.split( "\\|", -1 );
				connectedPeer = parts.length > 1 ? parts[1] : "a trader";
				connectedPeerHeroClass = parts.length > 2 ? parts[2] : "unknown";
				connectedPeerArmorTier = parts.length > 3 ? parseInt( parts[3], 0 ) : 0;
				status = connectedPeer + " joined your Wayfarer Exchange. Trade payloads are not enabled yet.";
				writer.write( ACCEPT_PREFIX + "|" + packetText( localName ) + "|" + packetText( localHeroClass ) + "|" + localArmorTier + "|" + packetText( Game.version ) + "\n" );
				writer.flush();
				closeSocket( activeSocket );
				mode = Mode.CONNECTED;
				status = "Connected to " + connectedPeer + ". Start a trade offer when ready.";
				beginSession( socket );
			} else {
				closeSocket( socket );
			}
		} catch (Exception e) {
			closeSocket( socket );
			if (running) status = "A trader tried to join, but the handshake failed.";
		}
	}

	private static void beginSession( Socket socket ) throws IOException {
		activeSocket = socket;
		activeSocket.setSoTimeout( 0 );
		sessionWriter = new BufferedWriter( new OutputStreamWriter( activeSocket.getOutputStream(), StandardCharsets.UTF_8 ) );
		sessionThread = new Thread( new Runnable() {
			@Override
			public void run() {
				readSession();
			}
		}, "Wayfarer Exchange Session" );
		sessionThread.setDaemon( true );
		sessionThread.start();
		bumpTradeRevision();
	}

	private static void readSession() {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( activeSocket.getInputStream(), StandardCharsets.UTF_8 ) );
			String line;
			while (running && mode == Mode.CONNECTED && (line = reader.readLine()) != null) {
				handleSessionLine( line );
			}
			if (running && mode == Mode.CONNECTED) {
				mode = Mode.ERROR;
				status = "The other trader disconnected.";
				bumpTradeRevision();
			}
		} catch (Exception e) {
			if (running && mode == Mode.CONNECTED) {
				mode = Mode.ERROR;
				status = "Trade session lost: " + e.getMessage();
				bumpTradeRevision();
			}
		}
	}

	private static void handleSessionLine( String line ) {
		if (line == null) return;
		if (line.startsWith( OFFER_PREFIX + "|" )) {
			synchronized (LOCK) {
				remoteOffer = decode( line.substring( (OFFER_PREFIX + "|").length() ) );
				remotePayload = WayfarerTradePayload.fromPacket( remoteOffer );
				localConfirmed = false;
				remoteConfirmed = false;
				tradeFinalized = false;
				status = connectedPeer + " updated their trade offer.";
				bumpTradeRevision();
			}
		} else if (line.equals( CONFIRM_PREFIX )) {
			synchronized (LOCK) {
				remoteConfirmed = true;
				status = connectedPeer + " confirmed the current trade preview.";
				bumpTradeRevision();
			}
		} else if (line.equals( SEAL_PREFIX )) {
			synchronized (LOCK) {
				remoteConfirmed = true;
				status = connectedPeer + " sealed the current trade.";
				bumpTradeRevision();
			}
		} else if (line.equals( CLEAR_PREFIX )) {
			synchronized (LOCK) {
				remoteOffer = "";
				remotePayload = new WayfarerTradePayload();
				localConfirmed = false;
				remoteConfirmed = false;
				status = connectedPeer + " cleared their trade offer.";
				bumpTradeRevision();
			}
		} else if (line.equals( CLOSE_PREFIX )) {
			closeRequested = true;
			status = connectedPeer + " closed the Wayfarer Exchange.";
			bumpTradeRevision();
		}
	}

	private static void sendSessionLine( String line ) {
		BufferedWriter writer = sessionWriter;
		if (writer == null || !tradeReady()) {
			mode = Mode.ERROR;
			status = "No active trader connection.";
			bumpTradeRevision();
			return;
		}
		try {
			synchronized (LOCK) {
				writer.write( line );
				writer.write( "\n" );
				writer.flush();
			}
		} catch (IOException e) {
			mode = Mode.ERROR;
			status = "Could not send trade update: " + e.getMessage();
			bumpTradeRevision();
		}
	}

	private static void clearTradeState() {
		synchronized (LOCK) {
			localPayload = new WayfarerTradePayload();
			remotePayload = new WayfarerTradePayload();
			localOffer = "";
			remoteOffer = "";
			localConfirmed = false;
			remoteConfirmed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
	}

	private static void pushLocalPayloadLocked() {
		localOffer = localPayload.toPacket();
		localConfirmed = false;
		remoteConfirmed = false;
		tradeFinalized = false;
		bumpTradeRevision();
		sendSessionLine( OFFER_PREFIX + "|" + encode( localOffer ) );
	}

	private static void returnEscrowLocked( int slot ) {
		if (slot < 0 || slot >= localEscrow.length || localEscrow[slot] == null) return;
		Item item = localEscrow[slot];
		localEscrow[slot] = null;
		localPayload.item( slot, null );
		collectOrDrop( item );
	}

	private static void returnAllEscrowLocked() {
		for (int i = 0; i < localEscrow.length; i++) {
			returnEscrowLocked( i );
		}
	}

	private static void clearEscrowWithoutReturningLocked() {
		for (int i = 0; i < localEscrow.length; i++) {
			localEscrow[i] = null;
		}
	}

	private static boolean hasLocalCurrencies() {
		if (Dungeon.homebase == null) return false;
		if (Dungeon.homebase.goldAmount() < localPayload.gold()) return false;
		if (Dungeon.homebase.energyAmount() < localPayload.energy()) return false;
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			if (Dungeon.homebase.amount( material ) < localPayload.material( material )) return false;
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			if (Dungeon.homebase.forgeResourceAmount( resource ) < localPayload.forge( resource )) return false;
		}
		return true;
	}

	private static boolean spendLocalCurrencies() {
		if (!Dungeon.homebase.spendGold( localPayload.gold() )) return false;
		if (!Dungeon.homebase.spendEnergy( localPayload.energy() )) return false;
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			if (!Dungeon.homebase.spend( material, localPayload.material( material ) )) return false;
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			if (!Dungeon.homebase.spendForgeResource( resource, localPayload.forge( resource ) )) return false;
		}
		return true;
	}

	private static void grantRemotePayload() {
		if (Dungeon.homebase != null) {
			Dungeon.homebase.addGold( remotePayload.gold() );
			Dungeon.homebase.addEnergy( remotePayload.energy() );
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				Dungeon.homebase.add( material, remotePayload.material( material ) );
			}
			for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
				Dungeon.homebase.addForgeResource( resource, remotePayload.forge( resource ) );
			}
		}
		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			Item item = remotePayload.item( i );
			if (item != null) collectOrDrop( item );
		}
	}

	private static void collectOrDrop( Item item ) {
		if (item == null || Dungeon.hero == null) return;
		if (!item.collect( Dungeon.hero.belongings.backpack ) && Dungeon.level != null) {
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private static void bumpTradeRevision() {
		tradeRevision++;
	}

	private static String beaconPayload() {
		return BEACON_PREFIX + "|"
				+ TRADE_PORT + "|"
				+ packetText( localName ) + "|"
				+ packetText( localHeroClass ) + "|"
				+ packetText( Game.version );
	}

	private static void parseBeacon( DatagramPacket packet ) {
		String payload = new String( packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8 );
		if (!payload.startsWith( BEACON_PREFIX + "|" )) return;
		String[] parts = payload.split( "\\|", -1 );
		if (parts.length < 5) return;
		int port;
		try {
			port = Integer.parseInt( parts[1] );
		} catch (NumberFormatException e) {
			return;
		}
		HostInfo info = new HostInfo( parts[2], parts[3], parts[4], packet.getAddress(), port, System.currentTimeMillis() );
		synchronized (LOCK) {
			discoveredHosts.put( info.key, info );
		}
	}

	private static ArrayList<InetAddress> broadcastAddresses() {
		ArrayList<InetAddress> addresses = new ArrayList<>();
		try {
			addresses.add( InetAddress.getByName( "255.255.255.255" ) );
		} catch (Exception ignored) {
			// Network interfaces below may still provide addresses.
		}
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces != null && interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (!networkInterface.isUp() || networkInterface.isLoopback()) continue;
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast != null && !addresses.contains( broadcast )) {
						addresses.add( broadcast );
					}
				}
			}
		} catch (Exception ignored) {
			// 255.255.255.255 is enough on many LANs.
		}
		return addresses;
	}

	private static void pruneOldHosts() {
		long now = System.currentTimeMillis();
		ArrayList<String> stale = new ArrayList<>();
		for (Map.Entry<String, HostInfo> entry : discoveredHosts.entrySet()) {
			if (now - entry.getValue().lastSeen > 5000) {
				stale.add( entry.getKey() );
			}
		}
		for (String key : stale) {
			discoveredHosts.remove( key );
		}
	}

	private static String clean( String text ) {
		if (text == null || text.trim().isEmpty()) return "Unknown Trader";
		return text.replace( '|', ' ' ).replace( '\n', ' ' ).replace( '\r', ' ' ).trim();
	}

	private static String packetText( String text ) {
		return clean( text );
	}

	private static int parseInt( String text, int fallback ) {
		try {
			return Integer.parseInt( text );
		} catch (Exception e) {
			return fallback;
		}
	}

	private static String cleanOffer( String text ) {
		if (text == null) return "";
		String trimmed = text.replace( '\r', '\n' ).trim();
		if (trimmed.length() > 320) trimmed = trimmed.substring( 0, 320 );
		return trimmed;
	}

	private static String encode( String text ) {
		return text
				.replace( "%", "%25" )
				.replace( "|", "%7C" )
				.replace( "\n", "%0A" );
	}

	private static String decode( String text ) {
		return text
				.replace( "%0A", "\n" )
				.replace( "%7C", "|" )
				.replace( "%25", "%" );
	}

	private static void sleep( long ms ) {
		try {
			Thread.sleep( ms );
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	private static void closeDatagramSocket( DatagramSocket socket ) {
		if (socket != null) socket.close();
	}

	private static void closeServerSocket( ServerSocket socket ) {
		if (socket == null) return;
		try {
			socket.close();
		} catch (IOException ignored) {
		}
	}

	private static void closeSocket( Socket socket ) {
		if (socket == null) return;
		try {
			socket.close();
		} catch (IOException ignored) {
		}
	}

	private WayfarerExchangeService() {
	}
}
