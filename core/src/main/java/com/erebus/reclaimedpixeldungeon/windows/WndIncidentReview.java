/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.erebus.reclaimedpixeldungeon.ui.IconButton;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public class WndIncidentReview extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 190;
	private final WayfarerAccountService.WayfarerReport report;
	private static final String[] VIOLATIONS = { "Hate Speech", "Cyberbullying and Harassment", "Excessive Profanity",
			"Threats of Violence", "Sexual Harassment", "Spam and Disruption", "Scams and Trade Fraud",
			"Personal Information and Doxxing", "Sexual Content Involving Minors",
			"Ban Evasion or Moderator Impersonation", "Other" };
	private static final String[] VIOLATION_INFO = {
			"Attacks, slurs, dehumanization, or exclusion based on protected characteristics.",
			"Repeated targeted insults, humiliation, intimidation, stalking, or unwanted contact.",
			"Persistent hostile or disruptive profanity. Occasional non-targeted swearing is not enough.",
			"Credible or implied threats of physical harm, including encouragement of self-harm.",
			"Unwanted sexual remarks, propositions, threats, or repeated sexual comments after rejection.",
			"Message flooding, advertisements, conversation disruption, or repeated unwanted trade solicitations.",
			"Dishonest trades, phishing, credential requests, or impersonation used to obtain items.",
			"Sharing or threatening to expose addresses, legal names, workplaces, schools, or precise locations.",
			"Sexualization, solicitation, grooming, or explicit content involving minors.",
			"Using other characters to evade restrictions or falsely claiming to be a moderator.",
			"A violation not covered above. The moderator note must clearly explain the classification." };

	public WndIncidentReview( WayfarerAccountService.WayfarerReport report ) {
		this.report = report;
		RenderedTextBlock title = PixelScene.renderTextBlock( "Incident #" + report.id, 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 3 );
		add( title );
		Component content = new Component();
		ScrollPane pane = new ScrollPane( content );
		add( pane );
		resize( WIDTH, HEIGHT );
		pane.setRect( 2, title.bottom() + 4, WIDTH - 4, HEIGHT - title.bottom() - 28 );
		float y = addLine( content, "Reporter: " + report.reporter, 0, 0x33CC66 );
		y = addLine( content, "Reported: " + report.reported, y + 2, 0xFF5555 );
		y = addLine( content, "Status: " + report.status, y + 2, TITLE_COLOR );
		if (!report.assignedModerator.isEmpty()) y = addLine( content,
				"Moderator: " + report.assignedModerator, y + 2, TITLE_COLOR );
		y = addLine( content, "Reason: " + report.reason, y + 5, 0xFFFFFF );
		y = addLine( content, "Chat evidence", y + 6, TITLE_COLOR );
		for (String line : report.evidence.split( "\\n" )) {
			int color = line.contains( "| Reporter:" ) ? 0x33CC66
					: line.contains( "| Reported player:" ) ? 0xFF5555 : 0xFFFFFF;
			y = addLine( content, line, y + 2, color );
		}
		content.setSize( WIDTH - 4, y + 4 );

		if ("open".equals( report.status )) {
			RedButton accept = new RedButton( "Accept Case", 7 ) {
				@Override protected void onClick() { super.onClick(); claim(); }
			};
			accept.setRect( 2, HEIGHT - 19, WIDTH - 4, 17 );
			add( accept );
		} else if ("reviewing".equals( report.status ) && report.assignedToCurrentModerator) {
			RedButton valid = new RedButton( "Valid", 7 ) {
				@Override protected void onClick() { super.onClick(); valid(); }
			};
			RedButton invalid = new RedButton( "Invalid", 7 ) {
				@Override protected void onClick() { super.onClick(); invalid(); }
			};
			valid.setRect( 2, HEIGHT - 19, (WIDTH - 6) / 2f, 17 );
			invalid.setRect( valid.right() + 2, HEIGHT - 19, (WIDTH - 6) / 2f, 17 );
			add( valid );
			add( invalid );
		}
	}

	private float addLine( Component content, String text, float y, int color ) {
		RenderedTextBlock line = PixelScene.renderTextBlock( text, 6 );
		line.maxWidth( WIDTH - 8 );
		line.hardlight( color );
		line.setPos( 2, y );
		content.add( line );
		return line.bottom();
	}

	private void claim() {
		WayfarerAccountService.claimIncident( report.id, this::showClaimResult );
	}

	private void showClaimResult( WayfarerAccountService.Result result ) {
		if (parent == null) return;
		if (!result.success) {
			GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Case Not Accepted",
					result.message, "Close" ) );
			return;
		}
		hide();
		GameScene.show( new WndOptions( Icons.get( Icons.CHANGES ), "Case Accepted",
				"Incident #" + report.id + " is now assigned to you.", "Review Case" ) {
			@Override protected void onSelect( int index ) {
				reopenClaimedCase();
			}
		} );
	}

	private void reopenClaimedCase() {
		WayfarerAccountService.reports( (result, reports) -> {
			if (!result.success) {
				GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Case Reload Failed",
						result.message, "Close" ) );
				return;
			}
			for (WayfarerAccountService.WayfarerReport updated : reports) {
				if (updated.id == report.id) {
					GameScene.show( new WndIncidentReview( updated ) );
					return;
				}
			}
			GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Case Unavailable",
					"This incident is no longer available in the moderator queue.", "Close" ) );
		} );
	}

	private void valid() {
		GameScene.show( new WndViolationCategory() );
	}

	private class WndViolationCategory extends Window {
		private static final int SELECT_WIDTH = 180;
		private static final int SELECT_HEIGHT = 180;
		WndViolationCategory() {
			int width = ReclaimedWindow.modalWidth( SELECT_WIDTH );
			RenderedTextBlock heading = PixelScene.renderTextBlock( "Violation Category", 9 );
			heading.hardlight( TITLE_COLOR );
			heading.setPos( (width - heading.width()) / 2f, 3 );
			add( heading );
			RenderedTextBlock help = PixelScene.renderTextBlock(
					"Select the violated guideline. Use the information buttons for examples.", 6 );
			help.maxWidth( width - 6 );
			help.setPos( 3, heading.bottom() + 3 );
			add( help );
			Component content = new Component();
			ScrollPane pane = new ScrollPane( content );
			add( pane );
			resize( width, SELECT_HEIGHT );
			pane.setRect( 2, help.bottom() + 3, width - 4, SELECT_HEIGHT - help.bottom() - 5 );
			float y = 0;
			for (int i = 0; i < VIOLATIONS.length; i++) {
				final int index = i;
				RedButton choice = new RedButton( VIOLATIONS[i], 6 ) {
					@Override protected void onClick() {
						super.onClick(); hide(); requestModeratorNote( VIOLATIONS[index] );
					}
				};
				choice.setRect( 0, y, width - 24, 18 );
				content.add( choice );
				IconButton info = new IconButton( Icons.get( Icons.INFO ) ) {
					@Override protected void onClick() {
						super.onClick();
						GameScene.show( new WndOptions( Icons.get( Icons.INFO ), VIOLATIONS[index], VIOLATION_INFO[index], "Close" ) );
					}
				};
				info.setRect( width - 22, y, 18, 18 );
				content.add( info );
				y += 21;
			}
			content.setSize( width - 4, y );
		}
	}

	private void requestModeratorNote( String category ) {
		GameScene.show( new WndTextInput( category,
				"Briefly explain why the submitted evidence violates this guideline. The server will calculate the sanction stage.",
				"", 500, true, "Validate", "Cancel" ) {
			@Override public void onSelect( boolean positive, String text ) {
				if (positive) WayfarerAccountService.resolveIncident( report.id, true, category,
						text == null ? "" : text.trim(), WndIncidentReview.this::showResult );
			}
		} );
	}

	private void invalid() {
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Dismiss Incident",
				"Mark this report as _invalid_ with no sanctions?", "Confirm", "Cancel" ) {
			@Override protected void onSelect( int index ) {
				if (index == 0) WayfarerAccountService.resolveIncident(
						report.id, false, "", "", WndIncidentReview.this::showResult );
			}
		} );
	}

	private void showResult( WayfarerAccountService.Result result ) {
		if (parent == null) return;
		GameScene.show( new WndOptions( Icons.get( result.success ? Icons.CHANGES : Icons.WARNING ),
				result.success ? "Incident Updated" : "Incident Update Failed", result.message, "Close" ) );
		if (result.success) hide();
	}
}
