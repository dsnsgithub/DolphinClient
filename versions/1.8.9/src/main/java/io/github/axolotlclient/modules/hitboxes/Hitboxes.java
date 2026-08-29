/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.axolotlclient.modules.hitboxes;

import java.util.EnumMap;
import java.util.Map;

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FishingBobberEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.mob.monster.Monster;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ThrownEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Hitboxes extends AbstractModule {

	@Getter
	private static final Hitboxes instance = new Hitboxes();

	private final BooleanOption enabled = new BooleanOption("enabled", false);
	private final IntegerOption lineWidth = new IntegerOption("hitboxes.line_width", 2, 1, 7);
	private final BooleanOption showLookVector = new BooleanOption("hitboxes.show_look_vector", true);
	private final BooleanOption showEyeHeight = new BooleanOption("hitboxes.show_eye_height", true);
	private final BooleanOption showInvisible = new BooleanOption("hitboxes.show_invisible", false);

	private final OptionCategory category = OptionCategory.create("hitboxes");
	private final Map<HitboxType, TypeSettings> types = new EnumMap<>(HitboxType.class);

	@Override
	public void init() {
		category.add(enabled, lineWidth, showLookVector, showEyeHeight, showInvisible);

		for (HitboxType type : HitboxType.values()) {
			OptionCategory group = OptionCategory.create(type.getTranslationKey());
			TypeSettings settings = new TypeSettings(
				new BooleanOption("hitboxes.show", true),
				new ColorOption("color", new Color(type.getDefaultColor()))
			);
			group.add(settings.show, settings.color);
			category.add(group);
			types.put(type, settings);
		}

		DolphinClient.config().addCategory(category);

		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "toggle_hitboxes").br$registerOnConsumeClick(() -> {
			enabled.toggle();
			DolphinClientCommon.getInstance().saveConfig();
		});
	}

	public boolean isEnabled() {
		return enabled.get();
	}

	public boolean shouldRender(Entity entity) {
		if (!enabled.get()) {
			return false;
		}
		if (entity.isInvisible() && !showInvisible.get()) {
			return false;
		}
		return types.get(typeOf(entity)).show.get();
	}

	public void render(Entity entity, double dx, double dy, double dz, float tickDelta) {
		TypeSettings settings = types.get(typeOf(entity));
		if (!settings.show.get()) {
			return;
		}

		Color color = settings.color.get();
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();

		GlStateManager.depthMask(false);
		GlStateManager.disableTexture();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		if (a < 255) {
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		} else {
			GlStateManager.disableBlend();
		}
		GL11.glLineWidth(lineWidth.get());

		Box shape = entity.getShape();
		WorldRenderer.renderOutlineShape(new Box(
			shape.minX - entity.x + dx,
			shape.minY - entity.y + dy,
			shape.minZ - entity.z + dz,
			shape.maxX - entity.x + dx,
			shape.maxY - entity.y + dy,
			shape.maxZ - entity.z + dz
		), r, g, b, a);

		if (showEyeHeight.get() && entity instanceof LivingEntity) {
			float halfWidth = entity.width / 2.0F;
			double eyeY = dy + entity.getEyeHeight();
			WorldRenderer.renderOutlineShape(new Box(
				dx - halfWidth,
				eyeY - 0.009999999776482582D,
				dz - halfWidth,
				dx + halfWidth,
				eyeY + 0.009999999776482582D,
				dz + halfWidth
			), 255, 0, 0, a);
		}

		if (showLookVector.get()) {
			Vec3d look = entity.getRotationVec(tickDelta);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer = tesselator.getBuffer();
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
			double eyeY = dy + entity.getEyeHeight();
			buffer.vertex(dx, eyeY, dz).color(0, 0, 255, a).nextVertex();
			buffer.vertex(dx + look.x * 2.0D, eyeY + look.y * 2.0D, dz + look.z * 2.0D).color(0, 0, 255, a).nextVertex();
			tesselator.end();
		}

		GL11.glLineWidth(1.0F);
		GlStateManager.enableTexture();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	HitboxType typeOf(Entity entity) {
		boolean self = entity == Minecraft.getInstance().player;
		boolean player = entity instanceof PlayerEntity;
		boolean arrow = entity instanceof ArrowEntity;
		boolean projectile = entity instanceof ThrownEntity
			|| entity instanceof ProjectileEntity
			|| entity instanceof FishingBobberEntity;
		boolean item = entity instanceof ItemEntity;
		boolean hostile = entity instanceof Monster;
		boolean living = entity instanceof LivingEntity
			&& !(entity instanceof PlayerEntity)
			&& !(entity instanceof ArmorStandEntity);
		return HitboxType.classify(self, player, arrow, projectile, item, hostile, living);
	}

	private record TypeSettings(BooleanOption show, ColorOption color) {
	}
}
