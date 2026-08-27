/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.Platform;
import io.github.axolotlclient.bridge.entity.AxoPlayer;
import io.github.axolotlclient.bridge.events.types.ReceiveChatMessageEvent;
import io.github.axolotlclient.bridge.events.types.ScoreboardRenderEvent;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.scores.AxoScoreboard;
import io.github.axolotlclient.bridge.scores.AxoScoreboardScore;
import io.github.axolotlclient.bridge.scores.AxoTeam;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.BedwarsTeamUpgrades;
import io.github.axolotlclient.util.ClientColors;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * @author DarkKronicle
 */

// TODO: localize messages, maybe add regexes for text recognition to HypixelMessages?
//	Currently this only works if Hypixel's language is set to English
public class BedwarsGame {
	private final Map<String, BedwarsPlayer> players = new HashMap<>();
	private final Map<UUID, BedwarsPlayer> playersById = new HashMap<>();
	@Getter
	private final Map<BedwarsTeam, List<AxoPlayerListEntry>> playersByTeam = new HashMap<>();
	private final AxoMinecraftClient mc;
	private final BedwarsMod mod;
	@Getter
	private final BedwarsTeamUpgrades upgrades = new BedwarsTeamUpgrades();
	private BedwarsTeam won = null;
	private int wonTick = -1;
	private int seconds = 0;
	@Getter
	private AxoText topBarText = AxoText.literal("");
	@Getter
	private AxoText bottomBarText = AxoText.literal("");
	private BedwarsPlayer me = null;
	@Getter
	private boolean started = false;
	private BedwarsPlayer lastKill;
	private BedwarsPlayer lastKiller;

	public BedwarsGame(BedwarsMod mod) {
		mc = AxoMinecraftClient.getInstance();
		this.mod = mod;
	}

	public void onStart() {
		players.clear();
		playersById.clear();
		playersByTeam.clear();
		for (AxoPlayerListEntry player : mc.br$getOnlinePlayers()) {
			String name = Platform.getTabNameFor(player).replaceAll("§.", "");
			if (name.charAt(1) != ' ') {
				continue;
			}
			BedwarsTeam team = BedwarsTeam.fromPrefix(name.charAt(0)).orElse(null);
			if (team == null) {
				continue;
			}
			playersByTeam.compute(team, (t, entries) -> {
				if (entries == null) {
					List<AxoPlayerListEntry> players = new ArrayList<>();
					players.add(player);
					return players;
				}
				entries.add(player);
				return entries;
			});
		}
		for (Map.Entry<BedwarsTeam, List<AxoPlayerListEntry>> teamPlayerList : playersByTeam.entrySet()) {
			teamPlayerList.getValue().sort(Comparator.comparing(AxoPlayerListEntry::br$getName));
			List<AxoPlayerListEntry> value = teamPlayerList.getValue();
			for (int i = 0; i < value.size(); i++) {
				AxoPlayerListEntry e = value.get(i);
				BedwarsPlayer p = new BedwarsPlayer(teamPlayerList.getKey(), e, i + 1);
				if (mc.br$getPlayer().br$getUuid().equals(e.br$getId())) {
					me = p;
				}
				players.put(e.br$getName(), p);
				playersById.put(e.br$getId(), p);
			}
		}
		this.started = true;

		BedwarsMod.GAME_START_EVENT.invoker().accept(this);
	}

	private AxoText calculateTopBarText() {
		String topBar = getFormattedTime();
		if (me.getStats() != null) {
			topBar += "\n" +
				"K: " + me.getStats().getGameKills() +
				" D: " + me.getStats().getGameDeaths() +
				" B: " + me.getStats().getGameBedsBroken();
		}
		return AxoText.literal(topBar);
	}

