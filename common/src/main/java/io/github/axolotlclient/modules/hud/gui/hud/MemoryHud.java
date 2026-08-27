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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.Justification;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.ClientColors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class MemoryHud extends TextHudEntry {
	private record MemoryInfo(long free, long max, long total) {
		private static MemoryInfo current() {
			return new MemoryInfo(
				Runtime.getRuntime().freeMemory(),
				Runtime.getRuntime().maxMemory(),
				Runtime.getRuntime().totalMemory()
			);
		}

		private static final MemoryInfo PLACEHOLDER = new MemoryInfo(
			2 * 1024 * 1024,
			3 * 1024 * 1024,
			4 * 1024 * 1024
		);

		private long used() {
			return total - free;
		}

		private float usage() {
			return (float) (used()) / max;
		}

		private static String toMiB(long bytes) {
			return (bytes / 1024L / 1024L) + "MiB";
		}

		private String getMemoryLine() {
			return toMiB(used()) + "/" + toMiB(max) + " (" + ((int) (usage() * 100)) + "%)";
		}

		private String getAllocationLine() {
			return AxoI18n.translate("allocated") + ": " + toMiB(total);
		}
	}

	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "memoryhud");

	protected final EnumOption<Justification> justification = new EnumOption<>("justification", Justification.class,
		Justification.CENTER);

	private final ColorOption graphUsedColor = new ColorOption("graphUsedColor",
		ClientColors.SELECTOR_RED.withAlpha(255));
	private final ColorOption graphFreeColor = new ColorOption("graphFreeColor",
		ClientColors.SELECTOR_GREEN.withAlpha(255));

	private final BooleanOption showGraph = new BooleanOption("showGraph", true);
	private final BooleanOption showText = new BooleanOption("showText", false);
	private final BooleanOption showAllocated = new BooleanOption("showAllocated", false);

	public MemoryHud() {
		super(150, 27, true);
	}

	private void doRender(AxoRenderContext context, MemoryInfo info) {
		DrawPosition pos = getContentPos();

		if (showGraph.get()) {
			final int usagePx = (int) ((getContentWidth() - 10) * info.usage());
			context.br$fillRect(pos.x() + 5, pos.y() + 5, usagePx, getContentHeight() - 10, graphUsedColor.get().toInt());
			context.br$fillRect(pos.x() + 5 + usagePx, pos.y() + 5, getContentWidth() - 10 - usagePx, getContentHeight() - 10,
				graphFreeColor.get().toInt());
			context.br$outlineRect(pos.x() + 5, pos.y() + 5, getContentWidth() - 10, getContentHeight() - 10, ClientColors.BLACK);
		}

		if (showText.get()) {
			String mem = info.getMemoryLine();

			context.br$drawString(
				mem,
				pos.x() + justification.get().getXOffset(context.br$getFont().br$getWidth(mem), getContentWidth() - 4) + 2,
				pos.y() + (Math.round((float) getContentHeight() / 2) - 4) - (showAllocated.get() ? 4 : 0),
				textColor.get().toInt(),
				shadow.get()
			);

			if (showAllocated.get()) {
				String alloc = info.getAllocationLine();
				context.br$drawString(
					alloc,
					pos.x() + justification.get().getXOffset(context.br$getFont().br$getWidth(alloc),
						getContentWidth() - 4) + 2,
					pos.y() + (Math.round((float) getContentHeight() / 2) - 4) + 4,
					textColor.get().toInt(),
					shadow.get()
				);
			}
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		doRender(context, MemoryInfo.current());
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		doRender(context, MemoryInfo.PLACEHOLDER);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(justification);
		options.add(showGraph);
		options.add(graphUsedColor);
		options.add(graphFreeColor);
		options.add(showText);
		options.add(showAllocated);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}
}
