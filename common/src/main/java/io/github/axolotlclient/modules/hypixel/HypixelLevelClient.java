/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hypixel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import io.github.axolotlclient.util.UUIDHelper;

/**
 * Loads a player's Hypixel level from {@code https://hypixel.dsns.dev/level/{uuid}}.
 * The endpoint returns a single number and only responds when the user agent contains
 * {@code DolphinClient} followed by the mod version.
 */
public final class HypixelLevelClient {
	public static final String LEVEL_ENDPOINT = "https://hypixel.dsns.dev/level/";
	private static final int MAX_ATTEMPTS = 3;

	private static final HttpClient HTTP = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	private HypixelLevelClient() {
	}

	public static String userAgent(String modVersion) {
		return "DolphinClient " + modVersion;
	}

	public static String formatLevel(Number level) {
		double value = level.doubleValue();
		if (!Double.isFinite(value) || value < 0) {
			return "???";
		}
		return Long.toString((long) Math.floor(value));
	}

	public static String levelUrl(String uuid) {
		return LEVEL_ENDPOINT + dashedUuid(uuid);
	}

	static String dashedUuid(String uuid) {
		String trimmed = uuid.trim();
		if (trimmed.indexOf('-') >= 0) {
			return trimmed;
		}
		return UUIDHelper.fromUndashed(trimmed).toString();
	}

	public static Optional<Double> parseLevel(int status, String body) {
		if (status != 200 || body == null) {
			return Optional.empty();
		}
		String trimmed = body.trim();
		if (trimmed.isEmpty() || trimmed.charAt(0) == '{' || trimmed.charAt(0) == '<') {
			return Optional.empty();
		}
		try {
			double level = Double.parseDouble(trimmed);
			if (!Double.isFinite(level) || level < 0) {
				return Optional.empty();
			}
			return Optional.of(level);
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
	}

	public static Optional<Double> requestLevel(String uuid, String userAgent) {
		final HttpRequest request;
		try {
			request = HttpRequest.newBuilder()
				.uri(URI.create(levelUrl(uuid)))
				.timeout(Duration.ofSeconds(8))
				.header("User-Agent", userAgent)
				.header("Accept", "text/plain, application/json")
				.GET()
				.build();
		} catch (RuntimeException ignored) {
			return Optional.empty();
		}

		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				int status = response.statusCode();
				if ((status == 429 || status >= 500) && attempt < MAX_ATTEMPTS) {
					if (!pause(attempt)) {
						return Optional.empty();
					}
					continue;
				}
				return parseLevel(status, response.body());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Optional.empty();
			} catch (IOException e) {
				if (attempt == MAX_ATTEMPTS || !pause(attempt)) {
					return Optional.empty();
				}
			}
		}
		return Optional.empty();
	}

	private static boolean pause(int attempt) {
		try {
			Thread.sleep(200L * attempt);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}
}
