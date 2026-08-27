/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
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
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.render.entity.layer.AbstractArmorLayer;
import net.minecraft.client.render.entity.layer.EntityRenderLayer;
import net.minecraft.client.render.model.Model;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArmorLayer.class)
public abstract class AbstractArmorLayerMixin implements EntityRenderLayer<LivingEntity> {

	@WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/Model;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
	private void hitColorOnArmor(Model instance, Entity entity, float walkAnimationProgress, float walkAnimationSpeed, float bob, float yaw, float pitch, float scale, Operation<Void> original) {
		var hasOverlay = false;
		if (entity instanceof LivingEntity l && DolphinClient.config().hitColorOnArmor.get()) {
			hasOverlay = setupOverlayColor(l);
		}
		original.call(instance, entity, walkAnimationProgress, walkAnimationSpeed, bob, yaw, pitch, scale);
		if (hasOverlay) {
			tearDownOverlayColor();
		}
	}

	// see: LivingEntityRenderer
	@Unique
	protected boolean setupOverlayColor(LivingEntity entity) {
		boolean bl2 = entity.damagedTimer > 0 || entity.deathTicks > 0;
		if (!bl2) {
			return false;
		}
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
		GlStateManager.enableTexture();
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, 8448);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, GLX.GL_TEXTURE0);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PRIMARY_COLOR);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 7681);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_TEXTURE0);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		GlStateManager.enableTexture();
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, GLX.GL_INTERPOLATE);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, GLX.GL_CONSTANT);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE2_RGB, GLX.GL_CONSTANT);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND2_RGB, 770);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 7681);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		var c = DolphinClient.config().hitColor.get().toInt();
		GL11.glTexEnvfv(8960, 8705, new float[]{ClientColors.ARGB.redFloat(c), ClientColors.ARGB.greenFloat(c), ClientColors.ARGB.blueFloat(c), ClientColors.ARGB.alphaFloat(c)});
		GlStateManager.activeTexture(GLX.GL_TEXTURE2);
		GlStateManager.enableTexture();
		GlStateManager.bindTexture(LivingEntityRendererAccessor.getWhiteTexture().getGlId());
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, 8448);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_TEXTURE1);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 7681);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
		return true;
	}

	@Unique
	protected void tearDownOverlayColor() {
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
		GlStateManager.enableTexture();
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, 8448);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, GLX.GL_TEXTURE0);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PRIMARY_COLOR);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 8448);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, GLX.GL_TEXTURE0);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_ALPHA, GLX.GL_PRIMARY_COLOR);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_ALPHA, 770);
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, 8448);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, 5890);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 8448);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, 5890);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.activeTexture(GLX.GL_TEXTURE2);
		GlStateManager.disableTexture();
		GlStateManager.bindTexture(0);
		GL11.glTexEnvi(8960, 8704, GLX.GL_COMBINE);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_RGB, 8448);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND1_RGB, 768);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_RGB, 5890);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE1_RGB, GLX.GL_PREVIOUS);
		GL11.glTexEnvi(8960, GLX.GL_COMBINE_ALPHA, 8448);
		GL11.glTexEnvi(8960, GLX.GL_OPERAND0_ALPHA, 770);
		GL11.glTexEnvi(8960, GLX.GL_SOURCE0_ALPHA, 5890);
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
	}
}
