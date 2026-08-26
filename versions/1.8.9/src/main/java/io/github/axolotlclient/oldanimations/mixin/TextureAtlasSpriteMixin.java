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
import net.minecraft.client.render.texture.TextureAtlasSprite;
import net.minecraft.client.resource.metadata.AnimationMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.image.BufferedImage;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
	private void axolotlclient$oldInterpolateColor(BufferedImage[] image, AnimationMetadata animation, CallbackInfo ci, @Local int[][] is) {
		/* this was removed in 1.8 for some reason */
		/* i have yet to see a difference with it however so im not sure if it meaningfully does anything */
		/* i probably wont give it its own toggle atp */
		if (OldAnimationsConfig.isEnabled()) {
			interpolateColor(is);
		}
	}

	@Unique
	private void interpolateColor(int[][] image) {
		/* taken from 1.7 */
		int[] is = image[0];
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;

		for (int value : is) {
			if ((value & 0xFF000000) != 0) {
				j += value >> 16 & 0xFF;
				k += value >> 8 & 0xFF;
				l += value & 0xFF;
				i++;
			}
		}

		if (i != 0) {
			j /= i;
			k /= i;
			l /= i;

			for (int var11 = 0; var11 < is.length; var11++) {
				if ((is[var11] & 0xFF000000) == 0) {
					is[var11] = j << 16 | k << 8 | l;
				}
			}
		}
	}
}
