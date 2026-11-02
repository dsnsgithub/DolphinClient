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

package io.github.dolphinclient.oldanimations.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dolphinclient.oldanimations.config.OldAnimationsConfig;
import io.github.dolphinclient.oldanimations.util.GlintHandler;
import io.github.dolphinclient.oldanimations.util.GlintQuadModel;
import io.github.dolphinclient.oldanimations.util.ItemUtil;
import io.github.dolphinclient.oldanimations.util.OpaqueLeavesHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemRenderer.class /* C_6823239 */)
public abstract class ItemRendererMixin {

	@Shadow
	protected abstract void render(BakedModel model, int color);

	@Shadow
	protected abstract void render(BakedModel model, ItemStack stack);

	@Shadow
	public abstract boolean isGui3d(ItemStack itemStack);

	@Shadow
	@Final
	private static Identifier ENCHANTMENT_GLINT_LOCATION;

	@Shadow
	@Final
	private TextureManager textureManager;

	@Unique
	private BakedModel dolphinclient$model = null;

	@Unique
	private int dolphinclient$glintColor = -8372020;

	@Unique
	private boolean dolphinclient$isLayered;

	@Unique
	private boolean dolphinclient$fastGraphics;

	@Unique
	private ModelTransformations.Type dolphinclient$transform;

	@WrapMethod(method = "renderItem")
	private void dolphinclient$captureModel(ItemStack item, BakedModel model, Operation<Void> original) {
		dolphinclient$model = model;
		original.call(item, model);
		dolphinclient$model = null;
	}

