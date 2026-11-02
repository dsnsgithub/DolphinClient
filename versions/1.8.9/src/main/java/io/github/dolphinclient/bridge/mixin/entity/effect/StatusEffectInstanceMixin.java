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
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffectInstance;
import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin implements AxoStatusEffectInstance {
	@Shadow
	public abstract int getId();

	@Shadow
	private int amplifier;

	@Override
	public AxoStatusEffect br$getType() {
		return StatusEffect.BY_ID[getId()];
	}

	@Override
	public int br$getAmplifier() {
		return amplifier;
	}

	@Override
	public AxoText br$formatDuration() {
		return AxoText.literal(StatusEffect.getDurationString((StatusEffectInstance) (Object) this));
	}
}
