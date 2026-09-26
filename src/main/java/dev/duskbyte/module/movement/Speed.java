
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.5, 1.0, 5.0, 0.1));

    public Speed() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (!mc.player.isOnGround() || mc.player.isSneaking()) return;
        Vec3d vel = mc.player.getVelocity();
        double horizontal = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (horizontal > 0.1 && horizontal < speed.get()) {
            double mult = speed.get() / horizontal;
            mc.player.setVelocity(vel.x * mult, vel.y, vel.z * mult);
        }
    }
}
