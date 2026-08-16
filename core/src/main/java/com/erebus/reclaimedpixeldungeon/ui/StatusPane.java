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

package com.erebus.reclaimedpixeldungeon.ui;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.SPDAction;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.Actor;
import com.erebus.reclaimedpixeldungeon.effects.CircleArc;
import com.erebus.reclaimedpixeldungeon.effects.Speck;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.windows.WndHero;
import com.erebus.reclaimedpixeldungeon.windows.WndKeyBindings;
import com.watabou.input.GameAction;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.ColorMath;
import com.watabou.utils.GameMath;

public class StatusPane extends Component {

	private NinePatch bg;
	private Image avatar;
	private Button heroInfo;
	public static float talentBlink;
	private float warning;

	public static final float FLASH_RATE = (float)(Math.PI*1.5f); //1.5 blinks per second

	private int lastTier = 0;

	private Image shieldSmall;
	private NinePatch hpSmallFrame;
	private NinePatch shieldSmallFrame;
	private Image shieldLarge;
	private NinePatch shieldLargeFrame;
	private float shieldSmallFullScale = 1f;
	private float shieldLargeFullScale = 1f;
	private Image hp;
	private BitmapText hpText;
	private BitmapText shieldText;
	private Button heroInfoOnBar;

	private static final int SHIELD_SMALL_FRAME_X = 60;
	private static final int SHIELD_SMALL_FRAME_Y = 39;
	private static final int SHIELD_SMALL_FRAME_W = 44;
	private static final int SHIELD_SMALL_FRAME_H = 9;
	private static final int SHIELD_LARGE_FRAME_X = 60;
	private static final int SHIELD_LARGE_FRAME_Y = 67;
	private static final int SHIELD_LARGE_FRAME_W = 13;
	private static final int SHIELD_LARGE_FRAME_H = 12;

	private Image exp;
	private BitmapText expText;

	private int lastLvl = -1;

	private BitmapText level;

	private BuffIndicator buffs;
	private Compass compass;

	private BusyIndicator busy;
	private CircleArc counter;

	private boolean large;

	//potentially extends the hero portrait space to avoid some cutouts
	public static float heroPaneExtraWidth = 0;
	private NinePatch heroPaneCutout;
	//potentially shrinks and/or repositions the hp bar to avoid some cutouts
	public static int hpBarMaxWidth = 50;
	private Image hpCutout;
	//potentially adjusts the row(s) of the the buff indicator to avoid some cutouts
	public static float[] buffBarRowMaxWidths;
	public static float[] buffBarRowAdjusts;

	public StatusPane( boolean large ){
		super();

		String asset = Assets.Interfaces.STATUS;

		this.large = large;

		if (large)  bg = new NinePatch( asset, 0, 64, 41, 39, 33, 0, 4, 0 );
		else        bg = new NinePatch( asset, 0,  0, 32, 38, 32, 0, 0, 0 );
		add( bg );

		heroPaneCutout = new NinePatch(asset, 0, 0, 5, 36, 4, 0, 0, 0);
		heroPaneCutout.visible = false;
		add(heroPaneCutout);

		hpCutout = new Image(asset, 90, 0, 12, 9);
		hpCutout.visible = false;
		add(hpCutout);

		heroInfo = new Button(){
			@Override
			protected void onClick () {
				Camera.main.panTo( Dungeon.hero.sprite.center(), 5f );
				GameScene.show( new WndHero() );
			}
			
			@Override
			public GameAction keyAction() {
				return SPDAction.HERO_INFO;
			}

			@Override
			protected String hoverText() {
				return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
			}
		};
		add(heroInfo);

		avatar = HeroSprite.avatar( Dungeon.hero );
		add( avatar );

		talentBlink = 0;

		compass = new Compass( Statistics.amuletObtained ? Dungeon.level.entrance() : Dungeon.level.exit() );
		add( compass );

		hpSmallFrame = new NinePatch(asset, 29, 0, 53, 9, 1, 1, 10, 1);
		add(hpSmallFrame);

		if (large)  hp = new Image(asset, 0, 103, 128, 9);
		else        hp = new Image(asset, 0, 40, 50, 4);
		add( hp );

		shieldSmallFrame = new NinePatch(asset, SHIELD_SMALL_FRAME_X, SHIELD_SMALL_FRAME_Y,
				SHIELD_SMALL_FRAME_W, SHIELD_SMALL_FRAME_H, 3);
		shieldSmall = new Image(asset, 0, 44, 50, 4);
		shieldLargeFrame = new NinePatch(asset, SHIELD_LARGE_FRAME_X, SHIELD_LARGE_FRAME_Y,
				SHIELD_LARGE_FRAME_W, SHIELD_LARGE_FRAME_H, 3);
		shieldLarge = new Image(asset, 0, 112, 128, 9);

		hpText = new BitmapText(PixelScene.pixelFont);
		hpText.alpha(0.6f);

		shieldText = new BitmapText(PixelScene.pixelFont);
		shieldText.hardlight(0x99CCFF);
		shieldText.alpha(0.75f);

		heroInfoOnBar = new Button(){
			@Override
			protected void onClick () {
				Camera.main.panTo( Dungeon.hero.sprite.center(), 5f );
				GameScene.show( new WndHero() );
			}
		};
		add(heroInfoOnBar);

		if (large)  exp = new Image(asset, 0, 121, 128, 7);
		else        exp = new Image(asset, 0, 48, 17, 4);
		add( exp );

		add(shieldLargeFrame);
		add(shieldLarge);
		add(shieldSmallFrame);
		add(shieldSmall);

		expText = new BitmapText(PixelScene.pixelFont);
		expText.hardlight( 0xFFFFAA );
		expText.alpha(0.6f);
		add(expText);

		level = new BitmapText( PixelScene.pixelFont);
		level.hardlight( 0xFFFFAA );
		add( level );

		buffs = new BuffIndicator( Dungeon.hero, large );
		buffs.bottomUp = large;
		add( buffs );

		busy = new BusyIndicator();
		add( busy );

		counter = new CircleArc(18, 4.25f);
		counter.color( 0x808080, true );
		counter.show(this, busy.center(), 0f);

		add(hpText);
		add(shieldText);
		setShieldBarVisible(false);
	}

