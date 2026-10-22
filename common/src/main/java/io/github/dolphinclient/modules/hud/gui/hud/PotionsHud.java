/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.modules.hud.gui.hud;

import java.util.List;
import java.util.Locale;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffectInstance;
import io.github.dolphinclient.bridge.entity.effect.AxoStatusEffects;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.bridge.util.AxoText;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.CardinalOrder;
import io.github.dolphinclient.modules.hud.util.DefaultOptions;
import io.github.dolphinclient.modules.hud.util.Rectangle;
import io.github.dolphinclient.util.CommonUtil;
import lombok.AllArgsConstructor;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public class PotionsHud extends TextHudEntry {
	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "potionshud");

	private final EnumOption<CardinalOrder> order = DefaultOptions.getCardinalOrder(CardinalOrder.TOP_DOWN);

	private final EnumOption<Mode> mode = new EnumOption<>("mode", Mode.class, Mode.ICONS_AND_TEXT);
	private final BooleanOption showEffectName = new BooleanOption("showEffectNames", true);
	private final ColorOption timerTextColor = new ColorOption("potionshud.timer_text_color", Color.parse("#7F7F7F"));

	private static AxoText formatNameAndAmplifier(AxoStatusEffectInstance effect) {
		return effect.br$getType().br$getDisplayName().br$copy()
			.br$append(" ")
			.br$append(CommonUtil.toRoman(effect.br$getAmplifier() + 1));
	}

	public PotionsHud() {
		super(50, 200, true);
		background = new BooleanOption("background", false);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		assert client.br$getPlayer() != null;
		renderEffects(graphics, client.br$getPlayer().br$getStatusEffects());
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		final var player = client.br$getPlayer();
		if (player == null) {
			return;
		}
		var effects = player.br$getStatusEffects();
		boolean noEffects = effects.isEmpty();
		int calcWidth = noEffects ? 0 : calculateWidth(effects);
		int calcHeight = noEffects ? 0 : calculateHeight(effects);
		boolean changed = false;
		if (calcWidth != getContentWidth()) {
			setContentWidth(calcWidth);
			changed = true;
		}
		if (calcHeight != getContentHeight()) {
			setContentHeight(calcHeight);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
		if (noEffects) {
			return;
		}
		super.render(ctx, delta);
	}

	private void renderEffects(AxoRenderContext graphics, List<AxoStatusEffectInstance> effects) {
		boolean noEffects = effects.isEmpty();
		int calcWidth = noEffects ? 0 : calculateWidth(effects);
		int calcHeight = noEffects ? 0 : calculateHeight(effects);
		boolean changed = false;
		if (calcWidth != getContentWidth()) {
			setContentWidth(calcWidth);
			changed = true;
		}
		if (calcHeight != getContentHeight()) {
			setContentHeight(calcHeight);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
		if (noEffects) {
			return;
		}
		int lastPos = 0;
		CardinalOrder direction = order.get();

		Rectangle bounds = getContentBounds();
		int x = bounds.x();
		int y = bounds.y();
		for (int i = 0; i < effects.size(); i++) {
			final var effect = effects.get(direction.getDirection() == -1 ? i : effects.size() - i - 1);
			if (direction.isXAxis()) {
				renderPotion(graphics, effect, x + lastPos + 2, y + 2);
				int nameWidth = 0;
				if (mode.get().hasText) {
					nameWidth += graphics.br$getFont().br$getWidth(effect.br$formatDuration()) + 2;
					if (showEffectName.get()) {
						nameWidth = Math.max(nameWidth, client.br$getFont().br$getWidth(formatNameAndAmplifier(effect)) + 2);
					}
				}
				if (mode.get().hasIcons) {
					nameWidth += 20;
				}
				lastPos += nameWidth;
			} else {
				renderPotion(graphics, effect, x + 2, y + 2 + lastPos);
				lastPos += 20;
			}
		}
	}

	private int calculateWidth(List<AxoStatusEffectInstance> effects) {
		final var widthStreamFullName = effects.stream()
			.map(PotionsHud::formatNameAndAmplifier)
			.mapToInt(client.br$getFont()::br$getWidth);
		final var widthStreamDuration = effects.stream()
			.map(AxoStatusEffectInstance::br$formatDuration)
			.mapToInt(client.br$getFont()::br$getWidth);

		int width = 2;
		if (order.get().isXAxis()) {
			if (mode.get().hasIcons) {
				width += 20 * effects.size();
			}

			if (mode.get().hasText) {
				if (!showEffectName.get()) {
					width += widthStreamDuration.map(i -> i + 2).sum();
				} else {
					width += widthStreamFullName.map(i -> i + 2).sum();
				}
			}
		} else {
			if (mode.get().hasIcons) {
				width += 20;
			}

			if (mode.get().hasText) {
				if (!showEffectName.get()) {
					width += widthStreamDuration.max().orElse(0) + 2;
				} else {
					width += widthStreamFullName.max().orElse(0) + 2;
				}
			}
		}
		return width;
	}

	private int calculateHeight(List<AxoStatusEffectInstance> effects) {
		if ((order.get()).isXAxis()) {
			return 24;
		} else {
			return 20 * effects.size() + 2;
		}
	}

	private void renderPotion(AxoRenderContext graphics, AxoStatusEffectInstance effect, int x, int y) {
		final var type = effect.br$getType();

		var mode = this.mode.get();
		if (mode.hasIcons) {
			graphics.br$drawTexture(type.br$getSprite(), x, y, 18, 18);
			x += 19;
		}

		if (mode.hasText) {
			if (showEffectName.get()) {
				graphics.br$drawString(formatNameAndAmplifier(effect), x, y + 1, textColor.get().toInt(), shadow.get());
				graphics.br$drawString(effect.br$formatDuration(), x, y + 1 + 10,
					timerTextColor.get().toInt(), shadow.get());
			} else {
				graphics.br$drawString(effect.br$formatDuration(), x, y + 5,
					timerTextColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		AxoStatusEffectInstance effect = AxoStatusEffectInstance.create(AxoStatusEffects.SPEED, 9999);
		AxoStatusEffectInstance jump = AxoStatusEffectInstance.create(AxoStatusEffects.JUMP_BOOST, 99999);
		AxoStatusEffectInstance haste = AxoStatusEffectInstance.create(AxoStatusEffects.HASTE, -1);
		renderEffects(graphics, List.of(effect, jump, haste));
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(order);
		options.add(mode);
		options.add(showEffectName);
		options.add(timerTextColor);
		return options;
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@AllArgsConstructor
	private enum Mode {
		ICONS_ONLY(true, false),
		TEXT_ONLY(false, true),
		ICONS_AND_TEXT(true, true);
		private final boolean hasIcons, hasText;

		@Override
		public String toString() {
			return "potionshud.mode." + super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
