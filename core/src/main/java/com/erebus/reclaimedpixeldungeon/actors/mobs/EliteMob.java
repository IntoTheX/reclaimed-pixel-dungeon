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
 */

package com.erebus.reclaimedpixeldungeon.actors.mobs;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Blob;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ConfusionGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.CorrosiveGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Fire;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Freezing;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ParalyticGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.SmokeScreen;
import com.erebus.reclaimedpixeldungeon.actors.blobs.StenchGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.ToxicGas;
import com.erebus.reclaimedpixeldungeon.actors.blobs.Web;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Barrier;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Blindness;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Bless;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Burning;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Chill;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Corrosion;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Cripple;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Drowsy;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Haste;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Invisibility;
import com.erebus.reclaimedpixeldungeon.actors.buffs.FlavourBuff;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Paralysis;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Poison;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Roots;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Terror;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vertigo;
import com.erebus.reclaimedpixeldungeon.actors.buffs.ChampionEnemy;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.erebus.reclaimedpixeldungeon.effects.CellEmitter;
import com.erebus.reclaimedpixeldungeon.effects.EliteSkillIcon;
import com.erebus.reclaimedpixeldungeon.effects.FloatingText;
import com.erebus.reclaimedpixeldungeon.effects.MagicMissile;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.effects.particles.LeafParticle;
import com.erebus.reclaimedpixeldungeon.items.Heap;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bombs.ArcaneBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Bomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Firebomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.FlashBangBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.FrostBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.HolyBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.Noisemaker;
import com.erebus.reclaimedpixeldungeon.items.bombs.ShrapnelBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.SmokeBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.WoollyBomb;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfFrost;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHaste;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfHealing;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfInvisibility;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfMindVision;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfParalyticGas;
import com.erebus.reclaimedpixeldungeon.items.potions.PotionOfToxicGas;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.PotionOfCorrosiveGas;
import com.erebus.reclaimedpixeldungeon.items.potions.exotic.PotionOfShielding;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTerror;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.sprites.MissileSprite;
import com.erebus.reclaimedpixeldungeon.tiles.DungeonTilemap;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.watabou.utils.PointF;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.function.Consumer;

/** Persistent rarity, bonuses, and skills for an elite enemy. */
public class EliteMob implements Bundlable {

	/** Returns the position of the owner's most immediate armed-bomb threat. */
	public int ownBombThreat(Mob owner) {
		if (owner == null || Dungeon.level == null || Dungeon.level.heaps == null) return -1;

		int threatPos = -1;
		int greatestDanger = Integer.MIN_VALUE;
		for (Heap heap : Dungeon.level.heaps.valueList()) {
			if (heap == null || heap.items == null) continue;
			for (Item item : heap.items) {
				if (!(item instanceof Bomb)) continue;
				Bomb bomb = (Bomb)item;
				if (!bomb.isArmed() || !bomb.wasPlacedBy(owner)) continue;

				int distance = Dungeon.level.distance(owner.pos, heap.pos);
				if (distance <= bomb.dangerRange()) {
					int danger = bomb.dangerRange() - distance;
					if (danger > greatestDanger) {
						greatestDanger = danger;
						threatPos = heap.pos;
					}
				}
			}
		}
		return threatPos;
	}

	public enum Rank {
		COMMON(ItemRarity.COMMON, 20, 5, 10, 20, 35, 1, 15, 0, 25),
		UNCOMMON(ItemRarity.UNCOMMON, 35, 10, 18, 35, 60, 2, 30, 0, 50),
		RARE(ItemRarity.RARE, 55, 18, 30, 60, 100, 3, 50, 1, 0),
		EPIC(ItemRarity.EPIC, 80, 30, 45, 100, 175, 4, 80, 1, 50),
		LEGENDARY(ItemRarity.LEGENDARY, 110, 45, 60, 175, 300, 5, 120, 2, 0),
		TRANSCENDANT(ItemRarity.TRANSCENDANT, 150, 60, 80, 300, 500, 4, 200, 3, 0);

		public final ItemRarity rarity;
		final int statBoost;
		final int minReduction;
		final int maxReduction;
		final int minHealth;
		final int maxHealth;
		final int skillCount;
		final int xpBonus;
		final int guaranteedLootRolls;
		final int additionalLootChance;

		Rank(ItemRarity rarity, int statBoost, int minReduction, int maxReduction,
			 int minHealth, int maxHealth, int skillCount, int xpBonus,
			 int guaranteedLootRolls, int additionalLootChance) {
			this.rarity = rarity;
			this.statBoost = statBoost;
			this.minReduction = minReduction;
			this.maxReduction = maxReduction;
			this.minHealth = minHealth;
			this.maxHealth = maxHealth;
			this.skillCount = skillCount;
			this.xpBonus = xpBonus;
			this.guaranteedLootRolls = guaranteedLootRolls;
			this.additionalLootChance = additionalLootChance;
		}
	}

	public enum Skill {
		BLOODRUSH(Rank.COMMON, "Bloodrush", "Below half health, this elite attacks 25% faster."),
		KEEN_HUNTER(Rank.COMMON, "Keen Hunter", "Gains 25% attack accuracy and notices prey more reliably."),
		THICK_HIDE(Rank.COMMON, "Thick Hide", "Gains additional armor based on its enemy level."),
		RELENTLESS_PURSUIT(Rank.COMMON, "Relentless Pursuit", "Moves 25% faster while actively hunting a target."),
		VAMPIRIC_EDGE(Rank.COMMON, "Vampiric Edge", "Melee attacks restore 10% of the damage they deal."),
		RETALIATORY_SPINES(Rank.COMMON, "Retaliatory Spines", "Reflects 10% of incoming direct attack damage."),
		STEADFAST(Rank.COMMON, "Steadfast", "Greatly resists roots, crippling, vertigo, and forced movement."),
		BATTLE_TRANCE(Rank.COMMON, "Battle Trance", "Periodically enters a short blessed and hasted combat trance."),

		PESTILENT_SHROUD(Rank.UNCOMMON, "Pestilent Shroud", "Releases a toxic aura around nearby enemies."),
		DELIRIUM_VEIL(Rank.UNCOMMON, "Delirium Veil", "Distorts the senses of nearby enemies with vertigo and blindness."),
		FETID_DOMINION(Rank.UNCOMMON, "Fetid Dominion", "Its choking presence can briefly paralyze nearby enemies."),
		MIREBLOOD_AURA(Rank.UNCOMMON, "Mireblood Aura", "Poisons and cripples enemies caught in its mire."),
		BRIAR_DOMAIN(Rank.UNCOMMON, "Briar Domain", "Blankets a radius of 2 tiles in grasping growth for one turn, rooting enemies caught within it."),
		ASHEN_SHROUD(Rank.UNCOMMON, "Ashen Shroud", "Blankets nearby enemies in blinding smoke."),
		WARBREW_RUSH(Rank.UNCOMMON, "Warbrew Rush", "Drinks a spectral warbrew for a burst of haste."),
		HUNTERS_REVELATION(Rank.UNCOMMON, "Hunter's Revelation", "Reveals hidden prey and gains accuracy for several turns."),

