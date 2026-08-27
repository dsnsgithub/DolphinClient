/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.axolotlclient.modules.hud;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Item icons used by the centered mods menu. Minecraft items stay readable at
 * small sizes and avoid extra texture assets.
 */
final class ModsMenuIcons {
	private static final Map<String, ItemStack> BY_KEY = new HashMap<>();
	private static final ItemStack FALLBACK = new ItemStack(Items.PAPER);

	static {
		put("fpshud", Items.CLOCK);
		put("cpshud", Items.BOW);
		put("armorhud", Items.IRON_CHESTPLATE);
		put("potionshud", Items.POTION);
		put("togglesprint", Items.IRON_BOOTS);
		put("iphud", Items.SIGN);
		put("iconhud", Items.NETHER_STAR);
		put("speedhud", Items.SUGAR);
		put("coordshud", Items.COMPASS);
		put("arrowhud", Items.ARROW);
		put("itemupdatehud", Item.byBlock(Blocks.HOPPER));
		put("irltimehud", Items.CLOCK);
		put("reachhud", Items.DIAMOND_SWORD);
		put("memoryhud", Items.REDSTONE);
		put("playercounthud", new ItemStack(Items.SKULL, 1, 3));
		put("compasshud", Items.COMPASS);
		put("tpshud", Items.REDSTONE);
		put("combohud", Items.IRON_SWORD);
		put("mousemovementhud", Items.FISHING_ROD);
		put("daycounterhud", Items.CLOCK);
		put("inventoryhud", Item.byBlock(Blocks.CHEST));
		put("xphud", Items.EXPERIENCE_BOTTLE);
		put("tab_overlay_hud", new ItemStack(Items.SKULL, 1, 3));
		put("subtitleshud", Items.BOOK);
		put("actionbarhud", Items.SIGN);
		put("bossbarhud", Items.NETHER_STAR);
		put("crosshairhud", Items.ARROW);
		put("debugcountershud", Items.REDSTONE);
		put("hotbarhud", Items.DIAMOND);
		put("scoreboardhud", Items.PAPER);
		put("keystrokehud", Items.BOOK);
		put("packdisplayhud", Items.BOOK);
		put("playerhud", Items.ARMOR_STAND);
		put("chathud", Items.SIGN);
		put("pinghud", Items.ENDER_PEARL);

		put("general", Items.REDSTONE);
		put("rendering", Item.byBlock(Blocks.GLASS));
		put("nametagOptions", Items.NAME_TAG);
		put("hud", Items.PAINTING);
		put("hypixel-mods", Items.GOLDEN_SWORD);
		put("api.category", Items.ENDER_EYE);
		put("zoom", Item.byBlock(Blocks.GLASS));
		put("motionBlur", Items.POTION);
		put("menublur", Item.byBlock(Blocks.GLASS));
		put("particles", Items.BLAZE_POWDER);
		put("tnttime", Item.byBlock(Blocks.TNT));
		put("freelook", Items.ENDER_EYE);
		put("rpc", Items.REDSTONE);
		put("scrollableTooltips", Items.BOOK);
		put("screenshotUtils", Items.PAINTING);
		put("fpsLimiter", Items.CLOCK);
		put("auth", Items.SKULL);
		put("sky", Items.ENDER_EYE);
		put("blockOutlines", Item.byBlock(Blocks.STONE));
		put("timeChanger", Items.CLOCK);
		put("beams", Items.BLAZE_ROD);
		put("levelhead", Items.GOLDEN_HELMET);
		put("autogg", Items.PAPER);
		put("autotip", Items.GOLD_INGOT);
		put("nickhider", Items.NAME_TAG);
		put("autoboop", Items.BONE);
		put("skyblock", Items.EMERALD);
		put("bedwars", Items.BED);
		put("titles", Items.SIGN);

		put("axolotlclient-oldanimations", Items.CLOCK);
		put("blockingItemUsing", Items.IRON_SWORD);
		put("sneaking", Items.IRON_BOOTS);
		put("items", Items.DIAMOND);
		put("spriteRendering", Items.PAINTING);
		put("itemPositions", Items.STICK);
		put("resources", Items.BOOK);
		put("models", Items.ARMOR_STAND);
		put("textures", Item.byBlock(Blocks.WOOL));
		put("sounds", Items.RECORD_13);
		put("combat", Items.DIAMOND_SWORD);
		put("gui", Items.SIGN);
		put("debugOverlay", Items.COMPASS);
		put("tabOverlay", new ItemStack(Items.SKULL, 1, 3));
		put("enchantmentGlint", Items.EXPERIENCE_BOTTLE);
		put("hitbox", Items.SLIME_BALL);
		put("offsets", Items.COMPASS);
		put("misc", Items.PAPER);
	}

	private ModsMenuIcons() {
	}

	static ItemStack forKey(String key) {
		if (key != null && key.startsWith("custom_hud")) {
			return FALLBACK;
		}
		ItemStack stack = BY_KEY.get(key);
		return stack != null ? stack : FALLBACK;
	}

	private static void put(String key, Item item) {
		BY_KEY.put(key, new ItemStack(item));
	}

	private static void put(String key, ItemStack stack) {
		BY_KEY.put(key, stack);
	}
}
