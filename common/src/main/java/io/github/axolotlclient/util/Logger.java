/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
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

package io.github.axolotlclient.util;

import io.github.axolotlclient.DolphinClientCommon;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;

public sealed interface Logger permits Logger.Slf4jLogger {
	void info(String msg, Object... args);

	void warn(String msg, Object... args);

	void error(String msg, Object... args);

	void debug(String msg, Object... args);

	final class Slf4jLogger implements Logger {

		private final org.slf4j.Logger delegate = LoggerFactory.getLogger("DolphinClient");
		private static final String prefix = FabricLoader.getInstance().isDevelopmentEnvironment() ? "" : "(DolphinClient) ";

		public void info(String msg, Object... args) {
			//noinspection StringConcatenationArgumentToLogCall
			delegate.info(prefix + msg, args);
		}

		public void warn(String msg, Object... args) {
			//noinspection StringConcatenationArgumentToLogCall
			delegate.warn(prefix + msg, args);
		}

		public void error(String msg, Object... args) {
			//noinspection StringConcatenationArgumentToLogCall
			delegate.error(prefix + msg, args);
		}

		public void debug(String msg, Object... args) {
			if (DolphinClientCommon.getInstance().getConfig().debugLogOutput.get()) {
				//noinspection StringConcatenationArgumentToLogCall
				delegate.info(prefix + "[DEBUG] " + msg, args);
			}
		}
	}

}
