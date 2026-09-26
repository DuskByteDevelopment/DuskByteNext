
package dev.duskbyte.hud;

import dev.duskbyte.DuskByteClient;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import java.awt.Color;
import java.util.List;

public class HudRenderer {
    public static void render(DrawContext ctx, RenderTickCounter counter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || mc.currentScreen != null) return;

        // Rainbow gradient watermark
        int rainbow = Color.HSBtoRGB((System.currentTimeMillis() % 5000) / 5000f, 0.8f, 1f);
        String watermark = "DuskByte " + DuskByteClient.VERSION;
        int ww = mc.textRenderer.getWidth(watermark);

        ctx.fill(4, 4, ww + 12, 18, 0x88000000);
        ctx.fill(4, 4, 6, 18, rainbow);
        ctx.drawTextWithShadow(mc.textRenderer, "§l" + watermark, 10, 7, rainbow);

        // ArrayList - sorted by width desc
        List<Module> enabled = ModuleManager.getEnabled();
        enabled.sort((a, b) -> mc.textRenderer.getWidth(b.getTitle().getString()) - mc.textRenderer.getWidth(a.getTitle().getString()));
        int y = 22;
        int index = 0;
        for (Module m : enabled) {
            int color = Color.HSBtoRGB((index * 0.06f + (System.currentTimeMillis() % 5000) / 5000f) % 1f, 0.7f, 1f);
            String modName = m.getTitle().getString();
            int w = mc.textRenderer.getWidth(modName);
            int right = this.width(mc);

            // Background pill
            ctx.fill(right - w - 16, y - 2, right, y + 10, 0x88000000);
            // Color accent
            ctx.fill(right - w - 16, y - 2, right - w - 14, y + 10, color | 0xFF000000);

            ctx.drawTextWithShadow(mc.textRenderer, modName, right - w - 12, y, color);
            y += 12;
            index++;
        }

        // FPS + Coords + Server
        String server = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
        String info = String.format("FPS:%d | %s | XYZ: %.0f %.0f %.0f",
                mc.getCurrentFps(), server,
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int iw = mc.textRenderer.getWidth(info);

        ctx.fill(4, y + 4, iw + 12, y + 18, 0x88000000);
        ctx.fill(4, y + 4, 6, y + 18, 0x4444FF);
        ctx.drawTextWithShadow(mc.textRenderer, info, 10, y + 7, 0xFFFFFF);
    }

    private static int width(DrawContext ctx) {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }
}
