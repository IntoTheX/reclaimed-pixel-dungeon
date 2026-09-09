/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerModeratorRewards;
import com.erebus.reclaimedpixeldungeon.network.WayfarerPresenceService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerChats;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

public class ChatIndicator extends Tag {
	private Image icon;
	private BitmapText count;
	private int lastUnread;
	private boolean lastTradeAttention;
	private boolean lastRewardAttention;
	private float blinkDelay;
	private boolean bright;

	public ChatIndicator() {
		super( 0x7A2E9B );
		setSize( SIZE, SIZE );
		visible = false;
	}

	@Override protected void createChildren() {
		super.createChildren();
		icon = Icons.get( Icons.INFO );
		add( icon );
		count = new BitmapText( PixelScene.pixelFont );
		count.hardlight( 0xFFFFFFFF );
		add( count );
	}

	@Override protected void layout() {
		super.layout();
		float contentLeft = flipped ? x + width - SIZE : x;
		icon.x = contentLeft + (SIZE - icon.width()) / 2f;
		icon.y = y + (height - icon.height()) / 2f;
		count.x = contentLeft + SIZE - count.width() - 2;
		count.y = y + 1;
		PixelScene.align( icon );
		PixelScene.align( count );
	}

	@Override public void update() {
		WayfarerPresenceService.pollModerationNotifications();
		refreshUnread();
		if (visible && (blinkDelay -= Game.elapsed) <= 0) {
			bright = !bright;
			setColor( bright ? 0xC04FE0 : 0x7A2E9B );
			blinkDelay = 0.45f;
		}
		super.update();
	}

	public void refreshUnread() {
		int unread = WayfarerChatStore.unreadCount();
		boolean tradeAttention = WayfarerChatStore.hasTradeAttention();
		boolean rewardAttention = WayfarerModeratorRewards.hasClaimableReward();
		if (unread != lastUnread || tradeAttention != lastTradeAttention
				|| rewardAttention != lastRewardAttention) {
			if (unread > lastUnread || rewardAttention && !lastRewardAttention) flash();
			lastUnread = unread;
			lastTradeAttention = tradeAttention;
			lastRewardAttention = rewardAttention;
			count.text( unread > 99 ? "99+" : Integer.toString( unread ) );
			if (unread == 0) count.text( "" );
			count.measure();
			visible = unread > 0 || tradeAttention || rewardAttention;
			if (visible) blinkDelay = 0;
			layout();
		}
	}

	@Override protected void onClick() {
		super.onClick();
		if (visible) GameScene.show( new WndWayfarerChats() );
	}

	@Override protected String hoverText() {
		return "Wayfarer chats, active trades, and claimable moderator rewards";
	}
}
