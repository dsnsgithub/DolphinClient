/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.bridge.mixin;

import java.net.InetAddress;
import java.util.List;

import io.github.axolotlclient.bridge.PlatformDispatch;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.modules.hypixel.autoboop.FilterListConfigurationScreen;
import io.github.axolotlclient.modules.hypixel.bedwars.SessionStatsHudEntryConfigScreen;
import io.github.axolotlclient.util.ThreadExecuter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.handler.ClientQueryPacketHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.NetworkProtocol;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.PingC2SPacket;
import net.minecraft.network.packet.c2s.query.ServerStatusC2SPacket;
import net.minecraft.network.packet.s2c.query.PingS2CPacket;
import net.minecraft.network.packet.s2c.query.ServerStatusS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PlatformDispatch.class, remap = false)
public abstract class PlatformDispatchMixin {
	@Unique
	private static void getRealTimeServerPing(String address, int port, MutableInt currentServerPing) {
		ThreadExecuter.scheduleTask(() -> {
			try {
				final Connection manager = Connection.connect(InetAddress.getByName(address), port, false);

				manager.setListener(new ClientQueryPacketHandler() {
					private long currentSystemTime = 0L;

					@Override
					public void onDisconnect(Text text) {

					}

					@Override
					public void handleServerStatus(ServerStatusS2CPacket serverStatusS2CPacket) {
						this.currentSystemTime = Minecraft.getTime();
						manager.send(new PingC2SPacket(this.currentSystemTime));
					}

					@Override
					public void handlePing(PingS2CPacket pingS2CPacket) {
						long time = this.currentSystemTime;
						long latency = Minecraft.getTime();
						currentServerPing.setValue((int) (latency - time));
						manager.disconnect(new LiteralText(""));
					}
				});
				manager.send(new HandshakeC2SPacket(47, address, port, NetworkProtocol.STATUS));
				manager.send(new ServerStatusC2SPacket());
			} catch (Exception ignored) {
			}
		});
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge.
	 */
	@Overwrite
	public static void pingHud$updatePing(MutableInt currentServerPing) {
		if (Minecraft.getInstance().getCurrentServerEntry() != null) {
			ServerAddress address = ServerAddress
				.parse(Minecraft.getInstance().getCurrentServerEntry().ip);
			getRealTimeServerPing(address.getAddress(), address.getPort(), currentServerPing);
		} else if (((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress() != null) {
			getRealTimeServerPing(((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress(),
				((MinecraftClientAccessor) Minecraft.getInstance()).getServerPort(), currentServerPing);
		} else if (Minecraft.getInstance().isIntegratedServerRunning()) {
			currentServerPing.setValue(1);
		}
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge.
	 */
	@Overwrite
	public static void autoBoop$openFiltersScreen(List<String> filters) {
		Minecraft.getInstance().openScreen(new FilterListConfigurationScreen(filters, Minecraft.getInstance().screen));
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge.
	 */
	@Overwrite
	public static void bedwars$sessionstats$openEntryConfigScreen() {
		Minecraft.getInstance().openScreen(new SessionStatsHudEntryConfigScreen(Minecraft.getInstance().screen));
	}
}
