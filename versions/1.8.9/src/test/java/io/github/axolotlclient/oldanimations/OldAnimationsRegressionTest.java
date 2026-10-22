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

package io.github.axolotlclient.oldanimations;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.SpriteQuadProvider;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.client.resource.model.BasicBakedModel;
import net.minecraft.entity.living.effect.PotionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;

/** Opt-in checks against Minecraft's actual, mixin-transformed model objects. */
public final class OldAnimationsRegressionTest {
	private static volatile List<BakedQuad> result;
	private static boolean finished;
	private static final OldAnimationsConfig CONFIG = OldAnimationsConfig.instance;
	private static final ItemStack WOOD = new ItemStack(Blocks.TRAPDOOR);
	private static final ItemStack IRON = new ItemStack(Blocks.IRON_TRAPDOOR);
	private static final ItemStack SWORD = new ItemStack(Items.DIAMOND_SWORD);
	private static BakedModel sword, oldWood, oldIron, vanillaWood, vanillaIron;
	private static List<BakedQuad> oldSprite;
	private static int oldPotionColor, vanillaPotionColor;
	private static boolean savedEnabled, savedTrapdoor, savedPotion;

	private OldAnimationsRegressionTest() {
	}

	public static void install() {
		int[] ticks = {0};
		MinecraftClientEvents.TICK_END.register(client -> {
			if (finished) return;
			int tick = ++ticks[0];
			if (tick < 40 || tick % 4 != 0) return;
			try {
				step((tick - 40) / 4, client);
			} catch (Throwable failure) {
				failure.printStackTrace();
				restoreOptions();
				System.exit(1);
			}
		});
	}

	private static void step(int stage, Minecraft client) {
		switch (stage) {
			case 0 -> {
				savedEnabled = CONFIG.enabled.get();
				savedTrapdoor = CONFIG.trapDoorItemPosition.get();
				savedPotion = CONFIG.oldJumpBoostPotionColor.get();
				CONFIG.enabled.set(true);
				CONFIG.trapDoorItemPosition.set(true);
				CONFIG.oldJumpBoostPotionColor.set(true);
			}
			case 1 -> {
				testQuads();
				benchmarkFilter();
				sword = model(SWORD);
				oldWood = model(WOOD);
				oldIron = model(IRON);
				oldSprite = SpriteQuadProvider.get(sword, sword.getQuads());
				oldPotionColor = PotionHelper.getColor(8203, false);
				CONFIG.reloadPotionColors = false;
				CONFIG.trapDoorItemPosition.set(false);
			}
			case 2 -> {
				checkNoReload();
				vanillaWood = model(WOOD);
				vanillaIron = model(IRON);
				check(vanillaWood != oldWood && vanillaIron != oldIron, "trapdoor toggle must change both models");
				check(!CONFIG.reloadPotionColors, "trapdoor toggle must not invalidate potion colors");
				CONFIG.trapDoorItemPosition.set(true);
			}
			case 3 -> {
				checkNoReload();
				check(model(WOOD) == oldWood && model(IRON) == oldIron, "trapdoor variants must already be baked");
				CONFIG.enabled.set(false);
			}
			case 4 -> {
				checkNoReload();
				check(model(WOOD) == vanillaWood && model(IRON) == vanillaIron, "master off must select vanilla trapdoors");
				check(CONFIG.reloadPotionColors, "master off must invalidate potion colors");
				vanillaPotionColor = PotionHelper.getColor(8203, false);
				check(oldPotionColor != vanillaPotionColor, "master toggle must change leaping potion color");
				check(!CONFIG.reloadPotionColors, "potion lookup must consume invalidation");
			}
			case 5 -> {
				checkNoReload();
				check(!CONFIG.reloadPotionColors, "unchanged settings must not invalidate repeatedly");
				CONFIG.enabled.set(true);
			}
			case 6 -> {
				checkNoReload();
				check(model(WOOD) == oldWood && model(IRON) == oldIron, "master on must restore existing models");
				check(CONFIG.reloadPotionColors, "master on must invalidate potion colors");
				check(PotionHelper.getColor(8203, false) == oldPotionColor, "old potion color must be restored");
				CONFIG.oldJumpBoostPotionColor.set(false);
			}
			case 7 -> {
				checkNoReload();
				check(CONFIG.reloadPotionColors, "individual potion option must invalidate");
				check(PotionHelper.getColor(8203, false) == vanillaPotionColor, "individual option must restore vanilla color");
				CONFIG.enabled.set(false);
			}
			case 8 -> {
				checkNoReload();
				check(!CONFIG.reloadPotionColors, "ineffective master toggle must not invalidate colors");
				CONFIG.enabled.set(true);
			}
			case 9 -> {
				checkNoReload();
				check(!CONFIG.reloadPotionColors, "ineffective master toggle must not invalidate colors");
				client.reloadResources();
				BakedModel reloaded = model(SWORD);
				check(reloaded != sword, "explicit reload must replace the baked model");
				List<BakedQuad> fresh = SpriteQuadProvider.get(reloaded, reloaded.getQuads());
				check(fresh != oldSprite, "explicit reload must replace the sprite cache");
				check(fresh.equals(referenceFilter(reloaded.getQuads())), "reloaded cache must match new geometry");
				restoreOptions();
				finished = true;
				client.openScreen(new GlintRenderChecks());
			}
			default -> throw new AssertionError("Unexpected test stage " + stage);
		}
	}

