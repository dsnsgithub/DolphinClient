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
import io.github.axolotlclient.oldanimations.util.PlayerUtil;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@ModifyArgs(method = "renderHitbox", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Box;<init>(DDDDDD)V"))
	private void axolotlclient$oldHitboxBehavior(Args args, @Local(argsOnly = true) Entity entity) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.thirdPersonSneaking.get() && PlayerUtil.INSTANCE.isSelf(entity)) {
			/* sneaking compatibility! */
			double eyeHeightOffset = PlayerUtil.INSTANCE.getEyeHeight() - 1.62F;
			args.set(1, (double) args.get(1) + eyeHeightOffset);
			args.set(4, (double) args.get(4) + eyeHeightOffset);
		}
	}
}
