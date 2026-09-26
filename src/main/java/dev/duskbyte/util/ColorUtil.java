
package dev.duskbyte.util;

import java.awt.Color;

public class ColorUtil {
    public static int rainbow(int index) {
        float hue = (index * 0.05f + (System.currentTimeMillis() % 5000) / 5000f) % 1f;
        return Color.HSBtoRGB(hue, 0.8f, 1f);
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    public static int fromRGB(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (rr << 16) | (rg << 8) | rb;
    }
}
