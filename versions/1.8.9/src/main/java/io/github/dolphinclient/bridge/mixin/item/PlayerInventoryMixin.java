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

package io.github.dolphinclient.bridge.mixin.item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import io.github.dolphinclient.bridge.impl.Bridge;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.item.AxoPlayerInventory;
import net.minecraft.entity.living.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements AxoPlayerInventory {
	@Shadow
	public abstract int getSize();

	@Shadow
	public abstract ItemStack getArmor(int par1);

	@Shadow
	public ItemStack[] items;

	@Shadow
	public abstract ItemStack getItem(int slot);

	@Shadow
	public abstract ItemStack getSelectedItem();

	@Override
	public AxoItemStack br$getMainHand() {
		return Bridge.wrapStack(getSelectedItem());
	}

	@Override
	public List<AxoItemStack> br$getItems() {
		return IntStream.range(0, getSize())
			.mapToObj(x -> Bridge.wrapStack(getItem(x)))
			.toList();
	}

	@Override
	public List<AxoItemStack> br$getArmor() {
		return IntStream.range(0, 4)
			.mapToObj(x -> Bridge.wrapStack(getArmor(x)))
			.toList();
	}

	@Override
	public List<? extends AxoItemStack> br$getNonEquipmentItems() {
		return Arrays.stream(items)
			.skip(9)
			.map(Bridge::wrapStack)
			.toList();
	}
}
