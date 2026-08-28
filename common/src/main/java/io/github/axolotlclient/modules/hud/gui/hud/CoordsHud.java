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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.bridge.math.Vec3;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.ClientColors;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class CoordsHud extends TextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "coordshud");

	private final ColorOption secondColor = new ColorOption("secondtextcolor", ClientColors.WHITE);
	private final ColorOption firstColor = new ColorOption("firsttextcolor", ClientColors.SELECTOR_BLUE);
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, val -> {
		StringBuilder format = new StringBuilder("0");
		if (val > 0) {
			format.append(".");
			format.append("0".repeat(val));
		}
		CoordsHud.this.format = new DecimalFormat(format.toString());
		CoordsHud.this.format.setRoundingMode(RoundingMode.FLOOR);
	}, 0, 15);
	private final BooleanOption minimal = new BooleanOption("minimal", false);
	private final BooleanOption biome = new BooleanOption("show_biome", false);
	private final StringOption delimiter = new StringOption("coordshud.delimiter", " ");
	private final StringOption separator = new StringOption("coordshud.separator", ", ");
	private final ColorOption separatorColor = new ColorOption("coordshud.separator.color", firstColor.getDefault());
	private final BooleanOption showNetherConversions = new BooleanOption("coordshud.show_nether_conversions", false);
	private final BooleanOption showCCount = new BooleanOption("debugcounters.ccount", false);
	private final BooleanOption showECount = new BooleanOption("debugcounters.ecount", false);
	private final BooleanOption showPCount = new BooleanOption("debugcounters.pcount", false);

	private DecimalFormat format = new DecimalFormat("0");

	public CoordsHud() {
		super(79, 31, true);
	}

	@Override
	public void postConfigLoad() {
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			format.append("0".repeat(decimalPlaces.get()));
		}
		CoordsHud.this.format = new DecimalFormat(format.toString());
		CoordsHud.this.format.setRoundingMode(RoundingMode.FLOOR);
	}

	/**
	 * Get direction. 1 = North, 2 North East, 3 East, 4 South East...
	 *
	 * @param yaw the player's yaw
	 * @return the direction, 0-360 degrees.
	 */
	public static int getDirection(double yaw) {
		yaw %= 360;

		if (yaw < 0) {
			yaw += 360;
		}
		int[] directions = {0, 23, 68, 113, 158, 203, 248, 293, 338, 360};
		for (int i = 0; i < directions.length; i++) {
			int min = directions[i];
			int max;
			if (i + 1 >= directions.length) {
				max = directions[0];
			} else {
				max = directions[i + 1];
			}
			if (yaw >= min && yaw < max) {
				if (i >= 8) {
					return 1;
				}
				return i + 1;
			}
		}
		return 0;
	}

	public static String getXDir(int dir) {
		return switch (dir) {
			case 3 -> "++";
			case 2, 4 -> "+";
			case 6, 8 -> "-";
			case 7 -> "--";
			default -> "";
		};
	}

	public static String getZDir(int dir) {
		return switch (dir) {
			case 5 -> "++";
			case 4, 6 -> "+";
			case 8, 2 -> "-";
			case 1 -> "--";
			default -> "";
		};
	}

	private void doRender(AxoRenderContext context, double yaw, Vec3 playerPos, String biomeName, boolean isOverworld, boolean isNether, String chunkDebug, String entityDebug, String particleDebug) {
		DrawPosition pos = getContentPos();

		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		String del = delimiter.get();

		int width, height;
		int xStart = pos.x() + 2;

		String fx = format.format(playerPos.x());
		String fy = format.format(playerPos.y());
		String fz = format.format(playerPos.z());
		if (minimal.get()) {
			int currPos = xStart;
			String separator = this.separator.get();
			currPos = context.br$drawString("XYZ" + del, currPos, pos.y() + 2, firstColor.get(), shadow.get());
			currPos = context.br$drawString(fx, currPos, pos.y() + 2, secondColor.get(),
				shadow.get());
			currPos = context.br$drawString(separator, currPos, pos.y() + 2, separatorColor.get(), shadow.get());
			currPos = context.br$drawString(fy, currPos, pos.y() + 2, secondColor.get(),
				shadow.get());
			currPos = context.br$drawString(separator, currPos, pos.y() + 2, separatorColor.get(), shadow.get());
			currPos = context.br$drawString(fz, currPos, pos.y() + 2, secondColor.get(),
				shadow.get());
			width = currPos - pos.x() + 2;
			height = 11;
			if (showNetherConversions.get() && (isNether || isOverworld)) {
				var name = AxoI18n.translate(isNether ? "coordshud.dimension.overworld" : "coordshud.dimension.nether");
				var factor = isNether ? 8f : 1 / 8f;
				currPos = xStart;
				currPos = context.br$drawString(name + del, currPos, pos.y() + 2, firstColor.get(), shadow.get());
				currPos = context.br$drawString("XYZ" + del, currPos, pos.y() + 2, firstColor.get(), shadow.get());
				currPos = context.br$drawString(format.format(playerPos.x() * factor), currPos, pos.y() + 2, secondColor.get(), shadow.get());
				currPos = context.br$drawString(format.format(playerPos.y() * factor), currPos, pos.y() + 2, secondColor.get(), shadow.get());
				currPos = context.br$drawString(format.format(playerPos.z() * factor), currPos, pos.y() + 2, secondColor.get(), shadow.get());
				width = Math.max(width, currPos - pos.x()+2);
				height += 10;
			}
		} else {
			int xEnd;
			int yEnd = pos.y() + 2;
			int nextX = context.br$drawString("X" + del, xStart, yEnd, firstColor.get(), shadow.get());
			xEnd = context.br$drawString(fx, nextX, yEnd,
				secondColor.get(), shadow.get());
			yEnd += 10;

			nextX = context.br$drawString("Y" + del, xStart, yEnd, firstColor.get(), shadow.get());
			xEnd = Math.max(xEnd, context.br$drawString(fy, nextX, yEnd,
				secondColor.get(), shadow.get()));

			yEnd += 10;

			nextX = context.br$drawString("Z" + del, xStart, yEnd, firstColor.get(), shadow.get());

			xEnd = Math.max(xEnd, context.br$drawString(fz, nextX, yEnd, secondColor.get(), shadow.get()));

			yEnd += 10;

			xEnd = Math.max(pos.x() + 60, xEnd + 4);

			context.br$drawString(direction, xEnd, pos.y() + 12, firstColor.get(), shadow.get());

			context.br$drawString(getXDir(dir), xEnd, pos.y() + 2, secondColor.get(),
				shadow.get());
			context.br$drawString(getZDir(dir), xEnd, pos.y() + 22, secondColor.get(),
				shadow.get());
			xEnd += 14;

			if (showNetherConversions.get() && (isNether || isOverworld)) {
				var name = AxoI18n.translate(isNether ? "coordshud.dimension.overworld" : "coordshud.dimension.nether");
				var offset = context.br$getFont().br$getWidth(name + del);
				var factor = isNether ? 8f : 1 / 8f;
				nextX = context.br$drawString(name + del + "X" + del, xStart, yEnd, firstColor.get(), shadow.get());
				xEnd = Math.max(xEnd, context.br$drawString(format.format(playerPos.x() * factor), nextX, yEnd, secondColor.get(), shadow.get()) + 4);
				yEnd += 10;

				nextX = context.br$drawString("Y" + del, xStart + offset, yEnd, firstColor.get(), shadow.get());
				xEnd = Math.max(xEnd, context.br$drawString(format.format(playerPos.y() * factor), nextX, yEnd, secondColor.get(), shadow.get()) + 4);
				yEnd += 10;

				nextX = context.br$drawString("Z" + del, xStart + offset, yEnd, firstColor.get(), shadow.get());
				xEnd = Math.max(xEnd, context.br$drawString(format.format(playerPos.z() * factor), nextX, yEnd, secondColor.get(), shadow.get()) + 4);
				yEnd += 10;
			}

			width = xEnd - pos.x();
			height = yEnd + 1 - pos.y();
		}

		if (biome.get()) {
			int bX = context.br$drawString(AxoI18n.translate("coordshud.biome") + del, xStart, height + pos.y(), firstColor.get(), shadow.get());
			width = Math.max(width + pos.x() - 1, context.br$drawString(biomeName, bX, height + pos.y(), secondColor.get(), shadow.get())) - pos.x() + 1;
			height += 10;
		}

		int[] size = {width, height};
		appendDebugLine(context, xStart, pos, size, chunkDebug);
		appendDebugLine(context, xStart, pos, size, entityDebug);
		appendDebugLine(context, xStart, pos, size, particleDebug);
		width = size[0];
		height = size[1];

		boolean changed = false;

		if (getContentWidth() != width) {
			setContentWidth(width);
			changed = true;
		}

		if (getContentHeight() != height) {
			setContentHeight(height);
			changed = true;
		}

		if (changed) {
			onBoundsUpdate();
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		if (client.br$getPlayer() == null) {
			return;
		}

		doRender(context, client.br$getPlayer().br$getYaw() + 180, client.br$getPlayer().br$getPos(), client.br$getWorld().br$getBiomeName(client.br$getPlayer().br$getPos()), client.br$getWorld().br$isOverworld(), client.br$getWorld().br$isNether(),
			showCCount.get() ? client.br$getChunkDebugInfo() : null,
			showECount.get() ? client.br$getEntityDebugInfo() : null,
			showPCount.get() ? client.br$getParticleDebugInfo() : null);
	}

	private void appendDebugLine(AxoRenderContext context, int xStart, DrawPosition pos, int[] size, String line) {
		if (line == null || line.isEmpty()) {
			return;
		}
		int xEnd = context.br$drawString(line, xStart, pos.y() + size[1], secondColor.get(), shadow.get());
		size[0] = Math.max(size[0], xEnd - pos.x() + 1);
		size[1] += 10;
	}

	public String getWordedDirection(int dir) {
		return switch (dir) {
			case 1 -> "N";
			case 2 -> "NE";
			case 3 -> "E";
			case 4 -> "SE";
			case 5 -> "S";
			case 6 -> "SW";
			case 7 -> "W";
			case 8 -> "NW";
			case 0 -> "?";
			default -> "";
		};
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		doRender(context, 180, new Vec3(109.2325, 180.8981, -5098.32698), "Plains", true, false,
			showCCount.get() ? "C: 186/15000 (s) D: 10, pC: 000, pU: 00, aB: 20" : null,
			showECount.get() ? "E: 695/3001, SD: 12" : null,
			showPCount.get() ? "P: 200" : null);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.remove(textColor);
		options.add(firstColor);
		options.add(secondColor);
		options.add(decimalPlaces);
		options.add(minimal);
		options.add(biome);
		options.add(showNetherConversions);
		options.add(showCCount);
		options.add(showECount);
		options.add(showPCount);
		options.add(delimiter);
		options.add(separator);
		options.add(separatorColor);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_MIDDLE;
	}
}
