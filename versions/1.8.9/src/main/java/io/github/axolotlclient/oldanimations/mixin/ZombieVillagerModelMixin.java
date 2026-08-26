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
import net.minecraft.client.render.model.entity.ZombieVillagerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ZombieVillagerModel.class)
public class ZombieVillagerModelMixin {

	@ModifyArg(method = "<init>(FFZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/ModelPart;addBox(FFFIIIF)V", ordinal = 0), index = 4)
	private int axolotlclient$zombieVillagerHelmet(int sizeY) {
		/* yet another barely visible change lol */
		/* how often does a zombie villager have a helmet??? */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.zombieVillagerHelmetOffset.get() ? 6 : sizeY;
	}
}
