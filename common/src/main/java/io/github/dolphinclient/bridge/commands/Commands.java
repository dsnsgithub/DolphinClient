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

package io.github.dolphinclient.bridge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

public interface Commands {
	static LiteralArgumentBuilder<AxoClientCmdSrcStack> literal(String literal) {
		return LiteralArgumentBuilder.literal(literal);
	}

	static <T> RequiredArgumentBuilder<AxoClientCmdSrcStack, T> argument(String name, ArgumentType<T> arg) {
		return RequiredArgumentBuilder.argument(name, arg);
	}

	CommandDispatcher<AxoClientCmdSrcStack> getDispatcher();

	default LiteralCommandNode<AxoClientCmdSrcStack> register(LiteralArgumentBuilder<AxoClientCmdSrcStack> node) {
		return getDispatcher().register(node);
	}
}
