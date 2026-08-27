/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.PlayerData;
import io.github.axolotlclient.modules.hypixel.PlayerData.Bedwars.CombinedGameData;
import org.jetbrains.annotations.Nullable;

public class StatsOverlay extends TextHudEntry {
	@FunctionalInterface
	private interface EntryRenderer {

		AxoText render(BedwarsTeam team, String name, PlayerData.Bedwars data, int winstreak);
	}

	private record Entry(boolean acceptNull, String name, Predicate<StatsOverlay> condition,
						 EntryRenderer compRenderer) {

	}

	private static final List<Entry> RENDER_ENTRIES = List.of(
		new Entry(true, "bedwars.stats_overlay.header.player", hud -> true, (t, n, bw, ws) -> AxoText.literal(t.getColorSection() + n)),
		new Entry(false, "bedwars.stats.overlay.header.level", o -> o.columnLevel.get(), (t, n, bw, ws) -> BedwarsPrestige.format(bw.level())),
		new Entry(false, "bedwars.stats_overlay.header.fkdr", o -> o.columnFkdr.get(), (t, n, bw, ws) -> AxoText.literal("%.2f (%s/%s)".formatted(bw.core().fkdr(), bw.core().finalKills(), bw.core().finalDeaths())).br$color(AxoText.Color.GOLD)),
		new Entry(false, "bedwars.stats_overlay.header.kdr", o -> o.columnKdr.get(), (t, n, bw, ws) -> AxoText.literal("%.2f (%s/%s)".formatted(bw.core().kdr(), bw.core().kills(), bw.core().deaths())).br$color(AxoText.Color.GOLD)),
		new Entry(false, "bedwars.stats_overlay.header.wlr", o -> o.columnWlr.get(), (t, n, bw, ws) -> AxoText.literal("%.2f (%s/%s)".formatted(bw.core().wlr(), bw.core().wins(), bw.core().losses())).br$color(AxoText.Color.GOLD)),
		new Entry(false, "bedwars.stats_overlay.header.ws", o -> o.columnWs.get(), (t, n, bw, ws) -> AxoText.literal(ws).br$color(AxoText.Color.GOLD))
	);

	private class RenderHelper {

		private final Map<String, PlayerData.Bedwars> stats;
		private final Map<BedwarsTeam, List<String>> playersByTeam;
		private int xCursor = getContentPos().x + padding.get();
		private int yFinal = 0;

		private RenderHelper(Map<String, PlayerData.Bedwars> stats, Map<BedwarsTeam, List<String>> playersByTeam) {
			this.stats = stats;
			this.playersByTeam = playersByTeam;
		}

		private void renderColumn(AxoRenderContext ctx, Entry renderEntry) {
			final var dy = AxoMinecraftClient.getInstance().br$getFont().br$getFontHeight() + rowMargin.get();

			int currY = getContentPos().y + padding.get();
			int newXCursor = ctx.br$drawString(AxoI18n.translate(renderEntry.name), xCursor, currY, 0xffffffff, shadow.get());

			currY += dy;

			for (final var entry : playersByTeam.entrySet()) {
				final var team = entry.getKey();
				final var members = entry.getValue();

				for (String playerName : members) {
					final var data = stats.get(playerName);
					final var text = data == null ?
						(renderEntry.acceptNull ? renderEntry.compRenderer.render(team, playerName, null, 0) : AxoText.literal("?").br$color(AxoText.Color.RED)) :
						renderEntry.compRenderer.render(team, playerName, data, data.all().winstreak());

					newXCursor = Math.max(newXCursor, ctx.br$drawString(text, xCursor, currY, 0xffffffff, shadow.get()));
					currY += dy;
				}
			}

			yFinal = currY;
			xCursor = newXCursor + columnMargin.get();
		}

		private void render(AxoRenderContext ctx) {
			for (final var renderEntry : RENDER_ENTRIES) {
				if (renderEntry.condition().test(StatsOverlay.this)) {
					renderColumn(ctx, renderEntry);
				}
			}

			// don't multiply the padding by two, since it's already accounted for by the cursors
			int newWidth = xCursor - getContentPos().x + padding.get() - columnMargin.get();
			int newHeight = yFinal - getContentPos().y + padding.get() - rowMargin.get();

			boolean dirty = newWidth != getContentWidth() || newHeight != getContentHeight();

			setContentWidth(newWidth);
			setContentHeight(newHeight);

			if (dirty) {
				onBoundsUpdate();
			}
		}
	}

	private static final Map<BedwarsTeam, List<String>> SAMPLE_PLAYERS = new EnumMap<>(BedwarsTeam.class);

	static {
		SAMPLE_PLAYERS.put(BedwarsTeam.AQUA, List.of("FloweyTF", "Adaklys"));
		SAMPLE_PLAYERS.put(BedwarsTeam.GREEN, List.of("herobrine", "steve"));
	}

	private static final Map<String, PlayerData.Bedwars> SAMPLE_STATS = Map.of(
		"FloweyTF", createFake(525, 3, new CombinedGameData(4234, 5634, 500, 300, 1469, 336, 230, 123)),
		"Adaklys", createFake(179, 3, new CombinedGameData(1984, 2048, 300, 500, 834, 737, 123, 273)),
		"steve", createFake(5, 2, new CombinedGameData(10, 1, 10, 1, 10, 1, 10, 1))
	);

