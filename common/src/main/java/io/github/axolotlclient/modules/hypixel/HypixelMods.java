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

package io.github.axolotlclient.modules.hypixel;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.DolphinClientConfigCommon;
import io.github.axolotlclient.bridge.events.Events;
import io.github.axolotlclient.modules.AbstractCommonModule;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;

public class HypixelMods extends AbstractCommonModule {

	public static final HypixelMods INSTANCE = new HypixelMods();
	public final EnumOption<HypixelApiCacheMode> cacheMode = new EnumOption<>("cache_mode", HypixelApiCacheMode.class,
		HypixelApiCacheMode.ON_CLIENT_DISCONNECT);

	private final OptionCategory category = OptionCategory.create("hypixel-mods");
	private final List<AbstractHypixelMod> subModules = new ArrayList<>();
	private final BooleanOption removeLobbyJoinMessages = new BooleanOption("removeLobbyJoinMessages", false);

	public static HypixelMods getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		category.add(cacheMode);
		category.add(removeLobbyJoinMessages);

		addSubModule(LevelHead.getInstance());
		addSubModule(AutoGG.getInstance());
		addSubModule(AutoTip.getInstance());
		addSubModule(NickHider.getInstance());
		addSubModule(AutoBoop.getInstance());
		addSubModule(Skyblock.getInstance());
		addSubModule(BedwarsMod.getInstance());
		addSubModule(StatsMod.getInstance());

		subModules.forEach(AbstractHypixelMod::init);

		DolphinClientConfigCommon.instance().addCategory(category);

		Events.END_RESOURCE_RELOAD.register(HypixelMessages.getInstance());

		Events.RECEIVE_CHAT_MESSAGE.register(event -> {
			AutoBoop.getInstance().handleMessage(event.getOriginalMessage());
			HypixelMessages.getInstance().process(removeLobbyJoinMessages, "lobby_join", event);
		});
	}

	@Override
	public void tick() {
		subModules.forEach(abstractHypixelMod -> {
			if (abstractHypixelMod.tickable())
				abstractHypixelMod.tick();
		});
	}

	private void addSubModule(AbstractHypixelMod mod) {
		this.subModules.add(mod);
		var category = mod.getCategory();
		if (category != null) {
			this.category.add(category);
		}
	}

	public enum HypixelApiCacheMode {
		ON_CLIENT_DISCONNECT, ON_PLAYER_DISCONNECT,
	}
}
