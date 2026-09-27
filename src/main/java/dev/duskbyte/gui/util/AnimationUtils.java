
package dev.duskbyte.gui.util;

/**
 * Animation utilities from Argon.
 * Smooth value transitions using goodLerp.
 */
public final class AnimationUtils {
    private double value;
    private final double originalValue;
    private double endValue;

    public AnimationUtils(double value) {
        this.value = value;
        this.originalValue = value;
        this.endValue = value;
    }

    /** Animate towards target value */
    public double animate(double delta, double end) {
        this.endValue = end;
        value = MathUtils.goodLerp((float) delta, value, end);
        return value;
    }

    public double getValue() { return value; }
    public double getEndValue() { return endValue; }

    /** Reset to original value */
    public void reset(double delta) {
        value = MathUtils.smoothStepLerp(delta, value, originalValue);
    }
}
