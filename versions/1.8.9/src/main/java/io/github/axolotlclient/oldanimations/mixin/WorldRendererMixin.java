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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(WorldRenderer.class /* C_8398976 */)
public class WorldRendererMixin {

	@WrapOperation(
		method = "doEvent",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/util/math/BlockPos;Ljava/lang/String;FFZ)V"),
		slice = @Slice( /* once again, this is such a weird slice... thanks switch statements */
			from = @At(value = "CONSTANT", args = "stringValue=random.click", ordinal = 0),
			to = @At(value = "CONSTANT", args = "stringValue=game.potion.smash")
		)
	)
	private void axolotlclient$dispenserSounds(ClientWorld instance, BlockPos pos, String sound, float volume, float pitch, boolean ignoreDistance, Operation<Void> original) {
		//todo: find bug report if any. it would be related to dispensers
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.dispenserClickSound.get()) {
			/* we need to remove the centering on these sounds */
			/* dispenser click and projectile sounds are affected */
			instance.playSound(pos.getX(), pos.getY(), pos.getZ(), sound, volume, pitch, ignoreDistance);
		} else {
			original.call(instance, pos, sound, volume, pitch, ignoreDistance);
		}
	}

//	@Inject(method = "reload(Lnet/minecraft/client/resource/manager/ResourceManager;)V", at = @At("HEAD"))
//	private void axolotlclient$refreshEntityRenderer(CallbackInfo ci) {
//		/* please work */
//		entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
//	}
}
