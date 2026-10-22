/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.bridge.util;

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.PlatformImplInternal;
import io.github.dolphinclient.bridge.internal.RequiresImpl;

public interface AxoIdentifier {
	static AxoIdentifier of(String ns, String path) {
		return PlatformImplInternal.createIdentifier(ns, path);
	}

	static AxoIdentifier of(String path) {
		return of("minecraft", path);
	}

	static AxoIdentifier parse(String id) {
		return PlatformImplInternal.parseIdentifier(id);
	}

	@RequiresImpl
	default String br$getPath() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default String br$getNamespace() {
		throw BridgeUtil.noImpl();
	}

	default String br$getAsFriendlyString() {
		String path = br$getPath();
		String namespace = br$getNamespace();
		if (!namespace.equals("minecraft")) {
			path += " (" + Character.toTitleCase(namespace.charAt(0)) + namespace.substring(1) + ")";
		}
		final String str = path.replace("_", " ");
		if (str.isEmpty()) {
			return str;
		}

		final int[] codepoints = str.codePoints().toArray();
		boolean capitalizeNext = true;
		for (int i = 0; i < codepoints.length; i++) {
			final int ch = codepoints[i];
			if (Character.isWhitespace(ch)) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				codepoints[i] = Character.toTitleCase(ch);
				capitalizeNext = false;
			}
		}
		return new String(codepoints, 0, codepoints.length);
	}
}
