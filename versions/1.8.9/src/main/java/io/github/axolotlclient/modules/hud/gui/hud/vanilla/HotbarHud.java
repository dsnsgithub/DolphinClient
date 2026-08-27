/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.resource.Identifier;

public class HotbarHud extends TextHudEntry {

	public static final Identifier ID = new Identifier(DolphinClientCommon.MODID, "hotbarhud");
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");

	private final Minecraft client = (Minecraft) super.client;

	public HotbarHud() {
		super(182, 22, false);
		supportsScaling = false;
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		if (this.client.getCamera() instanceof PlayerEntity) {
			super.render(context, delta);
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		PlayerEntity playerEntity = (PlayerEntity) this.client.getCamera();
		if (playerEntity == null || playerEntity.inventory == null || playerEntity.inventory.items == null) {
			return;
		}
		DrawPosition pos = getPos();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		this.client.getTextureManager().bind(WIDGETS_TEXTURE);

		context.br$pushMatrix();
		GlStateManager.translatef(0, 0, -90);
		DrawUtil.drawTexture(pos.x, pos.y, 0, 0, 182, 22, 256, 256);
		DrawUtil.drawTexture(pos.x - 1 + playerEntity.inventory.selectedSlot * 20, pos.y - 1, 0, 22, 24, 22, 256, 256);
		context.br$pushMatrix();
		GlStateManager.enableRescaleNormal();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		Lighting.turnOnGui();

		for (int j = 0; j < 9; ++j) {
			int k = pos.x + j * 20 + 3;
			int l = pos.y + 3;
			if (playerEntity.inventory.items[j] != null) {
				ItemUtil.renderGuiItemModel(playerEntity.inventory.items[j], k, l);
				ItemUtil.renderGuiItemOverlay(Minecraft.getInstance().textRenderer,
					playerEntity.inventory.items[j], k, l, null, textColor.get().toInt(), shadow.get());
			}
		}

		Lighting.turnOff();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		context.br$popMatrix();
		context.br$popMatrix();
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		DrawPosition pos = getPos();
		DrawUtil.drawCenteredString(Minecraft.getInstance().textRenderer, getName(), pos.x + width / 2,
			pos.y + height / 2 - 4, -1, true);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public boolean overridesF3() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> list = new ArrayList<>();
		list.add(enabled);
		list.add(hide);
		list.add(shadow);
		list.add(anchor);
		return list;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.BOTTOM_MIDDLE;
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.96;
	}
}
