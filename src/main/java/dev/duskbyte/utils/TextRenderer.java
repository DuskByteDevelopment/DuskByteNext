package dev.duskbyte.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static dev.duskbyte.DuskByte.mc;

public final class TextRenderer {

	public static void drawString(CharSequence string, DrawContext context, int x, int y, int color) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(2f, 2f, 2f);
		context.drawText(mc.textRenderer, string.toString(), x / 2, y / 2, color, false);
		matrices.pop();
	}

	public static int getWidth(CharSequence string) {
		return mc.textRenderer.getWidth(string.toString()) * 2;
	}

	public static void drawCenteredString(CharSequence string, DrawContext context, int x, int y, int color) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(2f, 2f, 2f);
		int w = mc.textRenderer.getWidth(string.toString());
		context.drawText(mc.textRenderer, string.toString(), (x - w) / 2, y / 2, color, false);
		matrices.pop();
	}

	public static void drawLargeString(CharSequence string, DrawContext context, int x, int y, int color) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(3, 3, 3);
		context.drawText(mc.textRenderer, string.toString(), x / 3, y / 3, color, false);
		matrices.pop();
	}
}
