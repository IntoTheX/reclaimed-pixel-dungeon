/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerModeratorRewards;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndWayfarerChats extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private final Component rows = new Component();
	private final float rowsTop;

	public WndWayfarerChats() {
		RenderedTextBlock title = PixelScene.renderTextBlock( "Chats", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 4 );
		add( title );
		rowsTop = title.bottom() + 6;
		rows.setRect( 0, 0, WIDTH, 1 );
		add( rows );
		rebuild( WayfarerChatStore.moderatorSpaceEnabled() );
		WayfarerAccountService.moderatorStatus( (result, moderator) -> {
			if (parent == null || !result.success) return;
			WayfarerChatStore.moderatorSpaceEnabled( moderator );
			rebuild( moderator );
		} );
	}

	private void rebuild( boolean moderator ) {
		rows.clear();
		ArrayList<WayfarerAccountService.NearbyPlayer> contacts =
				WayfarerChatStore.conversations();
		float y = rowsTop;
		if (moderator) {
			RedButton room = conversationButton( "Moderator Space",
					WayfarerChatStore.MODERATOR_SPACE_CONTACT_ID,
					() -> GameScene.show( new WndWayfarerModeratorSpace() ) );
			room.setRect( 3, y, WIDTH - 6, 18 );
			rows.add( room );
			y = room.bottom() + 4;
			ColorBlock divider = new ColorBlock( WIDTH - 8, 1, 0xFF777777 );
			divider.x = 4;
			divider.y = y;
			rows.add( divider );
			y += 5;
		}
		if (contacts.isEmpty() && !moderator) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					"No previous Wayfarer conversations on this device.", 6 );
			empty.maxWidth( WIDTH - 8 );
			empty.setPos( 4, y );
			rows.add( empty );
			y = empty.bottom() + 5;
		} else {
			for (WayfarerAccountService.NearbyPlayer player : contacts) {
				boolean moderation = WayfarerChatStore.MODERATION_CONTACT_ID.equals( player.characterId );
				String label = moderation ? player.playerName : player.playerName + " - " + player.presenceStatus();
				RedButton row = conversationButton( label, player.characterId,
						() -> GameScene.show( new WndWayfarerConversation( player ) ) );
				row.setRect( 3, y, WIDTH - 6, 18 );
				rows.add( row );
				y = row.bottom() + 3;
			}
		}
		resize( WIDTH, (int)Math.min( 180, y + 3 ) );
	}

	private RedButton conversationButton( String label, String characterId, Runnable action ) {
		return new RedButton( label, 7 ) {
			private float blinkDelay;
			private boolean bright;

			@Override public void update() {
				boolean attention = WayfarerChatStore.unreadCount( characterId ) > 0
						|| WayfarerChatStore.hasTradeAttention( characterId )
						|| (WayfarerChatStore.MODERATOR_SPACE_CONTACT_ID.equals( characterId )
						&& WayfarerModeratorRewards.hasClaimableReward());
				if (attention && (blinkDelay -= Game.elapsed) <= 0) {
					bright = !bright;
					if (bright) {
						bg.hardlight( 1f, 0.35f, 0.35f );
						text.hardlight( 0xFFFF44 );
					} else {
						bg.hardlight( 0.65f, 0.25f, 1f );
						text.resetColor();
					}
					blinkDelay = 0.45f;
				} else if (!attention && (bright || blinkDelay != 0)) {
					bright = false;
					blinkDelay = 0;
					bg.resetColor();
					text.resetColor();
				}
				super.update();
			}

			@Override protected void onClick() {
				super.onClick();
				action.run();
			}
		};
	}

}
