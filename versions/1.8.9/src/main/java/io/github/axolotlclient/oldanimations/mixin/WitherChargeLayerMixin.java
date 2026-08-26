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

import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.render.entity.layer.WitherChargeLayer;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WitherChargeLayer.class)
public class WitherChargeLayerMixin {

	@Unique
	private static final Identifier OLD_WITHER_CHARGE_LOCATION = new Identifier("textures/entity/wither/wither_invulnerable.png");

	@ModifyArg(method = "render(Lnet/minecraft/entity/living/mob/monster/boss/WitherEntity;FFFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/WitherRenderer;bindTexture(Lnet/minecraft/resource/Identifier;)V"))
	private Identifier axolotlclient$useIncorrectWitherTexture(Identifier par1) {
		/* MC-19702 */
		/* yeap. the wrong texture was being used lol */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.witherArmorTexture.get() ? OLD_WITHER_CHARGE_LOCATION : par1;
	}
}
