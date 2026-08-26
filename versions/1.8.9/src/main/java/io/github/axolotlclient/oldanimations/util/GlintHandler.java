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

import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BasicBakedModel;

import java.util.HashMap;

public final class GlintHandler {

	private static final HashMap<HashedModel, BakedModel> glintMap = new HashMap<>();

	/* custom glint model */
	public static BakedModel getModel(BakedModel model) {
		/* because we're creating new bakedmodels for the sole purpose of recreating the 1.7 enchantment glint, */
		/* we should reuse the common glint bakedmodels to reduce any crazy memory usage */
		/* redth is my hero */
		return glintMap.computeIfAbsent(
			new HashedModel(model), key ->
				new BasicBakedModel.Builder(model, CustomTextureAtlasSprite.INSTANCE).build()
		);
	}
}
