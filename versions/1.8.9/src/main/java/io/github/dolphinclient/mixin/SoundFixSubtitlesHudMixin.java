/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.mixin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.bridge.impl.AxoRenderContextImpl;
import io.github.dolphinclient.modules.hud.HudEditScreen;
import io.github.dolphinclient.modules.hud.HudManager;
import io.github.dolphinclient.modules.hud.gui.hud.vanilla.SubtitlesHudHud;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "io/github/moehreag/soundfix/subtitles/SubtitlesHud", remap = false)
public abstract class SoundFixSubtitlesHudMixin {

	@Unique
	private static MethodHandle GET_CLOSEST_HANDLE;

	static {
		if (!FabricLoader.getInstance().isModLoaded("soundfix")) {
			GET_CLOSEST_HANDLE = null;
		} else {
			try {
				var soundPlayedAt = Class.forName("io.github.moehreag.soundfix.subtitles.SubtitlesHud$SoundPlayedAt");
				var subtitle = Class.forName("io.github.moehreag.soundfix.subtitles.SubtitlesHud$Subtitle");
				GET_CLOSEST_HANDLE = MethodHandles.lookup().findVirtual(subtitle, "getClosest", MethodType.methodType(soundPlayedAt, Vec3d.class));
			} catch (Throwable e) {
				GET_CLOSEST_HANDLE = null;
			}
		}
	}

	@Shadow
	@Final
	private List<?> audibleSubtitles;

	@SuppressWarnings("LocalMayBeArgsOnly")
	@WrapOperation(method = "render()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;translated(DDD)V", ordinal = 0, remap = true), remap = false)
	private void subtitlesHudPosition(double x, double y, double z, Operation<Void> original, @Local(name = "halfWidth") int halfWidth,
									  @Local(name = "lineHeight") int lineHeight, @Local(name = "pos") Vec3d pos) {
		var subtitlesHud = (SubtitlesHudHud) HudManager.getInstance().get(SubtitlesHudHud.ID);
		if (subtitlesHud.isEnabled() && !audibleSubtitles.isEmpty()) {
			var h = (int) audibleSubtitles.stream().filter(s -> {
				try {
					return GET_CLOSEST_HANDLE != null && GET_CLOSEST_HANDLE.invoke(s, pos) != null;
				} catch (Throwable e) {
					return false;
				}
			}).count() * (lineHeight + 1) + 2;
			var w = halfWidth * 2 + 4;
			if (!(Minecraft.getInstance().screen instanceof HudEditScreen)) {
				var updated = false;
				if (h != subtitlesHud.getContentHeight()) {
					subtitlesHud.setContentHeight(h);
					updated = true;
				}
				if (w != subtitlesHud.getContentWidth()) {
					subtitlesHud.setContentWidth(w);
					updated = true;
				}
				if (updated) {
					subtitlesHud.onBoundsUpdate();
				}
			}
			subtitlesHud.renderHud(AxoRenderContextImpl.getInstance(), 0);
			var gr = AxoRenderContextImpl.getInstance();
			subtitlesHud.scale(gr);
			gr.br$translateMatrix(subtitlesHud.getContentX(), subtitlesHud.getContentY());
			original.call((double) (halfWidth + 2), (double) (h - lineHeight / 2 - 2), z);
		} else {
			original.call(x, y, z);
		}
	}

	@WrapOperation(method = "render()V", at = @At(value = "INVOKE", target = "Lio/github/moehreag/soundfix/subtitles/SubtitlesHud;fill(IIIII)V"), remap = false, require = 0)
	private void switchEntryBackground(int x0, int y0, int x1, int y1, int col, Operation<Void> original) {
		var subtitlesHud = (SubtitlesHudHud) HudManager.getInstance().get(SubtitlesHudHud.ID);
		if (!subtitlesHud.isEnabled() || subtitlesHud.vanillaEntryBackground.get()) {
			original.call(x0, y0, x1, y1, col);
		}
	}

	@WrapOperation(method = "render()V", at = @At(value = "INVOKE", target = "Lio/github/moehreag/soundfix/subtitles/SubtitlesHud;m_57734177(IIIII)V"), remap = false, require = 0)
	private void switchEntryBackground$prod(int x0, int y0, int x1, int y1, int col, Operation<Void> original) {
		switchEntryBackground(x0, y0, x1, y1, col, original);
	}
}
