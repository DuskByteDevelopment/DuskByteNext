
package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.AnimatedValue;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/**
 * Popup panel for editing module settings.
 * Reference: SettingPanel + SettingComponent from NekoFlanHelper.
 */
public class SettingPanel extends GuiPanel {
    private static SettingPanel instance;

    private final Module module;
    private final List<SettingRow> rows = new ArrayList<>();
    private final float titleHeight = 16f;
    private final float rowHeight = 14f;
    private SettingRow editingRow = null;

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
        instance.editingRow = null; // bind mode handled separately
    }

    public static void close() { instance = null; }
    public static SettingPanel get() { return instance; }

    @Override
    public float getTotalContentHeight() {
        return rows.size() * (rowHeight + 1);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        updateScrollPhysics();
        updateMouseState(mouseX, mouseY);

        // Background
        ctx.fill((int) x, (int) y, (int) (x + width), (int) (y + height), 0xE0111111);

        // Title bar
        ctx.fill((int) x, (int) y, (int) (x + width), (int) (y + titleHeight), 0xFF2A2A3A);
        ctx.drawTextWithShadow(mc.textRenderer, "§l" + module.getTitle().getString(), x + 4, y + 4, 0xFFFFFFFF);

        // Close hint
        ctx.drawTextWithShadow(mc.textRenderer, "ESC", x + width - 20, y + 4, 0x888888);

        // Setting rows
        float contentY = y + titleHeight + 2;
        float contentBottom = y + height;

        for (int i = 0; i < rows.size(); i++) {
            SettingRow row = rows.get(i);
            float rowY = contentY + i * (rowHeight + 1) - scrollProgress;

            if (rowY + rowHeight < contentY) continue;
            if (rowY > contentBottom) break;

            row.render(ctx, x + 2, rowY, width - 4, rowHeight, mouseX, mouseY);
        }

        // Scrollbar
        float totalContent = getTotalContentHeight();
        if (totalContent > height - titleHeight - 4) {
            float viewRatio = (height - titleHeight - 4) / totalContent;
            float scrollRatio = scrollProgress / totalContent;
            int sbX = (int) (x + width - 3);
            int sbH = Math.max(8, (int) ((height - titleHeight - 4) * viewRatio));
            int sbY = (int) (y + titleHeight + 2 + scrollRatio * (height - titleHeight - 4 - sbH));
            ctx.fill(sbX, sbY, sbX + 2, sbY + sbH, 0x60FFFFFF);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) {
            close();
            return;
        }

        // Title bar = drag
        if (mouseY >= y && mouseY <= y + titleHeight) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
            return;
        }

        // Row clicks
        float contentY = y + titleHeight + 2;
        for (int i = 0; i < rows.size(); i++) {
            SettingRow row = rows.get(i);
            float rowY = contentY + i * (rowHeight + 1) - scrollProgress;
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

    private class SettingRow {
        final Setting<?> setting;
        boolean draggingSlider = false;
        // String editing
        boolean editingString = false;
        String tempText = "";
        int cursorPos = 0;
        long lastBlink = 0;
        boolean cursorVisible = true;
        // Bind editing
        boolean editingBind = false;

        SettingRow(Setting<?> setting) {
            this.setting = setting;
        }

        void render(DrawContext ctx, float x, float y, float w, float h, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

            // Background
            int bg = hovered ? 0xFF252525 : 0xFF1A1A1A;
            if (editingString) bg = 0xFF2A2A4A;
            ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), bg);

            String name = setting.getName();

            if (setting instanceof BoolSetting bs) {
                // Toggle animation bar
                float progress = bs.get() ? 1f : 0f;
                int barColor = bs.get() ? 0xFF44AA44 : 0xFF444444;
                if (progress > 0) {
                    ctx.fill((int) x, (int) y, (int) (x + w * progress), (int) (y + h), barColor);
                }
                ctx.drawTextWithShadow(mc.textRenderer, name, x + 5, y + 3, 0xCCCCCC);
                String val = bs.get() ? "ON" : "OFF";
                ctx.drawTextWithShadow(mc.textRenderer, val, x + w - mc.textRenderer.getWidth(val) - 5, y + 3,
                    bs.get() ? 0x44FF44 : 0xFF4444);

            } else if (setting instanceof NumberSetting ns) {
                // Slider bar
                double range = ns.getMax() - ns.getMin();
                if (range > 0) {
                    float progress = (float) ((ns.get() - ns.getMin()) / range);
                    ctx.fill((int) x, (int) y, (int) (x + w * progress), (int) (y + h), 0xFF44AA44);
                }
                ctx.drawTextWithShadow(mc.textRenderer, name, x + 5, y + 3, 0xCCCCCC);
                String val = String.format("%.1f", ns.get());
                ctx.drawTextWithShadow(mc.textRenderer, val, x + w - mc.textRenderer.getWidth(val) - 5, y + 3, 0xFFFF44);

            } else if (setting instanceof ModeSetting ms) {
                ctx.drawTextWithShadow(mc.textRenderer, name, x + 5, y + 3, 0xCCCCCC);
                String val = ms.get();
                ctx.drawTextWithShadow(mc.textRenderer, val, x + w - mc.textRenderer.getWidth(val) - 5, y + 3, 0x44FFFF);

            } else if (setting instanceof ColorSetting cs) {
                ctx.drawTextWithShadow(mc.textRenderer, name, x + 5, y + 3, 0xCCCCCC);
                ctx.fill((int) (x + w - 14), (int) (y + 2), (int) (x + w - 2), (int) (y + 12), 0xFF000000 | cs.get());

            } else if (setting instanceof StringSetting ss) {
                ctx.drawTextWithShadow(mc.textRenderer, name + ":", x + 5, y + 3, 0xCCCCCC);
                if (editingString) {
                    String display = tempText + (cursorVisible ? "_" : "");
                    ctx.drawTextWithShadow(mc.textRenderer, display, x + 5, y + 3, 0xFFFF44);
                } else {
                    String val = ss.get().isEmpty() ? "[empty]" : ss.get();
                    ctx.drawTextWithShadow(mc.textRenderer, val, x + w - mc.textRenderer.getWidth(val) - 5, y + 3, 0x44FFFF);
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
            } else if (setting instanceof ColorSetting cs) {
                // Color picker could be added later
            }
        }

        void mouseReleased() {
            draggingSlider = false;
        }

        void mouseDragged(double mouseX) {
            if (draggingSlider && setting instanceof NumberSetting ns) {
                double range = ns.getMax() - ns.getMin();
                double value = Math.max(ns.getMin(), Math.min(ns.getMax(),
                    ns.getMin() + (mouseX - x) / width * range));
                // Snap to step
                double step = ns.getStep();
                value = Math.round(value / step) * step;
                ns.set(value);
            }
        }

        boolean keyPressed(int keyCode) {
            if (!editingString) return false;
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                finishEditing();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                editingString = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (cursorPos > 0) {
                    tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
                    cursorPos--;
                    resetBlink();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (cursorPos < tempText.length()) {
                    tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
                    resetBlink();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
                cursorPos--;
                resetBlink();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPos < tempText.length()) {
                cursorPos++;
                resetBlink();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_HOME) {
                cursorPos = 0;
                resetBlink();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_END) {
                cursorPos = tempText.length();
                resetBlink();
                return true;
            }
            return false;
        }

        boolean charTyped(char chr) {
            if (!editingString) return false;
            if (chr == '\n' || chr == '\r' || chr == '\t') return false;
            tempText = tempText.substring(0, cursorPos) + chr + tempText.substring(cursorPos);
            cursorPos++;
            resetBlink();
            return true;
        }

        private void finishEditing() {
            editingString = false;
            if (!tempText.isEmpty() && setting instanceof StringSetting ss) {
                ss.set(tempText);
            }
        }

        private void resetBlink() {
            lastBlink = System.currentTimeMillis();
            cursorVisible = true;
        }
    }
}
