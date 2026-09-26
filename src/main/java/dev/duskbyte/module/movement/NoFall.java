
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2CPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (mc.player.fallDistance > 2.5f) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2CPacket.OnGroundOnly(true, mc.player.horizontalCollision));
        }
    }
}
