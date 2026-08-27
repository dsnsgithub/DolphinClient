/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.config.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.PlainTextButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.bridge.util.AxoText;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.resource.Identifier;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class CreditsScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final SoundInstance bgm = SimpleSoundInstance.of(new Identifier("minecraft", "records.chirp"));

	public CreditsScreen(Screen parent) {
		super(I18n.translate("credits"));
		this.parent = parent;
	}

	@Override
	public void renderBackground() {
		if (DolphinClient.config().someNiceBackground.get()) { // Credit to pridelib for the colors
			fill(0, 0, width, height / 6, 0xFFff0018);
			fill(0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			fill(0, height * 2 / 6, width, height / 2, 0xFFffff41);
			fill(0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			fill(0, height / 2, width, height * 2 / 3, 0xFF008018);
			fill(0, height * 5 / 6, width, height, 0xFF86007d);
		} else {
			super.renderBackground();
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		addDrawableChild(new CreditsList(minecraft, width, height, height - 33 - 33, 33, 25));

		var back = addDrawableChild(new VanillaButtonWidget(width / 2 - 75, height - 33 / 2 - 20 / 2, 150, 20,
			I18n.translate("gui.back"), buttonWidget -> closeScreen()));

		addDrawableChild(new VanillaButtonWidget(6, back.getY(), 100, 20, I18n.translate("creditsBGM") + ": "
			+ I18n.translate(DolphinClient.config().creditsBGM.get() ? "options.on" : "options.off"),
			buttonWidget -> {
				DolphinClient.config().creditsBGM.toggle();
				DolphinClient.getInstance().saveConfig();
				stopBGM();
				buttonWidget.setMessage(I18n.translate("creditsBGM") + ": " +
					I18n.translate(DolphinClient.config().creditsBGM.get() ? "options.on" : "options.off"));
			}));
	}

	public void closeScreen() {
		minecraft.openScreen(parent);
		stopBGM();
	}

	@Override
	public void tick() {
		tickBGM();
	}

	public void tickBGM() {
		if (DolphinClient.config().creditsBGM.get() && !minecraft.getSoundManager().isPlaying(bgm)) {
			minecraft.getSoundManager().play(bgm);
		}
	}

	private void stopBGM() {
		minecraft.getSoundManager().stop(bgm);
	}

	private class CreditsList extends ElementListWidget<Entry> {

		public CreditsList(Minecraft minecraftClient, int width, int screenHeight, int height, int top,
						   int entryHeight) {
			super(minecraftClient, width, height, top, top + height, entryHeight);

			addEntry(new SpacerTitle("- - - - - - " + I18n.translate("contributors") + " - - - - - -"));
			Credits.getContributors().forEach(credit -> addEntry(new Credit(credit.getName(), credit.getThings())));

			addEntry(new SpacerTitle("- - - - - - " + I18n.translate("other_people") + " - - - - - -"));
			Credits.getOtherPeople().forEach(credit -> addEntry(new Credit(credit.getName(), credit.getThings())));

			if (!externalModuleCredits.isEmpty()) {
				addEntry(new SpacerTitle("- - - - - - " + I18n.translate("external_modules") + " - - - - - -"));
				externalModuleCredits.forEach((s, s2) -> addEntry(new Credit(s, s2)));
			}
		}
	}

	private abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	private class Credit extends Entry {

		private final String name;
		private final String[] things;
		private final VanillaButtonWidget c;

		public Credit(String name, String... things) {
			this.name = name;
			this.things = things;
			c = new VanillaButtonWidget(0, 0, 200, 20, name, buttonWidget -> minecraft.openScreen(new CreditOverlay(this))) {
				@Override
				protected void drawWidget(int mouseX, int mouseY, float delta) {
					if (isHovered()) {
						RenderUtil.drawOutline(getX(), getY(), getWidth(), getHeight(), ClientColors.ERROR.toInt());
					}
					var i = this.active ? (isHovered() ? ClientColors.SELECTOR_RED : ClientColors.WHITE) : ClientColors.GRAY;
					this.drawScrollingText(textRenderer, 2, i);
				}
			};
		}

		@Override
		public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			c.setPosition(x, y);
			c.render(mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(c);
		}
	}

	private class CreditOverlay extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
		private final Credit credit;
		private final List<Runnable> lines = new ArrayList<>();

		public CreditOverlay(Credit credit) {
			super(I18n.translate(credit.name));
			this.credit = credit;
		}


		public void closeScreen() {
			minecraft.openScreen(CreditsScreen.this);
		}

		@Override
		public void init() {
			int startY = 100;
			for (String t : credit.things) {
				int textWidth = textRenderer.br$getWidth(t);
				if (t.startsWith("http")) {
					addDrawableChild(new PlainTextButtonWidget(width / 2 - textWidth / 2, startY, textWidth, 12,
						((Text) AxoText.literal(t).br$color(ClientColors.SELECTOR_GREEN)).getFormattedString(), btn ->
						handleClickEvent(new LiteralText("").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, t)))), textRenderer));
				} else {
					int y = startY;
					lines.add(() -> textRenderer.draw(t, width / 2 - textWidth / 2, y,
						ClientColors.SELECTOR_GREEN.toInt()));
				}
				startY += 12;
			}
			addDrawableChild(new VanillaButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.back"), buttonWidget -> closeScreen()));
		}

		@Override
		public void renderBackground() {
			super.renderBackground();
			RenderUtil.drawRectangle(100, 50, width - 200, height - 100,
				ClientColors.DARK_GRAY.withAlpha(127));
			DrawUtil.outlineRect(100, 50, width - 200, height - 100,
				ClientColors.BLACK.toInt());
		}

		@Override
		public void render(int mouseX, int mouseY, float delta) {
			super.render(mouseX, mouseY, delta);
			drawCenteredString(textRenderer, credit.name,
				width / 2, 57, -16784327);
			lines.forEach(Runnable::run);
		}

		@Override
		public void tick() {
			CreditsScreen.this.tickBGM();
		}
	}

	private class SpacerTitle extends Entry {

		private final String name;

		public SpacerTitle(String name) {
			this.name = name;
		}

		@Override
		public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			CreditsScreen.this.drawCenteredString(textRenderer, name, x + entryWidth / 2, y, -128374);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}
}
