
package dev.duskbyte.module.misc;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import dev.duskbyte.setting.StringSetting;
import org.lwjgl.glfw.GLFW;

/**
 * Auto disconnects when health is low. (Meteor feature)
 */
public class AutoLog extends Module {
    private final NumberSetting health = add(new NumberSetting("Health", 8.0, 1.0, 20.0, 0.5));
    private final StringSetting reason = add(new StringSetting("Reason", "AutoLog"));

    public AutoLog() {
        super("AutoLog", "Disconnects when health is low", Category.MISC, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (mc.player.getHealth() <= health.get()) {
            if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
                mc.getNetworkHandler().getConnection().disconnect(
                    net.minecraft.text.Text.literal(reason.get()));
            }
            mc.disconnect();
        }
    }
}
