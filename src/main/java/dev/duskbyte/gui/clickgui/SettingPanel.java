
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.Theme;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/**
 * Meteor-style setting panel with blue glass theme.
 */
public class SettingPanel extends GuiPanel {
    private static SettingPanel instance;
    private final Module module;
    private final List<SettingRow> rows = new ArrayList<>();
    private final float titleHeight = 18f;
    private final float rowHeight = 15f;

    public SettingPanel(Module module, float x, float y) {
        super(x, y, 140, 220);
        this.module = module;
        for (Setting<?> s : module.getSettings()) rows.add(new SettingRow(s));
    }

    public static void open(Module module, float x, float y) { instance = new SettingPanel(module, x, y); }
    public static void openBind(Module module, float x, float y) { instance = new SettingPanel(module, x, y); }
    public static void close() { instance = null; }
    public static SettingPanel get() { return instance; }

    @Override public float getTotalContentHeight() { return rows.size() * (rowHeight + 1); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateScrollPhysics();
        updateMouseState(mouseX, mouseY);

        // Glass background
        ctx.fill(i(x), i(y), i(x + width), i(y + height), Theme.BG_PRIMARY);
        ctx.fill(i(x), i(y), i(x + width), i(y + 1), Theme.ACCENT);

        // Title
        ctx.fill(i(x), i(y + 1), i(x + width), i(y + titleHeight), Theme.BG_SECONDARY);
        ctx.drawTextWithShadow(mc.textRenderer, "§l" + module.getTitle().getString(),
            i(x + 9), i(y + 5), Theme.TEXT_PRIMARY);

        // Close hint
        ctx.fill(i(x + width - 26), i(y + 4), i(x + width - 4), i(y + 13), Theme.BG_MODULE);
        ctx.drawTextWithShadow(mc.textRenderer, "ESC", i(x + width - 24), i(y + 5), Theme.TEXT_DIM);

        // Rows
        float contentY = y + titleHeight + 2;
        float contentBottom = y + height - 2;

        for (int idx = 0; idx < rows.size(); idx++) {
            SettingRow row = rows.get(idx);
            float rowY = contentY + idx * (rowHeight + 1) - scrollProgress;
            if (rowY + rowHeight < contentY - rowHeight) continue;
            if (rowY > contentBottom) break;
            row.render(ctx, x + 3, rowY, width - 6, rowHeight, mouseX, mouseY);
        }

        // Scrollbar
        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight - 6) {
            float viewRatio = (height - titleHeight - 6) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = i(x + width - 3);
            int sbH = Math.max(10, i((height - titleHeight - 6) * viewRatio));
            int sbY = i(y + titleHeight + 4 + scrollRatio * (height - titleHeight - 10 - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, Theme.SCROLLBAR);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) { close(); return; }
        if (mouseY >= y && mouseY <= y + titleHeight) {
            if (button == 0) { dragging = true; dragOffsetX = mouseX - x; dragOffsetY = mouseY - y; }
            return;
        }
        float contentY = y + titleHeight + 2;
        for (int idx = 0; idx < rows.size(); idx++) {
            SettingRow row = rows.get(idx);
            float rowY = contentY + idx * (rowHeight + 1) - scrollProgress;
            if (mouseY >= rowY && mouseY <= rowY + rowHeight && mouseX >= x + 3 && mouseX <= x + width - 3) {
                row.mouseClicked(button);
                return;
            }
        }
    }

    @Override public void mouseReleased(double mx, double my, int btn) { dragging = false; for (SettingRow r : rows) r.mouseReleased(); }

