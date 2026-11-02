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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;
import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.StyleImpl;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.dolphinclient.config.ProfileConfigManager;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.bridge.util.AxoProfiler;
import io.github.dolphinclient.config.migration.ConfigMigration;
import io.github.dolphinclient.config.profiles.ProfileAware;
import io.github.dolphinclient.config.profiles.Profiles;
import io.github.dolphinclient.modules.Module;
import io.github.dolphinclient.modules.freelook.Freelook;
import io.github.dolphinclient.modules.hud.ClickInputTracker;
import io.github.dolphinclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.dolphinclient.modules.render.BeaconBeam;
import io.github.dolphinclient.modules.rpc.DiscordRPC;
import io.github.dolphinclient.modules.tnttime.TntTime;
import io.github.dolphinclient.modules.zoom.Zoom;
import io.github.dolphinclient.util.FeatureDisablerCommon;
import io.github.dolphinclient.util.Logger;
import io.github.dolphinclient.util.OSUtil;
import io.github.dolphinclient.util.notifications.NotificationProvider;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

public abstract class DolphinClientCommon {
	public static final String NAME = "DolphinClient";
	public static final String MODID = "dolphinclient";
	public static final String COMMON_MODID = "dolphinclient-common";
	public static final String CONFIG_FILE_NAME = "dolphinclient.json";
	public static final String KEY_CATEGORY = "category.dolphinclient";
	public static final AxoIdentifier BADGE_PATH = AxoIdentifier.of(MODID, "textures/badge.png");

	// static utility methods
	public static Path resolveConfigFile(String file) {
		return FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve(file);
	}

	public static Path resolveProfileConfigFile(String file) {
		return Profiles.getInstance().resolveProfileFile(file);
	}

	public static boolean isOwnNamespace(String namespace) {
		return MODID.equals(namespace);
	}

	public static final boolean SHADERS_SUPPORTED = OSUtil.getOS() != OSUtil.OperatingSystem.OTHER &&
		!FabricLoader.getInstance().isModLoaded("vulkanmod");

	public static final String VERSION = FabricLoader.getInstance()
		.getModContainer(COMMON_MODID)
		.orElseThrow()
		.getMetadata()
		.getVersion()
		.getFriendlyString();

	public static final String GAME_VERSION = FabricLoader.getInstance()
		.getModContainer("minecraft")
		.orElseThrow()
		.getMetadata()
		.getVersion()
		.getFriendlyString();

	private static DolphinClientCommon instance;

	private DolphinClientConfigCommon config;
	@Getter
	private final Logger logger = new Logger.Slf4jLogger();
	private NotificationProvider notificationProvider;
	private JsonConfigManager configManager;
	private boolean initialized = false;
	public final List<Module> modules = new ArrayList<>();

	protected DolphinClientCommon() {
	}

	// getters

	public DolphinClientConfigCommon getConfig() {
		Preconditions.checkState(initialized && config != null);
		return config;
	}

	/**
	 * @return The config manager
	 * @implNote Do not use to save the config as files other than the main config need to be saved as well
	 * @see #saveConfig()
	 */
	public ConfigManager getConfigManager() {
		Preconditions.checkState(initialized && configManager != null);
		return configManager;
	}

	public NotificationProvider getNotificationProvider() {
		Preconditions.checkState(initialized && notificationProvider != null);
		return notificationProvider;
	}

	public static DolphinClientCommon getInstance() {
		Preconditions.checkState(instance != null);
		return instance;
	}

	private void addBuiltinCommonModules() {
		registerModule(ClickInputTracker.getInstance());
		registerModule(BeaconBeam.getInstance());
		registerModule(Freelook.getInstance());
		registerModule(TntTime.getInstance());
		registerModule(DiscordRPC.getInstance());
		registerModule(Zoom.getInstance());
	}

	// init logic

	private void earlyModuleInit() {
		modules.forEach(Module::init);
	}

	private void lateModuleInit() {
		modules.forEach(Module::lateInit);
	}

	private void initConfig() {
		var configFile = getMainConfigFile();
		configManager = new ProfileConfigManager(configFile, config.getConfig(), ConfigMigration.CONFIG_VERSION,
			(oldVersion, newVersion, config, json) -> {
				ConfigMigration.apply(oldVersion.getMajor(), json);
				return json;
			});

		AxolotlClientConfig.getInstance().register(configManager);
		configManager.load();

		configManager.suppressName("x");
		configManager.suppressName("y");
		configManager.suppressName(config.hidden.getName());
	}

	protected final void init(NotificationProvider provider) {
		Preconditions.checkState(!initialized);
		Preconditions.checkState(instance == null);

		instance = this;
		addBuiltinCommonModules();

		initialized = true;
		Profiles.getInstance().loadProfiles();

		this.notificationProvider = provider;
		config = createConfig();

		earlyModuleInit();
		initConfig();

		ConfigUI.getInstance().runWhenLoaded(() -> {
			var ui = ConfigUI.getInstance();
			for (String style : List.of("vanilla", "rounded")) {
				String widgets = "io.github.dolphinclient.util.options." + style + ".";
				ui.addWidget(style, "forceableboolean", widgets + "ForceableBooleanWidget");
				ui.addWidget(style, "generic", widgets + "GenericWidget");
				ui.addWidget(style, "graphics", widgets + "AxoGraphicsWidget");
			}
			ui.addStyle(new StyleImpl("vanilla-quicktoggles", new HashMap<>(), null, "vanilla"));
			ui.addWidget("vanilla-quicktoggles", "category",
				"io.github.dolphinclient.util.options.vanilla.QuickToggleCategoryWidget");
			lateModuleInit();
		});

		Events.TICK.register(() -> {
			AxoProfiler.get().br$push("DolphinClient");
			modules.forEach(Module::tick);
			AxoProfiler.get().br$pop();
		});

		getFeatureDisabler().init();

		Events.END_RESOURCE_RELOAD.register(() -> HypixelAbstractionLayer.getInstance().clearPlayerData());
	}

	protected final void registerModule(Module module) {
		Preconditions.checkState(!initialized);
		modules.add(module);
	}

	protected abstract FeatureDisablerCommon getFeatureDisabler();

	protected abstract DolphinClientConfigCommon createConfig();

	// random stuff

	public void saveConfig() {
		AxolotlClientConfig.getInstance().saveAll();
		for (Module m : modules) {
			if (m instanceof ProfileAware p) {
				p.saveConfig();
			}
		}
	}

	public Path getMainConfigFile() {
		var path = resolveProfileConfigFile(CONFIG_FILE_NAME);
		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			getLogger().warn("Failed to create config directory, config may not be saved correctly!", e);
		}
		return path;
	}

	public void reloadConfig() {
		configManager.setFile(getMainConfigFile());
		configManager.load();
		for (Module m : modules) {
			if (m instanceof ProfileAware p) {
				p.reloadConfig();
			}
		}
		lateModuleInit();
	}
}
