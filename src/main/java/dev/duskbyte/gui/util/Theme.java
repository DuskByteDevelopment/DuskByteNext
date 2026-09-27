package dev.duskbyte.gui.util;

/**
 * Meteor-style blue glass theme colors.
 */
public class Theme {
    // Base colors
    public static final int BG_PRIMARY = 0xE00D1117;      // Dark blue-gray glass
    public static final int BG_SECONDARY = 0xCC111827;     // Slightly lighter
    public static final int BG_TERTIARY = 0xBB1F2937;      // Panel body
    public static final int BG_MODULE = 0xCC161B22;        // Module row
    public static final int BG_MODULE_ENABLED = 0xCC0D47A1; // Blue tint when enabled

    // Accent
    public static final int ACCENT = 0xFF3B82F6;           // Bright blue
    public static final int ACCENT_DIM = 0xFF2563EB;       // Dimmer blue
    public static final int ACCENT_GLOW = 0x403B82F6;      // Blue glow overlay

    // Text
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;      // White
    public static final int TEXT_SECONDARY = 0xFFB0B8C4;    // Light gray
    public static final int TEXT_DIM = 0xFF6B7280;          // Dim gray
    public static final int TEXT_ACCENT = 0xFF60A5FA;       // Light blue

    // States
    public static final int HOVER = 0x18FFFFFF;             // White overlay
    public static final int CLICK = 0x25FFFFFF;             // Stronger white
    public static final int ENABLED_GLOW = 0x153B82F6;     // Blue glow for enabled

    // Scrollbar
    public static final int SCROLLBAR = 0x403B82F6;        // Semi-transparent blue
    public static final int SCROLLBAR_BG = 0x15FFFFFF;     // Scrollbar track

    // Status colors
    public static final int ON = 0xFF4ADE80;               // Green
    public static final int OFF = 0xFFEF4444;              // Red
    public static final int VALUE = 0xFFFBBF24;            // Yellow
    public static final int STRING = 0xFF22D3EE;           // Cyan

    // Category colors (Meteor-style)
    public static final int CAT_COMBAT = 0xFFEF4444;       // Red
    public static final int CAT_MOVEMENT = 0xFF22C55E;     // Green
    public static final int CAT_RENDER = 0xFF3B82F6;       // Blue
    public static final int CAT_PLAYER = 0xFFFBBF24;       // Yellow
    public static final int CAT_MISC = 0xFFA855F7;         // Purple
    public static final int CAT_CLIENT = 0xFFFF6B6B;       // Pink

    /** Lerp between two ARGB colors */
    public static int lerp(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int aA = (a >> 24) & 0xFF, aR = (a >> 16) & 0xFF, aG = (a >> 8) & 0xFF, aB = a & 0xFF;
        int bA = (b >> 24) & 0xFF, bR = (b >> 16) & 0xFF, bG = (b >> 8) & 0xFF, bB = b & 0xFF;
        int rA = (int)(aA + (bA - aA) * t);
        int rR = (int)(aR + (bR - aR) * t);
        int rG = (int)(aG + (bG - aG) * t);
        int rB = (int)(aB + (bB - aB) * t);
        return (rA << 24) | (rR << 16) | (rG << 8) | rB;
    }

    /** With alpha */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
