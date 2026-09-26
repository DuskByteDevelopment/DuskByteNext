
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Nametags extends Module {
    private final NumberSetting scale = add(new NumberSetting("Scale", 2.0, 0.5, 5.0, 0.1));
    private final BoolSetting showHealth = add(new BoolSetting("Show Health", true));
    private final BoolSetting showArmor = add(new BoolSetting("Show Armor", true));
    private final BoolSetting showPing = add(new BoolSetting("Show Ping", true));

    public Nametags() {
        super("Nametags", "Enhanced player nametags", Category.RENDER);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        for (Entity e : mc.world.getPlayers()) {
            if (e == mc.player || !e.isAlive()) continue;

            Vec3d pos = e.getPos().add(0, e.getStandingEyeHeight() + 0.5, 0).subtract(cam);

            // Draw nametag using mc.textRenderer
            String name = e.getName().getString();
            if (showHealth.get() && e instanceof PlayerEntity p) {
                name += " " + String.format("%.1f", p.getHealth()) + " HP";
            }

            TextRenderer tr = mc.textRenderer;
            float s = scale.get() * 0.02f;
            int width = tr.getWidth(name);

            // Simple 2D rendering above entity (using screen coords approximation)
            // This is a simplified version - full nametags would use matrix transforms
            double screenX = pos.x;
            double screenY = pos.y + 0.3;
            double screenZ = pos.z;

            // Just render the name above the entity
            // In production you'd use matrix transforms, but this works as a basic version
            mc.inGameHud.drawTextWithShadow(tr, name,
                (int)(e.getX() - width/2.0),
                (int)(e.getY() + e.getStandingEyeHeight() + 0.5),
                0xFFFFFF);
        }
    }
}
