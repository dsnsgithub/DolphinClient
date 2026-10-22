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

package io.github.dolphinclient.modules.hud.gui.layout;

import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.util.AxoText;
import lombok.AllArgsConstructor;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

@AllArgsConstructor
public enum Justification {

	LEFT, CENTER, RIGHT;

	public int getXOffset(AxoText text, int width) {
		if (this == LEFT) {
			return 0;
		}
		return getXOffset(AxoMinecraftClient.getInstance().br$getFont().br$getWidth(text), width);
	}

	public int getXOffset(int textWidth, int width) {
		if (this == LEFT) {
			return 0;
		}
		if (this == RIGHT) {
			return width - textWidth;
		}
		return (width - textWidth) / 2;
	}

	public int getXOffset(String text, int width) {
		if (this == LEFT) {
			return 0;
		}
		return getXOffset(AxoMinecraftClient.getInstance().br$getFont().br$getWidth(text), width);
	}
}
