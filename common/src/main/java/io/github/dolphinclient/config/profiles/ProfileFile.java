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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** One profile document: {@code config/dolphinclient/<name>.json}. */
public final class ProfileFile {
	public static final String NAME = "name";
	public static final String CURRENT = "current";
	public static final String CONFIG = "config";
	public static final String CUSTOM_HUD = "custom_hud";
	public static final String KEYSTROKES = "keystrokes";
	public static final String HUD_DEPENDENCIES = "hud_dependencies";

	private static final Object LOCK = new Object();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ProfileFile() {
	}

	public static String fileName(String name) {
		String cleaned = name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
		cleaned = cleaned.replaceAll("[. ]+$", "");
		if (cleaned.isEmpty()) {
			cleaned = "profile";
		}
		return cleaned + ".json";
	}

	public static JsonObject read(Path file) throws IOException {
		synchronized (LOCK) {
			return readUnlocked(file);
		}
	}

	public static void write(Path file, JsonObject document) throws IOException {
		synchronized (LOCK) {
			writeUnlocked(file, document);
		}
	}

	public static JsonElement section(Path file, String key) throws IOException {
		synchronized (LOCK) {
			JsonObject document = readUnlocked(file);
			return document.has(key) ? document.get(key) : null;
		}
	}

	public static void putSection(Path file, String key, JsonElement value) throws IOException {
		synchronized (LOCK) {
			JsonObject document = readUnlocked(file);
			document.add(key, value);
			writeUnlocked(file, document);
		}
	}

	private static JsonObject readUnlocked(Path file) throws IOException {
		if (file == null || !Files.isRegularFile(file) || Files.size(file) == 0) {
			return new JsonObject();
		}
		try (var reader = Files.newBufferedReader(file)) {
			JsonElement parsed = GSON.fromJson(reader, JsonElement.class);
			if (parsed == null || !parsed.isJsonObject()) {
				throw new IOException("Profile file is not a JSON object: " + file);
			}
			return parsed.getAsJsonObject();
		}
	}

	private static void writeUnlocked(Path file, JsonObject document) throws IOException {
		Files.createDirectories(file.getParent());
		Files.writeString(file, GSON.toJson(document));
	}
}
