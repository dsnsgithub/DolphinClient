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

import java.util.ArrayList;
import java.util.List;

public class CustomModels {

	public static BakedModel brewingStandMiningModel(BakedModel model, BakedModel model2) {
		/* MC-4402 */
		List<BakedQuad> combined = new ArrayList<>(model.getQuads());
		for (BakedQuad quad : model2.getQuads()) {
			if (quad.getFace() != Direction.UP) {
				/* destroy stages textures. skips UP */
				combined.add(quad);
			}
		}

		return new BakedModel() {
			@Override
			public List<BakedQuad> getQuads(Direction face) {
				return model.getQuads(face);
			}

			@Override
			public List<BakedQuad> getQuads() {
				return combined;
			}

			@Override
			public boolean useAmbientOcclusion() {
				return model.useAmbientOcclusion();
			}

			@Override
			public boolean isGui3d() {
				return model.isGui3d();
			}

			@Override
			public boolean isCustomRenderer() {
				return model.isCustomRenderer();
			}

			@Override
			public TextureAtlasSprite getParticleIcon() {
				return model.getParticleIcon();
			}

			@Override
			public ModelTransformations getTransformations() {
				return model.getTransformations();
			}
		};
	}

	public static BakedModel bedMiningModel(BakedModel model, BakedModel model2) {
		/* MC-4402 */
		/* we need to keep every quad the same except for the UP and DOWN quads */
		List<BakedQuad> combined = new ArrayList<>();
		for (BakedQuad quad : model.getQuads()) {
			if (quad.getFace() == Direction.UP || quad.getFace() == Direction.DOWN) {
				/* bed texture. bug in 1.7 */
				combined.add(quad);
			}
		}
		for (BakedQuad quad : model2.getQuads()) {
			if (quad.getFace() != Direction.UP && quad.getFace() != Direction.DOWN) {
				/* destroy stages textures */
				combined.add(quad);
			}
		}

		return new BakedModel() {
			@Override
			public List<BakedQuad> getQuads(Direction face) {
				return model.getQuads(face);
			}

			@Override
			public List<BakedQuad> getQuads() {
				return combined;
			}

			@Override
			public boolean useAmbientOcclusion() {
				return model.useAmbientOcclusion();
			}

			@Override
			public boolean isGui3d() {
				return model.isGui3d();
			}

			@Override
			public boolean isCustomRenderer() {
				return model.isCustomRenderer();
			}

			@Override
			public TextureAtlasSprite getParticleIcon() {
				return model.getParticleIcon();
			}

			@Override
			public ModelTransformations getTransformations() {
				return model.getTransformations();
			}
		};
	}
}
