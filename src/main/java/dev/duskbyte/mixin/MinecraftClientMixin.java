package dev.duskbyte.mixin;

import dev.duskbyte.DuskByte;
import dev.duskbyte.event.EventManager;
import dev.duskbyte.event.events.*;
import dev.duskbyte.gui.AuthScreen;
import dev.duskbyte.utils.MouseSimulation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Nullable
	public ClientWorld world;

	@Shadow
	@Final
	private Window window;

	@Shadow
	@Nullable
	public net.minecraft.client.gui.screen.Screen currentScreen;

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		MinecraftClient mc = (MinecraftClient) (Object) this;

		// 兜底登录: 正常情况登录在游戏加载前由 JDialog(AuthDialog) 完成,这里不会触发;
		// 只有 AWT headless 导致 JDialog 弹不出来时,才在游戏内强制弹 AuthScreen,
		// 直到登录成功。
		if (DuskByte.INSTANCE != null && !DuskByte.INSTANCE.authenticated) {
			if (!(mc.currentScreen instanceof AuthScreen)) {
				mc.setScreen(new AuthScreen());
			}
		}

		if (world != null) {
			TickListener.TickEvent event = new TickListener.TickEvent();

			EventManager.fire(event);
		}
	}

	@Inject(method = "onResolutionChanged", at = @At("HEAD"))
	private void onResolutionChanged(CallbackInfo ci) {
		EventManager.fire(new ResolutionListener.ResolutionEvent(this.window));
	}

	@Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
	private void onItemUse(CallbackInfo ci) {
		ItemUseListener.ItemUseEvent event = new ItemUseListener.ItemUseEvent();

		EventManager.fire(event);
		if (event.isCancelled()) ci.cancel();

		if (MouseSimulation.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			MouseSimulation.mouseButtons.put(GLFW.GLFW_MOUSE_BUTTON_RIGHT, false);
			ci.cancel();
		}
	}

	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void onAttack(CallbackInfoReturnable<Boolean> cir) {
		AttackListener.AttackEvent event = new AttackListener.AttackEvent();

		EventManager.fire(event);
		if (event.isCancelled()) cir.setReturnValue(false);

		if (MouseSimulation.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
			MouseSimulation.mouseButtons.put(GLFW.GLFW_MOUSE_BUTTON_1, false);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void onBlockBreaking(boolean breaking, CallbackInfo ci) {
		BlockBreakingListener.BlockBreakingEvent event = new BlockBreakingListener.BlockBreakingEvent();

		EventManager.fire(event);
		if (event.isCancelled()) ci.cancel();

		if (MouseSimulation.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
			MouseSimulation.mouseButtons.put(GLFW.GLFW_MOUSE_BUTTON_1, false);
			ci.cancel();
		}
	}

	@Inject(method = "stop", at = @At("HEAD"))
	private void onClose(CallbackInfo ci) {
		DuskByte.INSTANCE.getProfileManager().saveProfile();
	}
}
