/*
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

package io.github.axolotlclient.oldanimations.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.CrosshairHud;
import io.github.axolotlclient.oldanimations.OldAnimations;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.ducks.Sneaky;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.WorldGeneratorType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements Sneaky {

	@Shadow
	/* why you not final :( */
	private Minecraft minecraft;

	@Unique
	private float lastCameraY;

	@Unique
	private float cameraY;

	@Unique
	private float eyeHeight;

	@Inject(method = "setupCamera", at = @At("HEAD"))
	protected void axolotlclient$lerpCamera(float tickDelta, int anaglyphRenderPass, CallbackInfo ci) {
		/* WorldRenderer#setupRender is where the position of the player is handled */
		/* but we can apply everything here... it's easier :p */
		/* eye height is interpolated between the last and current camera Y positions */
		if (!OldAnimationsConfig.isEnabled()) return;
		if (OldAnimationsConfig.instance.smoothSneaking.get()) {
			eyeHeight = lerp(tickDelta, lastCameraY, cameraY);
		} else if (OldAnimationsConfig.instance.slowUpSneak.get()) {
			eyeHeight = cameraY;
		}
	}

	//todo: this code is so messy i need to rewrite it all at some point
	@ModifyVariable(method = "transformCamera", at = @At(value = "STORE", ordinal = 0), index = 3)
	private float axolotlclient$useLerpEyeHeight(float original, @Local Entity entity) {
		if (OldAnimationsConfig.isEnabled()) {
			if (entity instanceof LivingEntity && ((LivingEntity) entity).isSleeping()) {
				/* this genuinely is MC-19 */
				/*  0.2F - 1.62F = -1.42F   modified from 1.7                                     */
				/*                + 1.0F    we need to negate the sleep's eye height              */
				/*                + 1.0F    this is the easiest way to do it                      */
				/*          0.2F - 0.58F	the math is kinda dodgy,                              */
				/*                          but it's genuinely the 1.7 sleep eye height value 1:1 */
				return OldAnimationsConfig.instance.sleepEyeHeight.get() ? -0.38F : original;
			}
			if (OldAnimationsConfig.instance.horseRiderEyeHeight.get() && entity.isRiding()) {
				/*  1.62F - 0.5F   modified from 1.7                       */
				/*        + 0.35F  cancels out PlayerEntity#getRideHeight  */
				/*                                                         */
				/* like the sleep eye height, this is math is 1:1 with 1.7 */
				return 1.47F;
			}
		}
		return axolotlclient$isEitherSneakOptionEnabled() ? axolotlclient$getEyeHeight() : original;
	}

	@ModifyArg(method = "renderAxisIndicators", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;translatef(FFF)V"), index = 1)
	private float axolotlclient$useLerpEyeHeight_Debug(float x) {
		/* debug crosshair parity */
		return axolotlclient$isEitherSneakOptionEnabled() ? axolotlclient$getEyeHeight() : x;
	}

	@WrapOperation(method = "renderAxisIndicators", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;hasReducedDebugInfo()Z"))
	private boolean axolotlclient$disableAxisIndicator(LocalClientPlayerEntity instance, Operation<Boolean> original) {
		boolean isCustomCrosshair = OldAnimations.AXOLOTLCLIENT && HudManager.getInstance().get(CrosshairHud.ID).isEnabled();
		return (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.alwaysShowCrosshair.get() && !isCustomCrosshair) || original.call(instance);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ItemInHandRenderer;tick()V")) /* placed below null check */
	private void axolotlclient$onTick(CallbackInfo ci) {
		/* updates the current eye height */
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}
		Entity camera = minecraft.getCamera();
		if ((OldAnimationsConfig.instance.smoothSneaking.get() || OldAnimationsConfig.instance.slowUpSneak.get())) {
			float eyeHeight = camera.isSneaking() ? 1.54F : 1.62F;
			lastCameraY = cameraY;
			if (OldAnimationsConfig.instance.slowUpSneak.get() && eyeHeight > cameraY) {
				/* the value is 0.4f in 1.7, however the math that is applied, when rearranged, */
				/* will yield 0.6f when adapted to 1.13+ sneaking logic */
				/* that being said, 1.13 uses 0.5f which is a tiny bit slower than 1.7! */
				/* turns out TheKodeToad was right the whole time... damn */
				cameraY += (eyeHeight - cameraY) * 0.6f;
			} else {
				cameraY = eyeHeight;
			}
		}
	}

	@ModifyExpressionValue(method = "applyHurtCam", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/living/LivingEntity;damagedTimer:I"))
	private int axolotlclient$oldDamageTick(int original) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldDamageTick.get()) {
			//todo: find the source
			return Math.max(original - 1, 0);
		}
		return original;
	}

	@ModifyExpressionValue(method = "setupFog", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/GameRenderer;renderDistance:F", ordinal = 1))
	private float axolotlclient$renderVoidFog(float original, @Local(argsOnly = true) int i, @Local(argsOnly = true) float f) {
		/* void fog logic taken straight from 1.7 */
		float gx = original;
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.voidFog.get()) {
			Entity entity = minecraft.getCamera();
			Dimension dimension = minecraft.world.dimension;
			if (i == 0 &&
				/* 1.7's hasFog() method */
				((DimensionAccessor) dimension).getGeneratorType() != WorldGeneratorType.FLAT && !dimension.hasNoSky()) {
				double d = ((entity.getLightLevel(f) & 15728640) >> 20) / 16.0 + (entity.prevY + (entity.y - entity.prevY) * f + 4.0) / 32.0;
				if (d < 1.0) {
					if (d < 0.0) {
						d = 0.0;
					}
					d *= d;
					float h = 100.0F * (float) d;
					if (h < 5.0F) {
						h = 5.0F;
					}
					if (gx > h) {
						gx = h;
					}
				}
			}
		}
		/* welcome back my friend */
		return gx;
	}

	@Inject(method = "render(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/WorldRenderer;render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I"))
	private void axolotlclient$applyMipmapToBlocks(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		/* optifine already does this, but i might as well give the player the option lol */
		minecraft.getTextureManager().get(TextureAtlas.BLOCKS_LOCATION).pushFilter(false,
			OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mipmapAllBlocks.get() &&
				minecraft.options.mipmapLevels > 0
		);
	}

	@Inject(method = "render(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/WorldRenderer;render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I", shift = At.Shift.AFTER))
	private void axolotlclient$applyMipmapToBlocks2(int anaglyphRenderPass, float tickDelta, long renderTimeLimit, CallbackInfo ci) {
		minecraft.getTextureManager().get(TextureAtlas.BLOCKS_LOCATION).popFilter();
	}

	@Unique
	private static float lerp(float delta, float start, float end) { /* taken straight from modern minecraft */
		return start + delta * (end - start);
	}

	@Unique
	private boolean axolotlclient$isEitherSneakOptionEnabled() {
		/* if neither of the sneaking options are selected, we might as well just use the original eyeheight */
		return OldAnimationsConfig.isEnabled() && (OldAnimationsConfig.instance.smoothSneaking.get() || OldAnimationsConfig.instance.slowUpSneak.get());
	}

	@Override
	public float axolotlclient$getEyeHeight() {
		return eyeHeight;
	}
}
