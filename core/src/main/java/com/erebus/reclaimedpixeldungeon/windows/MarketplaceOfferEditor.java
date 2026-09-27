/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.Locale;

/** Shared three-slot and resource editor used by marketplace listing drafts. */
class MarketplaceOfferEditor extends Component {
    static final int HEIGHT = 154;
    final WayfarerTradePayload payload = new WayfarerTradePayload();
    final Item[] sources = new Item[WayfarerTradePayload.ITEM_SLOTS];
    final int[] quantities = new int[WayfarerTradePayload.ITEM_SLOTS];
    private final String heading;
    private final boolean optional;
    private final Runnable changed;
    private float cursor;
    private int column;

    MarketplaceOfferEditor(String heading, boolean optional, Runnable changed) {
        this.heading = heading;
        this.optional = optional;
        this.changed = changed;
    }

    // Component calls this from its constructor, before this editor's fields are initialized.
    @Override protected void createChildren() {}

    void rebuild() {
		// Amount/item dialogs can outlive the listing window that opened them.
		if (members == null) return;
        clear(); cursor = 1; column = 0;
        RenderedTextBlock title = PixelScene.renderTextBlock("_" + heading + "_" + (optional ? " (optional)" : ""), 7);
        title.maxWidth((int)width - 8); title.setPos(x + 4, y + cursor); add(title);
		cursor = title.bottom() - y + 4;
        for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
            final int slot = i; final Item item = payload.item(i);
            InventorySlot box = new InventorySlot(item) {
                @Override protected void onClick() { choose(slot); }
                @Override protected boolean onLongClick() {
                    if (item == null) choose(slot); else GameScene.show(new WndInfoItem(item));
                    return true;
                }
            };
            box.setRect(x + (width - 104) / 2f + i * 36, y + cursor, 32, 32); add(box);
            if (item == null) box.clear();
        }
        cursor += 36;
		if (optional) {
			for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
				Item item = payload.item(i);
				if (item == null || !payload.itemTemplate(i)) continue;
				String minimum = item.supportsRarityStats()
						? payload.minimumRarity(i).coloredName() + "+ " : "";
				RenderedTextBlock request = PixelScene.renderTextBlock(minimum
						+ Messages.titleCase(item.trueName()) + " x"
						+ String.format(Locale.US, "%,d", item.quantity()), 6);
				request.maxWidth((int)width - 8);
				request.setPos(x + 4, y + cursor);
				add(request);
				cursor = request.bottom() - y + 2;
			}
		}
        resource("Gold", Icons.get(Icons.COIN_SML), payload.gold(), Dungeon.homebase.goldAmount(), payload::gold);
        resource("Energy", Icons.get(Icons.ENERGY_SML), payload.energy(), Dungeon.homebase.energyAmount(), payload::energy);
        for (HomebaseState.Material material : HomebaseState.Material.values())
            resource(WndHomebaseFacility.materialName(material), new ItemSprite(WndHomebaseFacility.materialIcon(material)),
                    payload.material(material), Dungeon.homebase.amount(material), value -> payload.material(material, value));
        for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values())
            resource(WndHomebaseFacility.forgeName(resource), new ItemSprite(WndHomebaseFacility.forgeIcon(resource)),
                    payload.forge(resource), Dungeon.homebase.forgeResourceAmount(resource), value -> payload.forge(resource, value));
        if (column == 1) cursor += 18;
        height = Math.max(HEIGHT, cursor + 2);
    }

    private interface Amount { void set(int value); }

    private void resource(String name, Image icon, int selected, int owned, Amount setter) {
        float gap = 4, iconSize = 12, plusSize = 16, rowHeight = 16;
        float columnWidth = (width - 6 - gap) / 2f;
        float left = x + 3 + column * (columnWidth + gap);
        float scale = Math.min(iconSize / icon.width, iconSize / icon.height);
        icon.scale.set(scale); icon.x = left; icon.y = y + cursor + (rowHeight - icon.height()) / 2f;
        PixelScene.align(icon); add(icon);
        float plusX = left + columnWidth - plusSize;
        RenderedTextBlock amount = PixelScene.renderTextBlock(optional
				? WndHomebaseFacility.compactAmount(selected)
				: WndHomebaseFacility.compactAmount(selected) + "/" + WndHomebaseFacility.compactAmount(owned), 6);
        amount.maxWidth((int)(plusX - left - iconSize - 4));
        amount.setPos(left + iconSize + 2, y + cursor + (rowHeight - amount.height()) / 2f); add(amount);
        RedButton plus = new RedButton("+", 9) {
            @Override protected void onClick() {
				amount(name, optional ? Integer.MAX_VALUE : owned, selected, setter, optional);
			}
        };
        plus.setRect(plusX, y + cursor, plusSize, rowHeight); add(plus);
        if (++column >= 2) { column = 0; cursor += 18; }
    }

    private void amount(String name, int maximum, int value, Amount setter) {
		amount(name, maximum, value, setter, false);
	}

    private void amount(String name, int maximum, int value, Amount setter, boolean unlimited) {
        GameScene.show(new WndTextInput(name, unlimited ? "_Requested amount:_ no ownership limit"
				: "_Owned:_ " + maximum, Integer.toString(value), 10, false, "Set", "Cancel") {
            @Override public void onSelect(boolean yes, String input) {
                if (yes) try {
                    long number = Long.parseLong(input.trim());
                    if (number < 0 || number > maximum) throw new NumberFormatException();
                    setter.set((int)number);
                } catch (Exception error) {
					WndGlobalTrade.notice(unlimited ? "Enter an amount from 0 to 2,147,483,647."
							: "Enter an amount between 0 and " + maximum + ".");
				}
                refresh();
            }
        });
    }

    private void choose(int slot) {
        if (payload.item(slot) != null) {
            GameScene.show(new WndOptions("Selected Item", "Remove this item from the listing?", "Remove", "Keep") {
                @Override protected void onSelect(int index) {
                    if (index == 0) { sources[slot] = null; quantities[slot] = 0; payload.item(slot, null); refresh(); }
                }
            });
            return;
        }
		if (optional) {
			GameScene.show(new WndMarketplaceCatalog(item -> configureRequestedItem(slot, item)));
			return;
		}
        GameScene.show(WndBag.getBag(new WndBag.ItemSelector() {
            @Override public String textPrompt() { return optional ? "Choose an example of the item you want" : "Choose an item to list"; }
            @Override public boolean itemSelectable(Item item) {
                if (item == null || item instanceof Bag || item.isEquipped(Dungeon.hero)) return false;
                for (Item selected : sources) if (selected == item) return false;
                return true;
            }
            @Override public void onSelect(Item item) {
                if (item == null) { refresh(); return; }
                if (item.quantity() > 1) amount(item.name(), item.quantity(), item.quantity(), value -> {
                    if (value > 0) setItem(slot, item, value);
                });
                else { setItem(slot, item, 1); refresh(); }
            }
        }));
    }

	private void configureRequestedItem(int slot, Item item) {
		if (item == null) return;
		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			if (i != slot && payload.itemTemplate(i) && payload.item(i) != null
					&& payload.item(i).getClass() == item.getClass()) {
				WndGlobalTrade.notice("That requested item is already listed. Increase its quantity instead.");
				return;
			}
		}
		if (item.stackable) {
			amount(item.trueName(), Integer.MAX_VALUE, 1, value -> {
				if (value > 0) chooseMinimumRarity(slot, item, value);
			}, true);
		} else {
			chooseMinimumRarity(slot, item, 1);
		}
	}

	private void chooseMinimumRarity(int slot, Item item, int quantity) {
		if (!item.supportsRarityStats()) {
			setRequestedItem(slot, item, quantity, null);
			return;
		}
		ItemRarity[] rarities = ItemRarity.values();
		String[] options = new String[rarities.length + 1];
		for (int i = 0; i < rarities.length; i++) options[i] = rarities[i].coloredName();
		options[rarities.length] = "Cancel";
		GameScene.show(new WndOptions(new ItemSprite(item), Messages.titleCase(item.trueName()),
				"Choose the minimum acceptable rarity.", options) {
			@Override
			protected void onSelect(int index) {
				if (index >= 0 && index < rarities.length) {
					setRequestedItem(slot, item, quantity, rarities[index]);
				}
			}
		});
	}

	private void setRequestedItem(int slot, Item item, int quantity, ItemRarity minimumRarity) {
		item.quantity(quantity);
		item.identifyForPreview();
		if (item.supportsRarityStats()) item.rarityForPreview(minimumRarity);
		payload.item(slot, item);
		payload.itemTemplate(slot, true);
		payload.minimumRarity(slot, minimumRarity);
		sources[slot] = null;
		quantities[slot] = 0;
		refresh();
	}

    private void setItem(int slot, Item item, int quantity) {
        WayfarerTradePayload copy = new WayfarerTradePayload(); copy.item(slot, item);
        Item preview = copy.copy().item(slot); preview.quantity(quantity);
        sources[slot] = item; quantities[slot] = quantity; payload.item(slot, preview);
		refresh();
    }

	private void refresh() {
		if (members == null) return;
		if (changed != null) changed.run(); else rebuild();
	}
}
