
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        // Claim on-ground when falling to prevent fall damage
        if (mc.player.fallDistance > 2.5f) {
            mc.player.setOnGround(true);
        }
    }
}
