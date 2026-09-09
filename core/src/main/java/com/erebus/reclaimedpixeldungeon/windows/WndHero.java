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

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.SPDAction;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.Statistics;
import com.erebus.reclaimedpixeldungeon.actors.buffs.Buff;
import com.erebus.reclaimedpixeldungeon.actors.hero.Hero;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.RarityStat;
import com.erebus.reclaimedpixeldungeon.items.rings.Ring;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfAccuracy;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfArcana;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfElements;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEnergy;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfEvasion;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfForce;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfFuror;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfHaste;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfMight;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfSharpshooting;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfTenacity;
import com.erebus.reclaimedpixeldungeon.items.rings.RingOfWealth;
import com.erebus.reclaimedpixeldungeon.items.trinkets.Trinket;
import com.erebus.reclaimedpixeldungeon.items.weapon.SpiritBow;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.levels.WayfarerExchangeLevel;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerExchangeService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.ui.BuffIcon;
import com.erebus.reclaimedpixeldungeon.ui.BuffIndicator;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.StatusPane;
import com.erebus.reclaimedpixeldungeon.ui.TalentButton;
import com.erebus.reclaimedpixeldungeon.ui.TalentsPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.DungeonSeed;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Locale;

public class WndHero extends WndTabbed {
	
	private static final int WIDTH		= ReclaimedWindow.modalWidth( 120 );
	private static final int HEIGHT		= 120;
	
	private StatsTab stats;
	private TalentsTab talents;
	private BuffsTab buffs;
	private RarityStatsTab rarityStats;
	private ExchangeTab exchange;

	public static int lastIdx = 0;

