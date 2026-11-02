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

package io.github.dolphinclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.util.ClientColors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class CompassHud extends TextHudEntry {
	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "compasshud");

	private final IntegerOption widthOption = new IntegerOption("width", width, this::updateWidth, 100, 800);

	private final ColorOption lookingBox = new ColorOption("lookingbox", new Color(0x80000000));
	private final ColorOption degreesColor = new ColorOption("degreescolor", new Color(-1));
	private final ColorOption majorIndicatorColor = new ColorOption("majorindicator", new Color(-1));
	private final ColorOption minorIndicatorColor = new ColorOption("minorindicator", new Color(0xCCFFFFFF));
	private final ColorOption cardinalColor = new ColorOption("cardinalcolor", ClientColors.WHITE);
	private final ColorOption semiCardinalColor = new ColorOption("semicardinalcolor", new Color(0xFFAAAAAA));
	private final BooleanOption invert = new BooleanOption("invert_direction", false);
	private final BooleanOption showDegrees = new BooleanOption("showdegrees", true);

	public CompassHud() {
		super(240, 33, false);
	}

	private static Indicator getIndicator(int degrees) {
		if (degrees % 90 == 0) {
			return Indicator.CARDINAL;
		}
		if (degrees % 45 == 0) {
			return Indicator.SEMI_CARDINAL;
		}
		return Indicator.SMALL;
	}

	private static String getCardString(Indicator indicator, int degrees) {
		if (indicator == Indicator.CARDINAL) {
			return switch (degrees) {
				case 0 -> "N";
				case 90 -> "E";
				case 180 -> "S";
				case 270 -> "W";
				default -> "NaD";
			};
		}
		return switch (degrees) {
			case 45 -> "NE";
			case 135 -> "SE";
			case 225 -> "SW";
			case 315 -> "NW";
			default -> "NaD";
		};
	}

	private void updateWidth(int newWidth) {
		setWidth(newWidth);
		onBoundsUpdate();
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		renderCompass(context);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		renderCompass(context);
	}

	public void renderCompass(AxoRenderContext context) {
		// N = 0
		// E = 90
		// S = 180
		// W = 270
		if (client.br$getPlayer() == null) {
			return;
		}
		float halfWidth = width / 2f;
		float degrees = (client.br$getPlayer().br$getYaw() + 180) % 360;
		if (degrees < -180) {
			degrees += 360;
		}
		float start = degrees - 150 + 360;
		//        float end = degrees + 150 + 360;
		int startIndicator = ((int) (start + 8) / 15) * 15;
		int amount = 21;
		//        int endIndicator = startIndicator + 15 * amount;
		int dist = width / (amount);
		DrawPosition pos = getPos();
		int x = pos.x();
		int y = pos.y() + 1;
		context.br$fillRect(pos.x() + (int) halfWidth - 1, pos.y(), 3, 11, lookingBox.get());
		if (showDegrees.get()) {
			context.br$drawCenteredString(String.valueOf((int) degrees), x + (int) halfWidth, y + 20, degreesColor.get(), shadow.get());
		}
		float shift = (startIndicator - start) / 15f * dist;
		if (invert.get()) {
			shift = dist - shift;
		}
		context.br$pushMatrix();
		context.br$translateMatrix(shift, 0);
		for (int i = 0; i < amount; i++) {
			int d;
			if (invert.get()) {
				d = (startIndicator + ((amount - i - 2) * 15)) % 360;
			} else {
				d = (startIndicator + i * 15) % 360;
			}
			int innerX = x + dist * (i + 1);
			Indicator indicator = getIndicator(d);

			float trueDist;
			if (invert.get()) {
				trueDist = ((amount - i) * dist) - shift;
			} else {
				trueDist = ((i + 1) * dist) - shift;
			}

			float targetOpacity = 1 - Math.abs((halfWidth - trueDist)) / halfWidth;
			if (indicator == Indicator.CARDINAL) {
				context.br$fillRect(innerX, y, 1, 9, majorIndicatorColor.get()
					.withAlpha((int) (majorIndicatorColor.get().getAlpha() * targetOpacity)));
				Color color = cardinalColor.get();
				color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
				if (color.getAlpha() > 0) {
					context.br$drawCenteredString(getCardString(indicator, d), innerX + 1, y + 10, color, shadow.get());
				}
			} else if (indicator == Indicator.SEMI_CARDINAL) {
				Color color = semiCardinalColor.get();
				color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
				if (color.getAlpha() > 0) {
					context.br$drawCenteredString(getCardString(indicator, d), innerX + 1, y + 1, color, shadow.get());
				}
			} else {
				// We have to call .color() here so that transparency stays
				context.br$fillRect(innerX, y, 1, 5, minorIndicatorColor.get()
					.withAlpha((int) (minorIndicatorColor.get().getAlpha() * targetOpacity)).toInt());
			}
		}

		context.br$popMatrix();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(widthOption);
		options.add(showDegrees);
		options.add(invert);
		options.add(lookingBox);
		options.add(degreesColor);
		options.add(cardinalColor);
		options.add(semiCardinalColor);
		options.add(majorIndicatorColor);
		options.add(minorIndicatorColor);
		return options;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_MIDDLE;
	}

	private enum Indicator {
		CARDINAL, SEMI_CARDINAL, SMALL
	}
}
