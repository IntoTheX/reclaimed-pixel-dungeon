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

package com.erebus.reclaimedpixeldungeon.scenes;

import com.erebus.reclaimedpixeldungeon.Assets;
import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.HomebaseState;
import com.erebus.reclaimedpixeldungeon.journal.Document;
import com.erebus.reclaimedpixeldungeon.journal.ReclaimedTutorial;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.ExitButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.windows.IconTitle;
import com.erebus.reclaimedpixeldungeon.windows.WndEmberforge;
import com.erebus.reclaimedpixeldungeon.windows.WndHomebaseFacility;
import com.erebus.reclaimedpixeldungeon.windows.WndMessage;
import com.erebus.reclaimedpixeldungeon.windows.WndMoonrootGarden;
import com.erebus.reclaimedpixeldungeon.windows.WndVaultStorage;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

public abstract class HomebaseFacilityScene extends PixelScene {

	private static final int RESOURCE_STRIP_HEIGHT = 28;
	private static float waterOffsetY = 0;
	private static HomebaseState.Building selectedDefenseBuilding = HomebaseState.Building.NORTH_WALL;
	private static String resultMessage = "";
	private static HomebaseState.Building resultMessageBuilding = null;
	private static HomebaseFacilityScene activeFacilityScene = null;

	private SkinnedBlock water;
	private ResourceStrip resourceStrip;
	private RenderedTextBlock resultMessageText;
	private float contentW;
	private RectF insets;
	private float modalTopReserve;
	private float modalBottomReserve;

	{
		inGameScene = true;
	}

	@Override
	public void create() {
		super.create();

		int w = Camera.main.width;
		int h = Camera.main.height;
		insets = getCommonInsets();
		activeFacilityScene = this;

		water = new SkinnedBlock(
				w, h,
				Dungeon.level == null ? Assets.Environment.WATER_HOMEBASE : Dungeon.level.waterTex() ) {

			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}

			@Override
			public void draw() {
				Blending.disable();
				super.draw();
				Blending.enable();
			}
		};
		water.autoAdjust = true;
		water.offsetTo( 0, waterOffsetY );
		add( water );

		Image shade = new Image( TextureCache.createGradient( 0x66000000, 0x88000000, 0xAA000000, 0xCC000000, 0xFF000000 ) );
		shade.angle = 90;
		shade.x = w;
		shade.scale.x = h/5f;
		shade.scale.y = w;
		add( shade );

		contentW = w - insets.left - insets.right;

		ExitButton btnExit = new ExitButton() {
			@Override
			protected void onClick() {
				Game.switchScene( GameScene.class );
			}
		};
		btnExit.setPos( insets.left + contentW - btnExit.width(), insets.top );
		add( btnExit );

		IconTitle title = new IconTitle( new ItemSprite( icon() ), title() );
		title.setSize( Math.min( 200, w - 28 ), 0 );
		title.setPos( insets.left + (contentW - title.reqWidth()) / 2f, insets.top + 6 );
		align( title );
		add( title );

		RenderedTextBlock desc = PixelScene.renderTextBlock( text(), 6 );
		desc.maxWidth( Math.min( 180, w - 28 ) );
		desc.setPos( insets.left + (contentW - desc.width()) / 2f, title.bottom() + 6 );
		align( desc );
		add( desc );
		modalTopReserve = desc.bottom() + 6;

		float stripW = Math.min( 240, contentW );
		resourceStrip = new ResourceStrip();
		resourceStrip.setRect(
				insets.left + (contentW - stripW) / 2f,
				h - insets.bottom - RESOURCE_STRIP_HEIGHT - 8,
				stripW,
				RESOURCE_STRIP_HEIGHT );
		align( resourceStrip );
		add( resourceStrip );
		modalBottomReserve = h - resourceStrip.top() + 4;

		resultMessageText = PixelScene.renderTextBlock( 6 );
		resultMessageText.visible = false;
		add( resultMessageText );
		refreshResultMessage();

