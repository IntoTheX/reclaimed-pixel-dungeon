/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.rewards;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HeroClassUnlocks;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.items.Generator;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.SpatialGeode;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfExperience;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.ExoticPotion;
import com.erebus.reclaimedpixeldungeon.items.remains.ClassCallItem;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.erebus.reclaimedpixeldungeon.items.stones.StoneOfEnchantment;
import com.erebus.reclaimedpixeldungeon.items.Stylus;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/** Local, per-character rewards earned from active play. */
public final class GameplayRewards {

	public static final long REWARD_INTERVAL_MILLIS = 15L * 60L * 1000L;
	public static final long AFK_MILLIS = 3L * 60L * 1000L;

	private static long lastActionAt;
	private static long lastAccountedAt;
	private static long seedCounter;
	private static boolean saveRequired;

	private GameplayRewards() {}

	public static final class RewardOption {
		public final ItemRarity rarity;
		public final String key;
		public final long seed;

		private RewardOption( ItemRarity rarity, String key, long seed ) {
			this.rarity = rarity;
			this.key = key;
			this.seed = seed;
		}
	}

	public static final class ClaimResult {
		public final boolean success;
		public final String message;

		private ClaimResult( boolean success, String message ) {
			this.success = success;
			this.message = message;
		}
	}

	public static synchronized void resetSession() {
		lastActionAt = 0;
		lastAccountedAt = 0;
		saveRequired = false;
	}

	public static synchronized void recordActivity() {
		if (!eligible()) {
			resetSession();
			return;
		}
		long now = System.currentTimeMillis();
		if (lastActionAt == 0 || now - lastActionAt > AFK_MILLIS) {
			accrue( now );
			lastActionAt = now;
			lastAccountedAt = now;
			return;
		}
		accrue( now );
		lastActionAt = now;
	}

	public static synchronized void poll() {
		if (!eligible()) {
			resetSession();
			return;
		}
		accrue( System.currentTimeMillis() );
		saveIfReady();
	}

	public static synchronized int pendingRewards() {
		return Dungeon.gameplayRewardSeeds == null ? 0 : Dungeon.gameplayRewardSeeds.size();
	}

	public static synchronized long activeMillisTowardNext() {
		return Math.max( 0L, Math.min( REWARD_INTERVAL_MILLIS, Dungeon.gameplayRewardActiveMillis ) );
	}

	public static synchronized ArrayList<RewardOption> currentOptions() {
		ArrayList<RewardOption> options = new ArrayList<>();
		if (pendingRewards() == 0) return options;
		long rewardSeed = Dungeon.gameplayRewardSeeds.get( 0 );
		Random.pushGenerator( rewardSeed );
		try {
			HashSet<String> used = new HashSet<>();
			for (int i = 0; i < 3; i++) {
				RewardOption option;
				int attempts = 0;
				do {
					ItemRarity rarity = Item.rollRandomRarityTier();
					String key = pool( rarity )[Random.Int( pool( rarity ).length )];
					option = new RewardOption( rarity, key, Random.Long() );
				} while (!used.add( option.rarity.name() + ":" + option.key ) && ++attempts < 12);
				options.add( option );
			}
		} finally {
			Random.popGenerator();
		}
		return options;
	}

	public static synchronized ArrayList<Item> specialSelectionOptions( int optionIndex ) {
		ArrayList<Item> result = new ArrayList<>();
		ArrayList<RewardOption> options = currentOptions();
		if (optionIndex < 0 || optionIndex >= options.size()) return result;
		RewardOption option = options.get( optionIndex );
		if (!("artifact".equals( option.key ) || "trinket".equals( option.key ))) return result;
		Class<?>[] classes = "artifact".equals( option.key )
				? Generator.Category.ARTIFACT.classes : Generator.Category.TRINKET.classes;
		Random.pushGenerator( option.seed );
		try {
			HashSet<Integer> used = new HashSet<>();
			while (result.size() < 3 && used.size() < classes.length) {
				int index = Random.Int( classes.length );
				if (!used.add( index )) continue;
				@SuppressWarnings("unchecked")
				Item item = (Item)Reflection.newInstance( (Class<? extends Item>)classes[index] );
				result.add( item.random().randomizeRarityStats().identify() );
			}
		} finally {
			Random.popGenerator();
		}
		return result;
	}

