
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

/**
 * Lock camera view angle. (Meteor feature)
 */
public class ViewLock extends Module {
    private final NumberSetting yaw = add(new NumberSetting("Yaw", 0.0, -180.0, 180.0, 1.0));
    private final NumberSetting pitch = add(new NumberSetting("Pitch", 0.0, -90.0, 90.0, 1.0));

    public ViewLock() {
        super("ViewLock", "Locks camera view angle", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        mc.player.setYaw((float) yaw.get());
        mc.player.setPitch((float) pitch.get());
    }
}