	@Override
	protected void layout() {

		height = large ? 39 : 38;

		float heroPaneWidth = 30 + heroPaneExtraWidth;

		bg.x = x + heroPaneExtraWidth;
		bg.y = y;
		if (large)  bg.size( 160, bg.height ); //HP bars must be 128px wide atm
		else        bg.size(32, bg.height);

		avatar.x = bg.x - avatar.width / 2f + 15;
		avatar.y = bg.y - avatar.height / 2f + 16;
		PixelScene.align(avatar);

		heroInfo.setRect( x, y, heroPaneWidth, large ? 40 : 36 );

		compass.x = avatar.x + avatar.width / 2f - compass.origin.x;
		compass.y = avatar.y + avatar.height / 2f - compass.origin.y;
		PixelScene.align(compass);

		if (large) {
			exp.x = x + 30;
			exp.y = y + 30;

			hp.x = x + 30;
			hp.y = y + 19;
			hp.scale.y = 1f;

			shieldLarge.x = hp.x;
			shieldLarge.y = y + 8;
			shieldLargeFullScale = 1f;
			shieldLarge.scale.y = 1f;
			shieldLargeFrame.x = shieldLarge.x - 2;
			shieldLargeFrame.y = shieldLarge.y - 2;
			shieldLargeFrame.size(132, 12);
			PixelScene.align(shieldLarge);
			PixelScene.align(shieldLargeFrame);

			hpText.x = hp.x + (128 - hpText.width())/2f;
			hpText.y = hp.y + 1;
			PixelScene.align(hpText);

			shieldText.x = shieldLarge.x + (128 - shieldText.width())/2f;
			shieldText.y = shieldLarge.y + 1;
			PixelScene.align(shieldText);

			expText.x = exp.x + (128 - expText.width())/2f;
			expText.y = exp.y;
			PixelScene.align(expText);

			heroInfoOnBar.setRect(heroInfo.right(), y + 19, 130, 20);

			//Keep large buff icons beside the status bars so they do not cover the chat.
			buffs.setRect(x + 162, y + 2, 124, 36);

			positionLargeBusyIndicator(Dungeon.hero.shielding());
		} else {
			exp.x = x+2;
			exp.y = y+30;

			if (heroPaneExtraWidth > 0){
				heroPaneCutout.visible = true;
				heroPaneCutout.x = x;
				heroPaneCutout.y = y;
				heroPaneCutout.size(heroPaneExtraWidth+4, heroPaneCutout.height);
			}

			float hpleft = x + heroPaneWidth;
			if (hpBarMaxWidth < 82){
				//the class variable assumes the left of the bar can't move, but we can inset it 9px
				int hpWidth = (int)hpBarMaxWidth;
				if (hpWidth <= 41){
					hpleft -= 9;
					hpWidth += 9;
					hpCutout.visible = true;
					hpCutout.x = hpleft - 2;
					hpCutout.y = y;
				}
				hp.frame(50-hpWidth, 40, 50, 4);
				shieldSmall.frame(50-hpWidth, 44, 50, 4);
			}

			hp.x = hpleft;
			hp.y = y + 3;
			hp.scale.y = 1f;
			hpSmallFrame.x = hpleft - 1;
			hpSmallFrame.y = y + 1;
			hpSmallFrame.size(Math.max(1f, hpBarMaxWidth + 3f), 9);
			PixelScene.align(hpSmallFrame);

			shieldSmall.x = hpleft - 1;
			float shieldSmallWidth = Math.max(1f, shieldSmall.width - 3f);
			shieldSmall.y = y + 9;
			shieldSmallFullScale = shieldSmallWidth / shieldSmall.width;
			shieldSmall.scale.y = 1f;
			shieldSmallFrame.x = hpleft - 1;
			shieldSmallFrame.y = y + 7;
			shieldSmallFrame.size(Math.max(1f, Math.max(44, hpBarMaxWidth) - 3f), 9);
			PixelScene.align(shieldSmall);
			PixelScene.align(shieldSmallFrame);

			hpText.scale.set(PixelScene.align(0.5f));
			hpText.x = hp.x + 1;
			hpText.y = hp.y + (hp.height - (hpText.baseLine()+hpText.scale.y))/2f;
			hpText.y -= 0.001f; //prefer to be slightly higher
			PixelScene.align(hpText);

			shieldText.scale.set(PixelScene.align(0.5f));
			shieldText.x = hpText.x;
			shieldText.y = shieldSmall.y;
			shieldText.y -= 0.001f; //prefer to be slightly higher
			PixelScene.align(shieldText);

			expText.scale.set(PixelScene.align(0.5f));
			expText.x = exp.x + 1;
			expText.y = exp.y + (exp.height - (expText.baseLine()+expText.scale.y))/2f;
			expText.y -= 0.001f; //prefer to be slightly higher
			PixelScene.align(expText);

			heroInfoOnBar.setRect(heroInfo.right(), y, 50, 9);

			if (buffBarRowMaxWidths != null){
				buffs.rowWidthLimits = buffBarRowMaxWidths;
			}
			if (buffBarRowAdjusts != null){
				buffs.rowHeightAdjusts = buffBarRowAdjusts;
			}
			buffs.setRect( x + heroPaneWidth + 1, y + 8, 55, 16 );

			busy.x = x + 1;
			busy.y = y + 37;
		}

		counter.point(busy.center());
	}

