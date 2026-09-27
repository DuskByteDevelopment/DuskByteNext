package dev.duskbyte.module.modules.combat;

import dev.duskbyte.event.events.ItemUseListener;
import dev.duskbyte.event.events.TickListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.KeybindSetting;
import dev.duskbyte.module.setting.NumberSetting;
import dev.duskbyte.utils.*;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public final class AutoCrystal extends Module implements TickListener, ItemUseListener {
	private final KeybindSetting activateKey = new KeybindSetting("Activate Key", GLFW.GLFW_MOUSE_BUTTON_RIGHT, false)
			.setDescription("Key that does the crystalling");

	private final NumberSetting placeDelay = new NumberSetting("Place Delay", 0, 20, 0, 1);
	private final NumberSetting breakDelay = new NumberSetting("Break Delay", 0, 20, 0, 1);

	private final NumberSetting placeChance = new NumberSetting("Place Chance", 0, 100, 100, 1)
			.setDescription("Randomization");
	private final NumberSetting breakChance = new NumberSetting("Break Chance", 0, 100, 100, 1)
			.setDescription("Randomization");

	private final BooleanSetting stopOnKill = new BooleanSetting("Stop on Kill", false)
			.setDescription("Won't crystal if a dead player is nearby");
	private final BooleanSetting fakePunch = new BooleanSetting("Fake Punch", false)
			.setDescription("Will hit every entity and block if you miss a hitcrystal");
	private final BooleanSetting clickSimulation = new BooleanSetting("Click Simulation", false)
			.setDescription("Makes the CPS hud think you're legit");
	private final BooleanSetting damageTick = new BooleanSetting("Damage Tick", false)
			.setDescription("Times your crystals for a perfect d-tap");
	private final BooleanSetting antiWeakness = new BooleanSetting("Anti-Weakness", false)
			.setDescription("Silently switches to a sword and then hits the crystal if you have weakness");

	private final NumberSetting particleChance = new NumberSetting("Particle Chance", 0, 100, 20, 1)
			.setDescription("Adds block breaking particles to make it seem more legit from your POV (Only works with fake punch)");

	private int placeClock;
	private int breakClock;
	public boolean crystalling;

	public AutoCrystal() {
		super("Auto Crystal",
				"Automatically crystals fast for you",
				-1,
				Category.COMBAT);
		addSettings(activateKey, placeDelay, breakDelay, placeChance, breakChance, stopOnKill, fakePunch, clickSimulation, damageTick, antiWeakness, particleChance);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		eventManager.add(ItemUseListener.class, this);

		placeClock = 0;
		breakClock = 0;
		crystalling = false;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(ItemUseListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null)
			return;

		boolean dontPlace = (placeClock != 0);
		boolean dontBreak = (breakClock != 0);

		if (stopOnKill.getValue() && WorldUtils.isDeadBodyNearby())
			return;

		int randomInt = MathUtils.randomInt(1, 100);

		if (dontPlace)
			placeClock--;

		if (dontBreak)
			breakClock--;

		if (mc.player.isUsingItem())
			return;

		if (damageTick.getValue() && damageTickCheck())
			return;

		if (activateKey.getKey() != -1 && !KeyUtils.isKeyPressed(activateKey.getKey())) {
			placeClock = 0;
			breakClock = 0;
			crystalling = false;
			return;
		} else crystalling = true;

		if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL)
			return;

		if (mc.crosshairTarget instanceof BlockHitResult hit) {
			if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {

				if (!dontPlace && randomInt <= placeChance.getValueInt()) {
					if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.OBSIDIAN) || BlockUtils.isBlock(hit.getBlockPos(), Blocks.BEDROCK) && CrystalUtils.canPlaceCrystalClientAssumeObsidian(hit.getBlockPos())) {

						if (clickSimulation.getValue())
							MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

						WorldUtils.placeBlock(hit, true);

						if (fakePunch.getValue()) {
							if (randomInt <= particleChance.getValue())
								if (CrystalUtils.canPlaceCrystalClientAssumeObsidian(hit.getBlockPos()) && hit.getSide() == Direction.UP)
									mc.particleManager.addBlockBreakingParticles(hit.getBlockPos(), hit.getSide());
						}

						placeClock = placeDelay.getValueInt();
					}
				}

				if (fakePunch.getValue()) {
					if (!dontBreak && randomInt <= breakChance.getValueInt()) {

						if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.OBSIDIAN) || BlockUtils.isBlock(hit.getBlockPos(), Blocks.BEDROCK))
							return;

						if (clickSimulation.getValue()) {
							if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.OBSIDIAN) || BlockUtils.isBlock(hit.getBlockPos(), Blocks.BEDROCK)) {
								if (CrystalUtils.canPlaceCrystalClientAssumeObsidian(hit.getBlockPos()))
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
							} else MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
						}

						mc.interactionManager.attackBlock(hit.getBlockPos(), hit.getSide());
						mc.player.swingHand(Hand.MAIN_HAND);
						mc.particleManager.addBlockBreakingParticles(hit.getBlockPos(), hit.getSide());
						mc.interactionManager.updateBlockBreakingProgress(hit.getBlockPos(), hit.getSide());

						breakClock = breakDelay.getValueInt();
					}

					if (!dontPlace && randomInt <= placeChance.getValueInt() && dontBreak) {
						if (clickSimulation.getValue())
							MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
					}
				}
			}

			if (mc.crosshairTarget.getType() == HitResult.Type.MISS) {
				if (fakePunch.getValue()) {
					if (!dontBreak && randomInt <= breakChance.getValueInt()) {
						if (mc.interactionManager.hasLimitedAttackSpeed())
							mc.attackCooldown = 10;

						if (clickSimulation.getValue())
							MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

						mc.player.resetLastAttackedTicks();
						mc.player.swingHand(Hand.MAIN_HAND);

						breakClock = breakDelay.getValueInt();
					}

					if (!dontPlace && randomInt <= placeChance.getValueInt() && dontBreak) {
						if (clickSimulation.getValue())
							MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
					}
				}
			}
		}

		randomInt = MathUtils.randomInt(1, 100);

		if (mc.crosshairTarget instanceof EntityHitResult hit) {
			if (!dontBreak && randomInt <= breakChance.getValueInt()) {
				Entity entity = hit.getEntity();

				if (!fakePunch.getValue() && !(entity instanceof EndCrystalEntity || entity instanceof SlimeEntity))
					return;

				int previousSlot = mc.player.getInventory().selectedSlot;

				if(entity instanceof EndCrystalEntity || entity instanceof SlimeEntity)
					if(antiWeakness.getValue() && cantBreakCrystal())
						InventoryUtils.selectSword();

				if (clickSimulation.getValue())
					MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

				WorldUtils.hitEntity(entity, true);
				breakClock = breakDelay.getValueInt();

				if(antiWeakness.getValue())
					InventoryUtils.setInvSlot(previousSlot);
			}
		}
	}

	@Override
	public void onItemUse(ItemUseEvent event) {
		if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
			if ((mc.crosshairTarget instanceof BlockHitResult h
					&& mc.crosshairTarget.getType() == HitResult.Type.BLOCK
					&& (BlockUtils.isBlock(h.getBlockPos(), Blocks.OBSIDIAN) || BlockUtils.isBlock(h.getBlockPos(), Blocks.BEDROCK)))) {
				event.cancel();
			}
		}
	}

	private boolean cantBreakCrystal() {
        assert mc.player != null;
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
		StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
		return (!(weakness == null || strength != null && strength.getAmplifier() > weakness.getAmplifier() || WorldUtils.isTool(mc.player.getMainHandStack())));
	}

	private boolean damageTickCheck() {
		return mc.world.getPlayers().parallelStream()
				.filter(e -> e != mc.player)
				.filter(e -> e.squaredDistanceTo(mc.player) < 36)
				.filter(e -> e.getLastAttacker() == null)
				.filter(e -> !e.isOnGround())
				.anyMatch(e -> e.hurtTime >= 2)

				&& !(mc.player.getAttacking() instanceof PlayerEntity);
	}
}