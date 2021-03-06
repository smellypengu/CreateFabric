package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public abstract class AbstractSimiScreen extends Screen {

	protected int sWidth, sHeight;
	protected int guiLeft, guiTop;
	protected List<AbstractButtonWidget> widgets;

	protected AbstractSimiScreen() {
		super(new LiteralText(""));
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		sWidth = width;
		sHeight = height;
		guiLeft = (this.width - sWidth) / 2;
		guiTop = (this.height - sHeight) / 2;
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
		renderWindow(ms, mouseX, mouseY, partialTicks);
		for (AbstractButtonWidget widget : widgets)
			widget.render(ms, mouseX, mouseY, partialTicks);
		renderWindowForeground(ms, mouseX, mouseY, partialTicks);
		for (AbstractButtonWidget widget : widgets)
			widget.renderToolTip(ms, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (AbstractButtonWidget widget : widgets) {
			if (widget.mouseClicked(x, y, button))
				result = true;
		}
		return result;
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (AbstractButtonWidget widget : widgets) {
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		}
		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (AbstractButtonWidget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
		if (character == 'e')
			onClose();
		return super.charTyped(character, code);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		for (AbstractButtonWidget widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, delta))
				return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseReleased(double x, double y, int button) {
		boolean result = false;
		for (AbstractButtonWidget widget : widgets) {
			if (widget.mouseReleased(x, y, button))
				result = true;
		}
		return result | super.mouseReleased(x, y, button);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		for (AbstractButtonWidget widget : widgets) {
			if (!widget.isHovered())
				continue;
			
			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip().isEmpty()) {
				renderTooltip(ms, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
			}
		}
	}

}
