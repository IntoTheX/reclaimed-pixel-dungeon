/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.*;
import com.erebus.reclaimedpixeldungeon.*;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import java.io.IOException;

/** Local receipts are saved in the same game bundle as inventory and resource changes. */
public final class WayfarerGlobalTrade {
    private WayfarerGlobalTrade() {}
    public static boolean working;

    public static WayfarerTradePayload decode(String packet) throws IOException {
        if (packet == null || packet.length()>262144) throw new IOException("Invalid trade item data.");
        try {
            JsonValue json = new JsonReader().parse(packet);
            inspect(json,0);
            WayfarerTradePayload result = WayfarerTradePayload.fromPacket(packet);
            for (int i=0;i<3;i++) {
                Item item=result.item(i);
                if (json.has("item_"+i) && (item==null || item instanceof Bag || item.quantity()<1))
                    throw new IOException("This item cannot be transferred.");
            }
            return result;
        } catch (RuntimeException error) { throw new IOException("Invalid trade item data.",error); }
    }
    private static void inspect(JsonValue node,int depth) throws IOException {
        if (depth>40) throw new IOException("Trade item data is too deeply nested.");
        if ("__className".equals(node.name) && (!node.isString()
                || !node.asString().startsWith("com.erebus.reclaimedpixeldungeon.items.")))
            throw new IOException("Unsupported trade item component.");
        for (JsonValue child=node.child;child!=null;child=child.next) inspect(child,depth+1);
    }
    private static JsonValue journal() { return new JsonReader().parse(Dungeon.globalTradeJournal); }
    private static void save(JsonValue journal) throws IOException {
        String previous=Dungeon.globalTradeJournal;
        Dungeon.globalTradeJournal=journal.toJson(JsonWriter.OutputType.json);
        if (!Dungeon.saveGameChecked(GamesInProgress.curSlot)) {
            Dungeon.globalTradeJournal=previous;
            throw new IOException("Could not save the trade receipt. Retry before closing the game.");
        }
    }
    private static void put(JsonValue object,String key,String value) {
        object.remove(key); object.addChild(key,new JsonValue(value));
    }
    public static boolean pending(String id) { return journal().has(id); }
    public static boolean localItems(String id) {
        JsonValue entry=journal().get(id);
        return entry!=null && entry.getString("stage","").equals("received") && !entry.getString("remaining","").isEmpty();
    }

