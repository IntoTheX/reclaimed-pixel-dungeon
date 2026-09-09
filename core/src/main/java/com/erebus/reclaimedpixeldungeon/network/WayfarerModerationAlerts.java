/* Reclaimed Pixel Dungeon, GPLv3 */
package com.erebus.reclaimedpixeldungeon.network;

import java.util.HashSet;
import java.util.Set;

public final class WayfarerModerationAlerts {
	private static final long POLL_INTERVAL = 10_000L;
	private static final Set<Long> knownOpenReports = new HashSet<>();
	private static boolean moderatorKnown;
	private static boolean moderator;
	private static boolean polling;
	private static long nextPoll;
	private static int openReports;

	private WayfarerModerationAlerts() {}

	public static synchronized void poll() {
		if (polling || System.currentTimeMillis() < nextPoll
				|| !WayfarerAccountService.isSignedIn()
				|| !WayfarerAccountService.currentCharacterEligible()) return;
		polling = true;
		nextPoll = System.currentTimeMillis() + POLL_INTERVAL;
		if (!moderatorKnown) {
			WayfarerAccountService.moderatorStatus( (result, value) -> {
				synchronized (WayfarerModerationAlerts.class) {
					polling = false;
					if (result.success) {
						moderatorKnown = true;
						moderator = value;
						nextPoll = 0;
					}
				}
			} );
		} else if (moderator) {
			WayfarerAccountService.reports( (result, reports) -> {
				synchronized (WayfarerModerationAlerts.class) {
					polling = false;
					if (!result.success) return;
					Set<Long> currentOpen = new HashSet<>();
					for (WayfarerAccountService.WayfarerReport report : reports) {
						if ("open".equals( report.status )) currentOpen.add( report.id );
					}
					openReports = currentOpen.size();
					knownOpenReports.addAll( currentOpen );
				}
			} );
		} else {
			polling = false;
		}
	}

	public static synchronized int openReportCount() { return openReports; }

	public static synchronized void refreshNow() { nextPoll = 0; }
}
