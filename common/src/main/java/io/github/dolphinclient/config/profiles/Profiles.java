/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.config.profiles;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import io.github.dolphinclient.DolphinClientCommon;
import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.resource.AxoResource;
import io.github.dolphinclient.bridge.util.AxoI18n;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.util.GsonHelper;
import io.github.dolphinclient.util.ThreadExecuter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class Profiles {
	private static final String PROFILE_EXPORT_FILE_EXTENSION = ".json";
	private static final DateTimeFormatter EXPORT_TIME_FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyy_MM_dd-HH_mm_ss").toFormatter();

	public static Profiles getInstance() {
		return INSTANCE;
	}

	private static final Profiles INSTANCE = new Profiles();

	private final List<Profile> available = new ArrayList<>();
	private Profile current;

	public static Path directory() {
		return FabricLoader.getInstance().getConfigDir().resolve(DolphinClientCommon.MODID);
	}

	public void loadProfiles() {
		available.clear();
		current = null;
		try {
			Files.createDirectories(directory());
			try (var files = Files.list(directory())) {
				files.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".json"))
					.sorted()
					.forEach(this::readProfile);
			}
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to load profiles!", e);
		}
		if (available.isEmpty()) {
			current = new Profile("Default", null);
			available.add(current);
			saveProfiles();
			return;
		}
		if (current == null) {
			current = available.get(0);
			saveProfiles();
		}
	}

	private void readProfile(Path path) {
		try {
			JsonObject document = ProfileFile.read(path);
			String name = document.has(ProfileFile.NAME)
				? document.get(ProfileFile.NAME).getAsString()
				: stem(path);
			Profile profile = new Profile(name, path);
			available.add(profile);
			if (current == null && document.has(ProfileFile.CURRENT) && document.get(ProfileFile.CURRENT).getAsBoolean()) {
				current = profile;
			}
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Skipping profile file {}", path, e);
		}
	}

	public void saveProfiles() {
		try {
			Files.createDirectories(directory());
			Set<Path> used = new HashSet<>();
			for (Profile profile : available) {
				Path target = uniqueFile(profile.name(), profile.file(), used);
				if (profile.file() != null && Files.exists(profile.file()) && !profile.file().equals(target)) {
					Files.move(profile.file(), target);
				}
				String fileStem = target.getFileName().toString();
				fileStem = fileStem.substring(0, fileStem.length() - ".json".length());
				if (!fileStem.equals(profile.name())) {
					profile.setName(fileStem);
				}
				profile.file(target);
				used.add(target);
				JsonObject document = ProfileFile.read(target);
				document.addProperty(ProfileFile.NAME, profile.name());
				document.addProperty(ProfileFile.CURRENT, profile == current);
				ProfileFile.write(target, document);
			}
			if (current != null) {
				DolphinClientCommon.getInstance().useProfileFile(current.file());
			}
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to save profiles!", e);
		}
	}

	private Path uniqueFile(String name, @Nullable Path existing, Set<Path> used) {
		String base = ProfileFile.fileName(name);
		String stem = base.substring(0, base.length() - ".json".length());
		Path candidate = directory().resolve(base);
		int suffix = 2;
		while (used.contains(candidate) || (Files.exists(candidate) && !candidate.equals(existing))) {
			candidate = directory().resolve(stem + " " + suffix + ".json");
			suffix++;
		}
		return candidate;
	}

	public void iterateAvailable(Consumer<Profile> action) {
		available.forEach(action);
	}

	public void remove(Profile profile) {
		available.remove(profile);
		if (profile.file() == null) {
			return;
		}
		try {
			Files.deleteIfExists(profile.file());
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to delete profile {}", profile.name(), e);
		}
	}

	public void switchTo(Profile profile) {
		if (!available.contains(profile)) {
			throw new IllegalArgumentException("Unknown profile!");
		}
		DolphinClientCommon.getInstance().getLogger().debug("Switching to profile {}", profile.name());
		if (current != null && current != profile) {
			DolphinClientCommon.getInstance().saveConfig();
		}
		current = profile;
		saveProfiles();
		DolphinClientCommon.getInstance().reloadConfig();
	}

	public Profile getCurrent() {
		return current;
	}

	public Profile newProfile(String name) {
		Profile profile = new Profile(name, null);
		available.add(profile);
		saveProfiles();
		return profile;
	}

	public Profile duplicate(Profile profile) {
		if (profile == current) {
			DolphinClientCommon.getInstance().saveConfig();
		}
		Profile duplicate = newProfile(AxoI18n.translate("profiles.duplicated", profile.name()));
		try {
			JsonObject document = ProfileFile.read(profile.file());
			document.addProperty(ProfileFile.NAME, duplicate.name());
			document.addProperty(ProfileFile.CURRENT, false);
			ProfileFile.write(duplicate.file(), document);
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to duplicate profile!");
		}
		return duplicate;
	}

	public CompletableFuture<?> exportProfile(Profile profile) {
		return CompletableFuture.runAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				if (profile == current) {
					DolphinClientCommon.getInstance().saveConfig();
				}
				var pointers = stack.mallocPointer(1);
				pointers.put(stack.UTF8("*" + PROFILE_EXPORT_FILE_EXTENSION));
				pointers.flip();
				var out = TinyFileDialogs.tinyfd_saveFileDialog("Choose export destination",
					FabricLoader.getInstance().getGameDir()
						.resolve(LocalDateTime.now().format(EXPORT_TIME_FORMAT) + "_" + profile.name() + PROFILE_EXPORT_FILE_EXTENSION).toString(),
					pointers, null);
				if (out == null) {
					return;
				}
				if (!out.endsWith(PROFILE_EXPORT_FILE_EXTENSION)) {
					DolphinClientCommon.getInstance().getNotificationProvider()
						.addStatus("profiles.profile.export.notification.failed",
							"profiles.profile.export.notification.failed.invalid_destination");
					return;
				}
				var outPath = Path.of(out);
				JsonObject document = ProfileFile.read(profile.file());
				document.remove(ProfileFile.CURRENT);
				ProfileFile.write(outPath, document);
				DolphinClientCommon.getInstance().getNotificationProvider()
					.addStatus("profiles.profile.export.notification.success",
						"profiles.profile.export.notification.success.desc", profile.name(), FabricLoader.getInstance().getGameDir().relativize(outPath));
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().info("Failed to export profile", e);
				DolphinClientCommon.getInstance().getNotificationProvider()
					.addStatus("profiles.profile.export.notification.failed",
						"profiles.profile.export.notification.failed.generic");
			}
		}, ThreadExecuter.service());
	}

	public CompletableFuture<List<Profile>> importProfiles() {
		return CompletableFuture.supplyAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				var pointers = stack.mallocPointer(1);
				pointers.put(stack.UTF8("*" + PROFILE_EXPORT_FILE_EXTENSION));
				pointers.flip();
				var files = TinyFileDialogs.tinyfd_openFileDialog("Import Profile",
					FabricLoader.getInstance().getGameDir().toString(),
					pointers,
					null, true
				);
				if (files == null) return Collections.emptyList();
				var imported = java.util.Arrays.stream(files.split("\\|")).map(Path::of)
					.map(this::importProfileFile).filter(Objects::nonNull).toList();
				if (!imported.isEmpty()) {
					var count = imported.size();
					if (count == 1) {
						DolphinClientCommon.getInstance().getNotificationProvider()
							.addStatus("profiles.profile.import.notification.success",
								"profiles.profile.import.notification.success.desc.one");
					} else {
						DolphinClientCommon.getInstance().getNotificationProvider()
							.addStatus("profiles.profile.import.notification.success",
								"profiles.profile.import.notification.success.desc.more", count);
					}
				}
				return imported;
			}
		}, ThreadExecuter.service());
	}

	private @Nullable Profile importProfileFile(Path path) {
		try {
			JsonObject document = ProfileFile.read(path);
			if (document.entrySet().isEmpty()) {
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus(
					"profiles.profile.import.notification.failed",
					"profiles.profile.import.notification.malformed_profile",
					path.getFileName());
				return null;
			}
			String name = document.has(ProfileFile.NAME) ? document.get(ProfileFile.NAME).getAsString() : stem(path);
			document.addProperty(ProfileFile.CURRENT, false);
			Profile profile = newProfile(name);
			document.addProperty(ProfileFile.NAME, profile.name());
			ProfileFile.write(profile.file(), document);
			return profile;
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to import profile from {}:", path, e);
			DolphinClientCommon.getInstance().getNotificationProvider()
				.addStatus("profiles.profile.import.notification.failed",
					"profiles.profile.import.notification.failed.generic");
			return null;
		}
	}

	public List<ProfilePreset> findPresets() {
		var mc = AxoMinecraftClient.getInstance();
		var resourceManager = mc.br$getResourceManager();
		var presetCandidates = new LinkedHashMap<AxoIdentifier, AxoResource>();
		presetCandidates.putAll(resourceManager.br$listResources(DolphinClientCommon.MODID, "profiles/presets", id -> id.br$getPath().endsWith(PROFILE_EXPORT_FILE_EXTENSION)));
		List<ProfilePreset> foundPresets = new ArrayList<>();
		presetCandidates.forEach((id, resource) -> {
			try (var in = new InputStreamReader(resource.br$asStream())) {
				JsonObject document = GsonHelper.GSON.fromJson(in, JsonObject.class);
				String name = document != null && document.has(ProfileFile.NAME)
					? document.get(ProfileFile.NAME).getAsString()
					: stem(Path.of(id.br$getPath()));
				foundPresets.add(new ProfilePreset(name, id, resource));
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Failed to read profile preset from {}", id, e);
			}
		});
		return foundPresets;
	}

	private static String stem(Path path) {
		String fileName = path.getFileName().toString();
		return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - ".json".length()) : fileName;
	}

	public record ProfilePreset(String name, AxoIdentifier id, AxoResource resource) {
		public void importProfile() {
			try (var in = resource.br$asStream()) {
				JsonObject document = GsonHelper.GSON.fromJson(new InputStreamReader(in), JsonObject.class);
				if (document == null) {
					return;
				}
				document.addProperty(ProfileFile.CURRENT, false);
				Profile profile = Profiles.getInstance().newProfile(name);
				document.addProperty(ProfileFile.NAME, profile.name());
				ProfileFile.write(profile.file(), document);
				Profiles.getInstance().switchTo(profile);
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Skipping bad profile preset {}, {}:", id(), name(), e);
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus(
					"profiles.profile.import.notification.failed",
					"profiles.profile.import.notification.malformed_profile",
					name());
			}
		}
	}

	public static final class Profile {
		@Setter
		private String name;
		private Path file;

		public Profile(String name, Path file) {
			this.name = name;
			this.file = file;
		}

		public Path file() {
			return file;
		}

		void file(Path file) {
			this.file = file;
		}

		public String name() {
			return name;
		}

		@Override
		public String toString() {
			return "Profile[name=" + name + ", file=" + file + ']';
		}
	}
}
