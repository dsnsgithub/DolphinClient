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

package io.github.dolphinclient.modules.hypixel;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.dolphinclient.DolphinClientCommon;
import io.github.dolphinclient.util.CachedAPI;
import io.github.dolphinclient.util.ThreadExecuter;
import io.github.dolphinclient.util.UUIDHelper;
import lombok.Getter;

/**
 * Cache for Hypixel stats. Network level is loaded from hypixel.dsns.dev.
 * The other lookups have no endpoint and complete empty.
 */
public class HypixelAbstractionLayer {
	@Getter
	private static final HypixelAbstractionLayer instance = new HypixelAbstractionLayer();

	private static <V> CachedAPI<String, V> empty() {
		return new CachedAPI<>(uuid -> CompletableFuture.completedFuture(Optional.empty()), 1, true);
	}

	@Getter
	private final CachedAPI<String, BedwarsData> bedwarsDataApi = empty();

	@Getter
	private final CachedAPI<String, Double> networkLevelApi = new CachedAPI<>(uuid -> CompletableFuture
		.supplyAsync(() -> HypixelLevelClient.requestLevel(uuid, HypixelLevelClient.userAgent(DolphinClientCommon.VERSION)),
			ThreadExecuter.service())
		.exceptionally(error -> Optional.empty()), 128, true);

	@Getter
	private final CachedAPI<String, Double> bedwarsLevelApi = empty();

	@Getter
	private final CachedAPI<String, Double> skywarsExpApi = empty();

	@Getter
	private final CachedAPI<String, PlayerData> playerDataApi = empty();

	public void clearPlayerData() {
		bedwarsDataApi.invalidate();
		networkLevelApi.invalidate();
		bedwarsLevelApi.invalidate();
		skywarsExpApi.invalidate();
		playerDataApi.invalidate();
	}

	public void handleDisconnectEvents(UUID uuid) {
		String key = UUIDHelper.toUndashed(uuid);
		bedwarsDataApi.invalidate(key);
		networkLevelApi.invalidate(key);
		bedwarsLevelApi.invalidate(key);
		skywarsExpApi.invalidate(key);
		playerDataApi.invalidate(key);
	}
}
