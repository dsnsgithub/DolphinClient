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

import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class FPSHud extends SimpleTextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "fpshud");

	public FPSHud() {
		super(true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public String getValue() {
		return String.valueOf(AxoMinecraftClient.getCurrentFps());
	}

	@Override
	public String getPlaceholderValue() {
		return "60";
	}

	@Override
	public String getLabel() {
		return "FPS";
	}
}
