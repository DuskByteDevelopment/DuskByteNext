
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public class AutoEZ extends Module {
    private final StringSetting message = add(new StringSetting("Message", "ez %name%"));
    private final NumberSetting delay = add(new NumberSetting("Delay", 500, 0, 2000, 100));
    private final BoolSetting publicChat = add(new BoolSetting("PublicChat", true));
    private final Set<String> recentlyKilled = new HashSet<>();
    private long lastSend = 0;

    public AutoEZ() {
        super("AutoEZ", "Sends taunt message after killing a player", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() {
        recentlyKilled.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();

        // Check tracked players for death
        Iterator<Map.Entry<String, Long>> it = deathTimers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            String name = entry.getKey();
            long deathTime = entry.getValue();

            if (now - deathTime > 10000) { // 10s timeout
                it.remove();
                recentlyKilled.remove(name);
                continue;
            }

            if (now - lastSend < delay.get()) continue;

            // Find the player entity
            Entity target = null;
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof PlayerEntity p && p.getName().getString().equals(name)) {
                    target = e;
                    break;
                }
            }

            // Player died (removed or dead pose)
            boolean isDead = (target == null || target.isRemoved() ||
                (target instanceof PlayerEntity p && p.getHealth() <= 0));

            if (isDead && !recentlyKilled.contains(name)) {
                recentlyKilled.add(name);
                String msg = message.get().replace("%name%", name);
                if (publicChat.get()) {
                    mc.player.networkHandler.sendChatMessage(msg);
                }
                lastSend = now;
                it.remove();
            }
        }
    }

    private final Map<String, Long> deathTimers = new HashMap<>();

    @Override
    public void onAttack(Entity target) {
        if (target instanceof PlayerEntity p) {
            String name = p.getName().getString();
            if (!deathTimers.containsKey(name)) {
                deathTimers.put(name, System.currentTimeMillis());
            }
        }
    }
}
