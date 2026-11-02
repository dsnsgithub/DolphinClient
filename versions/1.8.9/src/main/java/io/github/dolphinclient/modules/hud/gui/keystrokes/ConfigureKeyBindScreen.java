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

package io.github.dolphinclient.modules.hud.gui.keystrokes;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ClickableWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.IntegerWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.SliderWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.ConfigStyles;
import io.github.dolphinclient.bridge.impl.AxoRenderContextImpl;
import io.github.dolphinclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.dolphinclient.modules.hud.gui.layout.Justification;
import io.github.dolphinclient.util.DrawUtil;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

public class ConfigureKeyBindScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final Screen parent;
	private final KeystrokeHud hud;
	public final KeystrokeHud.Keystroke stroke;
	private final IntegerOption width;
	private final IntegerOption height;
	private final boolean isAddScreen;

	public ConfigureKeyBindScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke stroke, boolean isAddScreen) {
		super(I18n.translate("keystrokes.stroke.configure_stroke"));
		this.parent = parent;
		this.hud = hud;
		this.stroke = stroke;

		width = new IntegerOption("", stroke.getBounds().width(), v -> stroke.getBounds().width(v), 7, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> stroke.getBounds().height(v), 7, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	public void init() {
		if (hud.keystrokes == null) {
			hud.setKeystrokes();
		}

		int leftColX = super.width / 2 - 4 - 150;
		int leftColY = 36 + 5;
		int rightColX = super.width / 2 + 4;
		int rightColY = 36;

		addDrawableChild(new ClickableWidget(super.width / 2 - 100, rightColY, 200, 40, "") {
			@Override
			protected void drawWidget(int mouseX, int mouseY, float partialTick) {
				var rect = stroke.getRenderPosition();
				GlStateManager.pushMatrix();
				GlStateManager.translatef(getX(), getY(), 0);
				float scale = Math.min((float) getHeight() / rect.height(), (float) getWidth() / rect.width());
				GlStateManager.translatef(getWidth() / 2f - (rect.width() * scale) / 2f, 0, 0);
				GlStateManager.scalef(scale, scale, 1);
				GlStateManager.translatef(-rect.x(), -rect.y(), 0);
				DrawUtil.fillRect(rect, Colors.WHITE.withAlpha(128));
				stroke.render(AxoRenderContextImpl.getInstance());
				GlStateManager.popMatrix();
			}
		}).active = false;
		leftColY += 48;
		rightColY += 48;

		ClickableWidget currentKey = addDrawable(textWidget(0, rightColY, super.width, 9, "", textRenderer));
		if (stroke.getKey() != null) {
			currentKey.setMessage(I18n.translate("keystrokes.stroke.key", GameOptions.getKeyName(stroke.getKey().getKeyCode()), I18n.translate(stroke.getKey().getName())));
		} else {
			currentKey.setMessage("");
		}
		leftColY += 9 + 8;
		rightColY += 9 + 8;

		if (stroke.isLabelEditable()) {
			addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.label"), textRenderer));
			leftColY += 28;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;

			var label = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, supportsSynchronization ? 30 : 150, 20, ""));

			label.setText(stroke.getLabel());
			label.setChangedListener(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				ButtonWidget synchronizeButton = addDrawableChild(new VanillaButtonWidget(rightColX + 30 + 4, rightColY, 58, 20, I18n.translate("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? I18n.translate("options.on") : I18n.translate("options.off")), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(I18n.translate("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? I18n.translate("options.on") : I18n.translate("options.off")));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setText(stroke.getLabel());
					}
				}));
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
				addDrawableChild(CyclingButtonWidget.<Justification>builder(j -> I18n.translate(j.toString())).values(Justification.values())
					.initially(s.getJustification()).build(rightColX + 30 + 4 + 58 + 4, rightColY, 58, 20,
						I18n.translate("justification"), (btn, val) -> s.setJustification(val)));
			}
			rightColY += 28;
		}
		if (stroke instanceof KeystrokeHud.CustomRenderKeystroke customRender) {
			addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.graphics"), textRenderer));
			leftColY += 28;
			var sliderMin = 2;
			var sliderMax = 25;
			var sizeSlider = new SliderWidget<>(rightColX, rightColY, 98, 20, new IntegerOption("", customRender.getSize(), customRender::setSize, sliderMin, sliderMax)) {
				@Override
				public void setMessage(String message) {
					super.setMessage(I18n.translate("keystrokes.stroke.configure_graphics_size", Integer.parseInt(message)));
				}
			};
			sizeSlider.setMessage("" + customRender.getSize());
			addDrawableChild(sizeSlider);
			var widget = (ButtonWidget) ConfigStyles.createWidget(rightColX + 98 + 4, rightColY, 48, 20, customRender.getGraphics());
			addDrawableChild(new VanillaButtonWidget(rightColX + 98 + 4, rightColY, 48, 20, I18n.translate("keystrokes.stroke.configure_graphics"), btn ->
				widget.onPress()));
			rightColY += 28;
		}
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.width"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, width));
		rightColY += 28;
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.height"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, height));

		rightColY += 28;

		addDrawableChild(new VanillaButtonWidget(super.width / 2 - 150 - 4, rightColY, 150, 20, I18n.translate("keystrokes.stroke.configure_key"), b ->
			minecraft.openScreen(new KeyBindSelectionScreen(this, stroke))));
		addDrawableChild(new VanillaButtonWidget(super.width / 2 + 4, rightColY, 150, 20, I18n.translate("keystrokes.stroke.configure_position"), b ->
			minecraft.openScreen(new KeystrokePositioningScreen(this, hud, stroke))));


		if (isAddScreen) {
			ButtonWidget addButton = addDrawableChild(new VanillaButtonWidget(super.width / 2 - 150 - 4, super.height - 33 / 2 - 10, 150, 20, I18n.translate("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				closeScreen();
			}));
			addButton.active = stroke.getKey() != null;
		}
		addDrawableChild(new VanillaButtonWidget(isAddScreen ? super.width / 2 + 4 : super.width / 2 - 75, super.height - 33 / 2 - 10, 150, 20, isAddScreen ? I18n.translate("gui.cancel") : I18n.translate("gui.back"), b -> closeScreen()));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), super.width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}


	public void closeScreen() {
		minecraft.openScreen(parent);
		hud.saveKeystrokes();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			closeScreen();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private static ClickableWidget textWidget(int x, int y, int width, int height, String message, TextRenderer textRenderer) {
		return new ClickableWidget(x, y, width, height, message) {
			@Override
			public void render(int mouseX, int mouseY, float delta) {
				drawCenteredString(textRenderer, getMessage(), getX() + getWidth() / 2, getY() + getHeight() / 2 - textRenderer.fontHeight / 2, -1);
			}
		};
	}
}

