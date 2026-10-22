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

package io.github.dolphinclient.modules.screenshotUtils;

import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.util.ClientColors;
import io.github.dolphinclient.util.MathUtil;
import io.github.dolphinclient.util.notifications.toasts.Toast;
import io.github.dolphinclient.util.notifications.toasts.ToastManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import org.jetbrains.annotations.NotNull;

public class ScreenshotToast implements Toast {
	private static final int TOAST_HEIGHT = 100;
	private static final int DISPLAY_TIME_MILLIS = 3500;
	private final ImageInstance image;
	private final int width;
	private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

	public ScreenshotToast(ImageInstance instance) {
		this.image = instance;
		this.width = ((int) (instance.image().getWidth() * (TOAST_HEIGHT / (float) instance.image().getHeight())));
	}

	@Override
	public int width() {
		return 2 + width;
	}

	@Override
	public int height() {
		return 2 + TOAST_HEIGHT;
	}

	@Override
	public Toast.@NotNull Visibility getWantedVisibility() {
		return this.wantedVisibility;
	}

	@Override
	public void update(@NotNull ToastManager toastManager, long visibilityTime) {
		var time = DISPLAY_TIME_MILLIS * toastManager.getNotificationDisplayTimeMultiplier();
		this.wantedVisibility = visibilityTime < time ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}

	@Override
	public void render(AxoRenderContext graphics, TextRenderer font, long visibilityTime) {
		var color = ScreenshotUtils.getInstance().toastBorderColor.get().toInt();
		graphics.br$fillRect(0, 0, width(), height(), color);
		GlStateManager.enableTexture();
		GlStateManager.enableBlend();
		GlStateManager.color3f(1, 1, 1);
		Minecraft.getInstance().getTextureManager().bind(image.id());
		GuiElement.drawTexture(1, 1, 0, 0, width, TOAST_HEIGHT, width, TOAST_HEIGHT);
		float prog = MathUtil.lerp(MathUtil.clamp(visibilityTime / 300f, 0f, 1f), 1f, 0f);
		graphics.br$fillRect(1, 1, width, TOAST_HEIGHT, ClientColors.ARGB.color(prog * ClientColors.ARGB.alphaFloat(color), color));
	}

	@Override
	public long dolphinclient$animationDuration() {
		return 300;
	}
}
