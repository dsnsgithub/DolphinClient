/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.bridge;

import io.github.axolotlclient.DolphinClientCommon;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

@Getter
public enum BridgeVersion {
	V1_8("1.8.9"),
	V1_20("1.20"),
	V1_21("1.21"),
	V1_21_11("1.21.11"),
	V26_1("26.1"),
	V26_2("26.2");

	private final String name;

	BridgeVersion(String name) {
		this.name = name;
	}

	// We can't use the standard mechanism of dispatching platform specific logic since the mixin plugin
	// will read the bridge version, which will cause re-entrance errors
	private static final BridgeVersion VERSION = valueOf(readBridgeImplVersion());

	private static String readBridgeImplVersion() {
		var metadata = FabricLoader.getInstance()
			.getModContainer(DolphinClientCommon.MODID)
			.or(() -> FabricLoader.getInstance().getModContainer(DolphinClientCommon.LEGACY_MODID))
			.orElseThrow()
			.getMetadata();
		var custom = metadata.getCustomValue("dolphinclient:bridge_impl_version");
		if (custom == null) {
			custom = metadata.getCustomValue("axolotlclient:bridge_impl_version");
		}
		return custom.getAsString();
	}

	public static BridgeVersion version() {
		return VERSION;

	}

	public boolean isCurrent() {
		return this == version();
	}

	public static boolean is26OrLater() {
		return version() == V26_1 || version() == V26_2;
	}
}