class CyclingButtonWidget<T> extends VanillaButtonWidget {
	private final String optionText;
	private int index;
	@Getter
	private T value;
	private final CyclingButtonWidget.Values<T> values;
	private final Function<T, String> valueToText;
	private final CyclingButtonWidget.UpdateCallback<T> callback;

	CyclingButtonWidget(int x, int y, int width, int height, String message, String optionText, int index, T value,
						CyclingButtonWidget.Values<T> values, Function<T, String> valueToText,
						CyclingButtonWidget.UpdateCallback<T> callback) {
		super(x, y, width, height, message, btn -> {
		});
		this.optionText = optionText;
		this.index = index;
		this.value = value;
		this.values = values;
		this.valueToText = valueToText;
		this.callback = callback;
	}

	@Override
	public void onPress() {
		if (Screen.isShiftDown()) {
			this.cycle(-1);
		} else {
			this.cycle(1);
		}
	}

	private void cycle(int amount) {
		List<T> list = this.values.getCurrent();
		this.index = MathHelper.floorMod(this.index + amount, list.size());
		T object = list.get(this.index);
		this.internalSetValue(object);
		this.callback.onValueChange(this, object);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amount) {
		if (amount > 0.0) {
			this.cycle(-1);
		} else if (amount < 0.0) {
			this.cycle(1);
		}

		return true;
	}

