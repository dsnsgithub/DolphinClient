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

package io.github.axolotlclient.bridge.commands;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.axolotlclient.util.UUIDHelper;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import org.jetbrains.annotations.Nullable;

public class PlayerArgument implements ArgumentType<PlayerArgument.PlayerInfo> {
	public static final class PlayerInfo {
		private final String playerName;
		@Nullable
		private CompletableFuture<Optional<String>> uuid;

		public PlayerInfo(String playerName) {
			this.playerName = playerName;
		}

		public String playerName() {
			return playerName;
		}

		public CompletableFuture<Optional<String>> uuid() {
			if (uuid == null) {
				uuid = CompletableFuture.completedFuture(
					AxoMinecraftClient.getInstance().br$getOnlinePlayers().stream()
						.filter(player -> playerName.equalsIgnoreCase(player.br$getName()))
						.map(player -> UUIDHelper.toUndashed(player.br$getId()))
						.findFirst()
				);
			}

			return uuid;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (PlayerInfo) obj;
			return Objects.equals(this.playerName, that.playerName) &&
				Objects.equals(this.uuid, that.uuid);
		}

		@Override
		public int hashCode() {
			return Objects.hash(playerName, uuid);
		}

		@Override
		public String toString() {
			return "PlayerInfo[" +
				"playerName=" + playerName + ", " +
				"uuid=" + uuid + ']';
		}

	}

	private static final Pattern NAME_REGEX = Pattern.compile("[a-zA-Z0-9_]{2,16}");

	@Override
	public PlayerInfo parse(StringReader stringReader) {
		String playerName = stringReader.readUnquotedString();
		return new PlayerInfo(playerName);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		AxoMinecraftClient.getInstance().br$getOnlinePlayers().stream()
			.map(AxoPlayerListEntry::br$getName)
			.filter(name -> NAME_REGEX.matcher(name).matches())
			.filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemaining().toLowerCase(Locale.ROOT)))
			.forEach(builder::suggest);

		return builder.buildFuture();
	}

	public static PlayerInfo get(CommandContext<?> context, String name) {
		return context.getArgument(name, PlayerInfo.class);
	}

	public static PlayerArgument player() {
		return new PlayerArgument();
	}
}
