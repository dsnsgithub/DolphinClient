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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.ItemUtil;
import net.minecraft.client.render.entity.ItemSpriteRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemSpriteRenderer.class)
public abstract class ItemSpriteRendererMixin {

	@WrapMethod(method = "render")
	private void axolotlclient$captureGroundType(Entity entity, double dx, double dy, double dz, float yaw, float tickDelta, Operation<Void> original) {
		/* makes the distinction between the two ground types clearer */
		ItemUtil.INSTANCE.setType(ItemUtil.GroundType.THROWN);
		original.call(entity, dx, dy, dz, yaw, tickDelta);
		ItemUtil.INSTANCE.setType(null);
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 0), index = 0)
	private float axolotlclient$rotateProjectile(float angle) {
		/* fixed in 1.9+ thankfully */
		return angle + (axolotlclient$shouldMirrorProjectiles() ? 180.0F : 0.0F);
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 1), index = 0)
	private float axolotlclient$useProperCameraView(float angle) {
		return angle * (axolotlclient$shouldMirrorProjectiles() ? -1 : 1);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private void axolotlclient$applyProjectilePosition(Entity entity, double dx, double dy, double dz, float yaw, float tickDelta, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.potionEntityOffset.get()) {
			/* this translation matches item rendering to 1.7 */
			/* taken from ItemEntityRenderer */
			GlStateManager.translatef(0.0F, 0.25F, 0.0F);
		}

		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.projectileSprite.get()) {
			/* half of a pixel, matches 1.7's sprite rendering */
			GlStateManager.translatef(0.0F, 0.0F, 0.03125F);
		}
	}

	@Unique
	private boolean axolotlclient$shouldMirrorProjectiles() {
		/* because our sprite rendering literally render one face, the other side is non-existant */
		/* we need it to always be mirrored if that's enabled :p */
		return OldAnimationsConfig.isEnabled() && (OldAnimationsConfig.instance.mirroredProjectiles.get() || OldAnimationsConfig.instance.projectileSprite.get());
	}
}
