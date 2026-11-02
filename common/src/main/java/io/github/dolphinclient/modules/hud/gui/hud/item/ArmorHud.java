/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
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

package io.github.dolphinclient.modules.hud.gui.hud.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.dolphinclient.bridge.BridgeVersion;
import io.github.dolphinclient.bridge.item.AxoEnchants;
import io.github.dolphinclient.bridge.item.AxoItemStack;
import io.github.dolphinclient.bridge.item.AxoItems;
import io.github.dolphinclient.bridge.render.AxoRenderContext;
import io.github.dolphinclient.bridge.util.AxoIdentifier;
import io.github.dolphinclient.modules.hud.gui.entry.TextHudEntry;
import io.github.dolphinclient.modules.hud.gui.layout.AnchorPoint;
import io.github.dolphinclient.modules.hud.gui.layout.CardinalOrder;
import io.github.dolphinclient.modules.hud.util.DefaultOptions;
import io.github.dolphinclient.modules.hud.util.DrawPosition;
import io.github.dolphinclient.util.ClientColors;
import io.github.dolphinclient.util.CommonUtil;
import io.github.dolphinclient.util.ItemUtil;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * <p>License: GPL-3.0</p>
 */
public class ArmorHud extends TextHudEntry {

	public static final AxoIdentifier ID = AxoIdentifier.of("kronhud", "armorhud");
	private static final Supplier<AxoItemStack> PLACEHOLDER_MAIN_HAND = CommonUtil.memoize(() -> AxoItemStack.of(AxoItems.IRON_SWORD));
	private static final Supplier<List<AxoItemStack>> PLACEHOLDER_GEAR = CommonUtil.memoize(() -> List.of(
		AxoItemStack.of(AxoItems.IRON_BOOTS),
		AxoItemStack.of(AxoItems.IRON_LEGGINGS),
		AxoItemStack.of(AxoItems.IRON_CHESTPLATE),
		AxoItemStack.of(AxoItems.IRON_HELMET)
	));

	protected final BooleanOption showProtLvl = new BooleanOption("showProtectionLevel", false);
	private final BooleanOption showDurabilityNumber = new BooleanOption("show_durability_num", false);
	private final BooleanOption showMaxDurabilityNumber = new BooleanOption("show_max_durability_num", false);
	private final BooleanOption customDurabilityNumColor = new BooleanOption("armorhud.custom_durability_num_color",
		false);
	private final ColorOption durabilityNumColor = new ColorOption("armorhud.durability_num_color", Colors.WHITE);
	private final EnumOption<MainHandItemPosition> mainHandItemPosition = new EnumOption<>("armorhud" +
		".main_hand_item_position", MainHandItemPosition.class, MainHandItemPosition.BOTTOM);
	private final EnumOption<CardinalOrder> order = DefaultOptions.getCardinalOrder(CardinalOrder.LEFT_RIGHT);
	private final BooleanOption reverseArmorOrder = new BooleanOption("armorhud.reverse_armor_order", false);
	private final BooleanOption hideIfEmpty = DefaultOptions.getHideIfEmpty();

	public ArmorHud() {
		super(20, 100, true);
	}

	@Override
	public void render(AxoRenderContext ctx, float delta) {
		final var player = client.br$getPlayer();
		if (player == null) {
			return;
		}
		if (hideIfEmpty.get() && Stream.concat(mainHandItemPosition.get() == MainHandItemPosition.DISABLED ? Stream.empty() : Stream.of(player.br$getInventory().br$getMainHand()), player.br$getInventory().br$getArmor().stream()).allMatch(AxoItemStack::br$isEmpty)) {
			return;
		}
		super.render(ctx, delta);
	}