		NERVEFOG(Rank.RARE, "Nervefog", "Discharges a paralytic cloud around its target."),
		CAUSTIC_MIASMA(Rank.RARE, "Caustic Miasma", "Bathes nearby enemies in armor-eating corrosion."),
		CINDERWAKE(Rank.RARE, "Cinderwake", "Ignites enemies caught close to it."),
		WINTERS_GRASP(Rank.RARE, "Winter's Grasp", "Severely chills nearby enemies."),
		SILKEN_EXPANSE(Rank.RARE, "Silken Expanse", "Roots and cripples enemies in spectral webbing."),
		RAINCALLERS_GROUND(Rank.RARE, "Raincaller's Ground", "Calls a charged storm that chills and shocks its target."),
		VEILSTEP(Rank.RARE, "Veilstep", "Turns invisible to reposition, then breaks concealment when it attacks."),
		CRIMSON_RENEWAL(Rank.RARE, "Crimson Renewal", "Restores health when badly wounded."),
		DREAD_EDICT(Rank.RARE, "Dread Edict", "Commands its target to flee in terror."),
		VENOMWHEEL_VOLLEY(Rank.RARE, "Venomwheel Volley", "Opens at range with a volley that poisons its target."),
		EARTHEN_COVENANT(Rank.RARE, "Earthen Covenant", "Hardens its body with a temporary barrier."),

		STORM_CAGE(Rank.EPIC, "Storm Cage", "Traps its target in lightning and brief paralysis."),
		RIFT_ASSAULT(Rank.EPIC, "Rift Assault", "Teleports beside a distant target and immediately strikes."),
		RIFT_WITHDRAWAL(Rank.EPIC, "Rift Withdrawal", "Teleports away when critically wounded."),
		DROWSING_HYMN(Rank.EPIC, "Drowsing Hymn", "Lulls nearby enemies toward magical sleep."),
		POWDER_RAIN(Rank.EPIC, "Powder Rain", "Throws a randomly selected armed bomb whose normal fuse and effects occur after it lands."),
		ECHO_LEGION(Rank.EPIC, "Echo Legion", "Spectral echoes inherit part of its combat power and 50% of its rarity stats without granting rewards."),
		PRISMATIC_WARD(Rank.EPIC, "Prismatic Ward", "Raises a barrier worth 15% of its maximum health."),

		WRIT_OF_RETRIBUTION(Rank.LEGENDARY, "Writ of Retribution", "Punishes its target according to the elite's missing health."),
		NULL_MANTLE(Rank.LEGENDARY, "Null Mantle", "Halves magical and harmful status effects used against it.");

		final Rank minimumRank;
		final String title;
		final String description;

		Skill(Rank minimumRank, String title, String description) {
			this.minimumRank = minimumRank;
			this.title = title;
			this.description = description;
		}
	}

	private Rank rank;
	private int damageReduction;
	private int healthBoost;
	private final ArrayList<Skill> skills = new ArrayList<>();
	private int[] skillLevels = new int[0];
	private int[] cooldowns = new int[0];
	private int transcendantLevel = 1;
	private int transcendantExperience;
	private int consecutiveSkillEnhancements;
	private int globalCooldown;
	private int survivalCooldown;
	private int retreatTurns;
	private Bomb lastPowderRainBomb;
	private int cloudPulse;
	private boolean diagonalCloudPulse;
	private boolean bloodrushAnnounced;
	private boolean keenHunterAnnounced;
	private boolean deliriumVeilAnnounced;

	private static final String RANK = "rank";
	private static final String DAMAGE_REDUCTION = "damage_reduction";
	private static final String HEALTH_BOOST = "health_boost";
	private static final String SKILLS = "skills";
	private static final String COOLDOWNS = "cooldowns";
	private static final String SKILL_LEVELS = "skill_levels";
	private static final String TRANSCENDANT_LEVEL = "transcendant_level";
	private static final String TRANSCENDANT_EXPERIENCE = "transcendant_experience";
	private static final String CONSECUTIVE_SKILL_ENHANCEMENTS = "consecutive_skill_enhancements";
	private static final String GLOBAL_COOLDOWN = "global_cooldown";
	private static final String SURVIVAL_COOLDOWN = "survival_cooldown";
	private static final String RETREAT_TURNS = "retreat_turns";
	private static final String CLOUD_PULSE = "cloud_pulse";
	private static final String DIAGONAL_CLOUD_PULSE = "diagonal_cloud_pulse";

	public EliteMob() {
	}

	private EliteMob(Rank rank) {
		this.rank = rank;
		damageReduction = Random.IntRange(rank.minReduction, rank.maxReduction);
		healthBoost = Random.IntRange(rank.minHealth, rank.maxHealth);
		diagonalCloudPulse = Random.Int(2) == 0;
		rollSkills();
	}

	public static EliteMob roll(Mob mob) {
		if (!eligible(mob) || Random.Float() >= eliteChance(Dungeon.depth)) return null;
		return new EliteMob(rollRank(Dungeon.depth));
	}

	public static EliteMob force(Mob mob, Rank rank) {
		if (mob == null || rank == null) return null;
		return new EliteMob(rank);
	}

	public static boolean eligible(Mob mob) {
		if (mob == null || mob.alignment != Char.Alignment.ENEMY || mob.EXP <= 0) return false;
		if (Char.hasProp(mob, Char.Property.BOSS)
				|| Char.hasProp(mob, Char.Property.MINIBOSS)
				|| Char.hasProp(mob, Char.Property.BOSS_MINION)) return false;
		if (!mob.buffs(ChampionEnemy.class).isEmpty()) return false;
		String name = mob.getClass().getName();
		return !name.contains(".npcs.")
				&& !name.endsWith("Swarm")
				&& !name.contains("MirrorImage")
				&& !name.contains("PrismaticImage");
	}

	public static float eliteChance(int floor) {
		return Math.min(0.33f, 0.01f + Math.max(0, floor - 1) * 0.0032f);
	}

