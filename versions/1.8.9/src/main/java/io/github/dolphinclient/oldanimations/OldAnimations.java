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

package io.github.dolphinclient.oldanimations;

import io.github.dolphinclient.DolphinClient;
import io.github.dolphinclient.modules.AbstractModule;
import io.github.dolphinclient.oldanimations.config.OldAnimationsConfig;

/**
 * 1.7.10 animation/visual options, vendored from AxolotlClient OldAnimations
 * and shown in the Right Shift client menu.
 */
public class OldAnimations extends AbstractModule {

	public static final String MODID = "dolphinclient-oldanimations";

	@Override
	public void init() {
		OldAnimationsConfig.instance.initConfig();
		DolphinClient.config().addCategory(OldAnimationsConfig.instance.getCategory());
	}

	@Override
	public void lateInit() {
		OldAnimationsConfig.instance.onConfigLoaded();
	}
}
