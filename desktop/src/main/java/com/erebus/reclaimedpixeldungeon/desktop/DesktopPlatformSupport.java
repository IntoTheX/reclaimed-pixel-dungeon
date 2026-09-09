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

package com.erebus.reclaimedpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public class DesktopPlatformSupport extends PlatformSupport {

	@Override
	public boolean supportsLocation() {
		return System.getProperty( "os.name", "" ).toLowerCase().contains( "win" );
	}

	@Override
	public void requestApproximateLocation( LocationCallback callback ) {
		if (callback == null) return;
		if (!supportsLocation()) {
			callback.onFailure( "Automatic desktop location currently requires Windows." );
			return;
		}
		Thread locationThread = new Thread( () -> {
			try {
				String script = "Add-Type -AssemblyName System.Device; "
						+ "$w=New-Object System.Device.Location.GeoCoordinateWatcher; $w.Start(); "
						+ "$end=(Get-Date).AddSeconds(10); while ($w.Position.Location.IsUnknown -and (Get-Date) -lt $end) "
						+ "{ Start-Sleep -Milliseconds 200 }; if ($w.Position.Location.IsUnknown) { $w.Stop(); exit 2 }; "
						+ "Write-Output (($w.Position.Location.Latitude.ToString([Globalization.CultureInfo]::InvariantCulture)) "
						+ "+ '|' + ($w.Position.Location.Longitude.ToString([Globalization.CultureInfo]::InvariantCulture))); $w.Stop()";
				Process process = new ProcessBuilder( "powershell.exe", "-NoProfile", "-NonInteractive",
						"-Command", script ).redirectErrorStream( true ).start();
				if (!process.waitFor( 15, TimeUnit.SECONDS )) {
					process.destroyForcibly();
					throw new IllegalStateException();
				}
				String coordinate = null;
				try (BufferedReader reader = new BufferedReader( new InputStreamReader(
						process.getInputStream(), StandardCharsets.UTF_8 ) )) {
					String line;
					while ((line = reader.readLine()) != null) if (line.contains( "|" )) coordinate = line.trim();
				}
				int exit = process.exitValue();
				if (exit != 0 || coordinate == null) throw new IllegalStateException();
				String[] parts = coordinate.split( "\\|" );
				double latitude = Double.parseDouble( parts[0] );
				double longitude = Double.parseDouble( parts[1] );
				Gdx.app.postRunnable( () -> callback.onLocation( latitude, longitude ) );
			} catch (Exception error) {
				Gdx.app.postRunnable( () -> callback.onFailure(
						"Windows could not read your location. Enable Location Services, then try again." ) );
			}
		}, "Wayfarer Windows Location" );
		locationThread.setDaemon( true );
		locationThread.start();
	}

	//we recall previous window sizes as a workaround to not save maximized size to settings
	//have to do this as updateDisplaySize is called before maximized is set =S
	protected static Point[] previousSizes = null;

	@Override
	public void updateDisplaySize() {
		if (previousSizes == null){
			previousSizes = new Point[2];
			previousSizes[1] = SPDSettings.windowResolution();
		} else {
			previousSizes[1] = previousSizes[0];
		}
		previousSizes[0] = new Point(Game.width, Game.height);
		if (!SPDSettings.fullscreen()) {
			SPDSettings.windowResolution( previousSizes[0] );
		}
	}

	private static boolean first = true;

	@Override
	public void updateSystemUI() {
		Gdx.app.postRunnable( new Runnable() {
			@Override
			public void run () {
				if (SPDSettings.fullscreen()){
					int monitorNum = 0;
					if (!first){
						Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
						for (int i = 0; i < monitors.length; i++){
							if (((Lwjgl3Graphics.Lwjgl3Monitor)Gdx.graphics.getMonitor()).getMonitorHandle()
									== ((Lwjgl3Graphics.Lwjgl3Monitor)monitors[i]).getMonitorHandle()) {
								monitorNum = i;
							}
						}
					} else {
						monitorNum = SPDSettings.fulLScreenMonitor();
					}

					Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
					if (monitors.length <= monitorNum) {
						monitorNum = 0;
					}
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(monitors[monitorNum]));
					SPDSettings.fulLScreenMonitor(monitorNum);
				} else {
					Point p = SPDSettings.windowResolution();
					Gdx.graphics.setWindowedMode( p.x, p.y );
				}
				first = false;
			}
		} );
	}
	
	@Override
	public boolean connectedToUnmeteredNetwork() {
		return true; //no easy way to check this in desktop, just assume user doesn't care
	}

	@Override
	public boolean supportsVibration() {
		//only supports vibration via controller
		return ControllerHandler.vibrationSupported();
	}

	/* FONT SUPPORT */
	
	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;
	
	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}
		
		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());
		
		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}
	
	private static Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (asianMatcher.reset(input).find()){
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}
	
	//splits on newline (for layout), chinese/japanese (for font choice), and '_'/'**' (for highlighting)
	private Pattern regularsplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	//additionally splits on spaces, so that each word can be laid out individually
	private Pattern regularsplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularsplitterMultiline.split(text);
		} else {
			return regularsplitter.split(text);
		}
	}
}
