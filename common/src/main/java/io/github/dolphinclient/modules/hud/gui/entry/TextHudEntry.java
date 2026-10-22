/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.modules.hud.gui.entry;


import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.util.DefaultOptions;
import io.github.dolphinclient.util.ClientColors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public abstract class TextHudEntry extends BoxHudEntry implements DynamicallyPositionable {

	protected final ColorOption textColor = new ColorOption("textcolor", ClientColors.WHITE);
	protected final BooleanOption shadow = new BooleanOption("shadow", getShadowDefault());
	protected final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(getDefaultAnchor(), this);

	public TextHudEntry(int width, int height, boolean backgroundAllowed) {
		super(width, height, backgroundAllowed);
	}

	protected boolean getShadowDefault() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(textColor);
		options.add(shadow);
		options.add(anchor);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_LEFT;
	}
}
