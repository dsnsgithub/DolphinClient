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

package io.github.dolphinclient.bridge.mixin.item;

import io.github.dolphinclient.bridge.impl.AirItemImpl;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItems;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AxoItems.class, remap = false)
public abstract class AxoItemsMixin {
	@Mutable
	@Shadow
	@Final
	public static AxoItem AIR;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_HELMET;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_LEGGINGS;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_CHESTPLATE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_BOOTS;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_SWORD;

	@Mutable
	@Shadow
	@Final
	public static AxoItem ARROW;

	@Mutable
	@Shadow
	@Final
	public static AxoItem IRON_INGOT;

	@Mutable
	@Shadow
	@Final
	public static AxoItem GOLD_INGOT;

	@Mutable
	@Shadow
	@Final
	public static AxoItem DIAMOND;

	@Mutable
	@Shadow
	@Final
	public static AxoItem EMERALD;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE_SWORD;

	@Mutable
	@Shadow
	@Final
	public static AxoItem DIAMOND_SWORD;

	@Mutable
	@Shadow
	@Final
	public static AxoItem DIAMOND_CHESTPLATE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem DIAMOND_BOOTS;

	@Mutable
	@Shadow
	@Final
	public static AxoItem TIPPED_ARROW;

	@Mutable
	@Shadow
	@Final
	public static AxoItem SPECTRAL_ARROW;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE_PICKAXE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE_AXE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE_SHOVEL;

	@Mutable
	@Shadow
	@Final
	public static AxoItem STONE_HOE;

	@Mutable
	@Shadow
	@Final
	public static AxoItem GLOWSTONE_DUST;

	@Mutable
	@Shadow
	@Final
	public static AxoItem ENDER_EYE;

	@Shadow
	@Mutable
	@Final
	public static AxoItem DIAMOND_PICKAXE;

	@Shadow
	@Mutable
	@Final
	public static AxoItem GOLD_PICKAXE;

	@Shadow
	@Mutable
	@Final
	public static AxoItem IRON_PICKAXE;

	@Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
	private static void setStaticValues(CallbackInfo info) {
		AIR = AirItemImpl.getInstance();
		IRON_HELMET = Items.IRON_HELMET;
		IRON_CHESTPLATE = Items.IRON_CHESTPLATE;
		IRON_LEGGINGS = Items.IRON_LEGGINGS;
		IRON_BOOTS = Items.IRON_BOOTS;
		IRON_SWORD = Items.IRON_SWORD;
		ARROW = Items.ARROW;
		IRON_INGOT = Items.IRON_INGOT;
		GOLD_INGOT = Items.GOLD_INGOT;
		DIAMOND = Items.DIAMOND;
		EMERALD = Items.EMERALD;
		STONE_SWORD = Items.STONE_SWORD;
		DIAMOND_SWORD = Items.DIAMOND_SWORD;
		DIAMOND_CHESTPLATE = Items.DIAMOND_CHESTPLATE;
		DIAMOND_BOOTS = Items.DIAMOND_BOOTS;
		TIPPED_ARROW = null;
		SPECTRAL_ARROW = null;
		STONE = Item.byBlock(Blocks.STONE);
		STONE_PICKAXE = Items.STONE_PICKAXE;
		STONE_AXE = Items.STONE_AXE;
		STONE_SHOVEL = Items.STONE_SHOVEL;
		STONE_HOE = Items.STONE_HOE;
		GLOWSTONE_DUST = Items.GLOWSTONE_DUST;
		ENDER_EYE = Items.ENDER_EYE;
		DIAMOND_PICKAXE = Items.DIAMOND_PICKAXE;
		GOLD_PICKAXE = Items.GOLDEN_PICKAXE;
		IRON_PICKAXE = Items.IRON_PICKAXE;
		info.cancel();
	}
}
