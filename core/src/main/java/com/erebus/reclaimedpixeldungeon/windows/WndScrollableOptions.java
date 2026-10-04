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

import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/** A fixed-size choice window whose body remains clipped and scrollable. */
public class WndScrollableOptions extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int PREFERRED_HEIGHT = 150;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;
	private boolean optionSelected;

	public WndScrollableOptions(Image icon, String title, String message, String... options) {
		this(icon, title, message, null, options);
	}

	public WndScrollableOptions(Image icon, String title, String message,
			boolean[] purpleOptions, String... options) {
		int width = ReclaimedWindow.modalWidth(PixelScene.landscape() ? WIDTH_L : WIDTH_P);

		IconTitle titlebar = new IconTitle(icon, title);
		titlebar.setRect(0, 0, width, 0);
		add(titlebar);

		float bodyTop = titlebar.bottom() + 2 * MARGIN;
		int height = ReclaimedWindow.modalHeight(PREFERRED_HEIGHT, 0);
		resize(width, height);

		int contentWidth = width - 2 * MARGIN;
		Component content = new Component();
		ArrayList<RedButton> buttons = new ArrayList<>();
		RenderedTextBlock explanation = PixelScene.renderTextBlock(message, 6);
		explanation.maxWidth(contentWidth - 2);
		explanation.setPos(1, 0);
		content.add(explanation);

		float y = explanation.bottom() + 2 * MARGIN;
		for (int i = 0; i < options.length; i++) {
			final int index = i;
			RedButton button = new RedButton(options[i]) {
				@Override
				protected void onClick() {
					selectOption(index);
				}
			};
			button.multiline = true;
			if (purpleOptions != null && i < purpleOptions.length && purpleOptions[i]) {
				button.backgroundColor( 0x9B59D0 );
			}
			button.setRect(0, y, contentWidth - 2, BUTTON_HEIGHT);
			content.add(button);
			buttons.add(button);
			y = button.bottom() + MARGIN;
		}
		content.setSize(contentWidth, Math.max(y, height - bodyTop - MARGIN));

		ScrollPane pane = new ScrollPane(content) {
			@Override
			public void onClick(float x, float y) {
				for (int i = 0; i < buttons.size(); i++) {
					RedButton button = buttons.get(i);
					if (x >= button.left() && x <= button.right()
							&& y >= button.top() && y <= button.bottom()) {
						selectOption(i);
						return;
					}
				}
			}
		};
		pane.setRect(MARGIN, bodyTop, contentWidth, height - bodyTop - MARGIN);
		add(pane);
	}

	protected void onSelect(int index) {
	}

	private void selectOption(int index) {
		if (optionSelected) return;
		optionSelected = true;
		hide();
		onSelect(index);
	}
}
