/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.*;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.network.*;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.*;
import com.watabou.noosa.Image;
import java.util.UUID;

public class WndGlobalTrade extends Window {
    private static final int RESOURCE_ROW_HEIGHT=16;
    private static final int RESOURCE_ICON_SIZE=12;
    private static final int RESOURCE_PLUS_SIZE=16;
    private static final int RESOURCE_COLUMN_GAP=4;

    private final int w=ReclaimedWindow.modalWidth(180),h=180;
    private final GlobalTradeContent content=new GlobalTradeContent();
    private final ScrollPane pane=new ScrollPane(content);
    private final WayfarerAccountService.NearbyPlayer peer;
    private final String id;
    private final boolean response;
    private final Draft draft;
    private float y;
    private int resourceColumn;
    private static class Draft {
        WayfarerTradePayload payload=new WayfarerTradePayload();
        Item[] items=new Item[3];int[] quantities=new int[3];
    }
    public WndGlobalTrade(WayfarerAccountService.NearbyPlayer peer,String id,boolean response) {
        this(peer,id==null?UUID.randomUUID().toString():id,response,new Draft());
    }
    private WndGlobalTrade(WayfarerAccountService.NearbyPlayer peer,String id,boolean response,Draft draft) {
        this.peer=peer;this.id=id;this.response=response;this.draft=draft;
        add(pane);resize(w,h);pane.setRect(0,0,w,h);rebuild();
    }
    private void refresh() {
        if(parent==null || !exists) GameScene.show(new WndGlobalTrade(peer,id,response,draft));
        else rebuild();
    }
    private void text(String value) { y=GlobalTradeContent.label(content,value,w,y); }
    private void button(String title,Runnable action) {
        RedButton b=new RedButton(title,6) { @Override protected void onClick(){action.run();} };
        b.setRect(3,y,w-6,18);content.add(b);y+=21;
    }
    private void rebuild() {
        content.clear();y=3;
        text("_Offer to "+peer.playerName+"_");
        addEmeraldNotice();
        for(int i=0;i<3;i++) {
            final int slot=i;final Item item=draft.payload.item(i);
            InventorySlot box=new InventorySlot(item) {
                @Override protected void onClick(){choose(slot);}
                @Override protected boolean onLongClick(){
                    if(item==null)choose(slot);
                    else GameScene.show(new WndInfoItem(item));
                    return true;
                }
            };
            box.setRect((w-104)/2+i*36,y,32,32);content.add(box);
            if(item==null)box.clear();
        }
        y+=36;
        resourceColumn=0;
        addResourceRow("Gold",Icons.get(Icons.COIN_SML),draft.payload.gold(),Dungeon.homebase.goldAmount(),draft.payload::gold);
        addResourceRow("Energy",Icons.get(Icons.ENERGY_SML),draft.payload.energy(),Dungeon.homebase.energyAmount(),draft.payload::energy);
        for(HomebaseState.Material m:HomebaseState.Material.values())
            addResourceRow(WndHomebaseFacility.materialName(m),new ItemSprite(WndHomebaseFacility.materialIcon(m)),
                    draft.payload.material(m),Dungeon.homebase.amount(m),v->draft.payload.material(m,v));
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values())
            addResourceRow(WndHomebaseFacility.forgeName(f),new ItemSprite(WndHomebaseFacility.forgeIcon(f)),
                    draft.payload.forge(f),Dungeon.homebase.forgeResourceAmount(f),v->draft.payload.forge(f,v));
        if(resourceColumn==1)y+=RESOURCE_ROW_HEIGHT+2;
        y+=4;
        button("Send Offer",()->{
            if(WayfarerGlobalTrade.working || draft.payload.isEmpty()) return;
            WayfarerGlobalTrade.submit(peer.characterId,id,response,draft.payload,draft.items,draft.quantities,result->{
                if(result.success) { if(parent!=null) hide(); }
                else notice(result.message);
            });
        });
        if(WayfarerGlobalTrade.pending(id)) button("Retry Saved Offer",()->WayfarerGlobalTrade.retry(id,r->{if(r.success) hide();else notice(r.message);}));
        content.setSize(w,Math.max(h,y));pane.scrollTo(0,Math.min(pane.scrollY(),Math.max(0,y-h)));
    }

    private void addEmeraldNotice() {
        RenderedTextBlock count=PixelScene.renderTextBlock("1",6);
        count.setPos(4,y);
        content.add(count);
        Image emerald=new ItemSprite(WndHomebaseFacility.emeraldIcon());
        float scale=Math.min(10/emerald.width,10/emerald.height);
        emerald.scale.set(scale);
        emerald.x=count.right()+2;
        emerald.y=y+(count.height()-emerald.height())/2f;
        PixelScene.align(emerald);
        content.add(emerald);
        RenderedTextBlock notice=PixelScene.renderTextBlock(
                "_Emerald_ is reserved with your offer and refunded if the trade is cancelled. Both players must confirm before the exchange is finalized.",6);
        notice.maxWidth((int)(w-emerald.x-emerald.width()-6));
        notice.setPos(emerald.x+emerald.width()+2,y);
        content.add(notice);
        y=Math.max(Math.max(count.bottom(),emerald.y+emerald.height()),notice.bottom())+4;
    }
    private interface Amount { void set(int value); }

    private void addResourceRow(String name,Image icon,int offered,int owned,Amount setter) {
        float columnWidth=(w-6-RESOURCE_COLUMN_GAP)/2f;
        float x=3+resourceColumn*(columnWidth+RESOURCE_COLUMN_GAP);
        float rowY=y;
        if(icon!=null) {
            float scale=Math.min(RESOURCE_ICON_SIZE/icon.width,RESOURCE_ICON_SIZE/icon.height);
            icon.scale.set(scale);
            icon.x=x;
            icon.y=rowY+(RESOURCE_ROW_HEIGHT-icon.height())/2f;
            PixelScene.align(icon);
            content.add(icon);
        }
        float plusX=x+columnWidth-RESOURCE_PLUS_SIZE;
        RenderedTextBlock amount=PixelScene.renderTextBlock(
                WndHomebaseFacility.compactAmount(offered)+"/"+WndHomebaseFacility.compactAmount(owned),6);
        amount.maxWidth((int)(plusX-(x+RESOURCE_ICON_SIZE+2)-2));
        amount.setPos(x+RESOURCE_ICON_SIZE+2,rowY+(RESOURCE_ROW_HEIGHT-amount.height())/2f);
        content.add(amount);
        RedButton plus=new RedButton("+",9) {
            @Override protected void onClick(){amount(name,owned,offered,setter);}
        };
        plus.setRect(plusX,rowY,RESOURCE_PLUS_SIZE,RESOURCE_ROW_HEIGHT);
        content.add(plus);
        resourceColumn++;
        if(resourceColumn>=2){resourceColumn=0;y+=RESOURCE_ROW_HEIGHT+2;}
    }

    private void amount(String name,int owned,int value,Amount setter) {
        GameScene.show(new WndTextInput(name,"_Owned:_ "+owned,Integer.toString(value),10,false,"Set","Cancel") {
            @Override public void onSelect(boolean yes,String input) {
                if(yes) try { int n=Integer.parseInt(input.trim());if(n<0||n>owned) throw new NumberFormatException();setter.set(n); }
                catch(Exception e){notice("Enter an amount between 0 and "+owned+".");}
                refresh();
            }
        });
    }
    private void choose(int slot) {
        if(draft.items[slot]!=null) {
            GameScene.show(new WndOptions("Offered Item","Remove this item from your draft?","Remove","Keep") {
                @Override protected void onSelect(int index){if(index==0){draft.items[slot]=null;draft.payload.item(slot,null);refresh();}}
            });return;
        }
        GameScene.show(WndBag.getBag(new WndBag.ItemSelector(){
            @Override public String textPrompt(){return "Choose an item to trade";}
            @Override public boolean itemSelectable(Item item){
                if(item==null||item instanceof Bag||item.isEquipped(Dungeon.hero))return false;
                for(Item selected:draft.items)if(selected==item)return false;
                return true;
            }
            @Override public void onSelect(Item item){
                if(item==null){refresh();return;}
                if(item.quantity()>1)amount(item.name(),item.quantity(),item.quantity(),n->{
                    if(n>0)setItem(slot,item,n);
                });
                else {
                    setItem(slot,item,1);
                    refresh();
                }
            }
        }));
    }

    private void setItem(int slot,Item item,int quantity) {
        WayfarerTradePayload copy=new WayfarerTradePayload();copy.item(slot,item);
        Item preview=copy.copy().item(slot);preview.quantity(quantity);
        draft.items[slot]=item;draft.quantities[slot]=quantity;draft.payload.item(slot,preview);
    }
    public static void notice(String message){GameScene.show(new WndOptions("Wayfarer Trade",message==null?"Trade unavailable.":message,"Close"));}

    public static void review(WayfarerAccountService.NearbyPlayer peer,JsonValue trade) {
        boolean mine=trade.getString("sender").equals(Dungeon.wayfarerCharacterId());
        Window window=new Window();GlobalTradeContent c=new GlobalTradeContent();ScrollPane scroll=new ScrollPane(c);
        int width=ReclaimedWindow.modalWidth(180);window.add(scroll);window.resize(width,180);scroll.setRect(0,0,width,180);
        try {
            float y=GlobalTradeContent.label(c,"_You Send_",width,3);
            y=GlobalTradeContent.offer(c,WayfarerGlobalTrade.decode(trade.getString(mine?"sender_offer":"recipient_offer")),width,y);
            y=GlobalTradeContent.label(c,"_You Receive_",width,y+3);
            y=GlobalTradeContent.offer(c,WayfarerGlobalTrade.decode(trade.getString(mine?"recipient_offer":"sender_offer")),width,y);
            y=GlobalTradeContent.label(c,"_1 Emerald_ per player. Confirming accepts these exact offers.",width,y);
            RedButton accept=new RedButton("Confirm",6){@Override protected void onClick(){
                WayfarerAccountService.tradeAction(peer.characterId,trade.getString("trade_id"),"confirm",null,(r,data)->{if(r.success)window.hide();else notice(r.message);});
            }};
            accept.setRect(3,y,(width-9)/2f,18);c.add(accept);
            RedButton cancel=new RedButton("Decline",6){@Override protected void onClick(){
                WayfarerAccountService.tradeAction(peer.characterId,trade.getString("trade_id"),"cancel",null,(r,data)->{if(r.success)window.hide();else notice(r.message);});
            }};
            cancel.setRect(accept.right()+3,y,(width-9)/2f,18);c.add(cancel);c.setSize(width,y+22);
            GameScene.show(window);
        }catch(Exception e){window.destroy();notice(e.getMessage());}
    }
}
