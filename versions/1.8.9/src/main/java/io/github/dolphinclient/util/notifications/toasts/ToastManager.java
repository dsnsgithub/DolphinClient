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

package io.github.dolphinclient.util.notifications.toasts;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.google.common.collect.Queues;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.render.AxoWindow;
import io.github.dolphinclient.util.Util;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ToastManager {
	private static final int SLOT_COUNT = 5;
	@Getter
	final Minecraft minecraft;
	private final List<ToastInstance<?>> visibleToasts = new ArrayList<>();
	private final Deque<Toast> queued = Queues.newArrayDeque();

	public ToastManager(Minecraft minecraft) {
		this.minecraft = minecraft;
		MinecraftClientEvents.TICK_END.register(mc -> update());
	}

	public void update() {
		MutableBoolean mutableBoolean = new MutableBoolean(false);
		this.visibleToasts.removeIf(toastInstance -> {
			var vis = toastInstance.visibility;
			toastInstance.update();
			if (toastInstance.visibility != vis && mutableBoolean.isFalse()) {
				mutableBoolean.setTrue();
				toastInstance.visibility.playSound(minecraft.getSoundManager());
			}
			return toastInstance.hasFinishedRendering();
		});
		if (!this.queued.isEmpty()) {
			this.queued.removeIf(toast -> {
				var toastHeight = toast.height();
				var y = nextY();
				var wHeight = AxoWindow.getWindow().br$getScaledHeight();
				if (visibleToasts.size() < SLOT_COUNT && y <= wHeight / 2f && y + toastHeight < wHeight) {
					this.visibleToasts.add(new ToastInstance<>(toast));
					return true;
				}

				return false;
			});
		}
	}

	private int nextY() {
		return visibleToasts.stream().map(ToastManager.ToastInstance::getToast).mapToInt(Toast::height).sum();
	}

	public void render(AxoRenderContext graphics) {
		if (!this.minecraft.options.hideGui) {
			int i = (int) Util.getWindow().getScaledWidth();

			int y = 0;
			for (ToastInstance<?> toastInstance : this.visibleToasts) {
				toastInstance.y = y;
				toastInstance.render(graphics, i);
				y += toastInstance.getToast().height();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Toast> T getToast(Class<? extends T> class_, Object object) {
		for (ToastInstance<?> toastInstance : this.visibleToasts) {
			if (toastInstance != null && class_.isAssignableFrom(toastInstance.getToast().getClass()) && toastInstance.getToast().getToken().equals(object)) {
				return (T) toastInstance.getToast();
			}
		}

		for (Toast toast : this.queued) {
			if (class_.isAssignableFrom(toast.getClass()) && toast.getToken().equals(object)) {
				return (T) toast;
			}
		}

		return null;
	}

	public void clear() {
		this.visibleToasts.clear();
		this.queued.clear();
	}

	public void addToast(Toast toast) {
		this.queued.add(toast);
	}

	public double getNotificationDisplayTimeMultiplier() {
		return 1.0;//this.minecraft.options.notificationDisplayTime().get();
	}

	@Environment(EnvType.CLIENT)
	class ToastInstance<T extends Toast> {
		@Getter
		private final T toast;
		int y;
		private long animationStartTime = -1L;
		private long becameFullyVisibleAt = -1L;
		private Toast.Visibility visibility = Toast.Visibility.SHOW;
		private long fullyVisibleFor;
		private float visiblePortion;
		private boolean hasFinishedRendering;

		ToastInstance(final T toast) {
			this.toast = toast;
		}

		public boolean hasFinishedRendering() {
			return this.hasFinishedRendering;
		}

		private void calculateVisiblePortion(long l) {
			float f = MathHelper.clamp((float) (l - this.animationStartTime) / toast.dolphinclient$animationDuration(), 0.0F, 1.0F);
			f *= f;
			if (this.visibility == Toast.Visibility.HIDE) {
				this.visiblePortion = 1.0F - f;
			} else {
				this.visiblePortion = f;
			}
		}

		public void update() {
			long l = Minecraft.getTime();
			if (this.animationStartTime == -1L) {
				this.animationStartTime = l;
				this.visibility = Toast.Visibility.SHOW;
			}

			if (this.visibility == Toast.Visibility.SHOW && l - this.animationStartTime <= toast.dolphinclient$animationDuration()) {
				this.becameFullyVisibleAt = l;
			}

			this.fullyVisibleFor = l - this.becameFullyVisibleAt;
			this.calculateVisiblePortion(l);
			this.toast.update(ToastManager.this, this.fullyVisibleFor);
			Toast.Visibility visibility = this.toast.getWantedVisibility();
			if (visibility != this.visibility) {
				this.animationStartTime = l - (long) ((int) ((1.0F - this.visiblePortion) * toast.dolphinclient$animationDuration()));
				this.visibility = visibility;
			}
			boolean finished = this.hasFinishedRendering;
			this.hasFinishedRendering = this.visibility == Toast.Visibility.HIDE && l - this.animationStartTime > toast.dolphinclient$animationDuration();
			if (this.hasFinishedRendering && !finished) {
				this.toast.onFinishedRendering();
			}
		}

		public void render(AxoRenderContext graphics, int i) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float) i - (float) this.toast.width() * this.visiblePortion, y, 1000.0F);
			this.toast.render(graphics, ToastManager.this.minecraft.textRenderer, this.fullyVisibleFor);
			GlStateManager.popMatrix();
		}
	}
}
