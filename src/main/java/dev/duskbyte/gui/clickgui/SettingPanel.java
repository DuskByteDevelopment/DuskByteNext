
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup panel for editing module settings.
 */
public class SettingPanel extends GuiPanel {
    private static SettingPanel instance;

    private final Module module;
    private final List<SettingRow> rows = new ArrayList<>();
    private final float titleHeight = 16f;
    private final float rowHeight = 14f;

    public SettingPanel(Module module, float x, float y) {
        super(x, y, 130, 200);
        this.module = module;
        for (Setting<?> s : module.getSettings()) {
            rows.add(new SettingRow(s));
        }
    }

    public static void open(Module module, float x, float y) {
        instance = new SettingPanel(module, x, y);
    }

    public static void openBind(Module module, float x, float y) {
        instance = new SettingPanel(module, x, y);
    }

    public static void close() { instance = null; }
    public static SettingPanel get() { return instance; }

    @Override
    public float getTotalContentHeight() {
        return rows.size() * (rowHeight + 1);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateScrollPhysics();
        updateMouseState(mouseX, mouseY);

        ctx.fill(i(x), i(y), i(x + width), i(y + height), 0xE0111111);
        ctx.fill(i(x), i(y), i(x + width), i(y + titleHeight), 0xFF2A2A3A);
        ctx.drawTextWithShadow(mc.textRenderer, "§l" + module.getTitle().getString(), i(x + 4), i(y + 4), 0xFFFFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, "ESC", i(x + width - 20), i(y + 4), 0x888888);

        float contentY = y + titleHeight + 2;
        float contentBottom = y + height;

        for (int idx = 0; idx < rows.size(); idx++) {
            SettingRow row = rows.get(idx);
            float rowY = contentY + idx * (rowHeight + 1) - scrollProgress;
            if (rowY + rowHeight < contentY) continue;
            if (rowY > contentBottom) break;
            row.render(ctx, x + 2, rowY, width - 4, rowHeight, mouseX, mouseY);
        }

        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight - 4) {
            float viewRatio = (height - titleHeight - 4) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = i(x + width - 3);
            int sbH = Math.max(8, i((height - titleHeight - 4) * viewRatio));
            int sbY = i(y + titleHeight + 2 + scrollRatio * (height - titleHeight - 4 - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, 0x60FFFFFF);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) { close(); return; }

        if (mouseY >= y && mouseY <= y + titleHeight) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return;
        }

