/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.util;

public class CursorType {
	public static final CursorType DEFAULT = new CursorType("default", 0L);
	private final String name;
	private final long handle;

	private CursorType(final String name, final long handle) {
		this.name = name;
		this.handle = handle;
	}

	private static CursorType currentCursor = DEFAULT;

	public synchronized void select() {
		if (this != currentCursor) {
			currentCursor = this;
			WindowAccess.getInstance().setCursor(this.handle);
		}
	}

	public String toString() {
		return this.name;
	}

	static CursorType createStandardCursor(final WindowAccess.Cursor shape, final String name, final CursorType fallback) {
		long handle = WindowAccess.getInstance().createCursor(shape);
		return handle == 0L ? fallback : new CursorType(name, handle);
	}
}
