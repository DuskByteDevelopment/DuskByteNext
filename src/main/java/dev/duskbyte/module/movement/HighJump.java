
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public class HighJump extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", 1.5, 0.5, 5.0, 0.1));

    public HighJump() {
        super("HighJump", "Jump higher than normal", Category.MOVEMENT, GLFW.GLFW_KEY_H);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.player.isSpectator() || mc.player.isCreative()) return;

        if (mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
            mc.player.jump();
            // Boost velocity after jump
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, height.get(), vel.z);
        }
    }
}
