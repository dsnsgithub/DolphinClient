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
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.client.render.model.entity.PlayerModel;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel {

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
	private boolean axolotlclient$disableSneakTranslation(Entity instance, Operation<Boolean> original) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.thirdPersonSneaking.get()) {
			/* we need to remove the sneaking offset since we will be using our own */
			return false;
		}
		return original.call(instance);
	}

	@WrapWithCondition(method = "setupAnimation", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/render/model/ModelPart;y:F"), require = 0)
	private boolean axolotlclient$disableSneakCapeTranslations(ModelPart instance, float value) {
		/* this secretly changes the cape's y position. we need to remove it */
		/* optifine already removes it and places its own sneak translation in CapeLayer */
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.thirdPersonSneaking.get();
	}

	@ModifyExpressionValue(method = "translateRightArm", at = @At(value = "CONSTANT", args = "floatValue=1.0F"))
	private float axolotlclient$fixAlexArmOffset(float original) {
		/* this modification isn't exactly related to 1.7 visuals, but it's close enough that i should include it :P */
		return original / (!OldAnimationsConfig.isEnabled() || OldAnimationsConfig.instance.disableAlexModel.get() ? 1.0F : 2.0F);
	}
}
