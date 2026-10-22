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
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

	@ModifyExpressionValue(method = "handleAddExperienceOrb", at = @At(value = "CONSTANT", args = "doubleValue=32"))
	private double axolotlclient$oldOrbRendering(double original) {
		return original / (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.xpOrbPosition.get() ?
			/* MC-4167 and MC-12013 yall suck */
			32.0D : 1.0D /* renders the xp orbs similar to 1.7 by oddly offsetting them */
		);
	}

	@ModifyExpressionValue(method = "handleEntityPickup", at = @At(value = "CONSTANT", args = "floatValue=0.5"))
	private float axolotlclient$oldItemPickup(float original) {
		/* taken from 1.7 */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldItemPickup.get() ? -0.5F : original;
	}
}
