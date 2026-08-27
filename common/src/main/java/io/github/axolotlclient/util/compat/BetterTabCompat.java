/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.util.compat;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.BiConsumer;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.Module;
import net.fabricmc.loader.api.FabricLoader;

public class BetterTabCompat implements Module {
	@Override
	public void init() {
		if (FabricLoader.getInstance().isModLoaded("bettertab")) {
			var logger = DolphinClientCommon.getInstance().getLogger();
			try {
				var badgeManagerClass = Class.forName("tab.bettertab.tabList.BadgeManager");
				var handle = MethodHandles.lookup().findStatic(badgeManagerClass,"registerBadgeProvider", MethodType.methodType(void.class, BiConsumer.class));
				var consumer = (BiConsumer<AxoPlayerListEntry, List<AxoIdentifier>>)(entry, list) -> {
					if (DolphinClientCommon.getInstance().getConfig().showBadges.get() && UserRequest.getOnline(entry.br$getId().toString())) {
						list.add(DolphinClientCommon.BADGE_PATH);
					}
				};
				handle.invoke(consumer);
			} catch (Throwable e) {
				logger.warn("Failed to register badge provider for BetterTab");
			}
		}
	}
}
