
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "Removes block placement delay", Category.PLAYER, GLFW.GLFW_KEY_H);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.itemUseCooldown = 0;
    }
}
