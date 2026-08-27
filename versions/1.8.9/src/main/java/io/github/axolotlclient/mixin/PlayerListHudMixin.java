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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import io.github.axolotlclient.modules.hud.HudManagerCommon;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.PlayerTabOverlayHud;
import io.github.axolotlclient.modules.hypixel.NickHider;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.network.PlayerInfo;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.network.Connection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin extends GuiElement {
	@Shadow
	private Text header;
	@Shadow
	private Text footer;

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$nickHider(PlayerInfo playerEntry, CallbackInfoReturnable<String> cir) {
		if (playerEntry.getProfile().getId() == Minecraft.getInstance().player.getUuid()
			&& NickHider.getInstance().hideOwnName.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameSelf.get());
		} else if (playerEntry.getProfile().getId() != Minecraft.getInstance().player.getUuid()
			&& NickHider.getInstance().hideOtherNames.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameOthers.get());
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 0))
	private int axolotlclient$moveName(TextRenderer instance, String string, Operation<Integer> original, @Local PlayerInfo entry) {
		var width = original.call(instance, string);
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).numericalPing.get())
			width += instance.getWidth(String.valueOf(entry.getPing())) - 10;
		return width;
	}

	@Inject(method = "renderPing", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(int width, int x, int y, PlayerInfo entry, CallbackInfo ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().customTabList.get() &&
			BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).renderNumericPing(AxoRenderContextImpl.getInstance(), width, x, y, entry)) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
	private boolean axolotlclient$showPlayerHeads$1(Minecraft instance, Operation<Boolean> original) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$2(Connection instance, Operation<Boolean> original) {
		if (((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@Definition(id = "header", field = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;header:Lnet/minecraft/text/Text;")
	@Expression("this.header != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkHeaderOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showHeader.get();
	}

	@Definition(id = "footer", field = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;footer:Lnet/minecraft/text/Text;")
	@Expression("this.footer != null")
	@WrapOperation(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean checkFooterOption(Object left, Object right, Operation<Boolean> original) {
		return original.call(left, right) && ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).showFooter.get();
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getPlayer(Ljava/util/UUID;)Lnet/minecraft/entity/living/player/PlayerEntity;"))
	private UUID axolotlclient$makeStuff(UUID par1) {
		return ((PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID)).alwaysShowHeadLayer.get() ? Minecraft.getInstance().player.getUuid() : par1;
	}

	@Inject(
		method = "renderDisplayScore",
		at = @At(
			value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 1
		),
		cancellable = true
	)
	public void axolotlclient$renderCustomScoreboardObjective(
		ScoreboardObjective objective, int y, String player, int startX, int endX, PlayerInfo playerEntry, CallbackInfo ci
	) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}

		game.renderCustomScoreboardObjective(
			AxoRenderContextImpl.getInstance(), playerEntry.getProfile().getName(),
			objective.getScoreboard().getScore(player, objective).get(), y, endX);
		ci.cancel();


	}

	@ModifyVariable(
		method = "render",
		at = @At(
			value = "STORE"
		),
		ordinal = 7
	)
	public int axolotlclient$changeWidth(int value) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return value;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return value;
		}
		if (BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerInfo playerEntry, CallbackInfoReturnable<String> cir) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return;
		}
		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null || !game.isStarted()) {
			return;
		}
		BedwarsPlayer player = game.getPlayer(playerEntry.getProfile().getId()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(player.getTabListDisplay());
	}

	@Definition(id = "sortedCopy", method = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;")
	@Expression("? = ?.sortedCopy(?)")
	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	public List<PlayerInfo> axolotlclient$overrideSortedPlayers(List<PlayerInfo> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		if (!BedwarsMod.getInstance().customTabList.get()) {
			return original;
		}
		List<?> players = BedwarsMod.getInstance().getGame().orElseThrow().getTabPlayerList(Collections.unmodifiableList(original));
		if (players == null) {
			return original;
		}
		return (List<PlayerInfo>) players;
	}

	@Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeHeader(Text header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabHeader.get()) {
			return;
		}
		this.header = (Text) BedwarsMod.getInstance().getGame().orElseThrow().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Text footer, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		if (!BedwarsMod.getInstance().customTabFooter.get()) {
			return;
		}
		this.footer = (Text) BedwarsMod.getInstance().getGame().orElseThrow().getBottomBarText();
		ci.cancel();
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;split(Ljava/lang/String;I)Ljava/util/List;", ordinal = 1))
	private List<String> captureFooterLines(TextRenderer instance, String text, int width, Operation<List<String>> original, @Share("footerLines") LocalRef<List<String>> footerLines) {
		var lines = original.call(instance, text, width);
		footerLines.set(lines);
		return lines;
	}

	@Definition(id = "list2", local = @Local(type = List.class, ordinal = 1))
	@Expression("list2 != null")
	@Inject(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void renderBackgroundWithOptions(int width, Scoreboard scoreboard, ScoreboardObjective displayObjective, CallbackInfo ci, @Local(ordinal = 10) int r, @Local(ordinal = 9) int q, @Local(ordinal = 1) List<String> headerLines, @Share("footerLines") LocalRef<List<String>> footerLines, @Local(ordinal = 4) int m) {
		var tablist = (PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID);
		if (!tablist.isEnabled() || tablist.backgroundDisabled()) {
			return;
		}
		var g = AxoRenderContextImpl.getInstance();
		var x = width / 2 - r / 2 - 1;
		var y = q - 1;
		var x2 = width / 2 + r / 2 + 1;
		var headerLineCount = headerLines != null ? headerLines.size() : 0;
		var footerLineCount = footerLines.get() != null ? footerLines.get().size() : 0;
		var y2 = q + (headerLineCount + m + footerLineCount) * g.br$getFont().br$getFontHeight() + (footerLineCount > 0 ? 1 : 0) + (headerLineCount > 0 ? 1 : 0);
		int padding = tablist.getBackgroundPadding();
		x -= padding;
		x2 += padding;
		y -= padding;
		y2 += padding;
		if (y < 0) {
			y2 -= y;
			y = 0;
		}
		if (tablist.hasRoundBackground()) {
			var rounding = Math.min(tablist.getBackgroundRounding(), Math.min(x2 - x, y2 - y) / 2f);
			g.br$fillRectRound(x, y, x2 - x, y2 - y,
				tablist.customBackgroundColor.get() ? tablist.getBackgroundColor().toInt() : Integer.MIN_VALUE,
				rounding);

			if (tablist.hasOutline()) {
				g.br$outlineRectRound(x, y, x2 - x, y2 - y, tablist.getOutlineColor(), rounding);
			}
		} else {
			g.br$fillRect(x, y, x2 - x, y2 - y,
				tablist.customBackgroundColor.get() ? tablist.getBackgroundColor().toInt() : Integer.MIN_VALUE);
			if (tablist.hasOutline()) {
				g.br$outlineRect(x, y, x2 - x, y2 - y, tablist.getOutlineColor());
			}
		}
	}

	@Unique
	private void applyBackgroundOptions(int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		var tablist = (PlayerTabOverlayHud) HudManagerCommon.getInstance().get(PlayerTabOverlayHud.ID);
		if (!tablist.isEnabled()) {
			original.call(x1, y1, x2, y2, color);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;fill(IIIII)V"), slice = @Slice(to = @At(value = "CONSTANT", args = "intValue=553648127")))
	private void modifyBackground$headerAndMain(int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		applyBackgroundOptions(x1, y1, x2, y2, color, original);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;fill(IIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;renderPing(IIILnet/minecraft/client/network/PlayerInfo;)V")))
	private void modifyBackground$footer(int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
		applyBackgroundOptions(x1, y1, x2, y2, color, original);
	}
}
