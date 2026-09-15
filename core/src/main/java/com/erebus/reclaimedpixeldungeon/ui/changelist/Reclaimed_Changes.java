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
import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.effects.BadgeBanner;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.ChangesScene;
import com.erebus.reclaimedpixeldungeon.sprites.AlbinoSprite;
import com.erebus.reclaimedpixeldungeon.sprites.BeeSprite;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.GhoulSprite;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.sprites.KingSprite;
import com.erebus.reclaimedpixeldungeon.sprites.GnollExileSprite;
import com.erebus.reclaimedpixeldungeon.sprites.MimicSprite;
import com.erebus.reclaimedpixeldungeon.sprites.RatSprite;
import com.erebus.reclaimedpixeldungeon.sprites.RatKingSprite;
import com.erebus.reclaimedpixeldungeon.ui.BuffIcon;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class Reclaimed_Changes {

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		addAllChanges(changeInfos, 0);
	}

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos, int selectedTab ){
		if (selectedTab == 1) {
			add_v0_1_9_Changes(changeInfos);
			add_v0_1_8_Changes(changeInfos);
			add_v0_1_7_Changes(changeInfos);
			add_v0_1_6_Changes(changeInfos);
			add_v0_1_5_Changes(changeInfos);
			add_v0_1_4_Changes(changeInfos);
			add_v0_1_3_Changes(changeInfos);
			add_v0_1_2_Changes(changeInfos);
			add_v0_1_1_Changes(changeInfos);
			add_v0_1_0_Changes(changeInfos);
		} else {
			add_v0_2_5_Changes(changeInfos);
			add_v0_2_4_Changes(changeInfos);
			add_v0_2_3_Changes(changeInfos);
			add_v0_2_2_Changes(changeInfos);
			add_v0_2_1_Changes(changeInfos);
			add_v0_2_0_Changes(changeInfos);
		}
	}

	public static void add_v0_2_5_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.5", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new TabbedChangeButton(ChangeIcons.V40_CHANGES.get(), "Shattered v4.0 Migration",
				new String[]{ "v4.0 Foundation", "Reclaimed Preserved" },
				"_RECLAIMED NOW RUNS ON SHATTERED v4.0_\n\n"
						+ "Reclaimed Pixel Dungeon has been ported from the Shattered Pixel Dungeon _v3.3.8_ foundation to the stable _v4.0_ release. This brings the major upstream additions and fixes into Reclaimed while giving future updates a modern, maintained base.\n\n"
						+ "**-** The _Ambitious Imp_ now leads into Shattered's redesigned Dwarven Vault quest, with its new rooms, hazards, enemies, progression, and boss encounter.\n"
						+ "**-** Shattered's new weapon enchantments and curses, updated item behavior, balance adjustments, interface improvements, visual upgrades, and engine fixes have been carried forward.\n"
						+ "**-** The Shattered changelog now includes _v4.X_ and uses separate _Release_ and _Pre-release_ navigation so its complete history remains readable inside Reclaimed.",
				"_THE GAME IS STILL RECLAIMED_\n\n"
						+ "This migration changes the foundation, not Reclaimed's identity. Existing systems were reconciled with v4.0 instead of being replaced by their upstream versions.\n\n"
						+ "**-** _Infinite floors_, post-Amulet progression, expanded floor generation, rare chests, Mimic variants, elite enemies, rarity stats, and Transcendant equipment remain intact.\n"
						+ "**-** _Homebase_, raids, armed towers, autonomous Defenders, material resources, catalysts, multiple enchantments, and active-play rewards remain intact.\n"
						+ "**-** The _Wayfarer Network_, accounts, map, chat, global trading, moderation, sanctions, and moderator rewards remain part of each eligible character's progression.\n"
						+ "**-** Reclaimed's application identity and compatibility handling for existing saves were preserved throughout the port."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_OBLIVION_SEAL), "Catalyst Stone Artwork",
				"Updated every _catalyst runestone_ to use the same carved stone background introduced by Shattered's refreshed runestone artwork. Catalysts keep their distinct symbols and effects, but now belong visually beside the other stones in inventories, rewards, shops, and the Journal."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MASK), "Persistent Advanced Talents",
				"A hero's chosen _subclass_, _armor ability_, and their invested advanced talents now remain between dungeon expeditions. Returning to the Homebase still resets ordinary run levels and experience, but it no longer erases the character-defining progression earned from the Tengu's Mask and Dwarf King's Crown.\n\n"
						+ "Finding those choice items in a later run still lets the player _change subclass_ or _respec the armor ability_. Replacing either choice cleanly removes its old talent set before adding the new one, so abandoned abilities cannot remain active in the background."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_RESHAPERS_CRUCIBLE), "High-Rarity Reroll Warning",
				"Using any catalyst that _rerolls_ an _Epic or Legendary_ item now asks for confirmation before altering it. The warning appears before the catalyst is consumed and covers complete reforges, rarity rerolls, full-stat reshaping, individual stat changes, and value rerolls, giving valuable equipment one final safeguard against an accidental tap."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.TRAP_MECHANISM), "Scaling Trap Damage",
				"Damaging _traps_ now grow with both _floor depth_ and the level of enemies being generated there. This keeps hazards relevant throughout endless progression without increasing gas coverage, guardian counts, or other effects that could overload a turn. Percentage-based traps retain their natural scaling."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Homebase Placement",
				"Fixed returning heroes appearing in the middle of the _Founder's Camp_. The Homebase entrance now places the player on the path directly outside the Camp door, and the corrected entrance is applied to both newly generated and existing Homebases.\n\n"
						+ "Defenders also avoid using building footprints as wandering destinations. If a Defender is already standing on a structure when they become tired or need to recover, they first walk to open ground and only then go to sleep."));

		changes.addButton(new ChangeButton(Icons.get(Icons.COMPASS), "Wayfarer Location Privacy",
				"Fixed the desktop Wayfarer Map using inaccurate public-IP locations or exposing the exact coordinate returned by _Windows Location Services_ through the local _You_ marker and map center. Desktop location is now acquired automatically without a manual city prompt, while the local map and server presence consistently use the same stable position displaced by _400-500 meters_. The raw Windows coordinate is used only to calculate distances and is never displayed on the Wayfarer Map."));

		changes.addButton(new ChangeButton(new Image(new MimicSprite()), "Mimic Levels and Stats",
				"Fixed hidden _Mimics_ being skipped by enemy progression because their chest disguise begins with neutral alignment. Every Mimic variant now receives the appropriate _mob level and rarity stats_ when created, and Mimics already waiting inside existing saves are repaired when their floor is loaded."));

		changes.addButton(new ChangeButton(new Image(new GnollExileSprite()), "Infinite-Floor Enemy Loot",
				"Fixed _Gnoll Exiles_ and _Hermit Crabs_ applying an obsolete hero-level cutoff after the shared endless-floor loot check. Their special guaranteed loot now remains available during post-Amulet and infinite-floor runs whenever their scaled mob level makes them eligible."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARMOR_PLATE), "High-Level Mob Armor",
				"Rebalanced enemy _Armor_ after testing both extremes. The original compounding curve could reduce every player attack to _0 damage_, while the first linear correction made deep enemies too easy to one-shot. Armor now uses a moderate _subquadratic baseline_: it remains close to the original strength at early levels, grows meaningfully through endless progression, and falls increasingly below the old curve before it can overwhelm player damage. Random Defense and Armor Bonus stats still create tougher individuals, and the new baseline applies immediately to enemies already stored in existing saves."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.POISON, true), "Extreme Poison Freeze",
				"Fixed the health-bar preview calculating extreme _Poison_ damage one turn at a time. Very large poison durations could hold the entire render thread for several minutes even though the game had not crashed. The preview now produces the exact same total immediately, without changing Poison's duration or actual damage."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.VERTIGO, true), "Vertigo and Homebase Walls",
				"Fixed _Vertigo_ choosing its random movement from the floor terrain alone and overlooking the Homebase's constructed walls. Confused movement now uses the same live structure passability rules as ordinary movement, so walls, closed gates, and other blocked building cells remain solid."));

		changes.addButton(new ChangeButton(new Image(new BeeSprite()), "Tamed Bee Scaling",
				"Fixed a scaled _Honeypot Bee_ losing its maximum health after being tamed and restored from a save. Taming now preserves the Bee's level-scaled HP and rolled combat stats instead of combining its current health with the low unscaled maximum from its base template."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_SPELLBOOK), "Safe Artifact Morphing",
				"Fixed an equipped _Artifact_ disappearing when transmutation produced the same kind of Artifact already worn in another slot. If the transformed Artifact cannot be equipped, it is now retained in the backpack or placed safely on the ground when the backpack is full."));

		changes.addButton(new ChangeButton(Icons.get(Icons.JOURNAL), "Item Nickname Details",
				"Fixed adding a custom _nickname or note_ to an item bypassing its rarity and stat readout. Named equipment now keeps its complete rarity stats, enchantments, glyphs, combat values, and other inspection details beneath the custom label."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_HOLDER), "Ring Bag Equipment Swaps",
				"Fixed a ring stored inside the _Ring Bag_ being duplicated after replacing a full equipment slot and reopening the save. Equipment replacement now removes and restores items through the complete bag hierarchy, so the same ring cannot remain in its specialist bag while also appearing in the main backpack."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_HOLDER), "Unidentified Alchemy Previews",
				"Fixed the Alchemy Table revealing an _unidentified exotic scroll_ through its output description. Recipe previews no longer identify the underlying scroll type; identification still occurs normally only after the recipe is actually brewed."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_SANDALS), "Nature Footwear Seed Drops",
				"Fixed highly upgraded _Sandals, Boots, and Greaves of Nature_ eventually reversing their seed-drop formula and producing fewer rewards. Their chance now rises to the artifact's intended maximum and remains there at higher levels, including when combined with the Petrified Seed."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.AMULET), "Challenge Unlock on Victory",
				"Added a final victory validation when the player chooses to _return the Amulet_. This guarantees that Challenges and completed challenge badges are saved globally at the actual end of the run, even if the earlier Amulet scene validation was interrupted."));
	}

	public static void add_v0_2_4_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.4", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addTabbedDevCommentary(changes,
				new String[]{ "Wayfarers", "Safety", "Rewards", "Defenders", "Balance", "Reliability" },
				"_A WORLD THAT REMAINS OPTIONAL_\n"
						+ "v0.2.4 is the largest step Reclaimed Pixel Dungeon has taken toward connecting its players. The _Wayfarer Network_ begins only after a character unlocks the Wayfarer Exchange, and even then it remains a choice. A player who wants the familiar offline experience can keep it completely private, while a player who becomes _Visible_ can discover other Wayfarers, appear on their maps, and open the door to conversation and trade. That distinction matters because online features should add possibilities without changing the kind of game someone originally chose to play.\n\n"
						+ "_IDENTITY WITHOUT GIVING UP PRIVACY_\n"
						+ "Every eligible save registers as its own _character_, even when several characters belong to the same account. Unique online names make conversations, reports, trades, and moderator decisions understandable without merging the progress or reputation of separate heroes. The map shows the character's current class, level, portrait, and online duration, but public locations are deliberately shifted _400-500 meters_ away. Desktop automatically requests a fresh location from Windows only while Wayfarer visibility is active; both the local map preview and server presence use the displaced public position rather than displaying or retaining that live coordinate. The goal is to help players find a community and possible trading partners without pretending that an exact home address is an acceptable price for participating.\n\n"
						+ "_FROM SEEING SOMEONE TO REACHING THEM_\n"
						+ "The _Wayfarer Map_ is meant to be useful rather than decorative. Roads, buildings, place names, panning, and zooming provide enough context to understand distance, while the nearest-to-farthest list makes the whole visible community reachable even when someone is far away. Private chat persists across sessions, records useful local timestamps, and raises visible unread shortcuts because a conversation should not require both people to stare at the same screen. _Global Trading_ follows that same asynchronous idea: both players can prepare, inspect, and confirm an offer in their own time, while deposits and claimable returns protect the items committed to the exchange.",
				"_SAFETY HAS TO BE PART OF THE FOUNDATION_\n"
						+ "A feature designed to connect strangers cannot treat safety as a note added afterward. Every conversation carries a clear reminder, while _Block_ removes both characters from each other's discovery and messaging without erasing the history the blocker may still need. _Safety Reports_ are character-level and include the stated reason plus the latest _30 messages_ only when a player chooses to submit them. Moderators cannot browse ordinary private conversations; evidence becomes visible only through that deliberate report.\n\n"
						+ "_ACCOUNTABILITY WITHOUT COLLECTIVE PUNISHMENT_\n"
						+ "Confirmed violations progress through temporary chat and trading restrictions before reaching the final _Character Deletion Review_. Sanctions remain attached to the character responsible, not every save under the same account. The last stage includes an appeal period and requires a second moderator because deleting a developed character is intentionally serious and should never rest on one hurried decision. Both valid and dismissed reports return a view-only moderation notice so the reporter knows that someone actually reviewed the case.\n\n"
						+ "_TOOLS FOR THE PEOPLE DOING THE WORK_\n"
						+ "Moderators receive a dedicated _Moderator Space_, live case alerts, written guidelines, ownership controls, evidence views, and a separate administration dashboard. Cases can be claimed so two moderators do not unknowingly work on the same report, and records use character names and clear status colors so important information can be understood quickly. These tools are meant to support consistent judgment, preserve an audit trail, and make intervention possible without granting casual access to conversations that were never reported.",
				"_REWARDING PLAY, NOT AN OPEN WINDOW_\n"
						+ "The new _Active Play Rewards_ system recognizes the time someone actually spends exploring, fighting, collecting, and making progress. Every _15 active minutes_ produces three weighted choices using the established item-rarity odds. It works locally and offline, pauses after three minutes without meaningful activity, and queues rewards that are not claimed immediately. This avoids turning the feature into a login obligation or rewarding a game that was simply left running in the background.\n\n"
						+ "_A CHEST WORTH LOOKING FORWARD TO_\n"
						+ "Each choice uses an inventory-style presentation and a rarity aura so its quality can be understood before it is selected. Common rewards provide practical supplies, while the rarest rolls can offer large resource bundles, tier-V enchantment tools, Emeralds, Spatial Geodes, unlock items, or independently generated Artifact and Trinket choices. The blinking _Golden Chest_ shortcut returns until every queued reward is resolved, protecting earned progress instead of forcing a decision during combat or exploration.\n\n"
						+ "_THANKING MODERATOR SERVICE_\n"
						+ "Moderator welcome, daily, and weekly rewards remain an _additional_ system rather than replacing ordinary play rewards. Their timers count active service per character, pause during inactivity, and preserve progress throughout the day and week. These rewards are a practical thank-you for time spent playing, monitoring, and helping the community, while the activity rules ensure that recognition follows genuine participation rather than an unattended timer.",
				"_FROM FOLLOWERS TO SETTLEMENT RESIDENTS_\n"
						+ "Defenders have gradually become more than equipment holders, and v0.2.4 gives their virtual expeditions a visible life of their own. Their return summary now shows what they found and whether each item was _equipped_, _kept_, _traded_, or _salvaged_. Showing those decisions is important: a Defender's growth should feel like the result of a journey and a set of needs, not an unexplained number changing after the hero returns home.\n\n"
						+ "_INDEPENDENCE NEEDS THE SAME LIMITS_\n"
						+ "Each Defender now manages a persistent _20-slot backpack_, can purchase the same specialist bags used by the player, and can expand owned bags with a _Spatial Geode_. They compare equipment quality, respect Strength requirements, pay real Forge costs, and receive the same salvage value as the hero. These limits make their choices believable and prevent virtual expeditions from becoming an unlimited source of storage, upgrades, or resources.\n\n"
						+ "_PLANNING BEYOND THE CURRENT RUN_\n"
						+ "Useful supplies no longer have to be consumed merely because they were found. Defenders retain _Seeds_ until they can complete a normal three-seed recipe, hold _Scrolls of Remove Curse_ for cursed equipment, and save _Scrolls of Upgrade_ until a useful identified item falls behind their progression. A cursed equipped item locks its slot until it is cleansed, just as it should for the player. Together with trading, brewing, salvaging, and self-equipping, this lets Defenders prepare for future needs instead of making every decision in isolation.",
				"_REWARDS THAT KEEP UP WITH THE RISK_\n"
						+ "Powerful enemies should offer more than a longer health bar. _Elite Loot Scaling_ now ties bonus drops to elite rarity, so defeating a Legendary or Transcendant enemy feels meaningfully different from defeating a Common elite. _Treasure Luck_ was adjusted for the same reason but with an important restraint: it can create at most _5 ordinary drops_ from one enemy, then improves the quality of rarity-capable loot instead of burying the floor under hundreds of items. _Resourceful_ now increases actual material quantities so large values remain visible in the rewards they were meant to improve.\n\n"
						+ "_MORE WAYS TO BUILD A CHARACTER_\n"
						+ "_Stun Resistance_ and the other resistance stats are no longer confined to armor, allowing rings, artifacts, and Trinkets to support defensive plans. _Artifact Recharge Rate_ adds a matching progression route for players who build around reusable artifacts. Lost Defenders now request one of several sensible rescue supplies, quickslots preserve the bag and assignments the player deliberately chose, and expanded health abbreviations keep extreme progression readable after removing the old one-billion ceiling. These are different systems, but they share one purpose: keeping more character-building choices useful.\n\n"
						+ "_INFINITE PROGRESSION NEEDS GRADUAL ANSWERS_\n"
						+ "Reclaimed runs can continue far beyond the original dungeon's scale, so values that behave well at level 20 may break down at level 650. Enemy _Attack Speed remains uncapped_, but it grows less aggressively and is no longer guaranteed by ordinary stat growth. High-level Trinkets preserve their strongest intended behavior instead of wrapping into weaker or negative results. The aim is not to erase the power of deep floors; it is to let danger continue increasing without allowing one stat or formula to overwhelm every other part of a turn.",
				"_A LONG RUN HAS TO BE TRUSTWORTHY_\n"
						+ "In a roguelite, a bug is rarely just a brief inconvenience. A missing Mimic drop, an incorrect Barrier trigger, an invisible elite aura, or a description outside its window can hide information or erase the meaning of a hard-earned encounter. v0.2.4 repairs post-Amulet Mimic loot, Crystal Mimic death cleanup, Barrier ownership, rarity-stat Journal coverage, consecutive Magical Catalyst rolls, and Transcendant description layout so the result on screen agrees with the rules underneath it. _Homebase walls_ also clear vegetation left by existing saves, reject new grass and plants, and stop knocked-back enemies at the same boundary used by normal movement. A settlement wall should be a dependable piece of the battlefield, not a visual layer that nature or forced movement can quietly ignore.\n\n"
						+ "_WHEN THE GAME STOPS, THE RUN STOPS_\n"
						+ "The most important fixes address freezes that forced players to close the game and repeat exploration. High-speed mob turns now avoid enormous queues of repeated actions. Rendering no longer holds a scene lock while waiting for linked combat state. Wayfarer polling reuses a bounded background worker instead of creating threads until the operating system refuses another one. Finally, extreme _Potential_ glyph values can no longer trap wand charging in an endless loop; even an invalid or infinite charge resolves immediately within the wand's real capacity.\n\n"
						+ "_WHY THIS POLISH MATTERS_\n"
						+ "The Wayfarer Network, active rewards, autonomous Defenders, and endless progression all ask the game to remember and coordinate more than before. Reliability work is what makes those additions safe to enjoy. The purpose of these guardrails is simple: opening chat should not end a dungeon turn, a rare stat should not freeze combat, and leaving a window should not require reopening the save. v0.2.4 is a large expansion, but its real measure is whether players can trust it with the time they invest." );

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new TabbedChangeButton(Icons.get(Icons.CHANGES), "Wayfarer Network",
				new String[]{ "Online", "Account", "Map", "Chat", "Safety" },
				"_AN OPTIONAL ONLINE WORLD_\n\n"
						+ "Characters who unlock the _Wayfarer Exchange_ may join the Wayfarer Network. Online play is always optional: choosing _Visible_ lets other players find you, while switching it off immediately returns that character to private play.\n\n"
						+ "**-** Android and Windows players share the same network.\n"
						+ "**-** Desktop location updates automatically through Windows Location Services, avoiding inaccurate ISP routing locations without asking the player to enter a city.\n"
						+ "**-** Visibility returns automatically after reopening the game when the toggle was left on.\n"
						+ "**-** Your current character name, class, level, portrait, and online duration help other Wayfarers recognize you.\n"
						+ "**-** Connection and presence repairs make joining, returning, and switching visibility more dependable.",
				"_YOUR WAYFARER ACCOUNT_\n\n"
						+ "A Wayfarer account connects all of your eligible character saves without combining their inventories or progress. Sign-up, email confirmation, sign-in, password recovery, account switching, and sign-out are available inside the game.\n\n"
						+ "**-** Every online character has its own identity, even when several belong to one account.\n"
						+ "**-** Online names are unique. If your offline name is taken, the game helps you choose another and updates the name shown on the save list.\n"
						+ "**-** Returning players remain signed in between sessions.\n"
						+ "**-** Email addresses are shortened on screen to protect streamers and screenshots.",
				"_THE WAYFARER MAP_\n\n"
						+ "The _Wayfarer Map_ shows every player who has chosen to be visible, ordered from nearest to farthest. Roads, buildings, place names, and geographic features make the map familiar and easy to explore.\n\n"
						+ "**-** Drag to move the map, use the mouse wheel or pinch gesture to zoom, and select a player to view their profile.\n"
						+ "**-** Portraits follow the armor each character is currently wearing.\n"
						+ "**-** Public markers are moved _400-500 meters_ away from the submitted location. Exact locations are never shown.\n"
						+ "**-** Map loading, location checks, and reopening visibility have been stabilized so the map remains responsive.",
				"_CHAT AND GLOBAL TRADING_\n\n"
						+ "Visible Wayfarers can start private conversations that refresh automatically and remain saved on their own devices. Unread conversations blink in the chat list and raise a purple shortcut during play.\n\n"
						+ "**-** Messages show clear sender names and useful time or date stamps.\n"
						+ "**-** Trades can be proposed inside a conversation without both players staying online together.\n"
						+ "**-** Each side chooses up to _3 items_ plus resources, reviews the complete offer, and confirms before anything is exchanged.\n"
						+ "**-** A completed trade costs each participant _1 Emerald_. Cancelled deposits and completed offers remain safely claimable.",
				"_SAFETY AND MODERATION_\n\n"
						+ "Every conversation includes safety guidance, blocking, and character-level reporting. Blocking hides both characters from each other and stops new messages in both directions while preserving local history.\n\n"
						+ "**-** A report includes its reason and the latest _30 messages_ for moderator review. Ordinary chats are not visible to moderators.\n"
						+ "**-** Confirmed violations use escalating temporary restrictions. The final stage requires an appeal opportunity and review by a second moderator before that character can be deleted.\n"
						+ "**-** Moderators share a dedicated group space with written guidance, case alerts, and per-character rewards for active service.\n"
						+ "**-** Daily, weekly, character-lifetime, and combined moderator service records make active contributions easy to follow. Claimable rewards raise the blinking chat shortcut and highlight _Moderator Space_ until collected."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.LOCKED_CHEST), "Active Play Rewards",
				"Every character can now earn a completely _offline reward_ for each _15 minutes of active play_. Movement, combat, collecting items, and allied or corrupted-mob combat all count, while the timer pauses after three minutes without activity so leaving the game open cannot earn rewards. Moderators earn these too; their moderator service rewards remain an additional benefit.\n\n"
						+ "**-** Each reward presents _3 independently rolled choices_. Every choice uses the same rarity odds as generated items, from Common through Transcendant, and its inventory glow reveals that reward tier.\n"
						+ "**-** Higher rarities can offer larger resource and catalyst bundles, enchantment tools up to tier V, Emeralds, Spatial Geodes, character unlock items, or a choice of three independently rolled Artifacts or Trinkets.\n"
						+ "**-** Unclaimed rewards remain queued per character. A blinking _Golden Chest_ shortcut returns until the reward is chosen, and stacked rewards are offered one after another without losing progress."));

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Autonomous Defender Expeditions",
				"Defenders now return from their virtual dungeon runs with a visible record of the loot they found and the decisions they made. This gives their time away from the Homebase a real story: they can improve themselves, prepare supplies, and support the settlement without waiting for the player to make every choice.\n\n"
						+ "**-** Acquired items appear in inspectable _inventory-style boxes_. A red _salvaged_ tint marks dismantled loot, blue _trade_ marks new offers, green _keep_ marks stored or used supplies, and yellow _equipped_ marks gear the Defender chose to wear.\n"
						+ "**-** Every Defender has a persistent _20-slot backpack_ and must manage space just like the player. They can buy the same _19-slot specialist bags_ during virtual shop visits, while a Spatial Geode expands one owned bag by _5 slots_ through the same rotating expansion cycle. Inventory use and bag totals appear in their management and expedition summaries.\n"
						+ "**-** Defenders compare weapon damage, armor protection, upgrades, rarity stats, and enchantments before equipping better gear. They refuse cursed equipment and gear beyond their Strength.\n"
						+ "**-** A cursed equipped item locks its slot until the Defender finds or receives a _Scroll of Remove Curse_. Defenders retain cleansing scrolls for that need and save _Scrolls of Upgrade_ until an identified, useful piece of gear falls behind their current progression.\n"
						+ "**-** Replaced and unwanted equipment can enter the Defender's trade stock or be salvaged at the Forge using the _same return rates as the player_. Their own materials are then spent on real Forge upgrade costs whenever an equipped item can be improved.\n"
						+ "**-** Seeds remain in the Defender's inventory between runs until three are available. With an Alchemy station, they use the normal three-seed recipes; useful potions are consumed or stored, while other creations become new trade offers.\n"
						+ "**-** Defenders may now repay gifts of _potions, scrolls, Ankhs, and other accepted supplies_ from their personal resources, extending the same courtesy already used for gifted equipment."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.RECHARGING, true), "Artifact Recharge Rate",
				"Artifacts can now roll _Artifact Recharge Rate_, a new rarity stat that accelerates their natural charge recovery.\n\n"
						+ "**-** The bonus applies across the different ways artifacts naturally regain charge.\n"
						+ "**-** It can stack and improve through the existing rarity-stat progression systems.\n"
						+ "**-** Artifact Recharge Rate is documented in the rarity-stat Journal."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.CHEST), "Elite Loot Scaling",
				"Defeating an elite now grants extra loot based on its rarity, making dangerous encounters more rewarding.\n\n"
						+ "**-** Common elites have a _25%_ bonus-loot chance and Uncommon elites have a _50%_ chance.\n"
						+ "**-** Rare elites guarantee _1_ bonus item, Epic elites guarantee _1_ with a _50%_ chance for another, Legendary elites guarantee _2_, and Transcendant elites guarantee _3_.\n"
						+ "**-** A mob's own loot pool is used when available, with a general item fallback for enemies without one.\n"
						+ "**-** The iron rule remains: mobs killed by a Transcendant Elite drop no loot."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_AMETHYST), "Universal Resistance Rolls",
				"_Stun Resistance_ and every other resistance stat can now roll beyond armor. Armor, rings, trinkets, and artifacts all share the complete resistance pool, so no resistance is exclusive to one equipment category."));

		changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), "Quickslot and Bag Memory",
				"Using a _quickslot shortcut_ no longer sends the inventory view back to the default backpack when the current bag already contains a valid selection. The compact bag window and desktop inventory pane now remember the same bag.\n\n"
						+ "Starter items such as the _Waterskin_ and _Throwing Stone_ also respect existing quickslots when a new dungeon expedition begins. They use an empty slot when one is available and never replace a shortcut the player already assigned."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_TOPAZ), "Resourceful and Treasure Luck",
				"_Resourceful_ now increases the actual quantity of material and forge-resource stacks generated by dungeon rewards. A total of _+2332%_ multiplies a base stack by _24.32x_, so the stat remains meaningful in deep runs.\n\n"
						+ "_Treasure Luck_ can now produce at most _5 normal drops_ from one defeated enemy, preventing extreme values from flooding the floor and inventory. Its remaining value improves quality instead: every _1000%_ guarantees one rarity-tier improvement on rarity-capable drops, while partial progress gives a proportional chance toward another tier. It still does not alter chest rewards or gold stack size."));

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Lost Defender Requests",
				"Lost Defenders found in the dungeon now ask for _one randomly chosen supply_ before returning to the Homebase. Each survivor may need _food_, a _Potion of Healing_, or a _Return Scroll_, making rescues less predictable while preserving a clear single requirement for that Defender."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.POTION_CRIMSON), "Extended Health Readouts",
				"Removed the artificial _1-billion maximum-health limit_. Health calculations can now use the full safe range supported by the combat engine.\n\n"
						+ "Large readouts now share consistent two-decimal shortcuts, including _T_ for trillion, _Qu_ for quadrillion, _Qi_ for quintillion, _Sext_ for sextillion, and continued abbreviations for still larger values."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.CORRUPT, true), "High-Speed Mob Turn Processing",
				"Fixed very deep-floor turns appearing to load indefinitely when extremely fast enemies or corrupted allies acted outside the hero's view.\n\n"
						+ "**-** _Attack Speed is not capped._ Its random gains are smaller, its roll is much less common, and it is no longer automatically selected by level-based stat growth.\n"
						+ "**-** Existing extreme Attack Speed values are smoothly rebalanced once when their save is loaded, while larger values still remain faster than smaller values.\n"
						+ "**-** Movement scheduling remains guarded so high-level enemies cannot queue hundreds of repeated vision checks and paths before returning control.\n"
						+ "**-** This also prevents an engine-heavy enemy turn from incorrectly resembling player inactivity to the moderator shift tracker."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.CHEST), "Infinite-Floor Mimic Loot",
				"Fixed mimics sometimes dying without releasing their stored contents during post-Amulet infinite-floor runs. Mimic inventory now uses a dedicated death-drop path while still respecting the no-loot rule when a Transcendant Elite lands the killing blow. Late attack animations can no longer make a defeated Crystal Mimic steal into an already-released inventory and crash the run."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.ARMOR, true), "Barrier Proc Trigger",
				"Fixed a mob's _Barrier Proc_ activating when that mob was struck. The barrier can now trigger only after its owner lands a damaging hit."));

		changes.addButton(new ChangeButton(Icons.get(Icons.JOURNAL), "Rarity Stat Journal Coverage",
				"Fixed gameplay rarity stats being omitted from the Journal. The catalog now includes every rollable stat, including _Artifact Recharge Rate_, while keeping internal-only markers hidden."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.HASTE, true), "Moderator Shift Time Accuracy",
				"Fixed active moderator service being undercounted during frequent play. Sub-second activity is now preserved instead of rounded away, the valid three-minute window before AFK is counted correctly, and combat performed by allied or corrupted mobs contributes to the active shift. Visible presence now refreshes reliably alongside chat and trade traffic, and an expired server presence is repaired without discarding queued shift time."));

		changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "Desktop Combat Freeze",
				"Fixed a desktop lock-up that could make both Reclaimed Pixel Dungeon and the OpenJDK Platform binary stop responding when a linked Ghoul died. Rendering no longer holds a scene-group lock while reading combat state, allowing sprite removal and health-bar rendering to finish without waiting on each other."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Wayfarer Background Polling Stability",
				"Fixed a crash where recurring _Wayfarer Network_ checks could exhaust the system's available native threads, especially while memory was under heavy pressure. Account, chat, trade, moderation, presence, and reward requests now share one reusable background worker instead of creating a new operating-system thread for every check.\n\n"
						+ "If the system temporarily cannot service a request, the game now releases the affected polling state and retries later instead of allowing the failure to escape into the game loop."));

		changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "Transcendant Item Description Layout",
				"Fixed long Transcendant item descriptions being positioned outside their inspection modal on desktop. The scrollable description now uses the modal's full coordinate space with a contained inner margin."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.PETRIFIED_SEED), "High-Level Trinket Scaling",
				"Fixed several Trinkets losing, reversing, or disabling their intended effects when rarity stats and Homebase training raised their effective level beyond the original `+3` range.\n\n"
						+ "**-** Petrified Seed runestone conversion now continues from _80%_ toward _100%_, while its catalyst-runestone chance scales by _5% per effective level_ from its base _15%_, capped at _100%_. A `+28` effective Petrified Seed therefore shows and applies _100%_ for both rolls.\n"
						+ "**-** Parchment Scrap, Salt Cube, and Vial of Blood preserve their strongest defined endpoint instead of falling back to their weakest behavior above level 3.\n"
						+ "**-** Chaotic Censer remains functional above level 3, keeps scaling its activation interval safely, and uses its strongest gas rarity table.\n"
						+ "**-** Every other Trinket probability is contained between _0% and 100%_, while vision and spawn multipliers retain safe positive minimums so extreme potency cannot produce negative or inverted behavior."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.TRINKET_CATA), "Consecutive Magical Catalysts",
				"Fixed consecutive _Magical Catalyst_ conversions reusing the first catalyst's exact Trinket choices, rarities, and stats when the Alchemy screen remained open. Confirming a choice now consumes the specific catalyst that owns those displayed rolls instead of another catalyst in the inventory, so the next catalyst generates its own fresh selection."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.RECHARGING, true), "Wand Charge Overflow Freeze",
				"Fixed the game becoming permanently unresponsive when an extreme _Potential_ glyph activation attempted to grant an impossibly large amount of wand charge. Wand charging now resolves even enormous or invalid values in one bounded step, fills only the available charges, and safely repairs an invalid saved partial charge instead of trapping the combat animation in an endless loop."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_STONE), "Homebase Wall Integrity",
				"Fixed grass and plants appearing across Homebase walls and fixed knocked-back mobs passing through intact defenses.\n\n"
						+ "**-** Existing Homebase saves remove plants and grassy terrain from every wall, gate, and tower footprint when loaded.\n"
						+ "**-** New vegetation cannot grow on those protected structure cells.\n"
						+ "**-** Knockback now checks each forced step against the Homebase's real structure collision, stopping enemies at intact walls and enemy-blocking gates while still allowing movement through destroyed defenses."));
	}

	public static void add_v0_2_3_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.3", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"_Taking a Character With You_\n" +
				"v0.2.3 began with a practical question: if a player has spent dozens of hours developing a hero, rebuilding the _Homebase_, equipping _Defenders_, and expanding four _Tower Arsenals_, why should that progress be trapped on one device? _Local Save Transfer_ was built to move the complete character rather than a simplified export. Floors, bags, equipment, talents, settlement progression, defenders, and tower upgrades all travel together because each part contributes to the identity of that save. The sender is removed only after the receiver verifies installation, while temporary validation, checksums, version matching, and automatic slot selection protect the character from interrupted transfers and incompatible data.\n" +
				"\n" +
				"_Equipment With a Longer Story_\n" +
				"The _Multiple Enchantments_ system extends equipment progression without discarding the familiar enchantment mechanics already in the game. Five numbered slots make every application intentional: a lower-tier catalyst can replace one chosen effect without erasing the rest of an item's identity. Merge chances create a reason to collect duplicate _Stones of Enchantment_, _Scrolls of Enchantment_, and _Arcane Styluses_, while the increasing rarity of naturally generated multi-enchanted equipment keeps exceptional discoveries genuinely exceptional. Cycling glow colors and expanded descriptions make that power visible instead of hiding several effects behind a single item name.\n" +
				"\n" +
				"_Keeping Endless Depth Dangerous_\n" +
				"Reclaimed Pixel Dungeon allows progression far beyond the original dungeon, and that creates an unusual balance problem: enough rings, trinkets, and bonuses can push resistance beyond _100%_, eventually turning entire environmental systems off. _Escalating Environmental Hazards_ gives harmful gases and blobs potency that grows with true floor depth before resistance is deducted. The intent is not to invalidate defensive builds; resistance still meaningfully lowers the application chance. Instead, deeper floors continue asking the player to respect toxic clouds, paralysis, fire, frost, webs, and other hazards even after assembling an extremely powerful collection of defenses. True immunities remain absolute so distinct class and creature traits continue to matter.\n" +
				"\n" +
				"_Readable Growth_\n" +
				"Long-running saves naturally accumulate resources in the thousands or millions, where full numeric values begin competing with icons and controls for limited interface space. _Compact Resource Amounts_ preserve two useful decimal places while shortening large totals across pickups, inventories, bags, and homebase facilities. The _Expanded Journal Catalog_ serves the same goal from another direction: material resources, forge currencies, special keys, and mimic variants should be discoverable in the same reference system as the rest of the game rather than feeling like disconnected additions.\n" +
				"\n" +
				"_Protecting What the Interface Promises_\n" +
				"The smaller fixes in this release focus on trust between the interface and the underlying game state. _Tower Arsenal Costs_ now show both required and owned resources so an upgrade decision can be understood before it is pressed. _Homebase Stat Bars_ remain contained even when raid damage lowers a building's current cap below an already purchased stat level. Every specialized bag shows only the slots that are genuinely available, preventing empty-looking cells from promising storage the player has not unlocked. Finally, closing a _Defender Trade_ now removes its invisible input controls completely, restoring normal movement and examination without forcing the player to reload their save.\n" +
				"\n" +
				"Together, these changes make v0.2.3 a patch about _continuity_: carrying a complete character between devices, letting prized equipment develop new layers, preserving danger throughout infinite progression, and ensuring that increasingly complex systems remain readable and dependable." );

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Local Save Transfer",
				"Complete saved characters can now be transferred directly to another device on the same Wi-Fi network or hotspot.\n" +
				"\n" +
				"**-** A new _Transfer Save_ button appears below _Continue_ and _Erase_ on saved-character details.\n" +
				"**-** Nearby desktop, Android, and iOS devices become eligible receivers automatically while their game is open on the character-selection screen.\n" +
				"**-** Players can assign a persistent device name under _Settings > Connectivity Settings_ so nearby receivers are easy to recognize.\n" +
				"**-** Transfers include the full save directory: the hero, inventory and bags, talents, explored floors, homebase progression, Defenders and their equipment, tower arsenals, and all other character-bound progress.\n" +
				"**-** Both devices must run the **exact same app version** before they can discover and transfer to one another.\n" +
				"**-** The receiver must approve the request, after which the character is installed automatically into the lowest available save slot.\n" +
				"**-** Every transferred file is checksum-verified and validated in temporary storage before installation. Interrupted or invalid transfers cannot replace an existing save.\n" +
				"**-** The sender removes its local save only after the receiver confirms that the verified character was installed successfully. If confirmation is interrupted, the sender keeps its save to prevent progress loss.\n" +
				"\n" +
				"_Transfer Reliability_\n" +
				"**-** Receiving a save selects the lowest empty destination slot directly from the original request prompt, avoiding unsafe chained windows across Android and desktop.\n" +
				"**-** Nearby devices advertise frequently, with a fallback scan that returns early once receivers are found.\n" +
				"**-** Received packages are fully preview-validated in temporary storage before installation.\n" +
				"**-** Deleted-save markers are cleaned before installation, and verified files are copied explicitly instead of relying on platform-specific directory moves.\n" +
				"**-** Receiver generations are isolated so an old character-selection scene cannot interfere with its replacement listener."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), "Expanded Journal Catalog",
				"The journal now includes the previously missing homebase resources and special-chest discoveries.\n" +
				"\n" +
				"**-** Building materials, forge currencies, Arcane Keys, and Provision Keys are listed under _Misc. Consumables_.\n" +
				"**-** Arcane Reliquary Mimics and Provision Cache Mimics are listed under _Universal Enemies_."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_ENCHANT,
				new ItemSprite.CyclingGlowing(0xFF3333, 0xFFCC33, 0x55DD55, 0x3399FF, 0xAA55FF)), "Multiple Enchantments",
				"Weapons and armor can now hold up to **five enchantments** in independently targeted slots.\n" +
				"\n" +
				"**-** Stones of Enchantment, Scrolls of Enchantment, and Arcane Styluses now use tiers **I-V**, with each tier adding or replacing only its matching slot.\n" +
				"**-** Two copies of the same catalyst and tier can be merged through alchemy. Success chances follow Ascendant Spark merging: **100%**, **90%**, **75%**, and **60%**.\n" +
				"**-** Single-enchantment equipment keeps its familiar enchanted name. Equipment with multiple effects lists every enchantment and description in its information panel.\n" +
				"**-** Multi-enchanted equipment cycles its glow through every visible enchantment color.\n" +
				"**-** Scroll of Upgrade enchantment-loss rolls remove only one eligible effect instead of clearing every slot.\n" +
				"**-** Naturally enchanted weapons and armor can rarely generate with multiple distinct effects; three to five effects are exceptionally rare.\n" +
				"**-** Existing enchanted equipment is migrated safely into slot I when older saves are loaded."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.POISON, true), "Escalating Environmental Hazards",
				"Harmful gases and blobs now contest the hero's matching resistance with trigger potency that continues growing on deeper floors.\n" +
				"\n" +
				"**-** Hazard potency begins at **100%** and gains **3% per true floor**: `100% + 3% x (floor - 1)`.\n" +
				"**-** Final application chance is `hazard potency - matching resistance`, clamped between **0% and 100%**.\n" +
				"**-** Resistance is spent on the trigger roll and is not applied a second time to the resulting damage or duration. Other class, buff, property, immunity, and Ring of Elements defenses still apply normally.\n" +
				"**-** Scaling covers toxic, corrosive, paralytic, fetid, confusion, fire, freezing, electrical, regrowth, web, and boss-specific harmful blobs.\n" +
				"**-** True blob immunities remain absolute, while harmless, decorative, and beneficial blobs are unaffected.\n" +
				"**-** True floor depth is uncapped, allowing environmental pressure to keep pace with resistance stacking throughout infinite regions."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_STONE), "Compact Resource Amounts",
				"Large resource totals now use shorter values that remain easy to read while preserving useful precision.\n" +
				"\n" +
				"**-** Stack pickup totals use _k_, _m_, or _b_ suffixes with two decimal places, such as _Stone x7(1.35k)_.\n" +
				"**-** Inventory, bag, and homebase facility resource readouts follow the same two-decimal compact format.\n" +
				"**-** Amounts below 1,000 continue to display their exact whole-number value."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SWORD), "Tower Arsenal Cost Readout",
				"Fixed tower weapon unlock and upgrade costs displaying only the required amount. Each resource now shows `required/owned`, making affordability visible before spending resources."));

		changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "Homebase Stat Bar Overflow",
				"Fixed homebase stat progress bars extending outside their upgrade boxes after raid damage lowers a building's level and reduces its current stat cap. Over-cap values such as `10/8` remain visible, while the bar itself is contained at full width."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BACKPACK), "Desktop Bag Slot Display",
				"Fixed the desktop inventory showing additional slots that had not actually been unlocked. Every vanilla and Reclaimed specialized bag now starts with **19 usable slots**, while genuine upgrades from Spatial Geodes continue to add slots normally."));

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.ROGUE, 1), "Defender Trade Input Cleanup",
				"Fixed parts of the dungeon remaining unclickable after buying from or exiting a Defender trade. Rebuilt trade controls are now fully destroyed instead of leaving invisible pointer areas over the hero's field of view."));
	}

	public static void add_v0_2_2_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.2", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"_Building the Defense_\n" +
				"v0.2.2 expands _Settlement Defense_ from something the player simply survives into a system the whole homebase can prepare for. _Tower Arsenals_ were added so rebuilding a tower creates a lasting defensive asset with its own identity and progression. Random weapon unlocks make each tower develop differently, while separate _Damage_, _Vision Range_, and _Cooldown_ upgrades give gathered resources another meaningful long-term use. Their limits and targeting rules were chosen carefully: towers should become powerful, but they should not cover impossible distances, flood a single turn with attacks, or endanger the hero and allied defenders they were built to protect.\n" +
				"\n" +
				"_Protecting Defender Progress_\n" +
				"_Defenders_ received the same attention because they represent time and investment, not disposable raid pieces. Rare defenders are now guaranteed to enter the _Skill System_ instead of reaching the required rarity with nothing to show for it, and older affected defenders repair themselves when loaded. _Friendly Fire Protection_ prevents the player's projectiles from erasing an ally trained over many dungeon runs. Surviving defenders also earn _20-30% of their next level_ after a successful raid, rewarding the allies who actually helped hold the settlement instead of making raid survival feel disconnected from their growth.\n" +
				"\n" +
				"_Clearer Rewards and Information_\n" +
				"Several interface changes are about making growing systems easier to read. The _Raid Wave Readout_ separates the current wave from the remaining enemy count so players can judge how much of the attack is left. _Stack Pickup Totals_ show both what was collected and the new inventory total, which is especially useful once homebase materials begin accumulating in the hundreds. Tower upgrade screens expose real _Damage_, _Vision Range_, and _Cooldown_ values so resource spending can be understood before committing to it.\n" +
				"\n" +
				"The rare _Arcane Reliquary_ and _Provision Cache_ also needed their rewards to keep pace with the danger of deeper expeditions. Their old growth added too little from one region to the next, making a rare locked chest on a late floor feel barely different from one found near the entrance. Each new region now adds a full reward bundle: _5 catalyst stones_ for the Arcane Reliquary and _10 homebase resources_ for the Provision Cache. This keeps their rarity meaningful and makes pushing deeper feel properly rewarded.\n" +
				"\n" +
				"Deeper floors should also feel larger in more than just their enemy numbers. Every dungeon region now adds _one additional room_ to ordinary floor generation, chosen from the existing _Standard_, _Secret_, or _Special Locked_ room pools. This gives longer expeditions more ground to explore and more chances for unusual discoveries, while preserving the dungeon's established room rules and allowing endless regions to continue expanding naturally.\n" +
				"\n" +
				"Acquiring the _Amulet of Yendor_ should be a triumph, not an easy way to lose a settlement by mistake. Its first-pickup screen no longer places the immediate game-ending choice beside the option to continue. Players can safely return to their growing homebase, while intentionally ending the run remains available through the Amulet's dedicated item action.\n" +
				"\n" +
				"_Reliable Encounters_\n" +
				"The bug fixes in this patch protect the game state as much as the player's progress. The _Dwarf King_ now validates every ritual transition so indirect damage cannot leave him alive, invulnerable, and impossible to target. _Elite Rarity Auras_ now refresh after natural spawn rarity rolls, ensuring newly spawned elites communicate their danger as reliably as test-spawned ones. _Tower Targeting_, projectile effects, bomb volleys, and actor cleanup were also tightened because a defensive feature is not useful if it makes turns stall or hides whether a weapon actually fired. Together, these changes make raids more active without sacrificing clarity, performance, or trust in the systems surrounding them.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SWORD), "Tower Arsenals",
				"Homebase towers can now unlock weapons and actively defend the settlement during raids.\n" +
				"\n" +
				"**-** Each of the four towers maintains its own permanent arsenal.\n" +
				"**-** Every unlock rolls evenly from **16 thrown weapons**, **8 offensive wands**, and **9 harmful bombs** that the tower has not already unlocked.\n" +
				"**-** There is no arsenal-size limit beyond unlocking every available weapon, while each additional unlock becomes more expensive.\n" +
				"**-** Every weapon has separately upgradeable _Damage_, _Vision Range_, and _Cooldown_ values.\n" +
				"**-** Each ready weapon fires independently, so its cooldown directly controls its own attacks.\n" +
				"**-** Bombs retain their visible projectile and normal two-turn fuse before detonating.\n" +
				"**-** Tower attacks exclusively target active raiders and cannot harm the hero or allied defenders.\n" +
				"**-** Tower windows keep a focused Structure tab for their arsenal, with centered identified weapon slots, compact resource costs, and live combat values for every upgrade.\n" +
				"**-** Cooldown upgrades take 30 levels to reach their 1.00-turn floor, while Vision Range advances one tile at a time and caps when the tower covers the full homebase.\n" +
				"**-** Raid wave text now sits beneath the raid bar with a clearer gap.\n" +
				"**-** Tower targeting reuses its raid scan and cached arsenal data, while simultaneous tower bombs and raid progress resolve in compact batches to keep turns responsive.\n" +
				"**-** Resolved bomb volleys now leave the actor queue cleanly, preventing stalled raid turns.\n" +
				"**-** Disintegration, Prismatic Light, and Lightning wands use their proper beam or arc effects when fired by towers."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Defender Skill Eligibility",
				"Fixed Rare and higher defenders sometimes receiving no combat skill.\n" +
				"\n" +
				"**-** A defender now learns their first skill as soon as they reach Rare rarity.\n" +
				"**-** Existing Rare or higher defenders without a skill are repaired automatically when loaded.\n" +
				"**-** Later ten-level milestones retain their normal chance to add or improve skills."));

		changes.addButton(new ChangeButton(Icons.get(Icons.TARGET), "Defender Friendly Fire",
				"Player attacks and projectiles can no longer damage allied homebase defenders. Weapon effects are stopped before they can trigger against an allied defender."));

		changes.addButton(new ChangeButton(new Image(new KingSprite()), "Dwarf King Indirect-Kill Guard",
				"Fixed the _Dwarf King_ becoming untargetable after non-standard or indirect damage.\n" +
				"\n" +
				"**-** His ritual phases now validate their health and shield state before every turn.\n" +
				"**-** Indirect damage and direct death calls can no longer bypass a phase transition or prematurely end the fight.\n" +
				"**-** A phase-two King whose shield is gone now reliably enters the final phase instead of remaining invulnerable."));

		changes.addButton(new ChangeButton(new Image(new RatKingSprite()), "Elite Aura Spawn Refresh",
				"Fixed naturally spawned elite mobs occasionally missing their rarity aura.\n" +
				"\n" +
				"**-** Newly spawned and respawned elites now refresh their aura as soon as their rarity is rolled."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_STONE), "Stack Pickup Totals",
				"Picking up a stackable item now shows both the amount collected and the total held in the inventory.\n" +
				"\n" +
				"**-** Example: _Stone x7(135)_ means 7 stone were picked up and 135 are now held.\n" +
				"**-** Totals read from the correct storage source for _Gold_, _Energy Crystals_, nested _Material Satchel_ stacks, and resources deposited directly into the homebase."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.PROVISION_CACHE), "Special Chest Reward Scaling",
				"Rare locked special chests now gain a full reward bundle with every dungeon region, making deep-floor discoveries appropriately valuable.\n" +
				"\n" +
				"**-** The _Arcane Reliquary_ gains **5 additional catalyst stones per region**.\n" +
				"**-** The _Provision Cache_ gains **10 additional homebase resources per region**.\n" +
				"**-** Region totals begin at **5/10** and grow to **10/20**, **15/30**, **20/40**, and **25/50** through the five standard regions."));

		changes.addButton(new ChangeButton(Icons.STAIRS.get(), "Expanding Dungeon Floors",
				"Ordinary dungeon floors now grow larger as the expedition reaches deeper regions.\n" +
				"\n" +
				"**-** Every region adds **1 additional room** to each generated floor.\n" +
				"**-** Each added room rolls evenly between the existing _Standard_, _Secret_, and _Special Locked_ room pools.\n" +
				"**-** Expansion uses the true floor depth and continues through infinite post-Amulet regions."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.AMULET), "Safer Amulet Acquisition",
				"The first-pickup Amulet screen no longer includes the immediate _Let's Call It a Day_ ending option.\n" +
				"\n" +
				"**-** Acquiring the Amulet now offers only the option to continue the active run and preserve the settlement.\n" +
				"**-** Players who intentionally want to complete the run can still use the Amulet's dedicated _End the Game_ item action."));

		changes.addButton(new ChangeButton(Icons.get(Icons.BUFFS), "Raid Wave Readout",
				"The raid boss bar now displays _Wave x/y_ separately from the number of living raiders, making multi-wave settlement defenses easier to follow.\n" +
				"\n" +
				"**-** After successfully repelling a raid, every surviving defender gains a random **20-30%** of the XP required for their next level."));
	}

	private static void addDevCommentary( ChangeInfo changes, String text ){
		changes.addButton(new ChangeButton(Icons.get(Icons.RECLAIMED), "Developer Commentary", text));
	}

	private static void addTabbedDevCommentary( ChangeInfo changes, String[] tabLabels, String... messages ){
		changes.addButton(new TabbedChangeButton(Icons.get(Icons.RECLAIMED), "Developer Commentary", tabLabels, messages));
	}

	public static void add_v0_2_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.1", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"v0.2.1 is about making the settlement's growth feel visible, dependable, and worth exploring for. The new _Arcane Reliquary_ and _Provision Cache_ give ordinary dungeon floors more memorable discoveries, while their matching mimics make sure those prizes still carry a little danger. Deeper expeditions fill these caches with larger rewards, reinforcing the idea that pushing onward should matter.\n" +
				"\n" +
				"Defenders received the other major part of this update. Every living defender now completes a simulated dungeon run after the hero returns from a meaningful expedition. Their scouting XP grows predictably with the deepest floor reached and is further improved by their own _XP Gain_ stat. This replaces the old participation roll and inconsistent reward range, so a deeper expedition can no longer leave a defender with less progress than a shallow one. Defenders can also grow into higher rarities and may learn combat skills once they become seasoned enough. I want rescued defenders to feel like members of the settlement who reliably develop alongside the hero, rather than equipment holders who stop changing after recruitment.\n" +
				"\n" +
				"A lot of this release is also visual groundwork for long-term progression. _Transcendant_ XP, defender equipment, and _Magical Catalyst_ choices now use clearer, more consistent displays. Rarity glows remain visible where they matter, and catalyst choices use the normal identified slot appearance without prematurely adding those trinkets to the hero's discoveries.\n" +
				"\n" +
				"Finally, the Android status display and defender trade exit received focused fixes. Health and Shield should stay beside the hero portrait across supported layouts, and returning from a defender trade should no longer leave actors in a broken visual state. These fixes are less flashy than a new chest or defender skill, but they are essential to making every expedition and return home feel trustworthy.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.PROVISION_CACHE), "Locked Special Chests",
				"Added two rare locked chests to ordinary dungeon floors, each with its own matching key.\n" +
				"\n" +
				"**-** The _Arcane Reliquary_ contains at least **5 catalyst stones**, drawn from up to **3 catalyst types**.\n" +
				"**-** The _Provision Cache_ contains at least **10 recovered resources**, drawn from up to **3 resource types**.\n" +
				"**-** Each chest has an independent **5%** chance to appear on an ordinary floor.\n" +
				"**-** Their total loot grows every five dungeon floors, rewarding deeper expeditions with fuller caches.\n" +
				"**-** Special chest contents are not increased by treasure luck or the Ring of Wealth, preserving their intended three-type reward limit."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.MIMIC, 9 * 16, 4 * 16, 16, 16), "New Mimic Enemies",
				"The new locked chests may reveal themselves as dangerous new mimic enemies.\n" +
				"\n" +
				"**-** _Arcane Reliquary Mimics_ and _Provision Cache Mimics_ disguise themselves as their matching special chests.\n" +
				"**-** Each variant uses its own chest colors and attacking appearance, making it distinct once its disguise breaks.\n" +
				"**-** They use the same mimic appearance chance as vanilla locked chests, so every special cache carries a familiar element of risk."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Defender Growth",
				"Defenders now continue developing as experienced members of the settlement.\n" +
				"\n" +
				"**-** Defender inspection now shows a labeled level and XP bar matching the Transcendant progression display.\n" +
				"**-** Every living defender completes a simulated dungeon run whenever the hero returns from a meaningful expedition.\n" +
				"**-** Scouting XP grows predictably with the deepest floor reached and benefits from that defender's _XP Gain_ rarity stat.\n" +
				"**-** Defenders retain their chance to discover a new rarity stat every five levels.\n" +
				"**-** Every ten levels, a defender can ascend to the next rarity using the same chance as an unenhanced _Stone of Ascendant Spark_.\n" +
				"**-** Rare and higher defenders can learn combat skills, then gain or improve a skill at later ten-level milestones. Their skills only target settlement enemies."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Progression Item Displays",
				"Improved several equipment displays so item growth and rarity are easier to read.\n" +
				"\n" +
				"**-** _Transcendant_ item XP now uses a thicker progression bar with its current and required XP centered inside it.\n" +
				"**-** Defender weapons, armor, and ranged equipment now appear in inventory-style slots across inspection and management screens.\n" +
				"**-** Defender equipment slots now preserve rarity glows, making valuable equipment easier to recognize.\n" +
				"**-** Trinkets offered by a _Magical Catalyst_ now use the same inventory-style slots and display their rarity glows clearly."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), "Android Status Bars",
				"Fixed the hero status bars in Android builds.\n" +
				"\n" +
				"**-** Health and Shield no longer overlap the hero portrait.\n" +
				"**-** The Health border now spans the full Health fill.\n" +
				"**-** The Shield border, fill, and value now stay aligned as one group."));

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.ROGUE, 1), "Defender Trade Exit",
				"Fixed control and crash problems that could occur after closing a defender's trade window.\n" +
				"\n" +
				"**-** Closing the final defender trade window now restores normal ground movement and pathfinding.\n" +
				"**-** Energy Crystals purchased from defenders now go directly into the Energy resource counter instead of occupying inventory slots. Existing inventory copies are converted automatically when the save is loaded.\n" +
				"**-** Defenders can no longer resume movement, combat, or status animations through a missing on-screen sprite after a trade window closes.\n" +
				"**-** The same protection now covers the related return-from-exchange actor state, preventing the next walked turn from crashing the game."));
	}

	public static void add_v0_2_0_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.2.0", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"v0.2.0 starts a new cleanup track after the first wave of Reclaimed systems came together. The focus here is trust: trades should finish cleanly, extreme late-game upgrades should not break the hero, and boss fights should not become farming loops or softlocks.\n" +
				"\n" +
				"The _Dwarf King_ fixes are especially important for endless runs. His summoned subjects should be part of the boss fight, not a way to farm rare boss-only rewards, and cleave-heavy builds should never trap him in a half-dead ritual state.\n" +
				"\n" +
				"This patch also keeps a closer eye on runaway _Transcendant_ item growth. Huge stat lists should be easier to read, and a few defensive stats now have per-item ceilings so late-game gear stays powerful without becoming impossible to balance. Status information received the same attention: shielding, large buff icons, and growing enemy stat sheets now have dedicated spaces that stay readable without covering other parts of the interface.\n" +
				"\n" +
				"_Elite Mobs_ bring a new kind of pressure to that late-game growth. Their rarity rises with dungeon depth, their core defenses do not consume their active skill allowance, and their random abilities give individual enemies distinct combat identities instead of merely making every number larger. Their abilities also now belong fully to the dungeon world: invisibility, surprise attacks, bombs, smoke, webs, clouds, summoned echoes, and enemy-on-enemy hunts follow the same readable rules players already know from ordinary combat. Familiar item icons now accompany their skill announcements, making it easier to recognize what an Elite is doing before deciding how to respond.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), "Shield Bar",
		"Improved the hero status display for high-shield builds.\n" +
		"\n" +
		"**-** Shielding now appears as its own blue bar instead of being merged into Health as a gray overlay.\n" +
		"**-** The mobile interface places Shield below Health, while the full desktop interface places Shield above Health.\n" +
		"**-** Health and shield numbers now use shorter values such as _200.75k_ or _1.27m_ when they become very large.\n" +
		"**-** This should keep endless-run Health text readable instead of letting big numbers spill out of the bar."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.FORGE_EMBER_SHARD), "Forge Currency Artwork",
		"Added full-size item artwork for forge currencies so valuable drops are easier to notice in busy dungeon floors.\n" +
		"\n" +
		"**-** _Scrap_, _Ember Shards_, and _Ember Cores_ now use distinct full-size sprites when found on the ground or viewed as items.\n" +
		"**-** Their compact icons remain in resource strips, upgrade costs, and other currency displays."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CATALOG), "Enchanted Class Calls",
		"Polished the completed class-unlock items and made these rare discoveries easier to track.\n" +
		"\n" +
		"**-** Class Calls now pulse with an enchanted glow that sets them apart from ordinary remains.\n" +
		"**-** Every Class Call is now recorded under _Misc. Equipment_ in the catalogue.\n" +
		"**-** The _Spatial Geode_ is now also recorded under _Misc. Equipment_."));

		changes.addButton(new ChangeButton(new Image(new RatKingSprite()), "Elite Mobs",
		"Added rare elite enemies whose strength, appearance, abilities, and rewards grow with the depths.\n" +
		"\n" +
		"**-** Elite spawn chance begins at **1%**, rises gradually with floor depth, and caps at **33%**.\n" +
		"**-** Early floors strongly favor _Common_ elites, while deeper endless floors increasingly favor _Epic_, _Legendary_, and _Transcendant_ elites.\n" +
		"**-** Every elite receives free _Ironbound_, _Titanic Vitality_, and _Elite Amplification_ traits. These core traits do not consume the elite's rarity-based skill allowance.\n" +
		"**-** Common through Transcendant elites receive **1-6 additional skills**, chosen with limits that prevent excessive hard control, teleportation, or summoning combinations.\n" +
		"**-** Elite skills include combat passives, damaging auras, crowd control, ranged openers, barriers, healing, invisibility, teleportation, and other tactical effects.\n" +
		"**-** Epic and higher elites use survival abilities more deliberately when badly wounded.\n" +
		"**-** Elite names and rotating auras use their rarity color, and inspection lists their core traits and rolled skills.\n" +
		"**-** Defeating an elite grants bonus XP and a rarity-scaled chance for additional native loot."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.ARMOR, true), "Shield Readouts",
		"Made current shielding easier to check outside the main dungeon view.\n" +
		"\n" +
		"**-** Save details now list the hero's current shield as its own stat.\n" +
		"**-** The Hero Info screen now lists shield separately from Health.\n" +
		"**-** This matches the new separate shield bar and makes temporary protection easier to track."));

		changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.BLESS, true), "Full Interface Status Layout",
		"Moved the large buff and debuff icons and the turn indicator into clearer positions in the full desktop interface.\n" +
		"\n" +
		"**-** Large status icons now appear beside the Health, Shield, and Experience bars.\n" +
		"**-** Status icons wrap within their own area instead of rising into recent messages.\n" +
		"**-** The turn indicator now occupies the status icons' former position instead of overlapping the new icon grid.\n" +
		"**-** The turn indicator sits close to the status bars and shifts only enough to make room while Shield is active.\n" +
		"**-** Recent messages automatically reserve space for the shield-aware turn indicator.\n" +
		"**-** The mobile interface status icon layout is unchanged."));

		changes.addButton(new ChangeButton(new Image(new RatSprite()), "Enemy Inspection",
		"Reworked enemy inspection so growing mobs remain easy to read.\n" +
		"\n" +
		"**-** Enemy information and rarity stats now share one scrollable Info tab.\n" +
		"**-** The inspection window stays at a fixed readable size even when an enemy has many stats.\n" +
		"**-** Enemy Health now uses a thicker bar with current and maximum values shown inside it.\n" +
		"**-** Shielded enemies gain a separate blue shield bar beneath Health while that shield is active."));

		changes.addButton(new ChangeButton(new Image(new RatKingSprite()), "Elite Combat Rules",
		"Connected Elite abilities to the dungeon's familiar combat mechanics so their attacks are challenging but readable.\n" +
		"\n" +
		"**-** Invisible Elites now hide their sprite, aura, overhead Health, and targeting indicator until they reveal themselves. Their first attack from concealment counts as a surprise attack.\n" +
		"**-** Elite ranged skills now show the matching projectile, wand effect, web, growth effect, or armed bomb instead of dealing unexplained damage.\n" +
		"**-** Elite bombs keep their normal fuse and blast size. Bomb-throwing Elites recognize their own armed explosives and try to leave the danger area.\n" +
		"**-** Timed Elite skills now display a familiar potion, scroll, wand, dart, or bomb icon above the skill name. Powder Rain shows the exact bomb being thrown.\n" +
		"**-** Veilstep announces itself where the Elite vanished rather than revealing where the invisible enemy escaped to.\n" +
		"**-** Smoke bombs thrown by enemies now obscure the hero, while Elite cloud and aura skills apply their intended effects to nearby targets.\n" +
		"**-** Echo Legion copies inherit part of their creator's rarity stats, grant no loot, and are not hunted by their own Transcendant creator.\n" +
		"**-** Transcendant Elites can hunt other mobs for XP. If a Transcendant Elite deals the killing blow, that victim drops no loot at all, even when the hero helped damage it first."));

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Transcendant Item Details",
		"Improved item detail windows for very long _Transcendant_ stat lists.\n" +
		"\n" +
		"**-** Transcendant item detail windows now use a fixed readable size instead of expanding until they fill the whole screen.\n" +
		"**-** Long rarity stat lists can now be scrolled inside the detail window.\n" +
		"**-** This keeps the item actions and surrounding interface easier to reach when inspecting heavily leveled gear."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
		changes.hardlight(CharSprite.POSITIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new Image(new AlbinoSprite()), "Stronger Mob Growth",
		"Buffed the core combat growth enemies receive from every mob level.\n" +
		"\n" +
		"**-** Max Health now gains a guaranteed randomized increase at every level, so equal-level enemies no longer share identical Health growth.\n" +
		"**-** Attack Speed, Movement Speed, and Attack Accuracy each have a **70%** growth chance per level.\n" +
		"**-** Attack Damage and Armor each have a **50%** growth chance per level.\n" +
		"**-** Guard Break has a **30%** growth chance per level.\n" +
		"**-** Attack Bonus and Armor Bonus each have a **25%** growth chance per level.\n" +
		"**-** Successful rolls still use their existing value ranges, allowing mobs to develop different strengths as their levels rise."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(CharSprite.NEGATIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.SCROLL_COLOR), "Transcendant Stat Limits",
		"Added two more per-item limits for defensive _Transcendant_ stats.\n" +
		"\n" +
		"**-** Barkskin Power now caps at **+50** on each item.\n" +
		"**-** Critical Damage Reduction now caps at **+1000%** on each item.\n" +
		"**-** Multiple equipped items can still add their capped bonuses together."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.DUELIST, 1), "Defender Trades",
		"Fixed a defender trading bug that could complete more of a trade than the player actually bought.\n" +
		"\n" +
		"**-** Buying one defender trade now removes only that selected offer.\n" +
		"**-** The defender only receives payment for the item that was actually purchased.\n" +
		"**-** Closing the trade window after one purchase should no longer make other offers vanish or move items and currency into the wrong inventories."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Extreme Item Scaling",
		"Added safety limits around very high item levels and potency scaling.\n" +
		"\n" +
		"**-** Extremely upgraded _Ring of Might_ values should no longer push the hero's Health into negative numbers.\n" +
		"**-** Very high weapon damage and armor values are now clamped to safe numbers instead of overflowing.\n" +
		"**-** This keeps late endless builds powerful without letting number overflow instantly kill the hero or corrupt combat values."));

		changes.addButton(new ChangeButton(new Image(new GhoulSprite()), "Dwarf King Summons",
		"Stopped the _King of Dwarves'_ summoned subjects from acting like full reward enemies.\n" +
		"\n" +
		"**-** Dwarf King summons no longer grant XP.\n" +
		"**-** Dwarf King summons no longer drop normal loot, catalysts, or material resources.\n" +
		"**-** _Spatial Geodes_ now require the dying enemy to actually be a boss, so summoned minions on every 50th floor can no longer roll boss-exclusive geode drops."));

		changes.addButton(new ChangeButton(new Image(new KingSprite()), "Dwarf King Cleave Softlock",
		"Added another guard for the _King of Dwarves_ during his invulnerable summoning phase.\n" +
		"\n" +
		"**-** Non-ritual damage can no longer push the King below his phase-two safety threshold while his shielded ritual is active.\n" +
		"**-** This prevents splash or cleave-style damage from leaving him untargetable, invulnerable, and blocking the floor exit."));
	}

	public static void add_v0_1_9_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.9", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This update was about making long-term Reclaimed saves feel roomy enough to breathe. The _Wayfarer Exchange_ grew from a simple two-player test into a real gathering room, because trading should feel like meeting other adventurers, not like pressing buttons through a wire.\n" +
				"\n" +
				"_Emeralds_ were added to give every completed trade a small expedition cost. They are intentionally scarce, found only as dungeon floor loot, so trading stays meaningful without turning settlement materials into a bargaining tax.\n" +
				"\n" +
				"_Spatial Geodes_ and the inventory rework support the same idea from the loot side: endless dungeon crawls should keep giving you new reasons to push deeper, while bigger bags stay practical instead of swallowing the screen.\n" +
				"\n" +
				"A few runaway _Transcendant_ stats were also given per-item limits. The goal is to keep lucky, powerful items exciting without letting one piece of gear completely take over resource drops, treasure luck, or movement speed forever.\n" +
				"\n" +
				"Enemy scaling was also tuned around powerful equipment. If your active rings, artifacts, or trinkets are pushing their effects higher through rarity potency, the dungeon now reads that power and pushes back a little harder.\n" +
				"\n" +
				"_Attack Accuracy_ and _Guard Break_ are part of that same balance pass. Dodge and Block can still make strong builds, but now both have clear counters: accuracy pressures dodge, while Guard Break cuts directly into Block Chance.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Six-Trader Lobby",
		"Expanded the _Wayfarer Exchange_ so nearby players on the same network can gather in one trade room.\n" +
		"\n" +
		"**-** Up to **six** traders can now occupy the exchange room at once, each assigned to a pedestal around the central trade table.\n" +
		"**-** You can inspect another trader, view their equipped gear, and send a trade request directly to them.\n" +
		"**-** Trade requests can be accepted or declined, and players already trading are clearly marked as busy.\n" +
		"**-** When someone joins or leaves, the trading room updates so everyone sees who is still available."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.EMERALD), "Emerald Trade Fees",
		"Added _Emeralds_, a scarce currency used only to seal trades in the _Wayfarer Exchange_.\n" +
		"\n" +
		"**-** Each trader now spends **1 Emerald** when a trade is completed, giving trading a small expedition cost without using settlement resources.\n" +
		"**-** Emeralds cannot be traded and are not used for building, defense, or permanent stat training.\n" +
		"**-** Emeralds are found as rare loose floor loot in dungeon runs, never as monster drops, chest bonus drops, or luck-based rewards.\n" +
		"**-** Emerald spawn chance starts at **1%** in regions 1-2, rises to **5%** in regions 3-4, and becomes **8%** in region 5.\n" +
		"**-** In endless post-Amulet regions, Emerald spawn chance continues to ramp upward and caps at **30%**.\n" +
		"**-** The Wayfarer Exchange is still opened from the hero info screen while at the homebase, and the trade screen shows the Emerald fee before you confirm."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SPATIAL_GEODE), "Spatial Geodes",
		"Added _Spatial Geodes_, an extremely rare boss reward for deep endless expeditions.\n" +
		"\n" +
		"**-** Bosses on every **50th depth** now have a **5%** chance to drop a Spatial Geode, continuing for as long as the endless dungeon goes.\n" +
		"**-** Using a Spatial Geode permanently expands one chosen bag by **5 slots**.\n" +
		"**-** Each bag must be expanded once before the same bag can be expanded again.\n" +
		"**-** Expanded inventories now keep equipped slots frozen at the top while the bag contents scroll below them, so larger bags no longer stretch the inventory window.\n" +
		"**-** Spatial Geodes can be offered through the _Wayfarer Exchange_ like other valuable loot."));

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Accuracy Counterplay",
		"Expanded _Attack Accuracy_ so high-dodge enemies and players have a clearer counter.\n" +
		"\n" +
		"**-** Attack Accuracy can now appear on rings, artifacts, and trinkets in addition to weapons.\n" +
		"**-** Active Attack Accuracy improves hit chance and helps pressure Dodge Chance.\n" +
		"**-** Mobs can also roll Attack Accuracy, giving enemy builds a way to threaten dodge-heavy players."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MASTERY), "Guard Break",
		"Added _Guard Break_, a new Epic rarity stat that directly counters Block Chance.\n" +
		"\n" +
		"**-** Guard Break reduces the defender's Block Chance point-for-point when you attack.\n" +
		"**-** Enough Guard Break can completely shut off a block attempt.\n" +
		"**-** Guard Break can appear on weapons, rings, artifacts, trinkets, and enemies."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK_LRG), "Inventory Rework",
		"Reworked inventory windows so expanded bags stay comfortable to use.\n" +
		"\n" +
		"**-** Equipment slots stay fixed at the top while bag items scroll below them.\n" +
		"**-** Bigger bags no longer stretch the inventory window over the hotbar or across the whole screen.\n" +
		"**-** Empty bag spaces now stay clean instead of showing placeholder icons.\n" +
		"**-** Scrolling has been improved for mobile and desktop layouts."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
		changes.hardlight(CharSprite.POSITIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new Image(new AlbinoSprite()), "Mob Rarity Chances",
		"Clarified and preserved how high-level enemy rarity stats scale in endless play.\n" +
		"\n" +
		"**-** Enemy proc and chance stats can stack past **100%** when mob levels keep rising.\n" +
		"**-** This matches the player's ability to stack very high stats across multiple strong items.\n" +
		"**-** Ring Potency, Artifact Potency, and Trinket Potency from active rarity stats now contribute to mob level scaling.\n" +
		"**-** Enemy builds should feel more focused over time, with stacked strengths instead of only long lists of tiny bonuses."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(CharSprite.NEGATIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.SCROLL_COLOR), "Transcendant Stat Limits",
		"Added per-item limits to a few _Transcendant_ stats that could grow too far during endless progression.\n" +
		"\n" +
		"**-** Resourceful now caps at **+1000%** on each item.\n" +
		"**-** Treasure Luck now caps at **+1000%** on each item.\n" +
		"**-** Movement Speed now caps at **+500%** on each item.\n" +
		"**-** Multiple equipped items can still add their capped bonuses together."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.EXIT), "Trader Disconnects",
		"Improved how the Wayfarer Exchange handles traders leaving or disconnecting mid-session.\n" +
		"\n" +
		"**-** A disconnected trader now disappears from the trading floor right away.\n" +
		"**-** Open exchange windows now show that the trader disconnected instead of turning into a blank box.\n" +
		"**-** The trading floor vision radius has been expanded so the room is easier to read with more players inside."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.REMAINS), "Class Fragment Hoarding",
		"Fixed class-fragment boss drops staying too generous when a player saved up fragments without crafting the unlock item.\n" +
		"\n" +
		"**-** The game now checks your bags, floor drops, and the Quartermaster's Vault for five matching fragments.\n" +
		"**-** Once you have enough fragments to unlock a locked class, the boss fragment drop chance drops from **50%** to **25%** even if you have not crafted the unlock item yet.\n" +
		"**-** This keeps the intended pacing for unlocking more classes without letting players stockpile full unlock sets at the starter drop rate."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Weapon Rarity Procs",
		"Fixed weapon rarity proc damage not always carrying into the final hit.\n" +
		"\n" +
		"**-** Damage-changing rarity effects, including Legendary combo damage and piercing, now feed their updated damage back into the attack.\n" +
		"**-** Weapons with very high proc chances should now feel much more consistent when their effects trigger."));
	}

	public static void add_v0_1_8_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.8", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This patch tightened the first playable version of trading. The goal was to make the _Wayfarer Exchange_ feel trustworthy: if both players agree to a trade, the game should preserve the result cleanly, return everyone safely, and never leave one side wondering where their items went.\n" +
				"\n" +
				"Trader inspection also became more expressive here. Seeing another player's name, class, level, and gear gives the exchange a social purpose beyond moving items around. It becomes a little showcase for builds, trophies, and questionable dungeon decisions.\n" +
				"\n" +
				"The raid and image XP fixes were part of the same cleanup pass: rewarding clever play is good, but runaway farming and turn-spinner stalls make the game feel less solid.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Trader Inspection",
		"Expanded the _Wayfarer Exchange_ trading floor into a small bragging space for connected players.\n" +
		"\n" +
		"**-** Inspecting another trader now shows that player's character name, level, and class instead of generic missing text.\n" +
		"**-** The inspection view also shows a view-only equipment snapshot, including weapon, armor, three artifact slots, both misc slots, and three ring slots.\n" +
		"**-** Equipment slots preserve item rarity, upgrades, and visible item details so friends can inspect each other's current build while trading."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.SCROLL_COLOR), "Image Kill XP",
		"Reduced XP farming potential from summoned images.\n" +
		"\n" +
		"**-** Enemies killed by _Mirror Images_ or _Prismatic Images_ now only award **20%** of their normal XP.\n" +
		"**-** The kill still counts normally for loot, progress, and other death effects, but image armies should no longer turn strong builds into runaway XP farms."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK_LRG), "Trade Persistence",
		"Fixed Wayfarer trades not always persisting cleanly after a successful exchange.\n" +
		"\n" +
		"**-** Completed trades now save the received items and resources before the trader leaves the exchange.\n" +
		"**-** Equipped items are no longer selectable in trade offers, preventing worn gear from being duplicated through the exchange.\n" +
		"**-** This prevents traded items from disappearing or reverting after returning to the homebase."));

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Exchange Exit Crash",
		"Fixed a mobile force-close that could happen shortly after leaving the Wayfarer Exchange.\n" +
		"\n" +
		"**-** Defenders now safely reappear after returning from the trading room to the homebase.\n" +
		"**-** This resolves the crash path where mobile devices could force-close after taking a few steps at the homebase."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Trading Floor Refresh",
		"Fixed Android hosts not immediately seeing traders who joined their exchange.\n" +
		"\n" +
		"**-** The trading floor now refreshes automatically instead of waiting for the host to spend a turn.\n" +
		"**-** Joined traders should appear on the host's floor without causing the turn indicator to spin endlessly."));

		changes.addButton(new ChangeButton(new Image(new RatSprite()), "Raid Wave Stutter",
		"Fixed a turn-spinner hitch that could happen when killing the final raider in a homebase raid wave.\n" +
		"\n" +
		"**-** The next wave is now prepared after the last raider fully dies.\n" +
		"**-** This should reduce the pause where the spinner appears before the final raider disappears and the next wave begins."));
	}

	public static void add_v0_1_7_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.7", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This version opened the door to two major long-term pillars: local trading and endless dungeon progression. _The Wayfarer Exchange_ started here as a direct, local-network trade between two players, keeping the fantasy personal and low-cost instead of depending on servers.\n" +
				"\n" +
				"The post-Amulet dungeon also started becoming an actual endless mode rather than a stretched ending. Heroes can keep leveling, enemies can keep rewarding XP when they scale up, and deeper floors have fewer hard stops.\n" +
				"\n" +
				"A lot of the balance work in this update was about pressure. The dungeon should keep pushing back, but it should not feel like the game is punishing you just for surviving too well.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
	
		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Wayfarer Exchange",
		"Added the _Wayfarer Exchange_, allowing two players on the same local network to trade with one another.\n" +
		"\n" +
		"**-** One player can host an exchange while another joins from the same local network.\n" +
		"**-** Each trader can offer up to three inventory items, including a chosen quantity from stackable items.\n" +
		"**-** Gold, energy, homebase building materials, and forge resources can also be included in an offer.\n" +
		"**-** Resource rows show the amount being offered alongside the player's currently available total.\n" +
		"**-** Offered items appear in visual inventory-style slots rather than text-only buttons.\n" +
		"**-** The other trader's items and resources are shown in a separate, view-only offer preview.\n" +
		"**-** Remote item slots can be selected to inspect the item's full details before accepting the trade.\n" +
		"**-** Both players must confirm their current offers before the exchange can be completed.\n" +
		"**-** There is no built-in bargaining system; negotiations are entirely up to the players! Whether you're chatting in person, over voice chat, or messaging each other, agree on a fair trade before confirming the exchange."));
	
		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "Uncapped Hero Leveling",
		"Removed the old level 30 cap from hero progression.\n" +
		"\n" +
		"**-** Heroes can now continue gaining levels after level 30.\n" +
		"**-** Post-30 level ups still increase maximum health, accuracy, and evasion.\n" +
		"**-** Talent points still follow the normal talent tiers, so extra levels do not create unusable talent points."));

		changes.addButton(new ChangeButton(new Image(new RatSprite()), "Scaled Enemy XP",
		"Adjusted enemy XP rewards for Reclaimed's endless dungeon scaling.\n" +
		"\n" +
		"**-** Enemies that grow stronger through Reclaimed levels can keep giving XP, even when they appear far beyond their original floors.\n" +
		"**-** This keeps early enemies, such as sewer monsters after floor 25, rewarding XP when they are dangerous again.\n" +
		"**-** XP from these enemies is kept modest and is based on how strong they have become."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Dungeon Pressure",
		"The _Dungeon Pressure_ mechanic has been toned down to make prolonged dungeon runs feel fairer while still encouraging players to keep moving.\n" +
		"\n" +
		"**-** The raid threat multiplier now increases more gradually, requiring twice as much threat to reach its maximum enemy respawn rate.\n" +
		"**-** Respawned enemies now appear much farther away from the hero, reducing the chance of reinforcements suddenly appearing nearby.\n" +
		"**-** Respawned enemies now have a 50% chance to begin _Sleeping_ instead of always starting in a _Wandering_ state.\n" +
		"**-** These changes should make the dungeon feel less overwhelming while preserving the tension created by increasing raid threat."));

		changes.addButton(new ChangeButton(Icons.get(Icons.SCROLL_COLOR), "Transcendant Level Ups",
		"Transcendant equipment has been rebalanced to prevent effect durations from scaling indefinitely.\n" +
		"\n" +
		"**-** Bonus effect durations gained through _Transcendant_ level ups are now capped at **+20 turns**.\n" +
		"**-** This cap only applies to the bonus duration granted by Transcendant upgrades. Base effect durations remain unchanged."));

		changes.addButton(new ChangeButton(Icons.get(Icons.STATS), "Rarity Stat Caps",
		"Added caps to several high-scaling rarity stats so late-game builds can stay powerful without growing endlessly.\n" +
		"\n" +
		"**-** _Knockback Strength_ now caps at **+10**.\n" +
		"**-** _Crimson Echo_, _Glacial Rend_, _Static Ruin_, and _Spiritbreak_ now cap at **+100%** bonus damage.\n" +
		"**-** _Fatal Synchronicity_ now caps at **+50%** bonus damage for each qualifying debuff.\n" +
		"**-** _Critical Damage Multiplier_ now caps at **+1000%**.\n" +
		"**-** Transcendant upgrade choices now respect these caps and stop offering capped stats once they are full."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
	
		changes.addButton(new ChangeButton(Icons.STAIRS.get(), "Infinite Dungeon Floors",
		"Fixed several issues that could prevent progression through the post-Amulet infinite dungeon.\n" +
		"\n" +
		"**-** Fixed the staircase from floor 26 failing to properly generate and enter floor 27.\n" +
		"**-** Infinite floors now choose matching rooms, decorations, enemies, traps, and floor features more reliably.\n" +
		"**-** Fixed some City-style endless floors generating with broken layouts.\n" +
		"**-** Fixed some Demon Halls-style endless floors having incorrect vision.\n" +
		"**-** Added extra checks so missing stairs should not block deeper descent."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_COPPER), "Infinite Floor Mob Drops",
		"Fixed endless-floor enemies failing to drop some expected loot.\n" +
		"\n" +
		"**-** Enemies with Reclaimed levels can now keep dropping their normal monster loot, even when they appear outside their original floor range.\n" +
		"**-** Material currency, catalyst drops, _Ring of Wealth_ rewards, lucky drops, and other bonus drop systems now keep rolling on infinite floors.\n" +
		"**-** Weak enemies that have not been scaled up still have limited loot, keeping low-risk farming from becoming too generous."));
		
	}
	public static void add_v0_1_6_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.6", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This update reshaped how a Reclaimed save grows beyond a single hero. Classes became something earned through the post-Amulet journey, with boss fragments, alchemy, and class call items turning unlocks into part of the world's progression instead of a menu checkbox.\n" +
				"\n" +
				"Character names were added for the same reason. A save with a name feels more like a legacy, especially when classes can be sealed, unlocked, and carried forward through settlement progress.\n" +
				"\n" +
				"The Crown, Mage's Staff, trinket cycle, and hero remains fixes all protect player investment. Reclaimed has more systems modifying items now, so upgrades and rarity history need to survive the big transformation moments.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SPIRIT_BOW), "Class Unlock Mechanic",
				"Added Reclaimed's new class-unlock progression for post-Amulet dungeon runs.\n" +
				"\n" +
				"**-** After the _Amulet of Yendor_ has been secured at the homebase, boss kills can drop class fragments.\n" +
				"**-** Before any extra class has been unlocked on that save, bosses have a 50% chance to drop a fragment for a locked class.\n" +
				"**-** After the first extra class is unlocked, that boss-fragment chance drops to 25%.\n" +
				"**-** Five matching fragments can be merged at an alchemy pot into a class call item, such as _Huntress' Call_ or _Arcanist's Oath_.\n" +
				"**-** Using that call item unlocks the matching hero class for that character save and awards the updated class unlock badge.\n" +
				"**-** Each character save can unlock up to two extra classes, encouraging each legacy to grow in its own direction.\n" +
				"**-** Fragment drops exclude the Warrior, who is unlocked by default, and also exclude classes already unlocked on that save.\n" +
				"**-** Existing saves using a currently locked class are preserved, but sealed from continuing until that class is unlocked through the new system." ));

		changes.addButton(new ChangeButton(Icons.get(Icons.NEWS), "Character Naming",
				"Added character names for Reclaimed save identities.\n" +
				"\n" +
				"**-** New character saves now ask for a character name before the run begins.\n" +
				"**-** Existing saves without a character name ask for one the next time they are continued.\n" +
				"**-** _Games in Progress_ now shows the character name first, then the class and last played time beneath it." ));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.MAGE, 1), "Defender Rescue Animation",
				"Fixed rescued defenders looking like they died when recruited.\n" +
				"\n" +
				"**-** Lost defenders now use the scroll/operate animation when they agree to return to the homebase.\n" +
				"**-** The defender is removed only after that animation finishes, making the rescue read like a return instead of a defeat." ));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.CROWN), "Dwarf King's Crown",
				"Fixed the _Dwarf King's Crown_ rerolling Reclaimed rarity data when creating class armor.\n" +
				"\n" +
				"**-** Class armor now preserves the source armor's current rarity tier and rarity stats.\n" +
				"**-** Existing upgrade level, glyph, augment, seal, curse state, and charge behavior are unchanged." ));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.MAGES_STAFF), "Mage's Staff Imbuing",
				"Improved _Mage's Staff_ imbuing when both the staff and incoming wand have rarity stats.\n" +
				"\n" +
				"**-** The imbue confirmation now shows the current staff rarity stats and the new wand rarity stats.\n" +
				"**-** Players can choose whether the finished staff keeps the staff's rarity stats or inherits the wand's rarity stats.\n" +
				"**-** The chosen rarity stats are also synchronized back onto the imbued wand so staff effects stay consistent." ));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.TRINKET_CATA), "Trinket Catalyst Cycles",
				"Fixed _Magical Catalyst_ trinket choices advancing into the next duplicate cycle too early.\n" +
				"\n" +
				"**-** A second-cycle trinket is no longer offered until every first-cycle trinket has been acquired.\n" +
				"**-** If there are too few unowned first-cycle trinkets to fill all four choices, the remaining first-cycle choices can repeat instead of pulling from cycle two." ));

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

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SEAL), "Catalog Updates",
				"Updated the item catalog for Reclaimed's class-unlock path.\n" +
				"\n" +
				"**-** The Warrior's _Seal Shard_ is no longer listed in the catalog, as Warrior remains unlocked by default.\n" +
				"**-** Class call items such as _Huntress' Call_, _Duelist's Vow_, _Arcanist's Oath_, _Shadow Pact_, and _Sacred Summons_ are now cataloged." ));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.POTION_AMBER), "Strength Potion Text",
				"Updated _Potion of Strength_ wording for Reclaimed's run-reset structure.\n" +
				"\n" +
				"**-** Its description now clarifies that the Strength increase lasts for the current dungeon run.\n" +
				"**-** This avoids implying that potion Strength survives returning to the homebase." ));
	}

	public static void add_v0_1_5_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.5", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This release focused on making rare progression clearer and less mysterious. _Ascendant Sparks_ became something players can invest in through alchemy, with visible success chances, merge levels, and better previews before spending hard-earned stones.\n" +
				"\n" +
				"Defenders also started feeling more like residents with their own progress. Their scouting can grant XP, their trade view became easier to read, and their rewards are shown in a way that feels connected to the settlement resource system.\n" +
				"\n" +
				"The guidebook cleanup matters too. Reclaimed has many new rules layered on top of Pixel Dungeon, so outdated guide entries are worse than clutter; they teach the wrong thing.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_DIAMOND), "Transcendant XP",
				"Fixed _Transcendant_ items sometimes failing to gain XP from monster kills.\n" +
				"\n" +
				"**-** Transcendant kill XP now uses the slain enemy's own XP value even when the hero is too high-level to receive normal hero XP.\n" +
				"**-** The XP pass continues to scan the full belongings list, including expanded equipment slots and items stored inside bags.\n" +
				"**-** This keeps Transcendant rings, artifacts, trinkets, and carried gear progressing consistently across Reclaimed's longer runs."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Defender Trade Layout",
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
		addDevCommentary(changes,
				"v0.1.4 was a defender behavior pass. The homebase is meant to feel lived-in and defended, so it was not enough for rescued allies to simply exist near the walls. They needed safer positioning, better ranged behavior, and fewer ways to get trapped by the settlement they are trying to protect.\n" +
				"\n" +
				"Alchemy also became a little more roguelite here. Random 1-3 output ranges give brewing more texture without turning the alchemy table into a slot machine, and the guide was updated so players can see that range before experimenting.\n" +
				"\n" +
				"This patch also kept folding Reclaimed drops into older systems, like letting the _Ring of Wealth_ notice catalysts and materials instead of pretending the new economy does not exist.");

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

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.HUNTRESS, 1), "Defender Construction Safety",
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
		addDevCommentary(changes,
				"This was one of the first big balance-and-readability releases. By this point, Reclaimed had rarity loot, homebase upgrades, defenders, raids, and endless scaling all talking to each other, so the work shifted from adding systems to making sure those systems explained themselves and did not run away.\n" +
				"\n" +
				"Upgrade previews, catalyst feedback, ranking updates, and defender inspection tabs all came from the same need: if a player invests in a build or a settlement, the game should show what changed and why it mattered.\n" +
				"\n" +
				"The nerfs in this version were not meant to make powerful builds boring. They were meant to keep dodge, block, and defensive stats from erasing danger entirely, especially once mobs and raids started scaling with the homebase.");

	changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_TIWAZ), "Scroll of Upgrade Preview",
				"Fixed a crash in the improved _Scroll of Upgrade_ preview window.\n" +
				"\n" +
				"**-** Rarity stat preview lists now attach their scroll pane before resizing it, matching the rest of the UI lifecycle.\n" +
				"**-** The preview pane now recalculates its clipping area after the upgrade window finishes sizing and centering itself.\n" +
				"**-** Preview text now stays inside the upgrade window instead of spilling outside it.\n" +
				"**-** This prevents the upgrade window from crashing when an item has visible rarity stats."));

		changes.addButton(new ChangeButton(new Image(new KingSprite()), "Dwarf King Phase Guard",
				"Fixed a possible _King of Dwarves_ softlock during his invulnerable summoning phase.\n" +
				"\n" +
				"**-** If phase two has no pending summons and no living summoned subjects left, the king now safely advances to the next shield threshold.\n" +
				"**-** This keeps the fight moving when a summon wave is exhausted unexpectedly, without skipping active summons during normal play."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_SPELLBOOK), "Unstable Spellbook",
				"Fixed exotic scroll choices for high-level _Unstable Spellbooks_.\n" +
				"\n" +
				"**-** Spellbooks at or above level 10 can still offer an exotic scroll when its normal scroll is one of the book's current requests.\n" +
				"**-** Spellbooks below level 10 still follow the original empowered scroll rules.\n" +
				"**-** This keeps high-level Spellbooks from hiding valid exotic choices such as the exotic versions of _Remove Curse_ and _Magic Mapping_."));

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

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Defender Inspect Tabs",
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
				"**-** Capped Transcendant options now keep both previewed and applied gains within 100% for chance, effect, and resistance stats."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_TIWAZ), "Upgrade Preview",
				"Improved _Scroll of Upgrade_ previews for rarity-driven gear.\n" +
				"\n" +
				"**-** Upgrade windows now show an item's visible rarity stats alongside its normal upgrade preview.\n" +
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
				"Updated rankings so they better represent a long-running Reclaimed legacy instead of only one expedition.\n" +
				"\n" +
				"**-** Ranking records now track lifetime dungeon runs, total floors descended, total floors ascended, deepest floor reached, and total hero XP across expeditions.\n" +
				"**-** Ranking strength now includes restored homebase training bonuses when viewing a saved record.\n" +
				"**-** Score breakdowns now include a _Settlement_ category for homebase levels, permanent training, structure defenses, defenders, settlement requests, and raids survived.\n" +
				"**-** The ranking inventory tab now scrolls so expanded equipment slots and longer carried equipment lists can fit cleanly."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_STONE), "Material Drop Balance",
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
				"**-** High-impact effect chances, resistances, and specialized offensive effects now unlock later so scaling enemies have room to push back.\n" +
				"**-** Homebase buildings no longer have a fixed max level, and facility screens now show _Building Level: X_ instead of a capped level fraction."));

		changes.addButton(new ChangeButton(new Image(new RatSprite()), "Mob Build Scaling",
				"Reworked mob stat scaling so enemies grow into recognizable builds instead of carrying a huge flat list of low-impact stats.\n" +
				"\n" +
				"**-** Mob levels are no longer capped at 100.\n" +
				"**-** Every mob level now keeps adding baseline health, damage, and attack pressure.\n" +
				"**-** Armor, movement speed, and attack speed remain high-chance combat rolls so leveled mobs feel sturdier and more aggressive without every stat being guaranteed.\n" +
				"**-** Mob rarity-stat rolls now prefer focused stat pools, then stack duplicate rolls into stats the mob already has, similar to Transcendant item growth.\n" +
				"**-** Enemy chance-based stats and resistances can exceed 100%, matching how players can stack several Transcendant items."));

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

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.ROGUE, 1), "Defender Scouting Gifts",
				"Improved defender scouting rewards after expeditions.\n" +
				"\n" +
				"**-** Defenders who return from their off-screen dungeon runs can now present their donated materials in a dedicated popup.\n" +
				"**-** The popup shows the defender's sprite, rarity aura, name, and color-coded donated resources.\n" +
				"**-** Defenders now keep part of what they gather as their own personal currency instead of donating everything to the settlement.\n" +
				"**-** This makes defender scouting rewards easier to notice and gives each defender a small personal economy."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(CharSprite.NEGATIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ROUND_SHIELD), "Defensive Stat Nerfs",
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

		changes.addButton(new ChangeButton(Icons.STAIRS.get(), "Endless Reclaimed Depths",
				"Added the first post-Amulet endless dungeon loop.\n" +
				"\n" +
				"**-** Once the Amulet is recovered and brought safely back to the homebase, future expeditions no longer end at the old final floor.\n" +
				"**-** Floors 1-25 still follow the classic sewer-to-halls progression, including the normal boss floors.\n" +
				"**-** Floor 26 and deeper now continue forever with random dungeon regions instead of spawning another Amulet floor.\n" +
				"**-** Random boss floors appear every fifth floor after the original Halls boss, starting at floor 30.\n" +
				"**-** Merchant rooms appear inside normal dungeon floors after boss floors, starting at floor 26, giving each endless segment a recovery and spending point.\n" +
				"**-** A mine-style special region can now appear as part of the endless floor pool."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), "Defender Trading",
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
		addDevCommentary(changes,
				"This was a presentation and stability pass for the homebase screens. As the buildings gained more jobs, costs, defensive stats, and training options, the old single-column text walls stopped being comfortable to read.\n" +
				"\n" +
				"Facility dividers and defender list dividers were added so each screen feels more like a place with sections instead of a pile of buttons. It is a small visual change, but it matters when the homebase becomes something players visit constantly.\n" +
				"\n" +
				"The artifact recharge fixes also protected one of Reclaimed's bigger promises: uncapped artifact growth should be exciting, not a way to accidentally break your favorite tool.");

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "Facility Screen Dividers",
				"Improved visual separation in homebase facility screens.\n" +
				"\n" +
				"**-** Facility tabs now use stronger horizontal dividers between obvious sections.\n" +
				"**-** Divider styling now matches the clearer section language used by the talent and changes screens.\n" +
				"**-** Upgrade, training, storage, forge, garden, and camp content should read less like one long wall of text."));

		changes.addButton(new ChangeButton(Icons.get(Icons.MAGNIFY), "Defender Screen Dividers",
				"Improved readability in _Founder's Camp_ defender management.\n" +
				"\n" +
				"**-** Defender list entries now have visible dividers between each defender.\n" +
				"**-** The management screen should be easier to scan when several defenders have gear, stats, and supplies.\n" +
				"**-** This is a visual-only pass and does not change defender behavior."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed high-level artifact recharge issues caused by uncapped artifact levels.\n" +

				"**-** Fixed _Skeleton Key_ recharge math at high levels, where its missing-charge formula could become negative and stop visible recharge progress.\n" +
				"**-** Existing saves now repair broken artifact charge values so previously affected artifacts can recover.\n" +
				"**-** Artifact charge limits now refresh after loading, upgrading, rarity stat changes, and homebase potency changes.\n" +
				"**-** Similar recharge formulas on _Unstable Spellbook_, _Timekeeper's Hourglass_, _Cloak of Shadows_, _Holy Tome_, and_ Ethereal Chains_ now clamp to safe minimum recharge times.\n\n" +
				"Fixed screen layout issues on desktop and mobile.\n" +
				"**-** Homebase facility screens now use a more consistent layout when _forced landscape_ mode is enabled on mobile devices."
			));
	}

	public static void add_v0_1_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This update was the first broad tuning pass after the initial Reclaimed systems came together. Jackpot cache rooms were added because a roguelite dungeon should sometimes surprise you with a real windfall, especially when the homebase economy asks for long-term investment.\n" +
				"\n" +
				"Material display, contract pacing, and mob scaling were adjusted to make the loop clearer: explore, recover, rebuild, grow stronger, and then face a dungeon that notices your progress.\n" +
				"\n" +
				"The resistance fixes were especially important. If a build reaches full resistance, the game should respect that investment and clearly say when an effect was blocked.");

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
				"**-** Per-item _Transcendant_ resistance, effect, and chance stats now cap at 100%.\n" +
				"**-** Duplicate capped stat rolls now add together only up to the cap instead of going past it.\n" +
				"**-** Transcendant upgrade choices stop offering chance, effect, and resistance stats that are already full."));

		changes.addButton(new ChangeButton(Icons.get(Icons.CALENDAR), "Founder's Camp Contracts",
				"Adjusted contract slot pacing so the Founder's Camp feels useful earlier.\n" +
				"\n" +
				"**-** The camp now gains contract capacity much more often as it is upgraded.\n" +
				"**-** Higher camp levels can support a much larger board of active settlement work.\n" +
				"**-** Contract growth now better matches long-term building progression."));

		changes.addButton(new ChangeButton(new Image(new RatSprite()), "Mob Level Scaling",
				"Updated enemy mob level scaling so the dungeon reacts to permanent settlement growth.\n" +
				"\n" +
				"**-** New enemies now grow a little stronger as homebase buildings improve.\n" +
				"**-** Permanent training also adds to enemy growth.\n" +
				"**-** Transcendant item levels now make enemies scale faster than normal upgrades.\n" +
				"**-** This enemy growth is separate from raid threat."));
			
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

		ChangeInfo changes = new ChangeInfo("Initial Release\nv0.1.0", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);
		addDevCommentary(changes,
				"This is where Reclaimed Pixel Dungeon began as its own playable fork. The heart of the release was a new roguelite rhythm: descend into the dungeon, bring back what you can, rebuild a ruined homebase, and let each expedition leave a mark on the next one.\n" +
				"\n" +
				"Floor 0, recovered materials, permanent training, and facility screens were added to make the world feel like something you are restoring rather than simply passing through. The homebase is not just a menu; it is the place the dungeon keeps throwing you back to, and the place you slowly reclaim.\n" +
				"\n" +
				"Rarity loot, catalyst runestones, expanded equipment, defenders, raids, and mob levels gave the first release its roguelite identity. The goal was to keep Pixel Dungeon recognizable while giving long-term saves new pressure, new rewards, and new stories.");

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
				"**-** If item types are already known, it searches the hero's inventory and bags for unidentified items."));

		changes = new ChangeInfo("Rarity Loot", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_PRISMFORGE), "Item Rarities",
				"Added item rarities from _Common_ through _Transcendant_ for most long-term equipment.\n" +
				"\n" +
				"**-** Gear, weapons, rings, artifacts, trinkets, wands, throwables, the Spirit Bow, and the Mage Staff can roll rarity stats.\n" +
				"**-** Rarity names, stat lines, lore, prices, and inventory aura glows now reflect item rarity.\n" +
				"**-** Rarity effects now affect real combat and utility behavior instead of being only display text."));

		changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "Rarity Stats",
				"Added a broad rarity stat system inspired by RarityForge and Shattered Pixel Dungeon's existing effects.\n" +
				"\n" +
				"**-** Added offensive stats, defensive stats, effect chances, crit, lifesteal, XP gain, resource bonuses, and loot bonuses.\n" +
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

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.BUILDING_WOOD), "Recovered Materials",
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
				"**-** Custom bags have distinct textures and appear in shops after the original bags are purchased.\n" +
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

		changes.addButton(new ChangeButton(new Image(new AlbinoSprite()), "Mob Levels and Stats",
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
				"**-** Raider groups can include dungeon enemies seeking revenge.\n" +
				"**-** Structures have durability, armor, resistance upgrades, repair costs, destroyed states, and raid restrictions."));

		changes = new ChangeInfo("Defenders", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 1), "Recruitable Defenders",
				"Added _homebase defenders_ who can be rescued and brought back to the settlement.\n" +
				"\n" +
				"**-** Defenders can appear in rare secret and locked rooms.\n" +
				"**-** They roll names, classes, rarity, stats, XP, and matching hero-class appearances.\n" +
				"**-** Defenders can patrol, idle, sleep to recover, scout for materials, and hunt raiders during raids."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER), "Defender Gear",
				"Added equipment management for defenders.\n" +
				"\n" +
				"**-** Defenders can equip weapons and armor.\n" +
				"**-** Strength requirements apply to their gear.\n" +
				"**-** Defender gear can use rarity stats.\n" +
				"**-** Armor can change a defender's appearance, and ranged or reach weapons affect how they fight."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.POTION_CRIMSON), "Defender Gifts",
				"Added the foundation for giving defenders useful supplies.\n" +
				"\n" +
				"**-** Defenders are planned around gifts rather than natural strength gain.\n" +
				"**-** Strength potions, healing potions, scrolls of upgrade, and ankhs can support defender growth and survival."));

		changes.addButton(new ChangeButton(BadgeBanner.image( Badges.Badge.HIGH_SCORE_2.image ), "Defender Progression",
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
