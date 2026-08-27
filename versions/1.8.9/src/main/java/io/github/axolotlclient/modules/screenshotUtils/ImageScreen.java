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

package io.github.axolotlclient.modules.screenshotUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;

public class ImageScreen extends Screen {

	private final Screen parent;
	private final ImageInstance image;
	private final boolean freeOnClose;
	private final boolean isRemote;
	private final String title;
	private final String uploader;

	static Screen create(Screen parent, CompletableFuture<ImageInstance> future, boolean freeOnClose) {
		if (future.isDone()) {
			if (future.join() != null) {
				return new ImageScreen(parent, future.join(), freeOnClose);
			} else {
				return parent;
			}
		}
		return new LoadingImageScreen(parent, future.thenAccept(i -> {
			if (i != null) {
				Minecraft.getInstance().executeTask(() -> Minecraft.getInstance().openScreen(new ImageScreen(parent, i, freeOnClose)));
			} else {
				Minecraft.getInstance().executeTask(() -> Minecraft.getInstance().openScreen(parent));
			}
		}), freeOnClose);
	}

	private ImageScreen(Screen parent, ImageInstance instance, boolean freeOnClose) {
		super();
		this.title = instance.filename();
		this.parent = parent;
		this.image = instance;
		this.freeOnClose = freeOnClose;
		this.isRemote = image instanceof ImageInstance.Remote;
		this.uploader = isRemote ? ((ImageInstance.Remote) image).uploader() : null;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);
		if (isRemote) {
			ImageInstance.Remote r = (ImageInstance.Remote) image;
			drawCenteredString(textRenderer, title, width / 2, 38 / 2 - textRenderer.fontHeight - 2, -1);
			drawCenteredString(textRenderer,
				I18n.translate("gallery.image.upload_details", uploader,
					r.sharedAt().atZone(ZoneId.systemDefault()).format(DolphinClientCommon.getInstance().getConfig().getDateTimeFormatter())),
				width / 2, 38 / 2 + 2, -1);
		} else {
			drawCenteredString(textRenderer, title, width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
		}
	}

	@Override
	public void init() {

		int contentsHeight = height - ((isRemote ? 38 : 33) + 33);
		int buttonWidth = 75;
		double imgAspectRatio = image.image().getWidth() / (double) image.image().getHeight();
		int imageWidth = Math.min((int) (contentsHeight * imgAspectRatio), width - buttonWidth - 4 - 20);
		int imageHeight = (int) (imageWidth / imgAspectRatio);


		var element = new ImageElement(imageWidth, imageHeight);
		buttons.add(element);
		if (width / 2 > (imageWidth / 2) + buttonWidth + 4) {
			element.setPosition(width / 2 - imageWidth / 2, 36);
		} else {
			element.setPosition(width / 2 - imageWidth / 2 - buttonWidth / 2 - 2, 36);
		}
		int actionX = element.x + imageWidth + 4;
		var actions = new ArrayList<ButtonWidget>();
		if (image instanceof ImageInstance.Local) {
			actions.add(new ButtonWidget(1, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy")));
			actions.add(new ButtonWidget(2, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.open.external")));
			actions.add(new ButtonWidget(8, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.delete")));
		}
		if (image instanceof ImageInstance.Remote) {
			if (!(image instanceof ImageInstance.Local)) {
				actions.add(new ButtonWidget(3, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.save")));
				actions.add(new ButtonWidget(4, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy")));
			}
			actions.add(new ButtonWidget(5, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.open.external.browser")));
			actions.add(new ButtonWidget(6, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy_url")));
		}
		actions.add(new ButtonWidget(9, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.crop")));
		int actionY = element.y;
		for (ButtonWidget w : actions) {
			w.x = actionX;
			w.y = actionY;
			buttons.add(w);
			actionY += 4 + 20;
		}

		buttons.add(new ButtonWidget(7, width / 2 - 75, height - 33 + 33 / 2 - 10, 150, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget b) {
		switch (b.id) {
			case 1 -> ScreenshotCopying.copy(((ImageInstance.Local) image).location());
			case 2 -> OSUtil.getOS().open(((ImageInstance.Local) image).location().toUri());
			case 3 -> {
				ImageInstance.Remote remote = (ImageInstance.Remote) image;
				b.active = false;
				try {
					Path out = saveSharedImage(remote);
					minecraft.openScreen(new ImageScreen(parent, remote.toShared(out), freeOnClose));
				} catch (IOException e) {
					Notifications.getInstance().addStatus("gallery.image.save.failure", "gallery.image.save.failure.description", e.getMessage());
					DolphinClientCommon.getInstance().getLogger().warn("Failed to save shared image!", e);
				}
			}
			case 4 -> {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					ImageIO.write(image.image(), "png", baos);
					ScreenshotCopying.copy(baos.toByteArray());
				} catch (IOException e) {
					Notifications.getInstance().addStatus("gallery.image.copy.failure", "gallery.image.copy.failure.description", e.getMessage());
					DolphinClientCommon.getInstance().getLogger().warn("Failed to copy shared image!", e);
				}
			}
			case 5 -> OSUtil.getOS().open(((ImageInstance.Remote) image).url());
			case 6 -> setClipboard(((ImageInstance.Remote) image).url());
			case 7 -> {
				if (freeOnClose) {
					minecraft.getTextureManager().close(image.id());
				}
				minecraft.openScreen(parent);
			}
			case 8 -> {
				var loc = ((ImageInstance.Local) image).location();
				try {
					Files.delete(loc);
					Notifications.getInstance().addStatus("gallery.image.delete.success", "gallery.image.delete.success.description", loc);
				} catch (IOException e) {
					Notifications.getInstance().addStatus("gallery.image.delete.failure", "gallery.image.delete.failure.description", loc);
					DolphinClientCommon.getInstance().getLogger().warn("Failed to delete image!", e);
				}
			}
			case 9 -> minecraft.openScreen(new CropImageScreen(this, image));
		}
	}

	private Path saveSharedImage(ImageInstance.Remote remote) throws IOException {
		Path out = GalleryScreen.SCREENSHOTS_DIR.resolve("shared")
			.resolve(remote.uploader())
			.resolve(remote.filename());
		Path infoJson = out.resolveSibling(remote.filename() + ".json");

		Files.createDirectories(out.getParent());
		try (OutputStream s = Files.newOutputStream(out)) {
			ImageIO.write(remote.image(), "png", s);
		}
		Object json = Map.of("uploader", remote.uploader(), "shared_at", remote.sharedAt());
		Files.writeString(infoJson, GsonHelper.GSON.toJson(json));
		return out;
	}

	private class ImageElement extends ButtonWidget {

		public ImageElement(int width, int height) {
			super(99, 0, 0, width, height, "");
			active = false;
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			GlStateManager.color4f(1, 1, 1, 1);
			client.getTextureManager().bind(image.id());
			drawTexture(x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
		}

		private int getHeight() {
			return height;
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
