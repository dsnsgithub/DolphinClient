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

package io.github.axolotlclient.modules.zoom;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.modules.AbstractCommonModule;
import io.github.axolotlclient.util.MathUtil;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Based on
 * <a href="https://github.com/LogicalGeekBoy/logical_zoom/blob/master/src/main/java/com/logicalgeekboy/logical_zoom/LogicalZoom.java">Logical Zoom</a>
 */

public class Zoom extends AbstractCommonModule {

	@Getter
	private final AxoKeybinding key = AxoKeybinding.create(AxoKeys.KEY_C, "key.zoom");
	private final FloatOption zoomDivisor = new FloatOption("zoomDivisor", 4F, 1F, 16F);
	private final FloatOption zoomSpeed = new FloatOption("zoomSpeed", 7.5F, 1F, 10F);
	private final BooleanOption zoomScrolling = new BooleanOption("zoomScrolling", false);
	private final BooleanOption decreaseSensitivity = new BooleanOption("decreaseSensitivity", true);
	private final BooleanOption smoothCamera = new BooleanOption("smoothCamera", false);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	private boolean toggled = false;
	@Getter(AccessLevel.PUBLIC)
	private static final Zoom Instance = new Zoom();
	public boolean active;
	private Double originalSensitivity;
	private boolean originalSmoothCamera;
	private float targetFactor = 1;
	private float divisor;
	private float lastAnimatedFactor = 1;
	private float animatedFactor = 1;
	private double lastReturnedFov;
	public final OptionCategory zoom = OptionCategory.create("zoom");

	public float getFov(float current, float tickDelta) {
		float result =
			current * (zoomSpeed.get() == 10 ? targetFactor : MathUtil.lerp(tickDelta, lastAnimatedFactor, animatedFactor));

		if (lastReturnedFov != 0 && lastReturnedFov != result) {
			client.br$notifyLevelRenderer();
		}
		lastReturnedFov = result;

		return result;
	}

	public void update() {
		if (shouldStart()) {
			start();
		} else if (shouldStop()) {
			stop();
		}
	}

	private boolean shouldStart() {
		return keyHeld() && !active;
	}

	private void start() {
		active = true;
		setDivisor(zoomDivisor.get());
		setOptions();
	}

	private boolean shouldStop() {
		return !keyHeld() && active;
	}

	private void stop() {
		active = false;
		targetFactor = 1;
		restoreOptions();
	}

	private boolean keyHeld() {
		if (toggle.get()) {
			if (key.br$consumeClick()) {
				toggled = !toggled;
			}
			return toggled;
		}
		return key.br$isPressed();
	}

	private void setDivisor(float value) {
		divisor = value;
		targetFactor = 1F / value;
	}

	public void setOptions() {
		originalSensitivity = client.br$getGameOptions().br$getSensitivity();

		if (smoothCamera.get()) {
			originalSmoothCamera = client.br$getGameOptions().br$getSmoothCamera();
			client.br$getGameOptions().br$setSmoothCamera(true);
		}

		updateSensitivity();
	}

	public void restoreOptions() {
		client.br$getGameOptions().br$setSensitivity(originalSensitivity);
		client.br$getGameOptions().br$setSmoothCamera(originalSmoothCamera);
	}

	private void updateSensitivity() {
		if (decreaseSensitivity.get()) {
			client.br$getGameOptions().br$setSensitivity(originalSensitivity / (divisor * divisor));
		}
	}

	public boolean scroll(double amount) {
		if (active && zoomScrolling.get() && amount != 0) {
			setDivisor((float) Math.max(1, divisor + (amount / Math.abs(amount))));
			updateSensitivity();
			return true;
		}

		return false;
	}

	@Override
	public void init() {
		zoom.add(zoomDivisor, zoomSpeed, zoomScrolling, decreaseSensitivity, smoothCamera, toggle);

		DolphinClientCommon.getInstance().getConfig().rendering.add(zoom);

		active = false;

		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "key.zoom.increase")
			.br$registerOnConsumeClick(() -> scroll(zoomSpeed.get() / 2f));
		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "key.zoom.decrease")
			.br$registerOnConsumeClick(() -> scroll(-zoomSpeed.get() / 2f));
	}

	public void tick() {
		lastAnimatedFactor = animatedFactor;
		animatedFactor += (targetFactor - animatedFactor) * (zoomSpeed.get() / 10F);
	}
}
