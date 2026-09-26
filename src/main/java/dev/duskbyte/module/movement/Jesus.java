
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.fluid.Fluids;
import org.lwjgl.glfw.GLFW;

public class Jesus extends Module {
    private final NumberSetting upSpeed = add(new NumberSetting("UpSpeed", 0.1, 0.01, 0.5, 0.01));
    private final NumberSetting downSpeed = add(new NumberSetting("DownSpeed", 0.05, 0.01, 0.3, 0.01));

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.MOVEMENT, GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.player.isSpectator() || mc.player.isCreative()) return;

        // Check if in water/lava
        boolean inFluid = mc.player.isTouchingFluid(Fluids.WATER) || mc.player.isTouchingFluid(Fluids.LAVA);
        if (!inFluid) return;

        // Push up when in fluid
        var vel = mc.player.getVelocity();
        if (vel.y < 0) {
            mc.player.setVelocity(vel.x, downSpeed.get(), vel.z);
        } else if (vel.y < upSpeed.get()) {
            mc.player.setVelocity(vel.x, upSpeed.get(), vel.z);
        }

        // Jump when in fluid (helps climb out)
        if (mc.options.jumpKey.isPressed() && mc.player.isTouchingFluid(Fluids.WATER)) {
            mc.player.setVelocity(vel.x, upSpeed.get() * 2, vel.z);
        }
    }
}
