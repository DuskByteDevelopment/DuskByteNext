
package dev.duskbyte.util;

import com.google.gson.*;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.setting.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigManager {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("duskbyte");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static long lastSave = 0;
    private static final long SAVE_INTERVAL = 5000;

    public static void load() {
        try {
            if (!Files.exists(CONFIG_FILE)) return;
            String json = Files.readString(CONFIG_FILE);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) return;
            JsonObject modules = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module m : ModuleManager.getModules()) {
                JsonObject modObj = modules.has(m.getName()) ? modules.getAsJsonObject(m.getName()) : null;
                if (modObj == null) continue;
                if (modObj.has("enabled") && modObj.get("enabled").getAsBoolean()) m.enable();
                if (modObj.has("key")) m.setKey(modObj.get("key").getAsInt());
                if (modObj.has("settings") && m.getSettings().size() > 0) {
                    JsonObject sObj = modObj.getAsJsonObject("settings");
                    for (Setting<?> s : m.getSettings()) {
                        if (!sObj.has(s.getName())) continue;
                        JsonElement val = sObj.get(s.getName());
                        if (s instanceof BoolSetting bs) bs.set(val.getAsBoolean());
                        else if (s instanceof NumberSetting ns) ns.set(val.getAsDouble());
                        else if (s instanceof ModeSetting ms) ms.set(val.getAsString());
                        else if (s instanceof ColorSetting cs) cs.set(val.getAsInt());
                        else if (s instanceof StringSetting ss) ss.set(val.getAsString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DuskByte] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            JsonObject root = new JsonObject();
            JsonObject modules = new JsonObject();
            for (Module m : ModuleManager.getModules()) {
                JsonObject modObj = new JsonObject();
                modObj.addProperty("enabled", m.isEnabled());
                modObj.addProperty("key", m.getKey());
                if (m.getSettings().size() > 0) {
                    JsonObject sObj = new JsonObject();
                    for (Setting<?> s : m.getSettings()) {
                        if (s instanceof BoolSetting bs) sObj.addProperty(s.getName(), bs.get());
                        else if (s instanceof NumberSetting ns) sObj.addProperty(s.getName(), ns.get());
                        else if (s instanceof ModeSetting ms) sObj.addProperty(s.getName(), ms.get());
                        else if (s instanceof ColorSetting cs) sObj.addProperty(s.getName(), cs.get());
                        else if (s instanceof StringSetting ss) sObj.addProperty(s.getName(), ss.get());
                    }
                    modObj.add("settings", sObj);
                }
                modules.add(m.getName(), modObj);
            }
            root.add("modules", modules);
            Files.writeString(CONFIG_FILE, GSON.toJson(root));
        } catch (Exception e) {
            System.err.println("[DuskByte] Failed to save config: " + e.getMessage());
        }
    }

    public static void autosave() {
        long now = System.currentTimeMillis();
        if (now - lastSave >= SAVE_INTERVAL) {
            save();
            lastSave = now;
        }
    }
}