	private static Rank rollRank(int floor) {
		int[][] checkpoints = {
				{1, 95, 5, 0, 0, 0, 0},
				{10, 75, 22, 3, 0, 0, 0},
				{25, 45, 35, 15, 4, 1, 0},
				{50, 20, 30, 28, 16, 5, 1},
				{75, 5, 20, 30, 28, 13, 4},
				{100, 0, 10, 25, 32, 25, 8},
				{150, 0, 0, 10, 30, 40, 20}
		};
		int[] weights = new int[Rank.values().length];
		int lower = 0;
		while (lower + 1 < checkpoints.length && floor > checkpoints[lower + 1][0]) lower++;
		if (lower == checkpoints.length - 1) {
			System.arraycopy(checkpoints[lower], 1, weights, 0, weights.length);
		} else {
			int[] a = checkpoints[lower];
			int[] b = checkpoints[lower + 1];
			float t = Math.max(0f, Math.min(1f, (floor - a[0]) / (float)(b[0] - a[0])));
			for (int i = 0; i < weights.length; i++) {
				weights[i] = Math.round(a[i + 1] + (b[i + 1] - a[i + 1]) * t);
			}
		}
		int total = 0;
		for (int weight : weights) total += weight;
		int roll = Random.Int(Math.max(1, total));
		for (int i = 0; i < weights.length; i++) {
			roll -= weights[i];
			if (roll < 0) return Rank.values()[i];
		}
		return Rank.COMMON;
	}

	private void rollSkills() {
		ArrayList<Skill> pool = new ArrayList<>();
		for (Skill skill : Skill.values()) if (skill.minimumRank.ordinal() <= rank.ordinal()) pool.add(skill);
		for (int i = pool.size() - 1; i > 0; i--) {
			int swap = Random.Int(i + 1);
			Skill skill = pool.get(i);
			pool.set(i, pool.get(swap));
			pool.set(swap, skill);
		}
		boolean hardControl = false;
		boolean teleport = false;
		boolean summon = false;
		for (Skill skill : pool) {
			boolean isHardControl = skill == Skill.NERVEFOG || skill == Skill.STORM_CAGE || skill == Skill.DROWSING_HYMN;
			boolean isTeleport = skill == Skill.RIFT_ASSAULT || skill == Skill.RIFT_WITHDRAWAL;
			boolean isSummon = skill == Skill.ECHO_LEGION;
			if ((isHardControl && hardControl) || (isTeleport && teleport && rank != Rank.LEGENDARY && rank != Rank.TRANSCENDANT) || (isSummon && summon)) continue;
			skills.add(skill);
			hardControl |= isHardControl;
			teleport |= isTeleport;
			summon |= isSummon;
			if (skills.size() >= rank.skillCount) break;
		}
		cooldowns = new int[skills.size()];
		skillLevels = new int[skills.size()];
		for (int i = 0; i < skillLevels.length; i++) skillLevels[i] = 1;
		for (int i = 0; i < cooldowns.length; i++) cooldowns[i] = Random.IntRange(3, 6);
	}

	public boolean isTranscendant() {
		return rank == Rank.TRANSCENDANT;
	}

	public int transcendantLevel() {
		return isTranscendant() ? transcendantLevel : 0;
	}

	public int transcendantExperience() {
		return isTranscendant() ? transcendantExperience : 0;
	}

	public int transcendantExperienceToNext() {
		return 10 + Math.max(0, transcendantLevel - 1) * 5;
	}

	public void initializeTranscendant(Mob mob) {
		if (!isTranscendant() || mob == null || mob.mobStats == null) return;
		mob.mobStats.ensureStat(RarityStat.Type.XP_GAIN, Random.IntRange(5, 10));
	}

	/** Transcendant elites hunt living dungeon creatures, but never their own echoes. */
	public Char choosePrey(Mob hunter, Char current) {
		if (!isTranscendant() || hunter == null || Dungeon.level == null) return current;
		Char best = validPrey(hunter, current) ? current : null;
		int bestDistance = best == null ? Integer.MAX_VALUE : Dungeon.level.distance(hunter.pos, best.pos);
		for (Mob candidate : Dungeon.level.mobs) {
			if (!validPrey(hunter, candidate)) continue;
			int distance = Dungeon.level.distance(hunter.pos, candidate.pos);
			if (best == null || distance < bestDistance
					|| (distance == bestDistance && candidate.HP * 100 / Math.max(1, candidate.HT) < best.HP * 100 / Math.max(1, best.HT))) {
				best = candidate;
				bestDistance = distance;
			}
		}
		if (validPrey(hunter, Dungeon.hero)) {
			int distance = Dungeon.level.distance(hunter.pos, Dungeon.hero.pos);
			if (best == null || distance <= bestDistance) best = Dungeon.hero;
		}
		return best;
	}

	private boolean validPrey(Mob hunter, Char candidate) {
		if (candidate == null || candidate == hunter || !candidate.isAlive() || candidate.invisible > 0) return false;
		if (candidate == Dungeon.hero && HomebaseState.playerInvisibleUntargetableEnabled()) return false;
		if (candidate.pos < 0 || candidate.pos >= hunter.fieldOfView.length || !hunter.fieldOfView[candidate.pos]) return false;
		if (candidate instanceof EliteEcho && ((EliteEcho)candidate).isEchoOf(hunter)) return false;
		if (candidate instanceof Mob) {
			Mob mob = (Mob)candidate;
			if (mob.EXP <= 0 || mob.state == mob.PASSIVE) return false;
			if (Char.hasProp(mob, Char.Property.BOSS) || Char.hasProp(mob, Char.Property.MINIBOSS)
					|| Char.hasProp(mob, Char.Property.BOSS_MINION)) return false;
			if (mob.getClass().getName().contains(".npcs.")) return false;
		}
		return true;
	}

	public void gainExperience(Mob hunter, Mob victim) {
		if (!isTranscendant() || hunter == null || victim == null || victim == hunter || victim.EXP <= 0) return;
		if (victim instanceof EliteEcho && ((EliteEcho)victim).isEchoOf(hunter)) return;
		int victimLevel = victim.mobStats == null ? 1 : victim.mobStats.level();
		int base = Math.max(1, victim.EXP + victimLevel / 3);
		int gain = Math.max(1, Math.round(base * (1f + hunter.rarityStat(RarityStat.Type.XP_GAIN) / 100f)));
		gain = Math.min(gain, Math.max(1, transcendantExperienceToNext() / 4));
		transcendantExperience += gain;
		while (transcendantExperience >= transcendantExperienceToNext()) {
			transcendantExperience -= transcendantExperienceToNext();
			levelUp(hunter);
		}
		if (hunter.sprite != null) hunter.sprite.showStatusWithIcon(CharSprite.POSITIVE, "+" + gain, com.erebus.reclaimedpixeldungeon.effects.FloatingText.EXPERIENCE);
	}

	private void levelUp(Mob mob) {
		transcendantLevel++;
		if (mob.mobStats != null) {
			int oldHealth = mob.mobStats.health();
			mob.mobStats.improveExistingOnly(2);
			int healthGain = Math.max(0, mob.mobStats.health() - oldHealth);
			if (healthGain > 0) {
				int boosted = Math.max(1, Math.round(healthGain * (1f + healthBoost / 100f)));
				mob.HT += boosted;
				mob.HP = Math.min(mob.HT, mob.HP + boosted);
			}
		}
		if (transcendantLevel % 10 == 0) improveSkillMilestone();
		if (mob.sprite != null) {
			mob.sprite.showStatus(rank.rarity.color(), "Transcendant Lv. " + transcendantLevel);
			mob.sprite.centerEmitter().burst(Speck.factory(Speck.STAR), 14);
		}
	}

