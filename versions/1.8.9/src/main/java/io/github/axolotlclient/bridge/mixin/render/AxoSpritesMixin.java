/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.bridge.mixin.render;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.bridge.impl.AxoSpriteImpl;
import io.github.axolotlclient.bridge.render.AxoSprite;
import io.github.axolotlclient.bridge.render.AxoSprites;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AxoSprites.class, remap = false)
public abstract class AxoSpritesMixin {
	@Mutable
	@Shadow
	@Final
	public static AxoSprite BADGE;

	@Mutable
	@Shadow
	@Final
	public static AxoSprite BARRIER_ITEM_ICON;

	@Mutable
	@Shadow
	@Final
	public static AxoSprite FURNACE_OFF;

	@Mutable
	@Shadow
	@Final
	public static AxoSprite FURNACE_ON;

	@Mutable
	@Shadow
	@Final
	public static AxoSprite MAGNET_ICON;

	@Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
	private static void setStaticValues(CallbackInfo info) {
		BADGE = new AxoSpriteImpl.Simple((Identifier) DolphinClientCommon.BADGE_PATH, 0, 0, 15, 15);
		BARRIER_ITEM_ICON = new AxoSpriteImpl.Simple(new Identifier("textures/items/barrier.png"), 0, 0, 16, 16);
		FURNACE_OFF = new AxoSpriteImpl.Simple(new Identifier("textures/blocks/furnace_front_off.png"), 0, 0, 16, 16);
		FURNACE_ON = new AxoSpriteImpl.Simple(new Identifier("textures/blocks/furnace_front_on.png"), 0, 0, 16, 16);
		MAGNET_ICON = new AxoSpriteImpl.Simple(new Identifier(DolphinClientCommon.MODID, "textures/gui/sprites/magnet.png"), 0, 0, 9, 9);
		info.cancel();
	}
}
