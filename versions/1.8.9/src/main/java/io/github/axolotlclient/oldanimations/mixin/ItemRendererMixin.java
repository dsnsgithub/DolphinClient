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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.oldanimations.config.OldAnimationsConfig;
import io.github.axolotlclient.oldanimations.util.ItemUtil;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemRenderer.class /* C_6823239 */)
public abstract class ItemRendererMixin {

	@Unique
	private BakedModel axolotlclient$model = null;

	@Unique
	private ModelTransformations.Type axolotlclient$transform;

	@WrapMethod(method = "renderItem")
	private void axolotlclient$captureModel(ItemStack item, BakedModel model, Operation<Void> original) {
		axolotlclient$model = model;
		original.call(item, model);
		axolotlclient$model = null;
	}

	@WrapMethod(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V")
	private void axolotlclient$captureTransform(ItemStack item, BakedModel model, ModelTransformations.Type transform, Operation<Void> original) {
		axolotlclient$transform = transform;
		original.call(item, model, transform);
		axolotlclient$transform = null;
	}

	@WrapMethod(method = "renderGuiItemModel")
	private void axolotlclient$captureTransform2(ItemStack item, int x, int y, Operation<Void> original) {
		axolotlclient$transform = ModelTransformations.Type.GUI;
		original.call(item, x, y);
		axolotlclient$transform = null;
	}

	@ModifyArgs(method = "applyNormal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;postNormal(FFF)V"))
	private void axolotlclient$modifyNormals(Args args) {
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}
		if (axolotlclient$model == null || axolotlclient$model.isGui3d()) {
			/* please dont crash ;-; */
			return;
		}
		if (axolotlclient$shouldBeSpriteOrSwapNormal(true)) {
			args.setAll(args.get(0), args.get(2), args.get(1));
		}
	}

	@ModifyExpressionValue(method = "render(Lnet/minecraft/client/resource/model/BakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/model/BakedModel;getQuads()Ljava/util/List;"))
	private List<BakedQuad> axolotlclient$changeToSprite(List<BakedQuad> quads, @Local(argsOnly = true) BakedModel model) {
		if (!OldAnimationsConfig.isEnabled()) {
			return quads;
		}

		if (!model.isGui3d() && (axolotlclient$transform == ModelTransformations.Type.GUI || axolotlclient$shouldBeSpriteOrSwapNormal(false))) {
			/* this is the easiest way to have fast items with the 1.8 model system */
			/* even forge does this xD */
			List<BakedQuad> filtered = new ArrayList<>(quads.size());
			for (BakedQuad quad : quads) {
				if (quad.getFace() == Direction.SOUTH) {
					filtered.add(quad);
				}
			}
			return filtered;
		}
		return quads;
	}

	@Inject(method = "renderEnchantmentGlint", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$disableDefaultGlint(CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}

		if (axolotlclient$shouldBeSpriteOrSwapNormal(false)) {
			ci.cancel();
		}
	}

	@Inject(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private void axolotlclient$fastItemOffset(ItemStack stack, ModelTransformations.Type transformationType, CallbackInfo ci) {
		//todo: is this really needed? im not so sure anymore

		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}

		if (axolotlclient$shouldBeSpriteOrSwapNormal(false)) {
			GlStateManager.translatef(0.0F, 0.0F, -0.0625F);
		}
	}

	//todo: AYO FIX THIS SHIT

	@Inject(method = "renderGuiItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderGuiItemModel(Lnet/minecraft/item/ItemStack;II)V"))
	private void axolotlclient$addDepth(ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
		/* this fixes that shiny glint effect at the root */
		if (OldAnimationsConfig.isEnabled()) {
			GlStateManager.enableDepthTest();
		}
	}

	@WrapWithCondition(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/block/ModelTransformations;apply(Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private boolean axolotlclient$disableResourcePackTransformations(ModelTransformations instance, ModelTransformations.Type type, @Local(argsOnly = true) ItemStack item) {
		if (!OldAnimationsConfig.isEnabled() || ItemUtil.INSTANCE.isCustomRenderer(item)) {
			return true;
		}

		return (!OldAnimationsConfig.instance.firstPersonPositions.get() || type != ModelTransformations.Type.FIRST_PERSON) &&
			(!OldAnimationsConfig.instance.thirdPersonPositions.get() || type != ModelTransformations.Type.THIRD_PERSON);
	}

	@Unique
	private boolean axolotlclient$shouldBeSpriteOrSwapNormal(boolean swapNormals) {
		/* this is a nightmare lmao */
		boolean isGround = axolotlclient$transform == ModelTransformations.Type.GROUND;
		boolean isThrown = ItemUtil.INSTANCE.getType() == ItemUtil.GroundType.THROWN;
		boolean isDropped = ItemUtil.INSTANCE.getType() == ItemUtil.GroundType.DROPPED;
		boolean isFramed = axolotlclient$transform == ModelTransformations.Type.FIXED;

		boolean thrownOption = OldAnimationsConfig.instance.projectileSprite.get();
		boolean droppedOption = OldAnimationsConfig.instance.droppedItemSprite.get();
		boolean framedOption = OldAnimationsConfig.instance.framedItemSprite.get();

		if (swapNormals) {
			thrownOption = OldAnimationsConfig.instance.swapProjectileSpriteNormals.get();
			droppedOption = OldAnimationsConfig.instance.swapDroppedItemNormals.get();
			framedOption = OldAnimationsConfig.instance.swapFramedItemSpriteNormals.get();
		}

		return (framedOption && isFramed) || (isGround && ((droppedOption && isDropped) || (thrownOption && isThrown)));
	}
}
