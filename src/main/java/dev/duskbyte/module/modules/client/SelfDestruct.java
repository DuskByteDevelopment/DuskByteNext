package dev.duskbyte.module.modules.client;

import com.sun.jna.Memory;
import dev.duskbyte.DuskByte;
import dev.duskbyte.gui.ClickGui;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.*;
import dev.duskbyte.utils.Utils;

import java.io.File;

@SuppressWarnings("all")
public final class SelfDestruct extends Module {
	public static boolean destruct = false;

	private final BooleanSetting replaceMod = new BooleanSetting("Replace Mod", true)
			.setDescription("Repalces the mod with the original JAR file of the ImmediatelyFast mod");

	private final BooleanSetting saveLastModified = new BooleanSetting("Save Last Modified", true)
			.setDescription("Saves the last modified date after self destruct");

	private final StringSetting downloadURL = new StringSetting("Replace URL", "https://cdn.modrinth.com/data/5ZwdcRci/versions/FEOsWs1E/ImmediatelyFast-Fabric-1.2.11%2B1.20.4.jar");

	public SelfDestruct() {
		super("Self Destruct",
				"Removes the client from your game |Credits to lwes for deletion|",
				-1,
				Category.CLIENT);
		addSettings(replaceMod, saveLastModified, downloadURL);
	}

	@Override
	public void onEnable() {
		destruct = true;

		DuskByte.INSTANCE.getModuleManager().getModule(ClickGUI.class).setEnabled(false);
		setEnabled(false);

		DuskByte.INSTANCE.getProfileManager().saveProfile();

		if (mc.currentScreen instanceof ClickGui) {
			DuskByte.INSTANCE.guiInitialized = false;
			mc.currentScreen.close();
		}

		if (replaceMod.getValue()) {
			try {
				String modUrl = downloadURL.getValue();
				File currentJar = Utils.getCurrentJarPath();

				if (currentJar.exists())
                    Utils.replaceModFile(modUrl, Utils.getCurrentJarPath());
			} catch (Exception ignored) {}
		}

		for (Module module : DuskByte.INSTANCE.getModuleManager().getModules()) {
			module.setEnabled(false);

			module.setName(null);
			module.setDescription(null);

			for (Setting<?> setting : module.getSettings()) {
				setting.setName(null);
				setting.setDescription(null);

				if(setting instanceof StringSetting set)
					set.setValue(null);
			}
			module.getSettings().clear();
		}

		Runtime runtime = Runtime.getRuntime();

		if (saveLastModified.getValue())
			DuskByte.INSTANCE.resetModifiedDate();

		for (int i = 0; i <= 10; i++) {
			runtime.gc();
			runtime.runFinalization();

			try {
				Thread.sleep(100 * i);
				Memory.purge();
				Memory.disposeAll();
			} catch (InterruptedException ignored) {}
		}
	}
}