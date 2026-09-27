/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Character-level marketplace escrow with durable local delivery receipts. */
public final class WayfarerMarketplace {
    private WayfarerMarketplace() {}

    public static boolean working;
    private static String cachedRows = "[]";
    private static String cachedCharacter = "";
    private static long nextPoll;
    private static boolean loading;

	private static void ensureCharacter() {
		String current = Dungeon.wayfarerCharacterId();
		if (current == null) current = "";
		if (!current.equals(cachedCharacter)) {
			cachedCharacter = current;
			cachedRows = "[]";
			nextPoll = 0;
			loading = false;
			working = false;
		}
	}

    public interface ListingsCallback {
        void completed(WayfarerAccountService.Result result, JsonValue rows);
    }

    private static JsonValue journal() {
        try { return new JsonReader().parse(Dungeon.marketplaceJournal); }
        catch (RuntimeException error) { return new JsonValue(JsonValue.ValueType.object); }
    }

    private static void put(JsonValue object, String key, String value) {
        object.remove(key);
        object.addChild(key, new JsonValue(value == null ? "" : value));
    }

    private static void save(JsonValue value) throws IOException {
        String previous = Dungeon.marketplaceJournal;
        Dungeon.marketplaceJournal = value.toJson(JsonWriter.OutputType.json);
        if (!Dungeon.saveGameChecked(GamesInProgress.curSlot)) {
            Dungeon.marketplaceJournal = previous;
            throw new IOException("Could not save the marketplace receipt. Retry before closing the game.");
        }
    }

    public static JsonValue rows() {
		ensureCharacter();
        try { return new JsonReader().parse(cachedRows); }
        catch (RuntimeException error) { return new JsonValue(JsonValue.ValueType.array); }
    }

    public static void refreshSoon() { ensureCharacter(); nextPoll = 0; }

    public static void poll() {
		ensureCharacter();
        if (!WayfarerAccountService.isSignedIn() || !WayfarerAccountService.currentCharacterEligible()
                || loading || working || System.currentTimeMillis() < nextPoll) return;
        JsonValue pending = firstPendingDeposit();
        if (pending != null) {
            retryDeposit(pending.name, null);
            nextPoll = System.currentTimeMillis() + 5000;
            return;
        }
        load(null);
    }

    public static void load(ListingsCallback callback) {
		ensureCharacter();
        if (loading) return;
        loading = true;
        WayfarerAccountService.marketplaceListings((result, data) -> {
            loading = false;
            nextPoll = System.currentTimeMillis() + 5000;
            if (result.success && data != null) cachedRows = data.toJson(JsonWriter.OutputType.json);
            if (callback != null) callback.completed(result, rows());
        });
    }

    public static boolean hasAttention() {
        String character = Dungeon.wayfarerCharacterId();
        JsonValue values = rows();
        for (JsonValue row = values.child; row != null; row = row.next) {
            String state = row.getString("state", "");
            boolean mine = character.equals(row.getString("seller", ""));
            boolean buyer = character.equals(row.getString("buyer", ""));
            if (mine && "matched".equals(state) && !row.getBoolean("seller_claimed", false)) return true;
            if (mine && ("cancelled".equals(state) || "expired".equals(state))
                    && row.getBoolean("seller_deposited", false) && !row.getBoolean("seller_claimed", false)) return true;
            if (buyer && "matched".equals(state) && !row.getBoolean("buyer_claimed", false)) return true;
        }
        return false;
    }

    public static int attentionCount() {
        int count = 0;
        String character = Dungeon.wayfarerCharacterId();
        for (JsonValue row = rows().child; row != null; row = row.next) {
            String state = row.getString("state", "");
            boolean mine = character.equals(row.getString("seller", ""));
            boolean buyer = character.equals(row.getString("buyer", ""));
            if (mine && "matched".equals(state) && !row.getBoolean("seller_claimed", false)) count++;
            else if (mine && ("cancelled".equals(state) || "expired".equals(state))
                    && row.getBoolean("seller_deposited", false) && !row.getBoolean("seller_claimed", false)) count++;
            else if (buyer && "matched".equals(state) && !row.getBoolean("buyer_claimed", false)) count++;
        }
        return count;
    }

    public static boolean claimable(JsonValue row) {
        if (row == null) return false;
        String character = Dungeon.wayfarerCharacterId();
        boolean mine = character.equals(row.getString("seller", ""));
        boolean buyer = character.equals(row.getString("buyer", ""));
        String state = row.getString("state", "");
        if (mine && "matched".equals(state)) return !row.getBoolean("seller_claimed", false);
        if (mine && ("cancelled".equals(state) || "expired".equals(state)))
            return row.getBoolean("seller_deposited", false) && !row.getBoolean("seller_claimed", false);
        return buyer && "matched".equals(state) && !row.getBoolean("buyer_claimed", false);
    }

