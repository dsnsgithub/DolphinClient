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

import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.OpaqueLeavesHandler;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.item.ItemModelShaper;
import net.minecraft.client.resource.ModelIdentifier;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.ModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelShaperMixin {

	@Shadow
	public abstract ModelManager getManager();

	@Inject(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/resource/model/BakedModel;", at = @At("HEAD"), cancellable = true)
 	private void axolotlclient$useCustomModels(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
 		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.skullModel.get() && stack.getItem() instanceof SkullItem) {
			/* MC-4772 */
			 String id = switch (stack.getMetadata()) {
				case 0 -> "old_skull_skeleton";
				case 1 -> "old_skull_wither";
				case 2 -> "old_skull_zombie";
				case 4 -> "old_skull_creeper";
				default -> "old_skull_char";
			};
			cir.setReturnValue(getManager().getModel(new ModelIdentifier(id, "inventory")));
 		}

		/* first feature im adding in 2026 lol. im rusty. sorry */
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.opaqueLeavesTextures.get()) {
			Block block = Block.byItem(stack.getItem());
			if (OpaqueLeavesHandler.isOpaqueLeavesBlock(block)) {
				BlockState state = block.getStateFromMetadata(stack.getMetadata());
				cir.setReturnValue(getManager().getModel(new ModelIdentifier(
					OpaqueLeavesHandler.getVariantName(block, state) + "_leaves_opaque", "inventory")));
			}
		}

		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.trapDoorItemPosition.get() ) {
			/* not sure why, 1.8 trapdoors have a top or bottom model */
			/* 1.7 has one that's like neutral which is what i've ported here */
			Block block = Block.byItem(stack.getItem());
			if (block instanceof TrapdoorBlock) {
				if (block.getMaterial() == Material.IRON) {
					cir.setReturnValue(getManager().getModel(new ModelIdentifier("iron_trapdoor_inventory", "inventory")));
				} else {
					cir.setReturnValue(getManager().getModel(new ModelIdentifier("trapdoor_inventory", "inventory")));
				}
			}
		}

		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.fastGrass.get() &&
			Block.byItem(stack.getItem()) instanceof GrassBlock) {
			/* unlike the block, the item never uses the fancy model in 1.7 */
			if (!OldAnimationsConfig.instance.fastGrassItem.get() && Minecraft.getInstance().options.fancyGraphics) {
				/* we can give the user the option tho for fun */
				return;
			}
			cir.setReturnValue(getManager().getModel(new ModelIdentifier("fast_grass", "inventory")));
		}

		/* this was a pain in the ass to figure out... */
		if (Block.byItem(stack.getItem()) instanceof LiquidBlock) {
			if (Block.byItem(stack.getItem()).getMaterial() == Material.WATER) {
				cir.setReturnValue(getManager().getModel(new ModelIdentifier("water", "inventory")));
			} else if (Block.byItem(stack.getItem()).getMaterial() == Material.LAVA) {
				cir.setReturnValue(getManager().getModel(new ModelIdentifier("lava", "inventory")));
			}
		}
		if (Block.byItem(stack.getItem()) instanceof FireBlock) {
			cir.setReturnValue(getManager().getModel(new ModelIdentifier("fire", "inventory")));
		}
 	}
}
