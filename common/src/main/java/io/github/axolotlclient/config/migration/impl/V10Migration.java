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

package io.github.axolotlclient.config.migration.impl;

import java.util.Arrays;
import java.util.Optional;

import com.google.gson.JsonObject;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.api.Options;
import io.github.axolotlclient.config.migration.ConfigMigration;

/**
 * Drops the removed "Social Options" category, carrying the privacy policy
 * decision over to the standalone api config so users are not asked again.
 */
public class V10Migration implements ConfigMigration {
	private static final String CATEGORY = "api.category";
	private static final String PRIVACY_POLICY = "api.privacy_policy_accepted";

	@Override
	public int version() {
		return 10;
	}

	@Override
	public void apply(JsonObject config) {
		getObject(config, CATEGORY).ifPresent(api -> {
			getString(api, PRIVACY_POLICY)
				.flatMap(V10Migration::parseState)
				.ifPresent(DolphinClientCommon.getInstance().getApiOptions().privacyAccepted::set);
			config.remove(CATEGORY);
		});
	}

	private static Optional<Options.PrivacyPolicyState> parseState(String serialized) {
		return Arrays.stream(Options.PrivacyPolicyState.values())
			.filter(state -> state.toString().equals(serialized) || state.name().equalsIgnoreCase(serialized))
			.findFirst();
	}
}