	public static synchronized ClaimResult claim( int optionIndex, int specialIndex ) {
		if (!eligible() || pendingRewards() == 0) {
			return new ClaimResult( false, "Enter the active character before claiming this reward." );
		}
		ArrayList<RewardOption> options = currentOptions();
		if (optionIndex < 0 || optionIndex >= options.size()) {
			return new ClaimResult( false, "That reward choice is no longer available." );
		}
		RewardOption option = options.get( optionIndex );
		try {
			String delivered = applyReward( option, optionIndex, specialIndex );
			Dungeon.gameplayRewardSeeds.remove( 0 );
			Dungeon.saveAll();
			saveRequired = false;
			GLog.p( "Active play reward claimed: " + delivered );
			return new ClaimResult( true, delivered );
		} catch (Exception error) {
			Game.reportException( error );
			return new ClaimResult( false, error.getMessage() == null ? "The reward could not be delivered." : error.getMessage() );
		}
	}

	public static int rewardImage( RewardOption option ) {
		if (option == null) return ItemSpriteSheet.LOCKED_CHEST;
		if ("artifact".equals( option.key )) return ItemSpriteSheet.CRYSTAL_CHEST;
		if ("trinket".equals( option.key )) return ItemSpriteSheet.EBONY_CHEST;
		if (option.key.startsWith( "resources" )) return ItemSpriteSheet.PROVISION_CACHE;
		if (option.key.startsWith( "catalysts" )) return ItemSpriteSheet.ARCANE_RELIQUARY;
		if (option.key.startsWith( "geode" )) return new SpatialGeode().image;
		if (option.key.startsWith( "stone_enchant" )) return new StoneOfEnchantment().image;
		if (option.key.startsWith( "stylus" )) return new Stylus().image;
		if (option.key.startsWith( "scroll_enchant" )) return new ScrollOfEnchantment().image;
		if (option.key.startsWith( "experience" )) return new PotionOfExperience().image;
		if (option.key.startsWith( "transmutation" )) return new ScrollOfTransmutation().image;
		if (option.key.startsWith( "healing" )) return new PotionOfHealing().image;
		if (option.key.startsWith( "class_unlock" )) {
			ClassCallItem item = classUnlockItem( option.seed );
			return item == null ? ItemSpriteSheet.LOCKED_CHEST : item.image;
		}
		if (option.key.startsWith( "emeralds" )) return ItemSpriteSheet.HOMEBASE_EMERALD;
		if (option.key.startsWith( "random_scroll" ) || option.key.startsWith( "exotic_scroll" )) return ItemSpriteSheet.SCROLL_HOLDER;
		if (option.key.startsWith( "random_potion" ) || option.key.startsWith( "exotic_potion" )) return ItemSpriteSheet.POTION_HOLDER;
		if (option.key.startsWith( "random_stone" )) return ItemSpriteSheet.STONE_HOLDER;
		return ItemSpriteSheet.LOCKED_CHEST;
	}

