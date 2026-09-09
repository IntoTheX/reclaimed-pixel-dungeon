/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.erebus.reclaimedpixeldungeon.utils;

import com.erebus.reclaimedpixeldungeon.messages.Messages;

public final class CompactNumber {

	private static final String[] SUFFIXES = {
			"", "k", "m", "b", "T", "Qu", "Qi", "Sext", "Sept", "Oct", "Non", "Dec",
			"Undec", "Duodec", "Tredec", "Quattuordec", "Quindec", "Sexdec", "Septendec",
			"Octodec", "Novemdec", "Vig"
	};

	private CompactNumber() {
	}

	public static String format( long amount ) {
		return format( (double)amount );
	}

	public static String format( double amount ) {
		if (Double.isNaN( amount )) return "0";
		if (Double.isInfinite( amount )) return amount < 0 ? "-Inf" : "Inf";

		double magnitude = Math.abs( amount );
		if (magnitude < 1_000d) {
			return Long.toString( Math.round( amount ) );
		}

		int tier = Math.min( SUFFIXES.length - 1,
				(int)Math.floor( Math.log10( magnitude ) / 3d ) );
		double scaled = amount / Math.pow( 1_000d, tier );
		return Messages.decimalFormat( "0.00", scaled ) + SUFFIXES[tier];
	}
}
