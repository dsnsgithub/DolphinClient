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

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HypixelLevelClientTest {
	private static final String UUID = "557bafa1-0aad-40bb-b672-07a9cefa8220";
	private static final String UNDASHED = "557bafa10aad40bbb67207a9cefa8220";

	@Test
	void userAgentContainsDolphinClientAndVersion() {
		assertEquals("DolphinClient 1.0.2", HypixelLevelClient.userAgent("1.0.2"));
		assertTrue(HypixelLevelClient.userAgent("1.0.2").contains("DolphinClient 1.0.2"));
	}

	@Test
	void levelUrlUsesDashedUuid() {
		String expected = "https://hypixel.dsns.dev/level/" + UUID;
		assertEquals(expected, HypixelLevelClient.levelUrl(UUID));
		assertEquals(expected, HypixelLevelClient.levelUrl(UNDASHED));
	}

	@Test
	void parsesPlainLevelAndRejectsNonLevels() {
		assertEquals(251.84895433070866, HypixelLevelClient.parseLevel(200, "251.84895433070866\n").orElseThrow(), 1.0e-9);
		assertEquals(Optional.of(0.0), HypixelLevelClient.parseLevel(200, "0"));
		assertEquals(Optional.empty(), HypixelLevelClient.parseLevel(200, "{\"success\":true,\"player\":null}"));
		assertEquals(Optional.empty(), HypixelLevelClient.parseLevel(422, "{\"success\":false,\"cause\":\"Malformed UUID\"}"));
		assertEquals(Optional.empty(), HypixelLevelClient.parseLevel(200, "<!DOCTYPE html>"));
		assertEquals(Optional.empty(), HypixelLevelClient.parseLevel(403, "251"));
		assertEquals(Optional.empty(), HypixelLevelClient.parseLevel(200, "-1"));
	}

	@Test
	void formatsLevelForNametag() {
		assertEquals("251.85", HypixelLevelClient.formatLevel(251.84895433070866));
		assertEquals("12", HypixelLevelClient.formatLevel(12.0));
		assertEquals("0", HypixelLevelClient.formatLevel(0));
		assertEquals("???", HypixelLevelClient.formatLevel(Double.NaN));
	}

	@Test
	void fetchesExamplePlayerLevel() {
		Optional<Double> level = HypixelLevelClient.requestLevel(UUID, HypixelLevelClient.userAgent("1.0.2"));
		assertTrue(level.isPresent(), "level endpoint returned no number");
		assertTrue(level.get() >= 0);
		assertNotEquals("???", HypixelLevelClient.formatLevel(level.get()));
	}
}
