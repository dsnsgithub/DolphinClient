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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.PlayerUtil;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	//TODO: this can probably be moved somewhere else like in the main entity rendering method
	@Inject(method = "render(Lnet/minecraft/entity/living/LivingEntity;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;translatef(FFF)V"))
	private void axolotlclient$addSneakTranslation(LivingEntity livingEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.thirdPersonSneaking.get() && PlayerUtil.INSTANCE.isSelf(livingEntity)) {
			/* in order to match 1.7, we need to elevate the player model while sneaking */
			/* the elevation will be the difference between the player's sneaking eyeheight and their actual eyeheight (1.62 meters) */
			/* the player model should now move 1:1 with the crosshair */
			GlStateManager.translatef(0.0F, 1.62F - PlayerUtil.INSTANCE.getEyeHeight(), 0.0F);
		}
	}

	@ModifyArg(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;translatef(FFF)V", ordinal = 0), index = 1)
	private float axolotlclient$syncNameTag(float original, @Local(argsOnly = true) LivingEntity livingEntity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.thirdPersonSneaking.get() && PlayerUtil.INSTANCE.isSelf(livingEntity)) {
			/* we must ensure the nametag is synced with the interpolated player model position */
			original += PlayerUtil.INSTANCE.getEyeHeight() - 1.62F;
		}
		return original;
	}

	@ModifyArg(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;FD)V"), index = 2)
	private double axolotlclient$syncNameTag2(double original, @Local(argsOnly = true) LivingEntity livingEntity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.thirdPersonSneaking.get() && PlayerUtil.INSTANCE.isSelf(livingEntity)) {
			/* we must ensure the nametag is synced with the interpolated player model position once again */
			original = original + PlayerUtil.INSTANCE.getEyeHeight() - 1.62F;
		}
		return original;
	}
}
