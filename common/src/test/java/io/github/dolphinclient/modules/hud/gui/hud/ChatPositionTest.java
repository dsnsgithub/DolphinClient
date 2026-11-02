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

package io.github.dolphinclient.modules.hud.gui.hud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatPositionTest {

	@Test
	void customPositionRequiresTheToggle() {
		assertFalse(ChatPosition.usesCustomPosition(true, true, false));
		assertFalse(ChatPosition.usesCustomPosition(true, false, true));
		assertFalse(ChatPosition.usesCustomPosition(false, true, true));
		assertTrue(ChatPosition.usesCustomPosition(true, true, true));
	}

	@Test
	void vanillaAnchorIsAFixedInsetNotAScreenFraction() {
		int[][] resolutions = {
			{854, 480},
			{1280, 720},
			{1366, 768},
			{1600, 900},
			{1920, 1080},
			{2560, 1440},
			{3840, 2160}
		};
		int[] scales = {1, 2, 3, 4};

		for (int[] resolution : resolutions) {
			for (int scale : scales) {
				int scaledHeight = ChatPosition.scaledLength(resolution[1], scale);
				int anchorY = ChatPosition.vanillaAnchorY(scaledHeight);
				assertEquals(2, ChatPosition.vanillaAnchorX());
				assertEquals(28, scaledHeight - anchorY, resolution[0] + "x" + resolution[1] + " @" + scale);
			}
		}
	}

	@Test
	void savedPercentMissesTheAnchorWhenTheGuiScaleChanges() {
		int height = 1080;
		int scaledAt2 = ChatPosition.scaledLength(height, 2);
		int scaledAt4 = ChatPosition.scaledLength(height, 4);
		int anchorAt2 = ChatPosition.vanillaAnchorY(scaledAt2);
		int anchorAt4 = ChatPosition.vanillaAnchorY(scaledAt4);
		float percentFittedToScale2 = anchorAt2 / (float) scaledAt2;
		int predictedAt4 = Math.round(percentFittedToScale2 * scaledAt4);

		assertNotEquals(anchorAt4, predictedAt4);
		assertEquals(512, anchorAt2);
		assertEquals(242, anchorAt4);
		assertEquals(256, predictedAt4);
	}

	@Test
	void automaticGuiScaleStillKeepsThe28PixelInset() {
		int width = 1920;
		int height = 1080;
		int scale = ChatPosition.guiScale(width, height, 0, false);
		int scaledHeight = ChatPosition.scaledLength(height, scale);

		assertEquals(4, scale);
		assertEquals(270, scaledHeight);
		assertEquals(28, scaledHeight - ChatPosition.vanillaAnchorY(scaledHeight));
	}

	@Test
	void unicodeDropsOddAutomaticScalesAboveOne() {
		assertEquals(3, ChatPosition.guiScale(1280, 720, 0, false));
		assertEquals(2, ChatPosition.guiScale(1280, 720, 0, true));
		assertEquals(4, ChatPosition.guiScale(1920, 1080, 0, true));
		assertEquals(1, ChatPosition.guiScale(400, 300, 0, true));
	}

	@Test
	void lineSpacingDoesNotMoveTheBottomLine() {
		assertEquals(0, ChatPosition.lineBottom(0, 0, 9));
		assertEquals(0, ChatPosition.lineBottom(0, 0, 10));
		assertEquals(-9, ChatPosition.lineBottom(0, 1, 9));
		assertEquals(-20, ChatPosition.lineBottom(0, 2, 10));
	}
}
