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

import com.google.common.base.Preconditions;
import io.github.dolphinclient.bridge.item.AxoEnchant;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.item.AxoItems;
import io.github.dolphinclient.bridge.util.AxoText;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements AxoItemStack {
	@Shadow
	public abstract Item getItem();

	@Shadow
	public int size;

	@Shadow
	public abstract ItemStack copy();

	@Shadow
	public abstract void setItem(Item par1);

	@Shadow
	public abstract int getDamage();

	@Shadow
	public abstract int getMaxDamage();

	@Shadow
	public abstract NbtList getEnchantments();

	@Shadow
	public abstract void addEnchantment(Enchantment enchantment, int level);

	@Shadow
	public abstract String getHoverName();

	@Override
	public AxoItem br$getItem() {
		if (size == 0) {
			return AxoItems.AIR;
		}

		return getItem();
	}

	@Override
	public AxoItemStack br$copy() {
		return copy();
	}

	@Override
	public void br$setCount(int count) {
		size = count;
	}

	@Override
	public int br$getCount() {
		return size;
	}

	@Override
	public int br$getDamage() {
		return getDamage();
	}


	@Override
	public int br$getMaxDamage() {
		return getMaxDamage();
	}

	@Unique
	@Nullable
	private NbtCompound dolphinclient$getEnchantment(int id) {
		final var enchants = getEnchantments();
		if (enchants == null) {
			return null;
		}

		for (int i = 0; i < enchants.size(); i++) {
			if (enchants.getCompound(i).getShort("id") == id) {
				return enchants.getCompound(i);
			}
		}

		return null;
	}

	@Override
	public int br$getEnchantment(AxoEnchant enchant) {
		Preconditions.checkArgument(enchant != null, "enchant != null");
		final var data = dolphinclient$getEnchantment(((Enchantment) enchant).id);
		return data == null ? 0 : data.getShort("lvl");
	}

	@Override
	public void br$setEnchantment(AxoEnchant enchant, int level) {
		Preconditions.checkArgument(enchant != null, "enchant != null");
		Preconditions.checkArgument(level > 1, "level > 1");

		int id = ((Enchantment) enchant).id;
		final var data = dolphinclient$getEnchantment(id);

		if (data == null) {
			addEnchantment(Enchantment.byId(id), level);
		} else {
			data.putShort("lvl", (short) level);
		}
	}

	@Override
	public void br$removeEnchantment(AxoEnchant enchant) {
		final var enchants = getEnchantments();
		if (enchants == null) {
			return;
		}

		int id = ((Enchantment) enchant).id;

		for (int i = 0; i < enchants.size(); i++) {
			if (enchants.getCompound(i).getShort("id") == id) {
				enchants.removeElement(i);
				break;
			}
		}
	}

	@Override
	public AxoText br$getHoverName() {
		return AxoText.literal(getHoverName());
	}
}
