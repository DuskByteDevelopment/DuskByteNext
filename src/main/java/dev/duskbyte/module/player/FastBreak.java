
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

/**
 * Faster block breaking. (Meteor feature)
 */
public class FastBreak extends Module {
    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 1.5, 1.0, 5.0, 0.1));

    public FastBreak() {
        super("FastBreak", "Breaks blocks faster", Category.PLAYER);
    }
}
