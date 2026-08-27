/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
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

package io.github.axolotlclient.api.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.util.CachedAPI;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;

public class UUIDHelper {
	private static final HttpClient CLIENT = NetworkUtil.createHttpClient();

	private static CachedAPI<String, String> create(String endpoint, String jsonKey, String log) {
		return new CachedAPI<>(val -> {
			URI uri;
			try {
				uri = new URI(endpoint + val);
			} catch (Exception e) {
				return CompletableFuture.completedFuture(Optional.empty());
			}
			HttpRequest req = HttpRequest.newBuilder(uri)
				.GET()
				.build();

			return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
				.thenApply(res -> {
					final var obj = GsonHelper.fromJson(res.body());
					if (obj.has(jsonKey)) {
						return Optional.of(obj.get(jsonKey).getAsString());
					}

					API.getInstance().getLogger().debug("Conversion {} failed: {}", log, obj);

					return Optional.empty();
				});
		}, 4096, false);
	}

	public static final CachedAPI<String, String> USERNAME_TO_UUID =
		create("https://api.mojang.com/users/profiles/minecraft/", "id", "username -> uuid");

	public static final CachedAPI<String, String> UUID_TO_USERNAME =
		create("https://sessionserver.mojang.com/session/minecraft/profile/", "name", "uuid -> username");

	public static CompletableFuture<String> tryGetUsernameAsync(String uuid) {
		return UUID_TO_USERNAME.getAsync(uuid).thenApply(s -> s.orElse(uuid));
	}

	public static UUID fromUndashed(String uuid) {
		return UUID.fromString(uuid.trim().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"
		));
	}

	public static CompletableFuture<Optional<String>> ensureUuidOpt(String nameOrUuid) {
		if (nameOrUuid == null || nameOrUuid.isBlank()) {
			return CompletableFuture.completedFuture(Optional.empty());
		}
		try {
			return CompletableFuture.completedFuture(Optional.of(API.sanitizeUUID(nameOrUuid)));
		} catch (IllegalArgumentException e) {
			return USERNAME_TO_UUID.getAsync(nameOrUuid);
		}
	}

	public static CompletableFuture<String> ensureUuid(String nameOrUuid) {
		return ensureUuidOpt(nameOrUuid).thenApply(s -> s.orElse(nameOrUuid));
	}

	public static String toUndashed(UUID uuid) {
		return API.sanitizeUUID(uuid.toString());
	}
}
