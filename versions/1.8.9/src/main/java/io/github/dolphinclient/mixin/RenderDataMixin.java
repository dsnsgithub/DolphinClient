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

package io.github.dolphinclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.dolphinclient.modules.freelook.Freelook;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.living.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class RenderDataMixin {

	@WrapOperation(method = "setup", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;yaw:F", opcode = Opcodes.GETFIELD))
	private static float dolphinclient$freelook$getYaw(PlayerEntity instance, Operation<Float> original) {
		return Freelook.getInstance().yaw(original.call(instance));
	}

	@WrapOperation(method = "setup", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;pitch:F", opcode = Opcodes.GETFIELD))
	private static float dolphinclient$freelook$getPitch(PlayerEntity instance, Operation<Float> original) {
		return Freelook.getInstance().pitch(original.call(instance));
	}
}
