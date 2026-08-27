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

package io.github.axolotlclient.api.e4mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.api.e4mc.DolphinClientE4mcPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "link/e4mc/QuiclimeSession$2$1", remap = false)
public class QuiclimeSessionInnerMixin {

	@ModifyArg(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Llink/e4mc/QuiclimeSession$ControlMessageCodec$ControlMessage;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"), index = 1)
	private Object axolotlclient$hookE4mcDomain(Object o, @Local(name = "domain") String domain) {
		DolphinClientE4mcPlugin.INSTANCE.setE4mcDomain(domain);
		return o;
	}
}
