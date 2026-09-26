
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class AutoRespawn extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", 100, 0, 2000, 100));

    public AutoRespawn() {
        super("AutoRespawn", "Automatically respawns on death", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;

        // Check if dead (death screen or health <= 0)
        if (mc.player.getHealth() <= 0) {
            long now = System.currentTimeMillis();
            if (now - lastDeath > delay.get()) {
                mc.player.requestRespawn();
                lastDeath = now;
            }
        } else {
            lastDeath = System.currentTimeMillis();
        }
    }

    private long lastDeath = System.currentTimeMillis();
}
