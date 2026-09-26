
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", "Auto equips totems of undying", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        var sh = mc.player.playerScreenHandler;
        for (int i = 0; i < sh.slots.size(); i++) {
            Slot slot = sh.slots.get(i);
            if (slot.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                mc.interactionManager.clickSlot(sh.syncId, i, 40, SlotActionType.SWAP, mc.player);
                break;
            }
        }
    }
}
