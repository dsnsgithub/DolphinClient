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

package io.github.dolphinclient.util.options.rounded;

import java.util.Base64;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.screen.GraphicsEditorScreen;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.GraphicsWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.RoundedButtonWidget;
import io.github.dolphinclient.util.notifications.Notifications;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

@SuppressWarnings("unused")
public class AxoGraphicsWidget extends GraphicsWidget {
	private final GraphicsOption option;

	public AxoGraphicsWidget(int x, int y, int width, int height, GraphicsOption option) {
		super(x, y, width, height, option);
		this.option = option;
	}

	@Override
	public void onPress() {
		client.openScreen(new AxoGraphicsEditorScreen(client.screen, this.option));
	}

	public static class AxoGraphicsEditorScreen extends GraphicsEditorScreen {

		public AxoGraphicsEditorScreen(Screen parent, GraphicsOption option) {
			super(parent, option);
		}

		@Override
		public void init() {
			super.init();

			var clear = (RoundedButtonWidget) children().get(children().size() - 1);
			var buttonX = clear.getX();
			var buttonY = clear.getY();
			var buttonWidth = clear.getWidth();
			addDrawableChild(new RoundedButtonWidget(buttonX, buttonY + 24, I18n.translate("graphics.copy_text"),
				btn -> setClipboard(Base64.getEncoder().encodeToString(option.get().getPixelData())))).setWidth(buttonWidth);
			addDrawableChild(new RoundedButtonWidget(buttonX, buttonY + 48, I18n.translate("graphics.paste_text"),
				btn -> {
					try {
						option.get().setPixelData(Base64.getDecoder().decode(getClipboard()));
					} catch (IllegalArgumentException e) {
						Notifications.getInstance().addStatus("graphics.paste_text.failed", "graphics.paste_text.failed.desc");
					}
				})).setWidth(buttonWidth);
		}

		@Override
		protected int getCurrentHeight() {
			return super.getCurrentHeight() - 24 * 2;
		}
	}
}
