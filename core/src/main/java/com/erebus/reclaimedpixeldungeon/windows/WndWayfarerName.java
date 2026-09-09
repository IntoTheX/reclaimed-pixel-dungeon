/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.Dungeon;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.network.WayfarerAccountService;
import com.erebus.reclaimedpixeldungeon.scenes.GameScene;
import com.erebus.reclaimedpixeldungeon.scenes.StartScene;
import com.erebus.reclaimedpixeldungeon.ui.Icons;

public class WndWayfarerName extends WndOptions {

	public WndWayfarerName() {
		super( Icons.get( Icons.WARNING ), "Username Already Taken",
				"The online username _" + Dungeon.hero.characterName() + "_ is already registered to another "
						+ "Wayfarer character. Online character names must be _unique_.\n\n"
						+ "Update this character's name before using the _Wayfarer Map_, _chat_, or _global trading_.",
				"Update Character Name" );
	}

	@Override protected void onSelect( int index ) {
		if (index == 0) promptForName( "" );
	}

	@Override public void onBackPressed() {
		WayfarerAccountService.characterNamePromptFinished();
		super.onBackPressed();
	}

	private static void promptForName( String problem ) {
		String body = problem
				+ (problem.isEmpty() ? "" : "\n\n")
				+ "Choose a _unique online username_ for this character. Names are checked without regard to "
				+ "capitalization or repeated spaces.\n\n"
				+ "_Save-list update:_ The name shown for this character on the game-save list will also change.";
		GameScene.show( new WndTextInput(
				Messages.get( StartScene.class, "name_title" ), body,
				"", 20, false, Messages.get( StartScene.class, "name_confirm" ), null ) {
			@Override public void onSelect( boolean positive, String text ) {
				String name = GamesInProgress.cleanCharacterName( text );
				if (name.isEmpty()) {
					promptForName( "_Name required:_ Please enter a character name." );
					return;
				}
				WayfarerAccountService.renameCurrentCharacter( name, result -> {
					if (result.success) {
						WayfarerAccountService.characterNamePromptFinished();
						GameScene.show( new WndOptions( Icons.get( Icons.CHECKED ),
								"Character Name Updated",
								"This character is now registered online as _" + name + "_.\n\n"
										+ "The _character save list_ has also been updated.", "Close" ) );
					} else if (WayfarerAccountService.isCharacterNameTaken( result )) {
						promptForName( "_Username already taken:_ Choose a different name." );
					} else {
						showFailure( name, result.message );
					}
				} );
			}
		} );
	}

	private static void showFailure( String name, String message ) {
		GameScene.show( new WndOptions( Icons.get( Icons.WARNING ), "Name Update Failed",
				message, "Try Again", "Close" ) {
			@Override protected void onSelect( int index ) {
				if (index == 0) promptForName( "_Previous name:_ " + name );
				else WayfarerAccountService.characterNamePromptFinished();
			}

			@Override public void onBackPressed() {
				WayfarerAccountService.characterNamePromptFinished();
				super.onBackPressed();
			}
		} );
	}
}
