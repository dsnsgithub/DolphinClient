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

package io.github.dolphinclient.mixin;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.dolphinclient.DolphinClient;
import io.github.dolphinclient.bridge.AxoPerspective;
import io.github.dolphinclient.modules.hypixel.LevelHead;
import io.github.dolphinclient.modules.hypixel.LevelHeadPlacement;
import io.github.dolphinclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.dolphinclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

	@Shadow
	@Final
	protected EntityRenderDispatcher dispatcher;

	@Inject(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 1))
	private void dolphinclient$correctNameplateRotation(Entity entity, String string, double d, double e, double f, int i, CallbackInfo ci) {
		if (Minecraft.getInstance().options.perspective == AxoPerspective.THIRD_PERSON_FRONT.ordinal()) {
			GlStateManager.rotatef(-this.dispatcher.cameraPitch * 2, 1.0F, 0.0F, 0.0F);
		}
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;draw(Ljava/lang/String;III)I", ordinal = 1))
	public int dolphinclient$forceShadows(TextRenderer instance, String string, int x, int y, int color, Operation<Integer> original, Entity entity) {
		if (DolphinClient.config().useShadows.get() && !entity.isSneaking()) {
			return instance.draw(string, x, y, color, true);
		}
		return original.call(instance, string, x, y, color);
	}

	@Inject(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;draw(Ljava/lang/String;III)I", ordinal = 1))
	public void dolphinclient$addLevel(T entity, String string, double d, double e, double f, int i, CallbackInfo ci) {
		if (entity instanceof ClientPlayerEntity player && string.equals(entity.getDisplayName().getFormattedString())) {
			if (Util.currentServerAddressContains("hypixel.net")) {
				if (BedwarsMod.getInstance().isEnabled() &&
					BedwarsMod.getInstance().inGame() &&
					BedwarsMod.getInstance().bedwarsLevelHead.get()) {
					String levelhead = BedwarsMod.getInstance().getGame().get().getLevelHead(player);
					if (levelhead != null) {
						dolphinclient$drawLevelHead(player, string, levelhead);
					}
				} else if (LevelHead.getInstance().enabled.get()) {
					String text = LevelHead.getInstance().getDisplayString(entity.getUuid().toString());

					dolphinclient$drawLevelHead(player, string, text);
				}
			}
		}
	}

	@Unique
	private void dolphinclient$drawLevelHead(Entity entity, String name, String text) {
		TextRenderer textRenderer = Minecraft.getInstance().textRenderer;

		float x = textRenderer.getWidth(text) / 2F;
		int above = "deadmau5".equals(name) ? 20 : 10;
		if (LevelHead.getInstance().background.get()) {
			above += 2;
		}
		int y = -(above + dolphinclient$tagsAbove(entity, textRenderer.fontHeight, above));

		if (LevelHead.getInstance().background.get()) {
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuffer();
			GlStateManager.disableTexture();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(-x - 1, -1 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).nextVertex();
			bufferBuilder.vertex(-x - 1, 8 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).nextVertex();
			bufferBuilder.vertex(x + 1, 8 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).nextVertex();
			bufferBuilder.vertex(x + 1, -1 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).nextVertex();
			tesselator.end();
			GlStateManager.enableTexture();
		}

		textRenderer.draw(text, -x, y, LevelHead.getInstance().textColor.get().toInt(),
			DolphinClient.config().useShadows.get());
	}

	/**
	 * Pixels to lift the level so it clears the below-name scoreboard row and holograms
	 * parked just above the username. Vanilla only draws that scoreboard row inside 10 blocks.
	 */
	@Unique
	private int dolphinclient$tagsAbove(Entity entity, int fontHeight, int baseAbove) {
		boolean belowName = false;
		if (entity instanceof PlayerEntity player && !player.isSneaking() && this.dispatcher.camera != null
			&& player.squaredDistanceTo(this.dispatcher.camera) < 100.0) {
			Scoreboard scoreboard = player.getScoreboard();
			belowName = scoreboard != null && scoreboard.getDisplayObjective(2) != null;
		}

		double nameAnchor = entity.y + entity.height + 0.5;
		double[] tags = null;
		if (entity.world != null) {
			Box box = new Box(entity.x - 1.0, nameAnchor, entity.z - 1.0, entity.x + 1.0, nameAnchor + 3.0, entity.z + 1.0);
			List<Double> above = new ArrayList<>();
			for (Entity other : entity.world.getEntities(entity, box)) {
				if (other == entity || !other.hasCustomName() || !other.isCustomNameVisible()) {
					continue;
				}
				double dx = other.x - entity.x;
				double dz = other.z - entity.z;
				if (dx * dx + dz * dz > 0.36) {
					continue;
				}
				double anchor = other.y + other.height + 0.5;
				if (anchor > nameAnchor + 0.05) {
					above.add(anchor);
				}
			}
			if (!above.isEmpty()) {
				tags = new double[above.size()];
				for (int i = 0; i < above.size(); i++) {
					tags[i] = above.get(i);
				}
			}
		}
		return LevelHeadPlacement.extraPixels(fontHeight, belowName, nameAnchor, tags, baseAbove);
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;begin(ILnet/minecraft/client/render/vertex/VertexFormat;)V"))
	private boolean disableNameTagBackground(BufferBuilder instance, int drawMode, VertexFormat format) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;nextVertex()V"))
	private boolean disableNameTagBackground$2(BufferBuilder instance) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapWithCondition(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/Tesselator;end()V"))
	private boolean disableNameTagBackground$3(Tesselator instance) {
		return DolphinClient.config().nametagBackground.get();
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;vertex(DDD)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	private BufferBuilder disableNameTagBackground(BufferBuilder instance, double x, double y, double z, Operation<BufferBuilder> original) {
		if (DolphinClient.config().nametagBackground.get()) {
			return original.call(instance, x, y, z);
		}
		return instance;
	}

	@WrapOperation(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;color(FFFF)Lnet/minecraft/client/render/vertex/BufferBuilder;"))
	private BufferBuilder disableNameTagBackground$2(BufferBuilder instance, float r, float g, float b, float a, Operation<BufferBuilder> original) {
		if (DolphinClient.config().nametagBackground.get()) {
			return original.call(instance, r, g, b, a);
		}
		return instance;
	}
}
