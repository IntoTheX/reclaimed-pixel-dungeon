/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerModeratorRewards;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndWayfarerModeratorSpace extends Window {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 180;
	private static final String CONTACT_ID = WayfarerChatStore.MODERATOR_SPACE_CONTACT_ID;

	private final Component content = new Component();
	private final ScrollPane pane = new ScrollPane( content );
	private boolean receiving;
	private float pollDelay;
	private int shownMessageCount;
	private final IconButton rewardButton;

	public WndWayfarerModeratorSpace() {
		RenderedTextBlock title = PixelScene.renderTextBlock( "Moderator Space", 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH - 78 );
		title.setPos( (WIDTH - 72 - title.width()) / 2f, 7 );
		add( title );

		IconButton info = new IconButton( fitHeaderIcon( Icons.get( Icons.INFO ) ) ) {
			@Override protected void onClick() { GameScene.show( new WndModeratorGuidelines() ); }
		};
		info.setRect( WIDTH - 25, 2, 22, 22 );
		add( info );

		rewardButton = new IconButton( fitHeaderIcon( new ItemSprite( ItemSpriteSheet.LOCKED_CHEST ) ) ) {
			@Override protected void onClick() { WndModeratorRewards.open(); }
		};
		rewardButton.setRect( WIDTH - 73, 2, 22, 22 );
		rewardButton.visible = false;
		add( rewardButton );

		IconButton service = new IconButton( fitHeaderIcon( Icons.get( Icons.CALENDAR ) ) ) {
			@Override protected void onClick() { WndModeratorRewards.openServiceRecord(); }
		};
		service.setRect( WIDTH - 49, 2, 22, 22 );
		add( service );

		ColorBlock divider = new ColorBlock( WIDTH - 6, 1, 0xFF777777 );
		divider.x = 3;
		divider.y = Math.max( title.bottom() + 4, 26 );
		add( divider );

		pane.setRect( 0, divider.y + 4, WIDTH, HEIGHT - divider.y - 25 );
		add( pane );

		RedButton send = new RedButton( "Message", 7 ) {
			@Override protected void onClick() {
				super.onClick();
				compose();
			}
		};
		send.setRect( 3, HEIGHT - 19, WIDTH - 6, 16 );
		add( send );

		resize( WIDTH, HEIGHT );
		WayfarerChatStore.openConversation( CONTACT_ID );
		WayfarerChatStore.markRead( CONTACT_ID );
		rebuild( true );
		receive();
		WayfarerModeratorRewards.refreshStatus( (result, status) -> {
			if (parent == null || !result.success) return;
			rewardButton.visible = status != null && status.hasClaimableReward();
			rebuild( false );
		} );
	}

	private static com.watabou.noosa.Image fitHeaderIcon( com.watabou.noosa.Image icon ) {
		if (icon != null && icon.width > 0 && icon.height > 0) {
			float scale = Math.min( 1f, Math.min( 16f / icon.width, 16f / icon.height ) );
			icon.scale.set( scale );
		}
		return icon;
	}

	@Override public void destroy() {
		WayfarerChatStore.closeConversation( CONTACT_ID );
		super.destroy();
	}

	@Override public void update() {
		super.update();
		rewardButton.visible = WayfarerModeratorRewards.hasClaimableReward();
		if (rewardButton.visible && rewardButton.icon() != null) {
			rewardButton.icon().alpha( 0.45f + 0.55f * Math.abs(
					(float)Math.sin( com.watabou.noosa.Game.timeTotal * 4f ) ) );
		}
		int messageCount = history().size();
		if (messageCount != shownMessageCount) {
			boolean followNewest = pane.isAtBottom( 3 );
			WayfarerChatStore.markRead( CONTACT_ID );
			rebuild( followNewest );
		}
		pollDelay -= com.watabou.noosa.Game.elapsed;
		if (pollDelay <= 0 && !receiving) receive();
	}

	private void compose() {
		GameScene.show( new WndTextInput( "Moderator Space", "Message the moderator group.",
				"", 500, true, "Send", "Cancel" ) {
			@Override public void onSelect( boolean positive, String text ) {
				if (!positive || text == null || text.trim().isEmpty()) return;
				WayfarerAccountService.sendModeratorSpaceMessage( text, (result, message) -> {
					if (result.success) WayfarerModeratorRewards.recordActivity();
					if (WndWayfarerModeratorSpace.this.parent == null || !result.success || message == null) return;
					ArrayList<WayfarerAccountService.ModeratorSpaceMessage> delivered = new ArrayList<>();
					delivered.add( message );
					WayfarerChatStore.acceptModeratorSpace( delivered );
					WayfarerChatStore.markRead( CONTACT_ID );
					rebuild( true );
				} );
			}
		} );
	}

	private void receive() {
		receiving = true;
		pollDelay = 2f;
		WayfarerAccountService.receiveModeratorSpaceMessages(
				WayfarerChatStore.lastModeratorMessageId(), (result, messages) -> {
			receiving = false;
			if (parent == null || !result.success) return;
			boolean followNewest = pane.isAtBottom( 3 );
			WayfarerChatStore.acceptModeratorSpace( messages );
			WayfarerChatStore.markRead( CONTACT_ID );
			if (!messages.isEmpty()) rebuild( followNewest );
		} );
	}

	private ArrayList<WayfarerChatStore.Message> history() {
		return WayfarerChatStore.history( CONTACT_ID );
	}

	private void rebuild( boolean followNewest ) {
		float previousScroll = pane.scrollY();
		content.clear();
		float y = 3;
		WayfarerAccountService.ModeratorRewardStatus rewards = WayfarerModeratorRewards.cachedStatus();
		if (rewards != null && rewards.welcomeAvailable) {
			RenderedTextBlock welcome = PixelScene.renderTextBlock(
					"Welcome to the moderator team. Your one-time welcome bonus is ready in the golden chest above.", 6 );
			welcome.hardlight( 0xFFFF44 );
			welcome.maxWidth( WIDTH - 8 );
			welcome.setPos( 4, y );
			content.add( welcome );
			y = welcome.bottom() + 8;
		}
		for (WayfarerChatStore.Message message : history()) y = messageBubble( message, y );
		content.setSize( pane.width(), Math.max( pane.height(), y ) );
		shownMessageCount = history().size();
		pane.scrollTo( 0, followNewest ? Math.max( 0, y - pane.height() ) : previousScroll );
	}

	private float messageBubble( WayfarerChatStore.Message message, float y ) {
		RenderedTextBlock heading = PixelScene.renderTextBlock(
				message.name + "  " + WayfarerChatStore.messageTime( message ), 6 );
		heading.hardlight( message.outgoing ? 0x66CCFF : TITLE_COLOR );
		heading.maxWidth( WIDTH - 8 );
		float headingX = message.outgoing ? WIDTH - 3 - heading.width() : 3;
		heading.setPos( Math.max( 3, headingX ), y );
		content.add( heading );

		RenderedTextBlock body = PixelScene.renderTextBlock( message.text, 6 );
		body.maxWidth( (int)(WIDTH * 0.72f) );
		float bodyX = message.outgoing ? WIDTH - 6 - body.width() : 6;
		body.setPos( bodyX, heading.bottom() + 5 );
		ColorBlock bubble = new ColorBlock( body.width() + 6, body.height() + 6,
				message.outgoing ? 0xFF454545 : 0xFF245A73 );
		bubble.x = bodyX - 3;
		bubble.y = body.top() - 3;
		content.add( bubble );
		content.add( body );
		return Math.max( body.bottom(), bubble.y + bubble.height ) + 9;
	}
}
