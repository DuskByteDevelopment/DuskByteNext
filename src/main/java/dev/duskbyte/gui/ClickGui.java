package dev.duskbyte.gui;

import dev.duskbyte.DuskByte;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.modules.client.ClickGUI;
import dev.duskbyte.utils.ColorUtils;
import dev.duskbyte.utils.RenderUtils;
import dev.duskbyte.utils.ClickGuiBackground;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.duskbyte.DuskByte.mc;

public final class ClickGui extends Screen {
	public List<Window> windows = new ArrayList<>();
	public Color currentColor;
	private static final StackWalker sw = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public ClickGui() {
		super(Text.empty());

		int offsetX = 50;
		for (Category category : Category.values()) {
			windows.add(new Window(offsetX, 50, 230, 30, category, this));
			offsetX += 250;
		}
	}

	public boolean isDraggingAlready() {
		for(Window window : windows)
			if(window.dragging)
				return true;

		return false;
	}

	@Override
	protected void setInitialFocus() {
		if (client == null) {
			return;
		}
		super.setInitialFocus();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (mc.currentScreen == this) {
			if (DuskByte.INSTANCE.previousScreen != null)
				DuskByte.INSTANCE.previousScreen.render(context, 0, 0, delta);

			if (currentColor == null)
				currentColor = new Color(0, 0, 0, 0);
			else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

			if (currentColor.getAlpha() != (ClickGUI.background.getValue() ? 200 : 0))
				currentColor = ColorUtils.smoothAlphaTransition(0.05F, ClickGUI.background.getValue() ? 200 : 0, currentColor);

			if (mc.currentScreen instanceof ClickGui) {
				if (ClickGUI.background.getValue()) {
					float alpha = currentColor.getAlpha() / 255f;
					// 尝试渲染我们刚才准备的 assets/duskbyte/textures/background.jpg
					boolean rendered = ClickGuiBackground.draw(context, mc.getWindow().getWidth(), mc.getWindow().getHeight(), alpha);
					if (!rendered) {
						// 若图片未加载成功，回退到原先的纯色半透明背景
						context.fill(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), currentColor.getRGB());
					}
				}
			}

			RenderUtils.unscaledProjection();
			mouseX *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
			mouseY *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
			super.render(context, mouseX, mouseY, delta);

			for (Window window : windows) {
				window.render(context, mouseX, mouseY, delta);
				window.updatePosition(mouseX, mouseY, delta);
			}

			RenderUtils.scaledProjection();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (Window window : windows)
			window.keyPressed(keyCode, scanCode, modifiers);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouseX *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
		mouseY *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();

		for (Window window : windows)
			window.mouseClicked(mouseX, mouseY, button);

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		mouseX *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
		mouseY *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
		for (Window window : windows)
			window.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		MinecraftClient mc = MinecraftClient.getInstance();
		mouseY *= mc.getWindow().getScaleFactor();

		for (Window window : windows)
			window.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		DuskByte.INSTANCE.getModuleManager().getModule(ClickGUI.class).setEnabledStatus(false);
		onGuiClose();
	}

	public void onGuiClose() {
		mc.setScreenAndRender(DuskByte.INSTANCE.previousScreen);
		currentColor = null;

		for (Window window : windows)
			window.onGuiClose();
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseX *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
		mouseY *= (int) MinecraftClient.getInstance().getWindow().getScaleFactor();

		for (Window window : windows)
			window.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(mouseX, mouseY, button);
	}
}
