/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.windows;

import com.erebus.reclaimedpixeldungeon.scenes.PixelScene;
import com.erebus.reclaimedpixeldungeon.ui.RenderedTextBlock;
import com.erebus.reclaimedpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ui.Component;

public class WndModeratorGuidelines extends WndTabbed {

	private static final int WIDTH = ReclaimedWindow.modalWidth( 180 );
	private static final int HEIGHT = 180;
	private final Component content = new Component();
	private final ScrollPane pane = new ScrollPane( content );
	private final RenderedTextBlock body;

	public WndModeratorGuidelines() {
		body = PixelScene.renderTextBlock( 6 );
		body.maxWidth( WIDTH - 12 );
		body.setPos( 3, 3 );
		content.add( body );
		add( pane );
		resize( WIDTH, HEIGHT );
		pane.setRect( 2, 2, WIDTH - 4, HEIGHT - 4 );

		Tab duties = add( new LabeledTab( "Duties" ) );
		add( new LabeledTab( "Tools" ) );
		add( new LabeledTab( "Rules" ) );
		add( new LabeledTab( "Rewards" ) );
		layoutTabs();
		select( duties );
		showPage( 0 );
	}

	@Override protected void onClick( Tab tab ) {
		super.onClick( tab );
		showPage( tabs.indexOf( tab ) );
	}

	private void showPage( int page ) {
		if (page == 0) body.text( duties() );
		else if (page == 1) body.text( tools() );
		else if (page == 2) body.text( guidelines() );
		else body.text( rewards() );
		body.maxWidth( WIDTH - 12 );
		body.setPos( 3, 3 );
		content.setSize( WIDTH - 6, Math.max( pane.height(), body.bottom() + 5 ) );
		pane.scrollTo( 0, 0 );
	}

	private static String duties() {
		return "_WELCOME, MODERATOR_\n\n"
				+ "You help keep the Wayfarer community welcoming, fair, and safe. Your role is to review what was actually reported, not to investigate private conversations or punish players based on assumptions.\n\n"
				+ "_YOUR EXPECTED TASKS_\n\n"
				+ "_Review fairly:_ Read the report reason and all 30 submitted chat messages. Consider context, repetition, severity, and who said each message.\n\n"
				+ "_Protect privacy:_ Never copy report evidence into Moderator Space or share a player's private information. Evidence belongs only inside its incident report.\n\n"
				+ "_Be consistent:_ Match confirmed behavior to the written violation categories. Personal disagreements are not violations by themselves.\n\n"
				+ "_Finish what you accept:_ Accept a case only when you can review it carefully. Once assigned, other moderators cannot take it from you.\n\n"
				+ "_Explain the outcome:_ Choose Valid only when the evidence supports a guideline violation. Choose Invalid when it does not. Keep moderator notes factual and respectful.\n\n"
				+ "_Example:_ A rude disagreement may be unpleasant but not punishable. Repeatedly following someone across chats to insult or intimidate them can qualify as harassment.";
	}

	private static String tools() {
		return "_MODERATION TOOLS_\n\n"
				+ "_Safety Reports:_ Open reports appear first and blink until a moderator accepts them. Resolved and dismissed reports remain readable for context and accountability.\n\n"
				+ "_Accept Case:_ Assigns the incident to your moderator character. This prevents two moderators from issuing competing decisions.\n\n"
				+ "_Valid:_ Use this only when the submitted evidence clearly matches a violation. Select the closest category; the sanction stage is calculated automatically from the character's confirmed history.\n\n"
				+ "_Invalid:_ Use this when evidence is missing, taken out of context, or does not meet a guideline. Disliking a message is not enough on its own.\n\n"
				+ "_Moderator Space:_ Coordinate workloads and ask policy questions here. Do not paste report evidence, email addresses, exact locations, or other private details into the group chat.\n\n"
				+ "_Appeals and deletion:_ Stage 7 requires a separate deletion review, an appeal opportunity, and approval from a second moderator. Never promise an outcome before that process is complete.\n\n"
				+ "_Example workflow:_ Open report > verify speakers and context > accept case > compare evidence with Rules > write a concise note > mark Valid or Invalid.";
	}