	private static final int[] warningColors = new int[]{0x660000, 0xCC0000, 0x660000};

	private int oldHP = 0;
	private int oldShield = 0;
	private int shieldPeak = 0;
	private int oldMax = 0;

	@Override
	public void update() {
		super.update();
		
		int health = Dungeon.hero.HP;
		int shield = Dungeon.hero.shielding();
		int max = Dungeon.hero.HT;
		boolean shieldVisibilityChanged = (oldShield > 0) != (shield > 0);

		if (!Dungeon.hero.isAlive()) {
			avatar.tint(0x000000, 0.5f);
		} else if ((health/(float)max) < 0.334f) {
			warning += Game.elapsed * 5f *(0.4f - (health/(float)max));
			warning %= 1f;
			avatar.tint(ColorMath.interpolate(warning, warningColors), 0.5f );
		} else if (talentBlink > 0.33f){ //stops early so it doesn't end in the middle of a blink
			talentBlink -= Game.elapsed;
			avatar.tint(1, 1, 0, (float)Math.abs(Math.cos(talentBlink*FLASH_RATE))/2f);
		} else {
			avatar.resetColor();
		}

		float healthPercent = Math.min(1f, health/(float)max);
		hp.scale.x = healthPercent;
		if (shield <= 0) {
			shieldPeak = 0;
		} else if (shield > shieldPeak) {
			shieldPeak = shield;
		}
		float shieldPercent = shieldPeak > 0 ? Math.min(1f, shield/(float)shieldPeak) : 0f;
		shieldSmall.scale.x = shieldSmallFullScale * shieldPercent;
		shieldLarge.scale.x = shieldLargeFullScale * shieldPercent;
		setShieldBarVisible(shield > 0);
		if (large) {
			buffs.setRect(x + 162, y + 2, 124, 36);
			positionLargeBusyIndicator(shield);
			counter.point(busy.center());
		} else {
			buffs.setRect(x + 30 + heroPaneExtraWidth + 1, y + (shield > 0 ? 14 : 8), 55, 16);
		}

		if (oldHP != health || oldShield != shield || oldMax != max){
			hpText.text(compactBarNumber(health) + "/" + compactBarNumber(max));
			shieldText.text(compactBarNumber(shield));
			oldHP = health;
			oldShield = shield;
			oldMax = max;
		}
		if (large && shieldVisibilityChanged) {
			GameScene.layoutTags();
		}

		if (large) {
			exp.scale.x = (128 / exp.width) * Dungeon.hero.exp / Dungeon.hero.maxExp();

			hpText.measure();
			hpText.x = hp.x + (128 - hpText.width())/2f;

			shieldText.measure();
			shieldText.x = shieldLarge.x + (128 - shieldText.width())/2f;

			expText.text(Dungeon.hero.exp + "/" + Dungeon.hero.maxExp());
			expText.measure();
			expText.x = hp.x + (128 - expText.width())/2f;

		} else {
			exp.scale.x = ((17 + heroPaneExtraWidth) / exp.width) * Dungeon.hero.exp / Dungeon.hero.maxExp();
			expText.text(Dungeon.hero.exp + "/" + Dungeon.hero.maxExp());
		}

		if (Dungeon.hero.lvl != lastLvl) {

			if (lastLvl != -1) {
				showStarParticles();
			}

			lastLvl = Dungeon.hero.lvl;

			if (large){
				level.text( "lv. " + lastLvl );
				level.measure();
				level.x = x + (30f - level.width()) / 2f;
				level.y = y + 33f - level.baseLine() / 2f;
			} else {
				level.text( Integer.toString( lastLvl ) );
				level.measure();
				level.x = x + heroPaneExtraWidth + 25.5f - level.width() / 2f;
				level.y = y + 31.0f - level.baseLine() / 2f;
			}
			PixelScene.align(level);
		}

		int tier = Dungeon.hero.tier();
		if (tier != lastTier) {
			lastTier = tier;
			avatar.copy( HeroSprite.avatar( Dungeon.hero ) );
		}

		counter.setSweep((1f - Actor.now()%1f)%1f);
	}

