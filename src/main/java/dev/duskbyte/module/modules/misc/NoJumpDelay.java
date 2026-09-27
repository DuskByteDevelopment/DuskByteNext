package dev.duskbyte.module.modules.misc;

import dev.duskbyte.event.events.TickListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import org.lwjgl.glfw.GLFW;

public final class NoJumpDelay extends Module implements TickListener {
	public NoJumpDelay() {
		super("No Jump Delay",
				"Lets you jump faster, removing the delay",
				-1,
				Category.MISC);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null)
			return;

		if (!mc.player.isOnGround())
			return;

		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_PRESS)
			return;

		mc.options.jumpKey.setPressed(false);
		mc.player.jump();
	}
}
