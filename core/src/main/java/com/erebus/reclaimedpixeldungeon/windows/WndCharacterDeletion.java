package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RedButton;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.erebus.reclaimedpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

public final class WndCharacterDeletion extends Window {
    private final int width = ReclaimedWindow.modalWidth( 180 );
    private final RenderedTextBlock message;
    private final RedButton acknowledge;
    private final Component content = new Component();
    private final ScrollPane pane;
    private final float bodyTop;
    private static final String NOTICE = "Deletion of _this character_ was approved after a _second-moderator review_.\n\n"
            + "Selecting _I understand._ acknowledges this notice and permanently deletes _this character save_. "
            + "Your _Wayfarer account and other character saves_ will not be deleted.";

    public WndCharacterDeletion( int slot, String characterId ) {
        RenderedTextBlock title = PixelScene.renderTextBlock( "Character Deletion", 9 );
        title.hardlight( TITLE_COLOR ); title.setPos( 3, 3 ); add( title );
        bodyTop = title.bottom() + 5;
        pane = new ScrollPane( content ); add( pane );
        message = PixelScene.renderTextBlock( NOTICE, 6 ); message.maxWidth( width - 10 );
        message.setPos( 1, 0 ); content.add( message );
        acknowledge = new RedButton( "I understand.", 7 ) {
            @Override protected void onClick() {
                enable( false );
                WayfarerAccountService.acknowledgeCharacterDeletion( slot, characterId, result -> {
                    if (parent == null || WndCharacterDeletion.this.parent == null) return;
                    message.text( NOTICE + "\n\n" + result.message );
                    message.setPos( 1, 0 );
                    layoutNotice(); enable( true );
                } );
            }
        };
        add( acknowledge ); layoutNotice();
    }
    private void layoutNotice() {
        content.setSize( width - 6, message.bottom() + 3 );
        int height = ReclaimedWindow.modalHeight( (int)(bodyTop + content.height() + 30), 0 );
        resize( width, height );
        acknowledge.setRect( 3, height - 23, width - 6, 20 );
        pane.setRect( 3, bodyTop, width - 6, acknowledge.top() - bodyTop - 4 );
    }
    @Override public void onBackPressed() { }
    @Override protected boolean closesOnOutsideClick() { return false; }
}
