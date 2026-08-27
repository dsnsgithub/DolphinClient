/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.bridge.render.AxoWindow;
import io.github.axolotlclient.bridge.util.AxoIdentifier;
import io.github.axolotlclient.bridge.util.AxoProfiler;
import io.github.axolotlclient.config.profiles.ProfileAware;
import io.github.axolotlclient.modules.AbstractCommonModule;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.component.Positionable;
import io.github.axolotlclient.modules.hud.gui.entry.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.hud.*;
import io.github.axolotlclient.modules.hud.gui.hud.item.ArmorHud;
import io.github.axolotlclient.modules.hud.gui.hud.item.ArrowHud;
import io.github.axolotlclient.modules.hud.gui.hud.item.ItemUpdateHud;
import io.github.axolotlclient.modules.hud.gui.hud.simple.*;
import io.github.axolotlclient.modules.hud.gui.hud.item.InventoryHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.PlayerTabOverlayHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.SubtitlesHudHud;
import io.github.axolotlclient.modules.hud.gui.layout.SnapAnchorType;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public abstract class HudManagerCommon extends AbstractCommonModule implements ProfileAware {
	@Getter
	private static HudManagerCommon instance;

	public static final int HUD_RESCALE_GRAB_TOLERANCE = 5;
	private static final String HUD_DEPENDENCIES_SAVE_FILE_NAME = "hud_dependencies.json";
	private static final String CUSTOM_MODULE_SAVE_FILE_NAME = "custom_hud.json";
	private final AxoKeybinding key = AxoKeybinding.create(AxoKeys.KEY_RSHIFT, "key.openHud");
	private final AxoKeybinding toggleHud = AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "key.toggle_hud");
	private final OptionCategory hudCategory = OptionCategory.create("hud");
	private final OptionCategory hudEditScreenCategory = OptionCategory.create("hudEditScreen");
	private final BooleanOption snapping = new BooleanOption("snapping", true);
	private final BooleanOption enabled = new BooleanOption("enabled", true);
	public final BooleanOption hudLinkCreationEnabled = new BooleanOption("hud_link_creation_enabled", true);
	private final FloatOption hudLinkLineWidth = new FloatOption("hud.hud_link_line_width", 3f, 1f, 10f);
	public final ColorOption grabCornerColor = new ColorOption("rescale_grab_corner_color", Colors.PINK);
	private final Map<AxoIdentifier, HudEntry> entries;
	private final Deque<HudEntry> visitedEntries = new ArrayDeque<>();

	protected HudManagerCommon() {
		Preconditions.checkState(instance == null, "singleton already initialized");
		// bad antipattern, but whatever
		instance = this;
		this.entries = new LinkedHashMap<>();
	}

	public void init() {
		key.br$registerOnConsumeClick(this::openScreen);
		toggleHud.br$registerOnConsumeClick(() -> {
			enabled.toggle();
			DolphinClientCommon.getInstance().saveConfig();
		});
		DolphinClientCommon.getInstance().getConfig().addCategory(hudCategory);
		hudCategory.add(enabled, grabCornerColor, hudLinkCreationEnabled, hudLinkLineWidth);
		hudEditScreenCategory.add(snapping);
		DolphinClientCommon.getInstance().getConfig().hidden.add(hudEditScreenCategory);
		add(new PingHud());
		add(new FPSHud());
		add(new CPSHud());
		add(new ArmorHud());
		add(new PotionsHud());
		add(new ToggleSprintHud());
		add(new IPHud());
		add(new IconHud());
		add(new SpeedHud());
		add(new CoordsHud());
		add(new ArrowHud());
		add(new ItemUpdateHud());
		add(new IRLTimeHud());
		add(new ReachHud());
		add(new MemoryHud());
		add(new PlayerCountHud());
		add(new CompassHud());
		add(new TPSHud());
		add(new ComboHud());
		add(new MouseMovementHud());
		add(new DayCounterHud());
		add(new InventoryHud());
		add(new XPHud());
		add(new PlayerTabOverlayHud());
		add(new SubtitlesHudHud());

		addExtraHud();

		addNonConfigured(BedwarsMod.getInstance().getUpgradesOverlay());
		addNonConfigured(BedwarsMod.getInstance().getResourceOverlay());
		addNonConfigured(BedwarsMod.getInstance().getStatsOverlay());
		addNonConfigured(BedwarsMod.getInstance().getSessionStatsOverlay());

		entries.values().forEach(HudEntry::init);

		hudCategory.add(new GenericOption("hud.dependency_links", "hud.dependency_links.clear", () -> {
			entries.values().forEach(HudEntry::clearBoundsDependencies);
			saveHudDependencyLinks();
		}));
		hudCategory.add(new GenericOption("hud.custom_entry", "hud.custom_entry.add", () -> {
			CustomHudEntry entry = new CustomHudEntry(AxoIdentifier.of(DolphinClientCommon.MODID, "custom_hud/" + UUID.randomUUID()));
			entry.setEnabled(true);
			entry.init();
			entry.onBoundsUpdate();
			entries.put(entry.getId(), entry);
			hudCategory.add(entry.getAllOptions(), false);
			client.br$reinitScreen();
			saveCustomEntries();
		}));

		Events.CLIENT_START.register(() -> {
			loadCustomEntries();
			loadHudDependencyLinks();
		});
		Events.CLIENT_STOP.register(() -> {
			saveCustomEntries();
			saveHudDependencyLinks();
		});
	}

	@Override
	public void lateInit() {
		entries.values().forEach(HudEntry::postConfigLoad);
		if (AxoWindow.getWindow() == null) {
			Events.CLIENT_READY.register(this::refreshAllBounds);
		} else {
			refreshAllBounds();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadHudDependencyLinks() {
		try {
			var path = DolphinClientCommon.resolveProfileConfigFile(HUD_DEPENDENCIES_SAVE_FILE_NAME);
			if (Files.exists(path)) {
				var obj = (Map<String, Object>) GsonHelper.read(Files.readString(path));
				obj.forEach((name, o) -> {
					var hudId = AxoIdentifier.parse(name);
					var hud = get(hudId);
					if (hud == null) return;
					var deps = (Map<String, Object>) o;
					if (deps.containsKey("x")) {
						((Map<String, String>) deps.get("x")).forEach((id, type) -> {
							var dep = get(AxoIdentifier.parse(id));
							if (dep == null) return;
							var anchorType = SnapAnchorType.fromName(type);
							if (anchorType == null) return;
							hud.addBoundsDependency(dep, anchorType);
						});
					}
					if (deps.containsKey("y")) {
						((Map<String, String>) deps.get("y")).forEach((id, type) -> {
							var dep = get(AxoIdentifier.parse(id));
							if (dep == null) return;
							var anchorType = SnapAnchorType.fromName(type);
							if (anchorType == null) return;
							hud.addBoundsDependency(dep, anchorType);
						});
					}
				});
			}
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to load hud dependency links!", e);
		}
	}

	public void saveHudDependencyLinks() {
		try {
			var path = DolphinClientCommon.resolveProfileConfigFile(HUD_DEPENDENCIES_SAVE_FILE_NAME);
			Files.createDirectories(path.getParent());
			var writer = Files.newBufferedWriter(path);
			var json = new JsonWriter(writer);
			json.beginObject();
			for (Map.Entry<AxoIdentifier, HudEntry> entry : entries.entrySet()) {
				HudEntry hudEntry = entry.getValue();
				var dependenciesX = hudEntry.getDependenciesX();
				var dependenciesY = hudEntry.getDependenciesY();
				if (dependenciesX.isEmpty() && dependenciesY.isEmpty()) continue;
				json.name(hudEntry.getId().toString());
				json.beginObject();
				if (!dependenciesX.isEmpty()) {
					json.name("x").beginObject();
					for (Map.Entry<HudEntry, SnapAnchorType> e : dependenciesX.entrySet()) {
						json.name(e.getKey().getId().toString()).value(e.getValue().getName());
					}
					json.endObject();
				}
				if (!dependenciesY.isEmpty()) {
					json.name("y").beginObject();
					for (Map.Entry<HudEntry, SnapAnchorType> e : dependenciesY.entrySet()) {
						json.name(e.getKey().getId().toString()).value(e.getValue().getName());
					}
					json.endObject();
				}
				json.endObject();
			}
			json.endObject();
			json.close();
		} catch (Exception e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to save hud dependency links!", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadCustomEntries() {
		try {
			var path = DolphinClientCommon.resolveProfileConfigFile(CUSTOM_MODULE_SAVE_FILE_NAME);
			if (Files.exists(path)) {
				var obj = (List<Object>) GsonHelper.read(Files.readString(path));
				obj.forEach(o -> {
					var values = (Map<String, Object>) o;
					AxoIdentifier id;
					if (values.containsKey("id")) {
						id = AxoIdentifier.parse((String) values.get("id"));
					} else {
						id = AxoIdentifier.of(DolphinClientCommon.MODID, "custom_hud/" + UUID.randomUUID());
					}
					CustomHudEntry entry = new CustomHudEntry(id);
					entry.getAllOptions().getOptions().forEach(opt -> {
						if (values.containsKey(opt.getName())) {
							opt.fromSerializedValue((String) values.get(opt.getName()));
						}
					});
					entries.put(entry.getId(), entry);
					hudCategory.add(entry.getAllOptions(), false);
					entry.init();
					entry.onBoundsUpdate();
				});
			}
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to load custom hud modules!", e);
		}
	}

	public void saveCustomEntries() {
		try {
			var path = DolphinClientCommon.resolveProfileConfigFile(CUSTOM_MODULE_SAVE_FILE_NAME);
			Files.createDirectories(path.getParent());
			var writer = Files.newBufferedWriter(path);
			var json = new JsonWriter(writer);
			json.beginArray();
			for (Map.Entry<AxoIdentifier, HudEntry> entry : entries.entrySet()) {
				HudEntry hudEntry = entry.getValue();
				if (hudEntry instanceof CustomHudEntry hud) {
					json.beginObject();
					json.name("id").value(hud.getId().toString());
					for (Option<?> opt : hud.getCategory().getOptions()) {
						var value = opt.toSerializedValue();
						if (value != null) {
							json.name(opt.getName());
							json.value(value);
						}
					}
					json.endObject();
				}
			}
			json.endArray();
			json.close();
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().warn("Failed to save custom hud modules!", e);
		}
	}

	@Override
	public final void tick() {
		AxoProfiler.get().br$push("hud_modules");
		entries.values().stream()
			.filter(hudEntry -> hudEntry.isEnabled() && hudEntry.tickable())
			.forEach(hudEntry -> {
				AxoProfiler.get().br$push(hudEntry.getNameKey());
				AxoProfiler.get().br$push("tick");
				hudEntry.tick();
				AxoProfiler.get().br$pop();
				AxoProfiler.get().br$pop();
			});
		AxoProfiler.get().br$pop();
	}

	public final HudManagerCommon add(AbstractHudEntry entry) {
		addNonConfigured(entry);
		hudCategory.add(entry.getAllOptions());
		return this;
	}

	@SuppressWarnings("UnusedReturnValue")
	public final HudManagerCommon addNonConfigured(AbstractHudEntry entry) {
		entries.put(entry.getId(), entry);
		return this;
	}

	public final void refreshAllBounds() {
		for (HudEntry entry : getEntries()) {
			entry.onBoundsUpdate();
		}
	}

	public final List<HudEntry> getEntries() {
		if (!entries.isEmpty()) {
			return new ArrayList<>(entries.values());
		}
		return new ArrayList<>();
	}

	public final HudEntry get(AxoIdentifier identifier) {
		HudEntry entry = entries.get(identifier);
		if (entry != null) {
			return entry;
		}
		return entries.get(DolphinClientCommon.aliasIdentifier(identifier));
	}

	public final void removeEntry(AxoIdentifier identifier) {
		final var removed = entries.remove(identifier);
		if (removed != null) {
			hudCategory.getSubCategories().remove(removed.getCategory());
		}
	}

	public boolean hudsEnabled() {
		return enabled.get();
	}

	public void render(AxoRenderContext context, float delta) {
		if (!hudsEnabled()) return;
		for (HudEntry hud : getEntries()) {
			if (hud.isEnabled() && !hud.isHidden()) {
				hud.render(context, delta);
			}
		}
	}

	public final Optional<HudEntry> getEntryXY(int x, int y) {
		for (HudEntry entry : getMoveableEntries()) {
			Rectangle bounds = entry.getTrueBounds();
			if (bounds.x() <= x && bounds.x() + bounds.width() >= x && bounds.y() <= y
				&& bounds.y() + bounds.height() >= y) {
				return Optional.of(entry);
			}
		}
		return Optional.empty();
	}

	public final List<HudEntry> getMoveableEntries() {
		if (!entries.isEmpty()) {
			return entries.values().stream().filter((entry) -> entry.isEnabled() && entry.movable())
				.collect(Collectors.toCollection(ArrayList::new));
		}
		return new ArrayList<>();
	}

	public final void renderPlaceholder(AxoRenderContext context, float delta) {
		for (HudEntry hud : getEntries()) {
			if (hud.isEnabled()) {
				hud.renderPlaceholder(context, delta);
			}
		}
		for (HudEntry hud : getEntries()) {
			if (hud.isEnabled()) {
				SnappingHelper.renderLinks(context, hud, hudLinkLineWidth.get() / 2f);
			}
		}
	}

	public final List<Rectangle> getAllBounds() {
		return getMoveableEntries()
			.stream()
			.map(Positionable::getTrueBounds)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public void reloadConfig() {
		entries.entrySet().removeIf(entry -> {
			if (entry.getValue() instanceof CustomHudEntry custom) {
				hudCategory.getSubCategoryMap().remove(custom.getAllOptions());
				return true;
			}
			return false;
		});
		entries.values().forEach(HudEntry::clearBoundsDependencies);
		loadCustomEntries();
		loadHudDependencyLinks();
		for (var hud : getEntries()) {
			if (hud instanceof ProfileAware p) {
				p.reloadConfig();
			}
		}
	}

	@Override
	public void saveConfig() {
		saveCustomEntries();
		saveHudDependencyLinks();
		for (var hud : getEntries()) {
			if (hud instanceof ProfileAware p) {
				p.saveConfig();
			}
		}
	}

	public void updateBoundsDependencies(HudEntry origin) {
		if (visitedEntries.contains(origin)) return;
		visitedEntries.push(origin);
		var entries = HudManagerCommon.getInstance().getMoveableEntries();
		entries.remove(origin);
		entries.removeIf(e -> visitedEntries.stream().anyMatch(v -> v.dependsOnY(e).or(() -> v.dependsOnX(e)).isPresent()));
		for (HudEntry entry : entries) {
			entry.dependsOnX(origin).ifPresent(type -> type.updatePosX(origin, entry));
			entry.dependsOnY(origin).ifPresent(type -> type.updatePosY(origin, entry));
		}
		visitedEntries.pop();
	}

	public boolean isSnappingEnabled() {
		return snapping.get();
	}

	public void toggleSnapping() {
		snapping.toggle();
		DolphinClientCommon.getInstance().saveConfig();
	}

	protected abstract void openScreen();

	protected abstract void addExtraHud();

	public abstract void closeScreen();
}
