/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import io.github.dolphinclient.modules.hypixel.HypixelAbstractionLayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author DarkKronicle
 */

@Getter
@AllArgsConstructor
public class BedwarsPlayerStats {

	private final int losses;
	private final int wins;
	private final int winstreak;
	private final int stars;
	private int finalKills;
	private int finalDeaths;
	private int bedsBroken;
	private int bedsLost;
	private int deaths;
	private int kills;
	private int gameFinalKills;
	private int gameFinalDeaths;
	private int gameBedsBroken;
	private int gameDeaths;
	private int gameKills;

	public static BedwarsPlayerStats generateFake(String name) {
		long seed = 0;
		for (int i = 0; i < name.length(); i++) {
			seed = (seed << 2) + name.getBytes(StandardCharsets.UTF_8)[i];
		}
		Random random = new Random(seed);
		int star = (int) getGaussian(random, 150, 30);
		double fkdr = Math.min(getGaussian(random, 1.3F, 0.5F), 0.6F);
		double bblr = (fkdr * 8) / getGaussian(random, 10, 2);
		int wins = (int) (star * (fkdr * 4) * getFloat(random, 0.95F, 1.05F));
		int losses = (int) (wins * (2 / fkdr) * getFloat(random, 0.95F, 1.05F));
		int beds = (int) (bblr * losses);
		int finalDeaths = (int) (losses * getFloat(random, 1F, 1.03F));
		int deaths = (int) (finalDeaths * getFloat(random, 8, 20));
		int finalKills = (int) (deaths * fkdr);
		int kills = (int) (finalKills * getFloat(random, 1, 2));

		return new BedwarsPlayerStats(losses, wins, 0, star, finalKills, finalDeaths, beds, losses, deaths,
			kills, 0, 0, 0, 0, 0);
	}

	private static double getGaussian(Random random, float mean, float deviation) {
		return Math.max(Math.min(random.nextGaussian() * deviation + mean, mean - deviation * 3), mean + deviation * 3);
	}

	private static float getFloat(Random random, float origin, float bound) {
		return random.nextFloat() * (bound - origin) + origin;
	}

	public static CompletableFuture<Optional<BedwarsPlayerStats>> fromAPIAsync(String uuid) {
		return HypixelAbstractionLayer.getInstance().getPlayerDataApi().getAsync(uuid)
			.thenApply(r -> r.map(data -> new BedwarsPlayerStats(
				data.bedwars().all().losses(),
				data.bedwars().all().wins(),
				data.bedwars().all().winstreak(),
				data.bedwars().level(),
				data.bedwars().all().finalKills(),
				data.bedwars().all().finalDeaths(),
				data.bedwars().all().bedsBroken(),
				data.bedwars().all().bedsLost(),
				data.bedwars().all().deaths(),
				data.bedwars().all().kills(),
				0, 0, 0, 0, 0
			)));
	}

	public static CompletableFuture<BedwarsPlayerStats> fromAPIOrFakeAsync(String uuid) {
		return fromAPIAsync(uuid).thenApply(x -> x.orElse(generateFake(uuid)));
	}

	public void addDeath() {
		deaths++;
		gameDeaths++;
	}

	public void addFinalDeath() {
		finalDeaths++;
		gameFinalDeaths++;
	}

	public void addKill() {
		kills++;
		gameKills++;
	}

	public void addFinalKill() {
		finalKills++;
		gameFinalKills++;
	}

	public void addBed() {
		bedsBroken++;
		gameBedsBroken++;
	}

	public float getFKDR() {
		return (float) finalKills / finalDeaths;
	}

	public float getKDR() {
		return (float) kills / deaths;
	}

	public float getBBLR() {
		return (float) bedsBroken / losses;
	}

}
