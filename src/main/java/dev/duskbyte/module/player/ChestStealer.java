
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", 50, 0, 500, 50));
    private long lastSteal = 0;

    public ChestStealer() {
        super("ChestStealer", "Steals items from open chests", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen == null) return;
        if (!(mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> screen)) return;
        if (!(screen.getScreenHandler() instanceof GenericContainerScreenHandler handler)) return;
        long now = System.currentTimeMillis();
        if (now - lastSteal < delay.get()) return;
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.slots.get(i);
            if (slot.hasStack() && !(slot.inventory == mc.player.getInventory())) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                lastSteal = now;
                return;
            }
        }
    }
}
