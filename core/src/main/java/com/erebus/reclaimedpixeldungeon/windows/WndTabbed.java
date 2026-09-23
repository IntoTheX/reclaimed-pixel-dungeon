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
import com.erebus.reclaimedpixeldungeon.Chrome;
import com.erebus.reclaimedpixeldungeon.SPDAction;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.Button;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;
import com.watabou.utils.Signal;

import java.util.ArrayList;

public class WndTabbed extends Window {

	protected ArrayList<Tab> tabs = new ArrayList<>();
	protected Tab selected;

	private Signal.Listener<KeyEvent> tabListener;
	
	public WndTabbed() {
		super( 0, 0, Chrome.get( Chrome.Type.TAB_SET ) );

		KeyEvent.addKeyListener(tabListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent keyEvent) {

				if (!keyEvent.pressed && KeyBindings.getActionForKey(keyEvent) == SPDAction.CYCLE){
					int idx = tabs.indexOf(selected);
					idx++;
					if (idx >= tabs.size()) idx = 0;
					tabs.get(idx).onClick();

					return true;
				}

				return false;
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		KeyEvent.removeKeyListener(tabListener);
	}

	protected Tab add(Tab tab ) {

		tab.setPos( tabs.size() == 0 ?
			-chrome.marginLeft() + 1 :
			tabs.get( tabs.size() - 1 ).right(), height );
		tab.select( tab.selected );
		super.add( tab );
		
		tabs.add( tab );

		return tab;
	}
	
	public void select( int index ) {
		select( tabs.get( index ) );
	}
	
	public void select( Tab tab ) {
		if (tab != selected) {
			for (Tab t : tabs) {
				if (t == selected) {
					t.select( false );
				} else if (t == tab) {
					t.select( true );
				}
			}
			
			selected = tab;
			orderTabLayers();
		}
	}
	
	@Override
	public void resize( int w, int h ) {
		// -> super.resize(...)
		this.width = w;
		this.height = h;
		
		chrome.size(
			width + chrome.marginHor(),
			height + chrome.marginVer() );
		
		camera.resize( (int)chrome.width, chrome.marginTop() + height + tabStripHeight() );
		RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK);
		int screenW = (int)(Game.width - insets.left - insets.right);
		int screenH = (int)(Game.height - insets.top - insets.bottom);

		camera.x = (int)(screenW - camera.screenWidth()) / 2;
		camera.x += insets.left;
		camera.y = (int)(screenH - camera.screenHeight()) / 2;
		camera.y += insets.top;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect(
				camera.x / camera.zoom,
				camera.y / camera.zoom,
				chrome.width(), chrome.height );
		// <- super.resize(...)
		
		for (Tab tab : tabs) {
			remove( tab );
		}
		
		ArrayList<Tab> tabs = new ArrayList<>(this.tabs);
		this.tabs.clear();
		
		for (Tab tab : tabs) {
			add( tab );
		}
	}

	public void layoutTabs(){
		if (tabs.isEmpty()) return;
		//subtract two as that horizontal space is transparent at the bottom
		int fullWidth = width+chrome.marginHor()-2;
		int rows = Math.max( 1, Math.min( tabRows(), tabs.size() ) );
		int columns = (tabs.size() + rows - 1) / rows;
		int horizontalOverlap = Math.max( 0, tabHorizontalOverlap() );
		int verticalOverlap = Math.max( 0, Math.min( tabHeight() - 1, tabVerticalOverlap() ) );
		for (int row = 0; row < rows; row++) {
			int rowStart = row * columns;
			int rowCount = Math.min( columns, tabs.size() - rowStart );
			if (rowCount <= 0) break;
			float tabWidth = (fullWidth + horizontalOverlap * (rowCount - 1)) / (float)rowCount;
			float pos = -chrome.marginLeft() + 1;
			for (int column = 0; column < rowCount; column++) {
				Tab tab = tabs.get( rowStart + column );
				tab.setSize( tabWidth, tabHeight() );
				tab.setPos( pos, height + row * (tabHeight() - verticalOverlap) );
				pos = tab.right() - horizontalOverlap;
				PixelScene.align( tab );
			}
		}
		orderTabLayers();
	}

	private void orderTabLayers() {
		if (tabs.isEmpty()) return;
		int rows = Math.max( 1, Math.min( tabRows(), tabs.size() ) );
		int columns = (tabs.size() + rows - 1) / rows;
		for (int row = rows - 1; row >= 0; row--) {
			int rowStart = row * columns;
			int rowCount = Math.min( columns, tabs.size() - rowStart );
			Tab selectedInRow = null;
			for (int column = 0; column < rowCount; column++) {
				Tab tab = tabs.get( rowStart + column );
				if (tab == selected) {
					selectedInRow = tab;
				} else {
					bringToFront( tab );
				}
			}
			if (selectedInRow != null) bringToFront( selectedInRow );
		}
	}

	protected int tabStripHeight() {
		int rows = Math.max( 1, tabRows() );
		int overlap = Math.max( 0, Math.min( tabHeight() - 1, tabVerticalOverlap() ) );
		return tabHeight() + (rows - 1) * (tabHeight() - overlap);
	}

	protected int tabHorizontalOverlap() {
		return 0;
	}

	protected int tabVerticalOverlap() {
		return 0;
	}

	protected int tabRows() {
		return 1;
	}
	
	protected int tabHeight() {
		return 25;
	}
	
	protected void onClick( Tab tab ) {
		select( tab );
	}
	
	protected class Tab extends Button {
		
		protected final int CUT = 5;
		
		protected boolean selected;
		
		protected NinePatch bg;
		
		@Override
		protected void layout() {
			super.layout();
			
			if (bg != null) {
				bg.x = x;
				bg.y = y;
				bg.size( width, height );
			}
		}
		
		protected void select( boolean value ) {
			
			selected = value;
			
			if (bg != null) {
				remove( bg );
			}
			
			bg = Chrome.get( selected ?
				Chrome.Type.TAB_SELECTED :
				Chrome.Type.TAB_UNSELECTED );
			addToBack( bg );
			
			layout();
		}
		
		@Override
		protected void onClick() {
			if (!selected) {
				Sample.INSTANCE.play(Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f);
				WndTabbed.this.onClick(this);
			}
		}
	}
	
	protected class LabeledTab extends Tab {
		
		private RenderedTextBlock btLabel;
		
		public LabeledTab( String label ) {
			
			super();
			
			btLabel.text( label );
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			btLabel = PixelScene.renderTextBlock( 9 );
			add( btLabel );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			btLabel.setPos(
					x + (width - btLabel.width()) / 2,
					y + (height - btLabel.height()) / 2 - (selected ? 1 : 3)
			);
			PixelScene.align(btLabel);
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			btLabel.alpha( selected ? 1.0f : 0.6f );
		}
	}
	
	protected class IconTab extends Tab {
		
		protected Image icon;
		private RectF defaultFrame;
		
		public IconTab( Image icon ){
			super();
			
			this.icon.copy(icon);
			this.defaultFrame = icon.frame();
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			icon = new Image();
			add( icon );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			icon.frame(defaultFrame);
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2 - 1;
			if (!selected) {
				icon.y -= 2;
				//if some of the icon is going into the window, cut it off
				if (icon.y < y + CUT) {
					RectF frame = icon.frame();
					frame.top += (y + CUT - icon.y) / icon.texture.height;
					icon.frame( frame );
					icon.y = y + CUT;
				}
			}
			PixelScene.align(icon);
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			icon.am = selected ? 1.0f : 0.6f;
		}
	}

}
