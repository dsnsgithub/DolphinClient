/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.config.profiles;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.hash.Hashing;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.config.ConfigPathMigration;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.resource.AxoResource;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class Profiles {
	private static final Path PROFILES_CONFIG = DolphinClientCommon.resolveConfigFile("profiles").resolve("profiles.json");
	private static final String PROFILE_EXPORT_FILE_EXTENSION = ".axoprofile";
	private static final DateTimeFormatter EXPORT_TIME_FORMAT = new DateTimeFormatterBuilder().appendPattern("yyyy_MM_dd-HH_mm_ss").toFormatter();
	private static final String PROFILE_INFO_FILE = "profile_info.json";

	public static Profiles getInstance() {
		return INSTANCE;
	}

	private static final Profiles INSTANCE = new Profiles();

	private ProfileStorage storage;

	public void loadProfiles() {
		if (Files.exists(PROFILES_CONFIG)) {
			try (var stream = Files.newBufferedReader(PROFILES_CONFIG)) {
				storage = GsonHelper.GSON.fromJson(stream, ProfileStorage.class);
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Failed to load profiles!", e);
			}
		} else {
			storage = new ProfileStorage();
			storage.current = newProfile("Default");
			saveProfiles();
			for (String name : new String[]{
				DolphinClientCommon.CONFIG_FILE_NAME,
				DolphinClientCommon.LEGACY_CONFIG_FILE_NAME,
				"custom_hud.json",
				"keystrokes.json"
			}) {
				var oldPath = DolphinClientCommon.resolveConfigFile(name);
				var newPath = resolveProfileFile(name);
				if (Files.exists(oldPath) && !Files.exists(newPath)) {
					try {
						Files.createDirectories(newPath.getParent());
						Files.move(oldPath, newPath);
					} catch (IOException e) {
						DolphinClientCommon.getInstance().getLogger().warn("Failed to move {} to profile-based config path at {}", oldPath, newPath, e);
					}
				}
			}
		}
		migrateLegacyProfileConfigFiles();
	}

	private void migrateLegacyProfileConfigFiles() {
		if (storage == null) {
			return;
		}
		iterateAvailable(profile -> {
			try {
				ConfigPathMigration.resolveConfigFile(
					profile.getPath(),
					DolphinClientCommon.CONFIG_FILE_NAME,
					DolphinClientCommon.LEGACY_CONFIG_FILE_NAME);
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Failed to migrate config file in profile {}", profile.name(), e);
			}
		});
	}

	public void saveProfiles() {
		try {
			Files.createDirectories(PROFILES_CONFIG.getParent());
			try (var stream = Files.newBufferedWriter(PROFILES_CONFIG)) {
				GsonHelper.GSON.toJson(storage, stream);
			}
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to save profiles, falling back to 'default'", e);
		}
	}

	public void iterateAvailable(Consumer<Profile> action) {
		storage.available().forEach(action);
	}

	public void remove(Profile profile) {
		storage.available().remove(profile);
	}

	public void switchTo(Profile profile) {
		if (!storage.available().contains(profile)) {
			throw new IllegalArgumentException("Unknown profile!");
		}
		DolphinClientCommon.getInstance().getLogger().debug("Switching to profile {}", profile.name());
		storage.current = profile;
		DolphinClientCommon.getInstance().reloadConfig();
	}

	public Profile getCurrent() {
		return storage.current();
	}

	public Path resolveProfileFile(String path) {
		return getCurrent().getPath().resolve(path);
	}

	@SuppressWarnings("UnstableApiUsage")
	public Profile newProfile(String name) {
		var p = new Profile(name, Hashing.sha512().hashUnencodedChars(UUID.randomUUID().toString()).toString());
		storage.available().add(p);
		return p;
	}

	public Profile duplicate(Profile profile) {
		DolphinClientCommon.getInstance().saveConfig();
		var duplicate = newProfile(AxoI18n.translate("profiles.duplicated", profile.name()));
		try {
			Files.copy(profile.getPath(), duplicate.getPath());
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to duplicate profile!");
		}
		return duplicate;
	}

	public CompletableFuture<?> exportProfile(Profile profile) {
		return CompletableFuture.runAsync(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
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
				Files.deleteIfExists(outPath);
				try (var fs = FileSystems.newFileSystem(outPath, Map.of("create", "true"))) {
					var realRoot = profile.getPath();
					Files.walkFileTree(realRoot, new SimpleFileVisitor<>() {
						@Override
						public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
							var zipped = fs.getPath("profile").resolve(realRoot.relativize(file).toString());
							Files.createDirectories(zipped.getParent());
							Files.copy(file.toAbsolutePath(), zipped);
							return super.visitFile(file, attrs);
						}
					});
					Files.writeString(fs.getPath(PROFILE_INFO_FILE), GsonHelper.GSON.toJson(new ProfileInfo(profile)));
				}
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
				var imported = Arrays.stream(files.split("\\|")).map(Path::of)
					.map(this::readProfile).filter(Objects::nonNull).toList();
				saveProfiles();
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

	private @Nullable Profile readProfile(Path p) {
		try (var fs = FileSystems.newFileSystem(p)) {
			var profileInfoPath = fs.getPath(PROFILE_INFO_FILE);
			if (!Files.exists(profileInfoPath)) {
				DolphinClientCommon.getInstance().getLogger().warn("Skipping bad profile file at {}", p);
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus("profiles.profile.import.notification.failed", "profiles.profile.import.notification.malformed_profile", p.getFileName());
				return null;
			}
			var profileInfo = GsonHelper.GSON.fromJson(Files.readString(profileInfoPath), ProfileInfo.class);
			DolphinClientCommon.getInstance().getLogger().debug("Extracting profile: {}", profileInfo);
			var newProfile = newProfile(profileInfo.name());
			Files.createDirectories(newProfile.getPath());
			var fakeRoot = fs.getPath("/profile");
			if (!Files.exists(fakeRoot)) {
				DolphinClientCommon.getInstance().getLogger().debug("Profile is empty!");
				return newProfile;
			}
			Files.walkFileTree(fakeRoot, new SimpleFileVisitor<>() {
				@Override
				public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
					var outPath = newProfile.getPath().resolve(fakeRoot.relativize(file).toString()).normalize();
					DolphinClientCommon.getInstance().getLogger().debug("Extracting profile file {} to {}", file, outPath);
					if (!outPath.startsWith(newProfile.getPath())) {
						throw new AccessDeniedException(file.toString(), profileInfo.name(), null);
					}
					Files.copy(file, outPath);
					return super.visitFile(file, attrs);
				}
			});
			DolphinClientCommon.getInstance().getLogger().debug("Extracted profile {}", newProfile.name());
			return newProfile;
		} catch (AccessDeniedException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Profile {} tried to escape its directory, aborting import.", p);
			DolphinClientCommon.getInstance().getNotificationProvider().addStatus("profiles.profile.import.notification.failed", "profiles.profile.import.notification.malformed_profile", p.getFileName());
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to import profile from {}:", p, e);
			DolphinClientCommon.getInstance().getNotificationProvider()
				.addStatus("profiles.profile.import.notification.failed",
					"profiles.profile.import.notification.failed.generic");
		}
		return null;
	}

	public List<ProfilePreset> findPresets() {
		var mc = AxoMinecraftClient.getInstance();
		var resourceManager = mc.br$getResourceManager();
		var presetCandidates = new LinkedHashMap<AxoIdentifier, AxoResource>();
		presetCandidates.putAll(resourceManager.br$listResources(DolphinClientCommon.MODID, "profiles/presets", id -> id.br$getPath().endsWith(PROFILE_EXPORT_FILE_EXTENSION)));
		presetCandidates.putAll(resourceManager.br$listResources(DolphinClientCommon.LEGACY_MODID, "profiles/presets", id -> id.br$getPath().endsWith(PROFILE_EXPORT_FILE_EXTENSION)));
		List<ProfilePreset> foundPresets = new ArrayList<>();
		presetCandidates.forEach((id, resource) -> {
			try (var in = new ZipInputStream(resource.br$asStream())) {
				ZipEntry entry;
				do {
					entry = in.getNextEntry();
				} while (!entry.getName().equals(PROFILE_INFO_FILE));
				var profileInfo = GsonHelper.GSON.fromJson(new InputStreamReader(in), ProfileInfo.class);
				foundPresets.add(new ProfilePreset(profileInfo, id, resource));
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Failed to read profile preset from {}", id, e);
			}
		});
		return foundPresets;
	}

	public record ProfilePreset(ProfileInfo info, AxoIdentifier id, AxoResource resource) {
		public void importProfile() {
			var tmp = PROFILES_CONFIG.resolveSibling("tmp").resolve(info.id()+".zip");
			try {
				Files.createDirectories(tmp.getParent());
				Files.copy(resource.br$asStream(), tmp);
				var profile = Profiles.getInstance().readProfile(tmp);
				Files.delete(tmp);
				Files.delete(tmp.getParent());
				if (profile == null) {
					return;
				}
				Profiles.getInstance().saveProfiles();
				Profiles.getInstance().switchTo(profile);
			} catch (IOException e) {
				DolphinClientCommon.getInstance().getLogger().warn("Skipping bad profile file at {}, {}:", id(), info().name(), e);
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus("profiles.profile.import.notification.failed", "profiles.profile.import.notification.malformed_profile", info().name());
			}
		}
	}

	public static final class Profile {
		@Setter
		private String name;
		private final String id;

		public Profile(String name, String id) {
			this.name = name;
			this.id = id;
		}

		public Path getPath() {
			return DolphinClientCommon.resolveConfigFile("profiles").resolve(id());
		}

		public String name() {
			return name;
		}

		public String id() {
			return id;
		}

		@Override
		public String toString() {
			return "Profile[" +
				"name=" + name + ", " +
				"id=" + id + ']';
		}
	}

	@JsonAdapter(ProfileStorageLoader.class)
	public static final class ProfileStorage {
		private Profile current;
		private final List<Profile> available = new ArrayList<>();

		private ProfileStorage(Profile current, Collection<Profile> available) {
			this.current = current;
			this.available.addAll(available);
		}

		private ProfileStorage() {

		}

		public Profile current() {
			return current;
		}

		public List<Profile> available() {
			return available;
		}
	}

	public static class ProfileStorageLoader extends TypeAdapter<ProfileStorage> {

		@Override
		public void write(JsonWriter out, ProfileStorage value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}

			out.beginObject();
			out.name("current").value(value.current().id());
			out.name("available").beginArray();
			for (Profile entry : value.available()) {
				out.beginObject();
				out.name("name").value(entry.name());
				out.name("id").value(entry.id());
				out.endObject();
			}
			out.endArray();
			out.endObject();
		}

		@SuppressWarnings("unchecked")
		@Override
		public ProfileStorage read(JsonReader in) throws IOException {
			if (in.peek() != JsonToken.BEGIN_OBJECT) {
				return null;
			}

			Map<String, Object> obj = (Map<String, Object>) GsonHelper.read(in);
			if (obj == null) return null;
			var available = ((List<Map<String, String>>) obj.get("available")).stream()
				.map(e -> new Profile(e.get("name"), e.get("id"))).toList();
			Map<String, Profile> profiles = available.stream().collect(Collectors.toMap(Profile::id, Function.identity()));
			return new ProfileStorage(profiles.get((String) obj.get("current")), available);
		}
	}

	@JsonAdapter(ProfileInfo.ProfileInfoAdapter.class)
	public record ProfileInfo(String name, String id) {
		public ProfileInfo(Profile p) {
			this(p.name(), p.id());
		}

		public static class ProfileInfoAdapter extends TypeAdapter<ProfileInfo> {

			@Override
			public void write(JsonWriter jsonWriter, ProfileInfo profileInfo) throws IOException {
				if (profileInfo == null) {
					jsonWriter.nullValue();
					return;
				}
				jsonWriter.beginObject();
				jsonWriter.name("name").value(profileInfo.name())
					.name("id").value(profileInfo.id());
				jsonWriter.endObject();
			}

			@Override
			public ProfileInfo read(JsonReader jsonReader) throws IOException {
				if (jsonReader.peek() == JsonToken.NULL) return null;

				@SuppressWarnings("unchecked") var map = (Map<String, String>) GsonHelper.read(jsonReader);
				if (map == null) return null;
				return new ProfileInfo(map.get("name"), map.get("id"));
			}
		}
	}
}
