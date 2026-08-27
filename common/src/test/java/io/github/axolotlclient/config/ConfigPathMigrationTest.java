/*
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPathMigrationTest {

	@Test
	void migratesLegacyConfigDirectory(@TempDir Path temp) throws Exception {
		Path oldDir = temp.resolve("axolotlclient");
		Path profile = oldDir.resolve("profiles").resolve("default");
		Files.createDirectories(profile);
		Path oldConfig = profile.resolve("axolotlclient.json");
		Files.writeString(oldConfig, "{\"hud\":{}}");

		assertTrue(ConfigPathMigration.migrateConfigDirectory(temp, "axolotlclient", "dolphinclient"));

		Path migrated = temp.resolve("dolphinclient").resolve("profiles").resolve("default").resolve("axolotlclient.json");
		assertTrue(Files.isRegularFile(migrated));
		assertEquals("{\"hud\":{}}", Files.readString(migrated));
		assertFalse(Files.exists(oldDir));
	}

	@Test
	void leavesExistingNewDirectoryAlone(@TempDir Path temp) throws Exception {
		Path oldDir = temp.resolve("axolotlclient");
		Path newDir = temp.resolve("dolphinclient");
		Files.createDirectories(oldDir);
		Files.createDirectories(newDir);
		Files.writeString(oldDir.resolve("axolotlclient.json"), "old");
		Files.writeString(newDir.resolve("dolphinclient.json"), "new");

		assertFalse(ConfigPathMigration.migrateConfigDirectory(temp, "axolotlclient", "dolphinclient"));
		assertEquals("old", Files.readString(oldDir.resolve("axolotlclient.json")));
		assertEquals("new", Files.readString(newDir.resolve("dolphinclient.json")));
	}

	@Test
	void renamesLegacyConfigFileInPlace(@TempDir Path temp) throws Exception {
		Files.writeString(temp.resolve("axolotlclient.json"), "{\"general\":{}}");

		Path resolved = ConfigPathMigration.resolveConfigFile(temp, "dolphinclient.json", "axolotlclient.json");

		assertEquals(temp.resolve("dolphinclient.json"), resolved);
		assertTrue(Files.isRegularFile(resolved));
		assertEquals("{\"general\":{}}", Files.readString(resolved));
		assertFalse(Files.exists(temp.resolve("axolotlclient.json")));
	}

	@Test
	void migratesVeryOldRootConfigFile(@TempDir Path temp) throws Exception {
		Files.writeString(temp.resolve("AxolotlClient.json"), "{\"legacy\":true}");
		Path target = temp.resolve("dolphinclient").resolve("profiles").resolve("default").resolve("dolphinclient.json");

		assertTrue(ConfigPathMigration.migrateRootLegacyFile(temp, target, "AxolotlClient.json", "DolphinClient.json"));
		assertEquals("{\"legacy\":true}", Files.readString(target));
		assertFalse(Files.exists(temp.resolve("AxolotlClient.json")));
	}

	@Test
	void prefersExistingNewConfigFile(@TempDir Path temp) throws Exception {
		Files.writeString(temp.resolve("dolphinclient.json"), "new");
		Files.writeString(temp.resolve("axolotlclient.json"), "old");

		Path resolved = ConfigPathMigration.resolveConfigFile(temp, "dolphinclient.json", "axolotlclient.json");

		assertEquals("new", Files.readString(resolved));
		assertTrue(Files.exists(temp.resolve("axolotlclient.json")));
	}
}
