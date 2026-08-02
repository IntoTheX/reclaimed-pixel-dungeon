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

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.Belongings;
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.levels.HomebaseLevel;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.scenes.AlchemyScene;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.HomebaseFacilityScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.plants.Plant;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class WndHomebaseFacility extends WndTabbed {

	private static final int WIDTH_DESKTOP = 152;
	private static final int HEIGHT = 160;
	private static final int GAP = 3;
	private static final int CONTENT_TOP_PAD = 6;
	private static final int CONTENT_SIDE_PAD = 3;
	private static final int PORTRAIT_VIEWPORT_TOP_PAD = 12;
	private static final int PORTRAIT_VIEWPORT_BOTTOM_PAD = 0;
	private static final int SECTION_PAD = 2;
	private static final int SECTION_BG_A = 0x252922;
	private static final int SECTION_BG_B = 0x3B4035;
	private static final int SECTION_DIVIDER = 0xFF000000;
	private static final int TAB_CONTENT_BOTTOM_PAD = 8;
	private static final int BTN_HEIGHT = 18;
	private static final int SLOT_SIZE = 24;
	private static final int SLOT_MARGIN = 1;
	private static final int VAULT_COLS = 6;
	private static final int STAT_BUTTON_WIDTH = 32;
	private static final int STAT_BUTTON_HEIGHT = 28;
	private static final int GARDEN_COLS = 6;
	private static final int GARDEN_PLOT_SIZE = 22;
	private static final int GARDEN_PLOT_MARGIN = 2;
	private static final int TAB_FUNCTION = 0;
	private static final int TAB_UPGRADE = 1;
	private static final int TAB_STATS = 2;
	private static final int TAB_COUNT = 3;
	private static final int[] rememberedTabs = new int[HomebaseState.Building.values().length];
	private static final HomebaseState.Training[] rememberedTrainings = new HomebaseState.Training[HomebaseState.Building.values().length];
	private static final HomebaseState.BuildingDefense[] rememberedBuildingDefenses = new HomebaseState.BuildingDefense[HomebaseState.Building.values().length];
	private static final float[][] rememberedScrollY = new float[HomebaseState.Building.values().length][TAB_COUNT];
	private static int rememberedSettlementRequest = -1;

	private final HomebaseState.Building building;
	private FacilityTab function;
	private FacilityTab upgrade;
	private FacilityTab stats;
	private HomebaseState.Training selectedTraining;
	private HomebaseState.BuildingDefense selectedBuildingDefense;
	private int selectedSettlementRequest;
	private final int windowWidth;
	private final int contentWidth;
	private final int windowHeight;
	private final int contentTop;
	private final int contentHeight;

	public WndHomebaseFacility( HomebaseState.Building building ) {
		super();
		this.building = building;
		windowWidth = facilityWindowWidth();
		contentWidth = Math.max( 1, windowWidth - CONTENT_SIDE_PAD * 2 );
		selectedTraining = rememberedTrainings[building.ordinal()];
		selectedBuildingDefense = rememberedBuildingDefenses[building.ordinal()];
		selectedSettlementRequest = rememberedSettlementRequest;

		windowHeight = ReclaimedWindow.modalHeight( preferredWindowHeight(), chrome.marginTop() + tabHeight() );
		contentTop = portraitMobile() ? PORTRAIT_VIEWPORT_TOP_PAD : 0;
		int contentBottom = portraitMobile() ? PORTRAIT_VIEWPORT_BOTTOM_PAD : 0;
		contentHeight = Math.max( 1, windowHeight - contentTop - contentBottom );
		resize( windowWidth, windowHeight );

		function = new FacilityTab( TAB_FUNCTION );
		add( function );
		function.setRect( CONTENT_SIDE_PAD, contentTop, contentWidth, contentHeight );
		function.rebuild( functionContent() );

		upgrade = new FacilityTab( TAB_UPGRADE );
		add( upgrade );
		upgrade.setRect( CONTENT_SIDE_PAD, contentTop, contentWidth, contentHeight );
		upgrade.rebuild( upgradeContent() );

		stats = new FacilityTab( TAB_STATS );
		add( stats );
		stats.setRect( CONTENT_SIDE_PAD, contentTop, contentWidth, contentHeight );
		stats.rebuild( statsContent() );

		add( new LabeledTab( functionLabel() ) {
			@Override
			protected void select( boolean value ) {
				super.select( value );
				function.visible = function.active = selected;
			}
		} );
		add( new LabeledTab( Messages.get( this, "tab_upgrade" ) ) {
			@Override
			protected void select( boolean value ) {
				super.select( value );
				upgrade.visible = upgrade.active = selected;
			}
		} );
		add( new LabeledTab( Messages.get( this, "tab_stats" ) ) {
			@Override
			protected void select( boolean value ) {
				super.select( value );
				stats.visible = stats.active = selected;
			}
		} );

		layoutTabs();
		select( rememberedTabs[building.ordinal()] );
		offset( 0, ReclaimedWindow.modalYOffset( windowHeight, chrome.marginTop() + tabHeight() ) );
	}

	private int preferredWindowHeight() {
		return portraitMobile() ? Integer.MAX_VALUE : HEIGHT;
	}

	private int facilityWindowWidth() {
		int width = ReclaimedWindow.modalWidth( WIDTH_DESKTOP );
		if (portraitMobile() && PixelScene.uiCamera != null) {
			int available = PixelScene.uiCamera.width - chrome.marginHor() - 2;
			if (available > 0) {
				width = Math.min( width, available );
			}
		}
		return Math.max( 96, width );
	}

	private boolean portraitMobile() {
		return !ReclaimedWindow.isDesktop() && !PixelScene.landscape();
	}

	@Override
	public void select( Tab tab ) {
		super.select( tab );
		rememberSelectedTab();
	}

	@Override
	protected boolean blocksInput() {
		return false;
	}

	@Override
	protected boolean handlesBackButton() {
		return false;
	}

	private FacilityContent functionContent() {
		FacilityContent content = new FacilityContent();
		if (Dungeon.homebase == null) {
			content.addText( Messages.get( this, "no_homebase" ), Window.WHITE );
			return content.finish();
		}

		if (!Dungeon.homebase.isBuilt( building )) {
			content.addText( Messages.get( this, "function_locked" ), Window.WHITE );
			return content.finish();
		}

		switch (building) {
			case VAULT:
				buildVaultContent( content );
				break;
			case FORGE:
				buildForgeContent( content );
				break;
			case ALCHEMY:
				buildAlchemyContent( content );
				break;
			case GARDEN:
				buildGardenContent( content );
				break;
			case CAMP:
				buildCampContent( content );
				break;
			default:
				buildDefenseStructureContent( content );
				break;
		}
		return content.finish();
	}

	private void buildVaultContent( FacilityContent content ) {
		int capacity = Dungeon.homebase.vaultSlots();
		int count = Dungeon.homebase.vaultItems().size();
		content.beginSection();
		content.addText( Messages.get( WndVaultStorage.class, "status", count, capacity ), Window.TITLE_COLOR );

		RedButton deposit = new RedButton( Messages.get( WndVaultStorage.class, "deposit" ), 6 ) {
			@Override
			protected void onClick() {
				hide();
				selectItem( depositSelector );
			}
		};
		deposit.enable( capacity > 0 );
		content.addButton( deposit );
		content.endSection();

		content.beginSection();
		ArrayList<Item> items = Dungeon.homebase.vaultItems();
		int slots = Math.max( vaultCols(), capacity );
		for (int i = 0; i < slots; i++) {
			final Item item = i < items.size() ? items.get( i ) : null;
			InventorySlot slot = new InventorySlot( item ) {
				@Override
				protected void onClick() {
					if (item != null) {
						withdraw( item );
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item != null) {
						show( new WndInfoItem( item ) );
						return true;
					}
					return false;
				}
			};
			if (item == null) slot.enable( false );
			content.addSlot( slot, i );
		}
		content.endSlots( slots );
		content.endSection();
	}

	private void buildForgeContent( FacilityContent content ) {
		content.beginSection();
		content.addText( Messages.get( this, "forge_limit", Dungeon.homebase.maxForgeUpgradeLevel() ), Window.TITLE_COLOR );
		content.addForgeActionGrid();
		content.endSection();
	}

	private void buildAlchemyContent( FacilityContent content ) {
		content.beginSection();
		content.addText( Messages.get( this, "alchemy_desc" ), Window.WHITE );
		RedButton alchemy = new RedButton( Messages.get( this, "open_alchemy" ), 6 ) {
			@Override
			protected void onClick() {
				AlchemyScene.clearToolkit();
				ShatteredPixelDungeon.switchScene( AlchemyScene.class );
			}
		};
		alchemy.enable( Dungeon.homebase.isBuilt( HomebaseState.Building.ALCHEMY ) );
		content.addButton( alchemy );
		content.endSection();
	}

	private void buildGardenContent( FacilityContent content ) {
		content.beginSection();
		content.addText( Messages.get( WndMoonrootGarden.class, "status",
				Dungeon.homebase.buildingLevel( HomebaseState.Building.GARDEN ),
				Dungeon.homebase.moonrootReady(),
				Dungeon.homebase.moonrootPlots() ), Window.TITLE_COLOR );
		content.addText( Messages.get( WndMoonrootGarden.class, "desc" ), Window.WHITE );

		RedButton harvest = new RedButton( Messages.get( WndMoonrootGarden.class, "harvest" ), 6 ) {
			@Override
			protected void onClick() {
				WndMoonrootGarden.harvestGarden();
				reopen();
			}
		};
		harvest.enable( Dungeon.homebase.moonrootReady() > 0 );
		content.addButton( harvest );
		content.endSection();

		content.beginSection();
		content.addGardenGrid();
		content.endSection();
	}

	private void buildCampContent( FacilityContent content ) {
		content.beginSection();
		content.addText( Messages.get( this, "camp_desc" ), Window.WHITE );

		RedButton manageDefenders = new RedButton( Messages.get( this, "camp_manage_defenders" ), 6 ) {
			@Override
			protected void onClick() {
				show( new WndDefenderManagement() );
			}
		};
		manageDefenders.enable( Dungeon.homebase != null && !Dungeon.homebase.raidActive() );
		content.addButton( manageDefenders );
		content.endSection();

		ArrayList<HomebaseState.SettlementRequest> requests = Dungeon.homebase.settlementRequests();
		content.beginSection();
		content.addText( Messages.get( this, "camp_requests", requests.size(), Dungeon.homebase.campContractSlots() ), Window.TITLE_COLOR );
		content.addSettlementRequestGrid( requests );
		content.endSection();

		if (requests.isEmpty()) {
			content.beginSection();
			content.addCenteredText( Messages.get( this, "camp_no_requests" ), Window.WHITE );
			content.endSection();
			return;
		}

		if (selectedSettlementRequest < 0 || selectedSettlementRequest >= requests.size()) {
			content.beginSection();
			content.addCenteredText( Messages.get( this, "camp_select_request" ), Window.WHITE );
			content.endSection();
			return;
		}

		HomebaseState.SettlementRequest request = requests.get( selectedSettlementRequest );
		final int requestIndex = selectedSettlementRequest;
		content.beginSection();
		content.addText( settlementMissionName( request ), Window.TITLE_COLOR );
		if (settlementObjectiveUsesCurrency( request )) {
			content.addCurrencyLine( settlementObjectiveLine( "Objective:", request ) );
		} else {
			content.addText( "Objective: " + settlementObjectiveText( request ) + ".", Window.WHITE );
		}
		content.addCurrencyLine( settlementRewardLine( "Reward:", request ) );
		content.addCostLine( new ResourceCostLine( Messages.get( this,
				request.progressObjective() ? "camp_request_progress_label" : "camp_request_cost_label" ), request ) );

		RedButton fulfill = new RedButton( Messages.get( this, "camp_request_fulfill" ), 6 ) {
			@Override
			protected void onClick() {
				if (Dungeon.homebase.fulfillSettlementRequest( requestIndex )) {
					GLog.p( Messages.get( WndHomebaseFacility.this, "camp_request_done" ) );
					saveHomebase();
					function.rebuild( functionContent() );
				}
			}
		};
		fulfill.enable( Dungeon.homebase.canFulfillSettlementRequest( requestIndex ) );
		content.addButton( fulfill );
		content.endSection();
	}

	private void buildDefenseStructureContent( FacilityContent content ) {
		content.beginSection();
		content.addText( Messages.get( this, "defense_structure_desc" ), Window.WHITE );
		content.endSection();
	}

	private String settlementMissionName( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
				return Messages.get( this, "camp_mission_bounty" );
			case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
				return Messages.get( this, "camp_mission_scouting" );
			case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
				return Messages.get( this, "camp_mission_recovery" );
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return Messages.get( this, "camp_mission_forge" );
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				return Messages.get( this, "camp_mission_gold" );
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				return Messages.get( this, "camp_mission_energy" );
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return Messages.get( this, "camp_mission_material" );
		}
	}

	private String settlementObjectiveText( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
				return Messages.get( this, "camp_objective_bounty", request.amount() );
			case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
				return Messages.get( this, "camp_objective_scouting", request.amount() );
			case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
				return Messages.get( this, "camp_objective_recovery", request.amount() );
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return compactAmount( request.amount() ) + " " + forgeName( request.objectiveForgeResource() );
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				return compactAmount( request.amount() ) + " " + goldName();
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				return compactAmount( request.amount() ) + " " + energyName();
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return compactAmount( request.amount() ) + " " + materialName( request.objectiveMaterial() );
		}
	}

	private boolean settlementObjectiveUsesCurrency( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
				return true;
			default:
				return false;
		}
	}

	private WndCurrencyLine settlementObjectiveLine( String label, HomebaseState.SettlementRequest request ) {
		WndCurrencyLine line = new WndCurrencyLine( label );
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				line.addForge( request.objectiveForgeResource(), request.amount() );
				break;
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				line.addGold( request.amount() );
				break;
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				line.addEnergy( request.amount() );
				break;
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
				line.addMaterial( request.objectiveMaterial(), request.amount() );
				break;
		}
		return line;
	}

	private int settlementObjectiveIcon( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
				return ItemSpriteSheet.MOB_HOLDER;
			case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
				return ItemSpriteSheet.GUIDE_PAGE;
			case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
				return ItemSpriteSheet.BACKPACK;
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return forgeIcon( request.objectiveForgeResource() );
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				return goldIcon();
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				return energyIcon();
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return materialIcon( request.objectiveMaterial() );
		}
	}

	private int settlementObjectiveOwned( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
			case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
			case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
				return request.progress();
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return Dungeon.homebase.forgeResourceAmount( request.objectiveForgeResource() );
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				return Dungeon.homebase.goldAmount();
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				return Dungeon.homebase.energyAmount();
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return Dungeon.homebase.amount( request.objectiveMaterial() );
		}
	}

	private int settlementObjectiveColor( HomebaseState.SettlementRequest request ) {
		switch (request.objectiveType()) {
			case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
				return 0xFF8844;
			case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
				return 0x66CCFF;
			case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
				return 0x88DD66;
			case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
				return forgeColor( request.objectiveForgeResource() );
			case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
				return goldColor();
			case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
				return energyColor();
			case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
			default:
				return materialColor( request.objectiveMaterial() );
		}
	}

	private String settlementRewardText( HomebaseState.SettlementRequest request ) {
		switch (request.rewardType()) {
			case HomebaseState.SettlementRequest.REWARD_MATERIAL:
				return compactAmount( request.rewardAmount() ) + " " + materialName( request.rewardMaterial() );
			case HomebaseState.SettlementRequest.REWARD_FORGE_RESOURCE:
				return compactAmount( request.rewardAmount() ) + " " + forgeName( request.rewardForgeResource() );
			case HomebaseState.SettlementRequest.REWARD_GOLD:
				return compactAmount( request.rewardAmount() ) + " " + goldName();
			case HomebaseState.SettlementRequest.REWARD_ENERGY:
			default:
				return compactAmount( request.rewardAmount() ) + " " + energyName();
		}
	}

	private WndCurrencyLine settlementRewardLine( String label, HomebaseState.SettlementRequest request ) {
		WndCurrencyLine line = new WndCurrencyLine( label );
		switch (request.rewardType()) {
			case HomebaseState.SettlementRequest.REWARD_MATERIAL:
				line.addMaterial( request.rewardMaterial(), request.rewardAmount() );
				break;
			case HomebaseState.SettlementRequest.REWARD_FORGE_RESOURCE:
				line.addForge( request.rewardForgeResource(), request.rewardAmount() );
				break;
			case HomebaseState.SettlementRequest.REWARD_GOLD:
				line.addGold( request.rewardAmount() );
				break;
			case HomebaseState.SettlementRequest.REWARD_ENERGY:
			default:
				line.addEnergy( request.rewardAmount() );
				break;
		}
		return line;
	}

	private FacilityContent upgradeContent() {
		FacilityContent content = new FacilityContent();
		if (Dungeon.homebase == null) {
			content.addText( Messages.get( this, "no_homebase" ), Window.WHITE );
			return content.finish();
		}

		int level = Dungeon.homebase.buildingLevel( building );
		content.beginSection();
		content.addText( Messages.get( this, "building_level", level ), Window.TITLE_COLOR );
		content.addText( Messages.get( this, "building_hp",
				Dungeon.homebase.buildingHP( building ),
				Dungeon.homebase.buildingMaxHP( building ),
				Dungeon.homebase.buildingArmor( building ) ), Window.WHITE );

		if (Dungeon.homebase.buildingDamaged( building ) || Dungeon.homebase.buildingDestroyed( building )) {
			content.addCostLine( new ResourceCostLine( Messages.get( this, "repair_cost_label" ), building, true ) );
			RedButton repair = new RedButton( Dungeon.homebase.buildingDestroyed( building )
					? Messages.get( this, "repair_destroyed" )
					: Messages.get( this, "repair" ), 6 ) {
				@Override
				protected void onClick() {
					if (Dungeon.homebase != null && Dungeon.homebase.repair( building )) {
						GLog.p( Messages.get( WndHomebaseFacility.class, "repaired" ) );
						resolveAfterConstruction( building );
						saveHomebase();
						reopen( TAB_UPGRADE );
					}
				}
			};
			repair.enable( Dungeon.homebase.canRepair( building ) );
			content.addButton( repair );
		} else if (!Dungeon.homebase.isBuilt( building )) {
			content.addCostLine( new ResourceCostLine( Messages.get( this, "rebuild_cost_label" ), building ) );
			RedButton rebuild = new RedButton( Messages.get( this, "rebuild" ), 6 ) {
				@Override
				protected void onClick() {
					if (Dungeon.homebase != null && Dungeon.homebase.build( building )) {
						GLog.p( Messages.get( WndHomebaseFacility.class, "rebuilt" ) );
						ReclaimedTutorial.flash( Document.GUIDE_REBUILDING );
						resolveAfterConstruction( building );
						saveHomebase();
						reopen( TAB_UPGRADE );
					}
				}
			};
			rebuild.enable( Dungeon.homebase.canBuild( building ) );
			content.addButton( rebuild );
		} else {
			content.addCostLine( new ResourceCostLine( Messages.get( this, "upgrade_cost_label" ), building ) );
			RedButton upgrade = new RedButton( Messages.get( this, "upgrade" ), 6 ) {
				@Override
				protected void onClick() {
					if (Dungeon.homebase != null && Dungeon.homebase.upgrade( building )) {
						GLog.p( Messages.get( WndHomebaseFacility.class, "upgraded" ) );
						resolveAfterConstruction( building );
						saveHomebase();
						reopen( TAB_UPGRADE );
					}
				}
			};
			upgrade.enable( Dungeon.homebase.canUpgrade( building ) );
			content.addButton( upgrade );
		}
		content.endSection();

		if (Dungeon.homebase.isBuilt( building )) {
			addBuildingDefenseContent( content );
		}

		return content.finish();
	}

	private void addBuildingDefenseContent( FacilityContent content ) {
		ArrayList<HomebaseState.BuildingDefense> unlocked = new ArrayList<>();
		ArrayList<HomebaseState.BuildingDefense> locked = new ArrayList<>();
		for (HomebaseState.BuildingDefense defense : HomebaseState.BuildingDefense.values()) {
			if (!Dungeon.homebase.canEverUpgradeBuildingDefense( defense )) continue;
			if (Dungeon.homebase.buildingDefenseCap( building, defense ) > 0) {
				unlocked.add( defense );
			} else {
				locked.add( defense );
			}
		}
		unlocked.sort( (a, b) -> a.unlockLevel() - b.unlockLevel() );
		locked.sort( (a, b) -> a.unlockLevel() - b.unlockLevel() );

		if (!unlocked.isEmpty()) {
			if (selectedBuildingDefense == null
					|| Dungeon.homebase.buildingDefenseCap( building, selectedBuildingDefense ) <= 0) {
				selectedBuildingDefense = unlocked.get( 0 );
			}
			rememberedBuildingDefenses[building.ordinal()] = selectedBuildingDefense;
			content.beginSection();
			content.addBuildingDefenseGrid( unlocked );
			content.endSection();
			content.beginSection();
			addBuildingDefenseDetail( content, selectedBuildingDefense );
			content.endSection();
		}

		if (!locked.isEmpty()) {
			content.beginSection();
			for (HomebaseState.BuildingDefense defense : locked) {
				content.addCenteredText( Messages.get( this, "defense_locked", defense.label(), defense.unlockLevel() ), 0xAAAAAA );
			}
			content.endSection();
		}
	}

	private void addBuildingDefenseDetail( FacilityContent content, final HomebaseState.BuildingDefense defense ) {
		int trained = Dungeon.homebase.buildingDefenseLevel( building, defense );
		int cap = Dungeon.homebase.buildingDefenseCap( building, defense );

		content.addText( defense.label(), Window.TITLE_COLOR );
		content.addText( buildingDefenseDesc( defense ), Window.WHITE );
		if (trained < cap) {
			content.addCostLine( new ResourceCostLine( Messages.get( this, "defense_cost_label" ), building, defense ) );
			RedButton train = new RedButton( Messages.get( this, "upgrade_defense" ), 6 ) {
				@Override
				protected void onClick() {
					if (Dungeon.homebase != null && Dungeon.homebase.upgradeBuildingDefense( building, defense )) {
						GLog.p( Messages.get( WndHomebaseFacility.class, "defense_upgraded", defense.label() ) );
						saveHomebase();
						reopen( TAB_UPGRADE );
					}
				}
			};
			train.enable( Dungeon.homebase.canUpgradeBuildingDefense( building, defense ) );
			content.addButton( train );
		} else {
			content.addText( Messages.get( this, "defense_maxed" ), 0xCCCCCC );
		}
		content.addGap();
	}

	private String buildingDefenseDesc( HomebaseState.BuildingDefense defense ) {
		switch (defense) {
			case HP:
				return Messages.get( this, "desc_building_hp", defense.bonusPerLevel() );
			case ARMOR:
				return Messages.get( this, "desc_building_armor", defense.bonusPerLevel() );
			default:
				return Messages.get( this, "desc_building_resistance", defense.label(), defense.bonusPerLevel() );
		}
	}

	private FacilityContent statsContent() {
		FacilityContent content = new FacilityContent();
		if (Dungeon.homebase == null) {
			content.addText( Messages.get( this, "no_homebase" ), Window.WHITE );
			return content.finish();
		}

		ArrayList<HomebaseState.Training> unlocked = new ArrayList<>();
		ArrayList<HomebaseState.Training> locked = new ArrayList<>();
		for (HomebaseState.Training training : HomebaseState.Training.values()) {
			if (training.building() != building) continue;
			if (!Dungeon.homebase.canEverTrain( training )) continue;
			if (Dungeon.homebase.trainingCap( training ) > 0) {
				unlocked.add( training );
			} else {
				locked.add( training );
			}
		}
		unlocked.sort( (a, b) -> a.unlockLevel() - b.unlockLevel() );
		locked.sort( (a, b) -> a.unlockLevel() - b.unlockLevel() );

		if (unlocked.isEmpty() && locked.isEmpty()) {
			content.addText( Messages.get( this, "no_stats" ), Window.WHITE );
			return content.finish();
		}

		if (!unlocked.isEmpty()) {
			if (selectedTraining == null
					|| selectedTraining.building() != building
					|| Dungeon.homebase.trainingCap( selectedTraining ) <= 0) {
				selectedTraining = unlocked.get( 0 );
			}
			rememberedTrainings[building.ordinal()] = selectedTraining;
			content.beginSection();
			content.addTrainingGrid( unlocked );
			content.endSection();
			content.beginSection();
			addTrainingDetail( content, selectedTraining );
			content.endSection();
		}

		if (!locked.isEmpty()) {
			content.beginSection();
			for (HomebaseState.Training training : locked) {
				content.addCenteredText( Messages.get( this, "stat_locked", training.label(), training.unlockLevel() ), 0xAAAAAA );
			}
			content.endSection();
		}
		return content.finish();
	}

	private void addTrainingDetail( FacilityContent content, final HomebaseState.Training training ) {
		int trained = Dungeon.homebase.trainingLevel( training );
		int cap = Dungeon.homebase.trainingCap( training );

		content.addText( training.label(), Window.TITLE_COLOR );
		content.addText( Messages.get( this, trainingDescKey( training ), training.bonusPerLevel() ), Window.WHITE );
		if (trained < cap) {
			content.addCostLine( new ResourceCostLine( Messages.get( this, "stat_cost_label" ), training ) );
			RedButton train = new RedButton( trainButtonLabel( training ), 6 ) {
				@Override
				protected void onClick() {
					if (Dungeon.homebase != null && Dungeon.homebase.train( training )) {
						if (training == HomebaseState.Training.HEALTH
								|| training == HomebaseState.Training.RING_POTENCY
								|| training == HomebaseState.Training.TRINKET_POTENCY
								|| training == HomebaseState.Training.ARTIFACT_POTENCY) {
							Dungeon.hero.updateHT( true );
						}
						GLog.p( Messages.get( WndHomebaseFacility.class, "trained", training.label() ) );
						saveHomebase();
						reopen( TAB_STATS );
					}
				}
			};
			train.enable( Dungeon.homebase.canTrain( training ) );
			content.addButton( train );
		} else {
			content.addText( Messages.get( this, "stat_maxed" ), 0xCCCCCC );
		}
		content.addGap();
	}

	private String trainingDescKey( HomebaseState.Training training ) {
		return "desc_" + training.name().toLowerCase( Locale.ENGLISH );
	}

	private String trainButtonLabel( HomebaseState.Training training ) {
		String text = Messages.get( WndInfoCell.class, "train_" + training.name().toLowerCase( Locale.ENGLISH ), training.bonusPerLevel() );
		if (Messages.NO_TEXT_FOUND.equals( text )) {
			text = Messages.format( "Train %s +%d", training.label(), training.bonusPerLevel() );
		}
		return text;
	}

	private int vaultCols() {
		int preferred = ReclaimedWindow.isDesktop() ? VAULT_COLS : 5;
		int available = Math.max( 1, (contentWidth + SLOT_MARGIN) / (SLOT_SIZE + SLOT_MARGIN) );
		return Math.max( 1, Math.min( preferred, available ) );
	}

	private Image moonrootPlantVisual( Plant plant ) {
		int tile = plant.image + 7*16;
		return new Image( Assets.Environment.TERRAIN_FEATURES, (tile % 16) * 16, (tile / 16) * 16, 16, 16 );
	}

	private final WndBag.ItemSelector depositSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndVaultStorage.class, "prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canStoreInVault( item )
					&& !item.isEquipped( Dungeon.hero )
					&& !(item instanceof Bag);
		}

		@Override
		public void onSelect( Item item ) {
			if (item == null) {
				reopen( TAB_FUNCTION );
				return;
			}

			Item stored = item.detachAll( Dungeon.hero.belongings.backpack );
			if (Dungeon.homebase != null && stored != null && Dungeon.homebase.storeInVault( stored )) {
				GLog.p( Messages.get( WndVaultStorage.class, "stored", Messages.titleCase( stored.title() ) ) );
				Item.updateQuickslot();
				saveHomebase();
			} else if (stored != null) {
				stored.collect( Dungeon.hero.belongings.backpack );
				GLog.w( Messages.get( WndVaultStorage.class, "full" ) );
			}
			reopen( TAB_FUNCTION );
		}
	};

	private final WndBag.ItemSelector salvageSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndEmberforge.class, "salvage_prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return null;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canSalvage( item )
					&& !item.isEquipped( Dungeon.hero )
					&& !(item instanceof Bag);
		}

		@Override
		public void onSelect( final Item item ) {
			if (item == null) {
				reopen( TAB_FUNCTION );
				return;
			}

			if (item.quantity() > 1) {
				show( WndEmberforgeConfirm.salvageStack( item,
						new String[]{
								Messages.get( WndEmberforge.class, "salvage_one" ),
								Messages.get( WndEmberforge.class, "salvage_all", item.quantity() ),
								Messages.get( WndEmberforge.class, "cancel" ) },
						new WndEmberforgeConfirm.Callback() {
							@Override
							public void onSelect( int index ) {
						if (index == 0) {
							HomebaseFacilityScene.setResultMessage( building, "" );
							WndEmberforge.salvageAmount( item, 1 );
							selectItem( salvageSelector );
						} else if (index == 1) {
							HomebaseFacilityScene.setResultMessage( building, "" );
							WndEmberforge.salvageAmount( item, item.quantity() );
							selectItem( salvageSelector );
						} else {
							reopen( TAB_FUNCTION );
						}
					}
						},
						new Runnable() {
							@Override
							public void run() {
								reopen( TAB_FUNCTION );
							}
						} ) );
				return;
			}

			show( WndEmberforgeConfirm.salvage( item, 1,
					new String[]{
							Messages.get( WndEmberforge.class, "salvage_yes" ),
							Messages.get( WndEmberforge.class, "cancel" ) },
					new WndEmberforgeConfirm.Callback() {
						@Override
						public void onSelect( int index ) {
					if (index == 0) {
						HomebaseFacilityScene.setResultMessage( building, "" );
						WndEmberforge.salvageOne( item );
						selectItem( salvageSelector );
					} else {
						reopen( TAB_FUNCTION );
					}
				}
					},
					new Runnable() {
						@Override
						public void run() {
							reopen( TAB_FUNCTION );
						}
					} ) );
		}
	};

	private final WndBag.ItemSelector upgradeSelector = new WndBag.ItemSelector() {
		@Override
		public String textPrompt() {
			return Messages.get( WndEmberforge.class, "upgrade_prompt" );
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return item != null
					&& Dungeon.homebase != null
					&& Dungeon.homebase.canForgeUpgradeTarget( item );
		}

		@Override
		public void onSelect( final Item item ) {
			if (item == null) {
				reopen( TAB_FUNCTION );
				return;
			}
			show( WndEmberforgeConfirm.upgrade( item,
					new String[]{
							Messages.get( WndEmberforge.class, "upgrade_yes" ),
							Messages.get( WndEmberforge.class, "cancel" ) },
					new WndEmberforgeConfirm.Callback() {
						@Override
						public void onSelect( int index ) {
					if (index == 0) {
						boolean rarityImproved = WndEmberforge.upgradeItem( item );
						HomebaseFacilityScene.setResultMessage( building, rarityImproved
								? Messages.get( WndEmberforge.class, "rarity_improved", Messages.capitalize( item.name() ) )
								: "" );
						selectItem( upgradeSelector );
					} else {
						reopen( TAB_FUNCTION );
					}
				}
					},
					new Runnable() {
						@Override
						public void run() {
							reopen( TAB_FUNCTION );
						}
					} ) );
		}
	};

	private void withdraw( Item item ) {
		if (Dungeon.homebase == null || !Dungeon.homebase.removeFromVault( item )) return;

		if (item.collect( Dungeon.hero.belongings.backpack )) {
			GLog.p( Messages.get( WndVaultStorage.class, "withdrawn", Messages.titleCase( item.title() ) ) );
			Item.updateQuickslot();
			saveHomebase();
		} else {
			Dungeon.homebase.storeInVault( item );
			GLog.w( Messages.get( WndVaultStorage.class, "inventory_full" ) );
		}
		reopen( TAB_FUNCTION );
	}

	private void selectItem( WndBag.ItemSelector selector ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.selectItem( selector );
		} else {
			show( WndBag.getBag( selector ) );
		}
	}

	private void show( Window window ) {
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.show( window );
		} else if (ShatteredPixelDungeon.scene() instanceof PixelScene) {
			((PixelScene)ShatteredPixelDungeon.scene()).addToFront( window );
		}
	}

	private void reopen() {
		rememberScrollPositions();
		ShatteredPixelDungeon.switchNoFade( HomebaseFacilityScene.sceneFor( building ) );
	}

	private void reopen( int tab ) {
		rememberScrollPositions();
		rememberedTabs[building.ordinal()] = tab;
		ShatteredPixelDungeon.switchNoFade( HomebaseFacilityScene.sceneFor( building ) );
	}

	private void rememberSelectedTab() {
		int index = tabs.indexOf( selected );
		if (index >= 0) {
			rememberedTabs[building.ordinal()] = index;
		}
	}

	private void rememberScrollPositions() {
		if (function != null) function.rememberScroll();
		if (upgrade != null) upgrade.rememberScroll();
		if (stats != null) stats.rememberScroll();
	}

	private String functionLabel() {
		switch (building) {
			case VAULT: return Messages.get( this, "tab_storage" );
			case FORGE: return Messages.get( this, "tab_forge" );
			case ALCHEMY: return Messages.get( this, "tab_still" );
			case GARDEN: return Messages.get( this, "tab_garden" );
			case CAMP:
				return Messages.get( this, "tab_camp" );
			default:
				return Messages.get( this, "tab_structure" );
		}
	}

	private static void saveHomebase() {
		try {
			Dungeon.saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
		}
	}

	private static void resolveAfterConstruction( HomebaseState.Building building ) {
		if (Dungeon.level instanceof HomebaseLevel) {
			HomebaseLevel level = (HomebaseLevel)Dungeon.level;
			level.refreshBuildingVisual( building );
			level.ensureHeroOutsideBlockedStructure();
			level.ensureDefendersOutsideBlockedStructure( building );
			level.relocateHeapsBlockedByStructure( building );
			if (building == HomebaseState.Building.CAMP) {
				level.spawnHomebaseDefenders();
			}
		}
	}

	private int trainingIcon( HomebaseState.Training training ) {
		switch (training) {
			case HEALTH:
				return ItemSpriteSheet.DEWDROP;
			case STRENGTH:
				return ItemSpriteSheet.WEAPON_HOLDER;
			case ACCURACY:
				return ItemSpriteSheet.MISSILE_HOLDER;
			case EVASION:
				return ItemSpriteSheet.ARMOR_HOLDER;
			case ARMOR:
				return ItemSpriteSheet.ARMOR_HOLDER;
			case TREASURE_LUCK:
			case GOLD_GAIN:
				return ItemSpriteSheet.GOLD;
			case CATALYST_DROP_RATE:
			case BONUS_LOOT:
			case RESOURCE_YIELD:
			case MATERIAL_CACHE_SIZE:
				return ItemSpriteSheet.STONE_HOLDER;
			case WAND_RECHARGE:
			case WAND_DAMAGE:
			case WAND_CHARGES:
			case MAGIC_DAMAGE:
			case MAGIC_POWER:
				return ItemSpriteSheet.WAND_HOLDER;
			case MOVEMENT_SPEED:
				return ItemSpriteSheet.SEED_HOLDER;
			case ATTACK_DAMAGE:
			case ATTACK_SPEED:
			case CRITICAL_CHANCE:
			case CRITICAL_DAMAGE:
			case LIFESTEAL:
			case KNOCKBACK_CHANCE:
			case KNOCKBACK_STRENGTH:
			case CLEAVE_CHANCE:
			case PIERCING_CHANCE:
			case BLEED_PROC:
			case BLEED_DURATION:
			case STUN_CHANCE:
			case STUN_DURATION:
				return ItemSpriteSheet.WEAPON_HOLDER;
			case ARMOR_ABILITY_CHARGE:
			case TENACITY:
			case DODGE_CHANCE:
			case BLOCK_CHANCE:
			case ARMOR_BONUS:
			case THORNS_CHANCE:
			case THORNS_DAMAGE:
			case BARRIER_GUARD:
			case BARRIER_POWER:
			case BLEED_RESISTANCE:
			case DAZE_RESISTANCE:
			case VULNERABLE_RESISTANCE:
			case STUN_RESISTANCE:
			case WEAKNESS_RESISTANCE:
			case CHARM_RESISTANCE:
			case TERROR_RESISTANCE:
			case DREAD_RESISTANCE:
				return ItemSpriteSheet.ARMOR_HOLDER;
			case ENCHANTMENT_POWER:
			case STATUS_PROC_CHANCE:
			case STATUS_DURATION:
			case HEX_PROC:
			case HEX_DURATION:
			case DAZE_PROC:
			case DAZE_DURATION:
			case VULNERABLE_PROC:
			case VULNERABLE_DURATION:
			case WEAKNESS_PROC:
			case WEAKNESS_DURATION:
			case HEX_RESISTANCE:
			case AMOK_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
				return ItemSpriteSheet.SCROLL_HOLDER;
			case ELEMENTAL_RESISTANCE:
			case LIGHTNING_CHANCE:
			case BURNING_PROC:
			case BURNING_DURATION:
			case FROST_PROC:
			case FROST_DURATION:
			case POISON_PROC:
			case POISON_DURATION:
			case CORROSION_PROC:
			case CORROSION_DURATION:
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case POISON_RESISTANCE:
			case CORROSION_RESISTANCE:
			case OOZE_RESISTANCE:
				return ItemSpriteSheet.POTION_HOLDER;
			case RANGED_DAMAGE:
			case THROWN_DURABILITY:
				return ItemSpriteSheet.MISSILE_HOLDER;
			case BLINDNESS_PROC:
			case BLINDNESS_DURATION:
			case CRIPPLE_PROC:
			case CRIPPLE_DURATION:
			case ROOT_PROC:
			case ROOT_DURATION:
			case SLOW_PROC:
			case SLOW_DURATION:
			case VERTIGO_PROC:
			case VERTIGO_DURATION:
			case BLINDNESS_RESISTANCE:
			case CRIPPLE_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case SLEEP_RESISTANCE:
			case CHILL_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
				return ItemSpriteSheet.SEED_HOLDER;
			case TRINKET_POTENCY:
				return ItemSpriteSheet.TRINKET_HOLDER;
			case ARTIFACT_RECHARGE:
			case ARTIFACT_POTENCY:
				return ItemSpriteSheet.ARTIFACT_HOLDER;
			case RING_POTENCY:
				return ItemSpriteSheet.RING_HOLDER;
			case TALENT_POINT:
			case TALENT_TIER_2:
			case TALENT_TIER_3:
			case TALENT_TIER_4:
			default:
				return ItemSpriteSheet.MASTERY;
		}
	}

	private int buildingDefenseIcon( HomebaseState.BuildingDefense defense ) {
		switch (defense) {
			case HP:
				return ItemSpriteSheet.DEWDROP;
			case ARMOR:
				return ItemSpriteSheet.ARMOR_HOLDER;
			case FIRE_RESISTANCE:
			case FROST_RESISTANCE:
			case CORROSION_RESISTANCE:
			case HEX_RESISTANCE:
			case DEGRADE_RESISTANCE:
			case DOOM_RESISTANCE:
				return ItemSpriteSheet.WAND_HOLDER;
			case POISON_RESISTANCE:
			case ROOT_RESISTANCE:
			case SLOW_RESISTANCE:
			case VERTIGO_RESISTANCE:
			case CHILL_RESISTANCE:
			case OOZE_RESISTANCE:
			case MAGICAL_SLEEP_RESISTANCE:
				return ItemSpriteSheet.SEED_HOLDER;
			default:
				return ItemSpriteSheet.ARMOR_HOLDER;
		}
	}

	public static int materialIcon( HomebaseState.Material material ) {
		switch (material) {
			case WOOD:
				return ItemSpriteSheet.HOMEBASE_WOOD;
			case STONE:
				return ItemSpriteSheet.HOMEBASE_STONE;
			case COPPER:
				return ItemSpriteSheet.HOMEBASE_COPPER;
			case IRON:
				return ItemSpriteSheet.HOMEBASE_IRON;
			case GOLD:
			default:
				return ItemSpriteSheet.HOMEBASE_GOLD;
		}
	}

	public static int materialColor( HomebaseState.Material material ) {
		switch (material) {
			case WOOD:
				return 0xD2A15D;
			case STONE:
				return 0xB8B8B8;
			case COPPER:
				return 0xD9793F;
			case IRON:
				return 0xA8C4D8;
			case GOLD:
			default:
				return 0xFFD84A;
		}
	}

	public static int forgeIcon( HomebaseState.ForgeResource resource ) {
		switch (resource) {
			case SCRAP:
				return ItemSpriteSheet.HOMEBASE_SCRAP;
			case EMBER_SHARD:
				return ItemSpriteSheet.HOMEBASE_EMBER;
			case EMBER_CORE:
			default:
				return ItemSpriteSheet.HOMEBASE_CORE;
		}
	}

	public static int forgeColor( HomebaseState.ForgeResource resource ) {
		switch (resource) {
			case SCRAP:
				return 0xCACFC2;
			case EMBER_SHARD:
				return 0xFF9A3A;
			case EMBER_CORE:
			default:
				return 0xFF5555;
		}
	}

	public static String materialName( HomebaseState.Material material ) {
		switch (material) {
			case WOOD:
				return Messages.titleCase( "wood" );
			case STONE:
				return Messages.titleCase( "stone" );
			case COPPER:
				return Messages.titleCase( "copper ore" );
			case IRON:
				return Messages.titleCase( "iron ore" );
			case GOLD:
			default:
				return Messages.titleCase( "gold ore" );
		}
	}

	public static String forgeName( HomebaseState.ForgeResource resource ) {
		return Messages.titleCase( resource.label() );
	}

	public static int goldIcon() {
		return ItemSpriteSheet.GOLD;
	}

	public static int energyIcon() {
		return ItemSpriteSheet.ENERGY;
	}

	public static int emeraldIcon() {
		return ItemSpriteSheet.HOMEBASE_EMERALD;
	}

	public static int goldColor() {
		return 0xFFFF44;
	}

	public static int energyColor() {
		return 0x44CCFF;
	}

	public static int emeraldColor() {
		return 0x33FF88;
	}

	public static String goldName() {
		return Messages.titleCase( "gold" );
	}

	public static String energyName() {
		return Messages.titleCase( "energy" );
	}

	public static String emeraldName() {
		return Messages.titleCase( "emerald" );
	}

	public static String compactAmount( int amount ) {
		if (amount >= 1000000) {
			return amount / 1000000 + "m";
		} else if (amount >= 1000) {
			return amount / 1000 + "k";
		} else {
			return Integer.toString( amount );
		}
	}

	private class ResourceCostLine extends Component {

		private final RenderedTextBlock label;
		private final ArrayList<ResourceCostChip> chips = new ArrayList<>();

		private ResourceCostLine( String labelText, HomebaseState.Building building ) {
			this( labelText );
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				addMaterialCost( material, Dungeon.homebase.cost( building, material ) );
			}
			addGoldCost( Dungeon.homebase.goldCost( building ) );
			addEnergyCost( Dungeon.homebase.energyCost( building ) );
		}

		private ResourceCostLine( String labelText, HomebaseState.Training training ) {
			this( labelText );
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				addMaterialCost( material, Dungeon.homebase.trainingCost( training, material ) );
			}
			addGoldCost( Dungeon.homebase.trainingGoldCost( training ) );
			addEnergyCost( Dungeon.homebase.trainingEnergyCost( training ) );
		}

		private ResourceCostLine( String labelText, HomebaseState.Building building, HomebaseState.BuildingDefense defense ) {
			this( labelText );
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				addMaterialCost( material, Dungeon.homebase.buildingDefenseCost( building, defense, material ) );
			}
			addGoldCost( Dungeon.homebase.buildingDefenseGoldCost( building, defense ) );
			addEnergyCost( Dungeon.homebase.buildingDefenseEnergyCost( building, defense ) );
		}

		private ResourceCostLine( String labelText, HomebaseState.Building building, boolean repair ) {
			this( labelText );
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				addMaterialCost( material, Dungeon.homebase.repairCost( building, material ) );
			}
			addGoldCost( Dungeon.homebase.repairGoldCost( building ) );
			addEnergyCost( Dungeon.homebase.repairEnergyCost( building ) );
		}

		private ResourceCostLine( String labelText, HomebaseState.SettlementRequest request ) {
			this( labelText );
			switch (request.objectiveType()) {
				case HomebaseState.SettlementRequest.OBJECTIVE_BOUNTY:
				case HomebaseState.SettlementRequest.OBJECTIVE_SCOUTING:
				case HomebaseState.SettlementRequest.OBJECTIVE_RECOVERY:
					addProgressCost( request );
					break;
				case HomebaseState.SettlementRequest.OBJECTIVE_FORGE_RESOURCE:
					addForgeCost( request.objectiveForgeResource(), request.amount() );
					break;
				case HomebaseState.SettlementRequest.OBJECTIVE_GOLD:
					addGoldCost( request.amount() );
					break;
				case HomebaseState.SettlementRequest.OBJECTIVE_ENERGY:
					addEnergyCost( request.amount() );
					break;
				case HomebaseState.SettlementRequest.OBJECTIVE_MATERIAL:
				default:
					addMaterialCost( request.objectiveMaterial(), request.amount() );
					break;
			}
		}

		private ResourceCostLine( String labelText ) {
			label = PixelScene.renderTextBlock( labelText, 6 );
			label.hardlight( Window.WHITE );
			add( label );
		}

		private void addMaterialCost( HomebaseState.Material material, int needed ) {
			if (needed <= 0) return;
			ResourceCostChip chip = new ResourceCostChip(
					materialIcon( material ),
					materialName( material ),
					Dungeon.homebase.amount( material ),
					needed,
					materialColor( material ) );
			chips.add( chip );
			add( chip );
		}

		private void addForgeCost( HomebaseState.ForgeResource resource, int needed ) {
			if (needed <= 0) return;
			ResourceCostChip chip = new ResourceCostChip(
					forgeIcon( resource ),
					forgeName( resource ),
					Dungeon.homebase.forgeResourceAmount( resource ),
					needed,
					forgeColor( resource ) );
			chips.add( chip );
			add( chip );
		}

		private void addGoldCost( int needed ) {
			if (needed <= 0) return;
			ResourceCostChip chip = new ResourceCostChip(
					Icons.get( Icons.COIN_SML ),
					goldName(),
					Dungeon.homebase.goldAmount(),
					needed,
					goldColor() );
			chips.add( chip );
			add( chip );
		}

		private void addEnergyCost( int needed ) {
			if (needed <= 0) return;
			ResourceCostChip chip = new ResourceCostChip(
					Icons.get( Icons.ENERGY_SML ),
					energyName(),
					Dungeon.homebase.energyAmount(),
					needed,
					energyColor() );
			chips.add( chip );
			add( chip );
		}

		private void addProgressCost( HomebaseState.SettlementRequest request ) {
			ResourceCostChip chip = new ResourceCostChip(
					settlementObjectiveIcon( request ),
					settlementMissionName( request ),
					request.progress(),
					request.amount(),
					settlementObjectiveColor( request ) );
			chips.add( chip );
			add( chip );
		}

		@Override
		protected void layout() {
			label.setPos( x, y );
			PixelScene.align( label );

			float left = 0;
			float rowTop = label.bottom() + 2;
			for (ResourceCostChip chip : chips) {
				float chipWidth = chip.reqWidth();
				if (left > 0 && left + chipWidth > width) {
					left = 0;
					rowTop += 11;
				}
				chip.setRect( x + left, rowTop, chipWidth, 10 );
				left += chipWidth + 4;
			}
			height = chips.isEmpty() ? label.height() : rowTop + 10 - y;
		}
	}

	private class ResourceCostChip extends Button {

		private final Image icon;
		private final RenderedTextBlock amount;
		private final String resourceName;
		private final int owned;
		private final int needed;
		private final int color;

		private ResourceCostChip( int iconId, String resourceName, int owned, int needed, int color ) {
			this( new ItemSprite( iconId ), resourceName, owned, needed, color );
		}

		private ResourceCostChip( Image icon, String resourceName, int owned, int needed, int color ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.resourceName = resourceName;
			this.owned = owned;
			this.needed = needed;
			this.color = color;

			this.icon = icon;
			this.icon.resetColor();
			add( this.icon );

			amount = PixelScene.renderTextBlock( 5 );
			amount.text( compactAmount( owned ) + "/" + compactAmount( needed ) );
			add( amount );
			applyColors();
		}

		private float reqWidth() {
			return icon.width() + 1 + amount.width();
		}

		@Override
		protected void layout() {
			super.layout();
			icon.x = x;
			icon.y = y + (height - icon.height()) / 2f;
			amount.setPos( icon.x + icon.width() + 1, y + (height - amount.height()) / 2f );
			PixelScene.align( icon );
			PixelScene.align( amount );
		}

		@Override
		protected void onClick() {
			show( new WndMessage( resourceName + ": " + owned + "/" + needed ) );
		}

		@Override
		protected void onPointerDown() {
			icon.brightness( 1.5f );
			amount.hardlight( Window.WHITE );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			applyColors();
		}

		@Override
		protected String hoverText() {
			return resourceName + ": " + owned + "/" + needed;
		}

		private void applyColors() {
			icon.resetColor();
			amount.hardlight( owned >= needed ? 0x44FF44 : 0xFFFF44 );
		}
	}

	private class SettlementRequestButton extends Button {

		private final int requestIndex;
		private final HomebaseState.SettlementRequest request;
		private final Image bg;
		private final ColorBlock fill;
		private final ItemSprite icon;
		private final RenderedTextBlock label;

		private SettlementRequestButton( int requestIndex, HomebaseState.SettlementRequest request ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.requestIndex = requestIndex;
			this.request = request;

			bg = new Image( Assets.Interfaces.TALENT_BUTTON );
			bg.frame( 0, 0, 20, 26 );
			add( bg );

			fill = new ColorBlock( 0, 4, 0xFFFFFF44 );
			add( fill );

			icon = new ItemSprite( settlementObjectiveIcon( request ) );
			add( icon );

			label = PixelScene.renderTextBlock( 5 );
			add( label );
		}

		@Override
		protected void layout() {
			width = STAT_BUTTON_WIDTH;
			height = STAT_BUTTON_HEIGHT;
			super.layout();

			bg.x = x + (width - 20) / 2f;
			bg.y = y;
			bg.am = selectedSettlementRequest == requestIndex ? 1f : 0.72f;

			boolean ready = Dungeon.homebase.canFulfillSettlementRequest( requestIndex );
			applyButtonColors( ready );

			fill.x = bg.x + 2;
			fill.y = bg.y + 19;
			fill.size( Math.min( 1f, settlementObjectiveOwned( request ) / (float)Math.max( 1, request.amount() ) ) * 16, 5 );

			icon.x = bg.x + (20 - icon.width()) / 2f;
			icon.y = bg.y + 1 + (18 - icon.height()) / 2f;
			icon.am = selectedSettlementRequest == requestIndex ? 1f : 0.8f;
			PixelScene.align( icon );

			label.text( Integer.toString( requestIndex + 1 ) );
			label.setPos(
					x + (width - label.width()) / 2f,
					bg.y + 20 );
			PixelScene.align( label );
		}

		@Override
		protected void onClick() {
			selectedSettlementRequest = requestIndex;
			rememberedSettlementRequest = requestIndex;
			function.rebuild( functionContent() );
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			icon.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			applyButtonColors( Dungeon.homebase.canFulfillSettlementRequest( requestIndex ) );
		}

		@Override
		protected String hoverText() {
			return settlementMissionName( request ) + ": " + settlementObjectiveText( request );
		}

		private void applyButtonColors( boolean ready ) {
			bg.resetColor();
			icon.resetColor();
			fill.resetColor();
			label.hardlight( Window.WHITE );
			fill.hardlight( ready ? 0x44FF44 : settlementObjectiveColor( request ) );
			if (ready) {
				bg.tint( 0x44CC44, selectedSettlementRequest == requestIndex ? 0.35f : 0.2f );
			}
		}
	}

	private class TrainingButton extends Button {

		private final HomebaseState.Training training;
		private final Image bg;
		private final ColorBlock fill;
		private final ItemSprite icon;
		private final RenderedTextBlock progress;

		private TrainingButton( HomebaseState.Training training ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.training = training;

			bg = new Image( Assets.Interfaces.TALENT_BUTTON );
			bg.frame( 0, 0, 20, 26 );
			add( bg );

			fill = new ColorBlock( 0, 4, 0xFFFFFF44 );
			add( fill );

			icon = new ItemSprite( trainingIcon( training ) );
			add( icon );

			progress = PixelScene.renderTextBlock( 5 );
			add( progress );
		}

		@Override
		protected void layout() {
			width = STAT_BUTTON_WIDTH;
			height = STAT_BUTTON_HEIGHT;
			super.layout();

			bg.x = x + (width - 20) / 2f;
			bg.y = y;
			bg.am = selectedTraining == training ? 1f : 0.72f;

			int cap = Math.max( 1, Dungeon.homebase.trainingCap( training ) );
			boolean maxed = Dungeon.homebase.trainingCap( training ) > 0 && Dungeon.homebase.trainingLevel( training ) >= cap;
			applyButtonColors( maxed );
			fill.x = bg.x + 2;
			fill.y = bg.y + 19;
			fill.size( Dungeon.homebase.trainingLevel( training ) / (float)cap * 16, 5 );

			icon.x = bg.x + (20 - icon.width()) / 2f;
			icon.y = bg.y + 1 + (18 - icon.height()) / 2f;
			icon.am = selectedTraining == training ? 1f : 0.7f;
			PixelScene.align( icon );

			progress.text( Dungeon.homebase.trainingLevel( training ) + "/" + Dungeon.homebase.trainingCap( training ) );
			progress.setPos(
					x + (width - progress.width()) / 2f,
					bg.y + 20 );
			PixelScene.align( progress );
		}

		@Override
		protected void onClick() {
			selectedTraining = training;
			rememberedTrainings[building.ordinal()] = training;
			stats.rebuild( statsContent() );
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			icon.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			int cap = Math.max( 1, Dungeon.homebase.trainingCap( training ) );
			applyButtonColors( Dungeon.homebase.trainingCap( training ) > 0 && Dungeon.homebase.trainingLevel( training ) >= cap );
		}

		@Override
		protected String hoverText() {
			return training.label();
		}

		private void applyButtonColors( boolean maxed ) {
			bg.resetColor();
			icon.resetColor();
			fill.resetColor();
			if (maxed) {
				bg.tint( 0xCC3333, selectedTraining == training ? 0.45f : 0.3f );
				fill.hardlight( 0xFF5555 );
				icon.hardlight( 0xFFBBBB );
				progress.hardlight( 0xFFBBBB );
			} else {
				fill.hardlight( 0xFFFFFF );
				progress.hardlight( Window.WHITE );
			}
		}
	}

	private class BuildingDefenseButton extends Button {

		private final HomebaseState.BuildingDefense defense;
		private final Image bg;
		private final ColorBlock fill;
		private final ItemSprite icon;
		private final RenderedTextBlock progress;

		private BuildingDefenseButton( HomebaseState.BuildingDefense defense ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.defense = defense;

			bg = new Image( Assets.Interfaces.TALENT_BUTTON );
			bg.frame( 0, 0, 20, 26 );
			add( bg );

			fill = new ColorBlock( 0, 4, 0xFFFFFF44 );
			add( fill );

			icon = new ItemSprite( buildingDefenseIcon( defense ) );
			add( icon );

			progress = PixelScene.renderTextBlock( 5 );
			add( progress );
		}

		@Override
		protected void layout() {
			width = STAT_BUTTON_WIDTH;
			height = STAT_BUTTON_HEIGHT;
			super.layout();

			bg.x = x + (width - 20) / 2f;
			bg.y = y;
			bg.am = selectedBuildingDefense == defense ? 1f : 0.72f;

			int cap = Math.max( 1, Dungeon.homebase.buildingDefenseCap( building, defense ) );
			boolean maxed = Dungeon.homebase.buildingDefenseCap( building, defense ) > 0
					&& Dungeon.homebase.buildingDefenseLevel( building, defense ) >= cap;
			applyButtonColors( maxed );
			fill.x = bg.x + 2;
			fill.y = bg.y + 19;
			fill.size( Dungeon.homebase.buildingDefenseLevel( building, defense ) / (float)cap * 16, 5 );

			icon.x = bg.x + (20 - icon.width()) / 2f;
			icon.y = bg.y + 1 + (18 - icon.height()) / 2f;
			icon.am = selectedBuildingDefense == defense ? 1f : 0.7f;
			PixelScene.align( icon );

			progress.text( Dungeon.homebase.buildingDefenseLevel( building, defense ) + "/" + Dungeon.homebase.buildingDefenseCap( building, defense ) );
			progress.setPos(
					x + (width - progress.width()) / 2f,
					bg.y + 20 );
			PixelScene.align( progress );
		}

		@Override
		protected void onClick() {
			selectedBuildingDefense = defense;
			rememberedBuildingDefenses[building.ordinal()] = defense;
			upgrade.rebuild( upgradeContent() );
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			icon.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			int cap = Math.max( 1, Dungeon.homebase.buildingDefenseCap( building, defense ) );
			applyButtonColors( Dungeon.homebase.buildingDefenseCap( building, defense ) > 0
					&& Dungeon.homebase.buildingDefenseLevel( building, defense ) >= cap );
		}

		@Override
		protected String hoverText() {
			return defense.label();
		}

		private void applyButtonColors( boolean maxed ) {
			bg.resetColor();
			icon.resetColor();
			fill.resetColor();
			if (maxed) {
				bg.tint( 0xCC3333, selectedBuildingDefense == defense ? 0.45f : 0.3f );
				fill.hardlight( 0xFF5555 );
				icon.hardlight( 0xFFBBBB );
				progress.hardlight( 0xFFBBBB );
			} else {
				fill.hardlight( 0xFFFFFF );
				progress.hardlight( Window.WHITE );
			}
		}
	}

	private class GardenPlot extends Button {

		private final int plot;
		private final ColorBlock rim;
		private final ColorBlock soil;
		private final Image plant;
		private final String label;

		private GardenPlot( int plot ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.plot = plot;

			rim = new ColorBlock( 1, 1, 0xFF6C6959 );
			add( rim );

			soil = new ColorBlock( 1, 1, Dungeon.homebase.moonrootPlotReady( plot ) ? 0xFF445C35 : 0xFF3D2C1D );
			add( soil );

			Plant grown = Dungeon.homebase.moonrootPlant( plot );
			if (grown != null) {
				plant = moonrootPlantVisual( grown );
				label = Messages.get( WndHomebaseFacility.class, "moonroot_ready", Messages.titleCase( grown.name() ) );
				if (plant != null) add( plant );
			} else {
				plant = null;
				label = Messages.get( WndHomebaseFacility.class, "moonroot_empty" );
			}
		}

		@Override
		protected void layout() {
			super.layout();

			rim.x = x;
			rim.y = y;
			rim.size( width, height );

			soil.x = x + 2;
			soil.y = y + 2;
			soil.size( width - 4, height - 4 );

			if (plant != null) {
				plant.x = x + (width - plant.width()) / 2f;
				plant.y = y + (height - plant.height()) / 2f - 1;
				PixelScene.align( plant );
			}
		}

		@Override
		protected void onClick() {
			show( new WndMessage( label ) );
		}

		@Override
		protected void onPointerDown() {
			rim.brightness( 1.4f );
			soil.brightness( 1.25f );
			if (plant != null) plant.brightness( 1.25f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			rim.resetColor();
			soil.resetColor();
			if (plant != null) plant.resetColor();
		}

		@Override
		protected String hoverText() {
			return label;
		}
	}

	private class FacilityTab extends Component {
		private final int tabIndex;
		private ScrollPane pane;

		private FacilityTab( int tabIndex ) {
			this.tabIndex = tabIndex;
		}

		private void rebuild( FacilityContent content ) {
			rememberScroll();
			if (pane != null) {
				remove( pane );
				pane.destroy();
			}
			pane = new ScrollPane( content ) {
				{
					controller.blockLevel = PointerArea.NEVER_BLOCK;
				}
			};
			add( pane );
			pane.setRect( x, y, width, height );
			restoreScroll();
		}

		@Override
		protected void layout() {
			if (pane != null) {
				pane.setRect( x, y, width, height );
				restoreScroll();
			}
		}

		private void rememberScroll() {
			if (pane != null && pane.content() != null && pane.content().camera != null) {
				rememberedScrollY[building.ordinal()][tabIndex] = pane.content().camera.scroll.y;
			}
		}

		private void restoreScroll() {
			if (pane != null) {
				float scrollY = rememberedScrollY[building.ordinal()][tabIndex];
				if (scrollY < CONTENT_TOP_PAD + 2) scrollY = 0;
				pane.scrollTo( 0, scrollY );
			}
		}
	}

	private class ForgeActionButton extends Button {

		private final int iconIndex;
		private final String labelText;
		private final WndBag.ItemSelector selector;
		private final Image bg;
		private final ItemSprite icon;
		private final RenderedTextBlock label;

		private ForgeActionButton( int iconIndex, String labelText, WndBag.ItemSelector selector ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.iconIndex = iconIndex;
			this.labelText = labelText;
			this.selector = selector;

			bg = new Image( Assets.Interfaces.TALENT_BUTTON );
			bg.frame( 0, 0, 20, 26 );
			add( bg );

			icon = new ItemSprite( iconIndex );
			add( icon );

			label = PixelScene.renderTextBlock( 5 );
			label.hardlight( Window.WHITE );
			add( label );
		}

		@Override
		protected void layout() {
			width = STAT_BUTTON_WIDTH;
			height = STAT_BUTTON_HEIGHT;
			super.layout();

			boolean available = Dungeon.homebase != null && Dungeon.homebase.isBuilt( HomebaseState.Building.FORGE );
			bg.x = x + (width - 20) / 2f;
			bg.y = y;
			bg.am = available ? 0.9f : 0.35f;

			icon.x = bg.x + (20 - icon.width()) / 2f;
			icon.y = bg.y + 1 + (18 - icon.height()) / 2f;
			icon.am = available ? 1f : 0.35f;
			PixelScene.align( icon );

			label.text( labelText );
			label.hardlight( available ? Window.WHITE : 0x777777 );
			label.setPos(
					x + (width - label.width()) / 2f,
					bg.y + 20 );
			PixelScene.align( label );
		}

		@Override
		protected void onClick() {
			if (Dungeon.homebase == null || !Dungeon.homebase.isBuilt( HomebaseState.Building.FORGE )) return;
			hide();
			selectItem( selector );
		}

		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			icon.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			bg.resetColor();
			icon.resetColor();
		}

		@Override
		protected String hoverText() {
			return labelText;
		}
	}

	private class FacilityContent extends Component {
		private float pos = CONTENT_TOP_PAD;
		private int slotRowStart = -1;
		private ColorBlock sectionBg;
		private int sectionIndex = 0;

		private void beginSection() {
			endSection();
			if (pos > CONTENT_TOP_PAD + 0.1f) {
				addSectionDivider();
			}
			sectionBg = new ColorBlock( contentWidth, 1, sectionIndex++ % 2 == 0 ? SECTION_BG_A : SECTION_BG_B );
			sectionBg.am = 0.72f;
			sectionBg.x = 0;
			sectionBg.y = pos;
			addToBack( sectionBg );
			pos += SECTION_PAD;
		}

		private void endSection() {
			if (sectionBg == null) return;
			sectionBg.size( contentWidth, Math.max( 1, pos - sectionBg.y + SECTION_PAD ) );
			sectionBg = null;
			pos += SECTION_PAD;
		}

		private void addSectionDivider() {
			ColorBlock divider = new ColorBlock( contentWidth, 1, SECTION_DIVIDER );
			divider.x = 0;
			divider.y = pos;
			add( divider );
			pos += SECTION_PAD + 1;
		}

		private void addText( String text, int color ) {
			RenderedTextBlock block = PixelScene.renderTextBlock( text, 6 );
			block.maxWidth( contentWidth );
			block.hardlight( color );
			block.setPos( 0, pos );
			add( block );
			pos = block.bottom() + GAP;
		}

		private void addCenteredText( String text, int color ) {
			RenderedTextBlock block = PixelScene.renderTextBlock( text, 6 );
			block.maxWidth( contentWidth );
			block.hardlight( color );
			block.setPos( (contentWidth - block.width()) / 2f, pos );
			add( block );
			pos = block.bottom() + GAP;
		}

		private void addCostLine( ResourceCostLine line ) {
			add( line );
			line.setRect( 0, pos, contentWidth, 0 );
			pos = line.bottom() + GAP;
		}

		private void addCurrencyLine( WndCurrencyLine line ) {
			add( line );
			line.setRect( 0, pos, contentWidth, 0 );
			pos = line.bottom() + GAP;
		}

		private void addTrainingGrid( ArrayList<HomebaseState.Training> trainings ) {
			RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( WndHomebaseFacility.class, "stats_title" ), 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (contentWidth - title.width()) / 2f, pos );
			add( title );
			pos = title.bottom() + 4;

			int index = 0;
			while (index < trainings.size()) {
				int rowCount = Math.min( maxGridButtonsPerRow(), trainings.size() - index );
				float gap = (contentWidth - rowCount * STAT_BUTTON_WIDTH) / (rowCount + 1f);
				float left = gap;
				float rowTop = pos;
				for (int i = 0; i < rowCount; i++) {
					TrainingButton button = new TrainingButton( trainings.get( index++ ) );
					add( button );
					button.setPos( left, rowTop );
					left += STAT_BUTTON_WIDTH + gap;
				}
				pos = rowTop + STAT_BUTTON_HEIGHT + 5;
			}
		}

		private void addBuildingDefenseGrid( ArrayList<HomebaseState.BuildingDefense> defenses ) {
			RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( WndHomebaseFacility.class, "defense_title" ), 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (contentWidth - title.width()) / 2f, pos );
			add( title );
			pos = title.bottom() + 4;

			int index = 0;
			while (index < defenses.size()) {
				int rowCount = Math.min( maxGridButtonsPerRow(), defenses.size() - index );
				float gap = (contentWidth - rowCount * STAT_BUTTON_WIDTH) / (rowCount + 1f);
				float left = gap;
				float rowTop = pos;
				for (int i = 0; i < rowCount; i++) {
					BuildingDefenseButton button = new BuildingDefenseButton( defenses.get( index++ ) );
					add( button );
					button.setPos( left, rowTop );
					left += STAT_BUTTON_WIDTH + gap;
				}
				pos = rowTop + STAT_BUTTON_HEIGHT + 5;
			}
		}

		private void addSettlementRequestGrid( ArrayList<HomebaseState.SettlementRequest> requests ) {
			int index = 0;
			while (index < requests.size()) {
				int rowCount = Math.min( maxGridButtonsPerRow(), requests.size() - index );
				float gap = (contentWidth - rowCount * STAT_BUTTON_WIDTH) / (rowCount + 1f);
				float left = gap;
				float rowTop = pos;
				for (int i = 0; i < rowCount; i++) {
					SettlementRequestButton button = new SettlementRequestButton( index, requests.get( index ) );
					add( button );
					button.setPos( left, rowTop );
					left += STAT_BUTTON_WIDTH + gap;
					index++;
				}
				pos = rowTop + STAT_BUTTON_HEIGHT + 5;
			}
		}

		private void addForgeActionGrid() {
			ForgeActionButton salvage = new ForgeActionButton( ItemSpriteSheet.STONE_REFORGE_CONFLUX, "Salvage", salvageSelector );
			ForgeActionButton upgrade = new ForgeActionButton( ItemSpriteSheet.Icons.SCROLL_UPGRADE, "Upgrade", upgradeSelector );
			float gap = (contentWidth - 2 * STAT_BUTTON_WIDTH) / 3f;
			float left = gap;
			float rowTop = pos;
			add( salvage );
			salvage.setPos( left, rowTop );
			add( upgrade );
			upgrade.setPos( left + STAT_BUTTON_WIDTH + gap, rowTop );
			pos = rowTop + STAT_BUTTON_HEIGHT + 5;
		}

		private int maxGridButtonsPerRow() {
			return Math.max( 1, Math.min( 4, (contentWidth - GAP) / STAT_BUTTON_WIDTH ) );
		}

		private void addGardenGrid() {
			int plots = Dungeon.homebase.moonrootPlots();
			if (plots <= 0) return;

			int cols = Math.max( 1, Math.min( GARDEN_COLS, (contentWidth + GARDEN_PLOT_MARGIN) / (GARDEN_PLOT_SIZE + GARDEN_PLOT_MARGIN) ) );
			float gridWidth = cols * GARDEN_PLOT_SIZE + (cols - 1) * GARDEN_PLOT_MARGIN;
			float gridLeft = (contentWidth - gridWidth) / 2f;

			for (int i = 0; i < plots; i++) {
				GardenPlot plot = new GardenPlot( i );
				add( plot );
				plot.setRect(
						gridLeft + (i % cols) * (GARDEN_PLOT_SIZE + GARDEN_PLOT_MARGIN),
						pos + (i / cols) * (GARDEN_PLOT_SIZE + GARDEN_PLOT_MARGIN),
						GARDEN_PLOT_SIZE,
						GARDEN_PLOT_SIZE );
			}

			pos += (int)Math.ceil( plots/(float)cols ) * (GARDEN_PLOT_SIZE + GARDEN_PLOT_MARGIN) - GARDEN_PLOT_MARGIN + GAP;
		}

		private void addButton( RedButton button ) {
			add( button );
			button.setRect( 0, pos, contentWidth, BTN_HEIGHT );
			pos = button.bottom() + GAP;
		}

		private void addSlot( InventorySlot slot, int index ) {
			if (slotRowStart == -1) slotRowStart = (int)pos;
			add( slot );
			int cols = vaultCols();
			slot.setRect(
					(index % cols) * (SLOT_SIZE + SLOT_MARGIN),
					slotRowStart + (index / cols) * (SLOT_SIZE + SLOT_MARGIN),
					SLOT_SIZE,
					SLOT_SIZE );
		}

		private void endSlots( int slots ) {
			if (slotRowStart == -1) return;
			int cols = vaultCols();
			pos = slotRowStart + (int)Math.ceil( slots/(float)cols ) * (SLOT_SIZE + SLOT_MARGIN) - SLOT_MARGIN + GAP;
			slotRowStart = -1;
		}

		private void addGap() {
			pos += GAP;
		}

		private FacilityContent finish() {
			endSection();
			setSize( contentWidth, Math.max( contentHeight, pos + TAB_CONTENT_BOTTOM_PAD ) );
			return this;
		}
	}
}