	private void improveSkillMilestone() {
		ArrayList<Skill> learnable = new ArrayList<>();
		for (Skill skill : Skill.values()) if (!skills.contains(skill)) learnable.add(skill);
		ArrayList<Integer> improvable = new ArrayList<>();
		for (int i = 0; i < skills.size(); i++) if (skillLevels[i] < maxSkillLevel(skills.get(i))) improvable.add(i);

		boolean learn = !learnable.isEmpty() && (improvable.isEmpty() || consecutiveSkillEnhancements >= 2 || Random.Int(100) < 55);
		if (learn) {
			Skill skill = learnable.get(Random.Int(learnable.size()));
			skills.add(skill);
			skillLevels = append(skillLevels, 1);
			cooldowns = append(cooldowns, Random.IntRange(3, 6));
			consecutiveSkillEnhancements = 0;
		} else if (!improvable.isEmpty()) {
			int index = improvable.get(Random.Int(improvable.size()));
			skillLevels[index]++;
			consecutiveSkillEnhancements++;
		}
	}

	private static int[] append(int[] values, int value) {
		int[] result = new int[values.length + 1];
		System.arraycopy(values, 0, result, 0, values.length);
		result[values.length] = value;
		return result;
	}

	private int maxSkillLevel(Skill skill) {
		switch (skill.minimumRank) {
			case COMMON: return 6;
			case UNCOMMON: return 5;
			case RARE: return 4;
			case EPIC: return 3;
			default: return 2;
		}
	}

	private int skillLevel(Skill skill) {
		int index = skills.indexOf(skill);
		return index < 0 || index >= skillLevels.length ? 0 : Math.max(1, skillLevels[index]);
	}

	public Rank rank() {
		return rank;
	}

	public String coloredName(String mobName) {
		return rank.rarity.coloredName() + " " + mobName;
	}

	public int applyHealth(int health) {
		return Math.max(1, Math.round(health * (1f + healthBoost / 100f)));
	}

	public float amplify(float value) {
		return value * (1f + rank.statBoost / 100f);
	}

	public float applyAttackDelay(Mob mob, float delay) {
		delay /= 1f + rank.statBoost / 100f;
		if (has(Skill.BLOODRUSH) && mob.HP * 2 <= mob.HT) delay *= Math.max(0.5f, 0.75f - 0.05f * (skillLevel(Skill.BLOODRUSH) - 1));
		return Math.max(0.1f, delay);
	}

	public float applySpeed(Mob mob, float speed) {
		speed = amplify(speed);
		if (has(Skill.RELENTLESS_PURSUIT) && mob.state == mob.HUNTING) speed *= 1.25f + 0.05f * (skillLevel(Skill.RELENTLESS_PURSUIT) - 1);
		return speed;
	}

	public float applyAccuracy(float accuracy) {
		accuracy = amplify(accuracy);
		if (has(Skill.KEEN_HUNTER)) accuracy *= 1.25f + 0.05f * (skillLevel(Skill.KEEN_HUNTER) - 1);
		return accuracy;
	}

	public int applyArmor(Mob mob, int armor) {
		armor = Math.round(amplify(armor));
		if (has(Skill.THICK_HIDE)) armor += Math.max(1, (mob.mobStats == null ? 1 : mob.mobStats.level() / 3) * skillLevel(Skill.THICK_HIDE));
		return armor;
	}

	public int reduceDamage(int damage) {
		return Math.max(0, Math.round(damage * (1f - damageReduction / 100f)));
	}

	public int attackProc(Mob mob, Char enemy, int damage) {
		if (has(Skill.VAMPIRIC_EDGE) && damage > 0 && mob.HP < mob.HT) {
			int healing = Math.max(1, Math.round(damage * Math.min(0.30f, 0.10f + 0.04f * (skillLevel(Skill.VAMPIRIC_EDGE) - 1))));
			mob.HP = Math.min(mob.HT, mob.HP + healing);
		}
		return damage;
	}

	public void defenseProc(Mob mob, Char attacker, int damage) {
		if (has(Skill.RETALIATORY_SPINES) && attacker != null && attacker.isAlive() && damage > 0) {
			attacker.damage(Math.max(1, Math.round(damage * Math.min(0.30f, 0.10f + 0.04f * (skillLevel(Skill.RETALIATORY_SPINES) - 1)))), mob);
		}
	}

	public float resistanceMultiplier(Class effect) {
		float multiplier = has(Skill.NULL_MANTLE) ? 0.5f : 1f;
		if (has(Skill.STEADFAST) && (effect == Roots.class || effect == Cripple.class || effect == Vertigo.class)) multiplier *= 0.25f;
		return multiplier;
	}

	public void tick(Mob mob) {
		boolean bloodrushActive = has(Skill.BLOODRUSH) && mob.HP * 2 <= mob.HT;
		if (bloodrushActive && !bloodrushAnnounced) {
			announceSkill(Skill.BLOODRUSH, mob.pos);
			bloodrushAnnounced = true;
		} else if (!bloodrushActive) {
			bloodrushAnnounced = false;
		}
		if (!visibleToHero(mob.pos)) deliriumVeilAnnounced = false;

		if (globalCooldown > 0) globalCooldown--;
		if (survivalCooldown > 0) survivalCooldown--;
		if (retreatTurns > 0 && --retreatTurns == 0) mob.endEliteRetreat();
		for (int i = 0; i < cooldowns.length; i++) if (cooldowns[i] > 0) cooldowns[i]--;
		if (cloudPulse <= 0) {
			emitPassiveClouds(mob);
			cloudPulse = 3;
		} else {
			cloudPulse--;
		}
	}

	public boolean tryUseSkill(Mob mob, Char enemy, boolean enemyInFOV) {
		if (!enemyInFOV || enemy == null) {
			keenHunterAnnounced = false;
			return false;
		}
		if (has(Skill.KEEN_HUNTER) && !keenHunterAnnounced) {
			announceSkill(Skill.KEEN_HUNTER, mob.pos);
			keenHunterAnnounced = true;
		}
		if (rank.ordinal() >= Rank.EPIC.ordinal()
				&& survivalCooldown <= 0
				&& mob.HP * 100 <= mob.HT * 35) {
			mob.beginEliteRetreat(enemy);
			retreatTurns = 6;
			survivalCooldown = 18;
			announceSurvivalInstinct(mob.pos);
		}
		if (globalCooldown > 0) return false;
		for (int i = 0; i < skills.size(); i++) {
			if (cooldowns[i] > 0) continue;
			Skill skill = skills.get(i);
			if (!shouldUse(skill, mob, enemy)) continue;
			int activationCell = mob.pos;
			lastPowderRainBomb = null;
			if (use(skill, mob, enemy)) {
				cooldowns[i] = cooldown(skill);
				globalCooldown = 2;
				announceSkill(skill, activationCell);
				showSkillEffect(mob);
				return true;
			}
		}
		return false;
	}

