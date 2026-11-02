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

package io.github.dolphinclient.bridge.mixin.key;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.impl.AxoKeyImpl;
import io.github.dolphinclient.bridge.key.AxoKey;
import io.github.dolphinclient.bridge.key.AxoKeybinding;
import net.minecraft.client.options.KeyBinding;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * An abstract representation of a keybind
 */
@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements AxoKeybinding {
	@Shadow
	public abstract boolean isPressed();

	@Shadow
	private int keyCode;

	@Shadow
	public abstract boolean consumeClick();

	@Shadow
	private int clickCount;
	@Unique
	private List<Runnable> dolphinclient$onClicked = null;

	@Unique
	private List<Runnable> dolphinclient$onConsumeClick = null;

	@Unique
	private List<Runnable> dolphinclient$onReleased = null;

	@Inject(method = "set", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/KeyBinding;pressed:Z", opcode = Opcodes.PUTFIELD))
	private static void dispatchHandlers(int i, boolean bl, CallbackInfo ci, @Local KeyBinding binding) {
		List<Runnable> handlers = bl ? ((KeyBindingMixin) (Object) binding).dolphinclient$onClicked : ((KeyBindingMixin) (Object) binding).dolphinclient$onReleased;
		if (handlers != null) {
			handlers.forEach(Runnable::run);
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void registerClickHandler(String string, int i, String string2, CallbackInfo ci) {
		Events.TICK.register(() -> {
			while (dolphinclient$onConsumeClick != null && consumeClick()) {
				dolphinclient$onConsumeClick.forEach(Runnable::run);
			}
		});
	}

	@Override
	public void br$registerOnClicked(Runnable runnable) {
		if (dolphinclient$onClicked == null) {
			dolphinclient$onClicked = new ArrayList<>();
		}
		dolphinclient$onClicked.add(runnable);
	}

	@Override
	public void br$registerOnReleased(Runnable runnable) {
		if (dolphinclient$onReleased == null) {
			dolphinclient$onReleased = new ArrayList<>();
		}
		dolphinclient$onReleased.add(runnable);
	}

	@Override
	public void br$registerOnConsumeClick(Runnable runnable) {
		if (dolphinclient$onConsumeClick == null) {
			dolphinclient$onConsumeClick = new ArrayList<>();
		}
		dolphinclient$onConsumeClick.add(runnable);
	}

	@Override
	public boolean br$isPressed() {
		return isPressed();
	}

	@Override
	public AxoKey br$getBoundKey() {
		return AxoKeyImpl.get(keyCode);
	}

	@Override
	public boolean br$consumeClick() {
		return consumeClick();
	}

	@Override
	public void br$click() {
		clickCount++;
	}
}
