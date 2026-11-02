/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
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

package io.github.dolphinclient.bridge.mixin.resource;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import io.github.dolphinclient.bridge.resource.AxoResource;
import io.github.dolphinclient.bridge.resource.AxoResourceManager;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import net.minecraft.client.resource.Resource;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceManager.class)
public interface ResourceManagerMixin extends AxoResourceManager {

	@Shadow
	Resource getResource(Identifier identifier) throws IOException;

	@Override
	default Map<AxoIdentifier, AxoResource> br$listResources(String namespace, String prefix, Predicate<AxoIdentifier> filter) {
		// this cast is maybe not ideal
		var map = net.ornithemc.osl.resource.loader.api.resource.manager.ResourceManager.client().findResources(namespace, prefix, t -> filter.test(new Identifier(t.namespace(), t.identifier())));
		HashMap<AxoIdentifier, AxoResource> other = new HashMap<>(map.size());
		map.forEach((id, resource) -> other.put(new Identifier(id.namespace(), id.identifier()), new AxoResource() {
			@Override
			public InputStream br$asStream() throws IOException {
				return resource.open();
			}

			@Override
			public BufferedReader br$asReader() throws IOException {
				return resource.openAsReader();
			}
		}));
		return other;
	}

	@Override
	default AxoResource br$getResource(AxoIdentifier loc) throws IOException {
		return getResource((Identifier) loc);
	}
}
