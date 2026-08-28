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
import io.github.axolotlclient.oldanimations.util.PlayerUtil;
import io.github.axolotlclient.oldanimations.util.ducks.IClientPlayerInteractionManager;
import net.minecraft.block.Block;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.client.sound.system.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Minecraft.class, priority = 2050 /* priority needed for custom window title */)
public abstract class MinecraftMixin {

	@Shadow
	public LocalClientPlayerEntity player;

	@Shadow
	public HitResult crosshairTarget;

	@Shadow
	public ParticleManager particleManager;

	@Shadow
	private int attackCooldown;

	@Shadow
	public ClientWorld world;

	@Shadow
	public GameOptions options;

	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	public abstract SoundManager getSoundManager();

	@Inject(method = "handleMouseDown", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/Minecraft;attackCooldown:I"))
	private void axolotlclient$oldMiningBehavior(boolean holdingAttack, CallbackInfo ci) {
		/* this was a massive pain in the butt to figure out */
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}

		/* the conditions used in 1.7/1.8 */
		boolean hasIntentToMine = holdingAttack && crosshairTarget != null && crosshairTarget.type == HitResult.Type.BLOCK;

		if (OldAnimationsConfig.instance.oldSwingVisual.get() && attackCooldown > 0 && hasIntentToMine) {
			/* take a look at the method below for more information on this */
			axolotlclient$fakeMineEffects(crosshairTarget.getPos(), OldAnimationsConfig.instance.oldSwingVisualParticles.get());
		}

		if (attackCooldown <= 0 && player.hasItemInUse()) {
			if (OldAnimationsConfig.instance.useAndMine.get() && hasIntentToMine) {
				axolotlclient$fakeMineEffects(crosshairTarget.getPos(), OldAnimationsConfig.instance.useAndMineParticles.get());
			} else if (OldAnimationsConfig.instance.miningProgressResetLogic.get()) {
				/* in 1.7, if the player is using an item but not actively holding lmb down, then the mining progress will be reset. */
				((IClientPlayerInteractionManager) interactionManager).axolotlclient$fakeStopMiningBlock();
			}
		}
	}

	@Inject(method = "doAttack", at = @At("HEAD" /* this is the best injection point for this to be 1:1 with 1.7 */))
	private void axolotlclient$oldSwingVisual(CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.oldSwingVisual.get()) {
			return;
		}
		/* in 1.7, the 10 tick cooldown almost never occurs */
		/* it requires the client to think the block being looked at is an air block which mostly occurs in multiplayer */
		/* more precisely, the only blocks this really occurred on were instantly breakable ones like plants and foliage */
		/* you can use a command block in singleplayer to also recreate the cooldown if you wanted to */

		/* in 1.8, the 10 tick cooldown is enacted upon every missed hit aka any hit that isn't aimed at an entity or a block */
		/* this sounds bad, but this actually isn't much of a problem until we take double clicks or clicking 20 cps into account */
		/* in both 1.7 and 1.8, on every tick, the attack cooldown gets reset to 0 if the left mouse button isn't held down */
		/* that means that, in 1.8, in order to have a proper unregistered clicks, you would need to click fast enough such that */
		/* the attack cooldown isn't synchronized because you clicked twice within one tick. there are 20 ticks in 1 second. */
		/* Moulberry has a great video explanation on this https://www.youtube.com/watch?v=j1pblMl9ylY */
		/* bottom line is, 1.7 and 1.8 both have this attack cooldown field, but 1.8's refactors subtly changed the behavior */
		/* we can visually alleviate this by just recreating and reverting to the 1.7 attack cooldown conditions */
		if (attackCooldown > 0 && crosshairTarget != null && interactionManager.hasAttackCooldown()) {
			if (crosshairTarget.type == HitResult.Type.BLOCK && world.isAir(crosshairTarget.getPos())) {
				/* this is the only legitimate place where the cooldown would apply in 1.7 */
				return;
			}
			/* plays a fake swing animation and particles to really sell the illusion */
			PlayerUtil.INSTANCE.fakeSwing(player);
			if (OldAnimationsConfig.instance.oldSwingVisualParticles.get() && crosshairTarget.type == HitResult.Type.ENTITY) {
				PlayerUtil.INSTANCE.fakeAttackEntity(player, crosshairTarget.entity);
			}
		}
	}

	@ModifyExpressionValue(method = "doUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ClientPlayerInteractionManager;isMiningBlock()Z"))
	private boolean axolotlclient$allowMiningCancel(boolean original) {
		/* MC-70359 */
		/* this will flag Grim's PackOrderI... but so do the other clients, so we should be as safe as them */
		/* contrary to popular belief, this feature existed in 1.8.0 and was removed in 1.8.1 */
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.allowMiningCancel.get()) {
			return false;
		}
		return original;
	}

//	@Inject(method = "reloadResources", at = @At(value = "HEAD"))
//	private void axolotlclient$refreshEntityRenderer(CallbackInfo ci) {
//		/* this hopefully won't cause any weird memory issues. i need a reliable way to refresh entity models */
//		/* injecting at head means the world renderer's reload call will properly update accordingly later on */
//		entityRenderDispatcher = new EntityRenderDispatcher(textureManager, itemRenderer);
//	}

	@Unique
	private float axolotlclient$fakeMiningSoundTimer = -1F;

	@Unique
	private void axolotlclient$fakeMineEffects(BlockPos blockPos, boolean showParticles) {
		if (!world.isAir(blockPos)) {
			Block block = world.getBlockState(blockPos).getBlock();

			if (OldAnimationsConfig.instance.useAndMineSound.get() && !interactionManager.getGameMode().isCreative()) {
				if (axolotlclient$fakeMiningSoundTimer < 0F) {
					axolotlclient$fakeMiningSoundTimer = ((ClientPlayerInteractionManagerAccessor) interactionManager).getMiningSoundTimer();
				}

				/* he he ha */
				if (axolotlclient$fakeMiningSoundTimer % 4.0F == 0.0F) {
					getSoundManager().play(
						new SimpleSoundInstance(
							new Identifier(block.sounds.getStepping()),
							(block.sounds.getVolume() + 1.0F) / 8.0F,
							block.sounds.getPitch() * 0.5F,
							blockPos.getX() + 0.5F,
							blockPos.getY() + 0.5F,
							blockPos.getZ() + 0.5F
						)
					);
				}

				axolotlclient$fakeMiningSoundTimer++;
			}

			if (showParticles) {
				/* this is vital for the fake mining illusion */
				particleManager.addBlockMiningParticles(blockPos, crosshairTarget.face);
			}
			/* plays a packet-less swing animation when the player is using an item and punching */
			PlayerUtil.INSTANCE.fakeSwing(player);
		}
	}
}