	private AxoText calculateBottomBarText() {
		return AxoText.literal("")
			.br$append("Last Kill: ", AxoText.Color.AQUA)
			.br$append(lastKill == null ? "N/A" : lastKill.getColoredName())
			.br$append(" Last Killed By: ", AxoText.Color.AQUA)
			.br$append(lastKiller == null ? "N/A" : lastKiller.getColoredName());
	}

	public String getFormattedTime() {
		int minute = seconds / 60;
		int second = seconds % 60;
		String time = minute + ":";
		if (second < 10) {
			time += "0" + second;
		} else {
			time += second;
		}
		return time;
	}

	/*
	 * make sure to call `refreshPlayerNumbers` after calling this method!
	 */
	private Optional<BedwarsPlayer> addPlayer(AxoPlayerListEntry entry) {
		String listName = Platform.getTabNameFor(entry).replaceAll("§.", "");
		if (listName.charAt(1) != ' ') {
			return Optional.empty();
		}
		BedwarsTeam team = BedwarsTeam.fromPrefix(listName.charAt(0)).orElse(null);
		if (team == null) {
			return Optional.empty();
		}
		playersByTeam.compute(team, (t, entries) -> {
			if (entries == null) {
				List<AxoPlayerListEntry> players = new ArrayList<>();
				players.add(entry);
				return players;
			}
			entries.add(entry);
			return entries;
		});
		var teamPlayers = playersByTeam.get(team);
		teamPlayers.sort(Comparator.comparing(AxoPlayerListEntry::br$getName));
		return Optional.of(new BedwarsPlayer(team, entry, 0));
	}

	private void refreshPlayerNumbers(BedwarsTeam team, Collection<BedwarsPlayer> players) {
		var teamPlayers = playersByTeam.get(team);
		for (var p : players) {
			if (p.getTeam().equals(team)) {
				p.setNumber(teamPlayers.indexOf(p.getProfile()) + 1);
			}
		}
	}

	public Optional<BedwarsPlayer> getPlayer(UUID uuid) {
		return Optional.ofNullable(playersById.computeIfAbsent(uuid, unused -> mc.br$getOnlinePlayer(uuid).flatMap(entry -> {
			var p = addPlayer(entry);
			p.ifPresent(bedwarsPlayer -> {
				players.put(bedwarsPlayer.getProfile().br$getName(), bedwarsPlayer);
				refreshPlayerNumbers(bedwarsPlayer.getTeam(), players.values());
				BedwarsMod.PLAYER_ADD.invoker().accept(bedwarsPlayer);
			});
			return p;
		}).orElse(null)));
	}

	public Optional<BedwarsPlayer> getPlayer(String name) {
		return Optional.ofNullable(players.computeIfAbsent(name, unused ->
			mc.br$getOnlinePlayer(name).flatMap(entry -> {
				var p = addPlayer(entry);
				p.ifPresent(bedwarsPlayer -> {
					playersById.put(bedwarsPlayer.getProfile().br$getId(), bedwarsPlayer);
					refreshPlayerNumbers(bedwarsPlayer.getTeam(), playersById.values());
					BedwarsMod.PLAYER_ADD.invoker().accept(bedwarsPlayer);
				});
				return p;
			}).orElse(null)));
	}

	private void debug(String message) {
		AxoMinecraftClient.getInstance().br$sendToClient(AxoText.literal("§b§lINFO:§8 " + message));
	}

	private void died(ReceiveChatMessageEvent event, BedwarsPlayer player, @Nullable BedwarsPlayer killer,
					  BedwarsDeathType type, boolean finalDeath) {
		player.died();
		if (killer != null) {
			killer.killed(finalDeath);
		}
		if (mod.overrideMessages.get()) {
			event.setNewMessage(AxoText.literal(formatDeath(player, killer, type, finalDeath)));
		}
		if (me.equals(killer)) {
			lastKill = player;
			mod.getSessionStats().addKill(finalDeath);
		} else if (me.equals(player)) {
			lastKiller = killer;
			mod.getSessionStats().addDeath(finalDeath);
		}
	}