	private static void checkNoReload() {
		check(model(SWORD) == sword, "setting change unexpectedly rebaked resources");
	}

	private static void restoreOptions() {
		CONFIG.enabled.set(savedEnabled);
		CONFIG.trapDoorItemPosition.set(savedTrapdoor);
		CONFIG.oldJumpBoostPotionColor.set(savedPotion);
	}

	private static BakedModel model(ItemStack stack) {
		return Minecraft.getInstance().getItemRenderer().getModelShaper().getModel(stack);
	}

	private static BasicBakedModel model(List<BakedQuad> quads) {
		return new BasicBakedModel(quads, List.of(List.of(), List.of(), List.of(), List.of(), List.of(), List.of()),
			false, false, null, ModelTransformations.NONE);
	}

	private static void testQuads() {
		BakedQuad front = new BakedQuad(new int[28], 0, Direction.SOUTH);
		BakedQuad back = new BakedQuad(new int[28], 0, Direction.NORTH);
		BakedQuad layer = new BakedQuad(new int[28], 1, Direction.SOUTH);
		List<BakedQuad> quads = List.of(back, front, layer, back);
		BasicBakedModel model = model(quads);
		List<BakedQuad> filtered = SpriteQuadProvider.get(model, quads);
		check(filtered.equals(List.of(front, layer)), "preserve south faces, layer order, and quad identity");
		check(quads.equals(List.of(back, front, layer, back)), "do not mutate the source model");
		check(SpriteQuadProvider.get(model, quads) == filtered, "repeated renders must reuse the list");
		List<BakedQuad> replacement = List.of(layer);
		check(SpriteQuadProvider.get(model, replacement).equals(replacement), "replacement quad list must invalidate cache");
		BasicBakedModel empty = model(List.of(back));
		check(SpriteQuadProvider.get(empty, empty.getQuads()).isEmpty(), "empty sprite must stay empty");
		check(SpriteQuadProvider.get(empty, empty.getQuads()) == SpriteQuadProvider.get(empty, empty.getQuads()),
			"empty sprites must also be cached");

		// A modded subclass can mutate a list in place. It must bypass the cache.
		List<BakedQuad> dynamicQuads = new ArrayList<>(List.of(front));
		BasicBakedModel dynamic = new BasicBakedModel(dynamicQuads, List.of(), false, false, null, ModelTransformations.NONE) {};
		check(SpriteQuadProvider.get(dynamic, dynamic.getQuads()).equals(List.of(front)), "dynamic model initial geometry");
		dynamicQuads.set(0, layer);
		check(SpriteQuadProvider.get(dynamic, dynamic.getQuads()).equals(List.of(layer)), "dynamic model updated geometry");
	}

	/** Exact pre-fix filter, also used as the geometry oracle. */
	private static List<BakedQuad> referenceFilter(List<BakedQuad> quads) {
		List<BakedQuad> filtered = new ArrayList<>(quads.size());
		for (BakedQuad quad : quads) {
			if (quad.getFace() == Direction.SOUTH) filtered.add(quad);
		}
		return filtered;
	}

	private static void benchmarkFilter() {
		var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
		long thread = Thread.currentThread().getId();
		for (int count : new int[]{66, 258, 1026}) {
			List<BakedQuad> quads = new ArrayList<>();
			for (int i = 0; i < count; i++) quads.add(new BakedQuad(new int[28], 0, i == 0 ? Direction.SOUTH : Direction.NORTH));
			BakedModel model = model(quads);
			for (int i = 0; i < 20000; i++) {
				result = referenceFilter(quads);
				result = SpriteQuadProvider.get(model, quads);
			}
			long before = bean.getThreadAllocatedBytes(thread);
			long start = System.nanoTime();
			for (int i = 0; i < 100000; i++) result = referenceFilter(quads);
			long referenceTime = System.nanoTime() - start;
			long referenceBytes = bean.getThreadAllocatedBytes(thread) - before;
			before = bean.getThreadAllocatedBytes(thread);
			start = System.nanoTime();
			for (int i = 0; i < 100000; i++) result = SpriteQuadProvider.get(model, quads);
			long cachedTime = System.nanoTime() - start;
			long cachedBytes = bean.getThreadAllocatedBytes(thread) - before;
			check(cachedBytes < referenceBytes / 100, "warm cache must eliminate at least 99% of filter allocation");
			System.out.printf(java.util.Locale.ROOT,
				"SPRITE_FILTER quads=%d old_bytes/op=%.2f cached_bytes/op=%.2f old_ns/op=%.2f cached_ns/op=%.2f%n",
				count, referenceBytes / 100000., cachedBytes / 100000., referenceTime / 100000., cachedTime / 100000.);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