	public static String rewardLabel( RewardOption option ) {
		String key = option == null ? "" : option.key;
		if ("artifact".equals( key )) return "Artifact Crystal Chest";
		if ("trinket".equals( key )) return "Trinket Ebony Chest";
		if (key.startsWith( "resources" )) return amount( key ) + " Random Resources";
		if (key.startsWith( "catalysts" )) return amount( key ) + " Random Catalysts";
		if (key.startsWith( "geode" )) return "1 Spatial Geode";
		if (key.startsWith( "class_unlock" )) return "1 Character Unlock Item";
		if (key.startsWith( "stone_enchant" )) return "1 Stone of Enchantment " + roman( amount( key ) );
		if (key.startsWith( "stylus" )) return "1 Arcane Stylus " + roman( amount( key ) );
		if (key.startsWith( "scroll_enchant" )) return "1 Scroll of Enchantment " + roman( amount( key ) );
		if (key.startsWith( "experience" )) return amount( key ) + " Potions of Experience";
		if (key.startsWith( "transmutation" )) return amount( key ) + " Scrolls of Transmutation";
		if (key.startsWith( "healing" )) return amount( key ) + " Potions of Healing";
		if (key.startsWith( "emeralds" )) return amount( key ) + " Emeralds";
		if (key.startsWith( "random_scroll" )) return amount( key ) + " Random Scrolls";
		if (key.startsWith( "random_potion" )) return amount( key ) + " Random Potions";
		if (key.startsWith( "random_stone" )) return amount( key ) + " Random Runestones";
		if (key.startsWith( "exotic_scroll" )) return amount( key ) + " Random Exotic Scrolls";
		if (key.startsWith( "exotic_potion" )) return amount( key ) + " Random Exotic Potions";
		return "Active Play Reward";
	}

	public static String rewardDescription( RewardOption option ) {
		String detail = rewardLabel( option ) + ".";
		if (option != null && ("artifact".equals( option.key ) || "trinket".equals( option.key ))) {
			detail = "Preview and choose 1 of 3 random " + ("artifact".equals( option.key ) ? "Artifacts" : "Trinkets")
					+ ". Each rolls its own rarity and stats.";
		} else if (option != null && (option.key.startsWith( "resources" ) || option.key.startsWith( "catalysts" ))) {
			detail += " The total is rolled across the complete reward pool without a three-type limit.";
		}
		return option == null ? detail : option.rarity.coloredName() + " reward.\n\n" + detail;
	}

	private static String applyReward( RewardOption option, int optionIndex, int specialIndex ) throws IOException {
		ArrayList<Item> items = new ArrayList<>();
		if ("artifact".equals( option.key ) || "trinket".equals( option.key )) {
			ArrayList<Item> choices = specialSelectionOptions( optionIndex );
			if (specialIndex < 0 || specialIndex >= choices.size()) throw new IOException( "Choose one of the three items first." );
			items.add( choices.get( specialIndex ) );
		} else {
			Random.pushGenerator( option.seed );
			try {
				String key = option.key;
				if (key.startsWith( "resources" )) addResources( amount( key ) );
				else if (key.startsWith( "catalysts" )) addRandomItems( items, amount( key ), "catalyst" );
				else if (key.startsWith( "geode" )) items.add( new SpatialGeode() );
				else if (key.startsWith( "class_unlock" )) {
					ClassCallItem unlock = classUnlockItem( option.seed );
					if (unlock == null) throw new IOException( "No character unlock item could be rolled." );
					items.add( unlock );
				} else if (key.startsWith( "stone_enchant" )) items.add( tier( new StoneOfEnchantment(), amount( key ) ) );
				else if (key.startsWith( "stylus" )) items.add( tier( new Stylus(), amount( key ) ) );
				else if (key.startsWith( "scroll_enchant" )) items.add( tier( new ScrollOfEnchantment(), amount( key ) ) );
				else if (key.startsWith( "experience" )) items.add( new PotionOfExperience().quantity( amount( key ) ) );
				else if (key.startsWith( "transmutation" )) items.add( new ScrollOfTransmutation().quantity( amount( key ) ) );
				else if (key.startsWith( "healing" )) items.add( new PotionOfHealing().quantity( amount( key ) ) );
				else if (key.startsWith( "emeralds" )) Dungeon.homebase.addEmeralds( amount( key ) );
				else if (key.startsWith( "random_scroll" )) addRandomItems( items, amount( key ), "scroll" );
				else if (key.startsWith( "random_potion" )) addRandomItems( items, amount( key ), "potion" );
				else if (key.startsWith( "random_stone" )) addRandomItems( items, amount( key ), "stone" );
				else if (key.startsWith( "exotic_scroll" )) addRandomItems( items, amount( key ), "exotic_scroll" );
				else if (key.startsWith( "exotic_potion" )) addRandomItems( items, amount( key ), "exotic_potion" );
				else throw new IOException( "That active play reward is not supported." );
			} finally {
				Random.popGenerator();
			}
		}
		for (Item item : items) {
			item.identify();
			if (!item.collect( Dungeon.hero.belongings.backpack )) {
				Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
			}
		}
		return rewardLabel( option );
	}

