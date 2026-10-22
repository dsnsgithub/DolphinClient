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

import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class TPSHud extends SimpleTextHudEntry {

	public final static AxoIdentifier ID = AxoIdentifier.of("kronhud", "tpshud");
	private final static NumberFormat FORMATTER = new DecimalFormat("#0.00");
	private long lastTick = -1;
	private long lastUpdate = -1;
	private double tps = -1;

	public TPSHud() {
		super(true);
	}

	@Override
	public void init() {
		Events.UPDATE_TIME.register(ticks -> {
			if (lastTick < 0) {
				lastTick = ticks;
				lastUpdate = System.nanoTime();
				return;
			}

			long time = System.nanoTime();
			// In nanoseconds, so 1000000000 in a second
			// Or 1000000 in a millisecond
			double elapsedMilli = (time - lastUpdate) / 1000000d;
			int passedTicks = (int) (ticks - lastTick);
			if (passedTicks > 0) {
				double mspt = elapsedMilli / passedTicks;

				tps = Math.min(1000 / mspt, 20);
			}

			lastTick = ticks;
			lastUpdate = time;
		});
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public String getValue() {
		if (tps < 0) {
			return "NaN";
		}
		return FORMATTER.format(tps);
	}

	@Override
	public String getPlaceholderValue() {
		return "20.00";
	}

	@Override
	public String getLabel() {
		return "TPS";
	}
}
