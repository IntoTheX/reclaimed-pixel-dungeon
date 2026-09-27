/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplace;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ui.Component;

public class WndMarketplacePost extends Window {
    private static final int WIDTH = ReclaimedWindow.modalWidth(180), HEIGHT = 190;
    private final Component content = new Component();
    private final ScrollPane pane = new ScrollPane(content);
    private final MarketplaceOfferEditor offered;
    private final MarketplaceOfferEditor requested;
    private final RenderedTextBlock notice;
    private final RedButton post;
    private final Runnable posted;

    public WndMarketplacePost(Runnable posted) {
        this.posted = posted; add(pane); resize(WIDTH, HEIGHT); pane.setRect(0, 0, WIDTH, HEIGHT);
        notice = PixelScene.renderTextBlock(
                "Listings last _12 hours_. Requested returns enable instant matching fulfillment; leave it empty to negotiate through chat.", 6);
        notice.maxWidth(WIDTH - 8); notice.setPos(4, 3); content.add(notice);
        offered = new MarketplaceOfferEditor("You List", false, this::layoutContent);
        requested = new MarketplaceOfferEditor("Requested Return", true, this::layoutContent);
        content.add(offered);
        content.add(requested);
        post = new RedButton("Post Listing", 7) {
            @Override protected void onClick() {
                WayfarerMarketplace.post(offered.payload, requested.payload, offered.sources, offered.quantities, result -> {
                    if (!result.success) WndGlobalTrade.notice(result.message);
                    else { WayfarerMarketplace.refreshSoon(); if (posted != null) posted.run(); hide(); }
                });
            }
        };
        content.add(post);
        layoutContent();
    }

    private void layoutContent() {
        offered.setRect(0, notice.bottom() + 4, WIDTH, MarketplaceOfferEditor.HEIGHT);
        offered.rebuild();
        requested.setRect(0, offered.bottom() + 3, WIDTH, MarketplaceOfferEditor.HEIGHT);
        requested.rebuild();
        post.setRect(4, requested.bottom() + 5, WIDTH - 8, 18);
        content.setSize(WIDTH, post.bottom() + 4);
    }
}
