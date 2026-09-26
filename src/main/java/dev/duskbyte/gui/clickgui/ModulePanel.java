
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/**
 * Category panel showing modules in a scrollable, draggable window.
 */
public class ModulePanel extends GuiPanel {
    private final Category category;
    private final List<ModuleEntry> modules = new ArrayList<>();
    private final float titleHeight = 16f;
    private final float moduleHeight = 14f;
    private final float moduleSpacing = 1f;
    private float panelWidth;

    public ModulePanel(Category category, float x, float y) {
        super(x, y, 110, 300);
        this.category = category;
        this.panelWidth = calculateWidth();

        for (Module m : ModuleManager.getByCategory(category)) {
            modules.add(new ModuleEntry(m));
        }
    }

    private float calculateWidth() {
        float maxW = 80;
        for (ModuleEntry entry : modules) {
            float w = mc.textRenderer.getWidth(entry.module.getTitle().getString()) + 20;
            if (w > maxW) maxW = w;
        }
        return Math.min(maxW, 160);
    }

    @Override
    public float getTotalContentHeight() {
        return modules.size() * (moduleHeight + moduleSpacing);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateScrollPhysics();
        updateMouseState(mouseX, mouseY);
        width = panelWidth;

        // Background
        ctx.fill(i(x), i(y), i(x + width), i(y + height), 0xE0111111);

        // Title bar
        int catColor = category.color | 0xFF000000;
        ctx.fill(i(x), i(y), i(x + width), i(y + titleHeight), catColor);

        // Title text
        String title = "§l" + category.getTitle().getString();
        ctx.drawTextWithShadow(mc.textRenderer, title, i(x + 4), i(y + 4), 0xFFFFFFFF);

        // Module count
        String count = String.valueOf(modules.size());
        ctx.drawTextWithShadow(mc.textRenderer, count, i(x + width - mc.textRenderer.getWidth(count) - 4), i(y + 4), 0xAAFFFFFF);

        // Module list
        float contentY = y + titleHeight + 1;
        float contentBottom = y + height;

        for (int idx = 0; idx < modules.size(); idx++) {
            ModuleEntry entry = modules.get(idx);
            float modY = contentY + idx * (moduleHeight + moduleSpacing) - scrollProgress;

            if (modY + moduleHeight < contentY) continue;
            if (modY > contentBottom) break;

            entry.render(ctx, x + 2, modY, width - 4, moduleHeight, mouseX, mouseY);
        }

        // Scrollbar
        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight) {
            float viewRatio = (height - titleHeight) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = i(x + width - 3);
            int sbH = Math.max(10, i((height - titleHeight) * viewRatio));
            int sbY = i(y + titleHeight + scrollRatio * (height - titleHeight - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, 0x60FFFFFF);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;

        if (mouseY >= y && mouseY <= y + titleHeight) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return;
        }

        float contentY = y + titleHeight + 1;
        for (int idx = 0; idx < modules.size(); idx++) {
            ModuleEntry entry = modules.get(idx);
            float modY = contentY + idx * (moduleHeight + moduleSpacing) - scrollProgress;
            if (mouseY >= modY && mouseY <= modY + moduleHeight && mouseX >= x + 2 && mouseX <= x + width - 2) {
                if (button == 0) {
                    entry.module.toggle();
                    entry.flashTime = System.currentTimeMillis();
                } else if (button == 1) {
                    if (!entry.module.getSettings().isEmpty()) {
                        SettingPanel.open(entry.module, (float) mouseX, (float) mouseY);
                    }
                } else if (button == 2) {
                    SettingPanel.openBind(entry.module, (float) mouseX, (float) mouseY);
                }
                return;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            x = (float) (mouseX - dragOffsetX);
            y = (float) (mouseY - dragOffsetY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    @Override
    public boolean charTyped(char chr, int modifiers) { return false; }

    /** float to int helper */
    private static int i(float v) { return (int) v; }

    private static class ModuleEntry {
        final Module module;
        long flashTime = 0;

        ModuleEntry(Module module) { this.module = module; }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean enabled = module.isEnabled();

            int bgColor = enabled ? 0xFF333333 : 0xFF1A1A1A;
            ctx.fill(i(x), i(y), i(x + w), i(y + h), bgColor);

            long now = System.currentTimeMillis();
            long elapsed = now - flashTime;
            if (elapsed < 300) {
                float flash = 1f - elapsed / 300f;
                int flashAlpha = (int) (0x40 * flash);
                ctx.fill(i(x), i(y), i(x + w), i(y + h), 0xFF00FF00 | (flashAlpha << 24));
            }

            int accentColor = enabled ? (module.getCategory().color | 0xFF000000) : 0xFF333333;
            ctx.fill(i(x), i(y), i(x + 2), i(y + h), accentColor);

            if (hovered) {
                ctx.fill(i(x), i(y), i(x + w), i(y + h), 0x20FFFFFF);
            }

            String name = module.getTitle().getString();
            int textColor = enabled ? 0xFFFFFF : 0xAAAAAA;
            ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), textColor);

            if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                String keyName = GLFW.glfwGetKeyName(module.getKey(), 0);
                if (keyName == null) keyName = "K" + module.getKey();
                ctx.drawTextWithShadow(mc.textRenderer, keyName,
                    i(x + w - mc.textRenderer.getWidth(keyName) - 3), i(y + 3), 0x666666);
            }
        }
    }
}
