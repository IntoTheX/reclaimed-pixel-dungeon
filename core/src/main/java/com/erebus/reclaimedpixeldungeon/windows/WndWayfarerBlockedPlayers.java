/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Locale;

public class WndWayfarerBlockedPlayers extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 170;
	private final Component rows = new Component();
	private final ScrollPane pane = new ScrollPane( rows );
	private final RenderedTextBlock message = PixelScene.renderTextBlock( 6 );
	private final ArrayList<RedButton> buttons = new ArrayList<>();

	public WndWayfarerBlockedPlayers() {
		RenderedTextBlock title = PixelScene.renderTextBlock( "Blocked Players", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 4 );
		add( title );
		message.maxWidth( WIDTH - 8 );
		message.setPos( 4, title.bottom() + 5 );
		add( message );
		add( pane );
		resize( WIDTH, 40 );
		pane.visible = false;
		load();
	}

	private void load() {
		message.visible = true;
		message.text( "Loading blocked players..." );
		WayfarerAccountService.blockedPlayers( (result, players) -> {
			if (parent == null) return;
			clearButtons();
			if (!result.success || players.isEmpty()) {
				pane.visible = false;
				message.visible = true;
				message.text( result.success ? "You have not blocked any players." : result.message );
				resize( WIDTH, (int)Math.max( 40, message.bottom() + 5 ) );
				return;
			}
			message.visible = false;
			float y = 0;
			for (WayfarerAccountService.NearbyPlayer player : players) {
				String identity = Messages.titleCase( heroClass( player.heroClass ).title() )
						+ " Lv. " + player.heroLevel;
				RedButton row = new RedButton( player.playerName + " - " + identity, 6 ) {
					@Override protected void onClick() {
						super.onClick();
						confirmUnblock( player );
					}
				};
				row.icon( HeroSprite.avatar( heroClass( player.heroClass ),
						Math.max( 0, Math.min( 6, player.headSprite ) ) ) );
				row.setRect( 3, y, WIDTH - 6, 22 );
				rows.add( row );
				buttons.add( row );
				y = row.bottom() + 3;
			}
			pane.visible = true;
			resize( WIDTH, HEIGHT );
			pane.setRect( 0, titleBottom(), WIDTH, HEIGHT - titleBottom() - 3 );
			rows.setSize( WIDTH, Math.max( pane.height(), y ) );
			pane.scrollTo( 0, 0 );
		} );
	}

	private float titleBottom() {
		return message.top();
	}

	private void confirmUnblock( WayfarerAccountService.NearbyPlayer player ) {
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Unblock " + player.playerName,
				"This character may discover and exchange messages with you again.", "Unblock", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index != 0) return;
				WayfarerAccountService.blockPlayer( player, false, result -> {
					if (WndWayfarerBlockedPlayers.this.parent == null) return;
					if (result.success) {
						WayfarerChatStore.setBlocked( player.characterId, false );
						load();
					}
					GameScene.show( new WndOptions( Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
							result.success ? "Player Unblocked" : "Unblock Failed", result.message, "Close" ) );
				} );
			}
		} );
	}

	private void clearButtons() {
		for (RedButton button : buttons) {
			rows.remove( button );
			button.destroy();
		}
		buttons.clear();
	}

	private static HeroClass heroClass( String value ) {
		try {
			return HeroClass.valueOf( value == null ? "WARRIOR" : value.toUpperCase( Locale.ENGLISH ) );
		} catch (Exception ignored) {
			return HeroClass.WARRIOR;
		}
	}
}