		addToFront( window() );
		bringToFront( btnExit );
		queueFacilityTutorial();
		fadeIn();
	}

	private void queueFacilityTutorial() {
		switch (building()) {
			case CAMP:
				ReclaimedTutorial.flash( Document.GUIDE_FOUNDERS_CAMP );
				break;
			case VAULT:
				ReclaimedTutorial.flash( Document.GUIDE_QUARTERMASTER_VAULT );
				break;
			case FORGE:
				ReclaimedTutorial.flash( Document.GUIDE_EMBERFORGE );
				break;
			case ALCHEMY:
				ReclaimedTutorial.flash( Document.GUIDE_ALCHEMISTS_STILL );
				break;
			case GARDEN:
				ReclaimedTutorial.flash( Document.GUIDE_MOONROOT_GARDEN );
				break;
			default:
				if (isDefenseStructure( building() )) {
					ReclaimedTutorial.flash( Document.GUIDE_DEFENSE_WALLS );
				}
				break;
		}
	}

	@Override
	public void destroy() {
		if (activeFacilityScene == this) {
			activeFacilityScene = null;
			resultMessageBuilding = null;
			resultMessage = "";
		}
		super.destroy();
	}

	@Override
	public void update() {
		super.update();
		if (water != null) {
			water.offset( 0, -5 * Game.elapsed );
			waterOffsetY = water.offsetY();
		}
	}

		private class ResourceStrip extends Component {

		private final ResourceButton[] materialButtons = new ResourceButton[HomebaseState.Material.values().length];
		private final ResourceButton[] forgeButtons = new ResourceButton[HomebaseState.ForgeResource.values().length];
		private final ResourceButton[] currencyButtons = new ResourceButton[3];

		private ResourceStrip() {
			for (HomebaseState.Material material : HomebaseState.Material.values()) {
				ResourceButton button = new ResourceButton(
						WndHomebaseFacility.materialIcon( material ),
						WndHomebaseFacility.materialName( material ),
						Dungeon.homebase == null ? 0 : Dungeon.homebase.amount( material ),
						WndHomebaseFacility.materialColor( material ) );
				materialButtons[material.ordinal()] = button;
				add( button );
			}

			for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
				ResourceButton button = new ResourceButton(
						WndHomebaseFacility.forgeIcon( resource ),
						WndHomebaseFacility.forgeName( resource ),
						Dungeon.homebase == null ? 0 : Dungeon.homebase.forgeResourceAmount( resource ),
						WndHomebaseFacility.forgeColor( resource ) );
				forgeButtons[resource.ordinal()] = button;
				add( button );
			}

			currencyButtons[0] = new ResourceButton(
					Icons.get( Icons.COIN_SML ),
					WndHomebaseFacility.goldName(),
					Dungeon.homebase == null ? 0 : Dungeon.homebase.goldAmount(),
					WndHomebaseFacility.goldColor() );
			add( currencyButtons[0] );

			currencyButtons[1] = new ResourceButton(
					Icons.get( Icons.ENERGY_SML ),
					WndHomebaseFacility.energyName(),
					Dungeon.homebase == null ? 0 : Dungeon.homebase.energyAmount(),
					WndHomebaseFacility.energyColor() );
			add( currencyButtons[1] );

			currencyButtons[2] = new ResourceButton(
					WndHomebaseFacility.emeraldIcon(),
					WndHomebaseFacility.emeraldName(),
					Dungeon.homebase == null ? 0 : Dungeon.homebase.emeraldAmount(),
					WndHomebaseFacility.emeraldColor() );
			add( currencyButtons[2] );
		}

		@Override
		protected void layout() {
			float materialCell = width / HomebaseState.Material.values().length;
			for (int i = 0; i < materialButtons.length; i++) {
				materialButtons[i].setRect( x + i * materialCell, y, materialCell, 10 );
			}

			float forgeCell = width / (currencyButtons.length + forgeButtons.length);
			for (int i = 0; i < currencyButtons.length; i++) {
				currencyButtons[i].setRect( x + i * forgeCell, y + 12, forgeCell, 10 );
			}
			for (int i = 0; i < forgeButtons.length; i++) {
				forgeButtons[i].setRect( x + (currencyButtons.length + i) * forgeCell, y + 12, forgeCell, 10 );
			}
		}
	}

	private class ResourceButton extends Button {

		private final Image icon;
		private final RenderedTextBlock amountText;
		private final String resourceName;
		private final int amount;
		private final int color;

		private ResourceButton( int iconId, String resourceName, int amount, int color ) {
			this( new ItemSprite( iconId ), resourceName, amount, color );
		}

		private ResourceButton( Image icon, String resourceName, int amount, int color ) {
			super();
			hotArea.blockLevel = PointerArea.NEVER_BLOCK;
			this.resourceName = resourceName;
			this.amount = amount;
			this.color = color;

			this.icon = icon;
			this.icon.resetColor();
			add( icon );

			amountText = PixelScene.renderTextBlock( 5 );
			amountText.text( WndHomebaseFacility.compactAmount( amount ) );
			amountText.hardlight( color );
			add( amountText );
		}

		@Override
		protected void layout() {
			super.layout();
			float totalWidth = icon.width() + 1 + amountText.width();
			icon.x = x + (width - totalWidth) / 2f;
			icon.y = y + (height - icon.height()) / 2f;
			amountText.setPos( icon.x + icon.width() + 1, y + (height - amountText.height()) / 2f );
			align( icon );
			align( amountText );
		}

		@Override
		protected void onClick() {
			addToFront( new WndMessage( hoverText() ) );
		}

		@Override
		protected void onPointerDown() {
			icon.brightness( 1.5f );
			amountText.hardlight( Window.WHITE );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			icon.resetColor();
			amountText.hardlight( color );
		}

		@Override
		protected String hoverText() {
			return resourceName + ": " + amount;
		}
	}

	protected abstract int icon();

	protected abstract String title();

	protected abstract String text();

	protected abstract Window window();

	protected abstract HomebaseState.Building building();

	public static Class<? extends PixelScene> sceneFor( HomebaseState.Building building ) {
		if (Dungeon.homebase != null) {
			building = Dungeon.homebase.interactionBuilding( building );
		}
		switch (building) {
			case VAULT:
				return Vault.class;
			case FORGE:
				return Emberforge.class;
			case ALCHEMY:
				return Alchemy.class;
			case GARDEN:
				return Garden.class;
			case CAMP:
				return FoundersCamp.class;
			default:
				if (isDefenseStructure( building )) {
					selectedDefenseBuilding = building;
					return DefenseStructure.class;
				}
				return FoundersCamp.class;
		}
	}

	public static void setResultMessage( HomebaseState.Building building, String message ) {
		resultMessageBuilding = building;
		resultMessage = message == null ? "" : message;
		if (activeFacilityScene != null) {
			activeFacilityScene.refreshResultMessage();
		}
	}

	public static void setResultMessage( String message ) {
		setResultMessage( null, message );
	}

	public static int modalTopReserve() {
		return activeFacilityScene == null ? 0 : (int)Math.ceil( activeFacilityScene.modalTopReserve );
	}

	public static int modalBottomReserve() {
		return activeFacilityScene == null ? 0 : (int)Math.ceil( activeFacilityScene.modalBottomReserve );
	}

	private void refreshResultMessage() {
		if (resultMessageText == null || resourceStrip == null) return;
		if (resultMessage == null || resultMessage.isEmpty() || building() != resultMessageBuilding) {
			resultMessageText.visible = false;
			return;
		}

		resultMessageText.text( resultMessage, Math.min( 220, Camera.main.width - 24 ) );
		resultMessageText.hardlight( Window.TITLE_COLOR );
		resultMessageText.setPos(
				insets.left + (contentW - resultMessageText.width()) / 2f,
				resourceStrip.top() - resultMessageText.height() - 4 );
		align( resultMessageText );
		resultMessageText.visible = true;
	}

	private static boolean isDefenseStructure( HomebaseState.Building building ) {
		switch (building) {
			case NORTH_WALL:
			case EAST_WALL:
			case SOUTH_WALL:
			case WEST_WALL:
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return true;
			default:
				return false;
		}
	}

	private static boolean isTower( HomebaseState.Building building ) {
		switch (building) {
			case NORTHWEST_TOWER:
			case NORTHEAST_TOWER:
			case SOUTHWEST_TOWER:
			case SOUTHEAST_TOWER:
				return true;
			default:
				return false;
		}
	}

	private static String defenseTitle( HomebaseState.Building building ) {
		switch (building) {
			case NORTH_WALL:
				return "North Wall";
			case EAST_WALL:
				return "East Wall";
			case SOUTH_WALL:
				return "South Wall";
			case WEST_WALL:
				return "West Wall";
			case NORTHWEST_TOWER:
				return "Northwest Tower";
			case NORTHEAST_TOWER:
				return "Northeast Tower";
			case SOUTHWEST_TOWER:
				return "Southwest Tower";
			case SOUTHEAST_TOWER:
				return "Southeast Tower";
			default:
				return "Settlement Defense";
		}
	}

	public static class FoundersCamp extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return ItemSpriteSheet.KIT;
		}

		@Override
		protected String title() {
			return Messages.get( this, "title" );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return HomebaseState.Building.CAMP;
		}
	}

	public static class Emberforge extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return ItemSpriteSheet.BUILDING_IRON;
		}

		@Override
		protected String title() {
			return Messages.get( WndEmberforge.class, "title" );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return HomebaseState.Building.FORGE;
		}
	}

	public static class Vault extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return ItemSpriteSheet.CHEST;
		}

		@Override
		protected String title() {
			return Messages.get( WndVaultStorage.class, "title" );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return HomebaseState.Building.VAULT;
		}
	}

	public static class Alchemy extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return ItemSpriteSheet.POTION_HOLDER;
		}

		@Override
		protected String title() {
			return Messages.get( this, "title" );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return HomebaseState.Building.ALCHEMY;
		}
	}

	public static class Garden extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return ItemSpriteSheet.SEED_STARFLOWER;
		}

		@Override
		protected String title() {
			return Messages.get( WndMoonrootGarden.class, "title" );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return HomebaseState.Building.GARDEN;
		}
	}

	public static class DefenseStructure extends HomebaseFacilityScene {
		@Override
		protected int icon() {
			return building().name().contains( "TOWER" ) ? ItemSpriteSheet.BUILDING_IRON : ItemSpriteSheet.BUILDING_STONE;
		}

		@Override
		protected String title() {
			return defenseTitle( building() );
		}

		@Override
		protected String text() {
			return Messages.get( this, "text" );
		}

		@Override
		protected Window window() {
			return new WndHomebaseFacility( building() );
		}

		@Override
		protected HomebaseState.Building building() {
			return selectedDefenseBuilding;
		}
	}
}
