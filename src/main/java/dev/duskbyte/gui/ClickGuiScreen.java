
package dev.duskbyte.gui;

import dev.duskbyte.gui.clickgui.ModulePanel;
import dev.duskbyte.gui.clickgui.SettingPanel;
import dev.duskbyte.gui.util.Theme;
import dev.duskbyte.module.Category;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/**
 * Meteor-style ClickGUI with blue glass panels.
 */
public class ClickGuiScreen extends Screen {
    private final List<ModulePanel> panels = new ArrayList<>();
    private boolean initialized = false;

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            panels.clear();
            float posX = 8;
            float posY = 8;
            float spacing = 4;
            for (Category cat : Category.values()) {
                panels.add(new ModulePanel(cat, posX, posY));
                posX += panels.get(panels.size() - 1).getWidth() + spacing;
            }
            initialized = true;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        // Render panels
        for (ModulePanel panel : panels) panel.render(ctx, mouseX, mouseY, delta);

        // Setting panel on top
        SettingPanel sp = SettingPanel.get();
        if (sp != null) sp.render(ctx, mouseX, mouseY, delta);

        // Bottom info bar
        ctx.fill(0, this.height - 16, this.width, this.height, Theme.BG_PRIMARY);
        ctx.fill(0, this.height - 16, this.width, this.height - 15, Theme.ACCENT);
        ctx.drawTextWithShadow(textRenderer,
            "§l§bDuskByte §r§7v" + dev.duskbyte.DuskByteClient.VERSION
            + "  §8│  §7LClick=Toggle  RClick=Settings  MClick=Bind  Scroll=Navigate",
            8, this.height - 12, Theme.TEXT_PRIMARY);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) { sp.mouseClicked(mx, my, button); return true; }

        for (int i = panels.size() - 1; i >= 0; i--) {
            ModulePanel p = panels.get(i);
            if (p.isHovered(mx, my)) {
                panels.remove(i); panels.add(p);
                p.mouseClicked(mx, my, button);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseReleased(double mx, double my, int btn) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) sp.mouseReleased(mx, my, btn);
        for (ModulePanel p : panels) p.mouseReleased(mx, my, btn);
        return super.mouseReleased(mx, my, btn);
    }

    @Override public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) sp.mouseDragged(mx, my, btn, dx, dy);
        for (ModulePanel p : panels) p.mouseDragged(mx, my, btn, dx, dy);
        return true;
    }

    @Override public boolean mouseScrolled(double mx, double my, double horiz, double vert) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null && sp.isHovered(mx, my)) { sp.handleScroll(vert); return true; }
        for (ModulePanel p : panels) {
            if (p.isHovered(mx, my)) { p.handleScroll(vert); return true; }
        }
        return super.mouseScrolled(mx, my, horiz, vert);
    }

    @Override
    public boolean keyPressed(int keyCode, int sc, int mod) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) {
            if (sp.keyPressed(keyCode, sc, mod)) return true;
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { SettingPanel.close(); return true; }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(keyCode, sc, mod);
    }

    @Override
    public boolean charTyped(char chr, int mod) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null && sp.charTyped(chr, mod)) return true;
        return super.charTyped(chr, mod);
    }

    @Override public boolean shouldPause() { return false; }
}