	@Override
	public void renderComponent(AxoRenderContext graphics, float delta) {
		final var player = client.br$getPlayer();
		if (player == null) {
			return;
		}

		final var mainHand = player.br$getInventory().br$getMainHand();

		renderInternal(
			graphics,
			mainHand,
			player.br$getInventory().br$getArmor(),
			ItemUtil.getTotal(client, mainHand.br$getItem())
		);
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext graphics, float delta) {
		if (BridgeVersion.is26OrLater()) {
			if (client.br$getWorld() == null) {
				var w = order.get().isXAxis() ? 20 : 100;
				var h = order.get().isXAxis() ? 100 : 20;
				var updated = false;
				if (mainHandItemPosition.get() == MainHandItemPosition.DISABLED) {
					if (order.get().isXAxis()) h -= 20;
					else w -= 20;
				}
				if (w != getContentWidth()) {
					setContentWidth(w);
					updated = true;
				}
				if (h != getContentHeight()) {
					setContentHeight(h);
					updated = true;
				}
				if (updated) {
					onBoundsUpdate();
				}
				var pos = getContentPos();
				graphics.br$pushMatrix();
				if (order.get().isXAxis()) {
					graphics.br$rotateMatrixAround((float) Math.PI / 2f, pos.x() + getContentWidth() / 2f, pos.y() + getContentHeight() / 2f);
				}
				graphics.br$drawCenteredString(getName(), pos.x() + getContentWidth() / 2, pos.y() + getContentHeight() / 2 - graphics.br$getFont().br$getFontHeight() / 2, textColor.get());
				graphics.br$popMatrix();
				return;
			}
		}
		renderInternal(graphics, PLACEHOLDER_MAIN_HAND.get(), PLACEHOLDER_GEAR.get(), 1);
	}

