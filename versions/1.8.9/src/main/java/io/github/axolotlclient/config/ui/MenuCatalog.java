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

package io.github.axolotlclient.config.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.github.axolotlclient.DolphinClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.entry.AbstractHudEntry;
import io.github.axolotlclient.oldanimations.OldAnimations;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import lombok.Getter;
import net.minecraft.client.resource.language.I18n;

/**
 * Flattens the nested DolphinClient option tree into Lunar-style modules.
 * Nested categories become inline sections instead of extra screens.
 */
public final class MenuCatalog {
	public enum Tab {
		HUD,
		MODS,
		SETTINGS
	}

	public sealed interface Node {
		boolean matches(String query);
	}

	public record OptionNode(Option<?> option) implements Node {
		@Override
		public boolean matches(String query) {
			return contains(label(option.getName()), query) || contains(option.getName(), query);
		}
	}

	public record SectionNode(String nameKey, List<Node> children) implements Node {
		@Override
		public boolean matches(String query) {
			if (contains(label(nameKey), query) || contains(nameKey, query)) {
				return true;
			}
			for (Node child : children) {
				if (child.matches(query)) {
					return true;
				}
			}
			return false;
		}
	}

	@Getter
	public static final class Module {
		private final String id;
		private final String nameKey;
		private final Tab tab;
		private final BooleanOption enabled;
		private final HudEntry hud;
		private final List<Node> nodes;

		Module(String id, String nameKey, Tab tab, BooleanOption enabled, HudEntry hud, List<Node> nodes) {
			this.id = id;
			this.nameKey = nameKey;
			this.tab = tab;
			this.enabled = enabled;
			this.hud = hud;
			this.nodes = nodes;
		}

		public String displayName() {
			return stripRedundantHud(label(nameKey));
		}

		public boolean matches(String query) {
			if (query.isEmpty()) {
				return true;
			}
			if (contains(displayName(), query) || contains(nameKey, query)) {
				return true;
			}
			for (Node node : nodes) {
				if (node.matches(query)) {
					return true;
				}
			}
			return false;
		}
	}

	private MenuCatalog() {
	}

	public static List<Module> build() {
		List<Module> modules = new ArrayList<>();
		OptionCategory root = DolphinClient.getInstance().getConfig().getConfig();

		OptionCategory hudCategory = find(root.getSubCategories(), "hud");
		if (hudCategory != null) {
			List<Node> hudGlobals = nodesFrom(hudCategory, true);
			if (!hudGlobals.isEmpty()) {
				modules.add(new Module("hud", "hud", Tab.HUD, firstEnabled(hudCategory.getOptions()), null, hudGlobals));
			}
		}

		for (HudEntry entry : HudManager.getInstance().getEntries()) {
			List<Node> nodes = new ArrayList<>();
			for (Option<?> option : entry.getConfigurationOptions()) {
				if (isEnabledToggle(option) || isPosition(option)) {
					continue;
				}
				nodes.add(new OptionNode(option));
			}
			BooleanOption enabled = entry instanceof AbstractHudEntry hud
				? hud.getEnabled() : firstEnabled(optionsOf(entry));
			modules.add(new Module(entry.getId().toString(), entry.getNameKey(), Tab.HUD, enabled, entry, nodes));
		}

		for (OptionCategory category : root.getSubCategories()) {
			String name = category.getName();
			if ("hud".equals(name) || "storedOptions".equals(name) || "hidden".equals(name) || "config".equals(name)) {
				continue;
			}
			if (OldAnimations.MODID.equals(name)) {
				/* the 1.7 features are one toggleable mod tile with its groups as inline sections */
				modules.add(moduleFrom(name, Tab.MODS, firstEnabled(category.getOptions()), nodesFrom(category, false)));
				continue;
			}
			Tab tab = isSettings(name) ? Tab.SETTINGS : isHudTab(name) ? Tab.HUD : Tab.MODS;
			absorb(modules, category, tab);
		}

		modules.sort(Comparator.comparing(Module::displayName, String.CASE_INSENSITIVE_ORDER));
		return modules;
	}

