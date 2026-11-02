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

package io.github.dolphinclient.bridge.entity.effect;

import io.github.dolphinclient.bridge.internal.BridgeUtil;
import io.github.dolphinclient.bridge.internal.PlatformImplInternal;
import io.github.dolphinclient.bridge.internal.RequiresImpl;
import io.github.dolphinclient.bridge.util.AxoText;

public interface AxoStatusEffectInstance {
	static AxoStatusEffectInstance create(AxoStatusEffect effect, int duration) {
		return PlatformImplInternal.createStatusEffectInstance(effect, duration);
	}

	@RequiresImpl
	default AxoStatusEffect br$getType() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default AxoText br$formatDuration() {
		throw BridgeUtil.noImpl();
	}

	@RequiresImpl
	default int br$getAmplifier() {
		throw BridgeUtil.noImpl();
	}
}
