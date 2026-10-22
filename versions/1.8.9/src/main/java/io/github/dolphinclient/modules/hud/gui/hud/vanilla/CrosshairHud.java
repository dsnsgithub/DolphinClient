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

package io.github.dolphinclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.dolphinclient.modules.hud.gui.entry.AbstractHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.util.DrawUtil;
import io.github.dolphinclient.util.RenderUtil;
import io.github.dolphinclient.util.ClientColors;
import io.github.dolphinclient.util.Util;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class CrosshairHud extends AbstractHudEntry implements DynamicallyPositionable {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "crosshairhud");

	private final EnumOption<Crosshair> type = new EnumOption<>("crosshair_type", Crosshair.class, Crosshair.CROSS);
	private final BooleanOption showInF5 = new BooleanOption("showInF5", false);
	private final BooleanOption applyBlend = new BooleanOption("applyBlend", true);
	private final BooleanOption overrideF3 = new BooleanOption("overrideF3", false);
	private final ColorOption defaultColor = new ColorOption("defaultcolor", ClientColors.WHITE);
	private final ColorOption entityColor = new ColorOption("entitycolor", ClientColors.SELECTOR_RED);
	private final ColorOption containerColor = new ColorOption("blockcolor", ClientColors.SELECTOR_BLUE);

	private final GraphicsOption customTextureGraphics = new GraphicsOption("customTextureGraphics",
		new int[][]{
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		});

	private final EnumOption<TargetCrosshair> entityCrosshairType = new EnumOption<>("entity_crosshair_type", TargetCrosshair.class, TargetCrosshair.CROSSHAIR_TYPE_DEFAULT);
	private final EnumOption<TargetCrosshair> containerCrosshairType = new EnumOption<>("container_crosshair_type", TargetCrosshair.class, TargetCrosshair.CROSSHAIR_TYPE_DEFAULT);
	private final GraphicsOption entityCustomTextureGraphics = new GraphicsOption("entity_crosshair_graphics", customTextureGraphics.getDefault());
	private final GraphicsOption containerCustomTextureGraphics = new GraphicsOption("container_crosshair_graphics", customTextureGraphics.getDefault());
	private final Minecraft client = (Minecraft) super.client;

	public CrosshairHud() {
		super(15, 15);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public boolean movable() {
		return false;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(hide);
		options.add(type);
		options.add(customTextureGraphics);
		options.add(entityCrosshairType);
		options.add(entityCustomTextureGraphics);
		options.add(containerCrosshairType);
		options.add(containerCustomTextureGraphics);
		options.add(showInF5);
		options.add(overrideF3);
		options.add(applyBlend);
		options.add(defaultColor);
		options.add(entityColor);
		options.add(containerColor);
		return options;
	}

	@Override
	public boolean overridesF3() {
		return overrideF3.get();
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.5F;
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		if (!(client.options.perspective == 0) && !showInF5.get()) {
			return;
		}
		if (client.options.debugEnabled && !overridesF3()) {
			return;
		}

		GlStateManager.enableAlphaTest();

		GlStateManager.pushMatrix();
		scale(context);
		var mode = getMode();
		Color color = (switch (mode) {
			case DEFAULT -> defaultColor;
			case ENTITY -> entityColor;
			case CONTAINER -> containerColor;
		}).get();
		Crosshair defaultType = this.type.get();
		var typeOption = (switch (mode) {
			case DEFAULT -> this.type;
			case ENTITY -> entityCrosshairType;
			case CONTAINER -> containerCrosshairType;
		}).get();
		var type = typeOption instanceof Crosshair ? typeOption : ((TargetCrosshair) typeOption).asCrosshair(defaultType);
		GlStateManager.color4f((float) color.getRed() / 255, (float) color.getGreen() / 255,
			(float) color.getBlue() / 255, 1F);
		if (ClientColors.ARGB.opaque(color.toInt()) == ClientColors.WHITE.toInt() && applyBlend.get()) {
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(775, 769, 1, 0);
		} else {
			GlStateManager.disableBlend();
		}

		int x = getPos().x;
		int y = getPos().y + 1;
		if (type.equals(Crosshair.DOT)) {
			RenderUtil.fillBlend(x + (width / 2) - 1, y + (height / 2) - 2, 3, 3, color);
		} else if (type.equals(Crosshair.CROSS)) {
			RenderUtil.fillBlend(x + (width / 2) - 5, y + (height / 2) - 1, 6, 1, color);
			RenderUtil.fillBlend(x + (width / 2) + 1, y + (height / 2) - 1, 5, 1, color);
			RenderUtil.fillBlend(x + (width / 2), y + (height / 2) - 6, 1, 5, color);
			RenderUtil.fillBlend(x + (width / 2), y + (height / 2), 1, 5, color);
		} else if (type.equals(Crosshair.TEXTURE)) {
			Minecraft.getInstance().getTextureManager().bind(GuiElement.ICONS_LOCATION);

			// Draw crosshair
			//noinspection DataFlowIssue
			client.gui.drawTexture((int) (((Util.getWindow().getScaledWidth() / getScale()) - 14) / 2),
				(int) (((Util.getWindow().getScaledHeight() / getScale()) - 14) / 2), 0, 0, 16, 16);
		} else if (type.equals(Crosshair.CUSTOM)) {
			Util.bindTexture(switch (mode) {
				case DEFAULT -> customTextureGraphics;
				case ENTITY ->
					typeOption == TargetCrosshair.CROSSHAIR_TYPE_DEFAULT ? customTextureGraphics : entityCustomTextureGraphics;
				case CONTAINER ->
					typeOption == TargetCrosshair.CROSSHAIR_TYPE_DEFAULT ? customTextureGraphics : containerCustomTextureGraphics;
			});
			DrawUtil.drawTexture(x + (width / 2) - (15 / 2), y + height / 2 - 15 / 2 - 1, 0, 0, 15, 15, 15, 15);
		}
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.disableAlphaTest();
	}

	private CrosshairMode getMode() {
		HitResult hit = client.crosshairTarget;
		if (hit == null || hit.type == null) {
			return CrosshairMode.DEFAULT;
		} else if (hit.type == HitResult.Type.ENTITY) {
			return CrosshairMode.ENTITY;
		} else if (hit.type == HitResult.Type.BLOCK) {
			BlockPos blockPos = hit.getPos();
			World world = this.client.world;
			if (world.getBlockState(blockPos).getBlock() != null
				&& (world.getBlockState(blockPos).getBlock() instanceof ChestBlock
				|| world.getBlockState(blockPos).getBlock() instanceof EnderChestBlock
				|| world.getBlockState(blockPos).getBlock() instanceof HopperBlock)) {
				return CrosshairMode.CONTAINER;
			}
		}
		return CrosshairMode.DEFAULT;
	}

	@Override
	public void renderPlaceholder(AxoRenderContext context, float delta) {
		// Shouldn't need this...
	}

	@Override
	public AnchorPoint getAnchor() {
		return AnchorPoint.MIDDLE_MIDDLE;
	}

	private enum CrosshairMode {
		DEFAULT, ENTITY, CONTAINER
	}

	private enum Crosshair {
		CROSS, DOT, DIRECTION, TEXTURE, CUSTOM
	}

	private enum TargetCrosshair {
		CROSS, DOT, DIRECTION, TEXTURE, CUSTOM, CROSSHAIR_TYPE_DEFAULT;

		public Crosshair asCrosshair(Crosshair defaultType) {
			return switch (this) {
				case CROSS -> Crosshair.CROSS;
				case DOT -> Crosshair.DOT;
				case DIRECTION -> Crosshair.DIRECTION;
				case TEXTURE -> Crosshair.TEXTURE;
				case CUSTOM -> Crosshair.CUSTOM;
				case CROSSHAIR_TYPE_DEFAULT -> defaultType;
			};
		}
	}
}
