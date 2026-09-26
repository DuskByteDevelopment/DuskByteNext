
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class NoSlow extends Module {
    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 1.0, 0.5, 2.0, 0.1));

    public NoSlow() {
        super("NoSlow", "Removes slowdown from using items", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.player.isSpectator() || mc.player.isCreative()) return;

        // Compensate for item use slowdown
        if (mc.player.isUsingItem()) {
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(
                vel.x * multiplier.get(),
                vel.y,
                vel.z * multiplier.get()
            );
        }
    }
}
