
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.TextLayerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class Nametags extends Module {
    private final BoolSetting showHealth = add(new BoolSetting("Show Health", true));

    public Nametags() {
        super("Nametags", "Enhanced player nametags", Category.RENDER);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Matrix4f proj = context.projectionMatrix();
        VertexConsumerProvider consumers = context.consumers();

        for (Entity e : mc.world.getPlayers()) {
            if (e == mc.player || !e.isAlive()) continue;

            String name = e.getName().getString();
            if (showHealth.get() && e instanceof PlayerEntity p) {
                name += " " + String.format("%.1f", p.getHealth()) + " HP";
            }

            Vec3d pos = e.getPos().add(0, e.getStandingEyeHeight() + 0.5, 0).subtract(cam);
            var ms = context.matrixStack();
            ms.push();
            ms.translate(pos.x, pos.y, pos.z);
            ms.scale(0.025f, -0.025f, 0.025f);

            mc.textRenderer.draw(
                name,
                -mc.textRenderer.getWidth(name) / 2f, 0,
                0xFFFFFF, false,
                ms.peek().getPositionMatrix(),
                consumers,
                TextLayerType.SEE_THROUGH,
                0, 0xFFFFFF,
                false
            );
            ms.pop();
        }
    }
}
