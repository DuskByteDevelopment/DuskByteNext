
package dev.duskbyte;

import dev.duskbyte.gui.ClickGuiScreen;
import dev.duskbyte.hud.HudRenderer;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.util.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DuskByteClient implements ClientModInitializer {
    public static final String NAME = "DuskByte";
    public static String VERSION = "dev";

    static {
        try {
            var url = DuskByteClient.class.getProtectionDomain().getCodeSource().getLocation();
            var manifest = new java.net.URL(url, "META-INF/MANIFEST.MF").openConnection().getInputStream();
            var props = new java.util.Properties();
            props.load(manifest);
            manifest.close();
            VERSION = props.getProperty("Implementation-Version", "dev");
        } catch (Exception ignored) {}
    }

    public static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.duskbyte.clickgui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.duskbyte"));
        HudRenderCallback.EVENT.register(HudRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clickGuiKey.wasPressed()) client.setScreen(new ClickGuiScreen());
            ModuleManager.tick();
            ConfigManager.autosave();
        });
        ConfigManager.load();
    }
}
