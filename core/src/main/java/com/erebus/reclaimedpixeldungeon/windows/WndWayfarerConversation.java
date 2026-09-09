/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.actors.hero.HeroClass;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerChatStore;
import com.erebus.reclaimedpixeldungeon.network.WayfarerGlobalTrade;
import com.badlogic.gdx.utils.JsonValue;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.sprites.HeroSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WndWayfarerConversation extends Window {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 180;
	private final WayfarerAccountService.NearbyPlayer player;
	private final boolean moderationNotice;
	private final Component content = new GlobalTradeContent();
	private final ScrollPane pane = new ScrollPane( content );
	private final RenderedTextBlock blockBanner = PixelScene.renderTextBlock( 6 );
	private final ColorBlock blockTop = new ColorBlock( WIDTH - 6, 1, 0xFF777777 );
	private final ColorBlock blockBottom = new ColorBlock( WIDTH - 6, 1, 0xFF777777 );
	private final RenderedTextBlock restrictionBanner = PixelScene.renderTextBlock( 6 );
	private final ColorBlock restrictionTop = new ColorBlock( WIDTH - 6, 1, 0xFFAA3333 );
	private final ColorBlock restrictionBottom = new ColorBlock( WIDTH - 6, 1, 0xFFAA3333 );
	private float conversationTop;
	private float pollDelay;
	private boolean receiving;
	private int shownMessageCount;
	private int blockRelationship;
	private float safetyBottomY;
	private RedButton sendButton;
	private RedButton tradeButton;
	private IconButton claimShortcut;
	private IconButton activeTradeShortcut;
	private JsonValue trades;
	private float tradeDelay;
	private boolean loadingTrades;
	private String tradeSnapshot = "";
	private float activeTradeY = -1;

	private static final class TimelineEntry {
		final WayfarerChatStore.Message message;
		final JsonValue trade;
		final long order;

		TimelineEntry( WayfarerChatStore.Message message ) {
			this.message = message;
			this.trade = null;
			this.order = message.order;
		}

		TimelineEntry( JsonValue trade ) {
			this.message = null;
			this.trade = trade;
			this.order = serverTime( trade.getString( "updated_at", trade.getString( "created_at", "" ) ) );
		}
	}

	public WndWayfarerConversation( WayfarerAccountService.NearbyPlayer player ) {
		this.player = player;
		moderationNotice = WayfarerChatStore.MODERATION_CONTACT_ID.equals( player.characterId );
		WayfarerChatStore.remember( player );
		WayfarerChatStore.openConversation(player.characterId);
		WayfarerChatStore.markRead( player.characterId );
		HeroClass heroClass = heroClass( player.heroClass );
		if (!moderationNotice) {
			Image portrait = HeroSprite.avatar( heroClass, Math.max( 0, Math.min( 6, player.headSprite ) ) );
			portrait.x = 3;
			portrait.y = 3;
			add( portrait );
		}

		RenderedTextBlock title = PixelScene.renderTextBlock( player.playerName, 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( moderationNotice ? (WIDTH - title.width()) / 2f : 23, 4 );
		add( title );
		if (!moderationNotice) {
			RenderedTextBlock identity = PixelScene.renderTextBlock(
					Messages.titleCase( heroClass.title() ) + " Lv. " + player.heroLevel, 6 );
			identity.setPos( 23, title.bottom() + 2 );
			add( identity );
			IconButton safetyMenu = new IconButton( Icons.get( Icons.PREFS ) ) {
				@Override protected void onClick() { super.onClick(); safetyMenu(); }
			};
			safetyMenu.setRect( WIDTH - 19, 3, 16, 16 );
			add( safetyMenu );
			IconButton guidelines = new IconButton( Icons.get( Icons.INFO ) ) {
				@Override protected void onClick() { super.onClick(); GameScene.show( new WndWayfarerGuidelines() ); }
			};
			guidelines.setRect( WIDTH - 37, 3, 16, 16 );
			add( guidelines );
			claimShortcut = new IconButton( new ItemSprite( ItemSpriteSheet.LOCKED_CHEST ) ) {
				@Override protected void onClick() {
					super.onClick();
					GameScene.show( new WndWayfarerTradeClaims( player, trades, () -> tradeDelay=0 ) );
				}
			};
			claimShortcut.setRect( WIDTH - 55, 3, 16, 16 );
			claimShortcut.visible = false;
			add( claimShortcut );
			activeTradeShortcut = new IconButton( Icons.get( Icons.BUFFS ) ) {
				@Override protected void onClick() {
					super.onClick();
					if (activeTradeY >= 0) pane.scrollTo( 0, Math.max( 0, activeTradeY - 2 ) );
				}
			};
			activeTradeShortcut.setRect( WIDTH - 55, 3, 16, 16 );
			activeTradeShortcut.visible = false;
			add( activeTradeShortcut );
		}

		ColorBlock safetyTop = new ColorBlock( WIDTH - 6, 1, 0xFF666666 );
		safetyTop.x = 3;
		safetyTop.y = 24;
		add( safetyTop );
		RenderedTextBlock safety = PixelScene.renderTextBlock(
				"_Stay safe:_ Do not share private details. Meet only in public places and tell someone you trust.", 6 );
		safety.maxWidth( WIDTH - 6 );
		safety.setPos( 3, safetyTop.y + safetyTop.height + 2 );
		add( safety );
		ColorBlock safetyBottom = new ColorBlock( WIDTH - 6, 1, 0xFF666666 );
		safetyBottom.x = 3;
		safetyBottom.y = safety.bottom() + 2;
		add( safetyBottom );
		safetyBottomY = safetyBottom.y + safetyBottom.height;
		blockTop.visible = blockBottom.visible = blockBanner.visible = false;
		blockBanner.maxWidth( WIDTH - 6 );
		blockBanner.hardlight( 0xFFCC66 );
		add( blockTop );
		add( blockBanner );
		add( blockBottom );
		restrictionTop.visible = restrictionBottom.visible = restrictionBanner.visible = false;
		restrictionBanner.maxWidth( WIDTH - 6 );
		restrictionBanner.hardlight( 0xFF7777 );
		add( restrictionTop );
		add( restrictionBanner );
		add( restrictionBottom );

		add( pane );
		sendButton = new RedButton( "Message", 7 ) {
			@Override public void update() {
				if (blockRelationship != 0) bg.hardlight( 0x777777 );
				else bg.resetColor();
				super.update();
			}
			@Override protected void onClick() { super.onClick(); compose(); }
		};
		if (!moderationNotice) add( sendButton );
		RedButton safetyButton = new RedButton( "Trade", 7 ) {
			@Override protected void onClick() { GameScene.show(new WndGlobalTrade(player,null,false)); }
		};
		tradeButton = safetyButton;
		tradeButton.enable(false);
		safetyButton.visible = !moderationNotice;
		if (!moderationNotice) add( safetyButton );
		resize( WIDTH, HEIGHT );
		conversationTop = safetyBottomY + 3;
		layoutConversation();
		if (!moderationNotice) {
			sendButton.setRect( 2, HEIGHT - 18, WIDTH - 57, 16 );
			safetyButton.setRect( sendButton.right() + 2, HEIGHT - 18, 53, 16 );
		}
		rebuild();
		if (!moderationNotice) {
			receive( true );
			refreshBlockStatus();
			WayfarerAccountService.currentRestriction( (result, restriction) -> {
				if (parent == null || !result.success || !restriction.active) return;
				String until = restriction.endsAt.replace( 'T', ' ' );
				int zone = until.indexOf( '+' );
				if (zone > 0) until = until.substring( 0, zone );
				restrictionBanner.text( "_Restriction active:_ Chat and Wayfarer trading are disabled until "
						+ until + ". Sanction Stage " + restriction.stage + "." );
				restrictionTop.visible = restrictionBottom.visible = restrictionBanner.visible = true;
				layoutBanners();
				rebuild();
			} );
		}
	}

	@Override public void destroy() {
		WayfarerChatStore.closeConversation(player.characterId);
		super.destroy();
	}

	private void layoutConversation() {
		pane.setRect( 0, conversationTop, WIDTH, HEIGHT - conversationTop - (moderationNotice ? 3 : 21) );
	}

	private void layoutBanners() {
		float y = safetyBottomY + 3;
		if (blockRelationship != 0) {
			blockBanner.text( (blockRelationship & 1) != 0
					? "_Unblock this player_ to send a message."
					: "You can no longer respond to this conversation." );
			blockTop.visible = blockBottom.visible = blockBanner.visible = true;
			blockTop.x = 3;
			blockTop.y = y;
			blockBanner.setPos( 3, y + 3 );
			blockBottom.x = 3;
			blockBottom.y = blockBanner.bottom() + 2;
			y = blockBottom.y + 4;
		} else {
			blockTop.visible = blockBottom.visible = blockBanner.visible = false;
		}
		if (restrictionBanner.visible) {
			restrictionTop.x = 3;
			restrictionTop.y = y;
			restrictionBanner.setPos( 3, y + 3 );
			restrictionBottom.x = 3;
			restrictionBottom.y = restrictionBanner.bottom() + 2;
			y = restrictionBottom.y + 4;
		}
		conversationTop = y;
		if (sendButton != null) sendButton.enable( blockRelationship == 0 );
		layoutConversation();
	}

	@Override public void update() {
		super.update();
		int storedCount = history().size();
		if (storedCount != shownMessageCount) {
			boolean followNewest = pane.isAtBottom( 3 );
			WayfarerChatStore.markRead( player.characterId );
			rebuild( followNewest );
		}
		if (moderationNotice) return;
		if(claimShortcut!=null&&claimShortcut.visible)claimShortcut.icon().alpha(
				0.35f+0.65f*(float)Math.abs(Math.cos(1.5f*Math.PI*com.watabou.noosa.Game.timeTotal)));
		if(activeTradeShortcut!=null&&activeTradeShortcut.visible)activeTradeShortcut.icon().alpha(
				0.35f+0.65f*(float)Math.abs(Math.cos(1.5f*Math.PI*com.watabou.noosa.Game.timeTotal)));
		tradeDelay -= com.watabou.noosa.Game.elapsed;
		if (tradeDelay <= 0 && !loadingTrades && !WayfarerGlobalTrade.working) refreshTrades();
		pollDelay -= com.watabou.noosa.Game.elapsed;
		if (pollDelay <= 0 && !receiving) receive( false );
	}

	private ArrayList<WayfarerChatStore.Message> history() {
		return WayfarerChatStore.history( player.characterId );
	}

	private void compose() {
		if (blockRelationship != 0) return;
		WayfarerAccountService.blockRelationship( player, (result, relationship) -> {
			if (parent == null) return;
			if (!result.success) return;
			applyBlockRelationship( relationship );
			if (relationship == 0) showComposer();
		} );
	}

	private void showComposer() {
		GameScene.show( new WndTextInput( "Message " + player.playerName,
				"Messages are end-to-end encrypted. Only you and the receiving character can read them.",
				"", 500, true, "Send", "Cancel" ) {
			@Override public void onSelect( boolean positive, String text ) {
				if (!positive || text == null || text.trim().isEmpty()) return;
				String sentAt = now();
				WayfarerAccountService.sendMessage( player, text, sentAt, result -> {
					if (result.success) com.erebus.reclaimedpixeldungeon.network.WayfarerModeratorRewards.recordActivity();
					if (WndWayfarerConversation.this.parent == null) return;
					if (result.success) {
						WayfarerChatStore.addOutgoing( player, Dungeon.hero.characterName(),
								text.trim(), sentAt );
						rebuild();
					}
				} );
			}
		} );
	}

	private void refreshBlockStatus() {
		WayfarerAccountService.blockRelationship( player, (result, relationship) -> {
			if (parent == null || !result.success) return;
			applyBlockRelationship( relationship );
		} );
	}

	private void applyBlockRelationship( int relationship ) {
		blockRelationship = Math.max( 0, Math.min( 3, relationship ) );
		WayfarerChatStore.setBlocked( player.characterId, (blockRelationship & 1) != 0 );
		layoutBanners();
		rebuild();
	}

	private void safetyMenu() {
		boolean blocked = WayfarerChatStore.isBlocked( player.characterId );
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Player Safety",
				"_Reports_ share only the recent conversation excerpt you approve. _Blocking_ prevents discovery and messages between these two characters.",
				"Report Recent Messages", blocked ? "Unblock Player" : "Block Player", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index == 0) confirmReport();
				else if (index == 1) {
					if (blocked) confirmUnblock();
					else confirmBlock();
				}
			}
		} );
	}

	private void confirmReport() {
		String evidence = WayfarerChatStore.reportEvidence( player.characterId );
		if (evidence.isEmpty()) {
			GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Nothing to Report",
					"There are no messages to include in an incident report.", "Close" ) );
			return;
		}
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Report " + player.playerName,
				"The latest _30 messages_ in this conversation will be packaged with your reason and shared with Wayfarer moderators.",
				"Continue", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index == 0) requestReportReason( evidence );
			}
		} );
	}

	private void requestReportReason( String evidence ) {
		GameScene.show( new WndTextInput( "Report Reason",
				"Describe the safety concern clearly. This report will be reviewed by a moderator.",
				"", 500, true, "Submit", "Cancel" ) {
			@Override public void onSelect( boolean positive, String text ) {
				if (!positive) return;
				String reason = text == null ? "" : text.trim();
				if (reason.length() < 3) {
					GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Report Reason Required",
							"The report reason must contain at least 3 characters.", "Close" ) );
					return;
				}
				WayfarerAccountService.reportPlayer( player, reason, evidence,
						result -> {
							if (parent == null) return;
							GameScene.show( new WndOptions(
									Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
									result.success ? "Incident Report Sent" : "Incident Report Failed",
									result.success
											? "Thank you for helping make _Reclaimed Pixel Dungeon_ safe for everyone. A moderator will review your report, and sanctions will be applied once a violation is confirmed.\n\n" + result.message
											: result.message,
									"Close" ) );
						} );
			}
		} );
	}

	private void confirmBlock() {
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Block " + player.playerName,
				"This removes both characters from each other's _discovery_ and prevents all new _messages_. Existing local chat history is retained.",
				"Block", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index != 0) return;
				WayfarerAccountService.blockPlayer( player, true, result -> {
					if (WndWayfarerConversation.this.parent == null) return;
					if (result.success) refreshBlockStatus();
					GameScene.show( new WndOptions( Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
							result.success ? "Player Blocked" : "Block Failed",
							result.success
									? player.playerName + " can no longer discover or exchange messages with you. Existing chat history has been retained."
									: result.message,
							"Close" ) );
				} );
			}
		} );
	}

	private void confirmUnblock() {
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Unblock " + player.playerName,
				"This player may appear in discovery and exchange messages with you again.",
				"Unblock", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index != 0) return;
				WayfarerAccountService.blockPlayer( player, false, result -> {
					if (WndWayfarerConversation.this.parent == null) return;
					if (result.success) refreshBlockStatus();
					GameScene.show( new WndOptions( Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
							result.success ? "Player Unblocked" : "Unblock Failed",
							result.success
									? player.playerName + " may appear in discovery and exchange messages with you again."
									: result.message,
							"Close" ) );
				} );
			}
		} );
	}

	private void receive( boolean announce ) {
		if (receiving) return;
		receiving = true;
		pollDelay = 2f;
		WayfarerAccountService.receiveMessages( (result, messages) -> {
			receiving = false;
			if (parent == null) return;
			if (result.success) {
				boolean followNewest = pane.isAtBottom( 3 );
				WayfarerChatStore.accept( messages, player.characterId );
				WayfarerChatStore.markRead( player.characterId );
				if (!messages.isEmpty()) rebuild( followNewest );
			}
		} );
	}

	private void rebuild() {
		rebuild( true );
	}

	private void rebuild( boolean followNewest ) {
		float previousScroll = pane.scrollY();
		content.clear();
		activeTradeY = -1;
		float y = 2;
		ArrayList<TimelineEntry> timeline = new ArrayList<>();
		for (WayfarerChatStore.Message message : history()) timeline.add( new TimelineEntry( message ) );
		if (trades != null) for (JsonValue row=trades.child; row!=null; row=row.next) {
			boolean mine=row.getString("sender").equals(Dungeon.wayfarerCharacterId());
			if(mine || row.getBoolean("sender_deposited")) timeline.add( new TimelineEntry( row ) );
		}
		timeline.sort( (a,b) -> Long.compare( a.order, b.order ) );
		for (TimelineEntry entry : timeline) {
			y = entry.message == null ? tradeBubble( entry.trade, y ) : messageBubble( entry.message, y );
		}
		content.setSize( pane.width(), Math.max( pane.height(), y ) );
		shownMessageCount = history().size();
		pane.scrollTo( 0, followNewest ? Math.max( 0, y - pane.height() ) : previousScroll );
	}

	private float messageBubble( WayfarerChatStore.Message entry, float y ) {
		RenderedTextBlock heading = PixelScene.renderTextBlock(
				entry.name + "  " + WayfarerChatStore.messageTime( entry ), 6 );
		heading.hardlight( entry.outgoing ? 0x66CCFF : TITLE_COLOR );
		heading.maxWidth( WIDTH - 8 );
		float headingX = entry.outgoing ? WIDTH - 3 - heading.width() : 3;
		heading.setPos( Math.max( 3, headingX ), y );
		content.add( heading );
		RenderedTextBlock body = PixelScene.renderTextBlock( entry.text, 6 );
		body.maxWidth( (int)(WIDTH * 0.72f) );
		float bodyX = entry.outgoing ? WIDTH - 6 - body.width() : 6;
		body.setPos( bodyX, heading.bottom() + 5 );
		float bubbleX = bodyX - 3;
		float bubbleY = body.top() - 3;
		ColorBlock bubble = new ColorBlock( body.width() + 6, body.height() + 6,
				entry.outgoing ? 0xFF454545 : 0xFF245A73 );
		bubble.x = bubbleX;
		bubble.y = bubbleY;
		content.add( bubble );
		content.add( body );
		return Math.max( body.bottom(), bubbleY + bubble.height ) + 9;
	}

	private void refreshTrades() {
		loadingTrades=true; tradeDelay=4;
		WayfarerAccountService.tradeAction(player.characterId,null,"list",null,(result,rows)->{
			loadingTrades=false;
			if(parent==null || !exists || !result.success || rows==null) return;
			trades=rows;boolean active=false;boolean ongoing=false;
			for(JsonValue row=rows.child;row!=null;row=row.next) {
				String state=row.getString("state");
				if(!state.equals("cancelled")&&!state.equals("completed"))active=true;
				if(activeTradeState(state))ongoing=true;
			}
			if(claimShortcut!=null) {
				boolean claimable=WndWayfarerTradeClaims.hasClaimable(rows);
				claimShortcut.visible=claimable;
				if(!claimShortcut.visible)claimShortcut.icon().alpha(1f);
				activeTradeShortcut.visible=!claimable&&ongoing;
				if(!activeTradeShortcut.visible)activeTradeShortcut.icon().alpha(1f);
			}
			tradeButton.enable(!active && blockRelationship==0 && !restrictionBanner.visible);
			String snapshot=rows.toString()+com.erebus.reclaimedpixeldungeon.Dungeon.globalTradeJournal;
			if(!snapshot.equals(tradeSnapshot)) {
				tradeSnapshot=snapshot;rebuild(pane.isAtBottom(3));
			}
		});
	}
	private float tradeBubble(final JsonValue trade,float y) {
		final String id=trade.getString("trade_id"),state=trade.getString("state");
		if(activeTradeState(state))activeTradeY=y;
		boolean mine=trade.getString("sender").equals(Dungeon.wayfarerCharacterId());
		boolean deposited=trade.getBoolean(mine?"sender_deposited":"recipient_deposited");
		boolean claimed=trade.getBoolean(mine?"sender_claimed":"recipient_claimed");
		ColorBlock bubble=new ColorBlock(WIDTH-6,1,mine?0xFF454545:0xFF245A73);
		bubble.x=3;bubble.y=y;content.add(bubble);
		y=tradeHeader(mine?Dungeon.hero.characterName():player.playerName,state,y+4);
		if(!state.equals("cancelled")) {
			String key="sender_offer";
			if(state.equals("finalized")||state.equals("completed"))key=mine?"recipient_offer":"sender_offer";
			try {
				String packet=trade.getString(key,null);
				if(packet!=null)y=GlobalTradeContent.offer(content,WayfarerGlobalTrade.decode(packet),WIDTH,y);
			} catch(Exception error) { y=GlobalTradeContent.label(content,"_Unsupported item data:_ "+error.getMessage(),WIDTH,y); }
		}
		if(state.equals("invited")&&!mine) {
			y=tradeActions(y,"Accept",()->action(id,"accept",()->GameScene.show(new WndGlobalTrade(player,id,true))),"Decline",()->action(id,"cancel",null));
		} else if(state.equals("responding")&&!mine && trade.getString("recipient_offer",null)==null) {
			y=tradeActions(y,"Choose Offer",()->GameScene.show(new WndGlobalTrade(player,id,true)),"Cancel",()->action(id,"cancel",null));
		} else if(state.equals("review")) {
			y=GlobalTradeContent.label(content,"You: "+(trade.getBoolean(mine?"sender_confirmed":"recipient_confirmed")?"Confirmed":"Not confirmed")
				+" | Partner: "+(trade.getBoolean(mine?"recipient_confirmed":"sender_confirmed")?"Confirmed":"Not confirmed"),WIDTH,y);
			y=tradeActions(y,"Review Trade",()->WndGlobalTrade.review(player,trade),"Cancel",()->action(id,"cancel",null));
		} else if(state.equals("finalized") || state.equals("cancelled")) {
			if(!claimed && (deposited || state.equals("finalized")))
				y=tradeActions(y,state.equals("cancelled")?"Claim Return":"Claim",()->WayfarerGlobalTrade.claim(player.characterId,id,this::tradeResult),null,null);
			else if(state.equals("finalized")&&claimed)y=GlobalTradeContent.label(content,"_Claimed_",WIDTH,y);
		} else if(!state.equals("completed")) {
			if(!deposited && WayfarerGlobalTrade.pending(id))
				y=tradeActions(y,"Retry Deposit",()->WayfarerGlobalTrade.retry(id,this::tradeResult),"Cancel",()->action(id,"cancel",null));
			else y=tradeActions(y,"Cancel",()->action(id,"cancel",null),null,null);
		}
		bubble.size(WIDTH-6,y-bubble.y+3);
		return y+10;
	}

	private static boolean activeTradeState(String state) {
		return "invited".equals(state)||"responding".equals(state)||"review".equals(state);
	}

	private float tradeHeader(String name,String state,float y) {
		RenderedTextBlock status=PixelScene.renderTextBlock(Messages.titleCase(state.replace('_',' ')),6);
		status.hardlight(TITLE_COLOR);
		status.setPos(WIDTH-7-status.width(),y);
		content.add(status);
		RenderedTextBlock heading=PixelScene.renderTextBlock(name+" initiated a trade:",6);
		heading.maxWidth(Math.max(40,(int)(status.left()-10)));
		heading.setPos(6,y);
		content.add(heading);
		return Math.max(heading.bottom(),status.bottom())+4;
	}

	private static long serverTime(String value) {
		try {
			if(value==null || value.length()<19)return 0;
			String base=value.substring(0,19),fraction="000",zone="+0000";
			int dot=value.indexOf('.',19);
			int zoneAt=value.endsWith("Z")?value.length()-1:value.lastIndexOf('+');
			if(zoneAt<19)zoneAt=value.lastIndexOf('-');
			if(dot>=0) {
				int end=zoneAt>dot?zoneAt:value.length();
				String raw=value.substring(dot+1,end);
				fraction=(raw+"000").substring(0,3);
			}
			if(zoneAt>=19 && !value.endsWith("Z"))zone=value.substring(zoneAt).replace(":","");
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",Locale.ROOT).parse(base+"."+fraction+zone).getTime();
		} catch(Exception ignored) { return 0; }
	}
	private float tradeActions(float y,String first,Runnable a,String second,Runnable b) {
		float width=second==null?WIDTH-12:(WIDTH-15)/2f;
		RedButton one=new RedButton(first,6){@Override protected void onClick(){if(!WayfarerGlobalTrade.working)a.run();}};
		one.setRect(6,y,width,17);content.add(one);
		if(second!=null){RedButton two=new RedButton(second,6){@Override protected void onClick(){if(!WayfarerGlobalTrade.working)b.run();}};
			two.setRect(one.right()+3,y,width,17);content.add(two);}
		return y+21;
	}
	private void action(String id,String action,Runnable next) {
		WayfarerAccountService.tradeAction(player.characterId,id,action,null,(result,row)->{
			if(!result.success)WndGlobalTrade.notice(result.message);
			else if(next!=null)next.run();
			tradeDelay=0;
		});
	}
	private void tradeResult(WayfarerAccountService.Result result) {
		if(!result.success)WndGlobalTrade.notice(result.message);
		tradeDelay=0;
	}

	private static HeroClass heroClass( String value ) {
		try { return HeroClass.valueOf( value.toUpperCase( Locale.ENGLISH ) ); }
		catch (Exception ignored) { return HeroClass.WARRIOR; }
	}

	private static String now() {
		return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm", Locale.ROOT ).format( new Date() );
	}

}
