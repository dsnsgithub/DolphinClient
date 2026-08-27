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

import java.awt.image.BufferedImage;
import java.io.InputStream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.util.AltIcons;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.texture.SimpleTexture;
import net.minecraft.client.resource.Resource;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleTexture.class)
public class SimpleTextureMixin {

	@Shadow
	@Final
	protected Identifier location;

	@WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/Resource;asStream()Ljava/io/InputStream;"))
	private InputStream getAltIcon(Resource instance, Operation<InputStream> original) {
		if (DolphinClientCommon.BADGE_PATH.equals(location) && !DolphinClientCommon.getInstance().getConfig().noAltIcons.get()) {
			return AltIcons.getAltIcon().orElseGet(() -> original.call(instance));
		}
		return original.call(instance);
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/texture/TextureUtil;upload(ILjava/awt/image/BufferedImage;ZZ)I"))
	private void beforeUpload(ResourceManager resourceManager, CallbackInfo ci, @Local BufferedImage image) {
		if (this.location.equals(GuiElement.ICONS_LOCATION)) {
			// Fix the crosshair texture if it has a black background instead of the usual transparent one.
			// Why would anyone make a broken resource pack??
			var crosshair = image.getSubimage(0, 0, (int) (image.getWidth() * ((float) 16 / 256)), (int) (image.getHeight() * ((float) 16 / 256)));
			var ints = new int[crosshair.getWidth() * crosshair.getHeight()];
			crosshair.getRGB(0, 0, crosshair.getWidth(), crosshair.getHeight(), ints, 0, crosshair.getWidth());
			for (int j = 0; j < ints.length; j++) {
				var i = ints[j];
				if (i == 0xFF000000) {
					ints[j] = 0;
				}
			}
			crosshair.setRGB(0, 0, crosshair.getWidth(), crosshair.getHeight(), ints, 0, crosshair.getWidth());
		}
	}
}
