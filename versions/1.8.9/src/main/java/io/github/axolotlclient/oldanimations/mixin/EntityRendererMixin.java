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
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.MobUtil;
import io.github.axolotlclient.oldanimations.util.PlayerUtil;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	@ModifyExpressionValue(method = "renderOnFire", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/Entity;y:D"))
	private double axolotlclient$includeEyeHeight$Y(double original, @Local(argsOnly = true) Entity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.flameOffset.get() && PlayerUtil.INSTANCE.isSelf(entity)) {
			/* adapted from 1.7 */
			/* we must make sure the flame is synced with the interpolated player model position */
			original += axolotlclient$getEyeHeight(entity);
		}
		return original;
	}

	@ModifyExpressionValue(method = "renderOnFire", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/Entity;width:F"))
	private float axolotlclient$oldMobWidth(float original, @Local(argsOnly = true) Entity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mobSizeDimensions.get()) {
			/* since entity hitbox sizes are slightly different in 1.7, this is the closest we can get to emulating that */
			/* without altering combat */
			original = MobUtil.INSTANCE.oldMobWidth(entity, original);
		}
		return original;
	}

	@ModifyExpressionValue(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/Entity;height:F"))
	private float axolotlclient$oldMobHeight(float original, @Local(argsOnly = true) Entity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mobSizeDimensions.get()) {
			original = MobUtil.INSTANCE.oldMobHeight(entity, original);
		}
		return original;
	}

	//todo: we can improve this a bit
	@WrapOperation(method = "postRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderOnFire(Lnet/minecraft/entity/Entity;DDDF)V"))
	private void axolotlclient$includeEyeHeight$renderFire(EntityRenderer<?> instance, Entity entity, double dx, double dy, double dz, float tickDelta, Operation<Void> original) {
		boolean oldFlameHeight = OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.flameOffset.get() && PlayerUtil.INSTANCE.isSelf(entity);
		if (oldFlameHeight) {
			GlStateManager.pushMatrix();
			/* adapted from 1.7 */
			/* we must make sure the flame is synced with the interpolated player model position */
			GlStateManager.translatef(0.0F, axolotlclient$getEyeHeight(entity), 0.0F);
		}
		original.call(instance, entity, dx, dy, dz, tickDelta);
		if (oldFlameHeight) {
			GlStateManager.popMatrix();
		}
	}

	@WrapOperation(method = "postRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;shouldRenderShadow()Z"))
	private boolean axolotlclient$removeShadowCheck(EntityRenderDispatcher instance, Operation<Boolean> original) {
		/* its already true by default but eh */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.showShadowInInventory.get() || original.call(instance);
	}

	@ModifyArg(method = "postRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderShadow(Lnet/minecraft/entity/Entity;DDDFF)V"), index = 2)
	private double axolotlclient$addEyeHeightEntityPosition(double dy, @Local(argsOnly = true) Entity entity) {
		/* 1.7 has the player eyeheight factored into any y coord call*/
		return dy + (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.shadowOffset.get() && PlayerUtil.INSTANCE.isSelf(entity) ?
			axolotlclient$getEyeHeight(entity) : 0);
	}

	@ModifyVariable(method = "renderShadow", at = @At(value = "STORE", ordinal = 0), index = 14)
	private double axolotlclient$addEyeHeightEntityPosition2(double e, @Local(argsOnly = true) Entity entity) {
		/* we need to include the shadow height offset which is just some arbitrary value lmao */
		return e +
			(OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.shadowOffset.get() && PlayerUtil.INSTANCE.isSelf(entity) ?
				axolotlclient$getEyeHeight(entity) + axolotlclient$getShadowHeightOffset(entity) : 0);
	}

	@ModifyArg(method = "renderShadow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderShadowOnBlock(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/math/BlockPos;FFDDD)V"), index = 2)
	private double axolotlclient$addShadowHeightOffset(double dy, @Local(argsOnly = true) Entity entity) {
		return dy + (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.shadowOffset.get() && PlayerUtil.INSTANCE.isSelf(entity) ?
			axolotlclient$getShadowHeightOffset(entity) : 0);
	}

	@ModifyArg(method = "renderShadow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderShadowOnBlock(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/math/BlockPos;FFDDD)V"), index = 8)
	private double axolotlclient$addShadowHeightOffset2(double o, @Local(argsOnly = true) Entity entity) {
		return o + (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.shadowOffset.get() && PlayerUtil.INSTANCE.isSelf(entity) ?
			axolotlclient$getShadowHeightOffset(entity) : 0);
	}

	@Unique
	private float axolotlclient$getShadowHeightOffset(Entity entity) {
		/* MC-4767 */
		/* taken from 1.7. genuinely no idea why the hell its this value */
		return entity.height / 2;
	}

	@Unique
	private float axolotlclient$getEyeHeight(Entity entity) {
		//todo: this might need to be moved
		return (OldAnimationsConfig.instance.thirdPersonSneaking.get() ? PlayerUtil.INSTANCE.getEyeHeight() : entity.getEyeHeight());
	}
}
