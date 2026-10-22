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
package io.github.dolphinclient.util;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public final class CachedAPI<K, V> {
	public static final class ApiResult<T> {
		private static final ApiResult<?> PENDING_INSTANCE = new ApiResult<>(null);
		private static final ApiResult<?> ERROR_INSTANCE = new ApiResult<>(null);

		private final Object value;

		private ApiResult(Object value) {
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		public static <T> ApiResult<T> pending() {
			return (ApiResult<T>) PENDING_INSTANCE;
		}

		@SuppressWarnings("unchecked")
		public static <T> ApiResult<T> error() {
			return (ApiResult<T>) ERROR_INSTANCE;
		}

		public static <T> ApiResult<T> of(T value) {
			return new ApiResult<>(value);
		}

		@SuppressWarnings("unchecked")
		public T get() {
			if (this == PENDING_INSTANCE || this == ERROR_INSTANCE) {
				throw new NoSuchElementException("no value present");
			}

			return (T) value;
		}

		public boolean isPending() {
			return this == PENDING_INSTANCE;
		}

		public boolean isError() {
			return this == ERROR_INSTANCE;
		}

		public boolean hasValue() {
			return !isPending() && !isError();
		}

		public <U> U map(Supplier<U> onPending, Supplier<U> onError, Function<T, U> mapper) {
			if (this == PENDING_INSTANCE) {
				return onPending.get();
			}

			if (this == ERROR_INSTANCE) {
				return onError.get();
			}

			return mapper.apply(get());
		}

		public Optional<T> asOptional() {
			return map(Optional::empty, Optional::empty, Optional::of);
		}
	}

	public interface APIHandler<K, V> {
		CompletableFuture<Optional<V>> makeRequest(K key);
	}

	private final LoadingCache<K, CompletableFuture<Optional<V>>> cache;
	private final boolean retainFails;

	public CachedAPI(APIHandler<K, V> apiHandler, int size, boolean retainFails) {
		cache = CacheBuilder
			.newBuilder()
			.maximumSize(size)
			.build(new CacheLoader<>() {
				@Override
				public CompletableFuture<Optional<V>> load(K key) {
					return apiHandler.makeRequest(key);
				}
			});
		this.retainFails = retainFails;
	}

	public void invalidate() {
		cache.invalidateAll();
	}

	public void invalidate(K key) {
		cache.invalidate(key);
	}

	public CompletableFuture<Optional<V>> getAsync(K value) {
		if (retainFails) {
			return cache.getUnchecked(value);
		} else {
			final var res = cache.getUnchecked(value);

			// this doesn't need to be perfect, as long as we don't keep a bad value in the cache for too long
			// therefore, we can completely ignore possible race conditions
			if (getAsyncNow(value).isError()) {
				cache.invalidate(value);
			}

			return res;
		}
	}

	public ApiResult<V> getAsyncNow(K value) {
		final var res = cache.getUnchecked(value);

		if (!res.isDone()) {
			return ApiResult.pending();
		}

		return res.getNow(Optional.empty()).map(ApiResult::of).orElse(ApiResult.error());
	}
}
