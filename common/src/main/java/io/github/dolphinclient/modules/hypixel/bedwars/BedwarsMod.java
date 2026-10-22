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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.Platform;
import io.github.dolphinclient.bridge.events.EventBus;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.events.types.ReceiveChatMessageEvent;
import io.github.dolphinclient.bridge.events.types.ScoreboardRenderEvent;
import io.github.dolphinclient.bridge.events.types.WorldLoadEvent;
import io.github.dolphinclient.bridge.key.AxoKeybinding;
import io.github.dolphinclient.bridge.key.AxoKeys;
import io.github.dolphinclient.bridge.scores.AxoTeam;
import io.github.dolphinclient.bridge.util.AxoText;
import io.github.dolphinclient.modules.hypixel.AbstractHypixelMod;
import lombok.Getter;

/**
 * @author DarkKronicle
 */

public class BedwarsMod implements AbstractHypixelMod {

	public static EventBus<Consumer<BedwarsGame>> GAME_START_EVENT = EventBus.broadcast1();
	public static EventBus<Runnable> GAME_END_EVENT = EventBus.broadcast0();
	// Triggered when adding a player to our cached player lists after the game has started
	public static EventBus<Consumer<BedwarsPlayer>> PLAYER_ADD = EventBus.broadcast1();
	private final static Pattern[] GAME_START = {
		Pattern.compile("^\\s*?Protect your bed and destroy the enemy beds\\.\\s*?$")
	};

	@Getter
	private static BedwarsMod instance = new BedwarsMod();
	public final BooleanOption hardcoreHearts = new BooleanOption(getTranslationKey("hardcoreHearts"), true);
	public final BooleanOption showHunger = new BooleanOption(getTranslationKey("showHunger"), false);
	public final BooleanOption displayArmor = new BooleanOption(getTranslationKey("displayArmor"), true);
	public final BooleanOption bedwarsLevelHead = new BooleanOption(getTranslationKey("bedwarsLevelHead"), true);
	public final EnumOption<BedwarsLevelHeadMode> bedwarsLevelHeadMode = new EnumOption<>(getTranslationKey("bedwarsLevelHeadMode"),
		BedwarsLevelHeadMode.class,
		BedwarsLevelHeadMode.GAME_KILLS_GAME_DEATHS);
	@Getter
	protected final TeamUpgradesOverlay upgradesOverlay;
	@Getter
	protected final ResourceOverlay resourceOverlay;
	@Getter
	protected final StatsOverlay statsOverlay;
	@Getter
	protected final SessionStatisticsOverlay sessionStatsOverlay;
	protected final BooleanOption removeAnnoyingMessages = new BooleanOption(getTranslationKey("removeAnnoyingMessages"), true);
	protected final BooleanOption overrideMessages = new BooleanOption(getTranslationKey("overrideMessages"), true);
	@Getter
	private final OptionCategory category = OptionCategory.create("bedwars");
	private final BooleanOption enabled = new BooleanOption("enabled", "bedwars.enabled.tooltip", false);
	private final BooleanOption tabRenderLatencyIcon = new BooleanOption(getTranslationKey("tabRenderLatencyIcon"), false);

	private final BooleanOption showChatTime = new BooleanOption(getTranslationKey("showChatTime"), true);
	public final BooleanOption customTabList = new BooleanOption(getTranslationKey("custom_tab_list"), true);
	public final BooleanOption customTabHeader = new BooleanOption(getTranslationKey("custom_tab_header"), true);
	public final BooleanOption customTabFooter = new BooleanOption(getTranslationKey("custom_tab_footer"), true);
	@Getter
	private SessionStatistics sessionStats = new SessionStatistics();
	protected BedwarsGame currentGame = null;
	private int targetTick = -1;
	private boolean waiting = false;

	public BedwarsMod() {
		upgradesOverlay = new TeamUpgradesOverlay(this);
		resourceOverlay = new ResourceOverlay(this);
		statsOverlay = new StatsOverlay(this);
		sessionStatsOverlay = new SessionStatisticsOverlay(this);
	}

