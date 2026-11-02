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

package io.github.dolphinclient.bridge.mixin.world;

import java.util.Collections;
import java.util.List;

import io.github.dolphinclient.bridge.entity.AxoPlayer;
import io.github.dolphinclient.bridge.math.Vec3;
import io.github.dolphinclient.bridge.world.AxoWorld;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.NetherDimension;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldMixin implements AxoWorld {
	@Shadow
	public abstract long getTimeOfDay();

	@Shadow
	@Final
	public List<PlayerEntity> players;

	@Shadow
	public abstract Biome getBiome(BlockPos blockPos);

	@Shadow
	@Final
	public Dimension dimension;

	@Override
	public long br$getTimeOfDay() {
		return getTimeOfDay();
	}

	@Override
	public List<? extends AxoPlayer> br$getPlayers() {
		return Collections.unmodifiableList(this.players);
	}

	@Override
	public String br$getBiomeName(Vec3 pos) {
		return getBiome(new BlockPos(pos.x(), pos.y(), pos.z())).name;
	}

	@Override
	public boolean br$isOverworld() {
		return dimension instanceof OverworldDimension;
	}

	@Override
	public boolean br$isNether() {
		return dimension instanceof NetherDimension;
	}
}
