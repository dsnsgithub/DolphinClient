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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.bridge.impl.AxoRenderContextImpl;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.client.render.texture.TextureUtil;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.resource.loader.api.resource.pack.ResourcePack;
import net.ornithemc.osl.resource.loader.api.resource.repository.ResourcePackRepository;

public class PackDisplayHud extends TextHudEntry {

	public static final Identifier ID = new Identifier(DolphinClientCommon.MODID, "packdisplayhud");

	private final List<PackWidget> widgets = new ArrayList<>();
	private final List<ResourcePack> packs = new ArrayList<>();
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private PackWidget placeholder;

	public PackDisplayHud() {
		super(120, 18, true);
	}

	public void setPacks(Collection<ResourcePack> packs) {
		placeholder = null;
		widgets.clear();
		this.packs.clear();
		this.packs.addAll(packs.stream().toList());
	}

	@Override
	public void renderComponent(AxoRenderContext context, float f) {
		DrawPosition pos = getContentPos();

		if (widgets.isEmpty())
			init();

		int y = pos.y() + 1;
		for (int i = widgets.size() - 1; i >= 0; i--) {
			widgets.get(i).render(pos.x + 1, y);
			y += 17;
		}
		if (y - pos.y() != getContentHeight()) {
			setContentHeight(y - pos.y());
			onBoundsUpdate();
		}
	}

	@Override
	public void init() {
		packs.forEach(pack -> {
			try {
				if (pack.hasResource(ResourcePack.ICON_FILE)) {
					if (packs.size() == 1) {
						widgets.add(new PackWidget(pack));
					} else if (!pack.getName().equalsIgnoreCase("Default")) {
						widgets.add(new PackWidget(pack));
					}
				}
			} catch (Exception ignored) {
			}
		});

		AtomicInteger w = new AtomicInteger(20);
		widgets.forEach(packWidget -> {
			int textW = Minecraft.getInstance().textRenderer.getWidth(packWidget.getName()) + 20;
			if (textW > w.get())
				w.set(textW);
		});
		setContentWidth(w.get());

		setContentHeight(widgets.size() * 17 + 1);
		onBoundsUpdate();
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		boolean updateBounds = false;
		if (getContentHeight() < 18) {
			setContentHeight(18);
			updateBounds = true;
		}
		if (getContentWidth() < 56) {
			setContentWidth(56);
			updateBounds = true;
		}
		if (updateBounds) {
			onBoundsUpdate();
		}
		if (placeholder == null) {
			placeholder = new PackWidget(ResourcePackRepository.clientPackSource().getDefaultResourcePack());
		}
		placeholder.render(getContentPos().x + 1, getContentPos().y + 1);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(iconsOnly);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	private class PackWidget {

		@Getter
		private final String name;
		private int texture;

		public PackWidget(ResourcePack pack) {
			this.name = pack.getName();
			try {
				this.texture = new DynamicTexture(TextureUtil.readImage(pack.getResource(ResourcePack.ICON_FILE))).getGlId();
			} catch (Exception e) {
				DolphinClientCommon.getInstance().getLogger().warn("Pack " + pack.getName()
					+ " somehow threw an error! Please investigate... Does it have an icon?", e);
			}
		}

		public void render(int x, int y) {
			if (!iconsOnly.get()) {
				GlStateManager.color4f(1, 1, 1, 1F);
				GlStateManager.bindTexture(texture);
				GuiElement.drawTexture(x, y, 0, 0, 16, 16, 16, 16);
			}
			AxoRenderContextImpl.getInstance().br$drawString(name, x + 18, y + 16 / 2 - 9 / 2, textColor.get().toInt(), shadow.get());
		}
	}
}
