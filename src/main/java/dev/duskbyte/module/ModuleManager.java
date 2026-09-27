
package dev.duskbyte.module;

import dev.duskbyte.module.combat.*;
import dev.duskbyte.module.movement.*;
import dev.duskbyte.module.render.*;
import dev.duskbyte.module.player.*;
import dev.duskbyte.module.misc.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    private static final Map<Module, Boolean> keyStates = new ConcurrentHashMap<>();

    public static void init() {
        modules.add(new KillAura());
        modules.add(new Velocity());
        modules.add(new Criticals());
        modules.add(new AutoClicker());
        modules.add(new Sprint());
        modules.add(new Speed());
        modules.add(new Flight());
        modules.add(new NoFall());
        modules.add(new Step());
        modules.add(new ElytraFly());
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new Fullbright());
        modules.add(new Chams());
        modules.add(new AutoTotem());
        modules.add(new ChestStealer());
        modules.add(new FastPlace());
        modules.add(new AutoEat());
        modules.add(new AntiAFK());
        modules.add(new dev.duskbyte.module.misc.Timer());
        modules.add(new ChatSuffix());
        modules.add(new AutoEZ());
        modules.add(new AutoWeapon());
        modules.add(new TriggerBot());
        modules.add(new Jesus());
        modules.add(new Parkour());
        modules.add(new HighJump());
        modules.add(new Sneak());
        modules.add(new NoSlow());
        modules.add(new Nametags());
        modules.add(new StorageESP());
        modules.add(new AutoTool());
        modules.add(new AutoArmor());
        modules.add(new AutoRespawn());
        modules.add(new AutoLog());
        modules.add(new ChestAura());
        modules.add(new MiddleClickPearl());
        modules.add(new Zoom());
        modules.add(new ViewLock());
        modules.add(new FastBreak());
        modules.add(new NoRotate());
        modules.add(new FastUse());
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        long window = mc.getWindow().getHandle();
        for (Module m : modules) {
            if (m.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                boolean down = InputUtil.isKeyPressed(window, m.getKey());
                if (down && !keyStates.getOrDefault(m, false)) m.toggle();
                keyStates.put(m, down);
            }
            if (m.isEnabled()) m.onTick();
        }
    }

    public static void attack(net.minecraft.entity.Entity target) {
        for (Module m : modules) {
            if (m.isEnabled()) m.onAttack(target);
        }
    }

    public static Module get(Class<? extends Module> clazz) {
        return modules.stream().filter(m -> clazz.isInstance(m)).findFirst().orElse(null);
    }

    public static List<Module> getModules() { return Collections.unmodifiableList(modules); }

    public static List<Module> getEnabled() {
        List<Module> enabled = new ArrayList<>();
        for (Module m : modules) if (m.isEnabled()) enabled.add(m);
        return enabled;
    }

    public static List<Module> getByCategory(Category cat) {
        List<Module> list = new ArrayList<>();
        for (Module m : modules) if (m.getCategory() == cat) list.add(m);
        return list;
    }
}
