
package dev.duskbyte.module.render;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Nametags extends Module {
    private final BoolSetting showHealth = add(new BoolSetting("Show Health", true));

    public Nametags() {
        super("Nametags", "Enhanced player nametags", Category.RENDER);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        for (Entity e : mc.world.getPlayers()) {
            if (e == mc.player || !e.isAlive()) continue;

            String name = e.getName().getString();
            if (showHealth.get() && e instanceof PlayerEntity p) {
                name += " " + String.format("%.1f", p.getHealth()) + " HP";
            }

            // Project 3D position to screen
            Vec3d pos = e.getPos().add(0, e.getStandingEyeHeight() + 0.5, 0);
            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d rel = pos.subtract(camPos);

            // Check if behind camera
            double fov = mc.options.getFov().getValue();
            double viewDist = mc.getWindow().getScaledWidth() / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            // Simple projection (approximation)
            double halfW = mc.getWindow().getScaledWidth() / 2.0;
            double halfH = mc.getWindow().getScaledHeight() / 2.0;

            // Rotation by camera yaw/pitch
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);

            double cosY = Math.cos(-yawRad), sinY = Math.sin(-yawRad);
            double cosP = Math.cos(-pitchRad), sinP = Math.sin(-pitchRad);

            double rx = rel.x * cosY - rel.z * sinY;
            double ry = rel.y;
            double rz = rel.x * sinY + rel.z * cosY;

            double ry2 = ry * cosP - rz * sinP;
            double rz2 = ry * sinP + rz * cosP;

            if (rz2 <= 0.1) continue; // behind camera

            double screenX = halfW + (rx / rz2) * viewDist;
            double screenY = halfH - (ry2 / rz2) * viewDist;

            int textW = mc.textRenderer.getWidth(name);
            int x = (int)(screenX - textW / 2.0);
            int y = (int)(screenY - 8);

            // Background
            ctx.fill(x - 2, y - 1, x + textW + 2, y + 9, 0x88000000);
            ctx.drawTextWithShadow(mc.textRenderer, Text.literal(name), x, y, 0xFFFFFF);
        }
    }
}
