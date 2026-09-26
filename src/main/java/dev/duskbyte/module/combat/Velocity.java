
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;

public class Velocity extends Module {
    public Velocity() {
        super("Velocity", "Reduces knockback from attacks", Category.COMBAT);
    }
    // Logic is in ClientPlayNetworkHandlerMixin - cancels velocity packets
}
