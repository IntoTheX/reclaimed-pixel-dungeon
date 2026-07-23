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

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.ChangesScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class Reclaimed_Changes {

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add_v0_1_6_Changes(changeInfos);
		add_v0_1_5_Changes(changeInfos);
		add_v0_1_4_Changes(changeInfos);
		add_v0_1_3_Changes(changeInfos);
		add_v0_1_2_Changes(changeInfos);
		add_v0_1_1_Changes(changeInfos);
		add_v0_1_0_Changes(changeInfos);
	}

	public static void add_v0_1_6_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.6", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SPIRIT_BOW), "Class Unlock Mechanic",
				"Added Reclaimed's new class-unlock progression for post-Amulet dungeon runs.\n" +
				"\n" +
				"**-** After the _Amulet of Yendor_ has been secured at the homebase, boss kills can drop class fragments.\n" +
				"**-** Before any extra class has been unlocked on that save, bosses have a 50% chance to drop an eligible fragment.\n" +
				"**-** After the first extra class is unlocked, that boss-fragment chance drops to 25%.\n" +
				"**-** Five matching fragments can be merged at an alchemy pot into a class call item, such as _Huntress' Call_ or _Arcanist's Oath_.\n" +
				"**-** Using that call item unlocks the matching hero class for that character save and awards the updated class unlock badge.\n" +
				"**-** Each character save can unlock up to two extra classes, encouraging each legacy to develop its own available roster.\n" +
				"**-** Fragment drops exclude the Warrior, who is unlocked by default, and also exclude classes already unlocked on that save.\n" +
				"**-** Existing saves using a currently locked class are preserved, but sealed from continuing until that class is unlocked through the new system." ));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Character Naming",
				"Added character names for Reclaimed save identities.\n" +
				"\n" +
				"**-** New character saves now ask for a character name before the run begins.\n" +
				"**-** Existing saves without a character name ask for one the next time they are continued.\n" +
				"**-** _Games in Progress_ now shows the character name first, then the class and last played time beneath it." ));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BROKEN_HILT), "Hero Remains Reworked",
				"Reworked hero remains so they now belong fully to the class-unlock system.\n" +
				"\n" +
				"**-** Hero-remains fragments no longer have a use action or one-off item effect.\n" +
				"**-** _Bow Fragment_, _Broken Hilt_, _Broken Staff_, _Cloak Scrap_, _Seal Shard_, and _Torn Page_ are now crafting ingredients for class call items.\n" +
				"**-** Skeletal remains no longer generate these fragments, preventing the old remains system from bypassing Reclaimed's new unlock progression.\n" +
				"**-** Fragment descriptions now point players toward the post-Amulet boss-drop and alchemy-merge unlock path." ));
	}

	public static void add_v0_1_5_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.5", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_DIAMOND), "Transcendant XP",
				"Fixed _Transcendant_ items sometimes failing to gain XP from monster kills.\n" +
				"\n" +
				"**-** Transcendant kill XP now uses the slain enemy's own XP value even when the hero is too high-level to receive normal hero XP.\n" +
				"**-** The XP pass continues to scan the full belongings list, including expanded equipment slots and items stored inside bags.\n" +
				"**-** This keeps Transcendant rings, artifacts, trinkets, and carried gear progressing consistently across Reclaimed's longer runs."));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Defender Trade Layout",
				"Improved the defender trade screen on Android.\n" +
				"\n" +
				"**-** The defender _Pockets_ resource strip now starts lower in portrait layouts.\n" +
				"**-** This prevents the pocket icons from overlapping the defender's trade title when opened from _Founder's Camp_."));

		changes.addButton(new ChangeButton(Icons.get(Icons.ALCHEMY), "Alchemy Guide Cleanup",
				"Cleaned up guide entries that implied missing Return Scroll derivatives.\n" +
				"\n" +
				"**-** The alchemy guide no longer shows return-themed recipes on the scroll and spell reference page.\n" +
				"**-** Scroll-to-exotic preview code now safely rejects scrolls without an exotic counterpart.\n" +
				"**-** Scroll of Return remains a Reclaimed expedition tool, but it no longer appears as if it has a runestone or exotic-scroll conversion."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_NULLBRAND), "Ascendant Spark Merging",
				"Expanded _Stone of Ascendant Spark_ into a stronger alchemy progression catalyst.\n" +
				"\n" +
				"**-** Ascendant Sparks now show color-coded rarity ascension chances in their item description.\n" +
				"**-** Two sparks of the same level can be merged at an alchemy pot, creating a stronger spark on success.\n" +
				"**-** Failed merges return only one of the two sparks used.\n" +
				"**-** Spark levels are capped at +5, and Legendary to Transcendant ascension is capped at 50%." ));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.POTION_AZURE), "Defender Scouting XP",
				"Defender scouting now helps settlers grow between raids.\n" +
				"\n" +
				"**-** Defenders who complete simulated dungeon runs now gain XP based on the depth of that run.\n" +
				"**-** The _Defender Returns_ popup now reports XP gained from scouting.\n" +
				"**-** If scouting XP causes a defender to level up, the popup shows their old and new level." ));
	}

	public static void add_v0_1_4_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.4", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.WAND_MAGIC_MISSILE), "Defender Combat",
				"Fixed defenders hesitating during homebase raids when using ranged gear near walls.\n" +
				"\n" +
				"**-** Defenders can now fire valid wand and thrown-weapon shots through homebase defenses even when rebuilt walls block normal field-of-view.\n" +
				"**-** Defenders no longer keep trying to behave like ranged fighters after their wand charges or throwable projectiles are exhausted.\n" +
				"**-** When ranged attacks are unavailable, defenders fall back to normal melee pursuit so they keep fighting instead of pacing near their posts."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_KAUNAN), "Homebase Teleport Safety",
				"Fixed _Scroll of Teleportation_ sometimes placing the hero inside rebuilt homebase structures.\n" +
				"\n" +
				"**-** Floor 0 teleport destinations now reject wall, tower, gate, and building cells that would block the hero.\n" +
				"**-** Homebase teleportation still allows valid walkable ground and allied gate cells, but no longer strands the hero inside solid settlement defenses."));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Defender Construction Safety",
				"Fixed defenders getting trapped inside rebuilt homebase defenses.\n" +
				"\n" +
				"**-** Repairing or rebuilding a wall, tower, gate, or building now checks allied defenders just like the hero.\n" +
				"**-** Any defender standing in a newly blocked structure cell is moved to the nearest safe allied cell after construction resolves.\n" +
				"**-** This prevents repaired walls from hard-locking defenders inside the settlement defenses."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_AMETHYST), "Ring of Wealth Loot",
				"Expanded _Ring of Wealth_ special drops to include Reclaimed loot.\n" +
				"\n" +
				"**-** Special wealth drops can now include material currency bundles from the depth-aware material table.\n" +
				"**-** Mid and high-tier wealth drops can now include catalyst runestones.\n" +
				"**-** Material stacks from wealth drops scale with dungeon depth, so deeper expeditions can still feel rewarding."));

		changes.addButton(new ChangeButton(Icons.get(Icons.ALCHEMY), "Alchemy Output Ranges",
				"Adjusted some basic alchemy conversions to create small bonus stacks.\n" +
				"\n" +
				"**-** Brewing three seeds into a potion now creates a random stack of 1-3 potions.\n" +
				"**-** Brewing a regular potion into its exotic variant now creates a random stack of 1-3 exotic potions.\n" +
				"**-** The alchemy guide now marks these recipes with a _1-3_ output range."));
	}

	public static void add_v0_1_3_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.3", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

	changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_TIWAZ), "Scroll of Upgrade Preview",
				"Fixed a crash in the improved _Scroll of Upgrade_ preview window.\n" +
				"\n" +
				"**-** Rarity stat preview lists now attach their scroll pane before resizing it, matching the rest of the UI lifecycle.\n" +
				"**-** The preview pane now recalculates its clipping area after the upgrade window finishes sizing and centering itself.\n" +
				"**-** Preview text now stays clipped inside the upgrade window instead of rendering outside the modal.\n" +
				"**-** This prevents the upgrade window from crashing when an item has visible rarity stats."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SKULL), "Dwarf King Phase Guard",
				"Fixed a possible _King of Dwarves_ softlock during his invulnerable summoning phase.\n" +
				"\n" +
				"**-** If phase two has no pending summons and no living summoned subjects left, the king now safely advances to the next shield threshold.\n" +
				"**-** This keeps the fight moving when a summon wave is exhausted unexpectedly, without skipping active summons during normal play."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_SPELLBOOK), "Unstable Spellbook",
				"Fixed exotic scroll choices for uncapped _Unstable Spellbook_ levels.\n" +
				"\n" +
				"**-** Spellbooks at or above their normal artifact cap can now still offer an exotic scroll variant when that regular scroll is one of the book's current infusion requests.\n" +
				"**-** Spellbooks below the normal cap keep vanilla-style behavior for empowered scroll options.\n" +
				"**-** This keeps post-cap Spellbook growth from removing valid exotic choices such as the exotic variants of _Remove Curse_ and _Magic Mapping_."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.AMULET), "Return Scroll and Amulet",
				"Protected the _Amulet of Yendor_ ascent path from Return Scroll shortcuts.\n" +
				"\n" +
				"**-** A _Return Scroll_ can no longer be read while the hero is carrying the Amulet.\n" +
				"**-** Carrying the Amulet to the surface now secures it at the homebase instead of deleting the save.\n" +
				"**-** This keeps the Amulet from being safely sent back to the homebase through return magic."));

		changes.addButton(new ChangeButton(Icons.get(Icons.RANKINGS), "Ranking Compatibility",
				"Fixed older ranking records crashing when their detailed saved snapshot could no longer be restored.\n" +
				"\n" +
				"**-** Ranking details now fall back to the preserved summary record if old saved hero data is missing or incompatible.\n" +
				"**-** Old records can still show their score, date, version, hero class, and summary death or victory text when full tabs are unavailable.\n" +
				"**-** Version migration now drops only the broken detailed ranking snapshot instead of treating one old ranking as a recoverable crash."));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Defender Inspect Tabs",
				"Fixed defender inspection and equipment management windows drifting toward the inventory pane.\n" +
				"\n" +
				"**-** Defender equipment replacement now keeps the defender management window active behind the item picker, preventing the reopened window from inheriting the inventory-pane offset.\n" +
				"**-** Defender gear-tab content now uses the correct local pane coordinates after relayouts.\n" +
				"**-** Defender inspection now has a dedicated _Trade_ tab, so trading no longer needs to open a separate trade window from inside inspection."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Resistance Entry Points",
				"Fixed more status-effect entry points so full resistance blocks both gameplay and text feedback.\n" +
				"\n" +
				"**-** Fully resisted _Bleeding_ no longer announces bleeding text or chat output when the effect does not apply.\n" +
				"**-** _Toxic Gas_ damage from traps, potions, and gas clouds now reads _Poison Resistance_.\n" +
				"**-** Fully resisted direct-damage effects now show _Immune_ instead of a zero-damage tick.\n" +
				"**-** Paralytic gas paths were checked against _Stun Resistance_ and continue to use the shared paralysis resistance route."));
		
		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_FRACTURED_NEXUS), "Transcendant Clarity",
				"Improved the readability and identity of _Transcendant_ gear.\n" +
				"\n" +
				"**-** Transcendant upgrade choices now color their option text based on the minimum rarity of the offered stat.\n" +
				"**-** Direct item-upgrade choices use the Transcendant color so they stand apart from regular stat choices.\n" +
				"**-** Transcendant rarity now uses a stronger orange tone so it is easier to distinguish from _Legendary_.\n" +
				"**-** Transcendant item auras now use a rotating champion-style flare instead of the standard circular rarity halo.\n" +
				"**-** Capped Transcendant options now clamp their displayed and applied gains so chance, proc, and resistance stats cannot offer values above 100%."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_TIWAZ), "Upgrade Preview",
				"Improved _Scroll of Upgrade_ previews for rarity-driven gear.\n" +
				"\n" +
				"**-** Upgrade windows now show an item's visible rarity stats alongside its vanilla upgrade preview.\n" +
				"**-** Rarity stat names keep their minimum-rarity colors in the preview, so strong rolls are easier to scan.\n" +
				"**-** Current rarity stat values are shown before upgrading, while the possible new value is marked with a _?_ because rarity stat growth is still chance-based.\n" +
				"**-** Long rarity stat lists now scroll inside the upgrade window instead of stretching the panel."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_PRISMFORGE), "Catalyst Feedback",
				"Improved catalyst runestone feedback so rarity crafting changes are easier to read in chat.\n" +
				"\n" +
				"**-** _Aetherflux_ and _Ascendant Spark_ now report old rarity to new rarity with rarity-colored names.\n" +
				"**-** _Fractured Nexus_ reports the new stat it adds, using that stat's minimum-rarity color.\n" +
				"**-** _Reshaper's Crucible_ reports each old stat to new stat change as a readable colored list.\n" +
				"**-** _Oblivion Seal_ reports the stat it locked.\n" +
				"**-** _Fateweaver_ reports the old stat to new stat change.\n" +
				"**-** _Prismforge_ reports the stat value before and after the reroll."));

		changes.addButton(new ChangeButton(Icons.get(Icons.RANKINGS), "Roguelite Rankings",
				"Updated rankings so they better represent a long-running Reclaimed legacy instead of only a single vanilla-style expedition.\n" +
				"\n" +
				"**-** Ranking records now track lifetime dungeon runs, total floors descended, total floors ascended, deepest floor reached, and total hero XP across expeditions.\n" +
				"**-** Ranking strength now includes restored homebase training bonuses when viewing a saved record.\n" +
				"**-** Score breakdowns now include a _Settlement_ category for homebase levels, permanent training, structure defenses, defenders, settlement requests, and raids survived.\n" +
				"**-** The ranking inventory tab now scrolls so expanded equipment slots and longer carried equipment lists can fit cleanly."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.HOMEBASE_WOOD), "Material Drop Balance",
				"Rebalanced material currency drops so the resource economy now has a clearer rarity ladder.\n" +
				"\n" +
				"**-** Material loot now follows the intended rarity order: _wood_, _stone_, _copper ore_, _iron ore_, _gold ore_, _scrap_, _ember shards_, then _ember cores_.\n" +
				"**-** Extra chest drops, monster drops, loose level-generation drops, cache rooms, and material shop bundles now use the same depth-aware resource table.\n" +
				"**-** Deeper dungeon floors can roll larger material stacks, while rarer resources appear in smaller amounts when they do show up.\n" +
				"**-** Scrap and ember resources can now enter the normal material-drop economy instead of being limited to forge-only sources."));

		changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "Homebase Progression Balance",
				"Reordered homebase training unlocks around early-game survival, mid-game build support, and late-game specialization.\n" +
				"\n" +
				"**-** Core survival, accuracy, evasion, economy, and basic combat training now unlock earlier so rebuilt facilities feel useful sooner.\n" +
				"**-** Wand, ranged, artifact, ring, trinket, loot, and resource-yield upgrades now sit in the mid-game where they can support longer roguelite builds.\n" +
				"**-** High-impact procs, resistances, and specialized offensive effects now unlock later so scaling enemies have room to push back.\n" +
				"**-** Homebase buildings no longer have a fixed max level, and facility screens now show _Building Level: X_ instead of a capped level fraction."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SKULL), "Mob Build Scaling",
				"Reworked mob stat scaling so enemies grow into recognizable builds instead of carrying a huge flat list of low-impact stats.\n" +
				"\n" +
				"**-** Mob levels are no longer capped at 100.\n" +
				"**-** Every mob level now keeps adding baseline health, damage, and attack pressure.\n" +
				"**-** Armor, movement speed, and attack speed remain high-chance combat rolls so leveled mobs feel sturdier and more aggressive without every stat being guaranteed.\n" +
				"**-** Mob rarity-stat rolls now prefer focused stat pools, then stack duplicate rolls into stats the mob already has, similar to Transcendant item growth.\n" +
				"**-** Mob proc chances, resistances, and other chance stats can exceed 100% intentionally, matching the player's ability to stack multiple Transcendant sources."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
		changes.hardlight(CharSprite.POSITIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_ARMBAND), "Armband Steals",
				"Expanded _Master Thieves' Armband_ steals to include Reclaimed loot.\n" +
				"\n" +
				"**-** Successful monster steals can now pull catalyst runestones from the same catalyst table used by monster drops.\n" +
				"**-** Successful monster steals can now pull material resources from the same depth-aware resource table used by monster drops.\n" +
				"**-** Stolen material stacks scale with dungeon depth and respect homebase resource-yield training.\n" +
				"**-** Catalyst steal odds respect homebase catalyst-drop training."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_COLOR), "Defender Wall Coordination",
				"Improved defender raid behavior so the homebase defense feels more coordinated.\n" +
				"\n" +
				"**-** During raids, defenders now split their posts across the north, east, south, and west walls.\n" +
				"**-** Larger defender rosters form small wall groups instead of clumping into one side of the base.\n" +
				"**-** Wall assignments now react to raider pressure, so sides with more attackers can draw more defender attention.\n" +
				"**-** This gives rebuilt walls a stronger tactical role during homebase raids."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.WAND_MAGIC_MISSILE), "Defender Ranged Support",
				"Improved defender ranged combat around the homebase walls.\n" +
				"\n" +
				"**-** Defenders now treat rebuilt walls and structures as firing cover instead of trying to leave the base for a clear angle.\n" +
				"**-** Defender wand and thrown-weapon attacks can pass through homebase defenses when targeting raiders.\n" +
				"**-** Defender ranged attacks now show visible magic or projectile effects, so raiders no longer take damage from nowhere.\n" +
				"**-** These projectile rules are only for allied homebase defense and do not make the walls passable."));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Defender Scouting Gifts",
				"Improved defender scouting rewards after expeditions.\n" +
				"\n" +
				"**-** Defenders who return from their off-screen dungeon runs can now present their donated materials in a dedicated popup.\n" +
				"**-** The popup shows the defender's sprite, rarity aura, name, and color-coded donated resources.\n" +
				"**-** Defenders now keep part of what they gather as their own personal currency instead of donating everything to the settlement.\n" +
				"**-** This makes defender scouting rewards easier to notice and gives each defender a small personal economy."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(CharSprite.NEGATIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Defensive Stat Nerfs",
				"Adjusted high-value defensive stats so magic and surprise attacks keep their bite.\n" +
				"\n" +
				"**-** _Dodge Chance_ is only 20% effective against magic attacks.\n" +
				"**-** _Dodge Chance_ is ignored when the defender is surprise attacked.\n" +
				"**-** _Block Chance_ is only 25% effective against magic attacks.\n" +
				"**-** _Block Chance_ is ignored when the defender is surprise attacked."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.HOMEBASE_WOOD), "Battlefield Cleanup",
				"Raid cleanup is now less automatic.\n" +
				"\n" +
				"**-** Loose material resources at the homebase are no longer instantly vacuumed into protected storage when a raid ends.\n" +
				"**-** Defenders now need to walk to reachable battlefield material drops and secure them manually.\n" +
				"**-** This keeps post-raid recovery grounded in the homebase simulation instead of resolving every dropped resource at once."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.DEPTH), "Endless Reclaimed Depths",
				"Added the first post-Amulet endless dungeon loop.\n" +
				"\n" +
				"**-** Once the Amulet is recovered and brought safely back to the homebase, future expeditions no longer end at the old final floor.\n" +
				"**-** Floors 1-25 still follow the classic sewer-to-halls progression, including the normal boss floors.\n" +
				"**-** Floor 26 and deeper now continue forever with random dungeon regions instead of spawning another Amulet floor.\n" +
				"**-** Random boss floors appear every fifth floor after the original Halls boss, starting at floor 30.\n" +
				"**-** Shop floors appear after boss floors, starting at floor 26, giving each endless segment a recovery and spending point.\n" +
				"**-** A mine-style special region can now appear as part of the endless floor pool."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.GOLD), "Defender Trading",
				"Added the first defender trade economy.\n" +
				"\n" +
				"**-** Defenders now have personal pockets for gold, energy, materials, and forge resources.\n" +
				"**-** When defenders complete off-screen dungeon runs, they can keep useful supplies, use growth items, donate some materials, and list extra loot for trade.\n" +
				"**-** Defender trade stock can include scrolls, exotic scrolls, seeds, potions, exotic potions, runestones, catalyst stones, and very rare special finds.\n" +
				"**-** Trade offers reset after expeditions, and some defenders may return with nothing to sell.\n" +
				"**-** Defender trade prices usually use gold, but some offers can ask for forge resources instead.\n" +
				"**-** Defenders can also pay the hero for gifted equipment when they have enough personal currency."));

	}

	public static void add_v0_1_2_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.2", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Facility Screen Dividers",
				"Improved visual separation in homebase facility screens.\n" +
				"\n" +
				"**-** Facility tabs now use stronger horizontal dividers between obvious sections.\n" +
				"**-** Divider styling now matches the clearer section language used by the talent and changes screens.\n" +
				"**-** Upgrade, training, storage, forge, garden, and camp content should read less like one long wall of text."));

		changes.addButton(new ChangeButton(Icons.get(Icons.INFO), "Defender Screen Dividers",
				"Improved readability in _Founder's Camp_ defender management.\n" +
				"\n" +
				"**-** Defender roster entries now have visible horizontal dividers between each defender.\n" +
				"**-** The management screen should be easier to scan when several defenders have gear, stats, and supplies.\n" +
				"**-** This is a visual-only pass and does not change defender behavior."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed high-level artifact recharge issues caused by uncapped artifact levels.\n" +

				"**-** Fixed _Skeleton Key_ recharge math at high levels, where its missing-charge formula could become negative and stop visible recharge progress.\n" +
				"**-** Existing saves now sanitize invalid or negative partial artifact charge so previously affected artifacts can recover.\n" +
				"**-** Level-based artifact charge caps now resync after loading, upgrading, rarity stat changes, and homebase potency changes.\n" +
				"**-** Similar recharge formulas on _Unstable Spellbook_, _Timekeeper's Hourglass_, _Cloak of Shadows_, _Holy Tome_, and_ Ethereal Chains_ now clamp to safe minimum recharge times.\n\n" +
				"Fixed screen layout issues on desktop and mobile.\n" +
				"**-** Homebase facility screens now use a more consistent layout when _forced landscape_ mode is enabled on mobile devices."
			));
	}

	public static void add_v0_1_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

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

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Rarity Balance",
				"Adjusted rarity stat limits and resistance behavior for the first Reclaimed balance pass.\n" +
				"\n" +
				"**-** Per-item _Transcendant_ resistance, proc, and chance stats now cap at 100%.\n" +
				"**-** Duplicate capped stat rolls now aggregate up to the cap instead of exceeding it.\n" +
				"**-** Transcendant upgrade choices stop offering already-capped chance, proc, and resistance stats."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CALENDAR), "Founder's Camp Contracts",
				"Adjusted contract slot pacing so the Founder's Camp feels useful earlier.\n" +
				"\n" +
				"**-** The camp now gains contract capacity much more often as it is upgraded.\n" +
				"**-** Higher camp levels can support a much larger board of active settlement work.\n" +
				"**-** Contract growth now better matches long-term building progression."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SKULL), "Mob Level Scaling",
				"Updated enemy mob level scaling so the dungeon reacts to permanent settlement growth.\n" +
				"\n" +
				"**-** Newly spawned mobs now include a conservative mob level pressure bonus from homebase building levels.\n" +
				"**-** Permanent training levels also contribute to mob level pressure.\n" +
				"**-** Transcendant item levels now increase mob level pressure more sharply than normal upgrades.\n" +
				"**-** This scaling is separate from raid threat and still reads the current run's live mob level pressure."));
			
		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "Resistance Fixes",
				"Fixed several cases where 100% resistance still allowed status effects to slip through.\n" +
				"\n" +
				"**-** Fully resisted duration buffs now fail to attach instead of leaving a tiny remaining duration.\n" +
				"**-** Blob effects such as _Vertigo Gas_, fire, and corrosion now respect full rarity and homebase resistance.\n" +
				"**-** Fully resisted effects now show an _Immune_ text indicator instead of announcing the blocked debuff.\n" +
				"**-** Root effects now check resistance before announcing, so blocked roots no longer show _Root_.\n" +
				"**-** Cave spinner webs now use the same root-resistance check and immune feedback when their root is blocked.\n" +
				"**-** Mob resistance scaling now follows the same full-resistance behavior."));
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
