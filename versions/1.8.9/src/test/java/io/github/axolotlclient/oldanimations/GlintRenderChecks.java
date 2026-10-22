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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.GuiGlintRenderer;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.client.resource.model.BasicBakedModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Direction;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Checks the actual glint dispatch and compares pixels to the pre-#28 renderer. */
public final class GlintRenderChecks extends Screen {
	public static int vanillaPasses;
	private static final Identifier GLINT = new Identifier("textures/misc/enchanted_item_glint.png");
	private int frames;

	@Override
	public void render(int x, int y, float delta) {
		if (++frames != 5) return;
		boolean enabled = OldAnimationsConfig.isEnabled();
		try {
			ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
			sword.addEnchantment(Enchantment.byId(16), 1);
			ItemStack block = new ItemStack(Blocks.STONE);
			block.addEnchantment(Enchantment.byId(16), 1);
			checkPasses(sword, true, false, 0);
			checkPasses(block, true, false, 0);
			checkPasses(sword, false, false, 2);
			checkPasses(block, false, false, 2);
			checkPasses(sword, true, true, 2);
			checkPasses(block, true, true, 0);
			checkPasses(sword, false, true, 2);
			checkPasses(block, false, true, 2);
			OldAnimationsConfig.instance.enabled.set(true);
			comparePixels();
			System.out.println("OLD_ANIMATIONS_REGRESSION_TESTS PASSED (including glint dispatch and pixel equivalence)");
			OldAnimationsConfig.instance.enabled.set(enabled);
			System.exit(0);
		} catch (Throwable failure) {
			failure.printStackTrace();
			OldAnimationsConfig.instance.enabled.set(enabled);
			System.exit(1);
		}
	}

	private static void checkPasses(ItemStack stack, boolean enabled, boolean held, int expected) {
		OldAnimationsConfig.instance.enabled.set(enabled);
		vanillaPasses = 0;
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GlStateManager.enableDepthTest();
		GlStateManager.pushMatrix();
		try {
			if (held) {
				Minecraft.getInstance().getItemRenderer().renderItemInHand(stack, ModelTransformations.Type.FIRST_PERSON);
			} else {
				io.github.axolotlclient.modules.hud.util.ItemUtil.renderGuiItemModel(stack, 20, 20);
			}
		} finally {
			GlStateManager.popMatrix();
		}
		if (vanillaPasses != expected) throw new AssertionError("Glint passes: expected " + expected + ", got " + vanillaPasses);
		checkGl();
	}

	private static void comparePixels() throws Exception {
		Method render = ItemRenderer.class.getDeclaredMethod("render", BakedModel.class, int.class);
		render.setAccessible(true);
		int changed = 0;
		for (long time : new long[]{0, 1500, 2999, 4872, 12345}) {
			prepareImage();
			byte[] background = pixels();
			renderReference(render, time);
			byte[] reference = pixels();
			prepareImage();
			GuiGlintRenderer.render(Minecraft.getInstance().getTextureManager(), GLINT, time);
			byte[] actual = pixels();
			if (!Arrays.equals(reference, actual)) throw new AssertionError("GUI glint pixels differ at time " + time);
			for (int i = 0; i < actual.length; i++) if (actual[i] != background[i]) changed++;
			if (GL11.glGetInteger(GL11.GL_DEPTH_FUNC) != GL11.GL_LEQUAL || !GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)) {
				throw new AssertionError("GUI glint leaked depth state");
			}
			checkGl();
		}
		if (changed == 0) throw new AssertionError("Glint comparison rendered no visible pixels");
		System.out.println("GUI_GLINT_PIXELS: exact match against pre-#28 at five animation phases");
	}

	private static void prepareImage() {
		GL11.glViewport(0, 0, 64, 64);
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.loadIdentity();
		GL11.glOrtho(-1, 1, -1, 1, -1, 1);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.disableLighting();
		GlStateManager.disableAlphaTest();
		GlStateManager.disableCull();
		GlStateManager.enableDepthTest();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.disableBlend();
		GlStateManager.disableTexture();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		BufferBuilder buffer = Tesselator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(-1, -1, 0).color(40, 60, 80, 255).nextVertex();
		buffer.vertex(1, -1, 0).color(40, 60, 80, 255).nextVertex();
		buffer.vertex(1, 1, 0).color(40, 60, 80, 255).nextVertex();
		buffer.vertex(-1, 1, 0).color(40, 60, 80, 255).nextVertex();
		Tesselator.getInstance().end();
		GlStateManager.enableTexture();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, 1);
	}

	/** Original GUI glint submission from main at 591f42dd5, including two draws. */
	private static void renderReference(Method render, long time) throws Exception {
		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(518);
		GlStateManager.disableLighting();
		GlStateManager.blendFuncSeparate(772, 1, 0, 0);
		Minecraft.getInstance().getTextureManager().bind(GLINT);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(0.5F, 0.5F, 0.5F);
		GlStateManager.translatef(0.0F, -0.25F, 0.0F);
		render.invoke(Minecraft.getInstance().getItemRenderer(), referenceModel((float) (time % 3000L) / 3000.0F, 4.0F), -8372020);
		render.invoke(Minecraft.getInstance().getItemRenderer(), referenceModel((float) (time % 4873L) / 4873.0F, -1.0F), -8372020);
		GlStateManager.popMatrix();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.enableLighting();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
	}

	/** Vertex data from the deleted GlintQuadModel, independent of the new builder. */
	private static BakedModel referenceModel(float speed, float skew) {
		float dimension = 20.0F / 256.0F;
		float[] verts = {
			-0.5F, -0.25F, 0.0F, speed + dimension * skew, dimension,
			0.5F, -0.25F, 0.0F, speed + dimension + dimension * skew, dimension,
			0.5F, 0.75F, 0.0F, speed + dimension, 0.0F,
			-0.5F, 0.75F, 0.0F, speed, 0.0F
		};
		int[] data = new int[28];
		for (int i = 0; i < 4; i++) {
			int src = i * 5;
			int dst = i * 7;
			data[dst] = Float.floatToRawIntBits(verts[src]);
			data[dst + 1] = Float.floatToRawIntBits(verts[src + 1]);
			data[dst + 2] = Float.floatToRawIntBits(verts[src + 2]);
			data[dst + 4] = Float.floatToRawIntBits(verts[src + 3]);
			data[dst + 5] = Float.floatToRawIntBits(verts[src + 4]);
		}
		return new BasicBakedModel(List.of(new BakedQuad(data, -1, Direction.SOUTH)),
			List.of(List.of(), List.of(), List.of(), List.of(), List.of(), List.of()), false, false, null, ModelTransformations.NONE);
	}

	private static byte[] pixels() {
		ByteBuffer buffer = BufferUtils.createByteBuffer(64 * 64 * 4);
		GL11.glReadPixels(0, 0, 64, 64, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	private static void checkGl() {
		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR) throw new AssertionError("OpenGL error " + error);
	}
}
