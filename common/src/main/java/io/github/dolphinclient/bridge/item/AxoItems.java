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

package io.github.dolphinclient.bridge.item;

import io.github.dolphinclient.bridge.BridgeVersion;
import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AxoItems {
	@RequiresImpl
	public static final AxoItem AIR = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_LEGGINGS = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_CHESTPLATE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_HELMET = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_SWORD = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_BOOTS = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem ARROW = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_INGOT = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem GOLD_INGOT = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem DIAMOND = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem EMERALD = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE_SWORD = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem DIAMOND_SWORD = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem DIAMOND_CHESTPLATE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem DIAMOND_BOOTS = BridgeUtil.noImplValue();

	@RequiresImpl(min = BridgeVersion.V1_20)
	public static final @Nullable AxoItem TIPPED_ARROW = BridgeUtil.noImplValue();

	@RequiresImpl(min = BridgeVersion.V1_20)
	public static final @Nullable AxoItem SPECTRAL_ARROW = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE_PICKAXE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem IRON_PICKAXE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem GOLD_PICKAXE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem DIAMOND_PICKAXE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE_AXE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE_SHOVEL = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem STONE_HOE = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem GLOWSTONE_DUST = BridgeUtil.noImplValue();

	@RequiresImpl
	public static final AxoItem ENDER_EYE = BridgeUtil.noImplValue();
}
