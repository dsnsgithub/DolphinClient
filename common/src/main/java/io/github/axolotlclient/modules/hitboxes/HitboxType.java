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

import lombok.Getter;

/**
 * Entity groups the Hitboxes module can toggle and color independently.
 * Classification is first-match in the order below so more specific groups
 * (self, arrows) win over broader ones (players, projectiles, living).
 */
@Getter
public enum HitboxType {
	SELF("hitboxes.self", 0xFF00FFFF),
	PLAYERS("hitboxes.players", 0xFFFFFFFF),
	ARROWS("hitboxes.arrows", 0xFFFF5555),
	PROJECTILES("hitboxes.projectiles", 0xFFFFAA00),
	ITEMS("hitboxes.items", 0xFFFFFF55),
	HOSTILE("hitboxes.hostile", 0xFFAA0000),
	PASSIVE("hitboxes.passive", 0xFF55FF55),
	OTHER("hitboxes.other", 0xFFAAAAAA);

	private final String translationKey;
	private final int defaultColor;

	HitboxType(String translationKey, int defaultColor) {
		this.translationKey = translationKey;
		this.defaultColor = defaultColor;
	}

	public static HitboxType classify(
		boolean self,
		boolean player,
		boolean arrow,
		boolean projectile,
		boolean item,
		boolean hostile,
		boolean living
	) {
		if (self) {
			return SELF;
		}
		if (player) {
			return PLAYERS;
		}
		if (arrow) {
			return ARROWS;
		}
		if (projectile) {
			return PROJECTILES;
		}
		if (item) {
			return ITEMS;
		}
		if (hostile) {
			return HOSTILE;
		}
		if (living) {
			return PASSIVE;
		}
		return OTHER;
	}
}
