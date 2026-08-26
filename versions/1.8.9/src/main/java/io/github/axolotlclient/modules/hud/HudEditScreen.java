/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.NumberOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.config.ui.MenuCatalog;
import io.github.axolotlclient.config.ui.MenuCatalog.Module;
import io.github.axolotlclient.config.ui.MenuCatalog.Node;
import io.github.axolotlclient.config.ui.MenuCatalog.OptionNode;
import io.github.axolotlclient.config.ui.MenuCatalog.SectionNode;
import io.github.axolotlclient.config.ui.MenuCatalog.Tab;
import io.github.axolotlclient.config.ui.MenuTheme;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.component.Positionable;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.CursorType;
import io.github.axolotlclient.util.CursorTypes;
import io.github.axolotlclient.util.DrawUtil;
import io.github.axolotlclient.util.MathUtil;
import io.github.axolotlclient.util.options.GenericOption;
import io.github.axolotlclient.util.options.vanilla.AxoGraphicsWidget;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Lunar-style client menu: HUD layout canvas plus a single searchable module
 * list with inline settings. Opened with Right Shift.
 */
public class HudEditScreen extends Screen {

	private final Screen parent;
	private final StringBuilder search = new StringBuilder();
	private final StringBuilder editBuffer = new StringBuilder();

	private List<Module> modules = List.of();
	private Tab tab = Tab.HUD;
	private String selectedId;
	private String expandedColorId;
	private String editingOptionName;
	private boolean collapsed;
	private boolean searchFocused;
	private boolean mouseDown;
	private int listScroll;
	private int mouseX, mouseY;
	private int caretTicks;
	private static final int TAB_COLS = 2;

	private HudEntry current;
	private DrawPosition offset;
	private SnappingHelper snap;
	private ModificationMode pendingMode = ModificationMode.NONE;
	private ModificationMode mode = ModificationMode.NONE;
	private DragState drag;

	public HudEditScreen() {
		this(null);
	}

	public HudEditScreen(Screen parent) {
		this.parent = parent;
		mouseDown = false;
	}

	@Override
	public void init() {
		mode = ModificationMode.NONE;
		modules = MenuCatalog.build();
		if (selectedId != null && findModule(selectedId) == null) {
			selectedId = null;
		}
		clampScroll();
	}

	@Override
	public boolean shouldPauseGame() {
		return false;
	}

	@Override
	public void tick() {
		caretTicks++;
	}

	@Override
	public void removed() {
		super.removed();
		pendingMode = ModificationMode.NONE;
		mode = ModificationMode.NONE;
		CursorTypes.ARROW.select();
		AxolotlClient.getInstance().saveConfig();
	}

