/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerGlobalTrade;
import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplace;
import com.erebus.reclaimedpixeldungeon.network.WayfarerMarketplaceReference;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WndWayfarerMarketplace extends Window {
    private static final int WIDTH = ReclaimedWindow.modalWidth(180), HEIGHT = 190;
    private final GlobalTradeContent content = new GlobalTradeContent();
    private final ScrollPane pane = new ScrollPane(content);
    private final IconButton claims;
    private long nextRefresh;
    private long nextCountdown;
    private float blink;
    private boolean bright;

    public WndWayfarerMarketplace() {
        RenderedTextBlock title = PixelScene.renderTextBlock("Wayfarer Marketplace", 9);
        title.hardlight(TITLE_COLOR); title.setPos(4, 6); add(title);
        claims = new IconButton(new ItemSprite(ItemSpriteSheet.LOCKED_CHEST)) {
            @Override protected void onClick() { GameScene.show(new WndMarketplaceClaims(WndWayfarerMarketplace.this::refresh)); }
        };
        claims.setRect(WIDTH - 22, 2, 20, 20); add(claims);
        add(pane); resize(WIDTH, HEIGHT); pane.setRect(0, 24, WIDTH, HEIGHT - 24);
        rebuild(); refresh();
    }

    public static void open() { GameScene.show(new WndWayfarerMarketplace()); }

    private void refresh() {
        nextRefresh = System.currentTimeMillis() + 5000;
        WayfarerMarketplace.load((result, rows) -> { if (parent != null) rebuild(); });
    }

    @Override public void update() {
        super.update();
        long now = System.currentTimeMillis();
        if (now >= nextRefresh) refresh();
        if (now >= nextCountdown) { nextCountdown = now + 1000; rebuild(); }
        if (claims.visible && (blink -= Game.elapsed) <= 0) {
            bright = !bright; claims.icon().brightness(bright ? 1.7f : 1f); blink = 0.45f;
        }
    }

    private void rebuild() {
        float previous = pane.scrollY(); content.clear();
        claims.visible = WayfarerMarketplace.hasAttention(); claims.enable(claims.visible);
        float y = 3;
        RedButton post = new RedButton("Post Listing", 7) {
            @Override protected void onClick() { GameScene.show(new WndMarketplacePost(WndWayfarerMarketplace.this::refresh)); }
        };
        post.setRect(4, y, WIDTH - 8, 18); content.add(post); y = post.bottom() + 5;
        boolean any = false;
        for (JsonValue row = WayfarerMarketplace.rows().child; row != null; row = row.next) {
            any = true; y = listing(row, y);
        }
        if (!any) y = GlobalTradeContent.label(content, "No active listings are available yet.", WIDTH, y + 2);
        content.setSize(WIDTH, Math.max(HEIGHT - 24, y + 4));
        pane.scrollTo(0, Math.min(previous, Math.max(0, content.height() - (HEIGHT - 24))));
    }

    private float listing(JsonValue row, float y) {
        boolean mine = Dungeon.wayfarerCharacterId().equals(row.getString("seller", ""));
        String state = row.getString("state", "active");
        String seller = row.getString("seller_name", "Wayfarer");
        String timer = state.equals("active") || state.equals("reserved") ? duration(WayfarerMarketplace.secondsRemaining(row)) : title(state);
        RenderedTextBlock status = PixelScene.renderTextBlock(timer, 6);
        status.hardlight(TITLE_COLOR); status.setPos(WIDTH - 5 - status.width(), y); content.add(status);
        RenderedTextBlock owner = PixelScene.renderTextBlock("_" + seller + "_", 7);
        owner.maxWidth(Math.max(36, (int)(status.left() - 9))); owner.setPos(5, y); content.add(owner);
        y = Math.max(owner.bottom(), status.bottom()) + 4;
        try {
            WayfarerTradePayload offer = WayfarerGlobalTrade.decode(row.getString("seller_offer"));
            y = GlobalTradeContent.marketplaceOffer(content, offer, WIDTH, y);
            String requested = row.getString("requested_offer", null);
            if (requested != null && !requested.isEmpty()) {
                y = GlobalTradeContent.label(content, "_Requested Return_", WIDTH, y);
                y = GlobalTradeContent.marketplaceOffer(content, WayfarerGlobalTrade.decode(requested), WIDTH, y);
            } else y = GlobalTradeContent.label(content, "_Open to offers:_ Chat with the seller to negotiate.", WIDTH, y);
        } catch (Exception error) { y = GlobalTradeContent.label(content, "Unsupported listing data.", WIDTH, y); }

        if (WayfarerMarketplace.claimable(row)) {
            RedButton claim = new RedButton("Open Claims", 6) {
                @Override protected void onClick() { GameScene.show(new WndMarketplaceClaims(WndWayfarerMarketplace.this::refresh)); }
            };
            claim.setRect(6, y, WIDTH - 12, 17); content.add(claim); y = claim.bottom() + 4;
        } else if (mine && (state.equals("active") || state.equals("reserved"))) {
            RedButton cancel = new RedButton("Cancel Listing", 6) {
                @Override protected void onClick() {
                    GameScene.show(new WndOptions("Cancel Listing", "The listed items will move to Marketplace Claims.", "Cancel Listing", "Keep") {
                        @Override protected void onSelect(int index) { if (index == 0) WayfarerMarketplace.cancel(row.getString("listing_id"), result -> {
                            if (!result.success) WndGlobalTrade.notice(result.message); else refresh();
                        }); }
                    });
                }
            };
            cancel.setRect(6, y, WIDTH - 12, 17); content.add(cancel); y = cancel.bottom() + 4;
        } else if (!mine && state.equals("active")) {
            boolean exact = row.getString("requested_offer", null) != null;
			boolean compatible = row.getBoolean("compatible", true);
            float buttonWidth = exact ? (WIDTH - 15) / 2f : WIDTH - 12;
            RedButton chat = new RedButton("Chat Seller", 6) { @Override protected void onClick() { openChat(row); } };
            chat.setRect(6, y, buttonWidth, 17); content.add(chat);
            if (exact) {
                RedButton fulfill = new RedButton("Fulfill", 6) {
                    @Override protected void onClick() { GameScene.show(new WndMarketplaceFulfill(row, WndWayfarerMarketplace.this::refresh)); }
                };
				fulfill.enable(compatible);
                fulfill.setRect(chat.right() + 3, y, buttonWidth, 17); content.add(fulfill);
            }
            y = chat.bottom() + 4;
        }
        ColorBlock divider = new ColorBlock(WIDTH - 10, 1, 0xFF666666); divider.x = 5; divider.y = y; content.add(divider);
        return y + 6;
    }

    private void openChat(JsonValue row) {
        WayfarerAccountService.NearbyPlayer player = new WayfarerAccountService.NearbyPlayer(
                row.getString("seller", ""), row.getString("seller_name", "Wayfarer"),
                row.getString("seller_class", "WARRIOR"), row.getInt("seller_level", 1),
                row.getInt("seller_head_sprite", 0), row.getString("seller_public_key", ""),
                true, 0, 0, 0);
        WayfarerChatStore.remember(player);
        String listingId = row.getString("listing_id", "");
        if (!WayfarerChatStore.hasMarketplaceReference(player.characterId, listingId)) {
            try {
                String reference = WayfarerMarketplaceReference.encode(row);
                String sentAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ROOT).format(new Date());
                WayfarerAccountService.sendMarketplaceReference(player, reference, sentAt, result -> {
                    if (result.success) {
                        WayfarerChatStore.addOutgoing(player, Dungeon.hero.characterName(), reference, sentAt);
                    } else WndGlobalTrade.notice(result.message);
                });
            } catch (Exception error) {
                WndGlobalTrade.notice(error.getMessage() == null
                        ? "The Marketplace listing preview could not be prepared." : error.getMessage());
            }
        }
        GameScene.show(new WndWayfarerConversation(player));
    }

    private static String duration(long seconds) {
        long hours = seconds / 3600, minutes = (seconds % 3600) / 60, secs = seconds % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes > 0 ? minutes + "m " + secs + "s" : secs + "s";
    }

    private static String title(String value) {
        if (value == null || value.isEmpty()) return "Active";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).replace('_', ' ');
    }
}
