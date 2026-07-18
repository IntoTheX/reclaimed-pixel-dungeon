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

package com.erebus.reclaimedpixeldungeon.sprites;

import com.erebus.reclaimedpixeldungeon.actors.Char;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.actors.mobs.npcs.HomebaseDefender;
import com.erebus.reclaimedpixeldungeon.items.ItemRarity;
import com.watabou.noosa.Halo;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

public class HomebaseDefenderSprite extends MobSprite {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 15;
	private static final int RUN_FRAMERATE = 20;

	private Animation fly;
	private Halo rarityHalo;

	public HomebaseDefenderSprite() {
		this( HeroClass.WARRIOR, 0, null );
	}

	public HomebaseDefenderSprite( HeroClass heroClass, int tier ) {
		this( heroClass, tier, null );
	}

	public HomebaseDefenderSprite( HeroClass heroClass, int tier, ItemRarity rarity ) {
		super();
		updateArmor( heroClass, tier );
		if (rarity != null) updateRarityHalo( rarity );
		idle();
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		if (ch instanceof HomebaseDefender) {
			HomebaseDefender defender = (HomebaseDefender)ch;
			updateArmor( defender.heroClass(), defender.armorTier() );
			updateRarityHalo( defender.rarity() );
		}
	}

	public void updateArmor( HeroClass heroClass, int tier ) {
		texture( heroClass.spritesheet() );
		TextureFilm film = new TextureFilm( HeroSprite.tiers(), Math.max( 0, tier ), FRAME_WIDTH, FRAME_HEIGHT );

		idle = new Animation( 1, true );
		idle.frames( film, 0, 0, 0, 1, 0, 0, 1, 1 );

		run = new Animation( RUN_FRAMERATE, true );
		run.frames( film, 2, 3, 4, 5, 6, 7 );

		die = new Animation( 20, false );
		die.frames( film, 8, 9, 10, 11, 12, 11 );

		attack = new Animation( 15, false );
		attack.frames( film, 13, 14, 15, 0 );

		zap = attack.clone();

		operate = new Animation( 8, false );
		operate.frames( film, 16, 17, 16, 17 );

		fly = new Animation( 1, true );
		fly.frames( film, 18 );

		if (ch == null || ch.isAlive()) {
			idle();
		} else {
			die();
		}
	}

	@Override
	public void idle() {
		super.idle();
		if (ch != null && ch.flying) {
			play( fly );
		}
	}

	@Override
	public void move( int from, int to ) {
		super.move( from, to );
		if (ch != null && ch.flying) {
			play( fly );
		}
	}

	public void updateRarityHalo( ItemRarity rarity ) {
		if (rarity == null) rarity = ItemRarity.COMMON;
		if (rarityHalo == null) {
			rarityHalo = new Halo();
			rarityHalo.radius( 9f );
		}
		rarityHalo.hardlight( rarity.color() );
		rarityHalo.alpha( haloAlpha( rarity ) );
		updateRarityHaloPosition();
	}

	private static float haloAlpha( ItemRarity rarity ) {
		return Math.min( 0.68f, Math.max( 0.38f, rarity.auraAlpha() * 1.45f ) );
	}

	@Override
	public void update() {
		super.update();
		updateRarityHaloPosition();
	}

	@Override
	public void draw() {
		if (rarityHalo != null && rarityHalo.visible) {
			updateRarityHaloPosition();
			rarityHalo.draw();
		}
		super.draw();
	}

	private void updateRarityHaloPosition() {
		if (rarityHalo == null) return;
		if (rarityHalo.parent != null) {
			rarityHalo.parent.remove( rarityHalo );
		}
		rarityHalo.camera = camera();
		rarityHalo.visible = visible;
		rarityHalo.point( center().x, center().y + 1 );
	}

	@Override
	public void destroy() {
		if (rarityHalo != null) {
			rarityHalo.killAndErase();
			rarityHalo = null;
		}
		super.destroy();
	}

	@Override
	public void bloodBurstA( PointF from, int damage ) {
		// Hero-class defender sprites use the same no-blood presentation as hero sprites.
	}
}
