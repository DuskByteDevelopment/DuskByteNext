
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.BoolSetting;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

public class AutoWeapon extends Module {
    private final BoolSetting swords = add(new BoolSetting("Swords", true));
    private final BoolSetting axes = add(new BoolSetting("Axes", true));
    private int lastSlot = -1;

    public AutoWeapon() {
        super("AutoWeapon", "Switches to best weapon before attacking", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (lastSlot != -1) {
            mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                36 + lastSlot, 0, SlotActionType.SWAP, mc.player);
            lastSlot = -1;
        }
    }

    @Override
    public void onAttack(net.minecraft.entity.Entity target) {
        if (mc.player == null) return;
        int bestSlot = findBestWeapon();
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            lastSlot = mc.player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                36 + bestSlot, 0, SlotActionType.SWAP, mc.player);
        }
    }

    private int findBestWeapon() {
        int bestSlot = -1;
        int bestScore = -1;
        var inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            int score = getWeaponScore(stack.getItem());
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private int getWeaponScore(Item item) {
        if (item instanceof SwordItem) {
            if (!swords.get()) return 0;
            return getMaterialScore(item) * 100;
        }
        if (item instanceof AxeItem) {
            if (!axes.get()) return 0;
            return getMaterialScore(item) * 100 - 1;
        }
        return 0;
    }

    private int getMaterialScore(Item item) {
        if (item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE) return 6;
        if (item == Items.DIAMOND_SWORD || item == Items.DIAMOND_AXE) return 5;
        if (item == Items.IRON_SWORD || item == Items.IRON_AXE) return 3;
        if (item == Items.STONE_SWORD || item == Items.STONE_AXE) return 2;
        if (item == Items.WOODEN_SWORD || item == Items.WOODEN_AXE) return 1;
        return 0;
    }
}
