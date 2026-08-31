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
import net.minecraft.block.Block;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.render.item.ItemModelShaper;
import net.minecraft.client.resource.ModelIdentifier;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.ModelManager;
import net.minecraft.item.ItemStack;
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
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.trapDoorItemPosition.get()) {
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
	}
}
