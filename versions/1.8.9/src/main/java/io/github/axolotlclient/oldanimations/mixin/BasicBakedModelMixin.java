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

package io.github.axolotlclient.oldanimations.mixin;

import java.util.List;

import io.github.axolotlclient.oldanimations.util.SpriteQuadProvider;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.client.resource.model.BasicBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BasicBakedModel.class)
public abstract class BasicBakedModelMixin implements SpriteQuadProvider {
	@Unique
	private List<BakedQuad> axolotlclient$spriteSourceQuads;

	@Unique
	private List<BakedQuad> axolotlclient$spriteQuads;

	@Override
	public List<BakedQuad> axolotlclient$getSpriteQuads(List<BakedQuad> quads) {
		// The cache belongs to the baked model, so resource reloads replace it
		// along with the geometry. Glint passes reuse it without scanning or
		// allocating another list on every item render.
		if (axolotlclient$spriteSourceQuads != quads) {
			axolotlclient$spriteQuads = SpriteQuadProvider.filter(quads);
			axolotlclient$spriteSourceQuads = quads;
		}
		return axolotlclient$spriteQuads;
	}
}
