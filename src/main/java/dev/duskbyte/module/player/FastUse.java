
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

/**
 * Use items faster. (Meteor feature)
 */
public class FastUse extends Module {
    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 2.0, 1.0, 5.0, 0.5));

    public FastUse() {
        super("FastUse", "Uses items faster", Category.PLAYER);
    }
}
