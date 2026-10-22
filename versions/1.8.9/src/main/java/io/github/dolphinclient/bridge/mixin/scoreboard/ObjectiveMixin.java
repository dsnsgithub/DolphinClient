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

package io.github.dolphinclient.bridge.mixin.scoreboard;

import io.github.dolphinclient.bridge.scores.AxoObjective;
import io.github.dolphinclient.bridge.scores.AxoScoreboard;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ScoreboardObjective.class)
public abstract class ObjectiveMixin implements AxoObjective {

	@Shadow
	public abstract Scoreboard getScoreboard();

	@Shadow
	public abstract String getDisplayName();

	@Override
	public AxoScoreboard br$getScoreboard() {
		return getScoreboard();
	}

	@Override
	public String br$getDisplayName() {
		return getDisplayName();
	}
}
