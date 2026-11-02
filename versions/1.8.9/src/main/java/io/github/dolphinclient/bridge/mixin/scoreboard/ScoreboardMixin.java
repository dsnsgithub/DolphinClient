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

import java.util.Collection;

import io.github.dolphinclient.bridge.scores.AxoObjective;
import io.github.dolphinclient.bridge.scores.AxoScoreboard;
import io.github.dolphinclient.bridge.scores.AxoScoreboardScore;
import io.github.dolphinclient.bridge.scores.AxoTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.team.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements AxoScoreboard {

	@Shadow
	public abstract Team getTeamOfMember(String string);

	@Shadow
	public abstract Team getTeam(String string);

	@Shadow
	public abstract Collection<ScoreboardScore> getScores(ScoreboardObjective scoreboardObjective);

	@Override
	public Collection<? extends AxoScoreboardScore> br$getScores(AxoObjective objective) {
		return getScores((ScoreboardObjective) objective);
	}

	@Override
	public AxoTeam br$getTeamOfMember(String s) {
		return getTeamOfMember(s);
	}

	@Override
	public AxoTeam br$getTeam(String s) {
		return getTeam(s);
	}
}
