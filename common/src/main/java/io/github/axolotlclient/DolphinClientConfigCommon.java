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

package io.github.axolotlclient;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.*;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.bridge.key.AxoKeybinding;
import io.github.axolotlclient.bridge.key.AxoKeys;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import io.github.axolotlclient.util.options.GenericOption;
import net.fabricmc.loader.api.FabricLoader;

public abstract class DolphinClientConfigCommon {
	public enum MenuButtonMode {
		DISABLED,
		MODMENU() {
			@Override
			public boolean showButton() {
				return !(FabricLoader.getInstance().isModLoaded("modmenu")
					&& !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu")
					&& !FabricLoader.getInstance().isModLoaded("dolphinclient-modmenu"));
			}
		},
		ALWAYS() {
			@Override
			public boolean showButton() {
				return true;
			}
		};

		@Override
		public String toString() {
			return "menu_button_mode." + super.toString().toLowerCase(Locale.ROOT);
		}

		public boolean showButton() {
			return false;
		}

	}
	public enum TabBadgeMode {
		BEFORE_NAME,
		BEFORE_NAME_ALIGNED,
		BEFORE_PING;

		@Override
		public String toString() {
			return "tab_badge_mode." + super.toString().toLowerCase(Locale.ROOT);
		}

	}

	// options

	public final BooleanOption showOwnNametag = new BooleanOption("showOwnNametag", false);
	public final BooleanOption useShadows = new BooleanOption("useShadows", false);
	public final BooleanOption nametagBackground = new BooleanOption("nametagBackground", true);

	public final BooleanOption showBadges = new BooleanOption("showBadges", true);
	public final BooleanOption customBadge = new BooleanOption("customBadge", false);
	public final StringOption badgeText = new StringOption("badgeText", "");
	public final EnumOption<TabBadgeMode> tabBadgeMode = new EnumOption<>("tab_badge_mode", TabBadgeMode.class, TabBadgeMode.BEFORE_NAME);

	public final ForceableBooleanOption timeChangerEnabled = new ForceableBooleanOption("enabled", false);
	public final IntegerOption customTime = new IntegerOption("time", 0, 0, 24000);

	public final BooleanOption dynamicFOV = new BooleanOption("dynamicFov", true);
	public final ForceableBooleanOption fullBright = new ForceableBooleanOption("fullBright", false);
	public final BooleanOption removeVignette = new BooleanOption("removeVignette", false);
	public final ForceableBooleanOption lowFire = new ForceableBooleanOption("lowFire", false);
	public final ColorOption hitColor = new ColorOption("hitColor", new Color(0x4DFF0000), this::updateHitColor);
	public final BooleanOption hitColorOnArmor = new BooleanOption("hit_color_on_armor", false);

	public final BooleanOption minimalViewBob = new BooleanOption("minimalViewBob", false);
	public final BooleanOption noHurtCam = new BooleanOption("noHurtCam", false);
	public final BooleanOption hideChat = new BooleanOption("hide_chat", false);

	public final BooleanOption enableCustomOutlines = new BooleanOption("enabled", false);
	public final ColorOption outlineColor = new ColorOption("color", new Color(0x66000000));
	public final IntegerOption outlineWidth = new IntegerOption("outlineWidth", 1, 1, 15);
	public final BooleanOption outlineFill = new BooleanOption("block_outlines.fill", false);
	public final ColorOption outlineFillColor = new ColorOption("block_outlines.fill_color", Colors.WINE_RED.withAlpha(165));

	public final BooleanOption customWindowTitle = new BooleanOption("customWindowTitle", true, this::updateWindowTitle);
	public final BooleanOption confirmDisconnect = new BooleanOption("confirm_disconnect", false);

	public final OptionCategory general = OptionCategory.create("general");
	public final OptionCategory nametagOptions = OptionCategory.create("nametagOptions");
	public final OptionCategory rendering = OptionCategory.create("rendering");
	public final OptionCategory outlines = OptionCategory.create("blockOutlines");
	public final OptionCategory timeChanger = OptionCategory.create("timeChanger");

