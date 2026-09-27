package dev.duskbyte.module.modules.combat;

import dev.duskbyte.event.events.HudListener;
import dev.duskbyte.event.events.PacketSendListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.MinMaxSetting;
import dev.duskbyte.module.setting.NumberSetting;
import dev.duskbyte.utils.TimerUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class AutoWTap extends Module implements PacketSendListener, HudListener {
	private final MinMaxSetting delay = new MinMaxSetting(Delay, 0, 1000, 1,230, 270);
	private final BooleanSetting inAir = new BooleanSetting(In Air, false)
			.setDescription(Whether it should W tap in air);
	private final TimerUtils sprintTimer = new TimerUtils();
	private final TimerUtils tapTimer = new TimerUtils();
	private boolean holdingForward;
	private boolean sprinting;
	private int currentDelay;
	private boolean jumpedWhileHitting;

	public AutoWTap() {
		super(Auto WTap,
				Automatically W Taps for you so the opponent takes more knockback,
				-1,
				Category.COMBAT);
		addSettings(delay, inAir);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketSendListener.class, this);
		eventManager.add(HudListener.class, this);
		currentDelay = delay.getRandomValueInt();
		jumpedWhileHitting = false;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketSendListener.class, this);
		eventManager.remove(HudListener.class, this);
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) != 1) {
			sprinting = false;
			holdingForward = false;
			return;
		}

		if (!inAir.getValue() && !mc.player.isOnGround())
			return;

		if (mc.player.isOnGround()) {
			jumpedWhileHitting = false;
		}

		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == 1 && !inAir.getValue()) {
			if (holdingForward || sprinting) {
				mc.options.forwardKey.setPressed(true);
				holdingForward = false;
				sprinting = false;
				return;
			}
		}

		if (holdingForward && tapTimer.delay(1)) {
			mc.options.forwardKey.setPressed(false);
			sprintTimer.reset();
			sprinting = true;
			holdingForward = false;
		}

		if (sprinting && sprintTimer.delay(currentDelay)) {
			mc.options.forwardKey.setPressed(true);
			sprinting = false;
			currentDelay = delay.getRandomValueInt();
		}
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (!(event.packet instanceof PlayerInteractEntityC2SPacket packet))
			return;

		packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
			@Override
			public void interact(Hand hand) {
			}

			@Override
			public void interactAt(Hand hand, Vec3d pos) {
			}

			@Override
			public void attack() {
				if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == 1 && !inAir.getValue()) {
					jumpedWhileHitting = true;
				}

				if (!inAir.getValue() && !mc.player.isOnGround())
					return;

				if (!jumpedWhileHitting && mc.options.forwardKey.isPressed() && mc.player.isSprinting()) {
					sprintTimer.reset();
					holdingForward = true;
				}
			}
		});
	}
}