
package dev.duskbyte.module.misc;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * Middle click to throw ender pearl. (Meteor feature)
 */
public class MiddleClickPearl extends Module {
    private boolean wasPressed = false;

    public MiddleClickPearl() {
        super("MiddleClickPearl", "Middle click to throw ender pearl", Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;

        boolean pressed = mc.options.pickItemKey.isPressed();
        if (pressed && !wasPressed) {
            int pearlSlot = -1;
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                    pearlSlot = i;
                    break;
                }
            }
            if (pearlSlot != -1) {
                int oldSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = pearlSlot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = oldSlot;
            }
        }
        wasPressed = pressed;
    }
}
