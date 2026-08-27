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

package io.github.axolotlclient.modules.hypixel;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.util.BiContainer;
import io.github.axolotlclient.bridge.AxoMinecraftClient;
import io.github.axolotlclient.bridge.util.AxoText;
import lombok.Getter;

public class NickHider implements AbstractHypixelMod {
	@Getter
	private static final NickHider Instance = new NickHider();
	public final StringOption hiddenNameSelf = new StringOption("hiddenNameSelf", "You");
	public final StringOption hiddenNameOthers = new StringOption("hiddenNameOthers", "Player");
	public final BooleanOption hideOwnName = new BooleanOption("hideOwnName", false);
	public final BooleanOption hideOtherNames = new BooleanOption("hideOtherNames", false);
	public final BooleanOption hideOwnSkin = new BooleanOption("hideOwnSkin", false);
	public final BooleanOption hideOtherSkins = new BooleanOption("hideOtherSkins", false);
	private final OptionCategory category = OptionCategory.create("nickhider");

	@Override
	public void init() {
		category.add(hiddenNameSelf);
		category.add(hiddenNameOthers);
		category.add(hideOwnName);
		category.add(hideOtherNames);
		category.add(hideOwnSkin);
		category.add(hideOtherSkins);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	public AxoText editMessage(AxoText message) {
		final var minecraft = AxoMinecraftClient.getInstance();

		if (hideOwnName.get() || hideOtherNames.get()) {
			String msg = message.br$getRawString();

			List<BiContainer<String, String>> replacements = new ArrayList<>();
			if (minecraft.br$getPlayer() != null) {
				// TODO: is .br$getGameProfile().br$getName() good?
				String playerName = minecraft.br$getPlayer().br$getName();
				if (hideOwnName.get() && msg.contains(playerName)) {
					replacements.add(BiContainer.of(playerName, hiddenNameSelf.get()));
				}
			}

			if (hideOtherNames.get() && minecraft.br$getWorld() != null) {
				for (final var player : minecraft.br$getWorld().br$getPlayers()) {
					if (player == minecraft.br$getPlayer()) {
						continue;
					}

					// TODO: is .br$getGameProfile().br$getName() good?
					if (msg.contains(player.br$getName())) {
						replacements.add(BiContainer.of(player.br$getName(), hiddenNameOthers.get()));
					}
				}
			}

			if (!replacements.isEmpty()) {
				return editComponent(message, replacements, AxoText.literal(""));
			}
		}
		return message;
	}

	public AxoText editComponent(AxoText text, String find, String replace) {
		return editComponent(text, List.of(BiContainer.of(find, replace)), AxoText.literal(""));
	}

	private AxoText.Mutable editComponent(AxoText component, List<BiContainer<String, String>> replacements, AxoText.Mutable edited) {
		component.br$visit((edit, style) -> {
			for (var entry : replacements) {
				edit = edit.replace(entry.getLeft(), entry.getRight());
			}
			edited.br$append(AxoText.literal(edit).br$setStyle(style));
		});

		return edited;
	}
}
