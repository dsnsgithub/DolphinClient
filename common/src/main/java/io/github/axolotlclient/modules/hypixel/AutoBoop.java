/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
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

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.PlatformDispatch;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;

public class AutoBoop implements AbstractHypixelMod {
	@Getter
	private static final AutoBoop instance = new AutoBoop();
	private static final Pattern FRIEND_JOINED = Pattern.compile("^Friend > (\\b[A-Za-z0-9_§]{3,16}\\b) joined\\.$");

	public void handleMessage(String message) {
		if (!enabled.get()) {
			return;
		}

		ThreadExecuter.scheduleTask(() -> { // execute off-thread since the string manipulation for the filter list could potentially take a bit
			Matcher matcher;
			if ((matcher = FRIEND_JOINED.matcher(message)).matches()) {
				String player = matcher.group(1);
				if (FilterListMode.fromId(filterListMode.get())
					.getFunc().apply(player, filters)) {
					CompletableFuture.runAsync(() -> {
						AxoMinecraftClient.getInstance().br$sendToServer("/boop " + player);
						DolphinClientCommon.getInstance().getLogger().info("Booped " + player);
					}, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS, AxoMinecraftClient.getInstance()));
				}
			}
		});
	}

	protected final List<String> filters = new ArrayList<>();
	protected final OptionCategory cat = OptionCategory.create("autoboop");
	protected final BooleanOption enabled = new BooleanOption("enabled", "autoboop.enabled.tooltip", false);
	protected final GenericOption filterList = new GenericOption("autoboop.filterlist", "autoboop.filterlist.configure", () -> PlatformDispatch.autoBoop$openFiltersScreen(filters)) {
		@Override
		public String toSerializedValue() {
			return filters.stream().map(s -> s.getBytes(StandardCharsets.UTF_8)).map(s -> Base64.getEncoder().encodeToString(s)).collect(Collectors.joining(","));
		}

		@Override
		public void fromSerializedValue(String s) {
			filters.clear();
			filters.addAll(Arrays.stream(s.split(",")).map(v -> Base64.getDecoder().decode(v))
				.map(v -> new String(v, StandardCharsets.UTF_8)).toList());
		}
	};
	protected final StringArrayOption filterListMode = new StringArrayOption("autoboop.filterlist.mode", Arrays.stream(FilterListMode.values()).map(FilterListMode::getId).toArray(String[]::new));

	@Override
	public void init() {
		cat.add(enabled);
		cat.add(filterListMode);
		cat.add(filterList);
	}

	@Override
	public OptionCategory getCategory() {
		return cat;
	}

	@Getter
	private enum FilterListMode {
		DISABLED("disabled", (s, list) -> true),
		WHITELIST("whitelist", (s, list) -> list.contains(s)),
		BLACKLIST("blacklist", (s, list) -> !list.contains(s));

		private final String id;
		private final BiFunction<String, List<String>, Boolean> func;

		FilterListMode(String id, BiFunction<String, List<String>, Boolean> func) {
			this.id = "autoboop.filterlist.mode." + id;
			this.func = func;
		}

		private static final Map<String, FilterListMode> idMap = Arrays.stream(values()).collect(Collectors.toMap(m -> m.id, Function.identity()));

		public static FilterListMode fromId(String id) {
			FilterListMode mode = idMap.get(id);
			if (mode == null) {
				throw new IllegalArgumentException("Could not resolve mode: " + id);
			}
			return mode;
		}
	}
}
