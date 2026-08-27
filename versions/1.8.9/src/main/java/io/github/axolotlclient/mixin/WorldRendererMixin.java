/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.modules.sky.SkyboxManager;
import io.github.axolotlclient.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.HitResult;
import net.minecraft.world.dimension.Dimension;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * <p>License: MIT</p>
 **/

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCustomSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		if (this.world.dimension.isNatural()) {
			if (DolphinClient.config().customSky.get() && SkyboxManager.getInstance().hasSkyBoxes()) {
				GlStateManager.depthMask(false);
				this.minecraft.profiler.push("Custom Skies");
				SkyboxManager.getInstance().renderSkyboxes(tickDelta, world.getRain(tickDelta));
				this.minecraft.profiler.pop();
				GlStateManager.depthMask(true);
				ci.cancel();
			}
		}
	}

	@Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getCloudHeight()F"))
	public float axolotlclient$getCloudHeight(Dimension instance) {
		return DolphinClient.config().cloudHeight.get();
	}

	@ModifyArg(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glLineWidth(F)V"), remap = false)
	public float axolotlclient$OutlineWidth(float width) {
		if (DolphinClient.config().enableCustomOutlines.get() && DolphinClient.config().outlineWidth.get() > 1) {
			return width + DolphinClient.config().outlineWidth.get() - 1f;
		}
		return width;
	}

	@Inject(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;depthMask(Z)V", ordinal = 1))
	private void resetOutlineWidth(PlayerEntity playerEntity, HitResult hitResult, int i, float f4, CallbackInfo ci) {
		GL11.glLineWidth(2.0F);
	}

	@Inject(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;color4f(FFFF)V", shift = At.Shift.AFTER))
	public void axolotlclient$customOutlineColor(PlayerEntity camera, HitResult hitResult, int i, float tickDelta, CallbackInfo ci) {
		if (DolphinClient.config().enableCustomOutlines.get()) {
			DrawUtil.drawOutlines(camera, hitResult, tickDelta, world);

			GlStateManager.clearColor();

			int color = DolphinClient.config().outlineColor.get().toInt();
			float a = (float) (color >> 24 & 0xFF) / 255.0F;
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			GlStateManager.color4f(r, g, b, a);
		}
	}


}
