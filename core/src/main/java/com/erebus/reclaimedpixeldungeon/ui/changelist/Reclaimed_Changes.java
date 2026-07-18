/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.erebus.reclaimedpixeldungeon.ui.changelist;

import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.ChangesScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.Window;

import java.util.ArrayList;

public class Reclaimed_Changes {

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add_v0_1_1_Changes(changeInfos);
		add_v0_1_0_Changes(changeInfos);
	}

	public static void add_v0_1_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Rarity Balance",
				"Adjusted rarity stat limits and resistance behavior for the first Reclaimed balance pass.\n" +
				"\n" +
				"**-** Per-item _Transcendant_ resistance, proc, and chance stats now cap at 100%.\n" +
				"**-** Duplicate capped stat rolls now aggregate up to the cap instead of exceeding it.\n" +
				"**-** Transcendant upgrade choices stop offering already-capped chance, proc, and resistance stats."));

		changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), "Resistance Fixes",
				"Fixed several cases where 100% resistance still allowed status effects to slip through.\n" +
				"\n" +
				"**-** Fully resisted duration buffs now fail to attach instead of leaving a tiny remaining duration.\n" +
				"**-** Blob effects such as _Vertigo Gas_, fire, and corrosion now respect full rarity and homebase resistance.\n" +
				"**-** Fully resisted effects now show an _Immune_ text indicator instead of announcing the blocked debuff.\n" +
				"**-** Root effects now check resistance before announcing, so blocked roots no longer show _Root_.\n" +
				"**-** Cave spinner webs now use the same root-resistance check and immune feedback when their root is blocked.\n" +
				"**-** Mob resistance scaling now follows the same full-resistance behavior."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CALENDAR), "Founder's Camp Contracts",
				"Adjusted contract slot pacing so the Founder's Camp feels useful earlier.\n" +
				"\n" +
				"**-** The camp now gains contract capacity much more often as it is upgraded.\n" +
				"**-** Higher camp levels can support a much larger board of active settlement work.\n" +
				"**-** Contract growth now better matches long-term building progression."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.LOCKED_CHEST), "Jackpot Cache Rooms",
				"Added rarer, larger cache rooms as jackpot moments during dungeon exploration.\n" +
				"\n" +
				"**-** Large material cache rooms can appear with bigger piles of settlement resources.\n" +
				"**-** Large catalyst cache rooms can appear with a richer spread of catalyst runestones.\n" +
				"**-** These rooms are rarer than standard cache rooms and are meant to feel like a lucky break."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MATERIAL_SATCHEL), "Material Display",
				"Improved material currency display rules for the expanded inventory.\n" +
				"\n" +
				"**-** The homebase inventory display still shows all secured settlement materials.\n" +
				"**-** During dungeon runs, owning a _Material Satchel_ shows the current run's carried materials.\n" +
				"**-** Empty material resources stay hidden so the currency row stays cleaner."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SKULL), "Mob Level Scaling",
				"Updated enemy mob level scaling so the dungeon reacts to permanent settlement growth.\n" +
				"\n" +
				"**-** Newly spawned mobs now include a conservative mob level pressure bonus from homebase building levels.\n" +
				"**-** Permanent training levels also contribute to mob level pressure.\n" +
				"**-** Transcendant item levels now increase mob level pressure more sharply than normal upgrades.\n" +
				"**-** This scaling is separate from raid threat and still reads the current run's live mob level pressure."));
	}

	public static void add_v0_1_0_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.0", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.RECLAIMED), "Reclaimed Pixel Dungeon",
				"_Reclaimed Pixel Dungeon_ now has its own identity while preserving the GPLv3 foundation of Shattered Pixel Dungeon.\n" +
				"\n" +
				"**-** Added Reclaimed naming, title art, icon, and versioning.\n" +
				"**-** Updated support, crash contact, legal, and about text for this fork.\n" +
				"**-** Set _v0.1.0_ as the first Reclaimed release point for the roguelite fork."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STAIRS_GRASS), "Homebase Floor 0",
				"Added _floor 0_, a persistent homebase where the settlement begins to recover between dungeon runs.\n" +
				"\n" +
				"**-** Added custom terrain generation with water, grass, dirt paths, foliage, walls, gates, towers, and rebuilt structures.\n" +
				"**-** Successful returns strip expedition level and experience while preserving carried inventory.\n" +
				"**-** Death is harsher and clears run gear."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_EHWAZ), "Return Scroll",
				"Added the _Return Scroll_, a new scroll for ending an expedition early.\n" +
				"\n" +
				"**-** Sends the hero back to the homebase from inside a dungeon run.\n" +
				"**-** Uses its own scroll glyph and texture.\n" +
				"**-** Is always available through shops."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_ISAZ), "Scroll of Divination",
				"Improved the _Scroll of Divination_ so its insight stays useful after item types are learned.\n" +
				"\n" +
				"**-** It still identifies unknown potion colors, scroll runes, and ring gems first.\n" +
				"**-** If there are fewer unknown item types left, it now identifies unidentified carried items.\n" +
				"**-** The fallback scans the hero's inventory and nested bags."));

		changes = new ChangeInfo("Rarity Loot", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_PRISMFORGE), "Item Rarities",
				"Added item rarities from _Common_ through _Transcendant_ for most long-term equipment.\n" +
				"\n" +
				"**-** Gear, weapons, rings, artifacts, trinkets, wands, throwables, the Spirit Bow, and the Mage Staff can roll rarity stats.\n" +
				"**-** Rarity names, stat lines, lore, prices, and inventory aura glows now reflect item rarity.\n" +
				"**-** Rarity effects are hooked into real combat and utility behavior instead of being only display text."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Rarity Stats",
				"Added a broad rarity stat system inspired by RarityForge and Shattered Pixel Dungeon's existing effects.\n" +
				"\n" +
				"**-** Added offensive stats, defensive stats, proc chances, crit, lifesteal, XP gain, resource bonuses, and loot bonuses.\n" +
				"**-** Added wand charge/recharge stats, throwable usage stats, and artifact/ring/trinket potency.\n" +
				"**-** Debuff resistances are separated by effect rather than grouped together.\n" +
				"**-** Stats appear in item lore, catalog entries, hero stats, mob inspection, and defender inspection."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_FRACTURED_NEXUS), "Transcendant Growth",
				"_Transcendant_ items now grow beyond a normal rarity roll.\n" +
				"\n" +
				"**-** Transcendant items gain internal XP and display a progress bar.\n" +
				"**-** Leveling them offers a pick-one-of-three stat upgrade choice.\n" +
				"**-** Scrolls of Upgrade, artifact growth, trinket upgrades, Spirit Bow growth, and forge upgrades can improve rarity stat values."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_TOOLKIT), "Uncapped Artifacts and Wands",
				"Removed several limits so long-term roguelite builds can keep growing.\n" +
				"\n" +
				"**-** Removed max artifact level limits for continued artifact growth.\n" +
				"**-** Removed max wand charge limits so charge bonuses can keep mattering.\n" +
				"**-** Scrolls of Upgrade can upgrade artifacts once their _original artifact level_ reaches 10, before artifact potency or homebase potency are counted."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_OBLIVION_SEAL), "Catalyst Runestones",
				"Added _catalyst runestones_ that modify rarity stats directly.\n" +
				"\n" +
				"**-** Added Reforge Conflux, Aetherflux, Fractured Nexus, Reshaper's Crucible, Oblivion Seal, Fateweaver, Ascendant Spark, and Prismforge.\n" +
				"**-** Catalysts can drop from monsters, appear in cache rooms, and be sold by shops.\n" +
				"**-** Catalyst stones can be stored in the Material Satchel."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.TRINKET_CATA), "Trinket Catalyst Cycling",
				"Improved _Magical Catalyst_ trinket generation so new trinkets appear before duplicates.\n" +
				"\n" +
				"**-** The catalyst checks trinkets in the hero's inventory, bags, and protected vault storage.\n" +
				"**-** Unowned trinkets are offered first.\n" +
				"**-** Once every trinket has been owned, generation cycles into second copies, then third copies, and so on."));

		changes = new ChangeInfo("Settlement Progression", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.HOMEBASE_WOOD), "Recovered Materials",
				"Added persistent homebase resources for rebuilding and training.\n" +
				"\n" +
				"**-** Added wood, stone, copper ore, iron ore, gold ore, scraps, ember shards, and ember cores.\n" +
				"**-** Materials can drop from monsters, chests, raids, secret rooms, and locked rooms.\n" +
				"**-** Deeper floors can produce larger material amounts.\n" +
				"**-** Resource icons now appear near gold and energy while at the homebase."));

		changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "Permanent Training",
				"Added long-term stat training through rebuilt homebase facilities.\n" +
				"\n" +
				"**-** Added starting stats, talent points, combat bonuses, loot bonuses, movement, attack speed, wand support, artifact support, and trinket support.\n" +
				"**-** Added many separate debuff resistance upgrades.\n" +
				"**-** Buildings can level far beyond their first few upgrades and unlock more training options as they improve."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.CHEST), "Quartermaster's Vault",
				"Added the _Quartermaster's Vault_ for protected settlement storage.\n" +
				"\n" +
				"**-** Store items safely between expeditions.\n" +
				"**-** Upgrade storage capacity over time.\n" +
				"**-** Items left loose around the base can be lost when an expedition ends."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.HOMEBASE_EMBER), "Emberforge",
				"Added the _Emberforge_ for salvaging and equipment upgrading.\n" +
				"\n" +
				"**-** Salvage identified gear for materials.\n" +
				"**-** Salvage cursed or unidentified gear at lower yield.\n" +
				"**-** Salvage runestones, catalyst stones, scrolls, and exotic scrolls.\n" +
				"**-** Upgrade eligible gear and artifacts that have reached their natural level cap."));

		changes.addButton(new ChangeButton(Icons.get(Icons.ALCHEMY), "Alchemist's Still",
				"Moved alchemy into its own homebase facility screen.\n" +
				"\n" +
				"**-** Added trinket upgrade support from the Still.\n" +
				"**-** Added visible rarity-stat-upgrade feedback for trinket upgrading.\n" +
				"**-** Gold and alchemy energy now persist between dungeon runs."));

		changes.addButton(new ChangeButton(Icons.get(Icons.GRASS), "Moonroot Garden",
				"Added the _Moonroot Garden_ as the settlement's regrowing natural resource.\n" +
				"\n" +
				"**-** Upgrade garden beds through the facility screen.\n" +
				"**-** Beds can regrow seed plants over time.\n" +
				"**-** Harvestable plots show the plant sprite for what is ready to collect."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CALENDAR), "Founder's Camp",
				"Added settlement work to the _Founder's Camp_.\n" +
				"\n" +
				"**-** Added settlement requests and contracts.\n" +
				"**-** Contracts include resource submissions, bounty missions, scouting missions, and recovery missions.\n" +
				"**-** More contract slots unlock as the camp grows."));

		changes = new ChangeInfo("Inventory and Equipment", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MATERIAL_SATCHEL), "New Bags",
				"Added several new bag types for the expanded roguelite economy.\n" +
				"\n" +
				"**-** Added the Material Satchel, Trinket Bag, Key Holder, Artifact Bag, and Food Bag.\n" +
				"**-** Custom bags have distinct textures and appear in shops after vanilla bags are purchased.\n" +
				"**-** Custom bags count toward bag collection achievements.\n" +
				"**-** Trinkets inside the Trinket Bag apply their rarity stats."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_HOLDER), "Expanded Equipment",
				"Expanded the equipment layout for longer roguelite builds.\n" +
				"\n" +
				"**-** Heroes can wear more rings and artifacts.\n" +
				"**-** Added extra ring slots, artifact slots, and mixed ring/artifact slots.\n" +
				"**-** The expanded layout applies on desktop and mobile."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.KEY_HOLDER), "Keys and Ankhs",
				"Added the _Key Holder_ for compact storage of rings and ankhs.\n" +
				"\n" +
				"**-** Normal and blessed ankhs can be stored in the holder.\n" +
				"**-** Ankhs inside the holder can still trigger their normal death-prevention behavior."));

		changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK_LRG), "Inventory UI",
				"Improved inventory readability for the larger item and bag economy.\n" +
				"\n" +
				"**-** Added resource displays and custom bag tab icons.\n" +
				"**-** Improved bag tab sizing, including desktop multi-row bag tabs.\n" +
				"**-** Improved rarity auras and stat/lore readability."));

		changes = new ChangeInfo("Dungeons and Threat", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.LOCKED_CHEST), "Cache Rooms",
				"Added new cache rooms for Reclaimed's permanent progression resources.\n" +
				"\n" +
				"**-** Added secret catalyst cache rooms.\n" +
				"**-** Added secret material cache rooms.\n" +
				"**-** Added locked versions of both cache room types."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SKULL), "Mob Levels and Stats",
				"Added scaling enemy stats that are separate from raid threat.\n" +
				"\n" +
				"**-** Dungeon monsters, minibosses, bosses, guardians, statues, piranhas, and spawned enemies can gain levels and combat rarity stats.\n" +
				"**-** Newly spawned enemies should use the current expected mob level during a run.\n" +
				"**-** Enemy stat rolls focus on combat-relevant rarity stats."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "Raid Threat",
				"Added _raid threat_ as a separate long-term pressure system.\n" +
				"\n" +
				"**-** Threat increases from exploration, kills, chests, and locks.\n" +
				"**-** Threat carries through successful homebase returns.\n" +
				"**-** Threat resets only when the hero dies.\n" +
				"**-** Threat rolls toward future homebase raids instead of forcing a raid after every run."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_KEY), "Homebase Raids",
				"Added _homebase raids_ that turn the settlement into an active defense encounter.\n" +
				"\n" +
				"**-** Raids show a popup and a boss-bar-style alive/total counter.\n" +
				"**-** Raiders target buildings, walls, gates, and towers.\n" +
				"**-** Raider rosters can include dungeon enemies seeking revenge.\n" +
				"**-** Structures have durability, armor, resistance upgrades, repair costs, destroyed states, and raid restrictions."));

		changes = new ChangeInfo("Defenders", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Recruitable Defenders",
				"Added _homebase defenders_ who can be rescued and brought back to the settlement.\n" +
				"\n" +
				"**-** Defenders can appear in rare secret and locked rooms.\n" +
				"**-** They roll names, classes, rarity, stats, XP, and hero-class sprites.\n" +
				"**-** Defenders can patrol, idle, sleep to recover, scout for materials, and hunt raiders during raids."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER), "Defender Gear",
				"Added equipment management for defenders.\n" +
				"\n" +
				"**-** Defenders can equip weapons and armor.\n" +
				"**-** Strength requirements apply to their gear.\n" +
				"**-** Defender gear can use rarity stats.\n" +
				"**-** Armor can update defender sprites, and ranged/reach weapons affect combat behavior."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.POTION_CRIMSON), "Defender Gifts",
				"Added the foundation for giving defenders useful supplies.\n" +
				"\n" +
				"**-** Defenders are planned around gifts rather than natural strength gain.\n" +
				"**-** Strength potions, healing potions, scrolls of upgrade, and ankhs can support defender growth and survival."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Defender Progression",
				"Defenders now grow through their own XP and rarity stat progression.\n" +
				"\n" +
				"**-** Defenders gain XP, level up, and show XP progress.\n" +
				"**-** Leveling gives a chance to improve rarity stats.\n" +
				"**-** Every few levels can also offer a chance at an additional rarity stat."));

		changes = new ChangeInfo("Interface and Polish", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Hero Stats Tab",
				"Added a dedicated hero panel tab for total rarity stats.\n" +
				"\n" +
				"**-** Aggregates equipped item stats.\n" +
				"**-** Includes Spirit Bow stats even when the bow is not equipped.\n" +
				"**-** Includes homebase training.\n" +
				"**-** Includes trinkets in inventory and trinkets stored in the Trinket Bag."));

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Inspection Tabs",
				"Expanded inspection windows to reduce long stat walls.\n" +
				"\n" +
				"**-** Mob inspection now supports tabbed info and stats.\n" +
				"**-** Defender inspection supports info, stats, and gear views.\n" +
				"**-** Long rarity stat lists are easier to read on desktop and mobile."));

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Facility Screens",
				"Rebuilt homebase interactions into full facility screens.\n" +
				"\n" +
				"**-** Added tabbed screens for building functions, upgrades, and stats.\n" +
				"**-** Added scrolling training panels, resource readouts, resource tooltips, and upgrade feedback.\n" +
				"**-** Added alchemy-style animated backgrounds."));

		changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), "Fixes and Stabilization",
				"Fixed many fork-specific issues from the first Reclaimed development pass.\n" +
				"\n" +
				"**-** Fixed run resets, starter kits, wand recharge after returning, and duplicate bag sales.\n" +
				"**-** Fixed artifact, ring, trinket, Spirit Bow, Mage Staff, and rarity stat behavior.\n" +
				"**-** Fixed runestone textures, facility button layers, building collision, projectile rules, raids, defender UI, and playable build packaging."));
	}
}
