
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", 2.5, 1.0, 10.0, 0.5));

    public Step() {
        super("Step", "Step up blocks instantly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.stepHeight = (float) height.get().doubleValue();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.stepHeight = 0.6f;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.stepHeight = (float) height.get().doubleValue();
    }
}
