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

package io.github.dolphinclient.bridge.impl.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.dolphinclient.bridge.commands.AxoClientCmdSrcStack;
import io.github.dolphinclient.bridge.commands.Commands;
import io.github.dolphinclient.bridge.util.AxoText;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandsImpl implements Commands {
	public record SourceStack(CommandSource origin) implements AxoClientCmdSrcStack {
		@Override
		public void br$sendError(AxoText text) {
			origin.sendMessage((Text) text);
		}

		@Override
		public void br$sendFeedback(AxoText text) {
			origin.sendMessage((Text) text);
		}
	}

	@Getter
	private static final CommandsImpl instance = new CommandsImpl();
	@Getter
	private final CommandDispatcher<AxoClientCmdSrcStack> dispatcher = new CommandDispatcher<>();
	private final Logger logger = LogManager.getLogger("ClientCommandHandler");

	private static boolean isIgnoredException(CommandExceptionType type) {
		return type == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand() ||
			type == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException();
	}

	private static Text getErrorMessage(CommandSyntaxException e) {
		String context = e.getContext();
		return context != null ?
			new TranslatableText("lcu.command.parse_error", e.getMessage(), e.getCursor(), context) : new LiteralText(e.getMessage());
	}

	private static SourceStack buildClientSource(Minecraft client) {
		Preconditions.checkState(client.player != null);
		return new SourceStack(client.player);
	}

	public boolean dispatchClient(String command) {
		if (!command.startsWith("/")) {
			return false;
		}

		Minecraft client = Minecraft.getInstance();
		final var source = buildClientSource(client);
		// cancel if present
		command = command.trim().substring(1);

		try {
			dispatcher.execute(command, source);
			return true;
		} catch (CommandSyntaxException e) {
			if (isIgnoredException(e.getType())) {
				return false;
			}

			logger.warn("Syntax exception for command '{}'", command, e);
			source.origin().sendMessage(getErrorMessage(e));
			return true;
		} catch (Exception e) {
			logger.warn("Error while executing command '{}'", command, e);
			source.origin().sendMessage(new LiteralText(e.getMessage() == null ? "" : e.getMessage()));
			return true;
		}
	}

	public CompletableFuture<List<String>> getCompletionsClient(String command) {
		Minecraft client = Minecraft.getInstance();

		String command0 = command.startsWith("/") ? command.substring(1) : command;
		return dispatcher.getCompletionSuggestions(dispatcher.parse(command0, buildClientSource(client)))
			.thenApply(suggestions -> suggestions.getList()
				.stream()
				.map(x -> command0.contains(" ") ? x.getText() : "/" + x.getText())
				.toList()
			);
	}
}
