
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;

/**
 * Prevents server from rotating your camera. (Meteor feature)
 */
public class NoRotate extends Module {
    private float lastYaw, lastPitch;

    public NoRotate() {
        super("NoRotate", "Prevents server from rotating camera", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        // Restore rotation if server tried to change it
        if (mc.player.getYaw() != lastYaw || mc.player.getPitch() != lastPitch) {
            mc.player.setYaw(lastYaw);
            mc.player.setPitch(lastPitch);
        }
        lastYaw = mc.player.getYaw();
        lastPitch = mc.player.getPitch();
    }
}
