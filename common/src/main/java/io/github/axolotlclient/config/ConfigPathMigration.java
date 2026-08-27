/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.axolotlclient.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves on-disk config from AxolotlClient paths to DolphinClient paths so existing
 * installs keep their settings after upgrading.
 */
public final class ConfigPathMigration {
	private ConfigPathMigration() {
	}

	/**
	 * If {@code config/<newModId>} does not exist and {@code config/<legacyModId>} does,
	 * rename the directory so profiles, HUD files, and accounts stay intact.
	 */
	public static boolean migrateConfigDirectory(Path configDir, String legacyModId, String newModId) throws IOException {
		if (legacyModId.equals(newModId)) {
			return false;
		}
		Path newDir = configDir.resolve(newModId);
		Path oldDir = configDir.resolve(legacyModId);
		if (Files.exists(newDir) || !Files.isDirectory(oldDir)) {
			return false;
		}
		Files.move(oldDir, newDir);
		return true;
	}

	/**
	 * Prefer {@code newFileName} in {@code directory}. If it is missing, rename a known legacy file into place.
	 *
	 * @return the path that callers should read and write
	 */
	public static Path resolveConfigFile(Path directory, String newFileName, String... legacyFileNames) throws IOException {
		Path target = directory.resolve(newFileName);
		if (Files.exists(target)) {
			return target;
		}
		Files.createDirectories(directory);
		for (String legacyName : legacyFileNames) {
			if (legacyName.equals(newFileName)) {
				continue;
			}
			Path legacy = directory.resolve(legacyName);
			if (Files.isRegularFile(legacy)) {
				Files.move(legacy, target, StandardCopyOption.REPLACE_EXISTING);
				return target;
			}
		}
		return target;
	}

	/**
	 * Very old layouts stored a single JSON file directly in the Minecraft config folder.
	 */
	public static boolean migrateRootLegacyFile(Path configDir, Path target, String... rootLegacyNames) throws IOException {
		if (Files.exists(target)) {
			return false;
		}
		for (String name : rootLegacyNames) {
			Path legacy = configDir.resolve(name);
			if (Files.isRegularFile(legacy)) {
				Files.createDirectories(target.getParent());
				Files.move(legacy, target);
				return true;
			}
		}
		return false;
	}

	public static List<String> configFileCandidates(String newFileName, String legacyFileName) {
		List<String> names = new ArrayList<>();
		names.add(newFileName);
		if (!legacyFileName.equals(newFileName)) {
			names.add(legacyFileName);
		}
		return names;
	}
}
