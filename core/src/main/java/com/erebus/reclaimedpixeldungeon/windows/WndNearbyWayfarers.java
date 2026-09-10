/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.graphics.Pixmap;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerPresenceService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerMapTileService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ScrollArea;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.ui.Component;
import com.watabou.gltextures.SmartTexture;
import com.watabou.glwrap.Texture;
import com.watabou.utils.PointF;

import java.util.ArrayList;
import java.util.Locale;

public class WndNearbyWayfarers extends Window {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 180;
	private static final int MARGIN = 4;
	private static final int GAP = 3;
	private static final int ROW_HEIGHT = 29;
	private static final int MAP_HEIGHT = 110;

	private final ScrollPane list;
	private final Component content;
	private final RenderedTextBlock status;
	private final IconButton refresh;
	private final WayfarerMap map;
	private final RedButton zoomIn;
	private final RedButton zoomOut;

	public WndNearbyWayfarers() {
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( MARGIN, MARGIN + 3 );
		add( title );

		refresh = new IconButton( Icons.get( Icons.REPEAT ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				load();
			}
		};
		refresh.setRect( WIDTH - 22, MARGIN, 18, 18 );
		add( refresh );

		status = PixelScene.renderTextBlock( 6 );
		status.maxWidth( WIDTH - 2 * MARGIN );
		status.setPos( MARGIN, refresh.bottom() + 2 );
		add( status );

		map = new WayfarerMap();
		add( map );

		content = new Component();
		list = new ScrollPane( content );
		add( list );
		zoomIn = new RedButton( "+", 8 ) {
			@Override protected void onClick() { super.onClick(); map.zoom( true ); }
		};
		zoomOut = new RedButton( "-", 8 ) {
			@Override protected void onClick() { super.onClick(); map.zoom( false ); }
		};
		zoomIn.setRect( WIDTH - MARGIN - 15, status.bottom() + GAP + 3, 13, 13 );
		zoomOut.setRect( WIDTH - MARGIN - 15, status.bottom() + GAP + 18, 13, 13 );
		add( zoomIn );
		add( zoomOut );
		resize( WIDTH, HEIGHT );
		list.setRect( 0, status.bottom() + GAP, WIDTH, HEIGHT - status.bottom() - GAP );
		load();
	}

	private void load() {
		refresh.enable( false );
		status.text( Messages.get( this, "loading" ) );
		layoutList();
		clearRows();
		WayfarerPresenceService.nearbyPlayers( (result, players) -> {
			if (parent == null || status.parent == null) return;
			refresh.enable( true );
			if (!result.success) {
				status.text( Messages.get( this, "failed", result.message ) );
				layoutList();
				return;
			}
			status.text( players.isEmpty() ? Messages.get( this, "empty" ) : result.message );
			layoutList();
			populate( players );
		} );
	}

	private void layoutList() {
		map.setRect( MARGIN, status.bottom() + GAP, WIDTH - 2 * MARGIN, MAP_HEIGHT );
		zoomIn.setRect( WIDTH - MARGIN - 15, map.top() + 3, 13, 13 );
		zoomOut.setRect( WIDTH - MARGIN - 15, map.top() + 18, 13, 13 );
		list.setRect( 0, map.bottom() + 2, WIDTH, HEIGHT - map.bottom() - 2 );
	}

	private void clearRows() {
		content.clear();
		content.setSize( list.width(), list.height() );
	}

