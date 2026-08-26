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


package io.github.axolotlclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.VersionedJsonConfigManager;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Options;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoProfiler;
import io.github.axolotlclient.config.migration.ConfigMigration;
import io.github.axolotlclient.config.profiles.ProfileAware;
import io.github.axolotlclient.config.profiles.Profiles;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hud.ClickInputTracker;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.render.BeaconBeam;
import io.github.axolotlclient.modules.rpc.DiscordRPC;
import io.github.axolotlclient.modules.tnttime.TntTime;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.FeatureDisablerCommon;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.compat.BetterTabCompat;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

public abstract class AxolotlClientCommon {
	public static final String MODID = "axolotlclient";
	public static final AxoIdentifier BADGE_PATH = AxoIdentifier.of(MODID, "textures/badge.png");

	// static utility methods
	public static Path resolveConfigFile(String file) {
		return FabricLoader.getInstance().getConfigDir().resolve(MODID).resolve(file);
	}

	public static Path resolveProfileConfigFile(String file) {
		return Profiles.getInstance().resolveProfileFile(file);
	}

	public static final boolean SHADERS_SUPPORTED = OSUtil.getOS() != OSUtil.OperatingSystem.OTHER &&
		!FabricLoader.getInstance().isModLoaded("vulkanmod");

	public static final String VERSION = FabricLoader.getInstance()
		.getModContainer("axolotlclient-common")
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

	private static AxolotlClientCommon instance;

	private AxolotlClientConfigCommon config;
	@Getter
	private final Logger logger = new Logger.Slf4jLogger();
	private NotificationProvider notificationProvider;
	private JsonConfigManager configManager;
	private boolean initialized = false;
	public final List<Module> modules = new ArrayList<>();

	protected AxolotlClientCommon() {
	}

	// getters

	public AxolotlClientConfigCommon getConfig() {
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

	public static AxolotlClientCommon getInstance() {
		Preconditions.checkState(instance != null);
		return instance;
	}

	private void addBuiltinCommonModules() {
		registerModule(ClickInputTracker.getInstance());
		registerModule(BeaconBeam.getInstance());
		registerModule(Freelook.getInstance());
		registerModule(TntTime.getInstance());
		registerModule(DiscordRPC.getInstance());
		registerModule(getApiOptions());
		registerModule(Zoom.getInstance());
		registerModule(new BetterTabCompat());
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
		if (Files.notExists(configFile)) {
			var legacy = new Path[]{resolveConfigFile("axolotlclient.json"), FabricLoader.getInstance().getConfigDir().resolve("AxolotlClient.json")};
			for (Path p : legacy) {
				try {
					if (Files.exists(p)) {
						Files.move(p, configFile);
						break;
					}
				} catch (IOException e) {
					logger.warn("Failed to move config file, it might get reset! ", e);
				}
			}
		}
		configManager = new VersionedJsonConfigManager(configFile, config.getConfig(), ConfigMigration.CONFIG_VERSION,
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
			ConfigUI.getInstance().addWidget("vanilla", "graphics", "io.github.axolotlclient.util.options.vanilla.AxoGraphicsWidget");
			ConfigUI.getInstance().addWidget("rounded", "graphics", "io.github.axolotlclient.util.options.rounded.AxoGraphicsWidget");
			lateModuleInit();
		});

		Events.TICK.register(() -> {
			AxoProfiler.get().br$push("AxolotlClient");
			modules.forEach(Module::tick);
			AxoProfiler.get().br$pop();
		});

		getFeatureDisabler().init();

		// register events

		Events.END_RESOURCE_RELOAD.register(() -> HypixelAbstractionLayer.getInstance().clearPlayerData());
		Events.CLIENT_STOP.register(() -> API.getInstance().shutdown());
	}

	protected final void registerModule(Module module) {
		Preconditions.checkState(!initialized);
		modules.add(module);
	}

	protected abstract FeatureDisablerCommon getFeatureDisabler();

	protected abstract AxolotlClientConfigCommon createConfig();

	public abstract Options getApiOptions();

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
		var path = resolveProfileConfigFile("axolotlclient.json");
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
		API.getInstance().restart();
	}
}
