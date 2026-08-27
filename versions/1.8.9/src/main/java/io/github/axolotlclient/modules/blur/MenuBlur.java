/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.blur;

import java.io.IOException;

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.PostChain;
import net.minecraft.client.render.shaders.Uniform;
import net.minecraft.resource.Identifier;

/**
 * Totally not stolen from Sol.
 * License: GPL-3.0
 *
 * @author TheKodeToad
 * @author tterag1098
 */

public class MenuBlur extends AbstractModule {

	@Getter
	private static final MenuBlur Instance = new MenuBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	private final Identifier shaderLocation = new Identifier(DolphinClientCommon.MODID, "shaders/post/menu_blur.json");
	private final IntegerOption strength = new IntegerOption("strength", 8, 0, 100);
	private final IntegerOption fadeTime = new IntegerOption("fadeTime", 1, 0, 10);
	private final ColorOption bgColor = new ColorOption("bgcolor", new Color(0x64000000));
	private final OptionCategory category = OptionCategory.create("menublur");

	private final Color black = new Color(0);

	private long openTime;

	private PostChain shader;

	private int lastWidth;
	private int lastHeight;

	@Override
	public void init() {
		category.add(enabled, strength, fadeTime, bgColor);

		DolphinClient.config().rendering.add(category);
	}

	public boolean renderScreen() {
		if (enabled.get() && !(Minecraft.getInstance().screen instanceof ChatScreen) && shader != null) {
			GuiElement.fill(0, 0, Util.getWindow().getWidth(), Util.getWindow().getHeight(),
				ClientColors.blend(black, bgColor.get(), getProgress()).toInt());
			return true;
		}
		return false;
	}

	private float getProgress() {
		return Math.min((System.currentTimeMillis() - openTime) / (fadeTime.get() * 1000F), 1);
	}

	public void updateBlur() {
		if (enabled.get() && Minecraft.getInstance().screen != null && !(Minecraft.getInstance().screen instanceof ChatScreen)) {
			if ((shader == null || client.width != lastWidth || client.height != lastHeight) && client.height != 0
				&& client.width != 0) {
				try {
					shader = new PostChain(client.getTextureManager(), client.getResourceManager(),
						client.getRenderTarget(), shaderLocation);
					shader.resize(client.width, client.height);
				} catch (IOException e) {
					DolphinClientCommon.getInstance().getLogger().error("Failed to load Menu Blur: ", e);
					return;
				}
			}

			if (shader != null) {
				((ShaderEffectAccessor) shader).getPasses().forEach((shader) -> {
					Uniform radius = shader.getEffect().getUniform("Radius");
					Uniform progress = shader.getEffect().getUniform("Progress");

					if (radius != null) {
						radius.set(strength.get());
					}

					if (progress != null) {
						if (fadeTime.get() > 0) {
							progress.set(getProgress());
						} else {
							progress.set(1);
						}
					}
				});
			}

			lastWidth = client.width;
			lastHeight = client.height;
			renderBlur();
		}
	}

	public void renderBlur() {
		shader.process(((MinecraftClientAccessor) Minecraft.getInstance()).getTicker().tickDelta);
	}

	public void onScreenOpen() {
		openTime = System.currentTimeMillis();
	}
}