	private boolean shouldUse(Skill skill, Mob mob, Char enemy) {
		int distance = Dungeon.level.distance(mob.pos, enemy.pos);
		switch (skill) {
			case PESTILENT_SHROUD:
			case DELIRIUM_VEIL:
			case FETID_DOMINION:
			case MIREBLOOD_AURA:
			case ASHEN_SHROUD:
			case NERVEFOG:
			case CAUSTIC_MIASMA:
			case CINDERWAKE:
			case WINTERS_GRASP:
				return false;
			case CRIMSON_RENEWAL:
			case RIFT_WITHDRAWAL:
			case PRISMATIC_WARD:
				return mob.HP * 100 <= mob.HT * (rank.ordinal() >= Rank.EPIC.ordinal() ? 35 : 50);
			case RIFT_ASSAULT:
			case VENOMWHEEL_VOLLEY:
			case POWDER_RAIN:
			case HUNTERS_REVELATION:
				return distance >= 2 && distance <= 6;
			case BATTLE_TRANCE:
			case WARBREW_RUSH:
			case EARTHEN_COVENANT:
				return true;
			default:
				return distance <= 2;
		}
	}

	private boolean use(Skill skill, Mob mob, Char enemy) {
		if (skill == Skill.POWDER_RAIN) {
			return throwPowderRain(mob, enemy);
		}
		showRangedSkillFx(skill, mob, enemy);
		markTarget(mob, enemy);
		switch (skill) {
			case BATTLE_TRANCE:
				Buff.affect(mob, Bless.class, 5f);
				Buff.affect(mob, Haste.class, 5f);
				return true;
			case WARBREW_RUSH:
				Buff.affect(mob, Haste.class, 6f);
				return true;
			case HUNTERS_REVELATION:
				Buff.affect(mob, Bless.class, 8f);
				mob.aggro(enemy);
				return true;
			case PESTILENT_SHROUD:
				affectArea(mob, 2, target -> Buff.affect(target, Poison.class).set(6f));
				return true;
			case DELIRIUM_VEIL:
				affectArea(mob, 2, target -> {
					Buff.affect(target, Vertigo.class, 4f);
					Buff.affect(target, Blindness.class, 3f);
				});
				return true;
			case FETID_DOMINION:
				affectArea(mob, 2, target -> Buff.affect(target, Paralysis.class, 2f));
				return true;
			case MIREBLOOD_AURA:
				affectArea(mob, 2, target -> {
					Buff.affect(target, Poison.class).set(4f);
					Buff.affect(target, Cripple.class, 3f);
				});
				return true;
			case BRIAR_DOMAIN:
				showBriarDomain(mob, 2);
				affectArea(mob, 2, target -> Buff.affect(target, Roots.class, 4f));
				return true;
			case ASHEN_SHROUD:
				affectArea(mob, 2, target -> Buff.affect(target, Blindness.class, 5f));
				return true;
			case NERVEFOG:
				Buff.affect(enemy, Paralysis.class, 3f);
				return true;
			case CAUSTIC_MIASMA:
				affectArea(mob, 2, target -> Buff.affect(target, Corrosion.class).set(6f, Math.max(1, mob.HT / 20)));
				return true;
			case CINDERWAKE:
				affectArea(mob, 2, target -> Buff.affect(target, Burning.class).reignite(target, 5f));
				return true;
			case WINTERS_GRASP:
				affectArea(mob, 2, target -> Buff.affect(target, Chill.class, 6f));
				return true;
			case SILKEN_EXPANSE:
				laySilkenExpanse(mob, enemy);
				affectArea(mob, 2, target -> {
					Buff.affect(target, Roots.class, 3f);
					Buff.affect(target, Cripple.class, 4f);
				});
				return true;
			case RAINCALLERS_GROUND:
				Buff.affect(enemy, Chill.class, 3f);
				enemy.damage(Math.max(1, mob.damageRoll() / 4), mob);
				return true;
			case VEILSTEP:
				Buff.affect(mob, EliteVeilstep.class, 6f);
				return retreat(mob, enemy);
			case CRIMSON_RENEWAL:
				int healing = Math.max(1, mob.HT / 4);
				mob.HP = Math.min(mob.HT, mob.HP + healing);
				return true;
			case DREAD_EDICT:
				Buff.affect(enemy, Terror.class, 6f).object = mob.id();
				return true;
			case VENOMWHEEL_VOLLEY:
				enemy.damage(Math.max(1, mob.damageRoll() / 2), mob);
				Buff.affect(enemy, Poison.class).set(5f);
				return true;
			case EARTHEN_COVENANT:
				Buff.affect(mob, Barrier.class).incShield(Math.max(2, mob.HT / 10));
				return true;
			case STORM_CAGE:
				enemy.damage(Math.max(1, mob.damageRoll() / 2), mob);
				Buff.affect(enemy, Paralysis.class, 2f);
				return true;
			case RIFT_ASSAULT:
				if (!teleportBeside(mob, enemy)) return false;
				enemy.damage(Math.max(1, mob.damageRoll()), mob);
				return true;
			case RIFT_WITHDRAWAL:
				return retreat(mob, enemy);
			case DROWSING_HYMN:
				affectArea(mob, 2, target -> Buff.affect(target, Drowsy.class, Drowsy.DURATION));
				return true;
			case ECHO_LEGION:
				return summonEchoes(mob, enemy);
			case PRISMATIC_WARD:
				Buff.affect(mob, Barrier.class).incShield(Math.max(1, Math.round(mob.HT * 0.15f)));
				return true;
			case WRIT_OF_RETRIBUTION:
				int missing = mob.HT - mob.HP;
				enemy.damage(Math.max(1, Math.min(mob.HT / 3, missing / 3)), mob);
				return true;
			default:
				return false;
		}
	}

	private void emitPassiveClouds(Mob mob) {
		if (mob == null || !mob.isAlive() || Dungeon.level == null) return;
		if (has(Skill.DELIRIUM_VEIL) && !deliriumVeilAnnounced && visibleToHero(mob.pos)) {
			announceSkill(Skill.DELIRIUM_VEIL, mob.pos);
			deliriumVeilAnnounced = true;
		}
		int[] pulseCells = pulseCells(mob, diagonalCloudPulse);
		if (has(Skill.PESTILENT_SHROUD) || has(Skill.MIREBLOOD_AURA)) seedCloud(pulseCells, ToxicGas.class, 6);
		if (has(Skill.DELIRIUM_VEIL)) seedCloud(pulseCells, ConfusionGas.class, 5);
		if (has(Skill.FETID_DOMINION) || has(Skill.NERVEFOG)) seedCloud(pulseCells, StenchGas.class, 1);
		if (has(Skill.ASHEN_SHROUD)) seedCloud(pulseCells, SmokeScreen.class, 5);
		if (has(Skill.CAUSTIC_MIASMA)) seedCloud(pulseCells, CorrosiveGas.class, 5);
		if (has(Skill.CINDERWAKE)) seedCloud(pulseCells, Fire.class, 3);
		if (has(Skill.WINTERS_GRASP)) seedCloud(pulseCells, Freezing.class, 1);

		if (has(Skill.MIREBLOOD_AURA)) affectPulse(mob, pulseCells, target -> {
			Buff.affect(target, Poison.class).set(4f);
			Buff.affect(target, Cripple.class, 3f);
		});
		if (has(Skill.ASHEN_SHROUD)) {
			affectPulse(mob, pulseCells, target -> Buff.affect(target, Blindness.class, 5f));
		}
		diagonalCloudPulse = !diagonalCloudPulse;
	}

