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

package io.github.dolphinclient.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.dolphinclient.config.profiles.ProfileFile;
import io.github.dolphinclient.util.GsonHelper;

/** Loads one profile document. Missing categories fall back to the bundled Default profile. */
public class ProfileConfigManager extends JsonConfigManager {
	private static final String BUNDLED_PROFILE = "assets/dolphinclient/default-profile.json";

	public ProfileConfigManager(Path file, OptionCategory root) {
		super(file, root);
	}

	@Override
	public void load() {
		setDefaults(getRoot());
		applyBundledDefaults();
		try {
			JsonObject document = ProfileFile.read(getFile());
			if (document.has(ProfileFile.CONFIG) && document.get(ProfileFile.CONFIG).isJsonObject()) {
				load(getRoot(), document.getAsJsonObject(ProfileFile.CONFIG));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void applyBundledDefaults() {
		try (var in = ProfileConfigManager.class.getClassLoader().getResourceAsStream(BUNDLED_PROFILE)) {
			if (in == null) {
				return;
			}
			try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				JsonObject document = GsonHelper.GSON.fromJson(reader, JsonObject.class);
				if (document != null && document.has(ProfileFile.CONFIG) && document.get(ProfileFile.CONFIG).isJsonObject()) {
					load(getRoot(), document.getAsJsonObject(ProfileFile.CONFIG));
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void save() {
		try {
			JsonObject document = ProfileFile.read(getFile());
			JsonObject config = new JsonObject();
			save(config, getRoot());
			document.add(ProfileFile.CONFIG, config);
			ProfileFile.write(getFile(), document);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
