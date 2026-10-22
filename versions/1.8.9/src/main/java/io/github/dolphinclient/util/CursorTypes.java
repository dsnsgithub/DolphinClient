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

@SuppressWarnings("unused")
public final class CursorTypes {
	public static final CursorType ARROW = CursorType.createStandardCursor(WindowAccess.Cursor.ARROW, "arrow", CursorType.DEFAULT);
	public static final CursorType IBEAM = CursorType.createStandardCursor(WindowAccess.Cursor.IBEAM, "ibeam", CursorType.DEFAULT);
	public static final CursorType CROSSHAIR = CursorType.createStandardCursor(WindowAccess.Cursor.CROSSHAIR, "crosshair", CursorType.DEFAULT);
	public static final CursorType POINTING_HAND = CursorType.createStandardCursor(WindowAccess.Cursor.POINTING_HAND, "pointing_hand", CursorType.DEFAULT);
	public static final CursorType RESIZE_NS = CursorType.createStandardCursor(WindowAccess.Cursor.RESIZE_NS, "resize_ns", CursorType.DEFAULT);
	public static final CursorType RESIZE_EW = CursorType.createStandardCursor(WindowAccess.Cursor.RESIZE_EW, "resize_ew", CursorType.DEFAULT);
	public static final CursorType RESIZE_ALL = CursorType.createStandardCursor(WindowAccess.Cursor.RESIZE_ALL, "resize_all", CursorType.DEFAULT);
	public static final CursorType NOT_ALLOWED = CursorType.createStandardCursor(WindowAccess.Cursor.NOT_ALLOWED, "not_allowed", CursorType.DEFAULT);
	public static final CursorType RESIZE_NWSE = CursorType.createStandardCursor(WindowAccess.Cursor.RESIZE_NWSE, "resize_nwse", POINTING_HAND);
	public static final CursorType RESIZE_NESW = CursorType.createStandardCursor(WindowAccess.Cursor.RESIZE_NESW, "resize_nesw", POINTING_HAND);
}
