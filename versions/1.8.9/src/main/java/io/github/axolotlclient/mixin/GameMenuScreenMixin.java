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

import java.util.Objects;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.DolphinClientConfigCommon;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import io.github.axolotlclient.util.FeatureDisablerCommon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	@Unique
	private static boolean axolotlclient$hasModMenu() {
		return FabricLoader.getInstance().isModLoaded("modmenu")
			&& !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu")
			&& !FabricLoader.getInstance().isModLoaded("dolphinclient-modmenu");
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void axolotlclient$addConfigButton(CallbackInfo ci) {
		if (!DolphinClientConfigCommon.instance().gameMenuScreenOptionButtonMode.get().showButton())
			return;

		if (minecraft.isSingleplayer() && !this.minecraft.getServer().isPublished()) {
			buttons.add(new ButtonWidget(20, width / 2 - 100,
				height / 4 + 82,
				I18n.translate("config")));
			for (ButtonWidget button : buttons) {
				if (button.y >= this.height / 4 - 16 + 24 * 4 - 1 && button.id < 20) {
					button.y += 24;
				}
				//button.y -= 12;
			}
		} else {
			for (ButtonWidget button : buttons) {
				if (!button.active && button.id == 20) {
					button.active = true;
				}
			}
		}
	}

	@ModifyArgs(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIIIILjava/lang/String;)V", ordinal = 1))
	public void axolotlclient$addOptionsButton(Args args) {
		if (axolotlclient$hasModMenu())
			return;

		if (!minecraft.isSingleplayer() && ((minecraft.getServer() != null
			&& minecraft.getServer().isPublished())
			|| minecraft.getCurrentServerEntry() != null)) {
			args.set(0, 20);
			args.set(5, I18n.translate("title_short"));
		}
	}

	@Inject(method = "buttonClicked", at = @At("HEAD"))
	public void axolotlclient$customButtons(ButtonWidget button, CallbackInfo ci) {
		if (button.id == 20 && !axolotlclient$hasModMenu()) {
			minecraft.openScreen(new HudEditScreen(this));
		} else if (button.id == 1) {
			FeatureDisablerCommon.getInstance().clear();
			if (HypixelMods.getInstance().cacheMode.get() != null
				&& Objects.equals(HypixelMods.getInstance().cacheMode.get(),
				HypixelMods.HypixelApiCacheMode.ON_CLIENT_DISCONNECT)) {
				HypixelAbstractionLayer.getInstance().clearPlayerData();
			}
		}
	}

	@WrapMethod(method = "buttonClicked")
	private void confirmDisconnect(ButtonWidget button, Operation<Void> original) {
		if (button.id == 1 && minecraft.getCurrentServerEntry() != null && DolphinClient.config().confirmDisconnect.get()) {
			minecraft.openScreen(new ConfirmScreen((confirmed, i) -> {
				if (confirmed) original.call(button);
				else minecraft.openScreen(null);
			}, I18n.translate("confirm_disconnect.title"), I18n.translate("confirm_disconnect.message", minecraft.getCurrentServerEntry().ip), 0));
		} else {
			original.call(button);
		}
	}
}
