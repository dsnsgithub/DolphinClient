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

package io.github.axolotlclient.modules.hitboxes;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HitboxTypeTest {

	@Test
	void classifiesInSpecificToBroadOrder() {
		assertEquals(HitboxType.SELF, HitboxType.classify(true, true, false, false, false, false, false));
		assertEquals(HitboxType.PLAYERS, HitboxType.classify(false, true, false, false, false, false, true));
		assertEquals(HitboxType.ARROWS, HitboxType.classify(false, false, true, true, false, false, false));
		assertEquals(HitboxType.PROJECTILES, HitboxType.classify(false, false, false, true, false, false, false));
		assertEquals(HitboxType.ITEMS, HitboxType.classify(false, false, false, false, true, false, false));
		assertEquals(HitboxType.HOSTILE, HitboxType.classify(false, false, false, false, false, true, true));
		assertEquals(HitboxType.PASSIVE, HitboxType.classify(false, false, false, false, false, false, true));
		assertEquals(HitboxType.OTHER, HitboxType.classify(false, false, false, false, false, false, false));
	}

	@Test
	void keepsTranslationKeysAndDefaultColorsUnique() {
		Set<String> keys = new HashSet<>();
		Set<Integer> colors = new HashSet<>();
		for (HitboxType type : HitboxType.values()) {
			assertTrue(type.getTranslationKey().startsWith("hitboxes."));
			assertTrue(keys.add(type.getTranslationKey()), type.name());
			assertNotEquals(0, type.getDefaultColor() >>> 24, type.name());
			assertTrue(colors.add(type.getDefaultColor()), type.name());
		}
		assertEquals(HitboxType.values().length, keys.size());
		assertEquals(HitboxType.values().length, colors.size());
	}

	@Test
	void langFileContainsHitboxEntries() throws Exception {
		try (var in = HitboxType.class.getClassLoader()
			.getResourceAsStream("assets/dolphinclient/lang/en_us.json")) {
			assertTrue(in != null, "en_us.json");
			String json = new String(in.readAllBytes());
			assertTrue(json.contains("\"hitboxes\""));
			assertTrue(json.contains("\"hitboxes.show\""));
			assertTrue(json.contains("\"toggle_hitboxes\""));
			for (HitboxType type : HitboxType.values()) {
				assertTrue(json.contains("\"" + type.getTranslationKey() + "\""), type.getTranslationKey());
			}
		}
	}
}
