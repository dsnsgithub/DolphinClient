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
import net.minecraft.client.render.entity.GiantRenderer;
import net.minecraft.client.render.entity.layer.EntityRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GiantRenderer.class)
public class GiantRendererMixin {

	@WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/GiantRenderer;addLayer(Lnet/minecraft/client/render/entity/layer/EntityRenderLayer;)Z"))
	private boolean axolotlclient$removeGiantLayers(GiantRenderer instance, EntityRenderLayer<?> entityRenderLayer) {
		/* nobody will even notice this */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.removeGiantLayers.get();
	}
}
