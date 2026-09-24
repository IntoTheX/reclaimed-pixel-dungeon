/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.network.WayfarerGlobalTrade;
import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplace;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

public class WndMarketplaceClaims extends Window {
    private static final int WIDTH = ReclaimedWindow.modalWidth(180), HEIGHT = 185;
    private final GlobalTradeContent content = new GlobalTradeContent();
    private final ScrollPane pane = new ScrollPane(content);
    private final Runnable changed;

    public WndMarketplaceClaims(Runnable changed) {
        this.changed = changed; add(pane); resize(WIDTH, HEIGHT); pane.setRect(0, 0, WIDTH, HEIGHT); rebuild();
    }

    private void rebuild() {
        float previous = pane.scrollY(); content.clear();
        float y = GlobalTradeContent.label(content, "_Marketplace Claims & Returns_", WIDTH, 3);
        boolean any = false;
        for (JsonValue row = WayfarerMarketplace.rows().child; row != null; row = row.next) if (WayfarerMarketplace.claimable(row)) {
            any = true; y = add(row, y);
        }
        if (!any) y = GlobalTradeContent.label(content, "There are no marketplace deliveries or returned listings to claim.", WIDTH, y + 3);
        RedButton close = new RedButton("Close", 6) { @Override protected void onClick() { hide(); } };
        close.setRect(4, y + 3, WIDTH - 8, 18); content.add(close); y = close.bottom() + 4;
        content.setSize(WIDTH, Math.max(HEIGHT, y)); pane.scrollTo(0, Math.min(previous, Math.max(0, y - HEIGHT)));
    }

    private float add(JsonValue row, float y) {
        boolean mine = Dungeon.wayfarerCharacterId().equals(row.getString("seller", ""));
        String state = row.getString("state", "");
        boolean refund = mine && ("cancelled".equals(state) || "expired".equals(state));
        y = GlobalTradeContent.label(content, refund ? "_Returned Listing_" : "_Completed Marketplace Trade_", WIDTH, y + 2);
        String packet = refund ? row.getString("seller_offer", null)
                : mine ? row.getString("buyer_offer", null) : row.getString("seller_offer", null);
        try {
            WayfarerTradePayload payload = WayfarerGlobalTrade.decode(packet);
            y = GlobalTradeContent.marketplaceOffer(content, payload, WIDTH, y);
        } catch (Exception error) { y = GlobalTradeContent.label(content, "Unsupported delivery data.", WIDTH, y); }
        RedButton claim = new RedButton(refund ? "Claim Return" : "Claim", 6) {
            @Override protected void onClick() {
                WayfarerMarketplace.claim(row, result -> {
                    if (!result.success) WndGlobalTrade.notice(result.message);
                    else WayfarerMarketplace.load((loaded, rows) -> {
                        if (changed != null) changed.run(); rebuild();
                    });
                });
            }
        };
        claim.setRect(6, y, WIDTH - 12, 17); content.add(claim); y = claim.bottom() + 5;
        ColorBlock divider = new ColorBlock(WIDTH - 12, 1, 0xFF666666); divider.x = 6; divider.y = y; content.add(divider);
        return y + 5;
    }
}
