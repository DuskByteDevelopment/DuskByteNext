
package dev.duskbyte.gui.util;

import java.awt.Color;

/**
 * Color utilities from Argon.
 */
public final class ColorUtils {
    /** Rainbow color based on time and offset */
    public static Color getRainbow(int increment, int alpha) {
        float hue = ((System.currentTimeMillis() * 3 + increment * 175) % (360 * 20)) / (360f * 20);
        return new Color(Color.HSBtoRGB(hue, 0.6f, 1f) & 0x00FFFFFF | (alpha << 24));
    }

    /** Smooth color transition */
    public static Color smoothColorTransition(float speed, Color toColor, Color fromColor) {
        return new Color(
            (int) MathUtils.goodLerp(speed, fromColor.getRed(), toColor.getRed()),
            (int) MathUtils.goodLerp(speed, fromColor.getGreen(), toColor.getGreen()),
            (int) MathUtils.goodLerp(speed, fromColor.getBlue(), toColor.getBlue()));
    }

    /** Smooth alpha transition */
    public static Color smoothAlphaTransition(float speed, int toAlpha, Color fromColor) {
        return new Color(
            fromColor.getRed(),
            fromColor.getGreen(),
            fromColor.getBlue(),
            (int) MathUtils.goodLerp(speed, fromColor.getAlpha(), toAlpha));
    }

    /** Get main rainbow color for module index */
    public static Color getMainColor(int alpha, int index) {
        float hue = ((System.currentTimeMillis() % 2000) / 1000f + (float) index / 10f * 2) % 2;
        hue = Math.abs(hue - 1);
        float brightness = 0.25f + 0.75f * (hue % 2);
        int rgb = Color.HSBtoRGB(0.6f, 0.7f, brightness); // Blue-ish hue
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }
}
