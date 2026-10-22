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
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.modules.hud.util.Rectangle;
import io.github.dolphinclient.util.ClientColors;
import lombok.Getter;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
@SuppressWarnings("unused")
public abstract class BoxHudEntry extends AbstractHudEntry {

	private final boolean backgroundAllowed;

	protected BooleanOption background = new BooleanOption("background", true);
	protected ColorOption backgroundColor = new ColorOption("bgcolor", new Color(0x64000000));

	protected BooleanOption outline = new BooleanOption("outline", false);
	protected ColorOption outlineColor = new ColorOption("outlinecolor", ClientColors.WHITE);

	protected IntegerOption backgroundPadding = new IntegerOption("hud.background_padding", 0, val -> {
		setWidth(getContentWidth() + val * 2);
		setHeight(getContentHeight() + val * 2);
		onBoundsUpdate();
	}, 0, 15);
	protected BooleanOption roundBackground = new BooleanOption("hud.round_background", false);
	protected IntegerOption backgroundRounding = new IntegerOption("hud.background_rounding", 10, 1, 20);

	@Getter
	private Rectangle contentBounds = new Rectangle(0, 0, 0, 0);
	@Getter
	private DrawPosition contentPos = new DrawPosition(0, 0);
	@Getter
	private final int initialContentWidth, initialContentHeight;

	public BoxHudEntry(int width, int height, boolean backgroundAllowed) {
		super(width, height);
		this.initialContentWidth = width;
		this.initialContentHeight = height;
		this.backgroundAllowed = backgroundAllowed;
		if (!backgroundAllowed) {
			background = null;
			backgroundColor = null;
			outline = null;
			outlineColor = null;
			roundBackground = null;
			backgroundRounding = null;
		}
	}

	@Override
	public void postConfigLoad() {
		setContentWidth(initialContentWidth);
		setContentHeight(initialContentHeight);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		if (backgroundAllowed) {
			options.add(background);
			options.add(backgroundColor);
			options.add(outline);
			options.add(outlineColor);
			options.add(backgroundPadding);
			options.add(roundBackground);
			options.add(backgroundRounding);
		}
		return options;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		ctx.br$pushMatrix();
		scale(ctx);
		if (backgroundAllowed) {
			var bounds = getBounds();
			if (background.get() && backgroundColor.get().getAlpha() > 0) {
				if (roundBackground.get()) {
					ctx.br$fillRectRound(bounds, backgroundColor.get(), Math.min(Math.min(getHeight(), getWidth()) / 2f, backgroundRounding.get()));
				} else {
					ctx.br$fillRect(bounds, backgroundColor.get());
				}
			}
			if (outline.get() && outlineColor.get().getAlpha() > 0) {
				if (roundBackground.get()) {
					ctx.br$outlineRectRound(bounds, outlineColor.get(), Math.min(Math.min(getHeight(), getWidth()) / 2f, backgroundRounding.get()));
				} else {
					ctx.br$outlineRect(bounds, outlineColor.get());
				}
			}
		}
		renderComponent(ctx, delta);
		ctx.br$popMatrix();
	}

	@Override
	public void setBounds() {
		super.setBounds();
		var bounds = getBounds();
		var padding = backgroundPadding.get();
		if (padding == 0) {
			contentBounds = bounds.copy();
			contentPos = new DrawPosition(bounds.x(), bounds.y());
		} else {
			contentBounds = new Rectangle(bounds.x() + padding, bounds.y() + padding, bounds.width() - padding * 2, bounds.height() - padding * 2);
			contentPos = new DrawPosition(bounds.x() + padding, bounds.y() + padding);
		}
	}

	public int getContentX() {
		return contentPos.x();
	}

	public int getContentY() {
		return contentPos.y();
	}

	public void setContentX(int x) {
		var padding = backgroundPadding.get();
		if (padding == 0) setX(x);
		else setX(x - padding);
	}

	public void setContentY(int y) {
		var padding = backgroundPadding.get();
		if (padding == 0) setY(y);
		else setY(y - padding);
	}

	public int getContentWidth() {
		return contentBounds.width();
	}

	public int getContentHeight() {
		return contentBounds.height();
	}

	public void setContentWidth(int width) {
		var padding = backgroundPadding.get();
		if (padding == 0) setWidth(width);
		else setWidth(width + padding * 2);
	}

	public void setContentHeight(int height) {
		var padding = backgroundPadding.get();
		if (padding == 0) setHeight(height);
		else setHeight(height + padding * 2);
	}

	public int getTrueContentX() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getTrueX();
		return getTrueX() + (int) (padding * getScale());
	}

	public int getTrueContentY() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getTrueY();
		return getTrueY() + (int) (padding * getScale());
	}

	public int getRawTrueContentX() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getRawTrueX();
		return getRawTrueX() + (int) (padding * getScale());
	}

	public int getRawTrueContentY() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getRawTrueY();
		return getRawTrueY() + (int) (padding * getScale());
	}

	public int getTrueContentWidth() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getTrueWidth();
		return getTrueWidth() + (int) (padding * getScale() * 2);
	}

	public int getTrueContentHeight() {
		var padding = backgroundPadding.get();
		if (padding == 0) return getTrueHeight();
		return getTrueHeight() + (int) (padding * getScale() * 2);
	}

	public abstract void renderComponent(AxoRenderContext ctx, float delta);

	@Override
	public void renderPlaceholder(AxoRenderContext ctx, float delta) {
		ctx.br$pushMatrix();
		renderPlaceholderBackground(ctx);
		scale(ctx);
		renderPlaceholderComponent(ctx, delta);
		ctx.br$popMatrix();
		renderPlaceholderGrabCorners(ctx);
		hovered = false;
	}

	public abstract void renderPlaceholderComponent(AxoRenderContext ctx, float delta);
}
