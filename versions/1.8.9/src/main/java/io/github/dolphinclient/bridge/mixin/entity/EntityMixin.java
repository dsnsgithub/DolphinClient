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

package io.github.dolphinclient.bridge.mixin.entity;

import java.util.UUID;

import io.github.dolphinclient.bridge.entity.AxoEntity;
import io.github.dolphinclient.bridge.math.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements AxoEntity {
	@Shadow
	@Nullable
	public Entity vehicle;

	@Shadow
	public double z;

	@Shadow
	public double y;

	@Shadow
	public double x;

	@Shadow
	public double velocityX;

	@Shadow
	public double velocityY;

	@Shadow
	public double velocityZ;

	@Shadow
	public boolean onGround;

	@Shadow
	public abstract Vec3d getRotationVec(float tickDelta);

	@Shadow
	public float yaw;

	@Shadow
	public abstract UUID getUuid();

	@Shadow
	public float pitch;

	@Shadow
	private int networkId;

	@Shadow
	public abstract Vec3d getEyePosition(float f);

	@Shadow
	public float height;

	@Shadow
	public abstract Box getShape();

	@Override
	public @Nullable AxoEntity br$getVehicle() {
		return vehicle;
	}

	@Override
	public Vec3 br$getPos() {
		return new Vec3(this.x, this.y, this.z);
	}

	@Override
	public Vec3 br$getVelocity() {
		return new Vec3(this.velocityX, this.velocityY, this.velocityZ);
	}

	@Override
	public boolean br$isOnGround() {
		return onGround;
	}

	@Override
	public float br$getYaw() {
		return yaw;
	}

	@Override
	public float br$getPitch() {
		return pitch;
	}

	@Override
	public Vec3 br$getRotation(float deltaTick) {
		final var vec = getRotationVec(deltaTick);
		return new Vec3(vec.x, vec.y, vec.z);
	}

	@Override
	public UUID br$getUuid() {
		return getUuid();
	}

	@Override
	public int br$getNetId() {
		return networkId;
	}

	@Override
	public Vec3 br$getEyePos(float delta) {
		var pos = getEyePosition(delta);
		return new Vec3(pos.x, pos.y, pos.z);
	}

	@Override
	public float br$getHeight() {
		return height;
	}

	@Override
	public Vec3 br$getBoundingBoxHalfDimensions() {
		var box = getShape();
		return new Vec3(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ).div(2);
	}
}
