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
import io.github.axolotlclient.oldanimations.util.SoundUtil;
import net.minecraft.block.TrapdoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TrapdoorBlock.class)
public class TrapdoorBlockMixin {

	@ModifyArg(method = {"use", "neighborChanged"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;doEvent(Lnet/minecraft/entity/living/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V"), index = 1)
	private int axolotlclient$randomizeTrapdoorSound(int original) {
		/* MC-24778 */
		/* wrong door sounds must be played hehehe */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.randomizeDoorSound.get() ? SoundUtil.getDoorSound() : original;
	}
}
