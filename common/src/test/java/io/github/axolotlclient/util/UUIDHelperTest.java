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

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UUIDHelperTest {

	private static final String DASHED = "069a79f4-44e9-4726-a5be-fca90e38aaf5";
	private static final String UNDASHED = "069a79f444e94726a5befca90e38aaf5";

	@Test
	void sanitizesDashedAndUndashedUuids() {
		assertEquals(UNDASHED, UUIDHelper.sanitizeUUID(DASHED));
		assertEquals(UNDASHED, UUIDHelper.sanitizeUUID(UNDASHED));
	}

	@Test
	void roundTripsUndashedUuids() {
		UUID uuid = UUID.fromString(DASHED);
		assertEquals(UNDASHED, UUIDHelper.toUndashed(uuid));
		assertEquals(uuid, UUIDHelper.fromUndashed(UNDASHED));
	}

	@Test
	void rejectsInvalidUuids() {
		assertThrows(IllegalArgumentException.class, () -> UUIDHelper.sanitizeUUID("not-a-uuid"));
	}
}