	public void updateAvatar(){
		avatar.copy( HeroSprite.avatar( Dungeon.hero ) );
	}

	public void alpha( float value ){
		value = GameMath.gate(0, value, 1f);
		bg.alpha(value);
		heroPaneCutout.alpha(value);
		hpCutout.alpha(value);
		avatar.alpha(value);
		hpSmallFrame.alpha(value);
		shieldSmall.alpha(value);
		shieldSmallFrame.alpha(value);
		shieldLarge.alpha(value);
		shieldLargeFrame.alpha(value);
		hp.alpha(value);
		hpText.alpha(0.6f*value);
		shieldText.alpha(0.75f*value);
		exp.alpha(value);
		if (expText != null) expText.alpha(0.6f*value);
		level.alpha(value);
		compass.alpha(value);
		busy.alpha(value);
		counter.alpha(value);
	}

	private void positionLargeBusyIndicator(int shield) {
		busy.x = x + 31;
		busy.y = y + (shield > 0 ? -4 : 8);
		PixelScene.align(busy);
	}

	private void setShieldBarVisible( boolean visible ){
		hpSmallFrame.visible = !large;
		shieldSmall.visible = visible && !large;
		shieldSmallFrame.visible = visible && !large;
		shieldLarge.visible = visible && large;
		shieldLargeFrame.visible = visible && large;
		shieldText.visible = visible;
	}

	public void showStarParticles(){
		Emitter emitter = (Emitter)recycle( Emitter.class );
		emitter.revive();
		emitter.pos( avatar.center() );
		emitter.burst( Speck.factory( Speck.STAR ), 12 );
	}

	public static String compactBarNumber( int amount ) {
		if (amount < 0) amount = 0;
		if (amount >= 1_000_000_000) {
			return Messages.decimalFormat("0.00", amount / 1_000_000_000f) + "b";
		} else if (amount >= 1_000_000) {
			return Messages.decimalFormat("0.00", amount / 1_000_000f) + "m";
		} else if (amount >= 1_000) {
			return Messages.decimalFormat("0.00", amount / 1_000f) + "k";
		}
		return Integer.toString(amount);
	}

}
