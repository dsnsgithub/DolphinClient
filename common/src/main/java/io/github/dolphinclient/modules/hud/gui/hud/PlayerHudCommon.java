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

package io.github.dolphinclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.dolphinclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.util.DefaultOptions;

public abstract class PlayerHudCommon extends BoxHudEntry implements DynamicallyPositionable {
	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "playerhud");
	protected final DoubleOption rotation = new DoubleOption("rotation", 0d, 0d, 360d);
	protected final BooleanOption dynamicRotation = new BooleanOption("dynamicrotation", true);
	protected final BooleanOption autoHide = new BooleanOption("autoHide", false);
	protected final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(this);
	protected float lastYawOffset = 0;
	protected float yawOffset = 0;
	protected float lastYOffset = 0;
	protected float yOffset = 0;
	protected long hide;

	public PlayerHudCommon() {
		super(62, 94, true);
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		lastYawOffset = yawOffset;
		yawOffset *= .93f;
		lastYOffset = yOffset;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (autoHide.get()) {
			if (isPerformingAction()) {
				hide = -1;
			} else if (hide == -1) {
				hide = System.currentTimeMillis();
			}

			if (hide != -1 && System.currentTimeMillis() - hide > 500) {
				return;
			}
		}
		super.render(ctx, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext ctx, float delta) {
		renderPlayer(ctx, false, getRawTrueContentX(), getRawTrueContentY(), delta);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		renderPlayer(ctx, true, getRawTrueContentX(), getRawTrueContentY(), 0);
	}

	protected abstract void renderPlayer(AxoRenderContext ctx, boolean placeholder, double x, double y, float delta);

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(dynamicRotation);
		options.add(rotation);
		options.add(autoHide);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	protected abstract boolean isPerformingAction();
}
