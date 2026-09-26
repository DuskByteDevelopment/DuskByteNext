
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

public class Parkour extends Module {
    private final NumberSetting edgeDist = add(new NumberSetting("Edge Distance", 0.1, 0.05, 0.5, 0.05));

    public Parkour() {
        super("Parkour", "Jump when reaching edge of block", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.player.isSpectator() || mc.player.isCreative()) return;

        // Auto jump when on edge
        if (mc.player.isOnGround() && mc.options.jumpKey.isPressed()) {
            mc.player.jump();
        }
    }
}
