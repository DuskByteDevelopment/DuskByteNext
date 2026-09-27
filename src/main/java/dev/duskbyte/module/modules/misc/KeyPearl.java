package dev.duskbyte.module.modules.misc;

import dev.duskbyte.event.events.TickListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.KeybindSetting;
import dev.duskbyte.module.setting.NumberSetting;
import dev.duskbyte.utils.InventoryUtils;
import dev.duskbyte.utils.KeyUtils;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class KeyPearl extends Module implements TickListener {
    private final KeybindSetting activateKey = new KeybindSetting(Activate Key, -1, false);
    private final NumberSetting delay = new NumberSetting(Delay, 0, 20, 0, 1);
    private final BooleanSetting switchBack = new BooleanSetting(Switch Back, true);
    private final NumberSetting switchDelay = new NumberSetting(Switch Delay, 0, 20, 0, 1)
            .setDescription(Delay after throwing pearl before switching back);

    private boolean active, hasActivated;
    private int clock, previousSlot, switchClock;

    public KeyPearl() {
        super(Key Pearl, Switches to an ender pearl and throws it when you press a bind, -1, Category.MISC);
        addSettings(activateKey, delay, switchBack, switchDelay);
    }

    @Override
    public void onEnable() {
        eventManager.add(TickListener.class, this);
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if(mc.currentScreen != null)
            return;

        if(KeyUtils.isKeyPressed(activateKey.getKey())) {
            active = true;
        }

        if(active) {
            if(previousSlot == -1)
                previousSlot = mc.player.getInventory().selectedSlot;

            InventoryUtils.selectItemFromHotbar(Items.ENDER_PEARL);

            if(clock < delay.getValueInt()) {
                clock++;
                return;
            }

            if(!hasActivated) {
                ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                if (result.isAccepted() && result.shouldSwingHand())
                    mc.player.swingHand(Hand.MAIN_HAND);

                hasActivated = true;
            }

            if(switchBack.getValue())
                switchBack();
            else reset();
        }
    }

    private void switchBack() {
        if(switchClock < switchDelay.getValueInt()) {
            switchClock++;
            return;
        }

        InventoryUtils.setInvSlot(previousSlot);
        reset();
    }

    private void reset() {
        previousSlot = -1;
        clock = 0;
        switchClock = 0;
        active = false;
        hasActivated = false;
    }
}