	private String formatDisconnect(BedwarsPlayer disconnected) {
		String playerFormatted = getPlayerFormatted(disconnected);
		return playerFormatted + " §7§o/disconnected/";
	}

	private String formatReconnect(BedwarsPlayer reconnected) {
		String playerFormatted = getPlayerFormatted(reconnected);
		return playerFormatted + " §7§o/reconnected/";
	}

	private String formatEliminated(BedwarsTeam team) {
		StringBuilder message = new StringBuilder(
			"§6§l§oTEAM ELIMINATED §8§l> " + team.getColorSection() + team.getName() + " Team §7/eliminated/ ");
		for (BedwarsPlayer p : players.values().stream()
			.filter(b -> b.getTeam() == team)
			.sorted(Comparator.comparingInt(BedwarsPlayer::getNumber))
			.toList()) {
			BedwarsPlayerStats stats = p.getStats();
			if (stats == null) {
				continue;
			}
			message.append("\n")
				.append("§b")
				.append(stats.getStars())
				.append(" ")
				.append(p.getColoredName())
				.append("§7 Beds: §f")
				.append(stats.getBedsBroken())
				.append("§7 Finals: §f")
				.append(stats.getFinalKills())
				.append("§7 FKDR: §f")
				.append(String.format("%.2f", stats.getFKDR()))
				.append("§7 BBLR: §f")
				.append(String.format("%.2f", stats.getBBLR()));
		}
		return message.toString();
	}

	private String formatBed(BedwarsTeam team, BedwarsPlayer breaker) {
		String playerFormatted = getPlayerFormatted(breaker);
		return "§6§l§oBED BROKEN §8§l> " + team.getColorSection() + team.getName() + " Bed §7/broken/ " + playerFormatted +
			(breaker.getStats() == null || breaker.getTeam() != me.getTeam() ? "" :
				" §6" + breaker.getStats().getBedsBroken());
	}

	private String formatDeath(BedwarsPlayer player, @Nullable BedwarsPlayer killer, BedwarsDeathType type,
							   boolean finalDeath) {
		String inner = type.getInner().get();
		if (finalDeath) {
			inner = "§6§l/" + inner.toUpperCase(Locale.ROOT) + "/";
		} else {
			inner = "§7/" + inner + "/";
		}
		String playerFormatted = getPlayerFormatted(player);
		if (killer == null) {
			return playerFormatted + " " + inner;
		}
		String killerFormatted = getPlayerFormatted(killer);
		if (finalDeath && killer.getStats() != null && killer.getTeam() == me.getTeam()) {
			killerFormatted += " §6" + killer.getStats().getFinalKills();
		}
		return playerFormatted + " " + inner + " " + killerFormatted;
	}

	private String getPlayerFormatted(BedwarsPlayer player) {
		return player.getColoredTeamNumber() + " " + player.getProfile().br$getName();
	}

	public boolean isTeamEliminated(BedwarsTeam team) {
		return players.values().stream().filter(b -> b.getTeam() == team).allMatch(BedwarsPlayer::isFinalKilled);
	}

