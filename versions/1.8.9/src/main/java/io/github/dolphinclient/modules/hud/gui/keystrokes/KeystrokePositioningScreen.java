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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.dolphinclient.bridge.impl.AxoRenderContextImpl;
import io.github.dolphinclient.modules.hud.HudManager;
import io.github.dolphinclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.dolphinclient.modules.hud.snapping.SnappingHelper;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.util.DrawUtil;
import io.github.dolphinclient.modules.hud.util.Rectangle;
import io.github.dolphinclient.util.ClientColors;
import io.github.dolphinclient.util.CursorTypes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.input.Keyboard;

public class KeystrokePositioningScreen extends Screen {
	private static final String title = I18n.translate("keystrokes.stroke.move");
	private final Screen parent;
	private final KeystrokeHud hud;
	private KeystrokeHud.Keystroke focused;
	private final KeystrokeHud.Keystroke editing;

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke focused) {
		super();
		this.parent = parent;
		this.hud = hud;
		if (hud.keystrokes == null) {
			hud.setKeystrokes();
		}
		this.editing = focused;
		mouseDown = false;
	}

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud) {
		this(parent, hud, null);
	}

	private DrawPosition offset = null;
	private boolean mouseDown;
	private SnappingHelper snap;
	private int mouseX, mouseY;

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			closeScreen();
		} else if (buttonWidget.id == 1) {
			HudManager.getInstance().toggleSnapping();
			buttonWidget.message = I18n.translate("hud.snapping") + ": " +
				I18n.translate(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off");
		}
	}

	@Override
	public void init() {
		buttons.add(new ButtonWidget(0, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("gui.back")));
		buttons.add(new ButtonWidget(1, width / 2 - 50, height - 50, 100, 20, I18n.translate("hud.snapping") + ": "
			+ (I18n.translate(HudManager.getInstance().isSnappingEnabled() ? "options.on" : "options.off"))));
	}

	private float partialTick;

	@Override
	public void renderBackground() {
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, -300);
		super.renderBackground();
		HudManager.getInstance().renderPlaceholder(AxoRenderContextImpl.getInstance(), partialTick);
		GlStateManager.popMatrix();
		fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTick) {
		this.partialTick = partialTick;
		renderBackground();
		super.render(mouseX, mouseY, partialTick);
		Optional<KeystrokeHud.Keystroke> hovered = Optional.empty();
		if (editing != null) {
			drawStroke(mouseX, mouseY, editing);
			if (getScaledRenderPos(editing).isMouseOver(mouseX, mouseY)) {
				hovered = Optional.of(editing);
			}
		} else {
			for (KeystrokeHud.Keystroke k : hud.keystrokes) {
				var pos = Optional.of(getScaledRenderPos(k));
				if (pos.get().isMouseOver(mouseX, mouseY)) {
					hovered = Optional.of(k);
				}
				drawStroke(mouseX, mouseY, k);
			}
		}
		(hovered.isPresent() ? CursorTypes.RESIZE_ALL : CursorTypes.ARROW).select();
		if (mouseDown && snap != null) {
			snap.renderSnaps(AxoRenderContextImpl.getInstance());
		}
		// bweh
		if (mouseDown && (this.mouseX != mouseX || this.mouseY != mouseY)) {
			handleDrag(mouseX, mouseY);
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	private void drawStroke(int mouseX, int mouseY, KeystrokeHud.Keystroke s) {
		var rect = getScaledRenderPos(s);
		if (rect.isMouseOver(mouseX, mouseY)) {
			DrawUtil.fillRect(rect, ClientColors.SELECTOR_BLUE.withAlpha(100));
		} else {
			DrawUtil.fillRect(rect, ClientColors.WHITE.withAlpha(50));
		}
		GlStateManager.pushMatrix();
		GlStateManager.scalef(hud.getScale(), hud.getScale(), 1);
		s.render(AxoRenderContextImpl.getInstance());
		GlStateManager.popMatrix();
		DrawUtil.outlineRect(rect, Colors.BLACK);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		if (button == 0) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			Optional<Rectangle> pos = Optional.empty();
			if (editing == null) {
				for (KeystrokeHud.Keystroke k : hud.keystrokes) {
					pos = Optional.of(getScaledRenderPos(k));
					if (pos.get().isMouseOver(mouseX, mouseY)) {
						entry = Optional.of(k);
						break;
					}
				}
			} else {
				pos = Optional.of(getScaledRenderPos(editing));
				if (pos.get().isMouseOver(mouseX, mouseY)) {
					entry = Optional.of(editing);
				}
			}
			if (entry.isPresent()) {
				focused = entry.get();
				mouseDown = true;
				var rect = pos.get();
				offset = new DrawPosition(mouseX - rect.x(),
					mouseY - rect.y());
				updateSnapState();
			} else {
				focused = null;
			}
		} else if (button == 1 && editing == null) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			for (KeystrokeHud.Keystroke k : hud.keystrokes) {
				var pos = getScaledRenderPos(k);
				if (pos.isMouseOver(mouseX, mouseY)) {
					entry = Optional.of(k);
					break;
				}
			}
			entry.ifPresent(stroke -> minecraft.openScreen(new ConfigureKeyBindScreen(this, hud, stroke, false)));
		}
	}


	public void closeScreen() {
		minecraft.openScreen(parent);
		hud.saveKeystrokes();
	}

	@Override
	protected void keyPressed(char c, int code) {
		if (code == Keyboard.KEY_ESCAPE) {
			closeScreen();
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button) {
		if (focused != null) {
			hud.saveKeystrokes();
		}
		snap = null;
		mouseDown = false;
		focused = null;
		super.mouseReleased(mouseX, mouseY, button);
	}

	private void handleDrag(int mouseX, int mouseY) {
		if (focused != null && mouseDown) {
			focused.setX(Math.round((mouseX - offset.x()) / hud.getScale()));
			focused.setY(Math.round((mouseY - offset.y()) / hud.getScale()));
			if (snap != null) {
				Optional<Integer> snapX, snapY;
				var rect = getScaledRenderPos(focused);
				snap.setCurrent(rect);
				if ((snapX = snap.getCurrentXSnap()).isPresent()) {
					focused.setX(Math.round(snapX.get() / hud.getScale()));
				}
				if ((snapY = snap.getCurrentYSnap()).isPresent()) {
					focused.setY(Math.round(snapY.get() / hud.getScale()));
				}
			}
		}
	}

	private Rectangle getScaledRenderPos(KeystrokeHud.Keystroke stroke) {
		return stroke.getRenderPosition().scale(hud.getScale());
	}

	private List<Rectangle> getAllBounds() {
		return Stream.concat(HudManager.getInstance().getAllBounds().stream(), hud.keystrokes.stream().map(this::getScaledRenderPos)).toList();
	}

	private void updateSnapState() {
		if (HudManager.getInstance().isSnappingEnabled() && focused != null) {
			snap = new SnappingHelper(getAllBounds(), getScaledRenderPos(focused));
		} else if (snap != null) {
			snap = null;
		}
	}

	@Override
	public void removed() {
		CursorTypes.ARROW.select();
	}
}
