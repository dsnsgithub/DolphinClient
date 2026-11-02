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

package io.github.dolphinclient.bridge.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.impl.commands.CommandsImpl;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.item.ItemStack;
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.jetbrains.annotations.Nullable;

public class Bridge {
	@Nullable
	private static List<KeyBinding> keyBindings = new ArrayList<>();

	public static AxoItemStack wrapStack(@Nullable ItemStack stack) {
		return Objects.requireNonNullElseGet(stack, AirItemStackImpl::getInstance);
	}

	@Nullable
	public static ItemStack unwrapStack(AxoItemStack stack) {
		if (stack.br$isEmpty()) {
			return null;
		}

		Preconditions.checkArgument(stack instanceof ItemStack, "stack instanceof ItemStack");
		return (ItemStack) stack;
	}

	public static void addKeybind(KeyBinding keyBinding) {
		Preconditions.checkState(keyBindings != null, "keybind registered too late!");
		keyBindings.add(keyBinding);
	}

	public static void init() {
		KeybindEvents.REGISTER_KEYBINDS.register(() -> {
			Preconditions.checkState(keyBindings != null, "double keybind register");
			keyBindings.forEach(KeybindRegistry::register);
			keyBindings = null;
		});

		MinecraftClientEvents.START.register(minecraft -> Events.CLIENT_START.invoker().run());
		MinecraftClientEvents.READY.register(minecraft -> Events.CLIENT_READY.invoker().run());
		MinecraftClientEvents.STOP.register(minecraft -> Events.CLIENT_STOP.invoker().run());
		MinecraftClientEvents.TICK_END.register(minecraft -> Events.TICK.invoker().run());
		ClientResourceLoaderEvents.END_RESOURCE_RELOAD.register((manager, ctx) -> Events.END_RESOURCE_RELOAD.invoker().run());
		ClientConnectionEvents.DISCONNECT.register(mc -> Events.DISCONNECT.invoker().run());
	}

	public static void postInit() {
		Events.COMMAND_REGISTER.invoker().accept(CommandsImpl.getInstance());
	}
}
