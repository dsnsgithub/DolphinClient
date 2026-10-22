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

import java.util.List;
import java.util.function.BiConsumer;

import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Text.class)
public interface TextMixin extends AxoText.Mutable {
	@Shadow
	Text append(Text par1);

	@Shadow
	Text setStyle(net.minecraft.text.Style par1);

	@Shadow
	String getString();

	@Shadow
	Text copy();

	@Shadow
	String getContent();

	@Shadow
	net.minecraft.text.Style getStyle();

	@Shadow
	List<Text> getSiblings();

	@Override
	default Mutable br$append(AxoText child) {
		return append(((Text) child).copy());
	}

	@Override
	default Mutable br$setStyle(Style style) {
		return setStyle((net.minecraft.text.Style) style);
	}

	@Override
	default String br$getRawString() {
		return getString();
	}

	@Override
	default AxoText.Mutable br$copy() {
		return copy();
	}

	@Override
	default void br$visit(BiConsumer<String, AxoText.Style> handler) {
		handler.accept(getContent(), getStyle());

		for (Text c : getSiblings()) {
			c.br$visit(handler);
		}
	}

	@Override
	default AxoText.Style br$getStyle() {
		return getStyle();
	}
}
