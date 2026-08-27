/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.TextRenderUtils;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderUtils.class)
public abstract class TextRenderUtilsMixin {

	@Unique
	private static final Map<Character, Formatting> formattingCodes;

	static {
		var map = new Object2ObjectOpenHashMap<Character, Formatting>();
		map.put('0', Formatting.BLACK);
		map.put('1', Formatting.DARK_BLUE);
		map.put('2', Formatting.DARK_GREEN);
		map.put('3', Formatting.DARK_AQUA);
		map.put('4', Formatting.DARK_RED);
		map.put('5', Formatting.DARK_PURPLE);
		map.put('6', Formatting.GOLD);
		map.put('7', Formatting.GRAY);
		map.put('8', Formatting.DARK_GRAY);
		map.put('9', Formatting.BLUE);
		map.put('a', Formatting.GREEN);
		map.put('b', Formatting.AQUA);
		map.put('c', Formatting.RED);
		map.put('d', Formatting.LIGHT_PURPLE);
		map.put('e', Formatting.YELLOW);
		map.put('f', Formatting.WHITE);
		map.put('k', Formatting.OBFUSCATED);
		map.put('l', Formatting.BOLD);
		map.put('m', Formatting.STRIKETHROUGH);
		map.put('n', Formatting.UNDERLINE);
		map.put('o', Formatting.ITALIC);
		map.put('r', Formatting.RESET);
		formattingCodes = map;
	}


	@Inject(method = "wrapText", at = @At("HEAD"))
	private static void reformatText(Text pText, int i, TextRenderer textRenderer, boolean bl, boolean bl2, CallbackInfoReturnable<List<Text>> cir, @Local(argsOnly = true) LocalRef<Text> text) {
		text.set(format(text.get()));
	}

	@Unique
	private static Formatting byCodeOfFirstChar(String code) {
		char c = code.toLowerCase(Locale.ROOT).charAt(0);

		for (Map.Entry<Character, Formatting> formatting : formattingCodes.entrySet()) {
			if (formatting.getKey() == c) {
				return formatting.getValue();
			}
		}

		return null;
	}

	@Unique
	private static final Pattern CODE_PATTERN = Pattern.compile("§");

	@Unique
	private static Text format(Text text) {
		Text n = null;
		for (var t : text) {
			if (!t.getContent().contains("§")) {
				var r = new LiteralText(t.getContent());
				r.setStyle(t.getStyle());
				if (n == null) {
					n = r;
				} else {
					n.append(r);
					t.getStyle().setParent(n.getStyle());
				}
			} else {
				var formatted = formatFromCodes(t.getContent());
				formatted.setStyle(t.getStyle());
				if (n == null) {
					n = formatted;
				} else {
					n.append(formatted);
					formatted.getStyle().setParent(n.getStyle());
				}
			}
		}
		return n;
	}

	@Unique
	private static Text formatFromCodes(String formattedString) {
		Text text = new LiteralText("");
		String[] arr = CODE_PATTERN.split(formattedString);

		List<Formatting> modifiers = new ArrayList<>();
		Formatting color = null;
		Integer br$color = null;
		for (int i = 0, length = arr.length; i < length; i++) {
			String s = arr[i];
			if (s.isEmpty()) {
				continue;
			} else if (i == 0) {
				text.append(s);
				continue;
			}
			Formatting formatting = byCodeOfFirstChar(s);
			Text part;
			int pL = s.length();
			int formatLength = 1;
			if (formatting == null) {
				if (s.toLowerCase(Locale.ROOT).charAt(0) == '#') {
					br$color = Color.parse(s.substring(0, 7)).toInt();
					formatLength = 7;
				} else {
					text.append(s);
					continue;
				}
			} else {
				if (formatting.equals(Formatting.RESET)) {
					modifiers.clear();
					color = null;
				} else if (formatting.isModifier()) {
					modifiers.add(formatting);
				} else {
					color = formatting;
				}
			}
			if (pL == 1) {
				continue;
			}
			part = new LiteralText(s.substring(formatLength));

			if (color != null) {
				part.setStyle(part.getStyle().setColor(color));
			}
			if (br$color != null) {
				part.br$setStyle(part.getStyle().br$color(br$color));
			}

			if (!modifiers.isEmpty()) {
				for (Formatting mod : modifiers) {
					switch (mod) {
						case OBFUSCATED -> part.getStyle().setObfuscated(true);
						case BOLD -> part.getStyle().setBold(true);
						case ITALIC -> part.getStyle().setItalic(true);
						case UNDERLINE -> part.getStyle().setUnderlined(true);
						case STRIKETHROUGH -> part.getStyle().setStrikethrough(true);
						default -> DolphinClientCommon.getInstance().getLogger().warn("Unexpected modifier: " + mod);
					}
				}
			}
			text.append(part);
		}
		return text;
	}
}
