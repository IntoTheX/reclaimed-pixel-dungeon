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
import com.erebus.reclaimedpixeldungeon.items.Item;
import com.erebus.reclaimedpixeldungeon.items.bags.Bag;
import com.erebus.reclaimedpixeldungeon.levels.WayfarerExchangeLevel;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerExchangeService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerTradePayload;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.CharSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.InventorySlot;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.erebus.reclaimedpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndWayfarerExchange extends Window {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 160 );
	private static final int HEIGHT = 140;
	private static final int GAP = 4;

	private static final int TRADE_SLOT_SIZE = 32;
	private static final int TRADE_SLOT_GAP = 4;

	private static final int RESOURCE_ROW_HEIGHT = 16;
	private static final int RESOURCE_ICON_SIZE = 12;
	private static final int RESOURCE_PLUS_SIZE = 16;
	private static final int RESOURCE_COLUMN_GAP = 4;

	private static final int DIVIDER_MARGIN = 6;
	private static final int DIVIDER_COLOR = 0xFF777777;
	private static final int DIVIDER_GAP = 4;

	/*
	* Remote rows do not have a plus button, but keeping the same height
	* makes both halves of the trading window visually consistent.
	*/
	private static final int REMOTE_RESOURCE_ROW_HEIGHT = 14;

	private final boolean hostMode;
	private ScrollPane pane;
	private Component content;
	private float pos;
	private float refresh;
	private String lastStatus = "";
	private int lastHostCount = -1;
	private int lastTradeRevision = -1;
	private float localNameTicker = 0;
	private int resourceRowColumn = 0;

	public WndWayfarerExchange( boolean hostMode ) {
		this( hostMode, true );
	}

	public WndWayfarerExchange( boolean hostMode, boolean startNetwork ) {
		super();
		this.hostMode = hostMode;

		content = new Component();
		pane = new ScrollPane( content );
		add( pane );

		if (startNetwork) {
			WayfarerAccountService.currentCharacterRestricted( (result, restricted) -> {
				if (parent == null) return;
				if (!result.success || restricted) {
					GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Wayfarer Access Restricted",
							result.message, "Close" ) );
					hide();
					return;
				}
				if (hostMode) WayfarerExchangeService.startHost( traderName(), traderClass(), traderArmorTier() );
				else WayfarerExchangeService.startSearch( traderName(), traderClass(), traderArmorTier() );
			} );
		}

		resize( WIDTH, HEIGHT );
		pane.setRect( 0, 0, WIDTH, HEIGHT );
		rebuild();
	}

	@Override
	public void update() {
		super.update();
		if (Dungeon.level instanceof WayfarerExchangeLevel) {
			((WayfarerExchangeLevel)Dungeon.level).syncRemoteTrader();
			localNameTicker -= Game.elapsed;
			if (Dungeon.hero != null && Dungeon.hero.sprite != null && localNameTicker <= 0) {
				Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, traderName() );
				localNameTicker = 2.5f;
			}
		} else if (!hostMode && WayfarerExchangeService.mode() == WayfarerExchangeService.Mode.CONNECTED) {
			hide();
			WayfarerExchangeLevel.enter( false );
			return;
		}
		refresh -= Game.elapsed;
		ArrayList<WayfarerExchangeService.HostInfo> hosts = WayfarerExchangeService.discoveredHosts();
		if (refresh <= 0 || !lastStatus.equals( WayfarerExchangeService.status() )
				|| lastHostCount != hosts.size()
				|| lastTradeRevision != WayfarerExchangeService.tradeRevision()) {
			refresh = 0.5f;
			rebuild( hosts );
		}
	}

	private void rebuild() {
		rebuild( WayfarerExchangeService.discoveredHosts() );
	}

	private boolean canRebuildInPlace() {
		return alive
				&& exists
				&& content != null
				&& pane != null
				&& content.exists
				&& pane.exists
				&& content.parent != null
				&& pane.parent != null;
	}

	private boolean canRenderContent() {
		return alive
				&& exists
				&& content != null
				&& pane != null
				&& content.exists
				&& pane.exists;
	}

	private void safeRebuild() {
		if (canRebuildInPlace()) {
			rebuild();
		} else {
			Game.runOnRenderThread( new com.watabou.utils.Callback() {
				@Override
				public void call() {
					GameScene.show( new WndWayfarerExchange( hostMode, false ) );
				}
			} );
		}
	}

	private void rebuild( ArrayList<WayfarerExchangeService.HostInfo> hosts ) {
		if (!canRenderContent()) {
			return;
		}
		content.clear();
		pos = 3;
		lastStatus = WayfarerExchangeService.status();
		lastHostCount = hosts.size();
		lastTradeRevision = WayfarerExchangeService.tradeRevision();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get( this, hostMode ? "host_title" : "join_title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.maxWidth( WIDTH - 6 );
		title.setPos( (WIDTH - title.width()) / 2f, pos );
		PixelScene.align( title );
		content.add( title );
		pos = title.bottom() + GAP;

		RenderedTextBlock instructions = PixelScene.renderTextBlock(
				Messages.get( this, hostMode ? "host_info" : "join_info" ), 6 );
		instructions.maxWidth( WIDTH - 6 );
		instructions.setPos( 3, pos );
		content.add( instructions );
		pos = instructions.bottom() + GAP;

		RenderedTextBlock status = PixelScene.renderTextBlock( WayfarerExchangeService.status(), 6 );
		status.maxWidth( WIDTH - 6 );
		status.hardlight( statusColor() );
		status.setPos( 3, pos );
		content.add( status );
		pos = status.bottom() + GAP;

		if (WayfarerExchangeService.mode() == WayfarerExchangeService.Mode.CONNECTED && WayfarerExchangeService.tradeReady()) {
			addTradePreview();
		} else if (WayfarerExchangeService.lobbyReady()) {
			addLobbyView();
		} else if (WayfarerExchangeService.hasDisconnectMessage()) {
			RenderedTextBlock disconnected = PixelScene.renderTextBlock( WayfarerExchangeService.disconnectMessage(), 7 );
			disconnected.maxWidth( WIDTH - 6 );
			disconnected.hardlight( 0xFFAA33 );
			disconnected.setPos( 3, pos );
			content.add( disconnected );
			pos = disconnected.bottom() + GAP;
		} else if (!hostMode) {
			if (hosts.isEmpty()) {
				RenderedTextBlock empty = PixelScene.renderTextBlock( Messages.get( this, "no_hosts" ), 6 );
				empty.maxWidth( WIDTH - 6 );
				empty.hardlight( 0xAAAAAA );
				empty.setPos( 3, pos );
				content.add( empty );
				pos = empty.bottom() + GAP;
			} else {
				for (final WayfarerExchangeService.HostInfo host : hosts) {
					RedButton hostButton = new RedButton( host.summary(), 6 ) {
						@Override
						protected void onClick() {
							super.onClick();
							WayfarerExchangeService.connectTo( host, traderName(), traderClass(), traderArmorTier() );
							safeRebuild();
						}
					};
					hostButton.multiline = true;
					hostButton.setRect( 3, pos, WIDTH - 6, 18 );
					content.add( hostButton );
					pos = hostButton.bottom() + GAP;
				}
			}
		}

		RedButton restart = new RedButton( Messages.get( this, hostMode ? "restart_host" : "restart_search" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (hostMode) {
					WayfarerExchangeService.startHost( traderName(), traderClass(), traderArmorTier() );
				} else {
					WayfarerExchangeService.startSearch( traderName(), traderClass(), traderArmorTier() );
				}
				safeRebuild();
			}
		};
		restart.setRect( 3, pos, (WIDTH - 9) / 2f, 18 );
		content.add( restart );

		RedButton stop = new RedButton(
				Dungeon.level instanceof WayfarerExchangeLevel && WayfarerExchangeService.tradeReady()
						? "Back to Lobby"
						: Messages.get( this, "stop" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (Dungeon.level instanceof WayfarerExchangeLevel && WayfarerExchangeService.tradeReady()) {
					WayfarerExchangeService.endCurrentTrade();
					hide();
					Game.runOnRenderThread( new com.watabou.utils.Callback() {
						@Override
						public void call() {
							GameScene.show( new WndWayfarerExchange( hostMode, false ) );
						}
					} );
					return;
				}
				hide();
				if (Dungeon.level instanceof WayfarerExchangeLevel) {
					boolean notifyPeer = ((WayfarerExchangeLevel)Dungeon.level).hostSide();
					WayfarerExchangeService.closeExchange( notifyPeer );
					WayfarerExchangeLevel.returnHomebase();
				} else {
					WayfarerExchangeService.stop();
				}
			}
		};
		stop.setRect( restart.right() + GAP, pos, (WIDTH - 9) / 2f, 18 );
		content.add( stop );
		pos = stop.bottom() + GAP;

		content.setSize( pane.width(), Math.max( pane.height(), pos + GAP ) );
		pane.setSize( pane.width(), pane.height() );
	}

	private void addLobbyView() {
		String incoming = WayfarerExchangeService.incomingRequestFrom();
		if (incoming != null && !incoming.isEmpty()) {
			RenderedTextBlock request = PixelScene.renderTextBlock(
					WayfarerExchangeService.peerName( incoming ) + " is requesting to trade.", 6 );
			request.maxWidth( WIDTH - 6 );
			request.hardlight( TITLE_COLOR );
			request.setPos( 3, pos );
			content.add( request );
			pos = request.bottom() + GAP;

			RedButton accept = new RedButton( "Accept", 6 ) {
				@Override
				protected void onClick() {
					super.onClick();
					WayfarerExchangeService.acceptTradeRequest();
					safeRebuild();
				}
			};
			accept.setRect( 3, pos, (WIDTH - 9) / 2f, 18 );
			content.add( accept );

			RedButton decline = new RedButton( "Decline", 6 ) {
				@Override
				protected void onClick() {
					super.onClick();
					WayfarerExchangeService.declineTradeRequest();
					safeRebuild();
				}
			};
			decline.setRect( accept.right() + GAP, pos, (WIDTH - 9) / 2f, 18 );
			content.add( decline );
			pos = decline.bottom() + GAP;
			addHorizontalDivider();
		} else if ("Trade Request Expired.".equals( WayfarerExchangeService.requestNotice() )) {
			RenderedTextBlock expired = PixelScene.renderTextBlock( WayfarerExchangeService.requestNotice(), 7 );
			expired.maxWidth( WIDTH - 6 );
			expired.hardlight( 0xFFAA33 );
			expired.setPos( 3, pos );
			content.add( expired );
			pos = expired.bottom() + GAP;

			RedButton back = new RedButton( "Back", 6 ) {
				@Override
				protected void onClick() {
					super.onClick();
					WayfarerExchangeService.clearRequestNotice();
					hide();
				}
			};
			back.setRect( 3, pos, WIDTH - 6, 18 );
			content.add( back );
			pos = back.bottom() + GAP;
			addHorizontalDivider();
		}

		String outgoing = WayfarerExchangeService.outgoingRequestTo();
		if (outgoing != null && !outgoing.isEmpty()) {
			RenderedTextBlock waiting = PixelScene.renderTextBlock(
					"Requesting to trade with " + WayfarerExchangeService.peerName( outgoing ) + "...", 6 );
			waiting.maxWidth( WIDTH - 6 );
			waiting.hardlight( 0x66CCFF );
			waiting.setPos( 3, pos );
			content.add( waiting );
			pos = waiting.bottom() + GAP;
			addHorizontalDivider();
		}

		ArrayList<WayfarerExchangeService.PeerInfo> peers = WayfarerExchangeService.lobbyPeers();
		RenderedTextBlock roster = PixelScene.renderTextBlock(
				peers.isEmpty() ? "No other traders are in this exchange yet." : "Traders in this exchange:", 6 );
		roster.maxWidth( WIDTH - 6 );
		roster.hardlight( SHPX_COLOR );
		roster.setPos( 3, pos );
		content.add( roster );
		pos = roster.bottom() + GAP;

		for (final WayfarerExchangeService.PeerInfo peer : peers) {
			if (peer == null) continue;
			String label = peer.name() + " (" + peer.profile.heroClass + ")";
			if (peer.busy) {
				RenderedTextBlock busy = PixelScene.renderTextBlock( label + " is currently in a Trade.", 6 );
				busy.maxWidth( WIDTH - 6 );
				busy.hardlight( 0xAAAAAA );
				busy.setPos( 3, pos );
				content.add( busy );
				pos = busy.bottom() + GAP;
			} else {
				RedButton trade = new RedButton( "Trade with " + peer.name(), 6 ) {
					@Override
					protected void onClick() {
						super.onClick();
						WayfarerExchangeService.requestTrade( peer.id );
						safeRebuild();
					}
				};
				trade.multiline = true;
				trade.setRect( 3, pos, WIDTH - 6, 18 );
				content.add( trade );
				pos = trade.bottom() + GAP;
			}
		}
	}

	private void addTradePreview() {
		RenderedTextBlock peer = PixelScene.renderTextBlock(
				Messages.get( this, "connected_to", WayfarerExchangeService.connectedPeer() ), 6 );
		peer.maxWidth( WIDTH - 6 );
		peer.hardlight( SHPX_COLOR );
		peer.setPos( 3, pos );
		content.add( peer );
		pos = peer.bottom();

		addHorizontalDivider();

		addLocalOfferTitle();
		addLocalItemSlots();
		addResourceButtons();

		addHorizontalDivider();

		addRemoteOfferBlock(
				Messages.get( this, "remote_offer" ),
				WayfarerExchangeService.remotePayload(),
				WayfarerExchangeService.remoteConfirmed()
		);

		addTradeFeeLine();

		RedButton confirm = new RedButton( Messages.get( this, "confirm_offer" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				WayfarerExchangeService.confirmOffer();
				safeRebuild();
			}
		};
		confirm.enable( !WayfarerExchangeService.localPayload().isEmpty()
				&& !WayfarerExchangeService.remotePayload().isEmpty()
				&& !WayfarerExchangeService.finalized()
				&& Dungeon.homebase != null
				&& Dungeon.homebase.emeraldAmount() >= 1 );
		confirm.setRect( 3, pos, (WIDTH - 9) / 2f, 18 );
		content.add( confirm );

		RedButton clear = new RedButton( Messages.get( this, "clear_offer" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				WayfarerExchangeService.clearOffer();
				safeRebuild();
			}
		};
		clear.setRect( confirm.right() + GAP, pos, (WIDTH - 9) / 2f, 18 );
		content.add( clear );
		pos = clear.bottom() + GAP;

		if (WayfarerExchangeService.localConfirmed() && WayfarerExchangeService.remoteConfirmed()) {
			String error = WayfarerExchangeService.finalizeTrade();
			if (error == null || error.isEmpty()) {
				GLog.p( Messages.get( this, "trade_complete" ) );
				GameScene.show( new WndWayfarerTradeReceipt( WayfarerExchangeService.consumeReceiptPayload() ) );
			} else {
				GLog.w( Messages.get( this, "trade_failed", error ) );
			}
		}
	}

	private void addTradeFeeLine() {
		float lineY = pos;
		RenderedTextBlock label = PixelScene.renderTextBlock( "Trade fee:", 6 );
		label.hardlight( Window.TITLE_COLOR );
		label.setPos( 3, lineY );
		content.add( label );

		Image emerald = new ItemSprite( WndHomebaseFacility.emeraldIcon() );
		emerald.x = label.right() + 3;
		emerald.y = lineY + (label.height() - emerald.height()) / 2f;
		PixelScene.align( emerald );
		content.add( emerald );

		int owned = Dungeon.homebase == null ? 0 : Dungeon.homebase.emeraldAmount();
		RenderedTextBlock amount = PixelScene.renderTextBlock( owned + "/1 each", 6 );
		amount.hardlight( owned >= 1 ? WndHomebaseFacility.emeraldColor() : 0xFF5555 );
		amount.setPos( emerald.x + emerald.width() + 2, lineY );
		content.add( amount );

		pos = Math.max( label.bottom(), amount.bottom() ) + GAP;
	}

	/**
	 * Adds a thin horizontal divider across the trading content.
	 *
	 * The divider leaves a small margin on both sides and advances
	 * the shared vertical layout position after it is added.
	 */
	private void addHorizontalDivider() {
		pos += DIVIDER_GAP;

		ColorBlock divider = new ColorBlock(
				WIDTH - DIVIDER_MARGIN * 2,
				1,
				DIVIDER_COLOR
		);

		divider.x = DIVIDER_MARGIN;
		divider.y = pos;

		PixelScene.align( divider );
		content.add( divider );

		pos += 1 + DIVIDER_GAP;
	}

	private void addLocalOfferTitle() {
		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get( this, "local_offer" ),
				6
		);

		title.maxWidth( WIDTH - 6 );
		title.hardlight(
				WayfarerExchangeService.localConfirmed()
						? 0x66FF66
						: TITLE_COLOR
		);

		title.setPos(
				(WIDTH - title.width()) / 2f,
				pos
		);

		PixelScene.align( title );
		content.add( title );

		pos = title.bottom() + GAP;
	}

	private void addLocalItemSlots() {
		float slotsWidth =
				TRADE_SLOT_SIZE * WayfarerTradePayload.ITEM_SLOTS
						+ TRADE_SLOT_GAP
						* (WayfarerTradePayload.ITEM_SLOTS - 1);

		float slotsX = (WIDTH - slotsWidth) / 2f;

		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			final int slotIndex = i;
			final Item offeredItem =
					WayfarerExchangeService.localEscrowItem( slotIndex );

			InventorySlot slot = new InventorySlot( offeredItem ) {
				@Override
				protected void onClick() {
					onItemSlot( slotIndex );
				}

				@Override
				protected boolean onLongClick() {
					if (offeredItem == null) {
						onItemSlot( slotIndex );
					} else {
						GameScene.show(
								new WndInfoItem( offeredItem )
						);
					}

					return true;
				}
			};

			slot.setRect(
					slotsX
							+ slotIndex
							* (TRADE_SLOT_SIZE + TRADE_SLOT_GAP),
					pos,
					TRADE_SLOT_SIZE,
					TRADE_SLOT_SIZE
			);

			if (offeredItem == null) {
				/*
				* Removes InventorySlot's unknown-item placeholder while
				* keeping the slot itself active and clickable.
				*
				* Do not disable the slot. Empty local slots must still
				* respond to clicks so the inventory selector can open.
				*/
				slot.clear();
			}

			content.add( slot );
		}

		pos += TRADE_SLOT_SIZE + GAP;
	}

	private void addResourceButtons() {
		resourceRowColumn = 0;

		addResourceRow(
				Messages.get( this, "gold" ),
				goldIcon(),
				WayfarerExchangeService.localPayload().gold(),
				Dungeon.homebase == null
						? 0
						: Dungeon.homebase.goldAmount(),
				new ResourceSetter() {
					@Override
					public void set( int amount ) {
						WayfarerExchangeService.setGold( amount );
					}
				}
		);

		addResourceRow(
				Messages.get( this, "energy" ),
				energyIcon(),
				WayfarerExchangeService.localPayload().energy(),
				Dungeon.homebase == null
						? 0
						: Dungeon.homebase.energyAmount(),
				new ResourceSetter() {
					@Override
					public void set( int amount ) {
						WayfarerExchangeService.setEnergy( amount );
					}
				}
		);

		for (final HomebaseState.Material material :
				HomebaseState.Material.values()) {

			addResourceRow(
					WndHomebaseFacility.materialName( material ),
					materialIcon( material ),
					WayfarerExchangeService
							.localPayload()
							.material( material ),
					Dungeon.homebase == null
							? 0
							: Dungeon.homebase.amount( material ),
					new ResourceSetter() {
						@Override
						public void set( int amount ) {
							WayfarerExchangeService.setMaterial(
									material,
									amount
							);
						}
					}
			);
		}

		for (final HomebaseState.ForgeResource resource :
				HomebaseState.ForgeResource.values()) {

			addResourceRow(
					WndHomebaseFacility.forgeName( resource ),
					forgeIcon( resource ),
					WayfarerExchangeService
							.localPayload()
							.forge( resource ),
					Dungeon.homebase == null
							? 0
							: Dungeon.homebase
							.forgeResourceAmount( resource ),
					new ResourceSetter() {
						@Override
						public void set( int amount ) {
							WayfarerExchangeService
									.setForgeResource(
											resource,
											amount
									);
						}
					}
			);
		}

		/*
		* If the final row only occupied the left column,
		* advance past that row.
		*/
		if (resourceRowColumn == 1) {
			resourceRowColumn = 0;
			pos += RESOURCE_ROW_HEIGHT + 2;
		}

		pos += GAP;
	}

	private void addResourceRow(
			final String name,
			Image icon,
			int offered,
			final int owned,
			final ResourceSetter setter
	) {
		float columnWidth =
				(WIDTH - 6 - RESOURCE_COLUMN_GAP) / 2f;

		float x =
				3
						+ resourceRowColumn
						* (columnWidth + RESOURCE_COLUMN_GAP);

		float rowY = pos;

		/*
		* Resource icon
		*/
		if (icon != null) {

			float iconScale = Math.min(
					RESOURCE_ICON_SIZE / icon.width,
					RESOURCE_ICON_SIZE / icon.height
			);

			icon.scale.set( iconScale );

			icon.x = x;
			icon.y = rowY
					+ (RESOURCE_ROW_HEIGHT - icon.height()) / 2f;

			PixelScene.align( icon );
			content.add( icon );
		}

		/*
		* Offered amount / currently owned amount
		*/
		String amountText =
				WndHomebaseFacility.compactAmount( offered )
						+ "/"
						+ WndHomebaseFacility.compactAmount( owned );

		RenderedTextBlock amount = PixelScene.renderTextBlock(
				amountText,
				6
		);

		float amountX =
				x
						+ RESOURCE_ICON_SIZE
						+ 2;

		float plusX =
				x
						+ columnWidth
						- RESOURCE_PLUS_SIZE;

		float amountWidth =
				plusX
						- amountX
						- 2;

		amount.maxWidth( (int)amountWidth );
		amount.setPos(
				amountX,
				rowY
						+ (RESOURCE_ROW_HEIGHT
						- amount.height()) / 2f
		);

		content.add( amount );

		/*
		* Red plus button
		*/
		RedButton plus = new RedButton( "+", 9 ) {
			@Override
			protected void onClick() {
				super.onClick();
				showAmountInput(
						name,
						offered,
						owned,
						setter
				);
			}
		};

		plus.setRect(
				plusX,
				rowY,
				RESOURCE_PLUS_SIZE,
				RESOURCE_ROW_HEIGHT
		);

		content.add( plus );

		resourceRowColumn++;

		if (resourceRowColumn >= 2) {
			resourceRowColumn = 0;
			pos += RESOURCE_ROW_HEIGHT + 2;
		}
	}

	private Image goldIcon() {
		return Icons.get(Icons.COIN_SML);
	}

	private Image energyIcon() {
		return Icons.get(Icons.ENERGY_SML);
	}

	private Image materialIcon(HomebaseState.Material material) {
		return new ItemSprite(
				WndHomebaseFacility.materialIcon(material)
		);
	}

	private Image forgeIcon(HomebaseState.ForgeResource resource) {
		return new ItemSprite(
				WndHomebaseFacility.forgeIcon(resource)
		);
	}

	private void addRemoteOfferBlock(
			String label,
			WayfarerTradePayload payload,
			boolean confirmed
	) {
		RenderedTextBlock title = PixelScene.renderTextBlock( label, 6 );
		title.maxWidth( WIDTH - 6 );
		title.hardlight( confirmed ? 0x66FF66 : TITLE_COLOR );
		title.setPos((WIDTH - title.width()) / 2f, pos);
		PixelScene.align( title );
		content.add( title );

		pos = title.bottom() + GAP;

		if (payload == null) {
			payload = new WayfarerTradePayload();
		}

		final WayfarerTradePayload remotePayload = payload;

		float slotsWidth =
				TRADE_SLOT_SIZE * WayfarerTradePayload.ITEM_SLOTS
				+ TRADE_SLOT_GAP * (WayfarerTradePayload.ITEM_SLOTS - 1);

		float slotsX = (WIDTH - slotsWidth) / 2f;

		boolean hasRemoteItem = false;

		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			final Item remoteItem = remotePayload.item( i );

			if (remoteItem != null) {
				hasRemoteItem = true;
			}

			InventorySlot slot = new InventorySlot( remoteItem ) {
				@Override
				protected void onClick() {
					if (remoteItem == null) {
						return;
					}

					GameScene.show( new WndInfoItem( remoteItem ) );
				}

				@Override
				protected boolean onLongClick() {
					if (remoteItem == null) {
						return false;
					}

					GameScene.show( new WndInfoItem( remoteItem ) );
					return true;
				}
			};

			slot.setRect(
					slotsX + i * (TRADE_SLOT_SIZE + TRADE_SLOT_GAP),
					pos,
					TRADE_SLOT_SIZE,
					TRADE_SLOT_SIZE
			);

			if (remoteItem == null) {
				slot.clear();
				slot.enable( false );
			}

			content.add( slot );
			RenderedTextBlock itemName = PixelScene.renderTextBlock(
					remoteItem == null
							? "-"
							: Messages.titleCase( remoteItem.name() ),
					5
			);

			itemName.maxWidth( TRADE_SLOT_SIZE );
			itemName.setPos(
					slotsX + i * (TRADE_SLOT_SIZE + TRADE_SLOT_GAP),
					pos + TRADE_SLOT_SIZE + 1
			);

			content.add( itemName );
		}

		pos += TRADE_SLOT_SIZE + 12 + GAP;

		boolean hasRemoteResource = addRemoteResourceRows(
				remotePayload,
				confirmed
		);

		if (!hasRemoteItem && !hasRemoteResource) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					Messages.get( this, "empty_offer" ),
					6
			);

			empty.maxWidth( WIDTH - 6 );
			empty.hardlight( 0xAAAAAA );
			empty.setPos( 3, pos );
			content.add( empty );

			pos = empty.bottom() + GAP;
		}
	}

	/**
	 * Adds every resource contained in the other trader's offer.
	 *
	 * These rows are view-only. They display:
	 *
	 *     [resource icon] offered amount
	 *
	 * They intentionally do not display the other player's total inventory,
	 * because the remote trade payload only needs to communicate what is
	 * being offered.
	 *
	 * @return true when at least one remote resource was displayed.
	 */
	private boolean addRemoteResourceRows(
			WayfarerTradePayload payload,
			boolean confirmed
	) {
		if (payload == null) {
			return false;
		}

		resourceRowColumn = 0;
		boolean addedResource = false;

		if (payload.gold() > 0) {
			addRemoteResourceRow(
					goldIcon(),
					payload.gold(),
					confirmed
			);

			addedResource = true;
		}

		if (payload.energy() > 0) {
			addRemoteResourceRow(
					energyIcon(),
					payload.energy(),
					confirmed
			);

			addedResource = true;
		}

		for (HomebaseState.Material material :
				HomebaseState.Material.values()) {

			int offered = payload.material( material );

			if (offered > 0) {
				addRemoteResourceRow(
						materialIcon( material ),
						offered,
						confirmed
				);

				addedResource = true;
			}
		}

		for (HomebaseState.ForgeResource resource :
				HomebaseState.ForgeResource.values()) {

			int offered = payload.forge( resource );

			if (offered > 0) {
				addRemoteResourceRow(
						forgeIcon( resource ),
						offered,
						confirmed
				);

				addedResource = true;
			}
		}

		/*
		* If the final resource only occupied the left column,
		* move pos below that unfinished row.
		*/
		if (resourceRowColumn == 1) {
			resourceRowColumn = 0;
			pos += REMOTE_RESOURCE_ROW_HEIGHT + 2;
		}

		if (addedResource) {
			pos += GAP;
		}

		return addedResource;
	}

	/**
	 * Adds one view-only resource row for the other trader.
	 *
	 * Layout:
	 *
	 *     [small icon] amount
	 *
	 * There is no plus button and no input action.
	 */
	private void addRemoteResourceRow(
        Image icon,
        int offered,
        boolean confirmed
	) {
		float columnWidth =
				(WIDTH - 6 - RESOURCE_COLUMN_GAP) / 2f;

		float x =
				3
						+ resourceRowColumn
						* (columnWidth + RESOURCE_COLUMN_GAP);

		float rowY = pos;

		/*
		* Small resource icon.
		*/
		if (icon != null) {
			if (icon.width > 0 && icon.height > 0) {
				float iconScale = Math.min(
						RESOURCE_ICON_SIZE / icon.width,
						RESOURCE_ICON_SIZE / icon.height
				);

				icon.scale.set( iconScale );
			}

			icon.x = x;
			icon.y =
					rowY
							+ (REMOTE_RESOURCE_ROW_HEIGHT
							- icon.height()) / 2f;

			PixelScene.align( icon );
			content.add( icon );
		}

		/*
		* The remote side displays only the amount offered.
		*/
		RenderedTextBlock amount = PixelScene.renderTextBlock(
				"x" + WndHomebaseFacility.compactAmount( offered ),
				6
		);

		amount.maxWidth(
				(int)(
						columnWidth
								- RESOURCE_ICON_SIZE
								- 4
				)
		);

		amount.hardlight(
				confirmed
						? 0x66FF66
						: WHITE
		);

		amount.setPos(
				x + RESOURCE_ICON_SIZE + 3,
				rowY
						+ (REMOTE_RESOURCE_ROW_HEIGHT
						- amount.height()) / 2f
		);

		content.add( amount );

		resourceRowColumn++;

		if (resourceRowColumn >= 2) {
			resourceRowColumn = 0;
			pos += REMOTE_RESOURCE_ROW_HEIGHT + 2;
		}
	}

	private String payloadText( WayfarerTradePayload payload ) {
		if (payload == null || payload.isEmpty()) return Messages.get( this, "empty_offer" );
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < WayfarerTradePayload.ITEM_SLOTS; i++) {
			Item item = payload.item( i );
			if (item != null) appendLine( text, itemText( item ) );
		}
		if (payload.gold() > 0) appendLine( text, Messages.get( this, "gold" ) + " x" + payload.gold() );
		if (payload.energy() > 0) appendLine( text, Messages.get( this, "energy" ) + " x" + payload.energy() );
		for (HomebaseState.Material material : HomebaseState.Material.values()) {
			if (payload.material( material ) > 0) {
				appendLine( text, WndHomebaseFacility.materialName( material ) + " x" + payload.material( material ) );
			}
		}
		for (HomebaseState.ForgeResource resource : HomebaseState.ForgeResource.values()) {
			if (payload.forge( resource ) > 0) {
				appendLine( text, WndHomebaseFacility.forgeName( resource ) + " x" + payload.forge( resource ) );
			}
		}
		return text.toString();
	}

	
	private void appendLine( StringBuilder text, String line ) {
		if (text.length() > 0) text.append( '\n' );
		text.append( line );
	}

	private String itemText( Item item ) {
		if (item == null || item.name() == null) return Messages.get( this, "empty_offer" );
		return Messages.titleCase( item.name() ) + (item.quantity() > 1 ? " x" + item.quantity() : "" );
	}

	private void onItemSlot( final int slot ) {
		final Item current = WayfarerExchangeService.localEscrowItem( slot );
		if (current != null) {
			GameScene.show( new WndOptions(
					Messages.get( this, "slot_remove_title" ),
					Messages.get( this, "slot_remove_body", itemText( current ) ),
					Messages.get( this, "remove" ),
					Messages.get( this, "cancel" ) ) {
				@Override
				protected void onSelect( int index ) {
					if (index == 0) {
						WayfarerExchangeService.removeItem( slot );
						safeRebuild();
					}
				}
			} );
			return;
		}

		GameScene.show( WndBag.getBag( new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get( WndWayfarerExchange.this, "choose_item" );
			}

			@Override
			public boolean itemSelectable( Item item ) {
				return item != null && item.name() != null && !(item instanceof Bag) && !item.isEquipped( Dungeon.hero );
			}

			@Override
			public void onSelect( final Item item ) {
				if (item == null) return;
				if (item.quantity() > 1) {
					GameScene.show( new WndOptions(
							Messages.get( WndWayfarerExchange.this, "choose_quantity" ),
							Messages.get( WndWayfarerExchange.this, "quantity_body", itemText( item ), item.quantity() ),
							Messages.get( WndWayfarerExchange.this, "quantity_one" ),
							Messages.get( WndWayfarerExchange.this, "quantity_custom" ),
							Messages.get( WndWayfarerExchange.this, "quantity_all" ),
							Messages.get( WndWayfarerExchange.this, "cancel" ) ) {
						@Override
						protected void onSelect( int index ) {
							if (index == 0 || index == 2) {
								WayfarerExchangeService.reserveItem( slot, item, index == 0 ? 1 : item.quantity() );
								safeRebuild();
							} else if (index == 1) {
								showItemQuantityInput( slot, item );
							}
						}
					} );
				} else {
					WayfarerExchangeService.reserveItem( slot, item, 1 );
					safeRebuild();
				}
			}
		} ) );
	}

	private void showItemQuantityInput( final int slot, final Item item ) {
		if (item == null) return;
		final int owned = Math.max( 1, item.quantity() );
		GameScene.show(
				new WndTextInput(
						Messages.get( this, "choose_quantity" ),
						Messages.get( this, "quantity_body", itemText( item ), owned ),
						Integer.toString( Math.min( owned, Math.max( 1, owned / 2 ) ) ),
						9,
						false,
						Messages.get( this, "set_quantity" ),
						Messages.get( this, "cancel" )
				) {
					@Override
					public void onSelect( boolean positive, String text ) {
						if (!positive) return;
						int amount = Math.min( owned, Math.max( 1, parseAmount( text ) ) );
						WayfarerExchangeService.reserveItem( slot, item, amount );
						safeRebuild();
					}
				}
		);
	}

	private void showAmountInput(
			final String name,
			final int offered,
			final int owned,
			final ResourceSetter setter
	) {
		GameScene.show(
				new WndTextInput(
						Messages.get(
								this,
								"resource_amount_title"
						),
						Messages.get(
								this,
								"resource_amount_body",
								name,
								owned
						),
						Integer.toString( offered ),
						9,
						false,
						Messages.get(
								this,
								"set_quantity"
						),
						Messages.get(
								this,
								"cancel"
						)
				) {
					@Override
					public void onSelect(
							boolean positive,
							String text
					) {
						if (!positive) {
							return;
						}

						int amount = Math.min(
								owned,
								Math.max(
										0,
										parseAmount( text )
								)
						);

						setter.set( amount );
						safeRebuild();
					}
				}
		);
	}

	private int parseAmount( String text ) {
		try {
			return Integer.parseInt( text.trim() );
		} catch (Exception e) {
			return 0;
		}
	}

	private interface ResourceSetter {
		void set( int amount );
	}

	private int statusColor() {
		switch (WayfarerExchangeService.mode()) {
			case CONNECTED:
				return SHPX_COLOR;
			case ERROR:
				return 0xFF5555;
			case HOSTING:
			case SEARCHING:
			case CONNECTING:
				return 0x66CCFF;
			default:
				return WHITE;
		}
	}

	private static String traderName() {
		return Dungeon.hero == null ? "Unknown Trader" : Dungeon.hero.characterName();
	}

	private static String traderClass() {
		return Dungeon.hero == null ? "unknown" : Dungeon.hero.className();
	}

	private static int traderArmorTier() {
		return Dungeon.hero == null ? 0 : Dungeon.hero.tier();
	}

	public static void startTradeAndEnter( boolean hostMode ) {
		WayfarerAccountService.currentCharacterRestricted( (result, restricted) -> {
			if (!result.success || restricted) {
				GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Wayfarer Access Restricted",
						result.message, "Close" ) );
				return;
			}
			if (hostMode) {
				WayfarerExchangeService.startHost( traderName(), traderClass(), traderArmorTier() );
			} else {
				WayfarerExchangeService.startSearch( traderName(), traderClass(), traderArmorTier() );
			}
			WayfarerExchangeLevel.enter( hostMode );
		} );
	}
}
