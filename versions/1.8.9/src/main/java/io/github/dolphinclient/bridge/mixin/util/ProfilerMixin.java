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

package io.github.dolphinclient.bridge.mixin.util;

import io.github.dolphinclient.bridge.util.AxoProfiler;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Profiler.class)
public abstract class ProfilerMixin implements AxoProfiler {
	@Shadow
	public abstract void push(String string);

	@Shadow
	public abstract void pop();

	@Shadow
	public abstract void swap(String string);

	@Override
	public void br$push(String name) {
		push(name);
	}

	@Override
	public void br$pop() {
		pop();
	}

	@Override
	public void br$swap(String name) {
		swap(name);
	}
}
