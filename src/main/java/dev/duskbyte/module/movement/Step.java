
package dev.duskbyte.module.movement;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;

public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", 2.5, 1.0, 10.0, 0.5));

    public Step() {
        super("Step", "Step up blocks instantly", Category.MOVEMENT);
    }
    // Actual logic handled via mixin or stepHeight accessor
    // For now this is a placeholder - enable to boost stepHeight via mixin
}
