
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.BoolSetting;
import dev.duskbyte.setting.ColorSetting;
import dev.duskbyte.setting.NumberSetting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {
    private final NumberSetting lineWidth = add(new NumberSetting("LineWidth", 2.0, 0.5, 5.0, 0.5));
    private final ColorSetting color = add(new ColorSetting("Color", 0xFF00FF));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));

    public ESP() {
        super("ESP", "Highlights entities through walls", Category.RENDER);
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
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive()) continue;
            if (e instanceof PlayerEntity && !players.get()) continue;
            if (e instanceof Monster && !mobs.get()) continue;
            if (e instanceof AnimalEntity && !animals.get()) continue;
            Box box = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z).expand(0.05, 0.05, 0.05);
            WorldRenderer.drawBox(ms, vc, box, r, g, b, 1.0f);
        }
    }
}
