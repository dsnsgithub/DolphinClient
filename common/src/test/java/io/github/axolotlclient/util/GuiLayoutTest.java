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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiLayoutTest {

	private static final int FONT_HEIGHT = 9;
	private static final int BUTTON_HEIGHT = 20;
	private static final int PADDING = 8;

	private static int vanillaConnectTextY(int scaledHeight) {
		return scaledHeight / 2 - 50;
	}

	private static int vanillaConnectButtonY(int scaledHeight) {
		return scaledHeight / 4 + 120 + 12;
	}

	private static int adjustedButtonY(int scaledHeight) {
		return GuiLayout.moveBelowIfOverlapping(
			vanillaConnectButtonY(scaledHeight),
			BUTTON_HEIGHT,
			vanillaConnectTextY(scaledHeight),
			FONT_HEIGHT,
			PADDING
		);
	}

	@Test
	void keepsVanillaLayoutWhenThereIsAlreadyAGap() {
		int height = 240;
		assertEquals(vanillaConnectButtonY(height), adjustedButtonY(height));
	}

	@Test
	void keepsVanillaLayoutAt1080pScale2() {
		int height = 540;
		assertEquals(vanillaConnectButtonY(height), adjustedButtonY(height));
	}

	@Test
	void movesCancelBelowStatusAt1440pScale2() {
		int height = 720;
		int textY = vanillaConnectTextY(height);
		int vanillaButtonY = vanillaConnectButtonY(height);
		int adjusted = adjustedButtonY(height);

		assertEquals(310, textY);
		assertEquals(312, vanillaButtonY);
		assertEquals(textY + FONT_HEIGHT + PADDING, adjusted);
		assertEquals(327, adjusted);
	}

	@Test
	void doesNotMoveButtonWhenItSitsFullyAboveTheText() {
		int textY = 490;
		int buttonY = 267;
		assertEquals(buttonY, GuiLayout.moveBelowIfOverlapping(buttonY, BUTTON_HEIGHT, textY, FONT_HEIGHT, PADDING));
	}
}