	private static void addResources( int total ) {
		HomebaseState.Material[] materials = HomebaseState.Material.values();
		HomebaseState.ForgeResource[] forge = HomebaseState.ForgeResource.values();
		for (int i = 0; i < total; i++) {
			int selected = Random.Int( materials.length + forge.length );
			if (selected < materials.length) Dungeon.homebase.add( materials[selected], 1 );
			else Dungeon.homebase.addForgeResource( forge[selected - materials.length], 1 );
		}
	}

	private static void addRandomItems( ArrayList<Item> target, int total, String type ) {
		LinkedHashMap<Class<? extends Item>, Item> stacks = new LinkedHashMap<>();
		for (int i = 0; i < total; i++) {
			Item item;
			switch (type) {
				case "catalyst": item = Generator.randomRarityCatalyst(); break;
				case "scroll": item = directCategoryItem( Generator.Category.SCROLL ); break;
				case "potion": item = directCategoryItem( Generator.Category.POTION ); break;
				case "stone": item = directCategoryItem( Generator.Category.STONE ); break;
				case "exotic_scroll": item = randomMappedItem( new ArrayList<Class<? extends Item>>( ExoticScroll.regToExo.values() ) ); break;
				default: item = randomMappedItem( new ArrayList<Class<? extends Item>>( ExoticPotion.regToExo.values() ) ); break;
			}
			@SuppressWarnings("unchecked") Class<? extends Item> cls = (Class<? extends Item>)item.getClass();
			Item stack = stacks.get( cls );
			if (stack == null) {
				item.quantity( 1 );
				stacks.put( cls, item );
			} else {
				stack.quantity( stack.quantity() + 1 );
			}
		}
		target.addAll( stacks.values() );
	}

	private static Item directCategoryItem( Generator.Category category ) {
		float[] chances = category.defaultProbs == null ? category.probs : category.defaultProbs;
		int index = chances == null ? Random.Int( category.classes.length ) : Random.chances( chances );
		if (index < 0) index = Random.Int( category.classes.length );
		@SuppressWarnings("unchecked") Class<? extends Item> cls = (Class<? extends Item>)category.classes[index];
		return Reflection.newInstance( cls ).random();
	}

	private static Item randomMappedItem( ArrayList<Class<? extends Item>> classes ) {
		return Reflection.newInstance( classes.get( Random.Int( classes.size() ) ) ).random();
	}

	private static Item tier( Item item, int tier ) {
		item.level( Math.max( 0, Math.min( 4, tier - 1 ) ) );
		return item;
	}

	private static ClassCallItem classUnlockItem( long seed ) {
		ArrayList<HeroClass> classes = new ArrayList<>();
		for (HeroClass heroClass : HeroClass.values()) {
			if (HeroClassUnlocks.canUnlock( heroClass )) classes.add( heroClass );
		}
		if (classes.isEmpty()) {
			for (HeroClass heroClass : HeroClass.values()) if (heroClass != HeroClass.WARRIOR) classes.add( heroClass );
		}
		if (classes.isEmpty()) return null;
		java.util.Random random = new java.util.Random( seed ^ 0x434C4153534C4F4EL );
		return ClassCallItem.get( classes.get( random.nextInt( classes.size() ) ) );
	}

