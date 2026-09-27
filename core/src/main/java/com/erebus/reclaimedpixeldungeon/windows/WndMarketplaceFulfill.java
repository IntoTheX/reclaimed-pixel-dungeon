/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.network.WayfarerGlobalTrade;
import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplace;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;

public class WndMarketplaceFulfill extends Window {
    private static final int WIDTH = ReclaimedWindow.modalWidth(180), HEIGHT = 185;
    private final GlobalTradeContent content = new GlobalTradeContent();

    public WndMarketplaceFulfill(JsonValue listing, Runnable changed) {
        ScrollPane pane = new ScrollPane(content); add(pane); resize(WIDTH, HEIGHT); pane.setRect(0, 0, WIDTH, HEIGHT);
        float y = 3;
        try {
            WayfarerTradePayload receive = WayfarerGlobalTrade.decode(listing.getString("seller_offer"));
            WayfarerTradePayload send = WayfarerGlobalTrade.decode(listing.getString("requested_offer"));
            y = GlobalTradeContent.label(content, "_You Receive_", WIDTH, y);
            y = GlobalTradeContent.marketplaceOffer(content, receive, WIDTH, y);
            y = GlobalTradeContent.label(content, "_You Send_", WIDTH, y + 3);
            y = GlobalTradeContent.marketplaceOffer(content, send, WIDTH, y);
            int total = WayfarerTradePayload.totalEmeraldCost(send, receive);
            int fee = WayfarerTradePayload.emeraldShare(send, receive, false);
            y = GlobalTradeContent.label(content, "_Shared fee:_ " + total + " Emerald" + (total == 1 ? "" : "s")
                    + ". _Your share:_ " + fee + ". Matching item types, quantities, minimum rarities, and resources will be reserved immediately.", WIDTH, y + 2);
            RedButton fulfill = new RedButton("Fulfill Listing", 7) {
                @Override protected void onClick() {
                    WayfarerMarketplace.fulfill(listing, result -> {
                        if (!result.success) WndGlobalTrade.notice(result.message);
                        else { if (changed != null) changed.run(); hide(); }
                    });
                }
            };
            fulfill.setRect(4, y + 2, WIDTH - 8, 18); content.add(fulfill); y = fulfill.bottom() + 4;
        } catch (Exception error) {
            y = GlobalTradeContent.label(content, "This listing contains unsupported item data.", WIDTH, y);
        }
        content.setSize(WIDTH, Math.max(HEIGHT, y));
    }
}
