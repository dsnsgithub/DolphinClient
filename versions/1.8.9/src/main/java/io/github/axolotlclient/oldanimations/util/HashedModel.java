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
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* this should ensure that our glint model is reused to ensure good performance */
public final class HashedModel {
	private final int[] data;

	public HashedModel(int[] data) {
		this.data = Objects.requireNonNull(data, "data cannot be null");
	}

	public HashedModel(BakedModel model) {
		List<BakedQuad> allQuads = new ArrayList<>();

		for (Direction face : Direction.values()) {
			List<BakedQuad> faceQuads = model.getQuads(face);
			if (faceQuads != null) {
				allQuads.addAll(faceQuads);
			}
		}

		List<BakedQuad> generalQuads = model.getQuads();
		if (generalQuads != null) {
			allQuads.addAll(generalQuads);
		}

		int[] buffer = new int[allQuads.size() * 3];
		int idx = 0;
		for (BakedQuad quad : allQuads) {
			int[] vertices = quad.getVertices();
			int limit = Math.min(3, vertices.length);
			for (int v = 0; v < limit; v++) {
				buffer[idx++] = vertices[v];
			}
		}

		/* trim if any quad had fewer than 3 vertex components */
		this.data = (idx == buffer.length) ? buffer : Arrays.copyOf(buffer, idx);
	}

	public HashedModel copy(int[] newData) {
		return new HashedModel(newData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof HashedModel)) {
			return false;
		} else {
			return Arrays.equals(data, ((HashedModel) o).data);
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public String toString() {
		return "HashedModel(data=" + Arrays.toString(data) + ")";
	}
}
