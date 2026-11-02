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

package io.github.dolphinclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import io.github.dolphinclient.bridge.AxoMinecraftClient;
import io.github.dolphinclient.bridge.Platform;
import io.github.dolphinclient.bridge.entity.AxoPlayer;
import io.github.dolphinclient.bridge.item.AxoItem;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.item.AxoPlayerInventory;

public class ItemUtil {
	public static int getTotal(AxoPlayerInventory inventory, AxoItem item) {
		return inventory.br$getItems().stream()
			.filter(x -> x.br$getItem() == item)
			.mapToInt(AxoItemStack::br$getCount)
			.sum();
	}

	public static int getTotal(AxoMinecraftClient inventory, AxoItem item) {
		AxoPlayer player = inventory.br$getPlayer();

		if (player == null) {
			return 0;
		}

		return getTotal(player.br$getInventory(), item);
	}

	public static Optional<ItemStorage> getItemFromItem(AxoItemStack item, List<ItemStorage> list) {
		AxoItemStack compare = item.br$copy();
		compare.br$setCount(1);
		for (ItemUtil.ItemStorage storage : list) {
			if (isEqual(storage.stack, compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	private static boolean isEqual(AxoItemStack stack, AxoItemStack compare) {
		return stack != null && compare != null && stack.br$getItem() == compare.br$getItem();
	}

	public static ArrayList<TimedItemStorage> removeOld(List<ItemUtil.TimedItemStorage> list, int time) {
		ArrayList<ItemUtil.TimedItemStorage> stored = new ArrayList<>();
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.getPassedTime() <= time) {
				stored.add(storage);
			}
		}
		return stored;
	}

	public static Optional<ItemUtil.TimedItemStorage> getTimedItemFromItem(AxoItemStack item,
																		   List<ItemUtil.TimedItemStorage> list) {
		AxoItemStack compare = item.br$copy();
		compare.br$setCount(1);
		for (ItemUtil.TimedItemStorage storage : list) {
			if (isEqual(storage.stack, compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	public static List<ItemStorage> storageFromItem(List<AxoItemStack> items) {
		ArrayList<ItemStorage> storage = new ArrayList<>();
		for (AxoItemStack item : items) {
			if (item == null || item.br$isEmpty()) {
				continue;
			}
			Optional<ItemStorage> s = getItemFromItem(item, storage);
			if (s.isPresent()) {
				ItemUtil.ItemStorage store = s.get();
				store.incrementTimes(item.br$getCount());
			} else {
				storage.add(new ItemUtil.ItemStorage(item, item.br$getCount()));
			}
		}
		return storage;
	}

	public static List<ItemUtil.TimedItemStorage> untimedToTimed(List<ItemStorage> list) {
		ArrayList<TimedItemStorage> timed = new ArrayList<>();
		for (ItemStorage stack : list) {
			timed.add(stack.timed());
		}
		return timed;
	}

	public static List<AxoItemStack> getItems(AxoMinecraftClient client) {
		Preconditions.checkArgument(client.br$getPlayer() != null);
		return new ArrayList<>(client.br$getPlayer().br$getInventory().br$getItems());
	}


	/**
	 * Compares two ItemStorage Lists.
	 * If list1.get(1) is 10, and list2 is 5, it will return 5.
	 * Will return nothing if negative...
	 *
	 * @param list1 one to be based off of
	 * @param list2 one to compare to
	 * @return the item storage
	 */
	public static List<ItemStorage> compare(List<ItemStorage> list1, List<ItemStorage> list2) {
		ArrayList<ItemStorage> list = new ArrayList<>();
		for (ItemStorage current : list1) {
			Optional<ItemStorage> optional = getItemFromItem(current.stack, list2);
			if (optional.isPresent()) {
				ItemStorage other = optional.get();
				if (current.times - other.times <= 0) {
					continue;
				}
				list.add(new ItemStorage(other.stack.br$copy(), current.times - other.times));
			} else {
				list.add(current.copy());
			}
		}
		return list;
	}

	public static class ItemStorage {

		public final AxoItemStack stack;
		public int times;

		public ItemStorage(AxoItemStack stack, int times) {
			AxoItemStack copy = stack.br$copy();
			copy.br$setCount(1);
			this.stack = copy;
			this.times = times;
		}

		public void incrementTimes(int num) {
			times = times + num;
		}

		public ItemStorage copy() {
			return new ItemStorage(stack.br$copy(), times);
		}

		public TimedItemStorage timed() {
			return new TimedItemStorage(stack.br$copy(), times);
		}
	}

	public static class TimedItemStorage extends ItemStorage {

		public float start;

		public TimedItemStorage(AxoItemStack stack, int times) {
			super(stack, times);
			this.start = Platform.getMeasuringTimeMs();
		}

		public float getPassedTime() {
			return Platform.getMeasuringTimeMs() - start;
		}

		@Override
		public void incrementTimes(int num) {
			super.incrementTimes(num);
			refresh();
		}

		public void refresh() {
			start = Platform.getMeasuringTimeMs();
		}
	}
}