	public void closeMenu() {
		AxolotlClient.getInstance().saveConfig();
		CursorTypes.ARROW.select();
		Minecraft.getInstance().openScreen(parent);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (Minecraft.getInstance().world != null) {
			fillGradient(0, 0, width, height, MenuTheme.OVERLAY, 0x66101820);
		} else {
			renderBackground(0);
			fillGradient(0, 0, width, height, MenuTheme.OVERLAY, 0xAA101820);
		}

		GlStateManager.enableTexture();
		Optional<HudEntry> entry;
		if (current != null && mode != ModificationMode.NONE) {
			current.setHovered(true);
			entry = Optional.of(current);
		} else if (!inSidebar(mouseX, mouseY)) {
			entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
			entry.ifPresent(hud -> hud.setHovered(true));
		} else {
			entry = Optional.empty();
		}

		var graphics = AxoRenderContextImpl.getInstance();
		if (mouseDown && snap != null && HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
			snap.renderHighlights(graphics, current);
		}
		HudManager.getInstance().renderPlaceholder(graphics, delta);
		if (entry.isPresent()) {
			var bounds = entry.get().getTrueBounds();
			if (mode == ModificationMode.NONE && bounds.isMouseOver(mouseX, mouseY) && !inSidebar(mouseX, mouseY)) {
				var supportsScaling = entry.get().supportsScaling();
				var tolerance = HudManagerCommon.HUD_RESCALE_GRAB_TOLERANCE;
				var toleranceSquared = tolerance * tolerance;
				var pending = ModificationMode.MOVE;
				if (supportsScaling) {
					if (MathUtil.distSq(mouseX, mouseY, bounds.x(), bounds.y()) < toleranceSquared) {
						pending = ModificationMode.TOP_LEFT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.xEnd(), bounds.yEnd()) < toleranceSquared) {
						pending = ModificationMode.BOTTOM_RIGHT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.x(), bounds.yEnd()) < toleranceSquared) {
						pending = ModificationMode.BOTTOM_LEFT;
					} else if (MathUtil.distSq(mouseX, mouseY, bounds.xEnd(), bounds.y()) < toleranceSquared) {
						pending = ModificationMode.TOP_RIGHT;
					}
				}
				pending.type.select();
				this.pendingMode = pending;
			}
		} else if (current == null && !inSidebar(mouseX, mouseY)) {
			CursorType.DEFAULT.select();
			pendingMode = ModificationMode.NONE;
			mode = ModificationMode.NONE;
		}
		if (mouseDown && snap != null) {
			snap.renderSnaps(graphics);
		}
		if (mouseDown && current != null && (this.mouseX != mouseX || this.mouseY != mouseY)) {
			handleHudDrag(mouseX, mouseY);
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		renderSidebar(graphics, mouseX, mouseY);
		renderTooltip(mouseX, mouseY);
	}

	private void renderSidebar(AxoRenderContext gfx, int mouseX, int mouseY) {
		int x = MenuTheme.PAD;
		int y = MenuTheme.PAD;
		int w = sidebarWidth();
		int h = height - MenuTheme.PAD * 2;
		gfx.br$fillRectRound(x, y, w, h, MenuTheme.PANEL, MenuTheme.PANEL_RADIUS);
		gfx.br$outlineRectRound(x, y, w, h, MenuTheme.OUTLINE, MenuTheme.PANEL_RADIUS);

		if (collapsed) {
			drawCentered(I18n.translate("menu.client.expand"), x + w / 2, y + h / 2 - 4, MenuTheme.TEXT_MUTED);
			return;
		}

		int innerX = x + MenuTheme.PAD;
		int innerW = w - MenuTheme.PAD * 2;
		int cursorY = y + 5;
		textRenderer.draw(I18n.translate("menu.client.title"), innerX, cursorY, MenuTheme.TEXT);
		drawCollapseAffordance(x + w - 16, y + 4, mouseX, mouseY);
		cursorY += MenuTheme.HEADER_H - 4;

		int tabW = tabWidth(innerW);
		for (int i = 0; i < Tab.values().length; i++) {
			Tab t = Tab.values()[i];
			int tx = tabX(innerX, innerW, i);
			int ty = tabY(cursorY, i);
			boolean active = tab == t;
			int color = active ? MenuTheme.ACCENT : (hovered(tx, ty, tabW - 1, MenuTheme.TAB_H, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
			gfx.br$fillRectRound(tx, ty, tabW - 1, MenuTheme.TAB_H, color, 3f);
			drawCentered(I18n.translate("menu.client.tab." + t.name().toLowerCase(Locale.ROOT)), tx + (tabW - 1) / 2, ty + 5, MenuTheme.TEXT);
		}
		cursorY += tabsBlockHeight() + 4;

		int searchColor = searchFocused ? MenuTheme.PANEL_SELECTED : MenuTheme.PANEL_INNER;
		gfx.br$fillRectRound(innerX, cursorY, innerW, MenuTheme.SEARCH_H, searchColor, 3f);
		String searchText = search.isEmpty() && !searchFocused ? I18n.translate("menu.client.search") : search.toString();
		int searchTextColor = search.isEmpty() && !searchFocused ? MenuTheme.TEXT_DIM : MenuTheme.TEXT;
		textRenderer.draw(trim(searchText, innerW - 10), innerX + 5, cursorY + 5, searchTextColor);
		if (searchFocused && (caretTicks / 6) % 2 == 0) {
			int caret = innerX + 5 + textRenderer.getWidth(trim(search.toString(), innerW - 10));
			fill(caret, cursorY + 4, caret + 1, cursorY + MenuTheme.SEARCH_H - 4, MenuTheme.ACCENT);
		}
		cursorY += MenuTheme.SEARCH_H + 4;

		int listTop = cursorY;
		int listBottom = y + h - MenuTheme.FOOTER_H - 4;
		int listH = Math.max(1, listBottom - listTop);
		DrawUtil.enableScissor(innerX, listTop, innerX + innerW, listBottom);
		int drawY = listTop - listScroll;
		String tooltip = null;
		for (Module module : visibleModules()) {
			int moduleH = moduleHeight(module);
			if (drawY + moduleH >= listTop && drawY <= listBottom) {
				tooltip = renderModule(gfx, module, innerX, drawY, innerW, mouseX, mouseY, tooltip);
			}
			drawY += moduleH + 2;
		}
		DrawUtil.disableScissor();

		int contentH = contentHeight();
		if (contentH > listH) {
			int barH = Math.max(16, listH * listH / contentH);
			int barY = listTop + (int) ((listH - barH) * (listScroll / (float) Math.max(1, contentH - listH)));
			gfx.br$fillRectRound(x + w - 5, barY, 3, barH, MenuTheme.SCROLLBAR, 1.5f);
		}

		int footerY = y + h - MenuTheme.FOOTER_H + 2;
		boolean snapOn = HudManager.getInstance().isSnappingEnabled();
		drawFooterButton(gfx, innerX, footerY, 40, I18n.translate("menu.client.snap"), snapOn, mouseX, mouseY);
		drawFooterButton(gfx, innerX + innerW - 48, footerY, 48, I18n.translate("close"), false, mouseX, mouseY);

		this.hoverTooltip = tooltip;
	}

	private String hoverTooltip;

	private String renderModule(AxoRenderContext gfx, Module module, int x, int y, int w, int mouseX, int mouseY, String tooltip) {
		boolean selected = module.getId().equals(selectedId);
		int h = MenuTheme.MODULE_H;
		int bg = selected ? MenuTheme.PANEL_SELECTED : (hovered(x, y, w, h, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
		gfx.br$fillRectRound(x, y, w, h, bg, 4f);
		if (selected) {
			fill(x, y + 2, x + 2, y + h - 2, MenuTheme.ACCENT);
		}
		textRenderer.draw(trim(module.displayName(), w - (module.getEnabled() != null ? 40 : 16)), x + 6, y + 6, MenuTheme.TEXT);
		if (module.getEnabled() != null) {
			drawToggle(gfx, x + w - MenuTheme.TOGGLE_W - 6, y + (h - MenuTheme.TOGGLE_H) / 2, module.getEnabled().get(), MenuCatalog.isForcedOff(module.getEnabled()));
		}
		if (selected) {
			int optY = y + h + 2;
			tooltip = renderNodes(gfx, module.getNodes(), x + 4, optY, w - 8, mouseX, mouseY, tooltip, 0);
		}
		return tooltip;
	}

	private String renderNodes(AxoRenderContext gfx, List<Node> nodes, int x, int y, int w, int mouseX, int mouseY, String tooltip, int depth) {
		int cursor = y;
		for (Node node : nodes) {
			if (node instanceof SectionNode section) {
				textRenderer.draw(trim(MenuCatalog.label(section.nameKey()), w), x + depth * 4, cursor + 3, MenuTheme.ACCENT);
				cursor += MenuTheme.SECTION_H;
				tooltip = renderNodes(gfx, section.children(), x, cursor, w, mouseX, mouseY, tooltip, depth + 1);
				cursor += sectionHeight(section.children(), depth + 1);
			} else if (node instanceof OptionNode optionNode) {
				tooltip = renderOption(gfx, optionNode.option(), x, cursor, w, mouseX, mouseY, tooltip);
				cursor += optionHeight(optionNode.option());
			}
		}
		return tooltip;
	}

	private String renderOption(AxoRenderContext gfx, io.github.axolotlclient.AxolotlClientConfig.api.options.Option<?> option, int x, int y, int w, int mouseX, int mouseY, String tooltip) {
		int h = MenuTheme.OPTION_H;
		boolean hover = hovered(x, y, w, h, mouseX, mouseY);
		if (hover) {
			gfx.br$fillRectRound(x, y, w, h, MenuTheme.PANEL_HOVER, 2f);
			String tip = option.getTooltip();
			if (tip != null) {
				String translated = I18n.translate(tip);
				if (!translated.equals(tip)) {
					tooltip = translated.replace("<br>", "\n");
				}
			}
		}
		int controlW = 72;
		int labelW = w - controlW - 4;
		textRenderer.draw(trim(MenuCatalog.label(option.getName()), labelW), x + 2, y + 4, MenuTheme.TEXT_MUTED);
		int cx = x + w - controlW;
		if (option instanceof BooleanOption bool) {
			drawToggle(gfx, cx + controlW - MenuTheme.TOGGLE_W, y + 2, bool.get(), MenuCatalog.isForcedOff(bool));
		} else if (option instanceof NumberOption<?> number) {
			drawSlider(gfx, cx, y + 3, controlW, number);
		} else if (option instanceof ColorOption color) {
			fill(cx + controlW - 14, y + 3, cx + controlW - 2, y + h - 3, color.get().toInt());
			DrawUtil.outlineRect(cx + controlW - 14, y + 3, 12, h - 6, MenuTheme.OUTLINE);
			if (isExpandedColor(option)) {
				int py = y + h;
				drawChannelSlider(gfx, x, py, w, "R", color.getOriginal().getRed() / 255f, 0xFFFF5555);
				drawChannelSlider(gfx, x, py + MenuTheme.OPTION_H, w, "G", color.getOriginal().getGreen() / 255f, 0xFF55FF55);
				drawChannelSlider(gfx, x, py + MenuTheme.OPTION_H * 2, w, "B", color.getOriginal().getBlue() / 255f, 0xFF5555FF);
				drawChannelSlider(gfx, x, py + MenuTheme.OPTION_H * 3, w, "A", color.getOriginal().getAlpha() / 255f, MenuTheme.TEXT);
				textRenderer.draw(I18n.translate("chroma"), x + 2, py + MenuTheme.OPTION_H * 4 + 4, MenuTheme.TEXT_MUTED);
				drawToggle(gfx, x + w - MenuTheme.TOGGLE_W, py + MenuTheme.OPTION_H * 4 + 2, color.getOriginal().isChroma(), false);
			}
		} else if (option instanceof EnumOption<?> || option instanceof StringArrayOption) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, MenuTheme.PANEL_INNER, 3f);
			drawCentered(trim(enumLabel(option), controlW - 4), cx + controlW / 2, y + 4, MenuTheme.TEXT);
		} else if (option instanceof StringOption str) {
			boolean editing = option.getName().equals(editingOptionName);
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, editing ? MenuTheme.PANEL_SELECTED : MenuTheme.PANEL_INNER, 3f);
			String value = editing ? editBuffer.toString() : str.get();
			textRenderer.draw(trim(value == null ? "" : value, controlW - 6), cx + 3, y + 4, MenuTheme.TEXT);
		} else if (option instanceof GenericOption generic) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, hover ? MenuTheme.ACCENT : MenuTheme.PANEL_INNER, 3f);
			drawCentered(trim(I18n.translate(generic.getLabel()), controlW - 4), cx + controlW / 2, y + 4, hover ? 0xFF0B0D12 : MenuTheme.TEXT);
		} else if (option instanceof GraphicsOption) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, hover ? MenuTheme.ACCENT : MenuTheme.PANEL_INNER, 3f);
			drawCentered(I18n.translate("menu.client.edit"), cx + controlW / 2, y + 4, hover ? 0xFF0B0D12 : MenuTheme.TEXT);
		} else {
			textRenderer.draw(trim(String.valueOf(option.get()), controlW), cx, y + 4, MenuTheme.TEXT_MUTED);
		}
		return tooltip;
	}

	private void drawChannelSlider(AxoRenderContext gfx, int x, int y, int w, String name, float t, int tint) {
		textRenderer.draw(name, x + 2, y + 4, tint);
		int sx = x + 12;
		int sw = w - 14;
		gfx.br$fillRectRound(sx, y + 6, sw, 4, MenuTheme.SLIDER_TRACK, 2f);
		int knob = sx + MathUtil.clamp((int) (t * sw), 0, sw);
		gfx.br$fillRectRound(sx, y + 6, Math.max(2, knob - sx), 4, tint, 2f);
	}

	private void drawSlider(AxoRenderContext gfx, int x, int y, int w, NumberOption<?> number) {
		float min = number.getMin().floatValue();
		float max = number.getMax().floatValue();
		float value = number.get().floatValue();
		float t = max == min ? 0f : MathUtil.clamp((value - min) / (max - min), 0f, 1f);
		gfx.br$fillRectRound(x, y + 3, w, 4, MenuTheme.SLIDER_TRACK, 2f);
		int filled = Math.max(2, (int) (w * t));
		gfx.br$fillRectRound(x, y + 3, filled, 4, MenuTheme.ACCENT, 2f);
		String label = formatNumber(number);
		drawCentered(label, x + w / 2, y - 1, MenuTheme.TEXT);
	}

	private void drawToggle(AxoRenderContext gfx, int x, int y, boolean on, boolean forcedOff) {
		int color = forcedOff ? MenuTheme.TEXT_DIM : (on ? MenuTheme.TOGGLE_ON : MenuTheme.TOGGLE_OFF);
		gfx.br$fillRectRound(x, y, MenuTheme.TOGGLE_W, MenuTheme.TOGGLE_H, color, MenuTheme.TOGGLE_H / 2f);
		int knob = on ? x + MenuTheme.TOGGLE_W - MenuTheme.TOGGLE_H + 1 : x + 1;
		gfx.br$fillRectRound(knob, y + 1, MenuTheme.TOGGLE_H - 2, MenuTheme.TOGGLE_H - 2, MenuTheme.TEXT, (MenuTheme.TOGGLE_H - 2) / 2f);
	}

	private void drawFooterButton(AxoRenderContext gfx, int x, int y, int w, String text, boolean active, int mouseX, int mouseY) {
		int color = active ? MenuTheme.ACCENT : (hovered(x, y, w, 16, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
		gfx.br$fillRectRound(x, y, w, 16, color, 3f);
		drawCentered(trim(text, w - 4), x + w / 2, y + 4, active ? 0xFF0B0D12 : MenuTheme.TEXT);
	}

	private void drawCollapseAffordance(int x, int y, int mouseX, int mouseY) {
		int color = hovered(x, y, 12, 12, mouseX, mouseY) ? MenuTheme.TEXT : MenuTheme.TEXT_MUTED;
		textRenderer.draw("<", x, y + 2, color);
	}

	private void renderTooltip(int mouseX, int mouseY) {
		if (hoverTooltip == null || hoverTooltip.isEmpty()) {
			return;
		}
		String[] lines = hoverTooltip.split("\n");
		int tw = 0;
		for (String line : lines) {
			tw = Math.max(tw, textRenderer.getWidth(line));
		}
		int th = lines.length * 10 + 6;
		int tx = Math.min(mouseX + 10, width - tw - 10);
		int ty = Math.min(mouseY + 12, height - th - 4);
		fill(tx - 3, ty - 3, tx + tw + 3, ty + th - 3, 0xF0101018);
		DrawUtil.outlineRect(tx - 3, ty - 3, tw + 6, th, MenuTheme.ACCENT);
		int ly = ty;
		for (String line : lines) {
			textRenderer.draw(line, tx, ly, MenuTheme.TEXT);
			ly += 10;
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		searchFocused = false;
		if (inSidebar(mouseX, mouseY)) {
			if (button == 0) {
				handleSidebarClick(mouseX, mouseY);
			}
			return;
		}
		commitStringEdit();
		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
		if (button == 0) {
			mouseDown = true;
			if (entry.isPresent()) {
				current = entry.get();
				offset = new DrawPosition(mouseX - current.getTruePos().x(), mouseY - current.getTruePos().y());
				if (pendingMode == ModificationMode.MOVE) {
					updateSnapState();
				}
				mode = pendingMode;
				selectHud(current);
			} else {
				mode = ModificationMode.NONE;
				current = null;
			}
		} else if (button == 1) {
			entry.ifPresent(this::selectHud);
		}
	}

	private void handleSidebarClick(int mouseX, int mouseY) {
		int x = MenuTheme.PAD;
		int y = MenuTheme.PAD;
		int w = sidebarWidth();
		int h = height - MenuTheme.PAD * 2;
		if (collapsed) {
			collapsed = false;
			return;
		}
		if (hovered(x + w - 16, y + 4, 12, 12, mouseX, mouseY)) {
			collapsed = true;
			return;
		}
		int innerX = x + MenuTheme.PAD;
		int innerW = w - MenuTheme.PAD * 2;
		int cursorY = y + 5 + MenuTheme.HEADER_H - 4;
		int tabW = tabWidth(innerW);
		for (int i = 0; i < Tab.values().length; i++) {
			int tx = tabX(innerX, innerW, i);
			int ty = tabY(cursorY, i);
			if (hovered(tx, ty, tabW - 1, MenuTheme.TAB_H, mouseX, mouseY)) {
				tab = Tab.values()[i];
				listScroll = 0;
				return;
			}
		}
		cursorY += tabsBlockHeight() + 4;
		if (hovered(innerX, cursorY, innerW, MenuTheme.SEARCH_H, mouseX, mouseY)) {
			searchFocused = true;
			editingOptionName = null;
			return;
		}
		cursorY += MenuTheme.SEARCH_H + 4;
		int listTop = cursorY;
		int listBottom = y + h - MenuTheme.FOOTER_H - 4;
		int footerY = y + h - MenuTheme.FOOTER_H + 2;
		if (hovered(innerX, footerY, 40, 16, mouseX, mouseY)) {
			HudManager.getInstance().toggleSnapping();
			return;
		}
		if (hovered(innerX + innerW - 48, footerY, 48, 16, mouseX, mouseY)) {
			closeMenu();
			return;
		}

		int drawY = listTop - listScroll;
		for (Module module : visibleModules()) {
			int moduleH = moduleHeight(module);
			if (mouseY >= listTop && mouseY <= listBottom && hovered(innerX, drawY, innerW, MenuTheme.MODULE_H, mouseX, mouseY)) {
				if (module.getEnabled() != null && hovered(innerX + innerW - MenuTheme.TOGGLE_W - 6, drawY + 4, MenuTheme.TOGGLE_W, MenuTheme.TOGGLE_H, mouseX, mouseY)) {
					if (!MenuCatalog.isForcedOff(module.getEnabled())) {
						module.getEnabled().toggle();
					}
					return;
				}
				selectedId = selectedId != null && selectedId.equals(module.getId()) ? null : module.getId();
				return;
			}
			if (module.getId().equals(selectedId) && mouseY >= listTop && mouseY <= listBottom) {
				if (handleNodeClick(module.getNodes(), innerX + 4, drawY + MenuTheme.MODULE_H + 2, innerW - 8, mouseX, mouseY)) {
					return;
				}
			}
			drawY += moduleH + 2;
		}
	}

	private boolean handleNodeClick(List<Node> nodes, int x, int y, int w, int mouseX, int mouseY) {
		int cursor = y;
		for (Node node : nodes) {
			if (node instanceof SectionNode section) {
				cursor += MenuTheme.SECTION_H;
				if (handleNodeClick(section.children(), x, cursor, w, mouseX, mouseY)) {
					return true;
				}
				cursor += sectionHeight(section.children(), 1);
			} else if (node instanceof OptionNode optionNode) {
				int h = optionHeight(optionNode.option());
				if (hovered(x, cursor, w, h, mouseX, mouseY)) {
					return clickOption(optionNode.option(), x, cursor, w, mouseX, mouseY);
				}
				cursor += h;
			}
		}
		return false;
	}

	private boolean clickOption(io.github.axolotlclient.AxolotlClientConfig.api.options.Option<?> option, int x, int y, int w, int mouseX, int mouseY) {
		if (shiftDown()) {
			option.setDefault();
			return true;
		}
		int controlW = 72;
		int cx = x + w - controlW;
		if (option instanceof BooleanOption bool) {
			if (!MenuCatalog.isForcedOff(bool)) {
				bool.toggle();
			}
			return true;
		}
		if (option instanceof NumberOption<?> number) {
			beginNumberDrag(number, cx, controlW, mouseX);
			return true;
		}
		if (option instanceof ColorOption color) {
			if (isExpandedColor(option)) {
				int py = y + MenuTheme.OPTION_H;
				if (hovered(x, py, w, MenuTheme.OPTION_H, mouseX, mouseY)) {
					beginColorDrag(color, 0, x + 12, w - 14, mouseX);
					return true;
				}
				if (hovered(x, py + MenuTheme.OPTION_H, w, MenuTheme.OPTION_H, mouseX, mouseY)) {
					beginColorDrag(color, 1, x + 12, w - 14, mouseX);
					return true;
				}
				if (hovered(x, py + MenuTheme.OPTION_H * 2, w, MenuTheme.OPTION_H, mouseX, mouseY)) {
					beginColorDrag(color, 2, x + 12, w - 14, mouseX);
					return true;
				}
				if (hovered(x, py + MenuTheme.OPTION_H * 3, w, MenuTheme.OPTION_H, mouseX, mouseY)) {
					beginColorDrag(color, 3, x + 12, w - 14, mouseX);
					return true;
				}
				if (hovered(x, py + MenuTheme.OPTION_H * 4, w, MenuTheme.OPTION_H, mouseX, mouseY)) {
					color.getOriginal().setChroma(!color.getOriginal().isChroma());
					color.updated();
					return true;
				}
			}
			expandedColorId = isExpandedColor(option) ? null : option.getName();
			return true;
		}
		if (option instanceof EnumOption<?> enumOption) {
			cycleEnum(enumOption, shiftDown() ? -1 : 1);
			return true;
		}
		if (option instanceof StringArrayOption array) {
			cycleArray(array, shiftDown() ? -1 : 1);
			return true;
		}
		if (option instanceof StringOption str) {
			searchFocused = false;
			editingOptionName = option.getName();
			editBuffer.setLength(0);
			editBuffer.append(str.get() == null ? "" : str.get());
			return true;
		}
		if (option instanceof GenericOption generic) {
			generic.get().onClick();
			return true;
		}
		if (option instanceof GraphicsOption graphics) {
			Minecraft.getInstance().openScreen(new AxoGraphicsWidget.AxoGraphicsEditorScreen(this, graphics));
			return true;
		}
		return true;
	}

	private void beginNumberDrag(NumberOption<?> number, int x, int w, int mouseX) {
		drag = new DragState(number, null, -1, x, w);
		applyNumberDrag(mouseX);
	}

	private void beginColorDrag(ColorOption color, int channel, int x, int w, int mouseX) {
		drag = new DragState(null, color, channel, x, w);
		applyColorDrag(mouseX);
	}

	private void applyNumberDrag(int mouseX) {
		if (drag == null || drag.number == null) {
			return;
		}
		float t = MathUtil.clamp((mouseX - drag.x) / (float) Math.max(1, drag.w), 0f, 1f);
		float min = drag.number.getMin().floatValue();
		float max = drag.number.getMax().floatValue();
		float value = min + t * (max - min);
		if (drag.number instanceof IntegerOption integer) {
			integer.set(Math.round(value));
		} else if (drag.number instanceof FloatOption flt) {
			flt.set(value);
		} else if (drag.number instanceof DoubleOption dbl) {
			dbl.set((double) value);
		}
	}

	private void applyColorDrag(int mouseX) {
		if (drag == null || drag.color == null) {
			return;
		}
		int channel = MathUtil.clamp((int) (MathUtil.clamp((mouseX - drag.x) / (float) Math.max(1, drag.w), 0f, 1f) * 255), 0, 255);
		Color c = drag.color.getOriginal();
		switch (drag.channel) {
			case 0 -> c.setRed(channel);
			case 1 -> c.setGreen(channel);
			case 2 -> c.setBlue(channel);
			case 3 -> c.setAlpha(channel);
			default -> {
			}
		}
		drag.color.updated();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void cycleEnum(EnumOption enumOption, int delta) {
		Object[] values = enumOption.getClazz().getEnumConstants();
		int idx = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(enumOption.get())) {
				idx = i;
				break;
			}
		}
		int next = Math.floorMod(idx + delta, values.length);
		enumOption.set(values[next]);
	}

	private void cycleArray(StringArrayOption array, int delta) {
		String[] values = array.getValues();
		int idx = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(array.get())) {
				idx = i;
				break;
			}
		}
		array.set(values[Math.floorMod(idx + delta, values.length)]);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button) {
		if (current != null && current.getCategory() != null) {
			AxolotlClient.getInstance().saveConfig();
		}
		current = null;
		snap = null;
		mouseDown = false;
		drag = null;
		mode = ModificationMode.NONE;
		pendingMode.type.select();
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0 && inSidebar(mouseX, mouseY)) {
			listScroll -= Integer.signum(wheel) * 14;
			clampScroll();
		}
		if (drag != null && Mouse.isButtonDown(0)) {
			if (drag.number != null) {
				applyNumberDrag(mouseX);
			} else if (drag.color != null) {
				applyColorDrag(mouseX);
			}
		}
	}

	@Override
	protected void keyPressed(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE || (!searchFocused && editingOptionName == null && keyCode == Keyboard.KEY_RSHIFT)) {
			if (searchFocused || editingOptionName != null) {
				searchFocused = false;
				commitStringEdit();
				return;
			}
			closeMenu();
			return;
		}
		if (searchFocused) {
			typeInto(search, typedChar, keyCode, 40);
			listScroll = 0;
			return;
		}
		if (editingOptionName != null) {
			if (keyCode == Keyboard.KEY_RETURN) {
				commitStringEdit();
				return;
			}
			typeInto(editBuffer, typedChar, keyCode, 64);
			return;
		}
		super.keyPressed(typedChar, keyCode);
	}

	private void typeInto(StringBuilder buffer, char typedChar, int keyCode, int max) {
		if (keyCode == Keyboard.KEY_BACK && !buffer.isEmpty()) {
			buffer.deleteCharAt(buffer.length() - 1);
			return;
		}
		boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		if (ctrl && (keyCode == Keyboard.KEY_V || typedChar == 22)) {
			String clip = Screen.getClipboard();
			if (clip != null) {
				for (char c : clip.toCharArray()) {
					if (buffer.length() < max && ChatAllowedCharacter(c)) {
						buffer.append(c);
					}
				}
			}
			return;
		}
		if (buffer.length() < max && ChatAllowedCharacter(typedChar)) {
			buffer.append(typedChar);
		}
	}

	private boolean ChatAllowedCharacter(char c) {
		return c >= 32 && c != 127 && c != 167;
	}

	private void commitStringEdit() {
		if (editingOptionName == null) {
			return;
		}
		Module selected = findModule(selectedId);
		if (selected != null) {
			commitString(selected.getNodes(), editingOptionName, editBuffer.toString());
		}
		editingOptionName = null;
		editBuffer.setLength(0);
	}

	private boolean commitString(List<Node> nodes, String name, String value) {
		for (Node node : nodes) {
			if (node instanceof OptionNode optionNode && optionNode.option() instanceof StringOption str && name.equals(str.getName())) {
				str.set(value);
				return true;
			}
			if (node instanceof SectionNode section && commitString(section.children(), name, value)) {
				return true;
			}
		}
		return false;
	}

	private void selectHud(HudEntry hud) {
		tab = Tab.HUD;
		collapsed = false;
		for (Module module : modules) {
			if (module.getHud() == hud) {
				selectedId = module.getId();
				ensureVisible(module);
				return;
			}
		}
	}

	private void ensureVisible(Module module) {
		int y = 0;
		for (Module m : visibleModules()) {
			if (m.getId().equals(module.getId())) {
				listScroll = Math.max(0, y - 20);
				clampScroll();
				return;
			}
			y += moduleHeight(m) + 2;
		}
	}

	private void updateSnapState() {
		if (HudManager.getInstance().isSnappingEnabled() && current != null) {
			var bounds = SnappingHelper.getNonDependentEntries(current, HudManager.getInstance().getMoveableEntries())
				.stream()
				.map(Positionable::getTrueBounds)
				.collect(Collectors.toCollection(ArrayList::new));
			bounds.remove(current.getTrueBounds());
			current.getDependenciesX().keySet().forEach(e -> bounds.remove(e.getTrueBounds()));
			current.getDependenciesY().keySet().forEach(e -> bounds.remove(e.getTrueBounds()));
			snap = new SnappingHelper(bounds, current.getTrueBounds());
		} else if (snap != null) {
			snap = null;
		}
	}

	private void handleHudDrag(int mouseX, int mouseY) {
		if (current == null || !mouseDown) {
			return;
		}
		current.clearBoundsDependencies();
		if (mode == ModificationMode.MOVE) {
			current.setPos(mouseX - offset.x() + current.offsetTrueWidth(), mouseY - offset.y() + current.offsetTrueHeight());
			if (snap != null) {
				Collection<HudEntry> entries = null;
				Optional<Integer> snapX = snap.getCurrentXSnap(), snapY = snap.getCurrentYSnap();
				if (snapX.isPresent() || snapY.isPresent()) {
					entries = HudManagerCommon.getInstance().getMoveableEntries();
					entries.remove(current);
					entries.removeIf(e -> e.dependsOnX(current).isPresent() || e.dependsOnY(current).isPresent());
				}
				snap.setCurrent(current.getTrueBounds());
				if (snapX.isPresent()) {
					current.setX(snapX.get() + current.offsetTrueWidth());
					if (HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
						snap.getXTouching(entries, current).forEach(c -> {
							c.getLeft().removeBoundsDependencyX(current);
							current.addBoundsDependency(c.getLeft(), c.getRight());
						});
					}
				}
				if (snapY.isPresent()) {
					current.setY(snapY.get() + current.offsetTrueHeight());
					if (HudManagerCommon.getInstance().hudLinkCreationEnabled.get()) {
						snap.getYTouching(entries, current).forEach(c -> {
							c.getLeft().removeBoundsDependencyY(current);
							current.addBoundsDependency(c.getLeft(), c.getRight());
						});
					}
				}
				HudManagerCommon.getInstance().saveHudDependencyLinks();
			}
		} else {
			var bounds = current.getTrueBounds();
			int newWidth;
			int newHeight;
			if (mode == ModificationMode.TOP_LEFT) {
				newWidth = mouseX - bounds.xEnd();
				newHeight = mouseY - bounds.yEnd();
			} else if (mode == ModificationMode.BOTTOM_LEFT) {
				newWidth = mouseX - bounds.xEnd();
				newHeight = mouseY - bounds.y();
			} else if (mode == ModificationMode.TOP_RIGHT) {
				newWidth = mouseX - bounds.x();
				newHeight = mouseY - bounds.yEnd();
			} else if (mode == ModificationMode.BOTTOM_RIGHT) {
				newWidth = mouseX - bounds.x();
				newHeight = mouseY - bounds.y();
			} else {
				newWidth = bounds.width();
				newHeight = bounds.height();
			}
			float newScale = current.getScale() * Math.max((float) Math.abs(newWidth) / bounds.width(), (float) Math.abs(newHeight) / bounds.height());
			current.setScale(Math.max(0.1f, newScale));
			if (mode == ModificationMode.TOP_LEFT) {
				current.setPos(bounds.xEnd() - current.getTrueWidth(), bounds.yEnd() - current.getTrueHeight());
			} else if (mode == ModificationMode.BOTTOM_LEFT) {
				current.setX(bounds.xEnd() - current.getTrueWidth());
			} else if (mode == ModificationMode.TOP_RIGHT) {
				current.setY(bounds.yEnd() - current.getTrueHeight());
			}
		}
		if (current.tickable()) {
			current.tick();
		}
	}

	private List<Module> visibleModules() {
		String query = search.toString().toLowerCase(Locale.ROOT).trim();
		List<Module> visible = new ArrayList<>();
		for (Module module : modules) {
			if (!module.matches(query)) {
				continue;
			}
			if (query.isEmpty() && module.getTab() != tab) {
				continue;
			}
			visible.add(module);
		}
		return visible;
	}

	private int tabWidth(int innerW) {
		return innerW / TAB_COLS;
	}

	private int tabX(int innerX, int innerW, int index) {
		return innerX + (index % TAB_COLS) * tabWidth(innerW);
	}

	private int tabY(int tabsTop, int index) {
		return tabsTop + (index / TAB_COLS) * (MenuTheme.TAB_H + 2);
	}

	private int tabRowCount() {
		return (Tab.values().length + TAB_COLS - 1) / TAB_COLS;
	}

	private int tabsBlockHeight() {
		return tabRowCount() * (MenuTheme.TAB_H + 2) - 2;
	}

	private Module findModule(String id) {
		if (id == null) {
			return null;
		}
		for (Module module : modules) {
			if (id.equals(module.getId())) {
				return module;
			}
		}
		return null;
	}

	private int sidebarWidth() {
		if (collapsed) {
			return 18;
		}
		return MathUtil.clamp(width * 42 / 100, 158, 248);
	}

	private boolean inSidebar(int mouseX, int mouseY) {
		int x = MenuTheme.PAD;
		int y = MenuTheme.PAD;
		int w = sidebarWidth();
		int h = height - MenuTheme.PAD * 2;
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
	}

	private int moduleHeight(Module module) {
		int h = MenuTheme.MODULE_H;
		if (module.getId().equals(selectedId)) {
			h += 2 + sectionHeight(module.getNodes(), 0);
		}
		return h;
	}

	private int sectionHeight(List<Node> nodes, int depth) {
		int h = 0;
		for (Node node : nodes) {
			if (node instanceof SectionNode section) {
				h += MenuTheme.SECTION_H + sectionHeight(section.children(), depth + 1);
			} else if (node instanceof OptionNode optionNode) {
				h += optionHeight(optionNode.option());
			}
		}
		return h;
	}

	private int optionHeight(io.github.axolotlclient.AxolotlClientConfig.api.options.Option<?> option) {
		if (option instanceof ColorOption && isExpandedColor(option)) {
			return MenuTheme.OPTION_H * 6;
		}
		return MenuTheme.OPTION_H;
	}

	private boolean isExpandedColor(io.github.axolotlclient.AxolotlClientConfig.api.options.Option<?> option) {
		return option.getName().equals(expandedColorId);
	}

	private int contentHeight() {
		int h = 0;
		for (Module module : visibleModules()) {
			h += moduleHeight(module) + 2;
		}
		return h;
	}

	private void clampScroll() {
		int listH = Math.max(1, height - MenuTheme.PAD * 2 - MenuTheme.HEADER_H - tabsBlockHeight() - MenuTheme.SEARCH_H - MenuTheme.FOOTER_H - 28);
		listScroll = MathUtil.clamp(listScroll, 0, Math.max(0, contentHeight() - listH));
	}

	private void drawCentered(String text, int x, int y, int color) {
		textRenderer.draw(text, x - textRenderer.getWidth(text) / 2, y, color);
	}

	private String trim(String text, int maxWidth) {
		if (text == null) {
			return "";
		}
		if (textRenderer.getWidth(text) <= maxWidth) {
			return text;
		}
		String ellipsis = "...";
		int ellipsisW = textRenderer.getWidth(ellipsis);
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (textRenderer.getWidth(out.toString() + text.charAt(i)) + ellipsisW > maxWidth) {
				break;
			}
			out.append(text.charAt(i));
		}
		return out + ellipsis;
	}

	private String formatNumber(NumberOption<?> number) {
		if (number instanceof IntegerOption) {
			return String.valueOf(number.get().intValue());
		}
		float v = number.get().floatValue();
		if (Math.abs(v - Math.round(v)) < 0.05f) {
			return String.valueOf(Math.round(v));
		}
		return String.format(Locale.ROOT, "%.2f", v);
	}

	private String enumLabel(io.github.axolotlclient.AxolotlClientConfig.api.options.Option<?> option) {
		Object value = option.get();
		if (value == null) {
			return "";
		}
		return MenuCatalog.label(value.toString());
	}

	private boolean hovered(int x, int y, int w, int h, int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	private static boolean shiftDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

	private record DragState(NumberOption<?> number, ColorOption color, int channel, int x, int w) {
	}

	@RequiredArgsConstructor
	private enum ModificationMode {
		NONE(CursorType.DEFAULT),
		MOVE(CursorTypes.RESIZE_ALL),
		TOP_LEFT(CursorTypes.RESIZE_NWSE),
		TOP_RIGHT(CursorTypes.RESIZE_NESW),
		BOTTOM_LEFT(CursorTypes.RESIZE_NESW),
		BOTTOM_RIGHT(CursorTypes.RESIZE_NWSE);
		private final CursorType type;
	}
}
