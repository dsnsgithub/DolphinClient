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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.client.resource.model.BasicBakedModel;
import net.minecraft.util.math.Direction;

/** The south-facing quads used by the flat item renderer. */
public interface SpriteQuadProvider {
	List<BakedQuad> axolotlclient$getSpriteQuads(List<BakedQuad> quads);

	static List<BakedQuad> get(BakedModel model, List<BakedQuad> quads) {
		// Vanilla baked models keep the same quad list for their lifetime. Do not
		// cache custom models or subclasses: they may return dynamic geometry.
		if (model.getClass() == BasicBakedModel.class) {
			return ((SpriteQuadProvider) model).axolotlclient$getSpriteQuads(quads);
		}
		return filter(quads);
	}

	static List<BakedQuad> filter(List<BakedQuad> quads) {
		List<BakedQuad> filtered = new ArrayList<>();
		for (BakedQuad quad : quads) {
			if (quad.getFace() == Direction.SOUTH) {
				filtered.add(quad);
			}
		}
		return List.copyOf(filtered);
	}
}
