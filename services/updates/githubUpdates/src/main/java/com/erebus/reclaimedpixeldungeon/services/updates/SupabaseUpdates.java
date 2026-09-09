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

package com.erebus.reclaimedpixeldungeon.services.updates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;

public class SupabaseUpdates extends UpdateService {

	private static final String PROJECT_URL = "https://banqbcyyrlkwvyivbdfn.supabase.co";
	private static final String PUBLISHABLE_KEY = "sb_publishable_5wU9dOWG3zzejoEZ1ONvjQ_hKm2uc2K";
	private static final String RELEASE_PATH = "/rest/v1/reclaimed_app_releases"
			+ "?select=version_name,version_code,description,download_url,android_download_url,desktop_download_url"
			+ "&published=eq.true&order=version_code.desc&limit=1";

	@Override
	public boolean supportsUpdatePrompts() {
		return true;
	}

	@Override
	public boolean supportsBetaChannel() {
		return false;
	}

	@Override
	public void checkForUpdate(boolean useMetered, boolean includeBetas, UpdateResultCallback callback) {
		if (!useMetered && !Game.platform.connectedToUnmeteredNetwork()) {
			callback.onConnectionFailed();
			return;
		}

		Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
		request.setUrl(PROJECT_URL + RELEASE_PATH);
		request.setHeader("apikey", PUBLISHABLE_KEY);
		request.setHeader("Accept", "application/json");
		request.setTimeOut(8000);

		Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse(Net.HttpResponse response) {
				try {
					if (response.getStatus().getStatusCode() < 200
							|| response.getStatus().getStatusCode() >= 300) {
						callback.onConnectionFailed();
						return;
					}

					JsonValue rows = new JsonReader().parse(response.getResultAsString());
					JsonValue release = rows == null ? null : rows.child;
					if (release == null) {
						callback.onNoUpdateFound();
						return;
					}

					int versionCode = release.getInt("version_code", Game.versionCode);
					if (versionCode <= Game.versionCode) {
						callback.onNoUpdateFound();
						return;
					}

					AvailableUpdateData update = new AvailableUpdateData();
					update.versionCode = versionCode;
					update.versionName = release.getString("version_name", null);
					update.desc = release.getString("description", null);
					update.URL = platformDownloadUrl(release);

					if (update.URL == null || update.URL.trim().isEmpty()) {
						callback.onConnectionFailed();
					} else {
						callback.onUpdateAvailable(update);
					}
				} catch (Exception e) {
					Game.reportException(e);
					callback.onConnectionFailed();
				}
			}

			@Override
			public void failed(Throwable t) {
				callback.onConnectionFailed();
			}

			@Override
			public void cancelled() {
				callback.onConnectionFailed();
			}
		});
	}

	private static String platformDownloadUrl(JsonValue release) {
		String platformUrl;
		if (DeviceCompat.isAndroid()) {
			platformUrl = release.getString("android_download_url", null);
		} else {
			platformUrl = release.getString("desktop_download_url", null);
		}
		return platformUrl == null || platformUrl.trim().isEmpty()
				? release.getString("download_url", null)
				: platformUrl;
	}

	@Override
	public void initializeUpdate(AvailableUpdateData update) {
		Game.platform.openURI(update.URL);
	}

	@Override
	public boolean supportsReviews() {
		return false;
	}

	@Override
	public void initializeReview(ReviewResultCallback callback) {
		callback.onComplete();
	}

	@Override
	public void openReviewURI() {
		// Reclaimed Pixel Dungeon does not currently have a store review page.
	}
}
