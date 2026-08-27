/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import net.fabricmc.loader.api.FabricLoader;

public class SubtitlesHudHud extends BoxHudEntry implements DynamicallyPositionable {
	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "subtitleshud");
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(AnchorPoint.BOTTOM_RIGHT, this);
	// only relevant to 1.8.9
	private static final boolean SOUNDFIX_INSTALLED = FabricLoader.getInstance().isModLoaded("soundfix");
	public final BooleanOption vanillaEntryBackground = new BooleanOption("subtitles.entry_background", true);

	public SubtitlesHudHud() {
		super(80, 13, true);
	}

	@Override
	public double getDefaultX() {
		return 1.0;
	}

	@Override
	public double getDefaultY() {
		return 0.9;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
	}

	public void renderHud(AxoRenderContext ctx, float delta) {
		super.render(ctx, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		// handled externally
	}

	@Override
	public void renderPlaceholder(AxoRenderContext ctx, float delta) {
		if (BridgeVersion.V1_8.isCurrent() && !SOUNDFIX_INSTALLED) {
			hovered = false;
			return;
		}
		var h = 13;
		var w = 80;
		var updated = false;
		if (h > getHeight()) {
			setHeight(h);
			updated = true;
		}
		if (w > getWidth()) {
			setWidth(w);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}
		super.renderPlaceholder(ctx, delta);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		ctx.br$drawCenteredString(getName(), getContentX() + getContentWidth() / 2, getContentY() + getContentHeight() / 2 - ctx.br$getFont().br$getFontHeight()/2 + 1, -1);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(vanillaEntryBackground);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