	public void setValue(T value) {
		List<T> list = this.values.getCurrent();
		int i = list.indexOf(value);
		if (i != -1) {
			this.index = i;
		}

		this.internalSetValue(value);
	}

	private void internalSetValue(T value) {
		String text = this.composeText(value);
		this.setMessage(text);
		this.value = value;
	}

	private String composeText(T value) {
		return this.composeGenericOptionText(value);
	}

	private String composeGenericOptionText(T value) {
		return this.optionText + ": " + this.valueToText.apply(value);
	}

	static <T> CyclingButtonWidget.Builder<T> builder(Function<T, String> valueToText) {
		return new CyclingButtonWidget.Builder<>(valueToText);
	}

	static class Builder<T> {
		private int initialIndex;
		@Nullable
		private T value;
		private final Function<T, String> valueToText;
		private CyclingButtonWidget.Values<T> values = CyclingButtonWidget.Values.of(ImmutableList.<T>of());

		public Builder(Function<T, String> valueToText) {
			this.valueToText = valueToText;
		}

		public CyclingButtonWidget.Builder<T> values(Collection<T> values) {
			return this.values(CyclingButtonWidget.Values.of(values));
		}

		@SafeVarargs
		public final CyclingButtonWidget.Builder<T> values(T... values) {
			return this.values(ImmutableList.copyOf(values));
		}

		public CyclingButtonWidget.Builder<T> values(CyclingButtonWidget.Values<T> values) {
			this.values = values;
			return this;
		}

		public CyclingButtonWidget.Builder<T> initially(T value) {
			this.value = value;
			int i = this.values.getDefaults().indexOf(value);
			if (i != -1) {
				this.initialIndex = i;
			}

			return this;
		}

		public CyclingButtonWidget<T> build(int x, int y, int width, int height, String optionText, CyclingButtonWidget.UpdateCallback<T> callback) {
			List<T> list = this.values.getDefaults();
			if (list.isEmpty()) {
				throw new IllegalStateException("No values for cycle button");
			} else {
				T object = this.value != null ? this.value : list.get(this.initialIndex);
				String text = this.valueToText.apply(object);
				String text2 = optionText + ": " + text;
				return new CyclingButtonWidget<>(
					x,
					y,
					width,
					height,
					text2,
					optionText,
					this.initialIndex,
					object,
					this.values,
					this.valueToText,
					callback
				);
			}
		}
	}

	interface UpdateCallback<T> {
		void onValueChange(CyclingButtonWidget<T> cyclingButtonWidget, T object);
	}

	interface Values<T> {
		List<T> getCurrent();

		List<T> getDefaults();

		static <T> CyclingButtonWidget.Values<T> of(Collection<T> values) {
			final List<T> list = ImmutableList.copyOf(values);
			return new CyclingButtonWidget.Values<>() {
				@Override
				public List<T> getCurrent() {
					return list;
				}

				@Override
				public List<T> getDefaults() {
					return list;
				}
			};
		}

		static <T> CyclingButtonWidget.Values<T> of(BooleanSupplier alternativeToggle, List<T> defaults, List<T> alternatives) {
			final List<T> list = ImmutableList.copyOf(defaults);
			final List<T> list2 = ImmutableList.copyOf(alternatives);
			return new CyclingButtonWidget.Values<>() {
				@Override
				public List<T> getCurrent() {
					return alternativeToggle.getAsBoolean() ? list2 : list;
				}

				@Override
				public List<T> getDefaults() {
					return list;
				}
			};
		}
	}
}
