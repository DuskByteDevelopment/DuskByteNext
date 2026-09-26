
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.setting.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 3.5, 1.0, 6.0, 0.1));
    private final NumberSetting cps = add(new NumberSetting("CPS", 10, 1, 20, 1));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private long lastAttack = 0;

    public TriggerBot() {
        super("TriggerBot", "Attacks entity in crosshair", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.targetedEntity == null || !(mc.targetedEntity instanceof LivingEntity target)) return;
        if (!target.isAlive()) return;

        // Check if valid target
        if (!isValidTarget(target)) return;

        // Check range
        if (mc.player.distanceTo(target) > range.get()) return;

        // CPS check
        long now = System.currentTimeMillis();
        if (now - lastAttack < 1000.0 / cps.get()) return;

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
        ModuleManager.attack(target);
    }

    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity && players.get()) return true;
        if (e instanceof Monster && mobs.get()) return true;
        if (e instanceof AnimalEntity && animals.get()) return true;
        return false;
    }
}
