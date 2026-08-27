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

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.util.CachedAPI;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.UUIDHelper;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class LevelHead implements AbstractHypixelMod {
	@Getter
	private static final LevelHead Instance = new LevelHead();
	public final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", "levelhead.enabled.tooltip", false);
	public final BooleanOption background = new BooleanOption("background", false);
	public final ColorOption textColor = new ColorOption("textcolor", ClientColors.GOLD);
	public final EnumOption<Mode> mode = new EnumOption<>("levelHeadMode", Mode.class, Mode.NETWORK);
	private final OptionCategory category = OptionCategory.create("levelhead");

	public static String getDisplayString(Mode mode, String uuid) {
		return "Level: " + mode.getApi().getAsyncNow(uuid).map(() -> "...", () -> "???", val -> {
			if (mode == Mode.BEDWARS) {
				return val + "☆";
			} else {
				return String.valueOf(val);
			}
		});
	}

	public String getDisplayString(String uuid) {
		return getDisplayString(mode.get(), UUIDHelper.sanitizeUUID(uuid));
	}


	@Override
	public void init() {
		enabled.setForceOff(true, "levelhead.enabled.tooltip");
		category.add(enabled);
		category.add(textColor);
		category.add(background);
		category.add(mode);
	}


	@Override
	public OptionCategory getCategory() {
		return category;
	}

	@Getter
	@AllArgsConstructor
	public enum Mode {
		NETWORK(HypixelAbstractionLayer.getInstance().getNetworkLevelApi()),
		BEDWARS(HypixelAbstractionLayer.getInstance().getBedwarsLevelApi()),
		SKYWARS(HypixelAbstractionLayer.getInstance().getSkywarsExpApi());

		private final CachedAPI<String, Integer> api;
	}
}
