/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Reclaimed Pixel Dungeon additions
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.scenes.HomebaseFacilityScene;
import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.watabou.utils.DeviceCompat;

final class ReclaimedWindow {

	static final int INVENTORY_WIDTH = 144;
	private static final int SCREEN_MARGIN = 4;
	private static final int HOMEBASE_SCREEN_MARGIN = 10;
	private static final int MIN_MODAL_HEIGHT = 80;
	private static final int MIN_HOMEBASE_MODAL_HEIGHT = 40;

	private ReclaimedWindow() {
	}

	static int modalWidth( int desktopWidth ) {
		int preferredWidth = DeviceCompat.isDesktop() ? desktopWidth : INVENTORY_WIDTH;
		if (PixelScene.uiCamera == null) return preferredWidth;

		// Leave room for the window chrome as well as a visible strip of the scene on
		// both sides. This keeps Reclaimed's wider detail windows usable at 7x/8x UI.
		int availableWidth = PixelScene.uiCamera.width - 2 * SCREEN_MARGIN - 4;
		return Math.max( 40, Math.min( preferredWidth, availableWidth ) );
	}

	static int modalHeight( int preferredHeight, int nonBodyHeight ) {
		if (PixelScene.uiCamera == null) return preferredHeight;
		int margin = homebaseBottomReserve() > 0 ? HOMEBASE_SCREEN_MARGIN : SCREEN_MARGIN;
		int available = PixelScene.uiCamera.height
				- homebaseTopReserve()
				- homebaseBottomReserve()
				- nonBodyHeight
				- margin;
		int minimum = homebaseBottomReserve() > 0 ? MIN_HOMEBASE_MODAL_HEIGHT : MIN_MODAL_HEIGHT;
		return Math.max( minimum, Math.min( preferredHeight, available ) );
	}

	static int modalYOffset( int bodyHeight, int nonBodyHeight ) {
		if (PixelScene.uiCamera == null || homebaseBottomReserve() <= 0) return 0;
		int totalHeight = bodyHeight + nonBodyHeight;
		int topReserve = homebaseTopReserve();
		int usableHeight = PixelScene.uiCamera.height - topReserve - homebaseBottomReserve();
		int centeredTop = (PixelScene.uiCamera.height - totalHeight) / 2;
		int targetTop = topReserve + Math.max( 0, (usableHeight - totalHeight) / 2 );
		return targetTop - centeredTop;
	}

	private static int homebaseTopReserve() {
		return ShatteredPixelDungeon.scene() instanceof HomebaseFacilityScene ? HomebaseFacilityScene.modalTopReserve() : 0;
	}

	private static int homebaseBottomReserve() {
		return ShatteredPixelDungeon.scene() instanceof HomebaseFacilityScene ? HomebaseFacilityScene.modalBottomReserve() : 0;
	}

	static boolean isDesktop() {
		return DeviceCompat.isDesktop();
	}
}
