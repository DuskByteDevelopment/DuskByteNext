
package dev.duskbyte.module.misc;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AntiAFK extends Module {
    private final NumberSetting interval = add(new NumberSetting("Interval", 2.0, 0.5, 10.0, 0.5));
    private long lastAction = 0;
    private int swingCount = 0;

    public AntiAFK() {
        super("AntiAFK", "Prevents AFK kicks", Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - lastAction < interval.get() * 1000) return;
        lastAction = now;
        swingCount++;
        if (swingCount % 2 == 0) {
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, 0.42, vel.z);
            mc.player.setOnGround(false);
        }
    }
}
