
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

/**
 * Zoom in like OptiFine. (Meteor feature)
 */
public class Zoom extends Module {
    private final NumberSetting zoomLevel = add(new NumberSetting("Zoom", 4.0, 2.0, 16.0, 1.0));
    private int originalFov = 90;

    public Zoom() {
        super("Zoom", "Zoom in camera", Category.RENDER, GLFW.GLFW_KEY_C);
    }

    @Override
    public void onEnable() {
        originalFov = mc.options.getFov().getValue();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.options.getFov().setValue((int)(originalFov / zoomLevel.get()));
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getFov().setValue(originalFov);
        }
    }
}
