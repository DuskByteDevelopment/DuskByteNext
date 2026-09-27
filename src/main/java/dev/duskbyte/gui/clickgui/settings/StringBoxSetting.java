
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.gui.util.ColorUtils;
import dev.duskbyte.setting.StringSetting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

/**
 * Argon-style string input setting with cursor.
 */
public final class StringBoxSetting extends RenderableSetting {
    private final StringSetting setting;
    private boolean editing = false;
    private String tempText = "";
    private int cursorPos = 0;
    private long lastBlink = 0;
    private boolean cursorVisible = true;
    private Color currentAlpha;

    public StringBoxSetting(ModuleButton parent, StringSetting setting, int offset) {
        super(parent, setting, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // Label
        ctx.drawTextWithShadow(mc.textRenderer, setting.getName() + ":",
            parentX() + 5, parentY() + parentOffset() + offset + 7, 0xFFF5F5F5);

        if (editing) {
            // Blink cursor
            long now = System.currentTimeMillis();
            if (now - lastBlink > 530) { cursorVisible = !cursorVisible; lastBlink = now; }

            // Input underline
            ctx.fill(parentX() + 5, parentY() + parentOffset() + offset + parentHeight() - 4,
                parentX() + parentWidth() - 5, parentY() + parentOffset() + offset + parentHeight() - 3,
                ColorUtils.getMainColor(255, parent.settings.indexOf(this)).getRGB());

            String display = tempText + (cursorVisible ? "|" : "");
            ctx.drawTextWithShadow(mc.textRenderer, display,
                parentX() + 5, parentY() + parentOffset() + offset + 7, 0xFF60A5FA);
        } else {
            String val = setting.get().isEmpty() ? "..." : setting.get();
            ctx.drawTextWithShadow(mc.textRenderer, val,
                parentX() + parentWidth() - mc.textRenderer.getWidth(val) - 5,
                parentY() + parentOffset() + offset + 7, 0xFF22D3EE);
        }

        renderHover(ctx, mouseX, mouseY);
    }

    private void renderHover(DrawContext ctx, int mouseX, int mouseY) {
        if (parent.parent.dragging) return;
        int toAlpha = isHovered(mouseX, mouseY) ? 15 : 0;
        if (currentAlpha == null) currentAlpha = new Color(255, 255, 255, toAlpha);
        else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());
        if (currentAlpha.getAlpha() != toAlpha)
            currentAlpha = ColorUtils.smoothAlphaTransition(0.05f, toAlpha, currentAlpha);
        ctx.fill(parentX(), parentY() + parentOffset() + offset,
            parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(),
            currentAlpha.getRGB());
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            editing = !editing;
            if (editing) {
                tempText = setting.get();
                cursorPos = tempText.length();
                lastBlink = System.currentTimeMillis();
                cursorVisible = true;
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int sc, int mod) {
        if (!editing) return;
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            editing = false;
            if (!tempText.isEmpty()) setting.set(tempText);
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            editing = false;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && cursorPos > 0) {
            tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
            cursorPos--; lastBlink = System.currentTimeMillis(); cursorVisible = true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE && cursorPos < tempText.length()) {
            tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
            lastBlink = System.currentTimeMillis(); cursorVisible = true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
            cursorPos--; lastBlink = System.currentTimeMillis(); cursorVisible = true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPos < tempText.length()) {
            cursorPos++; lastBlink = System.currentTimeMillis(); cursorVisible = true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            cursorPos = 0; lastBlink = System.currentTimeMillis(); cursorVisible = true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            cursorPos = tempText.length(); lastBlink = System.currentTimeMillis(); cursorVisible = true;
        }
    }

    @Override
    public void onGuiClose() { currentAlpha = null; editing = false; }
}
