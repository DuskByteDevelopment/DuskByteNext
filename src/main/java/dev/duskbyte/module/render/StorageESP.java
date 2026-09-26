
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class StorageESP extends Module {
    private final BoolSetting chests = add(new BoolSetting("Chests", true));
    private final BoolSetting enderChests = add(new BoolSetting("Ender Chests", true));
    private final BoolSetting shulkers = add(new BoolSetting("Shulkers", true));
    private final ColorSetting color = add(new ColorSetting("Color", 0xFFFF00));

    public StorageESP() {
        super("StorageESP", "Highlights storage blocks through walls", Category.RENDER);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        MatrixStack ms = context.matrixStack();
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        int c = color.get();
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;

        // Iterate loaded chunks and their block entities
        ChunkPos playerChunk = mc.player.getChunkPos();
        int viewDist = 4;
        for (int cx = playerChunk.x - viewDist; cx <= playerChunk.x + viewDist; cx++) {
            for (int cz = playerChunk.z - viewDist; cz <= playerChunk.z + viewDist; cz++) {
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                if (chunk == null) continue;
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    boolean shouldRender = false;
                    if (chests.get() && be instanceof ChestBlockEntity) shouldRender = true;
                    if (enderChests.get() && be instanceof EnderChestBlockEntity) shouldRender = true;
                    if (shulkers.get() && be instanceof ShulkerBoxBlockEntity) shouldRender = true;

                    if (!shouldRender) continue;
                    if (mc.player.squaredDistanceTo(be.getPos().getX() + 0.5, be.getPos().getY() + 0.5, be.getPos().getZ() + 0.5) > 64 * 64) continue;

                    Box box = new Box(be.getPos()).offset(-cam.x, -cam.y, -cam.z).expand(0.02);
                    WorldRenderer.drawBox(ms, vc, box, r, g, b, 1.0f);
                }
            }
        }
    }
}
