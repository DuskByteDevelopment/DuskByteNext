
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.*;
import dev.duskbyte.gui.clickgui.settings.*;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.module.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Argon-style module button with inline setting expansion.
 */
public final class ModuleButton {
    public List<RenderableSetting> settings = new ArrayList<>();
    public GuiWindow parent;
    public Module module;
    public int offset;
    public boolean extended;
    public Color currentColor;
    public Color defaultColor = Color.WHITE;
    public Color hoverColor;
    public AnimationUtils animation = new AnimationUtils(0);
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public ModuleButton(GuiWindow parent, Module module, int offset) {
        this.parent = parent;
        this.module = module;
        this.offset = offset;
        this.extended = false;

        int settingOffset = parent.getHeight();
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BoolSetting bs)
                settings.add(new CheckBoxSetting(this, bs, settingOffset));
            else if (s instanceof NumberSetting ns)
                settings.add(new SliderSetting(this, ns, settingOffset));
            else if (s instanceof ModeSetting ms)
                settings.add(new ModeBoxSetting(this, ms, settingOffset));
            else if (s instanceof StringSetting ss)
                settings.add(new StringBoxSetting(this, ss, settingOffset));
            else if (s instanceof ColorSetting cs)
                settings.add(new ColorBoxSetting(this, cs, settingOffset));
            settingOffset += parent.getHeight();
        }
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (parent.getY() + offset > mc.getWindow().getHeight()) return;

        // Update setting positions
        for (RenderableSetting rs : settings) rs.onUpdate();

        // Background
        if (currentColor == null)
            currentColor = new Color(0, 0, 0, 0);
        else
            currentColor = new Color(0, 0, 0, currentColor.getAlpha());
        currentColor = ColorUtils.smoothAlphaTransition(0.05f, 170, currentColor);

        // Module color (rainbow when enabled)
        Color toColor = module.isEnabled()
            ? ColorUtils.getMainColor(255, moduleSortIndex())
            : Color.WHITE;
        if (defaultColor == null) defaultColor = Color.WHITE;
        defaultColor = ColorUtils.smoothColorTransition(0.1f, toColor, defaultColor);

        // Draw background
        ctx.fill(parent.getX(), parent.getY() + offset,
                 parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset,
                 currentColor.getRGB());

        // Left accent bar
        Color accent = ColorUtils.getMainColor(255, moduleSortIndex());
        ctx.fillGradient(parent.getX(), parent.getY() + offset,
                         parent.getX() + 2, parent.getY() + parent.getHeight() + offset,
                         accent.getRGB(), accent.darker().getRGB());

        // Module name centered
        String name = module.getTitle().getString();
        int textW = mc.textRenderer.getWidth(name);
        int centerX = parent.getX() + parent.getWidth() / 2 - textW / 2;
        ctx.drawTextWithShadow(mc.textRenderer, name, centerX, parent.getY() + offset + 5, defaultColor.getRGB());

        // Hover effect
        renderHover(ctx, mouseX, mouseY);

        // Render inline settings when expanded
        if (extended && animation.getValue() > parent.getHeight()) {
            for (RenderableSetting rs : settings)
                rs.render(ctx, mouseX, mouseY, delta);
        }

        // Description tooltip
        if (isHovered(mouseX, mouseY) && !parent.dragging) {
            String desc = module.getDescription();
            int tw = mc.textRenderer.getWidth(desc);
            int screenCenter = mc.getWindow().getScaledWidth() / 2;
            int textX = screenCenter - tw / 2;
            int textY = mc.getWindow().getScaledHeight() - 16;
            ctx.fill(textX - 4, textY - 2, textX + tw + 4, textY + 12, 0xC8333333);
            ctx.drawTextWithShadow(mc.textRenderer, desc, textX, textY, Color.WHITE.getRGB());
        }
    }

    private void renderHover(DrawContext ctx, int mouseX, int mouseY) {
        if (parent.dragging) return;
        int toAlpha = isHovered(mouseX, mouseY) ? 30 : 0;
        if (hoverColor == null)
            hoverColor = new Color(255, 255, 255, toAlpha);
        else
            hoverColor = new Color(255, 255, 255, hoverColor.getAlpha());
        if (hoverColor.getAlpha() != toAlpha)
            hoverColor = ColorUtils.smoothAlphaTransition(0.05f, toAlpha, hoverColor);
        ctx.fill(parent.getX(), parent.getY() + offset,
                 parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset,
                 hoverColor.getRGB());
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) module.toggle();
            if (button == 1 && !module.getSettings().isEmpty()) {
                if (!extended) onExtend();
                extended = !extended;
            }
        }
        if (extended) {
            for (RenderableSetting rs : settings)
                rs.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (RenderableSetting rs : settings) rs.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (extended) {
            for (RenderableSetting rs : settings)
                rs.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
    }

    public void keyPressed(int keyCode, int sc, int mod) {
        for (RenderableSetting rs : settings) rs.keyPressed(keyCode, sc, mod);
    }

    public void onExtend() {
        for (ModuleButton btn : parent.moduleButtons) btn.extended = false;
    }

    public void onGuiClose() {
        currentColor = null;
        hoverColor = null;
        defaultColor = Color.WHITE;
        for (RenderableSetting rs : settings) rs.onGuiClose();
    }

    public boolean isHovered(double mx, double my) {
        return mx > parent.getX() && mx < parent.getX() + parent.getWidth()
            && my > parent.getY() + offset && my < parent.getY() + offset + parent.getHeight();
    }

    private int moduleSortIndex() {
        var list = ModuleManager.getByCategory(module.getCategory());
        return list.indexOf(module);
    }

    // Helpers for settings
    public int parentX() { return parent.getX(); }
    public int parentY() { return parent.getY(); }
    public int parentWidth() { return parent.getWidth(); }
    public int parentHeight() { return parent.getHeight(); }
}
