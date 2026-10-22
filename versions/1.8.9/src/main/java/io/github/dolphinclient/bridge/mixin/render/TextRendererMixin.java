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

package io.github.dolphinclient.bridge.mixin.render;

import io.github.dolphinclient.bridge.render.AxoFont;
import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin implements AxoFont {
	@Shadow
	public abstract int draw(String string, float f, float g, int i, boolean bl);

	@Shadow
	public int fontHeight;

	@Shadow
	public abstract int getWidth(String string);

	@Override
	public int br$getWidth(AxoText text) {
		return this.br$getWidth(((Text) text).getString());
	}

	@Override
	public int br$getWidth(String text) {
		return this.getWidth(text);
	}

	@Override
	public int br$getFontHeight() {
		return fontHeight;
	}
}
