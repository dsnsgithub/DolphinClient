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

package io.github.axolotlclient.modules.auth.skin;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.hash.Hashing;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.client.render.texture.SkinImageProcessor;
import net.minecraft.resource.Identifier;

public class SkinManager {

	private final Set<AxoIdentifier> loadedTextures = new ConcurrentSkipListSet<>(Comparator.comparing(Object::toString));

	public Skin read(Path p) {
		return read(p, true);
	}

	@SuppressWarnings("UnstableApiUsage")
	public Skin read(Path p, boolean fix) {
		boolean slim;
		String sha256;
		try {
			var in = Files.readAllBytes(p);
			sha256 = Hashing.sha256().hashBytes(in).toString();
			try (var bs = new ByteArrayInputStream(in)) {
				var img = ImageIO.read(bs);
				int height = img.getHeight();
				int width = img.getWidth();
				if (width != 64) return null;
				if (height == 32) {
					if (fix) {
						try (var out = Files.newOutputStream(p)) {
							ImageIO.write(new SkinImageProcessor().process(img), "png", out);
						}
					}
					slim = false;
				} else if (height != 64) {
					return null;
				} else {
					slim = ClientColors.ARGB.alpha(img.getRGB(50, 16)) == 0;
				}
				var metadata = Skin.LocalSkin.readMetadata(p);
				if (metadata != null && metadata.containsKey(Skin.LocalSkin.CLASSIC_METADATA_KEY)) {
					slim = !(boolean) metadata.get(Skin.LocalSkin.CLASSIC_METADATA_KEY);
				}
			}
			return new Skin.LocalSkin(!slim, p, in, sha256);
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to probe skin: ", e);
		}
		return null;
	}

	public AxoIdentifier loadSkin(Skin skin) {
		var rl = AxoIdentifier.of(DolphinClientCommon.MODID, "skins/" + skin.sha256());
		if (loadedTextures.contains(rl)) {
			return rl;
		}

		try (var bs = new ByteArrayInputStream(skin.image())) {
			var img = ImageIO.read(bs);
			var tex = new DynamicTexture(img.getWidth(), img.getHeight());
			img.getRGB(0, 0, img.getWidth(), img.getHeight(), tex.getPixels(), 0, img.getWidth());
			tex.upload();
			Minecraft.getInstance().getTextureManager().register((Identifier) rl, tex);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		loadedTextures.add(rl);
		return rl;
	}

	public AxoIdentifier loadCape(Cape cape) {
		var rl = AxoIdentifier.of(DolphinClientCommon.MODID, "capes/" + cape.id());
		if (loadedTextures.contains(rl)) {
			return rl;
		}

		try (var bs = new ByteArrayInputStream(cape.image())) {
			var img = ImageIO.read(bs);
			var tex = new DynamicTexture(img.getWidth(), img.getHeight());
			img.getRGB(0, 0, img.getWidth(), img.getHeight(), tex.getPixels(), 0, img.getWidth());
			tex.upload();
			Minecraft.getInstance().getTextureManager().register((Identifier) rl, tex);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		loadedTextures.add(rl);
		return rl;
	}

	public void releaseAll() {
		loadedTextures.forEach(id -> Minecraft.getInstance().getTextureManager().close((Identifier) id));
		loadedTextures.clear();
	}
}
