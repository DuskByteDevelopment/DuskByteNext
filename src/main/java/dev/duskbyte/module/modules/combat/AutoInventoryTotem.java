package dev.duskbyte.module.modules.combat;

import dev.duskbyte.event.events.TickListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.ModeSetting;
import dev.duskbyte.module.setting.NumberSetting;
import dev.duskbyte.utils.FakeInvScreen;
import dev.duskbyte.utils.InventoryUtils;
import dev.duskbyte.utils.TimerUtils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;


public final class AutoInventoryTotem extends Module implements TickListener {
	public enum Mode {
		Blatant, Random
	}

	private final ModeSetting<Mode> mode = new ModeSetting<>("Mode", Mode.Blatant, Mode.class)
			.setDescription("Whether to randomize the toteming pattern or no");
	private final NumberSetting delay = new NumberSetting("Delay", 0, 20, 0, 1);
	private final BooleanSetting hotbar = new BooleanSetting("Hotbar", true).setDescription("Puts a totem in your hotbar as well, if enabled (Setting below will work if this is enabled)");
	private final NumberSetting totemSlot = new NumberSetting("Totem Slot", 1, 9, 1, 1)
			.setDescription("Your preferred totem slot");
	private final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", false)
			.setDescription("Switches to totem slot when going inside the inventory");
	private final BooleanSetting forceTotem = new BooleanSetting("Force Totem", false).setDescription("Puts the totem in the slot, regardless if its space is taken up by something else");
	private final BooleanSetting autoOpen = new BooleanSetting("Auto Open", false)
			.setDescription("Automatically opens and closes the inventory for you");
	private final NumberSetting stayOpenFor = new NumberSetting("Stay Open For", 0, 20, 0, 1);

	int clock = -1;
	int closeClock = -1;

	TimerUtils openTimer = new TimerUtils();
	TimerUtils closeTimer = new TimerUtils();

	public AutoInventoryTotem() {
		super("Auto Inventory Totem",
				"Automatically equips a totem in your offhand and main hand if empty",
				-1,
				Category.COMBAT);
		addSettings(mode, delay, hotbar, totemSlot, autoSwitch, forceTotem, autoOpen, stayOpenFor);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		clock = -1;
		closeClock = -1;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (shouldOpenScreen() && autoOpen.getValue())
			mc.setScreen(new FakeInvScreen(mc.player));

		if (!(mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof FakeInvScreen)) {
			clock = -1;
			closeClock = -1;
			return;
		}

		if (clock == -1)
			clock = delay.getValueInt();

		if (closeClock == -1)
			closeClock = stayOpenFor.getValueInt();

		if (clock > 0)
			clock--;

		PlayerInventory inventory = mc.player.getInventory();

		if (autoSwitch.getValue())
			inventory.selectedSlot = totemSlot.getValueInt() - 1;

		if (clock <= 0) {
			if (inventory.offHand.get(0).getItem() != Items.TOTEM_OF_UNDYING) {
				int slot = mode.isMode(Mode.Blatant) ? InventoryUtils.findTotemSlot() : InventoryUtils.findRandomTotemSlot();

				if (slot != -1) {
					mc.interactionManager.clickSlot(((InventoryScreen) mc.currentScreen).getScreenHandler().syncId, slot, 40, SlotActionType.SWAP, mc.player);
					return;
				}
			}

			if(hotbar.getValue()) {
				ItemStack mainHand = mc.player.getMainHandStack();
				if (mainHand.isEmpty() || forceTotem.getValue() && mainHand.getItem() != Items.TOTEM_OF_UNDYING) {
					int slot = mode.isMode(Mode.Blatant) ? InventoryUtils.findTotemSlot() : InventoryUtils.findRandomTotemSlot();

					if (slot != -1) {
						mc.interactionManager.clickSlot(((InventoryScreen) mc.currentScreen).getScreenHandler().syncId, slot, inventory.selectedSlot, SlotActionType.SWAP, mc.player);
						return;
					}
				}
			}


			if (shouldCloseScreen() && autoOpen.getValue()) {
				if (closeClock != 0) {
					closeClock--;
					return;
				}

				mc.currentScreen.close();
				closeClock = stayOpenFor.getValueInt();
			}
		}
	}

	public boolean shouldCloseScreen() {
		if(hotbar.getValue())
			return (mc.player.getInventory().getStack(totemSlot.getValueInt() - 1).getItem() == Items.TOTEM_OF_UNDYING && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && mc.currentScreen instanceof FakeInvScreen;
		else return ( mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && mc.currentScreen instanceof FakeInvScreen;
	}

	public boolean shouldOpenScreen() {
		if(hotbar.getValue())
			return (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || mc.player.getInventory().getStack(totemSlot.getValueInt() - 1).getItem() != Items.TOTEM_OF_UNDYING)
					&& !(mc.currentScreen instanceof FakeInvScreen) && InventoryUtils.countItemExceptHotbar(item -> item == Items.TOTEM_OF_UNDYING) != 0;
		else return (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING && !(mc.currentScreen instanceof FakeInvScreen) && InventoryUtils.countItemExceptHotbar(item -> item == Items.TOTEM_OF_UNDYING) != 0);
	}
}
