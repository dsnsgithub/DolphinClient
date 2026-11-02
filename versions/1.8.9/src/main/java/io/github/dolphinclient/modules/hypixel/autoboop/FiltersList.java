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

package io.github.dolphinclient.modules.hypixel.autoboop;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ClickableWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;

@Environment(EnvType.CLIENT)
public class FiltersList extends ElementListWidget<FiltersList.Entry> {
	final FilterListConfigurationScreen screen;

	public FiltersList(FilterListConfigurationScreen screen) {
		super(Minecraft.getInstance(), screen.width, screen.height, 33, screen.height - 33, 24);
		this.screen = screen;

		reload();
	}

	public void reload() {
		clearEntries();
		for (String entry : screen.filters) {
			this.addEntry(new FilterEntry(entry));
		}

		addEntry(new SpacerEntry());
		addEntry(new NewEntry());
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft() + getRowWidth() + 10;
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	public void apply() {
		screen.filters.clear();
		screen.filters.addAll(children().stream().filter(e -> e instanceof FilterEntry)
			.map(e -> (FilterEntry) e)
			.map(e -> e.editBox.getText())
			.filter(s -> !s.isBlank()).toList());
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		children().stream().filter(e -> e instanceof FilterEntry)
			.map(e -> (FilterEntry) e).map(e -> e.editBox).forEach(e -> e.setFocused(false));
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public static class SpacerEntry extends Entry {
		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}

	@Environment(EnvType.CLIENT)
	public class FilterEntry extends Entry {
		private static final String REMOVE_BUTTON_TITLE = I18n.translate("autoboop.filters.remove");
		private final TextFieldWidget editBox;
		private final ClickableWidget removeButton;

		FilterEntry(String filter) {
			this.editBox = new TextFieldWidget(client.textRenderer, 0, 0, 200, 20, I18n.translate("autoboop.filters.edit"));
			editBox.setText(filter);
			editBox.setMaxLength(16);
			this.removeButton = new VanillaButtonWidget(0, 0, 50, 20, REMOVE_BUTTON_TITLE, b -> {
				removeEntry(this);
				apply();
				setScrollAmount(getScrollAmount());
			});
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = getScrollbarPositionX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.setPosition(i, j);
			this.removeButton.render(mouseX, mouseY, partialTick);

			this.editBox.setPosition(left, j);
			this.editBox.setWidth(i - left - 4);
			this.editBox.render(mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.editBox, removeButton);
		}
	}

	public class NewEntry extends Entry {

		private final ClickableWidget addButton;

		public NewEntry() {
			this.addButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("autoboop.filters.add"), button -> {
				int i = FiltersList.this.children().indexOf(this);
				FiltersList.this.children().add(Math.max(i - 1, 0), new FilterEntry(""));
				apply();
				setScrollAmount(Math.max(0, getMaxPosition() - (bottom - top - 4)));
			});
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = getScrollbarPositionX() - width / 2 - 10 - addButton.getWidth() / 2;
			int j = top - 2;
			this.addButton.setPosition(i, j);
			this.addButton.render(mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(addButton);
		}
	}
}
