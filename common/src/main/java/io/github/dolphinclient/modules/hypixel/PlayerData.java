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

package io.github.dolphinclient.modules.hypixel;

import java.util.Map;

public record PlayerData(String name, Bedwars bedwars, Skywars skywars, DuelsData duels, String rank,
						 String rankFormatted, double level, int karma) {
	public String formattedName() {
		StringBuilder builder = new StringBuilder();
		builder.append(rankFormatted());
		if (rankFormatted().length() > 2) {
			builder.append(" ");
		}
		return builder.append(name()).append("§r").toString();
	}

	public record Bedwars(int level, GameData all, CombinedGameData core, GameData solo, GameData doubles,
						  GameData trios, GameData fours, GameData fourVFour, CombinedGameData dreams, GameData castle,
						  GameData doublesLucky, GameData foursLucky, GameData doublesUltimate, GameData foursUltimate,
						  GameData doublesArmed, GameData foursArmed, GameData doublesRush, GameData foursRush,
						  GameData doublesSwap, GameData foursSwap) {

		public record GameData(int kills, int deaths, int wins, int losses, int winstreak, int finalKills,
							   int finalDeaths, int bedsBroken, int bedsLost) implements BedwarsGameData {
		}

		public record CombinedGameData(int kills, int deaths, int wins, int losses, int finalKills, int finalDeaths,
									   int bedsBroken, int bedsLost) implements BedwarsGameData {
		}

		public interface BedwarsGameData extends WLR, KDR {
			int bedsBroken();

			int bedsLost();

			int finalDeaths();

			int finalKills();

			default float fkdr() {
				return (float) finalKills() / finalDeaths();
			}

			default float bblr() {
				return (float) bedsBroken() / bedsLost();
			}
		}
	}

	public record Skywars(String level, int exp, GameData all, GameData core, ModeData solo, ModeData team,
						  MegaModeData mega, GameData ranked, int winstreak) {

		public record ModeData(GameData normal, GameData insane) {
		}

		public record MegaModeData(GameData normal, GameData doubles) {
		}

		public record GameData(int kills, int deaths, int wins, int losses) implements KDR, WLR {
		}
	}

	public record DuelsData(Map<String, DuelsGameData> modes) {
		public record DuelsGameData(int kills, int deaths, int wins, int losses, int winstreak) implements KDR, WLR {
		}
	}

	public interface KDR {
		int kills();

		int deaths();

		default float kdr() {
			return (float) kills() / deaths();
		}
	}

	public interface WLR {
		int wins();

		int losses();

		default float wlr() {
			return (float) wins() / losses();
		}
	}
}
