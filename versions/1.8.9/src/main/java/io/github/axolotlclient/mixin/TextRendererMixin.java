/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

import java.util.Locale;
import java.util.regex.Pattern;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

	// Pain at its finest

	@Unique
	private final Identifier texture_g = new Identifier(DolphinClientCommon.MODID, "textures/font/g_breve_capital.png");
	@Shadow
	public int fontHeight;
	@Shadow
	private float r;
	@Shadow
	private float g;
	@Shadow
	private float b;
	@Shadow
	private float a;
	@Shadow
	private float x;
	@Shadow
	private float y;
	@Shadow
	private int color;
	@Unique
	private boolean shouldHaveShadow;

	@Inject(method = "drawLayer(Ljava/lang/String;FFIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawLayer(Ljava/lang/String;Z)V"))
	public void axolotlclient$getData(String text, float x, float y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
		if (text != null) {
			shouldHaveShadow = shadow;
		}
	}

	@Inject(method = "drawGlyph", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$gBreve(char c, boolean bl, CallbackInfoReturnable<Float> cir) {
		if (c == 'Ğ' && !Minecraft.getInstance().options.forceUnicodeFont) {
			Minecraft.getInstance().getTextureManager().bind(texture_g);

			if (!bl || shouldHaveShadow) {
				GlStateManager.color4f(this.r / 4, this.g / 4, this.b / 4, this.a);
				drawTexture(this.x + 1, this.y - this.fontHeight + 7);
			}

			GlStateManager.color4f(this.r, this.g, this.b, this.a);
			drawTexture(this.x, this.y - this.fontHeight + 6);

			GlStateManager.color4f(this.r, this.g, this.b, this.a);
			cir.setReturnValue(7.0F);
		}
	}

	@Unique
	private void drawTexture(float x, float y) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + 10, 0.0).texture(0, 1).nextVertex();
		bufferBuilder.vertex((x + 5), (y + 10), 0.0).texture(1, 1).nextVertex();
		bufferBuilder.vertex((x + 5), y, 0.0).texture(1, 0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0, 0).nextVertex();
		tesselator.end();
	}

	@Inject(method = "getWidth(C)I", at = @At(value = "HEAD"), cancellable = true)
	public void axolotlclient$modifiedCharWidth(char c, CallbackInfoReturnable<Integer> cir) {
		if (c == 'Ğ' && !Minecraft.getInstance().options.forceUnicodeFont) {
			cir.setReturnValue(7);
		}
	}

	@Unique
	private static final Pattern COLOR_PATTERN = Pattern.compile("(#(?:0x)?[a-fA-F0-9]{6})");

	// This target does not exist when using OptiFine
	@WrapOperation(method = "drawLayer(Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;color4f(FFFF)V", ordinal = 0), require = 0)
	private void customFormattingCode(float red, float green, float blue, float alpha, Operation<Void> original, String string, boolean bl, @Local(ordinal = 0) LocalIntRef index) {
		if (index.get() + 7 < string.length() && string.charAt(index.get() + 1) == '#') {

			String color = string.substring(index.get() + 1).toLowerCase(Locale.ROOT);
			var matcher = COLOR_PATTERN.matcher(color).region(0, Math.min(10, color.length()));
			if (!matcher.find()) {
				return;
			}
			Color parsed = Color.parse(matcher.group(1));
			int c = parsed.toInt();
			if (bl) {
				c = (c & 0xFCFCFC) >> 2 | c & 0xFF000000;
			}
			red = (float) (c >> 16 & 0xFF) / 255.0F;
			green = (float) (c >> 8 & 0xFF) / 255.0F;
			blue = (float) (c & 0xFF) / 255.0F;
			this.color = c;
			index.set(index.get() + matcher.end(1) - 1);
		}
		original.call(red, green, blue, alpha);
	}

	// This target only exists with OptiFine
	@WrapOperation(method = "drawLayer(Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;setColor(FFFF)V"), require = 0)
	private void customFormattingCode$OF(TextRenderer renderer, float red, float green, float blue, float alpha, Operation<Void> original, String string, boolean bl, @Local(ordinal = 0) LocalIntRef index) {
		if (index.get() + 7 < string.length() && string.charAt(index.get() + 1) == '#') {

			String color = string.substring(index.get() + 1).toLowerCase(Locale.ROOT);
			var matcher = COLOR_PATTERN.matcher(color).region(0, Math.min(10, color.length()));
			if (!matcher.find()) {
				return;
			}
			Color parsed = Color.parse(matcher.group(1));
			int c = parsed.toInt();
			if (bl) {
				c = (c & 0xFCFCFC) >> 2 | c & 0xFF000000;
			}
			red = (float) (c >> 16 & 0xFF) / 255.0F;
			green = (float) (c >> 8 & 0xFF) / 255.0F;
			blue = (float) (c & 0xFF) / 255.0F;
			this.color = c;
			index.set(index.get() + matcher.end(1) - 1);
		}
		original.call(renderer, red, green, blue, alpha);
	}

	// Again, the first target is for vanilla, the second for OptiFine.
	@WrapOperation(method = "getWidth(Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;getWidth(C)I"), require = 0)
	private int customFormattingCodeWidth(TextRenderer instance, char c, Operation<Integer> original, String string, @Local(ordinal = 1) LocalIntRef index) {
		if (index.get() + 7 < string.length() && string.charAt(index.get() + 1) == '#') {

			String color = string.substring(index.get() + 1).toLowerCase(Locale.ROOT);
			var matcher = COLOR_PATTERN.matcher(color).region(0, Math.min(10, color.length()));
			if (!matcher.find()) {
				return original.call(instance, c);
			}
			index.set(index.get() + matcher.end(1));
			return 0;
		}
		return original.call(instance, c);
	}

	@WrapOperation(method = "getWidth(Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;getCharWidthFloat(C)F"), require = 0)
	private float customFormattingCodeWidth$OF(TextRenderer instance, char c, Operation<Float> original, String string, @Local(ordinal = 0) LocalIntRef index) {
		if (index.get() + 7 < string.length() && string.charAt(index.get() + 1) == '#') {

			String color = string.substring(index.get() + 1).toLowerCase(Locale.ROOT);
			var matcher = COLOR_PATTERN.matcher(color).region(0, Math.min(10, color.length()));
			if (!matcher.find()) {
				return original.call(instance, c);
			}
			index.set(index.get() + matcher.end(1));
			return 0;
		}
		return original.call(instance, c);
	}
}
