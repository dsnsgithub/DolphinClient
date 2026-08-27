/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;


public class SessionStatsHudEntryConfigScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private final Screen parent;
	private final SessionStatisticsOverlay hud = BedwarsMod.getInstance().getSessionStatsOverlay();

	public SessionStatsHudEntryConfigScreen(Screen parent) {
		super(tr("title"));
		this.parent = parent;
	}

	private static String tr(String key) {
		return I18n.translate("bedwars.session_stats.configure." + key);
	}

	@Override
	public void closeScreen() {
		minecraft.openScreen(parent);
		DolphinClientCommon.getInstance().saveConfig();
	}

	@Override
	public void init() {
		addDrawableChild(new EntryList(minecraft, width, height, 33, height - 33, 24));
		addDrawable((mX, mY, partialTick) ->
			textRenderer.drawWithShadow(getTitle(), width / 2f - textRenderer.getWidth(getTitle()) / 2f, 33 / 2f - textRenderer.fontHeight / 2f, -1));
		addDrawableChild(new VanillaButtonWidget(width / 2 - 150 / 2, height - 33 / 2 - 20 / 2, 150, 20, I18n.translate("gui.back"), btn -> closeScreen()));
	}

	private class EntryList extends ElementListWidget<ListEntry> {

		private final int rowWidth;

		public EntryList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
			rowWidth = hud.getEntries().stream().mapToInt(e -> AxoRenderContextImpl.getInstance().br$getFont().br$getWidth(e.name())).max().orElse(100) + 180;
			hud.getEntries().forEach(e -> addEntry(new ListEntry(e, this)));
		}

		void moveEntry(ListEntry entry, int direction) {
			var index = children().indexOf(entry);
			var next = index + direction;
			var n = children().get(next);
			children().set(index, n);
			children().set(next, entry);
			var v = hud.getEntries().get(next);
			hud.getEntries().set(index, v);
			hud.getEntries().set(next, entry.entry);
		}

		@Override
		public int getRowWidth() {
			return rowWidth;
		}

		@Override
		protected int getScrollbarPositionX() {
			return this.getRowRight() + 6 + 2;
		}
	}

	private class ListEntry extends ElementListWidget.Entry<ListEntry> {
		private final VanillaButtonWidget showHide;
		private final SessionStatisticsOverlay.SessionStatsEntry entry;
		private final EntryList list;
		private final VanillaButtonWidget up, down;

		private ListEntry(SessionStatisticsOverlay.SessionStatsEntry entry, EntryList list) {
			this.entry = entry;
			this.list = list;
			showHide = new VanillaButtonWidget(0, 0, 50, 20,
				entry.enabled().get() ? tr("entry.hide") : tr("entry.show"), btn -> {
				entry.enabled().toggle();
				btn.setMessage(entry.enabled().get() ? tr("entry.hide") : tr("entry.show"));
			});
			up = new VanillaButtonWidget(0, 0, 50, 20, tr("entry.move_up"), btn ->
				minecraft.executeTask(() -> list.moveEntry(this, -1)));
			down = new VanillaButtonWidget(0, 0, 50, 20, tr("entry.move_down"), btn ->
				minecraft.executeTask(() -> list.moveEntry(this, 1)));
		}

		@Override
		public void render(int index, int entryY, int entryX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {

			var x = entryX + 2;
			var y = entryY + 2;
			var right = entryX + entryWidth - 2;
			down.setPosition(right - down.getWidth(), y);
			up.setPosition(down.getX() - up.getWidth(), y);
			showHide.setPosition(up.getX() - showHide.getWidth(), y);

			var size = list.children().size();

			down.active = index != size - 1;
			up.active = index != 0;

			AxoRenderContextImpl.getInstance().br$drawString(entry.name(), x, y + (entryHeight - 4) / 2 - textRenderer.fontHeight / 2, -1);
			down.render(mouseX, mouseY, partialTick);
			up.render(mouseX, mouseY, partialTick);
			showHide.render(mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(showHide, up, down);
		}
	}
}
