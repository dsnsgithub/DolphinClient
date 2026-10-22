/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.modules.hud.gui.component;

import java.util.Map;
import java.util.Optional;

import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.modules.hud.gui.layout.SnapAnchorType;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public interface HudEntry extends Identifiable, Configurable, Positionable {

	boolean isEnabled();

	void setEnabled(boolean value);

	default boolean tickable() {
		return false;
	}

	default boolean overridesF3() {
		return false;
	}

	default void tick() {
	}

	default void init() {
	}

	default void postConfigLoad() {
	}

	default double getDefaultX() {
		return 0;
	}

	default double getDefaultY() {
		return 0;
	}

	void render(AxoRenderContext ctx, float delta);

	void renderPlaceholder(AxoRenderContext ctx, float delta);

	void setHovered(boolean hovered);

	boolean isHovered();

	boolean isHidden();

	boolean supportsScaling();

	Optional<SnapAnchorType> dependsOnX(HudEntry entry);

	Optional<SnapAnchorType> dependsOnY(HudEntry entry);

	void addBoundsDependency(HudEntry dependency, SnapAnchorType type);

	void removeBoundsDependencyX(HudEntry entry);

	void removeBoundsDependencyY(HudEntry entry);

	void clearBoundsDependencies();

	Map<HudEntry, SnapAnchorType> getDependenciesX();

	Map<HudEntry, SnapAnchorType> getDependenciesY();
}
