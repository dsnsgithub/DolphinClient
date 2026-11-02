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

package io.github.dolphinclient.modules.hud.gui.hud.item;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.dolphinclient.bridge.BridgeVersion;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItemClass;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.item.AxoItems;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.util.DefaultOptions;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.util.CommonUtil;
import io.github.dolphinclient.util.ItemUtil;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class ArrowHud extends TextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "arrowhud");
	private static final List<AxoItem> ARROW_TYPES = Stream.of(
		AxoItems.ARROW,
		AxoItems.TIPPED_ARROW,
		AxoItems.SPECTRAL_ARROW
	).filter(Objects::nonNull).toList();
	private static final Supplier<AxoItemStack> DUMMY = CommonUtil.memoize(() -> AxoItemStack.of(AxoItems.ARROW, 1));

	private final BooleanOption dynamic = new BooleanOption("dynamic", false);
	private final BooleanOption allArrowTypes = new BooleanOption("allArrowTypes", false);
	private final BooleanOption hideIfEmpty = DefaultOptions.getHideIfEmpty();

	private int arrows = 0;
	private AxoItemStack currentArrow = BridgeVersion.is26OrLater() ? null : AxoItemStack.of(AxoItems.ARROW);

	public ArrowHud() {
		super(20, 22, true);
	}

	@Override
	public void render(AxoRenderContext graphics, float delta) {
		final var player = client.br$getPlayer();

		if (dynamic.get() && player != null) {
			final var mainHand = player.br$getInventory().br$getMainHand().br$getItem();

			if (!mainHand.br$is(AxoItemClass.RANGED_WEAPON)) {
				if (BridgeVersion.version() == BridgeVersion.V1_8) {
					return;
				}
				final var offHand = player.br$getInventory().br$getOffHand().br$getItem();
				if (!offHand.br$is(AxoItemClass.RANGED_WEAPON)) {
					return;
				}
			}
		}
		if (currentArrow == null) return;
		if (hideIfEmpty.get() && !isAllArrowTypes() && currentArrow.br$isEmpty()) {
			return;
		}

		super.render(graphics, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		DrawPosition pos = getContentPos();
		graphics.br$renderGuiItemModel(currentArrow, pos.x() + 2, pos.y() + 2);
		graphics.br$renderGuiItemOverlay(currentArrow, pos.x() + 2, pos.y() + 2, String.valueOf(arrows));
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		if (BridgeVersion.is26OrLater()) {
			if (client.br$getWorld() == null) {
				var pos = getContentPos();
				graphics.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2, textColor.get());
				return;
			}
		}
		DrawPosition pos = getContentPos();
		var dummy = DUMMY.get();
		graphics.br$renderGuiItemModel(dummy, pos.x() + 2, pos.y() + 2);
		graphics.br$renderGuiItemOverlay(dummy, pos.x() + 2, pos.y() + 2, "64");
	}

	@Override
	public boolean tickable() {
		return true;
	}

	private boolean isAllArrowTypes() {
		return BridgeVersion.version() != BridgeVersion.V1_8 && allArrowTypes.get();
	}

	@Override
	public void tick() {
		if (client.br$getPlayer() != null) {
			final var projectileItem = client.br$getPlayer().br$getProjectileItem();
			if (!isAllArrowTypes() && projectileItem != null) {
				currentArrow = AxoItemStack.of(projectileItem);
			} else {
				currentArrow = AxoItemStack.of(AxoItems.ARROW);
			}
		}

		if (isAllArrowTypes()) {
			arrows = ARROW_TYPES.stream().mapToInt(x -> ItemUtil.getTotal(client, x)).sum();
		} else {
			if (currentArrow == null) return;
			arrows = ItemUtil.getTotal(client, currentArrow.br$getItem());
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(dynamic);
		options.add(hideIfEmpty);

		if (BridgeVersion.version() != BridgeVersion.V1_8) {
			options.add(allArrowTypes);
		}

		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}
}
