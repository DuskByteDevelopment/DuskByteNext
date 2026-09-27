
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.Theme;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/**
 * Meteor-style category panel with blue glass theme.
 */
public class ModulePanel extends GuiPanel {
    private final Category category;
    private final List<ModuleEntry> modules = new ArrayList<>();
    private final float titleHeight = 18f;
    private final float moduleHeight = 15f;
    private final float moduleSpacing = 1f;
    private float panelWidth;

    public ModulePanel(Category category, float x, float y) {
        super(x, y, 110, 320);
        this.category = category;
        this.panelWidth = calculateWidth();
        for (Module m : ModuleManager.getByCategory(category)) {
            modules.add(new ModuleEntry(m));
        }
    }

    private float calculateWidth() {
        float maxW = 85;
        for (ModuleEntry e : modules) {
            float w = mc.textRenderer.getWidth(e.module.getTitle().getString()) + 22;
            if (w > maxW) maxW = w;
        }
        return Math.min(maxW, 170);
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

        // Panel background - glass effect
        ctx.fill(i(x), i(y), i(x + width), i(y + height), Theme.BG_PRIMARY);
        // Top accent line
        ctx.fill(i(x), i(y), i(x + width), i(y + 1), Theme.ACCENT);

        // Title bar
        ctx.fill(i(x), i(y + 1), i(x + width), i(y + titleHeight), Theme.BG_SECONDARY);

        // Category name with colored icon
        int catColor = getCategoryColor();
        ctx.fill(i(x + 3), i(y + 5), i(x + 5), i(y + titleHeight - 4), catColor);
        ctx.drawTextWithShadow(mc.textRenderer,
            "§l" + category.getTitle().getString(),
            i(x + 9), i(y + 5), Theme.TEXT_PRIMARY);

        // Module count badge
        String count = String.valueOf(modules.size());
        int badgeW = mc.textRenderer.getWidth(count) + 6;
        int badgeX = i(x + width - badgeW - 4);
        ctx.fill(badgeX, i(y + 4), badgeX + badgeW, i(y + 13), Theme.ACCENT_DIM);
        ctx.drawTextWithShadow(mc.textRenderer, count, badgeX + 3, i(y + 5), Theme.TEXT_PRIMARY);

        // Module list
        float contentY = y + titleHeight + 2;
        float contentBottom = y + height - 2;

        for (int idx = 0; idx < modules.size(); idx++) {
            ModuleEntry entry = modules.get(idx);
            float modY = contentY + idx * (moduleHeight + moduleSpacing) - scrollProgress;
            if (modY + moduleHeight < contentY - moduleHeight) continue;
            if (modY > contentBottom) break;
            entry.render(ctx, x + 3, modY, width - 6, moduleHeight, mouseX, mouseY, catColor);
        }

        // Scrollbar
        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight - 4) {
            float viewRatio = (height - titleHeight - 4) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = i(x + width - 3);
            int sbH = Math.max(12, i((height - titleHeight - 4) * viewRatio));
            int sbY = i(y + titleHeight + 4 + scrollRatio * (height - titleHeight - 8 - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, Theme.SCROLLBAR);
        }
    }

    private int getCategoryColor() {
        return switch (category) {
            case COMBAT -> Theme.CAT_COMBAT;
            case MOVEMENT -> Theme.CAT_MOVEMENT;
            case RENDER -> Theme.CAT_RENDER;
            case PLAYER -> Theme.CAT_PLAYER;
            case MISC -> Theme.CAT_MISC;
        };
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
        float contentY = y + titleHeight + 2;
        for (int idx = 0; idx < modules.size(); idx++) {
            ModuleEntry entry = modules.get(idx);
            float modY = contentY + idx * (moduleHeight + moduleSpacing) - scrollProgress;
            if (mouseY >= modY && mouseY <= modY + moduleHeight && mouseX >= x + 3 && mouseX <= x + width - 3) {
                if (button == 0) { entry.module.toggle(); entry.flashTime = System.currentTimeMillis(); }
                else if (button == 1 && !entry.module.getSettings().isEmpty())
                    SettingPanel.open(entry.module, (float) mouseX, (float) mouseY);
                else if (button == 2)
                    SettingPanel.openBind(entry.module, (float) mouseX, (float) mouseY);
                return;
            }
        }
    }

    @Override public void mouseReleased(double mouseX, double mouseY, int button) { dragging = false; }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) { x = (float)(mouseX - dragOffsetX); y = (float)(mouseY - dragOffsetY); }
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }

    private static int i(float v) { return (int) v; }

    private static class ModuleEntry {
        final Module module;
        long flashTime = 0;

        ModuleEntry(Module module) { this.module = module; }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY, int catColor) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean enabled = module.isEnabled();

            // Background
            int bg = enabled ? Theme.BG_MODULE_ENABLED : Theme.BG_MODULE;
            ctx.fill(i(x), i(y), i(x + w), i(y + h), bg);

            // Enabled glow
            if (enabled) {
                ctx.fill(i(x), i(y), i(x + w), i(y + h), Theme.ENABLED_GLOW);
            }

            // Flash on toggle
            long elapsed = System.currentTimeMillis() - flashTime;
            if (elapsed < 400) {
                float flash = 1f - elapsed / 400f;
                ctx.fill(i(x), i(y), i(x + w), i(y + h), Theme.withAlpha(Theme.ACCENT, (int)(0x50 * flash)));
            }

            // Left accent bar
            int barColor = enabled ? Theme.ACCENT : Theme.withAlpha(catColor, 0x40);
            ctx.fill(i(x), i(y), i(x + 2), i(y + h), barColor);

            // Hover overlay
            if (hovered && !enabled) {
                ctx.fill(i(x), i(y), i(x + w), i(y + h), Theme.HOVER);
            }

            // Module name
            int textColor = enabled ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY;
            ctx.drawTextWithShadow(mc.textRenderer, module.getTitle().getString(),
                i(x + 6), i(y + 3), textColor);

            // Keybind
            if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                String kn = GLFW.glfwGetKeyName(module.getKey(), 0);
                if (kn == null) kn = "K" + module.getKey();
                ctx.fill(i(x + w - mc.textRenderer.getWidth(kn) - 6), i(y + 2),
                    i(x + w - 3), i(y + h - 2), Theme.BG_SECONDARY);
                ctx.drawTextWithShadow(mc.textRenderer, kn,
                    i(x + w - mc.textRenderer.getWidth(kn) - 5), i(y + 3), Theme.TEXT_DIM);
            }

            // Enabled indicator dot
            if (enabled) {
                ctx.fill(i(x + w - 7), i(y + h - 4), i(x + w - 5), i(y + h - 2), Theme.ACCENT);
            }
        }
    }
}
