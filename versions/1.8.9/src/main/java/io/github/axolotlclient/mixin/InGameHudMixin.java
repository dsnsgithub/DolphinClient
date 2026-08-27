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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.events.types.ScoreboardRenderEvent;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.*;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.chat.ChatGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.RideableMinecartEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public abstract class InGameHudMixin {

	@Shadow
	private String subtitle;
	@Shadow
	private String title;
	@Unique
	private static final Entity axolotlclient$noHungerEntityTM = new RideableMinecartEntity(null);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;color4f(FFFF)V", ordinal = 0))
	private void axolotlclient$onHudRender(float tickDelta, CallbackInfo ci) {
		if (!(AxoMinecraftClient.getInstance().br$getScreen() instanceof HudEditScreen)) {
			HudManager.getInstance().render(AxoRenderContextImpl.getInstance(), tickDelta);
		}
	}

	@Inject(method = "renderScoreboardObjective", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customScoreBoard(ScoreboardObjective objective, Window window, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().accept(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;hasCrosshair()Z"))
	public boolean axolotlclient$noCrosshair(GameGui instance, Operation<Boolean> original) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			GlStateManager.blendFuncSeparate(775, 769, 1, 0);
			GlStateManager.enableAlphaTest();
			return false;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;draw(Ljava/lang/String;III)I", ordinal = 0))
	public int axolotlclient$actionBar(TextRenderer instance, String text, int x, int y, int color, Operation<Integer> original) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			hud.setActionBar(text, color);
			return 0;
		}
		return original.call(instance, text, x, y, color);
	}

	@Inject(method = "renderBossBars", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customBossBar(CallbackInfo ci) {
		BossBarHud hud = (BossBarHud) HudManager.getInstance().get(BossBarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customHotbar(Window window, float tickDelta, CallbackInfo ci) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "renderMainHandMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I"))
	public int axolotlclient$setItemNamePos(TextRenderer instance, String string, float x, float y, int color, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			x = hud.getRawTrueX() + (hud.getWidth() * hud.getScale() - Minecraft.getInstance().textRenderer.getWidth(string)) / 2;
			y = hud.getRawTrueY() - 36 + (!Minecraft.getInstance().interactionManager.hasStatusBars() ? 14 : 0);
		}
		return original.call(instance, string, x, y, color);
	}

	@WrapOperation(method = {"renderJumpBar", "renderXpBar"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;drawTexture(IIIIII)V"))
	public void axolotlclient$moveHorseHealth(GameGui instance, int x, int y, int u, int v, int w, int h, Operation<Void> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			x = hud.getRawTrueX();
			y = hud.getRawTrueY() - 7;
		}
		original.call(instance, x, y, u, v, w, h);
	}

	@WrapOperation(method = "renderXpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Window;getHeight()I", ordinal = 1))
	public int axolotlclient$moveXPBarHeight(Window instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			return hud.getRawTrueY() + 22;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderXpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Window;getWidth()I"))
	public int axolotlclient$moveXPBarWidth(Window instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			return hud.getRawTrueX() * 2 + hud.getWidth();
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Window;getHeight()I"))
	public int axolotlclient$moveStatusBarsHeight(Window instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			return hud.getRawTrueY() + 22;
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Window;getWidth()I"))
	public int axolotlclient$moveStatusBarsWidth(Window instance, Operation<Integer> original) {
		HotbarHud hud = (HotbarHud) HudManager.getInstance().get(HotbarHud.ID);
		if (HudManager.getInstance().hudsEnabled() && hud.isEnabled()) {
			return hud.getRawTrueX() * 2 + hud.getWidth();
		}
		return original.call(instance);
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;getArmorProtection()I"))
	private int axolotlclient$disableArmor(PlayerEntity instance, Operation<Integer> original) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && !BedwarsMod.getInstance().displayArmor.get()) {
			return 0;
		}
		return original.call(instance);
	}

	@ModifyVariable(
		method = "renderStatusBars",
		at = @At(
			value = "STORE"
		),
		ordinal = 18
	)
	public int axolotlclient$displayHardcoreHearts(int offset) {
		//noinspection OptionalGetWithoutIsPresent
		boolean hardcore = BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed();
		return hardcore ? 5 : offset;
	}

	@ModifyVariable(
		method = "renderStatusBars",
		at = @At(
			value = "STORE"
		),
		ordinal = 0
	)
	public Entity axolotlclient$dontHunger(Entity normal) {
		if (normal == null && BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().showHunger.get()) {
			return axolotlclient$noHungerEntityTM;
		}
		return normal;
	}

	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$removeVignette(float f, Window window, CallbackInfo ci) {
		if (DolphinClient.config().removeVignette.get()) {
			ci.cancel();
		}
	}

	@Unique
	private float titleScale = -1, subtitleScale = -1;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V", ordinal = 0))
	private void scaleTitle(float f, CallbackInfo ci) {
		if (!DolphinClient.config().scaleTitles.get()) {
			return;
		}
		if (titleScale == -1) {
			calculateTitleScale(Minecraft.getInstance(), title, Util.getWindow().getWidth() - DolphinClient.config().titlePadding.get() * 8);
		}
		if (titleScale != -1) {
			GlStateManager.scalef(titleScale, titleScale, 1);
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;scalef(FFF)V", ordinal = 1))
	private void scaleSubtitle(float f, CallbackInfo ci) {
		if (!DolphinClient.config().scaleTitles.get()) {
			return;
		}
		if (subtitleScale == -1) {
			calculateSubtitleScale(Minecraft.getInstance(), subtitle, Util.getWindow().getWidth() - DolphinClient.config().titlePadding.get() * 8);
		}
		if (subtitleScale != -1) {
			GlStateManager.scalef(subtitleScale, subtitleScale, 1);
		}
	}

	@Inject(method = "setTitles", at = @At("HEAD"))
	private void calculateScales(String string, String string2, int i, int j, int k, CallbackInfo ci) {
		if (!DolphinClient.config().scaleTitles.get()) {
			return;
		}
		var client = Minecraft.getInstance();
		int padding = DolphinClient.config().titlePadding.get();
		int windowWidth = Util.getWindow().getWidth() - padding * 8;
		calculateTitleScale(client, string, windowWidth);
		calculateSubtitleScale(client, string2, windowWidth);
	}

	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GameGui;title:Ljava/lang/String;", opcode = Opcodes.PUTFIELD))
	private void resetTitleScales(CallbackInfo ci) {
		titleScale = -1;
		subtitleScale = -1;
	}

	@Unique
	private void calculateTitleScale(Minecraft client, String string, int windowWidth) {
		int width = client.textRenderer.getWidth(string) * 4; // default scale for titles
		if (width > windowWidth) {
			float scale = (float) width / windowWidth;
			titleScale = 1 / scale;
		}
	}

	@Unique
	private void calculateSubtitleScale(Minecraft client, String string2, int windowWidth) {
		int width = client.textRenderer.getWidth(string2) * 2; // default scale for subtitles
		if (width > windowWidth) {
			float scale = (float) width / windowWidth;
			subtitleScale = 1 / scale;
		}
	}

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/chat/ChatGui;render(I)V"))
	private boolean hideChat(ChatGui instance, int i) {
		return !DolphinClient.config().hideChat.get();
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;render(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
	private void translateTabOverlay(PlayerTabOverlay instance, int width, Scoreboard scoreboard, ScoreboardObjective displayObjective, Operation<Void> original) {
		var hud = (PlayerTabOverlayHud) HudManager.getInstance().get(PlayerTabOverlayHud.ID);
		if (!hud.isHidden()) {
			var graphics = AxoRenderContextImpl.getInstance();
			graphics.br$pushMatrix();
			if (hud.isEnabled()) {
				graphics.br$translateMatrix(-graphics.br$guiWidth() / 2f, -9);
				graphics.br$translateMatrix(hud.getRawTrueX() + hud.getTrueWidth() / 2f, hud.getRawTrueY());
			}
			original.call(instance, width, scoreboard, displayObjective);
			graphics.br$popMatrix();
		}
	}
}
