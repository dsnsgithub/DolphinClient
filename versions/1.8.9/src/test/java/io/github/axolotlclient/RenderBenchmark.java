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

package io.github.axolotlclient;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Locale;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.lwjgl.opengl.GL11;

/**
 * Opt-in rendering microbenchmark. Measures nine-item batches, not world FPS.
 * The same source can be copied to the parent PR/base for an A/B comparison.
 */
public final class RenderBenchmark extends Screen {
	private static final int WARMUP = 1000;
	private static final int SAMPLES = 1000;
	private int frame;

	public static void install() {
		int[] ticks = {0};
		MinecraftClientEvents.TICK_END.register(client -> {
			if (++ticks[0] == 40) client.openScreen(new RenderBenchmark());
		});
	}

	@Override
	public void render(int x, int y, float delta) {
		if (++frame != 5) return;
		try {
			System.out.println("BENCH renderer=" + GL11.glGetString(GL11.GL_RENDERER));
			ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
			ItemStack enchanted = sword.copy();
			enchanted.addEnchantment(Enchantment.byId(16), 1);
			ItemStack block = new ItemStack(Blocks.STONE);
			ItemStack glintBlock = block.copy();
			glintBlock.addEnchantment(Enchantment.byId(16), 1);
			run("gui-sword", sword, false);
			run("gui-enchanted-sword", enchanted, false);
			run("gui-block", block, false);
			run("gui-enchanted-block", glintBlock, false);
			run("gui-skull", new ItemStack(Items.SKULL, 1, 3), false);
			run("held-sword", sword, true);
			run("held-enchanted-sword", enchanted, true);
			System.out.println("BENCH DONE");
			System.exit(0);
		} catch (Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
	}

	private void run(String name, ItemStack stack, boolean held) {
		long[] times = new long[SAMPLES];
		var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
		long thread = Thread.currentThread().getId();
		long allocated = 0;
		for (int sample = -WARMUP; sample < SAMPLES; sample++) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glFinish();
			long bytes = bean.getThreadAllocatedBytes(thread);
			long start = System.nanoTime();
			for (int i = 0; i < 9; i++) {
				if (held) {
					Lighting.turnOnGui();
					GlStateManager.pushMatrix();
					GlStateManager.translatef(20 + i * 20, 20, 0);
					GlStateManager.scalef(16, 16, 16);
					Minecraft.getInstance().getItemRenderer().renderItemInHand(stack, ModelTransformations.Type.FIRST_PERSON);
					GlStateManager.popMatrix();
					Lighting.turnOff();
				} else {
					io.github.axolotlclient.modules.hud.util.ItemUtil.renderGuiItemModel(stack, 20 + i * 20, 20);
				}
			}
			GL11.glFinish();
			long elapsed = System.nanoTime() - start;
			if (sample >= 0) {
				times[sample] = elapsed;
				allocated += bean.getThreadAllocatedBytes(thread) - bytes;
			}
		}
		Arrays.sort(times);
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) throw new AssertionError("OpenGL error " + error + " in " + name);
		System.out.printf(Locale.ROOT, "BENCH %s median_us=%.2f p95_us=%.2f p99_us=%.2f bytes_per_batch=%d gl_error=%d%n",
			name, times[SAMPLES / 2] / 1000., times[SAMPLES * 95 / 100] / 1000.,
			times[SAMPLES * 99 / 100] / 1000., allocated / SAMPLES, error);
	}
}
