/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.axolotlclient.test.mixin;

import io.github.axolotlclient.oldanimations.GlintRenderChecks;

import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.resource.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class GlintPassCounterMixin {
	@Inject(method = "render(Lnet/minecraft/client/resource/model/BakedModel;I)V", at = @At("HEAD"))
	private void countGlintPass(BakedModel model, int color, CallbackInfo ci) {
		GlintRenderChecks.vanillaPasses++;
	}
}
