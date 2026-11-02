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

package io.github.dolphinclient.modules.hud.gui.hud.simple;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.dolphinclient.bridge.Platform;
import io.github.dolphinclient.bridge.entity.AxoEntity;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.math.Vec3;
import io.github.dolphinclient.bridge.util.AxoI18n;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.SimpleTextHudEntry;

// https://github.com/AxolotlClient/AxolotlClient-mod/blob/4ae2678bfe9e0908be1a7a34e61e689c8005ae0a/src/main/java/io/github/dolphinclient/modules/hud/gui/hud/ReachDisplayHud.java
// https://github.com/DarkKronicle/KronHUD/blob/703b87a7c938ba25da9105d731b70d3bc66efd1e/src/main/java/io/github/darkkronicle/kronhud/gui/hud/simple/ReachHud.java
public class ReachHud extends SimpleTextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "reachhud");
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);

	private String currentDist;
	private long lastTime = 0;

	private static double getAttackDistance(AxoEntity attacking, AxoEntity receiving) {
		var recHitboxRadii = receiving.br$getBoundingBoxHalfDimensions();
		var attPos = attacking.br$getEyePos(1.0f).sub(receiving.br$getPos().add(0, receiving.br$getHeight()/2f, 0)).negate();
		return Math.max(distToBox(attPos, recHitboxRadii), 0);
	}

	// The distance to a box defined by its radii (half side length). Also known as the distance field. Remember?
	// See https://iquilezles.org/articles/distfunctions/
	private static double distToBox(Vec3 pos, Vec3 boxRadii) {
		var q = pos.abs().sub(boxRadii);
		return q.max(0).len() + Math.min(Math.max(q.x(), Math.max(q.y(), q.z())), 0);
	}

	public ReachHud() {
		super(true);
	}

	@Override
	public void init() {
		Events.PLAYER_ATTACK.register((attacking, receiving) -> {
			double distance = getAttackDistance(attacking, receiving);

			StringBuilder format = new StringBuilder("0");
			if (decimalPlaces.get() > 0) {
				format.append(".");
				format.append("0".repeat(Math.max(0, decimalPlaces.get())));
			}
			DecimalFormat formatter = new DecimalFormat(format.toString());
			formatter.setRoundingMode(RoundingMode.HALF_UP);
			currentDist = formatter.format(distance);
			lastTime = Platform.getMeasuringTimeMs();
		});
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(decimalPlaces);
		return options;
	}

	@Override
	public String getValue() {
		if (currentDist == null) {
			return "0";
		} else if (lastTime + 2000 < Platform.getMeasuringTimeMs()) {
			currentDist = null;
			return "0";
		}
		return currentDist;
	}

	@Override
	public String getPlaceholderValue() {
		return "3.45";
	}

	@Override
	public String getLabel() {
		return AxoI18n.translate("blocks");
	}
}
