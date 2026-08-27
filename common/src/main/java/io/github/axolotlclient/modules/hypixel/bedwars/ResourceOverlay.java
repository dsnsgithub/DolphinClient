/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.item.AxoItem;
import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.item.AxoItems;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.util.ItemUtil;

public class ResourceOverlay extends BoxHudEntry implements DynamicallyPositionable {
	public final static AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "bedwars_resources");
	private static final List<AxoItem> RESOURCES = List.of(AxoItems.IRON_INGOT, AxoItems.GOLD_INGOT, AxoItems.DIAMOND, AxoItems.EMERALD);
	private static final Map<AxoItem, Integer> PLACEHOLDER = Map.of(
		AxoItems.IRON_INGOT, 43,
		AxoItems.GOLD_INGOT, 7,
		AxoItems.DIAMOND, 7,
		AxoItems.EMERALD, 4
	);
	private final BooleanOption renderWhenRelevant = new BooleanOption(ID.br$getPath() + ".renderWhenRelevant", true);
	private final BooleanOption hideIfEmpty = DefaultOptions.getHideIfEmpty();
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(this);
	private final BedwarsMod mod;

	public ResourceOverlay(BedwarsMod mod) {
		super(4 * 18 + 1, 18 + 1, true);
		this.mod = mod;
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		if (!renderWhenRelevant.get() || mod.inGame()) {
			if (hideIfEmpty.get() && RESOURCES.stream().mapToInt(s -> ItemUtil.getTotal(client, s)).noneMatch(i -> i > 0)) {
				return;
			}
			super.render(context, delta);
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		draw(context, s -> ItemUtil.getTotal(client, s));
	}

	private void draw(AxoRenderContext context, Function<AxoItem, Integer> countFunction) {
		var pos = getContentPos();
		int x = pos.x() + 1;
		int y = pos.y() + 1;
		for (AxoItem item : RESOURCES) {
			int amount = countFunction.apply(item);
			final var stack = AxoItemStack.of(item, amount);
			if (amount > 0) {
				context.br$renderGuiItemModel(stack, x, y);
				context.br$renderGuiItemOverlay(stack, x, y, String.valueOf(amount), -1, true);
				x += 18;
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		if (BridgeVersion.is26OrLater()) {
			if (client.br$getWorld() == null) {
				var pos = getContentPos();
				context.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2 - context.br$getFont().br$getFontHeight() / 2, -1);
				return;
			}
		}
		draw(context, PLACEHOLDER::get);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(hideIfEmpty);
		options.add(renderWhenRelevant);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
