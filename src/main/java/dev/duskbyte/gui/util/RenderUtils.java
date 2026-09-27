
package dev.duskbyte.gui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * Argon RenderUtils - adapted for 1.21.1.
 * renderRoundedQuad uses DrawContext.fill() as 1.21.1 has no BufferRenderer.
 */
public final class RenderUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /** Argon's renderRoundedQuad - 1.21.1 uses fill() fallback */
    public static void renderRoundedQuad(DrawContext ctx, Color c, double x, double y, double x2, double y2,
                                          double corner1, double corner2, double corner3, double corner4, double samples) {
        ctx.fill((int) x, (int) y, (int) x2, (int) y2, c.getRGB());
    }

    /** Argon's renderRoundedQuad with uniform radius */
    public static void renderRoundedQuad(DrawContext ctx, Color c, double x, double y, double x1, double y1,
                                          double rad, double samples) {
        ctx.fill((int) x, (int) y, (int) x1, (int) y1, c.getRGB());
    }

    /** Argon's renderCircle - 1.21.1 uses fill() fallback */
    public static void renderCircle(DrawContext ctx, Color c, double originX, double originY, double rad, int segments) {
        int r = (int) rad;
        ctx.fill((int)(originX - r), (int)(originY - r), (int)(originX + r), (int)(originY + r), c.getRGB());
    }

    /** Argon's scissor */
    public static void setScissorRegion(int x, int y, int width, int height) {
        double scaleFactor = mc.getWindow().getScaleFactor();
        GL11.glScissor((int)(x * scaleFactor), (int)(y * scaleFactor),
            (int)(width * scaleFactor), (int)(height * scaleFactor));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
