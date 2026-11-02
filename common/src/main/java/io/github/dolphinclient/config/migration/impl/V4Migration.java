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

import java.util.Locale;

import com.google.gson.JsonObject;
import io.github.dolphinclient.config.migration.ConfigMigration;

public class V4Migration implements ConfigMigration {
	@Override
	public int version() {
		return 4;
	}

	@Override
	public void apply(JsonObject json) {
		getObject(json, "storedOptions")
			.ifPresent(hiddenOptions -> {
				JsonObject apiOptions = getOrAddObject(json, "api.category");
				getString(hiddenOptions, "privacyPolicyAccepted").ifPresent(s ->
					apiOptions.addProperty("api.privacy_policy_accepted",
						"privacy_policy_state." + s.toLowerCase(Locale.ROOT)));
			});
	}
}
