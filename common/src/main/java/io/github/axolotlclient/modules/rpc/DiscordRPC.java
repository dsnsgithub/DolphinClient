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

package io.github.axolotlclient.modules.rpc;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.*;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.modules.AbstractCommonModule;
import io.github.axolotlclient.util.CommonUtil;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import lombok.Getter;

public class DiscordRPC extends AbstractCommonModule {
	private static final long CLIENT_ID = 875835666729152573L;
	@Getter
	private static final DiscordRPC Instance = new DiscordRPC();

	private final OptionCategory category = OptionCategory.create("rpc");
	private final BooleanOption showActivity = new BooleanOption("showActivity", true);
	private final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", false, value -> {
		if (!value) {
			ThreadExecuter.scheduleTask(this::shutdown);
		}
	});
	private final StringArrayOption showServerNameMode = new StringArrayOption("showServerNameMode",
		new String[]{"showIp", "showName", "off"}, "off");
	private final BooleanOption showTime = new BooleanOption("showTime", true);

	private final Instant time = Instant.now();
	private final Logger logger = DolphinClientCommon.getInstance().getLogger();
	private final AtomicReference<State> state = new AtomicReference<>(State.NOT_CONNECTED);
	private IPCClient ipcClient;
	private String currentWorld = "";

	public void setWorld(String world) {
		currentWorld = world;
	}

	public void init() {
		category.add(enabled, showTime, showActivity, showServerNameMode);
		if (OSUtil.getOS() == OSUtil.OperatingSystem.OTHER) {
			enabled.setForceOff(true, "crash");
		}
		DolphinClientCommon.getInstance().getConfig().addCategory(category);
		Events.CLIENT_STOP.register(this::shutdown);
	}

	public void tick() {
		if (enabled.get() && state.compareAndSet(State.NOT_CONNECTED, State.STARTING)) {
			ThreadExecuter.scheduleTask(this::initRPC);
		} else if (state.get() == State.CONNECTED) {
			ThreadExecuter.scheduleTask(this::updateRichPresence);
		}
	}

	private void shutdown() {
		if (state.get() == State.CONNECTED) {
			setRichPresence(null);
			ipcClient.close();
		}
		state.set(State.NOT_CONNECTED);
	}

	private RichPresence createRichPresence(String state, String details) {
		RichPresence.Builder builder = new RichPresence.Builder();
		builder.setLargeImageWithTooltip("icon", "DolphinClient " + DolphinClientCommon.VERSION + " | Minecraft " + DolphinClientCommon.GAME_VERSION);
		if (showTime.get()) {
			builder.setStartTimestamp(time.getEpochSecond());
		}
		builder.setState(state).setDetails(details);
		builder.setStatusDisplayType(StatusDisplayType.Name);
		builder.setActivityType(ActivityType.Playing);
		return builder.build();
	}

	private void updateRichPresence() {
		String state = switch (showServerNameMode.get()) {
			case "showIp" -> client.br$getWorld() == null ? "In the menu"
				: (CommonUtil.getCurrentServerAddress() == null ? "Singleplayer" : CommonUtil.getCurrentServerAddress());
			case "showName" -> client.br$getWorld() == null ? "In the menu"
				: (client.br$getServerAddress() == null
				   ? (CommonUtil.getCurrentServerAddress() == null ? "Singleplayer"
					  : CommonUtil.getCurrentServerAddress())
				   : client.br$getServerName());
			default -> "";
		};

		String details;
		if (showActivity.get() && !client.br$isLocalServer()) {
			details = CommonUtil.getGame();
		} else if (showActivity.get() && !currentWorld.isEmpty()) {
			details = currentWorld;
			currentWorld = "";
		} else {
			details = "";
		}

		setRichPresence(createRichPresence(state, details));
	}

	private void setRichPresence(RichPresence presence) {
		if (ipcClient != null && state.get() == State.CONNECTED) {
			ipcClient.sendRichPresence(presence);
		}
	}

	private synchronized void initRPC() {
		if (enabled.get() && state.get() == State.STARTING) {
			if (ipcClient == null) {
				ipcClient = new IPCClient(CLIENT_ID);
				ipcClient.setListener(new IPCListener() {
					@Override
					public void onPacketSent(IPCClient client, Packet packet) {

					}

					@Override
					public void onPacketReceived(IPCClient client, Packet packet) {

					}

					@Override
					public void onActivityJoin(IPCClient client, String secret) {

					}

					@Override
					public void onActivitySpectate(IPCClient client, String secret) {

					}

					@Override
					public void onActivityJoinRequest(IPCClient client, String secret, User user) {

					}

					@Override
					public void onReady(IPCClient client) {
						state.set(State.CONNECTED);
						updateRichPresence();
					}

					@Override
					public void onClose(IPCClient client, JsonObject json) {
						logger.info("RPC Closed");
						state.set(State.NOT_CONNECTED);
					}

					@Override
					public void onDisconnect(IPCClient client, Throwable t) {
						state.set(State.NOT_CONNECTED);
					}
				});
			}
			try {
				ipcClient.connect();
				logger.info("Started RPC");
			} catch (Exception e) {
				logger.warn("Failed to start RPC", e);
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus("rpc.starting.error", e.getMessage());
				try {
					ipcClient.close();
				} catch (Throwable ignored) {
				}
				state.set(State.ERRORED);
			}
		}
	}

	private enum State {
		NOT_CONNECTED,
		STARTING,
		CONNECTED,
		ERRORED
	}
}
