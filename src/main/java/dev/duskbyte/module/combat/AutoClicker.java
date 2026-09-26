
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.util.Hand;

public class AutoClicker extends Module {
    private final NumberSetting cps = add(new NumberSetting("CPS", 12, 1, 20, 1));
    private long lastClick = 0;

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - lastClick >= 1000.0 / cps.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            lastClick = now;
        }
    }
}
