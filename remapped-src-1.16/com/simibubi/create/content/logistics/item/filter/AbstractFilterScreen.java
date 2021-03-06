package com.simibubi.create.content.logistics.item.filter;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static net.minecraft.util.Formatting.GRAY;

import java.util.Collections;
import java.util.List;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public abstract class AbstractFilterScreen<F extends AbstractFilterContainer> extends AbstractSimiContainerScreen<F> {

	protected AllGuiTextures background;

	private IconButton resetButton;
	private IconButton confirmButton;

	protected AbstractFilterScreen(F container, PlayerInventory inv, Text title, AllGuiTextures background) {
		super(container, inv, title);
		this.background = background;
	}

	@Override
	protected void init() {
		setWindowSize(background.width + 80, background.height + PLAYER_INVENTORY.height + 20);
		super.init();
		widgets.clear();

		resetButton =
			new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = x;
		int y = y;
		background.draw(ms, this, x, y);

		int invX = x + 50;
		int invY = y + background.height + 10;
		PLAYER_INVENTORY.draw(ms, this, invX, invY);
		textRenderer.draw(ms, playerInventory.getDisplayName(), invX + 7, invY + 6, 0x666666);
		textRenderer.draw(ms, I18n.translate(handler.filterItem.getTranslationKey()), x + 15, y + 3, 0xdedede);

		GuiGameElement.of(handler.filterItem)
			.at(x + background.width, y + background.height + 25, -150)
			.scale(5)
			.render(ms);

	}

	@Override
	public void tick() {
		handleTooltips();
		super.tick();
		handleIndicators();

		if (!handler.player.getMainHandStack()
			.equals(handler.filterItem, false))
			client.player.updateSubmergedInWaterState();
	}

	public void handleIndicators() {
		List<IconButton> tooltipButtons = getTooltipButtons();
		for (IconButton button : tooltipButtons)
			button.active = isButtonEnabled(button);
		for (AbstractButtonWidget w : widgets)
			if (w instanceof Indicator)
				((Indicator) w).state = isIndicatorOn((Indicator) w) ? State.ON : State.OFF;
	}

	protected abstract boolean isButtonEnabled(IconButton button);

	protected abstract boolean isIndicatorOn(Indicator indicator);

	protected void handleTooltips() {
		List<IconButton> tooltipButtons = getTooltipButtons();

		for (IconButton button : tooltipButtons) {
			if (!button.getToolTip()
				.isEmpty()) {
				button.setToolTip(button.getToolTip()
					.get(0));
				button.getToolTip()
					.add(TooltipHelper.holdShift(Palette.Yellow, hasShiftDown()));
			}
		}

		if (hasShiftDown()) {
			List<MutableText> tooltipDescriptions = getTooltipDescriptions();
			for (int i = 0; i < tooltipButtons.size(); i++)
				fillToolTip(tooltipButtons.get(i), tooltipDescriptions.get(i));
		}
	}

	protected List<IconButton> getTooltipButtons() {
		return Collections.emptyList();
	}

	protected List<MutableText> getTooltipDescriptions() {
		return Collections.emptyList();
	}

	private void fillToolTip(IconButton button, Text tooltip) {
		if (!button.isHovered())
			return;
		List<Text> tip = button.getToolTip();
		tip.addAll(TooltipHelper.cutTextComponent(tooltip, GRAY, GRAY));
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				client.player.updateSubmergedInWaterState();
				return true;
			}
			if (resetButton.isHovered()) {
				handler.clearContents();
				contentsCleared();
				sendOptionUpdate(Option.CLEAR);
				return true;
			}
		}

		return mouseClicked;
	}

	protected void contentsCleared() {}

	protected void sendOptionUpdate(Option option) {
		AllPackets.channel.sendToServer(new FilterScreenPacket(option));
	}

}
