
package dev.duskbyte.module;

import dev.duskbyte.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name, description;
    private final Category category;
    private final String translationKey;
    private int key;
    private boolean enabled;
    public final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
    }

    protected Module(String name, String description, Category category, int defaultKey) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = defaultKey;
        this.translationKey = "duskbyte.module." + category.name().toLowerCase() + "." + name.toLowerCase().replace(" ", "");
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public void toggle() {
        if (enabled) disable(); else enable();
    }

    public void enable() {
        enabled = true;
        onEnable();
    }

    public void disable() {
        enabled = false;
        onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
    public void onAttack(Entity target) {}

    public String getName() { return name; }
    public String getRawName() { return name; }
    public Text getTitle() { return Text.translatable(translationKey); }
    public String getTranslationKey() { return translationKey; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }
}
