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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.text.Formatting;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin implements AxoText.Style {
	@Unique
	@Nullable
	private Integer dolphinclient$color;

	@Shadow
	public abstract Style copy();

	@Shadow
	protected abstract Style getParent();

	@Shadow
	@Final
	private static Style ROOT;

	@Override
	public AxoText.Style br$color(AxoText.Color color) {
		return copy().setColor(switch (color) {
			case BLACK -> Formatting.BLACK;
			case DARK_BLUE -> Formatting.DARK_BLUE;
			case DARK_GREEN -> Formatting.DARK_GREEN;
			case DARK_AQUA -> Formatting.DARK_AQUA;
			case DARK_RED -> Formatting.DARK_RED;
			case DARK_PURPLE -> Formatting.DARK_PURPLE;
			case GOLD -> Formatting.GOLD;
			case GRAY -> Formatting.GRAY;
			case DARK_GRAY -> Formatting.DARK_GRAY;
			case BLUE -> Formatting.BLUE;
			case GREEN -> Formatting.GREEN;
			case AQUA -> Formatting.AQUA;
			case RED -> Formatting.RED;
			case LIGHT_PURPLE -> Formatting.LIGHT_PURPLE;
			case YELLOW -> Formatting.YELLOW;
			case WHITE -> Formatting.WHITE;
		});
	}

	@Override
	public AxoText.Style br$color(int color) {
		final var copy = copy();
		((StyleMixin) (Object) copy).dolphinclient$color = color;
		return copy;
	}

	@Unique
	private Integer dolphinclient$getColor() {
		if (((Object) this) == ROOT) return null;
		if (dolphinclient$color == null) {
			return ((StyleMixin) (Object) getParent()).dolphinclient$getColor();
		}
		return dolphinclient$color;
	}

	@ModifyReturnValue(method = "copy", at = @At("RETURN"))
	public Style copyColor(Style original) {
		((StyleMixin) (Object) original).dolphinclient$color = dolphinclient$getColor();
		return original;
	}

	@ModifyReturnValue(method = "deepCopy", at = @At("RETURN"))
	public Style deepCopyColor(Style original) {
		((StyleMixin) (Object) original).dolphinclient$color = dolphinclient$color;
		return original;
	}

	@Override
	public AxoText.Style br$tooltip(AxoText text) {
		return copy().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Text) text));
	}

	@Inject(method = "asString", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Style;isBold()Z"))
	private void formatColorCode(CallbackInfoReturnable<String> cir, @Local StringBuilder sb) {
		Integer color = dolphinclient$getColor();

		if (color != null) {
			sb.append("§#").append(StringUtils.leftPad(Integer.toUnsignedString(color & 0xffffff, 16), 6, "0"));
		}
	}

	@WrapMethod(method = "isEmpty")
	private boolean isEmptyColor(Operation<Boolean> original) {
		return original.call() && dolphinclient$color == null;
	}
}
