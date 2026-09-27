package dev.duskbyte.module.modules.combat;

import dev.duskbyte.DuskByte;
import dev.duskbyte.event.events.AttackListener;
import dev.duskbyte.event.events.TickListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.modules.client.Friends;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.MinMaxSetting;
import dev.duskbyte.module.setting.NumberSetting;
import dev.duskbyte.utils.MouseSimulation;
import dev.duskbyte.utils.TimerUtils;
import dev.duskbyte.utils.WorldUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class TriggerBot extends Module implements TickListener, AttackListener {
	private final BooleanSetting inScreen = new BooleanSetting(Work In Screen, false)
			.setDescription(Will trigger even if youre inside a screen);
	private final BooleanSetting whileUse = new BooleanSetting(While Use, false)
			.setDescription(Will hit the player no matter if you're eating or blocking with a shield);
	private final BooleanSetting onLeftClick = new BooleanSetting(On Left Click, false)
			.setDescription(Only gets triggered if holding down left click);
	private final BooleanSetting allItems = new BooleanSetting(All Items, false)
			.setDescription(Works with all Items /THIS USES SWORD DELAY AS THE DELAY/);
	private final MinMaxSetting swordDelay = new MinMaxSetting(Sword Delay, 0, 1000, 1, 540, 550)
			.setDescription(Delay for swords);
	private final MinMaxSetting axeDelay = new MinMaxSetting(Axe Delay, 0, 1000, 1, 780, 800)
			.setDescription(Delay for axes);
	/*private final NumberSetting swordDelay = new NumberSetting(Sword Delay, 0, 1000, 550, 1)
			.setDescription(Delay for swords);*/
	/*private final NumberSetting axeDelay = new NumberSetting(Axe Delay, 0, 1000, 800, 1)
			.setDescription(Delay for axes);*/
	private final BooleanSetting checkShield = new BooleanSetting(Check Shield, false)
			.setDescription(Checks if the player is blocking your hits with a shield (Recommended with Shield Disabler));
	private final BooleanSetting onlyCritSword = new BooleanSetting(Only Crit Sword, false)
			.setDescription(Only does critical hits with a sword);
	private final BooleanSetting onlyCritAxe = new BooleanSetting(Only Crit Axe, false)
			.setDescription(Only does critical hits with an axe);
	private final BooleanSetting swing = new BooleanSetting(Swing Hand, true)
			.setDescription(Whether to swing the hand or not);
	private final BooleanSetting whileAscend = new BooleanSetting(While Ascending, false)
			.setDescription(Wont hit if you're ascending from a jump, only if on ground or falling);
	private final BooleanSetting clickSimulation = new BooleanSetting(Click Simulation, false)
			.setDescription(Makes the CPS hud think you're legit);
	private final BooleanSetting strayBypass = new BooleanSetting(Stray Bypass, false)
			.setDescription(Bypasses stray's Anti-TriggerBot);
	private final BooleanSetting allEntities = new BooleanSetting(All Entities, false)
			.setDescription(Will attack all entities);
	private final BooleanSetting useShield = new BooleanSetting(Use Shield, false)
			.setDescription(Uses shield if it's in your offhand);
	private final NumberSetting shieldTime = new NumberSetting(Shield Time, 100, 1000, 350, 1);
	private final BooleanSetting sticky = new BooleanSetting(Same Player, false)
			.setDescription(Hits the player that was recently attacked, good for FFA);
	private final TimerUtils timer = new TimerUtils();

	private int currentSwordDelay, currentAxeDelay;

	public TriggerBot() {
		super(Trigger Bot,
				Automatically hits players for you,
				-1,
				Category.COMBAT);
		addSettings(inScreen, whileUse, onLeftClick, allItems, swordDelay, axeDelay, checkShield, whileAscend, sticky, onlyCritSword, onlyCritAxe, swing, clickSimulation, strayBypass, allEntities, useShield, shieldTime);
	}

	@Override
	public void onEnable() {
		currentSwordDelay = swordDelay.getRandomValueInt();
		currentAxeDelay = axeDelay.getRandomValueInt();

		eventManager.add(TickListener.class, this);
		eventManager.add(AttackListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(AttackListener.class, this);
		super.onDisable();
	}

	@SuppressWarnings("all")
	@Override
	public void onTick() {
		try {
			if (!inScreen.getValue() && mc.currentScreen != null)
				return;

			if(DuskByte.INSTANCE.getModuleManager().getModule(Friends.class).antiAttack.getValue() && DuskByte.INSTANCE.getFriendManager().isAimingOverFriend())
				return;

			Item item = mc.player.getMainHandStack().getItem();

			if (onLeftClick.getValue() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
				return;

			if (((mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD) || mc.player.getOffHandStack().getItem() instanceof ShieldItem) && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) && !whileUse.getValue())
				return;
			
			if (!whileAscend.getValue() && ((!mc.player.isOnGround() && mc.player.getVelocity().y > 0) || (!mc.player.isOnGround() && mc.player.fallDistance <= 0.0F)))
				return;

			if (!allItems.getValue()) {
				if (item instanceof SwordItem) {
					if (mc.crosshairTarget instanceof EntityHitResult hit) {
						Entity entity = hit.getEntity();

						assert mc.player.getAttacking() != null;
						if (sticky.getValue() && entity != mc.player.getAttacking())
							return;

						if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {

							if (entity instanceof PlayerEntity player) {
								if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
									return;
							}

							if (onlyCritSword.getValue() && mc.player.fallDistance <= 0.0F)
								return;

							if (timer.delay(currentSwordDelay)) {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD && mc.player.isBlocking())
										MouseSimulation.mouseRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
								}

								WorldUtils.hitEntity(entity, swing.getValue());

								if (clickSimulation.getValue())
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

								currentSwordDelay = swordDelay.getRandomValueInt();
								timer.reset();
							} else {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
										int useFor = shieldTime.getValueInt();
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
									}
								}
							}
						}
					}
				} else if (item instanceof AxeItem) {
					if (mc.crosshairTarget instanceof EntityHitResult hit) {
						Entity entity = hit.getEntity();

						if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {
							if (entity instanceof PlayerEntity player) {
								if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
									return;
							}

							if (onlyCritAxe.getValue() && mc.player.fallDistance <= 0.0F)
								return;

							if (timer.delay(currentAxeDelay)) {
								WorldUtils.hitEntity(entity, swing.getValue());

								if (clickSimulation.getValue())
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

								currentAxeDelay = axeDelay.getRandomValueInt();
								timer.reset();
							} else {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
										int useFor = shieldTime.getValueInt();
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
									}
								}
							}
						}
					}
				}
			} else {
				if (mc.crosshairTarget instanceof EntityHitResult entityHit && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
					Entity entity = entityHit.getEntity();

					assert mc.player.getAttacking() != null;
					if (sticky.getValue() && entity != mc.player.getAttacking())
						return;

					if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {
						if (entity instanceof PlayerEntity player) {
							if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
								return;
						}

						if (onlyCritSword.getValue() && mc.player.fallDistance <= 0.0F)
							return;

						if (timer.delay(currentSwordDelay)) {
							WorldUtils.hitEntity(entity, swing.getValue());

							if (clickSimulation.getValue())
								MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

							currentSwordDelay = swordDelay.getRandomValueInt();
							timer.reset();
						} else {
							if (useShield.getValue()) {
								if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
									int useFor = shieldTime.getValueInt();
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
								}
							}
						}
					}
				}
			}
		} catch (Exception ignored) {}
	}

	@Override
	public void onAttack(AttackEvent event) {
		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
			event.cancel();
	}
}