	public void onChatMessage(String rawMessage, ReceiveChatMessageEvent event) {
		try {
			if (mod.removeAnnoyingMessages.get() && BedwarsMessages.matched(BedwarsMessages.ANNOYING_MESSAGES,
				rawMessage).isPresent()) {
				event.setCancelled(true);
				return;
			}
			if (BedwarsDeathType.getDeath(rawMessage, (type, m) -> died(m, rawMessage, event, type))) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.BED_DESTROY, rawMessage, m -> {
				Optional<BedwarsPlayer> player =
					BedwarsMessages.matched(BedwarsMessages.BED_BREAK, rawMessage).flatMap(m1 -> getPlayer(m1.group(1)));
				if (player.isEmpty()) {
					DolphinClientCommon.getInstance().getLogger().warn("Unknown bed break message: " + rawMessage);
					//Notifications.getInstance().addStatus("bedwars.unknown_bed_break", "bedwars.unknown_message");
					return;
				}
				BedwarsTeam team = BedwarsTeam.fromName(m.group(1)).orElse(me.getTeam());
				bedDestroyed(event, team, player.get());
			})) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.DISCONNECT, rawMessage,
				m -> getPlayer(m.group(1)).ifPresent(p -> disconnected(event, p)))) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.RECONNECT, rawMessage,
				m -> getPlayer(m.group(1)).ifPresent(p -> reconnected(event, p)))) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.GAME_END, rawMessage, m -> {
				this.won = players.values().stream().filter(p -> !p.isFinalKilled()).findFirst().map(BedwarsPlayer::getTeam).orElse(null);
				this.wonTick = Platform.tickCount() + 10;
			})) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.TEAM_ELIMINATED, rawMessage,
				m -> BedwarsTeam.fromName(m.group(1)).ifPresent(t -> teamEliminated(event, t)))) {
				return;
			}
			upgrades.onMessage(rawMessage);
		} catch (Exception e) {
			debug("Error: " + e);
		}
	}

	private void died(Matcher m, String rawMessage, ReceiveChatMessageEvent event, BedwarsDeathType type) {
		BedwarsPlayer killed = getPlayer(m.group(1)).orElse(null);
		BedwarsPlayer killer = null;
		if (type != BedwarsDeathType.SELF_UNKNOWN && type != BedwarsDeathType.SELF_VOID) {
			killer = getPlayer(m.group(2)).orElse(null);
		}
		if (killed == null) {
			debug("Player " + m.group(1) + " was not found");
			return;
		}
		died(event, killed, killer, type, BedwarsMessages.matched(BedwarsMessages.FINAL_KILL, rawMessage).isPresent());
	}

	private void gameEnd(BedwarsTeam win) {
		if (me == null) {
			BedwarsMod.getInstance().gameEnd();
			return;
		}

		if (me.getTeam() == win) {
			mod.getSessionStats().win();
		}

		BedwarsMod.getInstance().gameEnd();
	}

	private void teamEliminated(ReceiveChatMessageEvent event, BedwarsTeam team) {
		// Make sure everyone is dead, just in case
		players.values().stream().filter(b -> b.getTeam() == team).forEach(b -> {
			b.setBed(false);
			b.died();
		});
		if (mod.overrideMessages.get()) {
			event.setNewMessage(AxoText.literal(formatEliminated(team)));
		}
		if (me.getTeam() == team) {
			mod.getSessionStats().loose();
			mod.gameEnd();
		}
	}

	private void bedDestroyed(ReceiveChatMessageEvent event, BedwarsTeam team, @Nullable BedwarsPlayer breaker) {
		players.values().stream().filter(b -> b.getTeam() == team).forEach(b -> b.setBed(false));
		if (breaker != null && breaker.getStats() != null) {
			breaker.getStats().addBed();
		}
		if (mod.overrideMessages.get()) {
			event.setNewMessage(AxoText.literal(formatBed(team, breaker)));
		}
		if (me.equals(breaker)) {
			mod.getSessionStats().addBrokenBed();
		} else if (me.getTeam() == team) {
			mod.getSessionStats().bedLost();
		}
	}

	private void disconnected(ReceiveChatMessageEvent event, BedwarsPlayer player) {
		player.disconnected();
		if (mod.overrideMessages.get()) {
			event.setNewMessage(AxoText.literal(formatDisconnect(player)));
		}
	}

	private void reconnected(ReceiveChatMessageEvent event, BedwarsPlayer player) {
		player.reconnected();
		if (mod.overrideMessages.get()) {
			event.setNewMessage(AxoText.literal(formatReconnect(player)));
		}
	}

	public void onScoreboardRender(ScoreboardRenderEvent event) {
		AxoScoreboard scoreboard = event.getObjective().br$getScoreboard();
		final var scores = scoreboard.br$getScores(event.getObjective());
		List<AxoScoreboardScore> filteredScores = scores.stream()
			.filter(score -> !score.br$isHidden())
			.collect(Collectors.toCollection(ArrayList::new)); // explicitly declare the type since we require mutability
		Collections.reverse(filteredScores);
		if (filteredScores.size() < 3) {
			return;
		}
		AxoScoreboardScore score = filteredScores.get(2);
		AxoTeam team = scoreboard.br$getTeamOfMember(score.br$getOwner());
		String timer = AxoText.strip(AxoTeam.br$getMemberDisplayName(team, score.br$getOwner()));
		if (!timer.contains(":")) {
			return;
		}
		if (timer.contains(score.br$getOwner())) {
			timer = timer.replace(score.br$getOwner(), "");
		}
		int seconds;
		try {
			seconds = Integer.parseInt(timer.split(":")[1].substring(0, 2));
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("couldn't parse timer '" + timer + "': ", e);
			return;
		}
		int target = (60 - seconds) % 60;
		if (this.seconds % 60 != target) {
			// Update seconds
			while (this.seconds % 60 != target) {
				updateClock();
			}
			if (me != null) {
				topBarText = calculateTopBarText();
				bottomBarText = calculateBottomBarText();
			}
		}
	}

	private void updateClock() {
		this.seconds++;
	}

	public void tick() {
		int currentTick = Platform.tickCount();
		if (won != null && currentTick >= wonTick) {
			gameEnd(won);
		}
		players.values().forEach(p -> p.tick(currentTick));
	}

	public void updateEntries(List<AxoPlayerListEntry> entries) {
		// Update latencies and other information for entries
		entries.forEach(entry ->
			getPlayer(entry.br$getName()).ifPresent(player -> player.updateListEntry(entry))
		);
	}

	public List<AxoPlayerListEntry> getTabPlayerList(List<AxoPlayerListEntry> original) {
		updateEntries(original);
		return players.values().stream().filter(b -> !b.isFinalKilled()).sorted((b1, b2) -> {
			if (b1.getTeam() == b2.getTeam()) {
				return Integer.compare(b1.getNumber(), b2.getNumber());
			}
			return Integer.compare(b1.getTeam().ordinal(), b2.getTeam().ordinal());
		}).map(BedwarsPlayer::getProfile).collect(Collectors.toList());
	}

	public BedwarsPlayer getSelf() {
		return me;
	}

	public String getLevelHead(AxoPlayer entity) {
		BedwarsPlayer player = getPlayer(entity.br$getUuid()).orElse(null);
		if (player == null) {
			return null;
		}
		BedwarsPlayerStats stats = player.getStats();
		if (stats == null) {
			return null;
		}
		BedwarsLevelHeadMode mode = mod.bedwarsLevelHeadMode.get();
		return mode.apply(stats);
	}

	public void renderCustomScoreboardObjective(AxoRenderContext context, String playerName, int objectiveValue, int y, int endX) {
		BedwarsPlayer bedwarsPlayer = getPlayer(playerName).orElse(null);
		if (bedwarsPlayer == null) {
			return;
		}

		String render;
		int color;
		if (!bedwarsPlayer.isAlive()) {
			if (bedwarsPlayer.isDisconnected()) {
				return;
			}
			int tickTillLive = Math.max(0, bedwarsPlayer.getTickAlive() - Platform.tickCount());
			float secondsTillLive = tickTillLive / 20f;
			render = String.format("%.1f", secondsTillLive) + "s";
			color = new Color(200, 200, 200).toInt();
		} else {
			color = ClientColors.blend(new Color(255, 255, 255), new Color(215, 0, 64),
				(int) (1 - (objectiveValue / 20f))).toInt();
			render = String.valueOf(objectiveValue);
		}

		// Health
		context.br$drawString(
			render,
			(endX - context.br$getFont().br$getWidth(render)),
			y,
			color,
			true
		);
	}
}
