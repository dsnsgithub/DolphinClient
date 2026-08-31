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

/**
 * Helpers for nametag rendering options.
 */
public final class NametagRendering {
	private NametagRendering() {
	}

	/**
	 * Vanilla only draws custom-name nametags when {@code entity} is the
	 * crosshair target. When {@code alwaysRender} is true, substitute the
	 * entity itself so that comparison succeeds without looking at it.
	 */
	public static <T> T lookTarget(T crosshairTarget, T entity, boolean alwaysRender) {
		return alwaysRender ? entity : crosshairTarget;
	}
}
