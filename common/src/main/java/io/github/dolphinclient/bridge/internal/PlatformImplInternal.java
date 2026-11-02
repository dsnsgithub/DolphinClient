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

package io.github.dolphinclient.bridge.internal;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.AxoPlayerListEntry;
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffect;
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffectInstance;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.key.AxoKey;
import io.github.dolphinclient.bridge.key.AxoKeybinding;
import io.github.dolphinclient.bridge.render.AxoSprite;
import io.github.dolphinclient.bridge.render.AxoWindow;
import io.github.dolphinclient.bridge.scores.AxoTeam;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.bridge.util.AxoProfiler;
import io.github.dolphinclient.bridge.util.AxoText;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Internal implementations for static platform methods.
 */
@SuppressWarnings("unused")
@ApiStatus.Internal
public class PlatformImplInternal {
	@RequiresImpl
	public static @Nullable AxoWindow getWindow() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoMinecraftClient getMinecraftClientInstance() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoProfiler getProfiler() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static String getTranslatedString(String nameKey, Object[] args) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static long getMeasuringTimeMs() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static int getCurrentFps() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static int tickCount() {
		throw BridgeUtil.noImpl();
	}

	// constructors
	@RequiresImpl
	public static AxoItemStack createItemStack(AxoItem item, int count) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoIdentifier createIdentifier(String ns, String path) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoIdentifier parseIdentifier(String id) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoKeybinding createKeyBinding(@Nullable AxoKey defaultKey, String name) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoStatusEffectInstance createStatusEffectInstance(AxoStatusEffect effect, int duration) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoSprite createTexture(GraphicsOption option) {
		throw BridgeUtil.noImpl();
	}

	public static AxoText.Mutable createEmpty() {
		return createLiteral("");
	}

	@RequiresImpl
	public static AxoText.Mutable createLiteral(String text) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static AxoText.Mutable createTranslatable(String key, Object... args) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static String stripText(String text) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static String getTabNameFor(AxoPlayerListEntry player) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static void setTabListHeader(AxoText text) {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	public static String getTeamMemberDisplayName(AxoTeam team, String s) {
		throw BridgeUtil.noImpl();
	}
}
