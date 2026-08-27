/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.modules.hud.gui.hud.item;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.item.AxoItems;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.util.CommonUtil;

public class InventoryHud extends BoxHudEntry implements DynamicallyPositionable {
	public static final AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "inventoryhud");
	private static final Supplier<List<AxoItemStack>> PLACEHOLDER = CommonUtil.memoize(() -> Stream.of(
		IntStream.range(0, 9).mapToObj(x -> AxoItemStack.of(AxoItems.STONE)),
		IntStream.range(0, 9).mapToObj(x -> (AxoItemStack) null),
		Stream.of(
			AxoItemStack.of(AxoItems.STONE_SWORD),
			AxoItemStack.of(AxoItems.STONE_PICKAXE),
			AxoItemStack.of(AxoItems.STONE_AXE),
			AxoItemStack.of(AxoItems.STONE_SHOVEL),
			AxoItemStack.of(AxoItems.STONE_HOE),
			null, null, null,
			AxoItemStack.of(AxoItems.GLOWSTONE_DUST, 63)
		)
	).flatMap(x -> x).toList());

	private static final int ITEM_SIZE = 18;
	private static final int ITEM_TILE_SIZE = 16;

	private final BooleanOption dynamic = new BooleanOption("dynamic", false);
	private final BooleanOption itemBackground = new BooleanOption("inventoryhud.item_background", true);
	private final ColorOption itemBackgroundColor = new ColorOption("inventoryhud.item_background_color", backgroundColor.getDefault());
	private final BooleanOption alwaysShowItemBackgrounds = new BooleanOption("inventoryhud.always_show_item_backgrounds", false);
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(AnchorPoint.MIDDLE_MIDDLE, this);

	public InventoryHud() {
		super(164, 56, true);
		int max = (ITEM_TILE_SIZE + 2) / 2;
		backgroundRounding = new IntegerOption("background_rounding", max, 1, max);
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.76;
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		if (dynamic.get()) {
			boolean render = false;
			for (AxoItemStack stack : client.br$getPlayer().br$getInventory().br$getNonEquipmentItems()) {
				if (stack != null && !stack.br$isEmpty()) {
					render = true;
					break;
				}
			}

			if (!render) return;
		}
		super.render(ctx, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		render(graphics, client.br$getPlayer().br$getInventory().br$getNonEquipmentItems());
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		if (BridgeVersion.is26OrLater()) {
			if (client.br$getWorld() == null) {
				var pos = getContentPos();
				graphics.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2, -1);
				return;
			}
		}
		render(graphics, PLACEHOLDER.get());
	}

	private void render(AxoRenderContext graphics, List<? extends AxoItemStack> inventorySlots) {
		var pos = getContentPos();
		int x = pos.x() + 2;
		int y = pos.y() + 2;

		for (int i = 0, inventorySlotsLength = inventorySlots.size(); i < inventorySlotsLength; i++) {
			AxoItemStack stack = inventorySlots.get(i);
			//if (stack != null && !stack.br$isEmpty()) {
			renderStack(graphics, x + (i % 9) * ITEM_SIZE, y + (i / 9) * ITEM_SIZE, stack);
			//}
		}
	}

	private void renderStack(AxoRenderContext graphics, int x, int y, AxoItemStack itemStack) {
		var empty = itemStack == null || itemStack.br$isEmpty();
		if ((!empty || alwaysShowItemBackgrounds.get()) && itemBackground.get() && itemBackgroundColor.get().getAlpha() > 0) {
			if (roundBackground.get()) {
				graphics.br$fillRectRound(x, y, ITEM_TILE_SIZE, ITEM_TILE_SIZE, itemBackgroundColor.get(), Math.min(backgroundRounding.get(), ITEM_TILE_SIZE / 2f));
			} else {
				graphics.br$fillRect(x, y, ITEM_TILE_SIZE, ITEM_TILE_SIZE, itemBackgroundColor.get().toInt());
			}
		}
		if (empty) return;

		graphics.br$renderGuiItemModel(itemStack, x, y);
		graphics.br$renderGuiItemOverlay(itemStack, x, y, null);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		Collections.addAll(options, anchor, hide, dynamic, itemBackground, itemBackgroundColor, alwaysShowItemBackgrounds);
		return options;
	}
}
