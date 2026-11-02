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

package io.github.dolphinclient.bridge.impl;

import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.render.AxoFont;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.render.AxoSprite;
import io.github.dolphinclient.bridge.util.AxoText;
import io.github.dolphinclient.util.DrawUtil;
import io.github.dolphinclient.modules.hud.util.ItemUtil;
import io.github.axolotlclient.rendering.font.StringSplitter;
import io.github.dolphinclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Unique;

public class AxoRenderContextImpl extends io.github.axolotlclient.rendering.DrawUtil implements AxoRenderContext {
	@Nullable
	private static AxoRenderContextImpl INSTANCE;

	public static AxoRenderContext getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AxoRenderContextImpl();
		}

		return INSTANCE;
	}


	private final Minecraft client = Minecraft.getInstance();
	private final StringSplitter splitter = new StringSplitter((cp, style) -> client.textRenderer.getWidth(style.asString() + Character.toString(cp)));

	@Override
	public void br$popMatrix() {
		GlStateManager.popMatrix();
	}

	@Override
	public void br$pushMatrix() {
		GlStateManager.pushMatrix();
	}

	@Override
	public void br$scaleMatrix(float sx, float sy) {
		GlStateManager.scalef(sx, sy, 1);
	}

	@Override
	public void br$translateMatrix(float x, float y) {
		GlStateManager.translatef(x, y, 0);
	}

	@Override
	public void br$rotateMatrix(float ang) {
		GlStateManager.rotatef((float) Math.toDegrees(ang), 0, 0, 1);
	}

	@Override
	public void br$pushScissor(int x, int y, int w, int h) {
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.pushScissor(x, y, w, h);
	}

	@Override
	public void br$popScissor() {
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.popScissor();
	}

	@Override
	public void br$fillRect(int x, int y, int width, int height, int color) {
		DrawUtil.fillRect(x, y, width, height, color);
	}

	@Override
	public void br$fillRectGradientVert(int x, int y, int width, int height, int color1, int color2) {
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		BufferBuilder consumer = Tesselator.getInstance().getBuffer();
		consumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		consumer.vertex(x + width, y, 0).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >> 24 & 255);
		consumer.vertex(x, y, 0).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >> 24 & 255);
		consumer.vertex(x, y + height, 0).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >> 24 & 255);
		consumer.vertex(x + width, y + height, 0).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >> 24 & 255);
		Tesselator.getInstance().end();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}

	@Override
	public void br$fillRectGradientHoriz(int x, int y, int width, int height, int color1, int color2) {
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		BufferBuilder consumer = Tesselator.getInstance().getBuffer();
		consumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		consumer.vertex(x, y, 0).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >> 24 & 255);
		consumer.vertex(x, y + height, 0).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, color1 >> 24 & 255);
		consumer.vertex(x + width, y + height, 0).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >> 24 & 255);
		consumer.vertex(x + width, y, 0).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, color2 >> 24 & 255);
		Tesselator.getInstance().end();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
	}

	@Override
	public void br$fillRectRoundGradient(int x, int y, int width, int height, int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight, float roundingPx) {
		axolotlclient_rendering$roundedRectGradient(x, y, x + width, y + height, colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight, roundingPx);
	}

	@Override
	public void br$fillSegment(int x0, int y0, int x1, int y1, int colorX0Y0, int colorX0Y1, int colorX1Y1, int colorX1Y0, float radius) {
		axolotlclient_rendering$segment(x0, y0, x1, y1, colorX0Y0, colorX0Y1, colorX1Y1, colorX1Y0, radius);
	}

	@Override
	public void br$outlineRect(int x, int y, int width, int height, int color) {
		DrawUtil.outlineRect(x, y, width, height, color);
	}

	@Override
	public void br$fillRectRound(int x, int y, int width, int height, int color, float rounding) {
		axolotlclient_rendering$roundedRect(x, y, x + width, y + height, color, rounding);
	}

	@Override
	public void br$fillRectRoundVarying(int x, int y, int width, int height, int color, float roundingTL, float roundingBL, float roundingBR, float roundingTR) {
		axolotlclient_rendering$roundedRectVarying(x, y, x + width, y + height, color, roundingTL, roundingBL, roundingBR, roundingTR);
	}

	@Override
	public void br$outlineRectRound(int x, int y, int width, int height, int color, float rounding) {
		axolotlclient_rendering$outlineRoundedRect(x, y, x + width, y + height, color, rounding, 0.5f);
	}

	@Override
	public void br$outlineRectRoundVarying(int x, int y, int width, int height, int color, float roundingTL, float roundingBL, float roundingBR, float roundingTR, float outlineWidth) {
		axolotlclient_rendering$outlineRoundedRectVarying(x, y, x + width, y + height, color, roundingTL, roundingBL, roundingBR, roundingTR, outlineWidth);
	}

	@Override
	public void br$drawTexture(AxoSprite sprite, int x, int y, int width, int height, int color) {
		((AxoSpriteImpl) sprite).draw(client, x, y, width, height, color);
	}

	@Override
	public int br$drawString(String value, int x, int y, int color, boolean shadow) {
		return client.textRenderer.draw(value, x, y, color, shadow);
	}

	@Override
	public int br$drawString(AxoText value, int x, int y, int color, boolean shadow) {
		return br$drawString(((Text) value).getFormattedString(), x, y, color, shadow);
	}

	@Override
	public void br$drawCenteredString(String value, int x, int y, int color, boolean shadow) {
		AxoRenderContext.super.br$drawCenteredString(value, x, y, color, shadow);
	}

	@Unique
	private void drawWordWrap(TextRenderer renderer, Text text, int x, int y, int width, boolean shadow, int color) {
		for (var orderedText : splitter.splitLines(text, width, new Style())) {
			br$drawString(orderedText, x, y, color, shadow);
			y += renderer.fontHeight;
		}
	}

	@Unique
	private void drawCenteredWordWrap(TextRenderer renderer, Text text, int x, int y, int width, boolean shadow, int color) {
		for (var orderedText : splitter.splitLines(text, width, new Style())) {
			br$drawCenteredString(orderedText, x - br$getFont().br$getWidth(orderedText) / 2, y, color, shadow);
			y += renderer.fontHeight;
		}
	}

	@Unique
	private void drawCenteredCenteredWordWrap(TextRenderer renderer, Text text, int x, int y, int width, boolean shadow, int color) {
		var lines = splitter.splitLines(text, width, new Style());
		y -= (lines.size() * renderer.fontHeight) / 2;
		for (var orderedText : lines) {
			br$drawCenteredString(orderedText, x - br$getFont().br$getWidth(orderedText) / 2, y, color, shadow);
			y += renderer.fontHeight;
		}
	}

	@Override
	public void br$drawWordWrap(String text, int x, int y, int width, boolean shadow, int color) {
		drawWordWrap(client.textRenderer, (Text) AxoText.literal(text), x, y, width, shadow, color);
	}

	@Override
	public void br$drawWordWrap(AxoText text, int x, int y, int width, boolean shadow, int color) {
		drawWordWrap(client.textRenderer, (Text) text, x, y, width, shadow, color);
	}

	@Override
	public void br$drawCenteredWordWrap(String text, int x, int y, int width, boolean shadow, int color) {
		drawCenteredWordWrap(client.textRenderer, (Text) AxoText.literal(text), x, y, width, shadow, color);
	}

	@Override
	public void br$drawCenteredWordWrap(AxoText text, int x, int y, int width, boolean shadow, int color) {
		drawCenteredWordWrap(client.textRenderer, (Text) text, x, y, width, shadow, color);
	}

	@Override
	public void br$drawCenteredCenteredWordWrap(String text, int centerX, int centerY, int width, boolean shadow, int color) {
		drawCenteredCenteredWordWrap(client.textRenderer, (Text) AxoText.literal(text), centerX, centerY, width, shadow, color);
	}

	@Override
	public void br$drawCenteredCenteredWordWrap(AxoText text, int centerX, int centerY, int width, boolean shadow, int color) {
		drawCenteredCenteredWordWrap(client.textRenderer, (Text) text, centerX, centerY, width, shadow, color);
	}

	@Override
	public AxoFont br$getFont() {
		return client.textRenderer;
	}

	@Override
	public void br$renderGuiItemModel(AxoItemStack stack, int x, int y) {
		final var vanilla = Bridge.unwrapStack(stack);

		if (vanilla != null) {
			ItemUtil.renderGuiItemModel(vanilla, x, y);
		}
	}

	@Override
	public void br$renderGuiItemOverlay(AxoItemStack stack, int x, int y, String countLabel, int textColor, boolean shadow) {
		ItemUtil.renderGuiItemOverlay(
			client.textRenderer,
			Bridge.unwrapStack(stack), x, y, countLabel, textColor,
			shadow
		);
	}

	@Override
	public int br$guiWidth() {
		return Util.getWindow().getWidth();
	}

	@Override
	public int br$guiHeight() {
		return Util.getWindow().getHeight();
	}
}