	private static String guidelines() {
		return "_WAYFARER CHAT GUIDELINES_\n\n"
				+ "Players may disagree, joke, or use occasional non-targeted profanity. A violation requires behavior that fits a category below. Context matters, but serious threats, doxxing, and child safety violations must be treated urgently.\n\n"
				+ "_Hate Speech_\nAttacks, slurs, dehumanization, or exclusion based on protected characteristics.\n_Example:_ Claiming a racial, religious, gender, disability, or identity group is inferior or should not be allowed to play.\n\n"
				+ "_Cyberbullying and Harassment_\nRepeated targeted insults, humiliation, intimidation, stalking, or unwanted contact.\n_Example:_ Continuing to insult and message a player after they clearly ask to be left alone.\n\n"
				+ "_Excessive Profanity_\nPersistent hostile or disruptive profanity, especially when aimed at another player. Occasional non-targeted swearing is not enough.\n_Example:_ Flooding a conversation with abusive profanity to drive someone away.\n\n"
				+ "_Threats of Violence_\nCredible or implied threats of physical harm, including encouragement of self-harm.\n_Example:_ Threatening to find and hurt a player outside the game.\n\n"
				+ "_Sexual Harassment_\nUnwanted sexual remarks, propositions, threats, or repeated sexual comments after rejection.\n_Example:_ Repeatedly requesting sexual content after the recipient says no.\n\n"
				+ "_Spam and Disruption_\nMessage flooding, advertisements, deliberate conversation disruption, or repeated unwanted trade requests.\n_Example:_ Sending the same trade solicitation dozens of times after refusal.\n\n"
				+ "_Scams and Trade Fraud_\nDishonest trades, phishing, credential requests, or impersonation intended to obtain items or access.\n_Example:_ Asking for an account password while pretending it is needed to complete a trade.\n\n"
				+ "_Personal Information and Doxxing_\nSharing or threatening to expose addresses, legal names, workplaces, schools, or precise locations.\n_Example:_ Posting a player's home address or threatening to reveal it unless they trade.\n\n"
				+ "_Sexual Content Involving Minors_\nSexualization, solicitation, grooming, or explicit content involving minors.\n_Example:_ Requesting explicit images from someone known or believed to be underage.\n\n"
				+ "_Ban Evasion or Moderator Impersonation_\nUsing other characters to evade restrictions or falsely claiming moderator authority.\n_Example:_ Creating another character to contact the same player during an active restriction.\n\n"
				+ "_Other_\nA serious guideline violation not covered above. The moderator note must clearly explain why it belongs here. Do not use Other merely because classification is uncertain.\n\n"
				+ "_Remember:_ A report shares only the latest 30 submitted messages and its stated reason. Judge only the evidence available. For credible real-world danger, preserve the report and follow the appropriate emergency escalation process.";
	}

	private static String rewards() {
		return "_THANK YOU FOR SHOWING UP_\n\n"
				+ "Moderation takes patience, attention, and care. These rewards are a small thank-you for spending part of your playtime helping other Wayfarers feel safer and better supported. Each moderator character has its own progress and rewards.\n\n"
				+ "_WELCOME BONUS_\nChoose one special chest: an Artifact chest, Trinket chest, 25 random Catalysts, or 100 random homebase Resources. Every choice also includes _1 Spatial Geode_. Artifact and Trinket choices show three independently rolled options for you to preview before committing.\n\n"
				+ "_HOURLY APPRECIATION_\nEvery completed hour of _active online service_ unlocks three independently rolled choices using the Active Play Reward pool. Moderator choices are always _Rare, Epic, Legendary, or Transcendant_, and there is no three-hour daily reward limit. Activity can be continued throughout the same UTC day, while the usual three-minute AFK protection excludes unattended time.\n\n"
				+ "_WEEKLY APPRECIATION_\nReach _15 active hours_ during the UTC week to choose _50 random Catalysts_, _1000 random Resources_, _1 Spatial Geode_, or _5 Ascendant Sparks_. Every eligible hour also continues to count toward this weekly goal.\n\n"
				+ "_WHAT COUNTS AS ACTIVE_\nMoving, fighting, collecting items, sending chat messages, and performing moderation actions keep the shift moving. Attacks and kills made by your _allies or corrupted mobs_ also count while you play. If no qualifying action occurs for more than _3 minutes_, the timer stops at the AFK boundary and resumes when you actively play or moderate.\n\n"
				+ "_YOUR SERVICE RECORD_\nThe _calendar button_ in Moderator Space shows today's shift, this week's progress, this character's lifetime service, and your combined moderator service across every eligible character. Lifetime totals do not reset when daily or weekly rewards roll over.\n\n"
				+ "_CLAIM WHEN READY_\nA blinking golden chest appears in Moderator Space whenever a reward is waiting. Preview every choice at your own pace. Once confirmed, items enter your inventory; anything that does not fit is placed safely at your feet.";
	}
}
