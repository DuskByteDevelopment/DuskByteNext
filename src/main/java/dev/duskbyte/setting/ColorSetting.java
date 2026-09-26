
package dev.duskbyte.setting;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int defaultColor) {
        super(name, defaultColor);
    }

    public int getRed() { return (value >> 16) & 0xFF; }
    public int getGreen() { return (value >> 8) & 0xFF; }
    public int getBlue() { return value & 0xFF; }

    public static int fromRGB(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }
}
