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

package io.github.axolotlclient.api;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.Module;

/**
 * Configuration of the backend connection.
 * <p>
 * The social features this used to configure have been removed; what remains is
 * the consent gate for the requests the client still makes (Hypixel data and
 * image sharing). It is answered through {@code openPrivacyNoteScreen} rather
 * than through a settings entry, and only acceptance is remembered.
 */
public abstract class Options implements Module {

	protected Supplier<CompletableFuture<Boolean>> openPrivacyNoteScreen = () -> CompletableFuture.completedFuture(false);

	private final OptionCategory apiConfig = OptionCategory.create("api");
	private final ConfigManager apiConfigManager = new JsonConfigManager(DolphinClientCommon.resolveConfigFile("api.json"), apiConfig);

	public final EnumOption<PrivacyPolicyState> privacyAccepted = new EnumOption<>("api.privacy_policy_accepted", PrivacyPolicyState.class, PrivacyPolicyState.UNSET, val -> {
		if (!val.isAccepted() && API.getInstance() != null && API.getInstance().isAuthenticated()) {
			API.getInstance().shutdown();
		}
		// Only acceptance is remembered. Declining means "not right now": since there is no
		// settings entry left to revisit the decision through, persisting it would turn the
		// remaining online features off for good.
		if (val.isAccepted()) {
			apiConfigManager.save();
		}
	});

	@Override
	public void init() {
		apiConfig.add(privacyAccepted);
		apiConfigManager.load();
	}

	public enum PrivacyPolicyState {
		UNSET,
		ACCEPTED() {
			@Override
			public boolean isAccepted() {
				return true;
			}
		},
		DENIED;

		public boolean isAccepted() {
			return false;
		}

		@Override
		public String toString() {
			return "privacy_policy_state." + super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
