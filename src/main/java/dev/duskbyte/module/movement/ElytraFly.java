
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.8, 0.5, 5.0, 0.1));

    public ElytraFly() {
        super("ElytraFly", "Enhanced elytra flight", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (!mc.player.isFallFlying()) {
            if (mc.player.input.jumping && mc.player.fallDistance > 1.5f) {
                mc.player.startFallFlying();
            }
            return;
        }
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.movementForward;
        double sideways = mc.player.input.movementSideways;
        double vx = -Math.sin(Math.toRadians(yaw)) * forward + Math.cos(Math.toRadians(yaw)) * sideways;
        double vz = Math.cos(Math.toRadians(yaw)) * forward - Math.sin(Math.toRadians(yaw)) * sideways;
        double vy = mc.player.input.jumping ? 0.5 : mc.player.input.sneaking ? -0.5 : 0.0;
        mc.player.setVelocity(vx * speed.get(), vy, vz * speed.get());
        mc.player.fallDistance = 0;
    }
}
