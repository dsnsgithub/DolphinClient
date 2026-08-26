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
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.ducks.IClientPlayerInteractionManager;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {

	@Shadow
	private boolean isMiningBlock;

	@Shadow
	private float miningProgress;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private BlockPos target;

	@ModifyExpressionValue(method = "stopMiningBlock", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/ClientPlayerInteractionManager;isMiningBlock:Z"))
	private boolean axolotlclient$allowStateReset(boolean original) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.miningProgressResetLogic.get()) {
			/* in 1.8, mining progress ONLY resets when the mining state is true. */
			/* this may sound logical, however, it's entirely possible for there */
			/* to be mining progress while the mining state is false in 1.8. */
			return true;
		}
		return original;
	}

	@WrapWithCondition(method = "stopMiningBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/handler/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	private boolean axolotlclient$sendIfMiningBlock(ClientPlayNetworkHandler instance, Packet<?> packet) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.miningProgressResetLogic.get()) {
			/* the mining abort packet should only be sent if the player is genuinely in the mining state like in 1.7 */
			return isMiningBlock;
		}
		return true;
	}

	@Override
	public void axolotlclient$fakeStopMiningBlock() {
		/* this is literally the stopMiningBlock method but with the packet removed */
		/* mining will stop client side but not server side. not much i can do about this if i want to keep it vanilla */
		/* this will not flag Grim's MultiPacketB check anymore */
		isMiningBlock = false;
		miningProgress = 0.0F;
		minecraft.world.updateBlockMiningProgress(minecraft.player.getNetworkId(), target, -1);
	}
}
