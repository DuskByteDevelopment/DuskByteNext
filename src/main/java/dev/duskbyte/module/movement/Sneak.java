
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sneak extends Module {
    public Sneak() {
        super("Sneak", "Auto sneak when standing", Category.MOVEMENT, GLFW.GLFW_KEY_X);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        mc.options.sneakKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.options.sneakKey.setPressed(false);
    }
}