	private void renderInternal(
		AxoRenderContext context,
		AxoItemStack mainHand, List<? extends AxoItemStack> armor,
		int mainHandCount
	) {
		int width = 20;
		int height = 100;
		boolean boundsChanged = false;
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();

		if (reverseArmorOrder.get()) {
			armor = new ArrayList<>(armor);
			Collections.reverse(armor);
		}

		MainHandItemPosition mhPos = mainHandItemPosition.get();
		if (hideIfEmpty.get() && mhPos != MainHandItemPosition.DISABLED && mainHand.br$isEmpty()) {
			mhPos = MainHandItemPosition.DISABLED;
		}
		var order = this.order.get();
		if (order.isXAxis()) {
			int labelWidth = (showDurability || showMaxDurability) ?
				(mhPos == MainHandItemPosition.DISABLED ? armor.stream() : Stream.concat(Stream.of(mainHand), armor.stream()))
				.mapToInt(stack -> {
					String text = showDurability && showMaxDurability
						? (stack.br$getMaxDamage() - stack.br$getDamage()) + "/" + stack.br$getMaxDamage()
						: String.valueOf(showDurability ? stack.br$getMaxDamage() - stack.br$getDamage()
										 : stack.br$getMaxDamage());
					return context.br$getFont().br$getWidth(text) + 2;
				}).max().orElse(0) : 0;
			width += labelWidth;
			if (width != getContentWidth()) {
				setContentWidth(width);
				boundsChanged = true;
			}

			if (mhPos == MainHandItemPosition.DISABLED) {
				height -= 20;
			}

			if (height != getContentHeight()) {
				setContentHeight(height);
				boundsChanged = true;
			}
			if (boundsChanged) {
				onBoundsUpdate();
			}
			DrawPosition pos = getContentPos();

			int lastY = 2 + (height - 20);

			if (mhPos == MainHandItemPosition.BOTTOM) {
				var x = pos.x() + 2;
				var y = pos.y() + lastY;
				if (order.getDirection() == 1) {
					renderDurabilityNumberXAxis(context, mainHand, x, y);
					renderMainItem(context, mainHand, x + labelWidth, y, mainHandCount);
				} else {
					renderMainItem(context, mainHand, x, y, mainHandCount);
					renderDurabilityNumberXAxis(context, mainHand, x + 18, y);
				}
				lastY -= 20;
			}

			for (AxoItemStack stack : armor) {
				String label = null;

				if (showProtLvl.get() && stack.br$hasEnchantment(AxoEnchants.PROTECTION)) {
					label = String.valueOf(stack.br$getEnchantment(AxoEnchants.PROTECTION));
				}

				var x = pos.x() + 2;
				var y = pos.y() + lastY;
				if (order.getDirection() == 1) {
					renderDurabilityNumberXAxis(context, stack, x, y);
					renderItem(context, stack, x + labelWidth, y, label);
				} else {
					renderItem(context, stack, x, y, label);
					renderDurabilityNumberXAxis(context, stack, x + 18, y);
				}
				lastY -= 20;
			}

			if (mhPos == MainHandItemPosition.TOP) {
				var x = pos.x() + 2;
				var y = pos.y() + lastY;
				if (order.getDirection() == 1) {
					renderDurabilityNumberXAxis(context, mainHand, x, y);
					renderMainItem(context, mainHand, x + labelWidth, y, mainHandCount);
				} else {
					renderMainItem(context, mainHand, x, y, mainHandCount);
					renderDurabilityNumberXAxis(context, mainHand, x + 18, y);
				}
			}
		} else {
			int labelWidth = showDurability || showMaxDurability ?
				(mhPos == MainHandItemPosition.DISABLED ? armor.stream() : Stream.concat(Stream.of(mainHand), armor.stream()))
				.mapToInt(stack -> {
					if (showDurability && showMaxDurability) {
						var text1 = String.valueOf(stack.br$getMaxDamage() - stack.br$getDamage());
						var text2 = "/" + stack.br$getMaxDamage();
						int t1W = context.br$getFont().br$getWidth(text1);
						int t2W = context.br$getFont().br$getWidth(text2);
						return Math.max(t1W, t2W);
					} else if (showDurability) {
						return context.br$getFont().br$getWidth(String.valueOf(stack.br$getMaxDamage() - stack.br$getDamage()));
					}
					return context.br$getFont().br$getWidth(String.valueOf(stack.br$getMaxDamage()));
				}).map(i -> i + 2).max().orElse(0) : 0;
			{
				int n = width;
				width = height - 6;
				height = n;
			}
			if (showDurability) {
				height += 10;
			}
			if (showMaxDurability) {
				height += 10;
			}
			if (labelWidth > 0) {
				width = (mhPos == MainHandItemPosition.DISABLED ? 4 : 5) * labelWidth + 2;
			}
			if (width != getContentWidth()) {
				setContentWidth(width);
				boundsChanged = true;
			}
			if (height != getContentHeight()) {
				setContentHeight(height);
				boundsChanged = true;
			}
			if (boundsChanged) {
				onBoundsUpdate();
			}
			DrawPosition pos = getContentPos();
			var x = pos.x() + 2;
			var y = pos.y() + 2;
			int stackWidth = 18;
			labelWidth = Math.max(stackWidth, labelWidth);
			if (mhPos == MainHandItemPosition.TOP) {
				if (order == CardinalOrder.TOP_DOWN) {
					var numHeight = renderDurabilityNumberYAxis(context, mainHand, x + labelWidth / 2, y);
					renderMainItem(context, mainHand, x - stackWidth / 2 + labelWidth / 2, y + numHeight, mainHandCount);
				} else {
					renderMainItem(context, mainHand, x - stackWidth / 2 + labelWidth / 2, y, mainHandCount);
					renderDurabilityNumberYAxis(context, mainHand, x + labelWidth / 2, y + 18);
				}
				x += labelWidth;
			}
			for (var stack : armor) {
				String label = null;

				if (showProtLvl.get() && stack.br$hasEnchantment(AxoEnchants.PROTECTION)) {
					label = String.valueOf(stack.br$getEnchantment(AxoEnchants.PROTECTION));
				}

				if (order == CardinalOrder.TOP_DOWN) {
					var numHeight = renderDurabilityNumberYAxis(context, stack, x + labelWidth / 2, y);
					renderItem(context, stack, x - stackWidth / 2 + labelWidth / 2, y + numHeight, label);
				} else {
					renderItem(context, stack, x - stackWidth / 2 + labelWidth / 2, y, label);
					renderDurabilityNumberYAxis(context, stack, x + labelWidth / 2, y + 18);
				}
				x += labelWidth;
			}
			if (mhPos == MainHandItemPosition.BOTTOM) {
				if (order == CardinalOrder.TOP_DOWN) {
					var numHeight = renderDurabilityNumberYAxis(context, mainHand, x + labelWidth / 2, y);
					renderMainItem(context, mainHand, x - stackWidth / 2 + labelWidth / 2, y + numHeight, mainHandCount);
				} else {
					renderMainItem(context, mainHand, x - stackWidth / 2 + labelWidth / 2, y, mainHandCount);
					renderDurabilityNumberYAxis(context, mainHand, x + labelWidth / 2, y + 18);
				}
			}
		}
	}

