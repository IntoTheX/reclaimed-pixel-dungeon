/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.rewards.GameplayRewards;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.windows.WndGameplayRewards;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;

public class GameplayRewardIndicator extends Tag {
	private ItemSprite icon;
	private BitmapText count;
	private int lastPending;
	private float blinkDelay;
	private boolean bright;

	public GameplayRewardIndicator() {
		super( 0x9A6A16 );
		setSize( SIZE, SIZE );
		visible = false;
	}

	@Override protected void createChildren() {
		super.createChildren();
		icon = new ItemSprite( ItemSpriteSheet.LOCKED_CHEST );
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
		GameplayRewards.poll();
		refreshPending();
		if (visible && (blinkDelay -= Game.elapsed) <= 0) {
			bright = !bright;
			setColor( bright ? 0xE2B126 : 0x9A6A16 );
			icon.brightness( bright ? 1.65f : 1f );
			blinkDelay = 0.45f;
		}
		super.update();
	}

	public void refreshPending() {
		int pending = GameplayRewards.pendingRewards();
		if (pending != lastPending) {
			if (pending > lastPending) flash();
			lastPending = pending;
			count.text( pending > 99 ? "99+" : pending > 1 ? Integer.toString( pending ) : "" );
			count.measure();
			visible = pending > 0;
			if (visible) blinkDelay = 0;
			layout();
		}
	}

	@Override protected void onClick() {
		super.onClick();
		if (visible) WndGameplayRewards.open();
	}

	@Override protected String hoverText() {
		return "Claim active play rewards";
	}
}
