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
import net.minecraft.client.render.entity.PrimedTntRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PrimedTntRenderer.class)
public abstract class PrimedTntRendererMixin {

	@WrapWithCondition(method = "render(Lnet/minecraft/entity/PrimedTntEntity;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;disablePolygonOffset()V"))
	private boolean axolotlclient$revertTntPolygonOffset() {
		/* MC-1532 */
		/* bringing back the primed tnt z fighting lmao */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.tntZFighting.get();
	}

	@WrapWithCondition(method = "render(Lnet/minecraft/entity/PrimedTntEntity;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;enablePolygonOffset()V"))
	private boolean axolotlclient$revertTntPolygonOffset2() {
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.tntZFighting.get();
	}

	@WrapWithCondition(method = "render(Lnet/minecraft/entity/PrimedTntEntity;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;polygonOffset(FF)V"))
	private boolean axolotlclient$revertTntPolygonOffset3(float factor, float units) {
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.tntZFighting.get();
	}
}