	public void renderMainItem(AxoRenderContext graphics, AxoItemStack stack, int x, int y, int mainHandCount) {
		renderItem(graphics, stack, x, y, mainHandCount == 1 ? null : String.valueOf(mainHandCount));
	}

	public void renderItem(AxoRenderContext graphics, AxoItemStack stack, int x, int y, String labelOverride) {
		graphics.br$renderGuiItemModel(stack, x, y);
		graphics.br$renderGuiItemOverlay(stack, x, y, labelOverride, textColor.get().toInt(), shadow.get());
	}

	private void renderDurabilityNumberXAxis(AxoRenderContext graphics, AxoItemStack stack, int x, int y) {
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		if (stack.br$isEmpty() || !(showMaxDurability || showDurability) || stack.br$getMaxDamage() == 0) {
			return;
		}
		String text = showDurability && showMaxDurability ?
			(stack.br$getMaxDamage() - stack.br$getDamage()) + "/" + stack.br$getMaxDamage() :
			String.valueOf((showDurability ? stack.br$getMaxDamage() - stack.br$getDamage() :
				stack.br$getMaxDamage()));
		int textY = y + 10 - graphics.br$getFont().br$getFontHeight() / 2;
		graphics.br$drawString(text, x, textY, customDurabilityNumColor.get() ? durabilityNumColor.get().toInt() :
			ClientColors.ARGB.opaque(stack.br$getBarColor()), true);
	}

	private int renderDurabilityNumberYAxis(AxoRenderContext graphics, AxoItemStack stack, int x, int y) {
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		if (!(showMaxDurability || showDurability)) {
			return 0;
		}
		if (stack.br$isEmpty() || stack.br$getMaxDamage() == 0) {
			return showDurability && showMaxDurability ? 20 : 10;
		}
		int textY = y + 10 / 2 - graphics.br$getFont().br$getFontHeight() / 2;
		if (showDurability && showMaxDurability) {
			var text1 = String.valueOf(stack.br$getMaxDamage() - stack.br$getDamage());
			var text2 = "/" + stack.br$getMaxDamage();
			int t1W = graphics.br$getFont().br$getWidth(text1);
			int t2W = graphics.br$getFont().br$getWidth(text2);
			graphics.br$drawString(text1, x - t1W / 2, textY, customDurabilityNumColor.get() ? durabilityNumColor.get().toInt() :
				ClientColors.ARGB.opaque(stack.br$getBarColor()), true);
			graphics.br$drawString(text2, x - t2W / 2, textY + 10, customDurabilityNumColor.get() ? durabilityNumColor.get().toInt() :
				ClientColors.ARGB.opaque(stack.br$getBarColor()), true);
			return 20;
		} else {
			var text = String.valueOf(showDurability ? stack.br$getMaxDamage() - stack.br$getDamage() : stack.br$getMaxDamage());
			graphics.br$drawString(text, x - graphics.br$getFont().br$getWidth(text) / 2, textY, customDurabilityNumColor.get() ? durabilityNumColor.get().toInt() :
				ClientColors.ARGB.opaque(stack.br$getBarColor()), true);
			return 10;
		}
	}

	@Override
	public AxoIdentifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(showProtLvl);
		options.add(showDurabilityNumber);
		options.add(showMaxDurabilityNumber);
		options.add(customDurabilityNumColor);
		options.add(durabilityNumColor);
		options.add(mainHandItemPosition);
		options.add(order);
		options.add(reverseArmorOrder);
		options.add(hideIfEmpty);
		return options;
	}

	@Override
	protected AnchorPoint getDefaultAnchor() {
		return AnchorPoint.TOP_RIGHT;
	}

	private enum MainHandItemPosition {
		BOTTOM,
		TOP,
		DISABLED,
		;

		@Override
		public String toString() {
			return "armorhud.main_hand_item_position." + super.toString().toLowerCase(Locale.ROOT);
		}
	}
}
