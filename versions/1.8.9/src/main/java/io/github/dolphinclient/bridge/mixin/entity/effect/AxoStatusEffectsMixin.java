/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.bridge.mixin.entity.effect;

import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffect;
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffects;
import net.minecraft.entity.living.effect.StatusEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AxoStatusEffects.class, remap = false)
public abstract class AxoStatusEffectsMixin {
	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect JUMP_BOOST;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect SPEED;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect HASTE;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect REGEN;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect BLINDNESS;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect MINING_FATIGUE;

	@Mutable
	@Shadow
	@Final
	public static AxoStatusEffect HEALTH_BOOST;

	@Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
	private static void setStaticValues(CallbackInfo info) {
		REGEN = StatusEffect.REGENERATION;
		JUMP_BOOST = StatusEffect.JUMP_BOOST;
		SPEED = StatusEffect.SPEED;
		HASTE = StatusEffect.HASTE;
		BLINDNESS = StatusEffect.BLINDNESS;
		MINING_FATIGUE = StatusEffect.MINING_FATIGUE;
		HEALTH_BOOST = StatusEffect.HEALTH_BOOST;

		info.cancel();
	}
}