	private void populate( ArrayList<WayfarerAccountService.NearbyPlayer> players ) {
		clearRows();
		map.players( players );
		float y = 0;
		for (WayfarerAccountService.NearbyPlayer player : players) {
			if (WayfarerChatStore.isBlocked( player.characterId )) continue;
			WayfarerChatStore.remember( player );
			if (y > 0) {
				ColorBlock divider = new ColorBlock( WIDTH - 2 * MARGIN, 1, 0xFF333333 );
				divider.x = MARGIN;
				divider.y = y;
				content.add( divider );
			}

			HeroClass heroClass = heroClass( player.heroClass );
			Image head = HeroSprite.avatar( heroClass, armorTier( player.headSprite ) );
			head.x = MARGIN;
			head.y = y + 4;
			content.add( head );

			RenderedTextBlock name = PixelScene.renderTextBlock(
					player.playerName + " - " + player.presenceStatus(), 6 );
			name.hardlight( TITLE_COLOR );
			name.maxWidth( WIDTH - 28 );
			name.setPos( 25, y + 3 );
			content.add( name );

			String details = Messages.titleCase( heroClass.title() ) + " Lv. " + player.heroLevel
					+ " - " + distance( player.distanceMeters );
			RenderedTextBlock detail = PixelScene.renderTextBlock( details, 6 );
			detail.maxWidth( WIDTH - 28 );
			detail.setPos( 25, name.bottom() + 1 );
			content.add( detail );

			if (player.interestedInTrading) {
				RenderedTextBlock trade = PixelScene.renderTextBlock( Messages.get( this, "trade" ), 6 );
				trade.hardlight( 0x66CC66 );
				trade.setPos( 25, detail.bottom() + 1 );
				content.add( trade );
			}
			Button openConversation = new Button() {
				@Override protected void onClick() {
					super.onClick();
					GameScene.show( new WndWayfarerConversation( player ) );
				}
			};
			openConversation.setRect( MARGIN, y + 1, WIDTH - 2 * MARGIN, ROW_HEIGHT - 1 );
			content.add( openConversation );
			y += ROW_HEIGHT;
		}
		content.setSize( list.width(), Math.max( list.height(), y ) );
	}

	private static HeroClass heroClass( String value ) {
		try {
			return HeroClass.valueOf( value == null ? "WARRIOR" : value.toUpperCase( Locale.ENGLISH ) );
		} catch (IllegalArgumentException ignored) {
			return HeroClass.WARRIOR;
		}
	}

	private static int armorTier( int value ) {
		return Math.max( 0, Math.min( 6, value ) );
	}

	private static String distance( int meters ) {
		if (meters < 1000) return Math.max( 100, Math.round( meters / 100f ) * 100 ) + " m away";
		return Messages.decimalFormat( meters < 10000 ? "#.0" : "#", meters / 1000f ) + " km away";
	}

	private static class WayfarerMap extends Component {
		private ArrayList<WayfarerAccountService.NearbyPlayer> players = new ArrayList<>();
		private double radiusKm = 100;
		private double panEastKm;
		private double panNorthKm;
		private Component visuals;
		private MapInput input;
		private SmartTexture mapTexture;
		private boolean tileDirty = true;
		private float tileDelay;
		private int tileGeneration;
		private double loadedPanEastKm;
		private double loadedPanNorthKm;
		private double loadedRadiusKm = 100;
		private double loadedCenterPixelX = 384;
		private double loadedCenterPixelY = 384;

		@Override
		protected void createChildren() {
			visuals = new Component();
			add( visuals );
			input = new MapInput();
			add( input );
		}

		void zoom( boolean inward ) {
			if (inward) radiusKm = Math.max( 0.1, radiusKm / 2 );
			else radiusKm = Math.min( 20050, radiusKm * 2 );
			mapChanged();
		}

		void players( ArrayList<WayfarerAccountService.NearbyPlayer> value ) {
			players = value == null ? new ArrayList<>() : value;
			double farthestKm = 0;
			for (WayfarerAccountService.NearbyPlayer player : players) {
				farthestKm = Math.max( farthestKm, player.distanceMeters / 1000.0 );
			}
			if (farthestKm > radiusKm) radiusKm = Math.min( 20050, farthestKm * 1.15 );
			mapChanged();
		}

