
package dev.duskbyte.gui;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.setting.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public class ClickGuiScreen extends Screen {
    private final List<Category> categories = new ArrayList<>(Arrays.asList(Category.values()));
    private final Map<Category, Boolean> expanded = new HashMap<>();
    private Module bindModule = null;
    private Module expandedModule = null;

    public ClickGuiScreen() {
        super(Text.literal("DuskByte ClickGUI"));
        for (Category c : categories) expanded.put(c, true);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        int panelW = 130, panelH = 20, spacing = 4, startX = 10, startY = 10;

        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            int x = startX + i * (panelW + spacing);
            int y = startY;

            ctx.fill(x, y, x + panelW, y + panelH, 0xFF222222);
            ctx.fill(x, y, x + panelW, y + panelH - 2, cat.color | 0xFF000000);
            ctx.fill(x, y + panelH - 2, x + panelW, y + panelH, 0xFF222222);

            String catName = cat.getTitle().getString();
            ctx.drawTextWithShadow(textRenderer, "§l" + catName, x + 4, y + 6, 0xFFFFFF);

            int modCount = ModuleManager.getByCategory(cat).size();
            ctx.drawTextWithShadow(textRenderer, modCount + "", x + panelW - 14, y + 6, 0xAAAAAA);

            if (!expanded.getOrDefault(cat, false)) continue;
            y += panelH + spacing;
            List<Module> mods = ModuleManager.getByCategory(cat);
            for (Module m : mods) {
                int bgColor = m.isEnabled() ? 0xFF333333 : 0xFF1A1A1A;
                int accentColor = m.isEnabled() ? (cat.color | 0xFF000000) : 0xFF333333;

                ctx.fill(x, y, x + panelW, y + panelH, bgColor);
                ctx.fill(x, y, x + 2, y + panelH, accentColor);

                String modName = m.getTitle().getString();
                int textColor = m.isEnabled() ? 0xFFFFFF : 0xAAAAAA;
                ctx.drawTextWithShadow(textRenderer, modName, x + 6, y + 6, textColor);

                if (m.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                    String keyName = GLFW.glfwGetKeyName(m.getKey(), 0);
                    if (keyName == null) keyName = "K" + m.getKey();
                    ctx.drawTextWithShadow(textRenderer, keyName, x + panelW - textRenderer.getWidth(keyName) - 4, y + 6, 0x666666);
                }

                if (expandedModule == m) {
                    int sy = y + panelH + 2;
                    for (Setting<?> s : m.getSettings()) {
                        ctx.fill(x + 4, sy, x + panelW, sy + 16, 0xFF111111);
                        String sName = s.getName();
                        if (s instanceof BoolSetting bs) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 8, sy + 4, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, bs.get() ? "ON" : "OFF", x + panelW - 24, sy + 4, bs.get() ? 0x44FF44 : 0xFF4444);
                        } else if (s instanceof NumberSetting ns) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 8, sy + 4, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, String.format("%.1f", ns.get()), x + panelW - 30, sy + 4, 0xFFFF44);
                        } else if (s instanceof ModeSetting ms) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 8, sy + 4, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, ms.get(), x + panelW - textRenderer.getWidth(ms.get()) - 4, sy + 4, 0x44FFFF);
                        } else if (s instanceof ColorSetting cs) {
                            ctx.fill(x + panelW - 16, sy + 2, x + panelW - 2, sy + 14, 0xFF000000 | cs.get());
                            ctx.drawTextWithShadow(textRenderer, sName, x + 8, sy + 4, 0xCCCCCC);
                        }
                        sy += 18;
                    }
                    // Keybind row
                    ctx.fill(x + 4, sy, x + panelW, sy + 16, 0xFF111111);
                    String keyName = m.getKey() == GLFW.GLFW_KEY_UNKNOWN ? "NONE" : GLFW.glfwGetKeyName(m.getKey(), 0);
                    if (keyName == null) keyName = "Key" + m.getKey();
                    String bindText = bindModule == m ? "> Press key..." : "Bind: " + keyName;
                    ctx.drawTextWithShadow(textRenderer, bindText, x + 8, sy + 4, bindModule == m ? 0xFFFF00 : 0xAAAAAA);
                    sy += 18;
                    y = sy - 2;
                } else {
                    y += panelH + spacing;
                }
            }
        }
        ctx.fill(0, this.height - 20, this.width, this.height, 0xFF111111);
        ctx.drawTextWithShadow(textRenderer, "§b§lDuskByte §7v" + dev.duskbyte.DuskByteClient.VERSION, 10, this.height - 14, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 130, panelH = 20, spacing = 4, startX = 10, startY = 10;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            int x = startX + i * (panelW + spacing);
            int y = startY;
            if (mouseX >= x && mouseX <= x + panelW && mouseY >= y && mouseY <= y + panelH) {
                if (button == 0) {
                    expanded.put(cat, !expanded.getOrDefault(cat, false));
                    return true;
                }
            }
            if (!expanded.getOrDefault(cat, false)) continue;
            y += panelH + spacing;
            List<Module> mods = ModuleManager.getByCategory(cat);
            for (Module m : mods) {
                if (mouseX >= x && mouseX <= x + panelW && mouseY >= y && mouseY <= y + panelH) {
                    if (button == 0) expandedModule = (expandedModule == m) ? null : m;
                    else if (button == 1) m.toggle();
                    else if (button == 2) bindModule = (bindModule == m) ? null : m;
                    return true;
                }
                y += panelH + spacing;
                if (expandedModule == m) {
                    int sy = y;
                    for (Setting<?> s : m.getSettings()) {
                        if (mouseX >= x + 4 && mouseX <= x + panelW && mouseY >= sy && mouseY <= sy + 16) {
                            if (s instanceof BoolSetting bs) bs.toggle();
                            else if (s instanceof NumberSetting ns) { if (button == 0) ns.increment(); else ns.decrement(); }
                            else if (s instanceof ModeSetting ms) ms.cycle();
                            return true;
                        }
                        sy += 18;
                    }
                    if (mouseX >= x + 4 && mouseX <= x + panelW && mouseY >= sy && mouseY <= sy + 16) {
                        bindModule = m;
                        return true;
                    }
                    y = sy + 18;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindModule != null) {
            bindModule.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            bindModule = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
