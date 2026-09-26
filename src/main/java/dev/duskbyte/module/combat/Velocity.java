
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

public class Velocity extends Module {
    private final NumberSetting horizontal = add(new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 5.0));
    private final NumberSetting vertical = add(new NumberSetting("Vertical", 0.0, 0.0, 100.0, 5.0));

    private int hurtTimer = 0;

    public Velocity() {
        super("Velocity", "Reduces knockback from attacks", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Track hurt time
        if (mc.player.hurtTime > 0) {
            hurtTimer = mc.player.hurtTime;
        }
        // Apply velocity reduction right after being hit
        if (hurtTimer > 0 && hurtTimer < mc.player.hurtTime + 1) {
            double hMod = horizontal.get() / 100.0;
            double vMod = vertical.get() / 100.0;
            mc.player.setVelocity(
                mc.player.getVelocity().x * hMod,
                mc.player.getVelocity().y * vMod,
                mc.player.getVelocity().z * hMod
            );
        }
        if (hurtTimer > 0) hurtTimer--;
    }

    @Override
    public void onDisable() {
        hurtTimer = 0;
    }
}
