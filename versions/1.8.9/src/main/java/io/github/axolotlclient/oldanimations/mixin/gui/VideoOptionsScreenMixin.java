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

package io.github.axolotlclient.oldanimations.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VideoOptionsScreen.class)
public class VideoOptionsScreenMixin {

	@Dynamic("Spoof OptiFine Minecraft version")
	@ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "stringValue=Minecraft 1.8.9"), require = 0)
	private String axolotlclient$spoofOptiFineMinecraftVersion(String original) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.show1_7_10.get()) {
			/* hopefully optifine doesnt have 1.8.9 anywhere else */
			return "Minecraft 1.7.10";
		}
		return original;
	}
}
