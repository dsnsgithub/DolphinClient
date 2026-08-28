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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.util.NametagRendering;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobRenderer.class)
public class MobRendererMixin {

	@WrapOperation(method = "shouldRenderNameTag(Lnet/minecraft/entity/living/mob/MobEntity;)Z", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;targetEntity:Lnet/minecraft/entity/Entity;"))
	private Entity axolotlclient$alwaysRenderNametags(EntityRenderDispatcher instance, Operation<Entity> original, MobEntity entity) {
		return NametagRendering.lookTarget(original.call(instance), entity, DolphinClient.config().alwaysRenderNametags.get());
	}
}
