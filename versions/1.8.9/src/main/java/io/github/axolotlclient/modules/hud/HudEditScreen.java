/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.axolotlclient.DolphinClient;
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
import io.github.axolotlclient.modules.hud.util.ItemUtil;
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
 * Centered client menu: HUD layout canvas plus a searchable module grid with
 * icons and inline settings. Opened with Right Shift.
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
	private static final int TAB_COLS = 4;

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
		DolphinClient.getInstance().saveConfig();
	}

	public void closeMenu() {
		DolphinClient.getInstance().saveConfig();
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
		} else if (!inPanel(mouseX, mouseY)) {
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
			if (mode == ModificationMode.NONE && bounds.isMouseOver(mouseX, mouseY) && !inPanel(mouseX, mouseY)) {
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
		} else if (current == null && !inPanel(mouseX, mouseY)) {
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

		renderPanel(graphics, mouseX, mouseY);
		renderTooltip(mouseX, mouseY);
	}

	private void renderPanel(AxoRenderContext gfx, int mouseX, int mouseY) {
		PanelLayout box = layout();
		if (collapsed) {
			int w = MenuTheme.COLLAPSED_W;
			int h = MenuTheme.COLLAPSED_H;
			int x = (width - w) / 2;
			int y = 8;
			gfx.br$fillRectRound(x, y, w, h, MenuTheme.PANEL, MenuTheme.PANEL_RADIUS);
			drawCentered(I18n.translate("menu.client.title"), x + w / 2, textY(y, h), MenuTheme.TEXT_MUTED);
			return;
		}

		gfx.br$fillRectRound(box.x, box.y, box.w, box.h, MenuTheme.PANEL, MenuTheme.PANEL_RADIUS);

		String title = I18n.translate("menu.client.title");
		textRenderer.draw(title, box.innerX, textY(box.y + MenuTheme.PAD, MenuTheme.HEADER_H), MenuTheme.TEXT);
		drawHeaderButton(gfx, box.minimizeX(), box.headerBtnY(), "-", mouseX, mouseY);
		drawHeaderButton(gfx, box.closeX(), box.headerBtnY(), "x", mouseX, mouseY);

		int tabW = tabWidth(box.innerW);
		for (int i = 0; i < Tab.values().length; i++) {
			Tab t = Tab.values()[i];
			int tx = tabX(box.innerX, box.innerW, i);
			int ty = tabY(box.tabsY, i);
			boolean active = tab == t;
			int color = active ? MenuTheme.ACCENT : (hovered(tx, ty, tabW, MenuTheme.TAB_H, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
			gfx.br$fillRectRound(tx, ty, tabW, MenuTheme.TAB_H, color, 3f);
			drawCentered(I18n.translate("menu.client.tab." + t.name().toLowerCase(Locale.ROOT)), tx + tabW / 2, textY(ty, MenuTheme.TAB_H), MenuTheme.TEXT);
		}

		int searchColor = searchFocused ? MenuTheme.PANEL_SELECTED : MenuTheme.PANEL_INNER;
		gfx.br$fillRectRound(box.innerX, box.searchY, box.innerW, MenuTheme.SEARCH_H, searchColor, 3f);
		String searchText = search.isEmpty() && !searchFocused ? I18n.translate("menu.client.search") : search.toString();
		int searchTextColor = search.isEmpty() && !searchFocused ? MenuTheme.TEXT_DIM : MenuTheme.TEXT;
		int searchTextY = textY(box.searchY, MenuTheme.SEARCH_H);
		textRenderer.draw(trim(searchText, box.innerW - 12), box.innerX + 6, searchTextY, searchTextColor);
		if (searchFocused && (caretTicks / 6) % 2 == 0) {
			int caret = box.innerX + 6 + textRenderer.getWidth(trim(search.toString(), box.innerW - 12));
			fill(caret, searchTextY, caret + 1, searchTextY + 8, MenuTheme.ACCENT);
		}

		DrawUtil.enableScissor(box.innerX, box.listTop, box.innerX + box.innerW, box.listBottom);
		String tooltip = null;
		Module selected = findModule(selectedId);
		if (selected != null) {
			tooltip = renderDetail(gfx, selected, box, mouseX, mouseY, tooltip);
		} else {
			tooltip = renderGrid(gfx, box, mouseX, mouseY, tooltip);
		}
		DrawUtil.disableScissor();

		int contentH = contentHeight();
		int listH = Math.max(1, box.listBottom - box.listTop);
		if (contentH > listH) {
			int barH = Math.max(12, listH * listH / contentH);
			int barY = box.listTop + (int) ((listH - barH) * (listScroll / (float) Math.max(1, contentH - listH)));
			gfx.br$fillRectRound(box.x + box.w - 5, barY, 2, barH, MenuTheme.SCROLLBAR, 1.5f);
		}

		boolean snapOn = HudManager.getInstance().isSnappingEnabled();
		drawFooterButton(gfx, box.innerX, box.footerY, MenuTheme.FOOTER_BTN_W, I18n.translate("menu.client.snap"), snapOn, mouseX, mouseY);

		this.hoverTooltip = tooltip;
	}

	private String renderGrid(AxoRenderContext gfx, PanelLayout box, int mouseX, int mouseY, String tooltip) {
		List<Module> visible = visibleModules();
		int tileW = tileWidth(box.innerW);
		for (int i = 0; i < visible.size(); i++) {
			int col = i % MenuTheme.COLS;
			int row = i / MenuTheme.COLS;
			int x = box.innerX + col * (tileW + MenuTheme.TILE_GAP);
			int y = box.listTop + row * (MenuTheme.MODULE_H + MenuTheme.TILE_GAP) - listScroll;
			if (y + MenuTheme.MODULE_H < box.listTop || y > box.listBottom) {
				continue;
			}
			tooltip = renderModuleTile(gfx, visible.get(i), x, y, tileW, mouseX, mouseY, tooltip);
		}
		return tooltip;
	}

	private String renderDetail(AxoRenderContext gfx, Module module, PanelLayout box, int mouseX, int mouseY, String tooltip) {
		int y = box.listTop - listScroll;
		int backW = MenuTheme.BACK_BTN_W;
		boolean backHover = hovered(box.innerX, y, backW, MenuTheme.MODULE_H, mouseX, mouseY);
		gfx.br$fillRectRound(box.innerX, y, backW, MenuTheme.MODULE_H, backHover ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER, 4f);
		drawCentered(I18n.translate("menu.client.back"), box.innerX + backW / 2, textY(y, MenuTheme.MODULE_H), MenuTheme.TEXT);
		int tileX = box.innerX + backW + MenuTheme.TILE_GAP;
		renderModuleTile(gfx, module, tileX, y, box.innerW - backW - MenuTheme.TILE_GAP, mouseX, mouseY, tooltip);
		y += MenuTheme.MODULE_H + MenuTheme.STACK_GAP;
		return renderNodes(gfx, module.getNodes(), box.innerX, y, box.innerW, mouseX, mouseY, tooltip, 0);
	}

	private String renderModuleTile(AxoRenderContext gfx, Module module, int x, int y, int w, int mouseX, int mouseY, String tooltip) {
		boolean selected = module.getId().equals(selectedId);
		int h = MenuTheme.MODULE_H;
		int bg = selected ? MenuTheme.PANEL_SELECTED : (hovered(x, y, w, h, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
		gfx.br$fillRectRound(x, y, w, h, bg, 4f);
		if (module.getEnabled() != null && module.getEnabled().get()) {
			fill(x + 1, y + 6, x + 3, y + h - 6, MenuTheme.ACCENT);
		}
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		int iconX = x + 4;
		int iconY = y + (h - MenuTheme.ICON_SIZE) / 2;
		ItemUtil.renderGuiItemModel(ModsMenuIcons.forKey(module.getNameKey()), iconX, iconY);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableTexture();
		int textLeft = iconX + MenuTheme.ICON_SIZE + MenuTheme.ICON_TEXT_GAP;
		int toggleX = x + w - MenuTheme.TOGGLE_W - MenuTheme.TOGGLE_INSET;
		int textRight = (module.getEnabled() != null ? toggleX : x + w) - 6;
		textRenderer.draw(trim(module.displayName(), Math.max(8, textRight - textLeft)), textLeft, y + (h - 8) / 2, MenuTheme.TEXT);
		if (module.getEnabled() != null) {
			drawToggle(gfx, toggleX, y + (h - MenuTheme.TOGGLE_H) / 2, module.getEnabled().get(), MenuCatalog.isForcedOff(module.getEnabled()));
		}
		return tooltip;
	}

	private String hoverTooltip;

	private String renderNodes(AxoRenderContext gfx, List<Node> nodes, int x, int y, int w, int mouseX, int mouseY, String tooltip, int depth) {
		int cursor = y;
		for (Node node : nodes) {
			if (node instanceof SectionNode section) {
				textRenderer.draw(trim(MenuCatalog.label(section.nameKey()), w), x + depth * 4, textY(cursor, MenuTheme.SECTION_H), MenuTheme.ACCENT);
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
		int controlW = MenuTheme.CONTROL_W;
		int labelW = w - controlW - 8;
		textRenderer.draw(trim(MenuCatalog.label(option.getName()), labelW), x + 2, textY(y, h), MenuTheme.TEXT_MUTED);
		int cx = x + w - controlW;
		if (option instanceof BooleanOption bool) {
			drawToggle(gfx, cx + controlW - MenuTheme.TOGGLE_W, y + (h - MenuTheme.TOGGLE_H) / 2, bool.get(), MenuCatalog.isForcedOff(bool));
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
				drawToggle(gfx, x + w - MenuTheme.TOGGLE_W - MenuTheme.TOGGLE_INSET, py + MenuTheme.OPTION_H * 4 + (MenuTheme.OPTION_H - MenuTheme.TOGGLE_H) / 2, color.getOriginal().isChroma(), false);
			}
		} else if (option instanceof EnumOption<?> || option instanceof StringArrayOption) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, MenuTheme.PANEL_INNER, 3f);
			drawCentered(trim(enumLabel(option), controlW - 4), cx + controlW / 2, textY(y, h), MenuTheme.TEXT);
		} else if (option instanceof StringOption str) {
			boolean editing = option.getName().equals(editingOptionName);
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, editing ? MenuTheme.PANEL_SELECTED : MenuTheme.PANEL_INNER, 3f);
			String value = editing ? editBuffer.toString() : str.get();
			textRenderer.draw(trim(value == null ? "" : value, controlW - 6), cx + 3, textY(y, h), MenuTheme.TEXT);
		} else if (option instanceof GenericOption generic) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, hover ? MenuTheme.ACCENT : MenuTheme.PANEL_INNER, 3f);
			drawCentered(trim(I18n.translate(generic.getLabel()), controlW - 4), cx + controlW / 2, textY(y, h), hover ? 0xFF0B0D12 : MenuTheme.TEXT);
		} else if (option instanceof GraphicsOption) {
			gfx.br$fillRectRound(cx, y + 1, controlW, h - 2, hover ? MenuTheme.ACCENT : MenuTheme.PANEL_INNER, 3f);
			drawCentered(I18n.translate("menu.client.edit"), cx + controlW / 2, textY(y, h), hover ? 0xFF0B0D12 : MenuTheme.TEXT);
		} else {
			textRenderer.draw(trim(String.valueOf(option.get()), controlW), cx, textY(y, h), MenuTheme.TEXT_MUTED);
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
		int h = MenuTheme.FOOTER_BTN_H;
		int color = active ? MenuTheme.ACCENT : (hovered(x, y, w, h, mouseX, mouseY) ? MenuTheme.PANEL_HOVER : MenuTheme.PANEL_INNER);
		gfx.br$fillRectRound(x, y, w, h, color, 3f);
		drawCentered(trim(text, w - 4), x + w / 2, textY(y, h), active ? 0xFF0B0D12 : MenuTheme.TEXT);
	}

	private void drawHeaderButton(AxoRenderContext gfx, int x, int y, String glyph, int mouseX, int mouseY) {
		boolean hover = hovered(x, y, MenuTheme.HEADER_BTN, MenuTheme.HEADER_BTN, mouseX, mouseY);
		gfx.br$fillRectRound(x, y, MenuTheme.HEADER_BTN, MenuTheme.HEADER_BTN, hover ? MenuTheme.ACCENT : MenuTheme.PANEL_HOVER, 3f);
		drawCentered(glyph, x + MenuTheme.HEADER_BTN / 2, textY(y, MenuTheme.HEADER_BTN), hover ? 0xFF0B0D12 : MenuTheme.TEXT);
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
		if (inPanel(mouseX, mouseY)) {
			if (button == 0) {
				handlePanelClick(mouseX, mouseY);
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

	private void handlePanelClick(int mouseX, int mouseY) {
		PanelLayout box = layout();
		if (collapsed) {
			collapsed = false;
			return;
		}
		if (hovered(box.closeX(), box.headerBtnY(), MenuTheme.HEADER_BTN, MenuTheme.HEADER_BTN, mouseX, mouseY)) {
			closeMenu();
			return;
		}
		if (hovered(box.minimizeX(), box.headerBtnY(), MenuTheme.HEADER_BTN, MenuTheme.HEADER_BTN, mouseX, mouseY)) {
			collapsed = true;
			return;
		}
		int tabW = tabWidth(box.innerW);
		for (int i = 0; i < Tab.values().length; i++) {
			int tx = tabX(box.innerX, box.innerW, i);
			int ty = tabY(box.tabsY, i);
			if (hovered(tx, ty, tabW, MenuTheme.TAB_H, mouseX, mouseY)) {
				tab = Tab.values()[i];
				selectedId = null;
				listScroll = 0;
				return;
			}
		}
		if (hovered(box.innerX, box.searchY, box.innerW, MenuTheme.SEARCH_H, mouseX, mouseY)) {
			searchFocused = true;
			editingOptionName = null;
			return;
		}
		if (hovered(box.innerX, box.footerY, MenuTheme.FOOTER_BTN_W, MenuTheme.FOOTER_BTN_H, mouseX, mouseY)) {
			HudManager.getInstance().toggleSnapping();
			return;
		}

		if (mouseY < box.listTop || mouseY > box.listBottom) {
			return;
		}

		Module selected = findModule(selectedId);
		if (selected != null) {
			int y = box.listTop - listScroll;
			if (hovered(box.innerX, y, MenuTheme.BACK_BTN_W, MenuTheme.MODULE_H, mouseX, mouseY)) {
				selectedId = null;
				listScroll = 0;
				return;
			}
			int tileX = box.innerX + MenuTheme.BACK_BTN_W + MenuTheme.TILE_GAP;
			int tileW = box.innerW - MenuTheme.BACK_BTN_W - MenuTheme.TILE_GAP;
			if (hovered(tileX, y, tileW, MenuTheme.MODULE_H, mouseX, mouseY)) {
				clickModuleToggle(selected, tileX, y, tileW, mouseX, mouseY);
				return;
			}
			handleNodeClick(selected.getNodes(), box.innerX, y + MenuTheme.MODULE_H + MenuTheme.STACK_GAP, box.innerW, mouseX, mouseY);
			return;
		}

		List<Module> visible = visibleModules();
		int tileW = tileWidth(box.innerW);
		for (int i = 0; i < visible.size(); i++) {
			int col = i % MenuTheme.COLS;
			int row = i / MenuTheme.COLS;
			int x = box.innerX + col * (tileW + MenuTheme.TILE_GAP);
			int y = box.listTop + row * (MenuTheme.MODULE_H + MenuTheme.TILE_GAP) - listScroll;
			if (hovered(x, y, tileW, MenuTheme.MODULE_H, mouseX, mouseY)) {
				Module module = visible.get(i);
				if (!clickModuleToggle(module, x, y, tileW, mouseX, mouseY)) {
					selectedId = module.getId();
					listScroll = 0;
				}
				return;
			}
		}
	}

	private boolean clickModuleToggle(Module module, int x, int y, int w, int mouseX, int mouseY) {
		if (module.getEnabled() != null && hovered(x + w - MenuTheme.TOGGLE_W - MenuTheme.TOGGLE_INSET, y + (MenuTheme.MODULE_H - MenuTheme.TOGGLE_H) / 2, MenuTheme.TOGGLE_W, MenuTheme.TOGGLE_H, mouseX, mouseY)) {
			if (!MenuCatalog.isForcedOff(module.getEnabled())) {
				module.getEnabled().toggle();
			}
			return true;
		}
		return false;
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
		int controlW = MenuTheme.CONTROL_W;
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
			DolphinClient.getInstance().saveConfig();
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
		if (wheel != 0 && inPanel(mouseX, mouseY)) {
			listScroll -= Integer.signum(wheel) * 22;
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
		listScroll = 0;
		clampScroll();
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
		return (innerW - MenuTheme.TAB_GAP * (TAB_COLS - 1)) / TAB_COLS;
	}

	private int tabX(int innerX, int innerW, int index) {
		return innerX + (index % TAB_COLS) * (tabWidth(innerW) + MenuTheme.TAB_GAP);
	}

	private int tabY(int tabsTop, int index) {
		return tabsTop + (index / TAB_COLS) * (MenuTheme.TAB_H + MenuTheme.STACK_GAP);
	}

	private int tabRowCount() {
		return (Tab.values().length + TAB_COLS - 1) / TAB_COLS;
	}

	private int tabsBlockHeight() {
		return tabRowCount() * (MenuTheme.TAB_H + MenuTheme.STACK_GAP) - MenuTheme.STACK_GAP;
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

	private int tileWidth(int innerW) {
		return (innerW - MenuTheme.TILE_GAP * (MenuTheme.COLS - 1)) / MenuTheme.COLS;
	}

	private PanelLayout layout() {
		int w = panelWidth();
		int h = panelHeight();
		int x = (width - w) / 2;
		int y = (height - h) / 2;
		int innerX = x + MenuTheme.PAD;
		int innerW = w - MenuTheme.PAD * 2;
		int tabsY = y + MenuTheme.PAD + MenuTheme.HEADER_H;
		int searchY = tabsY + tabsBlockHeight() + MenuTheme.STACK_GAP;
		int listTop = searchY + MenuTheme.SEARCH_H + MenuTheme.STACK_GAP;
		int footerY = y + h - MenuTheme.FOOTER_H;
		int listBottom = footerY - MenuTheme.STACK_GAP;
		return new PanelLayout(x, y, w, h, innerX, innerW, tabsY, searchY, listTop, listBottom, footerY);
	}

	private int panelWidth() {
		if (collapsed) {
			return MenuTheme.COLLAPSED_W;
		}
		int preferred = Math.min(width - 16, Math.max(MenuTheme.MIN_PANEL_W, width - 48));
		return Math.min(MenuTheme.MAX_PANEL_W, preferred);
	}

	private int panelHeight() {
		if (collapsed) {
			return MenuTheme.COLLAPSED_H;
		}
		int preferred = Math.min(height - 12, Math.max(MenuTheme.MIN_PANEL_H, height - 24));
		return Math.min(MenuTheme.MAX_PANEL_H, preferred);
	}

	private boolean inPanel(int mouseX, int mouseY) {
		if (collapsed) {
			int w = MenuTheme.COLLAPSED_W;
			int h = MenuTheme.COLLAPSED_H;
			int x = (width - w) / 2;
			int y = 8;
			return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
		}
		PanelLayout box = layout();
		return mouseX >= box.x && mouseX <= box.x + box.w && mouseY >= box.y && mouseY <= box.y + box.h;
	}

	private int contentHeight() {
		if (findModule(selectedId) != null) {
			Module selected = findModule(selectedId);
			return MenuTheme.MODULE_H + MenuTheme.STACK_GAP + sectionHeight(selected.getNodes(), 0);
		}
		int rows = (int) Math.ceil(visibleModules().size() / (double) MenuTheme.COLS);
		return Math.max(0, rows * (MenuTheme.MODULE_H + MenuTheme.TILE_GAP) - MenuTheme.TILE_GAP);
	}

	private void clampScroll() {
		PanelLayout box = layout();
		int listH = Math.max(1, box.listBottom - box.listTop);
		listScroll = MathUtil.clamp(listScroll, 0, Math.max(0, contentHeight() - listH));
	}

	private record PanelLayout(int x, int y, int w, int h, int innerX, int innerW, int tabsY, int searchY, int listTop, int listBottom, int footerY) {
		int closeX() {
			return x + w - MenuTheme.HEADER_BTN_INSET - MenuTheme.HEADER_BTN;
		}

		int minimizeX() {
			return closeX() - MenuTheme.HEADER_BTN - 4;
		}

		int headerBtnY() {
			return y + MenuTheme.PAD + (MenuTheme.HEADER_H - MenuTheme.HEADER_BTN) / 2;
		}
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

	private void drawCentered(String text, int x, int y, int color) {
		textRenderer.draw(text, x - textRenderer.getWidth(text) / 2, y, color);
	}

	private int textY(int top, int height) {
		return top + (height - 8) / 2;
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
