/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerGlobalTrade;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

public class WndWayfarerTradeClaims extends Window {

    private static final int WIDTH=ReclaimedWindow.modalWidth(180);
    private static final int HEIGHT=180;
    private final WayfarerAccountService.NearbyPlayer peer;
    private final Runnable changed;
    private final GlobalTradeContent content=new GlobalTradeContent();
    private final ScrollPane pane=new ScrollPane(content);
    private JsonValue trades;

    public WndWayfarerTradeClaims(WayfarerAccountService.NearbyPlayer peer,JsonValue trades,Runnable changed) {
        this.peer=peer;
        this.trades=trades;
        this.changed=changed;
        add(pane);
        resize(WIDTH,HEIGHT);
        pane.setRect(0,0,WIDTH,HEIGHT);
        rebuild();
    }

    static boolean hasClaimable(JsonValue rows) {
        if(rows==null)return false;
        for(JsonValue row=rows.child;row!=null;row=row.next)if(claimable(row))return true;
        return false;
    }

	private static boolean claimable(JsonValue row) {
		boolean mine=row.getString("sender","").equals(Dungeon.wayfarerCharacterId());
        boolean claimed=row.getBoolean(mine?"sender_claimed":"recipient_claimed",false);
        boolean deposited=row.getBoolean(mine?"sender_deposited":"recipient_deposited",false);
        String state=row.getString("state","");
		return !claimed && (state.equals("finalized")
				|| (state.equals("cancelled")&&deposited));
    }

    private void rebuild() {
        float previous=pane.scrollY();
        content.clear();
        float y=GlobalTradeContent.label(content,"_Unclaimed Trades_",WIDTH,3);
        boolean any=false;
        if(trades!=null)for(JsonValue row=trades.child;row!=null;row=row.next)if(claimable(row)) {
            any=true;
            y=addClaim(row,y);
        }
        if(!any)y=GlobalTradeContent.label(content,"There are no unclaimed trade deliveries or refunds in this conversation.",WIDTH,y+3);
        RedButton close=new RedButton("Close",6){@Override protected void onClick(){hide();}};
        close.setRect(4,y+3,WIDTH-8,18);
        content.add(close);
        y=close.bottom()+4;
        content.setSize(WIDTH,Math.max(HEIGHT,y));
        pane.scrollTo(0,Math.min(previous,Math.max(0,y-HEIGHT)));
    }

    private float addClaim(JsonValue row,float y) {
        final String id=row.getString("trade_id");
        final String state=row.getString("state");
        boolean mine=row.getString("sender").equals(Dungeon.wayfarerCharacterId());
		String title=state.equals("cancelled")?"_Trade Refund_":"_Trade Delivery_";
        y=GlobalTradeContent.label(content,title,WIDTH,y+2);
        String key=state.equals("cancelled")?(mine?"sender_offer":"recipient_offer")
                :(mine?"recipient_offer":"sender_offer");
        try {
            String packet=row.getString(key,null);
            if(packet!=null)y=GlobalTradeContent.offer(content,WayfarerGlobalTrade.decode(packet),WIDTH,y);
        } catch(Exception error) {
            y=GlobalTradeContent.label(content,"_Unsupported item data:_ "+error.getMessage(),WIDTH,y);
        }
		String label=state.equals("cancelled")?"Claim Return":"Claim";
		RedButton claim=new RedButton(label,6){@Override protected void onClick(){
			if(WayfarerGlobalTrade.working)return;
			WayfarerGlobalTrade.claim(peer.characterId,id,WndWayfarerTradeClaims.this::claimResult);
        }};
        claim.setRect(6,y,WIDTH-12,17);
        content.add(claim);
        y=claim.bottom()+5;
        ColorBlock divider=new ColorBlock(WIDTH-12,1,0xFF666666);
        divider.x=6;divider.y=y;content.add(divider);
        return y+5;
    }

    private void claimResult(WayfarerAccountService.Result result) {
        if(parent==null)return;
        if(!result.success) {
            WndGlobalTrade.notice(result.message);
            return;
        }
        if(changed!=null)changed.run();
        WayfarerAccountService.tradeAction(peer.characterId,null,"list",null,(loaded,rows)->{
            if(parent==null)return;
            if(loaded.success&&rows!=null)trades=rows;
            rebuild();
        });
    }
}
