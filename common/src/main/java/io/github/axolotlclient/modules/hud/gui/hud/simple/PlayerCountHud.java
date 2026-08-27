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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class PlayerCountHud extends SimpleTextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "playercounthud");

	public PlayerCountHud() {
		super(true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public String getValue() {
		if (client.br$getWorld() == null) {
			return getPlaceholderValue();
		}

		return String.valueOf(client.br$getOnlinePlayers().size());
	}

	@Override
	public String getPlaceholderValue() {
		return String.valueOf(3.141592);
	}

	@Override
	public String getLabel() {
		return AxoI18n.translate("players");
	}
}