	@Override
	public void init() {
		category.add(enabled, hardcoreHearts, showHunger, displayArmor, bedwarsLevelHead, bedwarsLevelHeadMode,
			removeAnnoyingMessages, tabRenderLatencyIcon, showChatTime, overrideMessages, customTabList,
			customTabHeader, customTabFooter);
		category.add(upgradesOverlay.getAllOptions());
		category.add(resourceOverlay.getAllOptions());
		category.add(statsOverlay.getAllOptions());
		category.add(sessionStatsOverlay.getAllOptions());
		category.add(BedwarsDeathType.getOptions());

		instance = this;

		Events.RECEIVE_CHAT_MESSAGE.register(this::onMessage);
		Events.SCOREBOARD_RENDER_EVENT.register(this::onScoreboardRender);
		Events.WORLD_LOAD_EVENT.register(this::onWorldLoad);
		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "bedwars.reset_session_stats").br$registerOnConsumeClick(this::resetSessionStats);
	}

	public boolean isEnabled() {
		return enabled.get();
	}

	public void onWorldLoad(WorldLoadEvent event) {
		if (currentGame != null) {
			gameEnd();
		}
	}

	public boolean isWaiting() {
		if (inGame()) {
			waiting = false;
		}
		return waiting;
	}

	public void onMessage(ReceiveChatMessageEvent event) {
		String rawMessage = event.getFormattedMessage().br$getRawString();
		if (currentGame != null) {
			currentGame.onChatMessage(rawMessage, event);

			final var time = AxoText.empty().br$append(AxoText.literal(currentGame.getFormattedTime())
				.br$color(AxoText.Color.GRAY)
				.br$append(" "));

			if (!event.isCancelled() && showChatTime.get()) {
				// Add time to every message received in game
				if (event.getNewMessage() != null) {
					event.setNewMessage(time.br$append(event.getNewMessage()));
				} else {
					event.setNewMessage(time.br$append(event.getFormattedMessage()));
				}
			}
		} else if (enabled.get() && targetTick < 0 && BedwarsMessages.matched(GAME_START, rawMessage).isPresent()) {
			// Give time for Hypixel to sync
			targetTick = Platform.tickCount() + 10;
		}
	}

	public Optional<BedwarsGame> getGame() {
		return currentGame == null ? Optional.empty() : Optional.of(currentGame);
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (currentGame != null) {
			waiting = false;
			if (currentGame.isStarted()) {
				// Trigger setting the header
				currentGame.tick();
				Platform.setTabListHeader(null);
			} else {
				if (checkReady()) {
					currentGame.onStart();
				}
			}
		} else {
			// The inspection is just wrong because tickCount() doesn't throw since the method is overwritten using mixins
			//noinspection ConstantValue
			if (targetTick > 0 && Platform.tickCount() > targetTick) {
				currentGame = new BedwarsGame(this);
				targetTick = -1;
			}
		}
	}

	private boolean checkReady() {
		for (final var player : AxoMinecraftClient.getInstance().br$getOnlinePlayers()) {
			String name = Platform.getTabNameFor(player).replaceAll("§.", "");
			if (name.charAt(1) == ' ') {
				return true;
			}
		}
		return false;
	}

	public boolean inGame() {
		return currentGame != null && currentGame.isStarted();
	}

	public void onScoreboardRender(ScoreboardRenderEvent event) {
		if (inGame()) {
			waiting = false;
			currentGame.onScoreboardRender(event);
			return;
		}
		if (!AxoText.strip(event.getObjective().br$getDisplayName()).contains("BED WARS")) {
			return;
		}
		final var scoreboard = event.getObjective().br$getScoreboard();
		final var scores = scoreboard.br$getScores(event.getObjective());
		final var filteredScores = scores.stream()
			.filter(score -> !score.br$isHidden())
			.toList();

		waiting = filteredScores.stream().anyMatch(score -> {
			final var team = scoreboard.br$getTeam(score.br$getOwner());
			String format = AxoText.strip(AxoTeam.br$getMemberDisplayName(team, score.br$getOwner())).replaceAll("[^A-z0-9 .:]", "");
			return format.contains("Waiting...") || format.contains("Starting in");
		});
	}

	public void gameEnd() {
		if (currentGame != null) {
			getSessionStats().gamePlayed();
			GAME_END_EVENT.invoker().run();
			currentGame = null;
		}
	}

	public boolean blockLatencyIcon() {
		return !tabRenderLatencyIcon.get();
	}

	private String getTranslationKey(String name) {
		return "bedwars." + name;
	}

	public void resetSessionStats() {
		sessionStats = new SessionStatistics();
	}
}