    public static long secondsRemaining(JsonValue row) {
        try { return Math.max(0, (serverTime(row.getString("expires_at")) - System.currentTimeMillis()) / 1000); }
        catch (Exception ignored) { return 0; }
    }

    private static long serverTime(String value) throws Exception {
        if (value == null || value.length() < 19) return 0;
        String base = value.substring(0, 19), fraction = "000", zone = "+0000";
        int dot = value.indexOf('.', 19);
        int zoneAt = value.endsWith("Z") ? value.length() - 1 : value.lastIndexOf('+');
        if (zoneAt < 19) zoneAt = value.lastIndexOf('-');
        if (dot >= 0) {
            int end = zoneAt > dot ? zoneAt : value.length();
            String raw = value.substring(dot + 1, end);
            fraction = (raw + "000").substring(0, 3);
        }
        if (zoneAt >= 19 && !value.endsWith("Z")) zone = value.substring(zoneAt).replace(":", "");
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT)
                .parse(base + "." + fraction + zone).getTime();
    }

    public static void post(WayfarerTradePayload offer, WayfarerTradePayload request,
            Item[] sources, int[] quantities, WayfarerAccountService.ResultCallback done) {
		ensureCharacter();
        if (working) return;
        final String id = UUID.randomUUID().toString();
        final boolean exact = request != null && !request.isEmpty();
        final int fee = exact ? WayfarerTradePayload.emeraldShare(offer, request, true) : 0;
        offer.reservedEmeraldCost(fee);
        if (exact) request.reservedEmeraldCost(WayfarerTradePayload.emeraldShare(request, offer, false));
        final String packet = offer.toPacket();
        final String requested = exact ? request.toPacket() : null;
        try { validateSubmission(packet, offer, sources, quantities, fee); }
        catch (Exception error) { done.completed(failure(error)); return; }

        working = true;
        WayfarerAccountService.marketplaceAction(id, "prepare", packet, requested, null, (result, row) -> {
            if (!result.success) { working = false; done.completed(result); return; }
            Item[] withdrawn = new Item[WayfarerTradePayload.ITEM_SLOTS];
            boolean charged = false;
            try {
                validateSubmission(packet, offer, sources, quantities, fee);
                charged = withdraw(offer, sources, quantities, withdrawn, fee);
                JsonValue all = journal(), entry = new JsonValue(JsonValue.ValueType.object);
                put(entry, "stage", "seller_deposit"); put(entry, "offer", packet);
                put(entry, "request", requested); put(entry, "emerald_fee", Integer.toString(fee));
                all.addChild(id, entry); save(all);
                working = false;
                retryDeposit(id, done);
            } catch (Exception error) {
                restore(offer, withdrawn, charged, fee);
                WayfarerAccountService.marketplaceAction(id, "cancel", null, null, null, (ignored, value) -> {
                    working = false; done.completed(failure(error));
                });
            }
        });
    }

    public static void fulfill(JsonValue listing, WayfarerAccountService.ResultCallback done) {
		ensureCharacter();
        if (working || listing == null) return;
        final String id = listing.getString("listing_id", "");
        try {
            final WayfarerTradePayload requested = WayfarerGlobalTrade.decode(
                    listing.getString("requested_offer", ""));
            final WayfarerTradePayload offered = WayfarerGlobalTrade.decode(listing.getString("seller_offer", ""));
            final Item[] sources = matchingSources(requested);
            final int[] quantities = quantities(requested);
            final WayfarerTradePayload request = fulfilledPayload(requested, sources, quantities);
            final String packet = request.toPacket();
            final int fee = WayfarerTradePayload.emeraldShare(request, offered, false);
            validateSubmission(packet, request, sources, quantities, fee);
            working = true;
            WayfarerAccountService.marketplaceAction(id, "reserve", null, null, packet, (reserved, row) -> {
                if (!reserved.success) { working = false; done.completed(reserved); return; }
                Item[] withdrawn = new Item[WayfarerTradePayload.ITEM_SLOTS];
                boolean charged = false;
                try {
                    validateSubmission(packet, request, sources, quantities, fee);
                    charged = withdraw(request, sources, quantities, withdrawn, fee);
                    JsonValue all = journal(), entry = new JsonValue(JsonValue.ValueType.object);
                    put(entry, "stage", "buyer_deposit"); put(entry, "offer", packet);
                    put(entry, "emerald_fee", Integer.toString(fee));
                    all.addChild(id, entry); save(all);
                    working = false;
                    retryDeposit(id, done);
                } catch (Exception error) {
                    restore(request, withdrawn, charged, fee);
                    WayfarerAccountService.marketplaceAction(id, "release", null, null, null, (ignored, value) -> {
                        working = false; done.completed(failure(error));
                    });
                }
            });
        } catch (Exception error) { done.completed(failure(error)); }
    }

    public static void cancel(String id, WayfarerAccountService.ResultCallback done) {
		ensureCharacter();
        if (working) return;
        working = true;
        WayfarerAccountService.marketplaceAction(id, "cancel", null, null, null, (result, row) -> {
            working = false; refreshSoon(); done.completed(result);
        });
    }

    private static JsonValue firstPendingDeposit() {
        JsonValue all = journal();
        for (JsonValue entry = all.child; entry != null; entry = entry.next) {
            String stage = entry.getString("stage", "");
            if ("seller_deposit".equals(stage) || "buyer_deposit".equals(stage)) return entry;
        }
        return null;
    }

    private static void retryDeposit(String id, WayfarerAccountService.ResultCallback done) {
        if (working) return;
        JsonValue entry = journal().get(id);
        if (entry == null) return;
        String stage = entry.getString("stage", "");
        String action = "seller_deposit".equals(stage) ? "deposit" : "fulfill";
        String payload = "buyer_deposit".equals(stage) ? entry.getString("offer", null) : null;
        working = true;
        WayfarerAccountService.marketplaceAction(id, action, null, null, payload, (result, row) -> {
            working = false;
            if (result.success) {
                try {
                    JsonValue all = journal(), saved = all.get(id);
                    if (saved != null) { put(saved, "stage", "escrow"); save(all); }
                    refreshSoon();
                } catch (IOException error) {
                    result = failure(error);
                }
            }
            if (done != null) done.completed(result);
        });
    }

    public static void claim(JsonValue listing, WayfarerAccountService.ResultCallback done) {
		ensureCharacter();
        if (working || listing == null) return;
        final String id = listing.getString("listing_id", "");
        JsonValue local = journal().get(id);
        final int paidFee = local == null ? 0 : Math.max(0, local.getInt("emerald_fee", 0));
        working = true;
        WayfarerAccountService.marketplaceAction(id, "claim", null, null,
                SPDSettings.wayfarerInstallationId(), (result, row) -> {
            working = false;
            if (!result.success) { done.completed(result); return; }
            try {
                if (row.getBoolean("claimed", false)) throw new IOException("This marketplace delivery was already claimed.");
                JsonValue all = journal(), entry = all.get(id);
                if (entry == null) { entry = new JsonValue(JsonValue.ValueType.object); all.addChild(id, entry); }
                put(entry, "stage", "granting"); put(entry, "remaining", row.getString("payload", ""));
                put(entry, "refund", Boolean.toString(row.getBoolean("refund", false)));
                put(entry, "emerald_fee", Integer.toString(paidFee));
                save(all); finishGrant(id); deliverItems(id); acknowledge(id, done);
            } catch (Exception error) { done.completed(failure(error)); }
        });
    }

    private static void acknowledge(String id, WayfarerAccountService.ResultCallback done) {
        if (working) return;
        working = true;
        WayfarerAccountService.marketplaceAction(id, "ack", null, null,
                SPDSettings.wayfarerInstallationId(), (result, row) -> {
            working = false;
            if (result.success) try {
                JsonValue all = journal(); all.remove(id); save(all); refreshSoon();
            } catch (IOException error) { done.completed(failure(error)); return; }
            done.completed(result);
        });
    }

    private static void finishGrant(String id) throws IOException {
        JsonValue all = journal(), entry = all.get(id);
        WayfarerTradePayload payload = WayfarerGlobalTrade.decode(entry.getString("remaining", ""));
        boolean refund = Boolean.parseBoolean(entry.getString("refund", "false"));
        int emeraldRefund = refund ? Math.max(0, entry.getInt("emerald_fee", 0)) : 0;
        HomebaseState homebase = Dungeon.homebase;
        validateCapacity(homebase, payload, emeraldRefund);
        homebase.addGold(payload.gold()); homebase.addEnergy(payload.energy());
        for (HomebaseState.Material material : HomebaseState.Material.values()) homebase.add(material, payload.material(material));
        for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) homebase.addForgeResource(resource, payload.forge(resource));
        if (emeraldRefund > 0) homebase.addEmeralds(emeraldRefund);
        WayfarerTradePayload items = new WayfarerTradePayload();
        for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) items.item(i, payload.item(i));
        put(entry, "stage", "received"); put(entry, "remaining", items.isEmpty() ? "" : items.toPacket());
        try { save(all); }
        catch (IOException error) {
            homebase.spendGold(payload.gold()); homebase.spendEnergy(payload.energy());
            for (HomebaseState.Material material : HomebaseState.Material.values()) homebase.spend(material, payload.material(material));
            for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) homebase.spendForgeResource(resource, payload.forge(resource));
            if (emeraldRefund > 0) homebase.spendEmeralds(emeraldRefund);
            throw error;
        }
    }

    private static void deliverItems(String id) throws IOException {
        JsonValue all = journal(), entry = all.get(id);
        String packet = entry.getString("remaining", "");
        if (packet.isEmpty()) return;
        if (Dungeon.hero == null || Dungeon.level == null) throw new IOException("Enter the active game to receive these items.");
        WayfarerTradePayload payload = WayfarerGlobalTrade.decode(packet);
        boolean floorChanged = false;
        for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) if (payload.item(i) != null) {
            String marker = "market:" + id + ":" + i;
            if (!alreadyDelivered(marker)) {
                Item item = payload.item(i); item.wayfarerDeliveryId(marker);
                if (!item.collect(Dungeon.hero.belongings.backpack)) {
                    Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop(); floorChanged = true;
                }
            }
            payload.item(i, null);
        }
        if (floorChanged) Dungeon.saveLevel(GamesInProgress.curSlot);
        save(all); put(entry, "remaining", ""); save(all);
        boolean cleaned = false;
        for (Item item : Dungeon.hero.belongings) if (item.wayfarerDeliveryId().startsWith("market:" + id + ":")) {
            item.wayfarerDeliveryId(""); cleaned = true;
        }
        if (cleaned) Dungeon.saveGameChecked(GamesInProgress.curSlot);
    }

    private static boolean alreadyDelivered(String marker) {
        for (Item item : Dungeon.hero.belongings) if (marker.equals(item.wayfarerDeliveryId())) return true;
        for (Heap heap : Dungeon.level.heaps.valueList()) for (Item item : heap.items)
            if (marker.equals(item.wayfarerDeliveryId())) return true;
        return false;
    }

    private static Item[] matchingSources(WayfarerTradePayload target) throws IOException {
        Item[] result = new Item[WayfarerTradePayload.ITEM_SLOTS];
        Set<Item> used = new HashSet<>();
        for (int slot = 0; slot < result.length; slot++) {
            Item wanted = target.item(slot);
            if (wanted == null) continue;
            for (Item candidate : Dungeon.hero.belongings) {
                boolean matches = target.itemTemplate(slot)
                        ? candidate.getClass() == wanted.getClass() && meetsMinimumRarity(candidate, target.minimumRarity(slot))
                        : wanted.isSimilar(candidate);
                if (!used.contains(candidate) && !(candidate instanceof Bag) && !candidate.isEquipped(Dungeon.hero)
                        && candidate.quantity() >= wanted.quantity() && matches) {
                    result[slot] = candidate; used.add(candidate); break;
                }
            }
            if (result[slot] == null) {
                String rarity = target.itemTemplate(slot) && wanted.supportsRarityStats()
                        ? " at " + target.minimumRarity(slot).displayName() + " rarity or higher" : "";
                throw new IOException("You do not have the requested " + wanted.trueName() + rarity
                        + " in the required quantity.");
            }
        }
        return result;
    }

    private static boolean meetsMinimumRarity(Item candidate, ItemRarity minimum) {
        if (!candidate.supportsRarityStats()) return true;
        return candidate.hasRarityRoll() && candidate.rarity().power() >= minimum.power();
    }

    private static WayfarerTradePayload fulfilledPayload(WayfarerTradePayload requested,
            Item[] sources, int[] quantities) {
        WayfarerTradePayload result = requested.copy();
        for (int slot = 0; slot < WayfarerTradePayload.ITEM_SLOTS; slot++) {
            if (sources[slot] == null) continue;
            WayfarerTradePayload itemCopy = new WayfarerTradePayload();
            itemCopy.item(0, sources[slot]);
            Item actual = itemCopy.copy().item(0);
            actual.quantity(quantities[slot]);
            result.item(slot, actual);
            result.itemTemplate(slot, false);
            result.minimumRarity(slot, null);
        }
        return result;
    }

    private static int[] quantities(WayfarerTradePayload payload) {
        int[] values = new int[WayfarerTradePayload.ITEM_SLOTS];
        for (int i = 0; i < values.length; i++) if (payload.item(i) != null) values[i] = payload.item(i).quantity();
        return values;
    }

    private static void validateSubmission(String packet, WayfarerTradePayload offer,
            Item[] sources, int[] quantities, int emeraldFee) throws IOException {
        if (offer == null || offer.isEmpty()) throw new IOException("Offer something before continuing.");
        WayfarerGlobalTrade.decode(packet);
        HomebaseState homebase = Dungeon.homebase;
        if (homebase == null || homebase.goldAmount() < offer.gold() || homebase.energyAmount() < offer.energy()
                || homebase.emeraldAmount() < emeraldFee) throw new IOException("Not enough resources or Emeralds.");
        for (HomebaseState.Material material : HomebaseState.Material.values())
            if (homebase.amount(material) < offer.material(material)) throw new IOException("Not enough materials.");
        for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values())
            if (homebase.forgeResourceAmount(resource) < offer.forge(resource)) throw new IOException("Not enough forge resources.");
        for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
            Item source = sources[i], item = offer.item(i);
            if ((source == null) != (item == null)) throw new IOException("An item selection changed.");
            if (source != null && (!Dungeon.hero.belongings.contains(source) || source.isEquipped(Dungeon.hero)
                    || source instanceof Bag || quantities[i] < 1 || source.quantity() < quantities[i]
                    || item.quantity() != quantities[i])) throw new IOException("An offered item changed.");
        }
    }

    private static boolean withdraw(WayfarerTradePayload offer, Item[] sources, int[] quantities,
            Item[] withdrawn, int emeraldFee) throws IOException {
        for (int i = 0; i < sources.length; i++) if (sources[i] != null) {
            withdrawn[i] = quantities[i] < sources[i].quantity() ? sources[i].split(quantities[i])
                    : sources[i].detachAll(Dungeon.hero.belongings.backpack);
            if (withdrawn[i] == null) throw new IOException("An item could not be reserved.");
            sources[i].updateQuickslot();
        }
        HomebaseState homebase = Dungeon.homebase;
        homebase.spendGold(offer.gold()); homebase.spendEnergy(offer.energy());
        for (HomebaseState.Material material : HomebaseState.Material.values()) homebase.spend(material, offer.material(material));
        for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) homebase.spendForgeResource(resource, offer.forge(resource));
        if (emeraldFee > 0 && !homebase.spendEmeralds(emeraldFee)) throw new IOException("Not enough Emeralds.");
        return true;
    }

    private static void restore(WayfarerTradePayload offer, Item[] withdrawn, boolean charged, int emeraldFee) {
        HomebaseState homebase = Dungeon.homebase;
        if (charged && homebase != null && !HomebaseState.infiniteTestResourcesEnabled()) {
            homebase.addGold(offer.gold()); homebase.addEnergy(offer.energy()); homebase.addEmeralds(emeraldFee);
            for (HomebaseState.Material material : HomebaseState.Material.values()) homebase.add(material, offer.material(material));
            for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) homebase.addForgeResource(resource, offer.forge(resource));
        }
        if (Dungeon.hero != null) for (Item item : withdrawn) if (item != null
                && !item.collect(Dungeon.hero.belongings.backpack) && Dungeon.level != null)
            Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
    }

    private static void validateCapacity(HomebaseState homebase, WayfarerTradePayload payload, int emeralds) throws IOException {
        if (homebase == null || (long)homebase.goldAmount() + payload.gold() > Integer.MAX_VALUE
                || (long)homebase.energyAmount() + payload.energy() > Integer.MAX_VALUE
                || (long)homebase.emeraldAmount() + emeralds > Integer.MAX_VALUE) throw new IOException("Resource storage is full.");
        for (HomebaseState.Material material : HomebaseState.Material.values())
            if ((long)homebase.amount(material) + payload.material(material) > Integer.MAX_VALUE) throw new IOException("Material storage is full.");
        for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values())
            if ((long)homebase.forgeResourceAmount(resource) + payload.forge(resource) > Integer.MAX_VALUE) throw new IOException("Forge storage is full.");
    }

    private static WayfarerAccountService.Result failure(Exception error) {
        return new WayfarerAccountService.Result(false,
                error.getMessage() == null ? "Marketplace action failed." : error.getMessage());
    }
}
