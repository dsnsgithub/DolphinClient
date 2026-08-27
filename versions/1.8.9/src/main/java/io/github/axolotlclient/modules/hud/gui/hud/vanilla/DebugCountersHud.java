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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;

public class DebugCountersHud extends TextHudEntry {
	public static final Identifier ID = new Identifier(DolphinClientCommon.MODID, "debugcountershud");
	private final BooleanOption showCCount = new BooleanOption("debugcounters.ccount", true);
	private final BooleanOption showECount = new BooleanOption("debugcounters.ecount", false);
	private final BooleanOption showPCount = new BooleanOption("debugcounters.pcount", false);

	private final Minecraft client = (Minecraft) super.client;

	public DebugCountersHud() {
		super(115, 32, true);
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		if (client.world == null) {
			renderPlaceholderComponent(context, delta);
		}
		DrawPosition pos = getContentPos();
		int lineY = pos.y() + 2;
		int lineX = pos.x() + 1;

		int xEnd = lineX + 50;
		if (showCCount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString(client.worldRenderer.getChunkDebugInfo(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showECount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString(client.worldRenderer.getEntityDebugInfo(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showPCount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString("P: " + client.particleManager.getDebugInfo(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}

		boolean boundsChanged = false;
		if (lineY != getContentHeight() + pos.y()) {
			boundsChanged = true;
			setContentHeight(lineY - pos.y());
		}
		if (xEnd != pos.x() + getContentWidth()) {
			boundsChanged = true;
			setContentWidth(xEnd - pos.x());
		}
		if (boundsChanged) {
			onBoundsUpdate();
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		DrawPosition pos = getContentPos();
		int lineY = pos.y() + 2;
		int lineX = pos.x() + 1;

		int xEnd = lineX + 50;
		if (showCCount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString("C: 186/15000 (s) D: 10, pC: 000, pU: 00, aB: 20", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showECount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString("E: 695/3001, SD: 12", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showPCount.get()) {
			xEnd = Math.max(xEnd, DrawUtil.drawString("P: 200", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}

		boolean boundsChanged = false;
		if (lineY != getContentHeight() + pos.y()) {
			boundsChanged = true;
			setContentHeight(lineY - pos.y());
		}
		if (xEnd != pos.x() + getContentWidth()) {
			boundsChanged = true;
			setContentWidth(xEnd - pos.x());
		}
		if (boundsChanged) {
			onBoundsUpdate();
		}
	}

	@Override
	public Identifier getId() {
		return ID;
	}


	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(hide);
		options.add(showCCount);
		options.add(showECount);
		options.add(showPCount);
		return options;
	}
}
