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

package io.github.dolphinclient.util;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientColors {
	public static Color WHITE = Colors.WHITE;
	public static Color BLACK = Colors.BLACK;
	public static Color GRAY = Colors.GRAY;
	public static Color DARK_GRAY = Colors.DARK_GRAY;
	public static Color SELECTOR_RED = new Color(191, 34, 34).immutable();
	public static Color GOLD = Color.parse("#b8860b").immutable();
	public static Color SELECTOR_GREEN = new Color(53, 219, 103).immutable();
	public static Color SELECTOR_BLUE = new Color(51, 153, 255, 255).immutable();
	public static Color ERROR = new Color(255, 0, 255).immutable();

	/**
	 * Blends two {@link Color}s based off of a percentage.
	 *
	 * @param original   color to start the blend with
	 * @param blend      color that when fully blended, will be this
	 * @param percentage the percentage to blend
	 * @return the simple color
	 */
	public static Color blend(Color original, Color blend, float percentage) {
		if (percentage >= 1) {
			return blend;
		}
		if (percentage <= 0) {
			return original;
		}
		int red = blendInt(original.getRed(), blend.getRed(), percentage);
		int green = blendInt(original.getGreen(), blend.getGreen(), percentage);
		int blue = blendInt(original.getBlue(), blend.getBlue(), percentage);
		int alpha = blendInt(original.getAlpha(), blend.getAlpha(), percentage);
		return new Color(red, green, blue, alpha);
	}

	/**
	 * Blends two ints together based off of a percent.
	 *
	 * @param start   starting int
	 * @param end     end int
	 * @param percent percent to blend
	 * @return the blended int
	 */
	public static int blendInt(int start, int end, float percent) {
		if (percent <= 0) {
			return start;
		}
		if (start == end || percent >= 1) {
			return end;
		}
		int dif = end - start;
		int add = Math.round((float) dif * percent);
		return start + (add);
	}

	@SuppressWarnings("unused")
	public class ARGB {

		public static int alpha(int color) {
			return color >>> 24;
		}

		public static int red(int color) {
			return color >> 16 & 0xFF;
		}

		public static int green(int color) {
			return color >> 8 & 0xFF;
		}

		public static int blue(int color) {
			return color & 0xFF;
		}

		public static int color(int alpha, int red, int green, int blue) {
			return alpha << 24 | red << 16 | green << 8 | blue;
		}

		public static int color(int red, int green, int blue) {
			return color(255, red, green, blue);
		}

		public static int multiply(int color1, int color2) {
			if (color1 == -1) {
				return color2;
			} else {
				return color2 == -1
					? color1
					: color(alpha(color1) * alpha(color2) / 255, red(color1) * red(color2) / 255, green(color1) * green(color2) / 255, blue(color1) * blue(color2) / 255);
			}
		}

		public static int scaleRGB(int color, float scale) {
			return scaleRGB(color, scale, scale, scale);
		}

		public static int scaleRGB(int color, float redScale, float greenScale, float blueScale) {
			return color(
				alpha(color),
				MathUtil.clamp((int) (red(color) * redScale), 0, 255),
				MathUtil.clamp((int) (green(color) * greenScale), 0, 255),
				MathUtil.clamp((int) (blue(color) * blueScale), 0, 255)
			);
		}

		public static int scaleRGB(int color, int scale) {
			return color(
				alpha(color),
				MathUtil.clamp(red(color) * scale / 255, 0, 255),
				MathUtil.clamp(green(color) * scale / 255, 0, 255),
				MathUtil.clamp(blue(color) * scale / 255, 0, 255)
			);
		}

		public static int greyscale(int color) {
			int i = (int) (red(color) * 0.3F + green(color) * 0.59F + blue(color) * 0.11F);
			return color(i, i, i);
		}

		public static int lerp(float delta, int color1, int color2) {
			int i = MathUtil.lerp(delta, alpha(color1), alpha(color2));
			int j = MathUtil.lerp(delta, red(color1), red(color2));
			int k = MathUtil.lerp(delta, green(color1), green(color2));
			int l = MathUtil.lerp(delta, blue(color1), blue(color2));
			return color(i, j, k, l);
		}

		public static int opaque(int color) {
			return color | 0xFF000000;
		}

		public static int transparent(int color) {
			return color & 16777215;
		}

		public static int color(int alpha, int color) {
			return alpha << 24 | color & 16777215;
		}

		public static int color(float alpha, int color) {
			return as8BitChannel(alpha) << 24 | color & 16777215;
		}

		public static int white(float alpha) {
			return as8BitChannel(alpha) << 24 | 16777215;
		}

		public static int colorFromFloat(float alpha, float red, float green, float blue) {
			return color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
		}

		public static int average(int color1, int color2) {
			return color((alpha(color1) + alpha(color2)) / 2, (red(color1) + red(color2)) / 2, (green(color1) + green(color2)) / 2, (blue(color1) + blue(color2)) / 2);
		}

		public static int as8BitChannel(float value) {
			return MathUtil.floor(value * 255.0F);
		}

		public static float alphaFloat(int color) {
			return from8BitChannel(alpha(color));
		}

		public static float redFloat(int color) {
			return from8BitChannel(red(color));
		}

		public static float greenFloat(int color) {
			return from8BitChannel(green(color));
		}

		public static float blueFloat(int color) {
			return from8BitChannel(blue(color));
		}

		private static float from8BitChannel(int value) {
			return value / 255.0F;
		}

		public static int invertAlpha(int color) {
			return color(255 - alpha(color), color);
		}

		public static int toABGR(final int color) {
			return color & -16711936 | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;
		}

		public static int fromABGR(final int color) {
			return toABGR(color);
		}
	}
}
