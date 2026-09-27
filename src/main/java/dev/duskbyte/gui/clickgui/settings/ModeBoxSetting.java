
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.gui.util.ColorUtils;
import dev.duskbyte.module.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Argon-style mode/cycle setting.
 */
public final class ModeBoxSetting extends RenderableSetting {
    private final ModeSetting setting;
    private Color currentAlpha;

    public ModeBoxSetting(ModuleButton parent, ModeSetting setting, int offset) {
        super(parent, setting, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // Label
        ctx.drawTextWithShadow(mc.textRenderer, setting.getName(),
            parentX() + 5, parentY() + parentOffset() + offset + 7, 0xFFF5F5F5);

        // Value pill
        String val = setting.get();
        int vw = mc.textRenderer.getWidth(val) + 8;
        int vx = parentX() + parentWidth() - vw - 4;
        Color accent = ColorUtils.getMainColor(255, parent.settings.indexOf(this));
        ctx.fillGradient(vx, parentY() + parentOffset() + offset + 3,
            vx + vw, parentY() + parentOffset() + offset + parentHeight() - 3,
            accent.getRGB(), accent.darker().getRGB());
        ctx.drawTextWithShadow(mc.textRenderer, val,
            vx + 4, parentY() + parentOffset() + offset + 7, Color.WHITE.getRGB());

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
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) setting.cycle();
    }

    @Override
    public void onGuiClose() { currentAlpha = null; }
}