		@Override
		protected void layout() {
			visuals.clear();
			visuals.setRect( x, y, width, height );
			input.x = x;
			input.y = y;
			input.width = width;
			input.height = height;
			ColorBlock background = new ColorBlock( width, height, 0xFF26352F );
			background.x = x;
			background.y = y;
			visuals.add( background );
			if (mapTexture != null) {
				int cropWidth = Math.max( 32, Math.min( 700,
						(int)Math.round( 512 * radiusKm / loadedRadiusKm ) ) );
				int cropHeight = Math.max( 20, Math.min( 700,
						(int)Math.round( cropWidth * height / width ) ) );
				double shiftX = (panEastKm - loadedPanEastKm) / loadedRadiusKm * 256;
				double shiftY = -(panNorthKm - loadedPanNorthKm) / loadedRadiusKm * 256;
				int sourceX = Math.max( 0, Math.min( 768 - cropWidth,
						(int)Math.round( loadedCenterPixelX + shiftX - cropWidth / 2f ) ) );
				int sourceY = Math.max( 0, Math.min( 768 - cropHeight,
						(int)Math.round( loadedCenterPixelY + shiftY - cropHeight / 2f ) ) );
				Image basemap = new Image( mapTexture, sourceX, sourceY, cropWidth, cropHeight );
				basemap.x = x;
				basemap.y = y;
				basemap.scale.set( width / basemap.width(), height / basemap.height() );
				visuals.add( basemap );
			}

			HeroClass ownClass = Dungeon.hero == null ? HeroClass.WARRIOR : Dungeon.hero.heroClass;
			addMarker( WayfarerPresenceService.publicMapLatitude(), WayfarerPresenceService.publicMapLongitude(),
					ownClass, Dungeon.hero == null ? 0 : Dungeon.hero.tier(),
					Messages.get( WndNearbyWayfarers.class, "you" ), true );
			for (WayfarerAccountService.NearbyPlayer player : players) {
				addMarker( player.mapLatitude, player.mapLongitude, heroClass( player.heroClass ),
						armorTier( player.headSprite ), player.playerName, false );
			}
			RenderedTextBlock attribution = PixelScene.renderTextBlock( "© OpenStreetMap", 5 );
			attribution.hardlight( 0xFFFFFF );
			attribution.setPos( x + width - attribution.width() - 2, y + height - attribution.height() - 1 );
			visuals.add( attribution );
		}

		@Override
		public void update() {
			super.update();
			if (tileDirty) {
				tileDelay -= com.watabou.noosa.Game.elapsed;
				if (tileDelay <= 0) requestTiles();
			}
		}

		private void mapChanged() {
			tileDirty = true;
			tileDelay = 0.12f;
			layout();
		}

		private void requestTiles() {
			tileDirty = false;
			int generation = ++tileGeneration;
			double originLat = WayfarerPresenceService.publicMapLatitude();
			double centerLat = Math.max( -85, Math.min( 85, originLat + panNorthKm / 111.0 ) );
			double longitudeScale = Math.max( 0.15, Math.cos( Math.toRadians( originLat ) ) );
			double centerLon = WayfarerPresenceService.publicMapLongitude() + panEastKm / (111.0 * longitudeScale);
			int zoom = Math.max( 2, Math.min( 19, (int)Math.round(
					Math.log( 40075.0 * longitudeScale / (radiusKm * 2.0) ) / Math.log( 2 ) ) ) );
			double requestedPanEast = panEastKm;
			double requestedPanNorth = panNorthKm;
			double requestedRadius = radiusKm;
			WayfarerMapTileService.request( centerLat, centerLon, zoom, result -> {
				if (generation != tileGeneration || result.tiles == null || parent == null) return;
				applyTiles( result, requestedPanEast, requestedPanNorth, requestedRadius );
			} );
		}

		private void applyTiles( WayfarerMapTileService.TileMosaic result, double requestedPanEast,
				double requestedPanNorth, double requestedRadius ) {
			Pixmap mosaic = new Pixmap( 768, 768, Pixmap.Format.RGBA8888 );
			try {
				for (int i = 0; i < result.tiles.length; i++) {
					Pixmap tile = new Pixmap( result.tiles[i], 0, result.tiles[i].length );
					mosaic.drawPixmap( tile, (i % 3) * 256, (i / 3) * 256 );
					tile.dispose();
				}
				if (mapTexture != null) mapTexture.delete();
				mapTexture = new SmartTexture( mosaic, Texture.LINEAR, Texture.CLAMP, false );
				loadedPanEastKm = requestedPanEast;
				loadedPanNorthKm = requestedPanNorth;
				loadedRadiusKm = requestedRadius;
				loadedCenterPixelX = result.centerPixelX;
				loadedCenterPixelY = result.centerPixelY;
				mosaic = null;
			} finally {
				if (mosaic != null) mosaic.dispose();
			}
			layout();
		}

