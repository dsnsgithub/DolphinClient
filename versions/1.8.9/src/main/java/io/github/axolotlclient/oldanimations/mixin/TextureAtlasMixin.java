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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.render.texture.AbstractTexture;
import net.minecraft.client.render.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.image.BufferedImage;

@Mixin(TextureAtlas.class /* C_6088271 */)
public abstract class TextureAtlasMixin extends AbstractTexture {

	@Dynamic("Revert OptiFine mipmap scaling")
	@WrapOperation(method = "loadAndStitch", at = @At(value = "INVOKE", target = "net/optifine/util/TextureUtils.scaleImage(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;"), require = 0)
	private BufferedImage axolotlclient$revertOptiFineMipmapScale(BufferedImage bi, int w2, Operation<BufferedImage> original) {
		/* i didnt want to do this but i had to */
		/* optifine breaks mipmapping :/ */
		return OldAnimationsConfig.isEnabled() ? bi : original.call(bi, w2);
	}

	@ModifyArg(method = "loadAndStitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;log2(I)I", ordinal = 2))
	private int axolotlclient$oldMipmapScale(int original) {
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mipmapPrecision.get() ? Integer.MAX_VALUE : original;
	}
}
