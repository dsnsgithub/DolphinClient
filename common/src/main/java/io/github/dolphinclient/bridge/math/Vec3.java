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

package io.github.dolphinclient.bridge.math;

@SuppressWarnings("unused")
public record Vec3(double x, double y, double z) {
	public Vec3 x(double x) {
		return new Vec3(x, y, z);
	}

	public Vec3 y(double y) {
		return new Vec3(x, y, z);
	}

	public Vec3 z(double z) {
		return new Vec3(x, y, z);
	}

	public double lenSq() {
		return x * x + y * y + z * z;
	}

	public double len() {
		return Math.sqrt(lenSq());
	}

	public Vec3 add(Vec3 rhs) {
		return add(rhs.x, rhs.y, rhs.z);
	}

	public Vec3 add(double x, double y, double z) {
		return new Vec3(this.x + x, this.y + y, this.z + z);
	}

	public Vec3 sub(Vec3 rhs) {
		return sub(rhs.x, rhs.y, rhs.z);
	}

	public Vec3 sub(double x, double y, double z) {
		return new Vec3(this.x - x, this.y - y, this.z - z);
	}

	public double distSq(Vec3 rhs) {
		double dx = x - rhs.x;
		double dy = y - rhs.y;
		double dz = z - rhs.z;

		return dx * dx + dy * dy + dz * dz;
	}

	public double dist(Vec3 rhs) {
		return Math.sqrt(distSq(rhs));
	}

	public Vec3 abs() {
		return new Vec3(Math.abs(x), Math.abs(y), Math.abs(z));
	}

	public Vec3 max(double val) {
		return new Vec3(Math.max(x, val), Math.max(y, val), Math.max(z, val));
	}

	public Vec3 min(double val) {
		return new Vec3(Math.min(x, val), Math.min(y, val), Math.min(z, val));
	}

	public Vec3 div(double scalar) {
		return div(scalar, scalar, scalar);
	}

	public Vec3 div(Vec3 d) {
		return div(d.x, d.y, d.z);
	}

	public Vec3 div(double x, double y, double z) {
		return new Vec3(x()/x, y()/y, z()/z);
	}

	public Vec3 negate() {
		return new Vec3(-x, -y, -z);
	}
}
