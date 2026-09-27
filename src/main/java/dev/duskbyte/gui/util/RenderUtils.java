
package dev.duskbyte.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Simplified render utilities from Argon.
 * Provides rounded quad rendering.
 */
public final class RenderUtils {
    /** Render a rounded quad with per-corner radii */
    public static void renderRoundedQuad(MatrixStack matrices, Color c, double x, double y, double x2, double y2,
                                          double corner1, double corner2, double corner3, double corner4, double samples) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        double[][] corners = {
            {x2 - corner4, y2 - corner4, corner4},  // bottom-right
            {x2 - corner2, y + corner2, corner2},    // top-right
            {x + corner1, y + corner1, corner1},     // top-left
            {x + corner3, y2 - corner3, corner3}     // bottom-left
        };

        BufferBuilder bb = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            double[] c2 = corners[i];
            double rad = c2[2];
            for (double deg = i * 90.0; deg < (360.0 / 4.0 + i * 90.0); deg += (90.0 / samples)) {
                float rad1 = (float) Math.toRadians(deg);
                float sx = (float) (Math.sin(rad1) * rad);
                float sy = (float) (Math.cos(rad1) * rad);
                bb.vertex(matrix, (float) c2[0] + sx, (float) c2[1] + sy, 0f).color(r, g, b, a);
            }
            float rad1 = (float) Math.toRadians(360.0 / 4.0 + i * 90.0);
            float sx = (float) (Math.sin(rad1) * rad);
            float sy = (float) (Math.cos(rad1) * rad);
            bb.vertex(matrix, (float) c2[0] + sx, (float) c2[1] + sy, 0f).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(bb.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /** Render a rounded quad with uniform radius */
    public static void renderRoundedQuad(MatrixStack matrices, Color c, double x, double y, double x2, double y2,
                                          double radius, double samples) {
        renderRoundedQuad(matrices, c, x, y, x2, y2, radius, radius, radius, radius, samples);
    }

    /** Render a circle */
    public static void renderCircle(MatrixStack matrices, Color c, double originX, double originY, double rad, int segments) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bb = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 360; i += Math.min(360 / segments, 360 - i)) {
            double rad = Math.toRadians(i);
            bb.vertex(matrix, (float) (originX + Math.sin(rad) * rad), (float) (originY + Math.cos(rad) * rad), 0f)
               .color(r, g, b, a);
        }
        BufferRenderer.drawWithGlobalProgram(bb.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
