
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay", 100, 0, 500, 50));

    public AutoArmor() {
        super("AutoArmor", "Automatically equips best armor", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();

        // Armor slot indices in PlayerScreenHandler (5=helmet,6=chest,7=legs,8=boots)
        for (int armorSlot = 5; armorSlot <= 8; armorSlot++) {
            if (now - lastEquip < delay.get()) return;
            equipBestForSlot(armorSlot);
        }
    }

    private long lastEquip = 0;

    private void equipBestForSlot(int armorSlotIndex) {
        var sh = mc.player.playerScreenHandler;
        Slot targetSlot = sh.slots.get(armorSlotIndex);

        // Determine body part from equipped item
        EquipmentSlot equipSlot = getEquipmentSlot(targetSlot);

        // Find best armor in inventory for this slot
        int bestIndex = -1;
        int bestScore = getArmorScore(targetSlot.getStack().getItem());

        for (int i = 9; i < sh.slots.size(); i++) {
            Slot slot = sh.slots.get(i);
            var stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem armor)) continue;

            // Check if this armor fits this slot
            if (armor.getSlotType() != equipSlot) continue;

            int score = getArmorScore(stack.getItem());
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        if (bestIndex != -1) {
            mc.interactionManager.clickSlot(sh.syncId, bestIndex, 0, SlotActionType.QUICK_MOVE, mc.player);
            lastEquip = System.currentTimeMillis();
        }
    }

    private EquipmentSlot getEquipmentSlot(Slot slot) {
        var stack = slot.getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armor) {
            return armor.getSlotType();
        }
        // Guess from inventory index
        int idx = slot.getIndex();
        if (idx >= 36 && idx <= 39) {
            return switch (idx) {
                case 36 -> EquipmentSlot.FEET;
                case 37 -> EquipmentSlot.LEGS;
                case 38 -> EquipmentSlot.CHEST;
                case 39 -> EquipmentSlot.HEAD;
                default -> EquipmentSlot.MAINHAND;
            };
        }
        return EquipmentSlot.MAINHAND;
    }

    private int getArmorScore(Item item) {
        // Helmet
        if (item == Items.NETHERITE_HELMET) return 53;
        if (item == Items.DIAMOND_HELMET) return 52;
        if (item == Items.IRON_HELMET) return 33;
        if (item == Items.CHAINMAIL_HELMET) return 23;
        if (item == Items.GOLDEN_HELMET) return 13;
        if (item == Items.LEATHER_HELMET) return 12;
        // Chestplate
        if (item == Items.NETHERITE_CHESTPLATE) return 58;
        if (item == Items.DIAMOND_CHESTPLATE) return 57;
        if (item == Items.IRON_CHESTPLATE) return 38;
        if (item == Items.CHAINMAIL_CHESTPLATE) return 28;
        if (item == Items.GOLDEN_CHESTPLATE) return 18;
        if (item == Items.LEATHER_CHESTPLATE) return 17;
        // Leggings
        if (item == Items.NETHERITE_LEGGINGS) return 55;
        if (item == Items.DIAMOND_LEGGINGS) return 54;
        if (item == Items.IRON_LEGGINGS) return 35;
        if (item == Items.CHAINMAIL_LEGGINGS) return 25;
        if (item == Items.GOLDEN_LEGGINGS) return 15;
        if (item == Items.LEATHER_LEGGINGS) return 14;
        // Boots
        if (item == Items.NETHERITE_BOOTS) return 53;
        if (item == Items.DIAMOND_BOOTS) return 52;
        if (item == Items.IRON_BOOTS) return 33;
        if (item == Items.CHAINMAIL_BOOTS) return 23;
        if (item == Items.GOLDEN_BOOTS) return 13;
        if (item == Items.LEATHER_BOOTS) return 12;
        return 0;
    }
}
