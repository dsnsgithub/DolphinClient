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

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.PlatformImplInternal;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

public interface AxoText {
	@AllArgsConstructor
	enum Color {
		BLACK("BLACK", '0', 0, 0),
		DARK_BLUE("DARK_BLUE", '1', 1, 170),
		DARK_GREEN("DARK_GREEN", '2', 2, 43520),
		DARK_AQUA("DARK_AQUA", '3', 3, 43690),
		DARK_RED("DARK_RED", '4', 4, 11141120),
		DARK_PURPLE("DARK_PURPLE", '5', 5, 11141290),
		GOLD("GOLD", '6', 6, 16755200),
		GRAY("GRAY", '7', 7, 11184810),
		DARK_GRAY("DARK_GRAY", '8', 8, 5592405),
		BLUE("BLUE", '9', 9, 5592575),
		GREEN("GREEN", 'a', 10, 5635925),
		AQUA("AQUA", 'b', 11, 5636095),
		RED("RED", 'c', 12, 16733525),
		LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 16733695),
		YELLOW("YELLOW", 'e', 14, 16777045),
		WHITE("WHITE", 'f', 15, 16777215);

		public final String name;
		public final char charCode;
		public final int id;
		public final int rgb;


		@Override
		public String toString() {
			return "§" + charCode;
		}
	}

	interface Style {
		@RequiresImpl
		default Style br$color(Color color) {
			return br$color(color.rgb);
		}

		@RequiresImpl
		default Style br$color(int color) {
			throw BridgeUtil.noImpl();
		}

		@RequiresImpl
		default Style br$tooltip(AxoText text) {
			throw BridgeUtil.noImpl();
		}
	}

	interface Mutable extends AxoText {
		@ApiStatus.NonExtendable
		default Mutable br$color(Color color) {
			return br$setStyle(br$getStyle().br$color(color));
		}

		@ApiStatus.NonExtendable
		default Mutable br$color(int color) {
			return br$setStyle(br$getStyle().br$color(color));
		}

		@ApiStatus.NonExtendable
		default Mutable br$color(io.github.axolotlclient.AxolotlClientConfig.api.util.Color color) {
			return br$color(color.toInt());
		}

		@RequiresImpl
		default Mutable br$append(AxoText child) {
			throw BridgeUtil.noImpl();
		}

		@ApiStatus.NonExtendable
		default Mutable br$append(AxoText child, Color color) {
			return br$append(child.br$copy().br$color(color));
		}

		@ApiStatus.NonExtendable
		default Mutable br$append(Object child) {
			return br$append(AxoText.literal(child));
		}

		@ApiStatus.NonExtendable
		default Mutable br$append(Object child, Color color) {
			return br$append(AxoText.literal(child).br$color(color));
		}

		@RequiresImpl
		default Mutable br$setStyle(Style style) {
			throw BridgeUtil.noImpl();
		}

		@ApiStatus.NonExtendable
		default Mutable br$withStyle(UnaryOperator<Style> op) {
			return br$setStyle(op.apply(br$getStyle()));
		}
	}

	static Mutable empty() {
		return PlatformImplInternal.createEmpty();
	}

	static Mutable literal(String value) {
		return PlatformImplInternal.createLiteral(value);
	}

	static Mutable literal(Object object) {
		return AxoText.literal(String.valueOf(object));
	}

	static Mutable translatable(String key, Object... args) {
		return PlatformImplInternal.createTranslatable(key, args);
	}

	static String strip(String text) {
		return PlatformImplInternal.stripText(text);
	}

	@RequiresImpl
	default String br$getRawString() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Mutable br$copy() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$visit(BiConsumer<String, Style> handler) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Style br$getStyle() {
		throw BridgeUtil.noImpl();
	}
}
