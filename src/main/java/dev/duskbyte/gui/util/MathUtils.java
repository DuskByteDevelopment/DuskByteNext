
package dev.duskbyte.gui.util;

/**
 * Math utilities from Argon.
 */
public final class MathUtils {
    /** Argon's goodLerp - step-based smooth interpolation */
    public static double goodLerp(float delta, double start, double end) {
        int step = (int) Math.ceil(Math.abs(end - start) * delta);
        if (start < end) return Math.min(start + step, end);
        else return Math.max(start - step, end);
    }

    /** Smooth step interpolation */
    public static double smoothStepLerp(double delta, double start, double end) {
        delta = Math.max(0, Math.min(1, delta));
        double t = delta * delta * (3 - 2 * delta);
        return start + (end - start) * t;
    }

    /** Round to decimal places */
    public static double roundToDecimal(double n, double point) {
        return point * Math.round(n / point);
    }
}
