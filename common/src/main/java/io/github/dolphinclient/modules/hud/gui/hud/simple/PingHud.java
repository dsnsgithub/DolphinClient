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

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.dolphinclient.bridge.PlatformDispatch;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.dolphinclient.util.ThreadExecuter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

// TODO: figure out how to implement this logic without exposing everything to bridge
public class PingHud extends SimpleTextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "pinghud");
	private final IntegerOption refreshDelay = new IntegerOption("refreshTime", 4, 1, 15);
	private final BooleanOption hideInSingleplayer = new BooleanOption("pinghud.hide_in_singleplayer", true);
	private final MutableInt currentServerPing = new MutableInt();
	private int second;

	public PingHud() {
		super(true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (second >= refreshDelay.get() * 20) {
			second = 0;
			ThreadExecuter.scheduleTask(() -> PlatformDispatch.pingHud$updatePing(currentServerPing));
		} else {
			second++;
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(refreshDelay);
		return options;
	}

	@Override
	public String getValue() {
		return String.valueOf(currentServerPing.getValue());
	}

	@Override
	public String getPlaceholderValue() {
		return "68";
	}

	@Override
	public String getLabel() {
		return "ms";
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (hideInSingleplayer.get() && client.br$isLocalServer()) {
			return;
		}
		super.render(ctx, delta);
	}
}
