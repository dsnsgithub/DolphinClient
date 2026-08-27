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

package io.github.axolotlclient.modules.sky;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.core.api.util.NamespacedIdentifiers;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;
import net.ornithemc.osl.resource.loader.api.resource.Resource;
import net.ornithemc.osl.resource.loader.api.resource.manager.ResourceManager;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * <p>License: MIT</p>
 **/

public class SkyResourceManager extends AbstractModule {

	@Getter
	private static final SkyResourceManager instance = new SkyResourceManager();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public void reload(ResourceManager resourceManager) {
		DolphinClientCommon.getInstance().getLogger().debug("Loading custom skies!");
		for (var entry : resourceManager
			.findResources("sky", identifier -> identifier.identifier().endsWith(".json")).entrySet()) {
			if (entry.getKey().namespace().equals("celestial")) { // Skip Celestial Packs, we cannot load them.
				continue;
			}
			DolphinClientCommon.getInstance().getLogger().debug("Loading FSB sky from " + entry.getKey());
			try (BufferedReader reader = entry.getValue().openAsReader()) {
				JsonObject json = gson.fromJson(reader.lines().collect(Collectors.joining("\n")), JsonObject.class);
				if (!json.has("type") || !json.get("type").getAsString().equals("square-textured")) {
					DolphinClientCommon.getInstance().getLogger().debug("Skipping " + entry + " as we currently cannot load it!");
					continue;
				}
				SkyboxManager.getInstance().addSkybox(new FSBSkyboxInstance(
					json));
				DolphinClientCommon.getInstance().getLogger().debug("Loaded FSB sky from " + entry.getKey());
			} catch (IOException ignored) {
			}
		}

		for (var entry : resourceManager
			.findResources("minecraft", "optifine/sky", identifier -> isMCPSky(identifier.identifier()))
			.entrySet()) {
			DolphinClientCommon.getInstance().getLogger().debug("Loading sky: " + entry.getKey());
			loadMCPSky("optifine", entry.getKey(), entry.getValue(), resourceManager);
			DolphinClientCommon.getInstance().getLogger().debug("Loaded sky: " + entry.getKey());
		}

		for (var entry : resourceManager
			.findResources("minecraft", "mcpatcher/sky", identifier -> isMCPSky(identifier.identifier()))
			.entrySet()) {
			DolphinClientCommon.getInstance().getLogger().debug("Loading sky: " + entry.getKey());
			loadMCPSky("mcpatcher", entry.getKey(), entry.getValue(), resourceManager);
			DolphinClientCommon.getInstance().getLogger().debug("Loaded sky: " + entry.getKey());
		}
	}

	private boolean isMCPSky(String path) {
		return path.endsWith(".properties") && path.substring(path.lastIndexOf("/") + 1).startsWith("sky");
	}

	private void loadMCPSky(String loader, NamespacedIdentifier id, Resource resource, ResourceManager resourceManager) {

		JsonObject object = new JsonObject();
		String string;
		String[] option;
		try (var reader = resource.openAsReader()) {
			while ((string = reader.readLine()) != null) {
				try {
					if (!string.startsWith("#")) {
						option = string.split("=");
						if (option[0].equals("source")) {
							if (!option[1].contains(":")) {
								if (option[1].startsWith("assets")) {
									option[1] = option[1].replace("./", "").replace("assets/minecraft/", "");
								}
								if (id.identifier().contains("world")) {
									option[1] = loader + "/sky/world" + id.identifier().split("world")[1].split("/")[0]
										+ "/" + option[1].replace("./", "");
								}
							}
							if (resourceManager.getResource(NamespacedIdentifiers.parse(option[1])).isEmpty()) {
								DolphinClientCommon.getInstance().getLogger().warn("Sky " + id + " does not have a valid texture attached to it: ", option[1]);
								DolphinClientCommon.getInstance().getLogger().warn("Please fix your packs.");
								return;
							}
						}
						if (option[0].equals("startFadeIn") || option[0].equals("endFadeIn")
							|| option[0].equals("startFadeOut") || option[0].equals("endFadeOut")) {
							option[1] = option[1].replace(":", "").replace("\\", "");
						}

						object.addProperty(option[0], option[1]);
					}
				} catch (Exception ignored) {
				}
			}

			SkyboxManager.getInstance().addSkybox(new MCPSkyboxInstance(object));
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().debug("Error while loading sky", e);
		}
	}

	@Override
	public void init() {
		ClientResourceLoaderEvents.START_RESOURCE_RELOAD.register((manager, ctx) -> SkyboxManager.getInstance().clearSkyboxes());
		ClientResourceLoaderEvents.END_RESOURCE_RELOAD.register((manager, ctx) -> reload(manager));
	}
}
