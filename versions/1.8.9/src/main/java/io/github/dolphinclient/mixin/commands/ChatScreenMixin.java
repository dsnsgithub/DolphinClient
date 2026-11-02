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

package io.github.dolphinclient.mixin.commands;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.dolphinclient.bridge.impl.commands.CommandsImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Shadow
	private boolean completed;
	@Unique
	@Nullable
	private CompletableFuture<List<String>> lcu$clientSuggestions;

	@Inject(
		method = "goThroughHistory(Ljava/lang/String;Ljava/lang/String;)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/gui/screen/ChatScreen;completed:Z",
			opcode = Opcodes.PUTFIELD)
	)
	private void prepareClientSideSuggestions(String partialMessage, String nextWord, CallbackInfo ci) {
		lcu$clientSuggestions = CommandsImpl.getInstance().getCompletionsClient(partialMessage);
	}

	@WrapMethod(method = "setMessageHistory")
	private void addClientSideSuggestions(String[] suggestions, Operation<Void> original) {
		if (!completed) {
			return;
		}

		if (lcu$clientSuggestions == null) {
			original.call((Object) suggestions);
		}

		lcu$clientSuggestions.whenCompleteAsync((strings, throwable) -> {
			if (strings == null) {
				original.call((Object) suggestions);
			} else {
				original.call(
					(Object) Stream.concat(
						Arrays.stream(suggestions),
						strings.stream()
					).toArray(String[]::new)
				);
			}
		}, Minecraft.getInstance()::executeTask);

		lcu$clientSuggestions = null;
	}
}
