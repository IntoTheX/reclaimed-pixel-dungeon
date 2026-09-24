/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Encrypted chat attachment containing a durable Marketplace listing preview. */
public final class WayfarerMarketplaceReference {

	private static final String PREFIX = "RPDMARKET1|";
	private static final int MAX_MESSAGE_LENGTH = 8_000;
	private static final int MAX_PACKET_LENGTH = 262_144;

	public static final class Reference {
		public final String listingId;
		public final String sellerName;
		public final WayfarerTradePayload offer;
		public final WayfarerTradePayload requested;

		private Reference( String listingId, String sellerName, WayfarerTradePayload offer,
				WayfarerTradePayload requested ) {
			this.listingId = listingId;
			this.sellerName = sellerName;
			this.offer = offer;
			this.requested = requested;
		}
	}

	private WayfarerMarketplaceReference() {}

	public static String encode( JsonValue listing ) throws IOException {
		if (listing == null) throw new IOException( "This Marketplace listing is unavailable." );
		String listingId = listing.getString( "listing_id", "" );
		String sellerName = listing.getString( "seller_name", "Wayfarer" );
		String offer = listing.getString( "seller_offer", "" );
		String requested = listing.getString( "requested_offer", "" );
		if (listingId.isEmpty() || listingId.length() > 64 || offer.isEmpty()) {
			throw new IOException( "This Marketplace listing is incomplete." );
		}
		WayfarerGlobalTrade.decode( offer );
		if (!requested.isEmpty()) WayfarerGlobalTrade.decode( requested );
		String result = PREFIX + listingId + "|" + plain( sellerName ) + "|"
				+ compressed( offer ) + "|" + (requested.isEmpty() ? "" : compressed( requested ));
		if (result.length() > MAX_MESSAGE_LENGTH) {
			throw new IOException( "This listing is too detailed to attach to chat." );
		}
		return result;
	}

	public static Reference decode( String message ) {
		if (message == null || !message.startsWith( PREFIX ) || message.length() > MAX_MESSAGE_LENGTH) return null;
		try {
			String[] fields = message.split( "\\|", -1 );
			if (fields.length != 5 || fields[1].isEmpty() || fields[1].length() > 64) return null;
			String sellerName = unplain( fields[2] );
			String offerPacket = uncompressed( fields[3] );
			String requestPacket = fields[4].isEmpty() ? "" : uncompressed( fields[4] );
			WayfarerTradePayload offer = WayfarerGlobalTrade.decode( offerPacket );
			WayfarerTradePayload requested = requestPacket.isEmpty()
					? null : WayfarerGlobalTrade.decode( requestPacket );
			return new Reference( fields[1], sellerName.isEmpty() ? "Wayfarer" : sellerName,
					offer, requested );
		} catch (Exception ignored) {
			return null;
		}
	}

	public static int maximumMessageLength() {
		return MAX_MESSAGE_LENGTH;
	}

	public static String evidenceText( String message ) {
		Reference reference = decode( message );
		return reference == null ? message
				: "Marketplace listing reference: " + reference.sellerName + "'s offer.";
	}

	private static String compressed( String value ) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream( bytes )) {
			gzip.write( value.getBytes( StandardCharsets.UTF_8 ) );
		}
		return String.valueOf( Base64Coder.encode( bytes.toByteArray() ) );
	}

	private static String uncompressed( String value ) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (GZIPInputStream gzip = new GZIPInputStream(
				new ByteArrayInputStream( Base64Coder.decode( value ) ) )) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = gzip.read( buffer )) >= 0) {
				if (output.size() + read > MAX_PACKET_LENGTH) throw new IOException( "Listing data is too large." );
				output.write( buffer, 0, read );
			}
		}
		return new String( output.toByteArray(), StandardCharsets.UTF_8 );
	}

	private static String plain( String value ) {
		return String.valueOf( Base64Coder.encode(
				(value == null ? "" : value).getBytes( StandardCharsets.UTF_8 ) ) );
	}

	private static String unplain( String value ) {
		return new String( Base64Coder.decode( value ), StandardCharsets.UTF_8 );
	}
}
