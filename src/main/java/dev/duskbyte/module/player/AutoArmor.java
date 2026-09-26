
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
        var targetStack = targetSlot.getStack();

        // Determine body part from equipped item
        EquipmentSlot equipSlot = getEquipmentSlot(targetSlot);

        // Find best armor in inventory for this slot
        int bestIndex = -1;
        int bestScore = getArmorScore(targetStack.getItem());

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
        if (stack.isEmpty()) {
            // Guess based on index
            return switch (slot.getIndex()) {
                case 36 -> EquipmentSlot.FEET;
                case 37 -> EquipmentSlot.LEGS;
                case 38 -> EquipmentSlot.CHEST;
                case 39 -> EquipmentSlot.HEAD;
                default -> EquipmentSlot.MAINHAND;
            };
        }
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getSlotType();
        }
        return EquipmentSlot.MAINHAND;
    }

    private int getArmorScore(Item item) {
        return switch (item) {
            case NetheriteHelmet h -> 53;
            case DiamondHelmet h -> 52;
            case IronHelmet h -> 33;
            case ChainmailHelmet h -> 23;
            case GoldenHelmet h -> 13;
            case LeatherHelmet h -> 12;
            case NetheriteChestplate c -> 58;
            case DiamondChestplate c -> 57;
            case IronChestplate c -> 38;
            case ChainmailChestplate c -> 28;
            case GoldenChestplate c -> 18;
            case LeatherTunic c -> 17;
            case NetheriteLeggings l -> 55;
            case DiamondLeggings l -> 54;
            case IronLeggings l -> 35;
            case ChainmailLeggings l -> 25;
            case GoldenLeggings l -> 15;
            case LeatherLeggings l -> 14;
            case NetheriteBoots b -> 53;
            case DiamondBoots b -> 52;
            case IronBoots b -> 33;
            case ChainmailBoots b -> 23;
            case GoldenBoots b -> 13;
            case LeatherBoots b -> 12;
            default -> 0;
        };
    }
}
