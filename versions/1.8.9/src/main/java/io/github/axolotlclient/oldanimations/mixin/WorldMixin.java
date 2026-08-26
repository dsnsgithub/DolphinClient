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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.particle.ParticleType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(World.class)
public class WorldMixin {

	@Shadow
	@Final
	public List<Entity> entities;

	@Inject(method = "addParticle(IZDDDDDD[I)V", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$disableServerSideSplashParticles(int type, boolean ignoreDistance, double x, double y, double z, double velocityX, double velocityY, double velocityZ, int[] parameters, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.disableItemEntitySplashParticles.get() &&
			(type == ParticleType.WATER_BUBBLE.getId() || type == ParticleType.WATER_SPLASH.getId())) {
			/* this didnt come with a bug report or anything but its very much related to MC-3884 */
			for (Entity entity : entities) {
				if (entity instanceof ItemEntity) {
					/* i dont know if this is the best way to do this */
					//todo: gauge performance of this. also change injection point some day
					ci.cancel();
				}
			}
		}
	}
}
