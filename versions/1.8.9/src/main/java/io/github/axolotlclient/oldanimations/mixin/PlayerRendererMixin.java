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
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.entity.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

	//TODO: Look into root cause
	@Inject(method = {"renderPlayerLeftHandModel", "renderRightHand"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/PlayerRenderer;setModelStatus(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;)V", shift = At.Shift.AFTER))
	private void legarity$fixVehicleArm(ClientPlayerEntity clientPlayerEntity, CallbackInfo ci, @Local PlayerModel playerModel) {
		/* fixes MC-1349 */
		/* this is probably the only one of the few bug fixes that im open to adding */
		if (OldAnimationsConfig.isEnabled()) {
			playerModel.riding = false;
		}
	}
}
