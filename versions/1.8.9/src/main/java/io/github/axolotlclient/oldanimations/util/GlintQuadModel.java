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

import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.texture.TextureAtlasSprite;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.util.math.Direction;

import java.util.Collections;
import java.util.List;

/* this class is pretty much unnecessary. i made it for fun >:D */
/* it was my attempt at porting the 1.7 gui glint rendering code to the 1.8 model system */
public class GlintQuadModel implements BakedModel {
	private final BakedQuad quad;

	public GlintQuadModel(float speed, float skew) {
		/* in 1.7, the height and width are both 20 which are both divided by 16 x 16 */
		float dimension = 20.0F / 256.0F;
		/* these position and uv values were taken from 1.7 */
		float[] verts = {
			-0.5F, -0.25F, 0.0F, speed + dimension * skew, dimension,
			0.5F, -0.25F, 0.0F, speed + dimension + dimension * skew, dimension,
			0.5F,  0.75F, 0.0F, speed + dimension, 0.0F,
			-0.5F,  0.75F, 0.0F, speed, 0.0F
		};
		/* we need to match the BLOCK_NORMALS vertex format */
		/* we already have 4 * 5 array, but we need 4 * 7 */
		int[] data = new int[28];
		for (int i = 0; i < 4; i++) {
			int src = i * 5; /* where to read from */
			int dst = i * 7; /* where to write to */
			data[dst] = Float.floatToRawIntBits(verts[src]);
			data[dst + 1] = Float.floatToRawIntBits(verts[src + 1]);
			data[dst + 2] = Float.floatToRawIntBits(verts[src + 2]);
			data[dst + 4] = Float.floatToRawIntBits(verts[src + 3]);
			data[dst + 5] = Float.floatToRawIntBits(verts[src + 4]);
			/* skipping 3 and 6 because they're overwritten by the item renderer */
		}
		/* tint index doesn't matter. having the glint on the SOUTH face ensures compatibility with our own culling */
		quad = new BakedQuad(data, -1, Direction.SOUTH);
	}

	@Override public List<BakedQuad> getQuads(Direction direction) {
		return Collections.emptyList();
	}

	@Override public List<BakedQuad> getQuads() {
		/* we literally have 1 singular quad sitting in here. */
		/* the end result is just a rectangle which is our glint */
		return Collections.singletonList(quad);
	}

	@Override public boolean useAmbientOcclusion() {
		return false;
	}

	@Override public boolean isGui3d() {
		return false;
	}

	@Override public boolean isCustomRenderer() {
		return false;
	}

	@Override public TextureAtlasSprite getParticleIcon() {
		return null;
	}

	@Override public ModelTransformations getTransformations() {
		return ModelTransformations.NONE;
	}
}
