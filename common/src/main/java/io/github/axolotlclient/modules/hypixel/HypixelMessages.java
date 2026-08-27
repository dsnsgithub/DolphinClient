/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.events.types.ReceiveChatMessageEvent;
import io.github.axolotlclient.util.GsonHelper;
import lombok.Getter;

public class HypixelMessages implements Runnable {

	@Getter
	private static final HypixelMessages instance = new HypixelMessages();

	@Getter
	private final Map<String, Map<String, Pattern>> languageMessageMap = new HashMap<>();
	private final Map<String, Map<String, Pattern>> messageLanguageMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	public void load() {
		languageMessageMap.clear();
		messageLanguageMap.clear();

		DolphinClientCommon.getInstance().getLogger().debug("Loading Hypixel Messages");
		AxoMinecraftClient.getInstance().br$getResourceManager().br$listResources("", "lang", id -> id.br$getPath().endsWith(".hypixel.json"))
			.forEach((id, resource) -> {
				int i = id.br$getPath().lastIndexOf("/") + 1;
				String lang = id.br$getPath().substring(i, i + 5);
				DolphinClientCommon.getInstance().getLogger().debug("Found message file: " + id);
				Map<String, String> lines;
				try {
					lines = (Map<String, String>) GsonHelper.read(resource.br$asStream());
				} catch (IOException e) {
					return;
				}
				languageMessageMap.computeIfAbsent(lang, s -> new HashMap<>());
				Map<String, Pattern> map = languageMessageMap.get(lang);
				lines.forEach((key, value) -> {
					Pattern pattern = Pattern.compile(value);
					map.putIfAbsent(key, pattern);
					messageLanguageMap.computeIfAbsent(key, s -> new HashMap<>())
						.put(lang, pattern);
				});
			});
	}

	public void process(BooleanOption option, String messageKey, ReceiveChatMessageEvent event) {
		if (option.get() && matchesAnyLanguage(messageKey, event.getOriginalMessage())) {
			event.setCancelled(true);
		}
	}

	public boolean matchesAnyLanguage(String key, String message) {
		return messageLanguageMap.getOrDefault(key, Collections.emptyMap()).values().stream()
			.map(pattern -> pattern.matcher(message))
			.anyMatch(Matcher::matches);
	}

	public boolean matchesAnyMessage(String lang, String message) {
		return languageMessageMap.getOrDefault(lang, Collections.emptyMap())
			.values().stream().map(pattern -> pattern.matcher(message)).anyMatch(Matcher::matches);
	}

	public boolean matchesAny(String message) {
		return languageMessageMap.values().stream().map(Map::values).anyMatch(patterns -> patterns.stream()
			.map(pattern -> pattern.matcher(message)).anyMatch(Matcher::matches));
	}

	@Override
	public void run() {
		load();
	}
}
