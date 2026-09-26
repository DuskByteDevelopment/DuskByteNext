
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEat extends Module {
    public AutoEat() {
        super("AutoEat", "Automatically eats food", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (mc.player.getHungerManager().getFoodLevel() > 18) return;
        if (mc.player.getMainHandStack().getFoodComponent() != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
