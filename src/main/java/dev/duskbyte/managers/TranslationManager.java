package dev.duskbyte.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.duskbyte.DuskByte;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.Setting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class TranslationManager {
    private static TranslationManager INSTANCE;
    private JsonObject zhTranslations;
    private boolean chineseActive = false;

    private final Map<Module, CharSequence[]> originalModuleData = new HashMap<>();
    private final Map<Setting<?>, CharSequence[]> originalSettingData = new HashMap<>();

    public TranslationManager() {
        INSTANCE = this;
        loadTranslations();
    }

    public static TranslationManager getInstance() {
        return INSTANCE;
    }

    private void loadTranslations() {
        try {
            zhTranslations = loadJsonResource("/assets/duskbyte/lang/zh_cn.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject loadJsonResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return new JsonObject();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    public void enableChinese() {
        if (zhTranslations == null) return;
        chineseActive = true;

        for (Module module : DuskByte.INSTANCE.getModuleManager().getModules()) {
            originalModuleData.put(module, new CharSequence[]{module.getName(), module.getDescription()});

            String moduleKey = getModuleTranslationKey(module);
            if (moduleKey != null && zhTranslations.has(moduleKey)) {
                module.setName(zhTranslations.get(moduleKey).getAsString());
            }

            String descKey = moduleKey != null ? moduleKey + ".desc" : null;
            if (descKey != null && zhTranslations.has(descKey)) {
                module.setDescription(zhTranslations.get(descKey).getAsString());
            }

            for (Setting<?> setting : module.getSettings()) {
                originalSettingData.put(setting, new CharSequence[]{setting.getName(), setting.getDescription()});

                String settingKey = getSettingTranslationKey(setting);
                if (settingKey != null && zhTranslations.has(settingKey)) {
                    setting.setName(zhTranslations.get(settingKey).getAsString());
                }
            }
        }
    }

    public void disableChinese() {
        chineseActive = false;

        for (Map.Entry<Module, CharSequence[]> entry : originalModuleData.entrySet()) {
            Module module = entry.getKey();
            module.setName(entry.getValue()[0]);
            module.setDescription(entry.getValue()[1]);
        }

        for (Map.Entry<Setting<?>, CharSequence[]> entry : originalSettingData.entrySet()) {
            Setting<?> setting = entry.getKey();
            setting.setName(entry.getValue()[0]);
            setting.setDescription(entry.getValue()[1]);
        }

        originalModuleData.clear();
        originalSettingData.clear();
    }

    public boolean isChineseActive() {
        return chineseActive;
    }

    private String getModuleTranslationKey(Module module) {
        String rawName = module.getName().toString();
        String name = rawName.toLowerCase().replace(" ", "");

        String cat = module.getCategory().name.toString().toLowerCase();
        return switch (cat) {
            case "combat" -> "duskbyte.module.combat." + name;
            case "misc" -> "duskbyte.module.misc." + name;
            case "render" -> "duskbyte.module.render." + name;
            case "client" -> "duskbyte.module.client." + name;
            default -> null;
        };
    }

    private String getSettingTranslationKey(Setting<?> setting) {
        return "duskbyte.setting." + setting.getName().toString().toLowerCase().replace(" ", "");
    }
}
