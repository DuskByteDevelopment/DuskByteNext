
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.ColorSetting;
import dev.duskbyte.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 64, 16, 256, 16));
    private final ColorSetting color = add(new ColorSetting("Color", 0x4444FF));

    public Tracers() {
        super("Tracers", "Draws lines to entities", Category.RENDER);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        MatrixStack ms = context.matrixStack();
        if (ms == null) return;
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d eye = mc.player.getEyePos();
        int c = color.get();
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        double maxDist = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive()) continue;
            double dist = mc.player.distanceTo(e);
            if (dist > maxDist) continue;
            Vec3d target = e.getPos().add(0, e.getStandingEyeHeight() * 0.5, 0).subtract(cam);
            float a = (float) (1.0 - dist / maxDist);
            int colorInt = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
            vc.vertex(ms.peek().getPositionMatrix(), 0, (float)(eye.y - cam.y), 0).color(colorInt);
            vc.vertex(ms.peek().getPositionMatrix(), (float)target.x, (float)target.y, (float)target.z).color(colorInt);
        }
    }
}
