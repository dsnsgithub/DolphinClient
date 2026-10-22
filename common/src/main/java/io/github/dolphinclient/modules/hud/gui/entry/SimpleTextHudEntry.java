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

package io.github.dolphinclient.modules.hud.gui.entry;

import java.util.List;
import java.util.Locale;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoI18n;
import io.github.dolphinclient.modules.hud.gui.layout.Justification;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import lombok.RequiredArgsConstructor;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public abstract class SimpleTextHudEntry extends TextHudEntry {

	protected final EnumOption<Justification> justification = new EnumOption<>("justification", Justification.class,
		Justification.CENTER);
	protected final BooleanOption showBrackets = new BooleanOption("show_brackets", false);
	private final EnumOption<ValueDescriptionRelation> order = new EnumOption<>("simple_text_hud.order", ValueDescriptionRelation.class, ValueDescriptionRelation.VALUE_DESCRIPTION);
	protected final StringOption separator = new StringOption("simple_text_hud.separator", " ");
	private final IntegerOption minWidth;
	private final IntegerOption minHeight;
	private final boolean hasOrder;

	public SimpleTextHudEntry() {
		this(53);
	}

	protected SimpleTextHudEntry(boolean hasOrder) {
		this(53, 13, true, hasOrder);
	}

	protected SimpleTextHudEntry(int width, int height, boolean backgroundAllowed, boolean hasOrder) {
		super(width, height, backgroundAllowed);
		minWidth = new IntegerOption("minwidth", width, 1, 300);
		minHeight = new IntegerOption("hud.height", height, 1, 150);
		this.hasOrder = hasOrder;
	}

	protected SimpleTextHudEntry(int width) {
		this(width, 13, true, false);
	}

	@Override
	public void renderComponent(AxoRenderContext render, float delta) {
		DrawPosition pos = getContentPos();
		String value = applyOptions(getValue(), getLabel());

		int valueWidth = render.br$getFont().br$getWidth(value);
		updateBounds(valueWidth);
		render.br$drawString(value,
			pos.x() + justification.get().getXOffset(valueWidth, getContentWidth() - 4) + 2,
			pos.y() + (Math.round((float) getContentHeight() / 2)) - 4, getTextColor().toInt(), shadow.get());
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext ctx, float delta) {
		DrawPosition pos = getContentPos();
		String value = applyOptions(getPlaceholderValue(), getPlaceholderLabel());
		int valueWidth = ctx.br$getFont().br$getWidth(value);
		updateBounds(valueWidth);
		ctx.br$drawString(value, pos.x() + justification.get().getXOffset(valueWidth, getContentWidth() - 4) + 2,
			pos.y() + (Math.round((float) getContentHeight() / 2)) - 4, getTextColor().toInt(), shadow.get());
	}

	private void updateBounds(int valueWidth) {
		int elementWidth = valueWidth + 4;
		int elementHeight = client.br$getFont().br$getFontHeight() + 4;
		boolean boundsChanged = false;
		int minW = minWidth.get();
		if (elementWidth < minW) {
			if (getContentWidth() != minW) {
				setContentWidth(minW);
				boundsChanged = true;
			}
		} else if (elementWidth != getContentWidth()) {
			setContentWidth(elementWidth);
			boundsChanged = true;
		}
		int minH = minHeight.get();
		if (elementHeight < minH) {
			if (getContentHeight() != minH) {
				setContentHeight(minH);
				boundsChanged = true;
			}
		} else if (elementHeight != getContentHeight()) {
			setContentHeight(elementHeight);
			boundsChanged = true;
		}

		if (boundsChanged) {
			onBoundsUpdate();
		}
	}

	protected String applyOptions(String value, String desc) {
		String s;
		if (hasOrder) s = order.get().func.apply(value, desc, separator.get());
		else s = value;
		if (showBrackets.get()) return AxoI18n.translate("bracket_format", s);
		return s;
	}

	public abstract String getPlaceholderValue();

	public String getPlaceholderLabel() {
		return getLabel();
	}

	public abstract String getValue();

	public String getLabel() {
		return "";
	}

	public Color getTextColor() {
		return textColor.get();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(justification);
		options.add(minWidth);
		options.add(minHeight);
		options.add(showBrackets);
		if (hasOrder) {
			options.add(order);
			options.add(separator);
		}
		return options;
	}

	@RequiredArgsConstructor
	private enum ValueDescriptionRelation {
		VALUE_DESCRIPTION((s, s2, sep) -> s + sep + s2),
		DESCRIPTION_VALUE((s, s2, sep) -> s2 + sep + s),
		VALUE_ONLY((s, s2, sep) -> s);

		private final Relation func;

		@Override
		public String toString() {
			return "simple_text_hud.order." + super.toString().toLowerCase(Locale.ROOT);
		}

		@FunctionalInterface
		private interface Relation {
			String apply(String value, String description, String separator);
		}
	}
}
