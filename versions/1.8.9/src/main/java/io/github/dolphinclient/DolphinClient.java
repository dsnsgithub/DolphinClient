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

package io.github.dolphinclient;

import io.github.dolphinclient.bridge.impl.Bridge;
import io.github.dolphinclient.config.DolphinClientConfig;
import io.github.dolphinclient.modules.ModuleLoader;
import io.github.dolphinclient.modules.blur.MenuBlur;
import io.github.dolphinclient.modules.blur.MotionBlur;
import io.github.dolphinclient.modules.hud.HudManager;
import io.github.dolphinclient.modules.hud.gui.hud.PackDisplayHud;
import io.github.dolphinclient.modules.hitboxes.Hitboxes;
import io.github.dolphinclient.modules.hypixel.HypixelMods;
import io.github.dolphinclient.modules.particles.Particles;
import io.github.dolphinclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.dolphinclient.modules.scrollableTooltips.ScrollableTooltips;
import io.github.dolphinclient.modules.sky.SkyResourceManager;
import io.github.dolphinclient.modules.unfocusedFpsLimiter.UnfocusedFpsLimiter;
import io.github.dolphinclient.oldanimations.OldAnimations;
import io.github.dolphinclient.util.FeatureDisabler;
import io.github.dolphinclient.util.FeatureDisablerCommon;
import io.github.dolphinclient.util.notifications.Notifications;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;

public class DolphinClient extends DolphinClientCommon {

	private void addBuiltinModules() {
		registerModule(SkyResourceManager.getInstance());
		registerModule(HudManager.getInstance());
		registerModule(HypixelMods.getInstance());
		registerModule(MotionBlur.getInstance());
		registerModule(MenuBlur.getInstance());
		registerModule(ScrollableTooltips.getInstance());

		registerModule(Hitboxes.getInstance());
		registerModule(Particles.getInstance());
		registerModule(ScreenshotUtils.getInstance());
		registerModule(UnfocusedFpsLimiter.getInstance());
		registerModule(new OldAnimations());
	}

	private void addExternalModules() {
		ModuleLoader.loadExternalModules().forEach(this::registerModule);
	}

	public void onInitializeClient() {
		Bridge.init();

		addBuiltinModules();
		addExternalModules();

		init(Notifications.getInstance());

		getLogger().debug("Debug Output enabled, Logs will be quite verbose!");
		getLogger().info("DolphinClient Initialized");

		Bridge.postInit();
		ClientResourceLoaderEvents.END_RESOURCE_PACKS_RELOAD.register(resourcePackRepository -> {
			PackDisplayHud hud = (PackDisplayHud) HudManager.getInstance().get(PackDisplayHud.ID);
			if (hud != null) {
				hud.setPacks(resourcePackRepository.openSelectedPacks());
			}
		});
	}

	@Override
	protected FeatureDisablerCommon getFeatureDisabler() {
		return FeatureDisabler.getInstance();
	}

	@Override
	protected DolphinClientConfigCommon createConfig() {
		return new DolphinClientConfig();
	}

	public static DolphinClientConfig config() {
		return (DolphinClientConfig) getInstance().getConfig();
	}
}
