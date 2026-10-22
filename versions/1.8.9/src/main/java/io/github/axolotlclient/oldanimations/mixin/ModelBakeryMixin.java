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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.model.ModelBakery;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

	@Shadow
	private Map<Item, List<String>> itemVariants;

	/**
	 * The 1.7 trapdoor item model is a separate variant, so it has to be
	 * registered before {@code ItemModelShaper} can hand it out.
	 */
	@Inject(method = "registerItemVariants", at = @At("TAIL"))
	private void axolotlclient$registerCustomModels(CallbackInfo ci) {
		itemVariants.put(Item.byBlock(Blocks.TRAPDOOR), Lists.newArrayList("trapdoor", "trapdoor_inventory"));
		itemVariants.put(Item.byBlock(Blocks.IRON_TRAPDOOR), Lists.newArrayList("iron_trapdoor", "iron_trapdoor_inventory"));
	}
}
