
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
    private StringSetting editingStringSetting = null;

    // Click animation data
    private final Map<String, ClickAnimation> animations = new HashMap<>();

    public ClickGuiScreen() {
        super(Text.literal("DuskByte ClickGUI"));
        for (Category c : categories) expanded.put(c, true);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        int panelW = 110, panelH = 14, spacing = 3, startX = 6, startY = 6;

        // Update animations
        long now = System.currentTimeMillis();
        animations.entrySet().removeIf(e -> now - e.getValue().startTime > 300);

        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            int x = startX + i * (panelW + spacing);
            int y = startY;

            // Category header with animation
            int bgColor = 0xFF222222;
            String catAnimKey = "cat_" + cat.name();
            if (animations.containsKey(catAnimKey)) {
                var anim = animations.get(catAnimKey);
                float progress = Math.min(1f, (now - anim.startTime) / 300f);
                int alpha = (int)(0xFF * (1f - progress));
                bgColor = 0xFF222222 | (alpha << 24);
            }
            ctx.fill(x, y, x + panelW, y + panelH, bgColor);
            ctx.fill(x, y, x + panelW, y + panelH - 2, cat.color | 0xFF000000);
            ctx.fill(x, y + panelH - 2, x + panelW, y + panelH, 0xFF222222);

            String catName = cat.getTitle().getString();
            ctx.drawTextWithShadow(textRenderer, "§l" + catName, x + 3, y + 3, 0xFFFFFF);

            int modCount = ModuleManager.getByCategory(cat).size();
            ctx.drawTextWithShadow(textRenderer, modCount + "", x + panelW - 10, y + 3, 0xAAAAAA);

            if (!expanded.getOrDefault(cat, false)) continue;
            y += panelH + spacing;
            List<Module> mods = ModuleManager.getByCategory(cat);
            for (Module m : mods) {
                int bgColorMod = m.isEnabled() ? 0xFF333333 : 0xFF1A1A1A;
                int accentColor = m.isEnabled() ? (cat.color | 0xFF000000) : 0xFF333333;

                // Module click animation
                String modAnimKey = "mod_" + m.getName();
                if (animations.containsKey(modAnimKey)) {
                    var anim = animations.get(modAnimKey);
                    float progress = Math.min(1f, (now - anim.startTime) / 300f);
                    int pulse = (int)(0x44 * (1f - progress));
                    bgColorMod = (bgColorMod & 0xFFFFFF) | ((pulse << 24));
                    accentColor = (accentColor & 0xFFFFFF) | ((pulse + 0xBB) << 24);
                }

                ctx.fill(x, y, x + panelW, y + panelH, bgColorMod);
                ctx.fill(x, y, x + 2, y + panelH, accentColor);

                String modName = m.getTitle().getString();
                int textColor = m.isEnabled() ? 0xFFFFFF : 0xAAAAAA;
                ctx.drawTextWithShadow(textRenderer, modName, x + 5, y + 3, textColor);

                if (m.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                    String keyName = GLFW.glfwGetKeyName(m.getKey(), 0);
                    if (keyName == null) keyName = "K" + m.getKey();
                    ctx.drawTextWithShadow(textRenderer, keyName, x + panelW - textRenderer.getWidth(keyName) - 3, y + 3, 0x666666);
                }

                if (expandedModule == m) {
                    int sy = y + panelH + 1;
                    for (Setting<?> s : m.getSettings()) {
                        ctx.fill(x + 3, sy, x + panelW, sy + 13, 0xFF111111);
                        String sName = s.getName();
                        if (s instanceof BoolSetting bs) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 6, sy + 3, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, bs.get() ? "ON" : "OFF", x + panelW - 20, sy + 3, bs.get() ? 0x44FF44 : 0xFF4444);
                        } else if (s instanceof NumberSetting ns) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 6, sy + 3, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, String.format("%.1f", ns.get()), x + panelW - 26, sy + 3, 0xFFFF44);
                        } else if (s instanceof ModeSetting ms) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 6, sy + 3, 0xCCCCCC);
                            ctx.drawTextWithShadow(textRenderer, ms.get(), x + panelW - textRenderer.getWidth(ms.get()) - 3, sy + 3, 0x44FFFF);
                        } else if (s instanceof ColorSetting cs) {
                            ctx.fill(x + panelW - 14, sy + 1, x + panelW - 2, sy + 12, 0xFF000000 | cs.get());
                            ctx.drawTextWithShadow(textRenderer, sName, x + 6, sy + 3, 0xCCCCCC);
                        } else if (s instanceof StringSetting ss) {
                            ctx.drawTextWithShadow(textRenderer, sName, x + 6, sy + 3, 0xCCCCCC);
                            String display = ss.get().isEmpty() ? "[empty]" : ss.get();
                            if (editingStringSetting == ss) {
                                display += "_";
                                ctx.fill(x + panelW - textRenderer.getWidth(display) - 6, sy + 1, x + panelW - 2, sy + 12, 0xFF444488);
                            }
                            ctx.drawTextWithShadow(textRenderer, display, x + panelW - textRenderer.getWidth(display) - 4, sy + 3, editingStringSetting == ss ? 0xFFFF44 : 0x44FFFF);
                        }
                        sy += 15;
                    }
                    // Keybind row
                    ctx.fill(x + 3, sy, x + panelW, sy + 13, 0xFF111111);
                    String keyName = m.getKey() == GLFW.GLFW_KEY_UNKNOWN ? "NONE" : GLFW.glfwGetKeyName(m.getKey(), 0);
                    if (keyName == null) keyName = "Key" + m.getKey();
                    String bindText = bindModule == m ? "> Press key..." : "Bind: " + keyName;
                    ctx.drawTextWithShadow(textRenderer, bindText, x + 6, sy + 3, bindModule == m ? 0xFFFF00 : 0xAAAAAA);
                    sy += 15;
                    y = sy - 2;
                } else {
                    y += panelH + spacing;
                }
            }
        }
        ctx.fill(0, this.height - 16, this.width, this.height, 0xFF111111);
        ctx.drawTextWithShadow(textRenderer, "§b§lDuskByte §7v" + dev.duskbyte.DuskByteClient.VERSION, 6, this.height - 12, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = 110, panelH = 14, spacing = 3, startX = 6, startY = 6;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            int x = startX + i * (panelW + spacing);
            int y = startY;
            if (mouseX >= x && mouseX <= x + panelW && mouseY >= y && mouseY <= y + panelH) {
                if (button == 0) {
                    expanded.put(cat, !expanded.getOrDefault(cat, false));
                    animations.put("cat_" + cat.name(), new ClickAnimation());
                    return true;
                }
            }
            if (!expanded.getOrDefault(cat, false)) continue;
            y += panelH + spacing;
            List<Module> mods = ModuleManager.getByCategory(cat);
            for (Module m : mods) {
                if (mouseX >= x && mouseX <= x + panelW && mouseY >= y && mouseY <= y + panelH) {
                    if (button == 0) {
                        expandedModule = (expandedModule == m) ? null : m;
                        animations.put("mod_" + m.getName(), new ClickAnimation());
                    }
                    else if (button == 1) {
                        m.toggle();
                        animations.put("mod_" + m.getName(), new ClickAnimation());
                    }
                    else if (button == 2) bindModule = (bindModule == m) ? null : m;
                    return true;
                }
                y += panelH + spacing;
                if (expandedModule == m) {
                    int sy = y;
                    for (Setting<?> s : m.getSettings()) {
                        if (mouseX >= x + 3 && mouseX <= x + panelW && mouseY >= sy && mouseY <= sy + 13) {
                            if (s instanceof BoolSetting bs) {
                                bs.toggle();
                                animations.put("mod_" + m.getName() + "_set_" + s.getName(), new ClickAnimation());
                            }
                            else if (s instanceof NumberSetting ns) { if (button == 0) ns.increment(); else ns.decrement(); }
                            else if (s instanceof ModeSetting ms) ms.cycle();
                            else if (s instanceof StringSetting ss) {
                                editingStringSetting = (editingStringSetting == ss) ? null : ss;
                            }
                            return true;
                        }
                        sy += 15;
                    }
                    if (mouseX >= x + 3 && mouseX <= x + panelW && mouseY >= sy && mouseY <= sy + 13) {
                        bindModule = m;
                        return true;
                    }
                    y = sy + 15;
                }
            }
        }
        editingStringSetting = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingStringSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                editingStringSetting = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                editingStringSetting.backspace();
                return true;
            }
            return true; // Consume all keys while editing
        }

        if (bindModule != null) {
            bindModule.setKey(keyCode == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : keyCode);
            bindModule = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editingStringSetting != null) {
            editingStringSetting.append(chr);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static class ClickAnimation {
        final long startTime = System.currentTimeMillis();
    }
}
