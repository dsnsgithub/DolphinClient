/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.util.MathUtil;
import lombok.AllArgsConstructor;

public class XPHud extends TextHudEntry {

	private static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "xphud");

	private final EnumOption<Mode> mode = new EnumOption<>("mode", Mode.class, Mode.LEVEL);
	private final BooleanOption minimal = new BooleanOption("minimal", false);
	private final List<String> lines = new ArrayList<>(3);

	public XPHud() {
		super(53, 13, true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	private void renderInternal(AxoRenderContext ctx, int level, float percent) {
		var mode = this.mode.get();
		lines.clear();
		if (minimal.get()) {
			StringBuilder b = new StringBuilder();
			if (mode.level) {
				b.append(AxoI18n.translate("xphud.level", level));
			}
			if (mode.percent) {
				if (b.isEmpty()) {
					b.append(AxoI18n.translate("xphud.percent", "%.2f".formatted(percent * 100f) + "%"));
				} else {
					b.append(" / ").append("%.2f".formatted(percent * 100f)).append("%");
				}
			}
			if (mode.total) {
				if (!b.isEmpty()) {
					b.append(" ");
				}
				b.append(AxoI18n.translate("xphud.total", getTotalXp(level, percent)));
			}
			lines.add(b.toString());
		} else {
			if (mode.level) {
				lines.add(AxoI18n.translate("xphud.level", level));
			}
			if (mode.percent) {
				lines.add(AxoI18n.translate("xphud.percent", "%.2f".formatted(percent * 100f) + "%"));
			}
			if (mode.total) {
				lines.add(AxoI18n.translate("xphud.total", getTotalXp(level, percent)));
			}
		}


		var h = (ctx.br$getFont().br$getFontHeight() + 1) * lines.size() + 3;
		var w = lines.stream().mapToInt(s -> ctx.br$getFont().br$getWidth(s) + 2).max().orElse(0) + 2;

		boolean updated = false;
		if (w != getContentWidth()) {
			setContentWidth(w);
			updated = true;
		}
		if (h != getContentHeight()) {
			setContentHeight(h);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}

		var pos = getContentPos();
		var x = pos.x() + 2;
		var y = pos.y() + 2;
		for (var l : lines) {
			ctx.br$drawString(l, x, y, textColor.get());
			y += ctx.br$getFont().br$getFontHeight() + 1;
		}
	}

	private int getTotalXp(int currentLevel, float percentToNext) {
		int totalXp = 0;
		for (int i = 0; i < currentLevel; i++) {
			totalXp += getXpNeededForNextLevel(i);
		}
		totalXp += MathUtil.floor(getXpNeededForNextLevel(currentLevel) * percentToNext);
		return totalXp;
	}

	private int getXpNeededForNextLevel(int experienceLevel) {
		if (experienceLevel >= 30) {
			return 112 + (experienceLevel - 30) * 9;
		} else {
			return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
		}
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		var player = AxoMinecraftClient.getInstance().br$getPlayer();
		renderInternal(ctx, player.br$getExperienceLevel(), player.br$getExperienceProgress());
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		renderInternal(ctx, 45, .362f);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(mode);
		options.add(minimal);
		return options;
	}

	@AllArgsConstructor
	private enum Mode {
		LEVEL(true, false, false),
		PERCENT(false, true, false),
		TOTAL(false, false, true),
		LEVEL_PERCENT(true, true, false),
		LEVEL_TOTAL(true, false, true),
		PERCENT_TOTAL(false, true, true),
		LEVEL_PERCENT_TOTAL(true, true, true);

		private final boolean level;
		private final boolean percent;
		private final boolean total;

		@Override
		public String toString() {
			return "xphud.mode." + super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
