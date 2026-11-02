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

package io.github.dolphinclient.modules.hypixel.bedwars;

import java.util.List;
import java.util.stream.IntStream;

import io.github.dolphinclient.bridge.util.AxoText;

import static io.github.dolphinclient.bridge.util.AxoText.Color.*;
import static io.github.dolphinclient.bridge.util.AxoText.literal;

public final class BedwarsPrestige {
	private static final List<AxoText.Color> RAINBOW = List.of(RED, GOLD, YELLOW, GREEN, AQUA, LIGHT_PURPLE, DARK_PURPLE);

	public static AxoText format(int level) {
		String levelString = level + "☆";
		return switch (level / 100) {
			case 0 -> literal(levelString).br$color(GRAY);
			case 1 -> literal(levelString).br$color(WHITE);
			case 2 -> literal(levelString).br$color(GOLD);
			case 3 -> literal(levelString).br$color(AQUA);
			case 4 -> literal(levelString).br$color(DARK_GREEN);
			case 5 -> literal(levelString).br$color(DARK_AQUA);
			case 6 -> literal(levelString).br$color(DARK_RED);
			case 7 -> literal(levelString).br$color(LIGHT_PURPLE);
			case 8 -> literal(levelString).br$color(BLUE);
			case 9 -> literal(levelString).br$color(DARK_PURPLE);
			default -> IntStream.range(0, levelString.length())
				.mapToObj(x -> literal(levelString.substring(x, x + 1)).br$color(RAINBOW.get(x % RAINBOW.size())))
				.reduce(literal(""), AxoText.Mutable::br$append);
		};
	}
}
