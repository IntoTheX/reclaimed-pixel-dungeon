/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.Base64Coder;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.SPDSettings;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class WayfarerChatStore {
	public static final String MODERATION_CONTACT_ID = "wayfarer-moderation";
	public static final String MODERATOR_SPACE_CONTACT_ID = "wayfarer-moderator-space";
	public static final class Message {
		public final String name, text, time;
		public final boolean outgoing;
		public final long order, remoteId;
		private Message( String name, String text, String time, boolean outgoing, long order ) {
			this( name, text, time, outgoing, order, 0 );
		}
		private Message( String name, String text, String time, boolean outgoing, long order, long remoteId ) {
			this.name = name; this.text = text; this.time = time;
			this.outgoing = outgoing; this.order = order; this.remoteId = remoteId;
		}
	}

	private static String loadedCharacter = "";
	private static final Map<String, WayfarerAccountService.NearbyPlayer> contacts = new LinkedHashMap<>();
	private static final Map<String, ArrayList<Message>> histories = new LinkedHashMap<>();
	private static final Map<String, Integer> unread = new LinkedHashMap<>();
	private static final Set<String> blocked = new HashSet<>();
	private static final Map<String, String> tradeSnapshots = new LinkedHashMap<>();
	private static final Map<String, String> tradePeers = new LinkedHashMap<>();
	private static final Map<String, Long> tradeOrders = new LinkedHashMap<>();
	private static final Map<String, String> actionableTradeStates = new LinkedHashMap<>();
	private static final Map<String, String> actionableTradePeers = new LinkedHashMap<>();
	private static String openConversation = "";
	private static boolean moderatorSpace;
	private static boolean moderatorSpaceInitialized;

	private WayfarerChatStore() {}

	public static synchronized void remember( WayfarerAccountService.NearbyPlayer player ) {
		ensureLoaded();
		if (player != null) contacts.put( player.characterId, player );
	}

	public static synchronized ArrayList<Message> history( String characterId ) {
		ensureLoaded();
		return new ArrayList<>( histories.computeIfAbsent( characterId, ignored -> new ArrayList<>() ) );
	}

	public static synchronized void addOutgoing( WayfarerAccountService.NearbyPlayer player,
			String name, String text, String time ) {
		remember( player );
		add( player.characterId, new Message( name, text, time, true, System.currentTimeMillis() ) );
		save();
	}

	public static synchronized void accept( ArrayList<WayfarerAccountService.WayfarerMessage> messages,
			String openCharacterId ) {
		ensureLoaded();
		if (openCharacterId == null) openCharacterId = openConversation;
		for (WayfarerAccountService.WayfarerMessage message : messages) {
			WayfarerAccountService.NearbyPlayer existing = contacts.get( message.senderCharacterId );
			contacts.put( message.senderCharacterId, new WayfarerAccountService.NearbyPlayer(
					message.senderCharacterId, message.senderName, message.senderHeroClass, 1,
					message.senderHeadSprite, message.senderPublicEncryptionKey,
					false, existing == null ? 0 : existing.distanceMeters,
					existing == null ? 0 : existing.mapLatitude, existing == null ? 0 : existing.mapLongitude,
					true, existing == null ? 0 : existing.statusSeconds ) );
			add( message.senderCharacterId, new Message( message.senderName, message.text,
					message.senderDisplayTime.isEmpty() ? localTimestamp( message.sentAt ) : message.senderDisplayTime,
					false, System.currentTimeMillis() ) );
			if (!message.senderCharacterId.equals( openCharacterId )) {
				unread.put( message.senderCharacterId, unread.getOrDefault( message.senderCharacterId, 0 ) + 1 );
			}
		}
		if (!messages.isEmpty()) save();
	}

	public static synchronized void openConversation(String characterId) {
		ensureLoaded();
		openConversation=characterId==null?"":characterId;
	}

	public static synchronized void closeConversation(String characterId) {
		if(characterId!=null && characterId.equals(openConversation))openConversation="";
	}

	public static synchronized void acceptTrades(ArrayList<WayfarerAccountService.GlobalTradeUpdate> updates) {
		ensureLoaded();
		boolean changed=false;
		actionableTradeStates.clear();
		actionableTradePeers.clear();
		for(WayfarerAccountService.GlobalTradeUpdate update:updates) {
			remember(update.peer);
			actionableTradeStates.put(update.tradeId,update.state);
			actionableTradePeers.put(update.tradeId,update.peer.characterId);
			String snapshot=update.state+"|"+update.updatedAt;
			if(!snapshot.equals(tradeSnapshots.get(update.tradeId))) {
				tradeSnapshots.put(update.tradeId,snapshot);
				tradePeers.put(update.tradeId,update.peer.characterId);
				tradeOrders.put(update.tradeId,System.currentTimeMillis());
				changed=true;
				if(update.notify && !update.peer.characterId.equals(openConversation))
					unread.put(update.peer.characterId,unread.getOrDefault(update.peer.characterId,0)+1);
			}
		}
		if(changed)save();
	}

	public static synchronized boolean hasTradeAttention() {
		ensureLoaded();
		return !actionableTradeStates.isEmpty();
	}

	public static synchronized boolean hasTradeAttention(String characterId) {
		ensureLoaded();
		return actionableTradePeers.containsValue(characterId);
	}

	public static synchronized boolean hasActiveTrade(String characterId) {
		ensureLoaded();
		for(Map.Entry<String,String> trade:actionableTradePeers.entrySet()) {
			if(characterId.equals(trade.getValue()) && activeTradeState(actionableTradeStates.get(trade.getKey())))
				return true;
		}
		return false;
	}

	public static synchronized boolean hasClaimableTrade(String characterId) {
		ensureLoaded();
		for(Map.Entry<String,String> trade:actionableTradePeers.entrySet()) {
			String state=actionableTradeStates.get(trade.getKey());
			if(characterId.equals(trade.getValue()) && ("finalized".equals(state)||"cancelled".equals(state)))
				return true;
		}
		return false;
	}

	private static boolean activeTradeState(String state) {
		return "invited".equals(state)||"responding".equals(state)||"review".equals(state);
	}

	public static synchronized void acceptModeration(
			ArrayList<WayfarerAccountService.ModerationNotification> notifications ) {
		ensureLoaded();
		if (notifications.isEmpty()) return;
		contacts.put( MODERATION_CONTACT_ID, new WayfarerAccountService.NearbyPlayer(
				MODERATION_CONTACT_ID, "Wayfarer Moderation", "WARRIOR", 1, 0, "",
				false, 0, 0, 0, true, 0 ) );
		for (WayfarerAccountService.ModerationNotification notification : notifications) {
			add( MODERATION_CONTACT_ID, new Message( notification.moderator, notification.text,
					localTimestamp( notification.createdAt ), false, System.currentTimeMillis() ) );
			unread.put( MODERATION_CONTACT_ID, unread.getOrDefault( MODERATION_CONTACT_ID, 0 ) + 1 );
		}
		save();
	}

	public static synchronized void moderatorSpaceEnabled( boolean enabled ) {
		ensureLoaded();
		moderatorSpace = enabled;
	}

	public static synchronized boolean moderatorSpaceEnabled() {
		ensureLoaded();
		return moderatorSpace;
	}

	public static synchronized void acceptModeratorSpace(
			ArrayList<WayfarerAccountService.ModeratorSpaceMessage> messages ) {
		acceptModeratorSpace( messages, true );
	}

	public static synchronized void acceptModeratorSpace(
			ArrayList<WayfarerAccountService.ModeratorSpaceMessage> messages, boolean countUnread ) {
		ensureLoaded();
		moderatorSpace = true;
		if (messages == null || messages.isEmpty()) {
			if (!moderatorSpaceInitialized) {
				moderatorSpaceInitialized = true;
				save();
			}
			return;
		}
		boolean wasInitialized = moderatorSpaceInitialized;
		boolean changed = false;
		ArrayList<Message> history = histories.computeIfAbsent(
				MODERATOR_SPACE_CONTACT_ID, ignored -> new ArrayList<>() );
		for (WayfarerAccountService.ModeratorSpaceMessage message : messages) {
			boolean duplicate = false;
			for (Message existing : history) {
				if (message.messageId > 0 && existing.remoteId == message.messageId) {
					duplicate = true;
					break;
				}
			}
			if (duplicate) continue;
			long order = serverTime( message.createdAt );
			if (order <= 0) order = System.currentTimeMillis();
			history.add( new Message( message.senderName, message.text,
					localTimestamp( message.createdAt ), message.outgoing, order, message.messageId ) );
			if (countUnread && !message.outgoing && !MODERATOR_SPACE_CONTACT_ID.equals( openConversation )) {
				unread.put( MODERATOR_SPACE_CONTACT_ID,
						unread.getOrDefault( MODERATOR_SPACE_CONTACT_ID, 0 ) + 1 );
			}
			changed = true;
		}
		moderatorSpaceInitialized = true;
		if (changed || !wasInitialized) save();
	}

	public static synchronized boolean moderatorSpaceInitialized() {
		ensureLoaded();
		return moderatorSpaceInitialized;
	}

	public static synchronized long lastModeratorMessageId() {
		ensureLoaded();
		long latest = 0;
		for (Message message : histories.getOrDefault(
				MODERATOR_SPACE_CONTACT_ID, new ArrayList<>() )) {
			latest = Math.max( latest, message.remoteId );
		}
		return latest;
	}

	public static synchronized void markRead( String characterId ) {
		ensureLoaded();
		if (unread.remove( characterId ) != null) save();
	}

	public static synchronized int unreadCount() {
		ensureLoaded();
		int total = 0;
		for (int count : unread.values()) total += count;
		return total;
	}

	public static synchronized int unreadCount( String characterId ) {
		ensureLoaded();
		return unread.getOrDefault( characterId, 0 );
	}

	public static synchronized ArrayList<WayfarerAccountService.NearbyPlayer> conversations() {
		ensureLoaded();
		ArrayList<WayfarerAccountService.NearbyPlayer> result = new ArrayList<>();
		for (Map.Entry<String, WayfarerAccountService.NearbyPlayer> contact : contacts.entrySet()) {
			if (!histories.getOrDefault( contact.getKey(), new ArrayList<>() ).isEmpty()
					|| tradePeers.containsValue(contact.getKey())) result.add( contact.getValue() );
		}
		Collections.sort( result, Comparator.comparingLong(
				(WayfarerAccountService.NearbyPlayer player) -> latest( player.characterId ) ).reversed() );
		return result;
	}

	public static synchronized String contactIds() {
		ensureLoaded();
		StringBuilder ids = new StringBuilder();
		for (String id : contacts.keySet()) {
			if (ids.length() > 0) ids.append( ',' );
			ids.append( id );
		}
		return ids.toString();
	}

	public static synchronized void updateContacts(
			ArrayList<WayfarerAccountService.NearbyPlayer> players ) {
		ensureLoaded();
		for (WayfarerAccountService.NearbyPlayer player : players) contacts.put( player.characterId, player );
		if (!players.isEmpty()) save();
	}

	public static synchronized String reportEvidence( String characterId ) {
		ensureLoaded();
		ArrayList<Message> list = histories.getOrDefault( characterId, new ArrayList<>() );
		StringBuilder evidence = new StringBuilder();
		int start = Math.max( 0, list.size() - 30 );
		for (int i = start; i < list.size(); i++) {
			Message message = list.get( i );
			String line = messageTime( message ) + " | " + (message.outgoing ? "Reporter" : "Reported player")
					+ ": " + message.text + "\n";
			if (evidence.length() + line.length() > 20000) break;
			evidence.append( line );
		}
		return evidence.toString().trim();
	}

	public static synchronized int reportEvidenceCount( String characterId ) {
		ensureLoaded();
		return Math.min( 30, histories.getOrDefault( characterId, new ArrayList<>() ).size() );
	}

	public static synchronized boolean isBlocked( String characterId ) {
		ensureLoaded();
		return blocked.contains( characterId );
	}

	public static synchronized void setBlocked( String characterId, boolean value ) {
		ensureLoaded();
		if (value) blocked.add( characterId );
		else blocked.remove( characterId );
		save();
	}

	private static void add( String id, Message message ) {
		ArrayList<Message> list = histories.computeIfAbsent( id, ignored -> new ArrayList<>() );
		list.add( message );
	}

	private static long latest( String id ) {
		ArrayList<Message> list = histories.get( id );
		long result=list == null || list.isEmpty() ? 0 : list.get( list.size() - 1 ).order;
		for(Map.Entry<String,String> trade:tradePeers.entrySet()) if(id.equals(trade.getValue()))
			result=Math.max(result,tradeOrders.getOrDefault(trade.getKey(),0L));
		return result;
	}

	private static void ensureLoaded() {
		String current = Dungeon.wayfarerCharacterId();
		if (current.equals( loadedCharacter )) return;
		loadedCharacter = current;
		contacts.clear(); histories.clear(); unread.clear(); blocked.clear();
		tradeSnapshots.clear(); tradePeers.clear(); tradeOrders.clear();
		actionableTradeStates.clear(); actionableTradePeers.clear(); openConversation="";
		moderatorSpace = WayfarerAccountService.isModeratorAccount();
		moderatorSpaceInitialized = false;
		String data = SPDSettings.wayfarerChatHistory( current );
		for (String line : data.split( "\\n" )) {
			try {
				String[] p = line.split( "\\|", -1 );
				if ((p.length == 11 || p.length == 13) && "C".equals( p[0] )) {
					contacts.put( dec( p[1] ), new WayfarerAccountService.NearbyPlayer(
							dec( p[1] ), dec( p[2] ), dec( p[3] ), Integer.parseInt( p[4] ),
							Integer.parseInt( p[5] ), dec( p[6] ), Boolean.parseBoolean( p[7] ),
							Integer.parseInt( p[8] ), Double.parseDouble( p[9] ), Double.parseDouble( p[10] ),
							p.length != 11 && Boolean.parseBoolean( p[11] ),
							p.length == 11 ? 0 : Long.parseLong( p[12] ) ) );
				} else if ((p.length == 7 || p.length == 8) && "M".equals( p[0] )) {
					add( dec( p[1] ), new Message( dec( p[2] ), dec( p[3] ), dec( p[4] ),
							Boolean.parseBoolean( p[5] ), Long.parseLong( p[6] ),
							p.length == 8 ? Long.parseLong( p[7] ) : 0 ) );
				} else if (p.length == 3 && "U".equals( p[0] )) unread.put( dec( p[1] ), Integer.parseInt( p[2] ) );
				else if (p.length == 2 && "B".equals( p[0] )) blocked.add( dec( p[1] ) );
				else if (p.length == 2 && "G".equals( p[0] )) moderatorSpaceInitialized = Boolean.parseBoolean( p[1] );
				else if (p.length == 5 && "T".equals(p[0])) {
					tradeSnapshots.put(dec(p[1]),dec(p[3]));
					tradePeers.put(dec(p[1]),dec(p[2]));
					tradeOrders.put(dec(p[1]),Long.parseLong(p[4]));
				}
			} catch (Exception ignored) {}
		}
	}

	private static void save() {
		StringBuilder out = new StringBuilder();
		for (WayfarerAccountService.NearbyPlayer p : contacts.values()) {
			out.append( "C|" ).append( enc( p.characterId ) ).append( '|' ).append( enc( p.playerName ) )
					.append( '|' ).append( enc( p.heroClass ) ).append( '|' ).append( p.heroLevel )
					.append( '|' ).append( p.headSprite ).append( '|' ).append( enc( p.publicEncryptionKey ) )
					.append( '|' ).append( p.interestedInTrading ).append( '|' ).append( p.distanceMeters )
					.append( '|' ).append( p.mapLatitude ).append( '|' ).append( p.mapLongitude )
					.append( '|' ).append( p.online ).append( '|' ).append( p.statusSeconds ).append( '\n' );
		}
		for (Map.Entry<String, ArrayList<Message>> history : histories.entrySet()) for (Message m : history.getValue()) {
			out.append( "M|" ).append( enc( history.getKey() ) ).append( '|' ).append( enc( m.name ) )
					.append( '|' ).append( enc( m.text ) ).append( '|' ).append( enc( m.time ) )
					.append( '|' ).append( m.outgoing ).append( '|' ).append( m.order )
					.append( '|' ).append( m.remoteId ).append( '\n' );
		}
		for (Map.Entry<String, Integer> value : unread.entrySet())
			out.append( "U|" ).append( enc( value.getKey() ) ).append( '|' ).append( value.getValue() ).append( '\n' );
		for (String id : blocked) out.append( "B|" ).append( enc( id ) ).append( '\n' );
		for(String id:tradeSnapshots.keySet()) out.append("T|").append(enc(id)).append('|')
				.append(enc(tradePeers.get(id))).append('|').append(enc(tradeSnapshots.get(id))).append('|')
				.append(tradeOrders.getOrDefault(id,0L)).append('\n');
		if (moderatorSpaceInitialized) out.append( "G|true\n" );
		SPDSettings.wayfarerChatHistory( loadedCharacter, out.toString() );
	}

	private static String enc( String value ) {
		return String.valueOf( Base64Coder.encode( (value == null ? "" : value).getBytes( StandardCharsets.UTF_8 ) ) );
	}
	private static String dec( String value ) {
		return new String( Base64Coder.decode( value ), StandardCharsets.UTF_8 );
	}
	public static String messageTime( Message message ) {
		String value = message == null ? "" : message.time;
		try {
			if (!value.matches( "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}" )) return value;
			Date date = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm", Locale.ROOT ).parse( value );
			String today = new SimpleDateFormat( "yyyy-MM-dd", Locale.ROOT ).format( new Date() );
			if (value.substring( 0, 10 ).equals( today )) return value.substring( 11 );
			String currentYear = today.substring( 0, 4 );
			String pattern = value.startsWith( currentYear ) ? "MMM d HH:mm" : "MMM d, yyyy HH:mm";
			return new SimpleDateFormat( pattern, Locale.ENGLISH ).format( date );
		} catch (Exception ignored) {
			return value;
		}
	}

	private static String localTimestamp( String value ) {
		long sentAt = serverTime( value );
		if (sentAt > 0) return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm", Locale.ROOT ).format( new Date( sentAt ) );
		return value != null && value.length() >= 16 ? value.substring( 11, 16 ) : "";
	}

	private static long serverTime( String value ) {
		try {
			if (value == null || value.length() < 19) return 0;
			String base = value.substring( 0, 19 );
			String fraction = "000";
			String zone = "+0000";
			int dot = value.indexOf( '.', 19 );
			int zoneAt = value.endsWith( "Z" ) ? value.length() - 1 : value.lastIndexOf( '+' );
			if (zoneAt < 19) zoneAt = value.lastIndexOf( '-' );
			if (dot >= 0) {
				int end = zoneAt > dot ? zoneAt : value.length();
				String raw = value.substring( dot + 1, end );
				fraction = (raw + "000").substring( 0, 3 );
			}
			if (zoneAt >= 19 && !value.endsWith( "Z" )) zone = value.substring( zoneAt ).replace( ":", "" );
			return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT )
					.parse( base + "." + fraction + zone ).getTime();
		} catch (Exception ignored) {
			return 0;
		}
	}
}