		private void addMarker( double latitude, double longitude, HeroClass heroClass,
				int armorTier, String label, boolean self ) {
			double originLat = WayfarerPresenceService.publicMapLatitude();
			double originLon = WayfarerPresenceService.publicMapLongitude();
			double lat1 = Math.toRadians( originLat );
			double lat2 = Math.toRadians( latitude );
			double deltaLon = Math.toRadians( longitude - originLon );
			double sinLat = Math.sin( (lat2 - lat1) / 2 );
			double sinLon = Math.sin( deltaLon / 2 );
			double arc = 2 * Math.asin( Math.min( 1, Math.sqrt(
					sinLat * sinLat + Math.cos( lat1 ) * Math.cos( lat2 ) * sinLon * sinLon ) ) );
			double distanceKm = 6371.0088 * arc;
			double bearing = Math.atan2( Math.sin( deltaLon ) * Math.cos( lat2 ),
					Math.cos( lat1 ) * Math.sin( lat2 )
							- Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( deltaLon ) );
			double northKm = distanceKm * Math.cos( bearing );
			double eastKm = distanceKm * Math.sin( bearing );
			eastKm -= panEastKm;
			northKm -= panNorthKm;
			float markerX = x + width / 2f + (float)(eastKm / radiusKm * width / 2f);
			float markerY = y + height / 2f - (float)(northKm / radiusKm * height / 2f);
			markerX = Math.max( x + 6, Math.min( x + width - 6, markerX ) );
			markerY = Math.max( y + 8, Math.min( y + height - 6, markerY ) );

			Image head = HeroSprite.avatar( heroClass, armorTier );
			head.scale.set( 0.5f );
			head.x = markerX - head.width() / 4f;
			head.y = markerY - head.height() / 4f;
			if (!self) head.hardlight( 0xCCFFFF );
			visuals.add( head );

			RenderedTextBlock name = PixelScene.renderTextBlock( label, 5 );
			name.hardlight( self ? 0xFFFF66 : 0xFFFFFF );
			name.setPos( Math.max( x, Math.min( x + width - name.width(), markerX - name.width() / 2f ) ),
					markerY - 8 );
			visuals.add( name );
		}

		private class MapInput extends ScrollArea {
			private PointerEvent another;
			private boolean pinching;
			private boolean dragging;
			private float startSpan;
			private double startRadius;
			private final PointF last = new PointF();

			MapInput() { super( 0, 0, 0, 0 ); }

			@Override
			protected void onScroll( ScrollEvent event ) {
				radiusKm = Math.max( 0.1, Math.min( 20050,
						radiusKm * Math.pow( 1.35, event.amount ) ) );
				mapChanged();
			}

			@Override
			protected void onPointerDown( PointerEvent event ) {
				if (event != curEvent && another == null) {
					pinching = true;
					another = event;
					startSpan = Math.max( 1, PointF.distance( curEvent.current, another.current ) );
					startRadius = radiusKm;
					dragging = false;
				} else if (event != curEvent) {
					reset();
				}
			}

			@Override
			protected void onPointerUp( PointerEvent event ) {
				if (pinching && (event == curEvent || event == another)) {
					pinching = false;
					if (event == curEvent) curEvent = another;
					another = null;
					if (curEvent != null) last.set( curEvent.current );
					dragging = true;
				} else {
					dragging = false;
				}
			}

			@Override
			protected void onDrag( PointerEvent event ) {
				if (pinching && another != null) {
					float span = Math.max( 1, PointF.distance( curEvent.current, another.current ) );
					radiusKm = Math.max( 0.1, Math.min( 20050, startRadius * startSpan / span ) );
					mapChanged();
					return;
				}
				if (!dragging && PointF.distance( event.current, event.start ) > PixelScene.defaultZoom * 2) {
					dragging = true;
					last.set( event.current );
				} else if (dragging) {
					float dx = (event.current.x - last.x) / PixelScene.defaultZoom;
					float dy = (event.current.y - last.y) / PixelScene.defaultZoom;
					panEastKm -= dx / Math.max( 1, width ) * radiusKm * 2;
					panNorthKm += dy / Math.max( 1, height ) * radiusKm * 2;
					last.set( event.current );
					mapChanged();
				}
			}
		}
	}
}