    @Override
    public void mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (dragging) { x = (float)(mx - dragOffsetX); y = (float)(my - dragOffsetY); }
        for (SettingRow r : rows) r.mouseDragged(mx);
    }

    @Override
    public boolean keyPressed(int keyCode, int sc, int mod) {
        for (SettingRow r : rows) if (r.keyPressed(keyCode)) return true;
        return false;
    }

    @Override
    public boolean charTyped(char chr, int mod) {
        for (SettingRow r : rows) if (r.charTyped(chr)) return true;
        return false;
    }

    private static int i(float v) { return (int) v; }

    private class SettingRow {
        final Setting<?> setting;
        boolean draggingSlider = false;
        boolean editingString = false;
        String tempText = "";
        int cursorPos = 0;
        long lastBlink = 0;
        boolean cursorVisible = true;

        SettingRow(Setting<?> s) { this.setting = s; }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            int bg = hovered ? 0xFF1A2332 : Theme.BG_MODULE;
            if (editingString) bg = 0xFF0D2847;
            ctx.fill(i(x), i(y), i(x + w), i(y + h), bg);

            String name = setting.getName();

            if (setting instanceof BoolSetting bs) {
                // Slider track
                ctx.fill(i(x + 1), i(y + 3), i(x + w - 1), i(y + h - 3), Theme.BG_SECONDARY);
                // Slider fill
                float prog = bs.get() ? 1f : 0f;
                if (prog > 0) ctx.fill(i(x + 1), i(y + 3), i(x + 1 + (w - 2) * prog), i(y + h - 3), Theme.ACCENT);
                // Label
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), Theme.TEXT_SECONDARY);
                // Value
                String val = bs.get() ? "ON" : "OFF";
                ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3),
                    bs.get() ? Theme.ON : Theme.OFF);

            } else if (setting instanceof NumberSetting ns) {
                // Slider track
                ctx.fill(i(x + 1), i(y + 3), i(x + w - 1), i(y + h - 3), Theme.BG_SECONDARY);
                // Slider fill
                double range = ns.getMax() - ns.getMin();
                if (range > 0) {
                    float prog = (float)((ns.get() - ns.getMin()) / range);
                    ctx.fill(i(x + 1), i(y + 3), i(x + 1 + (w - 2) * prog), i(y + h - 3), Theme.ACCENT);
                }
                // Label
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), Theme.TEXT_SECONDARY);
                // Value
                String val = String.format("%.1f", ns.get());
                ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3), Theme.VALUE);

            } else if (setting instanceof ModeSetting ms) {
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), Theme.TEXT_SECONDARY);
                // Value with bg pill
                String val = ms.get();
                int vw = mc.textRenderer.getWidth(val) + 6;
                int vx = i(x + w - vw - 4);
                ctx.fill(vx, i(y + 2), vx + vw, i(y + h - 2), Theme.ACCENT_DIM);
                ctx.drawTextWithShadow(mc.textRenderer, val, vx + 3, i(y + 3), Theme.TEXT_PRIMARY);

            } else if (setting instanceof ColorSetting cs) {
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), Theme.TEXT_SECONDARY);
                // Color preview with border
                ctx.fill(i(x + w - 16), i(y + 2), i(x + w - 2), i(y + h - 2), 0xFF000000);
                ctx.fill(i(x + w - 15), i(y + 3), i(x + w - 3), i(y + h - 3), 0xFF000000 | cs.get());

            } else if (setting instanceof StringSetting ss) {
                ctx.drawTextWithShadow(mc.textRenderer, name + ":", i(x + 5), i(y + 3), Theme.TEXT_SECONDARY);
                if (editingString) {
                    long now = System.currentTimeMillis();
                    if (now - lastBlink > 530) { cursorVisible = !cursorVisible; lastBlink = now; }
                    // Input field background
                    ctx.fill(i(x + 5), i(y + h - 4), i(x + w - 5), i(y + h - 3), Theme.ACCENT);
                    String display = tempText + (cursorVisible ? "|" : "");
                    ctx.drawTextWithShadow(mc.textRenderer, display, i(x + 5), i(y + 3), Theme.TEXT_ACCENT);
                } else {
                    String val = ss.get().isEmpty() ? "..." : ss.get();
                    ctx.drawTextWithShadow(mc.textRenderer, val,
                        i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3), Theme.STRING);
                }
            }
        }

        void mouseClicked(int button) {
            if (setting instanceof BoolSetting bs) { if (button == 0) bs.toggle(); }
            else if (setting instanceof NumberSetting ns) { if (button == 0) draggingSlider = true; }
            else if (setting instanceof ModeSetting ms) { if (button == 0) ms.cycle(); }
            else if (setting instanceof StringSetting ss) {
                if (button == 0) {
                    editingString = !editingString;
                    if (editingString) { tempText = ss.get(); cursorPos = tempText.length(); lastBlink = System.currentTimeMillis(); cursorVisible = true; }
                }
            }
        }

        void mouseReleased() { draggingSlider = false; }

        void mouseDragged(double mouseX) {
            if (draggingSlider && setting instanceof NumberSetting ns) {
                double range = ns.getMax() - ns.getMin();
                double value = Math.max(ns.getMin(), Math.min(ns.getMax(), ns.getMin() + (mouseX - x) / width * range));
                double step = ns.getStep();
                value = Math.round(value / step) * step;
                ns.set(value);
            }
        }

        boolean keyPressed(int kc) {
            if (!editingString) return false;
            if (kc == GLFW.GLFW_KEY_ENTER || kc == GLFW.GLFW_KEY_KP_ENTER) {
                editingString = false;
                if (!tempText.isEmpty() && setting instanceof StringSetting ss) ss.set(tempText);
                return true;
            }
            if (kc == GLFW.GLFW_KEY_ESCAPE) { editingString = false; return true; }
            if (kc == GLFW.GLFW_KEY_BACKSPACE && cursorPos > 0) {
                tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
                cursorPos--; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true;
            }
            if (kc == GLFW.GLFW_KEY_DELETE && cursorPos < tempText.length()) {
                tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
                lastBlink = System.currentTimeMillis(); cursorVisible = true; return true;
            }
            if (kc == GLFW.GLFW_KEY_LEFT && cursorPos > 0) { cursorPos--; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (kc == GLFW.GLFW_KEY_RIGHT && cursorPos < tempText.length()) { cursorPos++; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (kc == GLFW.GLFW_KEY_HOME) { cursorPos = 0; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (kc == GLFW.GLFW_KEY_END) { cursorPos = tempText.length(); lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            return false;
        }

        boolean charTyped(char c) {
            if (!editingString || c == '\n' || c == '\r' || c == '\t') return false;
            tempText = tempText.substring(0, cursorPos) + c + tempText.substring(cursorPos);
            cursorPos++; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true;
        }
    }
}
