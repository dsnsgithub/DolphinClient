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

package io.github.dolphinclient.bridge.entity;

import java.util.UUID;

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import io.github.dolphinclient.bridge.math.Vec3;
import org.jetbrains.annotations.Nullable;

public interface AxoEntity {
	@RequiresImpl
	default @Nullable AxoEntity br$getVehicle() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Vec3 br$getPos() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Vec3 br$getVelocity() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default boolean br$isOnGround() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default float br$getYaw() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default float br$getPitch() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default int br$getNetId() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Vec3 br$getRotation(float deltaTick) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default UUID br$getUuid() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default Vec3 br$getEyePos(float delta) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default float br$getHeight() {
		throw BridgeUtil.noImpl();
	}

	/**
	 * Get the radii of this entity's bounding box.
	 *
	 * @return a vector of half the box's side lengths. The origin of the vector is (0, height/2, 0) relative to the Entity's position.
	 */
	@RequiresImpl
	default Vec3 br$getBoundingBoxHalfDimensions() {
		throw BridgeUtil.noImpl();
	}
}