	private static PlayerData.Bedwars createFake(int level, int winstreak, CombinedGameData data) {
		return new PlayerData.Bedwars(level,
			new PlayerData.Bedwars.GameData(0, 0, 0, 0, winstreak, 0, 0, 0, 0),
			data, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null);
	}

	public final static AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "bedwars_stats_overlay");

	protected final IntegerOption padding = new IntegerOption("hud.padding", 3, 1, 10);
	protected final IntegerOption columnMargin = new IntegerOption("hud.column_margin", 3, 0, 10);
	protected final IntegerOption rowMargin = new IntegerOption("hud.row_margin", 1, 0, 10);
	private final BooleanOption columnLevel = new BooleanOption("bedwars.stats_overlay.column.level", false);
	private final BooleanOption columnFkdr = new BooleanOption("bedwars.stats_overlay.column.fkdr", true);
	private final BooleanOption columnKdr = new BooleanOption("bedwars.stats_overlay.column.kdr", true);
	private final BooleanOption columnWlr = new BooleanOption("bedwars.stats_overlay.column.wlr", true);
	private final BooleanOption columnWs = new BooleanOption("bedwars.stats_overlay.column.ws", true);
	private final BooleanOption toggleKey = new BooleanOption("toggle", "bedwars.stats_overlay.toggle", true);
	private final BooleanOption autoActivate = new BooleanOption("bedwars.stats_overlay.auto_activate", true);
	private final BedwarsMod mod;

	private Map<String, PlayerData.Bedwars> stats = new HashMap<>();
	private final Map<BedwarsTeam, List<String>> playersByTeam = new EnumMap<>(BedwarsTeam.class);
	private final AxoKeybinding toggle = AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "bedwars_stats_overlay");
	private boolean shouldRender = false;
	@Nullable
	private String errorMessage = null;

	public StatsOverlay(BedwarsMod mod) {
		super(400, 600, true);
		this.mod = mod;
		BedwarsMod.GAME_START_EVENT.register(this::onStart);
		BedwarsMod.GAME_END_EVENT.register(this::onEnd);
		BedwarsMod.PLAYER_ADD.register(player -> {
			var teamNames = playersByTeam.computeIfAbsent(player.getTeam(), unused -> new ArrayList<>());
			var namesSize = teamNames.size();
			if (namesSize == 0) {
				teamNames.add(player.getProfile().br$getName());
			} else {
				var index = Math.min(namesSize, player.getNumber() - 1);
				teamNames.add(index, player.getProfile().br$getName());
			}
			HypixelAbstractionLayer.getInstance().getPlayerDataApi()
				.getAsync(player.getProfile().br$getId().toString())
				.thenAcceptAsync(o ->
					o.ifPresent(data -> stats.put(player.getProfile().br$getName(), data.bedwars())), client);
		});
	}

	private void onStart(BedwarsGame g) {
		playersByTeam.clear();
		// can't call clear here, since we need a fresh map to avoid requests from writing
		stats = new HashMap<>();
		shouldRender = toggleKey.get() && autoActivate.get();

		if (!API.getInstance().getApiOptions().enabled.get()) {
			errorMessage = "API Not Enabled!";
			return;
		}

		if (!API.getInstance().isAuthenticated()) {
			errorMessage = "API Not Authenticated!";
			return;
		}

		final var api = HypixelAbstractionLayer.getInstance().getPlayerDataApi();

		// need to use capturedStats since this map could've been "retired"
		final var capturedStats = this.stats;

		g.getPlayersByTeam().forEach((t, e) -> {
			playersByTeam.put(t, e.stream().map(AxoPlayerListEntry::br$getName).collect(Collectors.toCollection(ArrayList::new))); // explicit creation because we require mutability
			e.forEach(entry ->
				api.getAsync(entry.br$getId().toString())
					.whenCompleteAsync((playerData, throwable) -> {
						if (playerData.isEmpty()) {
							return;
						}

						capturedStats.put(entry.br$getName(), playerData.get().bedwars());
					}, client));
		});
	}

	private void onEnd() {
		shouldRender = false;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (errorMessage != null) {
			ctx.br$drawString(AxoText.Color.RED + errorMessage, getContentPos().x, getContentPos().y, 0xffffffff, shadow.get());
		}

		if (mod.inGame() && shouldRender) {
			super.render(ctx, delta);
		}
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		new RenderHelper(stats, playersByTeam).render(ctx);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		new RenderHelper(SAMPLE_STATS, SAMPLE_PLAYERS).render(ctx);
	}

	@Override
	public void tick() {
		if (mod.inGame()) {
			if (toggleKey.get()) {
				if (this.toggle.br$consumeClick()) {
					shouldRender = !shouldRender;
				}
			} else {
				shouldRender = toggle.br$isPressed();
			}
		}
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		final var opts = super.getConfigurationOptions();
		opts.remove(textColor);
		opts.add(padding);
		opts.add(columnMargin);
		opts.add(rowMargin);
		opts.add(columnLevel);
		opts.add(columnFkdr);
		opts.add(columnKdr);
		opts.add(columnWlr);
		opts.add(columnWs);
		opts.add(toggleKey);
		opts.add(autoActivate);
		return opts;
	}
}
