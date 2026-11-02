/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
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

package io.github.dolphinclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.modules.particles.Particles;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.particle.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

	@Shadow
	public abstract void addCritParticles(Entity entity);

	@Shadow
	public abstract void addEnchantedCritParticles(Entity entity);

	public PlayerEntityMixin(World world) {
		super(world);
	}

	@Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/BlockState;"), cancellable = true)
	private void removeErrorOnAirBlock(BlockPos blockPos, CallbackInfoReturnable<PlayerEntity.SleepAllowedStatus> cir) {
		if (world.getBlockState(blockPos).getBlock().is(Blocks.AIR)) {
			cir.setReturnValue(PlayerEntity.SleepAllowedStatus.OTHER_PROBLEM);
		}
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;takeDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void onAttack(Entity target, CallbackInfo ci, @Local(ordinal = 0) boolean crit, @Local(ordinal = 1) float enchantedDamage) {
		if (Minecraft.getInstance().isOnSameThread()) {
			Events.PLAYER_ATTACK.invoker().accept((PlayerEntity) (Object) this, target);
			if (Particles.getInstance().getAlwaysOn(ParticleType.CRIT) && !crit) {
				addCritParticles(target);
			}
			if (Particles.getInstance().getAlwaysOn(ParticleType.CRIT_MAGIC) && enchantedDamage == 0) {
				addEnchantedCritParticles(target);
			}
		}
	}

	@Inject(method = "takeDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;takeDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void onDamage(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
		Events.PLAYER_HURT.invoker().accept((PlayerEntity) (Object) this, damageSource.getAttacker());
	}
}
