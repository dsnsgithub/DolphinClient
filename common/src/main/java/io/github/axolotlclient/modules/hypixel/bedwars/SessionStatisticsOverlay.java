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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.bridge.PlatformDispatch;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.CommonUtil;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;

public class SessionStatisticsOverlay extends TextHudEntry {
	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "bedwars.session_statistics");
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
	private static final SessionStatistics PLACEHOLDER_STATS = CommonUtil.make(new SessionStatistics(), s -> {
		s.wins = 50;
		s.losses = 17;
		s.bedsLost = 23;
		s.bedsBroken = 72;
		s.gamesPlayed = 67;
		s.finalDeaths = 20;
		s.finalKills = 65;
		s.winstreak = 4;
		s.deaths = 96;
		s.kills = 203;
	});
	@Getter
	private final List<SessionStatsEntry> entries = new ArrayList<>(List.of(
		SessionStatsEntry.ofInt("games_played", SessionStatistics::getGamesPlayed),
		SessionStatsEntry.ofInt("wins", SessionStatistics::getWins),
		SessionStatsEntry.ofInt("losses", SessionStatistics::getLosses),
		SessionStatsEntry.ofInt("winstreak", SessionStatistics::getWinstreak),
		SessionStatsEntry.ofInt("beds_broken", SessionStatistics::getBedsBroken),
		SessionStatsEntry.ofInt("beds_lost", SessionStatistics::getBedsLost),
		SessionStatsEntry.ofInt("final_deaths", SessionStatistics::getFinalDeaths),
		SessionStatsEntry.ofInt("final_kills", SessionStatistics::getFinalKills),
		SessionStatsEntry.ofInt("deaths", SessionStatistics::getDeaths),
		SessionStatsEntry.ofInt("kills", SessionStatistics::getKills),
		SessionStatsEntry.ofFloat("kdr", SessionStatistics::getKdr),
		SessionStatsEntry.ofFloat("fkdr", SessionStatistics::getFkdr)
	));
	private final BedwarsMod mod;

	private final BooleanOption shown = new BooleanOption("bedwars.session_stats.show_hud", true);
	private final GenericOption entryConfig = new GenericOption("bedwars.session_stats.order", "bedwars.session_stats.order.label", PlatformDispatch::bedwars$sessionstats$openEntryConfigScreen) {
		@Override
		public String toSerializedValue() {
			return String.join(",", entries.stream().map(e -> e.id).toList());
		}

		@Override
		public void fromSerializedValue(String s) {
			var order = Arrays.asList(s.split(","));
			entries.sort(Comparator.comparingInt(e -> order.indexOf(e.id())));
		}
	};
	private final ColorOption valueColor = new ColorOption("bedwars.session_stats.value_color", ClientColors.SELECTOR_BLUE);

	public SessionStatisticsOverlay(BedwarsMod mod) {
		super(150, 200, true);
		this.mod = mod;
		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "bedwars.session_stats.toggle_hud")
			.br$registerOnConsumeClick(() -> {
				shown.toggle();
				DolphinClientCommon.getInstance().saveConfig();
			});
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(options.indexOf(textColor) + 1, valueColor);
		options.add(shown);
		entries.forEach(e -> options.add(e.enabled));
		options.add(entryConfig);
		return options;
	}

	@Override
	public void postConfigLoad() {
		entries.forEach(e -> DolphinClientCommon.getInstance().getConfigManager().suppressName(e.enabled.getName()));
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (shown.get()) {
			super.render(ctx, delta);
		}
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		renderEntries(ctx, mod.getSessionStats());
	}

	private void renderEntries(AxoRenderContext ctx, SessionStatistics stats) {
		var x = getContentX() + 2;
		var width = getContentWidth() - 4;
		var y = getContentY() + 2;
		var lineHeight = ctx.br$getFont().br$getFontHeight() + 2;
		var newWidth = 0;
		for (SessionStatsEntry e : entries) {
			if (!e.enabled().get()) continue;
			newWidth = Math.max(newWidth, renderLine(ctx, e.name(), e.value().apply(stats), x, y, width));
			y += lineHeight;
		}
		y -= 2;
		boolean updated = false;
		if (newWidth != width) {
			setContentWidth(newWidth);
			updated = true;
		}
		if (y - getContentY() != getContentHeight()) {
			setContentHeight(y - getContentY());
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}
	}

	private int renderLine(AxoRenderContext ctx, AxoText left, String right, int x, int y, int width) {
		int leftWidth = ctx.br$drawString(left, x, y, textColor.get()) - x;
		int rightWidth = ctx.br$getFont().br$getWidth(right);
		ctx.br$drawString(right, x + width - rightWidth, y, valueColor.get());
		return leftWidth + 10 + rightWidth;
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		renderEntries(ctx, PLACEHOLDER_STATS);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	public record SessionStatsEntry(String id, AxoText name, Function<SessionStatistics, String> value,
									BooleanOption enabled) {

		// Someone has to explain to me why there is a ToIntFunction and a ToDoubleFunction but no ToFloatFunction
		public static SessionStatsEntry ofInt(String name, ToIntFunction<SessionStatistics> value) {
			return new SessionStatsEntry(name, AxoText.translatable("bedwars.session_stats." + name),
				s -> String.valueOf(value.applyAsInt(s)),
				getOption(name));
		}

		public static SessionStatsEntry ofFloat(String name, Function<SessionStatistics, Float> value) {
			return new SessionStatsEntry(name, AxoText.translatable("bedwars.session_stats." + name),
				value.andThen(FORMAT::format),
				getOption(name));
		}

		private static BooleanOption getOption(String name) {
			return new BooleanOption("bedwars.session_stats.show_" + name, true);
		}
	}
}
