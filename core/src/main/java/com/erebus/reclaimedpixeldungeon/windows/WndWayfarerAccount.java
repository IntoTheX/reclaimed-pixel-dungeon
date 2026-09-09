/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerPresenceService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.CheckBox;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;

public class WndWayfarerAccount extends Window {

	private static final int WIDTH = 135;
	private static final int MARGIN = 4;
	private static final int SETTINGS_SIZE = 18;

	private RenderedTextBlock visibilityDescription;
	private CheckBox visibilityToggle;
	private RedButton nearby;
	private RedButton chats;
	private RedButton reports;
	private RedButton deletionReview;
	private ColorBlock reportsDivider;

	public WndWayfarerAccount() {
		WayfarerAccountService.ensureCurrentCharacterRegistration();
		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get( this, "title" ), 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( MARGIN, MARGIN + 3 );
		add( title );

		IconButton settings = new IconButton( Icons.get( Icons.PREFS ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				WndSettings.showWayfarerAccountSettings();
			}
		};
		settings.setRect( WIDTH - MARGIN - SETTINGS_SIZE, MARGIN, SETTINGS_SIZE, SETTINGS_SIZE );
		add( settings );

		RenderedTextBlock account = PixelScene.renderTextBlock(
				Messages.get( this, "account", WayfarerAccountService.maskedEmail() ), 6 );
		account.maxWidth( WIDTH - 2 * MARGIN );
		account.setPos( MARGIN, settings.bottom() + 3 );
		add( account );

		RenderedTextBlock connected = PixelScene.renderTextBlock( Messages.get( this, "connected" ), 6 );
		connected.maxWidth( WIDTH - 2 * MARGIN );
		connected.setPos( MARGIN, account.bottom() + 2 );
		add( connected );

		visibilityToggle = new CheckBox( Messages.get( this, "visible" ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				enable( false );
				if (checked()) WayfarerPresenceService.enable( WndWayfarerAccount.this::presenceResult );
				else WayfarerPresenceService.disable( WndWayfarerAccount.this::presenceResult );
			}
		};
		visibilityToggle.checked( SPDSettings.wayfarerVisible() );
		visibilityToggle.setRect( MARGIN, connected.bottom() + 5, WIDTH - 2 * MARGIN, 16 );
		add( visibilityToggle );

		visibilityDescription = PixelScene.renderTextBlock( 6 );
		visibilityDescription.maxWidth( WIDTH - 2 * MARGIN );
		visibilityDescription.setPos( MARGIN, visibilityToggle.bottom() + 3 );
		add( visibilityDescription );
		refreshDescription();

		nearby = new RedButton( Messages.get( this, "nearby" ), 8 ) {
			@Override
			protected void onClick() {
				super.onClick();
				GameScene.show( new WndNearbyWayfarers() );
			}
		};
		nearby.setRect( MARGIN, visibilityDescription.bottom() + 5, WIDTH - 2 * MARGIN, 18 );
		add( nearby );
		chats = new RedButton( "Chats", 7 ) {
			@Override protected void onClick() {
				super.onClick();
				GameScene.show( new WndWayfarerChats() );
			}
		};
		add( chats );
		deletionReview = new RedButton( "Deletion Review", 7 ) {
			@Override protected void onClick() { GameScene.show( new WndDeletionReview() ); }
		};
		deletionReview.visible = false; add( deletionReview );
		reportsDivider = new ColorBlock( WIDTH - 2 * MARGIN, 1, 0xFF666666 );
		reportsDivider.visible = false;
		add( reportsDivider );
		reports = new RedButton( "Safety Reports", 7 ) {
			@Override protected void onClick() {
				super.onClick();
				GameScene.show( new WndWayfarerReports() );
			}
		};
		reports.visible = false;
		add( reports );

		layoutActions( false );
		WayfarerAccountService.moderatorStatus( (result, moderator) -> {
			if (parent != null && result.success) layoutActions( moderator );
		} );
		WayfarerAccountService.deletionReviewStatus( (result, review) -> {
			if (parent == null || !result.success) return;
			deletionReview.visible = !"none".equals( review.status );
			layoutActions( reports.visible );
		} );
	}

	private void layoutActions( boolean moderator ) {
		nearby.setRect( MARGIN, visibilityDescription.bottom() + 5, WIDTH - 2 * MARGIN, 18 );
		chats.setRect( MARGIN, nearby.bottom() + 3, WIDTH - 2 * MARGIN, 18 );
		float bottom = chats.bottom();
		if (deletionReview.visible) {
			deletionReview.setRect( MARGIN, bottom + 3, WIDTH - 2 * MARGIN, 18 ); bottom = deletionReview.bottom();
		}
		reportsDivider.visible = moderator;
		reports.visible = moderator;
		if (moderator) {
			reportsDivider.x = MARGIN;
			reportsDivider.y = bottom + 4;
			reports.setRect( MARGIN, reportsDivider.y + 4, WIDTH - 2 * MARGIN, 18 );
		}
		resize( WIDTH, (int)((moderator ? reports.bottom() : bottom) + MARGIN) );
	}

	private void refreshDescription() {
		visibilityDescription.text( Messages.get( this,
				SPDSettings.wayfarerVisible() ? "visibility_visible" : "visibility_hidden" ) );
		visibilityDescription.setPos( MARGIN, visibilityDescription.top() );
		if (nearby != null) {
			layoutActions( reports.visible );
		}
	}

	private void presenceResult( WayfarerAccountService.Result result ) {
		visibilityToggle.checked( SPDSettings.wayfarerVisible() );
		visibilityToggle.enable( true );
		refreshDescription();
		if (!result.success && parent != null) {
			GameScene.show( new WndOptions(
					Icons.get( Icons.WARNING ), Messages.get( this, "presence_failed" ),
					result.message, Messages.get( this, "close" ) ) );
		}
	}
}
