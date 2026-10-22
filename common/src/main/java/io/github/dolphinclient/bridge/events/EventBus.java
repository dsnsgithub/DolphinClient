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

package io.github.dolphinclient.bridge.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

/**
 * Probably an overengineered event bus.
 *
 * @param <T>
 */
public final class EventBus<T> {
	private final Function<Iterable<T>, T> combiner;
	private final Set<T> events = new HashSet<>();
	@Nullable
	private T cachedInvoker;

	public static <T> EventBus<Runnable> broadcast0() {
		return new EventBus<>(input -> () -> {
			for (Runnable op : input) {
				op.run();
			}
		});
	}

	public static <T> EventBus<Consumer<T>> broadcast1() {
		return new EventBus<>(input -> val -> {
			for (Consumer<T> op : input) {
				op.accept(val);
			}
		});
	}

	public static <T, U> EventBus<BiConsumer<T, U>> broadcast2() {
		return new EventBus<>(input -> (a, b) -> {
			for (BiConsumer<T, U> op : input) {
				op.accept(a, b);
			}
		});
	}

	public EventBus(Function<Iterable<T>, T> combiner) {
		this.combiner = combiner;
	}

	public T invoker() {
		if (cachedInvoker == null) {
			// copy to arraylist for iteration performance
			cachedInvoker = combiner.apply(new ArrayList<>(events));
		}

		return cachedInvoker;
	}

	public T register(T event) {
		events.add(event);
		cachedInvoker = null;
		return event;
	}

	public boolean unregister(T event) {
		boolean res = events.remove(event);
		cachedInvoker = null;
		return res;
	}
}
