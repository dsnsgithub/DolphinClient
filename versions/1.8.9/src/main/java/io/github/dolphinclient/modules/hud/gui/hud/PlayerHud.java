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

package io.github.dolphinclient.modules.hud.gui.hud;

import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.events.types.PlayerDirectionChangeEvent;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */

public class PlayerHud extends PlayerHudCommon {

	public static final Identifier ID = new Identifier("kronhud", "playerhud");
	@Getter
	private static boolean currentlyRendering = false;

	public PlayerHud() {
		super();
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void onPlayerDirectionChange(PlayerDirectionChangeEvent event) {
		yawOffset += (event.yaw() - event.prevYaw()) / 2;
	}

	@Override
	public void tick() {
		super.tick();
		yOffset *= .8f;
	}

	@Override
	protected void renderPlayer(AxoRenderContext ctx, boolean placeholder, double x, double y, float delta) {
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();

		var client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		float lerpY = (lastYOffset + ((yOffset - lastYOffset) * delta));

		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translated(
			x + getContentWidth() / 2f * getScale(),
			y + getContentHeight() * getScale() * client.player.height / 2f - lerpY,
			1050);
		GlStateManager.scalef(1, 1, -1);

		GlStateManager.translatef(0, 0, 1000);
		float scale = getScale() * 40;
		GlStateManager.scalef(scale, scale, scale);

		GlStateManager.rotatef(180, 0, 0, 1);

		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.yaw;
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}

		// Save these to set them back later
		float pastYaw = client.player.yaw;
		float pastBodyYaw = client.player.bodyYaw;
		float pastHeadYaw = client.player.headYaw;
		float pastPrevHeadYaw = client.player.lastHeadYaw;
		float pastPrevYaw = client.player.lastYaw;

		client.player.headYaw = client.player.yaw;
		client.player.lastHeadYaw = client.player.yaw;

		GlStateManager.rotatef(deltaYaw - 180 + rotation.get().floatValue(), 0, 1, 0);
		Lighting.turnOn();
		EntityRenderDispatcher renderer = client.getEntityRenderDispatcher();
		renderer.setCameraYaw(180);
		renderer.cameraPitch = 0;
		renderer.setRenderShadow(false);

		currentlyRendering = true;
		renderer.render(client.player, 0.0, 0.0, 0.0, 0, delta);
		currentlyRendering = false;

		renderer.setRenderShadow(true);
		GlStateManager.popMatrix();

		client.player.yaw = pastYaw;
		client.player.headYaw = pastHeadYaw;
		client.player.lastHeadYaw = pastPrevHeadYaw;
		client.player.lastYaw = pastPrevYaw;
		client.player.bodyYaw = pastBodyYaw;

		Lighting.turnOff();
		GlStateManager.disableRescaleNormal();
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		GlStateManager.disableTexture();
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
	}

	@Override
	protected boolean isPerformingAction() {
		// inspired by tr7zw's mod
		LocalClientPlayerEntity player = Minecraft.getInstance().player;
		return player.isSneaking() || player.isSprinting() || player.abilities.flying
			|| player.isSubmergedIn(Material.WATER) || player.isRidingRideableMob() || player.isUsingItem()
			|| player.armSwinging || player.damagedTimer > 0 || player.isOnFire();
	}
}
