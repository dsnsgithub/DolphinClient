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

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.DolphinClientConfigCommon;
import io.github.axolotlclient.modules.auth.AccountsScreen;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.AuthWidget;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	public abstract void render(int par1, int par2, float par3);

	@Shadow
	private boolean realmsEnabled;

	@Inject(method = "initWidgetsNormal", at = @At("TAIL"))
	private void axolotlclient$replaceRealmsButton(int i, int j, CallbackInfo ci) {
		List<ButtonWidget> buttons = new ArrayList<>();
		if (Auth.getInstance().showButton.get()) {
			buttons.add(new AuthWidget(10, 10));
		}
		this.buttons.addAll(buttons);

		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			try {
				Class<?> booleanConfigOpt = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.option.BooleanConfigOption");
				Class<?> enumConfigOpt = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.option.EnumConfigOption");
				Class<?> titleMenuButtonStyle = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.ModMenuConfig$TitleMenuButtonStyle");
				Class<?> modmenuConfig = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.ModMenuConfig");
				MethodHandle modifyTitleScreenHandle = MethodHandles.lookup().findStaticGetter(modmenuConfig, "MODIFY_TITLE_SCREEN", booleanConfigOpt);
				MethodHandle getValueB = MethodHandles.lookup().findVirtual(booleanConfigOpt, "getValue", MethodType.methodType(boolean.class));
				MethodHandle getValueE = MethodHandles.lookup().findVirtual(enumConfigOpt, "getValue", MethodType.methodType(Enum.class));
				var modifyTitleScreen = modifyTitleScreenHandle.invoke();
				boolean isModifyTitleScreen = (boolean) getValueB.invoke(modifyTitleScreen);
				MethodHandle modsButtonStyleHandle = MethodHandles.lookup().findStaticGetter(modmenuConfig, "MODS_BUTTON_STYLE", enumConfigOpt);
				var modsButtonStyle = getValueE.invoke(modsButtonStyleHandle.invoke());
				var classic = titleMenuButtonStyle.getEnumConstants()[0];
				if (isModifyTitleScreen && modsButtonStyle == classic) {
					buttons.forEach(r -> r.y -= 24 / 2);
				}
			} catch (Throwable ignored) {
			}
		}
	}

	@Inject(method = "initWidgetsNormal", at = @At("TAIL"))
	private void axolotlclient$addOptionsButton(int y, int spacingY, CallbackInfo ci) {
		if (DolphinClientConfigCommon.instance().titleScreenOptionButtonMode.get().showButton()) {
			buttons.add(new ButtonWidget(192, this.width / 2 - 100, y + spacingY * 3, I18n.translate("config") + "..."));
		}
	}

	@ModifyConstant(method = "init", constant = @Constant(intValue = 72))
	private int axolotlclient$moveButtons(int constant) {
		if (DolphinClientConfigCommon.instance().titleScreenOptionButtonMode.get().showButton()) {
			return constant + 25;
		}
		return constant;
	}

	@Inject(method = "buttonClicked", at = @At("TAIL"))
	public void axolotlclient$onClick(ButtonWidget button, CallbackInfo ci) {
		if (button.id == 192)
			Minecraft.getInstance().openScreen(new HudEditScreen(this));
		else if (button.id == 242)
			Minecraft.getInstance().openScreen(new AccountsScreen(Minecraft.getInstance().screen));
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	public void axolotlclient$customBranding(TitleScreen instance, TextRenderer textRenderer, String s, int x, int y, int color) {
		instance.drawString(textRenderer,
			"Minecraft 1.8.9/DolphinClient " + DolphinClientCommon.VERSION,
			x, y, color);
	}

	@Inject(method = "<init>",
		at = @At(value = "INVOKE",
			target = "Ljava/io/BufferedReader;readLine()Ljava/lang/String;", remap = false))
	private void axolotlclient$customSplashTexts(CallbackInfo ci, @Local List<String> list) throws IOException {
		try (InputStream input = Minecraft.getInstance().getResourceManager()
			.getResource(new Identifier(DolphinClientCommon.MODID, "texts/splashes.txt")).asStream()) {
			list.addAll(IOUtils.readLines(input));
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void disableRealms(CallbackInfo ci) {
		this.realmsEnabled = true;
	}
}
