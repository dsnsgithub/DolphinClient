/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.config.migration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.config.migration.impl.*;
import net.fabricmc.loader.api.FabricLoader;

public interface ConfigMigration {
	int CONFIG_VERSION = 8;
	List<ConfigMigration> MIGRATIONS = new ArrayList<>(List.of(
		new V2Migration(),
		new V3Migration(),
		new V4Migration(),
		new V5Migration(),
		new V6Migration(),
		new V7Migration(),
		new V8Migration(),
		new V9Migration()
	));

	static void apply(int oldVersion, JsonObject config) {
		MIGRATIONS.sort(Comparator.comparingInt(ConfigMigration::version));
		var logger = DolphinClientCommon.getInstance().getLogger();

		var devEnv = FabricLoader.getInstance().isDevelopmentEnvironment();
		if (devEnv) {
			logger.info("Applying config migrations to update from {} to {}", oldVersion, CONFIG_VERSION);
		}
		// We cannot use the option as it hasn't been set at this point yet.
		var debug = false;
		JsonObject general;
		if (config.has("general") && (general = config.getAsJsonObject("general")).isJsonObject() &&
			general.has("debugLogOutput") && general.get("debugLogOutput").getAsBoolean()) {
			debug = true;
		}
		for (var migration : MIGRATIONS) {
			if (oldVersion < migration.version()) {
				if (devEnv) {
					logger.info("Applying config migration ->{}", migration.version());
				}
				migration.apply(config);
			} else if (devEnv && debug) {
				logger.info("Skipping config migration ->{}", migration.version());

			}
		}
		if (devEnv) {
			logger.info("Applied config migrations.");
		}
	}

	/**
	 * The version this config migration applies to,
	 * meaning that it will run for all versions below this one.
	 *
	 * @return the version number
	 */
	int version();

	/**
	 * Migrate the config
	 *
	 * @param config the config (input+output; mutable)
	 */
	void apply(JsonObject config);

	/*
	 * Gson utility methods
	 */
	default Optional<JsonObject> getObject(JsonObject element, String name) {
		if (element.has(name)) {
			var obj = element.get(name);
			if (obj.isJsonObject()) {
				return Optional.of(obj.getAsJsonObject());
			}
		}
		return Optional.empty();
	}

	default JsonObject getOrAddObject(JsonObject element, String name) {
		if (element.has(name)) {
			var obj = element.get(name);
			if (obj.isJsonObject()) {
				return element.get(name).getAsJsonObject();
			}
		}
		var obj = new JsonObject();
		element.add(name, obj);
		return obj;
	}

	default Optional<String> getString(JsonObject element, String name) {
		if (element.has(name)) {
			var obj = element.get(name);
			if (obj.isJsonPrimitive()) {
				return Optional.of(obj.getAsString());
			}
		}
		return Optional.empty();
	}

	default boolean getBooleanOrFalse(JsonObject element, String name) {
		if (element.has(name)) {
			var b = element.get(name);
			if (b.isJsonPrimitive()) {
				return b.getAsBoolean();
			}
		}
		return false;
	}
}
