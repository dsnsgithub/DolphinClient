/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.axolotlclient.DolphinClientCommon;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientBrandRetriever.class)
public abstract class ClientBrandRetrieverMixin {
	@WrapMethod(method = "getClientModName")
	private static String axolotlclient$returnClientBrand(Operation<String> original) {
		if (DolphinClientCommon.getInstance().getConfig().modifyClientBrand.get()) {
			if ("vanilla".equals(original.call()) || "fabric".equals(original.call())) {
				return "DolphinClient";
			}
			return original.call().replace("vanilla", "DolphinClient");
		}
		return original.call();
	}
}