	public WndHero() {
		
		super();
		
		resize( WIDTH, HEIGHT );
		
		stats = new StatsTab();
		add( stats );

		talents = new TalentsTab();
		add(talents);
		talents.setRect(0, 0, WIDTH, HEIGHT);

		buffs = new BuffsTab();
		add( buffs );
		buffs.setRect(0, 0, WIDTH, HEIGHT);
		buffs.setupList();

		rarityStats = new RarityStatsTab();
		add( rarityStats );
		rarityStats.setRect(0, 0, WIDTH, HEIGHT);
		rarityStats.setupList();

		exchange = new ExchangeTab();
		add( exchange );
		exchange.setRect(0, 0, WIDTH, HEIGHT);
		
		add( new IconTab( Icons.get(Icons.RANKINGS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 0;
					if (!stats.visible) {
						stats.initialize();
					}
				}
				stats.visible = stats.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.TALENT) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 1;
				if (selected) StatusPane.talentBlink = 0;
				talents.visible = talents.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.BUFFS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 2;
				buffs.visible = buffs.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.CATALOG) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 3;
				rarityStats.visible = rarityStats.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.DATA) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 4;
				exchange.visible = exchange.active = selected;
			}
		} );

		layoutTabs();

		talents.setRect(0, 0, WIDTH, HEIGHT);
		talents.pane.scrollTo(0, talents.pane.content().height() - talents.pane.height());
		talents.layout();

		select( lastIdx );
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.HERO_INFO) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		talents.layout();
		buffs.layout();
		rarityStats.layout();
		exchange.layout();
	}

	private class StatsTab extends Group {
		
		private static final int GAP = 6;
		
		private float pos;
		
		public StatsTab() {
			initialize();
		}

		public void initialize(){

			for (Gizmo g : members){
				if (g != null) g.destroy();
			}
			clear();
			
			Hero hero = Dungeon.hero;

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar(hero) );
			if (hero.name().equals(hero.className()))
				title.label( Messages.get(this, "title", hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ) );
			else
				title.label((hero.name() + "\n" + Messages.get(this, "title", hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH-16, 0 );
			add(title);

			IconButton infoButton = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					if (ShatteredPixelDungeon.scene() instanceof GameScene){
						GameScene.show(new WndHeroInfo(hero.heroClass));
					} else {
						ShatteredPixelDungeon.scene().addToFront(new WndHeroInfo(hero.heroClass));
					}
				}

				@Override
				protected String hoverText() {
					return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
				}

			};
			infoButton.setRect(title.right(), 0, 16, 16);
			add(infoButton);

			pos = title.bottom() + 2*GAP;

			int strBonus = hero.STR() - hero.STR;
			if (strBonus > 0)           statSlot( Messages.get(this, "str"), hero.STR + " + " + strBonus );
			else if (strBonus < 0)      statSlot( Messages.get(this, "str"), hero.STR + " - " + -strBonus );
			else                        statSlot( Messages.get(this, "str"), hero.STR() );
			statSlot( Messages.get(this, "health"), StatusPane.compactBarNumber( hero.HP ) + "/" + StatusPane.compactBarNumber( hero.HT ) );
			statSlot( Messages.get(this, "shield"), StatusPane.compactBarNumber( hero.shielding() ) );
			statSlot( Messages.get(this, "exp"), hero.exp + "/" + hero.maxExp() );

			pos += GAP;

			statSlot( Messages.get(this, "gold"), Statistics.goldCollected );
			statSlot( Messages.get(this, "depth"), Statistics.deepestFloor );
			if (Dungeon.daily){
				if (!Dungeon.dailyReplay) {
					statSlot(Messages.get(this, "daily_for"), "_" + Dungeon.customSeedText + "_");
				} else {
					statSlot(Messages.get(this, "replay_for"), "_" + Dungeon.customSeedText + "_");
				}
			} else if (!Dungeon.customSeedText.isEmpty()){
				statSlot( Messages.get(this, "custom_seed"), "_" + Dungeon.customSeedText + "_" );
			} else {
				statSlot( Messages.get(this, "dungeon_seed"), DungeonSeed.convertToCode(Dungeon.seed) );
			}

			pos += GAP;
		}

		private void statSlot( String label, String value ) {

			int size = 8;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock( label, size );
				size--;
			} while (txt.width() >= WIDTH * 0.55f);
			txt.setPos(0, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );

			size = 8;
			do {
				txt = PixelScene.renderTextBlock( value, size );
				size--;
			} while (txt.width() >= WIDTH * 0.45f);
			txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );
			
			pos += GAP + txt.height();
		}
		
		private void statSlot( String label, int value ) {
			statSlot( label, Integer.toString( value ) );
		}
		
		public float height() {
			return pos;
		}
	}

	public class TalentsTab extends Component {

		TalentsPane pane;

		@Override
		protected void createChildren() {
			super.createChildren();
			pane = new TalentsPane(TalentButton.Mode.UPGRADE);
			add(pane);
		}

		@Override
		protected void layout() {
			super.layout();
			pane.setRect(x, y, width, height);
		}

	}
	
	private class BuffsTab extends Component {
		
		private static final int GAP = 2;
		
		private float pos;
		private ScrollPane buffList;
		private ArrayList<BuffSlot> slots = new ArrayList<>();

		@Override
		protected void createChildren() {

			super.createChildren();

			buffList = new ScrollPane( new Component() ){
				@Override
				public void onClick( float x, float y ) {
					int size = slots.size();
					for (int i=0; i < size; i++) {
						if (slots.get( i ).onClick( x, y )) {
							break;
						}
					}
				}
			};
			add(buffList);
		}
		
		@Override
		protected void layout() {
			super.layout();
			buffList.setRect(0, 0, width, height);
		}
		
		private void setupList() {
			Component content = buffList.content();
			for (Buff buff : Dungeon.hero.buffs()) {
				if (buff.icon() != BuffIndicator.NONE) {
					BuffSlot slot = new BuffSlot(buff);
					slot.setRect(0, pos, WIDTH, slot.icon.height());
					content.add(slot);
					slots.add(slot);
					pos += GAP + slot.height();
				}
			}
			content.setSize(buffList.width(), pos);
			buffList.setSize(buffList.width(), buffList.height());
		}

		private class BuffSlot extends Component {

			private Buff buff;

			Image icon;
			RenderedTextBlock txt;

			public BuffSlot( Buff buff ){
				super();
				this.buff = buff;

				icon = new BuffIcon(buff, true);
				icon.y = this.y;
				add( icon );

				txt = PixelScene.renderTextBlock( Messages.titleCase(buff.name()), 8 );
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
				add( txt );

			}

			@Override
			protected void layout() {
				super.layout();
				icon.y = this.y;
				txt.maxWidth((int)(width - icon.width()));
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
			}
			
			protected boolean onClick ( float x, float y ) {
				if (inside( x, y )) {
					GameScene.show(new WndInfoBuff(buff));
					return true;
				} else {
					return false;
				}
			}
		}
	}

	private class ExchangeTab extends Component {

		private static final int GAP = 4;

		private ScrollPane pane;
		private Component content;
		private float pos;

		@Override
		protected void createChildren() {
			super.createChildren();
			content = new Component();
			pane = new ScrollPane( content );
			add( pane );
		}

		@Override
		protected void layout() {
			super.layout();
			pane.setRect( 0, 0, width, height );
			rebuild();
		}

		private void rebuild() {
			if (content == null || pane == null) return;
			content.clear();
			pos = 3;

			RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.maxWidth( WIDTH - 4 );
			title.setPos( (WIDTH - title.width()) / 2f, pos );
			PixelScene.align( title );
			content.add( title );
			pos = title.bottom() + GAP;

			RenderedTextBlock body = PixelScene.renderTextBlock(
					Dungeon.homebase != null && Dungeon.homebase.wayfarerExchangeUnlocked()
							? Messages.get( this, "ready" )
							: Messages.get( this, "locked" ),
					6 );
			body.maxWidth( WIDTH - 6 );
			body.setPos( 3, pos );
			content.add( body );
			pos = body.bottom() + GAP;

			if (Dungeon.level instanceof WayfarerExchangeLevel) {
				WayfarerExchangeLevel exchangeLevel = (WayfarerExchangeLevel)Dungeon.level;
				RenderedTextBlock status = PixelScene.renderTextBlock( WayfarerExchangeService.status(), 6 );
				status.maxWidth( WIDTH - 6 );
				status.hardlight( Window.SHPX_COLOR );
				status.setPos( 3, pos );
				content.add( status );
				pos = status.bottom() + GAP;

				addButton( Messages.get( this, "open_trade" ), new Runnable() {
					@Override
					public void run() {
						hide();
						GameScene.show( new WndWayfarerExchange( exchangeLevel.hostSide(), false ) );
					}
				} );

				addButton( Messages.get( this, exchangeLevel.hostSide() ? "close_exchange" : "leave_exchange" ), new Runnable() {
					@Override
					public void run() {
						boolean notifyPeer = Dungeon.level instanceof WayfarerExchangeLevel
								&& ((WayfarerExchangeLevel)Dungeon.level).hostSide();
						hide();
						WayfarerExchangeService.closeExchange( notifyPeer );
						WayfarerExchangeLevel.returnHomebase();
					}
				} );
				addDivider();
				addAccountButton();
			} else if (Dungeon.homebase != null && Dungeon.homebase.wayfarerExchangeUnlocked()) {
				addButton( Messages.get( this, "host" ), new Runnable() {
					@Override
					public void run() {
						if (canOpenExchange()) {
							hide();
							WndWayfarerExchange.startTradeAndEnter( true );
						}
					}
				} );
				addButton( Messages.get( this, "join" ), new Runnable() {
					@Override
					public void run() {
						if (canOpenExchange()) {
							GameScene.show( new WndWayfarerExchange( false ) );
						}
					}
				} );
				addDivider();
				addAccountButton();
			} else {
				RenderedTextBlock cost = PixelScene.renderTextBlock(
						Messages.get( this, "cost",
								HomebaseState.WAYFARER_EXCHANGE_GOLD_COST,
								HomebaseState.WAYFARER_EXCHANGE_EMBER_CORE_COST ),
						6 );
				cost.maxWidth( WIDTH - 6 );
				cost.hardlight( Window.SHPX_COLOR );
				cost.setPos( 3, pos );
				content.add( cost );
				pos = cost.bottom() + GAP;

				addButton( Messages.get( this, "unlock" ), new Runnable() {
					@Override
					public void run() {
						if (Dungeon.homebase != null && Dungeon.homebase.unlockWayfarerExchange()) {
							GLog.p( Messages.get( ExchangeTab.this, "unlock_success" ) );
							rebuild();
						} else {
							GLog.w( Messages.get( ExchangeTab.this, "unlock_missing" ) );
						}
					}
				} );
			}

			content.setSize( pane.width(), Math.max( pane.height(), pos + GAP ) );
			pane.setSize( pane.width(), pane.height() );
		}

		private void addAccountButton() {
			addButton( Messages.get( this, "account" ), new Runnable() {
				@Override
				public void run() {
					if (WayfarerAccountService.isSignedIn()) {
						GameScene.show( new WndWayfarerAccount() );
					} else {
						WndSettings.showWayfarerAccount();
					}
				}
			} );
		}

		private void addDivider() {
			ColorBlock divider = new ColorBlock( WIDTH - 6, 1, 0xFF222222 );
			divider.x = 3;
			divider.y = pos + 1;
			content.add( divider );
			pos = divider.y + divider.height() + GAP + 1;
		}

		private void addButton( String label, final Runnable action ) {
			RedButton button = new RedButton( label, 8 ) {
				@Override
				protected void onClick() {
					super.onClick();
					action.run();
				}
			};
			button.setRect( 3, pos, WIDTH - 6, 18 );
			content.add( button );
			pos = button.bottom() + GAP;
		}

		private boolean canOpenExchange() {
			if (Dungeon.depth == 0 && Dungeon.level instanceof HomebaseLevel) {
				return true;
			}
			GLog.w( Messages.get( this, "homebase_only" ) );
			return false;
		}
	}

	private class RarityStatsTab extends Component {

		private static final int GAP = 2;

		private float pos;
		private ScrollPane statList;

		@Override
		protected void createChildren() {
			super.createChildren();
			statList = new ScrollPane( new Component() );
			add( statList );
		}

		@Override
		protected void layout() {
			super.layout();
			statList.setRect( 0, 0, width, height );
		}

		private void setupList() {
			Component content = statList.content();
			content.clear();
			pos = 3;

			RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (WIDTH - title.width()) / 2f, pos );
			PixelScene.align( title );
			content.add( title );
			pos = title.bottom() + 2 * GAP;

			ArrayList<AggregatedRarityStat> stats = aggregateRarityStats();
			if (stats.isEmpty()) {
				RenderedTextBlock none = PixelScene.renderTextBlock( Messages.get( this, "none" ), 6 );
				none.maxWidth( WIDTH - 4 );
				none.hardlight( 0xAAAAAA );
				none.setPos( 2, pos );
				content.add( none );
				pos = none.bottom();
			} else {
				for (AggregatedRarityStat stat : stats) {
					addStatLine( content, stat );
				}
			}

			content.setSize( statList.width(), Math.max( statList.height(), pos + GAP ) );
			statList.setSize( statList.width(), statList.height() );
		}

		private ArrayList<AggregatedRarityStat> aggregateRarityStats() {
			EnumMap<RarityStat.Type, AggregatedRarityStat> totals = new EnumMap<>( RarityStat.Type.class );
			ArrayList<AggregatedRarityStat> homebaseOnly = new ArrayList<>();
			ArrayList<Item> sources = new ArrayList<>();
			Hero hero = Dungeon.hero;

			addStatsFromItem( hero.belongings.weapon(), sources, totals );
			addStatsFromItem( hero.belongings.armor(), sources, totals );
			for (Item item : hero.belongings.equippedMiscItems()) {
				addStatsFromItem( item, sources, totals );
			}
			addStatsFromItem( hero.belongings.secondWep(), sources, totals );
			addStatsFromItem( hero.belongings.getItem( SpiritBow.class ), sources, totals );

			for (Item item : hero.belongings) {
				if (item instanceof Trinket) {
					addStatsFromItem( item, sources, totals );
				}
			}

			addHomebaseTrainingStats( totals, homebaseOnly );
			addRingEffectStats( hero, totals, homebaseOnly );

			ArrayList<AggregatedRarityStat> stats = new ArrayList<>( totals.values() );
			stats.addAll( homebaseOnly );
			Collections.sort( stats, new Comparator<AggregatedRarityStat>() {
				@Override
				public int compare( AggregatedRarityStat a, AggregatedRarityStat b ) {
					int rarity = a.rarityOrder - b.rarityOrder;
					if (rarity != 0) return rarity;
					return a.name().compareTo( b.name() );
				}
			} );
			return stats;
		}

		private void addStatsFromItem( Item item, ArrayList<Item> sources, EnumMap<RarityStat.Type, AggregatedRarityStat> totals ) {
			if (item == null || sources.contains( item )) return;
			sources.add( item );

			for (RarityStat stat : item.visibleRarityStats()) {
				AggregatedRarityStat total = totals.get( stat.type() );
				if (total == null) {
					total = new AggregatedRarityStat( stat.type() );
					totals.put( stat.type(), total );
				}
				total.add( stat );
			}
		}

		private void addRingEffectStats( Hero hero, EnumMap<RarityStat.Type, AggregatedRarityStat> totals, ArrayList<AggregatedRarityStat> customStats ) {
			ArrayList<Class<? extends Ring>> handledRings = new ArrayList<>();
			for (Ring ring : hero.belongings.equippedRings()) {
				if (ring == null || !ring.isIdentified() || handledRings.contains( ring.getClass() )) continue;
				handledRings.add( ring.getClass() );

				if (ring instanceof RingOfAccuracy) {
					addStatValue( totals, RarityStat.Type.ATTACK_ACCURACY, ringPercentBonus( 1.3f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfArcana) {
					addCustomStat( customStats, "Enchant Power", true, RarityStat.Type.MAGIC_BONUS.displayColor(), RarityStat.Type.MAGIC_BONUS.minimumRarity().ordinal(), ringPercentBonus( 1.175f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfElements) {
					addCustomStat( customStats, "Elemental Resistance", true, RarityStat.Type.FIRE_RESISTANCE.displayColor(), RarityStat.Type.FIRE_RESISTANCE.minimumRarity().ordinal(), ringPercentReduction( 0.825f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfEnergy) {
					float bonus = ringPercentBonus( 1.175f, ring.combinedBuffedBonus( hero ) );
					addStatValue( totals, RarityStat.Type.WAND_RECHARGE_RATE, bonus );
					addCustomStat( customStats, "Artifact Recharge", true, RarityStat.Type.WAND_RECHARGE_RATE.displayColor(), RarityStat.Type.WAND_RECHARGE_RATE.minimumRarity().ordinal(), bonus );
					addCustomStat( customStats, "Armor Ability Charge", true, RarityStat.Type.WAND_RECHARGE_RATE.displayColor(), RarityStat.Type.WAND_RECHARGE_RATE.minimumRarity().ordinal(), bonus );
				} else if (ring instanceof RingOfEvasion) {
					addCustomStat( customStats, "Evasion Multiplier", true, RarityStat.Type.EVASION.displayColor(), RarityStat.Type.EVASION.minimumRarity().ordinal(), ringPercentBonus( 1.125f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfForce) {
					addCustomStat( customStats, "Force Level", false, RarityStat.Type.ATTACK_DAMAGE.displayColor(), RarityStat.Type.ATTACK_DAMAGE.minimumRarity().ordinal(), ring.combinedBuffedBonus( hero ) );
				} else if (ring instanceof RingOfFuror) {
					addStatValue( totals, RarityStat.Type.ATTACK_SPEED, ringPercentBonus( 1.09051f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfHaste) {
					addStatValue( totals, RarityStat.Type.MOVEMENT_SPEED, ringPercentBonus( 1.175f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfMight) {
					addCustomStat( customStats, "Strength", false, RarityStat.Type.MAX_HEALTH.displayColor(), RarityStat.Type.MAX_HEALTH.minimumRarity().ordinal(), RingOfMight.strengthBonus( hero ) );
					addCustomStat( customStats, "Max Health Multiplier", true, RarityStat.Type.MAX_HEALTH.displayColor(), RarityStat.Type.MAX_HEALTH.minimumRarity().ordinal(), ringPercentBonus( 1.035f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfSharpshooting) {
					addCustomStat( customStats, "Missile Level", false, RarityStat.Type.ATTACK_DAMAGE.displayColor(), RarityStat.Type.ATTACK_DAMAGE.minimumRarity().ordinal(), ring.combinedBuffedBonus( hero ) );
					addCustomStat( customStats, "Thrown Durability", true, RarityStat.Type.THROWN_DURABILITY.displayColor(), RarityStat.Type.THROWN_DURABILITY.minimumRarity().ordinal(), ringPercentBonus( 1.2f, ring.combinedBonus( hero ) ) );
				} else if (ring instanceof RingOfTenacity) {
					addCustomStat( customStats, "Tenacity", true, RarityStat.Type.FIRE_RESISTANCE.displayColor(), RarityStat.Type.FIRE_RESISTANCE.minimumRarity().ordinal(), ringPercentReduction( 0.85f, ring.combinedBuffedBonus( hero ) ) );
				} else if (ring instanceof RingOfWealth) {
					addCustomStat( customStats, "Ring Wealth", true, RarityStat.Type.TREASURE_LUCK.displayColor(), RarityStat.Type.TREASURE_LUCK.minimumRarity().ordinal(), ringPercentBonus( 1.2f, ring.combinedBuffedBonus( hero ) ) );
				}
			}
		}

		private void addStatValue( EnumMap<RarityStat.Type, AggregatedRarityStat> totals, RarityStat.Type type, float value ) {
			AggregatedRarityStat total = totals.get( type );
			if (total == null) {
				total = new AggregatedRarityStat( type );
				totals.put( type, total );
			}
			total.addValue( value );
		}

		private void addCustomStat( ArrayList<AggregatedRarityStat> stats, String label, boolean percent, int color, int rarityOrder, float value ) {
			if (Math.abs( value ) < 0.005f) return;
			stats.add( new AggregatedRarityStat( label, percent, color, rarityOrder, value ) );
		}

		private float ringPercentBonus( float base, int level ) {
			return 100f * ((float)Math.pow( base, level ) - 1f);
		}

		private float ringPercentReduction( float base, int level ) {
			return 100f * (1f - (float)Math.pow( base, level ));
		}

		private void addHomebaseTrainingStats( EnumMap<RarityStat.Type, AggregatedRarityStat> totals, ArrayList<AggregatedRarityStat> homebaseOnly ) {
			if (Dungeon.homebase == null) return;

			for (HomebaseState.Training training : HomebaseState.Training.values()) {
				int bonus = Dungeon.homebase.trainingBonus( training );
				if (bonus <= 0) continue;

				RarityStat.Type type = homebaseStatType( training );
				if (type != null) {
					AggregatedRarityStat total = totals.get( type );
					if (total == null) {
						total = new AggregatedRarityStat( type );
						totals.put( type, total );
					}
					total.addValue( bonus );
				} else {
					homebaseOnly.add( new AggregatedRarityStat(
							training.label(),
							homebaseTrainingPercent( training ),
							homebaseTrainingColor( training ),
							homebaseTrainingOrder( training ),
							bonus ) );
				}
			}
		}

		private RarityStat.Type homebaseStatType( HomebaseState.Training training ) {
			switch (training) {
				case HEALTH: return RarityStat.Type.MAX_HEALTH;
				case ARMOR: return RarityStat.Type.DEFENSE;
				case EVASION: return RarityStat.Type.EVASION;
				case WAND_RECHARGE: return RarityStat.Type.WAND_RECHARGE_RATE;
				case MOVEMENT_SPEED: return RarityStat.Type.MOVEMENT_SPEED;
				case TREASURE_LUCK: return RarityStat.Type.TREASURE_LUCK;
				case ATTACK_DAMAGE: return RarityStat.Type.ATTACK_DAMAGE;
				case ATTACK_SPEED: return RarityStat.Type.ATTACK_SPEED;
				case WAND_CHARGES: return RarityStat.Type.WAND_CHARGES;
				case MAGIC_DAMAGE: return RarityStat.Type.MAGIC_DAMAGE;
				case MAGIC_POWER: return RarityStat.Type.MAGIC_BONUS;
				case TRINKET_POTENCY: return RarityStat.Type.TRINKET_POTENCY;
				case XP_GAIN: return RarityStat.Type.XP_GAIN;
				case RING_POTENCY: return RarityStat.Type.RING_POTENCY;
				case ARTIFACT_POTENCY: return RarityStat.Type.ARTIFACT_POTENCY;
				case CRITICAL_CHANCE: return RarityStat.Type.CRITICAL_CHANCE;
				case CRITICAL_DAMAGE: return RarityStat.Type.CRITICAL_DAMAGE_MULTIPLIER;
				case LIFESTEAL: return RarityStat.Type.LIFESTEAL;
				case DODGE_CHANCE: return RarityStat.Type.DODGE_CHANCE;
				case BLOCK_CHANCE: return RarityStat.Type.BLOCK_CHANCE;
				case BONUS_LOOT: return RarityStat.Type.BONUS_LOOT;
				case ARMOR_BONUS: return RarityStat.Type.ARMOR_BONUS;
				case THORNS_CHANCE: return RarityStat.Type.THORNS_CHANCE;
				case THORNS_DAMAGE: return RarityStat.Type.THORNS_DAMAGE;
				case KNOCKBACK_CHANCE: return RarityStat.Type.KNOCKBACK_CHANCE;
				case KNOCKBACK_STRENGTH: return RarityStat.Type.KNOCKBACK_STRENGTH;
				case CLEAVE_CHANCE: return RarityStat.Type.CLEAVE_CHANCE;
				case PIERCING_CHANCE: return RarityStat.Type.PIERCING_CHANCE;
				case LIGHTNING_CHANCE: return RarityStat.Type.SUMMON_LIGHTNING_CHANCE;
				case BLEED_PROC: return RarityStat.Type.BLEED_PROC;
				case BLEED_DURATION: return RarityStat.Type.BLEED_DURATION;
				case STUN_CHANCE: return RarityStat.Type.STUN_CHANCE;
				case STUN_DURATION: return RarityStat.Type.STUN_DURATION;
				case BURNING_PROC: return RarityStat.Type.BURNING_PROC;
				case BURNING_DURATION: return RarityStat.Type.BURNING_DURATION;
				case CORROSION_PROC: return RarityStat.Type.CORROSION_PROC;
				case CORROSION_DURATION: return RarityStat.Type.CORROSION_DURATION;
				case HEX_PROC: return RarityStat.Type.HEX_PROC;
				case HEX_DURATION: return RarityStat.Type.HEX_DURATION;
				case FROST_PROC: return RarityStat.Type.FROST_PROC;
				case FROST_DURATION: return RarityStat.Type.FROST_DURATION;
				case POISON_PROC: return RarityStat.Type.POISON_PROC;
				case POISON_DURATION: return RarityStat.Type.POISON_DURATION;
				case ROOT_PROC: return RarityStat.Type.ROOT_PROC;
				case ROOT_DURATION: return RarityStat.Type.ROOT_DURATION;
				case SLOW_PROC: return RarityStat.Type.SLOW_PROC;
				case SLOW_DURATION: return RarityStat.Type.SLOW_DURATION;
				case VERTIGO_PROC: return RarityStat.Type.VERTIGO_PROC;
				case VERTIGO_DURATION: return RarityStat.Type.VERTIGO_DURATION;
				case BLINDNESS_PROC: return RarityStat.Type.BLINDNESS_PROC;
				case BLINDNESS_DURATION: return RarityStat.Type.BLINDNESS_DURATION;
				case CRIPPLE_PROC: return RarityStat.Type.CRIPPLE_PROC;
				case CRIPPLE_DURATION: return RarityStat.Type.CRIPPLE_DURATION;
				case DAZE_PROC: return RarityStat.Type.DAZE_PROC;
				case DAZE_DURATION: return RarityStat.Type.DAZE_DURATION;
				case VULNERABLE_PROC: return RarityStat.Type.VULNERABLE_PROC;
				case VULNERABLE_DURATION: return RarityStat.Type.VULNERABLE_DURATION;
				case WEAKNESS_PROC: return RarityStat.Type.WEAKNESS_PROC;
				case WEAKNESS_DURATION: return RarityStat.Type.WEAKNESS_DURATION;
				case BARRIER_GUARD: return RarityStat.Type.BARRIER_PROC;
				case BARRIER_POWER: return RarityStat.Type.BARRIER_POWER;
				case FIRE_RESISTANCE: return RarityStat.Type.FIRE_RESISTANCE;
				case FROST_RESISTANCE: return RarityStat.Type.FROST_RESISTANCE;
				case POISON_RESISTANCE: return RarityStat.Type.POISON_RESISTANCE;
				case CORROSION_RESISTANCE: return RarityStat.Type.CORROSION_RESISTANCE;
				case BLEED_RESISTANCE: return RarityStat.Type.BLEED_RESISTANCE;
				case BLINDNESS_RESISTANCE: return RarityStat.Type.BLINDNESS_RESISTANCE;
				case CRIPPLE_RESISTANCE: return RarityStat.Type.CRIPPLE_RESISTANCE;
				case DAZE_RESISTANCE: return RarityStat.Type.DAZE_RESISTANCE;
				case HEX_RESISTANCE: return RarityStat.Type.HEX_RESISTANCE;
				case ROOT_RESISTANCE: return RarityStat.Type.ROOT_RESISTANCE;
				case SLOW_RESISTANCE: return RarityStat.Type.SLOW_RESISTANCE;
				case VERTIGO_RESISTANCE: return RarityStat.Type.VERTIGO_RESISTANCE;
				case VULNERABLE_RESISTANCE: return RarityStat.Type.VULNERABLE_RESISTANCE;
				case STUN_RESISTANCE: return RarityStat.Type.STUN_RESISTANCE;
				case WEAKNESS_RESISTANCE: return RarityStat.Type.WEAKNESS_RESISTANCE;
				default: return null;
			}
		}

		private boolean homebaseTrainingPercent( HomebaseState.Training training ) {
			switch (training) {
				case STRENGTH:
				case ACCURACY:
				case TALENT_POINT:
				case TALENT_TIER_2:
				case TALENT_TIER_3:
				case TALENT_TIER_4:
				case RANGED_DAMAGE:
				case BARRIER_POWER:
					return false;
				default:
					return true;
			}
		}

		private int homebaseTrainingColor( HomebaseState.Training training ) {
			switch (training) {
				case STRENGTH:
				case ACCURACY:
				case TALENT_POINT:
				case TALENT_TIER_2:
				case TALENT_TIER_3:
				case TALENT_TIER_4:
				case RANGED_DAMAGE:
					return RarityStat.Type.MAX_HEALTH.displayColor();
				case ARMOR_ABILITY_CHARGE:
				case WAND_DAMAGE:
				case ENCHANTMENT_POWER:
				case ELEMENTAL_RESISTANCE:
				case THROWN_DURABILITY:
				case TENACITY:
				case GOLD_GAIN:
				case RESOURCE_YIELD:
					return RarityStat.Type.TREASURE_LUCK.displayColor();
				case CATALYST_DROP_RATE:
				case STATUS_PROC_CHANCE:
				case STATUS_DURATION:
				case ARTIFACT_RECHARGE:
					return RarityStat.Type.CRITICAL_CHANCE.displayColor();
				default:
					return RarityStat.Type.HEX_RESISTANCE.displayColor();
			}
		}

		private int homebaseTrainingOrder( HomebaseState.Training training ) {
			switch (training) {
				case STRENGTH:
				case ACCURACY:
				case TALENT_POINT:
				case TALENT_TIER_2:
				case TALENT_TIER_3:
				case TALENT_TIER_4:
				case RANGED_DAMAGE:
					return RarityStat.Type.MAX_HEALTH.minimumRarity().ordinal();
				case ARMOR_ABILITY_CHARGE:
				case WAND_DAMAGE:
				case ENCHANTMENT_POWER:
				case ELEMENTAL_RESISTANCE:
				case THROWN_DURABILITY:
				case TENACITY:
				case GOLD_GAIN:
				case RESOURCE_YIELD:
					return RarityStat.Type.TREASURE_LUCK.minimumRarity().ordinal();
				case CATALYST_DROP_RATE:
				case STATUS_PROC_CHANCE:
				case STATUS_DURATION:
				case ARTIFACT_RECHARGE:
					return RarityStat.Type.CRITICAL_CHANCE.minimumRarity().ordinal();
				default:
					return RarityStat.Type.HEX_RESISTANCE.minimumRarity().ordinal();
			}
		}

		private void addStatLine( Component content, AggregatedRarityStat stat ) {
			RenderedTextBlock line = PixelScene.renderTextBlock( stat.displayText(), 6 );
			line.maxWidth( WIDTH - 4 );
			line.hardlight( stat.color );
			line.setPos( 2, pos );
			content.add( line );
			pos = line.bottom() + GAP;
		}
	}

	private static class AggregatedRarityStat {

		private final String label;
		private final boolean percent;
		private final boolean hasValue;
		private final int color;
		private final int rarityOrder;
		private float value;
		private int count;

		private AggregatedRarityStat( RarityStat.Type type ) {
			this( type.displayName(), type.percent(), type.hasValue(), type.displayColor(), type.minimumRarity().ordinal(), 0 );
		}

		private AggregatedRarityStat( String label, boolean percent, int color, int rarityOrder, float value ) {
			this( label, percent, true, color, rarityOrder, value );
		}

		private AggregatedRarityStat( String label, boolean percent, boolean hasValue, int color, int rarityOrder, float value ) {
			this.label = label;
			this.percent = percent;
			this.hasValue = hasValue;
			this.color = color;
			this.rarityOrder = rarityOrder;
			if (value != 0) addValue( value );
		}

		private void add( RarityStat stat ) {
			addValue( stat.value() );
		}

		private void addValue( float value ) {
			count++;
			if (hasValue) this.value += value;
		}

		private String name() {
			return label;
		}

		private String displayText() {
			if (!hasValue) {
				return count > 1 ? label + " x" + count : label;
			}
			String valueText = Math.abs( value - Math.round( value ) ) < 0.005f
					? Integer.toString( Math.round( value ) )
					: Messages.decimalFormat( "#.##", value );
			return (value > 0 ? "+" : "") + valueText + (percent ? "% " : " ") + label;
		}
	}
}
