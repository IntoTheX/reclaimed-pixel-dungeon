/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.network.WayfarerModerationAlerts;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerReports;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;

public class ModerationIndicator extends Tag {
	private RenderedTextBlock icon;
	private BitmapText count;
	private float colorDelay;
	private boolean red = true;

	public ModerationIndicator() {
		super( 0xCC3333 );
		setSize( SIZE, SIZE );
		visible = false;
	}

	@Override protected void createChildren() {
		super.createChildren();
		icon = PixelScene.renderTextBlock( "!", 12 );
		icon.hardlight( 0xFFFF44 );
		add( icon );
		count = new BitmapText( PixelScene.pixelFont );
		count.hardlight( 0xFFFFFF );
		add( count );
	}

	@Override protected void layout() {
		super.layout();
		float left = flipped ? x + width - SIZE : x;
		icon.setPos( left + (SIZE - icon.width()) / 2f,
				y + (SIZE - icon.height()) / 2f );
		count.x = left + SIZE - count.width() - 2;
		count.y = y + 1;
		PixelScene.align( icon );
		PixelScene.align( count );
	}

	@Override public void update() {
		WayfarerModerationAlerts.poll();
		int unread = WayfarerModerationAlerts.openReportCount();
		boolean wasVisible = visible;
		visible = unread > 0;
		if (visible) {
			count.text( unread > 99 ? "99+" : Integer.toString( unread ) );
			count.measure();
			if (!wasVisible) layout();
			if ((colorDelay -= Game.elapsed) <= 0) {
				red = !red;
				setColor( red ? 0xCC3333 : 0x3377DD );
				colorDelay = 0.45f;
			}
		}
		super.update();
	}

	@Override protected void onClick() {
		super.onClick();
		GameScene.show( new WndWayfarerReports() );
	}

	@Override protected String hoverText() { return "Unread Wayfarer incident reports"; }
}
