/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.erebus.reclaimedpixeldungeon.ui.changelist;

import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class LinkedTabbedChangeButton extends ChangeButton {

	private final String[] tabLabels;
	private final String linkLabel;
	private final String linkUrl;

	public LinkedTabbedChangeButton( Image icon, String title, String[] tabLabels,
			String linkLabel, String linkUrl, String... messages ) {
		super( icon, title, messages );
		this.tabLabels = tabLabels;
		this.linkLabel = linkLabel;
		this.linkUrl = linkUrl;
	}

	@Override
	protected void onClick() {
		ShatteredPixelDungeon.scene().addToFront( new LinkedWindow(
				new Image( icon ), title, tabLabels, messages, linkLabel, linkUrl ) );
	}

	private static class LinkedWindow extends WndChangesTabbed {
		private static final int LINK_HEIGHT = 18;

		private LinkedWindow( Image icon, String title, String[] tabLabels, String[] messages,
				String linkLabel, String linkUrl ) {
			super( icon, title, tabLabels, messages );

			RedButton link = new RedButton( linkLabel ) {
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.platform.openURI( linkUrl );
				}
			};
			link.icon( Icons.get( Icons.ENTER ) );
			link.textColor( Window.TITLE_COLOR );
			link.setRect( 0, height + 2, width, LINK_HEIGHT );
			add( link );
			resize( width, (int)link.bottom() );
			layoutTabs();
		}
	}
}
