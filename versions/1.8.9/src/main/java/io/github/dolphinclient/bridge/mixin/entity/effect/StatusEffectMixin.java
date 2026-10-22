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
import io.github.dolphinclient.bridge.impl.AxoSpriteImpl;
import io.github.dolphinclient.bridge.render.AxoSprite;
import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin implements AxoStatusEffect {
	@Unique
	private static final Identifier INVENTORY_TEXTURE = new Identifier("textures/gui/container/inventory.png");

	@Shadow
	public abstract int getIconIndex();

	@Shadow
	public abstract String getTranslationKey();

	@Override
	public AxoSprite br$getSprite() {
		int iconIdx = getIconIndex();
		return new AxoSpriteImpl.Simple(INVENTORY_TEXTURE, iconIdx % 8 * 18, 198 + iconIdx / 8 * 18, 256, 256);
	}

	@Override
	public AxoText br$getDisplayName() {
		return AxoText.translatable(getTranslationKey());
	}
}
