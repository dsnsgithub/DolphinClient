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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NametagRenderingTest {

	private static final Object PLAYER = new Object();
	private static final Object MOB = new Object();

	@Test
	void usesCrosshairTargetWhenDisabled() {
		assertSame(PLAYER, NametagRendering.lookTarget(PLAYER, MOB, false));
	}

	@Test
	void treatsEntityAsLookedAtWhenEnabled() {
		assertSame(MOB, NametagRendering.lookTarget(PLAYER, MOB, true));
	}

	@Test
	void keepsMatchWhenAlreadyLookingAtEntity() {
		assertSame(MOB, NametagRendering.lookTarget(MOB, MOB, false));
		assertSame(MOB, NametagRendering.lookTarget(MOB, MOB, true));
	}

	@Test
	void langDefinesAlwaysRenderNametagsOption() throws Exception {
		try (InputStream in = NametagRendering.class.getResourceAsStream("/assets/dolphinclient/lang/en_us.json")) {
			assertNotNull(in, "en_us.json should be on the classpath");
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			assertTrue(json.contains("\"alwaysRenderNametags\": \"Always Render Name Tags\""));
			assertTrue(json.contains("\"alwaysRenderNametags.tooltip\""));
			assertTrue(json.contains("looking"));
		}
	}
}
