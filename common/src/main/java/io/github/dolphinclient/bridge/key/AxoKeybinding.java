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

package io.github.dolphinclient.bridge.key;

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.PlatformImplInternal;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract representation of a keybind
 */
public interface AxoKeybinding {
	static AxoKeybinding create(@Nullable AxoKey defaultKey, String name) {
		return PlatformImplInternal.createKeyBinding(defaultKey, name);
	}

	@RequiresImpl
	default void br$registerOnClicked(Runnable runnable) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$registerOnReleased(Runnable runnable) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$registerOnConsumeClick(Runnable runnable) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default AxoKey br$getBoundKey() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default boolean br$isPressed() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default boolean br$consumeClick() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default void br$click() {
		throw BridgeUtil.noImpl();
	}
}
