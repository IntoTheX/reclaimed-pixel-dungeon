/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.ui.changelist;

import com.erebus.reclaimedpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Image;

public class TabbedChangeButton extends ChangeButton {

	private final String[] tabLabels;

	public TabbedChangeButton( Image icon, String title, String[] tabLabels, String... messages ) {
		super( icon, title, messages );
		this.tabLabels = tabLabels;
	}

	@Override
	protected void onClick() {
		ShatteredPixelDungeon.scene().addToFront(
				new WndChangesTabbed( new Image( icon ), title, tabLabels, messages ) );
	}
}
