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

package io.github.dolphinclient.config.screen;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.RecreatableScreen;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.dolphinclient.config.profiles.Profiles;
import io.github.dolphinclient.util.DrawUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import org.jetbrains.annotations.NotNull;

public class ProfilesScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen implements RecreatableScreen {

	private final Screen parent;

	public ProfilesScreen(Screen parent) {
		super(I18n.translate("profiles.configure.list"));
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		addDrawableChild(new ProfilesList(minecraft, width, height, 33, height - 33, 25));

		addDrawableChild(new VanillaButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.back"), btn -> closeScreen()));
	}


	public void closeScreen() {
		Profiles.getInstance().saveProfiles();
		minecraft.openScreen(parent);
	}

	@Override
	public @NotNull Screen recreate() {
		return new ProfilesScreen(RecreatableScreen.tryRecreate(parent));
	}

	public class ProfilesList extends ElementListWidget<ProfilesList.Entry> {
		private static final Entry SPACER = new SpacerEntry();
		private final Entry ADD = new NewEntry();

		public ProfilesList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
			reload();
		}

		public void reload() {
			clearEntries();
			Profiles.getInstance().iterateAvailable(p -> addEntry(new ProfileEntry(p)));
			addEntry(SPACER);
			addEntry(ADD);
			var presets = Profiles.getInstance().findPresets();
			if (!presets.isEmpty()) {
				addEntry(SPACER);
				presets.forEach(p -> addEntry(new PresetEntry(p)));
			}
		}

		@Override
		protected int getScrollbarPositionX() {
			return getRowLeft() + getRowWidth() + 10;
		}

		@Override
		public int getRowWidth() {
			return 340;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			children().stream().filter(e -> e instanceof ProfileEntry)
				.map(e -> (ProfileEntry) e).map(e -> e.profileName).forEach(e -> e.setFocused(false));
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Environment(EnvType.CLIENT)
		public abstract static class Entry extends ElementListWidget.Entry<Entry> {

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
		public class ProfileEntry extends Entry {
			private static final String EXPORT_BUTTON_TITLE = I18n.translate("profiles.profile.export");
			private static final String CURRENT_TEXT = I18n.translate("profiles.profile.current");
			private static final String LOAD_BUTTON_TITLE = I18n.translate("profiles.profile.load");
			private static final String DUPLICATE_BUTTON_TITLE = I18n.translate("profiles.profile.duplicate");
			private static final String REMOVE_BUTTON_TITLE = I18n.translate("profiles.profile.remove");
			private final TextFieldWidget profileName;
			private final ButtonWidget exportButton, loadButton, duplicateButton, removeButton;
			private final Profiles.Profile profile;

			ProfileEntry(Profiles.Profile profile) {
				this.profile = profile;
				profileName = new TextFieldWidget(textRenderer, 0, 0, 150, 20, "");
				profileName.setText(profile.name());
				profileName.setChangedListener(profile::setName);
				exportButton = new VanillaButtonWidget(0, 0, 50, 20, EXPORT_BUTTON_TITLE, btn -> {
					btn.active = false;
					Profiles.getInstance().exportProfile(profile).thenRun(() -> btn.active = true);
				});
				loadButton = new VanillaButtonWidget(0, 0, 50, 20, LOAD_BUTTON_TITLE, btn ->
					Profiles.getInstance().switchTo(profile));
				duplicateButton = new VanillaButtonWidget(0, 0, 50, 20, DUPLICATE_BUTTON_TITLE, b -> {
					var dup = Profiles.getInstance().duplicate(profile);
					double d = (double) ProfilesList.this.getMaxScroll() - ProfilesList.this.getScrollAmount();
					ProfilesList.this.children().add(ProfilesList.this.children().indexOf(ProfileEntry.this) + 1, new ProfileEntry(dup));
					ProfilesList.this.setScrollAmount(ProfilesList.this.getMaxScroll() - d);
				});

				this.removeButton = new VanillaButtonWidget(0, 0, 50, 20, REMOVE_BUTTON_TITLE, b -> {
					removeEntry(this);
					Profiles.getInstance().remove(profile);
					setScrollAmount(getScrollAmount());
				});
			}

			@Override
			public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				int i = getScrollbarPositionX() - removeButton.getWidth() - 4;
				int j = top - 2;
				this.removeButton.setPosition(i, j);
				this.removeButton.render(mouseX, mouseY, partialTick);

				i -= duplicateButton.getWidth();
				duplicateButton.setPosition(i, j);
				duplicateButton.render(mouseX, mouseY, partialTick);

				boolean current = Profiles.getInstance().getCurrent() == profile;
				loadButton.setMessage(current ? CURRENT_TEXT : LOAD_BUTTON_TITLE);
				loadButton.active = removeButton.active = !current;
				i -= loadButton.getWidth();
				this.loadButton.setPosition(i, j);
				this.loadButton.render(mouseX, mouseY, partialTick);
				i -= exportButton.getWidth();
				exportButton.setPosition(i, j);
				exportButton.render(mouseX, mouseY, partialTick);
				profileName.setWidth(i - left - 4);
				profileName.setPosition(left, j);
				profileName.render(mouseX, mouseY, partialTick);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(profileName, exportButton, this.loadButton, duplicateButton, removeButton);
			}
		}

		public class NewEntry extends Entry {

			private final ButtonWidget addButton, importButton;

			public NewEntry() {
				this.addButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("profiles.profile.add"), button -> {
					int i = ProfilesList.this.children().indexOf(this);
					ProfilesList.this.children().add(Math.max(i - 1, 0), new ProfileEntry(Profiles.getInstance().newProfile(I18n.translate("profiles.profile.default_new_name"))));
					Profiles.getInstance().saveProfiles();
					setScrollAmount(getMaxScroll());
				});
				this.importButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("profiles.profile.import"), btn -> {
					btn.active = false;
					Profiles.getInstance().importProfiles().thenRun(() -> {
						btn.active = true;
						ProfilesList.this.reload();
					});
				});
			}

			@Override
			public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				int i = getScrollbarPositionX() - width / 2 - 10 - addButton.getWidth() + 2;
				int j = top - 2;
				this.addButton.setPosition(i, j);
				this.addButton.render(mouseX, mouseY, partialTick);
				this.importButton.setPosition(addButton.getX() + addButton.getWidth() + 2, j);
				this.importButton.render(mouseX, mouseY, partialTick);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(addButton, importButton);
			}
		}

		public class PresetEntry extends Entry {
			private final ButtonWidget importButton;
			private final String label;

			public PresetEntry(Profiles.ProfilePreset preset) {
				this.importButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("profiles.profile.import_preset"), btn -> {
					preset.importProfile();
					ProfilesList.this.reload();
					ProfilesList.this.centerScrollOn(PresetEntry.this);
				});
				this.label = preset.info().name();
			}

			@Override
			public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				int i = getScrollbarPositionX() - importButton.getWidth() - 4;
				int j = top - 2;
				importButton.setPosition(i, j);
				importButton.render(mouseX, mouseY, partialTick);
				i -= textRenderer.getWidth(label);
				DrawUtil.drawScrollableText(textRenderer, label, left, top, i - 4, top + height, -1);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(importButton);
			}
		}
	}
}
