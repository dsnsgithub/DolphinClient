/*
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

package io.github.axolotlclient.oldanimations;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.config.profiles.ProfileAware;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;

/**
 * 1.7.10 animation/visual options, vendored from AxolotlClient OldAnimations
 * and shown in the Right Shift client menu.
 */
public class OldAnimations extends AbstractModule implements ProfileAware {

	public static final String MODID = "axolotlclient-oldanimations";
	public static final boolean AXOLOTLCLIENT = true;

	@Override
	public void init() {
		OldAnimationsConfig.instance.initConfig();
		Events.CLIENT_READY.register(() ->
			AxolotlClient.config().getConfig().add(OldAnimationsConfig.instance.getCategory(), false));
	}

	@Override
	public void saveConfig() {
		ConfigManager manager = manager();
		if (manager != null) {
			manager.save();
		}
	}

	@Override
	public void reloadConfig() {
		ConfigManager manager = manager();
		if (manager != null) {
			manager.load();
		}
	}

	private static ConfigManager manager() {
		return AxolotlClientConfig.getInstance().getConfigManager(OldAnimationsConfig.instance.getCategory());
	}
}
