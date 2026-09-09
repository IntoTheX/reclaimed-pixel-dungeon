/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public final class WayfarerMapTileService {

	private static final long CACHE_MILLIS = 7L * 24 * 60 * 60 * 1000;
	private static final String USER_AGENT = "ReclaimedPixelDungeon/0.2.4 (reclaimedpixeldungeon.auth@gmail.com)";
	private static final AtomicLong latestRequest = new AtomicLong();
	private static final ExecutorService worker = Executors.newSingleThreadExecutor( runnable -> {
		Thread thread = new Thread( runnable, "Wayfarer Map Tiles" );
		thread.setDaemon( true );
		return thread;
	} );

	private WayfarerMapTileService() {}

	public interface CallbackResult {
		void completed( TileMosaic result );
	}

	public static final class TileMosaic {
		public final byte[][] tiles;
		public final double centerPixelX;
		public final double centerPixelY;
		public final String error;

		private TileMosaic( byte[][] tiles, double centerPixelX, double centerPixelY, String error ) {
			this.tiles = tiles;
			this.centerPixelX = centerPixelX;
			this.centerPixelY = centerPixelY;
			this.error = error;
		}
	}

	public static void request( double latitude, double longitude, int zoom, CallbackResult callback ) {
		long requestId = latestRequest.incrementAndGet();
		worker.execute( () -> {
			TileMosaic result;
			try {
				double scale = Math.scalb( 1.0, zoom );
				double tileX = (longitude + 180.0) / 360.0 * scale;
				double sinLat = Math.sin( Math.toRadians( Math.max( -85.0511, Math.min( 85.0511, latitude ) ) ) );
				double tileY = (0.5 - Math.log( (1 + sinLat) / (1 - sinLat) ) / (4 * Math.PI)) * scale;
				int centerX = (int)Math.floor( tileX );
				int centerY = (int)Math.floor( tileY );
				byte[][] tiles = new byte[9][];
				int index = 0;
				for (int dy = -1; dy <= 1; dy++) {
					for (int dx = -1; dx <= 1; dx++) {
						if (requestId != latestRequest.get()) return;
						int count = 1 << zoom;
						int x = ((centerX + dx) % count + count) % count;
						int y = Math.max( 0, Math.min( count - 1, centerY + dy ) );
						tiles[index++] = tile( zoom, x, y );
					}
				}
				result = new TileMosaic( tiles, 256 + (tileX - centerX) * 256,
						256 + (tileY - centerY) * 256, null );
			} catch (Exception error) {
				result = new TileMosaic( null, 0, 0,
						"Map tiles could not be downloaded. Check the connection and try again." );
			}
			TileMosaic delivered = result;
			Game.runOnRenderThread( new Callback() {
				@Override public void call() {
					if (requestId == latestRequest.get() && callback != null) callback.completed( delivered );
				}
			} );
		} );
	}

	private static byte[] tile( int zoom, int x, int y ) throws Exception {
		FileHandle cache = Gdx.files.local( "wayfarer-map-cache/" + zoom + "/" + x + "/" + y + ".png" );
		if (cache.exists() && System.currentTimeMillis() - cache.lastModified() < CACHE_MILLIS) {
			return cache.readBytes();
		}
		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(
					"https://tile.openstreetmap.org/" + zoom + "/" + x + "/" + y + ".png" ).openConnection();
			connection.setConnectTimeout( 10_000 );
			connection.setReadTimeout( 15_000 );
			connection.setRequestProperty( "User-Agent", USER_AGENT );
			connection.setRequestProperty( "Accept", "image/png" );
			int status = connection.getResponseCode();
			if (status < 200 || status >= 300) throw new IllegalStateException( "HTTP " + status );
			byte[] bytes;
			try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = input.read( buffer )) >= 0) output.write( buffer, 0, read );
				bytes = output.toByteArray();
			}
			connection.disconnect();
			cache.parent().mkdirs();
			cache.writeBytes( bytes, false );
			return bytes;
		} catch (Exception error) {
			if (cache.exists()) return cache.readBytes();
			throw error;
		}
	}
}
