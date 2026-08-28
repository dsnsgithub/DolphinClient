/*
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

package io.github.axolotlclient.oldanimations.config;

import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.VersionedJsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.oldanimations.OldAnimations;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;

public class OldAnimationsConfig {

	public static OldAnimationsConfig instance = new OldAnimationsConfig();

	/* reloading resources does not reload certain niche things like the potion color cache */
	public boolean reloadPotionColors;

	@Getter
	private final OptionCategory category = OptionCategory.create(OldAnimations.MODID);
	private final OptionCategory blockingItemUsing = OptionCategory.create("blockingItemUsing");
	private final OptionCategory categorySneaking = OptionCategory.create("sneaking");
	private final OptionCategory categoryItems = OptionCategory.create("items");
	private final OptionCategory categorySpriteRendering = OptionCategory.create("spriteRendering");
	private final OptionCategory categoryItemPositions = OptionCategory.create("itemPositions");
	private final OptionCategory categoryOffsets = OptionCategory.create("offsets");

	public final BooleanOption useAndMine = new BooleanOption("useAndMine", true);
	public final BooleanOption useAndMineParticles = new BooleanOption("useAndMineParticles", true);
	public final BooleanOption useAndMineSound = new BooleanOption("useAndMineSound", true);
	public final BooleanOption blockHitting = new BooleanOption("blockHitting", true);
	public final BooleanOption allowMiningCancel = new BooleanOption("allowMiningCancel", true);
	public final BooleanOption miningProgressResetLogic = new BooleanOption("miningProgressResetLogic", true);
	public final BooleanOption blockingArm = new BooleanOption("blockingArm", true);
	public final BooleanOption swordBlockThirdPerson = new BooleanOption("swordBlockThirdPerson", true);
	public final BooleanOption oldSwingVisual = new BooleanOption("oldSwingVisual", true);
	public final BooleanOption oldSwingVisualParticles = new BooleanOption("oldSwingVisualParticles", true);

	public final BooleanOption smoothSneaking = new BooleanOption("smoothSneaking", true);
	public final BooleanOption slowUpSneak = new BooleanOption("slowUpSneak", true);
	public final BooleanOption thirdPersonSneaking = new BooleanOption("thirdPersonSneaking", true);
	public final BooleanOption fixThirdPersonHeldItemSneakDeSync = new BooleanOption("fixThirdPersonHeldItemSneakDeSync", true);
	public final BooleanOption doubleTapSneak = new BooleanOption("doubleTapSneak", true);
	public final BooleanOption stopLineTranslateSneak = new BooleanOption("stopLineTranslateSneak", true);

	public final BooleanOption mirroredProjectiles = new BooleanOption("mirroredProjectiles", true);
	public final BooleanOption droppedItemsFacePlayer = new BooleanOption("droppedItemsFacePlayer", true);
	public final BooleanOption stickRod = new BooleanOption("stickRod", true);
	public final BooleanOption equipLogic = new BooleanOption("equipLogic", true);
	public final BooleanOption framedItemLighting = new BooleanOption("framedItemLighting", true);
	public final BooleanOption oldJumpBoostPotionColor = new BooleanOption("oldJumpBoostPotionColor", true);
	public final BooleanOption goldenCarrotCreativeTab = new BooleanOption("goldenCarrotCreativeTab", true);

	public final BooleanOption droppedItemSprite = new BooleanOption("droppedItemSprite", true);
	public final BooleanOption swapDroppedItemNormals = new BooleanOption("swapDroppedItemNormals", true);
	public final BooleanOption projectileSprite = new BooleanOption("projectileSprite", true);
	public final BooleanOption swapProjectileSpriteNormals = new BooleanOption("swapProjectileSpriteNormals", true);
	public final BooleanOption framedItemSprite = new BooleanOption("framedItemSprite", true);
	public final BooleanOption swapFramedItemSpriteNormals = new BooleanOption("swapFramedItemSpriteNormals", true);

	public final BooleanOption firstPersonPositions = new BooleanOption("firstPersonPositions", true);
	public final BooleanOption thirdPersonPositions = new BooleanOption("thirdPersonPositions", true);
	public final BooleanOption framedItemPosition = new BooleanOption("framedItemPosition", true);
	public final BooleanOption oldBowRotation = new BooleanOption("oldBowRotation", true);
	public final BooleanOption trapDoorItemPosition = new BooleanOption("trapDoorItemPosition", true);

	public final BooleanOption oldItemPickup = new BooleanOption("oldItemPickup", true);
	public final BooleanOption xpOrbPosition = new BooleanOption("xpOrbPosition", true);
	public final BooleanOption flameOffset = new BooleanOption("flameOffset", true);
	public final BooleanOption creeperOffset = new BooleanOption("creeperOffset", true);
	public final BooleanOption shadowOffset = new BooleanOption("shadowOffset", true);
	public final BooleanOption horseRiderEyeHeight = new BooleanOption("horseRiderEyeHeight", true);
	public final BooleanOption sleepEyeHeight = new BooleanOption("sleepEyeHeight", true);
	public final BooleanOption potionEntityOffset = new BooleanOption("potionEntityOffset", true);
	public final BooleanOption playerEyeHeightOffset = new BooleanOption("playerEyeHeightOffset", true);
	public final BooleanOption zombieVillagerHelmetOffset = new BooleanOption("zombieVillagerHelmetOffset", true);
	public final BooleanOption framedItemRotationOffset = new BooleanOption("framedItemRotationOffset", true);

	/* toggling these two rebuilds cached game state, so their previous values are tracked */
	private boolean previousTrapDoorItemPosition = trapDoorItemPosition.get();
	private boolean previousOldJumpBoostPotionColor = oldJumpBoostPotionColor.get();

	/**
	 * The 1.7 features no longer have a master switch: every one of them is
	 * individually configurable, which makes a global toggle redundant.
	 */
	public static boolean isEnabled() {
		return true;
	}

	public void initConfig() {
		category.add(blockingItemUsing);
		blockingItemUsing.add(
			blockHitting,
			useAndMine,
			useAndMineParticles,
			useAndMineSound,
			allowMiningCancel,
			miningProgressResetLogic,
			blockingArm,
			swordBlockThirdPerson,
			oldSwingVisual,
			oldSwingVisualParticles
		);
		category.add(categorySneaking);
		categorySneaking.add(
			smoothSneaking,
			slowUpSneak,
			thirdPersonSneaking,
			fixThirdPersonHeldItemSneakDeSync,
			doubleTapSneak,
			stopLineTranslateSneak
		);
		category.add(categoryItems);
		categoryItems.add(
			mirroredProjectiles,
			droppedItemsFacePlayer,
			stickRod,
			equipLogic,
			framedItemLighting,
			oldJumpBoostPotionColor,
			goldenCarrotCreativeTab
		);
		categoryItems.add(categorySpriteRendering);
		categorySpriteRendering.add(
			droppedItemSprite,
			swapDroppedItemNormals,
			projectileSprite,
			swapProjectileSpriteNormals,
			framedItemSprite,
			swapFramedItemSpriteNormals
		);
		categoryItems.add(categoryItemPositions);
		categoryItemPositions.add(
			firstPersonPositions,
			thirdPersonPositions,
			framedItemPosition,
			oldBowRotation,
			trapDoorItemPosition
		);
		category.add(categoryOffsets);
		categoryOffsets.add(
			oldItemPickup,
			xpOrbPosition,
			flameOffset,
			creeperOffset,
			shadowOffset,
			horseRiderEyeHeight,
			sleepEyeHeight,
			potionEntityOffset,
			playerEyeHeightOffset,
			zombieVillagerHelmetOffset,
			framedItemRotationOffset
		);

		ConfigManager configManager = new VersionedJsonConfigManager(FabricLoader.getInstance().getConfigDir().resolve(OldAnimations.MODID + ".json"),
			category, 1, (configVersion, configVersion1, optionCategory, jsonObject) -> jsonObject);
		AxolotlClientConfig.getInstance().register(configManager);
		configManager.load();

		/* attempt to prevent calling resourcereload during game init */
		previousTrapDoorItemPosition = trapDoorItemPosition.get();
		previousOldJumpBoostPotionColor = oldJumpBoostPotionColor.get();

		/* reload the resources upon toggling certain options */
		reloadResources();
	}

	/**
	 * The trapdoor item model is baked at resource load, and potion colors are
	 * cached separately, so both need a nudge when their option is toggled.
	 */
	private void reloadResources() {
		MinecraftClientEvents.TICK_END.register(client -> {
			if (trapDoorItemPosition.get() != previousTrapDoorItemPosition) {
				previousTrapDoorItemPosition = trapDoorItemPosition.get();
				Minecraft.getInstance().reloadResources();
				reloadPotionColors = true;
				return;
			}
			if (oldJumpBoostPotionColor.get() != previousOldJumpBoostPotionColor) {
				previousOldJumpBoostPotionColor = oldJumpBoostPotionColor.get();
				reloadPotionColors = true;
			}
		});
	}
}
