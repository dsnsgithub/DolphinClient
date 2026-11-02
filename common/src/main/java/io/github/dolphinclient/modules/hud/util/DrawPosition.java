/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

@Data
@Accessors(fluent = true)
public class DrawPosition {

	public int x, y;

	public DrawPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public DrawPosition subtract(int x, int y) {
		return new DrawPosition(this.x - x, this.y - y);
	}

	public DrawPosition subtract(DrawPosition position) {
		return new DrawPosition(position.x, position.y);
	}

	public DrawPosition divide(float scale) {
		return new DrawPosition(Math.round(x / scale), Math.round(y / scale));
	}
}
