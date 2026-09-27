package dev.duskbyte.module.modules.client;

import dev.duskbyte.managers.TranslationManager;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class CNLang extends Module {
    private final BooleanSetting autoTranslate = new BooleanSetting("Auto Translate", true)
            .setDescription("Automatically translates all module names to Chinese");

    public CNLang() {
        super("中文语言",
                "将所有模块翻译为中文 | Translate all modules to Chinese",
                -1,
                Category.CLIENT);
        addSettings(autoTranslate);
    }

    @Override
    public void onEnable() {
        TranslationManager.getInstance().enableChinese();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TranslationManager.getInstance().disableChinese();
        super.onDisable();
    }
}
