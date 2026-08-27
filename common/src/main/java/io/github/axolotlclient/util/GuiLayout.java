/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.axolotlclient.util;

/**
 * Helpers for vanilla screen layouts that use independent Y formulas for
 * labels and buttons. Those formulas can collide at some scaled resolutions
 * (notably 1440p at GUI scale 2).
 */
public final class GuiLayout {

	private GuiLayout() {
	}

	/**
	 * If {@code y}..{@code y+height} would overlap {@code otherY}..{@code otherY+otherHeight}
	 * (including {@code padding} px of required gap), move {@code y} to sit below the other
	 * rect. Otherwise return {@code y} unchanged.
	 */
	public static int moveBelowIfOverlapping(int y, int height, int otherY, int otherHeight, int padding) {
		int otherBottom = otherY + otherHeight;
		if (y < otherBottom + padding && y + height > otherY) {
			return otherBottom + padding;
		}
		return y;
	}
}
