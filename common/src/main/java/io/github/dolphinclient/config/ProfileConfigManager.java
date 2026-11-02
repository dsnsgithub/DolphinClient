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

import java.nio.file.Path;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.VersionedJsonConfigManager;

/** Loads each profile independently, including categories absent from older profiles. */
public class ProfileConfigManager extends VersionedJsonConfigManager {
	public ProfileConfigManager(Path file, OptionCategory root, int version, VersionConverter converter) {
		super(file, root, version, converter);
	}

	@Override
	public void load() {
		// The library only resets missing options within categories present in the file.
		// Reset the whole tree so newly integrated animation settings cannot leak between profiles.
		setDefaults(getRoot());
		super.load();
	}
}
