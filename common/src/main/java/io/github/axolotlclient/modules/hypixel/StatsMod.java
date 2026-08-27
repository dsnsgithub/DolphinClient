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

package io.github.axolotlclient.modules.hypixel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.bridge.commands.AxoClientCmdSrcStack;
import io.github.axolotlclient.bridge.commands.Commands;
import io.github.axolotlclient.bridge.commands.PlayerArgument;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPrestige;
import lombok.Getter;

import static io.github.axolotlclient.bridge.commands.Commands.argument;
import static io.github.axolotlclient.bridge.util.AxoText.Color.*;
import static io.github.axolotlclient.bridge.util.AxoText.literal;
import static io.github.axolotlclient.bridge.util.AxoText.translatable;

public class StatsMod implements AbstractHypixelMod {
	private interface Handler {
		void accept(AxoClientCmdSrcStack ctx, String uuid, String username, PlayerData data);
	}

	private record Entry(String name, Handler handler) {
	}

	private static AxoText.Mutable statText(String key, Object... args) {
		return translatable(key, Arrays.stream(args).map(s -> {
			if (s instanceof Float f) {
				return literal(String.format("%.2f", f)).br$color(GREEN);
			} else {
				return literal(s.toString()).br$color(GREEN);
			}
		}).toArray());
	}

	private static AxoText buildBedwarsGameMode(String key, PlayerData.Bedwars.BedwarsGameData data) {
		final var text = statText(key);

		final var hover = literal("");
		hover.br$append(statText("playerstats.bedwars.kdr", data.kills(), data.deaths(), data.kdr()));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.bedwars.fkdr", data.finalKills(), data.finalDeaths(), data.fkdr()));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.bedwars.beds", data.bedsBroken(), data.bedsLost(), data.bblr()));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.bedwars.summary_short", data.wins(), data.losses(), data.wlr()));

		text.br$withStyle(s -> s
			.br$color(GOLD)
			.br$tooltip(hover)
		);

		return text;
	}

	private static AxoText buildBedwarsGameModesLine(PlayerData.Bedwars data) {
		final var text = literal("");

		text.br$append(buildBedwarsGameMode("playerstats.bedwars.solo", data.solo()));
		text.br$append(" | ");
		text.br$append(buildBedwarsGameMode("playerstats.bedwars.duos", data.doubles()));
		text.br$append(" | ");
		text.br$append(buildBedwarsGameMode("playerstats.bedwars.fours", data.fours()));
		text.br$append(" | ");
		text.br$append(buildBedwarsGameMode("playerstats.bedwars.core", data.core()));
		text.br$append(" | ");
		text.br$append(buildBedwarsGameMode("playerstats.bedwars.dreams", data.dreams()));

		return text;
	}

	private static AxoText buildSkywarsGameMode(String key, PlayerData.Skywars.GameData data) {
		final var text = statText(key);

		final var hover = literal("");

		hover.br$append(statText("playerstats.skywars.kdr", data.kills(), data.deaths(), data.kdr()));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.skywars.summary", data.wins(), data.losses(), data.wlr()));

		text.br$withStyle(s -> s
			.br$color(GOLD)
			.br$tooltip(hover)
		);

		return text;
	}

	private static AxoText buildSkywarsGameModesLine(PlayerData.Skywars data) {
		final var text = literal("");

		text.br$append(buildSkywarsGameMode("playerstats.skywars.solo", data.solo().normal()));
		text.br$append(" | ");
		text.br$append(buildSkywarsGameMode("playerstats.skywars.duos", data.team().normal()));
		text.br$append(" | ");
		text.br$append(buildSkywarsGameMode("playerstats.skywars.solo_insane", data.solo().insane()));
		text.br$append(" | ");
		text.br$append(buildSkywarsGameMode("playerstats.skywars.duos_insane", data.team().insane()));

		return text;
	}

	private static AxoText buildDuelsGameMode(Map.Entry<String, PlayerData.DuelsData.DuelsGameData> entry) {
		final var value = entry.getValue();
		final var hover = translatable("playerstats.duels.mode_title",
			translatable("playerstats.duels." + entry.getKey()).br$color(GOLD));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.duels.kdr", value.kills(), value.deaths(), value.kdr()));
		hover.br$append("\n");
		hover.br$append(statText("playerstats.duels.summary", value.wins(), value.losses(), value.wlr(),
			value.winstreak()));

		return translatable("playerstats.duels." + entry.getKey()).br$withStyle(s -> s
			.br$color(GOLD)
			.br$tooltip(hover)
		);
	}

	private static AxoText buildDuelsGameModesLine(PlayerData.DuelsData data) {
		final var text = literal("");
		boolean empty = true;
		for (Map.Entry<String, PlayerData.DuelsData.DuelsGameData> stringDuelsGameDataEntry :
			data.modes().entrySet()) {
			if (!empty) {
				text.br$append("\n");
			}
			AxoText buildDuelsGameMode = buildDuelsGameMode(stringDuelsGameDataEntry);
			text.br$append(literal("» ").br$color(RED));
			text.br$append(buildDuelsGameMode);
			empty = false;
		}
		return text;
	}

	private static final List<Entry> HANDLERS = List.of(
		new Entry("bedwars", (c, uuid, username, data) -> {
			final var allStats = data.bedwars().all();
			List.of(
				translatable("playerstats.bedwars.title", data.formattedName(),
					BedwarsPrestige.format(data.bedwars().level())),
				statText("playerstats.bedwars.kdr", allStats.kills(), allStats.deaths(), allStats.kdr()),
				statText("playerstats.bedwars.fkdr", allStats.finalKills(), allStats.finalDeaths(), allStats.fkdr()),
				statText("playerstats.bedwars.beds", allStats.bedsBroken(), allStats.bedsLost(), allStats.bblr()),
				statText("playerstats.bedwars.summary", allStats.wins(), allStats.losses(), allStats.wlr(),
					allStats.winstreak()),
				buildBedwarsGameModesLine(data.bedwars())
			).forEach(c::br$sendFeedback);
		}),
		new Entry("skywars", (c, uuid, username, data) -> {
			final var allStats = data.skywars().all();
			List.of(
				translatable("playerstats.skywars.title", data.formattedName(), data.skywars().level()),
				statText("playerstats.skywars.kdr", allStats.kills(), allStats.deaths(), allStats.kdr()),
				statText("playerstats.skywars.summary", allStats.wins(), allStats.losses(), allStats.wlr()),
				buildSkywarsGameModesLine(data.skywars())
			).forEach(c::br$sendFeedback);
		}),
		new Entry("duels", (c, uuid, username, data) ->
			List.of(
				translatable("playerstats.duels.title", data.formattedName()),
				buildDuelsGameModesLine(data.duels())
			).forEach(c::br$sendFeedback))
	);

	@Getter
	private static final StatsMod instance = new StatsMod();

	private void register(Commands commands) {
		final var command = Commands.literal("playerstats");

		for (Entry handler : HANDLERS) {
			command.then(Commands.literal(handler.name()).then(argument("player", PlayerArgument.player()).executes(c -> {
				c.getSource().br$sendError(translatable("playerstats.error.api_disabled").br$color(RED));
				return -1;
			})));
		}

		final var node = commands.register(command);
		commands.register(Commands.literal("pstats").redirect(node));
	}

	@Override
	public void init() {
		Events.COMMAND_REGISTER.register(this::register);
	}

	@Override
	public OptionCategory getCategory() {
		return null;
	}
}
