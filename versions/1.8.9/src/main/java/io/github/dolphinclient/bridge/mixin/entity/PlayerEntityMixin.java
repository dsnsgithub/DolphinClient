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

package io.github.dolphinclient.bridge.mixin.entity;

import com.mojang.authlib.GameProfile;
import io.github.dolphinclient.bridge.entity.AxoPlayer;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItems;
import io.github.dolphinclient.bridge.item.AxoPlayerInventory;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.living.player.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements AxoPlayer {
	@Shadow
	public PlayerInventory inventory;

	@Shadow
	public abstract GameProfile getGameProfile();

	@Shadow
	public int xpLevel;

	@Shadow
	public float xpProgress;

	@Override
	public AxoPlayerInventory br$getInventory() {
		return this.inventory;
	}

	@Override
	public @Nullable AxoItem br$getProjectileItem() {
		return AxoItems.ARROW;
	}

	@Override
	public String br$getName() {
		return getGameProfile().getName();
	}

	@Override
	public int br$getExperienceLevel() {
		return xpLevel;
	}

	@Override
	public float br$getExperienceProgress() {
		return xpProgress;
	}
}
