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

import java.util.Collection;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.dolphinclient.modules.particles.Particles;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.entity.particle.Particle;
import net.minecraft.entity.particle.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	@Unique
	private ParticleType cachedType;

	@Inject(method = "addParticle(IDDDDDD[I)Lnet/minecraft/client/entity/particle/Particle;", at = @At(value = "HEAD"), cancellable = true)
	public void dolphinclient$afterCreation(int i, double d, double e, double f, double g, double h, double j, int[] is,
	                                        CallbackInfoReturnable<Particle> cir) {
		cachedType = ParticleType.byId(i);

		if (!Particles.getInstance().getShowParticle(cachedType)) {
			cir.setReturnValue(null);
			cir.cancel();
		}
	}

	@Inject(method = "add", at = @At(value = "HEAD"))
	public void dolphinclient$afterCreation(Particle particle, CallbackInfo ci) {
		if (cachedType != null) {
			Particles.getInstance().particleMap.put(particle, cachedType);
			cachedType = null;
		}
	}

	@Inject(method = "add", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"))
	public void dolphinclient$removeParticlesWhenTooMany(Particle particle, CallbackInfo ci) {
		Particles.getInstance().particleMap.remove(particle);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
	public boolean dolphinclient$removeEmitterParticlesWhenRemoved(List<Particle> instance, Collection<Particle> objects, Operation<Boolean> original) {
		return dolphinclient$removeParticlesWhenRemoved(instance, objects, original);
	}

	@WrapOperation(method = "tickParticles(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
	public boolean dolphinclient$removeParticlesWhenRemoved(List<Particle> instance, Collection<Particle> objects, Operation<Boolean> original) {
		objects.forEach(particle -> Particles.getInstance().particleMap.remove(particle));

		return original.call(instance, objects);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
	public <E> E dolphinclient$applyOptions(List<E> instance, int i, Operation<E> original) {
		E particle = original.call(instance, i);
		if (Particles.getInstance().particleMap.containsKey(((Particle) particle))) {
			Particles.getInstance().applyOptions((Particle) particle);
		}
		return particle;
	}
}