	@WrapMethod(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V")
	private void dolphinclient$captureTransform(ItemStack item, BakedModel model, ModelTransformations.Type transform, Operation<Void> original) {
		dolphinclient$transform = transform;
		original.call(item, model, transform);
		dolphinclient$transform = null;
	}

	@WrapMethod(method = "renderGuiItemModel")
	private void dolphinclient$captureTransform2(ItemStack item, int x, int y, Operation<Void> original) {
		dolphinclient$transform = ModelTransformations.Type.GUI;
		original.call(item, x, y);
		dolphinclient$transform = null;
	}

	@ModifyArgs(method = "applyNormal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/vertex/BufferBuilder;postNormal(FFF)V"))
	private void dolphinclient$modifyNormals(Args args) {
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}
		if (dolphinclient$model == null || dolphinclient$model.isGui3d()) {
			/* please dont crash ;-; */
			return;
		}
		if (dolphinclient$shouldBeSpriteOrSwapNormal(true)) {
			args.setAll(args.get(0), args.get(2), args.get(1));
		}
	}

	@ModifyExpressionValue(method = "render(Lnet/minecraft/client/resource/model/BakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/model/BakedModel;getQuads()Ljava/util/List;"))
	private List<BakedQuad> dolphinclient$changeToSprite(List<BakedQuad> quads, @Local(argsOnly = true) BakedModel model) {
		if (!OldAnimationsConfig.isEnabled()) {
			return quads;
		}

		if (!model.isGui3d() && (dolphinclient$transform == ModelTransformations.Type.GUI || dolphinclient$shouldBeSpriteOrSwapNormal(false))) {
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

	@ModifyArg(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderEnchantmentGlint(Lnet/minecraft/client/resource/model/BakedModel;)V"))
	public BakedModel dolphinclient$replaceModel(BakedModel model) {
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGlint.get() ? GlintHandler.getModel(model) : model;
	}

	@ModifyArg(method = "render(Lnet/minecraft/client/resource/model/BakedModel;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;render(Lnet/minecraft/client/resource/model/BakedModel;ILnet/minecraft/item/ItemStack;)V"), index = 1)
	private int dolphinclient$replaceColor(int color) {
		dolphinclient$glintColor = color;
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGlintColor.get() &&
			dolphinclient$transform != ModelTransformations.Type.GUI ? -10407781 : color;
	}

	@WrapWithCondition(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderEnchantmentGlint(Lnet/minecraft/client/resource/model/BakedModel;)V"))
	private boolean dolphinclient$disableBlocksGlint(ItemRenderer instance, BakedModel bakedModel, @Local(argsOnly = true) ItemStack itemStack) {
		return !OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.disableGlintOnBlocks.get() || !isGui3d(itemStack);
	}

	@Inject(method = "renderEnchantmentGlint", at = @At("HEAD"), cancellable = true)
	private void dolphinclient$disableDefaultGlint(CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}

		boolean isGui = OldAnimationsConfig.instance.oldGuiGlint.get() && dolphinclient$transform == ModelTransformations.Type.GUI;
		if (isGui || dolphinclient$shouldBeSpriteOrSwapNormal(false)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "renderEnchantmentGlint", at = @At(value = "CONSTANT", args = "floatValue=8.0F"))
	private float dolphinclient$modifyScale(float original) {
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGlint.get() ? 1.0F / original : original;
	}

	@Inject(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private void dolphinclient$fastItemOffset(ItemStack stack, ModelTransformations.Type transformationType, CallbackInfo ci) {
		//todo: is this really needed? im not so sure anymore

		if (!OldAnimationsConfig.isEnabled()) {
			return;
		}

		if (dolphinclient$shouldBeSpriteOrSwapNormal(false)) {
			GlStateManager.translatef(0.0F, 0.0F, -0.0625F);
		}
	}

	@Inject(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;)V", shift = At.Shift.AFTER))
	private void dolphinclient$renderGuiGlint(ItemStack stack, int x, int y, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGuiGlint.get() && stack.hasEnchantmentGlint()) {
			if (OldAnimationsConfig.instance.disableGlintOnBlocks.get() && isGui3d(stack)) {
				return;
			}
			if (stack.hasEnchantmentGlint()) {
				GlStateManager.depthMask(false);
				GlStateManager.depthFunc(518);
				GlStateManager.disableLighting();
				GlStateManager.blendFuncSeparate(772, 1, 0, 0);
				textureManager.bind(ENCHANTMENT_GLINT_LOCATION);
				GlStateManager.pushMatrix();
				GlStateManager.scalef(0.5F, 0.5F, 0.5F);
				GlStateManager.translatef(0.0F, -0.25F, 0.0F);
				render(new GlintQuadModel((float) (Minecraft.getTime() % 3000L) / 3000.0F, 4.0F), dolphinclient$glintColor);
				render(new GlintQuadModel((float) (Minecraft.getTime() % 4873L) / 4873.0F, -1.0F), dolphinclient$glintColor);
				GlStateManager.popMatrix();
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
				GlStateManager.enableLighting();
				GlStateManager.depthFunc(515);
				GlStateManager.depthMask(true);
				textureManager.bind(TextureAtlas.BLOCKS_LOCATION);
			}
		}
	}

	//todo: AYO FIX THIS SHIT

	@ModifyVariable(method = "renderQuads", at = @At(value = "HEAD"), argsOnly = true)
	private List<BakedQuad> dolphinclient$filterModelQuads(List<BakedQuad> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGlintLayer.get() &&
			/* just to be safe, let's skip rendering while projectiles and dropped items are 2d */
			(!OldAnimationsConfig.instance.droppedItemSprite.get() ||
				dolphinclient$transform == ModelTransformations.Type.FIRST_PERSON ||
				dolphinclient$transform == ModelTransformations.Type.THIRD_PERSON)) {
			/* by default, items with multiple layers are rendered in multiple passes. MC-4660 and MC-4329 */
			/* in 1.7, the enchantment glint only shows on the base layer for whatever reason */
			List<BakedQuad> filtered = new ArrayList<>(original.size());
			for (BakedQuad quad : original) {
				if (dolphinclient$isLayered == (quad.getTintIndex() > 0)) {
					filtered.add(quad);
				}
			}
			return filtered;
		}
		return original;
	}

	@Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;popMatrix()V"))
	private void dolphinclient$renderAdditionalModelLayers(ItemStack itemStack, BakedModel bakedModel, CallbackInfo ci) {
		if (OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.oldGlintLayer.get() && !bakedModel.isCustomRenderer()
			&& !bakedModel.isGui3d() && /* gotta make sure we aren't rendering extra crap */
			(!OldAnimationsConfig.instance.droppedItemSprite.get() ||
				dolphinclient$transform == ModelTransformations.Type.FIRST_PERSON ||
				dolphinclient$transform == ModelTransformations.Type.THIRD_PERSON)) {
			/* additional layers will be rendered without the glint :> */
			dolphinclient$isLayered = true;
			render(bakedModel, itemStack);
			dolphinclient$isLayered = false;
		}
	}

	//todo: can this feature be more concise? i have like 4 mixin injections. hmmm
	@Inject(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;blendFuncSeparate(IIII)V", shift = At.Shift.AFTER))
	private void dolphinclient$fastGraphicsLeavesPre$held(ItemStack item, BakedModel model, ModelTransformations.Type transform, CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.oldFastLeaves.get()
			|| OldAnimationsConfig.instance.opaqueLeavesTextures.get()) {
			return;
		}
		dolphinclient$fastGraphics = false;
		if (item.getItem() instanceof BlockItem && OpaqueLeavesHandler.isOpaqueLeavesBlock(Block.byItem(item.getItem()))) {
			/* MC-57356 */
			/* to my knowledge, leaf blocks are the only blocks affected by fast/fancy graphics */
			/* when it comes to their render layer. if theyre in their solid mode when it comes to world rendering */
			/* then we know fast graphics was enabled and so we can make the rendered item solid as well */
			/* we wont be messing with any other blocks */
			dolphinclient$fastGraphics = true;
			GlStateManager.disableAlphaTest();
			GlStateManager.disableBlend();
		}
	}

	@Inject(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;)V", shift = At.Shift.AFTER))
	private void dolphinclient$fastGraphicsLeavesPost$held(ItemStack item, BakedModel model, ModelTransformations.Type transform, CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.oldFastLeaves.get()
			|| OldAnimationsConfig.instance.opaqueLeavesTextures.get()) {
			return;
		}
		if (dolphinclient$fastGraphics) {
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			dolphinclient$fastGraphics = false;
		}
	}

	@Inject(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;blendFunc(II)V", shift = At.Shift.AFTER))
	private void dolphinclient$fastGraphicsLeavesPre$gui(ItemStack item, int x, int y, CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.oldFastLeaves.get()
			|| OldAnimationsConfig.instance.opaqueLeavesTextures.get()) {
			return;
		}
		dolphinclient$fastGraphics = false;
		if (item.getItem() instanceof BlockItem && OpaqueLeavesHandler.isOpaqueLeavesBlock(Block.byItem(item.getItem()))) {
			/* ditto */
			dolphinclient$fastGraphics = true;
			GlStateManager.disableAlphaTest();
			GlStateManager.disableBlend();
		}
	}

	@Inject(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;)V", shift = At.Shift.AFTER))
	private void dolphinclient$fastGraphicsLeavesPost$gui(ItemStack item, int x, int y, CallbackInfo ci) {
		if (!OldAnimationsConfig.isEnabled() || !OldAnimationsConfig.instance.oldFastLeaves.get()
			|| OldAnimationsConfig.instance.opaqueLeavesTextures.get()) {
			return;
		}
		if (dolphinclient$fastGraphics) {
			/* vanilla calls disableAlphaTest() right after this anyway, so we only */
			/* need to restore blend here to avoid leaking a disabled-blend state */
			GlStateManager.enableBlend();
			dolphinclient$fastGraphics = false;
		}
	}

	@ModifyArg(method = "renderGuiItemModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/texture/Texture;pushFilter(ZZ)V"), index = 1)
	private boolean dolphinclient$guiItemsMipmap(boolean original) {
		/* MC-57574 and MC-277768. idk why there was 2 bug reports */
		/* in 1.7, the gui block items had mipmapping affect them since they were the same blocks used for world rendering */
		/* in 1.8, there is a distinction between block items and actual world rendered blocks */
		/* luckily it's easy to turn on mipmapping for rendered items */
		return OldAnimationsConfig.isEnabled() && OldAnimationsConfig.instance.guiBlockItemsMipmap.get() ?
			Minecraft.getInstance().options.mipmapLevels > 0 : original;
	}

	@Inject(method = "renderGuiItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderGuiItemModel(Lnet/minecraft/item/ItemStack;II)V"))
	private void dolphinclient$addDepth(ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
		/* this fixes that shiny glint effect at the root */
		if (OldAnimationsConfig.isEnabled()) {
			GlStateManager.enableDepthTest();
		}
	}

	@WrapWithCondition(method = "renderItemInHand(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resource/model/BakedModel;Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/block/ModelTransformations;apply(Lnet/minecraft/client/render/model/block/ModelTransformations$Type;)V"))
	private boolean dolphinclient$disableResourcePackTransformations(ModelTransformations instance, ModelTransformations.Type type, @Local(argsOnly = true) ItemStack item) {
		if (!OldAnimationsConfig.isEnabled() || ItemUtil.INSTANCE.isCustomRenderer(item)) {
			return true;
		}

		return (!OldAnimationsConfig.instance.firstPersonPositions.get() || type != ModelTransformations.Type.FIRST_PERSON) &&
			(!OldAnimationsConfig.instance.thirdPersonPositions.get() || type != ModelTransformations.Type.THIRD_PERSON);
	}

	@Unique
	private boolean dolphinclient$shouldBeSpriteOrSwapNormal(boolean swapNormals) {
		/* this is a nightmare lmao */
		boolean isGround = dolphinclient$transform == ModelTransformations.Type.GROUND;
		boolean isThrown = ItemUtil.INSTANCE.getType() == ItemUtil.GroundType.THROWN;
		boolean isDropped = ItemUtil.INSTANCE.getType() == ItemUtil.GroundType.DROPPED;
		boolean isFramed = dolphinclient$transform == ModelTransformations.Type.FIXED;

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
