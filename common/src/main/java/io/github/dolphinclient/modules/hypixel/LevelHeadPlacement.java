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

package io.github.dolphinclient.modules.hypixel;

import java.util.Arrays;

/**
 * How far above the username the level line needs to sit so it clears server nametags.
 * Hypixel draws extra lines there: the below-name scoreboard row, and holograms stacked over the player.
 */
public final class LevelHeadPlacement {
	/** Scale used by the vanilla nametag renderer ({@code 1.6 * 0.016666668}). */
	public static final double NAMETAG_SCALE = 0.02666667;
	/** Same gap {@code PlayerRenderer} leaves between the name and the scoreboard line. */
	public static final double LINE_FACTOR = 1.15;

	private LevelHeadPlacement() {
	}

	/**
	 * @param fontHeight     vanilla font height, normally 9
	 * @param belowNameLine  the below-name scoreboard objective is currently drawn
	 * @param nameAnchor     world Y of the player's nametag anchor
	 * @param tagsAbove      world Y of other nametag anchors stacked above that one
	 * @param baseAbove      pixels the level is already placed above the name
	 * @return extra pixels to move the level up
	 */
	public static int extraPixels(int fontHeight, boolean belowNameLine, double nameAnchor, double[] tagsAbove, int baseAbove) {
		double line = fontHeight * LINE_FACTOR;
		int count = (belowNameLine ? 1 : 0) + (tagsAbove == null ? 0 : tagsAbove.length);
		if (count == 0) {
			return 0;
		}
		double[] rises = new double[count];
		int n = 0;
		if (belowNameLine) {
			rises[n++] = line * NAMETAG_SCALE;
		}
		if (tagsAbove != null) {
			for (double tag : tagsAbove) {
				double rise = tag - nameAnchor;
				if (rise > 0.001) {
					rises[n++] = rise;
				}
			}
		}
		if (n == 0) {
			return 0;
		}
		double[] stacked = n == rises.length ? rises : Arrays.copyOf(rises, n);
		Arrays.sort(stacked);
		// Only the lines that actually touch the name. A sign floating well above the head is not part of the nametag.
		double maxGap = line * NAMETAG_SCALE * 2.5;
		double top = 0.0;
		double cursor = 0.0;
		for (double rise : stacked) {
			if (rise - cursor > maxGap) {
				break;
			}
			top = rise;
			cursor = rise;
		}
		if (top <= 0.001) {
			return 0;
		}
		double needed = top / NAMETAG_SCALE + line;
		return Math.max(0, (int) Math.ceil(needed - baseAbove));
	}
}
