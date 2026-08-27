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

import java.util.function.BooleanSupplier;

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
	private final OptionCategory categoryResources = OptionCategory.create("resources");
	private final OptionCategory categoryModels = OptionCategory.create("models");
	private final OptionCategory categoryTextures = OptionCategory.create("textures");
	private final OptionCategory categorySounds = OptionCategory.create("sounds");
	private final OptionCategory categoryParticles = OptionCategory.create("particles");
	private final OptionCategory categoryCombat = OptionCategory.create("combat");
	private final OptionCategory categoryGUI = OptionCategory.create("gui");
	private final OptionCategory categoryDebugOverlay = OptionCategory.create("debugOverlay");
	private final OptionCategory categoryTabOverlay = OptionCategory.create("tabOverlay");
	private final OptionCategory categoryEnchantmentGlint = OptionCategory.create("enchantmentGlint");
	private final OptionCategory categoryHitbox = OptionCategory.create("hitbox");
	private final OptionCategory categoryOffsets = OptionCategory.create("offsets");

	public final BooleanOption useAndMine = new BooleanOption("useAndMine", true);
	public final BooleanOption useAndMineParticles = new BooleanOption("useAndMineParticles", true);
	public final BooleanOption blockHitting = new BooleanOption("blockHitting", true);
	public final BooleanOption firstPersonPositions = new BooleanOption("firstPersonPositions", true);
	public final BooleanOption secondLayerDamageTint = new BooleanOption("secondLayerDamageTint", true);
	public final BooleanOption smoothSneaking = new BooleanOption("smoothSneaking", true);
	public final BooleanOption heartFlashing = new BooleanOption("heartFlashing", true);
	public final BooleanOption show1_7_10 = new BooleanOption("show1_7_10", true);
	public final BooleanOption debugInfo = new BooleanOption("debugInfo", true);
	public final BooleanOption disableDebugBackground = new BooleanOption("disableDebugBackground", true);
	public final BooleanOption alwaysShowCrosshair = new BooleanOption("alwaysShowCrosshair", true);
	public final BooleanOption debugTextSpacing = new BooleanOption("debugTextSpacing", true);
	public final BooleanOption debugTextColorScheme = new BooleanOption("debugTextColorScheme", true);
	public final BooleanOption debugTextShadow = new BooleanOption("debugTextShadow", true);
	public final BooleanOption thirdPersonSneaking = new BooleanOption("thirdPersonSneaking", true);
	public final BooleanOption allowMiningCancel = new BooleanOption("allowMiningCancel", true);
	public final BooleanOption damageTintColor = new BooleanOption("damageTintColor", true);
	public final BooleanOption stickRod = new BooleanOption("stickRod", true);
	public final BooleanOption blockingArm = new BooleanOption("blockingArm", true);
	public final BooleanOption droppedItemSprite = new BooleanOption("droppedItemSprite", true);
	public final BooleanOption mirroredProjectiles = new BooleanOption("mirroredProjectiles", true);
	public final BooleanOption flameOffset = new BooleanOption("flameOffset", true);
	public final BooleanOption disableTitles = new BooleanOption("disableTitles", true);
	public final BooleanOption oldItemPickup = new BooleanOption("oldItemPickup", true);
	public final BooleanOption oldGlint = new BooleanOption("oldGlint", true);
	public final BooleanOption oldGuiGlint = new BooleanOption("oldGuiGlint", true);
	public final BooleanOption oldGlintColor = new BooleanOption("oldGlintColor", true);
	public final BooleanOption centeredSelectionMenus = new BooleanOption("centeredSelectionMenus", true);
	public final BooleanOption tabDimensions = new BooleanOption("tabDimensions", true);
	public final BooleanOption disableTabPlayerHeads = new BooleanOption("disableTabPlayerHeads", true);
	public final BooleanOption disableTabHeader = new BooleanOption("disableTabHeader", true);
	public final BooleanOption disableTabFooter = new BooleanOption("disableTabFooter", true);
	public final BooleanOption equipLogic = new BooleanOption("equipLogic", true);
	public final BooleanOption oldDamageTick = new BooleanOption("oldDamageTick", true);
	public final BooleanOption oldSwingVisual = new BooleanOption("oldSwingVisual", true);
	public final BooleanOption oldSwingVisualParticles = new BooleanOption("oldSwingVisualParticles", true);
	public final BooleanOption slowUpSneak = new BooleanOption("slowUpSneak", true);
	public final BooleanOption stopLineTranslateSneak = new BooleanOption("stopLineTranslateSneak", true);
	public final BooleanOption oldGlintLayer = new BooleanOption("oldGlintLayer", true);
	public final BooleanOption skullModel = new BooleanOption("skullModel", true);
	public final BooleanOption xpOrbPosition = new BooleanOption("xpOrbPosition", true);
	public final BooleanOption disableServerSelectionButtons = new BooleanOption("disableServerSelectionButtons", true);
	public final BooleanOption disableUnknownServerIcon = new BooleanOption("disableUnknownServerIcon", true);
	public final BooleanOption disableSkinCustomizationButton = new BooleanOption("disableSkinCustomizationButton", true);
	public final BooleanOption oldMultiplayerSettingsPage = new BooleanOption("oldMultiplayerSettingsPage", true);
	public final BooleanOption fixThirdPersonHeldItemSneakDeSync = new BooleanOption("fixThirdPersonHeldItemSneakDeSync", true);
	public final BooleanOption oldBowRotation = new BooleanOption("oldBowRotation", true);
	public final BooleanOption swordBlockThirdPerson = new BooleanOption("swordBlockThirdPerson", true);
	public final BooleanOption fastGrass = new BooleanOption("fastGrass", true);
	public final BooleanOption difficultyLogic = new BooleanOption("difficultyLogic", true);
	public final BooleanOption moveSprintKeybind = new BooleanOption("moveSprintKeybind", true);
	public final BooleanOption oldDamageTintLighting = new BooleanOption("oldDamageTintLighting", true);
	public final BooleanOption refreshResourcesRegardless = new BooleanOption("refreshResourcesRegardless", true);
	public final BooleanOption removeHitboxEyeLine = new BooleanOption("removeHitboxEyeLine", true);
	public final BooleanOption hitboxOffset = new BooleanOption("hitboxOffset", true);
	public final BooleanOption disableGlintOnBlocks = new BooleanOption("disableGlintOnBlocks", true);
	public final BooleanOption separateDamageTintFromGlint = new BooleanOption("separateDamageTintFromGlint", true);
	public final BooleanOption doubleTapSneak = new BooleanOption("doubleTapSneak", true);
	public final BooleanOption framedItemLighting = new BooleanOption("framedItemLighting", true);
	public final BooleanOption clientSideEntityMovement = new BooleanOption("clientSideEntityMovement", true);
	public final BooleanOption voidFog = new BooleanOption("voidFog", true);
	public final BooleanOption dontSortTabEntries = new BooleanOption("dontSortTabEntries", true);
	public final BooleanOption hideScoreboardHearts = new BooleanOption("hideScoreboardHearts", true);
	public final BooleanOption oldObjectivesPosition = new BooleanOption("oldObjectivesPosition", true);
	public final BooleanOption miningProgressResetLogic = new BooleanOption("miningProgressResetLogic", true);
	public final BooleanOption rowBasedEntryOrder = new BooleanOption("rowBasedEntryOrder", true);
	public final BooleanOption dontUpdateEffectsHud = new BooleanOption("dontUpdateEffectsHud", true);
	public final BooleanOption inventoryTextLighting = new BooleanOption("inventoryTextLighting", true);
	public final BooleanOption oldJumpBoostPotionColor = new BooleanOption("oldJumpBoostPotionColor", true);
	public final BooleanOption controlsListButtonHeight = new BooleanOption("controlsListButtonHeight", true);
	public final BooleanOption fire = new BooleanOption("fire", true);
	public final BooleanOption eggEntityCollisionParticles = new BooleanOption("eggEntityCollisionParticles", true);
	public final BooleanOption fallParticles = new BooleanOption("fallParticles", true);
	public final BooleanOption noSkullLayerDamageTint = new BooleanOption("noSkullLayerDamageTint", true);
	public final BooleanOption fireChargeSound = new BooleanOption("fireChargeSound", true);
	public final BooleanOption skullBlockRendering = new BooleanOption("skullBlockRendering", true);
	public final BooleanOption creeperOffset = new BooleanOption("creeperOffset", true);
	public final BooleanOption hopperSound = new BooleanOption("hopperSound", true);
	public final BooleanOption fireSound = new BooleanOption("fireSound", true);
	public final BooleanOption disconnectScreen = new BooleanOption("disconnectScreen", true);
	public final BooleanOption disconnectServerToTitleScreen = new BooleanOption("disconnectServerToTitleScreen", true);
	public final BooleanOption customizeWorldPresetIcons = new BooleanOption("customizeWorldPresetIcons", true);
	public final BooleanOption packFormatWarning = new BooleanOption("packFormatWarning", true);
	public final BooleanOption goldenCarrotCreativeTab = new BooleanOption("goldenCarrotCreativeTab", true);
	public final BooleanOption fastSmoothLighting = new BooleanOption("fastSmoothLighting", true);
	public final BooleanOption litFurnaceMinecart = new BooleanOption("litFurnaceMinecart", true);
	public final BooleanOption tallBlockBreakSync = new BooleanOption("tallBlockBreakSync", true);
	public final BooleanOption buggedChatOpacity = new BooleanOption("buggedChatOpacity", true);
	public final BooleanOption modelShadeAndAmbientOcclusion = new BooleanOption("modelShadeAndAmbientOcclusion", true);
	public final BooleanOption fenceGateItemModel = new BooleanOption("fenceGateItemModel", true);
	public final BooleanOption fenceGateWallMode = new BooleanOption("fenceGateWallMode", true);
	public final BooleanOption itemModelSideQuadRendering = new BooleanOption("itemModelSideQuadRendering", false);
	public final BooleanOption oldFastLeaves = new BooleanOption("oldFastLeaves", true);
	public final BooleanOption opaqueLeavesTextures = new BooleanOption("opaqueLeavesTextures", true);
	public final BooleanOption guiBlockItemsMipmap = new BooleanOption("guiBlockItemsMipmap", true);
	public final BooleanOption tntZFighting = new BooleanOption("tntZFighting", true);
	public final BooleanOption shadowOffset = new BooleanOption("shadowOffset", true);
	public final BooleanOption showShadowInInventory = new BooleanOption("showShadowInInventory", true);
	public final BooleanOption removeHitboxEyeVector = new BooleanOption("removeHitboxEyeVector", true);
	public final BooleanOption randomizeDoorSound = new BooleanOption("randomizeDoorSound", true);
	public final BooleanOption witherArmorTexture = new BooleanOption("witherArmorTexture", true);
	public final BooleanOption horseRiderEyeHeight = new BooleanOption("horseRiderEyeHeight", true);
	public final BooleanOption sleepEyeHeight = new BooleanOption("sleepEyeHeight", true);
	public final BooleanOption mipmapPrecision = new BooleanOption("mipmapPrecision", true);
	public final BooleanOption dispenserClickSound = new BooleanOption("dispenserClickSound", true);
	public final BooleanOption potionEntityOffset = new BooleanOption("potionEntityOffset", true);
	public final BooleanOption playerEyeHeightOffset = new BooleanOption("playerEyeHeightOffset", true);
	public final BooleanOption dontShowFallParticlesOnFences = new BooleanOption("dontShowFallParticlesOnFences", true);
	public final BooleanOption zombieVillagerHelmetOffset = new BooleanOption("zombieVillagerHelmetOffset", true);
	public final BooleanOption mobSizeDimensions = new BooleanOption("mobSizeDimensions", true);
	public final BooleanOption droppedItemsFacePlayer = new BooleanOption("droppedItemsFacePlayer", true);
	public final BooleanOption dontAsyncReloadResources = new BooleanOption("dontAsyncReloadResources", true);
	public final BooleanOption inventoryPressurePlateDimensions = new BooleanOption("inventoryPressurePlateDimensions", true);
	public final BooleanOption mipmapAllBlocks = new BooleanOption("mipmapAllBlocks", true);
	public final BooleanOption projectileSprite = new BooleanOption("projectileSprite", true);
	public final BooleanOption swapDroppedItemNormals = new BooleanOption("swapDroppedItemNormals", true);
	public final BooleanOption swapProjectileSpriteNormals = new BooleanOption("swapProjectileSpriteNormals", true);
	public final BooleanOption framedItemSprite = new BooleanOption("framedItemSprite", true);
	public final BooleanOption swapFramedItemSpriteNormals = new BooleanOption("swapFramedItemSpriteNormals", true);
	public final BooleanOption framedItemPosition = new BooleanOption("framedItemPosition", true);
	public final BooleanOption thirdPersonPositions = new BooleanOption("thirdPersonPositions", true);
	public final BooleanOption trapDoorItemPosition = new BooleanOption("trapDoorItemPosition", true);
	public final BooleanOption disableItemEntitySplashSound = new BooleanOption("disableItemEntitySplashSound", true);
	public final BooleanOption useAndMineSound = new BooleanOption("useAndMineSound", true);
	public final BooleanOption disableItemEntitySplashParticles = new BooleanOption("disableItemEntitySplashParticles", true);
	public final BooleanOption fastGrassItem = new BooleanOption("fastGrassItem", true);
	public final BooleanOption framedItemRotationOffset = new BooleanOption("framedItemRotationOffset", true);

	private final BooleanSupplier[] suppliers = new BooleanSupplier[]{
		skullModel::get,
		fastGrass::get,
		fire::get,
		oldJumpBoostPotionColor::get,
		fastSmoothLighting::get,
		modelShadeAndAmbientOcclusion::get,
		fenceGateItemModel::get,
		fenceGateWallMode::get,
		itemModelSideQuadRendering::get,
		opaqueLeavesTextures::get,
		guiBlockItemsMipmap::get,
		mipmapPrecision::get,
//		creeperOffset::get,
//		zombieVillagerHelmetOffset::get,
		inventoryPressurePlateDimensions::get,
		mipmapAllBlocks::get,
		trapDoorItemPosition::get
	};
	private final boolean[] previousStates = {
		skullModel.get(),
		fastGrass.get(),
		fire.get(),
		oldJumpBoostPotionColor.get(),
		fastSmoothLighting.get(),
		modelShadeAndAmbientOcclusion.get(),
		fenceGateItemModel.get(),
		fenceGateWallMode.get(),
		itemModelSideQuadRendering.get(),
		opaqueLeavesTextures.get(),
		guiBlockItemsMipmap.get(),
		mipmapPrecision.get(),
//		creeperOffset.get(),
//		zombieVillagerHelmetOffset.get(),
		inventoryPressurePlateDimensions.get(),
		mipmapAllBlocks.get(),
		trapDoorItemPosition.get()
	};

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
		category.add(categoryResources);
		categoryResources.add(
			mipmapPrecision,
			mipmapAllBlocks,
			guiBlockItemsMipmap,
			oldFastLeaves,
			litFurnaceMinecart,
			tallBlockBreakSync,
			tntZFighting,
			dontAsyncReloadResources
		);
		categoryResources.add(categoryModels);
		categoryModels.add(
			skullModel,
			fastGrass,
			fastGrassItem,
			fire,
			skullBlockRendering,
			fenceGateItemModel,
			fenceGateWallMode,
			itemModelSideQuadRendering,
			modelShadeAndAmbientOcclusion,
			fastSmoothLighting,
			inventoryPressurePlateDimensions
		);
		categoryResources.add(categoryTextures);
		categoryTextures.add(
			opaqueLeavesTextures,
			witherArmorTexture
		);
		categoryResources.add(categorySounds);
		categorySounds.add(
			fireChargeSound,
			hopperSound,
			fireSound,
			randomizeDoorSound,
			dispenserClickSound,
			disableItemEntitySplashSound
		);
		categoryResources.add(categoryParticles);
		categoryParticles.add(
			eggEntityCollisionParticles,
			fallParticles,
			dontShowFallParticlesOnFences,
			disableItemEntitySplashParticles
		);
		category.add(categoryCombat);
		categoryCombat.add(
			clientSideEntityMovement,
			secondLayerDamageTint,
			damageTintColor,
			oldDamageTick,
			oldDamageTintLighting,
			separateDamageTintFromGlint,
			noSkullLayerDamageTint
		);
		category.add(categoryGUI);
		categoryGUI.add(
			show1_7_10,
			heartFlashing,
			alwaysShowCrosshair,
			centeredSelectionMenus,
			disableServerSelectionButtons,
			disableUnknownServerIcon,
			disableSkinCustomizationButton,
			oldMultiplayerSettingsPage,
			difficultyLogic,
			refreshResourcesRegardless,
			moveSprintKeybind,
			controlsListButtonHeight,
			disableTitles,
			dontUpdateEffectsHud,
			inventoryTextLighting,
			disconnectScreen,
			disconnectServerToTitleScreen,
			customizeWorldPresetIcons,
			packFormatWarning,
			buggedChatOpacity,
			showShadowInInventory
		);
		categoryGUI.add(categoryDebugOverlay);
		categoryDebugOverlay.add(
			debugInfo,
			disableDebugBackground,
			debugTextSpacing,
			debugTextColorScheme,
			debugTextShadow
		);
		categoryGUI.add(categoryTabOverlay);
		categoryTabOverlay.add(
			tabDimensions,
			disableTabPlayerHeads,
			disableTabHeader,
			disableTabFooter,
			hideScoreboardHearts,
			dontSortTabEntries,
			rowBasedEntryOrder,
			oldObjectivesPosition
		);
		category.add(categoryEnchantmentGlint);
		categoryEnchantmentGlint.add(
			oldGlint,
			oldGuiGlint,
			oldGlintColor,
			oldGlintLayer,
			disableGlintOnBlocks
		);
		category.add(categoryHitbox);
		categoryHitbox.add(
			removeHitboxEyeLine,
			removeHitboxEyeVector,
			hitboxOffset,
			mobSizeDimensions
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
		for (int i = 0; i < suppliers.length; i++) {
			previousStates[i] = suppliers[i].getAsBoolean();
		}

		/* reload the resources upon toggling certain options */
		reloadResources();
	}

	//todo: rewrite this to be more readable. also need to better document whats going on here
	private void reloadResources() {
		MinecraftClientEvents.TICK_END.register(client -> {
			boolean needsReload = false;
			boolean reloadPotionCache = false;
			boolean reloadWorld = false;
			for (int i = 0; i < suppliers.length; i++) {
				boolean current = suppliers[i].getAsBoolean();
				if (current != previousStates[i]) {
					previousStates[i] = current;
					if (i == 3) {
						reloadPotionCache = true;
					} else if (i == 1 || i == 4 || i == 7 || i == 9) {
						reloadWorld = true;
					} else {
						needsReload = true;
					}
				}
			}
			if (needsReload) {
				Minecraft.getInstance().reloadResources();
				reloadPotionColors = true;
			} else {
				if (reloadPotionCache) reloadPotionColors = true;
				if (reloadWorld) Minecraft.getInstance().worldRenderer.reload();
			}
		});
	}
}
