/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.util.GuiLayout;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen {

	@WrapOperation(method = "init", at = @At(value = "NEW", target = "(IIILjava/lang/String;)Lnet/minecraft/client/gui/widget/ButtonWidget;"))
	private ButtonWidget axolotlclient$keepCancelBelowStatus(int id, int x, int y, String message, Operation<ButtonWidget> original) {
		int textY = this.height / 2 - 50;
		int fontHeight = this.textRenderer != null ? this.textRenderer.fontHeight : 9;
		y = GuiLayout.moveBelowIfOverlapping(y, 20, textY, fontHeight, 8);
		return original.call(id, x, y, message);
	}
}
