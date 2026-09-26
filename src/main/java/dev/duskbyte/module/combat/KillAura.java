
package dev.duskbyte.module.combat;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.setting.BoolSetting;
import dev.duskbyte.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 3.5, 1.0, 6.0, 0.1));
    private final NumberSetting cps = add(new NumberSetting("CPS", 10, 1, 20, 1));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", true));
    private long lastAttack = 0;

    public KillAura() {
        super("KillAura", "Attacks nearby entities", Category.COMBAT, GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        Entity target = findTarget();
        if (target == null) return;
        if (rotate.get()) lookAt(target);
        long now = System.currentTimeMillis();
        if (now - lastAttack >= 1000.0 / cps.get()) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = now;
            ModuleManager.attack(target);
        }
    }

    private Entity findTarget() {
        Entity best = null;
        double bestDist = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive()) continue;
            if (!isValidTarget(e)) continue;
            double dist = mc.player.distanceTo(e);
            if (dist < bestDist) { bestDist = dist; best = e; }
        }
        return best;
    }

    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity && players.get()) return true;
        if (e instanceof Monster && mobs.get()) return true;
        if (e instanceof AnimalEntity && animals.get()) return true;
        return false;
    }

    private void lookAt(Entity target) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d pos = target.getPos().add(0, target.getStandingEyeHeight() * 0.5, 0);
        Vec3d diff = pos.subtract(eye);
        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
