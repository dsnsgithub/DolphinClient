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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;
import java.util.Locale;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.render.AxoSprites;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;

public class IconHud extends BoxHudEntry implements DynamicallyPositionable {
	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "iconhud");
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(this);
	private final EnumOption<Mode> mode = new EnumOption<>("iconhud.mode", Mode.class, Mode.BOTH);

	public IconHud() {
		super(16, 16, true);
		background = new BooleanOption("background", false);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (client.br$getScreen() == null && mode.get().showsInGame()) {
			super.render(ctx, delta);
		}
	}

	public void renderInGui(AxoRenderContext context, float delta) {
		if (mode.get().showsInGui()) {
			super.render(context, delta);
		}
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		ctx.br$pushMatrix();
		float scale = getScale();
		ctx.br$scaleMatrix(1 / scale, 1 / scale);
		ctx.br$translateMatrix(getRawTrueContentX(), getRawTrueContentY());
		ctx.br$scaleMatrix(scale, scale);
		ctx.br$drawTexture(AxoSprites.BADGE, 0, 0, 16, 16);
		ctx.br$popMatrix();
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		renderComponent(ctx, delta);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(mode);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	private enum Mode {
		IN_GAME() {
			@Override
			public boolean showsInGui() {
				return false;
			}
		},
		GUI() {
			@Override
			public boolean showsInGame() {
				return false;
			}
		},
		BOTH;

		public boolean showsInGame() {
			return true;
		}

		public boolean showsInGui() {
			return true;
		}

		@Override
		public String toString() {
			return "iconhud.mode." + super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
