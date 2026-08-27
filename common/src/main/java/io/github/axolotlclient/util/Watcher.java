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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import io.github.axolotlclient.DolphinClientCommon;
import org.jetbrains.annotations.Nullable;


public class Watcher implements AutoCloseable {
	private static final ScheduledExecutorService thread = Executors.newSingleThreadScheduledExecutor();
	private final WatchService watcher;
	private final Path path;
	private final Predicate<String> fileFilter;

	public Watcher(Path root, Predicate<String> fileFilter) throws IOException {
		this.path = root;
		this.fileFilter = fileFilter;
		this.watcher = path.getFileSystem().newWatchService();

		try {
			this.watchDir(path);
		} catch (Exception e) {
			this.watcher.close();
			throw e;
		}
	}

	@Nullable
	public static Watcher create(Path path, Predicate<String> fileFilter) {
		try {
			Files.createDirectories(path);
			return new Watcher(path, fileFilter);
		} catch (IOException var2) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to initialize directory {} monitoring", path, var2);
			return null;
		}
	}

	public static Watcher createSelfTicking(Path path, Runnable onUpdate) {
		return createSelfTicking(path, s -> true, onUpdate);
	}

	public static Watcher createSelfTicking(Path path, Predicate<String> fileFilter, Runnable onUpdate) {
		var watcher = create(path, fileFilter);
		if (watcher != null) {
			thread.scheduleAtFixedRate(() -> {
				try {
					if (watcher.pollForChanges()) {
						onUpdate.run();
					}
				} catch (IOException e) {
					try {
						watcher.close();
					} catch (IOException ignored) {
					}
				}
			}, 100, 100, TimeUnit.MILLISECONDS);
		}
		return watcher;
	}

	private void watchDir(Path path) throws IOException {
		path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	}

	public boolean pollForChanges() throws IOException {
		boolean bl = false;

		WatchKey watchKey;
		while ((watchKey = this.watcher.poll()) != null) {
			for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
				if (watchKey.watchable() == this.path && watchEvent.context() != null) {
					bl |= this.fileFilter.test(((Path) watchEvent.context()).toString());
				}
			}

			watchKey.reset();
		}

		return bl;
	}

	public void close() throws IOException {
		this.watcher.close();
	}

	public void safeClose() {
		try {
			close();
		} catch (IOException ignored) {
		}
	}

	public static void close(Watcher watcher) {
		if (watcher != null) {
			watcher.safeClose();
		}
	}
}
