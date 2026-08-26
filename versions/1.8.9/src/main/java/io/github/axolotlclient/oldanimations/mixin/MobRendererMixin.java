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
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.MobUtil;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.entity.living.mob.MobEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobRenderer.class)
public class MobRendererMixin {

	@ModifyExpressionValue(method = "renderRiders", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/living/mob/MobEntity;width:F"))
	private float axolotlclient$oldMobWidth(float original, @Local(argsOnly = true) MobEntity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mobSizeDimensions.get()) {
			/* since entity hitbox sizes are slightly different in 1.7, this is the closest we can get to emulating that */
			/* without altering combat */
			original = MobUtil.INSTANCE.oldMobWidth(entity, original);
		}
		return original;
	}

	@ModifyExpressionValue(method = "renderRiders", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/living/mob/MobEntity;height:F"))
	private float axolotlclient$oldMobHeight(float original, @Local(argsOnly = true) MobEntity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.mobSizeDimensions.get()) {
			original = MobUtil.INSTANCE.oldMobHeight(entity, original);
		}
		return original;
	}
}
