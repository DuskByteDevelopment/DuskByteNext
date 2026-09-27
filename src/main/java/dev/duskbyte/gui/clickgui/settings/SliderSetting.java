
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.gui.util.*;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Argon-style number slider with gradient fill and smooth animation.
 */
public final class SliderSetting extends RenderableSetting {
    private final NumberSetting setting;
    public boolean dragging;
    private double lerpedOffsetX = 0;
    private Color currentAlpha;
    private Color currentColor1, currentColor2;

    public SliderSetting(ModuleButton parent, NumberSetting setting, int offset) {
        super(parent, setting, offset);
        this.setting = setting;
    }

    @Override
    public void onUpdate() {
        Color c1 = ColorUtils.getMainColor(0, parent.settings.indexOf(this)).darker();
        Color c2 = ColorUtils.getMainColor(0, parent.settings.indexOf(this) + 1).darker();
        if (currentColor1 == null) currentColor1 = c1;
        else currentColor1 = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), currentColor1.getAlpha());
        if (currentColor2 == null) currentColor2 = c2;
        else currentColor2 = new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), currentColor2.getAlpha());
        if (currentColor1.getAlpha() != 255) currentColor1 = ColorUtils.smoothAlphaTransition(0.05f, 255, currentColor1);
        if (currentColor2.getAlpha() != 255) currentColor2 = ColorUtils.smoothAlphaTransition(0.05f, 255, currentColor2);
        super.onUpdate();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        double range = setting.getMax() - setting.getMin();
        double offset = range > 0 ? (setting.get() - setting.getMin()) / range * parentWidth() : 0;
        lerpedOffsetX = MathUtils.goodLerp(0.5f * delta, lerpedOffsetX, offset);

        // Gradient fill bar
        ctx.fillGradient(parentX(), parentY() + parentOffset() + this.offset + 25,
            (int) (parentX() + lerpedOffsetX), parentY() + parentOffset() + parentHeight(),
            currentColor1.getRGB(), currentColor2.getRGB());

        // Label
        ctx.drawTextWithShadow(mc.textRenderer,
            setting.getName() + ": " + String.format("%.1f", setting.get()),
            parentX() + 5, parentY() + parentOffset() + this.offset + 9, 0xFFF5F5F5);

        // Hover
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

    private void slide(double mouseX) {
        double a = mouseX - parentX();
        double b = Math.max(0, Math.min(1, a / parentWidth()));
        setting.set(MathUtils.roundToDecimal(b * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getStep()));
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) { dragging = true; slide(mouseX); }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) dragging = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) slide(mouseX);
    }

    @Override
    public void onGuiClose() { currentAlpha = null; currentColor1 = null; currentColor2 = null; }
}
