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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.bridge.AxoPerspective;
import io.github.axolotlclient.modules.hud.gui.hud.PlayerHud;
import io.github.axolotlclient.modules.hypixel.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.vertex.VertexFormat;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> extends EntityRenderer<T> {

	protected LivingEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Inject(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 1))
	private void axolotlclient$correctNameplateRotation(LivingEntity livingEntity, double d, double e, double f, CallbackInfo ci) {
		if (Minecraft.getInstance().options.perspective == AxoPerspective.THIRD_PERSON_FRONT.ordinal()) {
			GlStateManager.rotatef(-this.dispatcher.cameraPitch * 2, 1.0F, 0.0F, 0.0F);
		}
	}

	@Inject(method = "shouldRenderNameTag(Lnet/minecraft/entity/living/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$showOwnNametag(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
		if (DolphinClient.config().showOwnNametag.get()
			&& livingEntity.getNetworkId() == Minecraft.getInstance().player.getNetworkId()
			&& !PlayerHud.isCurrentlyRendering()) {
			cir.setReturnValue(true);
		}
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;getDisplayName()Lnet/minecraft/text/Text;"))
	public Text axolotlclient$hideNameWhenSneaking(LivingEntity instance, Operation<Text> original) {
		var orig = original.call(instance);
		if (instance instanceof ClientPlayerEntity) {
			if (NickHider.getInstance().hideOwnName.get() && instance.equals(Minecraft.getInstance().player)) {
				orig = (Text) NickHider.getInstance().editComponent(orig, instance.getName(), NickHider.getInstance().hiddenNameSelf.get());
			} else if (NickHider.getInstance().hideOtherNames.get()
				&& !instance.equals(Minecraft.getInstance().player)) {
				orig = (Text) NickHider.getInstance().editComponent(orig, instance.getName(), NickHider.getInstance().hiddenNameOthers.get());
			}
		}
		return orig;
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;begin(ILnet/minecraft/client/render/vertex/VertexFormat;)V"))
	private boolean disableNameTagBackground(BufferBuilder instance, int drawMode, VertexFormat format) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;nextVertex()V"))
	private boolean disableNameTagBackground$2(BufferBuilder instance) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/Tesselator;end()V"))
	private boolean disableNameTagBackground$3(Tesselator instance) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;vertex(DDD)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	private BufferBuilder disableNameTagBackground(BufferBuilder instance, double x, double y, double z, Operation<BufferBuilder> original) {
		if (DolphinClient.config().nametagBackground.get()) {
			return original.call(instance, x, y, z);
		}
		return instance;
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;color(FFFF)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	private BufferBuilder disableNameTagBackground$2(BufferBuilder instance, float r, float g, float b, float a, Operation<BufferBuilder> original) {
		if (DolphinClient.config().nametagBackground.get()) {
			return original.call(instance, r, g, b, a);
		}
		return instance;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 1.0f, ordinal = 0))
	private float axolotlclient$customHitColorRed(float constant) {
		return DolphinClient.config().hitColor.get().getRed() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.0f, ordinal = 0))
	private float axolotlclient$customHitColorGreen(float constant) {
		return DolphinClient.config().hitColor.get().getGreen() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.0f, ordinal = 1))
	private float axolotlclient$customHitColorBlue(float constant) {
		return DolphinClient.config().hitColor.get().getBlue() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.3f, ordinal = 0))
	private float axolotlclient$customHitColorAlpha(float constant) {
		return DolphinClient.config().hitColor.get().getAlpha() / 255F;
	}
}
