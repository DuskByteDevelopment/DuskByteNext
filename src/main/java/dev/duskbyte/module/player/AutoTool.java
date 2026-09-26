
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.BoolSetting;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module {
    private final BoolSetting switchBack = add(new BoolSetting("Switch Back", true));
    private final NumberSetting delay = add(new NumberSetting("Delay", 50, 0, 200, 10));
    private int lastSlot = -1;
    private long lastSwitch = 0;

    public AutoTool() {
        super("AutoTool", "Switches to best tool when mining", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.currentScreen != null) return;
        if (!mc.options.attackKey.isPressed() || mc.crosshairTarget == null) {
            // Switch back when not mining
            if (switchBack.get() && lastSlot != -1) {
                mc.interactionManager.clickSlot(
                    mc.player.playerScreenHandler.syncId,
                    36 + lastSlot, 0, SlotActionType.SWAP, mc.player);
                lastSlot = -1;
            }
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)) return;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);

        long now = System.currentTimeMillis();
        if (now - lastSwitch < delay.get()) return;

        int bestSlot = findBestTool(state);
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            if (lastSlot == -1) lastSlot = mc.player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                36 + bestSlot, 0, SlotActionType.SWAP, mc.player);
            lastSwitch = now;
        }
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        float bestStrength = -1;
        var inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            float strength = stack.getMiningSpeedMultiplier(state);
            if (strength > bestStrength) {
                bestStrength = strength;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
