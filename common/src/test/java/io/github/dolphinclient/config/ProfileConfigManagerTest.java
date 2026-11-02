/*
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient.
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

package io.github.dolphinclient.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigManagerTest {
	private final OptionCategory root = OptionCategory.create("config");
	private final BooleanOption animation = new BooleanOption("blockHitting", true);
	private final BooleanOption general = new BooleanOption("customWindowTitle", true);

	private ProfileConfigManager manager(Path file) {
		var animations = OptionCategory.create("dolphinclient-oldanimations");
		var blocking = OptionCategory.create("blockingItemUsing");
		blocking.add(animation);
		animations.add(blocking);
		root.add(animations);
		root.add(general);
		return new ProfileConfigManager(file, root);
	}

	@Test
	void savesAndReloadsGeneralAndAnimationsInOneFile(@TempDir Path temp) throws Exception {
		var file = temp.resolve("Default.json");
		var manager = manager(file);
		animation.set(false);
		general.set(false);
		manager.save();
		try (var files = Files.list(temp)) {
			assertEquals(List.of(file), files.toList());
		}
		assertTrue(Files.readString(file).contains("dolphinclient-oldanimations"));
		animation.set(true);
		general.set(true);
		manager.load();
		assertFalse(animation.get());
		assertFalse(general.get());
	}

	@Test
	void switchesProfilesWithoutLeakingMissingAnimationCategory(@TempDir Path temp) throws Exception {
		var first = temp.resolve("First.json");
		var second = temp.resolve("Second.json");
		var manager = manager(first);
		animation.set(false);
		manager.save();
		Files.createDirectories(second.getParent());
		Files.writeString(second, "{\"name\":\"Second\",\"current\":false,\"config\":{\"customWindowTitle\":\"false\"}}");
		manager.setFile(second);
		manager.load();
		assertTrue(animation.get());
		assertFalse(general.get());
		manager.setFile(first);
		manager.load();
		assertFalse(animation.get());
		assertTrue(general.get());
	}

	@Test
	void newProfileUsesDefaults(@TempDir Path temp) {
		var manager = manager(temp.resolve("Default.json"));
		animation.set(false);
		general.set(false);
		manager.load();
		assertTrue(animation.get());
		assertTrue(general.get());
	}
}
