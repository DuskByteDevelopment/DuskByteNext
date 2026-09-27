
package dev.duskbyte.gui.clickgui.settings;

import dev.duskbyte.gui.clickgui.ModuleButton;
import dev.duskbyte.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Base class for argon-style setting renderers.
 */
public abstract class RenderableSetting {
    protected final ModuleButton parent;
    protected final Setting<?> setting;
    public int offset;
    protected boolean mouseOver;
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public RenderableSetting(ModuleButton parent, Setting<?> setting, int offset) {
        this.parent = parent;
        this.setting = setting;
        this.offset = offset;
    }

    public void onUpdate() {
        mouseOver = isHovered(0, 0); // Will be updated by render
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        mouseOver = isHovered(mouseX, mouseY);
        // Background
        ctx.fill(parentX(), parentY() + parentOffset() + offset,
                 parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(),
                 0xBB1A1A2E);
    }

    public void renderDescription(DrawContext ctx, int mouseX, int mouseY, float delta) {}

    public void mouseClicked(double mouseX, double mouseY, int button) {}
    public void mouseReleased(double mouseX, double mouseY, int button) {}
    public void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {}
    public void keyPressed(int keyCode, int sc, int mod) {}
    public void onGuiClose() {}

    public boolean isHovered(double mx, double my) {
        return mx > parentX() && mx < parentX() + parentWidth()
            && my > parentY() + parentOffset() + offset
            && my < parentY() + parentOffset() + offset + parentHeight();
    }

    protected int parentX() { return parent.parentX(); }
    protected int parentY() { return parent.parentY(); }
    protected int parentWidth() { return parent.parentWidth(); }
    protected int parentHeight() { return parent.parentHeight(); }
    protected int parentOffset() { return parent.offset; }
}
