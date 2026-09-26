
package dev.duskbyte.module.misc;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

public class Timer extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 0.5, 10.0, 0.5));

    public Timer() {
        super("Timer", "Changes game tick speed", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.world != null) mc.world.getTickManager().setTickRate((float) speed.get().doubleValue());
    }

    @Override
    public void onDisable() {
        if (mc.world != null) mc.world.getTickManager().setTickRate(20.0f);
    }

    @Override
    public void onTick() {
        if (mc.world != null) mc.world.getTickManager().setTickRate((float) speed.get().doubleValue());
    }
}
