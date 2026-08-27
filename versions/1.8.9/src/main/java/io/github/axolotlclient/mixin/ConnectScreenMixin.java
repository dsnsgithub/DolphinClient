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

package io.github.axolotlclient.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla anchors the status line ("Connecting to the server...", "Logging in...") to the middle of
 * the screen ({@code height / 2 - 50}), but the cancel button to the top ({@code height / 4 + 132}).
 * Since those two anchors move at different speeds, the button climbs into the status line on tall
 * guis - at a scaled height of roughly 690 to 810 (a 1440p window at gui scale 2 gives 720) the
 * button is drawn on top of the text and cuts it in half.
 * <p>
 * Push the button down so it always stays below the status line.
 */
@Mixin(ConnectScreen.class)
public class ConnectScreenMixin extends Screen {

	@Inject(method = "init", at = @At("TAIL"))
	private void dolphinclient$keepCancelButtonBelowStatus(CallbackInfo ci) {
		/* the status line vanilla draws in render(), plus its shadow and a bit of breathing room */
		int minY = height / 2 - 50 + textRenderer.fontHeight + 8;
		for (ButtonWidget button : buttons) {
			if (button.y < minY) {
				button.y = minY;
			}
		}
	}
}
