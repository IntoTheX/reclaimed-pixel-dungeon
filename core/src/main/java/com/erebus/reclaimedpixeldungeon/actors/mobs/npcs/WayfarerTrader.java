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

package com.erebus.reclaimedpixeldungeon.actors.mobs.npcs;

import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTraderProfile;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.HomebaseDefenderSprite;

public class WayfarerTrader extends NPC {

	private String traderName = "Trader";
	private HeroClass heroClass = HeroClass.WARRIOR;
	private int armorTier = 0;
	private WayfarerTraderProfile profile = new WayfarerTraderProfile();
	private float nameTicker = 0;
	private String peerId = "";

	{
		HP = HT = 100;
		defenseSkill = 999;
		viewDistance = 4;
		spriteClass = HomebaseDefenderSprite.class;
	}

	public WayfarerTrader() {
	}

	public WayfarerTrader( String traderName, String heroClassName, int armorTier ) {
		this.traderName = traderName == null || traderName.trim().isEmpty() ? "Trader" : traderName.trim();
		this.heroClass = parseHeroClass( heroClassName );
		this.armorTier = Math.max( 0, Math.min( 6, armorTier ) );
		profile.name = this.traderName;
		profile.heroClass = this.heroClass.name();
		profile.armorTier = this.armorTier;
	}

	public WayfarerTrader( WayfarerTraderProfile profile ) {
		updateProfile( profile );
	}

	public WayfarerTrader( WayfarerTraderProfile profile, String peerId ) {
		this.peerId = peerId == null ? "" : peerId;
		updateProfile( profile );
	}

	public void updateProfile( WayfarerTraderProfile profile ) {
		if (profile == null) profile = new WayfarerTraderProfile();
		this.profile = profile;
		this.traderName = profile.name == null || profile.name.trim().isEmpty() ? "Trader" : profile.name.trim();
		this.heroClass = parseHeroClass( profile.heroClass );
		this.armorTier = Math.max( 0, Math.min( 6, profile.armorTier ) );
	}

	public WayfarerTraderProfile profile() {
		return profile;
	}

	public String peerId() {
		return peerId;
	}

	@Override
	public CharSprite sprite() {
		return new HomebaseDefenderSprite( heroClass, armorTier );
	}

	@Override
	public String name() {
		return traderName;
	}

	@Override
	public String description() {
		return "A fellow wayfarer bound to the rules of the exchange.";
	}

	@Override
	protected boolean act() {
		nameTicker -= TICK;
		if (sprite != null && nameTicker <= 0) {
			sprite.showStatus( CharSprite.POSITIVE, traderName );
			nameTicker = 2.5f;
		}
		spend( TICK );
		return true;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( com.erebus.reclaimedpixeldungeon.actors.Char target ) {
		return 0;
	}

	private static HeroClass parseHeroClass( String heroClassName ) {
		if (heroClassName != null) {
			for (HeroClass heroClass : HeroClass.values()) {
				if (heroClass.name().equalsIgnoreCase( heroClassName )
						|| heroClass.toString().equalsIgnoreCase( heroClassName )) {
					return heroClass;
				}
			}
		}
		return HeroClass.WARRIOR;
	}
}
