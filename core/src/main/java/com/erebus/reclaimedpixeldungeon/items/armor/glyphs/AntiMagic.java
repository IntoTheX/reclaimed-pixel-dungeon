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

package com.erebus.reclaimedpixeldungeon.items.armor.glyphs;

import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Charm;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Degrade;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Hex;
import com.erebus.reclaimedpixeldungeon.actors.buffs.MagicalSleep;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Vulnerable;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Weakness;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.mage.ElementalBlast;
import com.erebus.reclaimedpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.GuidingLight;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.HolyLance;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.HolyWeapon;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.Judgement;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.Smite;
import com.erebus.reclaimedpixeldungeon.actors.hero.spells.Sunray;
import com.erebus.reclaimedpixeldungeon.actors.mobs.CrystalWisp;
import com.erebus.reclaimedpixeldungeon.actors.mobs.DM100;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Eye;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Shaman;
import com.erebus.reclaimedpixeldungeon.actors.mobs.Warlock;
import com.erebus.reclaimedpixeldungeon.actors.mobs.YogFist;
import com.erebus.reclaimedpixeldungeon.items.armor.Armor;
import com.erebus.reclaimedpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.erebus.reclaimedpixeldungeon.items.bombs.ArcaneBomb;
import com.erebus.reclaimedpixeldungeon.items.bombs.HolyBomb;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.erebus.reclaimedpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.erebus.reclaimedpixeldungeon.items.wands.CursedWand;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfBlastWave;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfDisintegration;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFireblast;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfFrost;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLightning;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfLivingEarth;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfMagicMissile;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfPrismaticLight;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfTransfusion;
import com.erebus.reclaimedpixeldungeon.items.wands.WandOfWarding;
import com.erebus.reclaimedpixeldungeon.items.weapon.enchantments.Blazing;
import com.erebus.reclaimedpixeldungeon.items.weapon.enchantments.Grim;
import com.erebus.reclaimedpixeldungeon.items.weapon.enchantments.Shocking;
import com.erebus.reclaimedpixeldungeon.items.weapon.missiles.darts.HolyDart;
import com.erebus.reclaimedpixeldungeon.levels.traps.DisintegrationTrap;
import com.erebus.reclaimedpixeldungeon.levels.traps.GrimTrap;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class AntiMagic extends Armor.Glyph {

	private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x88EEFF );
	
	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( MagicalSleep.class );
		RESISTS.add( Charm.class );
		RESISTS.add( Weakness.class );
		RESISTS.add( Vulnerable.class );
		RESISTS.add( Hex.class );
		RESISTS.add( Degrade.class );
		
		RESISTS.add( DisintegrationTrap.class );
		RESISTS.add( GrimTrap.class );

		RESISTS.add( ArcaneBomb.class );
		RESISTS.add( HolyBomb.HolyDamage.class );
		RESISTS.add( ScrollOfRetribution.class );
		RESISTS.add( ScrollOfPsionicBlast.class );
		RESISTS.add( ScrollOfTeleportation.class );
		RESISTS.add( HolyDart.class );

		RESISTS.add( GuidingLight.class );
		RESISTS.add( HolyWeapon.class );
		RESISTS.add( Sunray.class );
		RESISTS.add( HolyLance.class );
		RESISTS.add( Smite.class );
		RESISTS.add( Judgement.class );

		RESISTS.add( ElementalBlast.class );
		RESISTS.add( CursedWand.class );
		RESISTS.add( WandOfBlastWave.class );
		RESISTS.add( WandOfDisintegration.class );
		RESISTS.add( WandOfFireblast.class );
		RESISTS.add( WandOfFrost.class );
		RESISTS.add( WandOfLightning.class );
		RESISTS.add( WandOfLivingEarth.class );
		RESISTS.add( WandOfMagicMissile.class );
		RESISTS.add( WandOfPrismaticLight.class );
		RESISTS.add( WandOfTransfusion.class );
		RESISTS.add( WandOfWarding.Ward.class );

		RESISTS.add( ChaliceOfBlood.class );

		RESISTS.add( ElementalStrike.class );
		RESISTS.add( Blazing.class );
		RESISTS.add( Shocking.class );
		RESISTS.add( Grim.class );

		RESISTS.add( WarpBeacon.class );
		
		RESISTS.add( DM100.LightningBolt.class );
		RESISTS.add( Shaman.EarthenBolt.class );
		RESISTS.add( CrystalWisp.LightBeam.class );
		RESISTS.add( Warlock.DarkBolt.class );
		RESISTS.add( Eye.DeathGaze.class );
		RESISTS.add( YogFist.BrightFist.LightBeam.class );
		RESISTS.add( YogFist.DarkFist.DarkBolt.class );
	}
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		//no proc effect, triggers in Char.damage
		return damage;
	}
	
	public static int drRoll( Char owner, int level ){
		if (level == -1){
			return 0;
		} else {
			return Random.NormalIntRange(
					Math.round(level * genericProcChanceMultiplier(owner)),
					Math.round((3 + (level * 1.5f)) * genericProcChanceMultiplier(owner)));
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return TEAL;
	}

}