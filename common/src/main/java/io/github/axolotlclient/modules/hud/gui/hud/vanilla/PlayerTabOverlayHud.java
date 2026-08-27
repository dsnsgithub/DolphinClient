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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;

public class PlayerTabOverlayHud extends TextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "tab_overlay_hud");
	public final BooleanOption showPlayerHeads = new BooleanOption("showPlayerHeads", true);
	public final BooleanOption showHeader = new BooleanOption("showHeader", true);
	public final BooleanOption showFooter = new BooleanOption("showFooter", true);
	public final BooleanOption alwaysShowHeadLayer = new BooleanOption("alwaysShowHeadLayer", false);
	public final BooleanOption numericalPing = new BooleanOption("numericalPing", false);
	private final BooleanOption smallPingText = new BooleanOption("tablist.small_ping_text", false);
	private final ColorOption pingColor0 = new ColorOption("pingColor0", Color.parse("#FF00FFFF"));
	private final ColorOption pingColor1 = new ColorOption("pingColor1", Color.parse("#FF00FF00"));
	private final ColorOption pingColor2 = new ColorOption("pingColor2", Color.parse("#FF008800"));
	private final ColorOption pingColor3 = new ColorOption("pingColor3", Color.parse("#FFFFFF00"));
	private final ColorOption pingColor4 = new ColorOption("pingColor4", Color.parse("#FFFF8800"));
	private final ColorOption pingColor5 = new ColorOption("pingColor5", Color.parse("#FFFF0000"));
	private final BooleanOption shadow = new BooleanOption("shadow", true);
	public final BooleanOption customBackgroundColor = new BooleanOption("custom_background_color", false);

	public PlayerTabOverlayHud() {
		super(150, 40, true);
		supportsScaling = false;
	}

	public boolean renderNumericPing(AxoRenderContext graphics, int width, int x, int y, AxoPlayerListEntry entry) {
		if (numericalPing.get()) {
			Color current;
			if (entry.br$getPing() < 0) {
				current = pingColor0.get();
			} else if (entry.br$getPing() < 150) {
				current = pingColor1.get();
			} else if (entry.br$getPing() < 300) {
				current = pingColor2.get();
			} else if (entry.br$getPing() < 600) {
				current = pingColor3.get();
			} else if (entry.br$getPing() < 1000) {
				current = pingColor4.get();
			} else {
				current = pingColor5.get();
			}

			String text = applySmallText(String.valueOf(entry.br$getPing()));
			graphics.br$pushMatrix();
			graphics.br$translateMatrix(x + width - 1, y);
			graphics.br$translateMatrix(-graphics.br$getFont().br$getWidth(text), 0);

			if (smallPingText.get()) {
				graphics.br$translateMatrix(0, -2);
			}

			graphics.br$drawString(text, 0, 0, current, shadow.get());
			graphics.br$popMatrix();
			return true;
		}
		return false;
	}

	private String applySmallText(String text) {
		if (smallPingText.get()) {
			StringBuilder builder = new StringBuilder(text.length());
			text.chars().map(i -> i >= '0' && i <= '9' ? i + 0x2050 : i).forEach(builder::appendCodePoint);
			return builder.toString();
		}
		return text;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		// done in mixins with transformations
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {

	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		var pos = getContentPos();
		ctx.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2, -1);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = new ArrayList<Option<?>>();
		options.add(enabled);
		Collections.addAll(options, background, customBackgroundColor, backgroundColor, roundBackground, backgroundRounding, backgroundPadding, outline, outlineColor);
		options.add(anchor);
		Collections.addAll(options, numericalPing, smallPingText, showPlayerHeads, shadow, showHeader, showFooter, alwaysShowHeadLayer);
		Collections.addAll(options, pingColor0, pingColor1, pingColor2, pingColor3, pingColor4, pingColor5);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_MIDDLE;
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.01;
	}

	public Color getBackgroundColor() {
		return backgroundColor.get();
	}

	public boolean backgroundDisabled() {
		return !background.get();
	}

	public boolean hasOutline() {
		return outline.get();
	}

	public Color getOutlineColor() {
		return outlineColor.get();
	}

	public boolean hasRoundBackground() {
		return roundBackground.get();
	}

	public int getBackgroundRounding() {
		return backgroundRounding.get();
	}

	public int getBackgroundPadding() {
		return backgroundPadding.get();
	}
}
