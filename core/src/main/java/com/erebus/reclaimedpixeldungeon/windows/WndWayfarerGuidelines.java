/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public class WndWayfarerGuidelines extends Window {
	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 180;

	public WndWayfarerGuidelines() {
		RenderedTextBlock title = PixelScene.renderTextBlock( "Wayfarer Chat Guidelines", 9 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2f, 3 );
		add( title );
		Component content = new Component();
		ScrollPane pane = new ScrollPane( content );
		add( pane );
		resize( WIDTH, HEIGHT );
		pane.setRect( 2, title.bottom() + 4, WIDTH - 4, HEIGHT - title.bottom() - 6 );
		String text = "_Stay safe:_ Never share private information. Meet strangers only in public places and tell someone you trust.\n\n"
				+ "_Respect every Wayfarer._ Hate speech, harassment, threats, sexual harassment, excessive targeted profanity, spam, scams, doxxing, sexual content involving minors, ban evasion, and moderator impersonation are prohibited.\n\n"
				+ "_Blocking_ prevents discovery and new messages. A report deliberately shares the latest _30 messages_ and your reason with moderators; ordinary private chats remain inaccessible to them.\n\n"
				+ "Validated violations escalate through _six temporary chat and trade restriction stages_. A clean conduct period reduces the active stage. _Stage 7_ is a two-moderator character deletion review with an _appeal period_. Open _Wayfarer Account > Deletion Review_ to see your deadline and submit an appeal. _Submitting an appeal allows an earlier verdict_. Otherwise a different moderator must wait until the deadline before approving deletion. Approved deletion is shown in a final notice; _I understand._ acknowledges that notice and deletes _only that character save_, never your account or other characters. False or malicious reports may also be reviewed.\n\n"
				+ "For credible real-world danger, preserve evidence and contact the appropriate local authorities.";
		RenderedTextBlock body = PixelScene.renderTextBlock( text, 6 );
		body.maxWidth( WIDTH - 8 );
		body.setPos( 2, 0 );
		content.add( body );
		content.setSize( WIDTH - 4, body.bottom() + 3 );
	}
}
