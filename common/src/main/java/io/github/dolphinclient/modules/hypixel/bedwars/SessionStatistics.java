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

package io.github.dolphinclient.modules.hypixel.bedwars;

import lombok.Getter;

public class SessionStatistics {
	@Getter
	int gamesPlayed;
	@Getter
	int wins, losses,
		kills, deaths, finalKills, finalDeaths,
		bedsBroken, bedsLost,
		winstreak;

	public void win() {
		wins++;
		winstreak += 1;
	}

	public void loose() {
		losses++;
		winstreak = 0;
	}

	public void gamePlayed() {
		gamesPlayed++;
	}

	public void addDeath(boolean finalDeath) {
		if (finalDeath) finalDeaths++;
		else deaths++;
	}

	public void addKill(boolean finalKill) {
		if (finalKill) finalKills++;
		else kills++;
	}

	public void addBrokenBed() {
		bedsBroken++;
	}

	public void bedLost() {
		bedsLost++;
	}

	public float getFkdr() {
		return (float) finalKills / finalDeaths;
	}

	public float getKdr() {
		return (float) kills / deaths;
	}
}
