/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.bridge.render.AxoRenderContext;
import io.github.axolotlclient.mixin.ChatHudAccessor;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatMessage;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.TextRenderUtils;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ChatHud extends TextHudEntry {

	public static final Identifier ID = new Identifier(DolphinClientCommon.MODID, "chathud");
	// tooltip: "chathud"
	public final BooleanOption background = new BooleanOption("background", true);
	public final ColorOption bgColor = new ColorOption("bgcolor", Color.parse("#80000000"));

	public final IntegerOption chatHistory = new IntegerOption("chatHistoryLength", 100, 10, 5000);
	public final ColorOption scrollbarColor = new ColorOption("scrollbarColor", Color.parse("#70CCCCCC"));
	public final IntegerOption lineSpacing = new IntegerOption("lineSpacing", 0, 0, 10);
	public final BooleanOption keepMessagesOnDisconnect = new BooleanOption("keep_messages_on_disconnect", false);
	public final BooleanOption animateChat = new BooleanOption("animate_chat", false);
	/**
	 * Off by default. Chat keeps the vanilla anchor at every GUI scale and
	 * resolution until this is turned on; background and transparency still apply.
	 */
	public final BooleanOption customPosition = new BooleanOption("custom_position", false);

	public int ticks;
	private int lastHeight;

	private float percentComplete;
	public int newLines;
	private long prevMillis = System.currentTimeMillis();
	public float animationPercent;

	private final Minecraft client = (Minecraft) super.client;

	private void updatePercentage(long diff) {
		if (percentComplete < 1)
			percentComplete += 0.004f * diff;
		percentComplete = MathHelper.clamp(percentComplete, 0, 1);
	}

	public ChatHud() {
		super(320, 80, false);
	}

	public static int getHeight(float chatHeight) {
		int i = 180;
		int j = 20;
		return MathHelper.floor(chatHeight * (float) (i - j) + (float) j);
	}

	public void resetAnimation() {
		percentComplete = 0;
		prevMillis = System.currentTimeMillis();
	}

	public boolean usesCustomPosition() {
		return ChatPosition.usesCustomPosition(isEnabled(), HudManager.getInstance().hudsEnabled(), customPosition.get());
	}

	/**
	 * Module features draw, but the matrix is the one vanilla just set up:
	 * {@code (0, scaledHeight - 48)} then {@code (2, 20)} then the chat scale.
	 */
	public boolean appliesOnVanillaChat() {
		return isEnabled() && HudManager.getInstance().hudsEnabled() && !customPosition.get();
	}

	private void updateAnimationClock() {
		long current = System.currentTimeMillis();
		long diff = current - prevMillis;
		prevMillis = current;
		updatePercentage(diff);
		float t = percentComplete - 1;
		animationPercent = MathHelper.clamp(1 - (float) Math.pow(t, 4), 0, 1);
	}

	@Override
	public void render(AxoRenderContext context, float delta) {
		if (!customPosition.get()) {
			return;
		}
		updateAnimationClock();
		if (this.client.options.chatVisibility == PlayerEntity.ChatVisibility.HIDDEN) {
			return;
		}
		GlStateManager.pushMatrix();
		scale(context);
		DrawPosition pos = getPos();
		List<ChatMessage> visibleMessages = ((ChatHudAccessor) client.gui.getChat()).getVisibleMessages();
		if (!visibleMessages.isEmpty()) {
			float g = getScale();
			int innerWidth = MathHelper.ceil((float) getWidth() / g);
			GlStateManager.pushMatrix();
			if (animateChat.get() && !((ChatHudAccessor) this.client.gui.getChat()).hasUnreadNewMessages()) {
				GlStateManager.translatef(0, (9 - 9 * animationPercent) * getScale(), 0);
			}
			drawMessages(context, pos.x, pos.y + getHeight(), innerWidth, getFontHeight(), false);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	/**
	 * Called from inside {@code ChatGui.render} after vanilla has translated and scaled.
	 * Local {@code (0, 0)} is the vanilla anchor for the current GUI scale and resolution.
	 */
	public void renderAtVanillaAnchor(AxoRenderContext context) {
		updateAnimationClock();
		if (this.client.options.chatVisibility == PlayerEntity.ChatVisibility.HIDDEN) {
			return;
		}
		float chatScale = client.gui.getChat().getChatScale();
		int innerWidth = MathHelper.ceil(client.gui.getChat().getWidth() / chatScale);
		int lineHeight = 9 + lineSpacing.get();
		GlStateManager.pushMatrix();
		if (animateChat.get() && !((ChatHudAccessor) this.client.gui.getChat()).hasUnreadNewMessages()) {
			GlStateManager.translatef(0, lineHeight * (1.0F - animationPercent), 0);
		}
		drawMessages(context, 0, 0, innerWidth, lineHeight, true);
		GlStateManager.popMatrix();
	}

	private void drawMessages(AxoRenderContext context, int left, int bottom, int innerWidth, int scrollbarRow, boolean vanillaAnchor) {
		int scrolledLines = ((ChatHudAccessor) client.gui.getChat()).getScrolledLines();
		List<ChatMessage> visibleMessages = ((ChatHudAccessor) client.gui.getChat()).getVisibleMessages();
		int visible = getVisibleLineCount();
		int count = visibleMessages.size();
		if (count <= 0) {
			return;
		}
		int lineHeight = 9 + lineSpacing.get();
		int considered = 0;
		for (int m = 0; m + scrolledLines < count && m < visible; ++m) {
			ChatMessage chatHudLine = visibleMessages.get(m + scrolledLines);
			if (chatHudLine == null) {
				continue;
			}
			int age = ticks - chatHudLine.getTimeOfCreation();
			if (age >= 200 && !isChatFocused()) {
				continue;
			}
			double fade = MathHelper.clamp((1.0 - age / 200.0) * 10.0, 0.0, 1.0);
			fade *= fade;
			if (animateChat.get() && m + scrolledLines < newLines) {
				fade *= animationPercent;
			}

			++considered;
			int alpha = Math.max(bgColor.get().getAlpha(), textColor.get().getAlpha());
			int opacity = isChatFocused() ? alpha : (int) (alpha * fade);
			if (opacity > 3) {
				int y = ChatPosition.lineBottom(bottom, m, lineHeight);
				if (background.get()) {
					Color bg = bgColor.get();
					if (!isChatFocused()) {
						bg = bg.withAlpha((int) (bg.getAlpha() * fade));
					}
					context.br$fillRect(left, y - lineHeight, innerWidth + 4, lineHeight, bg.toInt());
				}
				String string = chatHudLine.getText().getFormattedString();
				GlStateManager.enableBlend();
				Color text = textColor.get();
				if (!isChatFocused()) {
					text = text.withAlpha((int) (text.getAlpha() * fade));
				}
				context.br$drawString(string, left, y - 8, text, shadow.get());
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
			}
		}

		boolean overflow = vanillaAnchor
			? count != considered
			: ((ChatHudAccessor) client.gui.getChat()).getMessages().size() > visible;
		if (isChatFocused() && overflow) {
			int total = count * scrollbarRow + count;
			int shown = considered * scrollbarRow + considered;
			int barY = bottom - scrolledLines * shown / count;
			int barHeight = total == 0 ? 0 : shown * shown / total;
			GlStateManager.translatef(-3.0F, 0.0F, 0.0F);
			context.br$fillRect(left, barY, 2, -barHeight, scrollbarColor.get().toInt());
		}
	}

	@Override
	public void renderComponent(AxoRenderContext context, float delta) {
	}

	@Override
	public void renderPlaceholderComponent(AxoRenderContext context, float delta) {
		DrawPosition pos = getPos();
		if (Minecraft.getInstance().player != null) {
			client.textRenderer.drawWithShadow(Minecraft.getInstance().player.getDisplayName().getFormattedString()
				+ " We love Dolphin Client!", pos.x + 1, pos.y + getHeight() - 9, -1);
		} else {
			client.textRenderer.drawWithShadow("This is where your new and fresh looking chat will be!", pos.x + 1,
				pos.y + getHeight() - 9, -1);
		}
	}

	public int getVisibleLineCount() {
		return getHeight(
			this.isChatFocused() ? this.client.options.focusedChatHeight : this.client.options.unfocusedChatHeight)
			/ 9;
	}

	public boolean isChatFocused() {
		return this.client.screen instanceof ChatScreen;
	}

	protected int getFontHeight() {
		return MathHelper.floor(Minecraft.getInstance().textRenderer.fontHeight);
	}

	public Text getTextAt(int x, int y) {
		List<ChatMessage> visibleMessages = ((ChatHudAccessor) client.gui.getChat()).getVisibleMessages();

		int offsetOnHudX = MathHelper.floor(x / getScale() - getPos().x);
		int offsetOnHudY = MathHelper.floor(-(y / getScale() - (getPos().y + height)));

		int scrolledLines = ((ChatHudAccessor) client.gui.getChat()).getScrolledLines();

		if (offsetOnHudX >= 0 && offsetOnHudY >= 0) {
			int l = Math.min(this.getVisibleLineCount(), visibleMessages.size());
			if (offsetOnHudX <= MathHelper.floor((float) this.getWidth() / this.getScale())
				&& offsetOnHudY < (getFontHeight() + lineSpacing.get()) * l + l) {
				int m = offsetOnHudY / (getFontHeight() + lineSpacing.get()) + scrolledLines;
				if (m >= 0 && m < visibleMessages.size()) {
					ChatMessage chatHudLine = visibleMessages.get(m);
					int n = 0;

					for (Text text : chatHudLine.getText()) {
						if (text instanceof LiteralText) {
							n += this.client.textRenderer.getWidth(
								TextRenderUtils.prepareText(((LiteralText) text).getRawString(), false));
							if (n > offsetOnHudX) {
								return text;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public boolean overridesF3() {
		return true;
	}

	@Override
	public boolean movable() {
		return customPosition.get();
	}

	@Override
	public void renderPlaceholder(AxoRenderContext ctx, float delta) {
		if (!customPosition.get()) {
			hovered = false;
			return;
		}
		super.renderPlaceholder(ctx, delta);
	}

	@Override
	public void tick() {
		//setWidth((int) (client.options.chatWidth*320));
		if (lastHeight != getHeight(client.options.unfocusedChatHeight)) {
			setHeight(getHeight(this.client.options.unfocusedChatHeight));
			onBoundsUpdate();
			lastHeight = getHeight();
		}
	}

	@Override
	public double getDefaultX() {
		return 0.01;
	}

	@Override
	public double getDefaultY() {
		return 0.9;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		int enabledIndex = options.indexOf(enabled);
		options.add(enabledIndex < 0 ? 0 : enabledIndex + 1, customPosition);
		options.add(background);
		options.add(bgColor);
		options.add(lineSpacing);
		options.add(scrollbarColor);
		options.add(chatHistory);
		options.add(animateChat);
		options.add(keepMessagesOnDisconnect);
		return options;
	}
}
