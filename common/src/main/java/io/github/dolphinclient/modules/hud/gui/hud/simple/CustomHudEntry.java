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

package io.github.dolphinclient.modules.hud.gui.hud.simple;

import java.util.Collection;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.HudManagerCommon;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.dolphinclient.util.options.GenericOption;

public class CustomHudEntry extends SimpleTextHudEntry {

	private final AxoIdentifier id;
	public final StringOption value = new StringOption("custom_hud.value", "Text");
	private final GenericOption removeEntry;

	public CustomHudEntry(AxoIdentifier id) {
		this.id = id;
		removeEntry = new GenericOption("custom_hud.remove", "custom_hud.remove.label", () -> {
			HudManagerCommon.getInstance().removeEntry(this.id);
			HudManagerCommon.getInstance().saveCustomEntries();
			HudManagerCommon.getInstance().closeScreen();
		});
		AxolotlClientConfig.getInstance().register(new ConfigManager() {
			@Override
			public void save() {
				HudManagerCommon.getInstance().saveCustomEntries();
			}

			@Override
			public void load() {

			}

			@Override
			public OptionCategory getRoot() {
				return getAllOptions();
			}

			@Override
			public Collection<String> getSuppressedNames() {
				return List.of("x", "y");
			}

			@Override
			public void suppressName(String name) {
			}
		});
	}

	@Override
	public String getPlaceholderValue() {
		return value.get();
	}

	@Override
	public String getValue() {
		return value.get();
	}

	@Override
	public AxoIdentifier getId() {
		return id;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(value);
		options.add(removeEntry);
		return options;
	}
}
