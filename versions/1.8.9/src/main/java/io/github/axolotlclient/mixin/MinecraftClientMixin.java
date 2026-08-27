/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.bridge.events.types.WorldLoadEvent;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.rpc.DiscordRPC;
import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.chat.ChatGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.main.RunArgs;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.WorldSettings;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

	@Shadow
	public GameOptions options;
	@Shadow
	public LocalClientPlayerEntity player;
	@Shadow
	@Final
	private String gameVersion;
	@Shadow
	private TextureManager textureManager;

	protected MinecraftClientMixin(TextureManager textureManager) {
		this.textureManager = textureManager;
	}

	@SuppressWarnings("EmptyMethod")
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V", ordinal = 1), remap = false)
	public void axolotlclient$noSessionIDLeak(Logger instance, String s) {
	}

	/**
	 * @author TheKodeToad & Sk1erLLC (initially created this fix).
	 * @reason unnecessary garbage collection
	 */
	@SuppressWarnings("EmptyMethod")
	@Redirect(method = "startGame", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"))
	public void axolotlclient$noWorldGC() {
	}

	@Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("HEAD"))
	private void axolotlclient$onWorldLoad(ClientWorld clientWorld, String string, CallbackInfo ci) {
		io.github.axolotlclient.bridge.events.Events.WORLD_LOAD_EVENT.invoker().accept(new WorldLoadEvent(clientWorld));
	}

	@Inject(method = "initDisplay", at = @At("TAIL"))
	public void axolotlclient$setWindowTitle(CallbackInfo ci) {
		if (DolphinClient.config().customWindowTitle.get()) {
			Display.setTitle("DolphinClient " + this.gameVersion);
		}
	}

	@Redirect(method = "handleGuiKeyBindings", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;getKeyCode()I", ordinal = 5))
	// Fix taking a screenshot when pressing '<' (Because it has the same keyCode as F2)
	public int axolotlclient$iTryToFixTheScreenshotKey(KeyBinding instance) {
		if (Keyboard.getEventCharacter() != '<') {
			return instance.getKeyCode();
		}

		return -999; // There is no key with this Code, but this is what we want here.
	}

	// Don't ask me why we need both here, but otherwise it looks ugly
	@WrapOperation(method = "renderLoadingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;color(IIII)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	public BufferBuilder axolotlclient$loadingScreenColor(BufferBuilder instance, int red, int green, int blue, int alpha, Operation<BufferBuilder> original) {
		if (!DolphinClient.config().customLoadingScreenColor.get())
			return original.call(instance, red, green, blue, alpha);
		return original.call(instance, DolphinClient.config().loadingScreenColor.get().getRed(),
			DolphinClient.config().loadingScreenColor.get().getGreen(),
			DolphinClient.config().loadingScreenColor.get().getBlue(),
			DolphinClient.config().loadingScreenColor.get().getAlpha());
	}

	@WrapOperation(method = "renderLoadingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;color(IIII)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	public BufferBuilder axolotlclient$loadingScreenBg(BufferBuilder instance, int red, int green, int blue, int alpha, Operation<BufferBuilder> original) {
		if (!DolphinClient.config().customLoadingScreenColor.get())
			return original.call(instance, red, green, blue, alpha);
		return original.call(instance, DolphinClient.config().loadingScreenColor.get().getRed(),
			DolphinClient.config().loadingScreenColor.get().getGreen(),
			DolphinClient.config().loadingScreenColor.get().getBlue(),
			DolphinClient.config().loadingScreenColor.get().getAlpha());
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/texture/TextureManager;close(Lnet/minecraft/resource/Identifier;)V"))
	private void axolotlclient$onLaunch(CallbackInfo ci) {
		HudManager.getInstance().refreshAllBounds();
		if (!API.getInstance().isAuthenticated() && !Auth.getInstance().getCurrent().isOffline()) {
			API.getInstance().startup(Auth.getInstance().getCurrent());
		}
	}

	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/main/RunArgs$Game;version:Ljava/lang/String;", opcode = Opcodes.GETFIELD))
	private String axolotlclient$redirectVersion(RunArgs.Game game) {
		return "1.8.9";
	}

	@Inject(method = "startGame", at = @At("HEAD"))
	public void axolotlclient$startup(String worldFileName, String worldName, WorldSettings levelInfo, CallbackInfo ci) {
		DiscordRPC.getInstance().setWorld(worldFileName);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"), remap = false)
	public int axolotlclient$onScroll(Operation<Integer> original) {
		int amount = original.call();
		if (amount != 0 && Zoom.getInstance().scroll(amount)) {
			return 0;
		}
		return amount;
	}

	@Inject(method = "onResolutionChanged", at = @At(value = "TAIL"))
	public void axolotlclient$onResize(CallbackInfo ci) {
		Util.window = null;
		HudManager.getInstance().refreshAllBounds();
	}

	@Inject(method = "openScreen", at = @At("HEAD"))
	private void axolotlclient$onScreenOpen(Screen screen, CallbackInfo ci) {
		if (Minecraft.getInstance().screen == null) {
			MenuBlur.getInstance().onScreenOpen();
		}
	}

	@WrapOperation(method = "openScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/chat/ChatGui;clear()V"))
	private void keepChatMessagesOnDisconnect(ChatGui instance, Operation<Void> original) {
		io.github.axolotlclient.modules.hud.gui.hud.ChatHud hud = (io.github.axolotlclient.modules.hud.gui.hud.ChatHud) HudManager
			.getInstance().get(io.github.axolotlclient.modules.hud.gui.hud.ChatHud.ID);
		if (hud != null && !hud.keepMessagesOnDisconnect.get()) {
			original.call(instance);
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onClientInit(RunArgs runArgs, CallbackInfo ci) {
		new DolphinClient().onInitializeClient();
	}

	@Inject(method = "handleGuiKeyBindings", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;screenshotKey:Lnet/minecraft/client/options/KeyBinding;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void actionForScreenshotCropKey(CallbackInfo ci, @Local int keyCode) {
		var mapping = (KeyBinding) ScreenshotUtils.getInstance().screenshotCropBinding;
		if (keyCode == mapping.getKeyCode()) {
			mapping.br$click();
			ci.cancel();
		}
	}
}
