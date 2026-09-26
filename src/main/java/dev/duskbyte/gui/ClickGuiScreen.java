
package dev.duskbyte.gui;

import dev.duskbyte.gui.clickgui.ModulePanel;
import dev.duskbyte.gui.clickgui.SettingPanel;
import dev.duskbyte.gui.clickgui.GuiPanel;
import dev.duskbyte.module.Category;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/**
 * ClickGUI with panel-based architecture, draggable windows, smooth scrolling.
 * Reference: NekoFlanHelper ClickGUIScreen.
 */
public class ClickGuiScreen extends Screen {
    private final List<ModulePanel> panels = new ArrayList<>();
    private boolean initialized = false;

    public ClickGuiScreen() {
        super(Text.literal("DuskByte ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            panels.clear();
            float posX = 6;
            float posY = 6;
            float spacing = 3;

            for (Category cat : Category.values()) {
                ModulePanel panel = new ModulePanel(cat, posX, posY);
                panels.add(panel);
                posX += panel.width + spacing;
            }
            initialized = true;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        // Render panels (back to front)
        for (ModulePanel panel : panels) {
            panel.render(ctx, mouseX, mouseY, delta);
        }

        // Render setting panel on top
        SettingPanel sp = SettingPanel.get();
        if (sp != null) {
            sp.render(ctx, mouseX, mouseY, delta);
        }

        // Bottom bar
        ctx.fill(0, this.height - 14, this.width, this.height, 0xFF111111);
        ctx.drawTextWithShadow(textRenderer, "§b§lDuskByte §7v" + dev.duskbyte.DuskByteClient.VERSION
            + " §8| §7Right=Settings Middle=Bind", 6, this.height - 10, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Setting panel gets priority
        SettingPanel sp = SettingPanel.get();
        if (sp != null) {
            sp.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        // Panels (front to back for z-order)
        for (int i = panels.size() - 1; i >= 0; i--) {
            ModulePanel panel = panels.get(i);
            if (panel.isHovered(mouseX, mouseY)) {
                // Bring to front
                panels.remove(i);
                panels.add(panel);
                panel.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) sp.mouseReleased(mouseX, mouseY, button);
        for (ModulePanel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) sp.mouseDragged(mouseX, mouseY, button, dx, dy);
        for (ModulePanel panel : panels) panel.mouseDragged(mouseX, mouseY, button, dx, dy);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null && sp.isHovered(mouseX, mouseY)) {
            sp.handleScroll(verticalAmount);
            return true;
        }
        for (ModulePanel panel : panels) {
            if (panel.isHovered(mouseX, mouseY)) {
                panel.handleScroll(verticalAmount);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null) {
            if (sp.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                SettingPanel.close();
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        SettingPanel sp = SettingPanel.get();
        if (sp != null && sp.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
