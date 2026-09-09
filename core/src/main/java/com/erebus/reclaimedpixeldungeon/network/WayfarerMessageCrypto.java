/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.Base64Coder;
import com.erebus.reclaimedpixeldungeon.Dungeon;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class WayfarerMessageCrypto {

	private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
	private static final String MESSAGE_PREFIX = "RPDMSG1|";

	public static final class MessagePayload {
		public final String text;
		public final String displayTime;

		private MessagePayload( String text, String displayTime ) {
			this.text = text;
			this.displayTime = displayTime;
		}
	}

	private WayfarerMessageCrypto() {}

	public static synchronized void ensureCharacterKeys() throws Exception {
		if (!Dungeon.wayfarerPublicKey().isEmpty() && !Dungeon.wayfarerPrivateKey().isEmpty()) return;
		KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );
		generator.initialize( 2048 );
		KeyPair pair = generator.generateKeyPair();
		Dungeon.wayfarerEncryptionKeys(
				String.valueOf( Base64Coder.encode( pair.getPublic().getEncoded() ) ),
				String.valueOf( Base64Coder.encode( pair.getPrivate().getEncoded() ) ) );
	}

	public static String encrypt( String message, String encodedPublicKey ) throws Exception {
		PublicKey key = KeyFactory.getInstance( "RSA" ).generatePublic(
				new X509EncodedKeySpec( Base64Coder.decode( encodedPublicKey ) ) );
		KeyGenerator generator = KeyGenerator.getInstance( "AES" );
		generator.init( 128 );
		SecretKey messageKey = generator.generateKey();
		byte[] nonce = new byte[12];
		new java.security.SecureRandom().nextBytes( nonce );
		Cipher contentCipher = Cipher.getInstance( "AES/GCM/NoPadding" );
		contentCipher.init( Cipher.ENCRYPT_MODE, messageKey, new GCMParameterSpec( 128, nonce ) );
		byte[] encryptedContent = contentCipher.doFinal(
				message.getBytes( java.nio.charset.StandardCharsets.UTF_8 ) );
		Cipher keyCipher = Cipher.getInstance( TRANSFORMATION );
		keyCipher.init( Cipher.ENCRYPT_MODE, key );
		byte[] encryptedKey = keyCipher.doFinal( messageKey.getEncoded() );
		return encode( encryptedKey ) + "." + encode( nonce ) + "." + encode( encryptedContent );
	}

	public static String decrypt( String payload ) throws Exception {
		ensureCharacterKeys();
		PrivateKey key = KeyFactory.getInstance( "RSA" ).generatePrivate(
				new PKCS8EncodedKeySpec( Base64Coder.decode( Dungeon.wayfarerPrivateKey() ) ) );
		String[] parts = payload.split( "\\.", -1 );
		if (parts.length != 3) throw new java.security.GeneralSecurityException( "Invalid message envelope" );
		Cipher keyCipher = Cipher.getInstance( TRANSFORMATION );
		keyCipher.init( Cipher.DECRYPT_MODE, key );
		SecretKeySpec messageKey = new SecretKeySpec( keyCipher.doFinal( Base64Coder.decode( parts[0] ) ), "AES" );
		Cipher contentCipher = Cipher.getInstance( "AES/GCM/NoPadding" );
		contentCipher.init( Cipher.DECRYPT_MODE, messageKey,
				new GCMParameterSpec( 128, Base64Coder.decode( parts[1] ) ) );
		return new String( contentCipher.doFinal( Base64Coder.decode( parts[2] ) ),
				java.nio.charset.StandardCharsets.UTF_8 );
	}

	public static String packMessage( String message, String senderTimestamp ) {
		return MESSAGE_PREFIX + senderTimestamp + "|" + message;
	}

	public static MessagePayload unpackMessage( String payload ) {
		if (payload != null && payload.startsWith( MESSAGE_PREFIX )) {
			int separator = payload.indexOf( '|', MESSAGE_PREFIX.length() );
			if (separator > MESSAGE_PREFIX.length()) {
				String time = payload.substring( MESSAGE_PREFIX.length(), separator );
				if (time.matches( "(?:[01][0-9]|2[0-3]):[0-5][0-9]" )
						|| time.matches( "[0-9]{4}-[0-9]{2}-[0-9]{2}T(?:[01][0-9]|2[0-3]):[0-5][0-9]" )) {
					return new MessagePayload( payload.substring( separator + 1 ), time );
				}
			}
		}
		return new MessagePayload( payload == null ? "" : payload, "" );
	}

	private static String encode( byte[] data ) {
		return String.valueOf( Base64Coder.encode( data ) );
	}
}
