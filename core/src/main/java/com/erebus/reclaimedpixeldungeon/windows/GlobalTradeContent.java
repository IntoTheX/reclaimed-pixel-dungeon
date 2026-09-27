/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.scenes.*;
import com.erebus.reclaimedpixeldungeon.ui.*;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.*;
import com.watabou.noosa.ui.Component;
import java.util.ArrayList;
import java.util.Locale;

/** Rebuilding must unregister old item/button pointer areas as well as remove their visuals. */
class GlobalTradeContent extends Component {
    @Override public synchronized void clear() {
        for(Gizmo child:new ArrayList<>(members)) if(child!=null) child.destroy();
        super.clear();
    }
    static float label(Component target,String text,float width,float y) {
        RenderedTextBlock line=PixelScene.renderTextBlock(text,6);
        line.maxWidth((int)width-8); line.setPos(4,y); target.add(line);
        return line.bottom()+4;
    }
    static float offer(Component target,WayfarerTradePayload payload,float width,float y) {
		return offer(target,payload,width,y,false);
	}
	static float marketplaceOffer(Component target,WayfarerTradePayload payload,float width,float y) {
		return offer(target,payload,width,y,true);
	}
	private static float offer(Component target,WayfarerTradePayload payload,float width,float y,
			boolean skipEmptyItems) {
		boolean hasItems=false;
		for(int i=0;i<3;i++) if(payload.item(i)!=null) { hasItems=true; break; }
		if(hasItems || !skipEmptyItems) {
			float x=(width-104)/2;
			for(int i=0;i<3;i++) {
				final Item item=payload.item(i);
				InventorySlot slot=new InventorySlot(item) {
					@Override protected void onClick() { if(item!=null) GameScene.show(new WndInfoItem(item)); }
				};
				slot.setRect(x+i*36,y,32,32); target.add(slot);
				if(item==null) { slot.clear(); slot.enable(false); }
			}
			y+=36;
			for(int i=0;i<WayfarerTradePayload.ITEM_SLOTS;i++) {
				Item item=payload.item(i);
				if(item==null || !payload.itemTemplate(i)) continue;
				String minimum=item.supportsRarityStats()
						? payload.minimumRarity(i).coloredName()+"+ " : "";
				RenderedTextBlock description=PixelScene.renderTextBlock(minimum
						+Messages.titleCase(item.trueName())+" x"
						+String.format(Locale.US,"%,d",item.quantity()),6);
				description.maxWidth((int)width-10);
				description.setPos(5,y);
				target.add(description);
				y=description.bottom()+2;
			}
		}
        if(payload.gold()>0) y=resource(target,Icons.get(Icons.COIN_SML),"Gold",payload.gold(),width,y);
        if(payload.energy()>0) y=resource(target,Icons.get(Icons.ENERGY_SML),"Energy",payload.energy(),width,y);
        for(HomebaseState.Material m:HomebaseState.Material.values()) if(payload.material(m)>0)
            y=resource(target,new ItemSprite(WndHomebaseFacility.materialIcon(m)),m.toString(),payload.material(m),width,y);
        for(HomebaseState.ForgeResource f:HomebaseState.ForgeResource.values()) if(payload.forge(f)>0)
            y=resource(target,new ItemSprite(WndHomebaseFacility.forgeIcon(f)),f.toString(),payload.forge(f),width,y);
        return y;
    }
    private static float resource(Component target,Image icon,String name,int amount,float width,float y) {
        icon.scale.set(8/Math.max(icon.width,icon.height)); icon.x=5; icon.y=y; target.add(icon);
		RenderedTextBlock text=PixelScene.renderTextBlock(name.replace('_',' ')+" x"
				+String.format(Locale.US,"%,d",amount),6);
        text.maxWidth((int)width-22);text.setPos(18,y+1);target.add(text);
        return Math.max(y+10,text.bottom()+3);
    }
}
