
package dev.duskbyte.gui.util;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Simplified render utilities.
 */
public final class RenderUtils {
    /** Render a rounded quad (simplified: uses fill, no real rounding in 1.21.1) */
    public static void renderRoundedQuad(DrawContext ctx, Color c, double x, double y, double x2, double y2,
                                          double corner1, double corner2, double corner3, double corner4, double samples) {
        ctx.fill((int) x, (int) y, (int) x2, (int) y2, c.getRGB());
    }

    /** Render a rounded quad with uniform radius */
    public static void renderRoundedQuad(DrawContext ctx, Color c, double x, double y, double x2, double y2,
                                          double radius, double samples) {
        ctx.fill((int) x, (int) y, (int) x2, (int) y2, c.getRGB());
    }
}
