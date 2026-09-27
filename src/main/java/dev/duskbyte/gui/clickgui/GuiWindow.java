
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.*;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Argon-style category window with draggable header and module buttons.
 */
public final class GuiWindow {
    public List<ModuleButton> moduleButtons = new ArrayList<>();
    public int x, y;
    private final int width, height;
    public Color currentColor;
    private final Category category;
    public boolean dragging, extended;
    private int dragX, dragY;
    public int prevX, prevY;
    public ClickGuiScreen parent;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public GuiWindow(int x, int y, int width, int height, Category category, ClickGuiScreen parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        this.parent = parent;
        this.prevX = x;
        this.prevY = y;
        this.extended = true;

        int offset = height;
        List<Module> modules = new ArrayList<>(ModuleManager.getByCategory(category));
        for (Module module : modules) {
            moduleButtons.add(new ModuleButton(this, module, offset));
            offset += height;
        }
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Smooth alpha transition
        if (currentColor == null)
            currentColor = new Color(0, 0, 0, 0);
        else
            currentColor = new Color(0, 0, 0, currentColor.getAlpha());

        int targetAlpha = 200;
        if (currentColor.getAlpha() != targetAlpha)
            currentColor = ColorUtils.smoothAlphaTransition(0.05f, targetAlpha, currentColor);

        // Window background with rounded corners
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), currentColor, prevX, prevY, prevX + width, prevY + height, 6, 50);

        // Rainbow accent bar at bottom
        Color accent = ColorUtils.getMainColor(255, 0);
        ctx.fill(prevX, prevY + (height - 2), prevX + width, prevY + height, accent.getRGB());

        // Category name centered
        String name = category.getTitle().getString();
        int textWidth = mc.textRenderer.getWidth(name);
        int centerX = prevX + (width / 2) - textWidth / 2;
        ctx.drawTextWithShadow(mc.textRenderer, name, centerX, prevY + 6, Color.WHITE.getRGB());

        // Update module button positions
        updateButtons(delta);

        // Render module buttons
        for (ModuleButton btn : moduleButtons)
            btn.render(ctx, mouseX, mouseY, delta);
    }

    public void updateButtons(float delta) {
        int offset = height;
        for (ModuleButton btn : moduleButtons) {
            btn.animation.animate(0.5 * delta, btn.extended ? height * (btn.settings.size() + 1) : height);
            btn.offset = offset;
            offset += (int) btn.animation.getValue();
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && mouseY < y + height) {
            if (button == 0 && !parent.isDraggingAlready()) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            }
        }
        if (extended) {
            for (ModuleButton btn : moduleButtons)
                btn.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        for (ModuleButton btn : moduleButtons)
            btn.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (extended) {
            for (ModuleButton btn : moduleButtons)
                btn.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        prevY = y;
        y = (int) (y + amount * 20);
        prevY = y;
    }

    public void keyPressed(int keyCode, int sc, int mod) {
        for (ModuleButton btn : moduleButtons)
            btn.keyPressed(keyCode, sc, mod);
    }

    public void onGuiClose() {
        currentColor = null;
        dragging = false;
        for (ModuleButton btn : moduleButtons)
            btn.onGuiClose();
    }

    public void updatePosition(double mouseX, double mouseY, float delta) {
        prevX = x;
        prevY = y;
        if (dragging) {
            x = (int) MathUtils.goodLerp(0.3f * delta, isHovered(mouseX, mouseY) ? x : prevX, mouseX - dragX);
            y = (int) MathUtils.goodLerp(0.3f * delta, isHovered(mouseX, mouseY) ? y : prevY, mouseY - dragY);
        }
    }

    public boolean isHovered(double mx, double my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean isDraggingAlready() {
        for (GuiWindow w : parent.windows)
            if (w.dragging) return true;
        return false;
    }

    public int getX() { return prevX; }
    public int getY() { return prevY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Category getCategory() { return category; }
}
