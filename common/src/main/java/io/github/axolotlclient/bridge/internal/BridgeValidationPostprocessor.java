/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.bridge.internal;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.bridge.BridgeVersion;
import io.github.axolotlclient.bridge.entity.effect.AxoStatusEffects;
import io.github.axolotlclient.bridge.item.AxoEnchants;
import io.github.axolotlclient.bridge.item.AxoItems;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoSprites;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

public class BridgeValidationPostprocessor implements IMixinConfigPlugin {
	private static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	private static final List<String> messages = new ArrayList<>();
	private static Map<String, Set<String>> remainingInjections;

	private static boolean checkVersion(RequiresImpl impl) {
		return BridgeVersion.version().ordinal() >= impl.min().ordinal() &&
			BridgeVersion.version().ordinal() <= impl.max().ordinal();
	}

	private static boolean checkVersion(AnnotatedElement element) {
		final var ann = element.getAnnotation(RequiresImpl.class);
		if (ann == null) {
			return false;
		}

		return checkVersion(ann);
	}

	@SneakyThrows
	private static Class<?> forName(String name) {
		return Class.forName(name.replace("/", "."), false, BridgeValidationPostprocessor.class.getClassLoader());
	}

	@SneakyThrows
	private static Object getStatic(Field field) {
		return field.get(null);
	}

	@SneakyThrows
	private static void loadClasses(Path path) {
		Preconditions.checkArgument(Files.isDirectory(path));
		try (final var stream = Files.walk(path)) {
			stream.filter(Files::isRegularFile)
				.map(path::relativize)
				.map(Path::toString)
				.filter(p -> p.endsWith(".class"))
				.map(p -> p.substring(0, p.length() - ".class".length()).replace("/", "."))
				.filter(p -> p.startsWith("net.minecraft") || p.startsWith("io.github.axolotlclient") || p.startsWith("com.mojang.blaze3d"))
				.forEach(s -> {
					try {
						forName(s);
					} catch (RuntimeException e) {
						if (e.getMessage().contains("in environment type CLIENT")) {
							return;
						}

						throw e;
					}
				});
		}
	}

	private static void validateFields(Class<?> clazz) {
		Arrays.stream(clazz.getFields())
			.filter(x -> Modifier.isStatic(x.getModifiers()))
			.filter(BridgeValidationPostprocessor::checkVersion)
			.filter(field -> getStatic(field) == null)
			.forEach(field -> messages.add("missing bridge value implementation for %s::%s".formatted(
				clazz.getName(),
				field.getName())
			));
	}

	public static void validate() {
		Preconditions.checkState(IS_DEV, "BridgeValidationPostprocessor.validate() called in prod?");

		Stream.of(
				FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(),
				FabricLoader.getInstance().getModContainer(DolphinClientCommon.COMMON_MODID)
					.or(() -> FabricLoader.getInstance().getModContainer(DolphinClientCommon.LEGACY_COMMON_MODID))
					.orElseThrow()
			)
			.flatMap(x -> x.getRootPaths().stream())
			.forEach(BridgeValidationPostprocessor::loadClasses);

		validateFields(AxoStatusEffects.class);
		validateFields(AxoItems.class);
		validateFields(AxoSprites.class);
		validateFields(AxoKeys.class);
		validateFields(AxoEnchants.class);

		remainingInjections.forEach((k, v) -> {
			for (String ifc : v) {
				messages.add("bridge interface not injected: %s -> %s".formatted(ifc, k));

			}
		});

		if (!messages.isEmpty()) {
			messages.forEach(System.out::println);
			throw new RuntimeException("missing bridge implementations!");
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
		if (!IS_DEV) {
			return;
		}

		final var ctPath = FabricLoader.getInstance().getModContainer(DolphinClientCommon.MODID)
			.or(() -> FabricLoader.getInstance().getModContainer(DolphinClientCommon.LEGACY_MODID))
			.orElseThrow()
			.findPath("axolotlclient.classtweaker");
		if (ctPath.isPresent()) {
			try (var reader = Files.newBufferedReader(ctPath.get())) {
				var lines = reader.lines().filter(s -> s.startsWith("inject-interface")).map(s -> s.split(" +"))
					.peek(l -> {
						if (l.length != 3) {
							throw new IllegalArgumentException("Badly formatted interface injection in " + ctPath.get() + ": " + String.join(" ", l));
						}
					}).toList();
				var map = new HashMap<String, Set<String>>();
				lines.forEach(line -> map.computeIfAbsent(line[1].replace("/", "."), a -> new HashSet<>()).add(line[2]));
				remainingInjections = map;
			} catch (IOException e) {
				throw new RuntimeException("Failed to read classtweaker file!", e);
			}
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (!IS_DEV) {
			return;
		}

		mixinInfo.getClassNode(0).interfaces.forEach(s -> {
			final var ifs = remainingInjections.get(targetClassName);
			if (ifs != null) {
				ifs.remove(s);
			}
		});
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (!IS_DEV) {
			return;
		}

		final var bridgeMethods = targetClass.interfaces.stream()
			.filter(x -> x.startsWith("io/github/axolotlclient/bridge"))
			.map(BridgeValidationPostprocessor::forName)
			.flatMap(clazz -> Arrays.stream(
				clazz.getAnnotation(RequiresImpl.Inherits.class) != null
					? clazz.getMethods()
					: clazz.getDeclaredMethods()
			))
			.filter(BridgeValidationPostprocessor::checkVersion)
			.map(method -> method.getName() + Type.getMethodDescriptor(method))
			.collect(Collectors.toCollection(HashSet::new));

		for (final var method : targetClass.methods) {
			if (Annotations.getVisible(method, RequiresImpl.class) != null) {
				messages.add("bridge method not implemented: %s::%s%s".formatted(
					targetClassName,
					method.name,
					method.desc
				));
			}

			bridgeMethods.remove(method.name + method.desc);
		}

		for (final var bridgeMethod : bridgeMethods) {
			messages.add("bridge method not implemented: %s::%s".formatted(
				targetClassName,
				bridgeMethod
			));
		}
	}
}
