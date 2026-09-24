/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplace;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.windows.WndWayfarerMarketplace;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;

public class MarketplaceIndicator extends Tag {
    private ItemSprite icon;
    private BitmapText count;
    private int lastCount;
    private float blinkDelay;
    private boolean bright;

    public MarketplaceIndicator() { super(0xB85A16); setSize(SIZE, SIZE); visible = false; }

    @Override protected void createChildren() {
        super.createChildren(); icon = new ItemSprite(ItemSpriteSheet.LOCKED_CHEST); add(icon);
        count = new BitmapText(PixelScene.pixelFont); count.hardlight(0xFFFFFFFF); add(count);
    }

    @Override protected void layout() {
        super.layout(); float left = flipped ? x + width - SIZE : x;
        icon.x = left + (SIZE - icon.width()) / 2f; icon.y = y + (height - icon.height()) / 2f;
        count.x = left + SIZE - count.width() - 2; count.y = y + 1;
        PixelScene.align(icon); PixelScene.align(count);
    }

    @Override public void update() {
        WayfarerMarketplace.poll(); refreshAttention();
        if (visible && (blinkDelay -= Game.elapsed) <= 0) {
            bright = !bright; setColor(bright ? 0xFF972B : 0xB85A16); icon.brightness(bright ? 1.7f : 1f); blinkDelay = 0.45f;
        }
        super.update();
    }

    public void refreshAttention() {
        int pending = WayfarerMarketplace.attentionCount();
        if (pending != lastCount) {
            if (pending > lastCount) flash(); lastCount = pending;
            count.text(pending > 99 ? "99+" : pending > 1 ? Integer.toString(pending) : ""); count.measure();
            visible = pending > 0; if (visible) blinkDelay = 0; layout();
        }
    }

    @Override protected void onClick() { super.onClick(); if (visible) WndWayfarerMarketplace.open(); }
    @Override protected String hoverText() { return "Marketplace responses, deliveries, and returns"; }
}
