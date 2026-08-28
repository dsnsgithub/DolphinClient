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

package io.github.axolotlclient.modules.hud.gui.entry;

import java.util.*;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.modules.hud.HudManagerCommon;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.SnapAnchorType;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.MathUtil;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import lombok.Getter;
import lombok.Setter;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public abstract class AbstractHudEntry implements HudEntry {
	@Getter
	protected final ForceableBooleanOption enabled = DefaultOptions.getEnabled(this);
	protected final DoubleOption scale = DefaultOptions.getScale(this);
	protected final AxoMinecraftClient client = AxoMinecraftClient.getInstance();
	protected final BooleanOption hide = new BooleanOption("hud.hide", false);
	private final DoubleOption x = DefaultOptions.getX(getDefaultX());
	private final DoubleOption y = DefaultOptions.getY(getDefaultY());
	private final Map<HudEntry, SnapAnchorType> xDependencies = new HashMap<>();
	private final Map<HudEntry, SnapAnchorType> yDependencies = new HashMap<>();
	@Setter
	@Getter
	protected int width;
	@Setter
	@Getter
	protected int height;
	@Setter
	@Getter
	protected boolean hovered = false;
	protected boolean supportsScaling = true;
	@Getter
	private Rectangle trueBounds;
	private Rectangle renderBounds;
	private DrawPosition truePosition;
	private DrawPosition renderPosition;
	private OptionCategory category;

	public AbstractHudEntry(int width, int height) {
		this.width = width;
		this.height = height;
		truePosition = new DrawPosition(0, 0);
		renderPosition = new DrawPosition(0, 0);
		renderBounds = new Rectangle(0, 0, 1, 1);
		trueBounds = new Rectangle(0, 0, 1, 1);
	}

	public static float intToFloat(int current, int max, int offset) {
		return MathUtil.clamp((float) (current) / (max - offset), 0, 1);
	}

	public static int floatToInt(float percent, int max, int offset) {
		return MathUtil.clamp(Math.round((max - offset) * percent), 0, max);
	}

	public void renderPlaceholderBackground(AxoRenderContext context) {
		var bounds = getTrueBounds();
		if (hovered) {
			context.br$fillRect(bounds, ClientColors.ARGB.color(100, ClientColors.SELECTOR_BLUE.toInt()));
		} else {
			context.br$fillRect(bounds, ClientColors.ARGB.color(50, ClientColors.WHITE.toInt()));
		}
		context.br$outlineRect(bounds, Colors.BLACK);
	}

	public void renderPlaceholderGrabCorners(AxoRenderContext context) {
		if (!supportsScaling() || !movable()) return;
		var c = HudManagerCommon.getInstance().grabCornerColor.get();
		if (c.getAlpha() == 0) return;
		var bounds = getTrueBounds();
		var grabTolerance = Math.min(HudManagerCommon.HUD_RESCALE_GRAB_TOLERANCE, Math.min(bounds.width(), bounds.height())/2);
		var color = c.toInt();
		float rounding = grabTolerance-.5f;
		context.br$fillRectRoundVarying(bounds.x(), bounds.y(), grabTolerance, grabTolerance, color, 0, 0, rounding, 0);
		context.br$fillRectRoundVarying(bounds.x(), bounds.yEnd() - grabTolerance, grabTolerance, grabTolerance, color, 0, 0, 0, rounding);
		context.br$fillRectRoundVarying(bounds.xEnd() - grabTolerance, bounds.yEnd() - grabTolerance, grabTolerance, grabTolerance, color, rounding, 0, 0, 0);
		context.br$fillRectRoundVarying(bounds.xEnd() - grabTolerance, bounds.y(), grabTolerance, grabTolerance, color, 0, rounding, 0, 0);
	}

	public void scale(AxoRenderContext context) {
		float scale = getScale();
		context.br$scaleMatrix(scale, scale);
	}

	@Override
	public int getRawTrueX() {
		return truePosition.x();
	}

	public void setX(int x) {
		this.x.set((double) intToFloat(x, (int) AxoWindow.getWindow().br$getScaledWidth(), 0));
		onBoundsUpdate();
	}

	@Override
	public void setPos(int x, int y) {
		this.x.set((double) intToFloat(x, (int) AxoWindow.getWindow().br$getScaledWidth(), 0));
		this.y.set((double) intToFloat(y, (int) AxoWindow.getWindow().br$getScaledHeight(), 0));
		onBoundsUpdate();
	}

	@Override
	public float getScale() {
		return scale.get().floatValue();
	}

	public void setScale(float scale) {
		this.scale.set((double) scale);
	}

	public int getRawX() {
		return getPos().x;
	}

	@Override
	public int getRawTrueY() {
		return truePosition.y();
	}

	public int getRawY() {
		return getPos().y();
	}

	public void setY(int y) {
		this.y.set((double) intToFloat(y, (int) AxoWindow.getWindow().br$getScaledHeight(), 0));
		onBoundsUpdate();
	}

	/**
	 * Gets the hud's bounds when the matrix has already been scaled.
	 *
	 * @return The bounds.
	 */
	public Rectangle getBounds() {
		return renderBounds;
	}

	/**
	 * The position, in scaled screen pixels, this entry's top left corner is locked to.
	 * Entries that always render in one spot (instead of wherever the user dragged them)
	 * override this; the returned position is used as is, without the usual clamping to
	 * the window that a user placed entry gets.
	 *
	 * @param window The window to position against
	 * @return The locked position, or {@code null} to follow the stored x/y options
	 */
	protected DrawPosition getLockedPosition(AxoWindow window) {
		return null;
	}

	public void setBounds() {
		final var window = AxoWindow.getWindow();

		if (window == null) {
			truePosition = new DrawPosition(0, 0);
			renderPosition = new DrawPosition(0, 0);
			renderBounds = new Rectangle(0, 0, 1, 1);
			trueBounds = new Rectangle(0, 0, 1, 1);
			return;
		}
		int trueWidth = (int)(getWidth() * getScale());
		int trueHeight =(int)(getHeight() * getScale());
		int scaledX, scaledY;
		DrawPosition locked = getLockedPosition(window);
		if (locked != null) {
			scaledX = locked.x();
			scaledY = locked.y();
		} else {
			scaledX = floatToInt(x.get().floatValue(), (int) window.br$getScaledWidth(), 0) - offsetTrueWidth();
			scaledY = floatToInt(y.get().floatValue(), (int) window.br$getScaledHeight(), 0)
				- offsetTrueHeight();
			if (scaledX < 0) {
				scaledX = 0;
			}
			if (scaledY < 0) {
				scaledY = 0;
			}
			if (trueWidth < window.br$getScaledWidth() && scaledX + trueWidth > window.br$getScaledWidth()) {
				scaledX = (int) (window.br$getScaledWidth() - trueWidth);
			}
			if (trueHeight < window.br$getScaledHeight()
				&& scaledY + trueHeight > window.br$getScaledHeight()) {
				scaledY = (int) (window.br$getScaledHeight() - trueHeight);
			}
		}
		truePosition.x = scaledX;
		truePosition.y = scaledY;
		renderPosition = truePosition.divide(getScale());
		renderBounds = new Rectangle(renderPosition.x(), renderPosition.y(), getWidth(), getHeight());
		trueBounds = new Rectangle(scaledX, scaledY, trueWidth, trueHeight);
	}

	@Override
	public DrawPosition getPos() {
		return renderPosition;
	}

	@Override
	public DrawPosition getTruePos() {
		return truePosition;
	}

	@Override
	public int getTrueWidth() {
		if (trueBounds == null) {
			return HudEntry.super.getTrueWidth();
		}
		return trueBounds.width();
	}

	@Override
	public int getTrueHeight() {
		if (trueBounds == null) {
			return HudEntry.super.getTrueHeight();
		}
		return trueBounds.height();
	}

	@Override
	public void onBoundsUpdate() {
		setBounds();
		HudManagerCommon.getInstance().updateBoundsDependencies(this);
	}

	public OptionCategory getAllOptions() {
		if (category == null) {
			List<Option<?>> options = getSaveOptions();
			category = OptionCategory.create(getNameKey());
			options.forEach(category::add);
		}
		return category;
	}

	/**
	 * Returns a list of options that should be saved. By default, this includes {@link #getConfigurationOptions()}
	 *
	 * @return a list of options.
	 */
	@Override
	public List<Option<?>> getSaveOptions() {
		List<Option<?>> options = getConfigurationOptions();
		options.add(x);
		options.add(y);
		return options;
	}

	/**
	 * Returns a list of options that should be shown in configuration screens
	 *
	 * @return List of options
	 */
	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = new ArrayList<>();
		options.add(enabled);
		options.add(scale);
		return options;
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	@Override
	public boolean isEnabled() {
		return enabled.get();
	}

	@Override
	public void setEnabled(boolean value) {
		enabled.set(value);
	}

	@Override
	public boolean isHidden() {
		return hide.get();
	}

	@Override
	public boolean supportsScaling() {
		return supportsScaling;
	}

	@Override
	public Optional<SnapAnchorType> dependsOnX(HudEntry entry) {
		return Optional.ofNullable(xDependencies.get(entry));
	}

	@Override
	public Optional<SnapAnchorType> dependsOnY(HudEntry entry) {
		return Optional.ofNullable(yDependencies.get(entry));
	}

	@Override
	public void addBoundsDependency(HudEntry dependency, SnapAnchorType type) {
		switch (type) {
			case X_X, X_XEND, XEND_X, XEND_XEND -> xDependencies.put(dependency, type);
			case Y_Y, Y_YEND, YEND_Y, YEND_YEND -> yDependencies.put(dependency, type);
		}
	}

	@Override
	public void clearBoundsDependencies() {
		xDependencies.clear();
		yDependencies.clear();
	}

	@Override
	public void removeBoundsDependencyX(HudEntry entry) {
		xDependencies.remove(entry);
	}

	@Override
	public void removeBoundsDependencyY(HudEntry entry) {
		yDependencies.remove(entry);
	}

	@Override
	public Map<HudEntry, SnapAnchorType> getDependenciesX() {
		return xDependencies;
	}

	@Override
	public Map<HudEntry, SnapAnchorType> getDependenciesY() {
		return yDependencies;
	}
}
