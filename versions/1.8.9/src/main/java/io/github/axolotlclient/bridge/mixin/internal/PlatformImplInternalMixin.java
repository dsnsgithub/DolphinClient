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

package io.github.axolotlclient.bridge.mixin.internal;

import java.util.Objects;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.AxoPlayerListEntry;
import io.github.axolotlclient.bridge.entity.effect.AxoStatusEffect;
import io.github.axolotlclient.bridge.entity.effect.AxoStatusEffectInstance;
import io.github.axolotlclient.bridge.impl.AxoKeyImpl;
import io.github.axolotlclient.bridge.impl.AxoSpriteImpl;
import io.github.axolotlclient.bridge.impl.Bridge;
import io.github.axolotlclient.bridge.internal.PlatformImplInternal;
import io.github.axolotlclient.bridge.item.AxoItem;
import io.github.axolotlclient.bridge.item.AxoItemStack;
import io.github.axolotlclient.bridge.item.AxoItems;
import io.github.axolotlclient.bridge.key.AxoKey;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoSprite;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.bridge.scores.AxoTeam;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoProfiler;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.util.Util;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.PlayerInfo;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.locale.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.scoreboard.team.AbstractTeam;
import net.minecraft.scoreboard.team.Team;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("OverwriteModifiers")
@Mixin(value = PlatformImplInternal.class, remap = false)
public abstract class PlatformImplInternalMixin {
	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static @Nullable AxoWindow getWindow() {
		return Util.getWindow();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoMinecraftClient getMinecraftClientInstance() {
		return Minecraft.getInstance();
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoProfiler getProfiler() {
		return Minecraft.getInstance().profiler;
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTranslatedString(String nameKey, Object[] args) {
		return I18n.translate(nameKey, args);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static long getMeasuringTimeMs() {
		return Minecraft.getTime();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static int getCurrentFps() {
		return Minecraft.getCurrentFps();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoKeybinding createKeyBinding(AxoKey defaultKey, String name) {
		final var id = ((AxoKeyImpl) Objects.requireNonNullElse(defaultKey, AxoKeys.KEY_UNKNOWN)).id();
		final var binding = new KeyBinding(name, id, DolphinClientCommon.KEY_CATEGORY);
		Bridge.addKeybind(binding);
		return binding;
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoIdentifier createIdentifier(String ns, String path) {
		return new Identifier(ns, path);
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoIdentifier parseIdentifier(String id) {
		return new Identifier(id);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoStatusEffectInstance createStatusEffectInstance(AxoStatusEffect effect, int duration) {
		return new StatusEffectInstance(((StatusEffect) effect).getId(), duration);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoItemStack createItemStack(AxoItem item, int count) {
		if (count == 0 || item == AxoItems.AIR) {
			return new ItemStack(Item.byBlock(Blocks.STONE), 0);
		}

		return new ItemStack((Item) item, count);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoSprite createTexture(GraphicsOption option) {
		return new AxoSpriteImpl.Config(option);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoText.Mutable createLiteral(String text) {
		return new LiteralText(text);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static AxoText.Mutable createTranslatable(String key, Object... args) {
		return new TranslatableText(key, args);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static int tickCount() {
		return Minecraft.getInstance().gui.getTicks();
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String stripText(String text) {
		return Formatting.strip(text);
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTabNameFor(AxoPlayerListEntry player) {
		// Inlined PlayerTabOverlay#getDisplayName to avoid StackOverflowError due to mixin
		var p = (PlayerInfo) player;
		var displayName = p.getDisplayName();
		return displayName != null ? displayName.getFormattedString() : Team.getMemberDisplayName(p.getTeam(), p.getProfile().getName());
	}

	/**
	 * @author Flowey
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static void setTabListHeader(AxoText text) {
		Minecraft.getInstance().gui.getPlayerTabOverlay().setHeader((Text) text);
	}

	/**
	 * @author moehreag
	 * @reason Implement bridge platform.
	 */
	@Overwrite
	public static String getTeamMemberDisplayName(AxoTeam team, String s) {
		return Team.getMemberDisplayName((AbstractTeam) team, s);
	}
}
