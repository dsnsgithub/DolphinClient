/*
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

package io.github.axolotlclient.modules.hud.gui.hud;

/**
 * Vanilla 1.8.9 chat is not a percentage of the screen. {@code GameGui} translates
 * by {@code (0, scaledHeight - 48)} and {@code ChatGui} then translates by
 * {@code (2, 20)} before applying the chat-scale option. The anchor is therefore
 * a fixed GUI-pixel inset, which changes as a fraction of the screen whenever
 * the GUI scale or resolution changes.
 */
public final class ChatPosition {

	/** {@code GameGui} shift that lifts chat off the bottom of the scaled screen. */
	public static final int SCREEN_SHIFT = 48;
	/** Extra shift applied by {@code ChatGui} after the screen shift. */
	public static final int CHAT_SHIFT = 20;
	/** Left inset applied by {@code ChatGui}, in GUI-scaled pixels, outside chat scale. */
	public static final int LEFT_MARGIN = 2;

	private ChatPosition() {
	}

	/**
	 * Custom dragging is opt-in. The module can stay enabled for background,
	 * transparency, spacing, and the other chat options without moving chat.
	 */
	public static boolean usesCustomPosition(boolean moduleEnabled, boolean hudsEnabled, boolean positionToggle) {
		return moduleEnabled && hudsEnabled && positionToggle;
	}

	public static int vanillaAnchorX() {
		return LEFT_MARGIN;
	}

	public static int vanillaAnchorY(int scaledHeight) {
		return scaledHeight - SCREEN_SHIFT + CHAT_SHIFT;
	}

	/** Bottom of line {@code index}. Index 0 stays on the anchor for every line height. */
	public static int lineBottom(int anchorBottom, int index, int lineHeight) {
		return anchorBottom - index * lineHeight;
	}

	/**
	 * Matches {@code Window}'s forced or automatic GUI scale, including the
	 * unicode rule that drops an odd scale above 1.
	 */
	public static int guiScale(int framebufferWidth, int framebufferHeight, int requestedScale, boolean unicode) {
		int scale = 1;
		int max = requestedScale == 0 ? 1000 : requestedScale;
		while (scale < max
			&& framebufferWidth / (scale + 1) >= 320
			&& framebufferHeight / (scale + 1) >= 240) {
			scale++;
		}
		if (unicode && scale % 2 != 0 && scale != 1) {
			scale--;
		}
		return scale;
	}

	/** {@code ceil(framebufferPixels / guiScale)}, which is what {@code Window} stores. */
	public static int scaledLength(int framebufferPixels, int guiScale) {
		return (int) Math.ceil(framebufferPixels / (double) guiScale);
	}
}
