package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.AnimatedValue;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.setting.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
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
        long now = System.currentTimeMillis();
        updateScrollPhysics();
        updateMouseState(mouseX, mouseY);

        width = panelWidth;

        // Background
        ctx.fill((int) x, (int) y, (int) (x + width), (int) (y + height), 0xE0111111);

        // Title bar
        int catColor = category.color | 0xFF000000;
        ctx.fill((int) x, (int) y, (int) (x + width), (int) (y + titleHeight), catColor);

        // Title text
        String title = "§l" + category.getTitle().getString();
        ctx.drawTextWithShadow(mc.textRenderer, title, x + 4, y + 4, 0xFFFFFFFF);

        // Module count
        String count = String.valueOf(modules.size());
        ctx.drawTextWithShadow(mc.textRenderer, count, x + width - mc.textRenderer.getWidth(count) - 4, y + 4, 0xAAFFFFFF);

        // Module list (clipped area)
        float contentY = y + titleHeight + 1;
        float contentBottom = y + height;

        for (int i = 0; i < modules.size(); i++) {
            ModuleEntry entry = modules.get(i);
            float modY = contentY + i * (moduleHeight + moduleSpacing) - scrollProgress;

            // Skip if outside visible area
            if (modY + moduleHeight < contentY) continue;
            if (modY > contentBottom) break;

            entry.render(ctx, x + 2, modY, width - 4, moduleHeight, mouseX, mouseY);
        }

        // Scrollbar
        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight) {
            float viewRatio = (height - titleHeight) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = (int) (x + width - 3);
            int sbH = Math.max(10, (int) ((height - titleHeight) * viewRatio));
            int sbY = (int) (y + titleHeight + scrollRatio * (height - titleHeight - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, 0x60FFFFFF);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;

        // Title bar = drag
        if (mouseY >= y && mouseY <= y + titleHeight) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return;
        }

        // Module click
        float contentY = y + titleHeight + 1;
        for (int i = 0; i < modules.size(); i++) {
            ModuleEntry entry = modules.get(i);
            float modY = contentY + i * (moduleHeight + moduleSpacing) - scrollProgress;
            if (mouseY >= modY && mouseY <= modY + moduleHeight && mouseX >= x + 2 && mouseX <= x + width - 2) {
                if (button == 0) {
                    entry.module.toggle();
                    entry.flashTime = System.currentTimeMillis();
                } else if (button == 1) {
                    // Open settings panel
                    if (!entry.module.getSettings().isEmpty()) {
                        SettingPanel.open(entry.module, (float) mouseX, (float) mouseY);
                    }
                } else if (button == 2) {
                    // Bind
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    private static class ModuleEntry {
        final Module module;
        long flashTime = 0;

        ModuleEntry(Module module) {
            this.module = module;
        }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean enabled = module.isEnabled();

            // Background
            int bgColor = enabled ? 0xFF333333 : 0xFF1A1A1A;
            ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), bgColor);

            // Flash animation on toggle
            long now = System.currentTimeMillis();
            long elapsed = now - flashTime;
            if (elapsed < 300) {
                float flash = 1f - elapsed / 300f;
                int flashAlpha = (int) (0x40 * flash);
                ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), 0xFF00FF00 | (flashAlpha << 24));
            }

            // Accent bar
            int accentColor = enabled ? (module.getCategory().color | 0xFF000000) : 0xFF333333;
            ctx.fill((int) x, (int) y, (int) (x + 2), (int) (y + h), accentColor);

            // Hover overlay
            if (hovered) {
                ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), 0x20FFFFFF);
            }

            // Module name
            String name = module.getTitle().getString();
            int textColor = enabled ? 0xFFFFFF : 0xAAAAAA;
            ctx.drawTextWithShadow(mc.textRenderer, name, x + 5, y + 3, textColor);

            // Keybind
            if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                String keyName = GLFW.glfwGetKeyName(module.getKey(), 0);
                if (keyName == null) keyName = "K" + module.getKey();
                ctx.drawTextWithShadow(mc.textRenderer, keyName,
                    x + w - mc.textRenderer.getWidth(keyName) - 3, y + 3, 0x666666);
            }
        }
    }
}
