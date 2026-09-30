package net.shinyshoe.storagetracker.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
//? if > 1.19.2 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
*///?}

public final class RenderContext {

	//? if > 1.19.2 {
	private final GuiGraphics graphics;

	public RenderContext(GuiGraphics graphics) {
		this.graphics = graphics;
	}
	//?} else {
	/*private final PoseStack pose;

	public RenderContext(PoseStack pose) {
		this.pose = pose;
	}
	*///?}

	public void fill(int x1, int y1, int x2, int y2, int argbColor) {
		//? if > 1.19.2 {
		graphics.fill(x1, y1, x2, y2, argbColor);
		//?} else {
		/*GuiComponent.fill(pose, x1, y1, x2, y2, argbColor);
		*///?}
	}

	public void drawText(Font font, Component text, int x, int y, int argbColor) {
		//? if > 1.19.2 {
		graphics.drawString(font, text, x, y, argbColor);
		//?} else {
		/*GuiComponent.drawString(pose, font, text, x, y, argbColor);
		*///?}
	}

	public void drawText(Font font, String text, int x, int y, int argbColor) {
		//? if > 1.19.2 {
		graphics.drawString(font, text, x, y, argbColor);
		//?} else {
		/*GuiComponent.drawString(pose, font, text, x, y, argbColor);
		*///?}
	}

	public void drawItem(Font font, ItemStack stack, int x, int y) {
		//? if > 1.19.2 {
		graphics.renderItem(stack, x, y);
		graphics.renderItemDecorations(font, stack, x, y);
		//?} else {
		/*Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, x, y);
		Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, x, y);
		*///?}
	}

	public void enableScissor(int x1, int y1, int x2, int y2) {
		//? if > 1.19.2 {
		graphics.enableScissor(x1, y1, x2, y2);
		//?} else {
		/*GuiComponent.enableScissor(x1, y1, x2, y2);
		*///?}
	}

	public void disableScissor() {
		//? if > 1.19.2 {
		graphics.disableScissor();
		//?} else {
		/*GuiComponent.disableScissor();
		*///?}
	}

	public void renderBackground(Screen screen, int mouseX, int mouseY, float partialTick) {
		//? if > 1.19.2 {
		screen.renderBackground(graphics, mouseX, mouseY, partialTick);
		//?} else {
		/*screen.renderBackground(pose);
		*///?}
	}
}
