
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));

    public Flight() {
        super("Flight", "Allows creative-style flight", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.getAbilities().flying = true;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.getAbilities().flying = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed((float)(0.05 * speed.get()));
        mc.player.fallDistance = 0;
    }
}
