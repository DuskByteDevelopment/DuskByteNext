package dev.duskbyte.utils;

import dev.duskbyte.font.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static dev.duskbyte.DuskByte.mc;

public final class TextRenderer {

	public static void drawString(CharSequence string, DrawContext context, int x, int y, int color) {
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), x / 2f, y / 2f, color);
	}

	public static int getWidth(CharSequence string) {
		return (int) Fonts.HARMONY.getStringWidth(string.toString()) * 2;
	}

	public static void drawCenteredString(CharSequence string, DrawContext context, int x, int y, int color) {
		float w = Fonts.HARMONY.getStringWidth(string.toString());
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), (x - w) / 2f, y / 2f, color);
	}

	public static void drawLargeString(CharSequence string, DrawContext context, int x, int y, int color) {
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), x / 3f, y / 3f, color);
	}
}
