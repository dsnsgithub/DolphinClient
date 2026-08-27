/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.resource.Identifier;

public class BadgeRenderer {
	public static void renderNametagBadge(Entity entity) {
		if (!(entity instanceof PlayerEntity) || entity.isSneaking()) {
			return;
		}

		if (!DolphinClient.config().showBadges.get() || !UserRequest.getOnline(entity.getUuid().toString())) {
			return;
		}

		TextRenderer textRenderer = Minecraft.getInstance().textRenderer;

		int x = -(textRenderer
			.getWidth(entity.getUuid() == Minecraft.getInstance().player.getUuid()
				? (NickHider.getInstance().hideOwnName.get() ? NickHider.getInstance().hiddenNameSelf.get()
				: entity.getDisplayName().getFormattedString())
				: (NickHider.getInstance().hideOtherNames.get() ? NickHider.getInstance().hiddenNameOthers.get()
				: entity.getDisplayName().getFormattedString()))
			/ 2
			+ (DolphinClient.config().customBadge.get() ? textRenderer
			.getWidth(" " + DolphinClient.config().badgeText.get()) : 10));

		GlStateManager.color4f(1, 1, 1, 1);

		if (DolphinClient.config().customBadge.get())
			textRenderer.draw(DolphinClient.config().badgeText.get(), x, 0, -1, DolphinClient.config().useShadows.get());
		else {
			Minecraft.getInstance().getTextureManager().bind((Identifier) DolphinClientCommon.BADGE_PATH);
			GuiElement.drawTexture(x, 0, 0, 0, 8, 8, 8, 8);
		}
	}
}
