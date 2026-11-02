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

import net.minecraft.client.render.model.entity.PlayerModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerEntityModelMixin {

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/model/entity/PlayerModel;isBaby:Z", opcode = Opcodes.GETFIELD))
	public void dolphinclient$translucencyStart(Entity entity, float f, float g, float h, float i, float j, float scale,
												CallbackInfo ci) {
		startTranslucency();
	}

	@Unique
	private void startTranslucency() {
		GlStateManager.pushMatrix();

		GlStateManager.enableCull();
		//GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;popMatrix()V"))
	public void dolphinclient$translucencyStop(Entity entity, float f, float g, float h, float i, float j, float scale,
											   CallbackInfo ci) {
		stopTranslucency();
	}

	@Unique
	private void stopTranslucency() {
		GlStateManager.disableBlend();
		//GlStateManager.disableRescaleNormal();
		GlStateManager.disableCull();
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
	}

	@Inject(method = "renderRightArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelPart;render(F)V", ordinal = 1))
	public void dolphinclient$handTranslucentRightStart(CallbackInfo ci) {
		startTranslucency();
	}

	@Inject(method = "renderRightArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelPart;render(F)V", ordinal = 1, shift = At.Shift.AFTER))
	public void dolphinclient$handTranslucentRightStop(CallbackInfo ci) {
		stopTranslucency();
	}

	@Inject(method = "renderLeftArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelPart;render(F)V", ordinal = 1))
	public void dolphinclient$handTranslucentLeftStart(CallbackInfo ci) {
		startTranslucency();
	}

	@Inject(method = "renderLeftArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelPart;render(F)V", ordinal = 1, shift = At.Shift.AFTER))
	public void dolphinclient$handTranslucentLEftStop(CallbackInfo ci) {
		stopTranslucency();
	}
}