	private static void accrue( long now ) {
		long active = activeMillisBetween( lastActionAt, lastAccountedAt, now );
		if (active <= 0) return;
		Dungeon.gameplayRewardActiveMillis += active;
		lastAccountedAt += active;
		boolean earned = false;
		while (Dungeon.gameplayRewardActiveMillis >= REWARD_INTERVAL_MILLIS) {
			Dungeon.gameplayRewardActiveMillis -= REWARD_INTERVAL_MILLIS;
			Dungeon.gameplayRewardSeeds.add( nextSeed() );
			earned = true;
		}
		if (earned) saveRequired = true;
	}

	private static void saveIfReady() {
		if (!saveRequired || Dungeon.hero == null || !Dungeon.hero.ready || Actor.processing()) return;
		try {
			Dungeon.saveAll();
			saveRequired = false;
		} catch (IOException error) {
			Game.reportException( error );
		}
	}

	public static long activeMillisBetween( long lastAction, long lastAccounted, long now ) {
		if (lastAction <= 0 || lastAccounted <= 0 || now <= lastAccounted) return 0;
		return Math.max( 0L, Math.min( now, lastAction + AFK_MILLIS ) - lastAccounted );
	}

	public static int completedIntervals( long activeMillis ) {
		return (int)Math.max( 0L, activeMillis / REWARD_INTERVAL_MILLIS );
	}

	private static long nextSeed() {
		long character = Dungeon.wayfarerCharacterId() == null ? 0 : Dungeon.wayfarerCharacterId().hashCode();
		return System.nanoTime() ^ System.currentTimeMillis() ^ (character << 32) ^ ++seedCounter;
	}

	private static boolean eligible() {
		return Dungeon.hero != null && Dungeon.level != null && Dungeon.homebase != null && Dungeon.hero.isAlive();
	}

	private static int amount( String key ) {
		int split = key.lastIndexOf( '_' );
		if (split < 0 || split == key.length() - 1) return 1;
		try {
			return Integer.parseInt( key.substring( split + 1 ) );
		} catch (NumberFormatException ignored) {
			return 1;
		}
	}

	private static String roman( int tier ) {
		return new String[]{"I", "II", "III", "IV", "V"}[Math.max( 1, Math.min( 5, tier ) ) - 1];
	}

	private static String[] pool( ItemRarity rarity ) {
		switch (rarity) {
			case TRANSCENDANT: return new String[]{
					"artifact", "trinket", "resources_1000", "catalysts_100", "geode_1", "class_unlock_1",
					"stone_enchant_5", "stylus_5", "scroll_enchant_5", "healing_100", "emeralds_30"};
			case LEGENDARY: return new String[]{
					"resources_500", "catalysts_25", "experience_10", "transmutation_10", "healing_50",
					"stone_enchant_4", "stylus_4", "scroll_enchant_4", "emeralds_20"};
			case EPIC: return new String[]{
					"resources_250", "catalysts_12", "experience_5", "transmutation_5", "healing_25",
					"stone_enchant_3", "stylus_3", "scroll_enchant_3", "emeralds_10"};
			case RARE: return new String[]{
					"resources_150", "catalysts_6", "experience_3", "transmutation_3", "healing_12",
					"stone_enchant_2", "stylus_2", "scroll_enchant_2", "emeralds_5"};
			case UNCOMMON: return new String[]{
					"resources_75", "catalysts_3", "experience_1", "transmutation_1", "healing_6",
					"stone_enchant_1", "stylus_1", "scroll_enchant_1", "emeralds_3"};
			case COMMON: default: return new String[]{
					"resources_75", "catalysts_3", "healing_3", "emeralds_1", "random_scroll_5",
					"random_potion_5", "random_stone_5", "exotic_scroll_5", "exotic_potion_5"};
		}
	}
}
