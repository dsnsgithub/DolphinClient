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

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.util.DrawUtil;
import io.github.dolphinclient.util.ClientColors;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BossBar;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class BossBarHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "bossbarhud");
	private static final Identifier BARS_TEXTURE = new Identifier("textures/gui/icons.png");
	private final CustomBossBar placeholder = new CustomBossBar("Boss bar", ClientColors.WHITE);

	private final BooleanOption text = new BooleanOption("text", true);
	private final BooleanOption bar = new BooleanOption("bar", true);
	// TODO custom color
	private final Minecraft client = (Minecraft) super.client;

	public BossBarHud() {
		super(184, 24, false);
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		GlStateManager.enableAlphaTest();
		DrawPosition pos = getContentPos();
		if (BossBar.name != null && BossBar.timer > 0) {
			client.getTextureManager().bind(BARS_TEXTURE);
			--BossBar.timer;
			if (bar.get()) {
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				//GlStateManager.color4f(barColor.get().getRed(), barColor.get().getGreen(), barColor.get().getBlue(), barColor.get().getAlpha());
				DrawUtil.drawTexture(pos.x, pos.y + 12, 0, 74, 182, 5, 256, 256);
				DrawUtil.drawTexture(pos.x, pos.y + 12, 0, 74, 182, 5, 256, 256);
				if (BossBar.health * 183F > 0) {
					//GlStateManager.color4f(barColor.get().getRed(), barColor.get().getGreen(), barColor.get().getBlue(), barColor.get().getAlpha());
					DrawUtil.drawTexture(pos.x, pos.y + 12, 0, 79, (int) (BossBar.health * 183F), 5, 256, 256);
				}
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (text.get()) {
				String string = BossBar.name;
				client.textRenderer.draw(string,
					(float) ((pos.x + width / 2) - client.textRenderer.getWidth(BossBar.name) / 2),
					(float) (pos.y + 2), textColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		DrawPosition pos = getContentPos();
		placeholder.render(pos.x, pos.y + 14);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(hide);
		options.add(text);
		options.add(bar);
		return options;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_MIDDLE;
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@RequiredArgsConstructor
	public class CustomBossBar extends GuiElement {

		private final String name;
		private final Color barColor;

		public void render(int x, int y) {
			GlStateManager.enableTexture();
			if (bar.get()) {
				Minecraft.getInstance().getTextureManager().bind(BARS_TEXTURE);
				GlStateManager.color4f(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), barColor.getAlpha());
				this.drawTexture(x + 1, y, 0, 79, width, 5);
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (text.get()) {
				client.textRenderer.draw(name, (float) ((x + width / 2) - client.textRenderer.getWidth(name) / 2),
					(float) (y - 10), textColor.get().toInt(), shadow.get());
			}
		}
	}
}
