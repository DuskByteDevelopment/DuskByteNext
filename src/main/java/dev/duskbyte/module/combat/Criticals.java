
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.entity.Entity;

public class Criticals extends Module {
    public Criticals() {
        super("Criticals", "Makes attacks critical hits", Category.COMBAT);
    }

    @Override
    public void onAttack(Entity target) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) {
            mc.player.setVelocity(mc.player.getVelocity().add(0, 0.42, 0));
            mc.player.setOnGround(false);
        }
    }
}
