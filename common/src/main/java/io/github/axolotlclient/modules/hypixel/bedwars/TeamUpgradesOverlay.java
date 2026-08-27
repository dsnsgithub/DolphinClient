/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.BedwarsTeamUpgrades;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.TeamUpgrade;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.TrapUpgrade;

/**
 * @author DarkKronicle
 */
public class TeamUpgradesOverlay extends BoxHudEntry implements DynamicallyPositionable {

	public final static AxoIdentifier ID = AxoIdentifier.of(DolphinClientCommon.MODID, "bedwars_teamupgrades");
	private final static TrapUpgrade.TrapType[] trapEdit = {TrapUpgrade.TrapType.MINER_FATIGUE, TrapUpgrade.TrapType.ITS_A_TRAP};
	private final BooleanOption renderWhenRelevant = new BooleanOption(ID.br$getPath() + ".renderWhenRelevant", true);
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(this);
	private final BedwarsMod mod;
	private BedwarsTeamUpgrades upgrades = null;

	public TeamUpgradesOverlay(BedwarsMod mod) {
		super(35, 18, true);
		this.mod = mod;
		BedwarsMod.GAME_START_EVENT.register(game -> upgrades = game.getUpgrades());
		BedwarsMod.GAME_END_EVENT.register(() -> upgrades = null);
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		if (!renderWhenRelevant.get() || mod.inGame()) {
			super.render(context, delta);
		}
	}

	public void drawOverlay(AxoRenderContext context, DrawPosition position, boolean editMode) {
		if (upgrades == null && !editMode) {
			return;
		}

		int x = position.x() + 1;
		int y = position.y() + 1;
		int width = 18;
		int height;
		boolean normalUpgrades = false;
		if (upgrades != null) {
			for (TeamUpgrade u : upgrades.upgrades) {
				if (!u.isPurchased()) {
					continue;
				}
				if (u instanceof TrapUpgrade) {
					continue;
				}
				u.draw(context, x, y, 16, 16);
				x += 17;
				normalUpgrades = true;
			}
			width = Math.max(x - position.x() - 1, width);
		}
		x = position.x() + 1;
		if (normalUpgrades) {
			y += 17;
		}
		if (editMode) {
			for (TrapUpgrade.TrapType type : trapEdit) {
				type.draw(context, x, y, 16, 16);
				x += 17;
			}
			width = Math.max(x - position.x() - 1, width);
		} else if (upgrades != null) {
			upgrades.trap.draw(context, x, y, 16, 16);
			width = Math.max(x + upgrades.trap.getTrapCount() * 16 - position.x() - 1, width);
		}
		height = y - position.y() - 1 + 17;
		if (getContentHeight() != height || getContentWidth() != width) {
			setContentWidth(width);
			setContentHeight(height);
			onBoundsUpdate();
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
		drawOverlay(context, getContentPos(), false);
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
		drawOverlay(context, getContentPos(), true);
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(renderWhenRelevant);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
