
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.gui.util.ColorUtils;
import dev.duskbyte.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Argon-style color setting with preview square.
 */
public final class ColorBoxSetting extends RenderableSetting {
    private final ColorSetting setting;
    private Color currentAlpha;

    public ColorBoxSetting(ModuleButton parent, ColorSetting setting, int offset) {
        super(parent, setting, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // Label
        ctx.drawTextWithShadow(mc.textRenderer, setting.getName(),
            parentX() + 5, parentY() + parentOffset() + offset + 7, 0xFFF5F5F5);

        // Color preview with border
        int boxX = parentX() + parentWidth() - 18;
        int boxY = parentY() + parentOffset() + offset + 4;
        ctx.fill(boxX - 1, boxY - 1, boxX + 13, boxY + 13, 0xFF000000); // Border
        ctx.fill(boxX, boxY, boxX + 12, boxY + 12, 0xFF000000 | setting.get()); // Color

        renderHover(ctx, mouseX, mouseY);
    }

    private void renderHover(DrawContext ctx, int mouseX, int mouseY) {
        if (parent.parent.dragging) return;
        int toAlpha = isHovered(mouseX, mouseY) ? 15 : 0;
        if (currentAlpha == null) currentAlpha = new Color(255, 255, 255, toAlpha);
        else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());
        if (currentAlpha.getAlpha() != toAlpha)
            currentAlpha = ColorUtils.smoothAlphaTransition(0.05f, toAlpha, currentAlpha);
        ctx.fill(parentX(), parentY() + parentOffset() + offset,
            parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(),
            currentAlpha.getRGB());
    }

    @Override
    public void onGuiClose() { currentAlpha = null; }
}
