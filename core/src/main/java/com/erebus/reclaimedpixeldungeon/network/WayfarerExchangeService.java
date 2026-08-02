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
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
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
	private static final String LOBBY_PREFIX = "RPDX_LOBBY";
	private static final String REQUEST_PREFIX = "RPDX_REQUEST";
	private static final String ACCEPT_REQUEST_PREFIX = "RPDX_ACCEPT_REQUEST";
	private static final String DECLINE_REQUEST_PREFIX = "RPDX_DECLINE_REQUEST";
	private static final String EXPIRE_REQUEST_PREFIX = "RPDX_EXPIRE_REQUEST";
	private static final String END_TRADE_PREFIX = "RPDX_END_TRADE";
	private static final String TRADE_PREFIX = "RPDX_TRADE";
	private static final String PEER_LEFT_PREFIX = "RPDX_PEER_LEFT";
	private static final String BUSY_PREFIX = "RPDX_BUSY";

	public static final String HOST_ID = "HOST";
	private static final int MAX_TRADERS = 6;

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
	private static volatile WayfarerTraderProfile connectedPeerProfile = new WayfarerTraderProfile();
	private static volatile boolean closeRequested = false;
	private static volatile boolean disconnectPending = false;
	private static volatile String disconnectMessage = "";
	private static volatile boolean lobbySnapshotReceived = false;
	private static volatile String selfId = HOST_ID;
	private static volatile int localSeat = 1;
	private static volatile String activePeerId = "";
	private static volatile String incomingRequestFrom = "";
	private static volatile String outgoingRequestTo = "";
	private static volatile String requestNotice = "";
	private static volatile boolean incomingRequestPopupPending = false;

	private static DatagramSocket beaconSocket;
	private static DatagramSocket discoverySocket;
	private static ServerSocket serverSocket;
	private static Socket activeSocket;
	private static BufferedWriter sessionWriter;
	private static Thread beaconThread;
	private static Thread discoveryThread;
	private static Thread serverThread;
	private static Thread sessionThread;
	private static volatile int sessionGeneration = 0;
	private static int nextPeerIndex = 1;

	private static volatile String localOffer = "";
	private static volatile String remoteOffer = "";
	private static volatile boolean localConfirmed = false;
	private static volatile boolean remoteConfirmed = false;
	private static volatile boolean remoteSealed = false;
	private static volatile boolean tradeFinalized = false;
	private static volatile int tradeRevision = 0;
	private static WayfarerTradePayload localPayload = new WayfarerTradePayload();
	private static WayfarerTradePayload remotePayload = new WayfarerTradePayload();
	private static WayfarerTradePayload lastReceivedPayload = new WayfarerTradePayload();
	private static volatile boolean receiptPending = false;
	private static volatile boolean postReturnSavePending = false;
	private static volatile String postReturnNotice = "";
	private static final Item[] localEscrow = new Item[WayfarerTradePayload.ITEM_SLOTS];
	private static final ArrayList<ExchangeChatMessage> exchangeChatMessages = new ArrayList<>();
	private static final LinkedHashMap<String, PeerInfo> lobbyPeers = new LinkedHashMap<>();
	private static final LinkedHashMap<String, Session> sessions = new LinkedHashMap<>();
	private static final LinkedHashMap<String, String> pendingRequests = new LinkedHashMap<>();

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

	public static class PeerInfo {
		public final String id;
		public final WayfarerTraderProfile profile;
		public final int seat;
		public final boolean busy;

		private PeerInfo( String id, WayfarerTraderProfile profile, int seat, boolean busy ) {
			this.id = id == null ? "" : id;
			this.profile = profile == null ? new WayfarerTraderProfile() : profile;
			this.seat = Math.max( 1, Math.min( MAX_TRADERS, seat ) );
			this.busy = busy;
		}

		public String name() {
			return profile.name == null || profile.name.isEmpty() ? "Trader" : profile.name;
		}
	}

	public static class ExchangeChatMessage {
		public final String text;
		public final boolean warning;

		private ExchangeChatMessage( String text, boolean warning ) {
			this.text = text == null ? "" : text;
			this.warning = warning;
		}
	}

	private static class Session {
		private final String id;
		private final Socket socket;
		private final BufferedWriter writer;
		private WayfarerTraderProfile profile;
		private int seat;
		private boolean busy;
		private String tradePeerId = "";

		private Session( String id, Socket socket, BufferedWriter writer, WayfarerTraderProfile profile, int seat ) {
			this.id = id;
			this.socket = socket;
			this.writer = writer;
			this.profile = profile == null ? new WayfarerTraderProfile() : profile;
			this.seat = seat;
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

	public static WayfarerTraderProfile connectedPeerProfile() {
		return connectedPeerProfile;
	}

	public static String activePeerId() {
		return activePeerId;
	}

	public static int localSeat() {
		return localSeat;
	}

	public static boolean lobbyReady() {
		return mode == Mode.CONNECTED || mode == Mode.HOSTING;
	}

	public static ArrayList<PeerInfo> lobbyPeers() {
		synchronized (LOCK) {
			return new ArrayList<>( lobbyPeers.values() );
		}
	}

	public static boolean peerBusy( String peerId ) {
		synchronized (LOCK) {
			PeerInfo peer = lobbyPeers.get( peerId );
			return peer != null && peer.busy;
		}
	}

	public static String incomingRequestFrom() {
		return incomingRequestFrom;
	}

	public static String outgoingRequestTo() {
		return outgoingRequestTo;
	}

	public static String requestNotice() {
		return requestNotice;
	}

	public static void clearRequestNotice() {
		requestNotice = "";
		status = lobbyReady() ? "The Wayfarer Exchange is open." : status;
		bumpTradeRevision();
	}

	public static boolean consumeIncomingRequestPopup() {
		synchronized (LOCK) {
			if (!incomingRequestPopupPending) return false;
			incomingRequestPopupPending = false;
			return true;
		}
	}

	public static String peerName( String peerId ) {
		synchronized (LOCK) {
			if (HOST_ID.equals( peerId )) {
				WayfarerTraderProfile local = localProfile();
				return local.name == null || local.name.isEmpty() ? "Host" : local.name;
			}
			PeerInfo info = lobbyPeers.get( peerId );
			return info == null ? "Trader" : info.name();
		}
	}

	public static boolean consumeCloseRequest() {
		if (!closeRequested) return false;
		closeRequested = false;
		return true;
	}

	public static boolean hasDisconnectMessage() {
		return disconnectMessage != null && !disconnectMessage.isEmpty();
	}

	public static String disconnectMessage() {
		return disconnectMessage;
	}

	public static String consumeDisconnectMessage() {
		synchronized (LOCK) {
			if (!disconnectPending) return "";
			disconnectPending = false;
			return disconnectMessage;
		}
	}

	public static ExchangeChatMessage consumeExchangeChatMessage() {
		synchronized (LOCK) {
			if (exchangeChatMessages.isEmpty()) return null;
			return exchangeChatMessages.remove( 0 );
		}
	}

	public static String consumePostReturnNotice() {
		synchronized (LOCK) {
			String notice = postReturnNotice;
			postReturnNotice = "";
			return notice == null ? "" : notice;
		}
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

	public static boolean readyToFinalize() {
		return tradeReady()
				&& localConfirmed
				&& remoteConfirmed
				&& !tradeFinalized
				&& !localPayload.isEmpty()
				&& !remotePayload.isEmpty();
	}

	public static boolean peerSealed() {
		return remoteSealed;
	}

	public static WayfarerTradePayload localPayload() {
		return localPayload;
	}

	public static WayfarerTradePayload remotePayload() {
		return remotePayload;
	}

	public static WayfarerTradePayload consumeReceiptPayload() {
		synchronized (LOCK) {
			if (!receiptPending || lastReceivedPayload == null || lastReceivedPayload.isEmpty()) {
				receiptPending = false;
				return null;
			}
			receiptPending = false;
			return lastReceivedPayload.copy();
		}
	}

	public static boolean consumePostReturnSavePending() {
		synchronized (LOCK) {
			if (!postReturnSavePending) return false;
			postReturnSavePending = false;
			return true;
		}
	}

	public static Item localEscrowItem( int slot ) {
		return slot >= 0 && slot < localEscrow.length ? localEscrow[slot] : null;
	}

	public static boolean tradeReady() {
		if (mode != Mode.CONNECTED || activePeerId == null || activePeerId.isEmpty()) return false;
		if (HOST_ID.equals( selfId )) {
			Session session = sessions.get( activePeerId );
			return session != null && session.socket != null && session.socket.isConnected() && !session.socket.isClosed();
		}
		Socket socket = activeSocket;
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public static ArrayList<HostInfo> discoveredHosts() {
		synchronized (LOCK) {
			pruneOldHosts();
			return new ArrayList<>( discoveredHosts.values() );
		}
	}

	public static void requestTrade( String peerId ) {
		if (peerId == null || peerId.isEmpty() || peerBusy( peerId ) || tradeReady()) return;
		String previousIncoming = incomingRequestFrom;
		if (previousIncoming != null && !previousIncoming.isEmpty()) {
			if (HOST_ID.equals( selfId )) {
				handleTradeDeclined( HOST_ID, previousIncoming );
			} else {
				sendControlLine( DECLINE_REQUEST_PREFIX + "|" + selfId + "|" + previousIncoming );
			}
		}
		incomingRequestFrom = "";
		outgoingRequestTo = peerId;
		requestNotice = "Requesting to trade with " + peerName( peerId ) + "...";
		status = requestNotice;
		if (HOST_ID.equals( selfId )) {
			handleTradeRequest( HOST_ID, peerId );
		} else {
			sendControlLine( REQUEST_PREFIX + "|" + selfId + "|" + peerId );
		}
		bumpTradeRevision();
	}

	public static void acceptTradeRequest() {
		if (incomingRequestFrom == null || incomingRequestFrom.isEmpty()) return;
		String requester = incomingRequestFrom;
		if (HOST_ID.equals( selfId )) {
			handleTradeAccepted( HOST_ID, requester );
		} else {
			sendControlLine( ACCEPT_REQUEST_PREFIX + "|" + selfId + "|" + requester );
		}
	}

	public static void endCurrentTrade() {
		String peer = activePeerId;
		if (peer == null || peer.isEmpty()) return;
		if (HOST_ID.equals( selfId )) {
			endTradeBetween( HOST_ID, peer, true );
		} else {
			sendControlLine( END_TRADE_PREFIX + "|" + selfId + "|" + peer );
			endLocalTrade( peerName( peer ) + " returned to the lobby." );
		}
	}

	public static void declineTradeRequest() {
		if (incomingRequestFrom == null || incomingRequestFrom.isEmpty()) return;
		String requester = incomingRequestFrom;
		incomingRequestFrom = "";
		requestNotice = "You declined " + peerName( requester ) + "'s trade request.";
		status = requestNotice;
		if (HOST_ID.equals( selfId )) {
			handleTradeDeclined( HOST_ID, requester );
		} else {
			sendControlLine( DECLINE_REQUEST_PREFIX + "|" + selfId + "|" + requester );
		}
		bumpTradeRevision();
	}

	public static void startHost( String traderName, String heroClass, int armorTier ) {
		stop();
		clearDisconnectMessage();
		localName = clean( traderName );
		localHeroClass = clean( heroClass );
		localArmorTier = armorTier;
		selfId = HOST_ID;
		localSeat = 1;
		activePeerId = "";
		incomingRequestFrom = "";
		outgoingRequestTo = "";
		requestNotice = "";
		incomingRequestPopupPending = false;
		exchangeChatMessages.clear();
		lobbySnapshotReceived = false;
		nextPeerIndex = 1;
		synchronized (LOCK) {
			lobbyPeers.clear();
			sessions.clear();
			pendingRequests.clear();
		}
		running = true;
		mode = Mode.HOSTING;
		status = "Hosting The Wayfarer Exchange on this LAN.";
		startServerThread();
		startBeaconThread();
	}

	public static void startSearch( String traderName, String heroClass, int armorTier ) {
		stop();
		clearDisconnectMessage();
		localName = clean( traderName );
		localHeroClass = clean( heroClass );
		localArmorTier = armorTier;
		selfId = "";
		localSeat = 4;
		activePeerId = "";
		incomingRequestFrom = "";
		outgoingRequestTo = "";
		requestNotice = "";
		incomingRequestPopupPending = false;
		exchangeChatMessages.clear();
		lobbySnapshotReceived = false;
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
		clearDisconnectMessage();
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
					writer.write( JOIN_PREFIX + "|" + packetText( localName ) + "|" + packetText( localHeroClass ) + "|" + localArmorTier + "|" + packetText( Game.version ) + "|" + encode( localProfile().toPacket() ) + "\n" );
					writer.flush();

					String response = reader.readLine();
					if (response != null && response.startsWith( ACCEPT_PREFIX + "|" )) {
						String[] parts = response.split( "\\|", -1 );
						WayfarerTraderProfile hostProfile = parts.length > 5 ? WayfarerTraderProfile.fromPacket( decode( parts[5] ) ) : fallbackProfile( host.name, host.heroClass, 0 );
						selfId = parts.length > 6 && !parts[6].isEmpty() ? parts[6] : "C1";
						localSeat = parts.length > 7 ? parseInt( parts[7], 4 ) : 4;
						activePeerId = "";
						connectedPeer = "";
						connectedPeerHeroClass = "";
						connectedPeerArmorTier = 0;
						connectedPeerProfile = new WayfarerTraderProfile();
						synchronized (LOCK) {
							lobbyPeers.clear();
							lobbyPeers.put( HOST_ID, new PeerInfo( HOST_ID, hostProfile, 1, false ) );
						}
						mode = Mode.CONNECTED;
						status = "Joined " + hostProfile.name + "'s Wayfarer Exchange.";
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
			remoteSealed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
		sendSessionLine( OFFER_PREFIX + "|" + encode( cleanOffer ) );
	}

	public static boolean reserveItem( int slot, Item item, int quantity ) {
		if (slot < 0 || slot >= localEscrow.length || item == null || item instanceof Bag || Dungeon.hero == null) {
			return false;
		}
		if (item.isEquipped( Dungeon.hero )) {
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
			if (Dungeon.homebase == null || Dungeon.homebase.emeraldAmount() < 1) {
				status = "You need 1 emerald to seal a Wayfarer trade.";
				bumpTradeRevision();
				return;
			}
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
			remoteSealed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
		sendSessionLine( CLEAR_PREFIX );
	}

	public static String finalizeTrade() {
		return finalizeTrade( true );
	}

	public static String finalizeTradeFromPeerSeal() {
		return finalizeTrade( false );
	}

	private static String finalizeTrade( boolean notifyPeer ) {
		synchronized (LOCK) {
			if (tradeFinalized) return "The exchange has already sealed this trade.";
			if (!localConfirmed || !remoteConfirmed) return "Both traders must confirm first.";
			if (localPayload.isEmpty() || remotePayload.isEmpty()) return "Both traders must offer something.";
			if (Dungeon.homebase == null) return "No homebase storage is available.";
			if (Dungeon.homebase.emeraldAmount() < 1) return "you need 1 emerald to seal a Wayfarer trade.";
			if (!hasLocalCurrencies()) return "you no longer have the offered resources.";

			if (!Dungeon.homebase.spendEmeralds( 1 )) return "you need 1 emerald to seal a Wayfarer trade.";
			if (!spendLocalCurrencies()) return "you no longer have the offered resources.";
			lastReceivedPayload = remotePayload.copy();
			receiptPending = !lastReceivedPayload.isEmpty();
			grantRemotePayload();
			clearEscrowWithoutReturningLocked();
			localPayload = new WayfarerTradePayload();
			remotePayload = new WayfarerTradePayload();
			localOffer = "";
			remoteOffer = "";
			localConfirmed = false;
			remoteConfirmed = false;
			remoteSealed = false;
			tradeFinalized = true;
			status = "The Wayfarer Exchange seals the trade.";
			postReturnSavePending = true;
			saveGameAfterTrade();
			bumpTradeRevision();
			if (notifyPeer) {
				sendSessionLine( SEAL_PREFIX );
			}
			return "";
		}
	}

	public static void closeExchange( boolean notifyPeer ) {
		if (notifyPeer) {
			if (HOST_ID.equals( selfId )) {
				for (String peerId : new ArrayList<>( sessions.keySet() )) {
					sendControlToPeer( peerId, CLOSE_PREFIX );
				}
			} else {
				sendControlLine( CLOSE_PREFIX );
			}
		}
		stop();
	}

	public static void stop() {
		sessionGeneration++;
		synchronized (LOCK) {
			returnAllEscrowLocked();
		}
		running = false;
		closeDatagramSocket( beaconSocket );
		closeDatagramSocket( discoverySocket );
		closeServerSocket( serverSocket );
		closeSocket( activeSocket );
		synchronized (LOCK) {
			for (Session session : sessions.values()) {
				closeSocket( session.socket );
			}
			sessions.clear();
			lobbyPeers.clear();
			pendingRequests.clear();
		}
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
		connectedPeerProfile = new WayfarerTraderProfile();
		closeRequested = false;
		incomingRequestFrom = "";
		outgoingRequestTo = "";
		requestNotice = "";
		incomingRequestPopupPending = false;
		activePeerId = "";
		disconnectPending = false;
		disconnectMessage = "";
		exchangeChatMessages.clear();
		lobbySnapshotReceived = false;
		selfId = HOST_ID;
		localSeat = 1;
		activePeerId = "";
		incomingRequestFrom = "";
		outgoingRequestTo = "";
		requestNotice = "";
		nextPeerIndex = 1;
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
					while (running && HOST_ID.equals( selfId ) && (mode == Mode.HOSTING || mode == Mode.CONNECTED)) {
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
					while (running && HOST_ID.equals( selfId ) && (mode == Mode.HOSTING || mode == Mode.CONNECTED)) {
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
				WayfarerTraderProfile profile = parts.length > 5 ? WayfarerTraderProfile.fromPacket( decode( parts[5] ) ) : fallbackProfile( parts.length > 1 ? parts[1] : "a trader", parts.length > 2 ? parts[2] : "unknown", parts.length > 3 ? parseInt( parts[3], 0 ) : 0 );
				String peerId;
				int seat;
				synchronized (LOCK) {
					if (sessions.size() >= MAX_TRADERS - 1) {
						closeSocket( socket );
						status = "The Wayfarer Exchange is full.";
						return;
					}
					peerId = "C" + nextPeerIndex++;
					seat = nextAvailableSeatLocked();
					sessions.put( peerId, new Session( peerId, socket, writer, profile, seat ) );
					lobbyPeers.put( peerId, new PeerInfo( peerId, profile, seat, false ) );
				}
				status = profile.name + " joined your Wayfarer Exchange.";
				queueExchangeChatMessage( profile.name + " joined the exchange." );
				writer.write( ACCEPT_PREFIX + "|" + packetText( localName ) + "|" + packetText( localHeroClass ) + "|" + localArmorTier + "|" + packetText( Game.version ) + "|" + encode( localProfile().toPacket() ) + "|" + peerId + "|" + seat + "\n" );
				writer.flush();
				mode = Mode.CONNECTED;
				beginSession( peerId, socket );
				broadcastLobby();
			} else {
				closeSocket( socket );
			}
		} catch (Exception e) {
			closeSocket( socket );
			if (running) status = "A trader tried to join, but the handshake failed.";
		}
	}

	private static void beginSession( Socket socket ) throws IOException {
		sessionGeneration++;
		activeSocket = socket;
		activeSocket.setSoTimeout( 0 );
		sessionWriter = new BufferedWriter( new OutputStreamWriter( activeSocket.getOutputStream(), StandardCharsets.UTF_8 ) );
		final Socket sessionSocket = activeSocket;
		final int generation = sessionGeneration;
		sessionThread = new Thread( new Runnable() {
			@Override
			public void run() {
				readSession( sessionSocket, generation );
			}
		}, "Wayfarer Exchange Session" );
		sessionThread.setDaemon( true );
		sessionThread.start();
		bumpTradeRevision();
	}

	private static void beginSession( final String peerId, Socket socket ) throws IOException {
		final int generation = sessionGeneration;
		socket.setSoTimeout( 0 );
		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				readSession( peerId, socket, generation );
			}
		}, "Wayfarer Exchange Session " + peerId );
		thread.setDaemon( true );
		thread.start();
		bumpTradeRevision();
	}

	private static void readSession( Socket socket, int generation ) {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
			String line;
			while (sessionActive( generation ) && (line = reader.readLine()) != null) {
				handleSessionLine( line, generation );
			}
			if (sessionActive( generation )) {
				markPeerDisconnected( connectedPeer, "The other trader disconnected." );
			}
		} catch (Exception e) {
			if (sessionActive( generation )) {
				markPeerDisconnected( connectedPeer, "Trade session lost: " + e.getMessage() );
			}
		}
	}

	private static void readSession( String peerId, Socket socket, int generation ) {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream(), StandardCharsets.UTF_8 ) );
			String line;
			while (hostSessionActive( peerId, generation ) && (line = reader.readLine()) != null) {
				handleHostSessionLine( peerId, line );
			}
			if (hostSessionActive( peerId, generation )) {
				markLobbyPeerDisconnected( peerId );
			}
		} catch (Exception e) {
			if (hostSessionActive( peerId, generation )) {
				markLobbyPeerDisconnected( peerId );
			}
		}
	}

	private static boolean sessionActive( int generation ) {
		return running && mode == Mode.CONNECTED && generation == sessionGeneration;
	}

	private static boolean hostSessionActive( String peerId, int generation ) {
		synchronized (LOCK) {
			return running && HOST_ID.equals( selfId ) && generation == sessionGeneration && sessions.containsKey( peerId );
		}
	}

	private static void handleHostSessionLine( String fromId, String line ) {
		if (line == null || fromId == null || fromId.isEmpty()) return;
		if (line.startsWith( REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length < 3) return;
			handleTradeRequest( parts[1], parts[2] );
		} else if (line.startsWith( ACCEPT_REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length < 3) return;
			handleTradeAccepted( parts[1], parts[2] );
		} else if (line.startsWith( DECLINE_REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length < 3) return;
			handleTradeDeclined( parts[1], parts[2] );
		} else if (line.startsWith( END_TRADE_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length < 3) return;
			endTradeBetween( parts[1], parts[2], true );
		} else if (line.startsWith( TRADE_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", 3 );
			if (parts.length < 3) return;
			String target = parts[1];
			String routedLine = decode( parts[2] );
			if (HOST_ID.equals( target )) {
				handleSessionLine( routedLine, sessionGeneration );
			} else {
				sendToSession( target, routedLine );
			}
		} else if (line.equals( CLOSE_PREFIX )) {
			markLobbyPeerDisconnected( fromId );
		}
	}

	private static void handleSessionLine( String line, int generation ) {
		if (line == null || !sessionActive( generation )) return;
		if (line.startsWith( LOBBY_PREFIX + "|" )) {
			parseLobby( line );
		} else if (line.startsWith( REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length >= 3 && selfId.equals( parts[2] )) {
				incomingRequestFrom = parts[1];
				outgoingRequestTo = "";
				requestNotice = peerName( parts[1] ) + " is requesting to trade.";
				incomingRequestPopupPending = true;
				status = requestNotice;
				bumpTradeRevision();
			}
		} else if (line.startsWith( ACCEPT_REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length >= 3 && selfId.equals( parts[2] )) {
				startTradeWithPeer( parts[1] );
				requestNotice = peerName( parts[1] ) + " accepted your trade request.";
				status = "Trading with " + connectedPeer + ".";
				bumpTradeRevision();
			}
		} else if (line.startsWith( DECLINE_REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			if (parts.length >= 3 && selfId.equals( parts[2] )) {
				outgoingRequestTo = "";
				requestNotice = peerName( parts[1] ) + " declined your trade request.";
				status = requestNotice;
				bumpTradeRevision();
			}
		} else if (line.startsWith( EXPIRE_REQUEST_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			String requester = parts.length > 1 ? parts[1] : "";
			synchronized (LOCK) {
				if (requester.isEmpty() || requester.equals( incomingRequestFrom )) {
					incomingRequestFrom = "";
					requestNotice = "Trade Request Expired.";
					status = requestNotice;
					incomingRequestPopupPending = true;
				}
			}
			bumpTradeRevision();
		} else if (line.startsWith( PEER_LEFT_PREFIX + "|" )) {
			String[] parts = line.split( "\\|", -1 );
			String peerId = parts.length > 1 ? parts[1] : "";
			String name = parts.length > 2 ? decode( parts[2] ) : peerName( peerId );
			synchronized (LOCK) {
				lobbyPeers.remove( peerId );
				if (peerId.equals( activePeerId )) {
					returnAllEscrowLocked();
					activePeerId = "";
					connectedPeer = "";
					connectedPeerHeroClass = "";
					connectedPeerArmorTier = 0;
					connectedPeerProfile = new WayfarerTraderProfile();
					clearTradeStateLocked();
				}
				if (peerId.equals( incomingRequestFrom )) incomingRequestFrom = "";
				if (peerId.equals( outgoingRequestTo )) outgoingRequestTo = "";
				pendingRequests.remove( peerId );
				for (String requester : new ArrayList<>( pendingRequests.keySet() )) {
					if (peerId.equals( pendingRequests.get( requester ) )) pendingRequests.remove( requester );
				}
				disconnectMessage = name + " has disconnected.";
				disconnectPending = true;
				status = disconnectMessage;
				queueExchangeChatMessageLocked( name + " left the exchange.", true );
			}
			bumpTradeRevision();
		} else if (line.startsWith( BUSY_PREFIX + "|" )) {
			parseBusy( line );
		} else
		if (line.startsWith( OFFER_PREFIX + "|" )) {
			synchronized (LOCK) {
				if (!sessionActive( generation )) return;
				remoteOffer = decode( line.substring( (OFFER_PREFIX + "|").length() ) );
				remotePayload = WayfarerTradePayload.fromPacket( remoteOffer );
				localConfirmed = false;
				remoteConfirmed = false;
				remoteSealed = false;
				tradeFinalized = false;
				status = connectedPeer + " updated their trade offer.";
				bumpTradeRevision();
			}
		} else if (line.equals( CONFIRM_PREFIX )) {
			synchronized (LOCK) {
				if (!sessionActive( generation )) return;
				remoteConfirmed = true;
				status = connectedPeer + " confirmed the current trade preview.";
				bumpTradeRevision();
			}
		} else if (line.equals( SEAL_PREFIX )) {
			synchronized (LOCK) {
				if (!sessionActive( generation )) return;
				remoteConfirmed = true;
				remoteSealed = true;
				status = connectedPeer + " sealed the current trade.";
				bumpTradeRevision();
			}
		} else if (line.equals( CLEAR_PREFIX )) {
			synchronized (LOCK) {
				if (!sessionActive( generation )) return;
				remoteOffer = "";
				remotePayload = new WayfarerTradePayload();
				localConfirmed = false;
				remoteConfirmed = false;
				remoteSealed = false;
				status = connectedPeer + " cleared their trade offer.";
				bumpTradeRevision();
			}
		} else if (line.equals( CLOSE_PREFIX )) {
			String hostName = connectedPeer == null || connectedPeer.isEmpty() ? peerName( HOST_ID ) : connectedPeer;
			if (hostName == null || hostName.isEmpty() || "Trader".equals( hostName )) {
				hostName = "the host";
			}
			synchronized (LOCK) {
				postReturnNotice = "The Wayfarer Exchange host, " + hostName + " has closed the trade.";
				closeRequested = true;
				status = postReturnNotice;
			}
			bumpTradeRevision();
		} else if (line.equals( END_TRADE_PREFIX )) {
			endLocalTrade( connectedPeer + " returned to the lobby." );
		}
	}

	private static void handleTradeRequest( String fromId, String toId ) {
		if (fromId == null || toId == null || fromId.isEmpty() || toId.isEmpty()) return;
		synchronized (LOCK) {
			String previousTarget = pendingRequests.get( fromId );
			if (previousTarget != null && !previousTarget.isEmpty() && !previousTarget.equals( toId )) {
				sendControlToPeer( previousTarget, EXPIRE_REQUEST_PREFIX + "|" + fromId );
			}
			pendingRequests.remove( fromId );
			if (isBusyLocked( fromId ) || isBusyLocked( toId )) {
				sendControlToPeer( fromId, DECLINE_REQUEST_PREFIX + "|" + toId + "|" + fromId );
				return;
			}
			pendingRequests.put( fromId, toId );
		}
		if (HOST_ID.equals( toId )) {
			incomingRequestFrom = fromId;
			outgoingRequestTo = "";
			requestNotice = peerName( fromId ) + " is requesting to trade.";
			incomingRequestPopupPending = true;
			status = requestNotice;
			bumpTradeRevision();
		} else {
			sendControlToPeer( toId, REQUEST_PREFIX + "|" + fromId + "|" + toId );
		}
	}

	private static void handleTradeAccepted( String fromId, String toId ) {
		if (fromId == null || toId == null || fromId.isEmpty() || toId.isEmpty()) return;
		synchronized (LOCK) {
			String pendingTarget = pendingRequests.get( toId );
			if (pendingTarget == null || !pendingTarget.equals( fromId )
					|| isBusyLocked( fromId ) || isBusyLocked( toId )) {
				sendControlToPeer( fromId, EXPIRE_REQUEST_PREFIX + "|" + toId );
				return;
			}
			pendingRequests.remove( toId );
			setBusyLocked( fromId, true );
			setBusyLocked( toId, true );
			setTradePeerLocked( fromId, toId );
			setTradePeerLocked( toId, fromId );
		}
		if (HOST_ID.equals( toId )) {
			startTradeWithPeer( fromId );
			requestNotice = peerName( fromId ) + " accepted your trade request.";
			status = "Trading with " + connectedPeer + ".";
			sendControlToPeer( fromId, ACCEPT_REQUEST_PREFIX + "|" + toId + "|" + fromId );
		} else if (HOST_ID.equals( fromId )) {
			startTradeWithPeer( toId );
			sendControlToPeer( toId, ACCEPT_REQUEST_PREFIX + "|" + fromId + "|" + toId );
		} else {
			sendControlToPeer( fromId, ACCEPT_REQUEST_PREFIX + "|" + toId + "|" + fromId );
			sendControlToPeer( toId, ACCEPT_REQUEST_PREFIX + "|" + fromId + "|" + toId );
		}
		broadcastLobby();
		bumpTradeRevision();
	}

	private static void handleTradeDeclined( String fromId, String toId ) {
		if (fromId == null || toId == null || fromId.isEmpty() || toId.isEmpty()) return;
		synchronized (LOCK) {
			String pendingTarget = pendingRequests.get( toId );
			if (pendingTarget != null && pendingTarget.equals( fromId )) {
				pendingRequests.remove( toId );
			}
		}
		if (HOST_ID.equals( toId )) {
			outgoingRequestTo = "";
			requestNotice = peerName( fromId ) + " declined your trade request.";
			status = requestNotice;
			bumpTradeRevision();
		} else {
			sendControlToPeer( toId, DECLINE_REQUEST_PREFIX + "|" + fromId + "|" + toId );
		}
	}

	private static void startTradeWithPeer( String peerId ) {
		PeerInfo info;
		synchronized (LOCK) {
			info = HOST_ID.equals( peerId )
					? new PeerInfo( HOST_ID, localProfile(), 1, true )
					: lobbyPeers.get( peerId );
			activePeerId = peerId == null ? "" : peerId;
			incomingRequestFrom = "";
			outgoingRequestTo = "";
			clearTradeStateLocked();
		}
		if (info != null) {
			connectedPeer = info.name();
			connectedPeerHeroClass = info.profile.heroClass;
			connectedPeerArmorTier = info.profile.armorTier;
			connectedPeerProfile = info.profile;
		}
	}

	private static void endTradeBetween( String firstId, String secondId, boolean notify ) {
		if (firstId == null || secondId == null || firstId.isEmpty() || secondId.isEmpty()) return;
		synchronized (LOCK) {
			setBusyLocked( firstId, false );
			setBusyLocked( secondId, false );
			setTradePeerLocked( firstId, "" );
			setTradePeerLocked( secondId, "" );
			pendingRequests.remove( firstId );
			pendingRequests.remove( secondId );
			if (HOST_ID.equals( firstId ) || HOST_ID.equals( secondId )) {
				String peer = HOST_ID.equals( firstId ) ? secondId : firstId;
				if (peer.equals( activePeerId )) {
					endLocalTradeLocked( peerName( peer ) + " returned to the lobby." );
				}
			}
		}
		if (notify) {
			if (!HOST_ID.equals( firstId )) sendControlToPeer( firstId, END_TRADE_PREFIX );
			if (!HOST_ID.equals( secondId )) sendControlToPeer( secondId, END_TRADE_PREFIX );
		}
		broadcastLobby();
		bumpTradeRevision();
	}

	private static void endLocalTrade( String message ) {
		synchronized (LOCK) {
			endLocalTradeLocked( message );
		}
		bumpTradeRevision();
	}

	private static void endLocalTradeLocked( String message ) {
		returnAllEscrowLocked();
		activePeerId = "";
		connectedPeer = "";
		connectedPeerHeroClass = "";
		connectedPeerArmorTier = 0;
		connectedPeerProfile = new WayfarerTraderProfile();
		incomingRequestFrom = "";
		outgoingRequestTo = "";
		clearTradeStateLocked();
		requestNotice = message == null || message.isEmpty() ? "Returned to the trading lobby." : message;
		status = requestNotice;
	}

	private static int nextAvailableSeatLocked() {
		boolean[] used = new boolean[MAX_TRADERS];
		used[0] = true;
		for (Session session : sessions.values()) {
			if (session.seat >= 1 && session.seat <= MAX_TRADERS) {
				used[session.seat - 1] = true;
			}
		}
		for (int i = 0; i < used.length; i++) {
			if (!used[i]) return i + 1;
		}
		return MAX_TRADERS;
	}

	private static void setTradePeerLocked( String peerId, String tradePeerId ) {
		if (peerId == null || peerId.isEmpty() || HOST_ID.equals( peerId )) return;
		Session session = sessions.get( peerId );
		if (session != null) session.tradePeerId = tradePeerId == null ? "" : tradePeerId;
	}

	private static boolean isBusyLocked( String peerId ) {
		if (HOST_ID.equals( peerId )) return activePeerId != null && !activePeerId.isEmpty();
		Session session = sessions.get( peerId );
		if (session != null) return session.busy;
		PeerInfo info = lobbyPeers.get( peerId );
		return info != null && info.busy;
	}

	private static void setBusyLocked( String peerId, boolean busy ) {
		if (HOST_ID.equals( peerId )) {
			if (!busy) activePeerId = "";
			return;
		}
		Session session = sessions.get( peerId );
		if (session != null) {
			session.busy = busy;
			if (!busy) session.tradePeerId = "";
			lobbyPeers.put( peerId, new PeerInfo( peerId, session.profile, session.seat, busy ) );
		} else {
			PeerInfo info = lobbyPeers.get( peerId );
			if (info != null) lobbyPeers.put( peerId, new PeerInfo( peerId, info.profile, info.seat, busy ) );
		}
	}

	private static void broadcastLobby() {
		if (!HOST_ID.equals( selfId )) return;
		String packet = buildLobbyPacket();
		for (String peerId : new ArrayList<>( sessions.keySet() )) {
			sendControlToPeer( peerId, packet );
		}
		bumpTradeRevision();
	}

	private static String buildLobbyPacket() {
		StringBuilder builder = new StringBuilder( LOBBY_PREFIX );
		builder.append( "|" ).append( lobbyRecord( HOST_ID, localProfile(), 1, isBusyLocked( HOST_ID ) ) );
		synchronized (LOCK) {
			for (Session session : sessions.values()) {
				builder.append( "|" ).append( lobbyRecord( session.id, session.profile, session.seat, session.busy ) );
			}
		}
		return builder.toString();
	}

	private static String lobbyRecord( String peerId, WayfarerTraderProfile profile, int seat, boolean busy ) {
		return encode( encode( peerId )
				+ "|"
				+ encode( profile == null ? "" : profile.toPacket() )
				+ "|"
				+ seat
				+ "|"
				+ (busy ? 1 : 0) );
	}

	private static void parseLobby( String line ) {
		String[] parts = line.split( "\\|", -1 );
		synchronized (LOCK) {
			LinkedHashMap<String, PeerInfo> previous = new LinkedHashMap<>( lobbyPeers );
			lobbyPeers.clear();
			for (int i = 1; i < parts.length; i++) {
				String[] peer = decode( parts[i] ).split( "\\|", -1 );
				if (peer.length < 4) continue;
				String id = decode( peer[0] );
				if (id == null || id.isEmpty() || id.equals( selfId )) continue;
				WayfarerTraderProfile profile = WayfarerTraderProfile.fromPacket( decode( peer[1] ) );
				int seat = parseInt( peer[2], 1 );
				boolean busy = "1".equals( peer[3] );
				lobbyPeers.put( id, new PeerInfo( id, profile, seat, busy ) );
			}
			if (lobbySnapshotReceived) {
				for (PeerInfo peer : lobbyPeers.values()) {
					if (!previous.containsKey( peer.id )) {
						queueExchangeChatMessageLocked( peer.name() + " joined the exchange." );
					}
				}
				for (PeerInfo peer : previous.values()) {
					if (!lobbyPeers.containsKey( peer.id )) {
						queueExchangeChatMessageLocked( peer.name() + " left the exchange.", true );
					}
				}
			}
			lobbySnapshotReceived = true;
		}
		bumpTradeRevision();
	}

	private static void parseBusy( String line ) {
		String[] parts = line.split( "\\|", -1 );
		if (parts.length < 3) return;
		synchronized (LOCK) {
			PeerInfo info = lobbyPeers.get( parts[1] );
			if (info != null) {
				lobbyPeers.put( parts[1], new PeerInfo( info.id, info.profile, info.seat, "1".equals( parts[2] ) ) );
			}
		}
		bumpTradeRevision();
	}

	private static void sendControlLine( String line ) {
		BufferedWriter writer = sessionWriter;
		if (writer == null || activeSocket == null || activeSocket.isClosed()) {
			markPeerDisconnected( connectedPeer, "No Wayfarer Exchange host is connected." );
			return;
		}
		try {
			synchronized (LOCK) {
				writer.write( line );
				writer.write( "\n" );
				writer.flush();
			}
		} catch (IOException e) {
			markPeerDisconnected( connectedPeer, "Could not send lobby update: " + e.getMessage() );
		}
	}

	private static void sendControlToPeer( String peerId, String line ) {
		if (HOST_ID.equals( peerId )) {
			handleSessionLine( line, sessionGeneration );
			return;
		}
		sendRawToSession( peerId, line );
	}

	private static void sendToSession( String peerId, String line ) {
		sendRawToSession( peerId, line );
	}

	private static void sendRawToSession( String peerId, String line ) {
		Session session;
		synchronized (LOCK) {
			session = sessions.get( peerId );
		}
		if (session == null || session.socket == null || session.socket.isClosed()) return;
		try {
			synchronized (session.writer) {
				session.writer.write( line );
				session.writer.write( "\n" );
				session.writer.flush();
			}
		} catch (IOException e) {
			markLobbyPeerDisconnected( peerId );
		}
	}

	private static void markLobbyPeerDisconnected( String peerId ) {
		if (peerId == null || peerId.isEmpty()) return;
		Session removed;
		String name;
		String partner = "";
		synchronized (LOCK) {
			removed = sessions.remove( peerId );
			PeerInfo info = lobbyPeers.remove( peerId );
			name = info == null ? "A trader" : info.name();
			if (removed != null) {
				partner = removed.tradePeerId;
				closeSocket( removed.socket );
			}
			if (peerId.equals( activePeerId )) {
				returnAllEscrowLocked();
				activePeerId = "";
				connectedPeer = "";
				connectedPeerHeroClass = "";
				connectedPeerArmorTier = 0;
				connectedPeerProfile = new WayfarerTraderProfile();
				clearTradeStateLocked();
			}
			if (peerId.equals( incomingRequestFrom )) incomingRequestFrom = "";
			if (peerId.equals( outgoingRequestTo )) outgoingRequestTo = "";
			pendingRequests.remove( peerId );
			for (String requester : new ArrayList<>( pendingRequests.keySet() )) {
				if (peerId.equals( pendingRequests.get( requester ) )) pendingRequests.remove( requester );
			}
			setBusyLocked( peerId, false );
			if (partner != null && !partner.isEmpty()) {
				setBusyLocked( partner, false );
			}
			disconnectMessage = name + " has disconnected.";
			disconnectPending = true;
			status = disconnectMessage;
			queueExchangeChatMessageLocked( name + " left the exchange.", true );
		}
		for (String otherId : new ArrayList<>( sessions.keySet() )) {
			sendControlToPeer( otherId, PEER_LEFT_PREFIX + "|" + peerId + "|" + encode( name ) );
		}
		broadcastLobby();
		bumpTradeRevision();
	}

	private static void sendSessionLine( String line ) {
		if (!tradeReady()) {
			mode = Mode.ERROR;
			status = "No active trader connection.";
			bumpTradeRevision();
			return;
		}
		if (HOST_ID.equals( selfId )) {
			sendToSession( activePeerId, line );
			return;
		}
		BufferedWriter writer = sessionWriter;
		try {
			synchronized (writer) {
				writer.write( TRADE_PREFIX + "|" + activePeerId + "|" + encode( line ) );
				writer.write( "\n" );
				writer.flush();
			}
		} catch (IOException e) {
			markPeerDisconnected( connectedPeer, "Could not send trade update: " + e.getMessage() );
		}
	}

	private static void sendLegacySessionLine( String line ) {
		BufferedWriter writer = sessionWriter;
		if (writer == null) return;
		try {
			synchronized (writer) {
				writer.write( line );
				writer.write( "\n" );
				writer.flush();
			}
		} catch (IOException e) {
			markPeerDisconnected( connectedPeer, "Could not send trade update: " + e.getMessage() );
		}
	}

	private static void markPeerDisconnected( String peerName, String fallbackMessage ) {
		synchronized (LOCK) {
			String name = peerName == null || peerName.isEmpty() ? connectedPeer : peerName;
			if (name == null || name.isEmpty()) {
				PeerInfo host = lobbyPeers.get( HOST_ID );
				if (host != null) name = host.name();
			}
			if (name == null || name.isEmpty()) {
				disconnectMessage = fallbackMessage == null || fallbackMessage.isEmpty()
						? "The other trader disconnected."
						: fallbackMessage;
			} else {
				disconnectMessage = name + " has disconnected.";
			}
			postReturnNotice = "The Wayfarer Exchange host, "
					+ (name == null || name.isEmpty() ? "the host" : name)
					+ " has closed the trade.";
			disconnectPending = true;
			status = disconnectMessage;
			mode = Mode.ERROR;
			closeRequested = true;
			running = false;
			returnAllEscrowLocked();
			closeSocket( activeSocket );
			activeSocket = null;
			sessionWriter = null;
			connectedPeer = "";
			connectedPeerHeroClass = "";
			connectedPeerArmorTier = 0;
			connectedPeerProfile = new WayfarerTraderProfile();
			localPayload = new WayfarerTradePayload();
			remotePayload = new WayfarerTradePayload();
			localOffer = "";
			remoteOffer = "";
			localConfirmed = false;
			remoteConfirmed = false;
			remoteSealed = false;
			tradeFinalized = false;
			bumpTradeRevision();
		}
	}

	private static void clearDisconnectMessage() {
		synchronized (LOCK) {
			disconnectPending = false;
			disconnectMessage = "";
		}
	}

	private static void queueExchangeChatMessage( String message ) {
		synchronized (LOCK) {
			queueExchangeChatMessageLocked( message );
		}
	}

	private static void queueExchangeChatMessageLocked( String message ) {
		queueExchangeChatMessageLocked( message, false );
	}

	private static void queueExchangeChatMessageLocked( String message, boolean warning ) {
		if (message == null || message.isEmpty()) return;
		exchangeChatMessages.add( new ExchangeChatMessage( message, warning ) );
	}

	private static void clearTradeState() {
		synchronized (LOCK) {
			clearTradeStateLocked();
		}
	}

	private static void clearTradeStateLocked() {
		localPayload = new WayfarerTradePayload();
		remotePayload = new WayfarerTradePayload();
		localOffer = "";
		remoteOffer = "";
		localConfirmed = false;
		remoteConfirmed = false;
		remoteSealed = false;
		tradeFinalized = false;
		bumpTradeRevision();
	}

	private static void pushLocalPayloadLocked() {
		localOffer = localPayload.toPacket();
		localConfirmed = false;
		remoteConfirmed = false;
		remoteSealed = false;
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

	private static void saveGameAfterTrade() {
		if (GamesInProgress.curSlot < 0) return;
		try {
			Dungeon.saveGame( GamesInProgress.curSlot );
			GamesInProgress.set( GamesInProgress.curSlot );
		} catch (Exception e) {
			Game.reportException( e );
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

	private static WayfarerTraderProfile localProfile() {
		return WayfarerTraderProfile.local( localName, localHeroClass, localArmorTier );
	}

	private static WayfarerTraderProfile fallbackProfile( String name, String heroClass, int armorTier ) {
		WayfarerTraderProfile profile = new WayfarerTraderProfile();
		profile.name = clean( name );
		profile.heroClass = clean( heroClass );
		profile.armorTier = Math.max( 0, armorTier );
		return profile;
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
