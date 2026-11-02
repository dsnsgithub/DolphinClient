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

package io.github.dolphinclient.util;

public class MathUtil {
	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static long clamp(long value, long max, long min) {
		return Math.min(Math.max(value, max), min);
	}

	public static float clamp(float value, float min, float max) {
		return value < min ? min : Math.min(value, max);
	}

	public static double clamp(double value, double min, double max) {
		return value < min ? min : Math.min(value, max);
	}

	public static float lerp(float delta, float start, float end) {
		return start + delta * (end - start);
	}

	public static int lerp(float delta, int start, int end) {
		return start + floor(delta * (end - start));
	}

	public static int floor(float value) {
		int i = (int) value;
		return value < i ? i - 1 : i;
	}

	public static float distSq(float x1, float y1, float x2, float y2) {
		var dx = x1 - x2;
		var dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	public static double distSq(double x1, double y1, double x2, double y2) {
		var dx = x1 - x2;
		var dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	public static double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
	}

	public static int hsvToRgb(float hue, float saturation, float value) {
		int i = (int) (hue * 6.0F) % 6;
		float f = hue * 6.0F - (float) i;
		float g = value * (1.0F - saturation);
		float h = value * (1.0F - f * saturation);
		float j = value * (1.0F - (1.0F - f) * saturation);
		float k;
		float l;
		float m;
		switch (i) {
			case 0:
				k = value;
				l = j;
				m = g;
				break;
			case 1:
				k = h;
				l = value;
				m = g;
				break;
			case 2:
				k = g;
				l = value;
				m = j;
				break;
			case 3:
				k = g;
				l = h;
				m = value;
				break;
			case 4:
				k = j;
				l = g;
				m = value;
				break;
			case 5:
				k = value;
				l = g;
				m = h;
				break;
			default:
				throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
		}

		int n = clamp((int) (k * 255.0F), 0, 255);
		int o = clamp((int) (l * 255.0F), 0, 255);
		int p = clamp((int) (m * 255.0F), 0, 255);
		return n << 16 | o << 8 | p;
	}
}