	private static void absorb(List<Module> modules, OptionCategory category, Tab tab) {
		Collection<OptionCategory> children = category.getSubCategories();
		if (shouldPromote(children, tab)) {
			List<Node> own = optionNodes(category.getOptions());
			if (!own.isEmpty()) {
				modules.add(moduleFrom(category.getName(), tab, firstEnabled(category.getOptions()), own));
			}
			for (OptionCategory child : children) {
				modules.add(moduleFrom(child.getName(), tab, firstEnabled(child.getOptions()), nodesFrom(child, false)));
			}
			return;
		}
		modules.add(moduleFrom(category.getName(), tab, firstEnabled(category.getOptions()), nodesFrom(category, false)));
	}

	private static Module moduleFrom(String nameKey, Tab tab, BooleanOption enabled, List<Node> nodes) {
		return new Module(tab.name().toLowerCase(Locale.ROOT) + ":" + nameKey, nameKey, tab, enabled, null, nodes);
	}

	/**
	 * Settings keeps a tile per child even when only Screenshots remains
	 * under General, matching the layout from before Authentication was
	 * removed. Mods still need at least two children before splitting. HUD
	 * tiles always stay whole so an overlay's groups render as sections rather
	 * than as one tile per group.
	 */
	private static boolean shouldPromote(Collection<OptionCategory> children, Tab tab) {
		int size = children.size();
		if (size == 0 || size > 12) {
			return false;
		}
		if (tab == Tab.HUD) {
			return false;
		}
		return tab == Tab.SETTINGS || size >= 2;
	}

	private static List<Node> nodesFrom(OptionCategory category, boolean skipNestedHudEntries) {
		List<Node> nodes = optionNodes(category.getOptions());
		for (OptionCategory child : category.getSubCategories()) {
			if (skipNestedHudEntries) {
				continue;
			}
			List<Node> nested = nodesFrom(child, false);
			if (!nested.isEmpty()) {
				nodes.add(new SectionNode(child.getName(), nested));
			}
		}
		return nodes;
	}

	private static List<Node> optionNodes(Collection<Option<?>> options) {
		List<Node> nodes = new ArrayList<>();
		for (Option<?> option : options) {
			if (isEnabledToggle(option) || isPosition(option)) {
				continue;
			}
			nodes.add(new OptionNode(option));
		}
		return nodes;
	}

	private static BooleanOption firstEnabled(Collection<Option<?>> options) {
		for (Option<?> option : options) {
			if (isEnabledToggle(option) && option instanceof BooleanOption bool) {
				return bool;
			}
		}
		return null;
	}

	private static Collection<Option<?>> optionsOf(HudEntry entry) {
		if (entry.getCategory() != null) {
			return entry.getCategory().getOptions();
		}
		return entry.getConfigurationOptions();
	}

	private static boolean isEnabledToggle(Option<?> option) {
		return option instanceof BooleanOption && "enabled".equals(option.getName());
	}

	private static boolean isPosition(Option<?> option) {
		String name = option.getName();
		return "x".equals(name) || "y".equals(name);
	}

	private static boolean isSettings(String name) {
		return "general".equals(name);
	}

	/**
	 * Overlays that draw in the world rather than on a HUD widget still belong
	 * on the HUD tab, so they are routed there instead of Mods.
	 */
	private static boolean isHudTab(String name) {
		return "hitboxes".equals(name);
	}

	private static OptionCategory find(Collection<OptionCategory> categories, String name) {
		for (OptionCategory category : categories) {
			if (name.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}

	public static String label(String key) {
		String translated = I18n.translate(key);
		if (translated == null || translated.isEmpty()) {
			translated = key;
		}
		return polish(translated);
	}

	static String polish(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return text.replace("Hud", "HUD");
	}

	static String stripRedundantHud(String name) {
		if (name.endsWith(" HUD") && name.length() > 4) {
			return name.substring(0, name.length() - 4).trim();
		}
		return name;
	}

	static boolean contains(String value, String query) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(query);
	}

	public static boolean isForcedOff(Option<?> option) {
		return option instanceof ForceableBooleanOption forced && forced.isForceOff();
	}
}
