
package dev.duskbyte.module;

import net.minecraft.text.Text;

public enum Category {
    COMBAT("Combat", 0xFF4444),
    MOVEMENT("Movement", 0x44FF44),
    RENDER("Render", 0x4444FF),
    PLAYER("Player", 0xFFFF44),
    MISC("Misc", 0xFF44FF);

    public final String name;
    public final int color;
    private final String translationKey;

    Category(String name, int color) {
        this.name = name;
        this.color = color;
        this.translationKey = "duskbyte.category." + name().toLowerCase();
    }

    public Text getTitle() { return Text.translatable(translationKey); }
}
