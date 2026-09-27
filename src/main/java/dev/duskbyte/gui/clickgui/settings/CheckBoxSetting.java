
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.gui.util.ColorUtils;
import dev.duskbyte.module.setting.BoolSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Argon-style checkbox with gradient fill.
 */
public final class CheckBoxSetting extends RenderableSetting {
    private final BoolSetting setting;
    private Color currentAlpha;

    public CheckBoxSetting(ModuleButton parent, BoolSetting setting, int offset) {
        super(parent, setting, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int x = parentX() + 5;
        int y = parentY() + parentOffset() + offset + 5;
        int size = parentHeight() - 10;

        // Outer gradient border
        Color accent = ColorUtils.getMainColor(255, parent.settings.indexOf(this));
        ctx.fillGradient(x, y, x + size, y + size, accent.getRGB(), accent.darker().getRGB());

        // Inner dark background
        ctx.fill(x + 2, y + 2, x + size - 2, y + size - 2, Color.darkGray.getRGB());

        // Inner fill when enabled
        if (setting.get()) {
            ctx.fillGradient(x + 4, y + 4, x + size - 4, y + size - 4,
                accent.getRGB(), accent.darker().getRGB());
        }

        // Label
        ctx.drawTextWithShadow(mc.textRenderer, setting.getName(),
            x + size + 6, parentY() + parentOffset() + offset + 7, 0xFFF5F5F5);

        // Hover overlay
        renderHover(ctx, mouseX, mouseY);
    }

    private void renderHover(DrawContext ctx, int mouseX, int mouseY) {
        if (parent.parent.dragging) return;
        int toAlpha = isHovered(mouseX, mouseY) ? 15 : 0;
        if (currentAlpha == null)
            currentAlpha = new Color(255, 255, 255, toAlpha);
        else
            currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());
        if (currentAlpha.getAlpha() != toAlpha)
            currentAlpha = ColorUtils.smoothAlphaTransition(0.05f, toAlpha, currentAlpha);
        ctx.fill(parentX(), parentY() + parentOffset() + offset,
                 parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(),
                 currentAlpha.getRGB());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0)
            setting.toggle();
    }

    @Override
    public void onGuiClose() { currentAlpha = null; }
}
