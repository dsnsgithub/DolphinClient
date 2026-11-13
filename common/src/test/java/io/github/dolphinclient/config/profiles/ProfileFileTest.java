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

package io.github.dolphinclient.config.profiles;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ProfileFileTest {
	@Test
	void fileNameUsesTheProfileName() {
		assertEquals("Default.json", ProfileFile.fileName("Default"));
		assertEquals("New Profile.json", ProfileFile.fileName("New Profile"));
		assertEquals("a_b.json", ProfileFile.fileName("a/b"));
		assertEquals("profile.json", ProfileFile.fileName("   "));
	}

	@Test
	void keepsSectionsInOneFile(@TempDir Path temp) throws Exception {
		Path file = temp.resolve("Default.json");
		JsonObject config = new JsonObject();
		config.addProperty("customWindowTitle", "true");
		ProfileFile.putSection(file, ProfileFile.CONFIG, config);
		ProfileFile.putSection(file, ProfileFile.CUSTOM_HUD, new JsonArray());
		ProfileFile.putSection(file, ProfileFile.KEYSTROKES, new JsonArray());
		ProfileFile.putSection(file, ProfileFile.HUD_DEPENDENCIES, new JsonObject());

		try (var files = Files.list(temp)) {
			assertEquals(1, files.count());
		}
		JsonObject document = ProfileFile.read(file);
		assertEquals("true", document.getAsJsonObject(ProfileFile.CONFIG).get("customWindowTitle").getAsString());
		assertTrue(document.get(ProfileFile.CUSTOM_HUD).isJsonArray());
		assertTrue(document.get(ProfileFile.KEYSTROKES).isJsonArray());
		assertTrue(document.get(ProfileFile.HUD_DEPENDENCIES).isJsonObject());
		assertFalse(Files.isDirectory(temp.resolve("profiles")));
	}
}
