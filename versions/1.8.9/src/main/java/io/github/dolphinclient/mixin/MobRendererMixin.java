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

package io.github.dolphinclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.DolphinClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobRenderer.class)
public class MobRendererMixin {

	/**
	 * Vanilla 1.8 only renders a mob's name tag while it is under the crosshair; newer versions
	 * always show it. Fake the crosshair check to match modern behavior. The preceding
	 * {@code hasCustomName()} check is left untouched so unnamed mobs stay tag-free.
	 */
	@ModifyExpressionValue(method = "shouldRenderNameTag(Lnet/minecraft/entity/living/mob/MobEntity;)Z", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;targetEntity:Lnet/minecraft/entity/Entity;"))
	private Entity dolphinclient$renderNametagsWithoutCrosshair(Entity original, @Local(argsOnly = true) MobEntity mob) {
		if (DolphinClient.config().alwaysShowEntityNametags.get()) {
			return mob;
		}
		return original;
	}
}