	public final BooleanOption creditsBGM = new BooleanOption("creditsBGM", true);
	public final BooleanOption modifyClientBrand = new BooleanOption("modify_client_brand", true);
	public final BooleanOption debugLogOutput = new BooleanOption("debugLogOutput", false);

	public final BooleanOption noRain = new BooleanOption("noRain", false);

	public final BooleanOption noAltIcons = new BooleanOption("no_alt_icons", false);

	public final OptionCategory config = OptionCategory.create("config");
	public final OptionCategory hidden = OptionCategory.create("storedOptions");
	public final BooleanOption someNiceBackground = new BooleanOption("defNoSecret", false);
	public final StringOption datetimeFormat = new StringOption("datetime_format", "yyyy/MM/dd HH:mm:ss", s -> dateTimeFormatter = DateTimeFormatter.ofPattern(s));
	public final EnumOption<MenuButtonMode> titleScreenOptionButtonMode = new EnumOption<>("title_screen_button_mode", MenuButtonMode.class, MenuButtonMode.MODMENU);
	public final EnumOption<MenuButtonMode> gameMenuScreenOptionButtonMode = new EnumOption<>("game_menu_screen_button_mode", MenuButtonMode.class, MenuButtonMode.MODMENU);

	public DateTimeFormatter dateTimeFormatter;

	public DolphinClientConfigCommon() {
		config.add(general);
		config.add(rendering);
		config.add(hidden);

		rendering.add(outlines);
		rendering.add(nametagOptions);

		nametagOptions.add(showOwnNametag);
		nametagOptions.add(useShadows);
		nametagOptions.add(nametagBackground);

		general.add(customWindowTitle);
		general.add(debugLogOutput);

		general.add(datetimeFormat);
		general.add(titleScreenOptionButtonMode);
		general.add(gameMenuScreenOptionButtonMode);
		general.add(confirmDisconnect);

		timeChanger.add(timeChangerEnabled);
		timeChanger.add(customTime);
		timeChanger.add(new GenericOption("time_changer.preset.day", "time_changer.set_preset", () -> customTime.set(1000)));
		timeChanger.add(new GenericOption("time_changer.preset.noon", "time_changer.set_preset", () -> customTime.set(6000)));
		timeChanger.add(new GenericOption("time_changer.preset.night", "time_changer.set_preset", () -> customTime.set(13000)));
		timeChanger.add(new GenericOption("time_changer.preset.midnight", "time_changer.set_preset", () -> customTime.set(18000)));

		outlines.add(enableCustomOutlines);
		outlines.add(outlineColor);
		outlines.add(outlineWidth, outlineFill, outlineFillColor);

		rendering.add(timeChanger);

		rendering.add(
			dynamicFOV,
			fullBright,
			removeVignette,
			lowFire,
			minimalViewBob,
			noHurtCam,
			noRain,
			hideChat,
			hitColor,
			hitColorOnArmor
		);

		hidden.add(creditsBGM, someNiceBackground, modifyClientBrand, noAltIcons);

		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "toggle_hide_chat").br$registerOnConsumeClick(() -> {
			hideChat.toggle();
			DolphinClientCommon.getInstance().saveConfig();
		});
		AxoKeybinding.create(AxoKeys.KEY_UNKNOWN, "toggle_fullbright").br$registerOnConsumeClick(() -> {
			fullBright.toggle();
			DolphinClientCommon.getInstance().saveConfig();
		});

		Events.TICK.register(() -> {
			if (hitColor.getOriginal().isChroma()) {
				updateHitColor(hitColor.get());
			}
		});
	}

	public DateTimeFormatter getDateTimeFormatter() {
		if (dateTimeFormatter == null) {
			dateTimeFormatter = DateTimeFormatter.ofPattern(datetimeFormat.get());
		}

		return dateTimeFormatter;
	}

	public static DolphinClientConfigCommon instance() {
		return DolphinClientCommon.getInstance().getConfig();
	}

	public final void add(Option<?> option) {
		config.add(option);
	}

	public final void addCategory(OptionCategory cat) {
		config.add(cat);
	}

	public final OptionCategory getConfig() {
		return config;
	}

	protected abstract void updateWindowTitle(boolean useCustom);

	protected abstract void updateHitColor(Color color);
}
