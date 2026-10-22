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

package io.github.dolphinclient.bridge.impl;

import io.github.dolphinclient.bridge.item.AxoEnchant;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.util.AxoText;

public class AirItemStackImpl implements AxoItemStack {
	private static final AirItemStackImpl INSTANCE = new AirItemStackImpl();

	public static AirItemStackImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public AxoItem br$getItem() {
		return AirItemImpl.getInstance();
	}

	@Override
	public AxoItemStack br$copy() {
		return this;
	}

	@Override
	public void br$setCount(int count) {

	}

	@Override
	public int br$getCount() {
		return 0;
	}

	@Override
	public int br$getDamage() {
		return 0;
	}

	@Override
	public int br$getMaxDamage() {
		return 0;
	}

	@Override
	public int br$getEnchantment(AxoEnchant enchant) {
		return 0;
	}

	@Override
	public void br$setEnchantment(AxoEnchant enchant, int level) {

	}

	@Override
	public void br$removeEnchantment(AxoEnchant enchant) {

	}

	@Override
	public AxoText br$getHoverName() {
		return AxoText.empty();
	}
}
