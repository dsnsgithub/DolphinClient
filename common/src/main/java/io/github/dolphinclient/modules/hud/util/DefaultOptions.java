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

package io.github.dolphinclient.modules.hud.util;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.modules.hud.HudManagerCommon;
import io.github.dolphinclient.modules.hud.gui.component.HudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.gui.layout.CardinalOrder;
import io.github.dolphinclient.util.options.ForceableBooleanOption;
import lombok.experimental.UtilityClass;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

@UtilityClass
public class DefaultOptions {
	public static DoubleOption getX(double defaultX) {
		return new DoubleOption("x", defaultX, 0d, 1d);
	}

	public static DoubleOption getY(double defaultY) {
		return new DoubleOption("y", defaultY, 0d, 1d);
	}

	public static DoubleOption getScale(HudEntry entry) {
		return new DoubleOption("scale", 1d, value -> entry.onBoundsUpdate(), 0d, 2d);
	}

	public static ForceableBooleanOption getEnabled(HudEntry entry) {
		return new ForceableBooleanOption("enabled", false, v -> {
			if (!v) {
				entry.clearBoundsDependencies();
				HudManagerCommon.getInstance().getMoveableEntries().forEach(e -> {
					e.removeBoundsDependencyX(entry);
					e.removeBoundsDependencyY(entry);
				});
			}
		});
	}

	public static EnumOption<AnchorPoint> getAnchorPoint(HudEntry entry) {
		return getAnchorPoint(AnchorPoint.TOP_LEFT, entry);
	}

	public static EnumOption<AnchorPoint> getAnchorPoint(AnchorPoint defaultValue, HudEntry entry) {
		return new EnumOption<>("anchorpoint", AnchorPoint.class, defaultValue, value -> {
			var storedX = entry.getRawTrueX();
			var storedY = entry.getRawTrueY();
			var offsetX = entry.offsetTrueWidth();
			var offsetY = entry.offsetTrueHeight();
			entry.setX(storedX + offsetX);
			entry.setY(storedY + offsetY);
		});
	}

	public static EnumOption<CardinalOrder> getCardinalOrder(CardinalOrder defaultValue) {
		return new EnumOption<>("cardinalorder", CardinalOrder.class, defaultValue);
	}

	public static BooleanOption getHideIfEmpty() {
		return new BooleanOption("hud.hide_if_empty", true);
	}
}
