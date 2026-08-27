/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.autoboop;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public class FilterListConfigurationScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	final List<String> filters;
	private FiltersList filtersList;
	private final Screen parent;

	public FilterListConfigurationScreen(List<String> filters, Screen parent) {
		super(I18n.translate("autoboop.filters.configure"));
		this.filters = filters;
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		this.filtersList = addDrawableChild(new FiltersList(this));
		VanillaButtonWidget resetButton = new VanillaButtonWidget(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20,
			I18n.translate("autoboop.filters.clear"), button -> {
			filters.clear();
			filtersList.reload();
			DolphinClientCommon.getInstance().saveConfig();
		});
		addDrawableChild(resetButton);
		addDrawableChild(new VanillaButtonWidget(width / 2 + 4, height - 33 / 2 - 10, 150, 20,
			I18n.translate("gui.done"), button -> this.closeScreen()));
	}

	public void closeScreen() {
		this.minecraft.openScreen(this.parent);
		filtersList.apply();
		DolphinClientCommon.getInstance().saveConfig();
	}
}
