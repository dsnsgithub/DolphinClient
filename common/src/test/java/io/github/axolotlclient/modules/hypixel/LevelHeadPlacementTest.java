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

package io.github.axolotlclient.modules.hypixel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelHeadPlacementTest {
	private static final int FONT = 9;
	private static final int BASE = 10;
	private static final double NAME = 64.0;

	@Test
	void staysPutWhenNothingIsAboveTheName() {
		assertEquals(0, LevelHeadPlacement.extraPixels(FONT, false, NAME, null, BASE));
		assertEquals(0, LevelHeadPlacement.extraPixels(FONT, false, NAME, new double[]{NAME - 0.3}, BASE));
	}

	@Test
	void clearsTheBelowNameScoreboardLine() {
		int extra = LevelHeadPlacement.extraPixels(FONT, true, NAME, null, BASE);
		double line = FONT * LevelHeadPlacement.LINE_FACTOR;
		assertTrue(BASE + extra >= line * 2 - 0.01, "level should sit a full line above the scoreboard tag");
	}

	@Test
	void clearsHologramsStackedAboveTheName() {
		double lineBlocks = FONT * LevelHeadPlacement.LINE_FACTOR * LevelHeadPlacement.NAMETAG_SCALE;
		double[] tags = {NAME + lineBlocks * 2, NAME + lineBlocks};
		int extra = LevelHeadPlacement.extraPixels(FONT, false, NAME, tags, BASE);
		double highestPixels = (lineBlocks * 2) / LevelHeadPlacement.NAMETAG_SCALE;
		assertTrue(BASE + extra >= highestPixels + FONT * LevelHeadPlacement.LINE_FACTOR - 0.01);
	}

	@Test
	void ignoresASignThatIsNotPartOfTheNametag() {
		assertEquals(0, LevelHeadPlacement.extraPixels(FONT, false, NAME, new double[]{NAME + 2.0}, BASE));
	}
}
