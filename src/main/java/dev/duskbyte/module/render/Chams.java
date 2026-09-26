
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.BoolSetting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class Chams extends Module {
    private final BoolSetting throughWalls = add(new BoolSetting("ThroughWalls", true));

    public Chams() {
        super("Chams", "Draws entity boxes with filled color", Category.RENDER);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        MatrixStack ms = context.matrixStack();
        if (ms == null) return;
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        var cam = mc.gameRenderer.getCamera().getPos();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive()) continue;
            Box box = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z).expand(0.05, 0.05, 0.05);
            int c = (e instanceof PlayerEntity) ? 0xFF0000 : 0x00FF00;
            WorldRenderer.drawBox(ms, vc, box, ((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f, 1.0f);
        }
    }
}
