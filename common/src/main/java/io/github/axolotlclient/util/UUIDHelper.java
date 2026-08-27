/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
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

public class UUIDHelper {
	public static UUID fromUndashed(String uuid) {
		return UUID.fromString(uuid.trim().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	public static String toUndashed(UUID uuid) {
		return sanitizeUUID(uuid.toString());
	}

	public static String sanitizeUUID(String uuid) {
		if (uuid.contains("-")) {
			return validateUUID(uuid.replace("-", ""));
		}
		return validateUUID(uuid);
	}

	private static String validateUUID(String uuid) {
		if (uuid.length() != 32) {
			throw new IllegalArgumentException("Not a valid UUID (undashed): " + uuid);
		}
		return uuid;
	}
}
