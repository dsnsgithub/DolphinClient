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

package io.github.dolphinclient.modules.hypixel;

import java.util.regex.Pattern;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.events.types.ReceiveChatMessageEvent;
import lombok.Getter;

public class AutoTip implements AbstractHypixelMod {

	@Getter
	private final static AutoTip Instance = new AutoTip();

	private final OptionCategory category = OptionCategory.create("autotip");

	private final BooleanOption enabled = new BooleanOption("enabled", false);
	private final BooleanOption hideMessages = new BooleanOption("hideTipMessages", false);

	private final Pattern messagePattern = Pattern.compile("^You tipped [0-9]+ players in [0-9]+ different games!$");
	private final Pattern tippedPattern = Pattern.compile("^You already tipped everyone that has boosters active, so there isn't anybody to be tipped right now!$");

	private long lastTime;
	private boolean init = false;

	@Override
	public void init() {
		category.add(enabled, hideMessages);
		init = true;

		Events.RECEIVE_CHAT_MESSAGE.register(this::onChatMessage);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	@Override
	public void tick() {
		if (init) {
			final var client = AxoMinecraftClient.getInstance();
			final var serverAddress = AxoMinecraftClient.getInstance().br$getServerAddress();
			final var isHypixel = serverAddress != null && serverAddress.contains("hypixel");

			if (System.currentTimeMillis() - lastTime > 1200000 && isHypixel && enabled.get() && client.br$getPlayer() != null) {
				client.br$sendToServer("/tip all");
				lastTime = System.currentTimeMillis();
			}
		}
	}

	@Override
	public boolean tickable() {
		return true;
	}

	public void onChatMessage(ReceiveChatMessageEvent event) {
		event.setCancelled(enabled.get() && hideMessages.get() &&
			(messagePattern.matcher(event.getOriginalMessage()).matches() ||
				tippedPattern.matcher(event.getOriginalMessage()).matches()));
	}
}