    public static void submit(String peer,String id,boolean response,WayfarerTradePayload offer,
                              Item[] sources,int[] quantities,WayfarerAccountService.ResultCallback done) {
        if (working) return;
        final String packet=offer.toPacket();
        try {
            validateSubmission(id,packet,offer,sources,quantities);
        } catch(Exception error) {
            done.completed(new WayfarerAccountService.Result(false,error.getMessage()));
            return;
        }
        working=true;
        WayfarerAccountService.tradeAction(peer,id,response?"respond":"prepare",packet,(result,row)->{
            if (!result.success) { working=false; done.completed(result); return; }
            Item[] withdrawn=new Item[3];
            boolean charged=false;
            boolean committed=false;
            try {
                if (!row.getString("state").equals(response?"responding":"invited")) throw new IOException("This offer is no longer open.");
                validateSubmission(id,packet,offer,sources,quantities);
                for (int i=0;i<3;i++) if (sources[i]!=null) {
                    if (quantities[i]<sources[i].quantity()) withdrawn[i]=sources[i].split(quantities[i]);
                    else withdrawn[i]=sources[i].detachAll(Dungeon.hero.belongings.backpack);
                    if (withdrawn[i]==null) throw new IOException("An offered item could not be reserved.");
                    sources[i].updateQuickslot();
                }
                HomebaseState h=Dungeon.homebase;
                charged=true;
                h.spendEmeralds(1); h.spendGold(offer.gold()); h.spendEnergy(offer.energy());
                for(HomebaseState.Material m:HomebaseState.Material.values()) h.spend(m,offer.material(m));
                for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values()) h.spendForgeResource(f,offer.forge(f));
                JsonValue journal=journal(),entry=new JsonValue(JsonValue.ValueType.object);
                put(entry,"peer",peer); put(entry,"stage","deposited"); journal.addChild(id,entry);
                save(journal);
                committed=true;
                working=false; retry(id,done);
            } catch(Exception error) {
                if(!committed)restoreOffer(offer,withdrawn,charged);
                if(!committed)cancelPrepared(peer,id,error.getMessage(),done);
                else {
                    working=false;
                    done.completed(new WayfarerAccountService.Result(false,error.getMessage()));
                }
            }
        });
    }

    private static void validateSubmission(String id,String packet,WayfarerTradePayload offer,
                                           Item[] sources,int[] quantities) throws IOException {
        if(offer==null || offer.isEmpty())throw new IOException("Offer something before sending the trade request.");
        if(Dungeon.hero==null || sources==null || quantities==null || sources.length<3 || quantities.length<3)
            throw new IOException("The trade inventory is not available.");
        if(pending(id))throw new IOException("This offer already has a saved receipt. Use Retry.");
        decode(packet);
        validateFunds(offer);
        for(int i=0;i<3;i++) {
            Item source=sources[i],offered=offer.item(i);
            if((source==null)!=(offered==null))throw new IOException("An offered item changed. Review the offer again.");
            if(source==null)continue;
            if(!Dungeon.hero.belongings.contains(source) || source.isEquipped(Dungeon.hero)
                    || source instanceof Bag || quantities[i]<1 || source.quantity()<quantities[i]
                    || offered.quantity()!=quantities[i])
                throw new IOException("An offered item changed. Review the offer again.");
            for(int j=0;j<i;j++)if(sources[j]==source)throw new IOException("Select each item stack only once.");
        }
    }

    private static void cancelPrepared(String peer,String id,String message,WayfarerAccountService.ResultCallback done) {
        WayfarerAccountService.tradeAction(peer,id,"cancel",null,(cleanup,row)->{
            working=false;
            done.completed(new WayfarerAccountService.Result(false,message));
        });
    }

    private static void restoreOffer(WayfarerTradePayload offer,Item[] withdrawn,boolean charged) {
        HomebaseState h=Dungeon.homebase;
        if(charged && h!=null && !HomebaseState.infiniteTestResourcesEnabled()) {
            h.addEmeralds(1);h.addGold(offer.gold());h.addEnergy(offer.energy());
            for(HomebaseState.Material m:HomebaseState.Material.values())h.add(m,offer.material(m));
            for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values())h.addForgeResource(f,offer.forge(f));
        }
        if(Dungeon.hero==null)return;
        for(Item item:withdrawn)if(item!=null && !item.collect(Dungeon.hero.belongings.backpack)
                && Dungeon.level!=null)Dungeon.level.drop(item,Dungeon.hero.pos).sprite.drop();
    }
    private static void validateFunds(WayfarerTradePayload p) throws IOException {
        HomebaseState h=Dungeon.homebase;
        if(h==null || h.emeraldAmount()<1 || h.goldAmount()<p.gold() || h.energyAmount()<p.energy()) throw new IOException("Not enough resources or Emeralds.");
        for(HomebaseState.Material m:HomebaseState.Material.values()) if(h.amount(m)<p.material(m)) throw new IOException("Not enough materials.");
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values()) if(h.forgeResourceAmount(f)<p.forge(f)) throw new IOException("Not enough forge currencies.");
    }
    public static void retry(String id,WayfarerAccountService.ResultCallback done) {
        JsonValue entry=journal().get(id);
        if(entry==null || working) return;
        try {
            String stage=entry.getString("stage");
            if(stage.equals("granting")) finishGrant(id);
            if(stage.equals("granting") || stage.equals("received")) {
                deliverRemaining(id);
                acknowledge(id,done);
                return;
            }
            save(journal());
        } catch(IOException e) { done.completed(new WayfarerAccountService.Result(false,e.getMessage())); return; }
        String stage=entry.getString("stage");
        working=true;
        WayfarerAccountService.tradeAction(entry.getString("peer"),id,"deposit",null,(result,row)->{
            working=false;
            done.completed(result);
        });
    }
    public static void claim(String peer,String id,WayfarerAccountService.ResultCallback done) {
        if(working) return;
        JsonValue existing=journal().get(id);
        if(existing!=null && !existing.getString("stage").equals("deposited")) {
            retry(id,done);
            return;
        }
        working=true;
        WayfarerAccountService.tradeAction(peer,id,"claim",SPDSettings.wayfarerInstallationId(),(result,row)->{
            working=false;
            if(!result.success) { done.completed(result); return; }
            try {
                if(row.getBoolean("claimed",false)) throw new IOException("This delivery was already claimed on this character.");
                String packet=row.getString("payload",null);
                boolean refund=row.getBoolean("refund",false);
                JsonValue j=journal(),entry=new JsonValue(JsonValue.ValueType.object);
                put(entry,"peer",peer); put(entry,"stage","granting"); put(entry,"remaining",packet);
                put(entry,"refund",Boolean.toString(refund));
                j.remove(id);j.addChild(id,entry); save(j);
                finishGrant(id);
                deliverRemaining(id);
                acknowledge(id,done);
            } catch(Exception e) { done.completed(new WayfarerAccountService.Result(false,e.getMessage())); }
        });
    }

    private static void finishGrant(String id) throws IOException {
        JsonValue j=journal(),entry=j.get(id);
        String packet=entry.getString("remaining","");
        if(packet.isEmpty()) throw new IOException("The saved claim has no delivery data.");
        WayfarerTradePayload p=decode(packet);
        HomebaseState h=Dungeon.homebase;
        boolean refund=Boolean.parseBoolean(entry.getString("refund","false"));
        validateCapacity(h,p,refund);
        h.addGold(p.gold());h.addEnergy(p.energy());
        for(HomebaseState.Material m:HomebaseState.Material.values())h.add(m,p.material(m));
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values())h.addForgeResource(f,p.forge(f));
        if(refund)h.addEmeralds(1);
        WayfarerTradePayload items=new WayfarerTradePayload();
        for(int i=0;i<3;i++)items.item(i,p.item(i));
        put(entry,"stage","received");put(entry,"remaining",items.isEmpty()?"":items.toPacket());
        try { save(j); }
        catch(IOException error) { rollback(h,p,refund);throw error; }
    }

    private static void validateCapacity(HomebaseState h,WayfarerTradePayload p,boolean refund) throws IOException {
        if(h==null || (long)h.goldAmount()+p.gold()>Integer.MAX_VALUE || (long)h.energyAmount()+p.energy()>Integer.MAX_VALUE
                || (refund && h.emeraldAmount()==Integer.MAX_VALUE))throw new IOException("Resource storage is full.");
        for(HomebaseState.Material m:HomebaseState.Material.values())if((long)h.amount(m)+p.material(m)>Integer.MAX_VALUE)throw new IOException("Material storage is full.");
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values())if((long)h.forgeResourceAmount(f)+p.forge(f)>Integer.MAX_VALUE)throw new IOException("Forge storage is full.");
    }

    private static void rollback(HomebaseState h,WayfarerTradePayload p,boolean refund) {
        h.spendGold(p.gold());h.spendEnergy(p.energy());
        for(HomebaseState.Material m:HomebaseState.Material.values())h.spend(m,p.material(m));
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values())h.spendForgeResource(f,p.forge(f));
        if(refund)h.spendEmeralds(1);
    }

    private static void deliverRemaining(String id) throws IOException {
        JsonValue j=journal(),entry=j.get(id);
        String packet=entry.getString("remaining","");
        if(packet.isEmpty())return;
        if(Dungeon.level==null || Dungeon.hero==null)throw new IOException("Enter the active game to receive these items.");
        WayfarerTradePayload p=decode(packet);
        boolean floorChanged=false;
        for(int i=0;i<3;i++)if(p.item(i)!=null) {
            String marker=id+":"+i;
            if(!alreadyDelivered(marker)) {
                Item item=p.item(i);item.wayfarerDeliveryId(marker);
                if(!item.collect(Dungeon.hero.belongings.backpack)) {
                    Dungeon.level.drop(item,Dungeon.hero.pos).sprite.drop();
                    floorChanged=true;
                }
            }
        }
        if(floorChanged)Dungeon.saveLevel(GamesInProgress.curSlot);
        // Persist delivered markers together with the still-pending receipt first.
        save(j);
        for(int i=0;i<3;i++)p.item(i,null);
        put(entry,"remaining","");
        save(j);
        boolean cleaned=false;
        for(Item item:Dungeon.hero.belongings)if(item.wayfarerDeliveryId().startsWith(id+":")) {
            item.wayfarerDeliveryId("");cleaned=true;
        }
        if(cleaned)Dungeon.saveGameChecked(GamesInProgress.curSlot);
    }

    private static boolean alreadyDelivered(String marker) {
        if(Dungeon.level==null)return false;
        for(Item item:Dungeon.hero.belongings)if(marker.equals(item.wayfarerDeliveryId()))return true;
        for(Heap heap:Dungeon.level.heaps.valueList())for(Item item:heap.items)
            if(marker.equals(item.wayfarerDeliveryId()))return true;
        return false;
    }

    private static void acknowledge(String id,WayfarerAccountService.ResultCallback done) {
        JsonValue entry=journal().get(id);
        if(entry==null || working)return;
        working=true;
        WayfarerAccountService.tradeAction(entry.getString("peer"),id,"ack",SPDSettings.wayfarerInstallationId(),(result,row)->{
            working=false;
            if(result.success)try { JsonValue j=journal();j.remove(id);save(j); }
            catch(IOException error) { done.completed(new WayfarerAccountService.Result(false,error.getMessage()));return; }
            done.completed(result);
        });
    }
}
