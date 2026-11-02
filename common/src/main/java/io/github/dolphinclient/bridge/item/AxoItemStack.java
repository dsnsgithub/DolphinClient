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

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.PlatformImplInternal;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import io.github.dolphinclient.bridge.util.AxoText;
import io.github.dolphinclient.util.MathUtil;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public interface AxoItemStack {
	static AxoItemStack of(AxoItem item, int count) {
		return PlatformImplInternal.createItemStack(item, count);
	}

	static AxoItemStack of(AxoItem item) {
		return PlatformImplInternal.createItemStack(item, 1);
	}

	@RequiresImpl
	default AxoItem br$getItem() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default AxoItemStack br$copy() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$setCount(int count) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default int br$getCount() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default int br$getDamage() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default int br$getMaxDamage() {
		throw BridgeUtil.noImpl();
	}

	@ApiStatus.NonExtendable
	default boolean br$isEmpty() {
		return br$getCount() == 0 || br$getItem() == AxoItems.AIR;
	}

	@RequiresImpl
	default int br$getEnchantment(AxoEnchant enchant) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$setEnchantment(AxoEnchant enchant, int level) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$removeEnchantment(AxoEnchant enchant) {
		throw BridgeUtil.noImpl();
	}

	@ApiStatus.NonExtendable
	default boolean br$hasEnchantment(AxoEnchant enchant) {
		return br$getEnchantment(enchant) != 0;
	}

	default int br$getBarColor() {
		float f = Math.max(0.0F, ((float) this.br$getMaxDamage() - (float) br$getDamage()) / (float) this.br$getMaxDamage());
		return MathUtil.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}

	@RequiresImpl
	default AxoText br$getHoverName() {
		throw BridgeUtil.noImpl();
	}
}
