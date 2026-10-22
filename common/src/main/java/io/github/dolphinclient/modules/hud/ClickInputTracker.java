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

package io.github.dolphinclient.modules.hud;

import io.github.dolphinclient.bridge.Platform;
import io.github.dolphinclient.bridge.events.Events;
import io.github.dolphinclient.bridge.key.AxoKeys;
import io.github.dolphinclient.modules.AbstractCommonModule;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;

public class ClickInputTracker extends AbstractCommonModule {
	@Getter
	private static final ClickInputTracker instance = new ClickInputTracker();

	public final ClickList leftMouse = new ClickList();
	public final ClickList leftBind = new ClickList();
	public final ClickList rightMouse = new ClickList();
	public final ClickList rightBind = new ClickList();

	@Override
	public void init() {
		Events.KEY_INPUT.register(key -> {
			if (key.equals(client.br$getGameOptions().br$getAttackKey().br$getBoundKey())) {
				leftBind.click();
			} else if (key.equals(client.br$getGameOptions().br$getUseKey().br$getBoundKey())) {
				rightBind.click();
			}

			if (key.equals(AxoKeys.MOUSE_LEFT)) {
				leftMouse.click();
			} else if (key.equals(AxoKeys.MOUSE_RIGHT)) {
				rightMouse.click();
			}
		});
	}

	@Override
	public void tick() {
		leftMouse.update();
		leftBind.update();
		rightMouse.update();
		rightBind.update();
	}

	public static class ClickList {
		private final LongSet clicks;

		public ClickList() {
			clicks = new LongOpenHashSet();
		}

		public void update() {
			clicks.removeIf((long click) -> Platform.getMeasuringTimeMs() - click > 1000);
		}

		public void click() {
			clicks.add(Platform.getMeasuringTimeMs());
		}

		public int clicks() {
			return clicks.size();
		}
	}
}
