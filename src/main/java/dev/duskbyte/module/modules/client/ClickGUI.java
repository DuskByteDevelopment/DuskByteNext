package dev.duskbyte.module.modules.client;

import dev.duskbyte.DuskByte;
import dev.duskbyte.event.events.PacketReceiveListener;
import dev.duskbyte.gui.ClickGui;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.MinMaxSetting;
import dev.duskbyte.module.setting.ModeSetting;
import dev.duskbyte.module.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.lwjgl.glfw.GLFW;

public final class ClickGUI extends Module implements PacketReceiveListener {
	public static final NumberSetting red = new NumberSetting("Red", 0, 255, 255, 1);
	public static final NumberSetting green = new NumberSetting("Green", 0, 255, 0, 1);
	public static final NumberSetting blue = new NumberSetting("Blue", 0, 255, 50, 1);

	public static final NumberSetting alphaWindow = new NumberSetting("Window Alpha", 0, 255, 170, 1);

	public static final BooleanSetting breathing = new BooleanSetting("Breathing", true)
			.setDescription("Color breathing effect (only with rainbow off)");
	public static final BooleanSetting rainbow = new BooleanSetting("Rainbow", true)
			.setDescription("Enables LGBTQ mode");

	public static final BooleanSetting background = new BooleanSetting("Background", false).setDescription("Renders the background of the Click Gui");
	public static final BooleanSetting customFont = new BooleanSetting("Custom Font", true);

	private final BooleanSetting preventClose = new BooleanSetting("Prevent Close", true)
			.setDescription("For servers with freeze plugins that don't let you open the GUI");

	public static final NumberSetting roundQuads = new NumberSetting("Roundness", 1, 10, 5, 1);
	public static final ModeSetting<AnimationMode> animationMode = new ModeSetting<>("Animations", AnimationMode.Normal, AnimationMode.class);
	public static final BooleanSetting antiAliasing = new BooleanSetting("MSAA", true)
			.setDescription("Anti Aliasing | This can impact performance if you're using tracers but gives them a smoother look |");

	public enum AnimationMode {
		Normal, Positive, Off;
	}

	public ClickGUI() {
		super("DuskByte",
				"Settings for the client",
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				Category.CLIENT);

		addSettings(red, green, blue, alphaWindow, breathing, rainbow, background, preventClose, roundQuads, animationMode, antiAliasing);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketReceiveListener.class, this);
		DuskByte.INSTANCE.previousScreen = mc.currentScreen;

		if (DuskByte.INSTANCE.clickGui != null) {
			mc.setScreenAndRender(DuskByte.INSTANCE.clickGui);
		} else if (mc.currentScreen instanceof InventoryScreen) {
			DuskByte.INSTANCE.guiInitialized = true;
		}

		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketReceiveListener.class, this);

		if (mc.currentScreen instanceof ClickGui) {
			DuskByte.INSTANCE.clickGui.close();
			mc.setScreenAndRender(DuskByte.INSTANCE.previousScreen);
			DuskByte.INSTANCE.clickGui.onGuiClose();
		} else if (mc.currentScreen instanceof InventoryScreen) {
			DuskByte.INSTANCE.guiInitialized = false;
		}

		super.onDisable();
	}


	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (DuskByte.INSTANCE.guiInitialized) {
			if (event.packet instanceof OpenScreenS2CPacket) {
				if (preventClose.getValue())
					event.cancel();
			}
		}
	}
}