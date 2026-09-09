package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public final class WndDeletionReview extends Window {
    private final String characterId = Dungeon.wayfarerCharacterId();
    private final int bodyWidth = ReclaimedWindow.modalWidth( 180 );
    private final Component content = new Component();
    private final RenderedTextBlock body = PixelScene.renderTextBlock( 6 );
    private final RedButton submit;
    private final ScrollPane pane;
    private WayfarerAccountService.DeletionReviewStatus review;
    private String draft = "";

    public WndDeletionReview() {
        RenderedTextBlock title = PixelScene.renderTextBlock( "Deletion Review", 9 );
        title.hardlight( TITLE_COLOR ); title.setPos( 3, 3 ); add( title );
        int height = ReclaimedWindow.modalHeight( 190, 0 );
        pane = new ScrollPane( content ); add( pane );
        submit = new RedButton( "Submit Appeal", 7 ) {
            @Override protected void onClick() { enterAppeal( draft, "" ); }
        };
        submit.enable( false ); add( submit ); resize( bodyWidth, height );
        submit.setRect( 3, height - 23, bodyWidth - 6, 20 );
        pane.setRect( 3, title.bottom() + 4, bodyWidth - 6, submit.top() - title.bottom() - 8 );
        body.maxWidth( bodyWidth - 10 ); content.add( body ); setBody( "Loading deletion review..." );
        load();
    }

    private void load() {
        WayfarerAccountService.deletionReviewStatus( (result, status) -> {
            if (parent == null) return;
            if (!result.success) {
                setBody( result.message );
                submit.enable( review != null && "pending".equals( review.status ) && review.submittedAt.isEmpty() );
                return;
            }
            review = status;
            if (!"pending".equals( review.status )) {
                setBody( "approved".equals( review.status ) ? "_Character deletion has been approved._ Read the final deletion notice."
                        : "This character has _no pending deletion review_." );
                submit.enable( false ); return;
            }
            String text = "_Stage 7: Character Deletion Review_\n\n_Appeal deadline:_ " + review.deadline
                    + "\n\nA _different second moderator_ cannot approve deletion before this deadline unless you submit an appeal. "
                    + "_Submitting an appeal permits an earlier verdict_, including character deletion. Only _this character save_ is affected.";
            if (!review.submittedAt.isEmpty()) text += "\n\n_Appeal received:_ " + review.submittedAt + "\n\n" + review.appeal;
            setBody( text ); submit.enable( review.submittedAt.isEmpty() );
        } );
    }

    private void enterAppeal( String initial, String error ) {
        if (review == null || !"pending".equals( review.status ) || !review.submittedAt.isEmpty()) return;
        GameScene.show( new WndTextInput( "Character Deletion Appeal",
                error + "Explain why deletion should be reconsidered. _10-2000 characters_. "
                        + "Submitting allows the _second moderator to issue an earlier verdict_.",
                initial, 2000, true, "Submit Appeal", "Cancel" ) {
            @Override public void onSelect( boolean positive, String text ) {
                if (!positive || WndDeletionReview.this.parent == null) return;
                draft = text;
                if (text.trim().length() < 10) { enterAppeal( text, "_Your appeal is too short._\n\n" ); return; }
                submit.enable( false );
                WayfarerAccountService.submitDeletionAppeal( characterId, review.requestedAt, text, result -> {
                    if (WndDeletionReview.this.parent == null) return;
                    if (result.success) { draft = ""; load(); }
                    else { setBody( result.message ); load(); GameScene.show( new WndMessage( result.message ) ); }
                } );
            }
        } );
    }

    private void setBody( String text ) {
        body.text( text ); body.setPos( 1, 0 ); content.setSize( bodyWidth - 6, body.bottom() + 4 );
        pane.setRect( pane.left(), pane.top(), pane.width(), pane.height() );
    }
}
