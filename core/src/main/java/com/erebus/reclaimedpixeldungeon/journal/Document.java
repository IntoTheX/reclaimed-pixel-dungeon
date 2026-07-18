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

package com.erebus.reclaimedpixeldungeon.journal;

import com.erebus.reclaimedpixeldungeon.Badges;
import com.erebus.reclaimedpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.erebus.reclaimedpixeldungeon.messages.Messages;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSprite;
import com.erebus.reclaimedpixeldungeon.sprites.ItemSpriteSheet;
import com.erebus.reclaimedpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

import java.util.Collection;
import java.util.LinkedHashMap;

public enum Document {
	
	ADVENTURERS_GUIDE(ItemSpriteSheet.GUIDE_PAGE, false),
	ALCHEMY_GUIDE(ItemSpriteSheet.ALCH_PAGE, false),

	INTROS(Icons.STAIRS, true),
	SEWERS_GUARD(ItemSpriteSheet.SEWER_PAGE, true),
	PRISON_WARDEN(ItemSpriteSheet.PRISON_PAGE, true),
	CAVES_EXPLORER(ItemSpriteSheet.CAVES_PAGE, true),
	CITY_WARLOCK(ItemSpriteSheet.CITY_PAGE, true),
	HALLS_KING(ItemSpriteSheet.HALLS_PAGE, true);
	
	Document( int sprite, boolean lore ){
		pageIcon = null;
		pageSprite = sprite;
		loreDocument = lore;
	}

	Document( Icons icon, boolean lore ){
		pageIcon = icon;
		pageSprite = 0;
		loreDocument = lore;
	}

	public static final int NOT_FOUND = 0;
	public static final int FOUND = 1;
	public static final int READ = 2;
	private LinkedHashMap<String, Integer> pagesStates = new LinkedHashMap<>();
	
	public boolean findPage( String page ) {
		if (pagesStates.containsKey(page) && pagesStates.get(page) == NOT_FOUND){
			pagesStates.put(page, FOUND);
			Journal.saveNeeded = true;
			Badges.validateCatalogBadges();
			return true;
		}
		return false;
	}

