/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.config.migration.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import io.github.dolphinclient.config.migration.ConfigMigration;

public class V5Migration implements ConfigMigration {
	@Override
	public int version() {
		return 5;
	}

	@Override
	public void apply(JsonObject json) {
		getObject(json, "hypixel-mods")
			.flatMap(hypixel -> getObject(hypixel, "autoboop"))
			.ifPresent(autoboop ->
				getString(autoboop, "autoboop.filterlist").ifPresent(filterlist ->
					autoboop.getAsJsonObject().addProperty("autoboop.filterlist", Arrays.stream(filterlist.split(","))
						.map(s -> s.getBytes(StandardCharsets.UTF_8))
						.map(s -> Base64.getEncoder().encodeToString(s)).collect(Collectors.joining(",")))));
	}
}
