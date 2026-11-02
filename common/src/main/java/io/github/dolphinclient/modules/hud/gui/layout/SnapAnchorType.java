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

package io.github.dolphinclient.modules.hud.gui.layout;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.dolphinclient.modules.hud.gui.component.HudEntry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SnapAnchorType {
	X_X,
	X_XEND,
	XEND_X,
	XEND_XEND,
	Y_Y,
	Y_YEND,
	YEND_Y,
	YEND_YEND;
	private static final Map<String, SnapAnchorType> byName = Arrays.stream(values()).collect(Collectors.toMap(SnapAnchorType::getName, Function.identity()));

	public static SnapAnchorType fromName(String name) {
		return byName.get(name);
	}

	public String getName() {
		return toString().toLowerCase(Locale.ROOT);
	}

	private int getNewXPos(HudEntry dependency, HudEntry dependent) {
		var bounds = dependency.getTrueBounds();
		var depBounds = dependent.getTrueBounds();
		return switch (this) {
			case X_X -> bounds.x();
			case X_XEND -> bounds.xEnd();
			case XEND_X -> bounds.x() - depBounds.width();
			case XEND_XEND -> bounds.xEnd() - depBounds.width();
			default -> dependent.getX();
		};
	}

	private int getNewYPos(HudEntry dependency, HudEntry dependent) {
		var bounds = dependency.getTrueBounds();
		var depBounds = dependent.getTrueBounds();
		return switch (this) {
			case Y_Y -> bounds.y();
			case Y_YEND -> bounds.yEnd();
			case YEND_Y -> bounds.y() - depBounds.height();
			case YEND_YEND -> bounds.yEnd() - depBounds.height();
			default -> dependent.getY();
		};
	}

	public void updatePosX(HudEntry dependency, HudEntry dependent) {
		dependent.setTrueX((int) ((getNewXPos(dependency, dependent) + dependent.offsetTrueWidth()) * dependent.getScale()));
	}

	public void updatePosY(HudEntry dependency, HudEntry dependent) {
		dependent.setTrueY((int) ((getNewYPos(dependency, dependent) + dependent.offsetTrueHeight()) * dependent.getScale()));
	}
}
