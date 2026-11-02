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

package io.github.dolphinclient.modules.hud.gui.hud.vanilla;

import java.util.Comparator;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.util.Rectangle;
import io.github.dolphinclient.util.ClientColors;
import io.github.dolphinclient.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.resource.Identifier;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.scoreboard.team.Team;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class ScoreboardHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "scoreboardhud");
	private final ScoreboardObjective placeholder = Util.make(() -> {
		Scoreboard placeholderScoreboard = new Scoreboard();
		ScoreboardObjective objective = placeholderScoreboard.createObjective("Scoreboard", ScoreboardCriterion.DUMMY);
		ScoreboardScore dark = placeholderScoreboard.getScore("DarkKronicle", objective);
		dark.set(8780);

		ScoreboardScore moeh = placeholderScoreboard.getScore("moehreag", objective);
		moeh.set(743);

		ScoreboardScore kode = placeholderScoreboard.getScore("TheKodeToad", objective);
		kode.set(2948);

		placeholderScoreboard.setDisplayObjective(1, objective);

		return objective;
	});

	private final ColorOption backgroundColor = new ColorOption("backgroundcolor", new Color(0x4C000000));
	private final ColorOption topColor = new ColorOption("topbackgroundcolor", new Color(0x66000000));
	private final IntegerOption topPadding = new IntegerOption("toppadding", 0, 0, 4);
	private final BooleanOption scores = new BooleanOption("scores", true);
	private final ColorOption scoreColor = new ColorOption("scorecolor", new Color(0xFFFF5555));
	private final IntegerOption textAlpha = new IntegerOption("text_alpha", 255, 0, 255);

	private final Minecraft client = (Minecraft) super.client;

	public ScoreboardHud() {
		super(200, 146, true);
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		GlStateManager.pushMatrix();
		scale(context);
		renderComponent(context, delta);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		Scoreboard scoreboard = this.client.world.getScoreboard();
		ScoreboardObjective scoreboardObjective = null;
		Team team = scoreboard.getTeam(this.client.player.getDisplayName().getString());
		if (team != null) {
			int t = team.getColor().getId();
			if (t >= 0) {
				scoreboardObjective = scoreboard.getDisplayObjective(3 + t);
			}
		}

		ScoreboardObjective scoreboardObjective2 = scoreboardObjective != null ? scoreboardObjective
			: scoreboard.getDisplayObjective(1);
		if (scoreboardObjective2 != null) {
			this.renderScoreboardSidebar(context, scoreboardObjective2, false);
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		renderScoreboardSidebar(context, placeholder, true);
	}

	// Abusing this could break some stuff/could allow for unfair advantages. The goal is not to do this, so it won't
	// show any more information than it would have in vanilla.
	private void renderScoreboardSidebar(AxoRenderContext graphics, ScoreboardObjective objective, boolean placeholder) {
		var font = client.textRenderer;
		var scoreboard = objective.getScoreboard();

		@Environment(EnvType.CLIENT)
		record DisplayEntry(String name, String score, int scoreWidth) {
		}

		DisplayEntry[] entries = scoreboard.getScores(objective).stream()
			.filter((testScore) -> testScore.getOwner() != null && !testScore.getOwner().startsWith("#"))
			//.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(ScoreboardScore::get).reversed()
				.thenComparing(ScoreboardScore::getOwner, String.CASE_INSENSITIVE_ORDER))
			.limit(15L)
			.map(entry -> {
				var owner = entry.getOwner();
				var team = scoreboard.getTeamOfMember(owner);
				var value = String.valueOf(entry.get());
				return new DisplayEntry(Team.getMemberDisplayName(team, owner), value, font.getWidth(value));
			}).toArray(DisplayEntry[]::new);
		var title = objective.getDisplayName();
		int titleWidth = font.getWidth(title) + 2;
		int maxWidth = titleWidth;
		int textOffset = font.getWidth(": ");

		for (DisplayEntry lv : entries) {
			maxWidth = Math.max(maxWidth, font.getWidth(lv.name) + (lv.scoreWidth > 0 && scores.get() ?
				textOffset + lv.scoreWidth : 0));
		}

		maxWidth += 3;
		int entryCount = entries.length;
		int mainHeight = entryCount * font.fontHeight;

		int newHeight = mainHeight + 10 + topPadding.get() * 2;

		boolean updated = false;
		if (newHeight + 1 != getContentHeight()) {
			setContentHeight(newHeight + 1);
			updated = true;
		}
		if (maxWidth + 1 != getContentWidth()) {
			setContentWidth(maxWidth + 1);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}

		Rectangle bounds = getContentBounds();

		int yEnd = bounds.y() + bounds.height();
		int textX = bounds.x() + 2;
		int xEnd = bounds.x() + bounds.width() - 1;
		int titleEnd = yEnd - mainHeight;
		var bgBounds = getBounds();
		var maxRounding = Math.min(Math.min(font.fontHeight + topPadding.get() * 2 + backgroundPadding.get(), titleEnd - 1 - bgBounds.y()), xEnd - textX - 3) / 2f;
		float rounding = Math.min(maxRounding, backgroundRounding.get());
		var drawUtil = io.github.axolotlclient.rendering.DrawUtil.get();
		if (!placeholder) {
			if (background.get()) {
				if (roundBackground.get()) {
					drawUtil.axolotlclient_rendering$roundedRect(0, 0, 1, 1, 0, 0); // HELP
					drawUtil.axolotlclient_rendering$roundedRectVarying(bgBounds.x(), bgBounds.y(), bgBounds.xEnd(), titleEnd - 1,
						topColor.get().toInt(), rounding, 0, 0, rounding);
					drawUtil.axolotlclient_rendering$roundedRectVarying(bgBounds.x(), titleEnd - 1, bgBounds.xEnd(), bgBounds.yEnd(),
						backgroundColor.get().toInt(), 0, rounding, rounding, 0);
				} else {
					graphics.br$fillRect(bgBounds.x(), bgBounds.y(), bgBounds.width(), titleEnd - 1 - bgBounds.y(), topColor.get().toInt());
					graphics.br$fillRect(bgBounds.x(), titleEnd - 1, bgBounds.width(), bgBounds.yEnd() - titleEnd + 1, backgroundColor.get().toInt());
				}
			}
		} else {
			graphics.br$fillRect(bgBounds.x()+1, bgBounds.y()+1, bgBounds.width()-2, titleEnd - 1 - bgBounds.y()-1, ClientColors.DARK_GRAY.withAlpha(100));
		}
		font.draw(title, textX + maxWidth / 2f - titleWidth / 2f, titleEnd - font.fontHeight - topPadding.get(),
			ClientColors.ARGB.color(textAlpha.get(), -1), shadow.get());

		for (int v = 0; v < entryCount; v++) {
			DisplayEntry entry = entries[v];
			int y = yEnd - (entryCount - v) * font.fontHeight;
			font.draw(entry.name, textX, y, ClientColors.ARGB.color(textAlpha.get(), -1), shadow.get());
			if (scores.get()) {
				font.draw(entry.score, xEnd - entry.scoreWidth, y, scoreColor.get().toInt(),
					shadow.get());
			}
		}

		if (!placeholder) {
			if (outline.get() && outlineColor.get().getAlpha() > 0) {
				if (roundBackground.get()) {
					drawUtil.axolotlclient_rendering$outlineRoundedRect(bgBounds.x(), bgBounds.y(), bgBounds.xEnd(), bgBounds.yEnd(), outlineColor.get().toInt(), rounding, 0.5f);
				} else {
					graphics.br$outlineRect(bgBounds, outlineColor.get());
				}
			}
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.set(options.indexOf(super.backgroundColor), backgroundColor);
		options.add(hide);
		options.add(topColor);
		options.add(scores);
		options.add(scoreColor);
		options.add(topPadding);
		options.remove(textColor);
		options.add(textAlpha);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.MIDDLE_RIGHT;
	}

	@Override
	public double getDefaultX() {
		return 1.0;
	}

	@Override
	public double getDefaultY() {
		return 0.5;
	}
}
