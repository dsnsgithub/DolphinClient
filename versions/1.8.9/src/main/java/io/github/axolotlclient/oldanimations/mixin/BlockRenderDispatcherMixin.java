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

package io.github.axolotlclient.oldanimations.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.CustomModels;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.client.render.block.BlockRenderDispatcher;
import net.minecraft.client.resource.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {

	@ModifyArg(method = "renderMiningProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer;render(Lnet/minecraft/world/WorldView;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/block/state/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/vertex/BufferBuilder;)Z"), index = 1)
	private BakedModel axolotlclient$breakingModel(BakedModel miningModel, @Local Block block, @Local(ordinal = 0) BakedModel originalModel) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.replaceDestroyStageTexture.get()) {
			return miningModel;
		}

		if (block instanceof BedBlock) {
			return CustomModels.bedMiningModel(originalModel, miningModel);
		}
		if (block instanceof BrewingStandBlock) {
			return CustomModels.brewingStandMiningModel(originalModel, miningModel);
		}
		if (block instanceof BeaconBlock) {
			return originalModel;
		}
		return miningModel;
	}
}
