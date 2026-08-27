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

package io.github.axolotlclient.modules.blur;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.minecraft.client.render.PostChain;
import net.minecraft.client.render.shaders.Uniform;
import net.minecraft.resource.Identifier;

public class MotionBlur extends AbstractModule {

	@Getter
	private static final MotionBlur Instance = new MotionBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final FloatOption strength = new FloatOption("strength", 50F, 1F, 99F);
	public final BooleanOption inGuis = new BooleanOption("inGuis", false);
	public final OptionCategory category = OptionCategory.create("motionBlur");
	private final Identifier shaderLocation = new Identifier(DolphinClientCommon.MODID, "shaders/post/motion_blur.json");
	public PostChain shader;
	private float currentBlur;

	private int lastWidth;
	private int lastHeight;

	private static float getBlur() {
		return MotionBlur.getInstance().strength.get() / 100F;
	}

	@Override
	public void init() {
		category.add(enabled, strength, inGuis);

		DolphinClient.config().rendering.add(category);
	}

	public void onUpdate() {
		if ((shader == null || client.width != lastWidth || client.height != lastHeight) && client.height != 0
			&& client.width != 0) {
			currentBlur = 0.5f;
			try {
				shader = new PostChain(client.getTextureManager(), client.getResourceManager(),
					client.getRenderTarget(), shaderLocation);
				shader.resize(client.width, client.height);
			} catch (JsonSyntaxException | IOException e) {
				DolphinClientCommon.getInstance().getLogger().error("Could not load motion blur: ", e);
			}
		}
		if (currentBlur != getBlur()) {
			((ShaderEffectAccessor) shader).getPasses().forEach(shader -> {
				Uniform blendFactor = shader.getEffect().getUniform("BlendFactor");
				if (blendFactor != null) {
					blendFactor.set(getBlur());
				}
			});
			currentBlur = getBlur();
		}

		lastWidth = client.width;
		lastHeight = client.height;
	}
}
