
package dev.duskbyte.module.player;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

/**
 * Opens nearby chests automatically. (Meteor feature)
 */
public class ChestAura extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 4.5, 1.0, 6.0, 0.5));
    private final NumberSetting delay = add(new NumberSetting("Delay", 200, 0, 1000, 50));
    private long lastOpen = 0;

    public ChestAura() {
        super("ChestAura", "Automatically opens nearby chests", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - lastOpen < delay.get()) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get().intValue();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockEntity be = mc.world.getBlockEntity(pos);
                    if (be instanceof ChestBlockEntity || be instanceof EnderChestBlockEntity) {
                        if (mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= range.get() * range.get()) {
                            BlockHitResult hit = new BlockHitResult(
                                pos.toCenterPos(), Direction.UP, pos, false);
                            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                            lastOpen = now;
                            return;
                        }
                    }
                }
            }
        }
    }
}