	private void showBriarDomain(Mob mob, int radius) {
		int width = Dungeon.level.width();
		for (int dy = -radius; dy <= radius; dy++) {
			for (int dx = -radius; dx <= radius; dx++) {
				int cell = mob.pos + dx + dy * width;
				if (Dungeon.level.insideMap(cell)
						&& Dungeon.level.distance(mob.pos, cell) <= radius
						&& !Dungeon.level.solid[cell]) {
					CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 5);
				}
			}
		}
	}

	private int[] pulseCells(Mob mob, boolean diagonal) {
		int width = Dungeon.level.width();
		int[] offsets = diagonal
				? new int[]{0, -width - 1, -width + 1, width - 1, width + 1}
				: new int[]{0, -width, 1, width, -1};
		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : offsets) {
			int cell = mob.pos + offset;
			if (Dungeon.level.insideMap(cell) && Dungeon.level.distance(mob.pos, cell) <= 1
					&& !Dungeon.level.solid[cell]) cells.add(cell);
		}
		int[] result = new int[cells.size()];
		for (int i = 0; i < cells.size(); i++) result[i] = cells.get(i);
		return result;
	}

	private void seedCloud(int[] cells, Class<? extends Blob> cloud, int volume) {
		int cellVolume = Math.max(1, volume / 2);
		for (int cell : cells) GameScene.add(Blob.seed(cell, cellVolume, cloud));
	}

	private void affectPulse(Mob mob, int[] cells, Consumer<Char> effect) {
		for (Char target : Actor.chars()) {
			if (!canAffect(mob, target)) continue;
			for (int cell : cells) {
				if (target.pos == cell) {
					markTarget(mob, target);
					effect.accept(target);
					break;
				}
			}
		}
	}

	private void laySilkenExpanse(Mob mob, Char enemy) {
		if (enemy == null) return;
		int width = Dungeon.level.width();
		int[] offsets = {0, -width, 1, width, -1};
		for (int offset : offsets) {
			int cell = enemy.pos + offset;
			if (Dungeon.level.insideMap(cell) && Dungeon.level.distance(enemy.pos, cell) <= 1
					&& !Dungeon.level.solid[cell]) {
				GameScene.add(Blob.seed(cell, 20, Web.class));
			}
		}
	}

	private void showRangedSkillFx(Skill skill, Mob mob, Char enemy) {
		if (mob == null || enemy == null || mob.sprite == null || mob.sprite.parent == null) return;
		int missile = -1;
			switch (skill) {
			case SILKEN_EXPANSE:
				missile = MagicMissile.MAGIC_MISSILE;
				break;
			case VENOMWHEEL_VOLLEY:
				missile = MagicMissile.POISON;
				break;
			case RAINCALLERS_GROUND:
			case WINTERS_GRASP:
				missile = MagicMissile.FROST;
				break;
			case NERVEFOG:
			case STORM_CAGE:
				missile = MagicMissile.SPECK + Speck.PARALYSIS;
				break;
			case DREAD_EDICT:
			case WRIT_OF_RETRIBUTION:
				missile = MagicMissile.SHADOW;
				break;
			default:
				return;
		}
		mob.sprite.zap(enemy.pos);
		MagicMissile.boltFromChar(mob.sprite.parent, missile, mob.sprite, enemy.pos, null);
	}

	private boolean throwPowderRain(Mob mob, Char enemy) {
		if (mob == null || enemy == null || Dungeon.level == null) return false;
		final Bomb bomb = randomPowderRainBomb().damageSource(mob);
		lastPowderRainBomb = bomb;
		final int target = enemy.pos;

		if (mob.sprite != null && mob.sprite.parent != null) {
			mob.sprite.zap(target);
			((MissileSprite) mob.sprite.parent.recycle(MissileSprite.class))
					.reset(mob.sprite, target, bomb, () -> landArmedBomb(bomb, target));
		} else {
			landArmedBomb(bomb, target);
		}
		return true;
	}

	private void announceSkill(Skill skill, int activationCell) {
		if (!visibleToHero(activationCell)) return;

		PointF center = DungeonTilemap.tileCenterToWorld(activationCell);
		FloatingText.show(center.x, center.y - 8f, activationCell,
				skill.title, rank.rarity.color());
		switch (skill) {
			case POWDER_RAIN:
				EliteSkillIcon.showItem(activationCell,
						lastPowderRainBomb == null ? new Bomb() : lastPowderRainBomb);
				break;
			case BRIAR_DOMAIN:
				EliteSkillIcon.showInterfaceIcon(activationCell, Icons.GRASS);
				break;
			case SILKEN_EXPANSE:
				EliteSkillIcon.showWebProjectile(activationCell);
				break;
			case RAINCALLERS_GROUND:
				EliteSkillIcon.showTextIcon(activationCell, FloatingText.SHOCKING);
				break;
			case VENOMWHEEL_VOLLEY:
				EliteSkillIcon.showTextIcon(activationCell, FloatingText.POISON);
				break;
			case STORM_CAGE:
				EliteSkillIcon.showStormCage(activationCell);
				break;
			case BLOODRUSH:
				EliteSkillIcon.showBuffIcon(activationCell, BuffIndicator.RAGE);
				break;
			case KEEN_HUNTER:
				EliteSkillIcon.showOverlappingIcons(activationCell,
						ItemSpriteSheet.Icons.RING_ACCURACY,
						ItemSpriteSheet.Icons.POTION_MINDVIS);
				break;
			case DELIRIUM_VEIL:
				EliteSkillIcon.showOverlappingIcons(activationCell,
						ItemSpriteSheet.Icons.POTION_LEVITATE,
						ItemSpriteSheet.Icons.POTION_SHROUDFOG);
				break;
			default:
				int icon = skillIcon(skill);
				if (icon >= 0) EliteSkillIcon.showIcon(activationCell, icon);
				break;
		}
	}

	private void announceSurvivalInstinct(int activationCell) {
		if (!visibleToHero(activationCell)) return;
		PointF center = DungeonTilemap.tileCenterToWorld(activationCell);
		FloatingText.show(center.x, center.y - 8f, activationCell,
				"Survival Instinct", rank.rarity.color());
		EliteSkillIcon.showIcon(activationCell, ItemSpriteSheet.Icons.POTION_HASTE);
	}

	private boolean visibleToHero(int cell) {
		return Dungeon.level != null && Dungeon.level.heroFOV != null
				&& cell >= 0 && cell < Dungeon.level.length()
				&& Dungeon.level.heroFOV[cell];
	}

	private int skillIcon(Skill skill) {
		switch (skill) {
			case BATTLE_TRANCE:
			case WARBREW_RUSH:
				return ItemSpriteSheet.Icons.POTION_HASTE;
			case HUNTERS_REVELATION:
				return ItemSpriteSheet.Icons.POTION_MINDVIS;
			case PESTILENT_SHROUD:
			case MIREBLOOD_AURA:
				return ItemSpriteSheet.Icons.POTION_TOXICGAS;
			case FETID_DOMINION:
			case NERVEFOG:
				return ItemSpriteSheet.Icons.POTION_PARAGAS;
			case CAUSTIC_MIASMA:
				return ItemSpriteSheet.Icons.POTION_CORROGAS;
			case CINDERWAKE:
				return ItemSpriteSheet.Icons.POTION_LIQFLAME;
			case WINTERS_GRASP:
				return ItemSpriteSheet.Icons.POTION_FROST;
			case ASHEN_SHROUD:
				return ItemSpriteSheet.Icons.POTION_SHROUDFOG;
			case VEILSTEP:
				return ItemSpriteSheet.Icons.POTION_INVIS;
			case CRIMSON_RENEWAL:
				return ItemSpriteSheet.Icons.POTION_HEALING;
			case DREAD_EDICT:
				return ItemSpriteSheet.Icons.SCROLL_TERROR;
			case EARTHEN_COVENANT:
			case PRISMATIC_WARD:
				return ItemSpriteSheet.Icons.POTION_SHIELDING;
			case RIFT_ASSAULT:
			case RIFT_WITHDRAWAL:
				return ItemSpriteSheet.Icons.SCROLL_TELEPORT;
			case DROWSING_HYMN:
				return ItemSpriteSheet.Icons.SCROLL_LULLABY;
			case ECHO_LEGION:
				return ItemSpriteSheet.Icons.SCROLL_MIRRORIMG;
			case WRIT_OF_RETRIBUTION:
				return ItemSpriteSheet.Icons.SCROLL_RETRIB;
			default:
				return -1;
		}
	}

	private void landArmedBomb(Bomb bomb, int target) {
		if (bomb == null || Dungeon.level == null
				|| target < 0 || target >= Dungeon.level.length()) return;
		Heap heap = Dungeon.level.drop(bomb, target);
		if (heap.items.contains(bomb)) {
			bomb.lightFuse();
			heap.sprite.drop();
		}
	}

	private Bomb randomPowderRainBomb() {
		switch (Random.Int(10)) {
			case 1: return new FrostBomb();
			case 2: return new Firebomb();
			case 3: return new SmokeBomb();
			case 4: return new ArcaneBomb();
			case 5: return new WoollyBomb();
			case 6: return new Noisemaker();
			case 7: return new FlashBangBomb();
			case 8: return new HolyBomb();
			case 9: return new ShrapnelBomb();
			default: return new Bomb();
		}
	}

	public boolean isImmuneTo(Class effect) {
		return ((has(Skill.PESTILENT_SHROUD) || has(Skill.MIREBLOOD_AURA)) && ToxicGas.class.isAssignableFrom(effect))
				|| (has(Skill.MIREBLOOD_AURA) && (effect == Poison.class || effect == Cripple.class))
				|| (has(Skill.DELIRIUM_VEIL) && ConfusionGas.class.isAssignableFrom(effect))
				|| ((has(Skill.FETID_DOMINION) || has(Skill.NERVEFOG))
						&& (ParalyticGas.class.isAssignableFrom(effect) || StenchGas.class.isAssignableFrom(effect)))
				|| (has(Skill.ASHEN_SHROUD) && (SmokeScreen.class.isAssignableFrom(effect) || effect == Blindness.class))
				|| (has(Skill.BRIAR_DOMAIN) && effect == Roots.class)
				|| (has(Skill.SILKEN_EXPANSE) && (Web.class.isAssignableFrom(effect) || effect == Roots.class || effect == Cripple.class))
				|| (has(Skill.CAUSTIC_MIASMA) && CorrosiveGas.class.isAssignableFrom(effect))
				|| (has(Skill.CINDERWAKE) && Fire.class.isAssignableFrom(effect))
				|| (has(Skill.WINTERS_GRASP) && Freezing.class.isAssignableFrom(effect));
	}

	private boolean summonEchoes(Mob mob, Char enemy) {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = mob.pos + offset;
			if (cell >= 0 && cell < Dungeon.level.length() && Dungeon.level.passable[cell]
					&& Actor.findChar(cell) == null) cells.add(cell);
		}
		if (cells.isEmpty()) return false;
		int count = Math.min(rank.ordinal() >= Rank.LEGENDARY.ordinal() ? 2 : 1, cells.size());
		for (int i = 0; i < count; i++) {
			int cell = cells.remove(Random.Int(cells.size()));
			EliteEcho echo = new EliteEcho();
			echo.configure(mob, enemy, cell);
			GameScene.add(echo);
			Dungeon.level.occupyCell(echo);
			if (echo.sprite != null) {
				echo.sprite.alpha(0.65f);
				echo.sprite.centerEmitter().burst(Speck.factory(Speck.LIGHT), 8);
			}
		}
		return true;
	}

	private void showSkillEffect(Mob mob) {
		if (mob != null && mob.sprite != null) {
			mob.sprite.centerEmitter().burst(Speck.factory(Speck.STAR), 10);
		}
	}

	private void affectArea(Mob mob, int radius, Consumer<Char> effect) {
		for (Char target : Actor.chars()) {
			if (canAffect(mob, target)
					&& Dungeon.level.distance(mob.pos, target.pos) <= radius) {
				markTarget(mob, target);
				effect.accept(target);
			}
		}
	}

	private boolean canAffect(Mob mob, Char target) {
		if (target == null || target == mob || !target.isAlive()) return false;
		if (target instanceof EliteEcho && ((EliteEcho)target).isEchoOf(mob)) return false;
		return isTranscendant() || target.alignment != mob.alignment;
	}

	private void markTarget(Mob hunter, Char target) {
		if (!isTranscendant() || !(target instanceof Mob)) return;
		HuntMark mark = Buff.affect(target, HuntMark.class, 20f);
		mark.hunterId = hunter.id();
	}

	public static Mob killHunter(Mob victim, Object cause) {
		if (cause instanceof Mob) {
			Mob direct = (Mob)cause;
			if (direct.eliteMob != null && direct.eliteMob.isTranscendant()) return direct;
		}
		return null;
	}

	public static class HuntMark extends FlavourBuff {
		private static final String HUNTER_ID = "hunter_id";
		private int hunterId = -1;

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(HUNTER_ID, hunterId);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			hunterId = bundle.getInt(HUNTER_ID);
		}
	}

	private boolean teleportBeside(Mob mob, Char enemy) {
		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = enemy.pos + offset;
			if (cell >= 0 && cell < Dungeon.level.length() && Dungeon.level.passable[cell] && Actor.findChar(cell) == null) cells.add(cell);
		}
		if (cells.isEmpty()) return false;
		ScrollOfTeleportation.appear(mob, Random.element(cells));
		return true;
	}

	private boolean retreat(Mob mob, Char enemy) {
		int best = -1;
		int bestDistance = Dungeon.level.distance(mob.pos, enemy.pos);
		for (int offset : PathFinder.NEIGHBOURS8) {
			int cell = mob.pos + offset;
			if (cell < 0 || cell >= Dungeon.level.length() || !Dungeon.level.passable[cell] || Actor.findChar(cell) != null) continue;
			int distance = Dungeon.level.distance(cell, enemy.pos);
			if (distance > bestDistance) {
				bestDistance = distance;
				best = cell;
			}
		}
		if (best == -1) return false;
		ScrollOfTeleportation.appear(mob, best);
		return true;
	}

	private int cooldown(Skill skill) {
		switch (skill) {
			case VENOMWHEEL_VOLLEY: return 9;
			case POWDER_RAIN: return 14;
			case RIFT_ASSAULT: return 16;
			case BATTLE_TRANCE:
			case WARBREW_RUSH: return 18;
			case HUNTERS_REVELATION:
			case VEILSTEP: return 20;
			case DREAD_EDICT:
			case EARTHEN_COVENANT:
			case RIFT_WITHDRAWAL: return 22;
			case CRIMSON_RENEWAL:
			case PRISMATIC_WARD: return 24;
			case DROWSING_HYMN:
			case NULL_MANTLE: return 26;
			case ECHO_LEGION:
			case WRIT_OF_RETRIBUTION: return 28;
			default: return 12;
		}
	}

	private boolean has(Skill skill) {
		return skills.contains(skill);
	}

	public int bonusExperience(int base) {
		return Math.max(base, Math.round(base * (1f + rank.xpBonus / 100f)));
	}

	public int bonusLootRolls() {
		return rank.guaranteedLootRolls
				+ (Random.Int(100) < rank.additionalLootChance ? 1 : 0);
	}

	public String info() {
		StringBuilder text = new StringBuilder();
		text.append(rank.rarity.coloredName()).append(" Elite\n");
		text.append("_Ironbound_ - Reduces all incoming damage by ").append(damageReduction).append("%.\n");
		text.append("_Titanic Vitality_ - Increases maximum health by ").append(healthBoost).append("%.\n");
		text.append("_Elite Amplification_ - Increases combat stats by ").append(rank.statBoost).append("%.");
		if (rank.ordinal() >= Rank.EPIC.ordinal()) {
			text.append("\n_Survival Instinct_ - Retreats for several turns when reduced below 35% health.");
		}
		for (Skill skill : skills) {
			text.append("\n_").append(skill.title).append(" Lv. ").append(skillLevel(skill)).append("_")
					.append(" - ").append(skill.description);
		}
		return text.toString();
	}

	public void showAura(Mob mob) {
		if (mob != null && mob.sprite != null && rank != null && mob.invisible <= 0) {
			mob.sprite.aura(rank.rarity.color(), rank == Rank.TRANSCENDANT ? 12 : 6 + rank.ordinal());
		}
	}

	/** Elite concealment hides both the character and its rarity aura. */
	public static class EliteVeilstep extends Invisibility {
		@Override
		public void fx(boolean on) {
			if (target == null || target.sprite == null) return;
			target.sprite.fullInvisibility(on);
			if (!on && target.invisible == 0 && target instanceof Mob) {
				((Mob) target).showEliteAura();
			}
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put(RANK, rank.name());
		bundle.put(DAMAGE_REDUCTION, damageReduction);
		bundle.put(HEALTH_BOOST, healthBoost);
		String[] names = new String[skills.size()];
		for (int i = 0; i < skills.size(); i++) names[i] = skills.get(i).name();
		bundle.put(SKILLS, names);
		bundle.put(COOLDOWNS, cooldowns);
		bundle.put(SKILL_LEVELS, skillLevels);
		bundle.put(TRANSCENDANT_LEVEL, transcendantLevel);
		bundle.put(TRANSCENDANT_EXPERIENCE, transcendantExperience);
		bundle.put(CONSECUTIVE_SKILL_ENHANCEMENTS, consecutiveSkillEnhancements);
		bundle.put(GLOBAL_COOLDOWN, globalCooldown);
		bundle.put(SURVIVAL_COOLDOWN, survivalCooldown);
		bundle.put(RETREAT_TURNS, retreatTurns);
		bundle.put(CLOUD_PULSE, cloudPulse);
		bundle.put(DIAGONAL_CLOUD_PULSE, diagonalCloudPulse);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		rank = Rank.valueOf(bundle.getString(RANK));
		damageReduction = bundle.getInt(DAMAGE_REDUCTION);
		healthBoost = bundle.getInt(HEALTH_BOOST);
		skills.clear();
		for (String name : bundle.getStringArray(SKILLS)) {
			try { skills.add(Skill.valueOf(name)); } catch (IllegalArgumentException ignored) { }
		}
		cooldowns = bundle.getIntArray(COOLDOWNS);
		if (cooldowns.length != skills.size()) cooldowns = new int[skills.size()];
		skillLevels = bundle.contains(SKILL_LEVELS) ? bundle.getIntArray(SKILL_LEVELS) : new int[skills.size()];
		if (skillLevels.length != skills.size()) skillLevels = new int[skills.size()];
		for (int i = 0; i < skillLevels.length; i++) if (skillLevels[i] <= 0) skillLevels[i] = 1;
		transcendantLevel = bundle.contains(TRANSCENDANT_LEVEL) ? Math.max(1, bundle.getInt(TRANSCENDANT_LEVEL)) : 1;
		transcendantExperience = bundle.getInt(TRANSCENDANT_EXPERIENCE);
		consecutiveSkillEnhancements = bundle.getInt(CONSECUTIVE_SKILL_ENHANCEMENTS);
		globalCooldown = bundle.getInt(GLOBAL_COOLDOWN);
		survivalCooldown = bundle.getInt(SURVIVAL_COOLDOWN);
		retreatTurns = bundle.getInt(RETREAT_TURNS);
		cloudPulse = bundle.getInt(CLOUD_PULSE);
		diagonalCloudPulse = bundle.getBoolean(DIAGONAL_CLOUD_PULSE);
	}
}
