package dev.duskbyte.utils;

import dev.duskbyte.font.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import static dev.duskbyte.DuskByte.mc;

public final class TextRenderer {

	public static void drawString(CharSequence string, DrawContext context, int x, int y, int color) {
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), x, y, color);
	}

	public static int getWidth(CharSequence string) {
		return (int) Fonts.HARMONY.getStringWidth(string.toString());
	}

	public static void drawCenteredString(CharSequence string, DrawContext context, int x, int y, int color) {
		float w = Fonts.HARMONY.getStringWidth(string.toString());
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), x - w / 2f, y, color);
	}

	public static void drawLargeString(CharSequence string, DrawContext context, int x, int y, int color) {
		Fonts.HARMONY.drawString(context.getMatrices(), string.toString(), x, y, color);
	}
}
