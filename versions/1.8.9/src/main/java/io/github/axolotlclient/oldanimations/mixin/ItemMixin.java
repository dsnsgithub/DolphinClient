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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.item.CreativeModeTab;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Item.class)
public abstract class ItemMixin {

	@WrapOperation(
		method = "init",
		slice = @Slice(
			from = @At(value = "CONSTANT", args = "intValue=396"),
			to = @At(value = "CONSTANT", args = "intValue=397")
		),
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;setCreativeModeTab(Lnet/minecraft/item/CreativeModeTab;)Lnet/minecraft/item/Item;")
	)
	private static Item axolotlclient$goldenCarrotCreativeTab(Item instance, CreativeModeTab creativeModeTab, Operation<Item> original) {
		/* such a sus injection point.. oh well lol */
		/* MC-3664 */
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.goldenCarrotCreativeTab.get()) {
			return instance;
		}
		return original.call(instance, creativeModeTab);
	}
}
