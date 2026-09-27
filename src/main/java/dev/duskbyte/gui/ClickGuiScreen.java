
package dev.duskbyte.gui;

import dev.duskbyte.gui.clickgui.GuiWindow;
import dev.duskbyte.gui.util.*;
import dev.duskbyte.module.Category;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Argon-style ClickGUI with draggable windows and inline settings.
 */
public class ClickGuiScreen extends Screen {
    public List<GuiWindow> windows = new ArrayList<>();
    private Color bgColor;
    private boolean initialized = false;

    public ClickGuiScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            windows.clear();
            int offsetX = 50;
            for (Category cat : Category.values()) {
                windows.add(new GuiWindow(offsetX, 50, 110, 16, cat, this));
                offsetX += 130;
            }
            initialized = true;
        }
    }

    public boolean isDraggingAlready() {
        for (GuiWindow w : windows)
            if (w.dragging) return true;
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (client == null) return;

        // Smooth background fade
        if (bgColor == null)
            bgColor = new Color(0, 0, 0, 0);
        else
            bgColor = new Color(0, 0, 0, bgColor.getAlpha());

        int targetAlpha = 160;
        if (bgColor.getAlpha() != targetAlpha)
            bgColor = ColorUtils.smoothAlphaTransition(0.05f, targetAlpha, bgColor);

        ctx.fill(0, 0, this.width, this.height, bgColor.getRGB());

        // Render windows (back to front)
        for (GuiWindow window : windows) {
            window.render(ctx, mouseX, mouseY, delta);
            window.updatePosition(mouseX, mouseY, delta);
        }

        // Bottom info bar
        ctx.fill(0, this.height - 14, this.width, this.height, 0xE00D1117);
        ctx.fill(0, this.height - 14, this.width, this.height - 13, 0xFF3B82F6);
        ctx.drawTextWithShadow(textRenderer,
            "§l§bDuskByte §r§7v" + dev.duskbyte.DuskByteClient.VERSION
            + "  §8|  §7LClick=Toggle  RClick=Expand  Scroll=Move",
            8, this.height - 10, Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            GuiWindow w = windows.get(i);
            if (w.isHovered(mx, my) || (w.extended && isInsideAnyModule(mx, my, w))) {
                // Bring to front
                windows.remove(i);
                windows.add(w);
                w.mouseClicked(mx, my, button);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean isInsideAnyModule(double mx, double my, GuiWindow w) {
        return mx >= w.getX() && mx <= w.getX() + w.getWidth()
            && my >= w.getY() && my <= w.getY() + w.getHeight() + 500; // generous range
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        for (GuiWindow w : windows) w.mouseReleased(mx, my, button);
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (GuiWindow w : windows) w.mouseDragged(mx, my, button, dx, dy);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horiz, double vert) {
        for (GuiWindow w : windows) {
            if (w.isHovered(mx, my)) {
                w.mouseScrolled(mx, my, vert);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, horiz, vert);
    }

    @Override
    public boolean keyPressed(int keyCode, int sc, int mod) {
        for (GuiWindow w : windows) w.keyPressed(keyCode, sc, mod);
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, sc, mod);
    }

    @Override
    public void close() {
        for (GuiWindow w : windows) w.onGuiClose();
        bgColor = null;
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
