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

package io.github.axolotlclient.modules.hud;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.hud.ChatHud;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.gui.hud.PackDisplayHud;
import io.github.axolotlclient.modules.hud.gui.hud.PlayerHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.*;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public class HudManager extends HudManagerCommon {
	@Getter
	private final static HudManager instance = new HudManager();

	@Override
	protected void openScreen() {
		var current = Minecraft.getInstance().screen;
		if (current instanceof HudEditScreen menu) {
			menu.closeMenu();
			return;
		}
		Minecraft.getInstance().openScreen(new HudEditScreen());
	}

	@Override
	protected void addExtraHud() {
		add(new ActionBarHud());
		add(new BossBarHud());
		add(new CrosshairHud());
		add(new DebugCountersHud());
		add(new HotbarHud());
		add(new ScoreboardHud());
		add(new KeystrokeHud());
		add(new PackDisplayHud());
		add(new PlayerHud());
		add(new ChatHud());
	}

	@Override
	public void lateInit() {
		super.lateInit();
		if (!FabricLoader.getInstance().isModLoaded("soundfix")) {
			AxolotlClient.getInstance().getConfigManager().suppressName(SubtitlesHudHud.ID.br$getPath());
		}
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		final var mc = ((Minecraft) client);
		mc.profiler.push("Hud render");
		if (!(mc.screen instanceof HudEditScreen)) {
			GlStateManager.enableBlend();
			GlStateManager.color3f(1, 1, 1);
			super.render(context, delta);
		}
		mc.profiler.pop();
	}

	@Override
	public void closeScreen() {
		var screen = Minecraft.getInstance().screen;
		if (screen instanceof Screen s) {
			s.closeScreen();
			return;
		}
		Minecraft.getInstance().openScreen(null);
	}
}