	public boolean findPage( int pageIdx ) {
		return findPage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean deletePage( String page ){
		if (pagesStates.containsKey(page) && pagesStates.get(page) != NOT_FOUND){
			pagesStates.put(page, NOT_FOUND);
			Journal.saveNeeded = true;
			return true;
		}
		return false;
	}

	public boolean deletePage( int pageIdx ) {
		return deletePage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean unreadPage( String page ){
		if (pagesStates.containsKey(page) && pagesStates.get(page) == READ){
			pagesStates.put(page, FOUND);
			Journal.saveNeeded = true;
			return true;
		}
		return false;
	}

	public boolean unreadPage( int pageIdx ) {
		return deletePage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean isPageFound( String page ){
		return pagesStates.containsKey(page) && pagesStates.get(page) > NOT_FOUND;
	}

	public boolean isPageFound( int pageIdx ){
		return isPageFound( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean anyPagesFound(){
		for( Integer val : pagesStates.values()){
			if (val != NOT_FOUND){
				return true;
			}
		}
		return false;
	}

	public boolean allPagesFound(){
		for( Integer val : pagesStates.values()){
			if (val == NOT_FOUND){
				return false;
			}
		}
		return true;
	}

	public boolean readPage( String page ) {
		if (pagesStates.containsKey(page)){
			pagesStates.put(page, READ);
			Journal.saveNeeded = true;
			Badges.validateCatalogBadges();
			return true;
		}
		return false;
	}

	public boolean readPage( int pageIdx ) {
		return readPage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean isPageRead( String page ){
		return pagesStates.containsKey(page) && pagesStates.get(page) == READ;
	}

	public boolean isPageRead( int pageIdx ){
		return isPageRead( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public Collection<String> pageNames(){
		return pagesStates.keySet();
	}

	public int pageIdx(String name){
		int i = 0;
		for( String page : pagesStates.keySet()){
			if (page.equals(name)){
				return i;
			}
			i++;
		}
		return -1;
	}

	private int pageSprite;
	private Icons pageIcon;
	public Image pageSprite(){
		return pageSprite("");
	}

	public Image pageSprite(String page){
		if (page.isEmpty() || !isPageFound(page) || this != ADVENTURERS_GUIDE){
			if (pageIcon != null){
				return Icons.get(pageIcon);
			} else {
				return new ItemSprite(pageSprite);
			}
		} else {
			//special per-page visuals for guidebook
			switch (page){
				case Document.GUIDE_INTRO: default:
					return new ItemSprite(ItemSpriteSheet.MASTERY);
				case "Examining":
					return Icons.get(Icons.MAGNIFY);
				case "Surprise_Attacks":
					return Icons.get(Icons.SNAKE);
				case "Identifying":
					return new ItemSprite( new ScrollOfIdentify() );
				case "Food":
					return new ItemSprite( ItemSpriteSheet.PASTY );
				case "Alchemy":
					return new ItemSprite( ItemSpriteSheet.TRINKET_CATA );
				case "Dieing":
					return new ItemSprite( ItemSpriteSheet.TOMB );
				case Document.GUIDE_SEARCHING:
					return Icons.get(Icons.MAGNIFY);
				case "Strength":
					return new ItemSprite( ItemSpriteSheet.GREATAXE );
				case "Upgrades":
					return new ItemSprite( ItemSpriteSheet.RING_EMERALD );
				case "Looting":
					return new ItemSprite( ItemSpriteSheet.CRYSTAL_KEY );
				case "Levelling":
					return Icons.get(Icons.TALENT);
				case "Positioning":
					return new ItemSprite( ItemSpriteSheet.SPIRIT_BOW );
				case "Magic":
					return new ItemSprite( ItemSpriteSheet.WAND_FIREBOLT );
				case Document.GUIDE_HOMEBASE:
					return Icons.get( Icons.STAIRS_GRASS );
				case Document.GUIDE_MATERIALS:
					return new ItemSprite( ItemSpriteSheet.HOMEBASE_WOOD );
				case Document.GUIDE_RETURNING:
					return new ItemSprite( ItemSpriteSheet.SCROLL_EHWAZ );
				case Document.GUIDE_REBUILDING:
					return new ItemSprite( ItemSpriteSheet.BUILDING_STONE );
				case Document.GUIDE_FOUNDERS_CAMP:
					return new ItemSprite( ItemSpriteSheet.KIT );
				case Document.GUIDE_QUARTERMASTER_VAULT:
					return new ItemSprite( ItemSpriteSheet.CHEST );
				case Document.GUIDE_EMBERFORGE:
					return new ItemSprite( ItemSpriteSheet.BUILDING_IRON );
				case Document.GUIDE_ALCHEMISTS_STILL:
					return new ItemSprite( ItemSpriteSheet.POTION_HOLDER );
				case Document.GUIDE_MOONROOT_GARDEN:
					return new ItemSprite( ItemSpriteSheet.SEED_STARFLOWER );
				case Document.GUIDE_DEFENSE_WALLS:
					return new ItemSprite( ItemSpriteSheet.BUILDING_STONE );
				case Document.GUIDE_DEFENSE_TOWERS:
					return new ItemSprite( ItemSpriteSheet.BUILDING_STONE );
				case Document.GUIDE_RARITY_STATS:
					return Icons.get( Icons.STATS );
				case Document.GUIDE_CATALYSTS:
					return new ItemSprite( ItemSpriteSheet.STONE_PRISMFORGE );
				case Document.GUIDE_MOB_STATS:
					return Icons.get( Icons.SKULL );
				case Document.GUIDE_RAIDS:
					return Icons.get( Icons.CHALLENGE_COLOR );
				case Document.GUIDE_DUNGEON_PRESSURE:
					return new ItemSprite( ItemSpriteSheet.MOB_HOLDER );
				case Document.GUIDE_DEFENDERS:
					return Icons.get( Icons.INFO );
				case Document.GUIDE_BAGS_STORAGE:
					return new ItemSprite( ItemSpriteSheet.MATERIAL_SATCHEL );
				case Document.GUIDE_FORGE_STILL:
					return Icons.get( Icons.ALCHEMY );
			}
		}
	}

	private boolean loreDocument;
	public boolean isLoreDoc(){
		return loreDocument;
	}
	
	public String title(){
		return Messages.get( this, name() + ".title");
	}

	public String discoverHint(){
		return Messages.get( this, name() + ".discover_hint");
	}
	
	public String pageTitle( String page ){
		return Messages.get( this, name() + "." + page + ".title");
	}
	
	public String pageTitle( int pageIdx ){
		return pageTitle( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}
	
	public String pageBody( String page ){
		return Messages.get( this, name() + "." + page + ".body");
	}
	
	public String pageBody( int pageIdx ){
		return pageBody( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public static final String GUIDE_INTRO          = "Intro";
	public static final String GUIDE_EXAMINING      = "Examining";
	public static final String GUIDE_SURPRISE_ATKS  = "Surprise_Attacks";
	public static final String GUIDE_IDING          = "Identifying";
	public static final String GUIDE_FOOD           = "Food";
	public static final String GUIDE_ALCHEMY        = "Alchemy";
	public static final String GUIDE_DIEING         = "Dieing";
	public static final String GUIDE_HOMEBASE       = "Homebase";
	public static final String GUIDE_MATERIALS      = "Recovered_Materials";
	public static final String GUIDE_RETURNING      = "Returning_Home";
	public static final String GUIDE_REBUILDING     = "Rebuilding";
	public static final String GUIDE_FOUNDERS_CAMP  = "Founders_Camp";
	public static final String GUIDE_QUARTERMASTER_VAULT = "Quartermasters_Vault";
	public static final String GUIDE_EMBERFORGE     = "Emberforge";
	public static final String GUIDE_ALCHEMISTS_STILL = "Alchemists_Still";
	public static final String GUIDE_MOONROOT_GARDEN = "Moonroot_Garden";
	public static final String GUIDE_DEFENSE_WALLS  = "Defense_Walls";
	public static final String GUIDE_DEFENSE_TOWERS = "Defense_Towers";
	public static final String GUIDE_RARITY_STATS   = "Rarity_Stats";
	public static final String GUIDE_CATALYSTS      = "Catalysts";
	public static final String GUIDE_MOB_STATS      = "Mob_Stats";
	public static final String GUIDE_RAIDS          = "Raids";
	public static final String GUIDE_DUNGEON_PRESSURE = "Dungeon_Pressure";
	public static final String GUIDE_DEFENDERS      = "Defenders";
	public static final String GUIDE_BAGS_STORAGE   = "Bags_Storage";
	public static final String GUIDE_FORGE_STILL    = "Forge_Still";

	public static final String GUIDE_SEARCHING      = "Searching";

	public static final String KING_ATTRITION       = "attrition";

	//pages and default states
	static {
		boolean debug = DeviceCompat.isDebug();
		//hero gets these when guidebook is collected
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_INTRO,          debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_EXAMINING,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_SURPRISE_ATKS,  debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_IDING,          debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_FOOD,           debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_ALCHEMY,        debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_DIEING,         debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_HOMEBASE,       debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_MATERIALS,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_RETURNING,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_REBUILDING,     debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_FOUNDERS_CAMP,  debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_QUARTERMASTER_VAULT, debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_EMBERFORGE,     debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_ALCHEMISTS_STILL, debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_MOONROOT_GARDEN, debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_DEFENSE_WALLS,  debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_RARITY_STATS,   debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_CATALYSTS,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_MOB_STATS,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_RAIDS,          debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_DUNGEON_PRESSURE, debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_DEFENDERS,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_BAGS_STORAGE,   debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_FORGE_STILL,    debug ? READ : NOT_FOUND);
		//given in sewers
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_SEARCHING,      debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Strength",           debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Upgrades",           debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Looting",            debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Levelling",          debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Positioning",        debug ? READ : NOT_FOUND);
		ADVENTURERS_GUIDE.pagesStates.put("Magic",              debug ? READ : NOT_FOUND);
		
		//given in sewers
		ALCHEMY_GUIDE.pagesStates.put("Potions",                debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Stones",                 debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Energy_Food",            debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Exotic_Potions",         debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Exotic_Scrolls",         debug ? READ : NOT_FOUND);
		//given in prison
		ALCHEMY_GUIDE.pagesStates.put("Bombs",                  debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Weapons",                debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Brews_Elixirs",          debug ? READ : NOT_FOUND);
		ALCHEMY_GUIDE.pagesStates.put("Spells",                 debug ? READ : NOT_FOUND);

		INTROS.pagesStates.put("Dungeon",                       READ);
		INTROS.pagesStates.put("Sewers",                        debug ? READ : NOT_FOUND);
		INTROS.pagesStates.put("Prison",                        debug ? READ : NOT_FOUND);
		INTROS.pagesStates.put("Caves",                         debug ? READ : NOT_FOUND);
		INTROS.pagesStates.put("City",                          debug ? READ : NOT_FOUND);
		INTROS.pagesStates.put("Halls",                         debug ? READ : NOT_FOUND);

		SEWERS_GUARD.pagesStates.put("new_position",            debug ? READ : NOT_FOUND);
		SEWERS_GUARD.pagesStates.put("dangerous",               debug ? READ : NOT_FOUND);
		SEWERS_GUARD.pagesStates.put("crabs",                   debug ? READ : NOT_FOUND);
		SEWERS_GUARD.pagesStates.put("guild",                   debug ? READ : NOT_FOUND);
		SEWERS_GUARD.pagesStates.put("lost",                    debug ? READ : NOT_FOUND);
		SEWERS_GUARD.pagesStates.put("not_worth",               debug ? READ : NOT_FOUND);

		PRISON_WARDEN.pagesStates.put("journal",                debug ? READ : NOT_FOUND);
		PRISON_WARDEN.pagesStates.put("recruits",               debug ? READ : NOT_FOUND);
		PRISON_WARDEN.pagesStates.put("mines",                  debug ? READ : NOT_FOUND);
		PRISON_WARDEN.pagesStates.put("rotberry",               debug ? READ : NOT_FOUND);
		PRISON_WARDEN.pagesStates.put("no_support",             debug ? READ : NOT_FOUND);
		PRISON_WARDEN.pagesStates.put("letter",                 debug ? READ : NOT_FOUND);

		CAVES_EXPLORER.pagesStates.put("expedition",            debug ? READ : NOT_FOUND);
		CAVES_EXPLORER.pagesStates.put("gold",                  debug ? READ : NOT_FOUND);
		CAVES_EXPLORER.pagesStates.put("troll",                 debug ? READ : NOT_FOUND);
		CAVES_EXPLORER.pagesStates.put("city",                  debug ? READ : NOT_FOUND);
		CAVES_EXPLORER.pagesStates.put("alive",                 debug ? READ : NOT_FOUND);
		CAVES_EXPLORER.pagesStates.put("report",                debug ? READ : NOT_FOUND);

		CITY_WARLOCK.pagesStates.put("old_king",                debug ? READ : NOT_FOUND);
		CITY_WARLOCK.pagesStates.put("resistance",              debug ? READ : NOT_FOUND);
		CITY_WARLOCK.pagesStates.put("failure",                 debug ? READ : NOT_FOUND);
		CITY_WARLOCK.pagesStates.put("more_powerful",           debug ? READ : NOT_FOUND);
		CITY_WARLOCK.pagesStates.put("new_power",               debug ? READ : NOT_FOUND);
		CITY_WARLOCK.pagesStates.put("seen_it",                 debug ? READ : NOT_FOUND);

		HALLS_KING.pagesStates.put("Rejection",                 debug ? READ : NOT_FOUND);
		HALLS_KING.pagesStates.put("amulet",                    debug ? READ : NOT_FOUND);
		HALLS_KING.pagesStates.put("ritual",                    debug ? READ : NOT_FOUND);
		HALLS_KING.pagesStates.put("new_king",                  debug ? READ : NOT_FOUND);
		HALLS_KING.pagesStates.put("thing",                     debug ? READ : NOT_FOUND);
		HALLS_KING.pagesStates.put(KING_ATTRITION,              debug ? NOT_FOUND : NOT_FOUND);

	}
	
	private static final String DOCUMENTS = "documents";
	
	public static void store( Bundle bundle ){
		
		Bundle docsBundle = new Bundle();
		
		for ( Document doc : values()){
			Bundle pagesBundle = new Bundle();
			boolean empty = true;
			for (String page : doc.pageNames()){
				if (doc.pagesStates.get(page) != NOT_FOUND){
					pagesBundle.put(page, doc.pagesStates.get(page));
					empty = false;
				}
			}
			if (!empty){
				docsBundle.put(doc.name(), pagesBundle);
			}
		}
		
		bundle.put( DOCUMENTS, docsBundle );
		
	}
	
	public static void restore( Bundle bundle ){
		
		if (!bundle.contains( DOCUMENTS )){
			return;
		}
		
		Bundle docsBundle = bundle.getBundle( DOCUMENTS );
		
		for ( Document doc : values()){
			if (docsBundle.contains(doc.name())){
				Bundle pagesBundle = docsBundle.getBundle(doc.name());

				for (String page : doc.pageNames()) {
					if (pagesBundle.contains(page)) {
						doc.pagesStates.put(page, pagesBundle.getInt(page));
					}
				}
			}
		}
	}
	
}
