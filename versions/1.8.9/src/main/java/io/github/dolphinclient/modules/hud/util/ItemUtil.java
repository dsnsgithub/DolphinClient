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

package io.github.dolphinclient.modules.hud.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Formatting;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

@UtilityClass
public class ItemUtil {
	// The scaling stuff wasn't a problem on 1.8.9 so no need to create more complicated stuff

	public static void renderGuiItemModel(ItemStack stack, int x, int y) {
		if (stack != null && stack.getItem() != null) {
			Lighting.turnOnGui();
			GlStateManager.pushMatrix();
			Minecraft.getInstance().getItemRenderer().renderGuiItemModel(stack, x, y);
			GlStateManager.popMatrix();
			Lighting.turnOff();
		}
	}

	public static void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel,
											int textColor, boolean shadow) {
		Lighting.turnOnGui();
		GlStateManager.pushMatrix();
		GlStateManager.color4f(textColor >> 24 & 0xff, textColor >> 16 & 0xff, textColor >> 8 & 0xff, textColor & 0xff);
		if (stack != null) {
			if (stack.size != 1 || countLabel != null) {
				String string = countLabel == null ? String.valueOf(stack.size) : countLabel;
				if (countLabel == null && stack.size < 1) {
					string = Formatting.RED + String.valueOf(stack.size);
				}

				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableBlend();
				renderer.draw(string, (float) (x + 19 - 2 - renderer.getWidth(string)), (float) (y + 6 + 3),
					16777215, shadow);
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}

			if (stack.isDamaged()) {
				int i = (int) Math.round(13.0 - (double) stack.getDamage() * 13.0 / (double) stack.getMaxDamage());
				int j = (int) Math.round(255.0 - (double) stack.getDamage() * 255.0 / (double) stack.getMaxDamage());
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableTexture();
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuffer();
				renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
				renderGuiQuad(bufferBuilder, x + 2, y + 13, 12, 1, (255 - j) / 4, 64, 0, 255);
				renderGuiQuad(bufferBuilder, x + 2, y + 13, i, 1, 255 - j, j, 0, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableTexture();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}
		}

		Lighting.turnOff();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1, 1, 1, 1);
	}

	private static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green,
									  int blue, int alpha) {
		buffer.begin(7, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).nextVertex();
		Tesselator.getInstance().end();
	}
}
