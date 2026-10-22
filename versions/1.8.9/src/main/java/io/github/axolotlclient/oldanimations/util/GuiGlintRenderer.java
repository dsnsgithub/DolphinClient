/*
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

package io.github.axolotlclient.oldanimations.util;

import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.resource.Identifier;

/** The previous 1.7 GUI overlay, with both quads submitted in one draw. */
public final class GuiGlintRenderer {
	private GuiGlintRenderer() {
	}

	public static void render(TextureManager textures, Identifier glint, long time) {
		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(518);
		GlStateManager.disableLighting();
		GlStateManager.blendFuncSeparate(772, 1, 0, 0);
		textures.bind(glint);
		GlStateManager.pushMatrix();
		try {
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			GlStateManager.translatef(0.0F, -0.25F, 0.0F);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer = tesselator.getBuffer();
			buffer.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			appendQuad(buffer, (float) (time % 3000L) / 3000.0F, 4.0F);
			appendQuad(buffer, (float) (time % 4873L) / 4873.0F, -1.0F);
			tesselator.end();
		} finally {
			GlStateManager.popMatrix();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.enableLighting();
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);
			textures.bind(TextureAtlas.BLOCKS_LOCATION);
		}
	}

	private static void appendQuad(BufferBuilder buffer, float speed, float skew) {
		// Same positions, UVs and color as the removed GlintQuadModel, without
		// allocating a model, vertex arrays or lists for each overlay pass.
		float dimension = 20.0F / 256.0F;
		vertex(buffer, -0.5F, -0.25F, speed + dimension * skew, dimension);
		vertex(buffer, 0.5F, -0.25F, speed + dimension + dimension * skew, dimension);
		vertex(buffer, 0.5F, 0.75F, speed + dimension, 0.0F);
		vertex(buffer, -0.5F, 0.75F, speed, 0.0F);
	}

	private static void vertex(BufferBuilder buffer, float x, float y, float u, float v) {
		buffer.vertex(x, y, 0.0F).texture(u, v).color(128, 64, 204, 255).nextVertex();
	}
}
