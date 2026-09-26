package dev.duskbyte.gui.clickgui;

import dev.duskbyte.gui.util.AnimatedValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Base panel class with drag, scroll, animation.
 * Reference: CategoryPanel from NekoFlanHelper.
 */
public abstract class GuiPanel {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    protected float x, y, width, height;
    protected boolean dragging = false;
    protected double dragOffsetX, dragOffsetY;

    // Scroll
    protected float scrollProgress = 0f;
    protected float scrollVelocity = 0f;
    protected long lastScrollTime = System.currentTimeMillis();

    // Mouse state animation
    protected MouseState prevState = MouseState.NONE;
    protected MouseState mouseState = MouseState.NONE;
    protected long lastStateTime = System.currentTimeMillis();

    // Hover animation
    protected final AnimatedValue hoverAnim = new AnimatedValue(0, 12);

    public GuiPanel(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext ctx, int mouseX, int mouseY, float delta);
    public abstract void mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);
    public abstract void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy);
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);
    public abstract boolean charTyped(char chr, int modifiers);
    public abstract float getTotalContentHeight();

    public boolean isHovered(double mx, double my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    protected void updateScrollPhysics() {
        long now = System.currentTimeMillis();
        double dt = (now - lastScrollTime) / 1000.0;
        lastScrollTime = now;

        // Exponential decay
        double decay = Math.pow(0.001, dt);
        scrollProgress += scrollVelocity * dt * 3;
        scrollVelocity *= decay;

        // Clamp
        float maxScroll = Math.max(0, getTotalContentHeight() - height + 10);
        if (scrollProgress < 0) {
            scrollVelocity = 0;
            scrollProgress = 0;
        } else if (scrollProgress > maxScroll) {
            scrollVelocity = 0;
            scrollProgress = maxScroll;
        }
    }

    public void handleScroll(double verticalAmount) {
        scrollVelocity -= (float) verticalAmount * 20f;
    }

    protected void setMouseState(MouseState newState) {
        if (newState != this.mouseState) {
            this.prevState = this.mouseState;
            this.mouseState = newState;
            this.lastStateTime = System.currentTimeMillis();
        }
    }

    protected void updateMouseState(double mx, double my) {
        boolean hovered = isHovered(mx, my);
        MouseState newState = dragging ? MouseState.DRAG :
                mouseState == MouseState.CLICK ? MouseState.CLICK :
                hovered ? MouseState.HOVER : MouseState.NONE;
        setMouseState(newState);
    }

    protected int getOverlayColor() {
        return switch (mouseState) {
            case HOVER -> 0x30FFFFFF;
            case CLICK, DRAG -> 0x50FFFFFF;
            default -> 0x00000000;
        };
    }

    public enum MouseState { NONE, HOVER, CLICK, DRAG }
}
