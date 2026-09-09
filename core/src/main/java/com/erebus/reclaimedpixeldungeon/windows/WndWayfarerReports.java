/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.network.WayfarerModerationAlerts;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndWayfarerReports extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private final RenderedTextBlock status;
	private final Component rows = new Component();
	private final ScrollPane pane = new ScrollPane( rows );
	private final RenderedTextBlock title;
	private final ArrayList<RedButton> reportRows = new ArrayList<>();
	private boolean loading;
	private float refreshDelay;

	public WndWayfarerReports() {
		title = PixelScene.renderTextBlock( "Safety Reports", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 4 );
		add( title );
		status = PixelScene.renderTextBlock( "Loading incident reports...", 6 );
		status.maxWidth( WIDTH - 8 );
		status.setPos( 4, title.bottom() + 6 );
		add( status );
		add( pane );
		resize( WIDTH, 38 );
		pane.visible = false;
		load();
	}

	@Override public void update() {
		super.update();
		refreshDelay -= Game.elapsed;
		if (refreshDelay <= 0 && !loading) load();
	}

	private void load() {
		loading = true;
		refreshDelay = 5f;
		WayfarerAccountService.reports( (result, reports) -> {
			loading = false;
			if (parent == null) return;
			clearReportRows();
			status.visible = !result.success || reports.isEmpty();
			if (status.visible) status.text( result.success ? "No incident reports." : result.message );
			float y = 0;
			for (WayfarerAccountService.WayfarerReport report : reports) {
				String owner = report.assignedModerator.isEmpty() ? "Unassigned" : report.assignedModerator;
				boolean open = "open".equals( report.status );
				boolean closed = "resolved".equals( report.status ) || "dismissed".equals( report.status );
				RedButton row = new RedButton( "#" + report.id + " " + report.reported
						+ " - " + report.status + " - " + owner, 6 ) {
					private float pulse;

					@Override public void update() {
						super.update();
						if (open) {
							pulse += Game.elapsed;
							bg.hardlight( ((int)(pulse / 0.45f) & 1) == 0 ? 0xCC3333 : 0x425FCC );
						} else if (closed) {
							bg.hardlight( 0x777777 );
						}
					}

					@Override protected void onClick() {
						super.onClick();
						WndWayfarerReports.this.hide();
						GameScene.show( new WndIncidentReview( report ) );
					}
				};
				if (closed) row.textColor( 0xCCCCCC );
				row.setRect( 3, y, WIDTH - 6, 18 );
				rows.add( row );
				reportRows.add( row );
				y = row.bottom() + 3;
			}
			float paneTop = title.bottom() + 5;
			if (reports.isEmpty() || !result.success) {
				pane.visible = false;
				resize( WIDTH, (int)Math.max( 38, status.bottom() + 5 ) );
			} else {
				pane.visible = true;
				int windowHeight = (int)Math.min( 180, Math.max( 38, paneTop + y + 3 ) );
				resize( WIDTH, windowHeight );
				pane.setRect( 0, paneTop, WIDTH, windowHeight - paneTop - 3 );
				rows.setSize( WIDTH, Math.max( pane.height(), y ) );
				pane.scrollTo( 0, 0 );
			}
			WayfarerModerationAlerts.refreshNow();
		} );
	}

	private void clearReportRows() {
		for (RedButton row : reportRows) {
			rows.remove( row );
			row.destroy();
		}
		reportRows.clear();
	}
}