        float contentY = y + titleHeight + 2;
        for (int idx = 0; idx < rows.size(); idx++) {
            SettingRow row = rows.get(idx);
            float rowY = contentY + idx * (rowHeight + 1) - scrollProgress;
            if (mouseY >= rowY && mouseY <= rowY + rowHeight && mouseX >= x + 2 && mouseX <= x + width - 2) {
                row.mouseClicked(button);
                return;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        for (SettingRow row : rows) row.mouseReleased();
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            x = (float) (mouseX - dragOffsetX);
            y = (float) (mouseY - dragOffsetY);
        }
        for (SettingRow row : rows) row.mouseDragged(mouseX);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (SettingRow row : rows) {
            if (row.keyPressed(keyCode)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingRow row : rows) {
            if (row.charTyped(chr)) return true;
        }
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

        SettingRow(Setting<?> setting) { this.setting = setting; }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            int bg = hovered ? 0xFF252525 : 0xFF1A1A1A;
            if (editingString) bg = 0xFF2A2A4A;
            ctx.fill(i(x), i(y), i(x + w), i(y + h), bg);

            String name = setting.getName();

            if (setting instanceof BoolSetting bs) {
                if (bs.get()) {
                    ctx.fill(i(x), i(y), i(x + w), i(y + h), 0xFF44AA44);
                }
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), 0xCCCCCC);
                String val = bs.get() ? "ON" : "OFF";
                ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3),
                    bs.get() ? 0x44FF44 : 0xFF4444);

            } else if (setting instanceof NumberSetting ns) {
                double range = ns.getMax() - ns.getMin();
                if (range > 0) {
                    float progress = (float) ((ns.get() - ns.getMin()) / range);
                    ctx.fill(i(x), i(y), i(x + w * progress), i(y + h), 0xFF44AA44);
                }
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), 0xCCCCCC);
                String val = String.format("%.1f", ns.get());
                ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3), 0xFFFF44);

            } else if (setting instanceof ModeSetting ms) {
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), 0xCCCCCC);
                String val = ms.get();
                ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3), 0x44FFFF);

            } else if (setting instanceof ColorSetting cs) {
                ctx.drawTextWithShadow(mc.textRenderer, name, i(x + 5), i(y + 3), 0xCCCCCC);
                ctx.fill(i(x + w - 14), i(y + 2), i(x + w - 2), i(y + 12), 0xFF000000 | cs.get());

            } else if (setting instanceof StringSetting ss) {
                ctx.drawTextWithShadow(mc.textRenderer, name + ":", i(x + 5), i(y + 3), 0xCCCCCC);
                if (editingString) {
                    long now = System.currentTimeMillis();
                    if (now - lastBlink > 530) { cursorVisible = !cursorVisible; lastBlink = now; }
                    String display = tempText + (cursorVisible ? "_" : "");
                    ctx.drawTextWithShadow(mc.textRenderer, display, i(x + 5), i(y + 3), 0xFFFF44);
                } else {
                    String val = ss.get().isEmpty() ? "[empty]" : ss.get();
                    ctx.drawTextWithShadow(mc.textRenderer, val, i(x + w - mc.textRenderer.getWidth(val) - 5), i(y + 3), 0x44FFFF);
                }
            }
        }

        void mouseClicked(int button) {
            if (setting instanceof BoolSetting bs) {
                if (button == 0) bs.toggle();
            } else if (setting instanceof NumberSetting ns) {
                if (button == 0) draggingSlider = true;
            } else if (setting instanceof ModeSetting ms) {
                if (button == 0) ms.cycle();
            } else if (setting instanceof StringSetting ss) {
                if (button == 0) {
                    editingString = !editingString;
                    if (editingString) {
                        tempText = ss.get();
                        cursorPos = tempText.length();
                        lastBlink = System.currentTimeMillis();
                        cursorVisible = true;
                    }
                }
            }
        }

        void mouseReleased() { draggingSlider = false; }

        void mouseDragged(double mouseX) {
            if (draggingSlider && setting instanceof NumberSetting ns) {
                double range = ns.getMax() - ns.getMin();
                double value = Math.max(ns.getMin(), Math.min(ns.getMax(),
                    ns.getMin() + (mouseX - x) / width * range));
                double step = ns.getStep();
                value = Math.round(value / step) * step;
                ns.set(value);
            }
        }

        boolean keyPressed(int keyCode) {
            if (!editingString) return false;
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                editingString = false;
                if (!tempText.isEmpty() && setting instanceof StringSetting ss) ss.set(tempText);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { editingString = false; return true; }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && cursorPos > 0) {
                tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
                cursorPos--;
                lastBlink = System.currentTimeMillis();
                cursorVisible = true;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE && cursorPos < tempText.length()) {
                tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
                lastBlink = System.currentTimeMillis();
                cursorVisible = true;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPos > 0) { cursorPos--; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPos < tempText.length()) { cursorPos++; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (keyCode == GLFW.GLFW_KEY_HOME) { cursorPos = 0; lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            if (keyCode == GLFW.GLFW_KEY_END) { cursorPos = tempText.length(); lastBlink = System.currentTimeMillis(); cursorVisible = true; return true; }
            return false;
        }

        boolean charTyped(char chr) {
            if (!editingString) return false;
            if (chr == '\n' || chr == '\r' || chr == '\t') return false;
            tempText = tempText.substring(0, cursorPos) + chr + tempText.substring(cursorPos);
            cursorPos++;
            lastBlink = System.currentTimeMillis();
            cursorVisible = true;
            return true;
        }
    }
}
