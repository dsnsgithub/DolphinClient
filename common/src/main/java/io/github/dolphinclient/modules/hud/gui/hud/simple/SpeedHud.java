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

package io.github.dolphinclient.modules.hud.gui.hud.simple;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.dolphinclient.bridge.entity.AxoEntity;
import io.github.dolphinclient.bridge.entity.AxoPlayer;
import io.github.dolphinclient.bridge.math.Vec3;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public class SpeedHud extends SimpleTextHudEntry {
	private static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "speedhud");
	private final static NumberFormat FORMATTER = new DecimalFormat("#0.00");
	private final BooleanOption horizontal = new BooleanOption("horizontal", true);

	public SpeedHud() {
		super(true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(horizontal);
		return options;
	}

	@Override
	public String getValue() {
		AxoPlayer player = client.br$getPlayer();

		if (player == null) {
			return getPlaceholderValue();
		}

		AxoEntity entity = Objects.requireNonNullElse(player.br$getVehicle(), player);

		Vec3 vec = entity.br$getVelocity();
		if (horizontal.get() || entity.br$isOnGround() && vec.y() < 0) {
			vec = vec.y(0);
		}

		return FORMATTER.format(vec.len() * 20);
	}

	@Override
	public String getPlaceholderValue() {
		return "4.35";
	}

	@Override
	public String getLabel() {
		return "BPS";
	}
}
