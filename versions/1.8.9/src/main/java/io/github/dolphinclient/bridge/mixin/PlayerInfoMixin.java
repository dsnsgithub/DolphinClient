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

package io.github.dolphinclient.bridge.mixin;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import io.github.dolphinclient.bridge.AxoPlayerListEntry;
import net.minecraft.client.network.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin implements AxoPlayerListEntry {
	@Shadow
	@Final
	private GameProfile profile;

	@Shadow
	public abstract int getPing();

	@Override
	public String br$getName() {
		return profile.getName();
	}

	@Override
	public UUID br$getId() {
		return profile.getId();
	}

	@Override
	public int br$getPing() {
		return getPing();
	}
}
