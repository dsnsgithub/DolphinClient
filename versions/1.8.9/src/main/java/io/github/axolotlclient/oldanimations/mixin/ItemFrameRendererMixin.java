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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.render.entity.ItemFrameRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {

	/* wow */

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;disableLighting()V"))
	private boolean axolotlclient$allowLighting() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;pushLightingAttributes()V"))
	private boolean axolotlclient$allowLighting2() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/Lighting;turnOff()V"))
	private boolean axolotlclient$allowLighting3() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/Lighting;turnOn()V"))
	private boolean axolotlclient$allowLighting4() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;popAttributes()V"))
	private boolean axolotlclient$allowLighting5() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@WrapWithCondition(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;enableLighting()V"))
	private boolean axolotlclient$allowLighting6() {
		/* taken from 1.7 */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.framedItemLighting.get();
	}

	@Inject(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private void axolotlclient$applyFramedItemPosition(ItemFrameEntity itemFrame, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled()) {
			if (OldAnimationsConfig.instance.framedItemPosition.get()) {
				/* we need to first match the 3d item like in 1.7 */
				GlStateManager.translatef(0.0F, 0.0F, -0.03125F);
			}
			if (OldAnimationsConfig.instance.framedItemSprite.get()) {
				/* half of a pixel, matches 1.7's sprite rendering */
				GlStateManager.translatef(0.0F, 0.0F, -0.03125F);
			}
		}
	}

	@Inject(method = "renderDisplayItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 0, shift = At.Shift.AFTER))
	private void axolotlclient$centerFramedItem(ItemFrameEntity itemFrame, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.framedItemRotationOffset.get()) {
			/* MC-8662 */
			//todo: this is unfortunately not 100% accurate yet. its gonna take me some time
			switch (itemFrame.rotation()) {
				case 2:
					GL11.glTranslatef(-0.03125F, 0.03125F + 0.0078125F, 0.0F);
					break;
				case 4:
					GL11.glTranslatef(-0.03125F / 2F, 0.0625F, 0.0F);
					break;
				case 6:
					GL11.glTranslatef(0.0325F + 0.0078125F, 0.03125F + 0.0078125F, 0.0F);
					break;
			}
		}
	}
}
